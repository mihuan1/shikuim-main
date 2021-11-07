package com.shiku.push.service;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Semaphore;

import org.springframework.core.io.ClassPathResource;

import com.notnoop.apns.APNS;
import com.notnoop.apns.PayloadBuilder;
import com.shiku.push.autoconfigure.ApplicationProperties.PushConfig;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.concurrent.PushNotificationFuture;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.vo.MsgNotice;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ApnsHttp2PushService extends PushServiceUtils{
	

	private static final String sandboxServer= "gateway.sandbox.push.apple.com";
	private static ApnsClient apnsService;
	
	private static ApnsClient betaApnsService;
	
	private static ApnsClient viopService;
	
	private static EventLoopGroup eventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()*2);
	private static final Semaphore semaphore = new Semaphore(10000);
	public static enum PushEnvironment {
		BETA,//企业版 环境
		Pro,//发布环境  Apple Store 版
		VOIP//Viop  Apple Store 版
	}
	
	private static ApnsClientBuilder getApnsClientBuilder(PushConfig pushConfig,PushEnvironment environment) {
		try {
			String pkpath=null;
			switch (environment) {
				case BETA:
					pkpath=pushConfig.getBetaApnsPk();
					break;
				case Pro:
					pkpath=pushConfig.getAppStoreApnsPk();
					break;
				case VOIP:
					pkpath=pushConfig.getVoipPk();
					break;
				default:
					break;
			}
			if(pkpath.startsWith("classpath:")) {
				ClassPathResource resource = new ClassPathResource(pkpath);
				String path = resource.getClassLoader().getResource(pkpath.replace("classpath:", "")).getPath();
				pkpath=path;
			}
			ApnsClientBuilder builder = new ApnsClientBuilder();
			
			
			if(1==pushConfig.getIsApnsSandbox())
				builder.setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST);
			else
				builder.setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST);
			builder.setClientCredentials(new File(pkpath),pushConfig.getPkPassword());
			builder.setEventLoopGroup(eventLoopGroup).setConcurrentConnections(Runtime.getRuntime().availableProcessors());
			return builder;
		} catch (Exception e) {
			log.error("getApnsClientBuilder Exception {} "+e.getMessage());
			return null;
		}
		
	}
	private static ApnsClient getBataApnsService(){
		try {
			if (null==betaApnsService){
				PushConfig pushConfig=getPushConfig();
				synchronized (pushConfig) {
					if(null!=betaApnsService)
						return betaApnsService;
					ApnsClientBuilder builder=getApnsClientBuilder(pushConfig,PushEnvironment.BETA);
					
					betaApnsService=builder.build();
					log.info("====getBataApnsService======》");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return betaApnsService;
	}
	private static ApnsClient getApnsService(){
		try {
			if (null==apnsService){
				PushConfig pushConfig=getPushConfig();
				synchronized (pushConfig) {
					if(null!=apnsService)
						return apnsService;
					ApnsClientBuilder builder=getApnsClientBuilder(pushConfig,PushEnvironment.Pro);
					
					apnsService=builder.build();
					log.info("====getApnsService======》");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return apnsService;
	}
	private static ApnsClient getVoipService(){
		try {
			if (null==viopService){
				PushConfig pushConfig=getPushConfig();
				synchronized (pushConfig) {
					if(null!=viopService)
						return viopService;
					ApnsClientBuilder builder=getApnsClientBuilder(pushConfig,PushEnvironment.VOIP);
					
					viopService=builder.build();
					log.info("====getVoipService======》");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return viopService;
	}
	public static String builderPayloadOld(MsgNotice notice) {
		PayloadBuilder builder = APNS.newPayload();
		/*String payload = APNS.newPayload()
		            .badge(3)
		            .customField("secret", "what do you think?")
		            .localizedKey("GAME_PLAY_REQUEST_FORMAT")
		            .localizedArguments("Jenna", "Frank")
		            .actionKey("Play").build();*/
		
		 builder.customField("content-available", 1);
		 
		 builder.alertBody(notice.getText()+"");
		 builder.badge(notice.getMsgNum());
		 
		 builder.sound("default");
		 builder.customField("from",notice.getFrom()+"");
		 builder.customField("fromUserName",notice.getName());
		 builder.customField("messageType", notice.getType()+"");
		 builder.customField("to", notice.getTo()+"");
		 if(!StringUtil.isEmpty(notice.getRoomJid())){
				builder.customField("roomJid", notice.getRoomJid());
			 }
		 if(!StringUtil.isEmpty(notice.getObjectId())){
				builder.customField("url", notice.getObjectId());
				builder.customField("fromUserName","");
		 } 
		return builder.build();
	}
	public static String builderPayload(MsgNotice notice) {
		ApnsPayloadBuilder builder = new ApnsPayloadBuilder();
		
		builder.setContentAvailable(true);
		//builder.addCustomProperty("content-available", 1);
		
		builder.setAlertTitle(notice.getText()+"");
		//builder.setAlertBody();
		builder.setBadgeNumber(notice.getMsgNum());
		
		builder.setSound("default");
		builder.addCustomProperty("from",notice.getFrom()+"")
		.addCustomProperty("fromUserName",notice.getName())
		.addCustomProperty("messageType", notice.getType()+"")
		.addCustomProperty("to", notice.getTo()+"");
		return builder.buildWithDefaultMaximumLength();
	}
	
	public static void pushMsgToUser(String token,MsgNotice notice,PushEnvironment env){
		
		final String payload=builderPayloadOld(notice);
		
		 final ShikuApnsPushNotification notification;
		 if(PushEnvironment.VOIP!=env){
		   if(PushEnvironment.Pro==env)
			 notification=new ShikuApnsPushNotification(token,getPushConfig().getAppStoreAppId(), payload,env,notice.getMessageId());
		   else 
			   notification=new ShikuApnsPushNotification(token,getPushConfig().getBetaAppId(), payload,env,notice.getMessageId());
			   
		 }else
			 notification=new ShikuApnsPushNotification(token,getPushConfig().getAppStoreAppId()+".voip", payload,env,notice.getMessageId());
		 notification.setTo(notice.getTo());
		 
		
		sendPushToApns(notification);
		 
		
	}
	private static void listenerNotificationResponse(final PushNotificationFuture<ShikuApnsPushNotification, PushNotificationResponse<ShikuApnsPushNotification>> notificationFuture) {
		 try {
			 notificationFuture.addListener(new GenericFutureListener<Future<PushNotificationResponse>>() {
	             @Override
	             public void operationComplete(Future<PushNotificationResponse> pushNotificationResponseFuture) throws Exception {
	                 if (notificationFuture.isSuccess()) {
	                     final PushNotificationResponse<ShikuApnsPushNotification> response = notificationFuture.getNow();
	                     if (response.isAccepted()) {
	                    	 log.info("send apns Success collapseId:{}  to:{} ",notificationFuture.getPushNotification().getCollapseId(),notificationFuture.getPushNotification().getTo());
	                     } else {
	                         Date invalidTime = response.getTokenInvalidationTimestamp();
	                         log.error("Notification rejected by the APNs gateway: " + response.getRejectionReason());
	                         if (invalidTime != null) {
	                         	log.error("\t…and the token is invalid as of " + response.getTokenInvalidationTimestamp());
	                         }
	                     }
	                 } else {
	                	 ShikuApnsPushNotification notification = notificationFuture.getPushNotification();
	                 	log.error("send apns failed notification collapseId= {} to ={} device token={}   {} ", notification.getCollapseId(),notification.getTo(), notification.getToken(), notificationFuture.cause().getMessage());
	                 	sendPushToApns(notification);
	                 }
	                 semaphore.release();
	             }
	         });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void sendPushToApns(ShikuApnsPushNotification notification) {
		final PushNotificationFuture<ShikuApnsPushNotification, PushNotificationResponse<ShikuApnsPushNotification>> notificationFuture;
		if(KConstants.isDebug)
			log.info("{} apns push to > {}  {} ",notification.getCollapseId(),notification.getTo(),notification.getToken());
		try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            log.error("ios push InterruptedException get semaphore failed, collapseId: {} deviceToken:{} ",notification.getCollapseId(), notification.getToken());
            e.printStackTrace();
        }
		 try {
			if(PushEnvironment.VOIP!=notification.getEnvironment()) {
					if(PushEnvironment.BETA!=notification.getEnvironment()) {
						 notificationFuture = getApnsService().sendNotification(notification);
					} else
						notificationFuture=getBataApnsService().sendNotification(notification);
			}else
			   notificationFuture=getVoipService().sendNotification(notification);
				listenerNotificationResponse(notificationFuture);
			} catch (Exception e) {
				log.error(" apns push Exception "+e.getMessage());
				e.printStackTrace();
			} 
		
		 
		
	}
	
	public static class ShikuApnsPushNotification extends SimpleApnsPushNotification{
		
		private PushEnvironment environment;
		private int to;
		public ShikuApnsPushNotification(String token, String topic, String payload,PushEnvironment environment,String collapseId) {
			this(token, topic, payload,collapseId);
			this.setEnvironment(environment);
		}
		public ShikuApnsPushNotification(String token, String topic, String payload,String collapseId) {
			super(token, topic, payload, new Date(System.currentTimeMillis() + DEFAULT_EXPIRATION_PERIOD_MILLIS), DeliveryPriority.IMMEDIATE, collapseId, null);
		}
		public PushEnvironment getEnvironment() {
			return environment;
		}
		public void setEnvironment(PushEnvironment environment) {
			this.environment = environment;
		}
		public int getTo() {
			return to;
		}
		public void setTo(int to) {
			this.to = to;
		}
		
	}
	

}
