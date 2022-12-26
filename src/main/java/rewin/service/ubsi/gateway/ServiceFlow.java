package rewin.service.ubsi.gateway;

import com.mongodb.ReadPreference;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import rewin.ubsi.annotation.USEntry;
import rewin.ubsi.annotation.USParam;
import rewin.ubsi.common.Codec;
import rewin.ubsi.common.MongoUtil;
import rewin.ubsi.common.Util;
import rewin.ubsi.container.ServiceContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 流量控制接口
 */
public class ServiceFlow {

    @USEntry(
            tips = "设置请求转发",
            params = {
                    @USParam(name = "flow", tips = "请求转发的属性，DB.Flow，\"_id\"为0表示新增"),
            },
            readonly = false,
            result = "数据项ID"
    )
    public int setFlow(ServiceContext ctx, Map flow) throws Exception {
        DB.Flow rec = Codec.toType(flow, DB.Flow.class);
        rec.group = Util.checkEmpty(rec.group);
        rec.app = Util.checkEmpty(rec.app);
        rec.service = Util.checkEmpty(rec.service);
        rec.entry = Util.checkEmpty(rec.entry);
        if ( rec._id == 0 )
            rec._id = Util.hash(new ObjectId().toHexString());
        MongoCollection<DB.Flow> col = Service.MongoDBInst.getCollection(DB.COL_FLOW, DB.Flow.class);
        col.replaceOne(new Document("_id", rec._id), rec, new ReplaceOptions().upsert(true));

        Service.publish(ctx, rec.group, Service.FLOW);
        return rec._id;
    }

    @USEntry(
            tips = "删除请求转发",
            params = {
                    @USParam(name = "id", tips = "请求转发的记录ID"),
            },
            readonly = false
    )
    public void delFlow(ServiceContext ctx, int id) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_FLOW);
        Document q = new Document("_id", id);
        Document doc = (Document)col.find(q).first();
        if ( doc != null ) {
            col.deleteOne(q);
            Service.publish(ctx, doc.getString("group"), Service.FLOW);
        }
    }

    @USEntry(
            tips = "查询请求转发",
            params = {
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "enabled", tips = "是否可用，null表示不限"),
                    @USParam(name = "sortby", tips = "排序方式，格式：[\"field\", 1(升序)|-1(降序), ...]"),
                    @USParam(name = "skip", tips = "跳过的数量"),
                    @USParam(name = "limit", tips = "返回的数量，0表示不限")
            },
            result = "格式：[ 总数量(long), 请求转发的列表(List<Map>) ]"
    )
    public Object[] listFlow(ServiceContext ctx, String group, String app, Boolean enabled, List sortby, int skip, int limit) throws Exception {
        Document q = new Document();
        if ( group != null )
            q.append("group", new Document("$in", Util.toList(null, group)));
        if ( app != null )
            q.append("app", app);
        if ( enabled != null )
            q.append("enabled", enabled);
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_FLOW).withReadPreference(ReadPreference.primary());
        return new Object[] {
                col.countDocuments(q),
                MongoUtil.query(col, q, sortby == null ? null : Util.toBson(sortby.toArray()), skip, limit, null)
        };
    }

    @USEntry(
            tips = "查询请求转发参数中的group",
            result = "请求转发参数中非重复的group集合"
    )
    public Set<String> listGroupInFlow(ServiceContext ctx) {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_FLOW);
        return (Set)col.distinct("group", String.class).into(new HashSet());
    }

    // =====================================================================

    @USEntry(
            tips = "设置限流",
            params = {
                    @USParam(name = "rate", tips = "限流设置的属性，DB.Rate，\"_id\"为0表示新增"),
            },
            readonly = false,
            result = "数据项ID"
    )
    public int setRate(ServiceContext ctx, Map rate) throws Exception {
        DB.Rate rec = Codec.toType(rate, DB.Rate.class);
        rec.group = Util.checkEmpty(rec.group);
        rec.app = Util.checkEmpty(rec.app);
        rec.service = Util.checkEmpty(rec.service);
        rec.entry = Util.checkEmpty(rec.entry);
        if ( rec._id == 0 )
            rec._id = Util.hash(new ObjectId().toHexString());
        MongoCollection<DB.Rate> col = Service.MongoDBInst.getCollection(DB.COL_RATE, DB.Rate.class);
        col.replaceOne(new Document("_id", rec._id), rec, new ReplaceOptions().upsert(true));

        Service.publish(ctx, rec.group, Service.RATE);
        return rec._id;
    }

    @USEntry(
            tips = "删除限流设置",
            params = {
                    @USParam(name = "id", tips = "限流设置的记录ID"),
            },
            readonly = false
    )
    public void delRate(ServiceContext ctx, int id) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_RATE);
        Document q = new Document("_id", id);
        Document doc = (Document)col.find(q).first();
        if ( doc != null ) {
            col.deleteOne(q);
            Service.publish(ctx, doc.getString("group"), Service.RATE);
        }
    }

    @USEntry(
            tips = "查询限流设置",
            params = {
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "enabled", tips = "是否可用，null表示不限"),
                    @USParam(name = "sortby", tips = "排序方式，格式：[\"field\", 1(升序)|-1(降序), ...]"),
                    @USParam(name = "skip", tips = "跳过的数量"),
                    @USParam(name = "limit", tips = "返回的数量，0表示不限")
            },
            result = "格式：[ 总数量(long), 请求转发的列表(List<Map>) ]"
    )
    public Object[] listRate(ServiceContext ctx, String group, String app, Boolean enabled, List sortby, int skip, int limit) throws Exception {
        Document q = new Document();
        if ( group != null )
            q.append("group", new Document("$in", Util.toList(null, group)));
        if ( app != null )
            q.append("app", app);
        if ( enabled != null )
            q.append("enabled", enabled);
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_RATE).withReadPreference(ReadPreference.primary());
        return new Object[] {
                col.countDocuments(q),
                MongoUtil.query(col, q, sortby == null ? null : Util.toBson(sortby.toArray()), skip, limit, null)
        };
    }

    @USEntry(
            tips = "查询限流设置参数中的group",
            result = "限流设置参数中非重复的group集合"
    )
    public Set<String> listGroupInRate(ServiceContext ctx) {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_RATE);
        return (Set)col.distinct("group", String.class).into(new HashSet());
    }

    // =====================================================================

    @USEntry(
            tips = "设置路由",
            params = {
                    @USParam(name = "router", tips = "路由设置的属性，DB.Router，\"_id\"为0表示新增"),
            },
            readonly = false,
            result = "数据项ID"
    )
    public int setRouter(ServiceContext ctx, Map router) throws Exception {
        DB.Router rec = Codec.toType(router, DB.Router.class);
        rec.group = Util.checkEmpty(rec.group);
        rec.app = Util.checkEmpty(rec.app);
        rec.service = Util.checkEmpty(rec.service);
        if ( rec.service == null )
            throw new Exception("invalid service");
        rec.entry = Util.checkEmpty(rec.entry);
        if ( rec._id == 0 )
            rec._id = Util.hash(new ObjectId().toHexString());
        MongoCollection<DB.Router> col = Service.MongoDBInst.getCollection(DB.COL_ROUTER, DB.Router.class);
        col.replaceOne(new Document("_id", rec._id), rec, new ReplaceOptions().upsert(true));

        Service.publish(ctx, rec.group, Service.ROUTER);
        return rec._id;
    }

    @USEntry(
            tips = "删除路由设置",
            params = {
                    @USParam(name = "id", tips = "路由设置的记录ID"),
            },
            readonly = false
    )
    public void delRouter(ServiceContext ctx, int id) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_ROUTER);
        Document q = new Document("_id", id);
        Document doc = (Document)col.find(q).first();
        if ( doc != null ) {
            col.deleteOne(q);
            Service.publish(ctx, doc.getString("group"), Service.ROUTER);
        }
    }

    @USEntry(
            tips = "查询路由设置",
            params = {
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "enabled", tips = "是否可用，null表示不限"),
                    @USParam(name = "sortby", tips = "排序方式，格式：[\"field\", 1(升序)|-1(降序), ...]"),
                    @USParam(name = "skip", tips = "跳过的数量"),
                    @USParam(name = "limit", tips = "返回的数量，0表示不限")
            },
            result = "格式：[ 总数量(long), 请求转发的列表(List<Map>) ]"
    )
    public Object[] listRouter(ServiceContext ctx, String group, String app, Boolean enabled, List sortby, int skip, int limit) throws Exception {
        Document q = new Document();
        if ( group != null )
            q.append("group", new Document("$in", Util.toList(null, group)));
        if ( app != null )
            q.append("app", app);
        if ( enabled != null )
            q.append("enabled", enabled);
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_ROUTER).withReadPreference(ReadPreference.primary());
        return new Object[] {
                col.countDocuments(q),
                MongoUtil.query(col, q, sortby == null ? null : Util.toBson(sortby.toArray()), skip, limit, null)
        };
    }

    @USEntry(
            tips = "查询路由设置参数中的group",
            result = "路由设置参数中非重复的group集合"
    )
    public Set<String> listGroupInRouter(ServiceContext ctx) {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_ROUTER);
        return (Set)col.distinct("group", String.class).into(new HashSet());
    }

}
