package cn.xyz.mianshi.vo;

import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.NotSaved;

import com.alibaba.fastjson.JSON;

/** @version:（1.0） 
* @ClassName	Message
* @Description: （错误消息） 
* @author: wcl
* @date:2018年8月24日下午4:49:04  
*/ 
@Entity(value = "message", noClassnameStored = true)
public class ErrorMessage {
	@Id
	private ObjectId id;
	private String code;
	private String zh;
	private String en;
	private String type;
	private String big5;
	
	@NotSaved
	private Map<String,String> map;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getZh() {
		return zh;
	}

	public void setZh(String zh) {
		this.zh = zh;
	}

	public String getEn() {
		return en;
	}

	public void setEn(String en) {
		this.en = en;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBig5() {
		return big5;
	}

	public void setBig5(String big5) {
		this.big5 = big5;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return JSON.toJSONString(this);
	}

}
