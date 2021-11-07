package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity(value = "serverListConfig", noClassnameStored = true)
public class ServerListConfig {
	@Id
	private ObjectId id;
	private String name;// 机器名称
	private String url; // 服务器地址
	private String port;// 端口
	private int count;// 当前人数
	private int maxPeople;// 人数上限
	private String area;// 地区
	private int type;// 服务器类型     1 xmpp服务器    2 http服务器    3 视频服务器   4 直播服务器
	private int status;// 状态  1正常   -1禁用    使用中提示不能禁用
}
