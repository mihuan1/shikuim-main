package cn.xyz.mianshi.model;

import java.util.Date;

import com.mongodb.BasicDBObject;

public class PhotoVO extends BasicDBObject {
	private static final long serialVersionUID = 1L;

	public Integer getAvatar() {
		return getInt("avatar");
	}

	public Date getCreateTime() {
		return getDate("createTime");
	}

	public String getOUrl() {
		return getString("oUrl");
	}

	public String getPhotoId() {
		return getString("photoId");
	}

	public String getTUrl() {
		return getString("tUrl");
	}

	public void setAvatar(Integer avatar) {
		put("avatar", avatar);
	}

	public void setCreateTime(Date createTime) {
		put("createTime", createTime);
	}

	public void setOUrl(String oUrl) {
		put("oUrl", oUrl);
	}

	public void setPhotoId(String photoId) {
		put("photoId", photoId);
	}

	public void setTUrl(String tUrl) {
		put("tUrl", tUrl);
	}

}
