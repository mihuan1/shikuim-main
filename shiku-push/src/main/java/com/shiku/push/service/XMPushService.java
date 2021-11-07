package com.shiku.push.service;



import java.net.URLEncoder;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Message.Builder;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;

import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.vo.MsgNotice;

//小米通知栏推送集成

public class XMPushService extends PushServiceUtils{
	
	public static Sender sender = new Sender(getPushConfig().getXm_appSecret());//申请到的AppSecret
  
	public static void pushToRegId(MsgNotice notice,String callNum,String token){
		if(StringUtil.isEmpty(token))
    		return;
	  if(StringUtils.isEmpty(notice.getText()))
		  notice.setText("收到一条消息...");
	String messagePayload= notice.getText();
	  String title =notice.getTitle(); 
	 String description =notice.getText();
	 if(notice.getIsGroup()){
		 description=notice.getText().replace(notice.getGroupName(), "");
		 title=notice.getTitle();
	 }
	   
	    Message message=null;
	    Builder builder = new Message.Builder()
	                .title(title)
	                .description(description).payload(messagePayload)
	                .restrictedPackageName(appPkgName)
	               .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_LAUNCHER_ACTIVITY);
	                //.extra(Constants.EXTRA_PARAM_INTENT_URI, "intent:#Intent;component=com.xiaomi.mipushdemo/.NewsActivity;end")
	    
	    	//自定义参数 
	    	builder.extra("from",notice.getFrom()+"");
	    	builder.extra("fromUserName",notice.getName()+"");
	    	builder.extra("messageType", notice.getType()+"");
	    	builder.extra("to", notice.getTo()+"");
	    	if(100==notice.getType()||110==notice.getType()||
					115==notice.getType()||120==notice.getType()){
	    		builder.extra("callNum", callNum+"");
//	    		 builder.passThrough(1);  // 不使用透传消息 zhm
	    	}
	    	
			String url = null;
			if(notice.getIsGroup())
				url="intent://"+appPkgName+"/notification#Intent;scheme=sk;launchFlags=0x10000000;S.userId="+notice.getRoomJid()+";end";
			else
				url="intent://"+appPkgName+"/notification#Intent;scheme=sk;launchFlags=0x10000000;S.userId="+notice.getFrom()+";end";
		
			message = builder.notifyType(1).extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_ACTIVITY).extra(Constants.EXTRA_PARAM_INTENT_URI, url)     // 使用默认提示音提示
	                .build();
	    	 
	    try {
			Result result = sender.send(message, token, 3);
			log.info(result.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	  
  }
	
 public static void pushTransToRegId(MsgNotice notice,String callNum,String token){
	  if(StringUtil.isEmpty(token))
  		return;
	  if(StringUtils.isEmpty(notice.getText()))
		  notice.setText("收到一条消息...");
	String messagePayload= notice.getText();
	  String title =notice.getTitle(); 
	 String description =notice.getText();
	 if(notice.getIsGroup()){
		 description=notice.getText().replace(notice.getGroupName(), "");
		 title=notice.getTitle();
	 }
	   
	    Message message=null;
	    Builder builder = new Message.Builder()
	                .title(title)
	                .description(description).payload(messagePayload)
	                .restrictedPackageName(appPkgName)
	               .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_LAUNCHER_ACTIVITY);
	                //.extra(Constants.EXTRA_PARAM_INTENT_URI, "intent:#Intent;component=com.xiaomi.mipushdemo/.NewsActivity;end")
	    
	    	//自定义参数 
	    	builder.extra("from",notice.getFrom()+"");
	    	builder.extra("fromUserName",notice.getName()+"");
	    	builder.extra("messageType", notice.getType()+"");
	    	builder.extra("to", notice.getTo()+"");
	    	
    		builder.extra("callNum", callNum+"");
    		 builder.passThrough(1);
    		 
    		 message = builder.notifyType(1)     // 使用默认提示音提示
	                .build();
	    try {
	    
			Result result = sender.send(message, token, 3);
			log.info(result.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	  
  }
  
 
 	/** @Description:小米的全量推送 
 	* @throws Exception
 	**/ 
 	protected static void sendBroadcast(MsgNotice notice) throws Exception {
 		String url;
 		if(null == notice.getObjectId())
 			url="intent://"+appPkgName+"/notification#Intent;scheme=sk;launchFlags=0x10000000;end";
 		else
 			url="intent://"+appPkgName+"/notification#Intent;scheme=sk;launchFlags=0x10000000;S.url="+URLEncoder.encode(notice.getObjectId(), "UTF-8")+";end";
 		Constants.useOfficial();
	    String messagePayload = "This is a message";
	    String title = notice.getTitle();
	    String description = notice.getText();
//	    String topic = "testTopic";
	    Message message = new Message.Builder()
	                .title(title)
	                .description(description).payload(messagePayload)
	                .restrictedPackageName(appPkgName)
	                .notifyType(1)     // 使用默认提示音提示
	                .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_ACTIVITY).extra(Constants.EXTRA_PARAM_INTENT_URI, url) 
	                .build();
//	    sender.broadcast(message, topic, 3); //根据topic, 发送消息到指定一组设备上
	    Result broadcastAll = sender.broadcastAll(message, 3);
	    log.info(JSONObject.toJSONString(broadcastAll));
	}
 
}

