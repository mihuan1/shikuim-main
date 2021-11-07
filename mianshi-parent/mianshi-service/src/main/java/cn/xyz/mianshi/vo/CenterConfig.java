package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity(value = "centerConfig", noClassnameStored = true)
public class CenterConfig {
	@Id
	private ObjectId id;
	private String type;// 服务器类型    1 xmpp服务器    2 http服务器    3 视频服务器   4 直播服务器
	private String name;// 节点名称 ：香港会议中心服务器
	@Indexed
	private String clientA;// 国家代码 China
	@Indexed
	private String clientB;// 国家代码 Malaysia
	@Indexed
	private String area;// urlConfig配置表id 可配置多个香港视频服务器
	
	private int status;// 状态 1正常  -1禁用   使用中提示不能禁用
}
