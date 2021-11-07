package cn.xyz.commons.autoconfigure;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import cn.xyz.mianshi.vo.Language;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "im")
public class KApplicationProperties extends BaseProperties {
    // ,locations="classpath:application-test.properties" //外网测试环境
    // ,locations="classpath:application-local.properties" //本地测试环境
    //// application

    public KApplicationProperties() {
        // TODO Auto-generated constructor stub
    }

    private MongoConfig mongoConfig;
    private RedisConfig redisConfig;
    private XMPPConfig xmppConfig;
    private AppConfig appConfig;
    private SmsConfig smsConfig;

    private PushConfig pushConfig;

    private WXConfig wxConfig;

    private AliPayConfig aliPayConfig;

    private FcyPayConfig fcyPayConfig;

    private MQConfig mqConfig;

    private AgoraConfig agoraConfig;

    @Setter
    @Getter
    public static class AgoraConfig {
        private String appId;
        private String certificate;
    }

    @Setter
    @Getter
    public static class MongoConfig {

        /**
         * 数据库链接  127.0.0.1:27017,127.0.0.2:28018
         */
        private String uri;

        private String dbName;
        private String roomDbName;
        //配置是否使用集群模式   读写分离    0 单机 模式     1：集群模式
        private int cluster = 0;
        private String username;
        private String password;

        private int connectTimeout = 20000;
        private int socketTimeout = 20000;
        private int maxWaitTime = 20000;

    }

    @Setter
    @Getter
    public static class XMPPConfig {
        private String host;
        private int port;
        private String serverName;
        private String username;
        private String password;

        /**
         * 数据库链接  127.0.0.1:27017,127.0.0.2:28018
         */
        private String dbUri;
        private String dbName;
        private String dbUsername;
        private String dbPassword;
    }

    @Setter
    @Getter
    public static class RedisConfig {
        private String address;
        private int database = 0;

        private String password;
        private int connectionMinimumIdleSize = 32;
        private int connectionPoolSize = 64;
        private int connectTimeout = 10000;
        private int pingConnectionInterval = 500;
        private int pingTimeout = 10000;
        private int timeout = 10000;

        private String host;
        private int port;
        private short isCluster = 0; //是否开启集群 0 关闭 1开启

    }

    @Getter
    @Setter
    public static class AppConfig {
        private String uploadDomain = "http://upload.server.com";//上传服务器域名
        private String apiKey;
        private List<Language> languages; //语言

        private String buildTime;

        /**
         * ip 数据库目录
         */
        private String qqzengPath;

        private int openTask = 1;//是否开启定时任务
        private int distance = 20;

        private byte isBeta = 0;//是否测试版本  测试版本 附近的人和 所有房间不返回值

        private byte isDebug = 1;//是否开启调试  打印日志用到

        //是否开启清除 admin token，开启后在项目启动时会清除redis里存的 admin token (admin token 用于管理后台、公众号页面、开放平台)
        private byte openClearAdminToken = 0;

        private String wxChatUrl;// 微信公众号群聊网页路径

    }

    @Getter
    @Setter
    public static class PushConfig {

        // 企业版 app 包名
        protected String betaAppId;

        //appStore 版本 App 包名
        protected String appStoreAppId;

    }

    @Getter
    @Setter
    public static class SmsConfig {

        private int openSMS = 1;// 是否发送短信验证码
        // 天天国际短信服务
        private String host;
        private int port;
        private String api;
        private String username;// 短信平台用户名
        private String password;// 短信平台密码
        private String templateChineseSMS;// 中文短信模板
        private String templateEnglishSMS;// 英文短信模板
        // 阿里云短信服务
        private String product;// 云通信短信API产品,无需替换
        private String domain;// 产品域名,无需替换
        private String accesskeyid;// AK key
        private String accesskeysecret;// AK value
        private String signname;// 短信签名
        private String chinase_templetecode;// 中文短信模板标识
        private String english_templetecode;// 英文短信模板标识

    }


    @Getter
    @Setter
    public static class WXConfig {
        // 微信认证的自己应用ID
        private String appid;
        // 商户ID
        private String mchid;
        // App secret
        private String secret;
        // api  API密钥
        private String apiKey;
        //
        /**
         * 微信支付 回调 通知 url
         * 默认   http://imapi.server.com/user/recharge/wxPayCallBack
         */
        private String callBackUrl;
        //证书文件 名称
        private String pkPath;
    }

    @Getter
    @Setter
    public static class AliPayConfig {
        // 支付宝认证应用Id
        private String appid;
        // 应用私钥
        private String app_private_key;
        // 字符编码格式
        private String charset;
        // 支付宝公钥
        private String alipay_public_key;
        // 支付宝回调地址
        private String callBackUrl;
        // 账户pid
        private String pid;
    }

    @Getter
    @Setter
    public static class FcyPayConfig {
        private String api_id;
        private String app_key;
        private String pay_password;
        private String url;
    }

    /**
     * rocketmq  的 配置
     *
     * @author lidaye
     */
    @Getter
    @Setter
    public static class MQConfig {
        protected String nameAddr = "localhost:9876";

        protected int threadMin = Runtime.getRuntime().availableProcessors();

        protected int threadMax = Runtime.getRuntime().availableProcessors() * 2;

        protected int batchMaxSize = 20;

        private byte isConsumerUserStatus = 1;
    }
}
