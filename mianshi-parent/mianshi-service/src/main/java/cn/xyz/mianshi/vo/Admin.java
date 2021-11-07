package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 *  管理员实体类
 * @author hsg
 * @Date 2018-10-08
 */
@Entity(value = "admin", noClassnameStored = true)
public class Admin {
	
	@Id
	private ObjectId id;
	
	private  String account; //账号
	
	//private  String name;  //管理员姓名
	
	private  String password; //密码
	
	private byte   role = 0; //角色值  0: 普通管理员   1:超级管理员  
	
	private byte	state = 1; //状态值 0:禁用   1:正常

	private long 	createTime;  //创建时间
	
	private long  lastLoginTime; //最后登录时间 
	
	
	public Admin() {
		super();
	}

	public Admin(String account, String password, byte role, byte state,long createTime) {
		super();
		this.account = account;
		this.password = password;
		this.role = role;
		this.state = state;
		this.createTime = createTime;
		this.lastLoginTime = 0;
	}
	
	public Admin(ObjectId id, String password, byte role, byte state, long lastLoginTime) {
		super();
		this.id = id;
		this.password = password;
		this.role = role;
		this.state = state;
		this.lastLoginTime = lastLoginTime;
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

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public byte getRole() {
		return role;
	}

	public void setRole(byte role) {
		this.role = role;
	}

	public byte getState() {
		return state;
	}

	public void setState(byte state) {
		this.state = state;
	}
	
	
}
