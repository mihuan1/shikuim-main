package cn.xyz.mianshi.model;

public class AddCommentParam {
	private String messageId;
	private int toUserId;
	private String toNickname;
	private String toBody;
	private String body;

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public int getToUserId() {
		return toUserId;
	}

	public void setToUserId(int toUserId) {
		this.toUserId = toUserId;
	}

	public String getToNickname() {
		return toNickname;
	}

	public void setToNickname(String toNickname) {
		this.toNickname = toNickname;
	}

	public String getToBody() {
		return toBody;
	}

	public void setToBody(String toBody) {
		this.toBody = toBody;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}
