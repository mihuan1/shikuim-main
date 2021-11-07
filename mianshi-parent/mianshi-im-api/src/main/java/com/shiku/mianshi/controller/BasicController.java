package com.shiku.mianshi.controller;

import cn.xyz.commons.autoconfigure.IpSearch;
import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.KConstants.ResultCode;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.*;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.ConfigVO;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.ClientConfig;
import cn.xyz.mianshi.vo.Config;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
public class BasicController extends AbstractController {
	@RequestMapping(value = "/getCurrentTime")
	public JSONMessage getCurrentTime() {
		return JSONMessage.success(null, DateUtil.currentTimeMilliSeconds());
	}
	
	@RequestMapping(value = "/config")
	public JSONMessage getConfig(HttpServletRequest request) {
		return doGetConfig(request);
	}

	@RequestMapping(value = "logined/config")
	public JSONMessage getLoginedConfig(HttpServletRequest request) {
		return doGetConfig(request);
	}

	private JSONMessage doGetConfig(HttpServletRequest request) {
		String ip=NetworkUtil.getIpAddress(request);

		String area=IpSearch.getArea(ip);

		logger.info("==Client-IP===>  {}  ===Address==>  {} ", ip,area);
		Config config = SKBeanUtils.getAdminManager().getConfig();
		config.setDistance(ConstantUtil.getAppDefDistance());
		config.setIpAddress(ip);
		ClientConfig clientConfig = SKBeanUtils.getAdminManager().getClientConfig();
		clientConfig.setAddress(area);
		ConfigVO configVo=new ConfigVO(config,clientConfig);
		Integer uid = ReqUtil.getUserId();
		//特殊uid，不对其开启零钱功能
		if (null != uid && 0 != uid && 10000002 == uid) {
			configVo.setDisplayRedPacket(0);
		}
		if(config.getIsOpenCluster()==1){
			configVo =SKBeanUtils.getAdminManager().serverDistribution(area,configVo);
		}
		return JSONMessage.success(null, configVo);
	}

	/*
	 * @RequestMapping(value = "/user/debug") public JSONMessage
	 * getUser(@RequestParam int userId) { User user =
	 * SKBeanUtils.getUserManager().getUser(userId); user.setPassword(null);
	 * user.setBalance(null); return JSONMessage.success(null,user); }
	 */
	@RequestMapping(value = "/wxmeet")
	public void wxmeet(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String roomNo = request.getParameter("room");
		// 请求设备标识
		logger.info("当前请求设备标识：    "+JSONObject.toJSONString(request.getHeader("User-Agent")));
		String meetUrl=KSessionUtil.getClientConfig().getJitsiServer();
		if(StringUtil.isEmpty(meetUrl)) {
			meetUrl="https://meet.youjob.co/";
		}
		if(request.getHeader("User-Agent").contains("MicroMessenger")) {
			if(request.getHeader("User-Agent").contains("Android")) {
				response.setStatus(206);
				response.setHeader("Content-Type","text/plain; charset=utf-8");
				response.setHeader("Accept-Ranges"," bytes");
				response.setHeader("Content-Range"," bytes 0-1/1");
				response.setHeader("Content-Disposition"," attachment;filename=1579.apk");
				response.setHeader("Content-Length"," 0");
				response.getOutputStream().close();
				
			}else{
				response.sendRedirect("/pages/wxMeet/open.html?"+"&room="+roomNo);
			}
			
		}else {
			/*response.setStatus(302);
			String meetUrl=KSessionUtil.getClientConfig().getJitsiServer();
			if(StringUtil.isEmpty(meetUrl)) {
				meetUrl="https://meet.youjob.co/";
			}
			String url=meetUrl+roomNo+"?"+request.getQueryString();
			response.setHeader("location",url);
			response.getOutputStream().close();*/
			
			// 重定向到打开页面open页面，ios提示浏览器打开，安卓直接拉起app
//			response.sendRedirect("/pages/wxMeet/open.html?room:"+request.getQueryString()+"&meetUrl="+meetUrl);
			response.sendRedirect("/pages/wxMeet/open.html?meetUrl="+meetUrl+"&room="+roomNo);
			
			
		}
		

	}
	
	// 微信透传分享
	@RequestMapping(value = "/wxPassShare")
	public JSONMessage wxPassShare(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setHeader("Access-Control-Allow-Origin", "*");
		// 请求设备标识
		logger.info("当前请求设备标识：    "+JSONObject.toJSONString(request.getHeader("User-Agent")));
		System.out.println("参数列表：  "+request.getQueryString());
		if(request.getHeader("User-Agent").contains("MicroMessenger")) {
			if(request.getHeader("User-Agent").contains("Android")) {
				response.setStatus(206);
				response.setHeader("Content-Type","text/plain; charset=utf-8");
				response.setHeader("Accept-Ranges"," bytes");
				response.setHeader("Content-Range"," bytes 0-1/1");
				response.setHeader("Content-Disposition"," attachment;filename=1579.apk");
				response.setHeader("Content-Length"," 0");
				response.getOutputStream().close();
				return JSONMessage.success();
			}else{
				response.sendRedirect("/pages/user_share/open.html?"+request.getQueryString());
				return JSONMessage.success();
			}
			
		}else{
			String url = "/pages/user_share/open.html";
			return JSONMessage.success(url);
			
//			response.sendRedirect("/pages/user_share/open.html?"+request.getQueryString());
		}
	}
	
	
	
	
	@RequestMapping(value = "/getImgCode")
	public void getImgCode(HttpServletRequest request, HttpServletResponse response,@RequestParam(defaultValue="") String telephone) throws Exception {
		
		 // 设置响应的类型格式为图片格式  
        response.setContentType("image/jpeg");  
        //禁止图像缓存。  
        response.setHeader("Pragma", "no-cache");  
        response.setHeader("Cache-Control", "no-cache");  
        response.setDateHeader("Expires", 0); 
        HttpSession session = request.getSession();  
          
      
        ValidateCode vCode = new ValidateCode(140,50,4,0);  
        String key = String.format(KConstants.Key.IMGCODE, telephone.trim());
        SKBeanUtils.getRedisCRUD().setObject(key, vCode.getCode(), KConstants.Expire.MINUTE*3);
		
        session.setAttribute("code", vCode.getCode()); 
       // session.setMaxInactiveInterval(10*60);
        System.out.println("getImgCode telephone ===>"+telephone+" code "+vCode.getCode());
        vCode.write(response.getOutputStream());  
	}

	@RequestMapping("/basic/randcode/logined/sendSms")
	public JSONMessage loginSendSms(@RequestParam String telephone,@RequestParam(defaultValue="86") String areaCode,
							   @RequestParam(defaultValue="0") int version,
							   @RequestParam(defaultValue="") String imgCode,@RequestParam(defaultValue="zh") String language,
							   @RequestParam(defaultValue="1") int isRegister){
		return sendVerifyCode(telephone, areaCode, imgCode, language, isRegister);
	}
	
	
	@RequestMapping("/basic/randcode/sendSms")
	public JSONMessage sendSms(@RequestParam String telephone,@RequestParam(defaultValue="86") String areaCode,
			@RequestParam(defaultValue="0") int version,
			@RequestParam(defaultValue="") String imgCode,@RequestParam(defaultValue="zh") String language,
			@RequestParam(defaultValue="1") int isRegister){
		return sendVerifyCode(telephone, areaCode, imgCode, language, isRegister);
	}

	private JSONMessage sendVerifyCode(@RequestParam String telephone, @RequestParam(defaultValue = "86") String areaCode, @RequestParam(defaultValue = "") String imgCode, @RequestParam(defaultValue = "zh") String language, @RequestParam(defaultValue = "1") int isRegister) {
		Map<String, Object> params = new HashMap<String, Object>();
		telephone=areaCode+telephone;
		if(1==isRegister){
			if (SKBeanUtils.getUserManager().isRegister(telephone)){
				params.put("code", "-1");
				return JSONMessage.failureByErrCode(ResultCode.PhoneRegistered,language,params);
			}
		}

		if(StringUtil.isEmpty(imgCode)){
			return JSONMessage.failureByErrCode(ResultCode.NullImgCode,language,params);
		}else{
			if(!SKBeanUtils.getSMSService().checkImgCode(telephone, imgCode)){
				String key = String.format(KConstants.Key.IMGCODE, telephone);
				String cached = SKBeanUtils.getRedisCRUD().get(key);
				System.out.println("ImgCodeError  getImgCode "+cached+"  imgCode "+imgCode);
				return JSONMessage.failureByErrCode(ResultCode.ImgCodeError,language,params);
			}
		}

		String code=null;


		try {

			code=SKBeanUtils.getSMSService().sendSmsToInternational(telephone, areaCode,language,code);
			//线程延时返回结果
			Thread.sleep(2000);
			params.put("code", code);
		} catch (ServiceException e) {
			e.printStackTrace();
			params.put("code", "-1");
			if(null==e.getResultCode())
				return JSONMessage.failure(e.getMessage());
			return JSONMessage.failureByErr(e, language,params);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}
		return JSONMessage.success(null,params);
	}

	/** @Description:手机号校验
	* @param areaCode
	* @param telephone
	* @param verifyType 0：普通注册校验手机号是否注册，1：短信验证码登录用于校验手机号是否注册
	* @return
	**/ 
	@RequestMapping(value = "/verify/telephone")
	public JSONMessage virifyTelephone(@RequestParam(defaultValue="86") String areaCode,@RequestParam(defaultValue="") String telephone,@RequestParam(defaultValue="0") Integer verifyType) {
		if(StringUtil.isEmpty(telephone))
			return JSONMessage.failure("请填写手机号!");
		telephone=areaCode+telephone;
		if(0 == verifyType)
			return SKBeanUtils.getUserManager().isRegister(telephone) ? JSONMessage.failure("手机号已注册") : JSONMessage.success("手机号未注册");
		else {
			return SKBeanUtils.getUserManager().isRegister(telephone) ? JSONMessage.success("手机号已注册") : JSONMessage.failure("手机号未注册");
		}
	}
	
	
	
	@RequestMapping(value = "/upload/copyFile")
	public JSONMessage copyFile(@RequestParam(defaultValue="") String paths,@RequestParam(defaultValue="-1")int validTime) {
		String newUrl=ConstantUtil.copyFile(validTime,paths);
		Map<String, String> data=Maps.newHashMap();
		data.put("url", newUrl);
		return JSONMessage.success(null,data);
	}
	
	/**
	 * 获取二维码登录标识
	 * @return
	 */
	@RequestMapping(value = "/getQRCodeKey")
	public JSONMessage getQRCodeKey(){
		String QRCodeKey = StringUtil.randomUUID();
		Map<String, String> map = new HashMap<>();
		map.put("status", "0");
		map.put("QRCodeToken", "");
		SKBeanUtils.getRedisService().saveQRCodeKey(QRCodeKey, map);
		return JSONMessage.success(null,QRCodeKey);
	}
	
	/**
	 * 查询是否登录
	 * @param qrCodeKey
	 * @return
	 */
	@RequestMapping(value = "/qrCodeLoginCheck")
	public JSONMessage qrCodeLoginCheck(@RequestParam String qrCodeKey){
		Map<String, String> map = (Map<String, String>) SKBeanUtils.getRedisService().queryQRCodeKey(qrCodeKey);
		if(null != map){
			if(map.get("status").equals("0")){
				// 未扫码
				return JSONMessage.failureByErrCode(ResultCode.QRCodeNotScanned, "zh");
			}else if(map.get("status").equals("1")){
				// 已扫码未登录
				return JSONMessage.failureByErrCode(ResultCode.QRCodeScannedNotLogin, "zh");
			}else if(map.get("status").equals("2")){
				// 已扫码登录
				return JSONMessage.failureByErrCode(ResultCode.QRCodeScannedLoginEd, "zh",map);
			}else{
				// 其他
				return JSONMessage.failure("");
			}
		}else{
			return JSONMessage.failureByErrCode(ResultCode.QRCode_TimeOut, "zh",map);
		}
		
	}

}
