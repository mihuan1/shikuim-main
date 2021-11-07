package com.shiku.push.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.mongodb.morphia.query.Query;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.MsgNotice;
import cn.xyz.mianshi.vo.PushInfo;
import cn.xyz.mianshi.vo.User.DeviceInfo;

/** @version:（1.0） 
* @ClassName	FullPushService
* @author: wcl
* @Description: （全量推送） 
* @date:2019年6月12日上午11:32:45  
*/ 
public class FullPushService extends PushServiceUtils {
	
	public synchronized static void pushToDevice(MsgNotice notice){
		try {
			fullHwPush(notice);
			fullVivoPush(notice);
			officialFullPush(notice);
			fullAPNSPush(notice);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	// 华为批量推送
	public static void fullHwPush(MsgNotice notice){
		ThreadUtil.executeInThread(new Callback() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void execute(Object obj) {
				// TODO Auto-generated method stub
				Query<PushInfo> query = SKBeanUtils.getDatastore().createQuery(PushInfo.class).field("pushServer").equal(KConstants.PUSHSERVER.HUAWEI);
				// 华为处理
				List<PushInfo> pushInfos = query.asList();
				JSONArray deviceTokens = new JSONArray();//目标设备Token
				List<List<PushInfo>> fixedGrouping = StringUtil.fixedGrouping(pushInfos, 100);
				fixedGrouping.forEach(singlePushInfo ->{
					synchronized (singlePushInfo) {
						singlePushInfo.forEach(pushInfo ->{
							deviceTokens.add(pushInfo.getPushToken());
						});
						try {
							// 华为批量推送
							log.info("HUAWEI PUSH INFO tokens : {}",JSONObject.toJSONString(deviceTokens));
							HWPushService.fullSendPushMessage(notice, deviceTokens);
							Thread.sleep(100);
							deviceTokens.clear();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		});
	}
	
	// vivo批量推送
	public static void fullVivoPush(MsgNotice notice){
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				// TODO Auto-generated method stub
				Query<PushInfo> queryVivo = SKBeanUtils.getDatastore().createQuery(PushInfo.class).field("pushServer").equal(KConstants.PUSHSERVER.VIVO);
				List<PushInfo> pushInfosVivo = queryVivo.asList();
				List<List<PushInfo>> vivofixedGrouping = StringUtil.fixedGrouping(pushInfosVivo, 100);
				Set<String> retIds = new HashSet<String>();
				vivofixedGrouping.forEach(vivoPushInfo ->{
					vivoPushInfo.forEach(info ->{
						retIds.add(info.getPushToken());
					});
					try {
						// vivo批量推送
						log.info("VIVO PUSH INFO tokens : {}",JSONObject.toJSONString(retIds));
						VIVOPushService.listSend(notice, retIds);
						Thread.sleep(100);
						retIds.clear();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			}
		});
		
	}
	
	// apns标题栏推送
	public static void fullAPNSPush(MsgNotice notice){
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
//				Query<PushInfo> queryApns = SKBeanUtils.getDatastore().createQuery(PushInfo.class).field("pushServer").equal("apns");
				@SuppressWarnings("unchecked")
				List<Integer> pushInfosApns =SKBeanUtils.getUserRepository().distinct("pushInfo", "userId",new BasicDBObject("pushServer", KConstants.PUSHSERVER.APNS));
				log.info("fullPush IOS apns : {}",JSONObject.toJSONString(pushInfosApns));
				pushInfosApns.forEach(userId ->{
					synchronized (userId) {
						DeviceInfo iosDevice=KSessionUtil.getIosPushToken(userId);
						if(null == iosDevice)
							return;
						PushServiceUtils.pushToIos(userId, notice, iosDevice);
					}
				});
			}
		});
	}
	
	// 官方支持的全量推送
	public static void officialFullPush(MsgNotice notice){
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				// 处理oppo、小米、魅族、
				try {
					OPPOPushService.broadcastMessage(notice);
					XMPushService.sendBroadcast(notice);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					MZPushService.pushToAPP(notice);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
	}
}
