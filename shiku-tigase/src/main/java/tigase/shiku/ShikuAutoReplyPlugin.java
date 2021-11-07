package tigase.shiku;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tigase.db.NonAuthUserRepository;
import tigase.server.Message;
import tigase.server.Packet;
import tigase.server.monitor.MonitorRuntime;
import tigase.shiku.conf.ShikuConfigBean;
import tigase.util.TigaseStringprepException;
import tigase.xml.Element;
import tigase.xmpp.BareJID;
import tigase.xmpp.StanzaType;
import tigase.xmpp.XMPPException;
import tigase.xmpp.XMPPProcessor;
import tigase.xmpp.XMPPProcessorIfc;
import tigase.xmpp.XMPPResourceConnection;
import tigase.xmpp.impl.C2SDeliveryErrorProcessor;

/**
 * 消息插件
 * <p>
 * 1、单聊回执
 * </p>
 * <p>
 * 2、群聊回执
 * </p>
 * 
 *   test
 */
public class ShikuAutoReplyPlugin extends XMPPProcessor implements 
		XMPPProcessorIfc{
	
	private  Logger logger = LoggerFactory.getLogger(ShikuAutoReplyPlugin.class.getName());
	
	private static final String ID = "shiku-auto-reply";
	private static final String[] XMLNSS = { Packet.CLIENT_XMLNS };
	private static final Set<StanzaType> TYPES;
	
	private static  String VIRTHOSTS=null;
	static {
		HashSet<StanzaType> tmpTYPES = new HashSet<StanzaType>();
		tmpTYPES.add(null);
		tmpTYPES.addAll(EnumSet.of(StanzaType.groupchat, StanzaType.chat));
		TYPES = Collections.unmodifiableSet(tmpTYPES);
	}
	private static final String MESSAGE = "message";
	private static final String[][] ELEMENT_PATHS = { { MESSAGE } };
	
	public static final String[] REQUEST_PATH = {MESSAGE, "request" };
	public static final String MESSAGE_RECEIVED_XMLNS = "urn:xmpp:receipts";
	
	
	@Override
	public String id() {
		return ID;
	}
	
	private String  getVirthost() {
		if(null==VIRTHOSTS) {
			VIRTHOSTS=ShikuConfigBean.VIRTHOSTS;
			ShikuConfigBean.VIRTHOSTS=VIRTHOSTS;
			System.out.println("==ShikuAutoReplyPlugin== VIRTHOSTS ===> "+VIRTHOSTS);
			
		}
		return VIRTHOSTS;
	}

	@Override
	public void process(Packet packet, XMPPResourceConnection session,
			NonAuthUserRepository repo, Queue<Packet> results,
			Map<String, Object> settings) throws XMPPException {
		if (session == null) {
			return;
		}
		try {
			StanzaType type = packet.getType();
			//开始聊天啦
			if (Message.ELEM_NAME!= packet.getElemName()) {
				return;
			}
			/*
			 * else if(packet.getElement().getXMLNSStaticStr(REQUEST_PATH)!=
			 * MESSAGE_RECEIVED_XMLNS) { return; }
			 */
			else if (null==packet.getElemCDataStaticStr(Message.MESSAGE_BODY_PATH)||
					null!=packet.getElemCDataStaticStr(Message.MESSAGE_DELAY_PATH)){
					return;
			}else if ((type != StanzaType.chat)	&& (type != StanzaType.groupchat)){
				return;
			}
			
			// ignoring packets resent from c2s for redelivery as processing
			// them would create unnecessary duplication of messages in
			// archive
			if (C2SDeliveryErrorProcessor.isDeliveryError(packet))
				return;
			if(null!=session.getjid()&&!packet.getStanzaFrom().getBareJID().equals(session.getjid().getBareJID()))
				return;
			//String body = packet.getElement().getChildCData(Message.MESSAGE_BODY_PATH);
			BareJID receiver_jid=packet.getStanzaTo().getBareJID();
			// 回执接收方是登录用户时
				//登陆用户为当前session
			
			if(""==receiver_jid.toString()||null==receiver_jid){
				logger.error("ShikuAutoReplyPlugin == Error toJid is null");
			}
				/*
				long receiverId=0;
				boolean isReply = true;
				if (StanzaType.chat == type) {
					try {
						receiverId=getUserId(receiver_jid);
					} catch (NumberFormatException e) {
						System.out.println(" senderId==parseLong--Fail---"+receiver_jid);
						System.out.println(" chatType =》 "+type+" from===> "+packet.getStanzaFrom()+" body====> "+body);
					}
					// 接收方是否在线
					boolean isOnline = MonitorRuntime.getMonitorRuntime()
							.isJidOnline(packet.getStanzaTo());
					isReply = !isOnline;
				} else if (StanzaType.groupchat == type) {
					isReply = true;
				}
				if(receiverId<10030)
					isReply = true;
				if (isReply) {
				sendReply(packet, results);
				}
				*/
				sendReply(packet, results);
				
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Long getUserId(BareJID jid) {
		String strUserId = jid.toString();
		int index = strUserId.indexOf("@");
		strUserId = strUserId.substring(0, index);
		
		return Long.parseLong(strUserId);
	}
	
	//发送消息回执
	private void sendReply(Packet packet,Queue<Packet> results) throws TigaseStringprepException{
		//获取message ID
		String id = packet.getStanzaId();
		if (null == id || "".equals(id))
			return;
		
		Element received = new Element("received");
		received.setXMLNS(MESSAGE_RECEIVED_XMLNS);
		
		received.addAttribute("id", id);
		//received.addAttribute("messageId", id);
		received.addAttribute("status", "1");
		
		//StanzaType.chat==packet.getType()?packet.getStanzaTo().toString():""
		String from="";
		if(StanzaType.chat==packet.getType()){
			from=packet.getStanzaTo().getBareJID().toString();
		}else {
			from=getVirthost();
		}
		Packet receipt = Packet.packetInstance("message",
						from
						, packet
						.getStanzaFrom().toString(),
				StanzaType.normal);
		receipt.getElement().addChild(received);
		
		// 将回执写入流出队列
		results.offer(receipt);
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

	
	
	
	

}
 
