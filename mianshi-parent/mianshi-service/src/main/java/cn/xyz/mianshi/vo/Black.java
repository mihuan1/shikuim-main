package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
/**
 * 黑名单
 * @author Administrator
 *
 */
@Entity(value="black",noClassnameStored=true)
public class Black {
	private @Id ObjectId blackId;//id 
	private @Indexed int userId;//被踢用户
	private @Indexed ObjectId roomId;//房间id
	private long time;//创建时间
	
	public Black() {}
	
	public Black(ObjectId blackId, int userId, ObjectId roomId, long time) {
		this.blackId = blackId;
		this.userId = userId;
		this.roomId = roomId;
		this.time = time;
	}

	public ObjectId getBlackId() {
		return blackId;
	}

	public void setBlackId(ObjectId blackId) {
		this.blackId = blackId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public ObjectId getRoomId() {
		return roomId;
	}

	public void setRoomId(ObjectId roomId) {
		this.roomId = roomId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	
}
