package com.shiku.push.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import cn.xyz.commons.utils.FileUtil;
import cn.xyz.commons.utils.Md5Util;
import cn.xyz.mianshi.vo.MsgNotice;

public class BaiduPushService extends PushServiceUtils{

	public static class PushMessage {
		public static class Aps {
			private String alert;
			private int badge;
			private String sound="default";

			public Aps() {
				super();
			}

			public Aps(String alert) {
				super();
				this.alert = alert;
			}

			public String getAlert() {
				return alert;
			}

			public int getBadge() {
				return badge;
			}

			public String getSound() {
				return sound;
			}

			public void setAlert(String alert) {
				this.alert = alert;
			}

			public void setBadge(int badge) {
				this.badge = badge;
			}

			public void setSound(String sound) {
				this.sound = sound;
			}
			@Override
			public String toString() {
				return JSON.toJSONString(this);
			}
		}

		private Aps aps;
		private String description;// 内容
		private String title;// 标题

		private String name;
		private int status;// 状态：0=未读；1=已读
		private long time;
		private String toName;
		private int type;//消息类型
		private long toUserId; // 用户Id（个人用户或企业用户）
		private long userId;// 用户Id（个人用户或企业用户）
		/**
		 * 
		 */
		public PushMessage() {
			// TODO Auto-generated constructor stub
		}
		
		public PushMessage(MsgNotice notice) {
			this.userId = notice.getFrom();
			this.name = notice.getName();
			this.toUserId = notice.getTo();
			this.toName = "";
			this.time = notice.getTime();
			this.status = 0;

			this.title = notice.getTitle();
			this.description = notice.getText();
			this.aps = new Aps(notice.getText());
			this.aps.setSound("default");
			this.aps.setBadge(notice.getMsgNum());
		}
		public void bindMsg(PushMessage msg){
			
		}

		public Aps getAps() {
			return aps;
		}

		public void setAps(Aps aps) {
			this.aps = aps;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public String getToName() {
			return toName;
		}

		public void setToName(String toName) {
			this.toName = toName;
		}

		public long getToUserId() {
			return toUserId;
		}

		public void setToUserId(long toUserId) {
			this.toUserId = toUserId;
		}

		public long getUserId() {
			return userId;
		}

		public void setUserId(long userId) {
			this.userId = userId;
		}

		@Override
		public String toString() {
			return JSON.toJSONString(this);
		}
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}

	}
	
	public static final String APPSTORE_APPID=getPushConfig().getBd_appStore_appId();
	                                             
	public static final String APPSTORE_APPKEY=getPushConfig().getBd_appStore_appKey();
	                                                
	public static final String APPSTORE_SECRET_KEY=getPushConfig().getBd_appStore_secret_key();
	
	// EMPTY，安卓，iOS
	public static final String[] APP_KEY = (String[]) getPushConfig().getBd_appKey();
	public static final String REST_URL = getPushConfig().getBd_rest_url();
	public static final String[] SECRET_KEY = (String[]) getPushConfig().getBd_secret_key();

	private static byte[] getBytes(Map<String, Object> params) throws Exception {
		StringBuffer sb = new StringBuffer();
		Iterator<String> iter = params.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			// String value = URLEncoder.encode(params.get(key).toString(),
			// "UTF-8");
			sb.append(key).append('=').append(params.get(key)).append('&');
		}
		return sb.substring(0, sb.length() - 1).getBytes("UTF-8");
	}

	private static String getSign(int deviceId, String url, String method, Map<String, Object> params,String appId)
			throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(method).append(url);
		Iterator<String> iter = params.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			sb.append(key).append('=').append(params.get(key));
		}
		String baseStr =null;
		if(APPSTORE_APPID.equals(appId))
			 sb.append(APPSTORE_SECRET_KEY);
		else
			sb.append(SECRET_KEY[deviceId]);
		baseStr=sb.toString();
		// String signStr = DigestUtils.md5Hex(URLEncoder.encode(baseStr,
		// "UTF-8").replaceAll("\\*", "%2A"));
		String signStr = Md5Util.md5Hex(URLEncoder.encode(baseStr, "UTF-8").replaceAll("\\*", "%2A"));
		return signStr;
	}

	// private static String getUserAgent() {
	// StringBuffer sb = new StringBuffer();
	// String sysName = System.getProperty("os.name");
	// String sysVersion = System.getProperty("os.version");
	// String sysArch = System.getProperty("os.arch");
	// String sysLangVersion = System.getProperty("java.version");
	// sb.append("BCCS_SDK/3.0 (");
	// if (sysName != null) {
	// sb.append(sysName).append("; ");
	// }
	// if (sysVersion != null) {
	// sb.append(sysVersion).append("; ");
	// }
	// if (sysArch != null) {
	// sb.append(sysArch);
	// }
	// sb.append(") ").append("JAVA/").append(sysLangVersion).append(" (Baidu
	// Push Server SDK V.2.0.0)");
	// return sb.toString();
	// }

	public static String getString(String spec, String method, Map<String, Object> params) throws Exception {
		URL url = new URL(spec);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestMethod(method);
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		con.setRequestProperty("User-Agent",
				"BCCS_SDK/3.0 (Windows 7; 6.1; amd64) JAVA/1.8.0_05 (Baidu Push Server SDK V.2.0.0)");
		con.connect();

		OutputStream out = con.getOutputStream();
		out.write(getBytes(params));
		out.flush();
		out.close();

		if (200 == con.getResponseCode()) {
			return FileUtil.readAll(con.getInputStream());
		} else {
			return FileUtil.readAll(con.getErrorStream());
		}
	}

	public static void pushAll(int deviceId, PushMessage msg) {
		try {
			String spec = REST_URL + "/rest/3.0/push/all";
			String method = "POST";
			Map<String, Object> params = Maps.newTreeMap();
			params.put("apikey", getAppKey(deviceId));
			params.put("timestamp", System.currentTimeMillis() / 1000);
			params.put("msg", msg.toString());
			params.put("msg_type", 1);
			// params.put("deploy_status", 1);
			params.put("sign", getSign(deviceId, spec, method, params,""));
			String s = getString(spec, method, params);
			System.out.println(s);
		} catch (Exception e) {
			System.out.println(" BaiduPushService pushAll "+e.getMessage());
		}
	}

	public static String pushSingle(int deviceId, String channelId, PushMessage msg,String appId) {
		try {
			String spec = REST_URL + "/rest/3.0/push/single_device";
			String method = "POST";
			Map<String, Object> params = Maps.newTreeMap();
			if(APPSTORE_APPID.equals(appId))
				params.put("apikey",APPSTORE_APPKEY);
			else
				params.put("apikey", getAppKey(deviceId));
			params.put("timestamp", System.currentTimeMillis() / 1000);
			params.put("channel_id", channelId);
			params.put("msg_type", 1);
			 //1：开发状态；2：生产状态； 若不指定，则默认设置为生产状态。
			params.put("deploy_status",2); 
			if(deviceId==2)
			params.put("msg", msg.toString());
			else
				params.put("msg", msg.toString());
			params.put("sign", getSign(deviceId, spec, method, params,appId));

			String s = getString(spec, method, params);
			return s;
		} catch (Exception e) {
			System.out.println(" BaiduPushService pushSingle "+e.getMessage());
			return null;
		}
	}
	

	private static String getAppKey(int deviceId) {
		return APP_KEY[deviceId];
	}

}
