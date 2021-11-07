package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity(value = "urlConfig", noClassnameStored = true)
public class UrlConfig {
	@Id
	private ObjectId id;
	private String type;// 服务器类型  1 xmpp服务器    2 http服务器    3 视频服务器   4 直播服务器
	@Indexed
	private String area;// 访问来源地区
//	private String name;// 节点名称
//	private List<ObjectId> Ids;// 服务器地址
	@Indexed
	private String toArea;// 提供服务地区
}
