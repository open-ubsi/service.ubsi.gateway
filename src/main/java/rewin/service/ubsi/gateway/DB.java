package rewin.service.ubsi.gateway;

import rewin.ubsi.annotation.USNote;
import rewin.ubsi.annotation.USNotes;
import rewin.ubsi.common.Util;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MongoDB数据库表结构
 */
public class DB {

    public final static String DB = "ubsi_gateway";     // Database的名字
    public final static String COL_GATE = "gate";
    public final static String COL_RACL = "racl";
    public final static String COL_SACL = "sacl";
    public final static String COL_LOG = "log";
    public final static String COL_LOGS = "logs";
    public final static String COL_CACHE = "cache";
    public final static String COL_MOCK = "mock";
    public final static String COL_RATE = "rate";
    public final static String COL_ROUTER = "router";
    public final static String COL_FLOW = "flow";
    public final static String COL_APP = "app";

    @USNotes("网关的运行实例")
    public static class Gate {
        @USNote("实例标识")
        public int _id;
        @USNote("分组ID")
        public String group;
        @USNote("主机地址")
        public String host;
        @USNote("监听端口")
        public int port;
        @USNote("URL路径")
        public String path;
        @USNote("活动时间戳，毫秒")
        public long timeActive;
        @USNote("启动时间戳，毫秒")
        public long timeStart;
        @USNote("请求统计，格式：{\"service\":{\"entry\":count}}，注：\"service\"中的\".\"会被替换为\"_\"")
        public Map<String,Map<String,Long>> request;
    }
    @USNotes("远程主机的访问控制")
    public static class RAcl {
        @USNote("数据项标识")
        public int _id;
        @USNote("分组ID，null表示不限")
        public String group;
        @USNote("客户主机，null表示不限")
        public String remote;
        @USNote("App列表，null表示不限")
        public Set<String> apps;
        @USNote("是否允许访问")
        public boolean auth;
        @USNote("是否可用")
        public boolean enabled;
    }
    @USNotes("服务的访问控制")
    public static class SAcl {
        @USNote("数据项标识")
        public int _id;
        @USNote("分组ID，null表示不限")
        public String group;
        @USNote("应用，null表示不限")
        public String app;
        @USNote("服务名字，末尾的\"*\"表示通配符")
        public String service;
        @USNote("接口名字，末尾的\"*\"表示通配符")
        public String entry;
        @USNote("是否允许访问")
        public boolean auth;
        @USNote("是否可用")
        public boolean enabled;
    }
    @USNotes("服务的访问日志设置")
    public static class Log {
        @USNote("数据项标识")
        public int _id;
        @USNote("分组ID，null表示不限")
        public String group;
        @USNote("应用，null表示不限")
        public String app;
        @USNote("服务名字，末尾的\"*\"表示通配符")
        public String service;
        @USNote("接口名字，末尾的\"*\"表示通配符")
        public String entry;
        @USNote("是否记录日志，0:不记录，0x01:转发微服务的请求，0x02:转发分流Gateway的请求，0x04:被cache处理的请求，0x08:被mock处理的请求，0x10:被拒绝或发生异常的请求，0x1F00表示按位对应的请求需要记录参数")
        public int log;
        @USNote("是否跟踪请求")
        public boolean trace;
        @USNote("是否可用")
        public boolean enabled;
    }
    @USNotes("服务的访问日志数据")
    public static class Logs {
        @USNote("数据项标识")
        public String _id;
        @USNote("分组ID")
        public String group;
        @USNote("网关地址#端口")
        public String gate;
        @USNote("远程地址")
        public String remote;
        @USNote("应用ID")
        public String app;
        @USNote("服务名字")
        public String service;
        @USNote("接口名字")
        public String entry;
        @USNote("参数")
        public List params;
        @USNote("微服务的请求ID")
        public String reqId;
        @USNote("结果代码")
        public int result;
        @USNote("错误信息")
        public String error;
        @USNote("结果的出处，0:本机处理，1:微服务，2:分流的Gateway，3:Cache，4:Mock，-1:丢弃服务结果，-2:丢弃分流结果")
        public int source;
        @USNote("请求时间戳，毫秒")
        public long time;
        @USNote("耗时，毫秒数")
        public int elapse;
    }
    @USNotes("服务的结果缓冲设置")
    public static class Cache {
        @USNote("数据项标识")
        public int _id;
        @USNote("分组ID，null表示不限")
        public String group;
        @USNote("应用，null表示不限")
        public String app;
        @USNote("服务名字，末尾的\"*\"表示通配符")
        public String service;
        @USNote("接口名字，末尾的\"*\"表示通配符")
        public String entry;
        @USNote("缓冲的超时时间(秒)")
        public int timeout;
        @USNote("是否可用")
        public boolean enabled;
    }
    @USNotes("服务的仿真数据")
    public static class Mock {
        @USNote("数据项标识")
        public int _id;
        @USNote("分组ID，null表示不限")
        public String group;
        @USNote("应用，null表示不限")
        public String app;
        @USNote("服务名字")
        public String service;
        @USNote("接口名字")
        public String entry;
        @USNote("仿真数据")
        public Object data;
        @USNote("是否可用")
        public boolean enabled;
    }
    @USNotes("流量的转发设置")
    public static class Flow {
        @USNote("数据项标识")
        public int _id;
        @USNote("分组ID，null表示不限")
        public String group;
        @USNote("应用，null表示不限")
        public String app;
        @USNote("服务名字，末尾的\"*\"表示通配符")
        public String service;
        @USNote("接口名字，末尾的\"*\"表示通配符")
        public String entry;
        @USNote("请求比例，100表示全部")
        public int rate;
        @USNote("分流的目标网关的URL(循环转发)")
        public List<String> forward;
        @USNote("镜像的目标网关的URL(依次转发)")
        public List<String> mirror;
        @USNote("是否可用")
        public boolean enabled;
    }
    @USNotes("服务的限流设置")
    public static class Rate {
        @USNote("数据项标识")
        public int _id;
        @USNote("分组ID，null表示不限")
        public String group;
        @USNote("应用，null表示不限")
        public String app;
        @USNote("服务名字，末尾的\"*\"表示通配符")
        public String service;
        @USNote("接口名字，末尾的\"*\"表示通配符")
        public String entry;
        @USNote("最大请求次数")
        public int limit;
        @USNote("时间(秒)")
        public int seconds;
        @USNote("是否可用")
        public boolean enabled;
    }
    @USNotes("服务的路由设置")
    public static class Router {
        @USNote("数据项标识")
        public int _id;
        @USNote("分组ID，null表示不限")
        public String group;
        @USNote("应用，null表示不限")
        public String app;
        @USNote("请求比例，100表示全部")
        public int rate;
        @USNote("服务名字")
        public String service;
        @USNote("接口名字，末尾的\"*\"表示通配符")
        public String entry;
        @USNote("替换的服务名字，null表示不替换")
        public String altService;
        @USNote("最小版本")
        public int verMin;
        @USNote("最大版本，0表示不限")
        public int verMax;
        @USNote("是否正式版，1:是，0:不是，-1:不限")
        public int verRelease;
        @USNote("是否可用")
        public boolean enabled;
    }
    @USNotes("应用的注册数据")
    public static class App {
        @USNote("应用ID")
        public String   _id;
        @USNote("应用名字")
        public String   name;
        @USNote("应用描述")
        public String   desc;
        @USNote("应用所有者")
        public String   owner;
        @USNote("应用联系人")
        public String   contacter;
        @USNote("联系人邮箱")
        public String   email;
        @USNote("联系人电话")
        public String   phone;
        @USNote("应用标签")
        public Set<String>   tags;
        @USNote("状态，0:正常")
        public int      status;
        @USNote("认证密钥(SM3散列值)，null表示不需要认证")
        public byte[]   key;
        @USNote("创建时间")
        public long     createTime;
        @USNote("修改时间")
        public long     updateTime;
        @USNote("密钥修改时间")
        public long     keyTime;
        @USNote("上次成功认证的时间")
        public long     authTime;
    }

    /** 所有的 表及其索引 的列表 */
    public static Object[] Indexes = new Object[] {
            COL_LOGS, Util.toList(
            Util.toBson("service", 1, "entry", 1),
            Util.toBson("app", 1),
            Util.toBson("timeRequest", 1),
            Util.toBson("result", 1, "timeSpend", 1)
    )
    };

    /** 数据模型的说明 */
    public static Class[] Models = new Class[] { Gate.class, RAcl.class, SAcl.class,
            Log.class, Logs.class, Cache.class, Mock.class, Rate.class, Router.class, Flow.class, App.class
    };

}
