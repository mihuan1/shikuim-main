/**
 * 
 */
package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lidaye
 * 2017年7月21日
 */
@Getter
@Setter
@Entity(value="NewFriends",noClassnameStored=true)
public class NewFriends {
	
	@JSONField(serialize = false)
	private  @Id ObjectId id;// 关系Id
	
	@Indexed
	private int toUserId;// 好友Id
	@Indexed
	private int userId;// 用户Id
	
	private int from;//发起打招呼的用户ID
	
	private long createTime;// 建立关系时间
	private long modifyTime;// 修改时间
	
	private String content;// 信息内容
	
	private int direction;// 0=发出去的；1=收到的
	private int type;// 消息Type  
	private Integer status;// 状态（1=关注；2=好友；0=陌生人）
	private String toNickname;// 好友昵称
	
	
	
	
	

}
