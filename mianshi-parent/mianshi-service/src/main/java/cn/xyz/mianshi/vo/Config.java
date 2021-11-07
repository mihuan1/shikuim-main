package cn.xyz.mianshi.vo;


import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.NotSaved;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity(value="config",noClassnameStored=true)
public class Config {
	
	private @Id long id=10000;
	
	public int XMPPTimeout=180;// xmpp超时时间  秒 
	
	//聊天内容的 文件有效期  默认  -1
	private int fileValidTime=-1;
	
	//聊天记录// 过期销毁时长   -1、0:永久    0.04：一小时    1：1天     7：一周     30:一个月      120:一季     365：一年
	private int chatRecordTimeOut=-1;  
	
	private int telephoneSearchUser = 1;  //手机号搜索用户 0 :关闭      1:精确搜索    2:模糊搜索   默认精准搜索
	
	private int nicknameSearchUser = 2; //昵称搜索用户  0 :关闭       1:精确搜索    2:模糊搜索   默认模糊搜索
	
	private int isTelephoneLogin=1; //手机号登陆   1 开启  0 关闭
	
	private int isUserIdLogin=1; //用户ID登陆   1 开启  0 关闭 

	private String helpUrl;
	
	private String videoLen;
	
	private String audioLen;
	
	private String shareUrl;
	
	private String softUrl;
	
	
	private int distance;
	
	private int isAuthApi=1;//检验api 权限
	
	private int isKeyWord;  // 是否开启关键词过滤   1:开启    0:关闭
	
	private int isSaveMsg=1;  // 是否保存单聊聊天记录    1:开启     0:关闭
	
	private int isSaveMucMsg=1; // 是否保存群聊聊天记录   1:是      0:否
	
	private int isMsgSendTime; // 是否强制同步消息发送时间   1:是   0:否
	
	private int regeditPhoneOrName = 0;// 0：使用手机号注册，1：使用用户名注册
	
	private int registerInviteCode; // 注册邀请码 0:关闭 1:开启一对一邀请（一码一用，且必填）2:开启一对多邀请（一码多用，选填项）
	
	private int isSaveRequestLogs=0; // 是否保存接口请求日志 0:不保存 1：保存
	
	private String privacyPolicyPrefix; // 隐私设置URL地址前缀
	
	/** 用户隐私设置参数  **/
	
	private double roamingTime=-2;// 漫游时长   -2:不漫游     -1、0:永久  0.04：一小时   1：1天    7:一周     30：一个月    120：一季    365：一年
	
	private double  outTimeDestroy=-1;// 过期销毁时长   -1、0:永久    0.04：一小时    1：1天     7：一周     30:一个月      120:一季     365：一年
	
	private String language="zh";// 客户端默认语种   
	
	private int isFriendsVerify = 1;// 是否需要好友验证   1:开启    0:关闭
	
	private int isEncrypt;// 是否开启加密传输    1:开启    0:关闭
	
	private int isMultiLogin=1;// 是否开启多点登录   1:开启     0:关闭
	
	private int isVibration; // 是否振动   1：开启    0：关闭
	
	private int isTyping; // 让对方知道我正在输入   1：开启       0：关闭
	
	private int isUseGoogleMap;// 使用google地图    1：开启   0：关闭
	
	private Integer isKeepalive = 1;// 是否安卓后台常驻保活app 0：取消保活  1：保活
	
	private Integer phoneSearch = 1;// 允许手机号搜索 1 允许 0 不允许
	
	private Integer nameSearch = 1;// 允许昵称搜索  1 允许 0 不允许
	
	private Integer showLastLoginTime = 1;// 显示上次上线时间   -1 所有人不显示 1所有人显示  2 所有好友显示   3 手机联系人显示
	
	private Integer showTelephone = 1;// 显示我的手机号码   -1 所有人不显示 1所有人显示  2 所有好友显示   3 手机联系人显示
	
	/** 建立群组默认参数设置  **/

	private int maxUserSize=1000;// 群成员人数上限
	
	private int isAttritionNotice=1;// 群组减员发送通知（踢人，退出） 1：开启  0：关闭
	
	private Integer isLook=1;// 群组是否可见   0为可见   1为不可见
	
	private int showRead=0;// 群主设置 群内消息是否发送已读 回执 显示数量  1：是 0：否
	
	private Integer isNeedVerify=0; // 加群是否需要通过验证  0：不要   1：要
	
	private int showMember=1;// 显示群成员给 普通用户   1 显示  0  不显示
	
	private int allowSendCard=1;// 允许发送名片 好友  1 允许  0  不允许
	
	private int allowInviteFriend=1;// 允许普通成员邀请好友  1：允许 0：不允许
	
	private int allowUploadFile=1;// 允许群成员上传群共享文件 1：允许 0：不允许
	
	private int allowConference=1;// 允许成员 召开会议  1：允许 0：不允许
	
	private int allowSpeakCourse=1;// 允许群成员 开启 讲课  1：允许 0：不允许
	
	private String iosPushServer="apns";//   apns 推送   
	
	private String SMSType = "aliyun";// 短信服务支持
	
	private byte imgVerificationCode = 0;// 图形验证码类型0：字母和数字混合，1：存数字
	
	private int isAutoAddressBook;// 是否自动添加通讯录好友 1：开启 0：关闭
	
	private double giftRatio = 0.50;// 直播礼物分成比例
	
	private String promotionUrl;// 客服推广链接
	
	private String defaultTelephones;// 注册默认自动添加为好友 的用户手机号 
	
	private byte isOpenSMSCode = 1; //是否开启短信验证码 1：开启 0：关闭
	
	private byte isOpenReceipt=1;//是否启用 消息回执1：开启 0：关闭
	
	private byte isOpenOnlineStatus=0;//是否开启在线状态1：开启 0：关闭
	
	private byte isOpenCluster;// 是否开启集群 1：开启 0：关闭
	
	private byte isOpenVoip;// 是否打开ios voip推送 1：开启 0：关闭

	private byte isOpenGoogleFCM;// 是否打开Android Google推送 1：开启 0：关闭
	
	@NotSaved
	private String ipAddress;// 当前请求的ip地址
	
	//以下为版本更新的字段
	private int androidVersion;  //Android 版本号
	
	private int iosVersion;  //ios版本号
	
	private String androidAppUrl;  //Android App的下载地址
	
	private String iosAppUrl;    // IOS App 的下载地址
	
	private String androidExplain; //Android 说明
	
	private String iosExplain;   // ios 说明
	
	public Config() {
		
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
}
