package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value="courseMessage",noClassnameStored=true)
public class CourseMessage {
	private @Id ObjectId courseMessageId;
	private int userId;
	private String courseId;
	private String createTime;
	private String message;
	private String messageId;
	public CourseMessage() {}

	public CourseMessage(int userId, String courseId, String createTime, String message ,String messageId) {
		this.userId = userId;
		this.courseId = courseId;
		this.createTime = createTime;
		this.message = message;
		this.messageId = messageId;
	}
	
	public ObjectId getCourseMessageId() {
		return courseMessageId;
	}

	public void setCourseMessageId(ObjectId courseMessageId) {
		this.courseMessageId = courseMessageId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	
}
