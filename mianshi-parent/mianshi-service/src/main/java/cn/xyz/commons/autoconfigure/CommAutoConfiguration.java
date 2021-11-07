package cn.xyz.commons.autoconfigure;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.fastjson.JSONObject;

import cn.xyz.commons.autoconfigure.KApplicationProperties.AppConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.MQConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.MongoConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.RedisConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.SmsConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.WXConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.XMPPConfig;
import cn.xyz.commons.support.spring.converter.MappingFastjsonHttpMessageConverter;
import cn.xyz.commons.utils.StringUtil;

@Configuration
public class CommAutoConfiguration {
	@Autowired
	private KApplicationProperties config;
	
	@Bean
	public HttpMessageConverters customConverters() {
		return new HttpMessageConverters(
				new MappingFastjsonHttpMessageConverter());
	}
	

	@Bean(name="appConfig")
	public AppConfig appConfig(){
		AppConfig appConfig=config.getAppConfig();
		System.out.println("appConfig  ----"+JSONObject.toJSONString(appConfig));
		if(!StringUtil.isEmpty(appConfig.getQqzengPath()))
			IpSearch.getInstance(appConfig.getQqzengPath());
		return appConfig;
	}
	
	@Bean(name="smsConfig")
	public SmsConfig smsConfig(){
		SmsConfig smsConfig=config.getSmsConfig();
		return smsConfig;
	}
	@Bean(name="xmppConfig")
	public XMPPConfig xmppConfig(){
		XMPPConfig xmppConfig=config.getXmppConfig();
		return xmppConfig;
	}
	@Bean(name="mongoConfig")
	public MongoConfig mongoConfig(){
		MongoConfig mongoConfig=config.getMongoConfig();
		System.out.println("mongoConfig  ----"+JSONObject.toJSONString(mongoConfig));
		return mongoConfig;
	}
	@Bean(name="redisConfig")
	public RedisConfig redisConfig(){
		RedisConfig redisConfig=config.getRedisConfig();
		System.out.println("redisConfig  ----"+JSONObject.toJSONString(redisConfig));
		return redisConfig;
	}
	
	
	
	@Bean(name="wxConfig")
	public WXConfig wxConfig(){
		WXConfig wxConfig=config.getWxConfig();
		return wxConfig;
	}
	@Bean(name="mqConfig")
	public MQConfig mqConfig(){
		MQConfig mqConfig=config.getMqConfig();
		System.out.println("mqConfig  ----"+JSONObject.toJSONString(mqConfig));
		return mqConfig;
	}

    @Bean(name="agoraConfig")
    public KApplicationProperties.AgoraConfig agoraConfig(){
        KApplicationProperties.AgoraConfig agoraConfig=config.getAgoraConfig();
        return agoraConfig;
    }

}
