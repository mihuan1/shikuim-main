package com.shiku.push.service;


import java.net.URLEncoder;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.vivo.push.sdk.notofication.Message;
import com.vivo.push.sdk.notofication.Result;
import com.vivo.push.sdk.notofication.TargetMessage;
import com.vivo.push.sdk.server.Sender;

import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.vo.MsgNotice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VIVOPushService extends PushServiceUtils{
	private static int appId=getPushConfig().getVivo_appId();
	private static String appKey=getPushConfig().getVivo_appKey();
	private static String appSecret=getPushConfig().getVivo_appSecret();
	private static Sender sender;
	private static Result result;
	
	private static Sender getSender() throws Exception{
		if(null == sender){
			Sender appSender = new Sender(appSecret);//注册登录开发平台网站获取到的appSecret
			sender = appSender;
		}
		return sender;
	}
	private static Result getResult() throws Exception{
		if(null == result){
			Result appResult = getSender().getToken(appId , appKey);//注册登录开发平台网站获取到的appId和appKey
			result = appResult;
		}
		return result;
	}
	
	/** @Description: 通知栏消息
	* @param pushId
	* @param msgNotice
	* @return
	* @throws Exception
	**/ 
	public static Message buildMessage(String pushId,MsgNotice msgNotice) throws Exception {
		String url = null;
		if(msgNotice.getIsGroup())
			url="intent://"+appPkgName+"/notification#Intent;scheme=sk;launchFlags=0x10000000;S.userId="+msgNotice.getRoomJid()+";end";
		else
			url="intent://"+appPkgName+"/notification#Intent;scheme=sk;launchFlags=0x10000000;S.userId="+msgNotice.getFrom()+";end";
        Message message = new Message.Builder()
                .regId(pushId)//仅构建单推消息体需要
                .notifyType(2)// 响铃
                .title(msgNotice.getTitle())// 通知标题
                .content(msgNotice.getText())// 通知内容
                .timeToLive(1000)// 消息的生命周期,消息在服务器保存的时间, 单位: 秒
                .skipType(4)// 跳转类型1：打开APP首页 2：打开链接  3：自定义 4：打开app内指定页面
                .skipContent(url)
                .networkType(-1)// 可选项，发送推送使用的网络方式(-1 :任何网络下，1：仅wifi下)
//                .extra("http://www.vivo.com", "vivo")
                .requestId(pushId).build();
        return message;
    }
	
	public static void noticeColumnMessagePush(String pushId,MsgNotice msgNotice) throws Exception{
//		allSend(pushId,msgNotice);
		Sender senderMessage = new Sender(appSecret,getResult().getAuthToken());
		Result resultMessage = senderMessage.sendSingle(buildMessage(pushId,msgNotice));
		if(null != resultMessage && 0 != resultMessage.getResult())
			log.info("VIVOPush error code:%s comment:%s", resultMessage.getResult(),resultMessage.getDesc());
		
	}
	
	
	/** @Description:全量推送 
	* @param pushId
	* @param msgNotice
	* @throws Exception
	**/ 
	public static void allSend(String pushId,MsgNotice msgNotice) throws Exception {
		
		Sender sender1 = new Sender(appSecret);//实例化Sender
		
		Result result = sender1.getToken(appId,appKey);//发送鉴权请求
		
		Sender sender = new Sender(appSecret, result.getAuthToken());
		sender.initPool(20,10);//设置连接池参数，可选项
        Message allSend = new Message.Builder()
        		.notifyType(2)// 响铃
                .title(msgNotice.getTitle())// 通知标题
                .content(msgNotice.getText())// 通知内容
                .timeToLive(1000)// 消息的生命周期,消息在服务器保存的时间, 单位: 秒
                .skipType(2)// 跳转类型1：打开APP首页 2：打开链接  3：自定义 4：打开app内指定页面
                .skipContent("https://www.baidu.com")
                .requestId(StringUtil.randomUUID())
                .build();//构建要全量推送的消息体
        Result results = sender.sendToAll(allSend);//发送全量推送消息请求
//        result.getResult();//获取服务器返回的状态码，0成功，非0失败
//        result.getDesc();//获取服务器返回的调用情况文字描述
//        result.getTaskId();//如请求发送成功，将获得该条消息的任务编号，即taskId
        System.out.println(" vivo result : "+JSONObject.toJSONString(results));
    }
	
	public static String saveListPayload(MsgNotice msgNotice) throws Exception {
		String url;
		if(null == msgNotice.getObjectId())
			url="intent://"+appPkgName+"/notification#Intent;scheme=sk;launchFlags=0x10000000;end";
		else
			url="intent://"+appPkgName+"/notification#Intent;scheme=sk;launchFlags=0x10000000;S.url="+URLEncoder.encode(msgNotice.getObjectId(), "UTF-8")+";end";
		log.info("vivo save listPayload url : {}",url);
		Sender sender1 = new Sender(appSecret);// 实例化Sender
		Result result = sender1.getToken(appId, appKey);// 发送鉴权请求
		Sender sender = new Sender(appSecret, result.getAuthToken());
		sender.initPool(20, 10);// 设置连接池参数，可选项
		Message saveList = new Message.Builder().notifyType(2)// 响铃
				.title(msgNotice.getTitle())// 通知标题
				.content(msgNotice.getText())// 通知内容
				.timeToLive(1000)// 消息的生命周期,消息在服务器保存的时间, 单位: 秒
				.skipType(4)// 跳转类型1：打开APP首页 2：打开链接  3：自定义 4：打开app内指定页面
	            .skipContent(url)
				.requestId(StringUtil.randomUUID()).build();// 构建要保存的批量推送消息体
		Result resultPayload = sender.saveListPayLoad(saveList);// 发送保存群推消息请求
		resultPayload.getResult();// 获取服务器返回的状态码，0成功，非0失败
		resultPayload.getDesc();// 获取服务器返回的调用情况文字描述
		resultPayload.getTaskId();// 如请求发送成功，将获得该条消息的任务编号，即taskId
		return resultPayload.getTaskId();
	}
	
	/** @Description: 批量推送
	* @param msgNotice
	* @param regIds
	* @throws Exception
	**/ 
	public static void listSend(MsgNotice msgNotice,Set<String> regIds) throws Exception {
		Sender senderSecret = new Sender(appSecret);//实例化Sender
		Result result = senderSecret.getToken(appId,appKey);//发送鉴权请求
        Sender sender = new Sender(appSecret,result.getAuthToken());
        sender.initPool(20,10);//设置连接池参数，可选项
//        Set<String> regid = new HashSet<>();//构建批量推送用户群
//        regid.add(saveListPayload());// 2 - 1000
        TargetMessage targetMessage = new TargetMessage.Builder()
        		.taskId(saveListPayload(msgNotice))
        		.regIds(regIds)
        		.requestId(StringUtil.randomUUID())
        		.build();//构建批量推送的消息体
        Result resultTarget = sender.sendToList(targetMessage);//批量推送给用户
        resultTarget.getResult();//获取服务器返回的状态码，0成功，非0失败
        resultTarget.getDesc();//获取服务器返回的调用情况文字描述
        log.info("VIVO resultTarget 批量推送 ： {}",JSONObject.toJSONString(resultTarget));
	}
	
}
