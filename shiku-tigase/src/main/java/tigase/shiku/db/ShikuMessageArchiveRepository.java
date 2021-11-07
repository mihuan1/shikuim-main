package tigase.shiku.db;

import tigase.db.Repository;
import tigase.shiku.model.LastChatModel;
import tigase.shiku.model.MessageModel;
import tigase.shiku.model.MucMessageModel;

public interface ShikuMessageArchiveRepository extends Repository {

	void archiveMessage(MessageModel model);

	void archiveMessage(MucMessageModel model);
	
	void updateMsgReadStatus(String msgId);
	
	void refreshLastChat(LastChatModel model);
		
	
}
