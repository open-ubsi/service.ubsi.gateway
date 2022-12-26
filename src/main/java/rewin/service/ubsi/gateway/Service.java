package rewin.service.ubsi.gateway;

import com.mongodb.MongoClient;
import com.mongodb.client.*;
import rewin.ubsi.annotation.*;
import rewin.ubsi.common.JedisUtil;
import rewin.ubsi.common.MongoUtil;
import rewin.ubsi.common.Util;
import rewin.ubsi.container.ServiceContext;

import java.util.Map;

/**
 * UBSI网关服务：rewin.ubsi.gateway -> rewin.service.ubsi.gateway.Service
 */
@UService(
        name = "rewin.ubsi.gateway",
        tips = "UBSI网关服务",
        version = "1.0.1",
        release = true,
        container = "2.3.2"
)
public class Service extends ServiceGate {

    static MongoUtil.Config MongoDBConfig = null;       // 当前MongoDB配置
    static MongoClient      MongoDBClient = null;       // 当前MongoDB客户端实例
    static MongoDatabase    MongoDBInst = null;         // MongoDB数据库实例

    /** 初始化 */
    @USInit
    public static void init(ServiceContext ctx) throws Exception {
        MongoDBConfig = ctx.readDataFile(MongoUtil.CONFIG_FILE, MongoUtil.Config.class);
        MongoDBClient = MongoUtil.getMongoClient(MongoDBConfig, DB.DB);
        MongoDBInst = MongoDBClient.getDatabase(DB.DB);

        // 创建Collection及索引
        MongoUtil.createCollectionAndIndex(MongoDBInst, DB.Indexes);

        if ( !JedisUtil.isInited() )
            ctx.getLogger().warn("init", "ubsi gateway service need Redis to send NOTICE");
    }

    /** 结束 */
    @USClose
    public static void close(ServiceContext ctx) throws Exception {
        if ( MongoDBClient != null ) {
            MongoDBClient.close();
            MongoDBClient = null;
        }
        MongoDBConfig = null;
        MongoDBInst = null;
    }

    /** 运行信息 */
    @USInfo
    public static Object info(ServiceContext ctx) throws Exception {
        return "";
    }

    /** 返回配置参数 */
    @USConfigGet
    public static Config getConfig(ServiceContext ctx) throws Exception {
        Config res = new Config();
        if ( MongoDBConfig != null ) {
            res.mongo_servers = MongoDBConfig.servers;
            res.mongo_auth = MongoDBConfig.auth;
            if ( res.mongo_auth != null )
                for ( MongoUtil.Auth auth : res.mongo_auth )
                    if ( auth.username != null )
                        auth.password = "******";
            res.mongo_option = MongoDBConfig.option;
        }
        MongoUtil.Config config = ctx.readDataFile(MongoUtil.CONFIG_FILE, MongoUtil.Config.class);
        if ( config != null ) {
            res.mongo_servers_restart = config.servers;
            res.mongo_auth_restart = config.auth;
            if ( res.mongo_auth_restart != null )
                for ( MongoUtil.Auth auth : res.mongo_auth_restart )
                    if ( auth.username != null )
                        auth.password = "******";
            res.mongo_option_restart = config.option;
        }
        return res;
    }

    /** 设置配置参数 */
    @USConfigSet
    public static void setConfig(ServiceContext ctx, String json) throws Exception {
        Config cfg = Util.json2Type(json, Config.class);
        MongoUtil.Config config = new MongoUtil.Config();
        config.servers = cfg.mongo_servers;
        config.auth = cfg.mongo_auth;
        config.option = cfg.mongo_option;

        MongoUtil.checkConfig(config);
        ctx.saveDataFile(MongoUtil.CONFIG_FILE, config);
    }

    @USEntry(
            tips = "获得数据模型",
            result = "数据模型定义，格式：{ \"模型1\": { \"字段1\": \"描述\", ... }, ... }"
    )
    public Map<String,Map<String,String>> getModels(ServiceContext ctx) {
        return Util.getUSNotes(DB.Models);
    }

    final static String PUB_CHANNEL = "_ubsi_gateway_";     // 广播频道
    final static String RACL = "RACL";
    final static String SACL = "SACL";
    final static String FLOW = "FLOW";
    final static String RATE = "RATE";
    final static String ROUTER = "ROUTER";
    final static String LOG = "LOG";
    final static String CACHE = "CACHE";
    final static String MOCK = "MOCK";

    /* 发送广播消息 */
    static void publish(ServiceContext ctx, String group, String type) {
        String msg = (group == null ? "" : group) + "#" + type;
        if ( JedisUtil.isInited() ) {
            try {
                JedisUtil.publish(PUB_CHANNEL, msg);
                return;
            } catch (Exception e) {
            }
        }
        ctx.getLogger().warn("publish", msg);
    }
}
