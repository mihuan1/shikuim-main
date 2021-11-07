package com.shiku.push.rocketmq;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shiku.push.autoconfigure.ApplicationProperties.MQConfig;
import com.shiku.push.service.FullPushService;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.mianshi.vo.MsgNotice;
import lombok.extern.slf4j.Slf4j;

/** @version:（1.0） 
* @ClassName	SystemNoticeMsgListenerConcurrently
* @Description: 全量推送 
* @date:2019年5月29日下午3:12:10  
*/ 
@Slf4j
public class FullPushMsgListenerConcurrently implements MessageListenerConcurrently{
	
	protected MQConfig mqConfig;
	
	@Override
	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

		JSONObject jsonMsg= null;
		String body= null;
		for (MessageExt messageExt : msgs) {
			try {
				body=new String(messageExt.getBody(),"utf-8");
				if(KConstants.isDebug)
					log.info(" new msg ==> "+body);
				try {
					jsonMsg=JSON.parseObject(body);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				push(parseMsgNotice(jsonMsg));
			} catch (Exception e) {
				e.printStackTrace();
				log.error("=== error "+body+" ===> "+e.getMessage());
				try {
					if((DateUtil.currentTimeSeconds()-KConstants.Expire.HOUR)<jsonMsg.getLong("timeSend")) {
						continue;
					}
				} catch (Exception e2) {
					continue;
				}
				
//				reSendPushToMq(messageExt);
				continue;
			}
		}
		return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
	}
	
	private MsgNotice parseMsgNotice(JSONObject jsonObj) {
		MsgNotice notice=new MsgNotice();
		try {
			notice.setTitle(jsonObj.getString("title"));
			notice.setText(jsonObj.getString("content"));
			if(null != jsonObj.getString("objectId"))
				notice.setObjectId(jsonObj.getString("objectId"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return notice;
	}
	
	/**
	 * ArrayListBlockingQueue
	 * 
	 * LinkedBlockingQueue
	 */
	Queue<MsgNotice> queue =new LinkedBlockingQueue<>();
	
	private void push(final MsgNotice notice) {
		try {
			queue.offer(notice);
			System.out.println(" fullPush  query : "+JSONObject.toJSONString(queue));
		} catch (Exception e) {
			log.error("Queue push Exception {}",e.getMessage());
		}
	}

	private ExecutorService threadPool=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);;
	public FullPushMsgListenerConcurrently(MQConfig mqConfig) {
		this.mqConfig=mqConfig;
		FullPush pushThread=new FullPush();
		threadPool.execute(pushThread);
		log.info("pushThread  start end ===>");
	}
	
	public class FullPush extends Thread{
		@Override
		public void run() {
			while (true) {
//				System.out.println("FullPush thread aueue : "+JSONObject.toJSONString(queue));
				if(queue.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
					MsgNotice notice = queue.poll();
					if(null==notice)
						return;
					FullPushService.pushToDevice(notice);
				}
			}
		}
	}


}
