package com.shiku.mianshi.controller;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import cn.xyz.commons.constants.KConstants.ResultCode;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Transfer;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.AuthServiceUtils;

/**
 * 
 * @Description: TODO(用户转账接口)
 * @author zhm
 * @date 2019年2月18日 下午3:22:43
 * @version V1.0
 */
@RestController
@RequestMapping("/skTransfer")
public class SkTransferController extends AbstractController{
	
	/**
	 * 用户转账
	 * @param transfer
	 * @param money
	 * @param time
	 * @param secret
	 * @return
	 */
	@RequestMapping(value = "/sendTransfer")
	public JSONMessage sendTransfer(Transfer transfer,@RequestParam(defaultValue="") String money,
			@RequestParam(defaultValue="0") long time,@RequestParam(defaultValue="") String secret){
		Integer userId = ReqUtil.getUserId();
		String token=getAccess_token();
		
		User user=SKBeanUtils.getUserManager().getUser(userId);
		transfer.setUserId(user.getUserId());
		transfer.setUserName(user.getUsername());
		// 转账授权校验
		if(!AuthServiceUtils.authRedPacketV1(user.getPayPassword(),userId+"", token, time,money,secret)) {
			return JSONMessage.failure("支付密码错误!");
		}
		
		JSONMessage result=SKBeanUtils.getSkTransferManager().sendTransfer(userId, money, transfer);
		return result;
	}
	
	/**
	 * 用户转账
	 * @param transfer
	 * @param money
	 * @param time
	 * @param secret
	 * @return
	 */
	@RequestMapping(value = "/sendTransfer/v1")
	public JSONMessage sendTransferV1(@RequestParam(defaultValue="") String data,
			@RequestParam(defaultValue="") String codeId){
		Integer userId = ReqUtil.getUserId();
		String token=getAccess_token();
		String code = SKBeanUtils.getRedisService().queryTransactionSignCode(userId, codeId);
		if(StringUtil.isEmpty(code))
			return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
		User user=SKBeanUtils.getUserManager().getUser(userId);
		
		// 转账授权校验
		JSONObject jsonObject = AuthServiceUtils.authSendTransfer(userId+"", token, data, code, user.getPayPassword());
		if(null==jsonObject) {
			return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
		}
		Transfer transfer=JSONObject.toJavaObject(jsonObject, Transfer.class);
		if(null==transfer) {
			return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
		}
		transfer.setUserId(user.getUserId());
		transfer.setUserName(user.getUsername());
		JSONMessage result=SKBeanUtils.getSkTransferManager().sendTransfer(userId, transfer.getMoney().toString(), transfer);
		return result;
	}
	
	/**
	 * 用户接受转账
	 * @param id
	 * @param time
	 * @param secret
	 * @return
	 */
	@RequestMapping(value = "/receiveTransfer")
	public JSONMessage receiverTransfer(@RequestParam(defaultValue="") String id,@RequestParam(defaultValue="0") long time,
			@RequestParam(defaultValue="") String secret){
		String token=getAccess_token();
		Integer userId = ReqUtil.getUserId();
		//接口授权校验
		if(!AuthServiceUtils.authRedPacket(userId+"", token, time, secret)) {
			return JSONMessage.failure("权限验证失败!");
		}
		JSONMessage result=SKBeanUtils.getSkTransferManager().receiveTransfer(userId, new ObjectId(id));
		return result;
	}
	
	/**
	 * 获取转账信息
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/getTransferInfo")
	public JSONMessage getTransferInfo(@RequestParam(defaultValue="") String id){
		JSONMessage result = SKBeanUtils.getSkTransferManager().getTransferById(ReqUtil.getUserId(), new ObjectId(id));
		return result;
	}
	
	/**
	 * 获取用户转账列表
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/getTransferList")
	public JSONMessage getTransferList(@RequestParam(defaultValue="0")int pageIndex,@RequestParam(defaultValue="10")int pageSize){
		Object data=SKBeanUtils.getSkTransferManager().getTransferList(ReqUtil.getUserId(), pageIndex, pageSize);
		return JSONMessage.success(null, data);
	}
	
	/**
	 * 获取用户接受转账列表
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "getReceiveList")
	public JSONMessage getReceiveList(@RequestParam(defaultValue="0")int pageIndex,@RequestParam(defaultValue="10")int pageSize){
		Object data=SKBeanUtils.getSkTransferManager().getTransferReceiveList(ReqUtil.getUserId(), pageIndex, pageSize);
		return JSONMessage.success(null, data);
	}
	
}
