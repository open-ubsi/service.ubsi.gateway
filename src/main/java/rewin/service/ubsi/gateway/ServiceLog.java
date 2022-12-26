package rewin.service.ubsi.gateway;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import rewin.ubsi.annotation.USEntry;
import rewin.ubsi.annotation.USParam;
import rewin.ubsi.common.MongoUtil;
import rewin.ubsi.common.Util;
import rewin.ubsi.container.ServiceContext;

import java.util.*;

/**
 * 日志设置接口
 */
public class ServiceLog extends ServiceResult {

    @USEntry(
            tips = "设置日志参数",
            params = {
                    @USParam(name = "id", tips = "日志参数的记录ID，0表示新增"),
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "service", tips = "服务名字，末尾的\"*\"表示通配符"),
                    @USParam(name = "entry", tips = "接口名字，末尾的\"*\"表示通配符"),
                    @USParam(name = "log", tips = "是否记录日志，0:不记录，0x01:转发微服务的请求，0x02:转发分流Gateway的请求，0x04:被cache处理的请求，0x08:被mock处理的请求，0x10:被拒绝或发生异常的请求，0x1F00表示按位对应的请求需要记录参数"),
                    @USParam(name = "trace", tips = "是否跟踪请求"),
                    @USParam(name = "enabled", tips = "本条策略是否可用"),
            },
            readonly = false,
            result = "数据项ID"
    )
    public int setLog(ServiceContext ctx, int id, String group, String app, String service, String entry, int log, boolean trace, boolean enabled) throws Exception {
        DB.Log acl = new DB.Log();
        acl.group = group;
        acl.app = app;
        acl.service = service;
        acl.entry = entry;
        acl.log = log;
        acl.trace = trace;
        acl.enabled = enabled;
        acl._id = id == 0 ? Util.hash(new ObjectId().toHexString()) : id;
        MongoCollection<DB.Log> col = Service.MongoDBInst.getCollection(DB.COL_LOG, DB.Log.class);
        col.replaceOne(new Document("_id", acl._id), acl, new ReplaceOptions().upsert(true));

        Service.publish(ctx, group, Service.LOG);
        return acl._id;
    }

    @USEntry(
            tips = "删除日志参数",
            params = {
                    @USParam(name = "id", tips = "日志参数的记录ID"),
            },
            readonly = false
    )
    public void delLog(ServiceContext ctx, int id) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_LOG);
        Document q = new Document("_id", id);
        Document doc = (Document)col.find(q).first();
        if ( doc != null ) {
            col.deleteOne(q);
            Service.publish(ctx, doc.getString("group"), Service.LOG);
        }
    }

    @USEntry(
            tips = "查询日志参数",
            params = {
                    @USParam(name = "group", tips = "网关分组ID，null表示不限"),
                    @USParam(name = "app", tips = "应用ID，null表示不限"),
                    @USParam(name = "enabled", tips = "是否可用，null表示不限"),
                    @USParam(name = "sortby", tips = "排序方式，格式：[\"field\", 1(升序)|-1(降序), ...]"),
                    @USParam(name = "skip", tips = "跳过的数量"),
                    @USParam(name = "limit", tips = "返回的数量，0表示不限")
            },
            result = "格式：[ 总数量(long), 远程主机访问权限的列表(List<Map>) ]"
    )
    public Object[] listLog(ServiceContext ctx, String group, String app, Boolean enabled, List sortby, int skip, int limit) throws Exception {
        Document q = new Document();
        if ( group != null )
            q.append("group", new Document("$in", Util.toList(null, group)));
        if ( app != null )
            q.append("app", app);
        if ( enabled != null )
            q.append("enabled", enabled);
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_LOG).withReadPreference(ReadPreference.primary());
        return new Object[] {
                col.countDocuments(q),
                MongoUtil.query(col, q, sortby == null ? null : Util.toBson(sortby.toArray()), skip, limit, null)
        };
    }

    @USEntry(
            tips = "查询日志参数中的group",
            result = "日志参数中非重复的group集合"
    )
    public Set<String> listGroupInLog(ServiceContext ctx) {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_LOG);
        return (Set)col.distinct("group", String.class).into(new HashSet());
    }

    // =====================================================================

    @USEntry(
            tips = "记录日志",
            params = {
                    @USParam(name = "log", tips = "日志的数据，DB.Logs")
            },
            readonly = false
    )
    public void addLogs(ServiceContext ctx, Map<String,Object> log) throws Exception {
        Document doc = new Document(log);
        doc.put("_id", new ObjectId().toHexString());
        String gate = (String)log.getOrDefault("gate", "");
        int index = gate.indexOf('#');
        gate = index < 0 ? "#" : gate.substring(index);
        gate = ctx.getConsumerAddress().getAddress().getHostAddress() + gate;
        doc.put("gate", gate);
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_LOGS);
        col.withWriteConcern(WriteConcern.UNACKNOWLEDGED).insertOne(doc);
    }

    @USEntry(
            tips = "删除日志",
            params = {
                    @USParam(name = "query", tips = "查询条件(MongoDB语法)，可以为null")
            },
            readonly = false
    )
    public void delLogs(ServiceContext ctx, Map query) {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_LOGS);
        col.deleteMany(query == null ? new Document() : new Document(query));
    }

    @USEntry(
            tips = "日志查询",
            params = {
                    @USParam(name = "query", tips = "查询条件(MongoDB语法)，可以为null"),
                    @USParam(name = "sort", tips = "排序方式，格式：[\"field\", 1(升序)|-1(降序), ...]"),
                    @USParam(name = "skip", tips = "跳过的记录数"),
                    @USParam(name = "limit", tips = "返回的记录数，0表示不限"),
            },
            result = "格式：[ 总数量(long), 日志列表(List<Map>) ]"
    )
    public Object[] listLogs(ServiceContext ctx, Map query, List sort, int skip, int limit) {
        Document q = query == null ? null : new Document(query);
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_LOGS);
        return new Object[] {
                col.countDocuments(q),
                MongoUtil.query(col, q, sort == null ? null : Util.toBson(sort.toArray()), skip, limit, null)
        };
    }

    @USEntry(
            tips = "日志的聚合查询",
            params = {
                    @USParam(name = "pipeline", tips = "MongoDB aggregate语法的Map列表")
            },
            result = "聚合数据列表"
    )
    public List<Map> aggregateLogs(ServiceContext ctx, List<Map> pipeline) {
        for ( int i = 0; i < pipeline.size(); i ++ )
            pipeline.set(i, new Document(pipeline.get(i)));
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_LOGS);
        return (List)col.aggregate(pipeline).into(new ArrayList());
    }

    @USEntry(
            tips = "日志的MapReduce查询",
            params = {
                    @USParam(name = "map", tips = "MongoDB的map_function"),
                    @USParam(name = "reduce", tips = "MongoDB的reduce_function")
            },
            result = "结果数据列表"
    )
    public List<Map> mapReduceLogs(ServiceContext ctx, String map, String reduce) {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_LOGS);
        return (List)col.mapReduce(map, reduce).into(new ArrayList());
    }

}
