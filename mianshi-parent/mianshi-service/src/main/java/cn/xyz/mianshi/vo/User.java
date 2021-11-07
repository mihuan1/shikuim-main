package cn.xyz.mianshi.vo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.NotSaved;
import org.mongodb.morphia.utils.IndexDirection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.mianshi.model.UserExample;
import cn.xyz.mianshi.utils.SKBeanUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity(value = "user", noClassnameStored = true)
@Indexes({ @Index("status,birthday,sex,cityId") })
public class User {

	@Id
	private Integer userId;// 用户Id

	//@JSONField(serialize = false)

	@Indexed
	private String userKey;// 用户唯一标识

	/**
	 * 通讯账号 唯一
	 */
	@Indexed
	private String account;

	/**
	 * account 加密后的通讯账号
	 */
	private String encryAccount;

	/**
	 * 修改账号次数
	 */
	private int setAccountCount;


	@JSONField(serialize = false)
	private String username; //用户名

	//@JSONField(serialize = false)
	private String password;

	private String appId; //ios 需要判断的包名

	// 用户类型：1=普通用户；  2=公众号 ；
	private Integer userType=1;

	//消息免打扰
	private Integer offlineNoPushMsg=0;//1为开启  0为关闭

	@Setter
	@Getter
	private String openid;// 微信openId

	@Getter
	@Setter
	private String aliUserId; // 支付宝用户Id

	@Getter
	@Setter
	private String publicKey;
	@Getter
	@Setter
	private String privateKey;
	@Indexed
	private String areaCode;

	@Indexed
	private String telephone;

	@Indexed
	private String phone;

	private String name;// 姓名

	@Indexed(value = IndexDirection.ASC)
	private String nickname;// 昵称

	@Indexed(value = IndexDirection.ASC)
	private Long birthday;// 生日

	@Indexed(value = IndexDirection.ASC)
	private Integer sex;// 性别  0 女 1:男

	@Indexed(value = IndexDirection.ASC)
	private long active=0;// 最后出现时间

	@Indexed(value = IndexDirection.GEO2D)
	private Loc loc;// 地理位置

	private String description;// 签名、说说、备注

	private Integer countryId;// 国家Id

	private Integer provinceId;// 省份Id

	private Integer cityId;// 城市

	private Integer areaId;// 地区Id

	private Integer level;// 等级

	private Integer vip; // VIP级别

	private Double balance=0.0; //用户余额

	private Integer msgNum=0;//未读消息数量


	private Double totalRecharge=0.0;//充值总金额


	private Double totalConsume=0.0;//消费总金额


	private Integer friendsCount = 0;// 好友数


	private Integer fansCount = 0;// 粉丝数


	private Integer attCount = 0;// 关注数

	private Long createTime;// 注册时间

	private Long modifyTime;// 更新时间

	private String idcard;// 身份证号码

	private String idcardUrl;// 身份证图片地址

	private String msgBackGroundUrl;// 朋友圈背景URL

	private Integer isAuth = 0;// 是否认证 0:否  1:是  (该字段目前用于标示用户是否使用短信验证码注册--2018-10-11)

	private Integer status = 1;// 状态：1=正常, -1=禁用

	private @Indexed Integer onlinestate=0;   //在线状态，默认离线0  在线 1

	private String payPassword;// 用户支付密码

	private String regInviteCode; //注册时填写的邀请码

	//********************引用字段********************
	@NotSaved
	private String model;// 登录设备
	@NotSaved
	private long showLastLoginTime;// 最后上线时间

	private @NotSaved LoginLog loginLog;// 登录日志

	private UserSettings settings;// 用户设置

	private @NotSaved Company company;// 所属公司

	private @NotSaved Friends friends;// 好友关系

	private @NotSaved List<Integer> role;// 角色

	private @NotSaved String myInviteCode;//我的邀请码


	// 第三方帐号列表
	private @NotSaved List<ThridPartyAccount> accounts;

	// 关注列表
	private @NotSaved List<Friends> attList;


	// 好友列表
	private @NotSaved List<Friends> friendsList;

	//是否实名
	private @NotSaved Integer fourElements;

	//创建房间次数
	private int num=0;

	//是否暂停 0：正常 1：暂停
	private int isPasuse;

	// 用户的地理位置
	private String area;

	// ********************引用字段********************

	public Integer getUserId() {
		return userId;
	}


	public int getIsPasuse() {
		return isPasuse;
	}

	public void setIsPasuse(int isPasuse) {
		this.isPasuse = isPasuse;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getOfflineNoPushMsg() {
		return offlineNoPushMsg;
	}

	public void setOfflineNoPushMsg(Integer offlineNoPushMsg) {
		this.offlineNoPushMsg = offlineNoPushMsg;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Long getBirthday() {
		return birthday;
	}

	public void setBirthday(Long birthday) {
		this.birthday = birthday;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public long getActive() {
		return active;
	}

	public void setActive(long active) {
		this.active = active;
	}

	public Loc getLoc() {
		return loc;
	}

	public void setLoc(Loc loc) {
		this.loc = loc;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getCountryId() {
		return countryId;
	}

	public void setCountryId(Integer countryId) {
		this.countryId = countryId;
	}

	public Integer getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Integer provinceId) {
		this.provinceId = provinceId;
	}

	public Integer getCityId() {
		return cityId;
	}

	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}

	public Integer getAreaId() {
		return areaId;
	}

	public void setAreaId(Integer areaId) {
		this.areaId = areaId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getVip() {
		return vip;
	}

	public void setVip(Integer vip) {
		this.vip = vip;
	}


	public Integer getFriendsCount() {
		return friendsCount;
	}

	public void setFriendsCount(Integer friendsCount) {
		this.friendsCount = friendsCount;
	}

	public Integer getFansCount() {
		return fansCount;
	}

	public void setFansCount(Integer fansCount) {
		this.fansCount = fansCount;
	}

	public Integer getAttCount() {
		return attCount;
	}

	public void setAttCount(Integer attCount) {
		this.attCount = attCount;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Long getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Long modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	public String getIdcardUrl() {
		return idcardUrl;
	}

	public void setIdcardUrl(String idcardUrl) {
		this.idcardUrl = idcardUrl;
	}

	public Integer getIsAuth() {
		return isAuth;
	}

	public void setIsAuth(Integer isAuth) {
		this.isAuth = isAuth;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}



	public UserSettings getSettings() {
		return settings;
	}

	public void setSettings(UserSettings settings) {
		this.settings = settings;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Friends getFriends() {
		return friends;
	}

	public void setFriends(Friends friends) {
		this.friends = friends;
	}

	public List<ThridPartyAccount> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<ThridPartyAccount> accounts) {
		this.accounts = accounts;
	}

	public List<Friends> getAttList() {
		return attList;
	}

	public void setAttList(List<Friends> attList) {
		this.attList = attList;
	}


	public List<Friends> getFriendsList() {
		return friendsList;
	}

	public void setFriendsList(List<Friends> friendsList) {
		this.friendsList = friendsList;
	}



	public Integer getOnlinestate() {
		return onlinestate;
	}

	public void setOnlinestate(Integer onlinestate) {
		this.onlinestate = onlinestate;
	}
	public Double getTotalConsume() {
		return totalConsume;
	}

	public void setTotalConsume(Double totalConsume) {
		this.totalConsume = totalConsume;
	}

	public Double getTotalRecharge() {
		return totalRecharge;
	}

	public void setTotalRecharge(Double totalRecharge) {
		this.totalRecharge = totalRecharge;
	}
	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}
	// public static String buildUserKey(String telephone) {
	// return DigestUtils.md5Hex(DigestUtils.md5Hex(telephone));
	// }

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}


	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}


	public Integer getMsgNum() {
		return msgNum;
	}


	public void setMsgNum(Integer msgNum) {
		this.msgNum = msgNum;
	}

	public LoginLog getLoginLog() {
		return loginLog;
	}


	public void setLoginLog(LoginLog loginLog) {
		this.loginLog = loginLog;
	}


	public String getPayPassword() {
		return payPassword;
	}


	public void setPayPassword(String payPassword) {
		this.payPassword = payPassword;
	}


	public String getArea() {
		return area;
	}


	public void setArea(String area) {
		this.area = area;
	}

	public String getModel() {
		return model;
	}


	public void setModel(String model) {
		this.model = model;
	}

	public List<Integer> getRole() {
		return role;
	}


	public void setRole(List<Integer> role) {
		this.role = role;
	}


	public String getRegInviteCode() {
		return regInviteCode;
	}


	public void setRegInviteCode(String regInviteCode) {
		this.regInviteCode = regInviteCode;
	}


	public String getMyInviteCode() {
		return myInviteCode;
	}


	public void setMyInviteCode(String myInviteCode) {
		this.myInviteCode = myInviteCode;
	}


	public String getMsgBackGroundUrl() {
		return msgBackGroundUrl;
	}


	public void setMsgBackGroundUrl(String msgBackGroundUrl) {
		this.msgBackGroundUrl = msgBackGroundUrl;
	}

	public long getShowLastLoginTime() {
		return showLastLoginTime;
	}


	public void setShowLastLoginTime(long showLastLoginTime) {
		this.showLastLoginTime = showLastLoginTime;
	}

	public String getAccount() {
		return account;
	}


	public void setAccount(String account) {
		this.account = account;
	}


	public int getSetAccountCount() {
		return setAccountCount;
	}


	public void setSetAccountCount(int setAccountCount) {
		this.setAccountCount = setAccountCount;
	}

	public Integer getFourElements() {
		return fourElements;
	}

	public void setFourElements(Integer fourElements) {
		this.fourElements = fourElements;
	}

	//不是 本人 调用  设置 返回字段
	public void buildNoSelfUserVo() {
		this.setOpenid(null);
		this.setAliUserId(null);
		this.setAttCount(0);
		this.setFansCount(0);
		this.setFriendsCount(0);
		this.setMsgNum(0);
		this.setUserKey(null);
		this.setLoginLog(null);
		this.setOfflineNoPushMsg(null);
		this.setPayPassword(null);
		this.setTotalRecharge(0.0);
		this.setTotalConsume(0.0);
		this.setPrivateKey(null);
	}


	public String getEncryAccount() {
		return encryAccount;
	}


	public void setEncryAccount(String encryAccount) {
		this.encryAccount = encryAccount;
	}


	public static class LoginLog{


		private int isFirstLogin;
		private long loginTime;
		private String apiVersion;
		private String osVersion;
		private String model;
		private String serial;
		private double latitude;
		private double longitude;
		private String location;
		private String address;

		private long offlineTime;




		public int getIsFirstLogin() {
			return isFirstLogin;
		}

		public void setIsFirstLogin(int isFirstLogin) {
			this.isFirstLogin = isFirstLogin;
		}

		public long getLoginTime() {
			return loginTime;
		}

		public void setLoginTime(long loginTime) {
			this.loginTime = loginTime;
		}

		public String getApiVersion() {
			return apiVersion;
		}

		public void setApiVersion(String apiVersion) {
			this.apiVersion = apiVersion;
		}

		public String getOsVersion() {
			return osVersion;
		}

		public void setOsVersion(String osVersion) {
			this.osVersion = osVersion;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public String getSerial() {
			return serial;
		}

		public void setSerial(String serial) {
			this.serial = serial;
		}

		public double getLatitude() {
			return latitude;
		}

		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public long getOfflineTime() {
			return offlineTime;
		}

		public void setOfflineTime(long offlineTime) {
			this.offlineTime = offlineTime;
		}






	}
	public static class DeviceInfo{

		private long loginTime;
		/**
		 * 设备号   android  ios  web
		 */
		private String deviceKey;

		private String adress;// 地区标识  例  CN HK

		private int online;//在线状态

		//ios 推送 用到的 appId
		private String appId;

		/**
		 * 推送平台厂商
		 * 华为 huawei
		 * 小米 xiaomi
		 * 百度 baidu
		 * apns ios
		 */
		private String pushServer;

		/**
		 * 推送平台的 token
		 */
		private String pushToken;

		/**
		 * VOip  推送 token
		 */
		private String voipToken;

		/**
		 * 同时使用多个推送平台的
		 */
		//private Map<String,String> pushMap;

		/**
		 * 下线时间
		 */
		private long offlineTime;

		public long getLoginTime() {
			return loginTime;
		}

		public void setLoginTime(long loginTime) {
			this.loginTime = loginTime;
		}



		public String getPushServer() {
			return pushServer;
		}

		public void setPushServer(String pushServer) {
			this.pushServer = pushServer;
		}

		public String getPushToken() {
			return pushToken;
		}

		public void setPushToken(String pushToken) {
			this.pushToken = pushToken;
		}

		public long getOfflineTime() {
			return offlineTime;
		}

		public void setOfflineTime(long offlineTime) {
			this.offlineTime = offlineTime;
		}

		@Override
		public String toString() {
			return JSON.toJSONString(this);
		}

		public String getDeviceKey() {
			return deviceKey;
		}

		public void setDeviceKey(String deviceKey) {
			this.deviceKey = deviceKey;
		}

		public int getOnline() {
			return online;
		}

		public void setOnline(int online) {
			this.online = online;
		}

		public String getVoipToken() {
			return voipToken;
		}

		public void setVoipToken(String voipToken) {
			this.voipToken = voipToken;
		}

		public String getAppId() {
			return appId;
		}

		public void setAppId(String appId) {
			this.appId = appId;
		}

		public String getAdress() {
			return adress;
		}

		public void setAdress(String adress) {
			this.adress = adress;
		}









	}

	@Entity(value="userLoginLog",noClassnameStored=true)
	public static class UserLoginLog {

		@Id
		private Integer userId;
		/**
		 *
		* @Description: TODO(登陆日志信息)
		* @author lidaye
		* @date 2018年8月18日
		 */
		@Embedded
		private LoginLog loginLog;

		/**
		 * 登陆设备列表
		 * web DeviceInfo
		 * android  DeviceInfo
		 * ios DeviceInfo
		 */
		private Map<String,DeviceInfo> deviceMap;





		public UserLoginLog() {
			super();
		}
		public static LoginLog init(UserExample example, boolean isFirst) {
			LoginLog info = new LoginLog();
			info.setIsFirstLogin(isFirst ? 1 : 0);
			info.setLoginTime(DateUtil.currentTimeSeconds());
			info.setApiVersion(example.getApiVersion());
			info.setOsVersion(example.getOsVersion());

			info.setModel(example.getModel());
			info.setSerial(example.getSerial());
			info.setLatitude(example.getLatitude());
			info.setLongitude(example.getLongitude());
			info.setLocation(example.getLocation());
			info.setAddress(example.getAddress());
			info.setOfflineTime(0);

			return info;
		}
		public Integer getUserId() {
			return userId;
		}
		public void setUserId(Integer userId) {
			this.userId = userId;
		}
		public Map<String,DeviceInfo> getDeviceMap() {
			return deviceMap;
		}
		public void setDeviceMap(Map<String,DeviceInfo> deviceMap) {
			this.deviceMap = deviceMap;
		}
		public LoginLog getLoginLog() {
			return loginLog;
		}
		public void setLoginLog(LoginLog loginLog) {
			this.loginLog = loginLog;
		}

	}

	@Data
	public static class UserSettings {
		private int allowAtt=1;// 允许关注
		private int allowGreet=1;// 允许打招呼
		private int friendsVerify=1;// 加好友需验证
		private int openService=-1;//是否开启客服模式
		private int isVibration=1;// 是否振动   1：开启    0：关闭
		private int isTyping=1;// 让对方知道我正在输入   1：开启       0：关闭
		private int isUseGoogleMap=1;// 使用google地图    1：开启   0：关闭
		private int isEncrypt=1;// 是否开启加密传输    1:开启    0:关闭
		private int multipleDevices=1;// 是否开启多点登录   1:开启     0:关闭
		/**
		 * 关闭手机号搜索用户
		   关闭次选项 不用使用手机号搜索用户
		   0 开启    1 关闭
		   默认开启
		 */
		private int closeTelephoneFind=0;

		//聊天记录 销毁  时间   -1 0  永久   1 一天
		private String chatRecordTimeOut="0";

		private double chatSyncTimeLen=-1;//  聊天记录 最大 漫游时长    -1 永久  -2 不同步

		private Integer isKeepalive = 1;// 是否安卓后台常驻保活app 0：取消保活  1：保活

		private Integer showLastLoginTime = -1;// 显示上次上线时间   -1 所有人不显示 1所有人显示  2 所有好友显示   3 手机联系人显示

		private Integer showTelephone = -1;// 显示我的手机号码   -1 所有人不显示 1所有人显示  2 所有好友显示   3 手机联系人显示

		private Integer phoneSearch = 1;// 允许手机号搜索 1 允许 0 不允许

		private Integer nameSearch = 1;// 允许昵称搜索  1 允许 0 不允许

		private String friendFromList = "1,2,3,4,5";// 通过什么方式添加我  0:系统添加好友 1:二维码 2：名片 3：群组 4： 手机号搜索 5： 昵称搜索


		/*屏蔽  不开某些人的  生活圈  和 短视频*/
		private Set<Integer> filterCircleUserIds;




		/*sync*/
		public UserSettings() {
			super();
		}

		public UserSettings(int allowAtt, int allowGreet, int friendsVerify, int openService, int isVibration,
				int isTyping, int isUseGoogleMap, int isEncrypt, int multipleDevices,
				int closeTelephoneFind, String chatRecordTimeOut, double chatSyncTimeLen) {
			super();
			this.allowAtt = allowAtt;
			this.allowGreet = allowGreet;
			this.friendsVerify = friendsVerify;
			this.openService = openService;
			this.isVibration = isVibration;
			this.isTyping = isTyping;
			this.isUseGoogleMap = isUseGoogleMap;
			this.isEncrypt = isEncrypt;
			this.multipleDevices = multipleDevices;
			this.closeTelephoneFind = closeTelephoneFind;
			this.chatRecordTimeOut = chatRecordTimeOut;
			this.chatSyncTimeLen = chatSyncTimeLen;
		}



		public UserSettings(int openService) {
			super();
			this.openService = openService;
		}

		public static DBObject getDefault() {
			Config config = SKBeanUtils.getAdminManager().getConfig();
			DBObject dbObj = new BasicDBObject();
			dbObj.put("allowAtt", 1);// 允许关注
			dbObj.put("isVibration",config.getIsVibration());// 是否开启振动
			dbObj.put("isTyping",config.getIsTyping());// 让对方知道正在输入
			dbObj.put("isUseGoogleMap",config.getIsUseGoogleMap()); // 使用Google地图
			dbObj.put("allowGreet", 1);// 允许打招呼
			dbObj.put("friendsVerify", config.getIsFriendsVerify());// 加好友需要验证
			dbObj.put("openService", 0); // 是否开启客服模式
			dbObj.put("closeTelephoneFind",config.getTelephoneSearchUser());// 手机号搜索用户
			dbObj.put("chatRecordTimeOut",config.getOutTimeDestroy());// 聊天记录销毁时长
			dbObj.put("chatSyncTimeLen", config.getRoamingTime());// 漫游时长
			dbObj.put("isEncrypt",config.getIsEncrypt());// 加密传输
			dbObj.put("multipleDevices", config.getIsMultiLogin());// 支持多点登录
			dbObj.put("isKeepalive", config.getIsKeepalive());// 安卓保活
			dbObj.put("phoneSearch", config.getPhoneSearch());// 是否允许手机号搜索
			dbObj.put("nameSearch", config.getNameSearch());// 是否允许昵称号搜索
			dbObj.put("showLastLoginTime", config.getShowLastLoginTime());// 显示上次上线时间   -1 所有人不显示 1所有人显示  2 所有好友显示   3 手机联系人显示
			dbObj.put("showTelephone", config.getShowTelephone());// 显示我的手机号码   -1 所有人不显示 1所有人显示  2 所有好友显示   3 手机联系人显示
			dbObj.put("friendFromList", "1,2,3,4,5");// 通过什么方式添加我  0:系统添加好友 1:二维码 2：名片 3：群组 4： 手机号搜索 5： 昵称搜索
			return dbObj;
		}

	}


	public static class Count {
		private int att;
		private int fans;
		private int friends;

		public int getAtt() {
			return att;
		}

		public int getFans() {
			return fans;
		}

		public int getFriends() {
			return friends;
		}

		public void setAtt(int att) {
			this.att = att;
		}

		public void setFans(int fans) {
			this.fans = fans;
		}

		public void setFriends(int friends) {
			this.friends = friends;
		}
	}
	/**
	 * 坐标
	 *
	 * @author luorc@www.youjob.co
	 *
	 */
	public static class Loc {
		public Loc() {
			super();
		}

		public Loc(double lng, double lat) {
			super();
			this.lng = lng;
			this.lat = lat;
		}

		private double lng;// longitude  经度
		private double lat;// latitude   纬度

		public double getLng() {
			return lng;
		}

		public void setLng(double lng) {
			this.lng = lng;
		}

		public double getLat() {
			return lat;
		}

		public void setLat(double lat) {
			this.lat = lat;
		}

	}



	public static class ThridPartyAccount {

		private long createTime;
		private long modifyTime;
		private int status;// 状态（0：解绑；1：绑定）
		private String tpAccount;// 账号
		private String tpName;// 帐号所属平台名字或代码
		private String tpUserId;// 账号唯一标识

		public long getCreateTime() {
			return createTime;
		}

		public long getModifyTime() {
			return modifyTime;
		}

		public int getStatus() {
			return status;
		}

		public String getTpAccount() {
			return tpAccount;
		}

		public String getTpName() {
			return tpName;
		}

		public String getTpUserId() {
			return tpUserId;
		}

		public void setCreateTime(long createTime) {
			this.createTime = createTime;
		}

		public void setModifyTime(long modifyTime) {
			this.modifyTime = modifyTime;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public void setTpAccount(String tpAccount) {
			this.tpAccount = tpAccount;
		}

		public void setTpName(String tpName) {
			this.tpName = tpName;
		}

		public void setTpUserId(String tpUserId) {
			this.tpUserId = tpUserId;
		}

	}


}
