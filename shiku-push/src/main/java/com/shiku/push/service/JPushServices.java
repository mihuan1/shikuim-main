package com.shiku.push.service;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import cn.xyz.mianshi.utils.SKBeanUtils;

public class JPushServices extends JPushUtils{
	
	// 极光推送>>Android
	public static void jpushAndroid(Map<String, String> parm) {
		// 推送的关键,构造一个payload
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.android())// 指定android平台的用户
				.setAudience(Audience.registrationId(parm.get("regId")))// registrationId指定用户
//				.setAudience(Audience.all())// Audience设置为all，说明采用广播方式推送，所有用户都可以接收到
				.setNotification(Notification.android(parm.get("msg"), parm.get("title"), parm))// title 标题
				.setOptions(Options.newBuilder().setApnsProduction(false).build())
				// 这里是指定开发环境,不用设置也没关系
				.setMessage(Message.content(parm.get("msg")))// 自定义信息
				.build();

		try {
		
			PushResult result = jPushClient.sendPush(payload);
			System.out.println("JPush to Android ===== > result  " +JSONObject.toJSONString(result));
		} catch (APIConnectionException e) {
			log.error(e.getMessage());
		} catch (APIRequestException e) {
			log.error(e.getMessage());
		}catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	   
	// 极光推送>>ios {该方法暂时弃用}
	public static void jpushIOS(Map<String, String> parm) {
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.ios())// ios平台的用户
//				.setAudience(Audience.all())// 所有用户
				.setAudience(Audience.registrationId(parm.get("regId")))// registrationId指定用户
				.setNotification(
						Notification.newBuilder()
								.addPlatformNotification(IosNotification.newBuilder().setAlert(parm.get("msg"))
										.setBadge(+1).setSound("happy")// 这里是设置提示音(更多可以去官网看看)
										.addExtras(parm).build())
								.build())
				.setOptions(Options.newBuilder().setApnsProduction(true).build())
				.setMessage(Message.newBuilder().setMsgContent(parm.get("msg")).addExtras(parm).build())// 自定义信息
				.build();

		try {
			PushResult result = jPushClient.sendPush(payload);
			System.out.println(result);
		} catch (APIConnectionException e) {
			log.error(e.getMessage());
		} catch (APIRequestException e) {
			log.error(e.getMessage());
		}catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	 /** @Description: IOS极光推送 
	* @param parm
	**/ 
	public static void buildPushObject_ios_tagAnd_alertWithExtrasAndMessage(Map<String, String> parm) {
		boolean flag = 1 == SKBeanUtils.getAdminManager().getClientConfig().getIsOpenAPNSorJPUSH() ? false : true;
		//创建JPushClient
        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.ios())//ios平台的用户
//	                .setAudience(Audience.all())//所有用户
                .setAudience(Audience.registrationId(parm.get("regId")))//registrationId指定用户
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(IosNotification.newBuilder()
                            .setAlert(parm.get("msg"))
                            .setBadge(+1)
                            .setSound("happy")//这里是设置提示音(更多可以去官网看看)
                            .addExtras(parm)
                            .build())
                        .build())
                .setOptions(Options.newBuilder().setApnsProduction(flag).build())
                .setMessage(Message.newBuilder().setMsgContent(parm.get("msg")).addExtras(parm).build())//自定义信息
                .build();
        try {
            PushResult pu = jPushClient.sendPush(payload);
            log.info("JPush result : {}",pu.toString());
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (APIRequestException e) {
            e.printStackTrace();
        }
	 }
	
	//极光推送>>All所有平台
	public static void jpushAll(Map<String, String> parm) {
		// 创建option
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.all()) // 所有平台的用户
				.setAudience(Audience.registrationId(parm.get("regId")))// registrationId指定用户
				.setNotification(Notification.newBuilder()
						.addPlatformNotification(IosNotification.newBuilder().setAlert(parm.get("msg")).setBadge(+1)
								.setSound("happy").addExtras(parm).build())
						.addPlatformNotification(
								AndroidNotification.newBuilder().addExtras(parm).setAlert(parm.get("msg")).build())
						.build())
				.setOptions(Options.newBuilder().setApnsProduction(true).build())// 指定开发环境
				.setMessage(Message.newBuilder().setMsgContent(parm.get("msg")).addExtras(parm).build())// 自定义信息
				.build();
		try {
			PushResult result = jPushClient.sendPush(payload);
			System.out.println(result);
		} catch (APIConnectionException e) {
			log.error(e.getMessage());
		} catch (APIRequestException e) {
			log.error(e.getMessage());
		}catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}
