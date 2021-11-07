package tigase.shiku.db;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.shiku.commons.thread.pool.AbstractQueueRunnable;

/**
 * @author lidaye
 *
 */
public class ChatMessageDBRunnable extends AbstractQueueRunnable<DBObject>{

	private DBCollection dbCollection;
	
	private static Logger log = LoggerFactory.getLogger(ChatMessageDBRunnable.class.getName());
	/**
	 * @param executor
	 */
	public ChatMessageDBRunnable(DBCollection dbCollection) {
		this.dbCollection=dbCollection;
	}

	
	
	/* (non-Javadoc)
	 * @see tigase.shiku.pool.AbstractSynRunnable#runTask()
	 */
	@Override
	public void runTask() {
		List<DBObject> list=null;
		try {
			try {
				synchronized (msgQueue) {
					list =msgQueue.stream().collect(Collectors.toList());
					msgQueue.clear();
				}
				if(!list.isEmpty())
					dbCollection.insert(list);
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		} catch (Exception e) {
			log.error(e.toString(), e);
		}finally {
			list=null;
		}
		
	}

}
