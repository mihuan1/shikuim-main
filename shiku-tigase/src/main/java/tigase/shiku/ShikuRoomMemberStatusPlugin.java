package tigase.shiku;

import java.util.Map;
import java.util.Queue;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tigase.db.NonAuthUserRepository;
import tigase.server.Packet;
import tigase.shiku.conf.ShikuConfigBean;
import tigase.util.JIDUtils;
import tigase.xml.Element;
import tigase.xmpp.BareJID;
import tigase.xmpp.JID;
import tigase.xmpp.StanzaType;
import tigase.xmpp.XMPPPostprocessorIfc;
import tigase.xmpp.XMPPResourceConnection;
import tigase.xmpp.impl.annotation.AnnotatedXMPPProcessor;
import tigase.xmpp.impl.annotation.HandleStanzaTypes;
import tigase.xmpp.impl.annotation.Id;

/**
 * 群成员 在线状态插件
 * @author lidaye
 *
 */


@Deprecated
@HandleStanzaTypes({StanzaType.groupchat})
@Id("member-status-plugin")
public class ShikuRoomMemberStatusPlugin extends AnnotatedXMPPProcessor implements XMPPPostprocessorIfc {
	private  Logger logger = LoggerFactory.getLogger(ShikuRoomMemberStatusPlugin.class.getName());
	

	private static final String MESSAGE = "presence";
	private static final String[] ELEMENT_PATHS =  { MESSAGE };
	
	private static final String joinXml =  "http://jabber.org/protocol/muc";
	
	/**
	 * http://jabber.org/protocol/muc
	 * http://jabber.org/protocol/muc#user
	 */
	private static final String mucUserXml =  "http://jabber.org/protocol/muc#user";
	
	private static final StanzaType unavailable=StanzaType.unavailable;
	

	

	
	@Override
	public void postProcess(Packet packet, XMPPResourceConnection session, NonAuthUserRepository repo,
			Queue<Packet> results, Map<String, Object> settings) {
		try {
			
			if(MESSAGE!=packet.getElement().getName())
				return;
			Element x=packet.getElement().getChildStaticStr("x");
			if(null==x)
				return;
			String xml=x.getXMLNS();
			
			//StanzaType type = packet.getType();
			if(!mucUserXml.equals(xml)) {
				return;
			}
			Element item=x.getChildStaticStr("item");
			if(null==item)
				return;
			Element status = x.getChildStaticStr("status");
			if(null==status)
				return;
			String role = item.getAttributeStaticStr("role");
			int online=1;
			if("none".equals(role)) {
				online=0;
			}
			System.out.println(packet.getElement().toString());
			JID receiver_jid=packet.getStanzaTo();
			String toUserJid=receiver_jid.toString();
			String resource = JIDUtils.getNodeResource(toUserJid);
			if(!"android".equals(resource)&&!"ios".equals(resource)) {
				return;
				/**
				 * 不是手机设备丢弃消息		
				 */
			}
			
			
			BareJID sender_jid=packet.getStanzaFrom().getBareJID();
			String jid=getRoomJid(sender_jid.toString());
			
			Long userId=getUserId(toUserJid);
			//log.info(packet.getElement().toString());
			String msg=new StringBuffer().append(jid).append(":")
					.append(userId).append(":").append(online).toString();
			System.out.println("member-status-plugin > "+msg);
			
			org.apache.rocketmq.common.message.Message message=
					new org.apache.rocketmq.common.message.Message("memberStatusMessage",msg.getBytes("utf-8"));
			getChatProducer().sendOneway(message);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	private DefaultMQProducer producer;
	
	public DefaultMQProducer getChatProducer() {
		if(null!=producer)
			return producer;
		
			try {
				producer=new DefaultMQProducer("memberStatusProducer");
				producer.setNamesrvAddr(ShikuConfigBean.pushMqAddrVal);
				
				producer.setCreateTopicKey("memberStatusMessage");
				producer.start();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		return producer;
	}
	private Long getUserId(String jid) {
	
		int index = jid.indexOf("@");
		jid = jid.substring(0, index);
		
		return Long.parseLong(jid);
	}
	private String getRoomJid(String jid) {
		
		int index = jid.indexOf("@");
		jid = jid.substring(0, index);

		return jid;
	}
	
	@Override
	public String[] supNamespaces() {
		return ELEMENT_PATHS;
	}
}
