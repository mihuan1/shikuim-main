package com.yipay.util;

import cn.xyz.commons.utils.HttpUtil;
import cn.xyz.mianshi.vo.PaidMerchant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sign.CommonSignUtil;
import com.yipay.dto.YipayPayRedirectParam;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 易支付测试网页：http://pay1.572657.com/SDK/
 */
@Slf4j
public class YipayUtil {
    public static String getPayRedirectUrl(YipayPayRedirectParam param, String url, String secret)  {
        return url + "?" + CommonSignUtil.getHoleParamHasSignMD5(param, secret);
    }
    //http://pay1.572657.com/api.php?act=order&pid={商户ID}&key={商户密钥}&out_trade_no={商户订单号
    public static JSONObject getSuccessTradeByOrder(PaidMerchant paidMerchant, String out_trade_no) {
        Map<String, String> params = new HashMap();
        params.put("act", "order");
        params.put("pid", paidMerchant.getVendorMerchantId());
        params.put("key", paidMerchant.getSign());
        params.put("out_trade_no", out_trade_no);
        JSONObject res = JSON.parseObject(HttpUtil.URLGet(paidMerchant.getOrderQueryUrl(), params));
        if (1 == res.getInteger("code") && 1 == res.getInteger("status")) {
            return res;
        }
        return null;
    }

    //http://pay1.572657.com/submit.php?out_trade_no=071578899262915&money=1.0&name=测试商品&sitename=易支付测试站&sign=8157a45dc226794d2f41de5fb8baee96&return_url=http://120.24.174.150:8092//vendor/pay/user/recharge/yiPayPageJump&pid=1000&notify_url=http://120.24.174.150:8092//vendor/pay/user/recharge/yiPayCallBack&type=alipay&sign_type=MD5
    //http://pay1.572657.com/SDK/
    public void signTemplate(String[] args) {
        YipayPayRedirectParam redirectParam = new YipayPayRedirectParam();
        //正常签名请求template
        redirectParam.setPid("1000");
        redirectParam.setType("alipay");
        redirectParam.setOut_trade_no("20200113143118349");
        redirectParam.setNotify_url("http://pay1.572657.com/SDK/notify_url.php");
        redirectParam.setReturn_url("http://pay1.572657.com/SDK/return_url.php");
        redirectParam.setName("测试商品");
        redirectParam.setMoney(0.01 + "");
        redirectParam.setSitename("易支付测试站");
        System.out.println(getPayRedirectUrl(redirectParam, "http://pay1.572657.com/submit.php", "z0ULJswmmk8jktDLY0JSYE5ses8T89SK"));
    }
}

