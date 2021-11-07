package com.alipay.util;

import cn.xyz.commons.autoconfigure.KApplicationProperties.AliPayConfig;
import cn.xyz.mianshi.utils.SKBeanUtils;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.config.AlipayConfig;
import com.alipay.sign.RSA;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class AliPayUtil {
    static AliPayConfig aliPayConfig = SKBeanUtils.getLocalSpringBeanManager().getApplicationConfig().getAliPayConfig();
    public static String APP_ID = aliPayConfig.getAppid();
    public static String APP_PRIVATE_KEY = aliPayConfig.getApp_private_key();
    public static String CHARSET = aliPayConfig.getCharset();
    public static String ALIPAY_PUBLIC_KEY = aliPayConfig.getAlipay_public_key();
    public static String callBackUrl = aliPayConfig.getCallBackUrl();
    public static String PID = aliPayConfig.getPid();

    static AlipayClient alipayClient;

    public static AlipayClient getAliPayClient() {
        if (alipayClient != null) {
            return alipayClient;
        } else {
            alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2");
        }
        return alipayClient;
    }

    public static String getOutTradeNo() {
		// 产生2个0-9的随机数
        int r1 = (int) (Math.random() * (10));
        int r2 = (int) (Math.random() * (10));
		// 一个13位的时间戳
        long now = System.currentTimeMillis();
		// 订单ID
        String id = String.valueOf(r1) + String.valueOf(r2) + String.valueOf(now);
        return id;
    }

    /**
     * create the order info. 创建订单信息
     */
    public static String getOrderInfo(String subject, String body, String price, String orderNo) {

        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();

        model.setBody(body);
        model.setSubject(subject);
        model.setOutTradeNo(orderNo);
        model.setTimeoutExpress("30m");
        model.setTotalAmount(price);
        model.setProductCode("QUICK_MSECURITY_PAY");
//			model.setGoodsType("0");
        request.setBizModel(model);
        request.setNotifyUrl(callBackUrl);
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = getAliPayClient().sdkExecute(request);
            System.out.println("返回order  " + response.getBody());//就是orderString 可以直接给客户端请求，无需再做处理。

            return response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    public static String sign(String content) {
        return RSA.sign(content, AlipayConfig.private_key, AlipayConfig.input_charset);
    }

    /**
     * get the sign type we use. 获取签名方式
     *
     */
	  /* private String getSignType() {
	      return "sign_type=\"RSA\"";
	   }*/

    /**
     * 解析支付宝支付成功后返回的数据
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings("rawtypes")
    public static Map<String, String> getAlipayResult(javax.servlet.http.HttpServletRequest request) {
        // 获取支付宝POST过来反馈信息
        Map<String, String> params;
        params = new TreeMap<>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {

            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        return params;
    }

    public String getAuthInfo() throws AlipayApiException {
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        getAliPayClient().execute(request);
        return null;
    }
}
