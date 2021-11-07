package cn.xyz.mianshi.model;

import com.alibaba.fastjson.JSON;

public class KSession {
	private String telephone;
	private Integer userId;
	private int deviceId;
	private String channelId;
	private String language="zh";

	public KSession() {
		super();
	}

	public KSession(String telephone, Integer userId,String language) {
		super();
		this.telephone = telephone;
		this.userId = userId;
		this.language=language;
	}
	
	public KSession(String telephone, Integer userId) {
		super();
		this.telephone = telephone;
		this.userId = userId;
	}
	
	public KSession(String telephone, Integer userId,int deviceId, String channelId,String language) {
		super();
		this.telephone = telephone;
		this.userId = userId;
		this.channelId=channelId;
		this.language=language;
	}

	public String getTelephone() {
		return telephone;
	}

	public Integer getUserId() {
		return userId;
	}


	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}


	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

}
