package cn.xyz.mianshi.model;

import java.util.List;

public class AddMsgParam extends BaseExample {
	private String address;//地理位置
	private String audios;// 语音地址
	private int flag;//消息标记 ：默认是3  普通消息
	private String images;// 图片地址
	private String messageId;// 消息id
	private String remark;// 评论
	private int source;// 来源
	private String text;// 内容
	private long time;// 发送的时间
	private String title;// 标题
	private int type;// 基础属性  1=文字消息、2=图文消息、3=语音消息、4=视频消息、 5=文件消息 、 6=SDK分享消息
	private String videos;// 视频地址
	private String files;// 文件地址
	private int visible=1;// 默认 1  公开 2 私密 3  部分好友可见 4 不给谁看
	private String lable;// 标签（目前用于短视频标签）
	private String musicId;// 短视频的音乐Id
	
	private String sdkUrl;// sdk分享url
	private String sdkIcon;// sdk分享icon
	private String sdkTitle;// sdk分享title
	
	private List<Integer> userLook;//谁可以看的玩家id
	private List<Integer> userNotLook;//谁不能看的玩家id
	private List<Integer> userRemindLook;//提醒谁看的玩家id
	
	private int isAllowComment;// 是否允许评论  0：允许  1：禁止评论 

	public String getAddress() {
		return address;
	}

	public String getAudios() {
		return audios;
	}

	public int getFlag() {
		return flag;
	}

	public String getImages() {
		return images;
	}

	public String getMessageId() {
		return messageId;
	}

	public String getRemark() {
		return remark;
	}

	public int getSource() {
		return source;
	}

	public String getText() {
		return text;
	}

	public long getTime() {
		return time;
	}

	public String getTitle() {
		return title;
	}

	public int getType() {
		return type;
	}

	public String getVideos() {
		return videos;
	}

	public int getVisible() {
		return visible;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setAudios(String audios) {
		this.audios = audios;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setVideos(String videos) {
		this.videos = videos;
	}

	public void setVisible(int visible) {
		this.visible = visible;
	}

	public List<Integer> getUserLook() {
		return userLook;
	}

	public void setUserLook(List<Integer> userLook) {
		this.userLook = userLook;
	}

	public List<Integer> getUserNotLook() {
		return userNotLook;
	}

	public void setUserNotLook(List<Integer> userNotLook) {
		this.userNotLook = userNotLook;
	}

	public List<Integer> getUserRemindLook() {
		return userRemindLook;
	}

	public void setUserRemindLook(List<Integer> userRemindLook) {
		this.userRemindLook = userRemindLook;
	}

	public String getFiles() {
		return files;
	}

	public void setFiles(String files) {
		this.files = files;
	}

	public String getSdkUrl() {
		return sdkUrl;
	}

	public void setSdkUrl(String sdkUrl) {
		this.sdkUrl = sdkUrl;
	}

	public String getSdkIcon() {
		return sdkIcon;
	}

	public void setSdkIcon(String sdkIcon) {
		this.sdkIcon = sdkIcon;
	}

	public String getSdkTitle() {
		return sdkTitle;
	}

	public void setSdkTitle(String sdkTitle) {
		this.sdkTitle = sdkTitle;
	}

	public String getLable() {
		return lable;
	}

	public void setLable(String lable) {
		this.lable = lable;
	}

	public String getMusicId() {
		return musicId;
	}

	public void setMusicId(String musicId) {
		this.musicId = musicId;
	}

	public int getIsAllowComment() {
		return isAllowComment;
	}

	public void setIsAllowComment(int isAllowComment) {
		this.isAllowComment = isAllowComment;
	}
	
}
