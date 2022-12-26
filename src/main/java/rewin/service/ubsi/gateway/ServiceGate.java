package rewin.service.ubsi.gateway;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import rewin.ubsi.annotation.USEntry;
import rewin.ubsi.annotation.USParam;
import rewin.ubsi.common.MongoUtil;
import rewin.ubsi.common.Util;
import rewin.ubsi.container.ServiceContext;

import java.util.*;

/**
 * 运行实例管理接口
 */
public class ServiceGate extends ServiceApp {

    @USEntry(
            tips = "网关心跳报告",
            params = {
                    @USParam(name = "group", tips = "网关所属的分组ID"),
                    @USParam(name = "port", tips = "网关监听的服务端口号"),
                    @USParam(name = "start", tips = "网关的启动时间戳"),
                    @USParam(name = "req", tips = "请求计数，{\"service\":{\"entry\":count}}"),
                    @USParam(name = "path", tips = "网关的URL路径，缺省为/gateway", defaultValue = "'/gateway'"),
            },
            readonly = false
    )
    public void activeGate(ServiceContext ctx, String group, int port, long start, Map<String,Map<String,Long>> req, String path) throws Exception {
        DB.Gate gate = new DB.Gate();
        gate.group = group == null ? "" : group;
        gate.host = ctx.getConsumerAddress().getAddress().getHostAddress();
        gate.port = port;
        gate.path = path;
        gate.timeActive = System.currentTimeMillis();
        gate.timeStart = start;
        if ( req != null ) {
            gate.request = new HashMap<>();
            for ( String srv : req.keySet() )
                gate.request.put(srv.replaceAll("\\.", "_"), req.get(srv));
        }
        gate._id = Util.hash(group, gate.host, "" + port, path, "" + start);
        MongoCollection<DB.Gate> col = Service.MongoDBInst.getCollection(DB.COL_GATE, DB.Gate.class);
        col.replaceOne(new Document("_id", gate._id), gate, new ReplaceOptions().upsert(true));
    }

    @USEntry(
            tips = "删除网关",
            params = {
                    @USParam(name = "id", tips = "网关ID")
            },
            readonly = false
    )
    public void removeGate(ServiceContext ctx, int id) throws Exception {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_GATE);
        col.deleteOne(new Document("_id", id));
    }

    @USEntry(
            tips = "查询网关",
            params = {
                    @USParam(name = "group", tips = "网关所属的分组ID，null表示不限"),
                    @USParam(name = "sortby", tips = "排序方式，格式：[\"field\", 1(升序)|-1(降序), ...]"),
                    @USParam(name = "skip", tips = "跳过的数量"),
                    @USParam(name = "limit", tips = "返回的数量，0表示不限")
            },
            result = "格式：[ 总数量(long), 网关的列表(List<Map>) ]"
    )
    public Object[] listGates(ServiceContext ctx, String group, List sortby, int skip, int limit) throws Exception {
        // 删除过期的心跳
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_GATE);
        col.deleteMany(new Document("timeActive", new Document("$lte", System.currentTimeMillis() - 3600*1000)));

        Document q = new Document();
        if ( group != null )
            q.append("group", group);
        return new Object[] {
                col.countDocuments(q),
                MongoUtil.query(col, q, sortby == null ? null : Util.toBson(sortby.toArray()), skip, limit, null)
        };
    }

    @USEntry(
            tips = "查询网关数据中的group",
            result = "网关数据中非重复的group集合"
    )
    public Set<String> listGroupInGate(ServiceContext ctx) {
        MongoCollection col = Service.MongoDBInst.getCollection(DB.COL_GATE);
        return (Set)col.distinct("group", String.class).into(new HashSet());
    }

}
