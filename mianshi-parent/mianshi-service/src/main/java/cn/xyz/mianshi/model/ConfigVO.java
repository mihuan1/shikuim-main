package cn.xyz.mianshi.model;

import cn.xyz.mianshi.vo.ClientConfig;
import cn.xyz.mianshi.vo.Config;
import lombok.Data;

/**
 * 
 * @Description: TODO(config数据返回封装类)
 * @author zhm
 * @date 2018年9月21日 下午5:44:40
 * @version V1.0
 */
@Data
public class ConfigVO {
	
	public String XMPPDomain;  // xmpp虚拟域名
	
	public String XMPPHost;  // xmpp主机host
	
	private String apiUrl;  // 接口URL
	
	private String downloadAvatarUrl;  // 头像访问URL
	
	private String downloadUrl;  // 资源访问URL
	
	private String uploadUrl;  // 资源上传URL
	
	private String liveUrl;  // 直播URL
	
	private String jitsiServer; // 视频服务器URL
	
	public int XMPPTimeout=15; // xmpp超时时间  秒 
	
	private int xmppPingTime=10;
	
	private int displayRedPacket=1;

	private int fileValidTime=-1; //聊天内容的 文件有效期  默认  -1
	
	private int chatRecordTimeOut=-1; //聊天记录过期时间  -1 为永久  数值 为天数
	
	private byte isOpenSMSCode = 1; //是否开启短信验证码
	
	private int showContactsUser=1; 
	
	private int isOpenRegister = 1; //是否开启注册
	private byte isOpenReceipt=1;//是否启用 消息回执
	private byte isOpenReadReceipt=1;
	
	private byte isOpenCluster;// 是否开启集群
	
	private String helpUrl;
	
	private String videoLen;
	
	private String audioLen;
	
	private String shareUrl;
	
	private String softUrl;
	
	private int distance;
	
	private String address;// 根据ip得到的地址
	private String ipAddress;// 当前请求的ip地址
	private byte isOpenAPNSorJPUSH = 0;// IOS推送平台 0：APNS  1：极光推送开发版  2：极光推送生产版
	private String privacyPolicyPrefix; // 隐私设置URL地址前缀
	/*
	 * 以下为版本更新的字段
	*/
	
	private int androidVersion;  //Android 版本号
	private int iosVersion;     //ios版本号
	
	private String androidAppUrl;  //Android App的下载地址
	
	private String iosAppUrl;    // IOS App 的下载地址
	
	private String androidExplain; //Android 说明
	
	private String iosExplain;   // ios 说明
	
	private int pcVersion;     // pc版本号
	
	private String pcAppUrl;  // pc 软件的下载地址
	
	private String pcExplain; // pc 说明
	
	private int macVersion;     // mac版本号
	
	private String macAppUrl;  // mac 软件的下载地址
	
	private String macExplain; // mac 说明
	
	private String androidDisable;// android禁用版本号（凡低于此版本号的禁用）
	
	private String iosDisable;// ios禁用版本号（凡低于此版本号的禁用）
	
	private String pcDisable;// pc禁用版本号（凡低于此版本号的禁用）
	
	private String macDisable;// mac禁用版本号（凡低于此版本号的禁用）
	
	private String companyName;// 公司名称
	
	private String copyright;// 版权信息
	
	private int hideSearchByFriends=1;// 是否隐藏好友搜索功能 0:隐藏 1：开启
	
	private String appleId;// IOS AppleId
	
	private String website;// 公司下载页网址
	
	private int regeditPhoneOrName = 0;// 0：使用手机号注册，1：使用用户名注册
	
	private int registerInviteCode = 0; //0:关闭 1:开启一对一邀请（一码一用，且必填）2:开启一对多邀请（一码多用，选填项）
	
	private int nicknameSearchUser  = 0; //昵称搜索用户  0：关闭   1：昵称精准搜索  2：昵称模糊搜索
	
	private int isCommonFindFriends = 0;// 普通用户是否能搜索好友 0:允许 1：不允许

	private int isCommonCreateGroup = 0;// 普通用户是否能建群 0:允许 1：不允许
	
	private int isOpenPositionService = 0;// 是否开启位置相关服务 0：开启 1：关闭 
	
	private String headBackgroundImg;// 头部导航背景图
	
	private byte isOpenGoogleFCM;// 是否打开Android Google推送 1：开启 0：关闭
	
	private byte isOpenRoomSearch = 0;// 是否开启群组搜索 0：开启 1：关闭
	
	private byte isOpenOnlineStatus=0;//是否开启在线状态1：开启 0：关闭
	
	private String popularAPP;// 热门应用,1:开启，0：关闭， 示例:{\"lifeCircle\":1,\"videoMeeting\":1,\"liveVideo\":1,\"shortVideo\":0,\"peopleNearby\":0,\"scan\":0}",  
	
	public ConfigVO() {}
	
	public ConfigVO(Config config,ClientConfig clientConfig) {
		XMPPDomain = clientConfig.getXMPPDomain();
		XMPPHost = clientConfig.getXMPPHost();
		this.apiUrl = clientConfig.getApiUrl();
		this.downloadAvatarUrl = clientConfig.getDownloadAvatarUrl();
		this.downloadUrl = clientConfig.getDownloadUrl();
		this.uploadUrl = clientConfig.getUploadUrl();
		this.liveUrl = clientConfig.getLiveUrl();
		this.jitsiServer = clientConfig.getJitsiServer();
		this.androidVersion = clientConfig.getAndroidVersion();
		this.iosVersion = clientConfig.getIosVersion();
		this.androidAppUrl = clientConfig.getAndroidAppUrl();
		this.iosAppUrl = clientConfig.getIosAppUrl();
		this.androidExplain = clientConfig.getAndroidExplain();
		this.iosExplain = clientConfig.getIosExplain();
		this.pcAppUrl = clientConfig.getPcAppUrl();
		this.pcVersion = clientConfig.getPcVersion();
		this.pcExplain = clientConfig.getPcExplain();
		this.macAppUrl = clientConfig.getMacAppUrl();
		this.macVersion = clientConfig.getMacVersion();
		this.macExplain = clientConfig.getMacExplain();
		this.androidDisable = clientConfig.getAndroidDisable();
		this.iosDisable = clientConfig.getIosDisable();
		this.pcDisable = clientConfig.getPcDisable();
		this.macDisable = clientConfig.getMacDisable();
		this.displayRedPacket = clientConfig.getDisplayRedPacket();
		this.showContactsUser = clientConfig.getShowContactsUser();
		this.isOpenRegister = clientConfig.getIsOpenRegister();
		this.isOpenReadReceipt=clientConfig.getIsOpenReadReceipt();
		this.XMPPTimeout = config.getXMPPTimeout();
		this.fileValidTime = config.getFileValidTime();
		this.chatRecordTimeOut = config.getChatRecordTimeOut();
		this.isOpenReceipt=config.getIsOpenReceipt();
		this.helpUrl = config.getHelpUrl();
		this.videoLen = config.getVideoLen();
		this.audioLen = config.getAudioLen();
		this.shareUrl = config.getShareUrl();
		this.softUrl = config.getSoftUrl();
		this.distance = config.getDistance();
		this.isOpenSMSCode = config.getIsOpenSMSCode();
		this.isOpenCluster=config.getIsOpenCluster();
		this.xmppPingTime = (int)(config.getXMPPTimeout()/2.5);
		this.companyName = clientConfig.getCompanyName();
		this.copyright = clientConfig.getCopyright();
		this.hideSearchByFriends = clientConfig.getHideSearchByFriends();
		this.appleId = clientConfig.getAppleId();
		this.website = clientConfig.getWebsite();
		this.regeditPhoneOrName = config.getRegeditPhoneOrName();
		this.registerInviteCode = config.getRegisterInviteCode();
		this.nicknameSearchUser = config.getNicknameSearchUser();
		this.isCommonFindFriends=clientConfig.getIsCommonFindFriends();
		this.isCommonCreateGroup=clientConfig.getIsCommonCreateGroup();
		this.isOpenPositionService=clientConfig.getIsOpenPositionService();
		this.address=clientConfig.getAddress();
		this.headBackgroundImg=clientConfig.getHeadBackgroundImg();
		this.isOpenGoogleFCM=config.getIsOpenGoogleFCM();
		this.popularAPP=clientConfig.getPopularAPP();
		this.ipAddress=config.getIpAddress();
		this.isOpenAPNSorJPUSH = clientConfig.getIsOpenAPNSorJPUSH();
		this.privacyPolicyPrefix = config.getPrivacyPolicyPrefix();
		this.isOpenRoomSearch = clientConfig.getIsOpenRoomSearch();
		this.isOpenOnlineStatus = config.getIsOpenOnlineStatus();
	}
	
	
}
