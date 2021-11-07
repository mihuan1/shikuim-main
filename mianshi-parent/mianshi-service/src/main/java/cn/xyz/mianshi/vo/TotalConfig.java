package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity(value="totalConfig",noClassnameStored=true)
public class TotalConfig {
	@Id
	private ObjectId id;
	private String area;// 地区 China  HK
	private String xmppConfig;// UrlConfig配置表id
	private String liveConfig;// UrlConfig配置表id
	private String httpConfig;// UrlConfig配置表id
	private String videoConfig;// UrlConfig配置表id
	private String name;// 中国地区 配置 
	private int status;// 状态 1正常 -1禁用 
}
