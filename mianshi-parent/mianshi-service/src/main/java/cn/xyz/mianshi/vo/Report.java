/**
 * 
 */
package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.NotSaved;

/**
 * @author lidaye
 * 2017年6月26日
 */
@Entity(value = "Report", noClassnameStored = true)
public class Report {
	private @Id ObjectId id;//
	
	private @Indexed long userId;// 举报用户
	
	private @Indexed long toUserId=0;// 被举报用户
	
	private String roomId;//群组id 被举报群组
	
	private String webUrl;// 被举报的网页
	
	private int reason;// 原因Id
	
	private long time;// 举报时间
	
	private @Indexed int status=1;
	
	@NotSaved
	private int toUserStatus;// 被举报人当前账号状态   -1：锁定, 1:正常
	
	@NotSaved
	private int roomStatus;// 被举报群组当前状态   -1：锁定, 1:正常
	
	@NotSaved
	private int webStatus;// 被举报网页状态  -1：锁定   1：正常
	
	@NotSaved
	private String info;// 举报原因
	
	@NotSaved
	private String userName;// 举报人昵称
	
	@NotSaved
	private String toUserName;// 被举报人昵称
	
	@NotSaved
	private String roomName;// 被举报的群组昵称

	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getReason() {
		return reason;
	}
	public void setReason(int reason) {
		this.reason = reason;
	}
	public long getToUserId() {
		return toUserId;
	}
	public void setToUserId(long toUserId) {
		this.toUserId = toUserId;
	}
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public int getToUserStatus() {
		return toUserStatus;
	}
	public void setToUserStatus(int toUserStatus) {
		this.toUserStatus = toUserStatus;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getToUserName() {
		return toUserName;
	}
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public int getRoomStatus() {
		return roomStatus;
	}
	public void setRoomStatus(int roomStatus) {
		this.roomStatus = roomStatus;
	}
	public String getWebUrl() {
		return webUrl;
	}
	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}
	public int getWebStatus() {
		return webStatus;
	}
	public void setWebStatus(int webStatus) {
		this.webStatus = webStatus;
	}
	
}
