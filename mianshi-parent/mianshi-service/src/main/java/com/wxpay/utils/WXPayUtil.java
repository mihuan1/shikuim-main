package com.wxpay.utils;





import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

import cn.xyz.commons.autoconfigure.KApplicationProperties.WXConfig;
import cn.xyz.mianshi.utils.SKBeanUtils;



public class WXPayUtil {
	
	private static Object Server;
	private static String QRfromGoogle;

	/**
	 * 把对象转换成字符串
	 * @param obj
	 * @return String 转换成字符串,若对象为null,则返回空字符串.
	 */
	public static String toString(Object obj) {
		if(obj == null)
			return "";
		
		return obj.toString();
	}
	
	/**
	 * 把对象转换为int数值.
	 * 
	 * @param obj
	 *            包含数字的对象.
	 * @return int 转换后的数值,对不能转换的对象返回0。
	 */
	public static int toInt(Object obj) {
		int a = 0;
		try {
			if (obj != null)
				a = Integer.parseInt(obj.toString());
		} catch (Exception e) {

		}
		return a;
	}
	
	/**
	 * 获取当前时间 yyyyMMddHHmmss
	 * @return String
	 */ 
	public static String getCurrTime() {
		Date now = new Date();
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String s = outFormat.format(now);
		return s;
	}
	
	/**
	 * 获取当前日期 yyyyMMdd
	 * @param date
	 * @return String
	 */
	public static String formatDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String strDate = formatter.format(date);
		return strDate;
	}
	
	/**
	 * 取出一个指定长度大小的随机正整数.
	 * 
	 * @param length
	 *            int 设定所取出随机数的长度。length小于11
	 * @return int 返回生成的随机数。
	 */
	public static int buildRandom(int length) {
		int num = 1;
		double random = Math.random();
		if (random < 0.1) {
			random = random + 0.1;
		}
		for (int i = 0; i < length; i++) {
			num = num * 10;
		}
		return (int) ((random * num));
	}
	
	/**
	 * 获取编码字符集
	 * @param request
	 * @param response
	 * @return String
	 */

	public static String getCharacterEncoding(HttpServletRequest request,
			HttpServletResponse response) {
		
		if(null == request || null == response) {
			return "gbk";
		}
		
		String enc = request.getCharacterEncoding();
		if(null == enc || "".equals(enc)) {
			enc = response.getCharacterEncoding();
		}
		
		if(null == enc || "".equals(enc)) {
			enc = "gbk";
		}
		
		return enc;
	}
	
	public  static String URLencode(String content){
		
		String URLencode;
		
		URLencode= replace(Server.equals(content), "+", "%20");
		
		return URLencode;
	}
	private static String replace(boolean equals, String string, String string2) {
		
		return null;
	}

	/**
	 * 获取unix时间，从1970-01-01 00:00:00开始的秒数
	 * @param date
	 * @return long
	 */
	public static long getUnixTime(Date date) {
		if( null == date ) {
			return 0;
		}
		
		return date.getTime()/1000;
	}
	
	 public static String QRfromGoogle(String chl)
	    {
	        int widhtHeight = 300;
	        String EC_level = "L";
	        int margin = 0;
	        String QRfromGoogle;
	        chl = URLencode(chl);
	        
	        QRfromGoogle = "http://chart.apis.google.com/chart?chs=" + widhtHeight + "x" + widhtHeight + "&cht=qr&chld=" + EC_level + "|" + margin + "&chl=" + chl;
	       
	        return QRfromGoogle;
	    }

	/**
	 * 时间转换成字符串
	 * @param date 时间
	 * @param formatType 格式化类型
	 * @return String
	 */
	public static String date2String(Date date, String formatType) {
		SimpleDateFormat sdf = new SimpleDateFormat(formatType);
		return sdf.format(date);
	}
	
	/**
	 * 获取请求预支付id报文
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static SortedMap<String, String> getPackage(WxPayDto wxPayDto) {
		WXConfig wxConfig = SKBeanUtils.getLocalSpringBeanManager().getApplicationConfig().getWxConfig();
		// 附加数据 原样返回
		//String attach = "";
		// 总金额以分为单位，不带小数点
		String totalFee = getMoney(wxPayDto.getTotalFee());
		// 随机字符串
		String nonce_str = getNonceStr();
		SortedMap<String, String> contentMap = new TreeMap<String, String>();
		contentMap.put("appid", wxConfig.getAppid());
		contentMap.put("body", wxPayDto.getBody());
		contentMap.put("input_charset", "UTF-8");
		contentMap.put("mch_id",wxConfig.getMchid());
		contentMap.put("nonce_str", nonce_str);
		//contentMap.put("attach", attach);
		contentMap.put("out_trade_no", wxPayDto.getOrderId());
		
		// 这里写的金额为1 分到时修改
		contentMap.put("total_fee", totalFee);
		contentMap.put("spbill_create_ip", wxPayDto.getSpbillCreateIp());
		contentMap.put("notify_url", wxConfig.getCallBackUrl());

		contentMap.put("trade_type", "APP");
		//contentMap.put("openid", openId);

		RequestHandler reqHandler = new RequestHandler(null, null);
		reqHandler.init(wxConfig.getAppid(), wxConfig.getSecret(), wxConfig.getApiKey());

		String sign = reqHandler.createSign(contentMap);
		contentMap.put("sign", sign);
		String xml = WXPayUtil.paramsToxmlStr(contentMap);
		
		String prepay_id = new GetWxOrderno().getPayNo(WXPayConfig.PREPAY_ID_URL, xml);
		//获取prepay_id后，拼接最后请求支付所需要的package
		
		SortedMap<String, String> payMap = new TreeMap<String, String>();
		String timestamp = Sha1Util.getTimeStamp();
		 nonce_str = getNonceStr();
		contentMap.clear();
		contentMap.put("appid", wxConfig.getAppid()); 
		contentMap.put("noncestr", nonce_str);  
		contentMap.put("package", "Sign=WXPay");  
		contentMap.put("partnerid",wxConfig.getMchid()); 
		contentMap.put("prepayid", prepay_id); 
		contentMap.put("timestamp", timestamp);  
		
		
		
		payMap.put("appId",wxConfig.getAppid());  
		payMap.put("partnerId",wxConfig.getMchid()); 
		payMap.put("prepayId", prepay_id); 
		payMap.put("timeStamp", timestamp);  
		payMap.put("nonceStr", nonce_str);  
		payMap.put("package", "Sign=WXPay");  
		
		contentMap.put("prepayid", prepay_id);
		
		payMap.put("sign",reqHandler.createSign(contentMap));
		
		return payMap;
	}
	
	
	/**
	 * 获取随机字符串
	 * @return
	 */
	public static String getNonceStr() {
		// 随机数
		String currTime = WXPayUtil.getCurrTime();
		// 8位日期
		String strTime = currTime.substring(5, currTime.length());
		// 四位随机数
		String strRandom = WXPayUtil.buildRandom(4) + "";
		// 10位序列号,可以自行调整。
		return strTime + strRandom;
	}

	/**
	 * 元转换成分
	 * @param money
	 * @return
	 */
	public static String getMoney(String amount) {
		if(amount==null){
			return "";
		}
		// 金额转化为分为单位
		String currency =  amount.replaceAll("\\$|\\￥|\\,", "");  //处理包含, ￥ 或者$的金额  
        int index = currency.indexOf(".");  
        int length = currency.length();  
        Long amLong = 0l;  
        if(index == -1){  
            amLong = Long.valueOf(currency+"00");  
        }else if(length - index >= 3){  
            amLong = Long.valueOf((currency.substring(0, index+3)).replace(".", ""));  
        }else if(length - index == 2){  
            amLong = Long.valueOf((currency.substring(0, index+2)).replace(".", "")+0);  
        }else{  
            amLong = Long.valueOf((currency.substring(0, index+1)).replace(".", "")+"00");  
        }  
        return amLong.toString(); 
	}
	
	
	public static String createSign(TreeMap<String, String> treeMap,String partnerKey) {
		String string1 = WXPayUtil.originalStr(treeMap, true);
		String stringSignTemp = string1 + "key=" + partnerKey;
		System.out.println("签名调试输出：" + stringSignTemp);
		String sign = MD5Util
				.MD5Encode(stringSignTemp, WXPayConfig.INPUT_CHARSET)
				.toUpperCase();
		return sign;
	}
	/**
	 * 组装原始串
	 * @param treeMap
	 * @param endTag 是否保留最后的&
	 * @return
	 */
	public static String originalStr(Map<String,String> treeMap,boolean endTag){
		Set<Entry<String, String>> entry = treeMap.entrySet();
		StringBuffer sb = new StringBuffer();
		for(Entry<String,String> obj : entry){
			String k = obj.getKey();
			String v = obj.getValue();
			if(StringUtils.isEmpty(v))continue;
			sb.append(k+"="+v+"&");
		}
		if(!endTag){
			String params = sb.substring(0, sb.lastIndexOf("&"));
			return params;
		}
		return sb.toString();
	}
	public static String paramsToxmlStr(Map<String, String> param) {
		  StringBuffer buffer = new StringBuffer(); 
	        buffer.append("<xml>");
	        if (param != null && !param.isEmpty()) {  
	            for (Map.Entry<String, String> entry : param.entrySet()) {  
	            	if(entry.getKey().equals("attach") || entry.getKey().equals("body") || entry.getKey().equals("sign")){
	            		buffer.append("<"+entry.getKey()+">");
	            		buffer.append("<![CDATA["+entry.getValue()+"]]>");
	            		buffer.append("</"+entry.getKey()+">");
	            	}else{
	            		buffer.append("<"+entry.getKey()+">");
	            		buffer.append(entry.getValue());
	            		buffer.append("</"+entry.getKey()+">");
	            	}
	            }  
	        }  
	        buffer.append("</xml>");
	        return buffer.toString();
	}
	public static WxPayResult mapToWxPayResult(Map<String, String> m) {
		WxPayResult wpr = new WxPayResult();
			wpr.setAppid(m.get("appid"));
			wpr.setBankType(m.get("bank_type"));
			wpr.setCashFee(m.get("cash_fee"));
			wpr.setFeeType(m.get("fee_type"));
			wpr.setIsSubscribe(m.get("is_subscribe"));
			wpr.setMchId(m.get("mch_id"));
			wpr.setNonceStr(m.get("nonce_str"));
			wpr.setOpenid(m.get("openid"));
			wpr.setOutTradeNo(m.get("out_trade_no"));
			wpr.setResultCode(m.get("result_code"));
			wpr.setReturnCode(m.get("return_code"));
			wpr.setSign(m.get("sign"));
			wpr.setTimeEnd(m.get("time_end"));
			wpr.setTotalFee(m.get("total_fee"));
			wpr.setTradeType(m.get("trade_type"));
			wpr.setTransactionId(m.get("transaction_id"));
	        return wpr;
	}
}
	
	










