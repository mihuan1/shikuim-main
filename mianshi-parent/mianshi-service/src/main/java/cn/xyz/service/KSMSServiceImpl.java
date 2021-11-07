package cn.xyz.service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.collect.Maps;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.support.jedis.RedisCRUD;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.HttpUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.utils.SMSVerificationUtils;
import cn.xyz.mianshi.vo.Config;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class KSMSServiceImpl {

	private String app_id = "";
	private String app_secret = "";
	private String app_template_id_invite = "";
	private String app_template_id_random = "";
	public static final String SMS_ALI = "aliyun";
	public static final String SMS_TTGJ = "ttgj";
	public static final String vaildTimes = "vaildTimes";// 短信验证码有效次数
	
	public static RedisCRUD getRedisCRUD(){
		return SKBeanUtils.getRedisCRUD();
	}

	public boolean isAvailable(String telephone, String randcode) {
		// 验证码有效次数维护
		String key = String.format(KConstants.Key.RANDCODE, telephone);
		String _randcode = getRedisCRUD().get(key);
		JSONObject parseObject = JSONObject.parseObject(_randcode);
		Object effectiveCode = parseObject.get(telephone);
		int codeTimes = (int) parseObject.get(vaildTimes);
		ConcurrentMap<Object, Object> codeMap = Maps.newConcurrentMap();
		codeTimes--;
		if(codeTimes < 0){
			getRedisCRUD().del(key);
			throw new ServiceException("验证码已无效，请重新获取验证码");
		}
		codeMap.put(vaildTimes,codeTimes);
		codeMap.put(telephone, effectiveCode);
		getRedisCRUD().setObject(key, JSONObject.toJSONString(codeMap), KConstants.Expire.MINUTE*3);
		return randcode.equals(effectiveCode);
	}
	public void deleteSMSCode(String telephone) {
		String key = String.format(KConstants.Key.RANDCODE, telephone);
		getRedisCRUD().del(key);
	}
	
	public boolean checkImgCode(String telephone, String imgCode) {
		String key = String.format(KConstants.Key.IMGCODE, telephone);
		String cached = getRedisCRUD().get(key);
		return imgCode.toUpperCase().equals(cached);
	}
	
	
	public String sendSmsToInternational(String telephone,String areaCode,String language,String code) {
		Config config = SKBeanUtils.getAdminManager().getConfig();
		String key = String.format(KConstants.Key.RANDCODE, telephone);
		Long ttl = getRedisCRUD().ttl(key);
		code=getRedisCRUD().get(key);
		if (ttl >540) {
			String msg=ConstantUtil.getMsgByCode(KConstants.ResultCode.ManySedMsg+"", language).getValue();
			 msg=MessageFormat.format(msg,ttl-540);
			log.info("msg : "+msg);
			throw new ServiceException(msg);
		}
		if(null==code)
			code=StringUtil.randomCode();
		else{
			JSONObject parseObject = JSONObject.parseObject(code);
			code = parseObject.getString(telephone);
			log.info(" redis code is : {}",code);
		}
		final String smsCode=code;
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				log.info("SMSCONFIG:"+JSONObject.toJSONString(SKBeanUtils.getSmsConfig())+"  smsCode : "+smsCode);
				if (1 == SKBeanUtils.getSmsConfig().getOpenSMS()) { // 需要发送短信
					if (SMS_ALI.equals(config.getSMSType()))
						aliSMS(telephone, language, smsCode, areaCode, key);
					else if (SMS_TTGJ.equals(config.getSMSType()))
						ttgjSMS(telephone, areaCode, language, smsCode, key);
				}
			}
		});
		 return code;
	}
	
	//天天国际短信服务
	public void ttgjSMS(String telephone,String areaCode,String language,String smsCode,String key){
		String msgId = SMSVerificationUtils.sendSmsToMs360(telephone, areaCode, smsCode);
		if (!StringUtil.isEmpty(msgId)) {
			if (!"-".equals(msgId.substring(0, 1))) {
				ConcurrentMap<Object, Object> map = Maps.newConcurrentMap();
				map.put(telephone, smsCode);
				map.put("vaildTimes", 3);
				String jsonString = JSONObject.toJSONString(map);
				getRedisCRUD().setObject(key, jsonString, KConstants.Expire.MINUTE*3);
			} else {
				log.info("    发送短信错误      msgId=====>" + msgId);
				throw new ServiceException(KConstants.ResultCode.SedMsgFail, language);
			}
		} else {// msgId ==null
			throw new ServiceException(KConstants.ResultCode.SedMsgFail, language);
		}
	}
	
	// 阿里云短信服务
	public void aliSMS(String telephone, String language,String smsCode, String areaCode,String key){
		try {
			SendSmsResponse sendSms = SMSVerificationUtils.sendSms(telephone, smsCode, areaCode);
			if(null!=sendSms&&"OK".equals(sendSms.getCode())){
				ConcurrentMap<Object, Object> map = Maps.newConcurrentMap();
				map.put(telephone, smsCode);
				map.put("vaildTimes", 3);
				String jsonString = JSONObject.toJSONString(map);
				getRedisCRUD().setObject(key, jsonString, KConstants.Expire.MINUTE*3);
			}
			if(!StringUtil.isEmpty(sendSms.getCode()) && !"OK".equals(sendSms.getCode()))
				throw new ServiceException(sendSms.getCode(), sendSms.getMessage(), language);
		} catch (ClientException e) {
			e.printStackTrace();
		}
	}
 	
	public static class Result {
		private String access_token;
		private Integer expires_in;
		private String idertifier;
		private String res_code;
		private String res_message;

		public String getAccess_token() {
			return access_token;
		}

		public Integer getExpires_in() {
			return expires_in;
		}

		public String getIdertifier() {
			return idertifier;
		}

		public String getRes_code() {
			return res_code;
		}

		public String getRes_message() {
			return res_message;
		}

		public void setAccess_token(String access_token) {
			this.access_token = access_token;
		}

		public void setExpires_in(Integer expires_in) {
			this.expires_in = expires_in;
		}

		public void setIdertifier(String idertifier) {
			this.idertifier = idertifier;
		}

		public void setRes_code(String res_code) {
			this.res_code = res_code;
		}

		public void setRes_message(String res_message) {
			this.res_message = res_message;
		}
	}
	public JSONMessage applyVerify(String telephone) {
		String key = MessageFormat.format("randcode:{0}", telephone);
		Long ttl = getRedisCRUD().ttl(key);
		if (ttl > 0) {
			throw new ServiceException("请不要频繁请求短信验证码，等待" + ttl + "秒后再次请求");
		}
		JSONMessage jMessage;
		try {
			String param1 = StringUtil.randomCode();
			//String param2 = "2分钟";

			Map<String, String> params = new HashMap<String, String>();
			params.put("param1", param1);
			//params.put("param2", param2);

			Result result = sendSms(app_template_id_random, telephone, params);

			if ("0".equals(result.getRes_code())) {
				Map<String, String> data = new HashMap<String, String>(1);
				data.put("randcode", param1);
				jMessage = JSONMessage.success(null, data);
				getRedisCRUD().setObject(key, param1, 120);
			} else {
				jMessage = JSONMessage.failure(result.getRes_message());
			}
		} catch (Exception e) {
			jMessage = KConstants.Result.InternalException;
		}

		return jMessage;
	}

	public String getApp_id() {
		return app_id;
	}

	public String getApp_secret() {
		return app_secret;
	}

	public String getApp_template_id_invite() {
		return app_template_id_invite;
	}

	public String getApp_template_id_random() {
		return app_template_id_random;
	}

	public String getToken() throws Exception {
		String token = getRedisCRUD().get("open.189.access_token");
		if (StringUtil.isNullOrEmpty(token)) {
			Result result = getTokenObj();
			if ("0".equals(result.getRes_code())) {
				
				getRedisCRUD().setWithExpireTime("open.189.access_token", result.getAccess_token(),result.getExpires_in());
				token = result.getAccess_token();
			}
		}
		return token;
	}

	public Result getTokenObj() throws Exception {
		HttpUtil.Request request = new HttpUtil.Request();
		request.setSpec("https://oauth.api.189.cn/emp/oauth2/v3/access_token");
		request.setMethod(HttpUtil.RequestMethod.POST);
		request.getData().put("grant_type", "client_credentials");
		request.getData().put("app_id", app_id);
		request.getData().put("app_secret", app_secret);
		Result result = HttpUtil.asBean(request, Result.class);
		return result;
	}

	public JSONMessage sendInvite(String telephone, String companyName, String username, String password) {
		JSONMessage jMessage;

		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("param1", companyName);
			params.put("param2", username);
			params.put("param3", password);
			Result result = sendSms(app_template_id_invite, telephone, params);

			if ("0".equals(result.getRes_code())) {
				jMessage = JSONMessage.success();
			} else {
				jMessage = JSONMessage.failure(result.getRes_message());
			}
		} catch (Exception e) {
			jMessage = KConstants.Result.InternalException;
		}

		return jMessage;
	}

	private Result sendSms(String template_id, String telephone, Map<String, String> params) throws Exception {
		HttpUtil.Request request = new HttpUtil.Request();
		request.setSpec("http://api.189.cn/v2/emp/templateSms/sendSms");
		request.setMethod(HttpUtil.RequestMethod.POST);
		request.getData().put("app_id", app_id);
		request.getData().put("access_token", getToken());
		request.getData().put("acceptor_tel", telephone);
		request.getData().put("template_id", template_id);
		request.getData().put("template_param", JSON.toJSONString(params));
		request.getData().put("timestamp", DateUtil.getFullString());

		return HttpUtil.asBean(request, Result.class);
	}

}
