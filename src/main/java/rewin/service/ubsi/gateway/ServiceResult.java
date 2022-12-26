package rewin.service.ubsi.gateway;

import com.mongodb.ReadPreference;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import rewin.ubsi.annotation.USEntry;
import rewin.ubsi.annotation.USParam;
import rewin.ubsi.common.MongoUtil;
import rewin.ubsi.common.Util;
import rewin.ubsi.container.ServiceContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓冲及仿真设置接口
 */
public class ServiceResult extends ServiceFlow {

    @USEntry(
            tips = "设置服务接口的缓冲",
            params = {
                    @USParam(name = "id", tips = "缓冲设置的记录ID，0表示新增"),
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "service", tips = "服务名字，末尾的\"*\"表示通配符"),
                    @USParam(name = "entry", tips = "接口名字，末尾的\"*\"表示通配符"),
                    @USParam(name = "timeout", tips = "缓冲数据的超时时间(秒)"),
                    @USParam(name = "enabled", tips = "本条策略是否可用"),
            },
            readonly = false,
            result = "数据项ID"
    )
    public int setCache(ServiceContext ctx, int id, String group, String app, String service, String entry, int timeout, boolean enabled) throws Exception {
        DB.Cache cache = new DB.Cache();
        cache.group = Util.checkEmpty(group);
        cache.app = Util.checkEmpty(app);
        cache.service = Util.checkEmpty(service);
        cache.entry = Util.checkEmpty(entry);
        cache.timeout = timeout;
        cache.enabled = enabled;
        cache._id = id == 0 ? Util.hash(new ObjectId().toHexString()) : id;
        MongoCollection<DB.Cache> col = Service.MongoDBInst.getCollection(DB.COL_CACHE, DB.Cache.class);
        col.replaceOne(new Document("_id", cache._id), cache, new ReplaceOptions().upsert(true));

        Service.publish(ctx, group, Service.CACHE);
        return cache._id;
    }

    @USEntry(
            tips = "删除服务接口的缓冲",
            params = {
                    @USParam(name = "id", tips = "缓冲设置的记录ID"),
            },
            readonly = false
    )
    public void delCache(ServiceContext ctx, int id) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_CACHE);
        Document q = new Document("_id", id);
        Document doc = (Document)col.find(q).first();
        if ( doc != null ) {
            col.deleteOne(q);
            Service.publish(ctx, doc.getString("group"), Service.CACHE);
        }
    }

    @USEntry(
            tips = "查询服务接口的缓冲设置",
            params = {
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "service", tips = "服务名字，null表示不限"),
                    @USParam(name = "enabled", tips = "是否可用，null表示不限"),
                    @USParam(name = "sortby", tips = "排序方式，格式：[\"field\", 1(升序)|-1(降序), ...]"),
                    @USParam(name = "skip", tips = "跳过的数量"),
                    @USParam(name = "limit", tips = "返回的数量，0表示不限")
            },
            result = "格式：[ 总数量(long), 缓冲设置的列表(List<Map>) ]"
    )
    public Object[] listCache(ServiceContext ctx, String group, String app, String service, Boolean enabled, List sortby, int skip, int limit) throws Exception {
        Document q = new Document();
        if ( group != null )
            q.append("group", new Document("$in", Util.toList(null, group)));
        if ( app != null )
            q.append("app", app);
        if ( service != null )
            q.append("service", service);
        if ( enabled != null )
            q.append("enabled", enabled);
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_CACHE).withReadPreference(ReadPreference.primary());
        return new Object[] {
                col.countDocuments(q),
                MongoUtil.query(col, q, sortby == null ? null : Util.toBson(sortby.toArray()), skip, limit, null)
        };
    }

    @USEntry(
            tips = "查询缓冲设置数据中的group",
            result = "缓冲设置数据中非重复的group集合"
    )
    public Set<String> listGroupInCache(ServiceContext ctx) {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_CACHE);
        return (Set)col.distinct("group", String.class).into(new HashSet());
    }

    // =====================================================================

    @USEntry(
            tips = "设置服务接口的仿真数据",
            params = {
                    @USParam(name = "id", tips = "仿真数据的记录ID，0表示新增"),
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "service", tips = "服务名字"),
                    @USParam(name = "entry", tips = "接口名字"),
                    @USParam(name = "data", tips = "仿真数据"),
                    @USParam(name = "enabled", tips = "本条策略是否可用"),
            },
            readonly = false,
            result = "数据项ID"
    )
    public int setMock(ServiceContext ctx, int id, String group, String app, String service, String entry, Object data, boolean enabled) throws Exception {
        Document doc = new Document();
        doc.append("group", Util.checkEmpty(group));
        doc.append("app", Util.checkEmpty(app));
        service = Util.checkEmpty(service);
        entry = Util.checkEmpty(entry);
        if ( service == null || entry == null )
            throw new Exception("invalid service or entry");
        doc.append("service", service);
        doc.append("entry", entry);
        doc.append("data", data);
        doc.append("enabled", enabled);
        if ( id == 0 )
            id = Util.hash(new ObjectId().toHexString());
        doc.append("_id", id);
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_MOCK);
        col.replaceOne(new Document("_id", id), doc, new ReplaceOptions().upsert(true));

        Service.publish(ctx, group, Service.MOCK);
        return id;
    }

    @USEntry(
            tips = "删除服务接口的仿真数据",
            params = {
                    @USParam(name = "id", tips = "仿真数据的记录ID"),
            },
            readonly = false
    )
    public void delMock(ServiceContext ctx, int id) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_MOCK);
        Document q = new Document("_id", id);
        Document doc = (Document)col.find(q).first();
        if ( doc != null ) {
            col.deleteOne(q);
            Service.publish(ctx, doc.getString("group"), Service.MOCK);
        }
    }

    @USEntry(
            tips = "查询服务接口的仿真数据",
            params = {
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "service", tips = "服务名字，null表示不限"),
                    @USParam(name = "enabled", tips = "是否可用，null表示不限"),
                    @USParam(name = "sortby", tips = "排序方式，格式：[\"field\", 1(升序)|-1(降序), ...]"),
                    @USParam(name = "skip", tips = "跳过的数量"),
                    @USParam(name = "limit", tips = "返回的数量，0表示不限")
            },
            result = "格式：[ 总数量(long), 缓冲设置的列表(List<Map>) ]"
    )
    public Object[] listMock(ServiceContext ctx, String group, String app, String service, Boolean enabled, List sortby, int skip, int limit) throws Exception {
        Document q = new Document();
        if ( group != null )
            q.append("group", new Document("$in", Util.toList(null, group)));
        if ( app != null )
            q.append("app", app);
        if ( service != null )
            q.append("service", service);
        if ( enabled != null )
            q.append("enabled", enabled);
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_MOCK).withReadPreference(ReadPreference.primary());
        return new Object[] {
                col.countDocuments(q),
                MongoUtil.query(col, q, sortby == null ? null : Util.toBson(sortby.toArray()), skip, limit, null)
        };
    }

    @USEntry(
            tips = "查询仿真数据设置中的group",
            result = "仿真数据设置中非重复的group集合"
    )
    public Set<String> listGroupInMock(ServiceContext ctx) {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_MOCK);
        return (Set)col.distinct("group", String.class).into(new HashSet());
    }

}
