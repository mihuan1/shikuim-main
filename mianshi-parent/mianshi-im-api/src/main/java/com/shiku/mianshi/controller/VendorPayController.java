package com.shiku.mianshi.controller;

import cn.xyz.commons.ex.BizException;
import cn.xyz.commons.utils.HttpUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.impl.VendorPayManagerImpl;
import cn.xyz.mianshi.utils.SKBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/vendor/pay")

public class VendorPayController extends AbstractController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/launch")
    public JSONMessage launch(@RequestParam String payType, @RequestParam Double money) {
        try {
            return JSONMessage.success((Object) SKBeanUtils.getVendorPayManager().getPayRedirectUrl(ReqUtil.getUserId(), payType, money));
        } catch (BizException e) {
            return JSONMessage.failure(e.getErrorMessage());
        }
    }

    /**
     * 页面跳转回调
     */
    @RequestMapping(value="/user/recharge/yiPayPageJump")
    public String yiPayPageJump(String pid, String trade_no, String out_trade_no, String type, String name, Double money, String trade_status, String sign, String sign_type) {
        if (VendorPayManagerImpl.TRADE_SUCCESS.equals(trade_status)) {
            logger.info("支付成功，发起跳转成功:{}", trade_no);
            return "支付成功";
        } else {
            return "支付失败";
        }
    }
    /**
     * 易支付成功回调
     */
    @RequestMapping(value="/user/recharge/yiPayCallBack")
    public String yiPayCallBack(String pid, String trade_no, String out_trade_no, String type, String name, Double money, String trade_status, String sign, String sign_type) {
        return SKBeanUtils.getVendorPayManager().doPayCallback(pid, trade_no, out_trade_no, type, name, money, trade_status, sign, sign_type);
    }
}
