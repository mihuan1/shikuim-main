package com.shiku.mianshi.controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wxpay.utils.WXNotify;
import com.wxpay.utils.WXPayUtil;
import com.wxpay.utils.WxPayResult;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.model.PageVO;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.ConsumeRecord;

@RestController
public class ConsumeRecordController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping("/user/recharge/list")
	public JSONMessage getList(@RequestParam(defaultValue="0")int pageIndex,@RequestParam(defaultValue="10")int pageSize) {
		Object data = SKBeanUtils.getConsumeRecordManager().reChargeList(ReqUtil.getUserId(), pageIndex, pageSize);
		return JSONMessage.success(null, data);
	}
	
	@RequestMapping("/user/consumeRecord/list")
	public JSONMessage consumeRecordList(@RequestParam(defaultValue="0")int pageIndex,@RequestParam(defaultValue="10")int pageSize) {
		try {
			PageResult<ConsumeRecord> result = SKBeanUtils.getConsumeRecordManager().consumeRecordList(ReqUtil.getUserId(), pageIndex, pageSize,(byte)0);
			PageVO data = new PageVO(result.getData(),result.getCount(),pageIndex,pageSize);
			return JSONMessage.success(null, data);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	
	@RequestMapping("/friend/consumeRecordList")
	public JSONMessage friendRecordList(@RequestParam(defaultValue="0")int toUserId,@RequestParam(defaultValue="0")int pageIndex,@RequestParam(defaultValue="10")int pageSize) {
		try {
			PageResult<ConsumeRecord> result = SKBeanUtils.getConsumeRecordManager()
					.friendRecordList(ReqUtil.getUserId(),toUserId, pageIndex, pageSize,(byte)0);
			if(0==result.getCount())
				return JSONMessage.success(null, null);
			PageVO data = new PageVO(result.getData(),result.getCount(),pageIndex,pageSize);
			return JSONMessage.success(null, data);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	
	
	@RequestMapping("/recharge/delete")
	public JSONMessage delete(String id) {
		Object data = SKBeanUtils.getConsumeRecordManager().getConsumeReCord(ReqUtil.getUserId(),new ObjectId(id));
		if(null != data){
			SKBeanUtils.getConsumeRecordManager().deleteById(ReqUtil.parseId(id));
			return JSONMessage.success();
		}else{
			return JSONMessage.failure(null);
		}
	}
	
	@RequestMapping(value="/user/recharge/wxPayCallBack",method=RequestMethod.POST)
	public void wxPayCallBack(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		//把如下代码贴到的你的处理回调的servlet 或者.do 中即可明白回调操作
		logger.info("微信支付回调数据开始");
		BufferedOutputStream out = null;
		String inputLine;
		String notityXml = "";
		String resXml = "";
		try {
			while ((inputLine = request.getReader().readLine()) != null) {
				notityXml += inputLine;
			}
			request.getReader().close();
			
			Map<String,String> m = WXNotify.parseXmlToList2(notityXml);
			logger.info("接收到的报文：" + m);
				String tradeNo=m.get("out_trade_no");
				ConsumeRecord entity=SKBeanUtils.getConsumeRecordManager().getConsumeRecordByNo(tradeNo);
				if(null==entity)
					logger.info("交易订单号不存在！-----"+tradeNo);
				else if(0!=entity.getStatus())
					logger.info(tradeNo+"===status==="+entity.getStatus()+"=======交易已处理或已取消!");
				else if("SUCCESS".equals(m.get("result_code"))){
					boolean flag=Double.valueOf(m.get("cash_fee"))==entity.getMoney()*100;
					if(flag){
						 //logger.info("支付金额比较"+m.get("cash_fee")+"=="+entity.getMoney()*100+"=======>"+flag);
						WxPayResult wpr = WXPayUtil.mapToWxPayResult(m);
						//支付成功
						resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
						+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
						entity.setStatus(KConstants.OrderStatus.END);
						Double balance = SKBeanUtils.getUserManager().rechargeUserMoeny(entity.getUserId(), entity.getMoney(), KConstants.MOENY_ADD);
						entity.setOperationAmount(entity.getMoney());
						entity.setCurrentBalance(balance);
						SKBeanUtils.getConsumeRecordManager().update(entity.getId(), entity);
						SKBeanUtils.getConsumeRecordManager().saveEntity(wpr);
						logger.info(tradeNo+"========>>微信支付成功!");
					}else{
						logger.info("微信数据返回错误!");
						logger.info("localhost:Money---------"+entity.getMoney()*100);
						logger.info("Wxpay:Cash_fee---------"+m.get("cash_fee"));
					}
				}else{
					logger.info("微信支付失败======"+m.get("return_msg"));
					resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
					+ "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
				}
				out = new BufferedOutputStream(response.getOutputStream());
				out.write(resXml.getBytes());
				out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (out != null)
				out.close();
		}
		
	}

}
