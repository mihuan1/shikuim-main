package cn.xyz.mianshi.model;

public class MessageExample {
	private String bodyTitle;
	private int cityId;
	private int flag;
	private String msgId;
	private int pageSize = 20;

	public String getBodyTitle() {
		return bodyTitle;
	}

	public int getCityId() {
		return cityId;
	}

	public int getFlag() {
		return flag;
	}

	public String getMsgId() {
		return msgId;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setBodyTitle(String bodyTitle) {
		this.bodyTitle = bodyTitle;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

}
