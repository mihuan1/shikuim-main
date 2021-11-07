package cn.xyz.mianshi.utils;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.support.jedis.RedisCRUD;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.model.KSession;
import cn.xyz.mianshi.vo.ClientConfig;
import cn.xyz.mianshi.vo.Config;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.User.DeviceInfo;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public final class KSessionUtil {

	/**
	 * 根据用户Id获取access_token
	 */
	//public static final String GET_USERID_BYTOKEN = "at_%1$s";
	public static final String GET_USERID_BYTOKEN = "loginToken:userId:%s";
	
	//public static final String GET_ACCESS_TOKEN_BY_USER_ID ="uk_%1$s";
	public static final String GET_ACCESS_TOKEN_BY_USER_ID = "loginToken:token:%s";
	
	// 根据access_token获取管理员userId
	public static final String GET_ADMIN_USERID_BYTOKEN ="adminToken:userId:%s";
	
	// 根据管理员userId获取access_token
	public static final String GET_ADMIN_TOKEN_BY_USER_ID = "adminToken:token:%s";
	
	

	/**
	 * 根据access_token获取Session
	 */
	public static final String GET_SESSION_BY_ACCESS_TOKEN = "login:%s:session";
	
	
	public static final String GET_USER_BY_USERID = "user:%s:data";
	
	public static final String GET_CONFIG = "app:config";
	
	public static final String GET_CLIENTCONFIG = "clientConfig";
	
	
	//apns推送
	public static final String GET_APNS_KEY = "apns:%s:token";
	
	/**
	 * ios 推送
	 */
	public static final String GET_PUSH_IOS_KEY="impush:ios:%s";
	
	/**
	 * android 推送
	 */
	public static final String GET_PUSH_Android_KEY="impush:andoird:%s";
	
	public static final String GET_USER_MsgNum="userMsgNum:%s";
	
	public static RedisCRUD getRedisCRUD(){
		return SKBeanUtils.getRedisCRUD();
	}
	public static void setConfig(Config config) {
		getRedisCRUD().set(GET_CONFIG, config.toString());
	}
	public static Config getConfig() {
		String config=getRedisCRUD().get(GET_CONFIG);
		return StringUtil.isEmpty(config) ? null : JSON.parseObject(config, Config.class);
	}
	public static void setClientConfig(ClientConfig clientConfig){
		getRedisCRUD().set(GET_CLIENTCONFIG, clientConfig.toString());
	}
	public static ClientConfig getClientConfig() {
		String config=getRedisCRUD().get(GET_CLIENTCONFIG);
		return StringUtil.isEmpty(config) ? null : JSON.parseObject(config, ClientConfig.class);
	}
	
	public static Map<String, Object> loginSaveAccessToken(Object userKey,Object userId,String accessToken) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		try {
			
			int expire=KConstants.Expire.DAY7*5;
			String atKey = String.format(GET_ACCESS_TOKEN_BY_USER_ID, userKey);
			if(StringUtil.isEmpty(accessToken))
				accessToken=SKBeanUtils.getRedisCRUD().get(atKey);
			if(StringUtil.isEmpty(accessToken))
				accessToken = StringUtil.randomUUID();
			SKBeanUtils.getRedisCRUD().setWithExpireTime(atKey, accessToken,expire);
			
			String userIdKey =String.format(GET_USERID_BYTOKEN, accessToken);
			SKBeanUtils.getRedisCRUD().setWithExpireTime(userIdKey, String.valueOf(userId),expire);
			
			data.put("access_token", accessToken);
			data.put("expires_in",expire);
			// data.put("userId", userId);
			// data.put("nickname", user.getNickname());

			return data;
		}catch (Exception e) {
			e.printStackTrace();
			return data;
		} 
	}
	
	/**
	 * 保存所有后台登录的用户Token
	 * @param userId
	 * @param adminToken
	 * @return
	 */
	public static Map<String, Object> adminLoginSaveToken(Object userId,String adminToken){
		HashMap<String, Object> data = new HashMap<String, Object>();
		try {
			int expire = KConstants.Expire.HALF_AN_HOUR;
			
			String userIdKey = String.format(GET_ADMIN_TOKEN_BY_USER_ID, userId);
			if(StringUtil.isEmpty(adminToken))
				//根据 userId 到redis 查找 token
				adminToken = SKBeanUtils.getRedisCRUD().get(userIdKey);
			
			if(StringUtil.isEmpty(adminToken))
				// redis 中不存在则生成一个token保存到redis中
				adminToken = StringUtil.randomUUID();  
				
			SKBeanUtils.getRedisCRUD().setWithExpireTime(userIdKey, adminToken, expire);
				
			String tokenKey = String.format(GET_ADMIN_USERID_BYTOKEN, adminToken);
			SKBeanUtils.getRedisCRUD().setWithExpireTime(tokenKey, String.valueOf(userId), expire);
				
			data.put("access_Token", adminToken);
			data.put("expires_in", expire);
			
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return data;
		}
	}
	
	public static void removeAdminToken(Object userKey){
		log.info(" removeAdminToken ===== userKey ===== :"+userKey);
		
		String key = String.format(GET_ADMIN_TOKEN_BY_USER_ID, userKey);
		String admin_token = SKBeanUtils.getRedisCRUD().get(key);
		
		if(!StringUtil.isEmpty(admin_token))
			getRedisCRUD().delete(key);
		if(!StringUtil.isEmpty(admin_token)){
			String userIdKey = String.format(GET_ADMIN_USERID_BYTOKEN, admin_token);
			SKBeanUtils.getRedisCRUD().del(userIdKey);
		}
		
	}
	

	
	public static void  removeAccessToken(Object userKey) {
		log.info("  removeAccessToken  =====  userKey  ======= :"+userKey);
		// 根据userKey拿token
		String key = String.format(GET_ACCESS_TOKEN_BY_USER_ID, userKey);
		String access_token = SKBeanUtils.getRedisCRUD().get(key);
		
		
		if (!StringUtil.isEmpty(access_token)) {
			getRedisCRUD().delete(key);
		}
		if (!StringUtil.isEmpty(access_token)) {
			String userIdKey =String.format(GET_USERID_BYTOKEN, access_token);
			SKBeanUtils.getRedisCRUD().del(userIdKey);
		}
	}
	
	public static void  removeToken(Object token) {
		String userId=null;
			if (!StringUtil.isEmpty(token.toString())) {
				String userIdKey =String.format(GET_USERID_BYTOKEN, token);
				userId=SKBeanUtils.getRedisCRUD().get(userIdKey);
				SKBeanUtils.getRedisCRUD().del(userId);
			}
			
			// 根据userKey拿token
			String key = String.format(GET_ACCESS_TOKEN_BY_USER_ID, userId);
			String access_token = SKBeanUtils.getRedisCRUD().get(key);
			if (!StringUtil.isEmpty(access_token)) {
				getRedisCRUD().delete(key);
			}
			
	}
	
	
	public static KSession getSession(String access_token) {
		String key = String.format(GET_SESSION_BY_ACCESS_TOKEN, access_token);
		if(StringUtil.isEmpty(access_token))
			return null;
		String value = getRedisCRUD().get(key);
		return StringUtil.isEmpty(value) ? null : JSON.parseObject(value, KSession.class);
	}
	public static void saveSession(String access_token, KSession kSession) {
				String key = String.format(GET_SESSION_BY_ACCESS_TOKEN, access_token);
				String value = kSession.toString();
				getRedisCRUD().set(key, value);
	}

	public static void setAccessToken(String access_token, KSession kSession) {
		String key = String.format(GET_SESSION_BY_ACCESS_TOKEN, access_token);
				String value = kSession.toString();
				getRedisCRUD().set(key, value);
				//pipe.expire(key, KConstants.Expire.DAY7);

				key = String.format(GET_ACCESS_TOKEN_BY_USER_ID, kSession.getUserId());
				value = access_token;
				getRedisCRUD().set(key, value);
				//pipe.expire(key, KConstants.Expire.DAY7);

				
			
	}


	public static String getAccess_token(long userId){
		String key = String.format(GET_ACCESS_TOKEN_BY_USER_ID,userId);
		return getRedisCRUD().get(key);
	}
	public static String getUserIdBytoken(String token){
		String key = String.format(GET_USERID_BYTOKEN,token);
		return getRedisCRUD().get(key);
	}
	
	public static String getAdminToken(long userId){
		String key = String.format(GET_ADMIN_TOKEN_BY_USER_ID, userId);
		return getRedisCRUD().get(key);
	}
	public static String getAdminUserIdByToken(String token){
		String key = String.format(GET_ADMIN_USERID_BYTOKEN, token);
		return getRedisCRUD().get(key);
	}
	
	
	/**
	 * User
	 * @param userId
	 * @return
	 */
	public static User getUserByUserId(Integer userId) {
		String key = String.format(GET_USER_BY_USERID, userId);
		String value = getRedisCRUD().get(key);
		User user = null;
		try {
			user = JSON.parseObject(value, User.class);
		} catch (JSONException e) {
			return null;
		}
//		return StringUtil.isEmpty(value) ? null : JSON.parseObject(value, User.class);
		return user;
		
	}
	public static void saveUserByUserId(Integer userId,User user) {
		String key = String.format(GET_USER_BY_USERID, userId);
		getRedisCRUD().setWithExpireTime(key, user.toString(),KConstants.Expire.DAY1);
		
	}
	public static void deleteUserByUserId(Integer userId) {
		String key = String.format(GET_USER_BY_USERID, userId);
		getRedisCRUD().del(key);
	}
	
	
	
	
	/**
	* @Description: TODO(保存 ios 设备推送 的 信息)
	* @param @param userId
	* @param @param info    参数
	 */
	public static void saveIosPushToken(Integer userId,DeviceInfo info){
		String key=String.format(GET_PUSH_IOS_KEY, userId);
		getRedisCRUD().setWithExpireTime(key, info.toString(),KConstants.Expire.DAY7*5);
		
		
	}
	/**
	* @Description: TODO(获取 ios 设备推送 的 信息)
	* @param @param userId
	* @param @return    参数
	 */
	public static DeviceInfo getIosPushToken(Integer userId){
		String key=String.format(GET_PUSH_IOS_KEY, userId);
		String value = getRedisCRUD().get(key);
		return StringUtil.isEmpty(value) ? null : JSON.parseObject(value, DeviceInfo.class);
		
	}
	public static void removeIosPushToken(Integer userId){
		String key=String.format(GET_PUSH_IOS_KEY, userId);
		getRedisCRUD().del(key);
	}
	/**
	* @Description: TODO(保存 android 设备推送 的 信息)
	* @param @param userId
	* @param @param info    参数
	 */
	public static void saveAndroidPushToken(Integer userId,DeviceInfo info){
		String key=String.format(GET_PUSH_Android_KEY, userId);
		getRedisCRUD().setWithExpireTime(key, info.toString(),KConstants.Expire.DAY7);
		
		
	}
	/**
	* @Description: TODO(获取 android 设备推送 的 信息)
	* @param @param userId
	* @param @return    参数
	 */
	public static DeviceInfo getAndroidPushToken(Integer userId){
		String key=String.format(GET_PUSH_Android_KEY, userId);
		String value = getRedisCRUD().get(key);
		return StringUtil.isEmpty(value) ? null : JSON.parseObject(value, DeviceInfo.class);
		
	}
	public static void removeAndroidPushToken(Integer userId){
		String key=String.format(GET_PUSH_Android_KEY, userId);
		getRedisCRUD().del(key);
	}
	
	
	public static void saveAPNSToken(String regId,Integer userId) {
		String key = String.format(GET_APNS_KEY, String.valueOf(userId));
		getRedisCRUD().setWithExpireTime(key, regId,KConstants.Expire.DAY7);
	}
	public static String getAPNSToken(Integer userId) {
		String key = String.format(GET_APNS_KEY, userId);
		return getRedisCRUD().get(key);
	}
	
	public static synchronized void saveUserMsgNum(Integer userId,Integer num) {
		String key = String.format(GET_USER_MsgNum, String.valueOf(userId));
		getRedisCRUD().setWithExpireTime(key, num.toString(),KConstants.Expire.DAY1*3);
	}
	public static synchronized Integer getUserMsgNum(Integer userId) {
		String key = String.format(GET_USER_MsgNum, userId);
		try {
			return Integer.valueOf(getRedisCRUD().get(key));
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static final String GET_ADDRESS_BYIP="clientIp:%s";
	public static String getAddressByIp(String ip){
		String key = String.format(GET_ADDRESS_BYIP, ip);
		return getRedisCRUD().get(key);
	}
	public static void setAddressByIp(String ip,String address){
		String key = String.format(GET_ADDRESS_BYIP, ip);
		getRedisCRUD().setWithExpireTime(key, address,KConstants.Expire.HOUR12);
		
	}
	
	
	
	
}
