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
import tigase.db.TigaseDBException;
import tigase.server.Message;
import tigase.server.Packet;
import tigase.shiku.conf.ShikuConfigBean;
import tigase.util.DNSResolver;
import tigase.xmpp.BareJID;
import tigase.xmpp.JID;
import tigase.xmpp.StanzaType;
import tigase.xmpp.XMPPException;
import tigase.xmpp.XMPPProcessor;
import tigase.xmpp.XMPPProcessorIfc;
import tigase.xmpp.XMPPResourceConnection;
import tigase.xmpp.impl.C2SDeliveryErrorProcessor;

public class ShikuMessageArchivePlugin extends XMPPProcessor implements
		XMPPProcessorIfc {
	public static final String OWNNER_JID = "ownner";

	private  Logger logger = LoggerFactory.getLogger(ShikuMessageArchivePlugin.class.getName());

	private static final String ID = "shiku-message-archive-plugin";

	private static final String MESSAGE = "message";
	private static final String[][] ELEMENT_PATHS = { { MESSAGE } };

	private static final String[] XMLNSS = { Packet.CLIENT_XMLNS };

	private static final Set<StanzaType> TYPES;
	static {
		HashSet<StanzaType> tmpTYPES = new HashSet<StanzaType>();
		tmpTYPES.add(null);
		tmpTYPES.addAll(EnumSet.of(StanzaType.groupchat, StanzaType.chat));
		TYPES = Collections.unmodifiableSet(tmpTYPES);
	}

	private JID shiku_ma_jid = null;

	public JID getShikuMa_Jid() {
		if(null!=shiku_ma_jid)
			return shiku_ma_jid;
		String componentJidStr = ShikuConfigBean.shikuArchiveJid;
		
		if (null!=componentJidStr) {
			shiku_ma_jid = JID.jidInstanceNS(componentJidStr);
		} else {
			String defHost = DNSResolver.getDefaultHostname();
			shiku_ma_jid = JID.jidInstanceNS("message-archive", defHost, null);
		}
		
		logger.info("Shiku MA LOADED =  {}",shiku_ma_jid);
		return shiku_ma_jid;
	}

	@Override
	public void process(Packet packet, XMPPResourceConnection session,
			NonAuthUserRepository repo, Queue<Packet> results,
			Map<String, Object> settings) throws XMPPException {
		
		StanzaType type = packet.getType();
		try {
			if (session == null) {
				return;
			}else if (Message.ELEM_NAME != packet.getElemName()) {
				return;
			}else if((type != StanzaType.chat)&&(type != StanzaType.groupchat)) {
				return;
			}else if(null==(packet.getElement().findChildStaticStr(
					Message.MESSAGE_BODY_PATH))) {
				return;
			}
			
			
			// ignoring packets resent from c2s for redelivery as processing
			// them would create unnecessary duplication of messages in
			// archive
			if (C2SDeliveryErrorProcessor.isDeliveryError(packet))
				return;
			
			if(type==StanzaType.groupchat&&null!=session.getjid()&&
				!packet.getStanzaFrom().getBareJID().equals(session.getjid().getBareJID()))
				return;
			BareJID ownJid=null;
			if(null!=session.getjid()) {
				ownJid=session.getjid().getBareJID();
			}
			Packet result=packet.copyElementOnly();
			result.setPacketTo(getShikuMa_Jid());
			result.getElement().addAttribute(OWNNER_JID,
					null!=ownJid?ownJid.toString():null);
			
			/*String body=packet.getElemCDataStaticStr(Message.MESSAGE_BODY_PATH);
			
			result.getElement().setAttribute("body", body);*/
			
			// 删除body以外节点
			// Element message = result.getElement();
			// for (Element elem : message.getChildren()) {
			// switch (elem.getName()) {
			// case "body":
			// break;
			// default:
			// message.removeChild(elem);
			// }
			// }
			results.offer(result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void init(Map<String, Object> settings) throws TigaseDBException {
		// TODO Auto-generated method stub
		super.init(settings);
		
	
	}

	@Override
	public String id() {
		return ID;
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
