package com.shiku.commons.vo;

import com.alibaba.fastjson.JSONObject;

/**
 * JSON消息
 * 
 * @author luorc
 * 
 */
public class JMessage extends JSONObject {
	private static final long serialVersionUID = 1L;

	public JMessage() {
	}

	public JMessage(int resultCode, String resultMsg) {
		setResultCode(resultCode);
		setResultMsg(resultMsg);
	}

	public JMessage(int resultCode, String resultMsg, Object data) {
		setResultCode(resultCode);
		setResultMsg(resultMsg);
		setData(data);
	}

	public JMessage(boolean isSuccess, int resultCode, String resultMsg,
			Object data) {
		setSuccess(isSuccess);
		setResultCode(resultCode);
		setResultMsg(resultMsg);
		setData(data);
	}

	public boolean isSuccess() {
		return getBooleanValue("isSuccess");
	}

	public void setSuccess(boolean isSuccess) {
		put("isSuccess", isSuccess);
	}

	public int getResultCode() {
		return getInteger("resultCode");
	}

	public void setResultCode(int resultCode) {
		put("resultCode", resultCode);
	}

	public String getResultMsg() {
		return getString("resultMsg");
	}

	public void setResultMsg(String resultMsg) {
		put("resultMsg", resultMsg);
	}

	public Object getData() {
		return get("data");
	}

	public void setData(Object data) {
		put("data", data);
	}

}