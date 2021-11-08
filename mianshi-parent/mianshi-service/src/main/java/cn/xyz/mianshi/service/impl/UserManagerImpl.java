package cn.xyz.mianshi.service.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.xyz.mianshi.vo.*;
import com.mongodb.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.MsgType;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.Md5Util;
import cn.xyz.commons.utils.RandomUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.commons.utils.ValueUtil;
import cn.xyz.commons.utils.WXUserUtils;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.KSession;
import cn.xyz.mianshi.model.NearbyUser;
import cn.xyz.mianshi.model.PageVO;
import cn.xyz.mianshi.model.UserExample;
import cn.xyz.mianshi.model.UserQueryExample;
import cn.xyz.mianshi.service.UserManager;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.User.DeviceInfo;
import cn.xyz.mianshi.vo.User.UserLoginLog;
import cn.xyz.mianshi.vo.User.UserSettings;
import cn.xyz.repository.mongo.UserRepositoryImpl;
import cn.xyz.service.KSMSServiceImpl;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;
import cn.xyz.service.RedisServiceImpl;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@SuppressWarnings("deprecation")
@Service(UserManagerImpl.BEAN_ID)
public class UserManagerImpl extends MongoRepository<User, Integer> implements UserManager {
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getLocalSpringBeanManager().getDatastore();
	}

	@Override
	public Class<User> getEntityClass() {
		return User.class;
	}
	public static final String BEAN_ID = "UserManagerImpl";

	private static UserRepositoryImpl getUserRepository(){
		return SKBeanUtils.getUserRepository();
	}
	private static RedisServiceImpl getRedisServiceImpl(){
		return SKBeanUtils.getRedisService();
	}

	@Override
	public User createUser(String telephone, String password) {
		User user = new User();
		user.setUserId(createUserId());
		user.setUserKey(DigestUtils.md5Hex(telephone));
		user.setPassword(DigestUtils.md5Hex(password));
		user.setTelephone(telephone);

		getUserRepository().addUser(user);

		return user;
	}

	@Override
	public void createUser(User user) {
		getUserRepository().addUser(user);

	}

	@Override
	public User.UserSettings getSettings(int userId) {
		UserSettings settings=null;
		User user=null;
		user=getUser(userId);
		if(null==user)
			return null;
		settings=user.getSettings();
		return null!=settings?settings:new UserSettings();
	}

	@Override
	public User getUser(int userId) {
		//先从 Redis 缓存中获取
		User user =KSessionUtil.getUserByUserId(userId);
		if(null==user){
				user = getUserRepository().getUser(userId);
			if (null == user){
				log.info("该用户不存在, userId: {}",userId);
				throw new ServiceException("该用户不存在!");
			}
			KSessionUtil.saveUserByUserId(userId, user);
		}

		return user;
	}
	public User getUserByAccount(String account) {
		//先从 Redis 缓存中获取
		User user =SKBeanUtils.getRedisService().queryUserByAccount(account);
		if(null==user){
				user = getUserRepository().queryOne("account", account);
			if (null == user){
				log.info("该用户不存在, account: {}",account);
				return null;
			}
			SKBeanUtils.getRedisService().saveUserByAccount(account, user);
		}

		return user;
	}


	/* (non-Javadoc)
	 * @see cn.xyz.mianshi.service.UserManager#getNickName(int)
	 */
	@Override
	public String getNickName(int userId) {
		String nickName=SKBeanUtils.getRedisService().queryUserNickName(userId);
		if(!StringUtil.isEmpty(nickName))
			return nickName;
		return (String) queryOneFieldById("nickname", userId);
	}


	public synchronized int getMsgNum(int userId) {
		int userMsgNum = KSessionUtil.getUserMsgNum(userId);
		if(0!=userMsgNum) {
			return userMsgNum;
		}else {
			userMsgNum=(int)queryOneFieldById("msgNum",userId);
			 KSessionUtil.saveUserMsgNum(userId, userMsgNum);
		}
		/*
		 * if(0==userMsgNum){ updateAttributeByIdAndKey(userId, "msgNum", 0); return 0;
		 * }
		 */

		 return userMsgNum;
	}

	public synchronized void changeMsgNum(int userId,int num) {
		KSessionUtil.saveUserMsgNum(userId, num);
		UpdateOperations<User> operations = createUpdateOperations();
		operations.set("msgNum", num);
		 updateAttributeByOps(userId, operations);
	}

	/** @Description:（锁定解锁用户状态）
	* @param userId
	* @param status
	**/
	public void changeStatus(int userId,int status) {
		UpdateOperations<User> operations = createUpdateOperations();
		operations.set("status", status);
		updateAttributeByOps(userId, operations);
		//维护redis中的数据
		KSessionUtil.removeAccessToken(userId);
		KSessionUtil.deleteUserByUserId(userId);
	}
	public void uploadKey(int userId,String publicKey,String privateKey) {
		UpdateOperations<User> operations = createUpdateOperations();
		operations.set("publicKey", publicKey);
		operations.set("privateKey", privateKey);
		updateAttributeByOps(userId, operations);
		KSessionUtil.deleteUserByUserId(userId);
	}
	@Override
	public User getUser(int userId, int toUserId) {
		User user = getUser(toUserId);
		if(null != user){
			Friends friends = SKBeanUtils.getFriendsManager().getFriends(new Friends(userId, toUserId));
			user.setFriends(null == friends ? null : friends);
			if(userId == toUserId){
				List<Integer> userRoles = SKBeanUtils.getRoleManager().getUserRoles(userId);
				user.setRole(userRoles);
			}
			// 隐私设置数据
			setUserSettingInfo(user,userId,toUserId);
		}else{
			throw new ServiceException("该用户不存在!");
		}
		return user;
	}

	private void setUserSettingInfo(User user, Integer userId, Integer toUserId) {
		String phone = getUser(userId).getPhone();
		if(!StringUtil.isEmpty(phone) && !phone.equals("18938880001")){
			// 上线时间显示
			UserLoginLog loginLog = getDatastore().createQuery(UserLoginLog.class).field("userId").equal(toUserId).get();
			if(null != user.getSettings()){
				if (-1 != user.getSettings().getShowLastLoginTime()) {
					boolean flag = SKBeanUtils.getFriendsManager().isAddressBookOrFriends(userId, toUserId,
							user.getSettings().getShowLastLoginTime());
					if (flag&&null!=loginLog&&null!=loginLog.getLoginLog())
						user.setShowLastLoginTime(loginLog.getLoginLog().getLoginTime());
				}else if (-1 == user.getSettings().getShowLastLoginTime() && userId.equals(toUserId)) {
					if(null!=loginLog&&null!=loginLog.getLoginLog())
						user.setShowLastLoginTime(loginLog.getLoginLog().getLoginTime());
				}
				// 手机号显示
				if (-1 == user.getSettings().getShowTelephone() && !userId.equals(toUserId)) {
					user.setAreaCode("");
					user.setTelephone("");
					user.setPhone("");
				} else if (2 == user.getSettings().getShowTelephone() || 3 == user.getSettings().getShowTelephone()) {
					if(userId.equals(toUserId))
						return;
					boolean flag = SKBeanUtils.getFriendsManager().isAddressBookOrFriends(userId, toUserId,
							user.getSettings().getShowTelephone());
					if (!flag) {
						user.setAreaCode("");
						user.setTelephone("");
						user.setPhone("");
					}
				}
			}
	}

	}

	@Override
	public User getUser(String telephone) {
		//Integer userId=KSessionUtil.getUserIdByTelephone(telephone);
		User user=getUserRepository().getUser(telephone);
		return user;
	}
	/**
	 *
	* @Description: TODO(获取登陆过的设备列表)
	* @param @return    参数
	 */
	public Map<String, DeviceInfo> getLoginDeviceMap(Integer userId){
		Query<UserLoginLog> query=getDatastore().createQuery(UserLoginLog.class);
		UserLoginLog userLoginLog = query.filter("_id", userId).get();
		if(null==userLoginLog)
			return null;
		return userLoginLog.getDeviceMap();

	}

	@Override
	public int getUserId(String accessToken) {
		return 0;
	}

	@Override
	public boolean isRegister(String telephone) {
		return 1 == getUserRepository().getCount(telephone);
	}

	@Override
	public User login(String telephone, String password) {
		String userKey = DigestUtils.md5Hex(telephone);

		User user = getUserRepository().getUserv1(userKey, null);
		if (null == user) {
			throw new ServiceException("帐号不存在");
		} else {
			user.setPayPassword("");
			String _md5 = DigestUtils.md5Hex(password);
//			String _md5_md5 = DigestUtils.md5Hex(_md5);

			if (password.equals(user.getPassword()) || _md5.equals(user.getPassword())) {
				return user;
			} else {
				throw new ServiceException("帐号或密码错误");
			}
		}
	}

	public User mpLogin(String telephone, String password){
		String userKey = DigestUtils.md5Hex(telephone);
		User user = getUserRepository().getUserv1(userKey, null);
		if (null == user) {
			throw new ServiceException("帐号不存在");
		} else {
			if(2 != user.getUserType())
				throw new ServiceException("您不是公众号，暂时无法登陆");
			user.setPayPassword("");
			String _md5 = DigestUtils.md5Hex(password);
			if (password.equals(user.getPassword()) || _md5.equals(user.getPassword())) {
				return user;
			} else {
				throw new ServiceException("帐号或密码错误");
			}
		}
	}

	@Override
	public Map<String, Object> login(UserExample example) {
		User user =null;
		if(0!=example.getUserId())
			user=getUserRepository().getUser(example.getUserId());
		else
			user=getUserRepository().getUser(example.getAreaCode(),example.getTelephone(), null);
		if (null == user) {
			throw new ServiceException(KConstants.ResultCode.AccountNotExist, "帐号不存在, 请注册!");
		}else if(-1 == user.getStatus()){
			throw new ServiceException(KConstants.ResultCode.ACCOUNT_IS_LOCKED, "您的账号已被锁定");
		}else {
			if(0 == example.getLoginType()){
				// 账号密码登录
				String password = example.getPassword();
				if (!password.equals(user.getPassword()) )
					throw new ServiceException(KConstants.ResultCode.AccountOrPasswordIncorrect, "帐号或密码错误");
			}else if (1 == example.getLoginType()) {
				KSMSServiceImpl smsService = SKBeanUtils.getSMSService();
				// 短信验证码登录
				if(null == example.getVerificationCode())
					throw new ServiceException("短信验证码不能为空!");
				if(!smsService.isAvailable(user.getTelephone(),example.getVerificationCode()))
					throw new ServiceException("短信验证码不正确!");
				// 清除短信验证码
				smsService.deleteSMSCode(user.getTelephone());
			}
			//登录成功后维护客服模块当前用户的会话人数和会话状态
			if (null == user.getUserId()) {
				throw new RuntimeException("获取用户信息失败！");
			}else{
				//将用户的客服模式置为关闭
				UserSettings settings = user.getSettings();
				if (null == settings) settings = new UserSettings();
				settings.setOpenService(0);
				user.setSettings(settings);
				SKBeanUtils.getUserManager().updateSettings(user.getUserId(),user.getSettings());
				//将客服模块分配状态置为不分配
				SKBeanUtils.getCompanyManager().modifyEmployeesByuserId(user.getUserId());
			}
			return loginSuccess(user, example);
		}

	}

	//登陆成功方法
	public  Map<String, Object> loginSuccess(User user,UserExample example){

		KSession session=new KSession(user.getTelephone(), user.getUserId());
		// 获取上次登录日志
		User.LoginLog login = getUserRepository().getLogin(user.getUserId());

		// 保存登录日志
		getUserRepository().updateUserLoginLog(user.getUserId(), example);
		// f1981e4bd8a0d6d8462016d2fc6276b3
		Map<String, Object> data = KSessionUtil.loginSaveAccessToken(user.getUserId(),user.getUserId(),null);

		Object token = data.get("access_token");

		KSessionUtil.saveSession(token.toString(), session);

		data.put("userId", user.getUserId());

		data.put("nickname", user.getNickname());
		if(StringUtil.isEmpty(user.getPayPassword())){
			data.put("payPassword", 0);
		}else{
			data.put("payPassword", 1);
		}
		// 判断如果是第三方sdk登录,返回客户端
		if(example.getIsSdkLogin()==1){
			data.put("telephone", user.getPhone());
			data.put("areaCode", user.getAreaCode());
			data.put("password", user.getPassword());
		}
		if(1 == example.getLoginType())
			data.put("password", user.getPassword());
		data.put("sex",user.getSex());
		data.put("birthday", user.getBirthday());
		data.put("offlineNoPushMsg", user.getOfflineNoPushMsg());
		data.put("multipleDevices", user.getSettings().getMultipleDevices());
		data.put("login", login);
		data.put("settings", getSettings(user.getUserId()));
		if(StringUtil.isEmpty(login.getSerial())){
			data.put("isupdate", 1);//用户登陆不同设备，通知客户端更新用户
		}else if(!login.getSerial().equals(example.getSerial())){
			data.put("isupdate", 1);
		}else{
			data.put("isupdate", 0);
		}

		Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(user.getUserId());

		//好友关系数量
		data.put("friendCount", q.countAll());
		// 用户角色
		List<Integer> userRoles = SKBeanUtils.getRoleManager().getUserRoles(user.getUserId());
		if(null != userRoles && userRoles.size() > 0)
			data.put("role", (0 == userRoles.size() ? "":userRoles));
		///检查该用户  是否注册到 Tigase
		examineTigaseUser(user.getUserId(), user.getPassword());
		destroyMsgRecord(user.getUserId());

		// 保存用户登录位置信息
		user.setArea(example.getArea());
		// 地理位置
		User.Loc loc = new User.Loc(example.getLongitude(),example.getLatitude());
		user.setLoc(loc);
		if(null == user.getAccount()){
			user.setAccount(user.getUserId()+StringUtil.randomCode());
			user.setEncryAccount(DigestUtils.md5Hex(user.getAccount()));
		}
		if(null == user.getEncryAccount())
			user.setEncryAccount(DigestUtils.md5Hex(user.getAccount()));
		save(user);

		//查找出该用户的推广形(一码多用)邀请码
//		InviteCode myInviteCode = SKBeanUtils.getAdminManager().findUserPopulInviteCode(user.getUserId());
//		data.put("myInviteCode", (myInviteCode==null?"":myInviteCode.getInviteCode()));

		//是否实名
		List<BankCard> bankCardList = SKBeanUtils.getBankCardManager().getBankCardList(user.getUserId());
		if(bankCardList==null||bankCardList.size()==0){
			data.put("fourElements",0);
		}else {
			data.put("fourElements",1);
		}

		//saveIosAppId(user.getUserId(), example.getAppId());
		return data;
	}


	public Map<String, Object> loginAuto(String access_token, int userId, String serial, String appId, double latitude,
			double longitude) {

		User user = getUser(userId);
		if (null == user)
			throw new ServiceException(1040101, "帐号不存在, 请重新注册!");
		else if (-1 == user.getStatus())
			throw new ServiceException(KConstants.ResultCode.ACCOUNT_IS_LOCKED, "您的账号已被锁定");

		User.LoginLog loginLog = getUserRepository().getLogin(userId);
		String atKey = KSessionUtil.getUserIdBytoken(access_token);
		boolean exists = SKBeanUtils.getRedisCRUD().keyExists(atKey);
		// 1=没有设备号、2=设备号一致、3=设备号不一致
		int serialStatus = null == loginLog ? 1 : (serial.equals(loginLog.getSerial()) ? 2 : 3);
		// 1=令牌存在、0=令牌不存在
		int tokenExists = exists ? 1 : 0;

		try {
			Map<String, Object> result = Maps.newHashMap();
			result.put("serialStatus", serialStatus);
			result.put("tokenExists", tokenExists);
			result.put("userId", userId);
			result.put("nickname", user.getNickname());
			result.put("name", user.getName());
			result.put("login", loginLog);
			result.put("settings", getSettings(userId));
			result.put("serialStatus", serialStatus);
			result.put("multipleDevices", user.getSettings().getMultipleDevices());
			// 用户角色
			List<Integer> userRoles = SKBeanUtils.getRoleManager().getUserRoles(user.getUserId());
			if(null != userRoles && userRoles.size() > 0)
				result.put("role", (0 == userRoles.size() ? "":userRoles));
			if (StringUtil.isEmpty(user.getPayPassword())) {
				result.put("payPassword", "0");
			} else {
				result.put("payPassword", "1");
			}

			//查找出该用户的推广型邀请码(一码多用)
//			InviteCode myInviteCode = SKBeanUtils.getAdminManager().findUserPopulInviteCode(user.getUserId());
//			result.put("myInviteCode", (myInviteCode==null?"":myInviteCode.getInviteCode()));

			//是否实名
			List<BankCard> bankCardList = SKBeanUtils.getBankCardManager().getBankCardList(user.getUserId());
			if(bankCardList==null||bankCardList.size()==0){
				result.put("fourElements",0);
			}else {
				result.put("fourElements",1);
			}

			updateLoc(latitude, longitude, userId);
			getUserRepository().updateLoginLogTime(userId);

			examineTigaseUser(userId, user.getPassword());
			destroyMsgRecord(userId);
			return result;
		} catch (NullPointerException e) {
			throw new ServiceException("帐号不存在");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	private void updateLoc(double latitude, double longitude, Integer userId) {
		User.Loc loc = new User.Loc(longitude, latitude);
		UpdateOperations<User> ops = getDatastore().createUpdateOperations(getEntityClass());
		ops.set("active", DateUtil.currentTimeSeconds());
		ops.set("loc", loc);
		updateAttributeByOps(userId, ops);
	}

	public void savePushToken(Integer userId,DeviceInfo info){
		Query<PushInfo> query1=getDatastore().createQuery(PushInfo.class);
		query1.filter("pushServer", info.getPushServer());
		query1.filter("pushToken", info.getPushToken());
		query1.filter("deviceKey", info.getDeviceKey());
		PushInfo pushInfo = query1.get();
		if(null!=pushInfo) {
			if(!userId.equals(pushInfo.getUserId())) {
				cleanPushToken(pushInfo.getUserId(), info.getDeviceKey());
				UpdateOperations<PushInfo> ops = getDatastore().createUpdateOperations(PushInfo.class);
				ops.set("userId",userId);
				ops.set("time", DateUtil.currentTimeSeconds());
				getDatastore().update(query1, ops);
			}else {
				UpdateOperations<PushInfo> ops = getDatastore().createUpdateOperations(PushInfo.class);
				ops.set("time", DateUtil.currentTimeSeconds());
				getDatastore().update(query1, ops);
			}
		}else {
			pushInfo=new PushInfo();
			pushInfo.setUserId(userId);
			pushInfo.setPushServer(info.getPushServer());
			pushInfo.setPushToken(info.getPushToken());
			pushInfo.setDeviceKey(info.getDeviceKey());
			pushInfo.setTime(DateUtil.currentTimeSeconds());
			getDatastore().save(pushInfo);
		}

		Query<UserLoginLog> query=getDatastore().createQuery(UserLoginLog.class);
		query.filter("_id", userId);
		UpdateOperations<UserLoginLog> ops = getDatastore().createUpdateOperations(UserLoginLog.class);
		try {
			if(!StringUtil.isEmpty(info.getDeviceKey()))	{
				ops.set("deviceMap."+info.getDeviceKey()+".pushServer",info.getPushServer());
				ops.set("deviceMap."+info.getDeviceKey()+".pushToken",info.getPushToken());
			}
			if(KConstants.DeviceKey.IOS.equals(info.getDeviceKey())) {
				if(!StringUtil.isEmpty(info.getAppId()))
					ops.set("deviceMap."+info.getDeviceKey()+".appId",info.getAppId());
			}
			getDatastore().update(query, ops);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void saveVoipPushToken(Integer userId,String token){
		/*Query<UserLoginLog> query=getDatastore().createQuery(UserLoginLog.class);
		query.filter("_id", userId);
		UpdateOperations<UserLoginLog> ops = getDatastore().createUpdateOperations(UserLoginLog.class);
		try {
				//ops.set("deviceMap."+KConstants.DeviceKey.IOS+".pushServer",info.getPushServer());
				ops.set("deviceMap."+KConstants.DeviceKey.IOS+".voipToken",token);
				//ops.set("deviceMap."+KConstants.DeviceKey.IOS+".appId",appId);

			getDatastore().update(query, ops);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		DeviceInfo deviceInfo = KSessionUtil.getIosPushToken(userId);
		if(null!=deviceInfo){
			deviceInfo.setVoipToken(token);
			KSessionUtil.saveIosPushToken(userId, deviceInfo);
		}
	}

	public void saveIosAppId(Integer userId,String appId){
		if(StringUtil.isEmpty(appId))
			return;
		Query<UserLoginLog> query=getDatastore().createQuery(UserLoginLog.class);
		query.filter("_id", userId);
		UpdateOperations<UserLoginLog> ops = getDatastore().createUpdateOperations(UserLoginLog.class);
		try {

				ops.set("deviceMap."+KConstants.DeviceKey.IOS+".appId",appId);

			getDatastore().update(query, ops);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void cleanPushToken(Integer userId, String devicekey) {
		Query<UserLoginLog> query = getDatastore().createQuery(UserLoginLog.class);

		query.field("_id").equal(userId);
		UpdateOperations<UserLoginLog> ops = getDatastore().createUpdateOperations(UserLoginLog.class);
		ops.set("loginLog.offlineTime",DateUtil.currentTimeSeconds());

		try {
			if(KConstants.DeviceKey.Android.equals(devicekey)){
				KSessionUtil.removeAndroidPushToken(userId);
			}else if (KConstants.DeviceKey.IOS.equals(devicekey)) {
				KSessionUtil.removeIosPushToken(userId);
			}
			if(!StringUtil.isEmpty(devicekey))	{
				ops.set("deviceMap."+devicekey+".pushServer","");
				ops.set("deviceMap."+devicekey+".pushToken","");
			}

			getDatastore().update(query, ops);
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	@Override
	public void logout(String access_token,String areaCode,String userKey,String devicekey) {

		cleanPushToken(ReqUtil.getUserId(),devicekey);
		KSessionUtil.removeAccessToken(ReqUtil.getUserId());
	}

	@Override
	public List<DBObject> query(UserQueryExample param) {
		return getUserRepository().queryUser(param);
	}
	public List<User> queryPublicUser(int page,int limit,String keyWorld) {
		Query<User> query =createQuery();
		query.filter("userType", 2);
		if (!StringUtil.isEmpty(keyWorld)) {
			// 是否为数字
			if(StringUtil.isNumeric(keyWorld)){
				int userId = Integer.valueOf(keyWorld);
				query.or(query.criteria("_id").equal(userId),query.criteria("nickname").containsIgnoreCase(keyWorld));
			}else{
				query.criteria("nickname").containsIgnoreCase(keyWorld);
			}
		}
		return query.offset(page * limit).limit(limit).asList();
	}

	@Override
	public Map<String, Object> register(UserExample example) {
		if (isRegister(example.getTelephone()))
			throw new ServiceException("手机号已被注册");
		//生成userId
		Integer userId = createUserId();
		//新增用户
		Map<String, Object> data = getUserRepository().addUser(userId, example);

		if(null != data) {
			try {
				KXMPPServiceImpl.getInstance().registerByThread(userId.toString(), example.getPassword());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return data;
		}
		throw new ServiceException("用户注册失败");
	}





	@Override
	public Map<String, Object> registerIMUser(UserExample example) throws Exception{

		// 检查手机号是否已经注册
		if (isRegister(example.getTelephone()))
			throw new ServiceException("手机号已被注册");

		// 生成userId
		Integer userId = createUserId();

		// 核验邀请码,及相关操作
		if(example.getIsAdmin() != 1){
			checkInviteCode(example,userId);
		}
		//example.setAccount(userId+StringUtil.randomCode());
		// 新增用户
		Map<String, Object> data = getUserRepository().addUser(userId, example);
		if (null != data) {
			try {
				KXMPPServiceImpl.getInstance().registerByThread(userId.toString(), example.getPassword());

				SKBeanUtils.getFriendsManager().followUser(userId, 10000, 0);
				if(example.getIsAdmin() != 1){
					// 默认成为好友
					defaultTelephones(example, userId);
				}
				// 调用组织架构功能示例方法
				SKBeanUtils.getCompanyManager().autoJoinCompany(userId);
				// 自动创建 好友标签
				SKBeanUtils.getFriendGroupManager().autoCreateGroup(userId);
				if(example.getUserType()!=null){
					if(example.getUserType()==3){
						SKBeanUtils.getRoomManager().join(userId, new ObjectId("5a2606854adfdc0cd071485e"),3);
					}
				}
				//更新通讯录好友
				Object creteTime = data.get("createTime");
				String valueOf = String.valueOf(creteTime);
				Long time = Long.valueOf(valueOf);
				SKBeanUtils.getLocalSpringBeanManager().getAddressBookManger().notifyBook(example.getTelephone(), userId, example.getNickname(),time);
				// 清除redis中没有系统号的表
				SKBeanUtils.getRedisService().deleteNoSystemNumUserIds();
				// 清除短信验证码
				SKBeanUtils.getSMSService().deleteSMSCode(example.getTelephone());
			} catch (Exception e) {
				throw e;
			}

			// 维护公众号角色
			if (example.getUserType()!=null && example.getUserType() == 2) {
				ThreadUtil.executeInThread(new Callback() {

					@Override
					public void execute(Object obj) {
						Role role = new Role(userId, example.getTelephone(), (byte)2, (byte)1, 0);
						getDatastore().save(role);
						SKBeanUtils.getRoleManager().updateFriend(userId, 2);
					}
				});
			}

			return data;
		}
		throw new ServiceException("用户注册失败");
	}


	@Override
	public Map<String, Object> registerIMUser(UserExample example, int type,String loginInfo) throws Exception {
		// 检查手机号是否已经注册
		if (isRegister(example.getTelephone()))
			throw new ServiceException("手机号已被注册");

		// 生成userId
		Integer userId = createUserId();

		// 核验邀请码,及相关操作
		if(example.getIsAdmin() != 1){
			checkInviteCode(example,userId);
		}

		// 新增用户
		Map<String, Object> data = getUserRepository().addUser(userId, example);
		if (null != data) {
			try {
				KXMPPServiceImpl.getInstance().registerByThread(userId.toString(), example.getPassword());

				SKBeanUtils.getFriendsManager().followUser(userId, 10000, 0);
				if(example.getIsAdmin() != 1){
					// 默认成为好友
					defaultTelephones(example, userId);
				}
				// 调用组织架构功能示例方法
				SKBeanUtils.getCompanyManager().autoJoinCompany(userId);
				// 自动创建 好友标签
				SKBeanUtils.getFriendGroupManager().autoCreateGroup(userId);
				if(example.getUserType()!=null){
					if(example.getUserType()==3){
						SKBeanUtils.getRoomManager().join(userId, new ObjectId("5a2606854adfdc0cd071485e"),3);
					}
				}
				//更新通讯录好友
				Object creteTime = data.get("createTime");
				String valueOf = String.valueOf(creteTime);
				Long time = Long.valueOf(valueOf);
				SKBeanUtils.getLocalSpringBeanManager().getAddressBookManger().notifyBook(example.getTelephone(), userId, example.getNickname(),time);

			} catch (Exception e) {
				throw e;
			}

			// 维护公众号角色
			if (example.getUserType()!=null && example.getUserType() == 2) {
				ThreadUtil.executeInThread(new Callback() {

					@Override
					public void execute(Object obj) {
						Role role = new Role(userId, example.getTelephone(), (byte)2, (byte)1, 0);
						getDatastore().save(role);
						SKBeanUtils.getRoleManager().updateFriend(userId, 2);
					}
				});
			}

			// 新增第三方登录
			ThreadUtil.executeInThread(new Callback() {

				@Override
				public void execute(Object obj) {
					SdkLoginInfo entity=new SdkLoginInfo();
					entity.setUserId(userId);
					entity.setType(type);
					entity.setLoginInfo(loginInfo);
					getDatastore().save(entity);
				}
			});

			return data;
		}


		throw new ServiceException("用户注册失败");
	}
	/**
	 屏蔽 某人的 朋友圈
	 */
	public void filterCircleUser(int toUserId) {
		Integer userId = ReqUtil.getUserId();
		UserSettings settings = getSettings(userId);
		if(null==settings.getFilterCircleUserIds()) {
			settings.setFilterCircleUserIds(new HashSet<>());
		}
		settings.getFilterCircleUserIds().add(toUserId);
		updateSettings(userId, settings);
	}
	/**
	取消 屏蔽 某人的 朋友圈
	 */
	public void cancelFilterCircleUser(int toUserId) {
		Integer userId = ReqUtil.getUserId();
		UserSettings settings = getSettings(userId);
		if(null!=settings.getFilterCircleUserIds()) {
			settings.getFilterCircleUserIds().remove(toUserId);
			updateSettings(userId, settings);
		}

	}

	/**
	 * 检查注册邀请码的及相关处理
	 * @param inviteCode 邀请码
	 * @return
	 */
	private void checkInviteCode(UserExample example,int userId){

		//获取系统当前的邀请码模式 0:关闭   1:开启
    	int inviteCodeMode = SKBeanUtils.getAdminManager().getConfig().getRegisterInviteCode();
    	if(inviteCodeMode==0) { //关闭
    		return;
    	}
		InviteCode inviteCode = SKBeanUtils.getAdminRepository().findInviteCodeByCode(example.getInviteCode());
    	if(inviteCodeMode==1) { //开启一对一邀请
    		//该模式下邀请码为必填项
    		if(StringUtil.isEmpty(example.getInviteCode())) {
    			throw new ServiceException("请填写邀请码");
    		}
			if(inviteCode==null){ //status = 0; //状态值 0,为初始状态未使用   1:已使用  -1 禁用
				throw new ServiceException("邀请码无效");
			}
			inviteCode.setCout(inviteCode.getCout()+1);
			SKBeanUtils.getAdminRepository().savaInviteCode(inviteCode);
    	}

	}



	/** @Description:（注册默认成为好友）
	* @param example
	* @param userId
	**/
	private void defaultTelephones(UserExample example, Integer userId) {
		try {
			// 注册默认成为好友
			int inviteCodeMode = SKBeanUtils.getAdminManager().getConfig().getRegisterInviteCode();
			String telephones = "";
			if(inviteCodeMode == 0){
				telephones = SKBeanUtils.getSystemConfig().getDefaultTelephones();
			}else {
				InviteCode inviteCode = SKBeanUtils.getAdminRepository().findInviteCodeByCode(example.getInviteCode());
				if(inviteCode.getDefaultfriend()!=""){
					telephones = inviteCode.getDefaultfriend();
				}else {
					telephones = SKBeanUtils.getSystemConfig().getDefaultTelephones();
				}
			}

			log.info(" config defaultTelephones : " + telephones);

			if (!StringUtil.isEmpty(telephones)) {
				String[] phones = StringUtil.getStringList(telephones);
				for (int i = 0; i < phones.length; i++) {
					User user = getUser(new StringBuffer().append(phones[i]).toString());
					if(null == user)
						continue;
					SKBeanUtils.getFriendsManager().addFriends(userId, user.getUserId());// 过滤好友验证直接成为好友
				}


			}
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 管理后台自动创建用户 或者 群组
	 *
	 * @param example
	 * @return
	 */
	//@Override
	public void autoCreateUserOrRoom(int userNum,String roomId) {

		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
				boolean isJoinRoom = false;
				ObjectId  objRoomId = null;
				if(!roomId.isEmpty() && roomId!=null){
					objRoomId = new ObjectId(roomId);
					isJoinRoom = true;
				}
				addRobot(userNum, isJoinRoom, objRoomId);
			}
		});
	}

	public List<Integer> addRobot(int userNum,boolean isJoinRoom,ObjectId  objRoomId){
		Random rand = new Random();
		List<Integer> userIds = new ArrayList<Integer>();
		UserExample  userExample= new UserExample();
		//3=机器账号，由系统自动生成
		userExample.setAreaCode("86");
		userExample.setBirthday(DateUtil.currentTimeSeconds());
		userExample.setCountryId(ValueUtil.parse(0));
		userExample.setProvinceId(ValueUtil.parse(0));
		userExample.setCityId(ValueUtil.parse(400300));
		userExample.setAreaId(ValueUtil.parse(0));
		for (int i = 1; i <=userNum; i++) {
			//生成userId
			Integer userId = createUserId();
			userIds.add(userId);
			String  name = i%3 == 0 ? RandomUtil.getRandomZh(rand.nextInt(3)+2):
				RandomUtil.getRandomEnAndNum(rand.nextInt(4)+2);

			userExample.setPassword(DigestUtils.md5Hex(""+(userId-1000)/2));
			userExample.setTelephone("86"+String.valueOf(userId));
			userExample.setPhone(String.valueOf(userId));
			userExample.setName(name);
			userExample.setNickname(name);
			userExample.setDescription(String.valueOf(userId));
			userExample.setSex(userId%2 == 0 ? 0 : 1 );

			if(userId!=0 && getUserRepository().addUser(userId, userExample)!=null){
				try {
					KXMPPServiceImpl.getInstance().registerAndXmppVersion(userId.toString(), userExample.getPassword());
					System.out.println("第"+i+"条用户数据已经生成");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				throw new ServiceException("自动生成用户数据失败");
		}
			if(isJoinRoom){
				Integer userSize = SKBeanUtils.getRoomManager().getRoom(objRoomId).getUserSize();//1 - 5
				int maxUserSize = SKBeanUtils.getSystemConfig().getMaxUserSize();
				if(userSize+1 > maxUserSize){
					log.info("群人数已达到上限，不能继续加入。当前上限人数"+maxUserSize);
					throw new ServiceException("群人数已达到上限，不能继续加入");
				}
				SKBeanUtils.getRoomManager().joinRoom(userId,name,objRoomId, 2);
			}
			// 角色信息
			Role role = new Role(userId,String.valueOf(userId),(byte)3,(byte)1,0);
			getDatastore().save(role);
		}


		return userIds;
	}

	@Override
	public void resetPassword(String telephone, String newPassword) {
		User user = getUser(telephone);
		getUserRepository().updatePassword(telephone, newPassword);
		KSessionUtil.removeAccessToken(user.getUserId());
		KXMPPServiceImpl.getInstance().changePassword(user.getUserId()+"", user.getPassword(), newPassword);
	}

	public void resetPassword(int userId, String newPassword) {
		if(get(userId).getPassword().equals(newPassword))
			throw new ServiceException("重置的密码不能与旧密码相同");
		User user = getUser(userId);
		getUserRepository().updatePassowrd(userId, newPassword);
		KSessionUtil.removeAccessToken(userId);
		KXMPPServiceImpl.getInstance().changePassword(userId+"", user.getPassword(), newPassword);
		multipointLoginDataSync(userId, user.getNickname(), KConstants.MultipointLogin.SYNC_LOGIN_PASSWORD);
	}

	@Override
	public void updatePassword(int userId, String oldPassword, String newPassword) {
		User user = getUser(userId);
		if (oldPassword.equals(user.getPassword()) ) {
			log.info("进行密码更新.....");
			getUserRepository().updatePassowrd(userId, newPassword);
			KSessionUtil.removeAccessToken(userId);
			log.info("进行tigase密码更新.....");
			KXMPPServiceImpl.getInstance().changePassword(userId+"", user.getPassword(), newPassword);
			// xmpp消息处理
			multipointLoginDataSync(userId, user.getNickname(), KConstants.MultipointLogin.SYNC_LOGIN_PASSWORD);
		} else {
			throw new ServiceException("旧密码错误");
		}

	}

	@Override
	public User updateSettings(int userId,User.UserSettings userSettings) {
		User user=getUserRepository().updateSettings(userId, userSettings);
		user.setPayPassword("");
		KSessionUtil.deleteUserByUserId(userId);
		return user;
	}

	public void sendMessage(String jid,int chatType,int type,String content,String fileName) {
		Integer userId=ReqUtil.getUserId();
		MessageBean messageBean=new MessageBean();
		messageBean.setType(type);
		messageBean.setFromUserId(userId.toString());
		messageBean.setFromUserName(getNickName(userId));
		messageBean.setToUserId(jid);
		messageBean.setTo(jid);

		if(1==chatType) {
			messageBean.setMsgType(0);
			messageBean.setToUserName(getNickName(Integer.valueOf(jid)));
		}else {
			messageBean.setMsgType(1);
			messageBean.setRoomJid(jid);
		}

		messageBean.setContent(content);
		messageBean.setFileName(fileName);
		messageBean.setTimeSend(DateUtil.currentTimeSeconds());
		messageBean.setMessageId(StringUtil.randomCode());
		KXMPPServiceImpl.getInstance().send(messageBean);

		/**
		 * 发送给自己
		 */
		messageBean.setMsgType(0);
		messageBean.setTo(userId.toString());
		KXMPPServiceImpl.getInstance().send(messageBean);

	}

	/**
	 * 用户 绑定微信 openId
	 * @param userId
	 * @param openid
	 */
	public Object bindWxopenid(int userId,String code) {
		if(StringUtil.isEmpty(code)) {
			return null;
		}
		JSONObject jsonObject = WXUserUtils.getWxOpenId(code);
		String openid=jsonObject.getString("openid");
		if(StringUtil.isEmpty(openid)) {
			return null;
		}
		System.out.println(String.format("======> bindWxopenid  userId %s  openid  %s", userId,openid));
		updateAttribute(userId, "openid", openid);
		return jsonObject;
	}

	public void bindAliUserId(int userId,String aliUserId){
		if(StringUtil.isEmpty(aliUserId)){
			return ;
		}
		updateAttribute(userId, "aliUserId", aliUserId);
	}

	@Override
	public User updateUser(int userId, UserExample param) {
		User user=getUserRepository().updateUser(userId, param);
		if(StringUtil.isEmpty(user.getPayPassword())){
			user.setPayPassword("0");
		}else{
			user.setPayPassword("1");
		}

		return user;
	}

	/** @Description:多点登录下个人信息修改通知
	* @param userId
	* @param nickName
	* @param toUserId
	* @param toNickName
	* @param type  type=0:修改用户信息，type=1:修改好友备注
	**/
	public void multipointLoginUpdateUserInfo(Integer userId,String nickName,Integer toUserId,String toNickName,int type){
		Query<OfflineOperation> query = getDatastore().createQuery(OfflineOperation.class).field("userId").equal(userId);
		if(0 == type){
			query.field("friendId").equal(String.valueOf(userId));
		}else if (1 == type){
			query.field("friendId").equal(String.valueOf(toUserId));
		}
		if(null  == query.get())
			getDatastore().save(new OfflineOperation(userId, KConstants.MultipointLogin.TAG_FRIEND, null == toUserId ? String.valueOf(userId) : String.valueOf(toUserId), DateUtil.currentTimeSeconds()));
		else{
			UpdateOperations<OfflineOperation> ops = getDatastore().createUpdateOperations(OfflineOperation.class);
			ops.set("operationTime", DateUtil.currentTimeSeconds());
			getDatastore().update(query, ops);
		}
		updatePersonalInfo(userId, nickName,toUserId,toNickName,type);
	}

	public List<User> findUserList(int pageIndex, int pageSize,Integer notId) {
		Query<User> query = createQuery();
		List<Integer> ids = new ArrayList<Integer>(){{add(10000);add(10005);add(10006);add(notId);}};
		query.field("_id").notIn(ids);
		return query.order("-_id").offset(pageIndex * pageSize).limit(pageSize).asList();
	}

	/**
	 * 查找对应类型的用户数据
	 * @param pageIndex
	 * @param pageSize
	 * @param type
	 * @return
	 */
	public List<User> findUserList(int pageIndex, int pageSize,String keyworld,short onlinestate,short userType) throws ServiceException{

			return getUserRepository().searchUsers(pageIndex, pageSize, keyworld,onlinestate, userType);
	}




	@Override
	public List<DBObject> findUser(int pageIndex, int pageSize) {
		return getUserRepository().findUser(pageIndex, pageSize);
	}




	@Override
	public List<Integer> getAllUserId() {
		return getDatastore().getCollection((getClass())).distinct("_id");
	}


	@Override
	public void outtime(String access_token, int userId) {
		Query<UserLoginLog> q = getDatastore().createQuery(UserLoginLog.class).field("_id")
				.equal(userId);
		UserLoginLog loginLog = q.get();
		if(null==q.get())
			return;
		if(null==loginLog.getLoginLog())
			return;
		UpdateOperations<UserLoginLog> ops = getDatastore().createUpdateOperations(UserLoginLog.class);
		ops.set("loginLog.offlineTime",DateUtil.currentTimeSeconds());
		getDatastore().findAndModify(q, ops);
	}

	@Override
	public void addUser(int userId, String password) {
		getUserRepository().addUser(userId, password);
	}
	/**
	* @Description: TODO(销毁该用户 已过期的 聊天记录)
	* @param @param userId    参数
	*/
	public void destroyMsgRecord(int userId) {
		ThreadUtil.executeInThread(new Callback() {

			@Override
			public void execute(Object obj) {
				DBCursor cursor = null;
				DBCollection dbCollection=null;
				DBCollection lastdbCollection=null;
				try{

					dbCollection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_msgs");
					lastdbCollection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_lastChats");
					BasicDBObject query = new BasicDBObject();
					BasicDBObject lastquery=new BasicDBObject();
					query.append("sender", userId);
					query.append("deleteTime", new BasicDBObject(MongoOperator.GT,0)
							.append(MongoOperator.LT, DateUtil.currentTimeSeconds()))
							.append("isRead", 1);

					BasicDBObject base=(BasicDBObject)dbCollection.findOne(query);
					BasicDBList queryOr = new BasicDBList();
					if(base!=null){
						queryOr.add(new BasicDBObject("jid", String.valueOf(base.get("sender"))).append("userId", base.get("receiver").toString()));
						queryOr.add(new BasicDBObject("userId",String.valueOf(base.get("sender"))).append("jid", base.get("receiver").toString()));
						lastquery.append(MongoOperator.OR, queryOr);
					}


					// 删除文件
					query.append("contentType",new BasicDBObject(MongoOperator.IN, MsgType.FileTypeArr));
					List<String> fileList=dbCollection.distinct("content", query);
					for (String url : fileList) {
						ConstantUtil.deleteFile(url);
					}
					query.remove("contentType");

					dbCollection.remove(query); //将消息记录中的数据删除

					// 重新查询一条消息记录插入
					BasicDBList baslist = new BasicDBList();
					if(base!=null){
						baslist.add(new BasicDBObject("receiver", base.get("sender")));
						baslist.add(new BasicDBObject("sender", base.get("sender")));
						query.append(MongoOperator.OR,baslist);
					}

					query.remove("sender");
					query.remove("deleteTime");
					query.remove("isRead");
					DBObject lastMsgObj=dbCollection.find(query).sort(new BasicDBObject("timeSend", -1)).limit(1).one();

					if(lastMsgObj!=null){
						BasicDBObject values=new BasicDBObject();
						values.put("messageId", lastMsgObj.get("messageId"));
						values.put("timeSend",new Double(lastMsgObj.get("timeSend").toString()).longValue());
						values.put("content", lastMsgObj.get("content"));
						if(!lastquery.isEmpty())
							lastdbCollection.update(lastquery,new BasicDBObject(MongoOperator.SET, values) ,false,true);
					}else{
						if(!lastquery.isEmpty())
							lastdbCollection.remove(lastquery);
					}

					} catch (Exception e){
						e.printStackTrace();
					}finally {
						if(cursor != null) cursor.close();
					}
			}
		});


	}
	/*@Override
	public User getfUser(int userId) {
		User user = userRepository.getUser(userId);
		if (null == user)
			return null;
		if (0 != user.getCompanyId())
			user.setCompany(companyManager.get(user.getCompanyId()));
			return user;
	}*/

	//用户充值 type 1 充值  2 消费
	public synchronized Double rechargeUserMoeny(Integer userId,Double money,int type){
			try {
				Query<User> q =getDatastore().createQuery(getEntityClass());
				q.field("_id").equal(userId);
				UpdateOperations<User> ops =getDatastore().createUpdateOperations(getEntityClass());
				User user=getUser(userId);
				if(null==user)
					return 0.0;
				if(KConstants.MOENY_ADD==type){
					ops.set("balance", StringUtil.addDouble(user.getBalance(), money));
					ops.set("totalRecharge", StringUtil.addDouble(user.getTotalRecharge(), money));
					user.setBalance(StringUtil.addDouble(user.getBalance(), money));
				}else{
					ops.set("balance", StringUtil.subDouble(user.getBalance(), money));
					ops.set("totalConsume", StringUtil.addDouble(user.getTotalConsume(), money));
					user.setBalance(StringUtil.subDouble(user.getBalance(), money));
				}
				getDatastore().update(q, ops);
				KSessionUtil.saveUserByUserId(userId, user);
			return q.get().getBalance();
			} catch (Exception e) {
				e.printStackTrace();
				return 0.0;
			}
	}

	//用户充值 type 1 充值  2 消费
	public Double getUserMoeny(Integer userId){
		try {
			Object field = queryOneFieldById("balance", userId);
			if(null==field)
				return 0.0;
		return Double.valueOf(field.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}
	}


	public int getOnlinestateByUserId(Integer userId) {
		return SKBeanUtils.getRedisService().queryUserOnline(userId);
	}


	public void examineTigaseUser(Integer userId,String password){
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
						DBObject query=new BasicDBObject("user_id",userId+"@"+SKBeanUtils.getXMPPConfig().getServerName());
						BasicDBObject result=(BasicDBObject) getTigaseDatastore().getDB().getCollection("tig_users").findOne(query);
						if(null==result){
							KXMPPServiceImpl.getInstance().registerByThread(String.valueOf(userId), password);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}).start();


	}

	public void report(Integer userId,Integer toUserId,int reason,String roomId,String webUrl){

		if(toUserId==null&&StringUtil.isEmpty(roomId)&&StringUtil.isEmpty(webUrl)){
			throw new ServiceException(KConstants.ResultCode.ParamsAuthFail);
		}
		Report report=new Report();
		report.setUserId(userId);
		report.setToUserId(toUserId);
		report.setReason(reason);
		report.setRoomId(roomId);
		if(!StringUtil.isEmpty(webUrl))
			report.setWebUrl(webUrl);
		report.setWebStatus(1);
		report.setTime(DateUtil.currentTimeSeconds());
		saveEntity(report);

	}

	public boolean checkReportUrlImpl(String webUrl){
		try {
			URL requestUrl = new URL(webUrl);
			webUrl = requestUrl.getHost();
		} catch (Exception e) {
			throw new ServiceException("参数对应的URL格式错误");
		}
		logger.info("URL HOST :"+webUrl);
		List<Report> reportList = getDatastore().createQuery(Report.class).field("webUrl").contains(webUrl).asList();
		if(null != reportList && reportList.size() > 0){
			reportList.forEach(report ->{
				if(null != report && -1 == report.getWebStatus())
					throw new ServiceException("该网页地址已被举报");
			});
		}
		return true;
	}


	/** @Description: 获取举报列表
	* @param type 0：用户相关，1：群组相关  2：web网页
	* @param sender
	* @param receiver
	* @param pageIndex
	* @param pageSize
	* @return
	**/
	public Map<String,Object> getReport(int type,int sender,String receiver,int pageIndex,int pageSize) {
			Map<String,Object> dataMap = Maps.newConcurrentMap();
			List<Report> data = null;
			try {
				if (type == 0) {
					Query<Report> q = getDatastore().createQuery(Report.class);
					if(0!=sender)
						q.field("userId").equal(sender);
					if(!StringUtil.isEmpty(receiver)){
						q.field("toUserId").equal(Long.valueOf(receiver));
					}else{
						q.field("toUserId").notEqual(0);
					}
					q.field("roomId").equal("");
					q.order("-time");
					q.offset(pageSize*pageIndex);
					data = q.limit(pageSize).asList();
					for(Report report : data){
						report.setUserName(getNickName((int) report.getUserId()));
						report.setToUserName(getNickName((int) report.getToUserId()));
						if(KConstants.ReportReason.reasonMap.containsKey(report.getReason()))
							report.setInfo(KConstants.ReportReason.reasonMap.get(report.getReason()));
						if(null == getUser(Integer.valueOf(String.valueOf(report.getToUserId()))))
							report.setToUserStatus(-1);
						else{
							Integer status = getUser(Integer.valueOf(String.valueOf(report.getToUserId()))).getStatus();
							report.setToUserStatus(status);
						}
					}
					dataMap.put("count", q.count());
				} else if (type == 1) {
					Query<Report> q = getDatastore().createQuery(Report.class);
					if(0!=sender)
						q.field("userId").equal(sender);
					if(!StringUtil.isEmpty(receiver))
						q.field("roomId").equal(receiver);
					q.field("roomId").notEqual("");
					q.order("-time");
					q.offset(pageSize*pageIndex);
					for(Report report : q.asList()){
						report.setUserName(getNickName((int) report.getUserId()));
						report.setRoomName(SKBeanUtils.getRoomManager().getRoomName(new ObjectId(report.getRoomId())));
						Integer roomStatus = SKBeanUtils.getRoomManager().getRoomStatus(new ObjectId(report.getRoomId()));
						if(null != roomStatus)
							report.setRoomStatus(roomStatus);
						if(KConstants.ReportReason.reasonMap.containsKey(report.getReason()))
							report.setInfo(KConstants.ReportReason.reasonMap.get(report.getReason()));
					}
					data = q.limit(pageSize).asList();
					dataMap.put("count", q.count());
				}else if(type == 2){
					Query<Report> q = getDatastore().createQuery(Report.class);
					if(0!=sender)
						q.field("userId").equal(sender);
					if(!StringUtil.isEmpty(receiver))
						q.field("webUrl").equal(receiver);
					q.field("webUrl").notEqual(null);
					q.field("toUserId").equal(0);
					q.order("-time");
					for(Report report : q.asList()){
						report.setUserName(getNickName((int)report.getUserId()));
						if(KConstants.ReportReason.reasonMap.containsKey(report.getReason()))
							report.setInfo(KConstants.ReportReason.reasonMap.get(report.getReason()));
					}
					q.offset(pageSize*pageIndex);
					data = q.limit(pageSize).asList();
					dataMap.put("count", q.count());
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		dataMap.put("data", data);
		return dataMap;
	}

	/** @Description: 删除相关的举报信息
	* @param userId
	* @param roomId
	**/
	public void delReport(Integer userId,String roomId){
		Query<Report> query = getDatastore().createQuery(Report.class);
		if(null != userId)
			query.or(query.criteria("userId").equal(userId),query.criteria("toUserId").equal(userId));
		else if (null != roomId)
			query.field("roomId").equal(roomId);
		getDatastore().delete(query);
	}

	// 删除 被删除的用户得账单记录
	public void delRecord(Integer userId){
		Query<ConsumeRecord> query = getDatastore().createQuery(ConsumeRecord.class);
		query.field("userId").equal(userId);
		getDatastore().delete(query);
	}

	// 清除被删除用户相关组织架构数据
	public void delCompany(Integer userId){
		try {
			// 删除对应创建的公司数据
			Query<Company> ownCompanyQuery = getDatastore().createQuery(Company.class).field("createUserId").equal(userId);
			List<Company> companyList = ownCompanyQuery.asList();
			for(Company company : companyList){
				getDatastore().delete(company);
				// 对应的部门、员工数据
				Query<Department> departMentQuery = getDatastore().createQuery(Department.class).field("companyId").equal(company.getId());
				getDatastore().delete(departMentQuery);
				Query<Employee> ownEmployeeQuery = getDatastore().createQuery(Employee.class).field("companyId").equal(company.getId());
				getDatastore().delete(ownEmployeeQuery);
			}
			// 退出对应部门 删除员工数据
			Query<Employee> employeeQuery = getDatastore().createQuery(Employee.class).field("userId").equal(userId);
			List<Employee> employees = employeeQuery.asList();
			for(Employee employee : employees){
				// 维护对应部门人数
				Query<Department> departMentQuery = getDatastore().createQuery(Department.class).field("_id").equal(employee.getDepartmentId());
				UpdateOperations<Department> ops = getDatastore().createUpdateOperations(Department.class);
				int empNum = departMentQuery.get().getEmpNum();
				int departEmpNum = empNum - 1;
				ops.set("empNum", departEmpNum);
				getDatastore().findAndModify(departMentQuery, ops);
				// 维护公司员工人数
				Query<Company> companyQuery = getDatastore().createQuery(Company.class).field("_id").equal(employee.getCompanyId());
				int companyEmpNum = companyQuery.get().getEmpNum();
				UpdateOperations<Company> companyOps = getDatastore().createUpdateOperations(Company.class);
				int num = companyEmpNum - 1;
				companyOps.set("empNum", num);
				getDatastore().findAndModify(companyQuery, companyOps);
			}
			getDatastore().delete(employeeQuery);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//获取用户Id
	public synchronized Integer createUserId(){
		DBCollection collection=getDatastore().getDB().getCollection("idx_user");
		if(null==collection)
			return createIdxUserCollection(collection,0);
		DBObject obj=collection.findOne();
		if(null!=obj){
			Integer id=new Integer(obj.get("id").toString());
			id+=1;
			collection.update(new BasicDBObject("_id", obj.get("_id")),
					new BasicDBObject(MongoOperator.INC, new BasicDBObject("id", 1)));
			return id;
		}else{
			return createIdxUserCollection(collection,0);
		}

	}

	//获取Call
	public synchronized Integer createCall(){
		DBCollection collection=getDatastore().getDB().getCollection("idx_user");
		if(null==collection){
			return createIdxUserCollection(collection,0);
		}
		DBObject obj=collection.findOne();
		if(null!=obj){
			if(obj.get("call")==null){
				obj.put("call", 300000);
			}
			Integer call=new Integer(obj.get("call").toString());
			call+=1;
			if(call>349999){
				call=300000;
			}
			collection.update(new BasicDBObject("_id", obj.get("_id")),new BasicDBObject(MongoOperator.SET, new BasicDBObject("call", call)));
			return call;
		}else{
			return createIdxUserCollection(collection,0);
		}
	}

	//获取videoMeetingNo
	public synchronized Integer createvideoMeetingNo(){
		DBCollection collection=getDatastore().getDB().getCollection("idx_user");
		if(null==collection){
			return createIdxUserCollection(collection,0);
		}
		DBObject obj=collection.findOne();
		if(null!=obj){
			if(obj.get("videoMeetingNo")==null){
				obj.put("videoMeetingNo",350000);
			}
			Integer videoMeetingNo=new Integer(obj.get("videoMeetingNo").toString());
			videoMeetingNo+=1;
			if(videoMeetingNo>399999){
				videoMeetingNo=350000;
			}
			collection.update(new BasicDBObject("_id",obj.get("_id")),new BasicDBObject(MongoOperator.SET, new BasicDBObject("videoMeetingNo", videoMeetingNo)));
			return videoMeetingNo;
		}else{
			return createIdxUserCollection(collection,0);
		}
	}

//	//获取注册邀请码计数值
//	@Override
//	public synchronized Integer createInviteCodeNo(int createNum){
//		DBCollection collection = getDatastore().getDB().getCollection("idx_user");
//		if(null==collection) {
//			 createIdxUserCollection(collection,0);
//		}
//		DBObject obj = collection.findOne();
//		if(null!=obj){
//			if(obj.get("inviteCodeNo")==null){
//				obj.put("inviteCodeNo",1001);
//			}
//		}else {
//			createIdxUserCollection(collection,0);
//		}
//
//		Integer inviteCodeNo = new Integer(obj.get("inviteCodeNo").toString());
//		//inviteCodeNo += 1;
//		collection.update(new BasicDBObject("_id", obj.get("_id")),
//				new BasicDBObject(MongoOperator.INC, new BasicDBObject("inviteCodeNo", createNum)));
//		return inviteCodeNo;
//	}

	//初始化自增长计数表数据
	private Integer createIdxUserCollection(DBCollection collection,long userId){
		if(null==collection)
			collection=getDatastore().getDB().createCollection("idx_user", new BasicDBObject());
		BasicDBObject init=new BasicDBObject();
		Integer id=getMaxUserId();
		if(0==id||id<KConstants.MIN_USERID)
			id=new Integer("10000001");
		id+=1;
		 init.append("id", id);
		 init.append("stub","id");
		 init.append("call",300000);
		 init.append("videoMeetingNo",350000);
		 init.append("inviteCodeNo",1001);
		collection.insert(init);
		return id;
	}

	public Integer getMaxUserId(){
		BasicDBObject projection=new BasicDBObject("_id", 1);
		DBObject dbobj=getDatastore().getDB().getCollection("user").findOne(null, projection, new BasicDBObject("_id", -1));
		if(null==dbobj)
			return 0;
		Integer id=new Integer(dbobj.get("_id").toString());
			return id;
	}

	public Integer getServiceNo(String areaCode){
		DBCollection collection=getDatastore().getDB().getCollection("sysServiceNo");
		BasicDBObject obj=(BasicDBObject) collection.findOne(new BasicDBObject("areaCode", areaCode));
		if(null!=obj)
			return obj.getInt("userId");
		return createServiceNo(areaCode);
	}

	//获取系统最大客服号
	private Integer getMaxServiceNo(){
		DBCollection collection=getDatastore().getDB().getCollection("sysServiceNo");
		BasicDBObject obj=(BasicDBObject) collection.findOne(null, new BasicDBObject("userId", 1), new BasicDBObject("userId", -1));
		if(null!=obj){
			return obj.getInt("userId");
		}else{
			BasicDBObject query=new BasicDBObject("_id",new BasicDBObject(MongoOperator.LT, 10200));
			query.append("_id",new BasicDBObject(MongoOperator.GT, 10200));
			BasicDBObject projection=new BasicDBObject("_id", 1);
			DBObject dbobj=getDatastore().getDB().getCollection("user").findOne(query, projection, new BasicDBObject("_id", -1));
			if(null==dbobj)
				return 10200;
			Integer id=new Integer(dbobj.get("_id").toString());
				return id;
		}
	}

	//创建系统服务号
	private Integer createServiceNo(String areaCode){
		DBCollection collection=getDatastore().getDB().getCollection("sysServiceNo");
		Integer userId=getMaxServiceNo()+1;
		BasicDBObject value=new BasicDBObject("areaCode", areaCode);
		value.append("userId", userId);
		collection.save(value);
		addUser(userId, Md5Util.md5Hex(userId+""));
		return userId;
	}

	//消息免打扰
	@Override
	public User updataOfflineNoPushMsg(int userId,int OfflineNoPushMsg) {
		Query<User> q=getDatastore().createQuery(getEntityClass()).field("_id").equal(userId);
		UpdateOperations<User> ops = getDatastore().createUpdateOperations(getEntityClass());
		ops.set("OfflineNoPushMsg", OfflineNoPushMsg);
		User user=getDatastore().findAndModify(q, ops);
		user.setPayPassword("");
		return user;
	}

	/** @Description:（收藏）
	* @param emoji
	**/
	public Emoji addNewEmoji(String emoji){
		Emoji newEmoji = null;
		if(StringUtil.isEmpty(emoji))
			throw new ServiceException("addNewEmoji emoji is null");
		List<Emoji> emojiList = JSONObject.parseArray(emoji, Emoji.class);
		for(Emoji emojis : emojiList){
			emojis.setUserId(ReqUtil.getUserId());
			Query<Emoji> query = getDatastore().createQuery(Emoji.class).field("msg").equal(emojis.getMsg()).field("type").equal(emojis.getType()).field("userId").equal(emojis.getUserId());
			if(null != query.get())
				throw new ServiceException("不能重复收藏");
			if(!StringUtil.isEmpty(emojis.getMsgId()) && 0 == emojis.getCollectType()) {
				// 添加收藏
				newEmoji = newAddCollection(ReqUtil.getUserId(), emojis);
			} else if(StringUtil.isEmpty(emojis.getMsgId()) && 0 == emojis.getCollectType()) {
				// 添加表情
				newEmoji = newAddEmoji(ReqUtil.getUserId(),emojis);
			}else if(StringUtil.isEmpty(emojis.getMsgId()) && -1 == emojis.getCollectType()){
				// 无关消息的相关收藏
				newEmoji = newAddCollection(ReqUtil.getUserId(), emojis);
			}
			if(StringUtil.isEmpty(emojis.getMsgId()) && 1 == emojis.getCollectType() && StringUtil.isEmpty(emojis.getTitle()) && StringUtil.isEmpty(emojis.getShareURL())){
				// 朋友圈收藏
				newEmoji = msgCollect(ReqUtil.getUserId(),emojis,0);
				saveCollect(new ObjectId(newEmoji.getCollectMsgId()), getNickName(emojis.getUserId()), emojis.getUserId());
			}else if(StringUtil.isEmpty(emojis.getMsgId()) && 1 == emojis.getCollectType() && !StringUtil.isEmpty(emojis.getTitle())&& !StringUtil.isEmpty(emojis.getShareURL())){
				// SDK分享链接
				newEmoji = msgCollect(ReqUtil.getUserId(),emojis,1);
				saveCollect(new ObjectId(newEmoji.getCollectMsgId()), getNickName(emojis.getUserId()), emojis.getUserId());
			}
		}
		return newEmoji;
	}


	private Emoji msgCollect(Integer userId,Emoji msgEmoji,int isShare) {
		StringBuffer buffer = new StringBuffer();
		if(msgEmoji.getType() != 5){
			String[] msgs = msgEmoji.getMsg().split(",");
			String copyFile = "";
			String newCopyFile = null;
			for (int i = 0; i < msgs.length; i++) {
				copyFile = ConstantUtil.copyFile(-1, msgs[i]);
				buffer.append(copyFile).append(",");
			}
			newCopyFile = buffer.deleteCharAt(buffer.length()-1).toString();
			msgEmoji.setUrl(newCopyFile);
		}
		Emoji emoji = null;
		if(0 == isShare){
			emoji = new Emoji(msgEmoji.getUserId(), msgEmoji.getType(), (5 == msgEmoji.getType() ? null : msgEmoji.getUrl()), msgEmoji.getMsg(),
					msgEmoji.getFileName(), msgEmoji.getFileSize(), msgEmoji.getFileLength(), msgEmoji.getCollectType(),msgEmoji.getCollectContent(),msgEmoji.getCollectMsgId());
		}else if(1 == isShare){
			emoji = new Emoji(msgEmoji.getUserId(), msgEmoji.getType(), (5 == msgEmoji.getType() ? null : msgEmoji.getUrl()), msgEmoji.getMsg(),
					msgEmoji.getFileName(), msgEmoji.getFileSize(), msgEmoji.getFileLength(), msgEmoji.getCollectType(),msgEmoji.getCollectContent(),msgEmoji.getCollectMsgId(),msgEmoji.getTitle(),msgEmoji.getShareURL());
		}
		getDatastore().save(emoji);
//		// 维护朋友圈收藏
		getRedisServiceImpl().deleteUserCollectCommon(userId);
		return emoji;
	}

	/** @Description:（添加收藏）
	* @param userId
	* @param emoji
	 * @return
	**/
	public synchronized Emoji newAddCollection(Integer userId, Emoji emoji) {
		if (emoji.getType() != 5) {
			try {
				String copyFile = ConstantUtil.copyFile(-1, emoji.getMsg());
				emoji.setUrl(copyFile);
			} catch (ServiceException e) {
				throw new ServiceException(e.getMessage());
			}
		}else if(emoji.getType() == 5){
			emoji.setCollectContent(emoji.getMsg());
		}
		BasicDBObject emojiMsg = emojiMsg(emoji);
		if (null != emojiMsg) {
			JSONObject parseObject = JSONObject.parseObject(emojiMsg.toString());
			// 格式化body 转译&quot;
			String unescapeHtml3 = StringEscapeUtils.unescapeHtml3((String) parseObject.get("body"));
			JSONObject test = JSONObject.parseObject(unescapeHtml3);
			if (emoji.getType() != 5){
				if(null!=test.get("fileName"))
					emoji.setFileName(test.get("fileName").toString());
				if(null!=test.get("fileSize"))
					emoji.setFileSize(Double.valueOf(test.get("fileSize").toString()));
			}
			if (emoji.getType() == 4)
				emoji.setFileLength(Integer.valueOf(test.get("timeLen").toString()));
		}
		/*if (!StringUtil.isEmpty(emoji.getRoomJid()))
			emoji.setRoomJid(emoji.getRoomJid());
		if(!StringUtil.isEmpty(emoji.getTitle()))
			emoji.setTitle(emoji.getTitle());
		if(!StringUtil.isEmpty(emoji.getShareURL()))
			emoji.setShareURL(emoji.getShareURL());*/
		emoji.setUserId(userId);
		emoji.setCreateTime(DateUtil.currentTimeSeconds());
		getDatastore().save(emoji);
		/**
		 * 维护用户收藏的缓存
		 */
		getRedisServiceImpl().deleteUserCollectCommon(userId);
		return emoji;
	}

	/** @Description:（语音、文件特殊处理）
	* @param emoji
	* @param type
	* @return
	**/
	private BasicDBObject emojiMsg(Emoji emoji){
		int isSaveMsg = SKBeanUtils.getSystemConfig().getIsSaveMsg();
		if (0 == isSaveMsg)
			throw new ServiceException("当前设置不保存单聊聊天记录,暂不支持收藏");
		DBCollection dbCollection = null;
		BasicDBObject data = null;
		if (StringUtil.isEmpty(emoji.getRoomJid())) {
			dbCollection = getTigaseDatastore().getDB().getCollection("shiku_msgs");
		} else {
			dbCollection = getRoomDatastore().getDB().getCollection("mucmsg_" + emoji.getRoomJid());
		}
		BasicDBObject q = new BasicDBObject();
		q.put("messageId", emoji.getMsgId());
		data = (BasicDBObject) dbCollection.findOne(q);
		logger.info(" emojiMsg  文件："+JSONObject.toJSONString(data));
		return data;
	}

		/** @Description:（添加收藏表情）
		* @param userId
		* @param emoji
		 * @return
		**/
		public Emoji newAddEmoji(Integer userId,Emoji emoji){
			try {
				String copyFile = ConstantUtil.copyFile(-1,emoji.getUrl());
				emoji.setUserId(userId);
				emoji.setType(emoji.getType());
				if(!StringUtil.isEmpty(copyFile))
					emoji.setUrl(copyFile);
				else
					emoji.setUrl(emoji.getUrl());

				emoji.setCreateTime(DateUtil.currentTimeSeconds());
				getDatastore().save(emoji);
				/**
				 * 维护用户自定义表情缓存
				 */
				getRedisServiceImpl().deleteUserCollectEmoticon(userId);
			} catch (ServiceException e) {
				throw new ServiceException("文件服务器连接超时");
			}
			return emoji;
		}

		// 收藏详情记录
		public void saveCollect(ObjectId msgId, String nickname, int userId){
			Collect collect = new Collect(msgId,nickname,userId);
			getDatastore().save(collect);
		}


		/**
		 * 旧版收藏 兼容版本
		 */
		// 添加收藏
		@Override
		public List<Object> addCollection(int userId, String roomJid, String msgId, String type) {
			int isSaveMsg = SKBeanUtils.getSystemConfig().getIsSaveMsg();
			if(0 == isSaveMsg)
				throw new ServiceException("当前设置不保存单聊聊天记录,暂不支持收藏");
			Query<Emoji> query=null;
			BasicDBObject data=null;
			List<Object> listEmoji=new ArrayList<>();
			List<String> listMsgId=new ArrayList<>();
			List<String> listType=new ArrayList<>();
			if(!StringUtil.isEmpty(msgId)){
				listMsgId=Arrays.asList(msgId.split(","));
				listType=Arrays.asList(type.split(","));
				for(int i=0;i<listMsgId.size();i++){
					query=getDatastore().createQuery(Emoji.class).filter("userId",userId).filter("msgId", listMsgId.get(i));
					if(query.get()==null){
						Emoji emoji=new Emoji();
						emoji.setUserId(userId);
						emoji.setType(Integer.valueOf(listType.get(i)));
						if(!StringUtil.isEmpty(roomJid)){
							emoji.setRoomJid(roomJid);
						}

						if(!StringUtil.isEmpty(listMsgId.get(i))){
							emoji.setMsgId(listMsgId.get(i));
							DBCollection dbCollection=null;
							if(StringUtil.isEmpty(roomJid)){
								dbCollection = getTigaseDatastore().getDB().getCollection("shiku_msgs");
							}else{
								dbCollection = getRoomDatastore().getDB().getCollection("mucmsg_"+roomJid);
							}

							BasicDBObject q = new BasicDBObject();
							q.put("messageId",listMsgId.get(i));
							data=(BasicDBObject) dbCollection.findOne(q);
							if(data==null){
								continue;
							}

						}
						if(Integer.valueOf(listType.get(i))!=5){
							JSONObject obj=JSONObject.parseObject(data.toString());
							try {
								String copyFile = ConstantUtil.copyFile(-1,obj.get("content").toString());
								data.replace("content", copyFile);
								emoji.setUrl(copyFile);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						emoji.setMsg(data.toString());
						emoji.setCreateTime(DateUtil.currentTimeSeconds());
						getDatastore().save(emoji);
						listEmoji.add(emoji);
						/**
						 * 维护用户收藏的缓存
						 */
						getRedisServiceImpl().deleteUserCollectCommon(userId);
						getRedisServiceImpl().deleteUserCollectEmoticon(userId);
					}else{
						return null;
					}
				}

			}

			return listEmoji;
		}


		//添加收藏表情
		@Override
		public Object addEmoji(int userId,String url,String type) {

			Query<Emoji> query=null;
			if(!StringUtil.isEmpty(url)){
				query=getDatastore().createQuery(Emoji.class).filter("userId",userId).filter("url", url);
			}

			String copyFile = ConstantUtil.copyFile(-1,url);

			if(query.get()==null){
				Emoji emoji=new Emoji();
				emoji.setUserId(userId);
				emoji.setType(Integer.valueOf(type));
				if(!StringUtil.isEmpty(copyFile)){
					emoji.setUrl(copyFile);
				}else
					emoji.setUrl(url);

				emoji.setCreateTime(DateUtil.currentTimeSeconds());
				getDatastore().save(emoji);
				/**
				 * 维护用户表情缓存
				 */
				getRedisServiceImpl().deleteUserCollectEmoticon(userId);
				return emoji;
			}else{
				return null;
			}

		}


		//取消收藏
		@Override
		public void deleteEmoji(Integer userId,String emojiId) {
			List<String> list=new ArrayList<>();
			list=Arrays.asList(emojiId.split(","));
			Emoji dbObj=null;
			for(String emjId:list){
				if(!ObjectId.isValid(emjId))
					continue;
				Query<Emoji> query=getDatastore().createQuery(Emoji.class).field("_id").equal(new ObjectId(emjId)).field("userId").equal(userId);
				dbObj=query.get();
				if(null != dbObj){

					if(dbObj.getCollectType() == 1){
						Query<Collect> collectQuery = getDatastore().createQuery(Collect.class).field("msgId").equal(new ObjectId(dbObj.getCollectMsgId()));
						getDatastore().delete(collectQuery);
					}
					// 删除收藏不删除源文件
//					ConstantUtil.deleteFile(dbObj.getUrl());

					if(dbObj.getType() != 6)
						getRedisServiceImpl().deleteUserCollectCommon(ReqUtil.getUserId());
					else
						getRedisServiceImpl().deleteUserCollectEmoticon(ReqUtil.getUserId());
					getDatastore().delete(query);
				}

			}
		}


		//收藏列表
		@Override
		public List<Emoji> emojiList(int userId,int type,int pageSize,int pageIndex) {
			// 用户收藏
			List<Emoji> emojiLists = null;
			if(type != 0){
				Query<Emoji> query=getDatastore().createQuery(Emoji.class).field("userId").equal(userId).filter("type", new BasicDBObject(MongoOperator.LT,6)).order("-createTime");
				query.filter("type",type);
				emojiLists = query.asList();
				// 兼容旧版文本
				emojiLists = unescapeHtml3(emojiLists);
			}else {
				List<Emoji> userCollectCommon = getRedisServiceImpl().getUserCollectCommon(userId);
				if(null != userCollectCommon && userCollectCommon.size() > 0){
					emojiLists = userCollectCommon;
				}else{
//					Query<Emoji> query1=getDatastore().createQuery(Emoji.class).field("userId").equal(userId).filter("type", new BasicDBObject(MongoOperator.LT,6)).order("-createTime");
					Query<Emoji> query = getDatastore().createQuery(Emoji.class).field("userId").equal(userId).order("-createTime");
					query.or(query.criteria("type").lessThan(6),query.criteria("type").equal(7));
					emojiLists = query.asList();
					// 兼容旧版文本
					emojiLists = unescapeHtml3(emojiLists);
					getRedisServiceImpl().saveUserCollectCommon(userId, emojiLists);
				}
			}
			return emojiLists;
		}

		/** @Description: 旧版收藏的文本数据格式化
		* @param emojiLists
		* @return
		**/
		public List<Emoji> unescapeHtml3(List<Emoji> emojiLists){
			if(null == emojiLists)
				return null;
			emojiLists.forEach(emojis->{
				if(5 == emojis.getType() && null == emojis.getCollectContent()){
					BasicDBObject emojiMsg = emojiMsg(emojis);
					if (null != emojiMsg) {
						JSONObject parseObject = JSONObject.parseObject(emojiMsg.toString());
						// 格式化body 转译&quot;
						String unescapeHtml3 = StringEscapeUtils.unescapeHtml3((String) parseObject.get("body"));
						JSONObject test = JSONObject.parseObject(unescapeHtml3);
						if(null != test.get("content")){
							emojis.setMsg(test.get("content" ).toString());
							log.info("旧版转译后的 content:"+test.get("content" ).toString());
							emojis.setCollectContent(test.get("content").toString());
						}
					}
				}
			});
			return emojiLists;
		}


		//收藏表情列表
		@Override
		public List<Emoji> emojiList(int userId) {
			List<Emoji> emojis = null;
			List<Emoji> userCollectEmoticon = getRedisServiceImpl().getUserCollectEmoticon(userId);
			if(null != userCollectEmoticon && userCollectEmoticon.size() >0)
				emojis = userCollectEmoticon;
			else{
				Query<Emoji> query=getDatastore().createQuery(Emoji.class).filter("userId", userId).filter("type", 6);
				List<Emoji> emojiList= query.order("-createTime").asList();
				emojis = emojiList;
				getRedisServiceImpl().saveUserCollectEmoticon(userId, emojis);
			}
			return emojis;
		}


		//添加课程
		@Override
		public void addMessageCourse(int userId, List<String> messageIds, long createTime, String courseName,String roomJid) {
			Course course=new Course();
			course.setUserId(userId);
			course.setMessageIds(messageIds);
			course.setCreateTime(createTime);
			course.setCourseName(courseName);
			course.setRoomJid(roomJid);
			saveEntity(course);
			ThreadUtil.executeInThread(new Callback() {

				@Override
				public void execute(Object obj) {
					DBCollection dbCollection=null;
					if(course.getRoomJid().equals("0")){
						dbCollection = getTigaseDatastore().getDB().getCollection("shiku_msgs");
					}else{
						 dbCollection = getRoomDatastore().getDB().getCollection("mucmsg_"+course.getRoomJid());
					}
					BasicDBObject q = new BasicDBObject();
					q.put("messageId",new BasicDBObject(MongoOperator.IN,messageIds));
					q.put("sender", course.getUserId());
					DBCursor dbCursor = dbCollection.find(q);
					DBObject dbObj;
					CourseMessage courseMessage=new CourseMessage();
					while (dbCursor.hasNext()) {
						dbObj = dbCursor.next();
						courseMessage.setCourseMessageId(new ObjectId());
						courseMessage.setUserId(course.getUserId());
						courseMessage.setCourseId(course.getCourseId().toString());
						courseMessage.setCreateTime(String.valueOf(dbObj.get("timeSend")));
						courseMessage.setMessage(String.valueOf(dbObj));
						courseMessage.setMessageId(String.valueOf(dbObj.get("messageId")));
						saveEntity(courseMessage);
					}
					dbCursor.close();
				}
			});

		}

		//获取课程列表
		@Override
		public List<Course> getCourseList(int userId) {
			Query<Course> query=getDatastore().createQuery(Course.class).filter("userId", userId);
			List<Course> list=query.order("createTime").asList();
			return list;
		}

		//修改课程
		@Override
		public void updateCourse(Course course,String courseMessageId) {
			Query<Course> q = getDatastore().createQuery(Course.class).filter("courseId", course.getCourseId());
			UpdateOperations<Course> ops = getDatastore().createUpdateOperations(Course.class);
			if(null!=course.getMessageIds()){
				ops.set("messageIds",course.getMessageIds());
			}
			if(0!=course.getUpdateTime()){
				ops.set("updateTime",course.getUpdateTime());
			}
			if(null!=course.getCourseName()){
				ops.set("courseName",course.getCourseName());
			}
			if(!StringUtil.isEmpty(courseMessageId)){
				Query<CourseMessage> query=getDatastore().createQuery(CourseMessage.class).filter("messageId", courseMessageId);
				CourseMessage courseMessage=query.get();
				// 兼容IOS旧版本
				if(null == courseMessage){
					Query<CourseMessage> oldQuery=getDatastore().createQuery(CourseMessage.class).filter("_id", new ObjectId(courseMessageId));
					courseMessage=oldQuery.get();
				}
				getDatastore().delete(courseMessage);
				// 维护讲课messageIds
				List<String> messageIds = q.get().getMessageIds();
				Iterator<String> iterator = messageIds.iterator();
				while (iterator.hasNext()) {
					String next = iterator.next();
					if(next.equals(courseMessageId))
						iterator.remove();
				}
				if(0 == messageIds.size()){
					getDatastore().delete(q);
					return;
				}
				ops.set("messageIds", messageIds);
			}
			getDatastore().update(q, ops);

		}

		//删除课程
		@Override
		public boolean deleteCourse(Integer userId,ObjectId courseId) {
			Query<Course> query=getDatastore().createQuery(Course.class).filter("courseId",courseId);
			query.field("userId").equal(userId);
			if(0==getDatastore().getCount(query))
				return false;
			Query<CourseMessage> q=getDatastore().createQuery(CourseMessage.class).filter("courseId",String.valueOf(courseId));
			q.field("userId").equal(userId);
			List<CourseMessage> asList = q.asList();
			for(int i=0;i<asList.size();i++){
				getDatastore().delete(asList.get(i));
			}
			getDatastore().delete(query);
			return true;

		}
		//获取详情
		@Override
		public List<CourseMessage> getCourse(String courseId) {
			List<CourseMessage> listMessage=new ArrayList<CourseMessage>();
			Query<CourseMessage> que=getDatastore().createQuery(CourseMessage.class).filter("courseId",courseId);
			listMessage=que.asList();
			return listMessage;
		}

		@Override
		public WxUser addwxUser(JSONObject jsonObject) {
			WxUser wxUser=new WxUser();
			Integer userId=createUserId();
			wxUser.setWxuserId(userId);
			wxUser.setOpenId(jsonObject.getString("openid"));
			wxUser.setNickname(jsonObject.getString("nickname"));
			wxUser.setImgurl(jsonObject.getString("headimgurl"));
			wxUser.setSex(jsonObject.getIntValue("sex"));
			wxUser.setCity(jsonObject.getString("city"));
			wxUser.setCountry(jsonObject.getString("country"));
			wxUser.setProvince(jsonObject.getString("province"));
			wxUser.setCreatetime(DateUtil.currentTimeSeconds());
			getDatastore().save(wxUser);

			try {
				KXMPPServiceImpl.getInstance().registerByThread(userId.toString(),jsonObject.getString("openid"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return wxUser;
		}


		/**
		 * 用户注册统计      时间单位每日，最好可选择：每日、每月、每分钟、每小时
		 * @param startDate
		 * @param endDate
		 * @param counType  统计类型   1: 每个月的数据      2:每天的数据       3.每小时数据   4.每分钟的数据 (小时)
		 */
		public List<Object> getUserRegisterCount(String startDate, String endDate, short timeUnit){


			List<Object> countData = new ArrayList<>();


			long startTime = 0; //开始时间（秒）

			long endTime = 0; //结束时间（秒）,默认为当前时间

			/**
			 * 如时间单位为月和天，默认开始时间为当前时间的一年前 ; 时间单位为小时，默认开始时间为当前时间的一个月前;
			 * 时间单位为分钟，则默认开始时间为当前这一天的0点
			 */
			long defStartTime = timeUnit==4? DateUtil.getTodayMorning().getTime()/1000
					: timeUnit==3 ? DateUtil.getLastMonth().getTime()/1000 : DateUtil.getLastYear().getTime()/1000;

			startTime = StringUtil.isEmpty(startDate) ? defStartTime :DateUtil.toDate(startDate).getTime()/1000;
			endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime()/1000;

			BasicDBObject queryTime = new BasicDBObject("$ne",null);

			if(startTime!=0 && endTime!=0){
				queryTime.append("$gt", startTime);
				queryTime.append("$lt", endTime);
			}

			BasicDBObject query = new BasicDBObject("createTime",queryTime);

			//获得用户集合对象
			DBCollection collection = SKBeanUtils.getDatastore().getCollection(getEntityClass());

			String mapStr = "function Map() { "
		            + "var date = new Date(this.createTime*1000);"
		            +  "var year = date.getFullYear();"
					+  "var month = (\"0\" + (date.getMonth()+1)).slice(-2);"  //month 从0开始，此处要加1
					+  "var day = (\"0\" + date.getDate()).slice(-2);"
					+  "var hour = (\"0\" + date.getHours()).slice(-2);"
					+  "var minute = (\"0\" + date.getMinutes()).slice(-2);"
					+  "var dateStr = date.getFullYear()"+"+'-'+"+"(parseInt(date.getMonth())+1)"+"+'-'+"+"date.getDate();";

					if(timeUnit==1){ // counType=1: 每个月的数据
						mapStr += "var key= year + '-'+ month;";
					}else if(timeUnit==2){ // counType=2:每天的数据
						mapStr += "var key= year + '-'+ month + '-' + day;";
					}else if(timeUnit==3){ //counType=3 :每小时数据
						mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour +' : 00';";
					}else if(timeUnit==4){ //counType=4 :每分钟的数据
						mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour + ':'+ minute;";
					}

					mapStr += "emit(key,1);}";

			 String reduce = "function Reduce(key, values) {" +
				                "return Array.sum(values);" +
		                    "}";
			 MapReduceCommand.OutputType type =  MapReduceCommand.OutputType.INLINE;//
			 MapReduceCommand command = new MapReduceCommand(collection, mapStr, reduce,null, type,query);


			int i = 0;
			MapReduceOutput mapReduceOutput = null;
			while (i < 5) {
				i++;
				try {
					mapReduceOutput = collection.mapReduce(command);
					break;
				} catch (MongoSocketWriteException e) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					logger.info("retry getAddFriendsCount mapReduce:{}", i);
				}
			}
			if (null == mapReduceOutput) return countData;
			 Iterable<DBObject> results = mapReduceOutput.results();
			 Map<String,Double> map = new HashMap<String,Double>();
			for (Iterator iterator = results.iterator(); iterator.hasNext();) {
				DBObject obj = (DBObject) iterator.next();

				map.put((String)obj.get("_id"),(Double)obj.get("value"));
				countData.add(JSON.toJSON(map));
				map.clear();

			}

			return countData;
		}



		// 1: 每个月的数据      2:每天的数据       3.每小时数据   4.每分钟的数据
		public List<Object> userOnlineStatusCount(String startDate, String endDate, short timeUnit){

			List<Object> countData = new ArrayList<>();

			long startTime = 0; //开始时间（秒）

			long endTime = 0; //结束时间（秒）,默认为当前时间

			/**
			 * 如时间单位为月和天，默认开始时间为当前时间的一年前 ; 时间单位为小时，默认开始时间为当前时间的一个月前;
			 * 时间单位为分钟，则默认开始时间为当前这一天的0点
			 */
			long defStartTime = timeUnit==4? DateUtil.getTodayMorning().getTime()/1000
					: timeUnit==3 ? DateUtil.getLastMonth().getTime()/1000 : DateUtil.getLastYear().getTime()/1000;


			startTime = StringUtil.isEmpty(startDate) ? defStartTime :DateUtil.toDate(startDate).getTime()/1000;
			endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime()/1000;


			BasicDBObject queryTime = new BasicDBObject("$ne",null);

			if(startTime!=0 && endTime!=0){
				queryTime.append("$gte", startTime);
				queryTime.append("$lt", endTime);
			}

			//用户在线采样标识, 对应 UserStatusCount 表的type 字段     1零时统计   2:小时统计   3:天数统计
			short  minute_sampling = 1, hour_sampling = 2, day_sampling = 3;

			BasicDBObject queryType = new BasicDBObject("$eq",day_sampling); //默认筛选天数据

			if(1 == timeUnit){ //月数据
				queryType.append("$eq",day_sampling);
			}else if(2 == timeUnit) {//天数据
				queryType.append("$eq",day_sampling);
			}else if(timeUnit == 3) {//小时数据
				queryType.append("$eq",hour_sampling);
			}else if(timeUnit == 4) {//分钟数据
				queryType.append("$eq", minute_sampling);
			}

			BasicDBObject query = new BasicDBObject("time",queryTime).append("type", queryType);

			//获得用户集合对象
			DBCollection collection = SKBeanUtils.getDatastore().getCollection(UserStatusCount.class);

			String mapStr = "function Map() { "
		            + "var date = new Date(this.time*1000);"
		            +  "var year = date.getFullYear();"
					+  "var month = (\"0\" + (date.getMonth()+1)).slice(-2);"  //month 从0开始，此处要加1
					+  "var day = (\"0\" + date.getDate()).slice(-2);"
					+  "var hour = (\"0\" + date.getHours()).slice(-2);"
					+  "var minute = (\"0\" + date.getMinutes()).slice(-2);"
					+  "var dateStr = date.getFullYear()"+"+'-'+"+"(parseInt(date.getMonth())+1)"+"+'-'+"+"date.getDate();";

					if(timeUnit==1){ // counType=1: 每个月的数据
						mapStr += "var key= year + '-'+ month;";
					}else if(timeUnit==2){ // counType=2:每天的数据
						mapStr += "var key= year + '-'+ month + '-' + day;";
					}else if(timeUnit==3){ //counType=3 :每小时数据
						mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour +' : 00';";
					}else if(timeUnit==4){ //counType=4 :每分钟的数据
						mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour + ':'+ minute;";
					}

					mapStr += "emit(key,this.count);}";

			 String reduce = "function Reduce(key, values) {" +
				                "return Array.sum(values);" +
		                    "}";
			 MapReduceCommand.OutputType type =  MapReduceCommand.OutputType.INLINE;//
			 MapReduceCommand command = new MapReduceCommand(collection, mapStr, reduce,null, type,query);


			int i = 0;
			MapReduceOutput mapReduceOutput = null;
			while (i < 5) {
				i++;
				try {
					mapReduceOutput = collection.mapReduce(command);
					break;
				} catch (MongoSocketWriteException e) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					logger.info("retry getAddFriendsCount mapReduce:{}", i);
				}
			}
			if (null == mapReduceOutput) return countData;
			 Iterable<DBObject> results = mapReduceOutput.results();
			 Map<String,Object> map = new HashMap<String,Object>();
			for (Iterator iterator = results.iterator(); iterator.hasNext();) {
				DBObject obj = (DBObject) iterator.next();

				map.put((String)obj.get("_id"),obj.get("value"));
				countData.add(JSON.toJSON(map));
				map.clear();

			}

			return countData;

		}

		/** @Description:（设置消息免打扰）
		* @param offlineNoPushMsg
		* @return
		**/
		public User updatemessagefree(int offlineNoPushMsg) {
			Query<User> q = getDatastore().createQuery(User.class).field("_id").equal(ReqUtil.getUserId());
			UpdateOperations<User> ops = getDatastore().createUpdateOperations(getEntityClass());
			ops.set("offlineNoPushMsg", offlineNoPushMsg);
			User data = getDatastore().findAndModify(q, ops);
			data.setPayPassword("");
			return data;
		}

		/** @Description:（获取微信用户）
		* @param openid
		* @param userId
		* @return
		**/
	public WxUser getWxUser(String openid, Integer userId) {
		WxUser wxUser = null;
		if (!StringUtil.isEmpty(openid))
			wxUser = getDatastore().createQuery(WxUser.class).field("openId").equal(openid).get();
		else if (null != userId) {
			wxUser = getDatastore().createQuery(WxUser.class).field("wxuserId").equal(userId).get();
		}
		return wxUser;
	}

	/**
	 * @Description:（按注册时间降序排序用户）
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 **/
	public List<User> getUserlimit(int pageIndex, int pageSize,int isAuth) {

		Query<User> q = getDatastore().createQuery(User.class);

		if (1 == isAuth) {
			q.field("isAuth").equal(isAuth);
		}
		q.order("-createTime"); // 按创建时间降序排列

		List<User> dataList = q.offset(pageIndex * pageSize).limit(pageSize).asList();
		return dataList;
	}

	/**
	 * @Description:（附近的用户）
	 * @param poi
	 * @return
	 **/
	public List<User> nearbyUser(NearbyUser poi) {
		List<User> data = null;
		try {
			// 过滤隐私设置中关闭手机号和昵称搜索的用户
			Query<User> q = getDatastore().createQuery(User.class);
			if(null != poi.getSex())
				q.field("sex").equal(poi.getSex());
			q.disableValidation();
			int distance = poi.getDistance();
			Double d = 0d;
			if (0 == distance)
				distance = ConstantUtil.getAppDefDistance();
			d = distance / KConstants.LBS_KM;// 0.180180180.....

			if (0 != poi.getLatitude() && 0 != poi.getLongitude()){
				q.field("loc").near(poi.getLongitude(), poi.getLatitude(), d);
				// 附近的人排除自己
				q.field("_id").notEqual(ReqUtil.getUserId());
			}
			if (!StringUtil.isEmpty(poi.getNickname())) {
				Config config = SKBeanUtils.getAdminManager().getConfig();
				if (0 == config.getTelephoneSearchUser()) { //手机号搜索关闭

					if(0 == config.getNicknameSearchUser()) { //昵称搜索关闭
						q.field("account").equal(poi.getNickname());
					}else if(1 == config.getNicknameSearchUser()) { //昵称精准搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("nickname").equal(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
							);
					}else if(2 == config.getNicknameSearchUser()) { //昵称模糊搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("nickname").containsIgnoreCase(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
						);
					}

				}else if(1 == config.getTelephoneSearchUser()){ //手机号精确搜索

					if(0 == config.getNicknameSearchUser()) { //昵称搜索关闭
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("phone").equal(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0)
							);
					}else if(1 == config.getNicknameSearchUser()) { //昵称精准搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("phone").equal(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0),
							q.criteria("nickname").equal(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
							);
					}else if(2 == config.getNicknameSearchUser()) { //昵称模糊搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("phone").equal(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0),
							q.criteria("nickname").containsIgnoreCase(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
							);
					}

				}else if(2 == config.getTelephoneSearchUser()){ //手机号模糊搜索

					if(0 == config.getNicknameSearchUser()) { //昵称搜索关闭
						q.or(q.criteria("account").equal(poi.getNickname()),
							 q.criteria("phone").containsIgnoreCase(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0)
							);

					}else if(1 == config.getNicknameSearchUser()) { //昵称精准搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("phone").containsIgnoreCase(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0),
							q.criteria("nickname").equal(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
							);
					}else if(2 == config.getNicknameSearchUser()) { //昵称模糊搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							 q.criteria("phone").containsIgnoreCase(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0),
							 q.criteria("nickname").containsIgnoreCase(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
							 );
					}

				}

			}else if(0 == poi.getLatitude() && 0 == poi.getLongitude()) { //搜索关键字为空，且坐标没传的情况下不返回数据
				return null;
			}

			if (null != poi.getUserId()) {
				q.field("_id").equal(poi.getUserId());
			}
			if (null != poi.getSex()) {
				q.field("sex").equal(poi.getSex());
			}
			if (null != poi.getActive() && 0 != poi.getActive()) {
				q.field("active").greaterThanOrEq(DateUtil.currentTimeSeconds() - poi.getActive() * 86400000);
				q.field("active").lessThanOrEq(DateUtil.currentTimeSeconds());

			}
			//排除系统号
			q.field("_id").greaterThan(100050);

			q.project("sex", true)
			  .project("account", true)
			  .project("nickname", true)
			  .project("loc", true)
			  .project("createTime", true);

			q.offset(poi.getPageIndex() * (poi.getPageSize())).limit(poi.getPageSize());


			data = q.asList();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;

	}


	public PageVO nearbyUserWeb(NearbyUser poi) {

		List<User> data = null;
		try {
			Query<User> q = getDatastore().createQuery(User.class).field("_id").greaterThan(100050);
			q.disableValidation();
			int distance = poi.getDistance();
			Double d = 0d;
			if (0 == distance)
				distance = ConstantUtil.getAppDefDistance();
			d = distance / KConstants.LBS_KM;
			if (0 != poi.getLatitude() && 0 != poi.getLongitude())
				q.field("loc").near(poi.getLongitude(), poi.getLatitude(), d);
			if (!StringUtil.isEmpty(poi.getNickname())) {
				Config config = SKBeanUtils.getAdminManager().getConfig();
				if (0 == config.getTelephoneSearchUser()) { //手机号搜索关闭

					if(0 == config.getNicknameSearchUser()) { //昵称搜索关闭
						q.field("account").equal(poi.getNickname());
					}else if(1 == config.getNicknameSearchUser()) { //昵称精准搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("nickname").equal(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
							);
					}else if(2 == config.getNicknameSearchUser()) { //昵称模糊搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("nickname").containsIgnoreCase(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
						);
					}

				}else if(1 == config.getTelephoneSearchUser()){ //手机号精确搜索

					if(0 == config.getNicknameSearchUser()) { //昵称搜索关闭
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("phone").equal(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0)
							);
					}else if(1 == config.getNicknameSearchUser()) { //昵称精准搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("phone").equal(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0),
							q.criteria("nickname").equal(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
							);
					}else if(2 == config.getNicknameSearchUser()) { //昵称模糊搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("phone").equal(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0),
							q.criteria("nickname").containsIgnoreCase(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
							);
					}

				}else if(2 == config.getTelephoneSearchUser()){ //手机号模糊搜索

					if(0 == config.getNicknameSearchUser()) { //昵称搜索关闭
						q.or(q.criteria("account").equal(poi.getNickname()),
							 q.criteria("phone").containsIgnoreCase(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0)
							);

					}else if(1 == config.getNicknameSearchUser()) { //昵称精准搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							q.criteria("phone").containsIgnoreCase(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0),
							q.criteria("nickname").equal(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
							);
					}else if(2 == config.getNicknameSearchUser()) { //昵称模糊搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
							 q.criteria("phone").containsIgnoreCase(poi.getNickname()).criteria("settings.phoneSearch").notEqual(0),
							 q.criteria("nickname").containsIgnoreCase(poi.getNickname()).criteria("settings.nameSearch").notEqual(0)
							 );
					}

				}
			}else {
				return null;
			}
			if (null != poi.getUserId()) {
				q.field("_id").equal(poi.getUserId());
			}
			if (null != poi.getSex()) {
				q.field("sex").equal(poi.getSex());
			}
			if (null != poi.getActive() && 0 != poi.getActive()) {
				q.field("active").greaterThanOrEq(DateUtil.currentTimeSeconds() - poi.getActive() * 86400000);
				q.field("active").lessThanOrEq(DateUtil.currentTimeSeconds());

			}
			q.offset(poi.getPageIndex() * (poi.getPageSize())).limit(poi.getPageSize());
			q.project("sex", true)
			  .project("account", true)
			  .project("nickname", true)
			  .project("loc", true)
			  .project("createTime", true);
			data = q.asList();
			return new PageVO(data, q.count(), poi.getPageIndex(), poi.getPageSize());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 删除用户
	public void deleteUser(String... userIds){
		try {
			Integer systemUserId = 10005;
			for (String strUserId : userIds) {
				Integer userId = Integer.valueOf(strUserId);
				if (0 != userId) {
					DBCollection tdbCollection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("tig_users");
					String xmpphost=SKBeanUtils.getXMPPConfig().getServerName();
					tdbCollection.remove(new BasicDBObject("user_id", userId+"@"+xmpphost));
					// 发送xmpp通知 客户端更新本地数据
					consoleDeleteUserXmpp(systemUserId, userId);

					ThreadUtil.executeInThread(new Callback() {

						@Override
						public void execute(Object obj) {
							// 退出用户加入的群聊、解散创建的群组
							List<ObjectId> roomIdList = SKBeanUtils.getRoomManagerImplForIM().getRoomIdList(userId);
							try {

								roomIdList.forEach(roomId ->{
									SKBeanUtils.getRoomManagerImplForIM().deleteMember(getUser(userId), roomId, userId);
								});
								// 删除用户关系
								SKBeanUtils.getFriendsManager().deleteFansAndFriends(userId);
								// 删除通讯录好友
								SKBeanUtils.getAddressBookManger().delete(null, null, userId);
								// 删除用户的相关举报信息
								delReport(userId, null);
								// 删除用户的账单记录
								delRecord(userId);
								// 删除用户的角色信息
								SKBeanUtils.getRoleManager().deleteAllRoles(userId);
								// 删除用户组织架构相关信息
								delCompany(userId);
								DBCollection dbCollection = getDatastore().getCollection(User.class);
								dbCollection.remove(new BasicDBObject("_id", userId));
								KSessionUtil.removeAccessToken(userId);
								KSessionUtil.deleteUserByUserId(userId);

								// 清除redis中没有系统号的表
								SKBeanUtils.getRedisService().deleteNoSystemNumUserIds();
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}
						}
					});

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void consoleDeleteUserXmpp(Integer userId,Integer toUserId){
		List<Integer> friendsUserIdsList;
		List<Integer> friendsUserIds = SKBeanUtils.getRedisService().getFriendsUserIdsList(toUserId);
		if(null != friendsUserIds && friendsUserIds.size() > 0){
			friendsUserIdsList = friendsUserIds;
		}else{
			List<Integer> friendsUserIdsDB = SKBeanUtils.getFriendsManager().queryFansId(toUserId);
			friendsUserIdsList = friendsUserIdsDB;
		}
		log.info(" delete user  =====> userId : "+toUserId+"   好友friends : "+friendsUserIdsList);
		ThreadUtil.executeInThread(new Callback() {

			@Override
			public void execute(Object obj) {
				//以系统号发送删除好友通知
				//xmpp推送消息
				List<MessageBean> messageBeans = Collections.synchronizedList(new ArrayList<MessageBean>());
				friendsUserIdsList.forEach(strToUserId ->{
					MessageBean messageBean=new MessageBean();
					messageBean.setType(KXMPPServiceImpl.consoleDeleteUsers);
					messageBean.setFromUserId(userId.toString());
					messageBean.setFromUserName(("10005".equals(userId)?"后台管理员":getNickName(userId)));
					messageBean.setToUserId(strToUserId.toString());
					messageBean.setToUserName(getNickName(strToUserId));
					messageBean.setContent("后台管理员解除了你们好友关系");
					messageBean.setObjectId(toUserId);
					messageBean.setMessageId(StringUtil.randomUUID());
					messageBeans.add(messageBean);
					messageBean.setMsgType(0);
				});
				try {
					KXMPPServiceImpl.getInstance().send(friendsUserIdsList,messageBeans);
				} catch (Exception e) {
				}
			}
		});
	}

	@Override
	public SdkLoginInfo addSdkLoginInfo(int type, Integer userId, String loginInfo) {
		SdkLoginInfo entity=new SdkLoginInfo();
		entity.setType(type);
		entity.setLoginInfo(loginInfo);
		entity.setUserId(userId);
		entity.setCreateTime(DateUtil.currentTimeSeconds());
		getDatastore().save(entity);
		return entity;
	}

	/**
	 * 获取用户绑定信息
	 * @param userId
	 * @return
	 */
	public List<SdkLoginInfo> getBindInfo(Integer userId){
		Query<SdkLoginInfo> query = getDatastore().createQuery(SdkLoginInfo.class).field("userId").equal(userId);
		return query.asList();
	}

	/**
	 * 解除绑定
	 * @param type
	 * @param userId
	 * @return
	 */
	public JSONMessage unbind(int type,Integer userId){
		Query<SdkLoginInfo> query=getDatastore().createQuery(SdkLoginInfo.class).field("type").equal(type).field("userId").equal(userId);
		if(null!=query.get()){
			getDatastore().delete(query);
			return JSONMessage.success();
		}else{
			return JSONMessage.failure("绑定关系不存在");
		}
	}

	@Override
	public SdkLoginInfo findSdkLoginInfo(int type, String loginInfo) {
		Query<SdkLoginInfo> query=getDatastore().createQuery(SdkLoginInfo.class).field("type").equal(type).field("loginInfo").equal(loginInfo);
		return query.get();
	}

	@Override
	public JSONObject getWxOpenId(String code) {
		if(StringUtil.isEmpty(code)) {
			return null;
		}
		JSONObject jsonObject = WXUserUtils.getWxOpenId(code);
		String openid=jsonObject.getString("openid");
		if(StringUtil.isEmpty(openid)) {
			return null;
		}
		return jsonObject;
	}

	@Override
	public String getWxToken() {
		JSONObject jsonObject = WXUserUtils.getWxToken();
		String token=jsonObject.getString("access_token");
		return token;
	}

	/** @Description:是否开启多点登录
	* @return
	**/
	public boolean isOpenMultipleDevices(Integer userId){
		Integer multipleUserId = ReqUtil.getUserId();
		UserSettings settings = getUser(0 != multipleUserId ? multipleUserId : userId).getSettings();
		if (null == settings) settings = new UserSettings();
		return 1 == settings.getMultipleDevices() ? true : false;
	}

	/** @Description:多点登录数据同步
	* @param userId
	**/
	public void multipointLoginDataSync(Integer userId,String nickName,String operationType){
		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
					MessageBean messageBean=new MessageBean();
					messageBean.setType(KXMPPServiceImpl.multipointLoginDataSync);
					messageBean.setFromUserId(String.valueOf(userId));
					messageBean.setFromUserName(nickName);
					messageBean.setToUserId(String.valueOf(userId));
					messageBean.setToUserName(nickName);
					messageBean.setObjectId(operationType);
					messageBean.setMessageId(StringUtil.randomUUID());
					try {
						KXMPPServiceImpl.getInstance().send(messageBean);
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		});
	}

	/** @Description:多点登录下修改用户个人、好友资料,标签的通知
	* @param userId
	* @param nickName
	* @param type=0 :修改个人信息，type=1:修改好友相关设置
	**/
	public void updatePersonalInfo(Integer userId,String nickName,Integer toUserId,String toNickName,int type){
		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
					MessageBean messageBean=new MessageBean();
					messageBean.setType(KXMPPServiceImpl.updatePersonalInfo);
					messageBean.setFromUserId(String.valueOf(userId));
					messageBean.setFromUserName(nickName);
					if(1 == type){
						messageBean.setTo(String.valueOf(userId));
					}
					messageBean.setToUserId(null == toUserId ? String.valueOf(userId) : String.valueOf(toUserId));
					messageBean.setToUserName(StringUtil.isEmpty(toNickName) ? nickName : toNickName);
					messageBean.setMessageId(StringUtil.randomUUID());
					try {
						KXMPPServiceImpl.getInstance().send(messageBean);
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		});
	}

	/** @Description:获取离线时间段相关操作列表
	* @param userId
	* @param startTime
	* @return
	**/
	public List<OfflineOperation> getOfflineOperation(Integer userId,long startTime){
		Query<OfflineOperation> query = getDatastore().createQuery(OfflineOperation.class);
		query.field("userId").equal(userId).field("operationTime").greaterThanOrEq(startTime);
		return query.asList();
	}
}
