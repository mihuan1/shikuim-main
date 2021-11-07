package com.shiku.push.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Message.Builder;

import cn.xyz.mianshi.vo.MsgNotice;

public class GooglePushService extends PushServiceUtils{
	
	private static String dataBaseUrl = getPushConfig().getFCM_dataBaseUrl();
	
	static Builder builder=Message.builder();
	
	static com.google.firebase.messaging.AndroidConfig.Builder androidBuilder=AndroidConfig.builder();
	
	static AndroidNotification.Builder androidNotifiBuilder=AndroidNotification.builder();
	
	static FirebaseApp firebaseApp=null;
	
	
	/**
	 * 初始化FireBaseApp
	 */
	private static void initFireBase(){
		
		try {
			GooglePushUtil googlePushUtil=new GooglePushUtil();
			FirebaseOptions options = new FirebaseOptions.Builder()	
					  .setCredentials(GoogleCredentials.fromStream(googlePushUtil.getJson()))
					  .setDatabaseUrl(dataBaseUrl)
					  .build();
			if(firebaseApp==null)
				firebaseApp = FirebaseApp.initializeApp(options);
			
			androidBuilder.setRestrictedPackageName(appPkgName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送消息
	 * @param token
	 * @param msgNotice
	 */
	public static void fcmPush(String token,MsgNotice msgNotice){
		
		initFireBase();
		
		androidNotifiBuilder.setColor("#55BEB7");// 设置通知颜色
		androidNotifiBuilder.setBody(msgNotice.getText());// 设置通知内容
		//androidNotifiBuilder.setIcon("");// 设置通知图标
		androidNotifiBuilder.setTitle(msgNotice.getTitle());// 设置通知标题
		
		AndroidNotification androidNotification=androidNotifiBuilder.build();
		
		androidBuilder.setNotification(androidNotification);
		
		AndroidConfig androidConfig=androidBuilder.build();
		
		builder.setToken(token);// 设置token
		builder.setAndroidConfig(androidConfig);
		builder.putData("userId", msgNotice.getIsGroup() ? msgNotice.getRoomJid() : String.valueOf(msgNotice.getFrom()));// 自定义数据
		Message message=builder.build();
		try {
			String fcm=FirebaseMessaging.getInstance().send(message);
			log.info("google fcm push success : {}",fcm);
		}catch(Exception e){
			e.printStackTrace();
			log.error("google fcm error====》    "+e.getMessage());
		}
	}
}
