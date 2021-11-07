package com.yipay.dto;

import lombok.Data;

@Data
public class YipayPayRedirectParam {
    //商户ID
    private String pid;
    //支付方式:	alipay:支付宝,tenpay:财付通,qqpay:QQ钱包,wxpay:微信支付

    /**
     * @see cn.xyz.commons.constants.KConstants.ThirdPayType
     */
    private String type;
    //商户订单号
    private String out_trade_no;
    //服务器异步通知地址
    private String notify_url;
    //页面跳转通知地址
    private String return_url;
    //商品名称
    private String name;
    //金额
    private String money;
    //网站名称
    private String sitename;
    //签名字符串
    private String sign;
    //MD5
    private String sign_type;
}
