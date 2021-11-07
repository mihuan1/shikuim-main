package com.shiku.push.autoconfigure;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shiku.push.autoconfigure.ApplicationProperties.MQConfig;
import com.shiku.push.autoconfigure.ApplicationProperties.PushConfig;

import cn.xyz.commons.autoconfigure.KApplicationProperties.MongoConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.RedisConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.XMPPConfig;
import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.support.spring.converter.MappingFastjsonHttpMessageConverter;

@Configuration
public class CommAutoConfiguration {
	@Autowired
	private ApplicationProperties config;
	
	@Bean
	public HttpMessageConverters customConverters() {
		return new HttpMessageConverters(
				new MappingFastjsonHttpMessageConverter());
	}
	
	@Bean(name="xmppConfig")
	public XMPPConfig xmppConfig(){
		XMPPConfig xmppConfig=config.getXmppConfig();
		
		return xmppConfig;
	}
	@Bean(name="mongoConfig")
	public MongoConfig mongoConfig(){
		MongoConfig mongoConfig=config.getMongoConfig();
		return mongoConfig;
	}
	@Bean(name="redisConfig")
	public RedisConfig redisConfig(){
		RedisConfig redisConfig=config.getRedisConfig();
		return redisConfig;
	}
	
	@Bean(name="pushConfig")
	public PushConfig pushConfig(){
		PushConfig pushConfig=config.getPushConfig();
		if(null!=pushConfig)
			KConstants.isDebug=1==pushConfig.getIsDebug();
		return pushConfig;
	}
	
	@Bean(name="mqConfig")
	public MQConfig mqConfig(){
		MQConfig mqConfig=config.getMqConfig();
		
		return mqConfig;
	}
	
	
	
	
	
	
}
