package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

@Entity(value = "customer", noClassnameStored = true)
@Indexes({ @Index("customerId,macAddress") })
public class Customer {

	@Id
	private ObjectId id;
	
	private Integer customerId; //访客Id 相当于用户userId
	
	private String userKey; //userKey 由ip地址MD5加密得到
	
	
	private String macAddress; //mac地址
	
	private String ip;// IP地址
	
	private String companyId; //公司id
	
	private Long createTime;// 注册时间
	

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public Integer getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Integer customerId) {
		this.customerId = customerId;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	

	
}
