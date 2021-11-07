package cn.xyz.commons.utils;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import cn.xyz.commons.autoconfigure.KApplicationProperties.WXConfig;
import cn.xyz.mianshi.utils.SKBeanUtils;

/**
 * 获取微信 用户信息  工具类
 * @author lidaye
 *
 */
public class WXUserUtils {
	
	private final static String GETOPENIDURL=
			"https://api.weixin.qq.com/sns/oauth2/access_token";

	private final static String GETTOKEN=
			"https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	
	private final static String GETUSERURL=
			"https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID";
	
	private static WXConfig wxConfig =SKBeanUtils.getLocalSpringBeanManager().getApplicationConfig().getWxConfig();
	
	/*private static WXConfig wxConfig=null;
	static {
		wxConfig=new WXConfig();
		wxConfig.setAppid("wx373339ef4f3cd807");
		wxConfig.setSecret("ec6e99350b0fdb428cf50a5be403b268");
	}*/
	/**
	 * 获取 微信用户 openId
	 * @param code
	 * @return
	 */
	public static JSONObject  getWxOpenId(String code) {
		Map<String, String> params=new HashMap<>();
		params.put("grant_type","authorization_code");
		params.put("appid", wxConfig.getAppid());
		params.put("secret",wxConfig.getSecret());
		params.put("code", code);
		String result=HttpUtil.URLGet(GETOPENIDURL, params);
	
		System.out.println("\n\n getWxOpenId ===> "+result);
		return JSONObject.parseObject(result);
		
	}
	
	/**
	 * 获取微信Token
	 * @param openId
	 * @return
	 */
	public static JSONObject getWxToken(){
		Map<String, String> params=new HashMap<>();
		params.put("appid", wxConfig.getAppid());
		params.put("secret", wxConfig.getSecret());
		
		String result=HttpUtil.URLGet(GETTOKEN, params);
		return JSONObject.parseObject(result);
	}
	/**
	 * 获取微信 用户资料
	 * @param token
	 * @param openid
	 * @return
	 */
	public static JSONObject  getWxUserInfo(String token,String openid) {
		Map<String, String> params=new HashMap<>();
		params.put("access_token",token);
		params.put("openid", openid);
		
		String result=HttpUtil.URLGet(GETUSERURL, params);
	
		System.out.println("\n\n getWxUserInfo ===> "+result);
		return JSONObject.parseObject(result);
		
	}
	
}
