package cn.xyz.mianshi.model;

import lombok.Data;

@Data
public class UserExample extends BaseExample {

    private Long birthday;

    private String description;

    private String idcard;

    private String idcardUrl;

    private String name;

    private String nickname;

    private String password;

    private Integer sex; //0:男   1:女

    private String telephone;

    /**
     * 账号
     */
    private String account;

    /**
     * account 加密后的通讯账号
     */
    private String encryAccount;

    private int userId = 0;

    private String areaCode = "86";

    private String randcode;

    private String phone;

    private Integer userType;

    private String appId;//ios  当前包名

    private int xmppVersion; //xmpp 心跳包的时候用到

    private Integer d = 0;

    private Integer w = 0;

    private String email;

    private String payPassWord; //支付密码

    private int multipleDevices = -1; //多设备登陆

    private byte isSmsRegister = 0; //是否使用短信验证码注册 0:不是  1:是

    private String area;// 用户地理位置

    private String myInviteCode; //我的邀请码

    private String inviteCode; //注册时填写的邀请码

    private String msgBackGroundUrl;// 朋友圈背景URL

    private int isSdkLogin;// 第三方登录标识  0 不是     1 是

    private int loginType;// 登录类型 0：账号密码登录，1：短信验证码登录

    private String verificationCode;// 短信验证码

    //当前登陆设备
    private String deviceKey;

    // 个人隐私设置
//	private int allowGreet=1;// 允许打招呼
    private int friendsVerify = -1;// 加好友需验证 1：开启    0：关闭

    private int isAdmin = 0;// 是否通过管理员添加 0 不是 1是
    //	private int openService=-1;//是否开启客服模式 1：开启    0：关闭
    private int isVibration = -1;// 是否振动   1：开启    0：关闭
    private int isTyping = -1;// 让对方知道我正在输入   1：开启       0：关闭
    private int isUseGoogleMap = -1;// 使用google地图    1：开启   0：关闭
    private int isEncrypt = -1;// 是否开启加密传输    1:开启    0:关闭
    //	private int multipleDevices=1;// 是否开启多点登录   1:开启     0:关闭
    private int closeTelephoneFind = 0;// 关闭手机号搜索用户    关闭次选项 不用使用手机号搜索用户   0 开启    1 关闭    默认开启

    //聊天记录 销毁  时间   -1 0  永久   1 一天
    private String chatRecordTimeOut = "0";

    private double chatSyncTimeLen = 0;//  聊天记录 最大 漫游时长    -1 永久  -2 不同步

    private Integer isKeepalive = -1;// 是否安卓后台常驻保活app 0：取消保活  1：保活

    private Integer showLastLoginTime = 0;// 显示上次上线时间   -1 所有人不显示 1所有人显示  2 所有好友显示   3 手机联系人显示

    private Integer showTelephone = 0;// 显示我的手机号码   -1 所有人不显示 1所有人显示  2 所有好友显示   3 手机联系人显示

    private Integer phoneSearch = -1;// 允许手机号搜索 1 允许 0 不允许

    private Integer nameSearch = -1;// 允许昵称搜索  1 允许 0 不允许

    private String friendFromList;// 通过什么方式添加我  0:系统添加好友 1:二维码 2：名片 3：群组 4： 手机号搜索 5： 昵称搜索

    public int getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
    }

	public Long getBirthday() {
		return birthday;
	}

    public void setBirthday(Long birthday) {
        this.birthday = birthday;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Integer getD() {
        return d;
    }

    public void setD(Integer d) {
        this.d = d;
    }

    public Integer getW() {
        return w;
    }

    public void setW(Integer w) {
        this.w = w;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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

    public String getRandcode() {
        return randcode;
    }

    public void setRandcode(String randcode) {
        this.randcode = randcode;
    }

    public int getXmppVersion() {
        return xmppVersion;
    }

    public void setXmppVersion(int xmppVersion) {
        this.xmppVersion = xmppVersion;
    }

    public int getMultipleDevices() {
        return multipleDevices;
    }

    public void setMultipleDevices(int multipleDevices) {
        this.multipleDevices = multipleDevices;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getPayPassWord() {
        return payPassWord;
    }

    public void setPayPassWord(String payPassWord) {
        this.payPassWord = payPassWord;
    }

    public byte getIsSmsRegister() {
        return isSmsRegister;
    }

    public void setIsSmsRegister(byte isSmsRegister) {
        this.isSmsRegister = isSmsRegister;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
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

    public int getIsSdkLogin() {
        return isSdkLogin;
    }

    public void setIsSdkLogin(int isSdkLogin) {
        this.isSdkLogin = isSdkLogin;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getLoginType() {
        return loginType;
    }

    public void setLoginType(int loginType) {
        this.loginType = loginType;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

}
