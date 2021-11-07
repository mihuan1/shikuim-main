package cn.xyz.mianshi.vo;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.NotSaved;

import lombok.Data;

@Data
@Entity(value = "groupHelper", noClassnameStored = true)
public class GroupHelper {
	private @Id ObjectId id;
	
	private String helperId;// 群助手Id
	
	private String roomId;// 房间id
	
	private String roomJid;// 房间jid
	
	private List<KeyWord> keywords;// 关键字
	
	private Integer userId;// 创建用户Id
	
	@NotSaved
	private Helper helper;
	
	@Data
	public static class KeyWord{
		private String id;
		private String keyWord;
		private String value;
	}
}
