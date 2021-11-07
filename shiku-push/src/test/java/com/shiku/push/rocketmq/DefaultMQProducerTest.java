package com.shiku.push.rocketmq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.junit.Test;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;

public class DefaultMQProducerTest {

private DefaultMQProducer chatProducer;
	
	private DefaultMQProducer groupProducer;
	
	private final String name_addr="192.168.0.139:9876";
	public DefaultMQProducer getChatProducer() {
		if(null!=chatProducer)
			return chatProducer;
		
			try {
				chatProducer=new DefaultMQProducer("chatProducer");
				chatProducer.setNamesrvAddr(name_addr);
				chatProducer.setCreateTopicKey("chatMessage");
				chatProducer.setSendMsgTimeout(30000);
				
				chatProducer.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		return chatProducer;
	}
	public DefaultMQProducer getGroupProducer() {
		if(null!=groupProducer)
			return groupProducer;
		
			try {
				groupProducer=new DefaultMQProducer("groupProducer");
				groupProducer.setNamesrvAddr(name_addr);
				groupProducer.setCreateTopicKey("groupMessage");
				groupProducer.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		return groupProducer;
	}
	
	@Test
	public void testProducer() {
		
		  MessageBean messageBean=new MessageBean();
	        messageBean.setContent("");
	        messageBean.setFromUserId("10005");
	        messageBean.setFromUserName("群主");
	        messageBean.setToUserId("10006");
	        messageBean.setType(1);
	        
	       
	        
	       // muc.sendMessage(messageBean.toString());
	        //muc.leave();
	        
	        DefaultMQProducer producer = getChatProducer();
	       /* try {
	        	 producer.createTopic("chatMessage", "chatMessage", 1);
			} catch (Exception e) {
				e.printStackTrace();
			}*/
	       
	        String content="=== ";
	        long start=System.currentTimeMillis();
	        Message message=null;
	       
	       for (int i = 1; i <=100000; i++) {
	    	   messageBean.setTimeSend(DateUtil.currentTimeSeconds());
	    	   messageBean.setMessageId(StringUtil.randomCode());
	    	   messageBean.setContent(content+i);
	    	   
	    	   message=new Message("chatMessage",messageBean.toString().getBytes());
	    	   try {
	    		   producer.send(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
	       }
	       long count=(System.currentTimeMillis()-start)/1000;
	        System.out.println("send all timeCount "+count);
		
		
	}
	
	@Test
	public void testGroupProducer() {
		
		  MessageBean messageBean=new MessageBean();
	        messageBean.setContent("");
	        messageBean.setFromUserId("10005");
	        messageBean.setFromUserName("群主");
	        messageBean.setToUserId("10006");
	        messageBean.setType(1);
	        
	       
	        
	       // muc.sendMessage(messageBean.toString());
	        //muc.leave();
	        
	        DefaultMQProducer producer = getGroupProducer();
	       /* try {
	        	 producer.createTopic("chatMessage", "chatMessage", 1);
			} catch (Exception e) {
				e.printStackTrace();
			}*/
	       
	        String content="=== ";
	        long start=System.currentTimeMillis();
	        Message message=null;
	       
	       for (int i = 1; i <=100; i++) {
	    	   messageBean.setTimeSend(DateUtil.currentTimeSeconds());
	    	   messageBean.setMessageId(StringUtil.randomCode());
	    	   messageBean.setContent(content+i);
	    	   
	    	   message=new Message("groupMessage",messageBean.toString().getBytes());
	    	   try {
	    		   producer.send(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
	       }
	       long count=(System.currentTimeMillis()-start)/1000;
	        System.out.println("send all timeCount "+count);
		
		
	}
	
	
}
