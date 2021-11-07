package tigase.shiku.db;

import java.util.Date;

import com.mongodb.DBObject;

import tigase.mongodb.muc.MongoHistoryProvider;
import tigase.server.Packet;
import tigase.util.TigaseStringprepException;
import tigase.xmpp.BareJID;
import tigase.xmpp.JID;

/*public class ShikuMongoHistoryProvider extends MongoHistoryProvider {

	public ShikuMongoHistoryProvider() {
		System.out.println(" ShikuMongoHistoryProvider init =========>");
	}
	
	
	
	
	private Packet createMessage(BareJID roomJid, JID senderJID, DBObject dto, boolean addRealJids) throws TigaseStringprepException {
		String sender_nickname = (String) dto.get("sender_nickname");
		String msg = (String) dto.get("msg");
		String body = (String) dto.get("body");
		String sender_jid = (String) dto.get("sender_jid");
		Date timestamp = (Date) dto.get("timestamp");
		
		return createMessage(roomJid, senderJID, sender_nickname, msg, body, sender_jid, addRealJids, timestamp);
	}
}*/
