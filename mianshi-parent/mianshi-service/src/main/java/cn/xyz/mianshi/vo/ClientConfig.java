package cn.xyz.mianshi.vo;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.Setter;
/**
 * 
 * @Description: TODO(客户端配置)
 * @author Administrator
 * @date 2018年9月20日 上午10:50:51
 * @version V1.0
 */

@Entity(value="clientConfig",noClassnameStored=true)
public class ClientConfig {
	@Id
	private long id = 10000;
	
	public String XMPPDomain;// xmpp虚拟域名
	
	public String XMPPHost;// xmpp主机host
	
	private String apiUrl;// 接口URL
	
	private String downloadAvatarUrl;// 头像访问URL
	
	private String downloadUrl;// 资源访问URL
	
	private String uploadUrl;// 资源上传URL
	
	private String liveUrl;// 直播URL
	
	private String jitsiServer;// 视频服务器URL
	
	private int isOpenRegister = 1; //是否开启注册
	
	private String address;// 请求config后得到用户IP，查询出地址
	/**
	 * 显示通讯录好友
	 * 0 不显示   1 显示
	 */
	private int  showContactsUser=1;
	
	private byte isOpenReadReceipt=1;//是否启用 已读消息回执
	
	private int displayRedPacket=1;// 是否开启ios红包
	
	private int hideSearchByFriends=1;// 是否隐藏好友搜索功能 0:隐藏 1：开启
	
	private String appleId;// IOS AppleId

	private String companyName;// 公司名称
	
	private String copyright;// 版权信息

	private String website;// 公司下载页网址
	
	private String headBackgroundImg;// 头部导航背景图
	
	private int isCommonFindFriends = 0;// 普通用户是否能搜索好友 0:允许 1：不允许

	private int isCommonCreateGroup = 0;// 普通用户是否能建群 0:允许 1：不允许
	
	private int isOpenPositionService = 0;// 是否开启位置相关服务 0：开启 1：关闭 
	
	private byte isOpenAPNSorJPUSH = 1;// IOS推送平台 0：APNS  1：极光推送开发版  2：极光推送生产版

	private byte isOpenRoomSearch = 0;// 是否开启群组搜索 0：开启 1：关闭
	
	
	// 以下为版本更新的字段
	private int androidVersion; // Android 版本号
	
	private int iosVersion; // ios版本号

	private String androidAppUrl; // Android App的下载地址

	private String iosAppUrl; // IOS App 的下载地址

	private String androidExplain; // Android 说明

	private String iosExplain; // ios 说明

	private int pcVersion;// pc版本号

	private String pcAppUrl;// pc 软件的下载地址

	private String pcExplain;// pc 说明

	private int macVersion;// mac版本号

	private String macAppUrl;// mac 软件的下载地址

	private String macExplain;// mac 说明

	private String androidDisable;// android禁用版本号（凡低于此版本号的禁用）

	private String iosDisable;// ios禁用版本号（凡低于此版本号的禁用）

	private String pcDisable;// pc禁用版本号（凡低于此版本号的禁用）

	private String macDisable;// mac禁用版本号（凡低于此版本号的禁用）
	
	public String popularAPP;// 热门应用,1:开启，0：关闭， 示例:{\"lifeCircle\":1,\"videoMeeting\":1,\"liveVideo\":1,\"shortVideo\":0,\"peopleNearby\":0,\"scan\":0}",

	public String haikeInviteCode;

	public String getHaikeInviteCode() {
		return haikeInviteCode;
	}

	public void setHaikeInviteCode(String haikeInviteCode) {
		this.haikeInviteCode = haikeInviteCode;
	}

	public String getXMPPDomain() {
		return XMPPDomain;
	}

	public void setXMPPDomain(String XMPPDomain) {
		this.XMPPDomain = XMPPDomain;
	}

	public String getXMPPHost() {
		return XMPPHost;
	}

	public void setXMPPHost(String XMPPHost) {
		this.XMPPHost = XMPPHost;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getDownloadAvatarUrl() {
		return downloadAvatarUrl;
	}

	public void setDownloadAvatarUrl(String downloadAvatarUrl) {
		this.downloadAvatarUrl = downloadAvatarUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getUploadUrl() {
		return uploadUrl;
	}

	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;
	}

	public String getLiveUrl() {
		return liveUrl;
	}

	public void setLiveUrl(String liveUrl) {
		this.liveUrl = liveUrl;
	}

	public String getJitsiServer() {
		return jitsiServer;
	}

	public void setJitsiServer(String jitsiServer) {
		this.jitsiServer = jitsiServer;
	}

	public int getIsOpenRegister() {
		return isOpenRegister;
	}

	public void setIsOpenRegister(int isOpenRegister) {
		this.isOpenRegister = isOpenRegister;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getShowContactsUser() {
		return showContactsUser;
	}

	public void setShowContactsUser(int showContactsUser) {
		this.showContactsUser = showContactsUser;
	}

	public byte getIsOpenReadReceipt() {
		return isOpenReadReceipt;
	}

	public void setIsOpenReadReceipt(byte isOpenReadReceipt) {
		this.isOpenReadReceipt = isOpenReadReceipt;
	}

	public int getDisplayRedPacket() {
		return displayRedPacket;
	}

	public void setDisplayRedPacket(int displayRedPacket) {
		this.displayRedPacket = displayRedPacket;
	}

	public int getHideSearchByFriends() {
		return hideSearchByFriends;
	}

	public void setHideSearchByFriends(int hideSearchByFriends) {
		this.hideSearchByFriends = hideSearchByFriends;
	}

	public String getAppleId() {
		return appleId;
	}

	public void setAppleId(String appleId) {
		this.appleId = appleId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getHeadBackgroundImg() {
		return headBackgroundImg;
	}

	public void setHeadBackgroundImg(String headBackgroundImg) {
		this.headBackgroundImg = headBackgroundImg;
	}

	public int getIsCommonFindFriends() {
		return isCommonFindFriends;
	}

	public void setIsCommonFindFriends(int isCommonFindFriends) {
		this.isCommonFindFriends = isCommonFindFriends;
	}

	public int getIsCommonCreateGroup() {
		return isCommonCreateGroup;
	}

	public void setIsCommonCreateGroup(int isCommonCreateGroup) {
		this.isCommonCreateGroup = isCommonCreateGroup;
	}

	public int getIsOpenPositionService() {
		return isOpenPositionService;
	}

	public void setIsOpenPositionService(int isOpenPositionService) {
		this.isOpenPositionService = isOpenPositionService;
	}

	public byte getIsOpenAPNSorJPUSH() {
		return isOpenAPNSorJPUSH;
	}

	public void setIsOpenAPNSorJPUSH(byte isOpenAPNSorJPUSH) {
		this.isOpenAPNSorJPUSH = isOpenAPNSorJPUSH;
	}

	public byte getIsOpenRoomSearch() {
		return isOpenRoomSearch;
	}

	public void setIsOpenRoomSearch(byte isOpenRoomSearch) {
		this.isOpenRoomSearch = isOpenRoomSearch;
	}

	public int getAndroidVersion() {
		return androidVersion;
	}

	public void setAndroidVersion(int androidVersion) {
		this.androidVersion = androidVersion;
	}

	public int getIosVersion() {
		return iosVersion;
	}

	public void setIosVersion(int iosVersion) {
		this.iosVersion = iosVersion;
	}

	public String getAndroidAppUrl() {
		return androidAppUrl;
	}

	public void setAndroidAppUrl(String androidAppUrl) {
		this.androidAppUrl = androidAppUrl;
	}

	public String getIosAppUrl() {
		return iosAppUrl;
	}

	public void setIosAppUrl(String iosAppUrl) {
		this.iosAppUrl = iosAppUrl;
	}

	public String getAndroidExplain() {
		return androidExplain;
	}

	public void setAndroidExplain(String androidExplain) {
		this.androidExplain = androidExplain;
	}

	public String getIosExplain() {
		return iosExplain;
	}

	public void setIosExplain(String iosExplain) {
		this.iosExplain = iosExplain;
	}

	public int getPcVersion() {
		return pcVersion;
	}

	public void setPcVersion(int pcVersion) {
		this.pcVersion = pcVersion;
	}

	public String getPcAppUrl() {
		return pcAppUrl;
	}

	public void setPcAppUrl(String pcAppUrl) {
		this.pcAppUrl = pcAppUrl;
	}

	public String getPcExplain() {
		return pcExplain;
	}

	public void setPcExplain(String pcExplain) {
		this.pcExplain = pcExplain;
	}

	public int getMacVersion() {
		return macVersion;
	}

	public void setMacVersion(int macVersion) {
		this.macVersion = macVersion;
	}

	public String getMacAppUrl() {
		return macAppUrl;
	}

	public void setMacAppUrl(String macAppUrl) {
		this.macAppUrl = macAppUrl;
	}

	public String getMacExplain() {
		return macExplain;
	}

	public void setMacExplain(String macExplain) {
		this.macExplain = macExplain;
	}

	public String getAndroidDisable() {
		return androidDisable;
	}

	public void setAndroidDisable(String androidDisable) {
		this.androidDisable = androidDisable;
	}

	public String getIosDisable() {
		return iosDisable;
	}

	public void setIosDisable(String iosDisable) {
		this.iosDisable = iosDisable;
	}

	public String getPcDisable() {
		return pcDisable;
	}

	public void setPcDisable(String pcDisable) {
		this.pcDisable = pcDisable;
	}

	public String getMacDisable() {
		return macDisable;
	}

	public void setMacDisable(String macDisable) {
		this.macDisable = macDisable;
	}

	public String getPopularAPP() {
		return popularAPP;
	}

	public void setPopularAPP(String popularAPP) {
		this.popularAPP = popularAPP;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
