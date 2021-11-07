package com.shiku.mianshi.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shiku.utils.Base64;
import com.shiku.utils.encrypt.RSA;

import cn.xyz.commons.constants.KConstants.ResultCode;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.impl.AuthKeysServiceImpl;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.AuthServiceUtils;

/**
 * @author lidaye
 * 用户授权数据相关接口 
 *
 */
@RestController
public class AuthKeysController extends AbstractController{

	@Autowired
	private AuthKeysServiceImpl authKeysService;
	
	
	@RequestMapping(value = "/authkeys/getPayPrivateKey")
	public JSONMessage getPayPrivateKey() {
		try {
			Integer userId = ReqUtil.getUserId();
			String privateKey = authKeysService.getPayPrivateKey(userId);
			if(StringUtil.isEmpty(privateKey))
				return JSONMessage.success();
			JSONObject jsonObject=new JSONObject();
			jsonObject.put("privateKey",privateKey);
			return JSONMessage.success(jsonObject);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		} catch(Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/authkeys/uploadPayKey")
	public JSONMessage uploadPayKey(@RequestParam(defaultValue ="") String publicKey,@RequestParam(defaultValue ="")String privateKey
			,@RequestParam(defaultValue ="")String mac) {
		try {
			if(StringUtil.isEmpty(privateKey)||StringUtil.isEmpty(publicKey)||StringUtil.isEmpty(mac)) {
				return JSONMessage.failureByErrCode(ResultCode.ParamsAuthFail);
			}
			Integer userId = ReqUtil.getUserId();
			String payPwd = SKBeanUtils.getUserManager().getUser(userId).getPayPassword();
			if(StringUtil.isEmpty(payPwd))
				return JSONMessage.failureByErrCode(ResultCode.PayPasswordNotExist);
			if(!AuthServiceUtils.checkUserUploadKeySign(privateKey, publicKey, mac,payPwd))
				return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
			authKeysService.uploadPayKey(userId, publicKey, privateKey);
			return JSONMessage.success();
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		} catch(Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	@RequestMapping(value = "/authkeys/uploadMsgKey")
	public JSONMessage uploadMsgKey(@RequestParam(defaultValue ="") String publicKey,@RequestParam(defaultValue ="")String privateKey,
			@RequestParam(defaultValue ="")String mac) {
		try {
			if(StringUtil.isEmpty(privateKey)) {
				return JSONMessage.failureByErrCode(ResultCode.ParamsAuthFail);
			}
			Integer userId = ReqUtil.getUserId();
			
			if(!AuthServiceUtils.checkUserUploadKeySign(privateKey, publicKey, mac,null))
				return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
			authKeysService.uploadMsgKey(userId, publicKey, privateKey);
			return JSONMessage.success();
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		} catch(Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	
	/** @Description:手机号校验
	*获取交易 随机码
	* @return
	**/ 
	@RequestMapping(value = "/transaction/getCode")
	public JSONMessage transactionGetCode(@RequestParam(defaultValue ="")String mac,
			@RequestParam(defaultValue ="")String salt) {
		int userId=ReqUtil.getUserId();
		String publicKey=authKeysService.getPayPublicKey(userId);
		 if(StringUtil.isEmpty(publicKey)) {
			return JSONMessage.success();
		}
		 String payPassword = SKBeanUtils.getUserManager().getUser(userId).getPayPassword();
		if(!AuthServiceUtils.authTransactiongetCode(userId+"", getAccess_token(), salt, mac, payPassword))
			return JSONMessage.failureByErrCode(ResultCode.PayPasswordIsWrong);
		
		try {
			byte[] codeArr=new byte[16];
			Random rom =new Random();
			rom.nextBytes(codeArr);
			byte[] key = RSA.encrypt(codeArr,Base64.decode(publicKey));
			
			String code=Base64.encode(codeArr);
			String codeId=StringUtil.randomUUID();
			logger.info("server code ====》 {}",code);
			SKBeanUtils.getRedisService().saveTransactionSignCode(userId, codeId,code);
			Map<String,String> map=new HashMap<String,String>();
			
			map.put("code",Base64.encode(key));
			logger.info("codeArr ====》 {}",Base64.encode(codeArr));
			logger.info("publicKey ====》 {}",publicKey);
			logger.info("code ====》 {}",Base64.encode(key));
			map.put("codeId",codeId);
			logger.info("data  ---> {}",map);
			return JSONMessage.success(map);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
}
