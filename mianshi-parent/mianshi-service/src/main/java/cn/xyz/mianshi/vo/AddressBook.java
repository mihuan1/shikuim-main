package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

/**
 * 通讯录
 * @author Administrator
 *
 */

@Entity(value="AddressBook",noClassnameStored=true)
public class AddressBook {
	
	@Id
	private ObjectId id;
	@Indexed
	private String telephone;// 自己的手机号码
	@Indexed
	private String toTelephone;// 通讯录的手机号码

	private Integer userId;

	private Integer toUserId;

	private String toUserName;

	private int registerEd = 0; // 是否已注册

	private long registerTime = 0;

	private String toRemarkName;// 通讯录的备注

	private int status = 0;// 通讯录好友状态  0：不是通讯录好友 , 1：通讯录好友, 2:在成为通讯录好友前就已经成为好友了

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getToTelephone() {
		return toTelephone;
	}

	public void setToTelephone(String toTelephone) {
		this.toTelephone = toTelephone;
	}

	public Integer getToUserId() {
		return toUserId;
	}

	public void setToUserId(Integer toUserId) {
		this.toUserId = toUserId;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getToUserName() {
		return toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	public int getRegisterEd() {
		return registerEd;
	}

	public void setRegisterEd(int registerEd) {
		this.registerEd = registerEd;
	}

	public long getRegisterTime() {
		return registerTime;
	}

	public void setRegisterTime(long registerTime) {
		this.registerTime = registerTime;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getToRemarkName() {
		return toRemarkName;
	}

	public void setToRemarkName(String toRemarkName) {
		this.toRemarkName = toRemarkName;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
