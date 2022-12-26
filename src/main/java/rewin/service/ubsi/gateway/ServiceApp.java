package rewin.service.ubsi.gateway;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
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
 * App管理接口
 */
public class ServiceApp extends ServiceAcl {

    @USEntry(
            tips = "注册应用",
            params = {
                    @USParam(name = "app", tips = "应用的属性(DB.App)")
            },
            readonly = false
    )
    public void regApp(ServiceContext ctx, Map app) throws Exception {
        DB.App rec = Codec.toType(app, DB.App.class);
        rec._id = Util.checkEmpty(rec._id);
        if ( rec._id == null )
            throw new Exception("_id is empty");
        rec.name = Util.checkEmpty(rec.name);
        if ( rec.name == null )
            throw new Exception("name is empty");
        long t = System.currentTimeMillis();
        if ( rec.createTime == 0 )
            rec.createTime = t;
        if ( rec.updateTime == 0 )
            rec.updateTime = t;
        if ( rec.keyTime == 0 )
            rec.keyTime = t;
        MongoCollection<DB.App> col = Service.MongoDBInst.getCollection(DB.COL_APP, DB.App.class);
        col.insertOne(rec);
    }

    @USEntry(
            tips = "注销应用",
            params = {
                    @USParam(name = "id", tips = "应用ID")
            },
            readonly = false
    )
    public void unregApp(ServiceContext ctx, String id) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_APP);
        col.deleteOne(new Document("_id", id));
    }

    @USEntry(
            tips = "修改应用",
            params = {
                    @USParam(name = "id", tips = "应用ID"),
                    @USParam(name = "app", tips = "应用的属性")
            },
            readonly = false
    )
    public void setApp(ServiceContext ctx, String id, Map app) throws Exception {
        Document set = new Document(app);
        if ( set.containsKey("key") ) {
            Object key = set.get("key");
            if ( key != null && !(key instanceof byte[]) )
                throw new Exception("key must be byte[]");
            set.put("keyTime", System.currentTimeMillis());
        } else
            set.remove("keyTime");
        set.remove("_id");
        set.remove("createTime");
        set.remove("authTime");
        set.put("updateTime", System.currentTimeMillis());
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_APP);
        col.updateOne(new Document("_id", id), new Document("$set", set));
    }

    @USEntry(
            tips = "查询应用中的所有标签",
            result = "标签的集合(Set<String>)"
    )
    public Set getAppTags(ServiceContext ctx) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_APP);
        Document q = new Document("tags.0", new Document("$exists", true));
        return (Set)col.distinct("tags", q, String.class).into(new HashSet());
    }

    @USEntry(
            tips = "查询应用",
            params = {
                    @USParam(name = "ids", tips = "应用ID")
            },
            result = "应用列表[DB.App]，按照_id升序排列"
    )
    public List<Map> getApps(ServiceContext ctx, Set<String> ids) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_APP);
        Document q = new Document("_id", new Document("$in", ids));
        return MongoUtil.query(col, q, new Document("_id", 1), 0, 0, null);
    }

    @USEntry(
            tips = "查询应用",
            params = {
                    @USParam(name = "name", tips = "应用名字(模糊查询)，null表示不限"),
                    @USParam(name = "tags", tips = "应用标签，null表示不限"),
                    @USParam(name = "status", tips = "应用状态，，0:不限，1:正常，-1:不正常"),
                    @USParam(name = "sortby", tips = "排序方式，格式：[\"field\", 1(升序)|-1(降序), ...]"),
                    @USParam(name = "skip", tips = "跳过的数量"),
                    @USParam(name = "limit", tips = "返回的数量，0表示不限")
            },
            result = "格式：[ 总数量(long), 应用的列表(List<Map>) ]"
    )
    public Object[] listApps(ServiceContext ctx, String name, Set<String> tags, int status, List sortby, int skip, int limit) throws Exception {
        Document q = new Document();
        if ( name != null )
            q.append("name", Util.fuzzyMatch(name, false, false, true));
        if ( tags != null )
            q.append("tags", new Document("$in", tags));
        if ( status != 0 )
            q.append("status", status > 0 ? 0 : new Document("$ne", 0));
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_APP);
        return new Object[] {
                col.countDocuments(q),
                MongoUtil.query(col, q, sortby == null ? null : Util.toBson(sortby.toArray()), skip, limit, null)
        };
    }

}
