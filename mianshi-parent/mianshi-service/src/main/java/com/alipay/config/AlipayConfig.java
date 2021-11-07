package com.alipay.config;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.3
 *日期：2012-08-10
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
	
 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */

public class AlipayConfig {
	
	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// 合作身份者ID，以2088开头由16位纯数字组成的字符串
	public static String partner = "";//  2088001053772698
	public static final String SELLER = "";

	// 商户的私钥
	public static String private_key = "";	 
	
	// 支付宝的公钥，无需修改该值
	public static String ali_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB/zsuudywHfh27AtcJgF/upxOZW4X+6iLRlBlUv64l0A0qL+GSO2i8n9j90FaAN3orij+156JytyPVwIDAQAB";
	
	//
	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	

	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String input_charset = "utf-8";
	
	// 签名方式 不需修改
	public static String sign_type = "RSA";
	
	public static final String configUrl="config";
	/** 扫码成功的编码 */
	public static final String SUCCESS_CODE = "10000";

	/** 扫码失败的编码 */
	public static final String FAIL_CODE = "40004";

	/** 支付宝公钥-从支付宝服务窗获取 */
	//public static final String ALIPAY_PUBLIC_KEY =PropertiesUtils.getProperty(configUrl, "alipay_public_key");

	/** 签名编码-视支付宝服务窗要求 */
	public static final String SIGN_CHARSET = "UTF-8";

	/** 字符编码-传递给支付宝的数据编码 */
	public static final String CHARSET = "UTF-8";

	/** 签名类型-视支付宝服务窗要求 */
	public static final String SIGN_TYPE = "RSA";


	/** 服务窗appId */
	

	// 调试用，创建TXT日志文件夹路径
	public static final String LOG_PATH = "D:\\";

	

	// 签名方式 不需修改
	public static final String MD5_SIGNTYPE = "MD5";
	public static final String RSA_SIGNTYPE = "RSA";

	// 开发者请使用openssl生成的密钥替换此处
	// 请看文档：https://fuwu.alipay.com/platform/doc.htm#2-1接入指南
	// TODO !!!! 注：该私钥为测试账号私钥 开发者必须设置自己的私钥 , 否则会存在安全隐患
	//public static final String PRIVATE_KEY = ;

	// TODO !!!! 注：该公钥为测试账号公钥 开发者必须设置自己的公钥 ,否则会存在安全隐患
	//public static final String PUBLIC_KEY = ;

	/** 支付宝网关 */
	public static final String ALIPAY_GATEWAY = "https://openapi.alipay.com/gateway.do";

	/** 授权访问令牌的授权类型 */
	public static final String GRANT_TYPE = "authorization_code";
	
	
	
	public final static String ALIPAY_NOTIFY_URL="http://imapi.server.com/user/recharge/aliPayCallBack";
	public final static String ALIPAY_WAP_NOTIFY_URL="";

}
