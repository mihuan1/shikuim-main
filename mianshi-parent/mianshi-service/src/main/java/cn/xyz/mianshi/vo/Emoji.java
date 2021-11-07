package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

import cn.xyz.commons.utils.DateUtil;

/**
 * 收藏
 * @author Administrator
 *
 */
@Entity(value = "emoji", noClassnameStored = true)
@Indexes({ @Index("userId"), @Index("emojiId") })
public class Emoji{
	@Id
	private ObjectId emojiId;//收藏id
	private Integer userId;//用户id
	private int type;//收藏类型    1.图片   2.视频    3.文件  4.语音  5.文本   6.表情  7.SDK分享的链接
	private String url;// 复制后的文件地址
	private String msgId;
	private String msg;// 内容、文件URL
	private String roomJid;//房间JId
	private long createTime;//收藏时间
	private String fileName;// 文件名称
	private double fileSize;// 文件大小
	private int fileLength;// 文件长度
	private int collectType;// 收藏type; -1:消息无关的收藏,0:普通收藏 ,1：朋友圈收藏 
	private String collectContent;// 朋友圈收藏的文本内容
	private String collectMsgId;// 朋友圈内容id
	
	// 朋友圈分享的链接
	private String title;// 分享链接的标题
	private String shareURL;// 分享链接的url地址
	
	
	public Emoji() {}
	
	
	
	public Emoji(Integer userId, int type, String url, String msg, String fileName, double fileSize, int fileLength, int collectType ,String collectContent ,String collectMsgId) {
		this.userId = userId;
		this.type = type;
		this.url = url;
		this.msg = msg;
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.fileLength = fileLength;
		this.collectType = collectType;
		this.collectContent = collectContent;
		this.createTime = DateUtil.currentTimeSeconds();
		this.collectMsgId = collectMsgId;
	}
	
	public Emoji(Integer userId, int type, String url, String msg, String fileName, double fileSize, int fileLength, int collectType ,String collectContent ,String collectMsgId,String title,String shareURL) {
		this.userId = userId;
		this.type = type;
		this.url = url;
		this.msg = msg;
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.fileLength = fileLength;
		this.collectType = collectType;
		this.collectContent = collectContent;
		this.collectMsgId = collectMsgId;
		this.createTime = DateUtil.currentTimeSeconds();
		this.title = title;
		this.shareURL = shareURL;
	}



	public ObjectId getEmojiId() {
		return emojiId;
	}

	public void setEmojiId(ObjectId emojiId) {
		this.emojiId = emojiId;
	}

	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getRoomJid() {
		return roomJid;
	}

	public void setRoomJid(String roomJid) {
		this.roomJid = roomJid;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public double getFileSize() {
		return fileSize;
	}

	public void setFileSize(double fileSize) {
		this.fileSize = fileSize;
	}

	public int getFileLength() {
		return fileLength;
	}

	public void setFileLength(int fileLength) {
		this.fileLength = fileLength;
	}

	public int getCollectType() {
		return collectType;
	}

	public void setCollectType(int collectType) {
		this.collectType = collectType;
	}

	public String getCollectContent() {
		return collectContent;
	}

	public void setCollectContent(String collectContent) {
		this.collectContent = collectContent;
	}

	public String getCollectMsgId() {
		return collectMsgId;
	}

	public void setCollectMsgId(String collectMsgId) {
		this.collectMsgId = collectMsgId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getShareURL() {
		return shareURL;
	}

	public void setShareURL(String shareURL) {
		this.shareURL = shareURL;
	}
	
	
	
}
