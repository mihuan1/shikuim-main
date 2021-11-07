package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Data;

@Data
@Entity(value = "helper", noClassnameStored = true)
public class Helper {
	
	private @Id ObjectId id;
	
	private String openAppId;
	
	private String name;// 标题
	
	private String desc;// 描述
	
	private String iconUrl;// 图标
		
	private String developer;// 开发者
	
	private String link;// 地址
	
	private Other other;// 富文本
	
	private Integer type;// 群助手类型	1:自动回复  2:网页链接  3:富文本

	private long createTime;// 创建时间
	
	private String appPackName;// android app包名  type=2、3 时会有
	
	private String callBackClassName;// android回调类 type=2、3时会有
	
	private String iosUrlScheme;//ios跳转必填参数 URL Scheme
	
	@Data
	public static class Other{
		private String title;
		private String subTitle;
		private String url;
		private String imageUrl;
		private String appIcon;
		private String appName;
	}
}
