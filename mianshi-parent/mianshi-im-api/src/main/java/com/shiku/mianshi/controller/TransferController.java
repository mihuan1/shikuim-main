package com.shiku.mianshi.controller;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shiku.utils.Money;
import com.wxpay.utils.HttpUtils;
import com.wxpay.utils.PayUtil;
import com.wxpay.utils.WXNotify;
import com.wxpay.utils.WXPayUtil;
import com.wxpay.utils.XmlUtil;

import cn.xyz.commons.autoconfigure.KApplicationProperties.AppConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.WXConfig;
import cn.xyz.commons.constants.KConstants.ResultCode;
import cn.xyz.commons.utils.CollectionUtil;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.NumberUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.impl.TransfersRecordManagerImpl;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.TransfersRecord;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.AuthServiceUtils;

/**
 * 微信 提现的接口
 * 
 * @author andy
 * @version 2.2
 */

@RestController
@RequestMapping("/transfer")
public class TransferController extends AbstractController{

	
	
	private static final String TRANSFERS_PAY = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers"; // 企业付款

	private static final String TRANSFERS_PAY_QUERY = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo"; // 企业付款查询
	
	@Resource
	private WXConfig wxConfig;
	
	@Resource
	private AppConfig appConfig;
	
	@Autowired
	private TransfersRecordManagerImpl transfersManager;
	
	
	/**
	 * 企业向个人支付转账
	 * @param request
	 * @param response
	 * @param openid 用户openid
	 * @param callback
	 */
	@RequestMapping(value = "/wx/pay", method = RequestMethod.POST)
	public JSONMessage transferPay(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(defaultValue="") String amount,@RequestParam(defaultValue="0") long time,
			@RequestParam(defaultValue="") String secret, String callback) {
		if(StringUtil.isEmpty(amount)) {
			return JSONMessage.failure("请输入提现金额！");
		}else if(StringUtil.isEmpty(secret)) {
			return JSONMessage.failure("缺少提现密钥");
		}
		
		int userId = ReqUtil.getUserId();
		User user = SKBeanUtils.getUserManager().getUser(userId);
		if(null==user) {
			//return JSONMessage.failure("");
		}
		/**
		 * 默认提现 0.3元
		 */
		//amount="30";
		
		String openid=user.getOpenid();
		//业务判断 openid是否有收款资格
		if(StringUtil.isEmpty(openid)) {
			return JSONMessage.failure("请先 微信授权 没有授权不能提现 ");
		}else if(!AuthServiceUtils.authRequestTime(time)) {
			return JSONMessage.failure("授权认证失败");
		}
		
		DecimalFormat df = new DecimalFormat("#.00");
		/**
		 * 0.5
		 * 提现金额
		 */
		double total=Double.valueOf(amount)/100;
		if(0.5>total) {
			return JSONMessage.failure("提现最低限制 0.5元 ");
		}
		String token = getAccess_token();
		
		
		if(!AuthServiceUtils.authWxTransferPay(user.getPayPassword(),userId+"", token, amount,openid,time, secret)) {
			return JSONMessage.failure("输入密码错误");
		}
		
		if(StringUtil.isEmpty(amount)) {
			return JSONMessage.failure("请输入提现金额！");
		}
		return wxWithdrawalPay(amount,user,request.getRemoteAddr());
		
	}
	
	/**
	 * 企业向个人支付转账
	 * @param request
	 * @param response
	 * @param openid 用户openid
	 * @param callback
	 */
	@RequestMapping(value = "/wx/pay/v1", method = RequestMethod.POST)
	public JSONMessage transferPayV1(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(defaultValue="") String data,
			@RequestParam(defaultValue="") String codeId, String callback) {
		int userId = ReqUtil.getUserId();
		String token = getAccess_token();
		String code = SKBeanUtils.getRedisService().queryTransactionSignCode(userId, codeId);
		if(StringUtil.isEmpty(code))
			return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
		User user = SKBeanUtils.getUserManager().getUser(userId);
		
		String openid=user.getOpenid();
		//业务判断 openid是否有收款资格
		if(StringUtil.isEmpty(openid)) {
			return JSONMessage.failure("请先 微信授权 没有授权不能提现 ");
		}
		JSONObject jsonObj = AuthServiceUtils.authWxWithdrawalPay(userId+"", token, data, code, user.getPayPassword());
		if(null==jsonObj) {
			return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
		}
		String amount = jsonObj.getString("amount");
		
		if(StringUtil.isEmpty(amount)) {
			return JSONMessage.failure("请输入提现金额！");
		}
		return wxWithdrawalPay(amount,user,request.getRemoteAddr());
		
	}
	
	private JSONMessage wxWithdrawalPay(String amount,User user,String remoteAddr) {
		int userId=user.getUserId();
		String openid=user.getOpenid();
		/**
		 * 默认提现 0.3元
		 */
		//amount="30";
		
		
		
		DecimalFormat df = new DecimalFormat("#.00");
		/**
		 * 0.5
		 * 提现金额
		 */
		double total=Double.valueOf(amount);
		if(0.5>total) {
			return JSONMessage.failure("提现最低限制 0.5元 ");
		}
		//total=total*100;
		/**
		 * 0.01
		 * 
		 * 0.6%
		 * 提现手续费
		 */
		double fee =Double.valueOf(df.format((total*0.006)));
		if(0.01>fee) {
			fee=0.01;
		}else  {
			fee=NumberUtil.getCeil(fee, 2);
		}
		
		/**
		 * 0.49
		 * 实际到账金额
		 */
		Double totalFee= Double.valueOf(df.format(total-fee));
		
		if(totalFee>user.getBalance()) {
			return JSONMessage.failure("账号余额不足 请先充值 ");
		}
		
		/**
		 * 49.0
		 */
		Double realFee=(totalFee*100);
		
		/**
		 * 49
		 */
		String realFeeStr=realFee.intValue()+"";
		
		logger.info(String.format("=== transferPay userid %s username %s 提现金额   %s 手续费   %s  到账金额   %s ", 
				userId,user.getNickname(),total,fee,totalFee));
		/**
		 * ow9Ctwy_qP8OoLr_6T-5oMnBud8w
		 */
		
		
		
		
		Map<String, String> restmap = null;
		
		TransfersRecord record=new TransfersRecord();
		try {
			record.setUserId(userId);
			record.setAppid(wxConfig.getAppid());
			record.setMchId(wxConfig.getMchid());
			record.setNonceStr(WXPayUtil.getNonceStr());
			record.setOutTradeNo(StringUtil.getOutTradeNo());
			record.setOpenid(openid);
			record.setTotalFee(amount);
			record.setFee(fee+"");
			record.setRealFee(realFeeStr);
			record.setCreateTime(DateUtil.currentTimeSeconds());
			record.setStatus(0);
			
			Map<String, String> parm = new HashMap<String, String>();
			parm.put("mch_appid", wxConfig.getAppid()); //公众账号appid
			parm.put("mchid", wxConfig.getMchid()); //商户号
			parm.put("nonce_str", record.getNonceStr()); //随机字符串
			parm.put("partner_trade_no", record.getOutTradeNo()); //商户订单号
			parm.put("openid", openid); //用户openid	
			parm.put("check_name", "NO_CHECK"); //校验用户姓名选项 OPTION_CHECK
			//parm.put("re_user_name", "安迪"); //check_name设置为FORCE_CHECK或OPTION_CHECK，则必填
			parm.put("amount", realFeeStr); //转账金额
			parm.put("desc", "即时通讯提现"); //企业付款描述信息
			parm.put("spbill_create_ip", remoteAddr); //支付Ip地址
			parm.put("sign", PayUtil.getSign(parm, wxConfig.getApiKey()));

			String restxml = HttpUtils.posts(TRANSFERS_PAY, XmlUtil.xmlFormat(parm, false));
			restmap = WXNotify.parseXmlToList2(restxml);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		if (CollectionUtil.isNotEmpty(restmap) && "SUCCESS".equals(restmap.get("result_code"))) {
			logger.info("提现成功：" + restmap.get("result_code") + ":" + restmap.get("return_code"));
			Map<String, String> transferMap = new HashMap<>();
			transferMap.put("partner_trade_no", restmap.get("partner_trade_no"));//商户转账订单号
			transferMap.put("payment_no", restmap.get("payment_no")); //微信订单号
			transferMap.put("payment_time", restmap.get("payment_time")); //微信支付成功时间
			
			record.setPayNo(restmap.get("payment_no"));
			record.setPayTime(restmap.get("payment_time"));
			record.setResultCode(restmap.get("result_code"));
			record.setReturnCode(restmap.get("return_code"));
			record.setStatus(1);
			transfersManager.transfersToWXUser(record);
			
			return JSONMessage.success(null, transferMap);
		}else {
			if (CollectionUtil.isNotEmpty(restmap)) {
				String resultMsg=restmap.get("err_code") + ":" + restmap.get("err_code_des");
				logger.error("提现失败：" + resultMsg);
				record.setErrCode(restmap.get("err_code"));
				record.setErrDes(restmap.get("err_code_des"));
				record.setStatus(-1);
				transfersManager.save(record);
				return JSONMessage.failure(resultMsg);
			}
			return JSONMessage.failure("提现失败,请联系客服!");
		}
	}

	/**
	 * 企业向个人转账查询
	 * @param request
	 * @param response
	 * @param tradeno 商户转账订单号
	 * @param callback
	 */
	@RequestMapping(value = "/pay/query", method = RequestMethod.POST)
	public void orderPayQuery(HttpServletRequest request, HttpServletResponse response, String tradeno,
			String callback) {
		logger.info("[/transfer/pay/query]");
		if (StringUtil.isEmpty(tradeno)) {
			
		}

		Map<String, String> restmap = null;
		try {
			Map<String, String> parm = new HashMap<String, String>();
			parm.put("appid", wxConfig.getAppid());
			parm.put("mch_id", wxConfig.getMchid());
			parm.put("partner_trade_no", tradeno);
			parm.put("nonce_str", WXPayUtil.getNonceStr());
			parm.put("sign", PayUtil.getSign(parm, wxConfig.getApiKey()));

			String restxml = HttpUtils.posts(TRANSFERS_PAY_QUERY, XmlUtil.xmlFormat(parm, true));
			restmap = WXNotify.parseXmlToList2(restxml);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		if (CollectionUtil.isNotEmpty(restmap) && "SUCCESS".equals(restmap.get("result_code"))) {
			// 订单查询成功 处理业务逻辑
			logger.info("订单查询：订单" + restmap.get("partner_trade_no") + "支付成功");
			Map<String, String> transferMap = new HashMap<>();
			transferMap.put("partner_trade_no", restmap.get("partner_trade_no"));//商户转账订单号
			transferMap.put("openid", restmap.get("openid")); //收款微信号
			transferMap.put("payment_amount", restmap.get("payment_amount")); //转账金额
			transferMap.put("transfer_time", restmap.get("transfer_time")); //转账时间
			transferMap.put("desc", restmap.get("desc")); //转账描述
		
		}else {
			if (CollectionUtil.isNotEmpty(restmap)) {
				logger.error("订单转账失败：" + restmap.get("err_code") + ":" + restmap.get("err_code_des"));
			}
			
		}
	}

}
