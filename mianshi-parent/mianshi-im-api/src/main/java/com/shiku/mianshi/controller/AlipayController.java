package com.shiku.mianshi.controller;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayFundTransOrderQueryRequest;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.response.AlipayFundTransOrderQueryResponse;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.alipay.util.AliPayParam;
import com.alipay.util.AliPayUtil;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.KConstants.ResultCode;
import cn.xyz.commons.utils.BeanUtils;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.NumberUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.impl.TransfersRecordManagerImpl;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.AliPayTransfersRecord;
import cn.xyz.mianshi.vo.ConsumeRecord;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.AuthServiceUtils;

@RestController
@RequestMapping("/alipay")
public class AlipayController extends AbstractController{
	
	@Autowired
	private TransfersRecordManagerImpl transfersManager;
	
	@RequestMapping("/callBack")
	public JSONMessage payCheck(HttpServletRequest request, HttpServletResponse response){
		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
		    String name = (String) iter.next();
		    String[] values = (String[]) requestParams.get(name);
		    String valueStr = "";
		    for (int i = 0; i < values.length; i++) {
		        valueStr = (i == values.length - 1) ? valueStr + values[i]
		                    : valueStr + values[i] + ",";
		  	}
		    //乱码解决，这段代码在出现乱码时使用。
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			params.put(name, valueStr);
		}
		try {
			String tradeNo = params.get("out_trade_no");
			String tradeStatus=params.get("trade_status");
					
			logger.info("订单号    "+tradeNo);
			
			boolean flag = AlipaySignature.rsaCheckV1(params,AliPayUtil.ALIPAY_PUBLIC_KEY, AliPayUtil.CHARSET,"RSA2");
			if(flag){
				ConsumeRecord entity = SKBeanUtils.getConsumeRecordManager().getConsumeRecordByNo(tradeNo);
				if(null==entity)
					logger.info("订单号  错误 不存在 {} ",tradeNo);
				if(entity.getStatus()!=KConstants.OrderStatus.END&&"TRADE_SUCCESS".equals(tradeStatus)){
					//把支付宝返回的订单信息存到数据库
					AliPayParam aliCallBack=new AliPayParam();
					BeanUtils.populate(aliCallBack, params);
					SKBeanUtils.getConsumeRecordManager().saveEntity(aliCallBack);
					User user=SKBeanUtils.getUserManager().get(entity.getUserId());
					user.setAliUserId(aliCallBack.getBuyer_id());
					SKBeanUtils.getUserManager().rechargeUserMoeny(entity.getUserId(), entity.getMoney(), KConstants.MOENY_ADD);
					entity.setStatus(KConstants.OrderStatus.END);
					entity.setOperationAmount(entity.getMoney());
					entity.setCurrentBalance(user.getBalance());
					SKBeanUtils.getConsumeRecordManager().update(entity.getId(), entity);
					logger.info("支付宝支付成功 {}",tradeNo);
				}else if("TRADE_CLOSED".equals(tradeStatus)) {
					logger.info("订单号  已取消  {}  ",tradeNo);
					SKBeanUtils.getConsumeRecordManager().updateAttribute(entity.getId(), "status", -1);
					return JSONMessage.success();
				}
				return JSONMessage.success();
			}else{
				logger.info("支付宝回调失败"+flag);
				return JSONMessage.failure(null);
				
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 支付宝提现
	 * @param amount
	 * @param time
	 * @param secret
	 * @param callback
	 * @return
	 */
	@RequestMapping(value = "/transfer")
	public JSONMessage transfer(@RequestParam(defaultValue="") String amount,@RequestParam(defaultValue="0") long time,
			@RequestParam(defaultValue="") String secret, String callback){
		if(StringUtil.isEmpty(amount)) {
			return JSONMessage.failure("请输入提现金额！");
		}else if(StringUtil.isEmpty(secret)) {
			return JSONMessage.failure("缺少提现密钥");
		}
		
		
		int userId = ReqUtil.getUserId();
		User user=SKBeanUtils.getUserManager().get(userId);
		String token = getAccess_token();
		if(StringUtil.isEmpty(user.getAliUserId())){
			return JSONMessage.failure("请先 支付宝授权 没有授权不能提现 ");
		}else if(!AuthServiceUtils.authWxTransferPay(user.getPayPassword(),userId+"", token, amount,user.getAliUserId(),time, secret)){
			return JSONMessage.failure("授权认证失败");
		}
		return aliWithdrawalPay(user, amount);
		
	}
	/**
	 * 支付宝提现
	 * @param amount
	 * @param time
	 * @param secret
	 * @param callback
	 * @return
	 */
	@RequestMapping(value = "/transfer/v1")
	public JSONMessage transferV1(@RequestParam(defaultValue="") String data,
	@RequestParam(defaultValue="") String codeId, String callback){
		int userId = ReqUtil.getUserId();
		User user=SKBeanUtils.getUserManager().get(userId);
		String token = getAccess_token();
		if(StringUtil.isEmpty(user.getAliUserId())){
			return JSONMessage.failure("请先 支付宝授权 没有授权不能提现 ");
		}
		String code = SKBeanUtils.getRedisService().queryTransactionSignCode(userId, codeId);
		if(StringUtil.isEmpty(code))
			return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
		JSONObject jsonObj = AuthServiceUtils.authWxWithdrawalPay(userId+"", token, data, code, user.getPayPassword());
		if(null==jsonObj) {
			return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
		}
		String amount = jsonObj.getString("amount");
		if(StringUtil.isEmpty(amount)) {
			return JSONMessage.failure("请输入提现金额！");
		}
		
		return aliWithdrawalPay(user, amount);
		
	}
	
	public JSONMessage aliWithdrawalPay(User user, String amount) {
		int userId=user.getUserId();
		// 提现金额
		double total=(Double.valueOf(amount));
		if(100<total) {
			return JSONMessage.failure("单次提现  最多 100元");
		}
		
		/**
		 * 提现手续费 0.6%
		 * 支付宝是没有手续费，但是因为充值是收取0.6%费用，在这里提现收取0.6%的费用
		 */
		DecimalFormat df = new DecimalFormat("#.00");
		double fee =Double.valueOf(df.format(total*0.006));
		if(0.01>fee) {
			fee=0.01;
		}else  {
			fee=NumberUtil.getCeil(fee, 2);
		}
		
		/**
		 * 
		 * 实际到账金额  = 提现金额-手续费
		 */
		Double totalFee= Double.valueOf(df.format(total-fee));
		
		if(totalFee>user.getBalance()) {
			return JSONMessage.failure("账号余额不足 请先充值 ");
		}
		String orderId=StringUtil.getOutTradeNo();
		AliPayTransfersRecord record=new AliPayTransfersRecord();
		record.setUserId(userId);
		record.setAppid(AliPayUtil.APP_ID);
		record.setOutTradeNo(orderId);
		record.setAliUserId(user.getAliUserId());
		record.setTotalFee(amount);
		record.setFee(fee+"");
		record.setRealFee(totalFee+"");
		record.setCreateTime(DateUtil.currentTimeSeconds());
		record.setStatus(0);
		
		AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
//		request.setBizModel(bizModel);
		
		request.setBizContent("{" +
				"    \"out_biz_no\":\""+orderId+"\"," +  // 订单Id
				"    \"payee_type\":\"ALIPAY_USERID\"," + // 收款人的账户类型
				"    \"payee_account\":\""+user.getAliUserId()+"\"," + // 收款人
				"    \"amount\":\""+totalFee+"\"," +	// 金额
				"    \"payer_show_name\":\"余额提现\"," +
				"    \"remark\":\"转账备注\"," +
				"  }");
		try {
			AlipayFundTransToaccountTransferResponse response = AliPayUtil.getAliPayClient().execute(request);
			System.out.println("支付返回结果  "+response.getCode());
			if(response.isSuccess()){
				record.setResultCode(response.getCode());
				record.setCreateTime(DateUtil.toTimestamp(response.getPayDate()));
				record.setStatus(1);
				transfersManager.transfersToAliPay(record);
				
				logger.info("支付宝提现成功");
				return JSONMessage.success();
			} else {
				record.setErrCode(response.getErrorCode());
				record.setErrDes(response.getMsg());
				record.setStatus(-1);
				transfersManager.saveEntity(record);
				logger.info("支付宝提现失败");
				return JSONMessage.failure("支付宝提现失败");
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return JSONMessage.failure("支付宝提现失败");
		}
	}
	
	/**
	 * 支付宝提现查询
	 * @param tradeno
	 * @param callback
	 * @return
	 */
	@RequestMapping(value ="/aliPayQuery")
	public JSONMessage aliPayQuery(String tradeno,String callback){
		if (StringUtil.isEmpty(tradeno)) {
			return null;
		}
		AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
		request.setBizContent("{" +
				"\"out_biz_no\":\""+tradeno+"\"," + // 订单号
				"\"order_id\":\"\"" +
				"  }");
		try {
			AlipayFundTransOrderQueryResponse response = AliPayUtil.getAliPayClient().execute(request);
			logger.info("支付返回结果  "+response.getCode());
			if(response.isSuccess()){
				logger.info("调用成功");
			} else {
				logger.info("调用失败");
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}

		return JSONMessage.success();
	}
}
