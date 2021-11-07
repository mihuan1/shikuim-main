package com.shiku.push.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import cn.xyz.commons.autoconfigure.BaseProperties;
import cn.xyz.commons.autoconfigure.KApplicationProperties.MongoConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.RedisConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.XMPPConfig;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix="im")
public class ApplicationProperties extends BaseProperties {
	// ,locations="classpath:application-test.properties" //外网测试环境
	// ,locations="classpath:application-local.properties" //本地测试环境
	//// application

	public ApplicationProperties() {
		// TODO Auto-generated constructor stub
	}
	private MongoConfig mongoConfig;
	private RedisConfig redisConfig;
	private XMPPConfig xmppConfig;
	
	private PushConfig pushConfig;
	
	
	private MQConfig mqConfig;
	

	@Getter
	@Setter
	public static class PushConfig extends cn.xyz.commons.autoconfigure.KApplicationProperties.PushConfig {
		// 服务器地区 例  CN、HK
		private String serverAdress="CN";
		
		private String packageName;
		
		// 小米
		private String xm_appSecret;
		
		// 华为
		private String hw_appSecret;
		private String hw_appId;
		private String hw_tokenUrl;
		private String hw_apiUrl;
		private String hw_iconUrl;
		private byte IsOpen;// 是否使用单独部署华为推送   1是    0：否
		
		// 百度
		private String bd_appStore_appId;
		private String bd_appStore_appKey;
		private String bd_appStore_secret_key;
		private String[] bd_appKey;
		private String bd_rest_url;
		private String[] bd_secret_key;
		
		// 极光
		private String jPush_appKey;
		private String jPush_masterSecret;
		
		// google FCM
		private String FCM_dataBaseUrl;
		private String FCM_keyJson;
		
		// 魅族
		private String mz_appSecret;
		private long mz_appId;
		
		// VIVO
		private int vivo_appId;
		private String vivo_appKey;
		private String vivo_appSecret;
		
		// OPPO
		private String oppo_appKey;
		private String oppo_masterSecret;
		
		//企业版 测试版 apns 推送证书
		private String betaApnsPk;
		
		//appStore 版本 App 包名
		private String appStoreAppId;
		//appStore apns 推送证书
		private String appStoreApnsPk;
		
		//voip 证书
		private String voipPk;
		
		//证书 密码
		private String pkPassword;
		
		private byte isApnsSandbox=0;
		
		//调试模式  打印 log
		private byte isDebug=0;
		
	}
	
	/**
	 * rocketmq  的 配置
	 * 
	 * @author lidaye
	 *
	 */
	@Getter
	@Setter
	public static class MQConfig extends cn.xyz.commons.autoconfigure.KApplicationProperties.MQConfig{
		
	}

	
}
