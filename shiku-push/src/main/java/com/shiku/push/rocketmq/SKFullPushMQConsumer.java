package com.shiku.push.rocketmq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shiku.push.autoconfigure.ApplicationProperties.MQConfig;

import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
public class SKFullPushMQConsumer implements InitializingBean{

	@Autowired 
	private MQConfig mqConfig;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		log.info(" MQ config nameAddr ===> "+mqConfig.getNameAddr());
		
		 DefaultMQPushConsumer consumer = getPushConsumer();
		 	
	        consumer.registerMessageListener(new FullPushMsgListenerConcurrently(mqConfig));
	       try {
	    	   consumer.subscribe("fullPushMessage", "*");
	    	   consumer.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	private DefaultMQPushConsumer fullPushConsumer;
	
	public DefaultMQPushConsumer getPushConsumer() {
		if(null!=fullPushConsumer)
			return fullPushConsumer;
			try {
				fullPushConsumer=new DefaultMQPushConsumer("fullPushProducer");
				fullPushConsumer.setNamesrvAddr(mqConfig.getNameAddr());
				fullPushConsumer.setVipChannelEnabled(false);
				fullPushConsumer.setConsumeThreadMin(mqConfig.getThreadMin());
				fullPushConsumer.setConsumeThreadMax(mqConfig.getThreadMax());
				fullPushConsumer.setConsumeMessageBatchMaxSize(mqConfig.getBatchMaxSize());
				fullPushConsumer.setMessageModel(MessageModel.CLUSTERING);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		return fullPushConsumer;
	}
}
