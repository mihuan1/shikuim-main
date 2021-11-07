package cn.xyz.commons.ex;

import java.util.Map;

public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private Integer resultCode;
	private String errCode;
	private String language;
	private String errMessage;
	private Map<String, String> resultMap;
	
	
	public ServiceException(Integer resultCode, String message) {
		super(message);
		
		this.resultCode = resultCode;
		this.setErrCode(resultCode+"");
		this.setLanguage(language);
	}
	
	public ServiceException(Integer resultCode) {
		super();
		this.resultCode = resultCode;
		this.setErrCode(resultCode+"");
	}
	
	public ServiceException(String errCode, String errMessage , String message) {
		super(message);
		this.errMessage = errMessage;
		this.errCode = errCode;
	}

	public ServiceException(String message) {
		super(message);
	}

	public Integer getResultCode() {
		return resultCode;
	}
	public String getErrCode() {
		return errCode;
	}
	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public Map<String, String> getResultMap() {
		return resultMap;
	}
	public void setResultMap(Map<String, String> resultMap) {
		this.resultMap = resultMap;
	}
	public String getErrMessage() {
		return errMessage;
	}
	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}

}
