package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity(value = "areaConfig", noClassnameStored = true)
public class AreaConfig {
	@Id
	private ObjectId id;
	@Indexed
	private String area;// 地区代号
	private String name;// 名称
//	private String xmppConfig;// urlConfig配置表Id
//	private String liveConfig;// urlConfig配置表Id
//	private String httpConfig;// urlConfig配置表Id
//	private String videoConfig;// urlConfig配置表Id
//	private String name;// 中国地区配置
//	private String status;// 状态 1 正常  -1 禁用
}
