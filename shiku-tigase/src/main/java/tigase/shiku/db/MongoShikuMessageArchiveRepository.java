package tigase.shiku.db;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import tigase.db.DBInitException;
import tigase.shiku.ShikuMessageArchiveComponent;
import tigase.shiku.conf.ShikuConfigBean;
import tigase.shiku.model.LastChatModel;
import tigase.shiku.model.MessageModel;
import tigase.shiku.model.MucMessageModel;

public class MongoShikuMessageArchiveRepository implements
		ShikuMessageArchiveRepository {

	
	
	private  Logger logger = LoggerFactory.getLogger(MongoShikuMessageArchiveRepository.class.getName());
	// private static final String HASH_ALG = "SHA-256";
	
	//mucMsg_
	private static final String MUC_MSGS_COLLECTION = "shiku_muc_msgs";
	private static final String MSGS_COLLECTION = "shiku_msgs";
	private static final String LASTCHAT_COLLECTION = "shiku_lastChats";
	private static final String MUCMsg_="mucmsg_";
	
//	private static final String ROOM_MEMBER="shiku_room_member";
//	private static final String SHIKU_ROOM="shiku_room";
	private MongoClient mongo;
	private DB db;
	private DB mucdb;
	
	// private byte[] generateId(BareJID user) throws TigaseDBException {
	// try {
	// MessageDigest md = MessageDigest.getInstance(HASH_ALG);
	// return md.digest(user.toString().getBytes());
	// } catch (NoSuchAlgorithmException ex) {
	// throw new TigaseDBException("Should not happen!!", ex);
	// }
	// }

	private static MongoShikuMessageArchiveRepository instance = new MongoShikuMessageArchiveRepository();

	public static MongoShikuMessageArchiveRepository getInstance() {
		return instance;
	}
	
	private ChatMessageDBRunnable chatMessageRunnable;
	
	private GroupMessageDBRunnable groupMessageRunnable;
	
	private LastMessageDBRunnable lastMessageDBRunnable;
	
	@Override
	public void initRepository(String resource_uri, Map<String, String> params)
			throws DBInitException {
		String mucRpoUriStr=null;
		try {
			MongoClientURI uri = new MongoClientURI(resource_uri);
			mongo = new MongoClient(uri);
			db = mongo.getDB(uri.getDatabase());
			
			 mucRpoUriStr=params.get(ShikuMessageArchiveComponent.MUC_REPO_URI);
			 if(null!=mucRpoUriStr) {
				 try {
						MongoClientURI mucUri = new MongoClientURI(mucRpoUriStr);
						mucdb = new MongoClient(mucUri).getDB(mucUri.getDatabase());
					} catch (Exception e) {
						throw new DBInitException(
								"Could not connect to MongoDB server using URI = "
										+ mucRpoUriStr, e);
					}
					
			 }else {
				 mucdb = mongo.getDB("imRoom");
			 }
			

			//初始化群组聊天记录集合
			DBCollection msgCollection = null;
			
			// 初始化聊天记录集合
			msgCollection = !db.collectionExists(MSGS_COLLECTION) ? db
					.createCollection(MSGS_COLLECTION, new BasicDBObject())
					: db.getCollection(MSGS_COLLECTION);
					msgCollection.createIndex(new BasicDBObject("sender", 1));
					msgCollection.createIndex(new BasicDBObject("receiver", 1));
					msgCollection.createIndex(new BasicDBObject("sender", 1).append(
					"receiver", 1));
					msgCollection.createIndex(new BasicDBObject("sender", 1).append(
					"receiver", 1).append("ts", 1));
					BasicDBObject index = new BasicDBObject("timeSend", 1);
					index.append("messageId", 1);
					msgCollection.createIndex(index);
					
					DBCollection lastMsgCollection = !db.collectionExists(LASTCHAT_COLLECTION) ? db
					.createCollection(LASTCHAT_COLLECTION, new BasicDBObject())
					: db.getCollection(LASTCHAT_COLLECTION);
					
					lastMsgCollection.createIndex(new BasicDBObject("userId", 1));
					lastMsgCollection.createIndex(new BasicDBObject("userId", 1).append("jid", 1));
					
			chatMessageRunnable=new ChatMessageDBRunnable(msgCollection);
			
			groupMessageRunnable=new GroupMessageDBRunnable(mucdb);

			lastMessageDBRunnable=new LastMessageDBRunnable(lastMsgCollection);
			chatMessageRunnable.setSleep(3000);
			groupMessageRunnable.setSleep(3000);
			lastMessageDBRunnable.setSleep(3000);
			
			new Thread(chatMessageRunnable).start();
			new Thread(lastMessageDBRunnable).start();
			new Thread(groupMessageRunnable).start();
		} catch (Exception ex) {
			throw new DBInitException(
					"Could not connect to MongoDB server using URI = "
							+ resource_uri, ex);
		}
	}

	
	
	@Override
	public void archiveMessage(MessageModel model) {
		if(1==ShikuConfigBean.shikuSaveMsg) {
			BasicDBObject dbObj = new BasicDBObject(14);
			dbObj.put("body", model.getBody());
			dbObj.put("direction", model.getDirection());
			dbObj.put("message", model.getMessage());
			dbObj.put("sender_jid", model.getSender_jid());
			
			dbObj.put("receiver_jid", model.getReceiver_jid());
			dbObj.put("ts", model.getTs());
			dbObj.put("type", model.getType());
			dbObj.put("contentType", model.getContentType());
			dbObj.put("messageId", model.getMessageId());
			dbObj.put("timeSend", model.getTimeSend());
			dbObj.put("deleteTime", model.getDeleteTime());
			if(0==model.getDirection()){
				dbObj.put("sender", model.getSender());
				dbObj.put("receiver", model.getReceiver());
				dbObj.put("isRead", 0);
			}else{
				dbObj.put("sender", model.getReceiver());
				dbObj.put("receiver", model.getSender());
				dbObj.put("isRead", 1);
			}
			if(null != model.getContent()){
				dbObj.put("content", model.getContent());
			}
			if(ShikuConfigBean.isDeBugMode()&&0==model.getDirection()){
				logger.info("  storeMessageChat  {}",model.getBody());
			}
			chatMessageRunnable.addMsg(dbObj);
			
		}
		if(1==model.getDirection())
			return;
		LastChatModel lastChat=new LastChatModel();
		lastChat.setMessageId(model.getMessageId());
		lastChat.setContent(model.getContent());
		lastChat.setUserId(model.getSender().toString());
		lastChat.setJid(model.getReceiver().toString());
		lastChat.setIsRoom(0);
		lastChat.setType(model.getContentType());
		lastChat.setTimeSend(model.getTimeSend().longValue());
		lastChat.setIsEncrypt(model.getIsEncrypt());
		lastChat.setBody(model.getBody());// body
	
		refreshLastChat(lastChat);
		
	}
	@Override
	public void updateMsgReadStatus(String msgId) {
		if(null==msgId)
			return;
		BasicDBObject query = new BasicDBObject("messageId",msgId);
		db.getCollection(MSGS_COLLECTION).update(query, new BasicDBObject("$set",new BasicDBObject("isRead", 1)));
		
	}

	@Override
	public void archiveMessage(MucMessageModel model) {
		try {
			//imroom
			//mucMsg_adfsdfdsfds
			groupMessageRunnable.putMessageToTask(model);
			
			LastChatModel lastChat=new LastChatModel();
			lastChat.setMessageId(model.getMessageId());
			lastChat.setContent(model.getContent());
			lastChat.setUserId(model.getSender().toString());
			lastChat.setJid(model.getRoom_id());
			lastChat.setIsRoom(1);
			lastChat.setType(model.getContentType());
			lastChat.setTimeSend(model.getTimeSend().longValue());
			lastChat.setIsEncrypt(model.getIsEncrypt());
			lastChat.setBody(model.getBody());// room Body
			refreshLastChat(lastChat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void refreshLastChat(LastChatModel model) {
		lastMessageDBRunnable.putLastChat(model);
		
	}
	


}
