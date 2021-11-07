package tigase.shiku;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.shiku.utils.DateUtil;

import tigase.conf.ConfigurationException;
import tigase.osgi.ModulesManagerImpl;
import tigase.server.AbstractMessageReceiver;
import tigase.server.Packet;
import tigase.shiku.conf.ShikuConfigBean;
import tigase.shiku.db.ShikuMessageArchiveRepository;
import tigase.shiku.db.UserDao;
import tigase.shiku.model.MessageModel;
import tigase.shiku.model.MucMessageModel;
import tigase.shiku.utils.Callback;
import tigase.shiku.utils.ThreadUtil;
import tigase.xmpp.BareJID;
import tigase.xmpp.JID;
import tigase.xmpp.StanzaType;

/**
 * 消息记录归档组件
 * <p>
 * <strong>功能：</strong>单聊和群聊消息归档
 * </p>
 * 
 *
 */
public class ShikuMessageArchiveComponent extends AbstractMessageReceiver {
	
	private static final Logger log = Logger
			.getLogger(ShikuMessageArchiveComponent.class.getCanonicalName());
	private static final String MSG_ARCHIVE_REPO_CLASS_PROP_KEY = "archive-repo-class";
	private static final String MSG_ARCHIVE_REPO_URI_PROP_KEY = "archive-repo-uri";
	public static final String MUC_REPO_URI="muc-repo-uri";
	private static final String[] MSG_BODY_PATH = { "message", "body" };

	private  ShikuMessageArchiveRepository repo = null;

	public ShikuMessageArchiveComponent() {
		super();
		setName("shiku-message-archive");
	}

	@Override
	public void processPacket(Packet packet) {
		if ((packet.getStanzaTo() != null)
				&& !getComponentId().equals(packet.getStanzaTo())) {
			// 保存消息
			storeMessage(packet);
			return;
		}
	}
	
	/* (non-Javadoc)
	 * @see tigase.server.AbstractMessageReceiver#processingInThreads()
	 */
	@Override
	public int processingInThreads() {
		// TODO Auto-generated method stub
		return Runtime.getRuntime().availableProcessors();
	}
	/* (non-Javadoc)
	 * @see tigase.server.AbstractMessageReceiver#processingOutThreads()
	 */
	@Override
	public int processingOutThreads() {
		// TODO Auto-generated method stub
		return Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setProperties(Map<String, Object> props)
			throws ConfigurationException {
		try {
			super.setProperties(props);

			if (props.size() == 1) {
				return;
			}

			Map<String, String> repoProps = new HashMap<String, String>(4);
			for (Entry<String, Object> entry : props.entrySet()) {
				if ((entry.getKey() == null) || (entry.getValue() == null))
					continue;
				repoProps.put(entry.getKey(), entry.getValue().toString());
			}

			String repoClsName = (String) props
					.get(MSG_ARCHIVE_REPO_CLASS_PROP_KEY);
			String uri = (String) props.get(MSG_ARCHIVE_REPO_URI_PROP_KEY);
			String mucRpoUri = (String)props.get(MUC_REPO_URI);
			if (null != uri) {
				if (null != repoClsName) {
					try {
						@SuppressWarnings("unchecked")
						Class<? extends ShikuMessageArchiveRepository> repoCls = (Class<? extends ShikuMessageArchiveRepository>) ModulesManagerImpl
								.getInstance().forName(repoClsName);
						repo = repoCls.newInstance();
						repo.initRepository(uri, repoProps);
					} catch (ClassNotFoundException e) {
						log.log(Level.SEVERE,
								"Could not find class "
										+ repoClsName
										+ " an implementation of ShikuMessageArchive repository",
								e);
						throw new ConfigurationException(
								"Could not find class "
										+ repoClsName
										+ " an implementation of ShikuMessageArchive repository",
								e);
					}
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "消息归档组件初始化失败", e);
			throw new ConfigurationException("消息归档组件初始化失败", e);
		}
	}

	@Override
	public void release() {
		super.release();
	}

	@Override
	public String getDiscoDescription() {
		return "ShiKu Message Archiving Support";
	}

	private Long getUserId(BareJID jid) {
		//得到账号ID
		String strUserId = jid.toString();
		int index = strUserId.indexOf("@");
		strUserId = strUserId.substring(0, index);

		return Long.parseLong(strUserId);
	}
	private Long getUserId(JID jid) {
		//得到账号ID
		String strUserId = jid.toString();
		int index = strUserId.indexOf("@");
		strUserId = strUserId.substring(0, index);

		return Long.parseLong(strUserId);
	}
	private String getRoomId(JID jid) {
		//得到房间ID
		String strUserId = jid.toString();
		int index = strUserId.indexOf("@");
		strUserId = strUserId.substring(0, index);

		return strUserId;
	}
	private String getRoomId(BareJID jid) {
		//得到房间ID
		String strUserId = jid.toString();
		int index = strUserId.indexOf("@");
		strUserId = strUserId.substring(0, index);

		return strUserId;
	}
	private void storeMessage(Packet packet) {
		String ownerStr = packet
				.getAttributeStaticStr(ShikuMessageArchivePlugin.OWNNER_JID);

		if (null==ownerStr) {
			log.log(Level.INFO, "Owner attribute missing from packet: {0}",
					packet);
			return;
		}
		
		
		packet.getElement().removeAttribute(
				ShikuMessageArchivePlugin.OWNNER_JID);
		StanzaType type = packet.getType();
		
			// 单聊
			if (StanzaType.chat == type) {
				storeMessageChat(packet, ownerStr);
			}
			// 群聊
			else if (StanzaType.groupchat == type) {
				storeMessageGrouChat(packet, ownerStr);
			}
			
	}
	//保存单聊记录
	private void storeMessageChat(Packet packet,String ownerStr){
		try {
			BareJID ownerJid = BareJID.bareJIDInstanceNS(ownerStr);
			
			//单聊
			int direction = ownerJid.equals(packet.getStanzaFrom()
					.getBareJID()) ? 0 : 1;// 0=发出去的；1=收到的
			
			JID sender_jid=packet.getStanzaFrom();
			JID receiver_jid =packet.getStanzaTo();
					
			long sender = getUserId(sender_jid);
			long receiver = getUserId(receiver_jid);
			
			long ts = DateUtil.getSysCurrentTimeMillis_sync();
			double timeSend=getTimeSend(ts);
			String message = packet.getElement().toString();
			String messageId = packet.getElement().getAttribute("id");
			
			
			int messageType = 1;// 1=chat
			String body = packet.getElement().getChildCData(MSG_BODY_PATH);
			
			if(null == body)
				return;
			int contextType=0;
			boolean isReadDel = false;  //是否为阅后即焚消息  false:不是  true:是
			String content = ""; //聊天内容
			long deleteTime=-1;//消息销毁时间
			int isEncrypt=0;
			JSONObject	jsonObj=null;
			if(null!=body){
				jsonObj=bodyToJson(body);
				contextType=jsonObj.getIntValue("type");
				isReadDel = jsonObj.getBooleanValue("isReadDel");
				content = jsonObj.getString("content");
				timeSend=jsonObj.getDoubleValue("timeSend");
				isEncrypt=jsonObj.getIntValue("isEncrypt");
				if(null!=jsonObj.get("deleteTime"))
					deleteTime=jsonObj.getLongValue("deleteTime");
				
				//替换timeSend 成服务器的时间
				//body=replaceTimeSend(timeSend, body, jsonObj);
				
				//同账号 转发的消息 不需要保存
				int toUserId=0;
				if(sender==receiver){
					if(jsonObj.get("fromUserId")!=jsonObj.get("toUserId"))
						return;
					toUserId=jsonObj.getIntValue("toUserId");
					if(0!=toUserId&&sender!=toUserId)
						return;
				}
				int formUserId = jsonObj.getIntValue("fromUserId");
				if(0!=formUserId) {
					sender=formUserId;
				}
				if(0!=toUserId) {
					receiver=toUserId;
				}
				
			}
			/*
			 * if(ShikuConfigBean.isDeBugMode()&&0==direction){
			 * System.out.println("  storeMessageChat  "+jsonObj); }
			 */
			
			//消息回执存储
			
			//加关注 加好友  打招呼 信息
			if(0==contextType)
				return;
			else if(26==contextType){
				//消息已读回执处理
				repo.updateMsgReadStatus(content);
				return;
			}else if(5==contextType/100){
				if(sender==receiver)
					return;
				UserDao.getInstance().saveNewFriendsInThread(sender, receiver,jsonObj.get("fromUserId"),direction, contextType, content);
				return;
			}
			else if(9==contextType/100)//群控制信息
				return;
			else if(99<contextType&&202!=contextType){
				//其他 系统 通知消息
				return;
			}
			else if(isReadDel == true) {
				return;
			}
				
				
			MessageModel model = new MessageModel(sender,
					sender_jid.toString(), receiver,
					receiver_jid.toString(), ts, direction, messageType,
					jsonObj.toJSONString(), message,content);
			model.setContentType(contextType);
			model.setMessageId(messageId);
			model.setTimeSend(timeSend);
			model.setDeleteTime(deleteTime);
			model.setIsEncrypt(isEncrypt);
			repo.archiveMessage(model);
			
		} catch (JSONException e) {
			log.info(e.getMessage());
		} catch (Exception e) {
			log.info(e.getMessage());
		}
	}
	
	//保存群聊记录
	private void storeMessageGrouChat(Packet packet,String ownerStr){
		BareJID sender_jid = BareJID.bareJIDInstanceNS(ownerStr);
		long sender = getUserId(sender_jid);
		
		
			BareJID room_jid = packet.getStanzaTo().getBareJID();
			String room_id = getRoomId(room_jid);
			String nickname = "";
			String body = packet.getElement().getChildCData(
					MSG_BODY_PATH);
			String message = packet.getElement().toString();
			String messageId = packet.getElement().getAttribute("id");
			Integer public_event = 1;
			Long ts = System.currentTimeMillis(); 
			double timeSend=getTimeSend(ts);
			Integer event_type = 1;

			int contextType=0;
			String context = ""; //消息内容
			long deleteTime=-1;//消息销毁时间
			int isEncrypt=0;
			JSONObject	jsonObj=null;
			if(null!=body){
				try {
					jsonObj=bodyToJson(body);
					contextType=jsonObj.getIntValue("type");
					context = jsonObj.getString("content");
					isEncrypt=jsonObj.getIntValue("isEncrypt");
					timeSend=jsonObj.getDoubleValue("timeSend");
					int formUserId = jsonObj.getIntValue("fromUserId");
					if(0!=formUserId) {
						sender=formUserId;
					}
					if(null!=jsonObj.get("deleteTime"))
						deleteTime=jsonObj.getLongValue("deleteTime");
					//替换timeSend 成服务器的时间
					//body=replaceTimeSend(timeSend, body, jsonObj);
				} catch (JSONException e) {
					
				}
			}
		/*
		 * if(ShikuConfigBean.isDeBugMode()){
		 * System.out.println(" storeMessageGrouChat  "+jsonObj); }
		 */
			//加关注 加好友  打招呼 信息
			if(0==contextType)
				return;
			/*else if(contextType/100==9)//群控制信息
				return;
			*/
			else if(26==contextType)
				return;
			else if(99<contextType&&201>contextType){
				//其他 系统 通知消息
				return;
			}
			
			MucMessageModel model = new MucMessageModel(room_id,
					room_jid.toString(), sender, sender_jid.toString(),
					nickname, jsonObj.toJSONString(), message, public_event, ts,
					event_type,context);
			model.setContentType(contextType);
			model.setMessageId(messageId);
			model.setTimeSend(timeSend);
			model.setDeleteTime(deleteTime);
			model.setIsEncrypt(isEncrypt);
			repo.archiveMessage(model);
			
		
	}
	
	private JSONObject bodyToJson(String body){
		JSONObject jsonObj=null;
		try {
			jsonObj= JSON.parseObject(body.replaceAll("&quot;", "\"").replaceAll("&quot;", "\""));
			return jsonObj;
		} catch (Exception e) {
			return null;
		}
	}
	
	private double getTimeSend(long ts){
		double time =(double)ts;
		DecimalFormat dFormat = new DecimalFormat("#.000");
		return new Double(dFormat.format(time/1000));
	}
	
	private String replaceTimeSend(Double timeSend,String body,JSONObject jsonObj){
			String oldTime=jsonObj.getString("timeSend");
			if(oldTime==null){
				return body;
			}
			body=body.replace(oldTime, timeSend.toString());
		return body;
	}
	
}
