package cn.xyz.mianshi.opensdk.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import lombok.Getter;
import lombok.Setter;
/**
 * 
 * @Description: TODO(app应用)
 * @author Administrator
 * @date 2018年10月29日 下午4:42:19
 * @version V1.0
 */
@Getter
@Setter
@Entity(value = "SkOpenApp", noClassnameStored = true)
public class SkOpenApp {
	
	@Id
	private  ObjectId id; //记录id
	
	/**
	 * 账号ID
	 */
	private String accountId;
	
	private Long createTime;
	
	private Long modifyTime;
	
	/**
	 * 应用名称
	 */
	private String appName;
	/**
	 * 应用简介
	 */
	private String appIntroduction;
	/**
	 * 应用官网
	 */
	private String appUrl;
	
	/**
	 * 网站信息扫描件
	 */
	private String webInfoImg;
	
	/**
	 * 应用小 图片 28*28
	 */
	private String appsmallImg;
	/**
	 * 应用大图片 108*108
	 */
	private String appImg;
	/**
	 * appId
	 */
	private String appId;
	/**
	 * appSecret
	 */
	private String appSecret;
	/**
	 * 分享权限  0 未获得  1 已获得   2 申请中
	 */
	private Byte isAuthShare = 0;
	/**
	 * 登陆权限 0 未获得  1 已获得   2 申请中
	 */
	private Byte isAuthLogin = 0;
	/**
	 * 支付权限 0 未获得  1 已获得   2 申请中
	 */
	private Byte isAuthPay = 0;
	
	/**
	 * 是否开启群助手  0 未开启  1已开启  2 申请中
	 */
	private Byte isGroupHelper = 0;
	
	private String helperName;// 群助手名称
	
	private String helperDesc;// 群助手描述
	
	private String helperDeveloper;// 群助手开发者
	
	/**
	 * 支付回调域名
	 */
	private String payCallBackUrl;
	
	/**
	 * 状态 0 审核中 1正常 -1禁用 下架  2审核失败
	 */
	private Byte status = 0;
	/**
	 * IOs Bundle ID
	 */
	private String iosAppId;
	/**
	 * 测试版本Bundle ID
	 */
	private String iosBataAppId;
	/**
	 * Ios 下载地址
	 */
	private String iosDownloadUrl;
	
	/**
	 * android应用包名
	 */
	private String androidAppId;
	
	/**
	 * android下载地址
	 */
	private String androidDownloadUrl;
	/**
	 * 安卓应用签名
	 */
	private String androidSign;
	
	/**
	 * 网页应用授权回调域
	 */
	private String callbackUrl;
	
	/**
	 * 网站类型  1：app  2:网页
	 */
	private Byte appType = 0;
	
	public SkOpenApp(SkOpenApp skOpenApp) {
		if (!StringUtil.isEmpty(skOpenApp.getAndroidAppId()))
			this.androidAppId = skOpenApp.getAndroidAppId();
		if (!StringUtil.isEmpty(skOpenApp.getAndroidDownloadUrl()))
			this.androidDownloadUrl = skOpenApp.getAndroidDownloadUrl();
		if (!StringUtil.isEmpty(skOpenApp.getAndroidSign()))
			this.androidSign = skOpenApp.getAndroidSign();
		if (!StringUtil.isEmpty(skOpenApp.getAppIntroduction()))
			this.appIntroduction = skOpenApp.getAppIntroduction();
		if(!StringUtil.isEmpty(skOpenApp.getWebInfoImg())){
			this.webInfoImg = skOpenApp.getWebInfoImg();
		}
		if(!StringUtil.isEmpty(skOpenApp.getCallbackUrl())){
			this.callbackUrl = skOpenApp.getCallbackUrl();
		}
		this.accountId = skOpenApp.getAccountId();
		this.appName = skOpenApp.getAppName();
		this.createTime = DateUtil.currentTimeSeconds();
		this.modifyTime = DateUtil.currentTimeSeconds();
		this.appUrl = skOpenApp.getAppUrl();
		this.appsmallImg = skOpenApp.getAppsmallImg();
		this.appImg = skOpenApp.getAppImg();
		this.iosAppId = skOpenApp.getIosAppId();
		this.iosBataAppId = skOpenApp.getIosBataAppId();
		this.iosDownloadUrl = skOpenApp.getIosDownloadUrl();
		this.appType = skOpenApp.getAppType();
	}


	public SkOpenApp() {
		
	}
	
}
