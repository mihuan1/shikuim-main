package tigase.shiku.model;

/**
* @Description: TODO(最近的聊天记录)
* @author lidaye
* @date 2018年8月8日 
*/
public class LastChatModel {
	
	private String content;//聊天内容
	
	private int type;//消息类型
	
	private long timeSend;//发送时间 秒
	//用户ID
	private String userId;
	
	/**
	 * 发送者用户ID
	 */
	private String from;
	/**
	 *  单聊 为 好友的 userId String 值 
	 *    群聊为 群组的jid
	 */
	private String jid;
	
	private int isRoom;//是否群聊   1 群聊  0单聊
	
	private int isEncrypt=0;
	
	private String messageId;//消息ID
	
	private String fromUserName;
	
	private String toUserName;
	
	private String to;
	
	private String body;
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getTimeSend() {
		return timeSend;
	}

	public void setTimeSend(long timeSend) {
		this.timeSend = timeSend;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public int getIsRoom() {
		return isRoom;
	}

	public void setIsRoom(int isRoom) {
		this.isRoom = isRoom;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public int getIsEncrypt() {
		return isEncrypt;
	}

	public void setIsEncrypt(int isEncrypt) {
		this.isEncrypt = isEncrypt;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	public String getToUserName() {
		return toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}

