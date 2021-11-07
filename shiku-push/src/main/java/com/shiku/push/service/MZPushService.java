package com.shiku.push.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.meizu.push.sdk.constant.PushType;
import com.meizu.push.sdk.server.IFlymePush;
import com.meizu.push.sdk.server.constant.ResultPack;
import com.meizu.push.sdk.server.model.push.PushResult;
import com.meizu.push.sdk.server.model.push.VarnishedMessage;

import cn.xyz.mianshi.vo.MsgNotice;

public class MZPushService extends PushServiceUtils{
	private static String appSecret=getPushConfig().getMz_appSecret();
	private static long appId=getPushConfig().getMz_appId();
	
	/**
	 * 通知栏消息
	 * @param pushId
	 * @param msgNotice
	 * @throws IOException
	 */
	public static void varnishedMessagePush(String pushId,MsgNotice msgNotice) throws IOException{
		String url = null;
		if(msgNotice.getIsGroup())
			url="sk://"+appPkgName+"/notification?userId="+msgNotice.getRoomJid();
		else
			url="sk://"+appPkgName+"/notification?userId="+msgNotice.getFrom();
		
		IFlymePush push = new IFlymePush(appSecret);
		
		// 组装消息
        VarnishedMessage message = new VarnishedMessage.Builder().appId(appId)
                .title(msgNotice.getTitle())// 通知标题
                .content(msgNotice.getText())// 通知内容
                .noticeBarType(2) // 通知栏样式 0 标准    2安卓原生
                .clickType(2) // 点击动作  0 打开应用    1 打开网页应用     2 打开url页面     3 应用客户端自定义
                .url(url)
                .build();
        
        // 目标用户
	    List<String> pushIds = new ArrayList<String>();
	    pushIds.add(pushId);
	    
	    // 1 调用推送服务
        ResultPack<PushResult> result = push.pushMessage(message, pushIds);
        if (result.isSucceed()) {
            // 2 调用推送服务成功 （其中map为设备的具体推送结果，一般业务针对超速的code类型做处理）
            PushResult pushResult = result.value();
//            String msgId = pushResult.getMsgId();//推送消息ID，用于推送流程明细排查
            Map<String, List<String>> targetResultMap = pushResult.getRespTarget();//推送结果，全部推送成功，则map为empty
            if (targetResultMap != null && !targetResultMap.isEmpty()) {
            	log.info("push fail token:" + targetResultMap);
            }
        } else {
            // 调用推送接口服务异常 eg: appId、appKey非法、推送消息非法.....
            // result.code(); //服务异常码
            // result.comment();//服务异常描述
        	log.info(String.format("pushMessage error code:%s comment:%s", result.code(), result.comment()));
        }
	}
	
	/** @Description: 魅族全推 
	* @param pushId
	* @param msgNotice
	* @throws IOException
	**/ 
	public static void pushToAPP(MsgNotice msgNotice) throws IOException{
		String url;
		if(null == msgNotice.getObjectId())
			url="sk://"+appPkgName+"/notification";
		else
			url="sk://"+appPkgName+"/notification?url="+URLEncoder.encode(msgNotice.getObjectId(), "UTF-8");
			
		IFlymePush push = new IFlymePush(appSecret);
		//组装消息
		VarnishedMessage message = new VarnishedMessage.Builder()
				.appId(appId).title(msgNotice.getTitle()).content(msgNotice.getText())
				.noticeBarType(2) // 通知栏样式 0 标准    2安卓原生
				.clickType(2) // 点击动作  0 打开应用    1 打开网页应用     2 打开url页面     3 应用客户端自定义
				.url(url)
				.build();
		ResultPack<Long> result = push.pushToApp(PushType.STATUSBAR,message);
		log.info("IFlymePush result is {}",JSONObject.toJSONString(result));
		if (!result.isSucceed()) {
			log.info("pushMessage error code:%s comment:%s", result.code(), result.comment());
		}
	}
}
