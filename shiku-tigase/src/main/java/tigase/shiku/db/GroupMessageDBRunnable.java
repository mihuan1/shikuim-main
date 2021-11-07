package tigase.shiku.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.shiku.commons.thread.pool.AbstractMapRunnable;

import tigase.shiku.conf.ShikuConfigBean;
import tigase.shiku.model.MucMessageModel;

/**
 * @author lidaye
 *
 */
public class GroupMessageDBRunnable extends AbstractMapRunnable<List<DBObject>>{

	
	private DB mucdb;
	
	private  Logger logger = LoggerFactory.getLogger(GroupMessageDBRunnable.class.getName());
	
	private static final String MUCMsg_="mucmsg_";
	
	protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	/**
	 * @param executor
	 */
	public GroupMessageDBRunnable(DB mucdb) {
		this.mucdb=mucdb;
	}

	
	public void putMessageToTask(MucMessageModel model) {
		if(1!=ShikuConfigBean.shikuSaveMucMsg) 
			return;
			
		  if(ShikuConfigBean.isDeBugMode())
			  logger.info("  storeGroupMessageChat  {}",model.getBody());
			 
			BasicDBObject dbObj = new BasicDBObject();
			dbObj.put("body", model.getBody());
			dbObj.put("event_type", model.getEvent_type());
			dbObj.put("message", model.getMessage());
			dbObj.put("nickname", "");
			dbObj.put("public_event", 0);
			dbObj.put("room_jid_id", model.getRoom_id());
			dbObj.put("room_jid", model.getRoom_jid());
			dbObj.put("sender_jid", model.getSender_jid());
			dbObj.put("sender", model.getSender());
			dbObj.put("ts", model.getTs());
			dbObj.put("contentType",model.getContentType());
			dbObj.put("messageId", model.getMessageId());
			dbObj.put("timeSend", model.getTimeSend());
			dbObj.put("deleteTime", model.getDeleteTime());
			if(null != model.getContent()){
				dbObj.put("content", model.getContent());
			}
			List<DBObject> list=null;
			
			try {
				lock.readLock().lock();
				list = maps.get(model.getRoom_id());
				if(null==list) {
					list=Collections.synchronizedList(new LinkedList<DBObject>());
					synchronized (maps) {
						maps.put(model.getRoom_id(), list);
					}
				}
				list.add(dbObj);
			}catch (Exception e) {
				logger.error(e.getMessage());
			} finally {
				lock.readLock().unlock();
			}
				
			/*if(!mucdb.collectionExists(MUCMsg_+model.getRoom_id()))
				mucdb.createCollection(MUCMsg_+model.getRoom_id(), new BasicDBObject());
			mucdb.getCollection(MUCMsg_+model.getRoom_id()).insert(dbObj);*/
			
			
		
	}

	/* (non-Javadoc)
	 * @see tigase.shiku.pool.AbstractSynRunnable#runTask()
	 */
	@Override
	public void runTask() {
		Iterator<Entry<String, List<DBObject>>> iterator=null;
		List<DBObject> list=null;
		try {
			while (!maps.isEmpty()) {
				iterator = maps.entrySet().iterator();
				while (iterator.hasNext()) {
					try {
						Entry<String,List<DBObject>> entry = iterator.next();
						lock.writeLock().lock();
						list=entry.getValue();
						if(!list.isEmpty()) {
							mucdb.getCollection(MUCMsg_+entry.getKey()).insert(new ArrayList<>(list));
							list.clear();
						}
						else {
							iterator.remove();
						}
					} catch (Exception e) {
						logger.error(e.toString(), e);
					}finally {
						lock.writeLock().unlock();
						Thread.sleep(500);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}finally {
			
		}
		
	}

}
