package com.shiku.mianshi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商务圈接口
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/wx")
public class WXController extends AbstractController {

	private static Logger logger = LoggerFactory.getLogger(WXController.class);

	/*@Resource(name = "wxConfig")
	protected WXConfig wxConfig;*/
	
	private static final String TRANSFERS_PAY = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers"; // 企业付款

	
	private static final String TRANSFERS_PAY_QUERY = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo"; // 企业付款查询
	
	/**
	 * 企业向个人支付转账
	 * @param request
	 * @param response
	 * @param openid 用户openid
	 * @param callback
	 */
	/*@RequestMapping(value = "/pay", method = RequestMethod.POST)
	public void transferPay(HttpServletRequest request, HttpServletResponse response, String openid, String callback) {
		logger.info("[/transfer/pay]");
		//业务判断 openid是否有收款资格
		
		Map<String, String> restmap = null;
		try {
			Map<String, String> parm = new HashMap<String, String>();
			parm.put("mch_appid", wxConfig.getAppid()); //公众账号appid
			parm.put("mchid", wxConfig.getAppid()); //商户号
			parm.put("nonce_str", PayUtil.getNonceStr()); //随机字符串
			parm.put("partner_trade_no", PayUtil.getTransferNo()); //商户订单号
			parm.put("openid", openid); //用户openid	
			parm.put("check_name", "NO_CHECK"); //校验用户姓名选项 OPTION_CHECK
			//parm.put("re_user_name", "安迪"); //check_name设置为FORCE_CHECK或OPTION_CHECK，则必填
			parm.put("amount", "100"); //转账金额
			parm.put("desc", "测试转账到个人"); //企业付款描述信息
			parm.put("spbill_create_ip", PayUtil.getLocalIp(request)); //Ip地址
			parm.put("sign", PayUtil.getSign(parm, wxConfig.getSecret()));

			String restxml = HttpUtils.posts(TRANSFERS_PAY, XmlUtil.xmlFormat(parm, false));
			restmap = XmlUtil.xmlParse(restxml);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		if (CollectionUtil.isNotEmpty(restmap) && "SUCCESS".equals(restmap.get("result_code"))) {
			logger.info("转账成功：" + restmap.get("err_code") + ":" + restmap.get("err_code_des"));
			Map<String, String> transferMap = new HashMap<>();
			transferMap.put("partner_trade_no", restmap.get("partner_trade_no"));//商户转账订单号
			transferMap.put("payment_no", restmap.get("payment_no")); //微信订单号
			transferMap.put("payment_time", restmap.get("payment_time")); //微信支付成功时间
			WebUtil.response(response,
					WebUtil.packJsonp(callback,
							JSON.toJSONString(new JsonResult(1, "转账成功", new ResponseData(null, transferMap)),
									SerializerFeatureUtil.FEATURES)));
		}else {
			if (CollectionUtil.isNotEmpty(restmap)) {
				logger.info("转账失败：" + restmap.get("err_code") + ":" + restmap.get("err_code_des"));
			}
			
			WebUtil.response(response, WebUtil.packJsonp(callback, JSON
					.toJSONString(new JsonResult(-1, "转账失败", new ResponseData()), SerializerFeatureUtil.FEATURES)));
		}
	}*/
	

}
