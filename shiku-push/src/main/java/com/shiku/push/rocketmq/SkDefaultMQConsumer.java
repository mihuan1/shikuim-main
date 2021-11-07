package com.shiku.push.rocketmq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shiku.push.autoconfigure.ApplicationProperties.MQConfig;
import com.shiku.push.autoconfigure.ApplicationProperties.PushConfig;


@Component
public class SkDefaultMQConsumer implements InitializingBean{
	private static final Logger log = LoggerFactory.getLogger(SkDefaultMQConsumer.class);
	
	@Autowired 
	private MQConfig mqConfig;
	
	@Autowired
	private PushConfig pushConfig;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(pushConfig.getIsOpen()==1){
			initHWConsumer();
		}else{
			initConsumer();
		}
		
	}
	private DefaultMQPushConsumer pushConsumer;
	
	public void initConsumer(){
		log.info(" MQ config nameAddr ===> "+mqConfig.getNameAddr());
		
		 DefaultMQPushConsumer consumer = getPushConsumer();
		 	
	        consumer.registerMessageListener(new SkMessageListenerConcurrently(mqConfig));
	       try {
	    	   consumer.subscribe("pushMessage", "*");
	    	   consumer.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public DefaultMQPushConsumer getPushConsumer() {
		if(null!=pushConsumer)
			return pushConsumer;
			try {
				pushConsumer=new DefaultMQPushConsumer("pushProducer");
				pushConsumer.setNamesrvAddr(mqConfig.getNameAddr());
				pushConsumer.setVipChannelEnabled(false);
				pushConsumer.setConsumeThreadMin(mqConfig.getThreadMin());
				pushConsumer.setConsumeThreadMax(mqConfig.getThreadMax());
				pushConsumer.setConsumeMessageBatchMaxSize(mqConfig.getBatchMaxSize());
				pushConsumer.setMessageModel(MessageModel.CLUSTERING);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		return pushConsumer;
	}
	// 华为推送专用
	private DefaultMQPushConsumer HWPushConsumer;
	// 华为推送专用
	public void initHWConsumer(){
		log.info(" MQ config nameAddr ===> "+mqConfig.getNameAddr());
		
		 DefaultMQPushConsumer consumer = getHWPushConsumer();
		 	
	        consumer.registerMessageListener(new SkMessageListenerConcurrently(mqConfig));
	       try {
	    	   consumer.subscribe("HWPushMessage", "*");
	    	   consumer.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	// 华为推送专用
	public DefaultMQPushConsumer getHWPushConsumer() {
		if(null!=HWPushConsumer)
			return HWPushConsumer;
			try {
				HWPushConsumer=new DefaultMQPushConsumer("HWSendPushProducer");
				HWPushConsumer.setNamesrvAddr(mqConfig.getNameAddr());
				HWPushConsumer.setVipChannelEnabled(false);
				HWPushConsumer.setConsumeThreadMin(mqConfig.getThreadMin());
				HWPushConsumer.setConsumeThreadMax(mqConfig.getThreadMax());
				HWPushConsumer.setConsumeMessageBatchMaxSize(mqConfig.getBatchMaxSize());
				HWPushConsumer.setMessageModel(MessageModel.CLUSTERING);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		return HWPushConsumer;
	}
	
	public void stopConsumer(){
		log.info(" MQ config nameAddr ===> "+mqConfig.getNameAddr());
		 try {
	    	   DefaultMQPushConsumer consumer = getPushConsumer();
	    	   if(null!=consumer) {
	    		   consumer.shutdown();
	    	   }
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	
	
	
	
	
}
