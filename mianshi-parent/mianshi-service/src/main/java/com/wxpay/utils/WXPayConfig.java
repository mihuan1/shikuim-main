package com.wxpay.utils;

public class WXPayConfig {

	/**
	 * 微信相关
	 */
	//调用统一下单接口(微信预支付)地址
	public final static String PREPAY_ID_URL="https://api.mch.weixin.qq.com/pay/unifiedorder";
	
	public final static String QUERY_URL="https://api.mch.weixin.qq.com/pay/orderquery";
	
	// 字符编码格式 目前支持 gbk 或 utf-8
	public static final String INPUT_CHARSET = "UTF-8";
	
	public final static String WX_APPID="";
	/**
	 *旧的 app key 2e2368adcbd69220c8f0fa43aa53e05a
	 */
	public final static String WXAPPSECRET="";
	public final static String WX_PARTNERKEY="";
	public final static String WXMCH_ID="1492798782";
	public final static String WXSPBILL_CREATE_IP="";
	public final static String TRADE_TYPE_JS="NATIVE";
	public final static String WX_JSAPI="APP";
	
	/**支付回调url*/
	public final static String WXPAY_NOTIFY_URL="http://imapi.server.com/user/recharge/wxPayCallBack";
	public final static String WXJSPAY_NOTIFY_URL="http://imapi.server.com/user/recharge/wxPayCallBack";
	
	
}
