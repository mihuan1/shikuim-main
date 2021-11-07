package tigase.shiku.model;

public class MessageModel {
	private String body;
	private Integer direction;// 0=发出的；1=收到的
	private String message;
	private Long receiver;
	private String receiver_jid;
	private Long sender;
	private String sender_jid;
	private Long ts;
	private Integer type;
	private String messageId;  //消息id
	//body  消息体里面的 type
	private Integer contentType;
	private String content; //消息内容
	
	private Double timeSend;//消息发送时间
	
	
	private long deleteTime;////消息 销毁时间
	
	private int isRead=0;//标记消息是否 已读   0 未读 1 已读
	
	private int isEncrypt=0;
	/**
	 * 发送者名称
	 */
	private String fromUserName;
	/*//消息发送者
	private Long from;
	//消息接受者
	private Long to;*/

	public MessageModel() {
		
		super();
	}

	
	public MessageModel(Long sender, String sender_jid, Long receiver,
			String receiver_jid, Long ts, Integer direction, Integer type,
			String body, String message,String content) {
		super();
		this.sender = sender;
		this.sender_jid = sender_jid;
		this.receiver = receiver;
		this.receiver_jid = receiver_jid;
		this.ts = ts;
		this.direction = direction;
		this.type = type;
		this.body = body;
		this.message = message;
		this.content = content;
	}
	

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}


	public Integer getContentType() {
		return contentType;
	}


	public void setContentType(Integer contentType) {
		this.contentType = contentType;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}


	public String getBody() {
		return body;
	}

	public Integer getDirection() {
		return direction;
	}

	public String getMessage() {
		return message;
	}

	public Long getReceiver() {
		return receiver;
	}

	public String getReceiver_jid() {
		return receiver_jid;
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

	public Integer getType() {
		return type;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setReceiver(Long receiver) {
		this.receiver = receiver;
	}

	public void setReceiver_jid(String receiver_jid) {
		this.receiver_jid = receiver_jid;
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

	public void setType(Integer type) {
		this.type = type;
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


	public int getIsRead() {
		return isRead;
	}


	public void setIsRead(int isRead) {
		this.isRead = isRead;
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
