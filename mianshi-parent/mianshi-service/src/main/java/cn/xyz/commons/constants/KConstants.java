package cn.xyz.commons.constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.xyz.commons.vo.JSONMessage;
import lombok.Getter;
import lombok.Setter;


/**
 * 常量
 * 
 * @author luorc
 * 
 */
public class KConstants {
	
	public static boolean isDebug=true;
	
	public static final String PAGE_INDEX = "0";
	public static final String PAGE_SIZE = "15";
	
	public static final int MOENY_ADD = 1; //金钱增加
	
	public static final int MOENY_REDUCE = 2; //金钱减少
	
	public static final double LBS_KM=111.01;
	
	public static final int LBS_DISTANCE=50;
	
	
	// 不经过普通接口校验
	public static final Set<String> filterSet = new HashSet<String>(){{
		
		add("/redPacket/sendRedPacket");// 发送红包
		
		add("/redPacket/sendRedPacket/v1");// 发送红包新版
		
		add("/redPacket/openRedPacket");// 打开红包
		
		add("/user/recharge/getSign");// 充值
		
		add("/transfer/wx/pay");// 企业向个人支付转账
		
		add("/alipay/transfer");// 支付宝提现
		
		add("/skTransfer/sendTransfer");// 系统转账
		
		add("/skTransfer/receiveTransfer");// 接受转账
		
		add("/pay/codePayment");// 付款码支付
		
		add("/pay/codeReceipt");// 二维码收款
		
		add("/pay/passwordPayment");// 对外支付
		
	}};

	
	/**
	 * 用户ID 起始值 
	 */
	public static final int MIN_USERID=100000;
	/**
	 * 数据库分表 取余  计算值
	 * @author lidaye
	 *
	 */
	public interface DB_REMAINDER{
		/**
		 * 用户  联系人表  取余数
		 */
		public static final int ADDRESSBOOK=10000;
		/**
		 * 群成员
		 */
		public static final int MEMBER=10000;
		
		/**
		 * 好友
		 */
		public static final int FIRENDS=10000;
		
		/**
		 * 
		 */
		public static final int DEFAULT=10000;
	}
	/**
	* @Description: TODO(设备标识)
	* @author lidaye
	* @date 2018年8月20日
	 */
	public interface DeviceKey{
		public static final String Android= "android";
		public static final String IOS= "ios";
		public static final String WEB= "web";
		public static final String PC= "pc";
		public static final String MAC="mac";
	}
	/**
	* @Description: TODO(推送平台)
	* @author lidaye
	* @date 2018年8月20日
	 */
	public interface PUSHSERVER{
		//apns 推送
		public static final String APNS= "apns";
		
		public static final String APNS_VOIP= "apns_voip";
		//百度 推送
		public static final String BAIDU= "baidu";
		//小米 推送
		public static final String XIAOMI= "xiaomi";
		//华为 推送
		public static final String HUAWEI= "huawei";
		//极光 推送
		public static final String JPUSH= "Jpush";
		// google fcm推送
		public static final String FCM = "fcm";
		// 魅族 推送
		public static final String MEIZU = "meizu";
		// VIVO 推送
		public static final String VIVO = "vivo";
		// OPPO 推送
		public static final String OPPO = "oppo";
	}
	
	// 消费类型 
	public interface ConsumeType {
		public static final int USER_RECHARGE = 1;// 用户充值
		public static final int PUT_RAISE_CASH = 2;// 用户提现
		public static final int SYSTEM_RECHARGE = 3;// 后台充值
		public static final int SEND_REDPACKET = 4;// 发红包
		public static final int RECEIVE_REDPACKET = 5;// 领取红包
		public static final int REFUND_REDPACKET = 6;// 红包退款
		public static final int SEND_TRANSFER = 7;// 转账
		public static final int RECEIVE_TRANSFER = 8;// 接受转账
		public static final int REFUND_TRANSFER = 9;// 转账退回
		public static final int SEND_PAYMENTCODE = 10;// 付款码付款
		public static final int RECEIVE_PAYMENTCODE = 11;// 付款码收款
		public static final int SEND_QRCODE = 12;// 二维码收款 付款方
		public static final int RECEIVE_QRCODE = 13;// 二维码收款 收款方
		
		public static final int LIVE_GIVE = 14;// 直播送礼物 
		public static final int LIVE_RECEIVE=15;// 直播收到礼物

		public static final int SYSTEM_HANDCASH=16;// 后台手工提现
		public static final int SYSTEM_RETURN = 17;// 后台返还
	}
	
	public interface Room_Role{
		/**
		 * 群组 创建者
		 */
		public static final byte CREATOR=1;
		/**
		 * 管理员
		 */
		public static final byte ADMIN=2;
		/**
		 * 群成员
		 */
		public static final byte MEMBER=3;
		
	}
	
	// 后台角色权限
	public interface Admin_Role{
		// 游客  没有系统账单访问权限，没有财务人员访问权限，没有压测的访问权限，其他所有后台功能没有操作权限，只提供数据浏览
		public static final byte TOURIST = 1;
		// 公众号
		public static final byte PUBLIC = 2;
		// 机器人账号
		public static final byte ROBOT = 3;
		// 客服  提供用户，群组，相关聊天记录，朋友圈相关 的数据浏览
		public static final byte CUSTOMER = 4;
		// 管理员 除了 没有系统配置的操作权限，没有系统账单访问权限，没有财务人员访问权限，其他功能同超级管理员
		public static final byte ADMIN = 5;
		// 超级管理员 所有权限
		public static final byte SUPER_ADMIN = 6;
		// 财务  提供用户，群组，相关聊天记录，系统账单，红包,直播相关 的数据浏览   和账单相关的操作
		public static final byte FINANCE = 7;
	}
	
	// 集群配置标识
	public interface CLUSTERKEY{
		public static final int XMPP=1;// xmpp服务器
		public static final int HTTP=2;// http服务器
		public static final int VIDEO=3;// 视频服务器
		public static final int LIVE=4;// 直播服务器
	}
	
	//订单状态
	public interface OrderStatus {
		public static final int CREATE = 0;// 创建
		public static final int END = 1;// 成功
		public static final int DELETE = -1;// 删除
	}
	//支付方式
	public interface PayType {
		public static final int ALIPAY = 1;// 支付宝支付
		public static final int WXPAY = 2;// 微信支付
		public static final int BALANCEAY = 3;// 余额支付
		public static final int SYSTEMPAY = 4;// 系统支付
	}
	public interface Key {
		public static final String RANDCODE = "KSMSService:randcode:%s";
		public static final String IMGCODE = "KSMSService:imgcode:%s";
	}

	//public static final KServiceException InternalException = new KServiceException(KConstants.ErrCode.InternalException,KConstants.ResultMsg.InternalException);

	public interface Expire {
		
		static final int DAY1 = 86400;
		static final int DAY7 = 604800;
		static final int HOUR12 = 43200;
		static final int HOUR=3600;
		static final int HALF_AN_HOUR=1800;
		static final int MINUTE=60;
	}

	
	public interface SystemNo{
		static final int System=10000;//系统号码
		static final int NewKFriend=10001;//新朋友
		static final int Circle=10002;//商务圈
		static final int AddressBook=10003;//通讯录
		static final int Notice=10006;//系统通知
		
	}
	/**
	* @Description: TODO(举报原因)
	* @author lidaye
	* @date 2018年8月9日
	 */
	public interface ReportReason{
		static final Map<Integer,String> reasonMap=new HashMap<Integer, String>() {
            {
                put(100, "发布不适当内容对我造成骚扰");
                put(101, "发布色情内容对我造成骚扰");
                put(102, "发布违法违禁内容对我造成骚扰");
                put(103, "发布赌博内容对我造成骚扰");
                put(104, "发布政治造谣内容对我造成骚扰");
                put(105, "发布暴恐血腥内容对我造成骚扰");
                put(106, "发布其他违规内容对我造成骚扰");
                
                put(120, "存在欺诈骗钱行为");
                put(130, "此账号可能被盗用了");
                put(140, "存在侵权行为");
                put(150, "发布仿冒品信息");
                
                put(200, "群成员存在赌博行为");
                put(210, "群成员存在欺诈骗钱行为");
                put(220, "群成员发布不适当内容对我造成骚扰");
                put(230, "群成员传播谣言信息");
                
                put(300, "网页包含欺诈信息(如：假红包)");
                put(301, "网页包含色情信息");
                put(302, "网页包含暴力恐怖信息");
                put(303, "网页包含政治敏感信息");
                put(304, "网页在收集个人隐私信息(如：钓鱼链接)");
                put(305, "网页包含诱导分享/关注性质的内容");
                put(306, "网页可能包含谣言信息");
                put(307, "网页包含赌博信息");
            }
        };
		
	}

	public interface Result {
		static final JSONMessage InternalException = new JSONMessage(1020101, "接口内部异常");
		static final JSONMessage ParamsAuthFail = new JSONMessage(1010101, "请求参数验证失败，缺少必填参数或参数错误");
		static final JSONMessage TokenEillegal = new JSONMessage(1030101, "缺少访问令牌");
		static final JSONMessage TokenInvalid = new JSONMessage(1030102, "访问令牌过期或无效");
		static final JSONMessage AUTH_FAILED = new JSONMessage(1030103, "权限验证失败");
	}
	public interface ResultCode {
		
		//接口调用成功
		static final int Success = 1;
		
		//接口调用失败
		static final int Failure = 0;
		
		//请求参数验证失败，缺少必填参数或参数错误
		static final int ParamsAuthFail = 1010101;
		
		//缺少请求参数：
		static final int ParamsLack = 1010102;
		
		//接口内部异常
		static final int InternalException = 1020101;
		
		//链接已失效
		static final int Link_Expired = 1020102;
		
		//缺少访问令牌
		static final int TokenEillegal = 1030101;
		
		//访问令牌过期或无效
		static final int TokenInvalid = 1030102;
		
		//权限验证失败
		static final int AUTH_FAILED = 1030103;
		
		//帐号不存在
		static final int AccountNotExist = 1040101;
		
		//帐号或密码错误
		static final int AccountOrPasswordIncorrect = 1040102;
		
		//原密码错误
		static final int OldPasswordIsWrong = 1040103;
		
		//短信验证码错误或已过期
		static final int VerifyCodeErrOrExpired = 1040104;
		
		//发送验证码失败,请重发!
		static final int SedMsgFail = 1040105;
		
		//请不要频繁请求短信验证码，等待{0}秒后再次请求
		static final int ManySedMsg = 1040106;
		
		//手机号码已注册!
		static final int PhoneRegistered = 1040107;
		
		//余额不足
		static final int InsufficientBalance = 1040201;
		//支付密码未设置
		static final int PayPasswordNotExist = 1040202;
		
		//支付密码错误
		static final int PayPasswordIsWrong = 1040203;
		
		//请输入图形验证码
		static final int NullImgCode=1040215;
		
		//图形验证码错误
		static final int ImgCodeError=1040216;

		//没有选择支付方式!
		static final int NotSelectPayType = 1040301;
		
		//支付宝支付后回调出错：
		static final int AliPayCallBack_FAILED = 1040302;
		
		//你没有权限删除!
		static final int NotPermissionDelete = 1040303;
		
		//账号被锁定
		static final int ACCOUNT_IS_LOCKED = 1040304;
		
		// 第三方登录未绑定手机号码
		static final int UNBindingTelephone = 1040305;
		
		// 第三方登录提示账号不存在
		static final int SdkLoginNotExist = 1040306;
		
		// 二维码未被扫取
		static final int QRCodeNotScanned = 1040307;
		//二维码已扫码未登录
		static final int QRCodeScannedNotLogin = 1040308;
		//二维码已扫码登陆
		static final int QRCodeScannedLoginEd = 1040309;
		
		//二维码已失效
		static final int QRCode_TimeOut = 1040310;
		
		// 红包领取超时
		static final int RedPacket_TimeOut = 100101;
		
		// 你手太慢啦  已经被领完了!
		static final int RedPacket_NoMore = 100102;
		
	}
	
	
	public interface ResultMsgs {
		
		String PERMISSION_ERROR="权限不足";
		String ParamsAuthFail="请求参数验证失败，缺少必填参数或参数错误";
		String ROOM_NOT_EXIST="群组 不存在 或已解散!";
		
		String DATA_NOT_EXIST="数据不存在 或已删除!";
		
		
		
	}

	public interface ErrCodes {
		static final String InternalException ="InternalException";
		static final String ParamsAuthFail ="ParamsAuthFail";
		static final String TokenEillegal ="TokenEillegal";
		static final String TokenInvalid ="TokenInvalid";
		static final String AUTH_FAILED ="AUTH_FAILED";
		static final String PublishVerify_FAILED ="PublishVerify_FAILED";
		static final String AliPayCallBack_FAILED="AliPayCallBack_FAILED";
		static final String NotExistSendResume_FAILED="NotExistSendResume_FAILED";
		static final String NotCreateResume="NotCreateResume";
		static final String NotSelectPayType="NotSelectPayType";
		static final String PhoneRegistered="PhoneRegistered";
		static final String AccountNotExist="AccountNotExist";
		static final String AccountOrPasswordIncorrect="AccountOrPasswordIncorrect";
		static final String OldPasswordIsWrong="OldPasswordIsWrong";
		static final String VerifyCodeErrOrExpired="VerifyCodeErrOrExpired";
		static final String InsufficientBalance="InsufficientBalance";
		static final String OpenTalkResumeNotDetailed="OpenTalkResumeNotDetailed";
		static final String Resume_ConditionNotSatisfied="Resume_ConditionNotSatisfied";
		static final String NotTalk_Oneself="NotTalk_Oneself";	
	}
	
	// 多点登录下操作类型
	public interface MultipointLogin {
		static final String SYNC_LOGIN_PASSWORD = "sync_login_password";// 修改密码
		static final String SYNC_PAY_PASSWORD = "sync_pay_password";// 支付密码
		static final String SYNC_PRIVATE_SETTINGS = "sync_private_settings";// 隐私设置
		static final String SYNC_LABEL = "sync_label";// 好友标签
		
		static final String TAG_FRIEND = "friend";// 好友相关
		static final String TAG_ROOM = "room";// 群组标签相关
		static final String TAG_LABLE = "label";// 好友分组操作相关
	}
	
	//公司相关常量
	public interface Company{
		static final byte COMPANY_CREATER = 3;  //公司创建者
		static final byte COMPANY_MANAGER = 2;  //公司管理员
		static final byte DEPARTMENT_MANNAGER = 1; //部门管理者
		static final byte COMMON_EMPLOYEE = 0; //普通员工
	}

	public static final Map<Integer, String> BANK_MAP = new HashMap<>();
	static {
		for (Bank bank : Bank.values()) {
			BANK_MAP.put(bank.getId(), bank.getName());
		}
	}
	//支持绑卡的银行
	@Getter
	public enum  Bank {
		ALIPAY(100, "支付宝"),
		BOC(101, "中国银行"),
		CCB(102, "中国建设银行"),
		ICBC(103, "中国工商银行"),
		ABC(104, "中国农业银行"),
		BOCM(105, "中国交通银行"),
		PSBC(106, "中国邮政储蓄"),
		;
		private int id;
		private String name;

		Bank(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	//银行卡种类
	public interface BankCardType {
		byte DEPOSIT_CARD = 0;  //储蓄卡
		byte CREDIT_CARD = 1;  //信用卡
	}

	//审核状态
	public interface AuditStatusCons {
		byte AUDIT_PASS = 0;  //审核通过
		byte AUDIT_WAIT = 1;  //待审核
		byte AUDIT_FAIL = 2;  //审核拒绝
	}

	//审核状态
	@Getter
	public enum AuditStatus {
		AUDIT_PASS(0, "%s成功"),
		AUDIT_WAIT(1, "%s待审核"),
		AUDIT_FAIL(2, "%s审核拒绝"),
		;
		private int id;
		private String name;

		AuditStatus(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	//资金流动种类
	public interface FundsChangeType {
		String DRAWINGS = "提现";
	}

	//支付方式
	public interface ThirdPayType {
		//alipay:支付宝,tenpay:财付通,qqpay:QQ钱包,wxpay:微信支付
		String ALIPAY = "alipay";
		String TENPAY = "tenpay";
		String QQPAY = "qqpay";
		String WXPAY = "wxpay";
	}

	public static Map<String, Integer> PAY_TYPE_MAP = new HashMap<>();

	static {
		PAY_TYPE_MAP.put(ThirdPayType.ALIPAY, PayType.ALIPAY);
		PAY_TYPE_MAP.put(ThirdPayType.TENPAY, PayType.WXPAY);
		PAY_TYPE_MAP.put(ThirdPayType.QQPAY, PayType.WXPAY);
		PAY_TYPE_MAP.put(ThirdPayType.WXPAY, PayType.WXPAY);
	}

	//支付平台渠道
	public interface PayVendorChannel {
		//yipay:易支付
		String YIPAY = "yipay";
	}
	
}
