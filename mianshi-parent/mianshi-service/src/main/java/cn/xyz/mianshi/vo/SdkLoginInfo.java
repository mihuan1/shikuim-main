package cn.xyz.mianshi.vo;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(value = "sdkLoginInfo", noClassnameStored = true)
public class SdkLoginInfo {
	@Id
	private String id;
	
	private Integer userId;
	
	private int type;// 第三方登录类型   1：QQ 2:微信  
	
	private String loginInfo;// 登录标识  例 微信的openId
	
	private long createTime;// 绑定时间
	
}
