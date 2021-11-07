package com.shiku.push.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;

import com.alibaba.fastjson.JSONObject;

import cn.xyz.mianshi.vo.MsgNotice;

//华为推送集成通知栏消息
public class HWPushService extends PushServiceUtils{
	 private static String appSecret = getPushConfig().getHw_appSecret();
	 private static  String appId = getPushConfig().getHw_appId();
	 private static  String tokenUrl = getPushConfig().getHw_tokenUrl(); 
	 private static  String apiUrl = getPushConfig().getHw_apiUrl();
	 private static  String iconUrl = getPushConfig().getHw_iconUrl();
	 private static  String accessToken;
	 private static  long tokenExpiredTime;
	 
	/*public static void main(String[] args) throws IOException{
	        sendPushMessage("0865217039424357300001122700CN01");
	 }*/
	 
	//获取下发通知消息的认证Token
    private static  void refreshToken() throws IOException
    {
        String msgBody = MessageFormat.format(
         "grant_type=client_credentials&client_secret={0}&client_id={1}", 
         URLEncoder.encode(appSecret, "UTF-8"), appId);
        String response = httpPost(tokenUrl, msgBody, 10000, 10000);
        JSONObject obj = JSONObject.parseObject(response);
        if(null==obj) {
        	log.error("HWPushService refreshToken response is null ");
        }
        String token = obj.getString("access_token");
        Long expires_in = obj.getLong("expires_in");
        if(null!=token)
        	accessToken = token;
        if(null!=expires_in)
          tokenExpiredTime = System.currentTimeMillis() + obj.getLong("expires_in") - 5*60*1000;
    }
    
    // 发送Push消息
    public static void sendPushMessage(MsgNotice notice,String callNum,String token) throws IOException{
        if (tokenExpiredTime <= System.currentTimeMillis())
        {
            refreshToken();
        }
       
        /*PushManager.requestToken为客户端申请token的方法，可以调用多次以防止申请token失败*/
        /*PushToken不支持手动编写，需使用客户端的onToken方法获取*/
        JSONArray deviceTokens = new JSONArray();//目标设备Token
        deviceTokens.add(token);
        /*deviceTokens.add("22345678901234561234567890123456");
        deviceTokens.add("32345678901234561234567890123456");*/
          
        JSONObject body = new JSONObject();//仅通知栏消息需要设置标题和内容，透传消息key和value为用户自定义
        body.put("title", notice.getTitle());//消息标题
        body.put("content", notice.getText());//消息内容体
        /*if(120==notice.getType()||115==notice.getType()){
        	body.put("callNum", callNum);
		  }*/
        
        JSONObject param = new JSONObject();
//        param.put("appPkgName", appPkgName);//定义需要打开的appPkgName
        String url="intent://"+appPkgName+"/notification#Intent;scheme=sk;launchFlags=0x10000000;S.userId="+notice.getFrom()+";end";
        param.put("intent", url);
        JSONObject action = new JSONObject();
//        action.put("type", 3);//类型3为打开APP，其他行为请参考接口文档设置
        action.put("type", 1);
        action.put("param", param);//消息点击动作参数
        
        JSONObject msg = new JSONObject();
        msg.put("type", 3);//3: 通知栏消息，异步透传消息请根据接口文档设置
        msg.put("action", action);//消息点击动作
        msg.put("body", body);//通知栏消息body内容
        
        JSONObject ext = new JSONObject();//扩展信息，含BI消息统计，特定展示风格，消息折叠。
        ext.put("biTag", "Trump");//设置消息标签，如果带了这个标签，会在回执中推送给CP用于检测某种类型消息的到达率和状态
        ext.put("icon",iconUrl);//自定义推送消息在通知栏的图标,value为一个公网可以访问的URL
        
        JSONObject hps = new JSONObject();//华为PUSH消息总结构体
        hps.put("msg", msg);
        hps.put("ext", ext);
        
        JSONObject payload = new JSONObject();
        payload.put("hps", hps);
        
        String postBody = MessageFormat.format(
         "access_token={0}&nsp_svc={1}&nsp_ts={2}&device_token_list={3}&payload={4}",
            URLEncoder.encode(accessToken,"UTF-8"),
            URLEncoder.encode("openpush.message.api.send","UTF-8"),
            URLEncoder.encode(String.valueOf(System.currentTimeMillis() / 1000),"UTF-8"),
            URLEncoder.encode(deviceTokens.toString(),"UTF-8"),
            URLEncoder.encode(payload.toString(),"UTF-8"));
        
        String postUrl = apiUrl + "?nsp_ctx=" + URLEncoder.encode("{\"ver\":\"1\", \"appId\":\"" + appId + "\"}", "UTF-8");
        httpPost(postUrl, postBody, 5000, 5000);
    }
    
    
    public static  void sendTransMessage(MsgNotice notice,String callNum,String token) throws IOException
    {
        if (tokenExpiredTime <= System.currentTimeMillis())
        {
            refreshToken();
        }
       
        /*PushManager.requestToken为客户端申请token的方法，可以调用多次以防止申请token失败*/
        /*PushToken不支持手动编写，需使用客户端的onToken方法获取*/
        JSONArray deviceTokens = new JSONArray();//目标设备Token       
        deviceTokens.add(token);
        /*deviceTokens.add("22345678901234561234567890123456");
        deviceTokens.add("32345678901234561234567890123456");*/
             
        JSONObject body = new JSONObject();
        body.put("key1", notice.getText());//透传消息自定义body内容
       /* body.put("key2", "value2");//透传消息自定义body内容
        body.put("key3", "value3");//透传消息自定义body内容
*/	        
        JSONObject msg = new JSONObject();
        msg.put("type", 1);//1: 透传异步消息，通知栏消息请根据接口文档设置
        msg.put("body", body.toString());//body内容不一定是JSON，可以是String，若为JSON需要转化为String发送
        
        JSONObject hps = new JSONObject();//华为PUSH消息总结构体
        hps.put("msg", msg);
        
        JSONObject payload = new JSONObject();
        payload.put("hps", hps);
        
        String postBody = MessageFormat.format(
        "access_token={0}&nsp_svc={1}&nsp_ts={2}&device_token_list={3}&payload={4}",
            URLEncoder.encode(accessToken,"UTF-8"),
            URLEncoder.encode("openpush.message.api.send","UTF-8"),
            URLEncoder.encode(String.valueOf(System.currentTimeMillis() / 1000),"UTF-8"),
            URLEncoder.encode(deviceTokens.toString(),"UTF-8"),
            URLEncoder.encode(payload.toString(),"UTF-8"));
        
        String postUrl = apiUrl + "?nsp_ctx=" + URLEncoder.encode("{\"ver\":\"1\", \"appId\":\"" + appId + "\"}", "UTF-8");
        httpPost(postUrl, postBody, 5000, 5000);
    }
    
    public static String httpPost(String httpUrl, String data, int connectTimeout, int readTimeout) {
        OutputStream outPut = null;
        HttpURLConnection urlConnection = null;
        InputStream in = null;
        /*if(KConstants.isDebug) {
        	log.info("httpPost  ==> {}",httpUrl);
        	log.info("data  ==> {}",data);
        }*/
        try
        {
            URL url = new URL(httpUrl);
            urlConnection = (HttpURLConnection)url.openConnection();          
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            urlConnection.setConnectTimeout(connectTimeout);
            urlConnection.setReadTimeout(readTimeout);
            urlConnection.connect();
            
            // POST data
            outPut = urlConnection.getOutputStream();
            outPut.write(data.getBytes("UTF-8"));
            outPut.flush();
            
            // read response
            if (urlConnection.getResponseCode() < 400)
            {
                in = urlConnection.getInputStream();
            }
            else
            {
                in = urlConnection.getErrorStream();
            }
            
            List<String> lines = IOUtils.readLines(in, urlConnection.getContentEncoding());
            StringBuffer strBuf = new StringBuffer();
            for (String line : lines)
            {
                strBuf.append(line);
            }
            System.out.println(strBuf.toString());
            return strBuf.toString();
        }catch (SocketTimeoutException e) {
        	log.error("huawei push Exception {} ",e.getMessage());
			 httpPost(httpUrl, data, 5000, 5000);
			return null;
		}catch (Exception e) {
			log.error("huawei push Exception {} ",e.getMessage());
			 //httpPost(httpUrl, data, 10000, 10000);
			return null;
		}
        finally{
            IOUtils.closeQuietly(outPut);
            IOUtils.closeQuietly(in);
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
        }
    }
    
    
    // 华为批量推送
    public static void fullSendPushMessage(MsgNotice notice,JSONArray tokens) throws IOException{
        if (tokenExpiredTime <= System.currentTimeMillis())
        {
            refreshToken();
        }
        JSONObject body = new JSONObject();//仅通知栏消息需要设置标题和内容，透传消息key和value为用户自定义
        body.put("title", notice.getTitle());//消息标题
        body.put("content", notice.getText());//消息内容体
        
        JSONObject param = new JSONObject();
//        param.put("appPkgName", appPkgName);//定义需要打开的appPkgName
        String url;
        if(null == notice.getObjectId())
        	url="intent://"+appPkgName+"/notification#Intent;scheme=sk;launchFlags=0x10000000;end";
        else
        	url="intent://"+appPkgName+"/notification#Intent;scheme=sk;launchFlags=0x10000000;S.url="+URLEncoder.encode(notice.getObjectId(), "UTF-8")+";end";
        param.put("intent", url);
        JSONObject action = new JSONObject();
//        action.put("type", 3);//类型3为打开APP，其他行为请参考接口文档设置
        action.put("type", 1);
        action.put("param", param);//消息点击动作参数
        
        JSONObject msg = new JSONObject();
        msg.put("type", 3);//3: 通知栏消息，异步透传消息请根据接口文档设置
        msg.put("action", action);//消息点击动作
        msg.put("body", body);//通知栏消息body内容
        
        JSONObject ext = new JSONObject();//扩展信息，含BI消息统计，特定展示风格，消息折叠。
        ext.put("biTag", "Trump");//设置消息标签，如果带了这个标签，会在回执中推送给CP用于检测某种类型消息的到达率和状态
        ext.put("icon",iconUrl);//自定义推送消息在通知栏的图标,value为一个公网可以访问的URL
        
        JSONObject hps = new JSONObject();//华为PUSH消息总结构体
        hps.put("msg", msg);
        hps.put("ext", ext);
        
        JSONObject payload = new JSONObject();
        payload.put("hps", hps);
        
        String postBody = MessageFormat.format(
         "access_token={0}&nsp_svc={1}&nsp_ts={2}&device_token_list={3}&payload={4}",
            URLEncoder.encode(accessToken,"UTF-8"),
            URLEncoder.encode("openpush.message.api.send","UTF-8"),
            URLEncoder.encode(String.valueOf(System.currentTimeMillis() / 1000),"UTF-8"),
            URLEncoder.encode(tokens.toString(),"UTF-8"),
            URLEncoder.encode(payload.toString(),"UTF-8"));
        
        String postUrl = apiUrl + "?nsp_ctx=" + URLEncoder.encode("{\"ver\":\"1\", \"appId\":\"" + appId + "\"}", "UTF-8");
        httpPost(postUrl, postBody, 5000, 5000);
    }
    
}
