package rewin.service.ubsi.gateway;

import com.mongodb.ReadPreference;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import rewin.ubsi.annotation.USEntry;
import rewin.ubsi.annotation.USParam;
import rewin.ubsi.common.JedisUtil;
import rewin.ubsi.common.MongoUtil;
import rewin.ubsi.common.Util;
import rewin.ubsi.container.ServiceContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 访问权限管理接口
 */
public class ServiceAcl extends ServiceLog {

    @USEntry(
            tips = "设置一个远程主机的访问权限",
            params = {
                    @USParam(name = "id", tips = "权限的记录ID，0表示新增"),
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "remote", tips = "远程主机的地址，null表示不限"),
                    @USParam(name = "apps", tips = "指定的应用，null表示不限"),
                    @USParam(name = "auth", tips = "是否允许访问"),
                    @USParam(name = "enabled", tips = "本条策略是否可用"),
            },
            readonly = false,
            result = "数据项ID"
    )
    public int setRAcl(ServiceContext ctx, int id, String group, String remote, Set<String> apps, boolean auth, boolean enabled) throws Exception {
        DB.RAcl acl = new DB.RAcl();
        acl.group = Util.checkEmpty(group);
        acl.remote = Util.checkEmpty(remote);
        acl.apps = apps;
        acl.auth = auth;
        acl.enabled = enabled;
        acl._id = id == 0 ? Util.hash(new ObjectId().toHexString()) : id;
        MongoCollection<DB.RAcl> col = Service.MongoDBInst.getCollection(DB.COL_RACL, DB.RAcl.class);
        col.replaceOne(new Document("_id", acl._id), acl, new ReplaceOptions().upsert(true));

        Service.publish(ctx, acl.group, Service.RACL);
        return acl._id;
    }

    @USEntry(
            tips = "删除一个远程主机的访问权限",
            params = {
                    @USParam(name = "id", tips = "访问权限的记录ID"),
            },
            readonly = false
    )
    public void delRAcl(ServiceContext ctx, int id) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_RACL);
        Document q = new Document("_id", id);
        Document doc = (Document)col.find(q).first();
        if ( doc != null ) {
            col.deleteOne(q);
            Service.publish(ctx, doc.getString("group"), Service.RACL);
        }
    }

    @USEntry(
            tips = "查询远程主机的访问权限",
            params = {
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "remote", tips = "远程主机的地址，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "enabled", tips = "是否可用，null表示不限"),
                    @USParam(name = "sortby", tips = "排序方式，格式：[\"field\", 1(升序)|-1(降序), ...]"),
                    @USParam(name = "skip", tips = "跳过的数量"),
                    @USParam(name = "limit", tips = "返回的数量，0表示不限")
            },
            result = "格式：[ 总数量(long), 远程主机访问权限的列表(List<Map>) ]"
    )
    public Object[] listRAcl(ServiceContext ctx, String group, String remote, String app, Boolean enabled, List sortby, int skip, int limit) throws Exception {
        Document q = new Document();
        if ( group != null )
            q.append("group", new Document("$in", Util.toList(null, group)));
        if ( remote != null )
            q.append("remote", remote);
        if ( app != null )
            q.append("apps", app);
        if ( enabled != null )
            q.append("enabled", enabled);
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_RACL).withReadPreference(ReadPreference.primary());
        return new Object[] {
                col.countDocuments(q),
                MongoUtil.query(col, q, sortby == null ? null : Util.toBson(sortby.toArray()), skip, limit, null)
        };
    }

    @USEntry(
            tips = "查询主机权限数据中的group",
            result = "主机权限数据中非重复的group集合"
    )
    public Set<String> listGroupInRAcl(ServiceContext ctx) {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_RACL);
        return (Set)col.distinct("group", String.class).into(new HashSet());
    }

    // =====================================================================

    @USEntry(
            tips = "设置服务接口的访问权限",
            params = {
                    @USParam(name = "id", tips = "权限的记录ID，0表示新增"),
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "service", tips = "服务名字，末尾的\"*\"表示通配符"),
                    @USParam(name = "entry", tips = "接口名字，末尾的\"*\"表示通配符"),
                    @USParam(name = "auth", tips = "是否允许访问"),
                    @USParam(name = "enabled", tips = "本条策略是否可用"),
            },
            readonly = false,
            result = "数据项ID"
    )
    public int setSAcl(ServiceContext ctx, int id, String group, String app, String service, String entry, boolean auth, boolean enabled) throws Exception {
        DB.SAcl acl = new DB.SAcl();
        acl.group = Util.checkEmpty(group);
        acl.app = Util.checkEmpty(app);
        acl.service = Util.checkEmpty(service);
        acl.entry = Util.checkEmpty(entry);
        acl.auth = auth;
        acl.enabled = enabled;
        acl._id = id == 0 ? Util.hash(new ObjectId().toHexString()) : id;
        MongoCollection<DB.SAcl> col = Service.MongoDBInst.getCollection(DB.COL_SACL, DB.SAcl.class);
        col.replaceOne(new Document("_id", acl._id), acl, new ReplaceOptions().upsert(true));

        Service.publish(ctx, group, Service.SACL);
        return acl._id;
    }

    @USEntry(
            tips = "删除一个服务接口的访问权限",
            params = {
                    @USParam(name = "id", tips = "访问权限的记录ID"),
            },
            readonly = false
    )
    public void delSAcl(ServiceContext ctx, int id) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_SACL);
        Document q = new Document("_id", id);
        Document doc = (Document)col.find(q).first();
        if ( doc != null ) {
            col.deleteOne(q);
            Service.publish(ctx, doc.getString("group"), Service.SACL);
        }
    }

    @USEntry(
            tips = "查询服务接口的访问权限",
            params = {
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "enabled", tips = "是否可用，null表示不限"),
                    @USParam(name = "sortby", tips = "排序方式，格式：[\"field\", 1(升序)|-1(降序), ...]"),
                    @USParam(name = "skip", tips = "跳过的数量"),
                    @USParam(name = "limit", tips = "返回的数量，0表示不限")
            },
            result = "格式：[ 总数量(long), 服务接口访问权限的列表(List<Map>) ]"
    )
    public Object[] listSAcl(ServiceContext ctx, String group, String app, Boolean enabled, List sortby, int skip, int limit) throws Exception {
        Document q = new Document();
        if ( group != null )
            q.append("group", new Document("$in", Util.toList(null, group)));
        if ( app != null )
            q.append("app", app);
        if ( enabled != null )
            q.append("enabled", enabled);
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_SACL).withReadPreference(ReadPreference.primary());
        return new Object[] {
                col.countDocuments(q),
                MongoUtil.query(col, q, sortby == null ? null : Util.toBson(sortby.toArray()), skip, limit, null)
        };
    }

    @USEntry(
            tips = "查询服务权限数据中的group",
            result = "服务权限数据中非重复的group集合"
    )
    public Set<String> listGroupInSAcl(ServiceContext ctx) {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_SACL);
        return (Set)col.distinct("group", String.class).into(new HashSet());
    }

}
