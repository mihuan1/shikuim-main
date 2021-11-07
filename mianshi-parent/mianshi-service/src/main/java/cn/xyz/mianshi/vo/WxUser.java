package cn.xyz.mianshi.vo;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value = "wxuser", noClassnameStored = true)
public class WxUser {
	private @Id Integer wxuserId;
	private String openId;
	private String nickname;
	private int sex;
	private String imgurl;
	private String city;
	private String province;
	private String country;
	private Long createtime;
	
	public WxUser() {}

	public WxUser(Integer wxuserId, String openId, String nickname, int sex, String imgurl, String city,
			String province, String country, Long createtime) {
		this.wxuserId = wxuserId;
		this.openId = openId;
		this.nickname = nickname;
		this.sex = sex;
		this.imgurl = imgurl;
		this.city = city;
		this.province = province;
		this.country = country;
		this.createtime = createtime;
	}

	public Integer getWxuserId() {
		return wxuserId;
	}
	public void setWxuserId(Integer wxuserId) {
		this.wxuserId = wxuserId;
	}
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public String getImgurl() {
		return imgurl;
	}
	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}

	public Long getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Long createtime) {
		this.createtime = createtime;
	}
	
}
