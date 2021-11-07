package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.NotSaved;

import cn.xyz.commons.utils.DateUtil;

@Entity(value = "userRoles", noClassnameStored = true)
public class Role {

	@Id
	private ObjectId id;

	private Integer userId; 
	
	private String phone;// 账号

	private byte role = 0; // 1=游客（用于后台浏览数据）；2=公众号 ；3=机器账号，由系统自动生成；4=客服账号;5=管理员；6=超级管理员；7=财务；

	private byte status = 1; // 状态值 -1:禁用, 1:正常

	private long createTime; // 创建时间

	private long lastLoginTime; // 最后登录时间
	
	private String promotionUrl;// 客服推广链接
	
	@NotSaved
	private String nickName;// 昵称
	
	public Role(Integer userId,String phone, byte userType, byte status, long lastLoginTime) {
		this.userId = userId;
		this.phone = phone;
		this.role = userType;
		this.status = status;
		this.createTime = DateUtil.currentTimeSeconds();
		this.lastLoginTime = lastLoginTime;
	}
	
	public Role(Integer userId,String phone, byte userType, byte status, long lastLoginTime,String promotionUrl) {
		this.userId = userId;
		this.phone = phone;
		this.role = userType;
		this.status = status;
		this.createTime = DateUtil.currentTimeSeconds();
		this.lastLoginTime = lastLoginTime;
		this.promotionUrl = promotionUrl;
	}
	
	public Role(Integer userId) {
		this.userId = userId;
		this.lastLoginTime = DateUtil.currentTimeSeconds();
	}

	public Role() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	/*
	 * public String getNickname() { return nickname; }
	 * 
	 * public void setNickname(String nickname) { this.nickname = nickname; }
	 * 
	 * public String getPassword() { return password; }
	 * 
	 * public void setPassword(String password) { this.password = password; }
	 */

	public Integer getUserId() {
		return userId;
	}

	public byte getRole() {
		return role;
	}

	public void setRole(byte role) {
		this.role = role;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPromotionUrl() {
		return promotionUrl;
	}

	public void setPromotionUrl(String promotionUrl) {
		this.promotionUrl = promotionUrl;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

}
