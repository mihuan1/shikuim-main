package tigase.shiku;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.ServiceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import tigase.db.NonAuthUserRepository;
import tigase.server.Message;
import tigase.server.Packet;
import tigase.server.monitor.MonitorRuntime;
import tigase.shiku.conf.ShikuConfigBean;
import tigase.xmpp.BareJID;
import tigase.xmpp.StanzaType;
import tigase.xmpp.XMPPException;
import tigase.xmpp.XMPPProcessor;
import tigase.xmpp.XMPPProcessorIfc;
import tigase.xmpp.XMPPResourceConnection;
import tigase.xmpp.impl.C2SDeliveryErrorProcessor;

/**
 * 离线推送通知、离线消息推送插件
 * 
 *
 */
public class ShikuOfflineMsgPlugin extends XMPPProcessor implements XMPPProcessorIfc {
	private  Logger logger = LoggerFactory.getLogger(ShikuOfflineMsgPlugin.class.getName());
	private static final String ID = "shiku-offline-msg";
	private static final String[] XMLNSS = { Packet.CLIENT_XMLNS };
	private static final Set<StanzaType> TYPES;

	static {
		HashSet<StanzaType> tmpTYPES = new HashSet<StanzaType>();
		tmpTYPES.add(null);
		tmpTYPES.addAll(EnumSet.of(StanzaType.groupchat, StanzaType.chat));
		TYPES = Collections.unmodifiableSet(tmpTYPES);
	}

	private static final String MESSAGE = "message";
	private static final String[][] ELEMENT_PATHS = { { MESSAGE } };
	/**
	 * 
	 */
	public ShikuOfflineMsgPlugin() {

	}
	
	@Override
	public String id() {
		return ID;
	}
	
	private DefaultMQProducer pushProducer;
	
	public synchronized DefaultMQProducer getPushProducer() {
		if(null!=pushProducer)
			return pushProducer;
		restartProducer();
		return pushProducer;
	}
	public synchronized void   startPushProducer(){
		try {
			pushProducer=new DefaultMQProducer("pushProducer");
			pushProducer.setNamesrvAddr(ShikuConfigBean.pushMqAddrVal);
			pushProducer.setVipChannelEnabled(false);
			pushProducer.setCreateTopicKey("pushMessage");
			pushProducer.start();

		} catch (Exception e) {
			System.out.println("getPushProducer error  "+e.getMessage());
		}
	}
	public void restartProducer() {
		System.out.println("pushProducer restartProducer ===》 "+ShikuConfigBean.pushMqAddrVal);
		try {
			if(null!=pushProducer&&null!=pushProducer.getDefaultMQProducerImpl()) {
				if(ServiceState.CREATE_JUST==pushProducer.getDefaultMQProducerImpl().getServiceState()) {
					try {
						pushProducer.start();
					} catch (Exception e) {
						pushProducer=null;
						startPushProducer();
					}
				}
			}else {
				pushProducer=null;
				startPushProducer();
			}
		} catch (Exception e) {
			System.err.println("restartProducer Exception "+e.getMessage());
			
		}	
		
	}
	
	@Override
	public void process(Packet packet, XMPPResourceConnection session, NonAuthUserRepository repo,
			Queue<Packet> results, Map<String, Object> settings) throws XMPPException {
		StanzaType type = packet.getType();
		if (session == null) {
			return;
		}else if (Message.ELEM_NAME!= packet.getElemName()) {
			return;
		}else if (null==(packet.getElement().findChildStaticStr(Message.MESSAGE_BODY_PATH))){
			return;
		}else if ((type != StanzaType.chat)	&& (type != StanzaType.groupchat)){
			return;
		}
		

		// ignoring packets resent from c2s for redelivery as processing
		// them would create unnecessary duplication of messages in
		// archive
		if (C2SDeliveryErrorProcessor.isDeliveryError(packet)) {
			return;
		}
		if(null!=session.getjid()&&!packet.getStanzaFrom().getBareJID().equals(session.getjid().getBareJID()))
			return;
		//BareJID sender_jid=packet.getStanzaFrom().getBareJID();
		BareJID receiver_jid=packet.getStanzaTo().getBareJID();
		
		
		
		String body = packet.getElement().getChildCData(new String[] { "message", "body" });
		if(null == body)
			return;
		JSONObject bodyObj=null;
		int contextType=0;
		String objectId=null;
		if(null!=body){
			try {
				bodyObj= JSON.parseObject(body.replaceAll("&quot;", "\"").replaceAll("&quot;", "\""));
			} catch (Exception e) {
				logger.error("==== {} ====> {}",body,e.getMessage());
				return;
			}
		}
		contextType=bodyObj.getIntValue("type");
		if(0==contextType)
			return;
		//消息回执不处理
		else if(26==contextType||27==contextType||200==contextType||201==contextType)
			return;
		objectId=bodyObj.getString("objectId");
		
		long receiverId=0;
		if(type == StanzaType.chat){
			try {
				receiverId=getUserId(receiver_jid);
			} catch (NumberFormatException e) {
				logger.error("receiverId==parseLong--Fail--- {}",receiver_jid);
			}
				//系统推送 不离线通知
			if(receiverId<100020)
				return;
		}
		try {
			// TODO 此处请自行实现目标用户离线的消息处理逻辑
			// 单聊消息
			if(StanzaType.chat== type){
				// 用户离线、执行离线推送逻辑
				// 接收方是否在线
				boolean isOnline = MonitorRuntime.getMonitorRuntime().isJidOnline(packet.getStanzaTo());
				// 是否进行离线通知
				if(ShikuConfigBean.isDeBugMode())
					logger.info("ShikuOfflineMsgPlugin发出：接收方是否在线---  {}",isOnline);
				boolean isNotify =!isOnline;
				if(isNotify) {
					bodyObj.put("toUserId", receiverId);
					bodyObj.put("isGroup", false);
					
						org.apache.rocketmq.common.message.Message message=
								new org.apache.rocketmq.common.message.Message("pushMessage",bodyObj.toJSONString().getBytes("utf-8"));
					try {	
						SendResult result = getPushProducer().send(message);
						if(SendStatus.SEND_OK!=result.getSendStatus()){
							logger.error(result.toString());
						}
					} catch (Exception e) {
						logger.error("send  push Exception "+e.getMessage());
						restartProducer();
					}
					
				}
				
			}else if(StanzaType.groupchat == type&&(objectId!=null||null!=receiver_jid)){
				 if(contextType/100==9)
						return;
				bodyObj.put("isGroup", true);
				bodyObj.put("roomJid", receiver_jid.getLocalpart());
				org.apache.rocketmq.common.message.Message message=
						new org.apache.rocketmq.common.message.Message("pushMessage",bodyObj.toJSONString().getBytes("utf-8"));
				try {
					SendResult result = getPushProducer().send(message);
					if(SendStatus.SEND_OK!=result.getSendStatus()){
						System.out.println(result.toString());
					}
				} catch (Exception e) {
					logger.error("send  push Exception "+e.getMessage());
					restartProducer();
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String[][] supElementNamePaths() {
		return ELEMENT_PATHS;

	}

	@Override
	public String[] supNamespaces() {
		return XMLNSS;
	}

	@Override
	public Set<StanzaType> supTypes() {
		return TYPES;
	}

	private Long getUserId(BareJID jid) {
		String strUserId = jid.toString();
		int index = strUserId.indexOf("@");
		strUserId = strUserId.substring(0, index);
		
		return Long.parseLong(strUserId);
	}
	private String getRoomJid(BareJID jid) {
		String strUserId = jid.toString();
		int index = strUserId.indexOf("@");
		strUserId = strUserId.substring(0, index);

		return strUserId;
	}
}
