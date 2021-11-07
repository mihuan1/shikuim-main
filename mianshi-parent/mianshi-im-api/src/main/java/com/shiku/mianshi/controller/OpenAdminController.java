package com.shiku.mianshi.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;

import cn.xyz.commons.autoconfigure.KApplicationProperties.AppConfig;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.Md5Util;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.opensdk.OpenAccountManageImpl;
import cn.xyz.mianshi.opensdk.entity.SkOpenAccount;
import cn.xyz.mianshi.opensdk.entity.SkOpenApp;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Helper;
import cn.xyz.mianshi.vo.Helper.Other;
import cn.xyz.service.AuthServiceUtils;

@RestController
@RequestMapping("/open")
public class OpenAdminController extends AbstractController{
	
	@Resource(name = "appConfig")
	protected AppConfig appConfig;
	
	private OpenAccountManageImpl getOpenAccountManage(){
		return SKBeanUtils.getLocalSpringBeanManager().getOpenAccountManage();
	}
	
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public void openLogin(HttpServletRequest request, HttpServletResponse response) {
		
		try {			
		
			String path=request.getContextPath()+"/pages/open/login.html";
			response.sendRedirect(path);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Object login(@RequestParam(defaultValue = "0") String account,
			@RequestParam(defaultValue = "0") String password,HttpServletRequest request,HttpServletResponse response) {
		HashMap<String, Object> map=new HashMap<>();
		try {
			SkOpenAccount data=getOpenAccountManage().loginUserAccount(account, password,request,response);
			map.put("telephone", data.getTelephone());
			
			Map<String, Object> tokenMap =   KSessionUtil.adminLoginSaveToken(data.getUserId(), null);
			map.put("access_Token", tokenMap.get("access_Token"));
			map.put("userId", data.getUserId().toString());
			map.put("apiKey", appConfig.getApiKey());
//			map.put("status", data.getStatus().toString());
			return JSONMessage.success(map);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
		
		
	}
	
	/**
	 * 校验用户
	 * @param telephone
	 * @param password
	 * @return
	 */
	@RequestMapping(value="/ckeckOpenAccountt")
	public Object ckeckOpenAccount(@RequestParam(defaultValue="") String telephone,@RequestParam(defaultValue="") String password){
		try {
			SkOpenAccount data=SKBeanUtils.getOpenAccountManage().ckeckOpenAccount(telephone, password);
			if(null!=data){
				return JSONMessage.success(data);
			}else {
				return JSONMessage.failure("账号或密码错误");
			}
			
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 获取个人信息
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/getOpenAccount")
	public Object getOpenAccount(@RequestParam(defaultValue="") Integer userId){
		try {
			Object data=getOpenAccountManage().getOpenAccount(userId);
			return JSONMessage.success(data);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 完善个人信息
	 * @param skOpenAccount
	 * @return
	 */
	@RequestMapping(value = "/perfectUserInfo")
	public Object perfectUserInfo(@ModelAttribute SkOpenAccount skOpenAccount){
		try {
			SKBeanUtils.getOpenAccountManage().perfectUserInfo(skOpenAccount);
			return JSONMessage.success();
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	
	/**
	 * 修改用户密码
	 * @param userId
	 * @param oldPassword
	 * @param newPassword
	 * @return
	 */
	@RequestMapping(value="/updatePassword")
	public Object updatePassword(@RequestParam(defaultValue="") Integer userId,@RequestParam(defaultValue="") String oldPassword,@RequestParam(defaultValue="") String newPassword){
		try {
			SKBeanUtils.getOpenAccountManage().updatePassword(userId, oldPassword, newPassword);
			return JSONMessage.success();
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 申请成为开发者
	 * @param userId
	 * @param status
	 * @return
	 */
	@RequestMapping(value = "/applyDeveloper")
	public Object applyDeveloper(@RequestParam(defaultValue="") Integer userId,@RequestParam(defaultValue="0") int status){
		try {
			SKBeanUtils.getOpenAccountManage().applyDeveloper(userId, status);
			return JSONMessage.success();
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 创建应用
	 * @param skOpenApp
	 * @return
	 */
	@RequestMapping(value = "/createApp")
	public Object createApp(@ModelAttribute SkOpenApp skOpenApp){
		try {
			Object data=SKBeanUtils.getOpenAppManage().createApp(skOpenApp);
			return JSONMessage.success(data);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 删除移动应用
	 * @param skOpenApp
	 * @return
	 */
	@RequestMapping(value = "/delApp")
	public Object deleteApp(@ModelAttribute SkOpenApp skOpenApp){
		try {
			if(ReqUtil.getUserId().equals(Integer.valueOf(skOpenApp.getAccountId()))){
				SKBeanUtils.getOpenAppManage().deleteAppById(skOpenApp.getId(),skOpenApp.getAccountId());
				return JSONMessage.success();
			}else{
				return JSONMessage.failure(null);
			}
			
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	
	/**
	 * 应用列表
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/appList")
	public Object appList(@RequestParam(defaultValue="") String userId,@RequestParam(defaultValue="") Integer type,@RequestParam(defaultValue="0") int pageIndex,@RequestParam(defaultValue = "10") int pageSize){
		Object data=SKBeanUtils.getOpenAppManage().appList(userId,type,pageIndex, pageSize);
		return JSONMessage.success(data);
	}
	
	/**
	 * 应用详情
	 * @param id
	 * @return
	 */
	@RequestMapping(value ="/appInfo")
	public Object appInfo(@RequestParam(defaultValue="") String id){
		Object data=SKBeanUtils.getOpenAppManage().appInfo(new ObjectId(id));
		return JSONMessage.success(data);
	}
	
	/**
	 * 申请开通应用权限
	 * @param skOpenApp
	 * @return
	 */
	@RequestMapping(value = "/application")
	public Object application(@ModelAttribute SkOpenApp skOpenApp){
		try {
			SKBeanUtils.getOpenAppManage().openAccess(skOpenApp);
			return JSONMessage.success();
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	

	/** @Description:（app授权校验） 
	* @param appId
	* @param appSecret
	* @return
	**/ 
	@RequestMapping(value="/authorization")
	public Object authorization(@RequestParam(defaultValue="") String appId,@RequestParam(defaultValue="") String appSecret,
			@RequestParam(defaultValue="0") long time,@RequestParam(defaultValue="") String secret){
		try {
			if(!AuthServiceUtils.getAppAuthorization(appId, appSecret, time, secret))
				return JSONMessage.failure("授权认证失败");
			int flag = SKBeanUtils.getOpenAppManage().authorization(appId, appSecret);
			return JSONMessage.success(flag);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 登录、分享接口授权校验
	 * @param userId
	 * @param appId
	 * @param appSecret
	 * @param time
	 * @param secret
	 * @param type
	 * @return
	 */
	@RequestMapping(value = "/authInterface")
	public Object authInterface(@RequestParam(defaultValue="") String userId,@RequestParam(defaultValue="") String appId,@RequestParam(defaultValue="") String appSecret,
			@RequestParam(defaultValue="0") long time,@RequestParam(defaultValue="") String secret,@RequestParam(defaultValue="") int type){
		try {
			String token=getAccess_token();
			if(!AuthServiceUtils.getAuthInterface(appId, userId, token, time, appSecret, secret)){
				return JSONMessage.failure("授权认证失败");
			}
			int flag = SKBeanUtils.getOpenAppManage().authInterface(appId, appSecret, type);
			Map<String, String> map=new HashMap<>();
			map.put("flag", String.valueOf(flag));
			if(flag==1){
				
				map.put("userId", Md5Util.md5Hex(String.valueOf(userId)));
				return JSONMessage.success(map);
			}else{
				return JSONMessage.success(map);
			}
			
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/** 
	 ** 流程：  1.接口返回授权页面  2.授权后获取 动态code 3.根据code 拿user信息并返回openId
	* @Description:授权登录获取code
	* @param appId
	* @param state
	* @param callbackUrl
	* @return
	**/ 
	@RequestMapping(value = "/codeAuthorCheck")
	public JSONMessage codeAuthorCheck(@RequestParam(defaultValue="") String appId,@RequestParam(defaultValue="") String state,
			@RequestParam(defaultValue = "") String callbackUrl) {
		try {
			/*
			 * if(StringUtil.isEmpty(appId) || StringUtil.isEmpty(state) ||StringUtil.isEmpty(callbackUrl))
			 *  return JSONMessage.failure("参数有误，请重试");
			 */
			if ("null".equals(callbackUrl) || StringUtil.isEmpty(callbackUrl))
				return JSONMessage.failure("callbackUrl参数不能为null");
			if (StringUtil.isEmpty(appId))
				return JSONMessage.failure("appId参数不能为null");
			if (StringUtil.isEmpty(state))
				return JSONMessage.failure("state参数不能为null");
			String code = SKBeanUtils.getOpenAppManage().codeAuthorCheckImpl(appId, state);
			Map<String, String> map = Maps.newConcurrentMap();
			map.put("code", code);
			map.put("callbackUrl", callbackUrl);
			return JSONMessage.success(map);
		} catch (Exception e) {
			return JSONMessage.failure("获取code值失败");
		}
	}
	
	/** @Description: 根据code拿取用户相关信息 
	* @param code
	* @return
	**/ 
	@RequestMapping(value = "/code/oauth")
	public JSONMessage codeOauth(String code){
		try {
			if(StringUtil.isEmpty(code)){
				return JSONMessage.failure("code 参数不能为空");
			}
			// 过滤des加密后生成的特殊符号
			code = code.replace(' ','+');
			Map<String, String> codeOauthImpl = SKBeanUtils.getOpenAppManage().codeOauthImpl(code);
			return JSONMessage.success(codeOauthImpl);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	
	}
	
	
	// 申请获取权限
//	@RequestMapping(value = "/webApplication")
//	public Object webApplication(@ModelAttribute SkOpenWeb skOpenWeb){
//		try {
//			SKBeanUtils.getOpenWebAppManage().openAccess(skOpenWeb);
//			return JSONMessage.success();
//		} catch (Exception e) {
//			return JSONMessage.failure(e.getMessage());
//		}
//	}
	
	
	// 网页校验
	@RequestMapping(value="/webAppCheck")
	public JSONMessage webAppCheck(HttpServletRequest request,HttpServletResponse response,@RequestParam(defaultValue="") String appId,@RequestParam List<String> jsApiList){
		try {
//			response.addHeader("Access-Control-Allow-Origin", "*");
			Map<String, String> data=new HashMap<>();
			SkOpenApp skOpenWeb=SKBeanUtils.getOpenWebAppManage().checkWebAPPByAppId(appId);
			for(int i=0;i<jsApiList.size();i++){
				if(jsApiList.get(i).equals("chooseSKPay")){
					if(skOpenWeb.getIsAuthPay()!=1){
						return JSONMessage.failure("权限验证失败，暂无该权限");
					}
				}
			}
			if(null!=skOpenWeb){
				data.put("appName", skOpenWeb.getAppName());
				data.put("appIocn", skOpenWeb.getAppImg());
				return JSONMessage.success(null,data);
			}else {
				return JSONMessage.failure("");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	
	/**
	 * 添加群助手
	 * @param helper
	 * @param other
	 * @return
	 */
	@RequestMapping("/addHelper")
	public JSONMessage addHelper(@ModelAttribute Helper helper,@ModelAttribute Other other){
		try {
			helper.setOther(other);
			SKBeanUtils.getOpenAppManage().addHelper(helper);
			return JSONMessage.success();
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 获取所有群助手列表
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@RequestMapping("/getHelperList")
	public JSONMessage getHelperList(@RequestParam(defaultValue="") String openAppId,@RequestParam(defaultValue="0") int pageIndex,@RequestParam(defaultValue="10") int pageSize){
		try {
			Object data = SKBeanUtils.getOpenAppManage().getHelperList(openAppId,pageIndex,pageSize);
			return JSONMessage.success(null, data);
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	@RequestMapping("/updateHelper")
	public JSONMessage updateHelper(@ModelAttribute Helper helper,@ModelAttribute Other other){
		try {
			helper.setOther(other);
			SKBeanUtils.getOpenAppManage().updateHelper(helper);
			return JSONMessage.success();
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 删除群助手
	 * @param id
	 * @return
	 */
	@RequestMapping("/deleteHelper")
	public JSONMessage deleteHelper(@RequestParam(defaultValue="") String id){
		try {
			SKBeanUtils.getOpenAppManage().deleteHelper(ReqUtil.getUserId(),new ObjectId(id));
			return JSONMessage.success();
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	
	/**
	 * 发送消息例如  分享房间、分享战绩
	 * @param roomId
	 * @param userId
	 * @param title
	 * @param desc
	 * @param imgUrl
	 * @param type
	 * @param url
	 * @return
	 */
	@RequestMapping("/sendMsgByGroupHelper")
	public JSONMessage sendMessage(@RequestParam(defaultValue="") String roomId,@RequestParam(defaultValue="") Integer userId,
			@RequestParam(defaultValue="") String title,@RequestParam(defaultValue="") String desc,
			@RequestParam(defaultValue="") String imgUrl,@RequestParam(defaultValue="") Integer type,
			@RequestParam(defaultValue="") String url,@RequestParam(defaultValue="") String appId){
		try {
			JSONMessage data = SKBeanUtils.getOpenAppManage().sendMsgByGroupHelper(roomId,userId,title,desc,imgUrl,type,url,appId);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
}
