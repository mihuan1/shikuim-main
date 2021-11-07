package com.shiku.mianshi.controller;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.wxpay.utils.GetWxOrderno;
import com.wxpay.utils.MD5Util;
import com.wxpay.utils.WXPayUtil;

import cn.xyz.commons.constants.KConstants.ResultCode;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.impl.PayServiceImpl;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.RedPacket;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.AuthServiceUtils;
/**
 * 
 * @Description: TODO(支付收款相关)
 * @author zhm
 * @date 2019年2月16日 下午6:08:53
 * @version V1.0
 */

@RestController
@RequestMapping("/pay")
public class PayController extends AbstractController{
	
	@Autowired
	private PayServiceImpl payService;
	
	/**
	 * 条码、付款码支付(付款)
	 * @param paymentCode
	 * @param money
	 * @param secret
	 * @return
	 */
	@RequestMapping(value = "/codePayment")
	public JSONMessage codePayment(@RequestParam(defaultValue="") String paymentCode,@RequestParam(defaultValue="") String money,
			@RequestParam(defaultValue="0") long time,@RequestParam(defaultValue="") String desc,@RequestParam(defaultValue="") String secret){
		// 解析付款码
		Integer fromUserId=payService.analysisCode(paymentCode);
		if(fromUserId==null){
			return JSONMessage.failure("付款码错误");
		}
		// 校验付款码唯一性
		if(payService.checkPaymentCode(fromUserId, paymentCode)){
			return JSONMessage.failure("付款码已失效");
		}
		Integer userId=ReqUtil.getUserId();
		
		if(userId.equals(fromUserId)){
			return JSONMessage.failure("不支持向自己付款");
		}
		// 校验加密规则
		if(!AuthServiceUtils.authPaymentCode(paymentCode, userId.toString(), money, getAccess_token(), time, secret)){
			return JSONMessage.failure("付款码支付失败，授权验证失败");
		}
		try {
				// 用户金额操作
				payService.paymentCodePay(paymentCode,userId, fromUserId, money,desc);
				return JSONMessage.success();
			} catch (ServiceException e) {
				return JSONMessage.failure(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				return JSONMessage.error(e);
			}
			
		
	}
	
	/**
	 * 二维码收款设置金额
	 * @param money
	 * @return
	 */
	@RequestMapping(value = "/setMoney")
	public JSONMessage setMoney(@RequestParam(defaultValue="") String money){
		
		return null;
		
	}
	
	/**
	 * 二维码收款
	 * @param toUserId 收款人（金钱增加）
	 * @param money
	 * @param secret
	 * @return
	 */
	@RequestMapping(value = "/codeReceipt")
	public JSONMessage codeTransfer(@RequestParam(defaultValue="") Integer toUserId,@RequestParam(defaultValue="") String money,@RequestParam(defaultValue="0") long time,
			@RequestParam(defaultValue="") String desc,@RequestParam(defaultValue="") String secret){
		Integer userId=ReqUtil.getUserId();
		if(userId.equals(toUserId)){
			return JSONMessage.failure("不支持向自己付款");
		}
		String token=getAccess_token();
		User user=SKBeanUtils.getUserManager().getUser(userId);
		// 校验加密规则
		if(!AuthServiceUtils.authQRCodeReceipt(userId.toString(), token, money, time,user.getPayPassword(),secret)){
			return JSONMessage.failure("支付密码错误");
		}
		
		try {
			payService.receipt(userId, toUserId, money,desc);
			return JSONMessage.success();
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 二维码收款
	 * @param toUserId 收款人（金钱增加）
	 * @param money
	 * @param secret
	 * @return
	 */
	@RequestMapping(value = "/codeReceipt/v1")
	public JSONMessage codeTransferV1(@RequestParam(defaultValue="") String codeId,@RequestParam(defaultValue="") String data){
		Integer userId=ReqUtil.getUserId();
		
		String token=getAccess_token();
		User user=SKBeanUtils.getUserManager().getUser(userId);
		String code = SKBeanUtils.getRedisService().queryTransactionSignCode(userId, codeId);
		if(StringUtil.isEmpty(code))
			return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
		// 校验加密规则
		JSONObject jsonObject = AuthServiceUtils.authQrCodeTransfer(userId+"", token, data, code, user.getPayPassword());
		if(null==jsonObject) 
			return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
		Integer toUserId = jsonObject.getInteger("toUserId");
		if(userId.equals(toUserId)){
			return JSONMessage.failure("不支持向自己付款");
		}
		
		try {
			payService.receipt(userId,jsonObject.getInteger("toUserId")
					,jsonObject.getString("money"),jsonObject.getString("desc"));
			return JSONMessage.success();
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 统一下单接口
	 * @param xmlParam 中包含的参数
	 * appId
	 * body 商品描述
	 * input_charset 编码格式
	 * nonce_str 随机生成的数
	 * notify_url 回调的url
	 * sign 签名
	 * spbill_create_ip 请求的Ip
	 * total_fee 总费用
	 * trade_no 交易订单
	 * trade_type 交易类型：APP,WEB
	 * @return prepayId
	 */
	@RequestMapping(value = "/unifiedOrder")
	public JSONMessage unifiedOrder(HttpServletRequest request,HttpServletResponse response){
		try {
			 Map<String, String> map = null;
			 java.util.Enumeration<String>  enums=request.getParameterNames();
			 while(enums.hasMoreElements()){    
                 String  paramName=(String)enums.nextElement();                        
                 map=GetWxOrderno.doXMLParse(paramName); 
			 }  
			JSONMessage data = payService.unifiedOrderImpl(map);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 获取预支付订单信息
	 * @param appId
	 * @param prepayId
	 * @return
	 */
	@RequestMapping(value = "/getOrderInfo")
	public JSONMessage getOrderInfo(@RequestParam(defaultValue="") String appId,@RequestParam(defaultValue="") String prepayId){
		JSONMessage data = payService.getOrderInfo(appId, prepayId);
		return data;
	}
	
	/**
	 * 确认密码支付
	 * @param appId
	 * @param prepayId
	 * @param sign
	 * @return
	 */
	@RequestMapping(value = "/passwordPayment")
	public JSONMessage passwordPayment(@RequestParam(defaultValue="") String appId,@RequestParam() String prepayId,
			@RequestParam(defaultValue="") String sign,@RequestParam(defaultValue="0") long time,@RequestParam(defaultValue="") String secret){
		Integer userId=ReqUtil.getUserId();
		String token=getAccess_token();
		JSONMessage data = payService.passwordPayment(appId,prepayId,sign,userId,token,time,secret);
		return data;
	}
	/**
	 * 确认密码支付
	 * @param appId
	 * @param prepayId
	 * @param sign
	 * @return
	 */
	@RequestMapping(value = "/passwordPayment/v1")
	public JSONMessage passwordPaymentV1(@RequestParam(defaultValue="") String codeId,@RequestParam(defaultValue="") String data){
		Integer userId=ReqUtil.getUserId();
		String token=getAccess_token();
		User user=SKBeanUtils.getUserManager().getUser(userId);
		String code = SKBeanUtils.getRedisService().queryTransactionSignCode(userId, codeId);
		if(StringUtil.isEmpty(code))
			return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
		// 校验加密规则
		JSONObject jsonObj = AuthServiceUtils.authOrderPay(userId+"", token, data, code, user.getPayPassword());
		if(null==jsonObj)
			return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
		JSONMessage result = payService.passwordPaymentV1(jsonObj.getString("appId"),jsonObj.getString("prepayId"),
				jsonObj.getString("sign"),userId);
		return result;
	}
	
	/**
	 * 测试程序
	 * @param money
	 * @return
	 */
	@RequestMapping(value= "/SKPayTest")
	public JSONMessage skPayTest(@RequestParam(defaultValue="") String money){
		String totalFee= money;
		// 随机字符串
		String nonce_str = getNonceStr();
		
		SortedMap<String, String> contentMap = new TreeMap<String, String>();
		
		contentMap.put("appId", "sk96d738a743d048ad");
		contentMap.put("body", "测试APP");
		contentMap.put("input_charset", "UTF-8");
		contentMap.put("nonce_str", nonce_str);
		contentMap.put("notify_url", "http://192.168.0.168:8092/user/recharge/wxPayCallBack");
		
		contentMap.put("spbill_create_ip", "121.121.121.121");
		// 这里写的金额为1 分到时修改
		contentMap.put("total_fee", totalFee);
		contentMap.put("out_trade_no", StringUtil.getOutTradeNo());
		contentMap.put("trade_type", "WEB");
		String sign = createSign(contentMap);
		contentMap.put("sign", sign);
		
		String xml = WXPayUtil.paramsToxmlStr(contentMap);
		String prepay_id = new GetWxOrderno().getPayNo("http://192.168.0.168:8092/pay/unifiedOrder", xml);
		System.out.println("返回数据 "+prepay_id);
		Map<String, String> map = new HashMap<>();
		map.put("prepay_id", prepay_id);
		map.put("sign", sign);
		return JSONMessage.success(map);
	}
	/**
	 * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 */
	public static String createSign(SortedMap<String, String> packageParams) {
		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
				//System.out.println(k+"----"+v);
			}
		}
//		sb.append("key=" + this.getKey());
		//System.out.println("key====="+this.getKey());
		String sign = MD5Util.MD5Encode(sb.toString(), "UTF-8")
				.toUpperCase();
		return sign;

	}
	
	
	
	/**
	 * 获取随机字符串
	 * @return
	 */
	public static String getNonceStr() {
		// 随机数
		String currTime = getCurrTime();
		// 8位日期
		String strTime = currTime.substring(5, currTime.length());
		// 四位随机数
		String strRandom = buildRandom(4) + "";
		// 10位序列号,可以自行调整。
		return strTime + strRandom;
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
}
