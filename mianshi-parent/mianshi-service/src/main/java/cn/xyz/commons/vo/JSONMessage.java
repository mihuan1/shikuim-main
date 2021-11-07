package cn.xyz.commons.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.KConstants.ResultCode;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.vo.Constant;



public class JSONMessage extends JSONObject {
	private static final long serialVersionUID = 1L;
	public static final Object EMPTY_OBJECT = new Object();

	public static JSONMessage success(String resultMsg) {
		return new JSONMessage(ResultCode.Success, resultMsg);
	}

	public static JSONMessage success() {
		return success(null, null);
	}
	public static JSONMessage success(Object data) {
		return new JSONMessage(ResultCode.Success,null, data);
	}
	public static JSONMessage success(String resultMsg, Object data) {
		return new JSONMessage(ResultCode.Success, resultMsg, data);
	}
	
	public static JSONMessage failure(String resultMsg) {
		return new JSONMessage(ResultCode.Failure, resultMsg);
	}
	
	public static JSONMessage success(PageResult result) {
		JSONMessage success = success(null,result.getData());
		success.put("count", result.getCount());
		success.put("total", result.getTotal());
		success.put("totalVo", result.getTotalVo());
		return success;
	}
	
	public static JSONMessage failureAndData(String resultMsg,Object data) {
		return new JSONMessage(ResultCode.Failure, resultMsg,data);
	}
	public static JSONMessage error(Exception e) {
		return new JSONMessage(1020101, "接口内部异常", e.getMessage());
	}
	public static JSONMessage failureByErrCode(Integer errCode,String language,Object data) {
		Constant constant=ConstantUtil.getMsgByCode(errCode+"", language);
		if(null!=constant){
			
			return new JSONMessage(errCode, constant.getValue(),data);
		}
		return new JSONMessage(KConstants.ResultCode.InternalException,"");
	}
	public static JSONMessage failureByErrCode(Integer errCode) {
		return failureByErrCode(errCode, ConstantUtil.defLanguage);
	}
	public static JSONMessage failureByErrCode(Integer errCode,String language) {
		Constant constant=ConstantUtil.getMsgByCode(String.valueOf(errCode), language);
		if(null!=constant){
			System.out.println("===     "+constant.getValue()+"     =======>");
			return new JSONMessage(errCode, constant.getValue());
		}
		return new JSONMessage(KConstants.ResultCode.InternalException,"");
	}
	//2017 年3月2日修改  错误码国际化
	public static JSONMessage failureByErr(ServiceException e,String language,Object data) {
		if(0!=e.getResultCode()){
			//请不要频繁请求短信验证码，等待{0}秒后再次请求
			if(KConstants.ResultCode.ManySedMsg==e.getResultCode())
				return new JSONMessage(KConstants.ResultCode.ManySedMsg, e.getMessage(),data);
			if(!StringUtil.isEmpty(e.getLanguage()))
					language=e.getLanguage();
			Constant constant=ConstantUtil.getMsgByCode(e.getResultCode()+"", language);
			if(null!=constant){
				System.out.println("===     "+constant.getName()+"     =======>");
				return new JSONMessage(constant.getCode(), constant.getValue(),data);
			}
		}
		else if(null!=e.getResultMap())
			return new JSONMessage(e.getErrCode(),e.getResultMap().get(language));
		return new JSONMessage(KConstants.ResultCode.InternalException,"");
	}
	//2017 年3月2日修改  错误码国际化
	public static JSONMessage failureByErr(ServiceException e,String language) {
			if(0!=e.getResultCode()){
				//请不要频繁请求短信验证码，等待{0}秒后再次请求
				if(KConstants.ResultCode.ManySedMsg==e.getResultCode())
					return new JSONMessage(KConstants.ResultCode.ManySedMsg, e.getMessage());
				if(!StringUtil.isEmpty(e.getLanguage()))
						language=e.getLanguage();
				Constant constant=ConstantUtil.getMsgByCode(e.getResultCode()+"", language);
				if(null!=constant){
					System.out.println("===     "+constant.getName()+"     =======>");
					return new JSONMessage(constant.getCode(), constant.getValue());
				}
			}
			else if(null!=e.getResultMap())
				return new JSONMessage(e.getErrCode(),e.getResultMap().get(language));
			return new JSONMessage(KConstants.ResultCode.InternalException,"");
	}
	
	
	public JSONMessage() {
	}
	public JSONMessage(String errCode, String resultMsg,Object data) {
		setResultCode(errCode);
		setErrCode(errCode);
		setResultMsg(resultMsg);
		setDetailMsg(resultMsg);
		setData(data);
		setCurrentTime(DateUtil.currentTimeMilliSeconds()+"");
	}
	
	public JSONMessage(int resultCode, String resultMsg) {
		setResultCode(resultCode);
		setResultMsg(resultMsg);
		setCurrentTime(DateUtil.currentTimeMilliSeconds());
	}

	public JSONMessage(int resultCode, String resultMsg, String detailMsg) {
		setResultCode(resultCode);
		setResultMsg(resultMsg);
		setDetailMsg(detailMsg);
		setCurrentTime(DateUtil.currentTimeMilliSeconds());
	}

	public JSONMessage(int resultCode, String resultMsg, Object data) {
		setResultCode(resultCode);
		setResultMsg(resultMsg);
		setData(data);
		setCurrentTime(DateUtil.currentTimeMilliSeconds());
	}

	public JSONMessage(String groupCode, String serviceCode, String nodeCode,
			String resultMsg) {
		setResultCode(new StringBuffer().append(groupCode).append(serviceCode)
				.append(nodeCode).toString());
		setResultMsg(resultMsg);
	}
	public JSONMessage(String errCode, String resultMsg) {
		setResultCode(errCode);
		setErrCode(errCode);
		setResultMsg(resultMsg);
		setDetailMsg(resultMsg);
		setData(new Object());
		setCurrentTime(DateUtil.currentTimeMilliSeconds()+"");
	}
	
	public Object getCurrentTime() {
		return get("currentTime");
	}

	public void setCurrentTime(Object currentTime) {
		put("currentTime", currentTime);
	}
	public Object getErrCode() {
		return get("errCode");
	}

	public void setErrCode(Object errCode) {
		put("errCode", errCode);
	}
	public Object getResultCode() {
		return get("resultCode");
	}

	public void setResultCode(Object resultCode) {
		put("resultCode", resultCode);
	}

	public String getResultMsg() {
		return getString("resultMsg");
	}

	public void setResultMsg(String resultMsg) {
		put("resultMsg", resultMsg);
	}

	public String getDetailMsg() {
		return getString("detailMsg");
	}

	public void setDetailMsg(String detailMsg) {
		put("detailMsg", detailMsg);
	}

	public Object getData() {
		return get("data");
	}

	public void setData(Object data) {
		put("data", data);
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}


}