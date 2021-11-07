package cn.xyz.mianshi.vo;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value="course",noClassnameStored=true)
public class Course {
	private @Id ObjectId courseId;//课程Id
	private int userId;//用户Id
	private List<String> messageIds;//消息Id
	private long createTime;//创建时间
	private long updateTime;//修改时间
	private String courseName;//课程名称
	private String roomJid;//房间jid
	
	public Course() {}

	public Course(ObjectId courseId, int userId, List<String> messageIds, long createTime, long updateTime,
			String courseName, String roomJid) {
		this.courseId = courseId;
		this.userId = userId;
		this.messageIds = messageIds;
		this.createTime = createTime;
		this.updateTime = updateTime;
		this.courseName = courseName;
		this.roomJid = roomJid;
	}

	public String getRoomJid() {
		return roomJid;
	}

	public void setRoomJid(String roomJid) {
		this.roomJid = roomJid;
	}

	public ObjectId getCourseId() {
		return courseId;
	}
	public void setCourseId(ObjectId courseId) {
		this.courseId = courseId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public List<String> getMessageIds() {
		return messageIds;
	}

	public void setMessageIds(List<String> messageIds) {
		this.messageIds = messageIds;
	}

	public long getCreateTime() {
		return createTime;
	}
	
	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	
	
}	
