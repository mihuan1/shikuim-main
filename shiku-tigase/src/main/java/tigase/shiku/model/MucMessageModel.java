package tigase.shiku.model;

public class MucMessageModel {
	private String body; //消息体
	private Integer event_type = 1;
	private String message; //完整消息 xml格式
	private String nickname;
	private Integer public_event = 1;
	private String room_id;
	private String room_jid;

	private Long sender;
	private String sender_jid;
	private Long ts; //时间 毫秒
	private String messageId; //消息Id
	private String content; //消息内容
	
	//body  消息体里面的 type
	private Integer contentType;
	
	private Double timeSend;//消息发送时间
	
	private long deleteTime;////消息 销毁时间
	
	private int isEncrypt=0;
	
	/**
	 * 发送者名称
	 */
	private String fromUserName;

	public MucMessageModel() {
		super();
	}

	public MucMessageModel(String room_id, String room_jid, Long sender,
			String sender_jid, String nickname, String body, String message,
			Integer public_event, Long ts, Integer event_type,String content) {
		super();
		this.room_id = room_id;
		this.room_jid = room_jid;
		this.sender = sender;
		this.sender_jid = sender_jid;
		this.nickname = nickname;
		this.body = body;
		this.message = message;
		this.public_event = public_event;
		this.ts = ts;
		this.event_type = event_type;
		this.content = content;
	}
	
	
	

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getBody() {
		return body;
	}

	public Integer getEvent_type() {
		return event_type;
	}

	public String getMessage() {
		return message;
	}

	public String getNickname() {
		return nickname;
	}

	public Integer getPublic_event() {
		return public_event;
	}

	public String getRoom_id() {
		return room_id;
	}

	public String getRoom_jid() {
		return room_jid;
	}

	public Long getSender() {
		return sender;
	}

	public String getSender_jid() {
		return sender_jid;
	}

	public Long getTs() {
		return ts;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setEvent_type(Integer event_type) {
		this.event_type = event_type;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setPublic_event(Integer public_event) {
		this.public_event = public_event;
	}

	public void setRoom_id(String room_id) {
		this.room_id = room_id;
	}

	public void setRoom_jid(String room_jid) {
		this.room_jid = room_jid;
	}

	public void setSender(Long sender) {
		this.sender = sender;
	}

	public void setSender_jid(String sender_jid) {
		this.sender_jid = sender_jid;
	}

	public void setTs(Long ts) {
		this.ts = ts;
	}

	public Integer getContentType() {
		return contentType;
	}

	public void setContentType(Integer contentType) {
		this.contentType = contentType;
	}

	public Double getTimeSend() {
		return timeSend;
	}

	public void setTimeSend(Double timeSend) {
		this.timeSend = timeSend;
	}

	public long getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(long deleteTime) {
		this.deleteTime = deleteTime;
	}

	public int getIsEncrypt() {
		return isEncrypt;
	}

	public void setIsEncrypt(int isEncrypt) {
		this.isEncrypt = isEncrypt;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	
	

}
