package tigase.shiku.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.ServiceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;

import tigase.shiku.conf.ShikuConfigBean;
import tigase.shiku.utils.Callback;
import tigase.shiku.utils.StringUtils;
import tigase.shiku.utils.ThreadUtil;
import tigase.xmpp.BareJID;
import tigase.xmpp.JID;
import tigase.xmpp.XMPPResourceConnection;


public class UserDao {

	private static Logger logger = LoggerFactory.getLogger(UserDao.class.getName());
	private static UserDao instance = new UserDao();
	
	/**
	 * 用户表
	 */
	private static final String USER = "user";
	/**
	 * 用户登陆记录表
	 */
	private static final String USERLOGINLOG = "userLoginLog";
	/**
	 * 新朋友表
	 */
	private static final String NEWFRIENDS = "NewFriends";
	/**
	 * 好友表
	 */
	private static final String FRIENDS = "u_friends";
	/**
	 * 敏感词表
	 */
	private static final String NOTKEYWORD="keyWord";
	/**
	 * 配置表
	 * @return
	 */
	private static final String CONFIG="config";
	
	public static final List<String> RESOURCES=Arrays.asList("ios","android","youjob");
	
	
	
	public static UserDao getInstance() {
		return instance;
	}

	
	private  MongoClient mongoClient;
	private MongoClientURI apiUri;
	
	private  MongoClient tigaseClient;
	
	private  MongoClientURI tigaseUri;
	
	
	private DefaultMQProducer producer;
	
	public DefaultMQProducer getProducer() {
		if(null!=producer)
			return producer;
		startProducer();
		return producer;
	}
	public  synchronized void startProducer(){
		try {

			logger.info("UserDao init Producer {}",ShikuConfigBean.pushMqAddrVal);

			producer=new DefaultMQProducer("userStatusProducer");
			producer.setNamesrvAddr(ShikuConfigBean.pushMqAddrVal);
			producer.setVipChannelEnabled(false);
			producer.setCreateTopicKey("userStatusMessage");
			producer.start();

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	public void restartProducer() {
		logger.info("UserDao restartProducer ===》 {}",ShikuConfigBean.pushMqAddrVal);
		try {
			if(null!=producer&&null!=producer.getDefaultMQProducerImpl()) {
				if(ServiceState.CREATE_JUST==producer.getDefaultMQProducerImpl().getServiceState()) {
					try {
						producer.start();
					} catch (Exception e) {
						producer=null;
						startProducer();
					}
				}
			}else {
				producer=null;
				startProducer();
			}
		} catch (Exception e) {
			logger.error("restartProducer Exception {}",e.getMessage());
			
		}	
		
	}
	
	/**
	 * SessionManager.handleResourceBind 调用过来的
	 * 
	* @Description: TODO(用户 登陆xmpp 上线时  更新 用户的数据库状态信息 )
	* @param @param conn    xmpp  链接
	 */
	public void handleLogin(final XMPPResourceConnection conn){
		String userIdStr=null;
		try {
			
			userIdStr=conn.getUserId().getLocalpart();
			logger.info("UserDao.handleLogin == userId ====>  {} resource {} ",userIdStr,conn.getResource());
			 long userId=Long.valueOf(userIdStr);
			if(0==userId){
				/*if(ShikuConfigBean.isDeBugMode()) {
					System.out.println("UserDao.handleLogin ====> userId  is zero ");
				}*/
				return;
			}else if (null==conn.getResource()||"".equals(conn.getResource())) {
				logger.info("UserDao.handleLogin ====> getResource  is null ");
				return ;
			}
			
				 String msg=new StringBuffer().append(userId).append(":")
							.append(1).append(":").append(conn.getResource()).toString();
					org.apache.rocketmq.common.message.Message message=
							new org.apache.rocketmq.common.message.Message("userStatusMessage",msg.getBytes("utf-8"));
			try {	
				SendResult result = getProducer().send(message);
				if(SendStatus.SEND_OK!=result.getSendStatus()){
					logger.error(result.toString());
				}
			} catch (Exception e) {
				logger.error("handleLogin send userStatusMessage Exception {}",e.getMessage());
				restartProducer();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	/**
	* @Description: TODO(关闭 用户 xmpp 链接 调用的  修改用户 状态)
	* @param @param connection
	* @param @param userIdStr    参数
	 */
	public void closeConnection(final XMPPResourceConnection conn,String userIdStr) {
		try {
			if(null==conn) {
				/*if(ShikuConfigBean.isDeBugMode())
					System.out.println(" UserDao.closeConnection =====> conn >  is null ");*/
				return;
			}
			try {
				JID[] allResourcesJIDs = conn.getAllResourcesJIDs();
				if(null!=allResourcesJIDs&&0<allResourcesJIDs.length) {
					logger.info("closeConnection 账号还有 设备在线  ===>  {}",StringUtils.join(allResourcesJIDs, ","));
					return;
				}
				if(null==userIdStr||"".equals(userIdStr))
					userIdStr=null!=conn.getjid()?conn.getjid().getLocalpart():"0";
				else {
					userIdStr=getUserIdStr(userIdStr);
				}
			} catch (Exception e) {
				logger.error("closeConnection error {}",e.getMessage());
				return;
			}
			final long userId=Long.valueOf(userIdStr);
			if(0==userId){
				/*if(ShikuConfigBean.isDeBugMode()) {
					System.out.println("UserDao.closeConnection ====> userId  is zero ");
				}*/
				return;
			}else if (null==conn.getResource()||"".equals(conn.getResource())) {
				/*if(ShikuConfigBean.isDeBugMode())
					System.out.println("UserDao.closeConnection ====> getResource  is null ");*/
				return ;
			}
			logger.info(" UserDao.closeConnection =====> userId > {} resource {}",userIdStr,conn.getResource());
			

				 String msg=new StringBuffer().append(userId).append(":")
							.append(0).append(":").append(conn.getResource()).toString();
					org.apache.rocketmq.common.message.Message message=
							new org.apache.rocketmq.common.message.Message("userStatusMessage",msg.getBytes("utf-8"));
			try {
				SendResult result = getProducer().send(message);
				if(SendStatus.SEND_OK!=result.getSendStatus()){
					logger.error(result.toString());
				}
			} catch (Exception e) {
				logger.error("closeConnection send userStatusMessage Exception {}",e.getMessage());
				restartProducer();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	* @Description: TODO(加 新好友  消息)
	* @param @param userId
	* @param @param toUserId
	* @param @param direction
	* @param @param type
	* @param @param content    参数
	 */
	public void saveNewFriendsInThread(final long userId,final long toUserId,final Object from, final int direction,final int type,final String content){
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				saveNewFriends(userId, toUserId,from, direction,type, content);
			}
		});
		
	}
	public void saveNewFriends(long userId, long toUserId,Object from,int direction, int type, String content){
		DBObject query = new BasicDBObject();
		if(0 == direction){
			query.put("userId", userId);
			query.put("toUserId", toUserId);
		}else{
			query.put("userId", toUserId);
			query.put("toUserId", userId);
		}

		DBCollection collection=getCollection(NEWFRIENDS);
		DBObject obj = collection.findOne(query);
		DBObject dbObj=new BasicDBObject();
		
		long modifyTime=System.currentTimeMillis()/1000;
		if(null==content)
			content="";
		if(500==type)
			dbObj.put("from", from);
		if(null==obj){
			if(0 == direction){
				dbObj.put("userId", userId);
				dbObj.put("toUserId", toUserId);
			}else{
				dbObj.put("userId", toUserId);
				dbObj.put("toUserId", userId);
			}
			dbObj.put("direction", direction);
			dbObj.put("createTime", modifyTime);
			dbObj.put("modifyTime", modifyTime);
			dbObj.put("type", type);
			dbObj.put("content", content);
			collection.insert(dbObj);
		}else{
			dbObj.put("content", content);
			dbObj.put("direction", direction);
			dbObj.put("type", type);
			dbObj.put("content", content);
			dbObj.put("modifyTime", modifyTime);
			collection.update(query, new BasicDBObject("$set", dbObj));
		}
	}
	
	
	
	public boolean getKeyWord(String keyword) {
		DBCollection dbCollection = getCollection(NOTKEYWORD);
			BasicDBObject query = new BasicDBObject();
			query.put("word",new BasicDBObject("$regex", keyword)); //妯＄硦鏌ヨ
			long count = dbCollection.count(query);
		return count>0;
	}
	/**
	* @Description: TODO(启动服务的时候 查询 数据库所有的 敏感词)
	* @param @return    参数
	 */
	public List<String> getAllKeyWord() {
		DBCollection dbCollection = getCollection(NOTKEYWORD);
			DBCursor cursor = dbCollection.find();
			DBObject dbObj=null;
			List<String> keyWords=new ArrayList<String>();
			while(cursor.hasNext()){
				dbObj=cursor.next();
				if(null==dbObj)
					continue;
				keyWords.add(dbObj.get("word").toString());
			}
		return keyWords;
	}
	
	/**
	 * @Description: TODO(启动服务的时候 查询 数据库中的配置表)
	 * @return
	 */
	public DBObject getConfig(){
		BasicDBObject keys = new BasicDBObject();
		keys.put("XMPPTimeout", 1);
		keys.put("isSaveMsg", 1);
		keys.put("isSaveMucMsg", 1);
		keys.put("isMsgSendTime", 1);
		keys.put("isKeyWord", 1);
		DBCollection dbCollection = getCollection(CONFIG);
		DBObject obj = dbCollection.findOne(new BasicDBObject(), keys);
		return obj;
	}
	
	//保存最后沟通时间
	public void saveLastTalk(long sender, long receiver){
		DBObject query = new BasicDBObject("userId", receiver);
		query.put("toUserId", sender);
		DBCollection collection=getCollection(FRIENDS);
		DBObject values=new BasicDBObject();
		
		long modifyTime=System.currentTimeMillis()/1000;
		
		values.put("$set", new BasicDBObject("lastTalkTime", modifyTime));
		
		//values.put("$inc", new BasicDBObject("msgNum", 1));
		
		collection.update(query,values);
		
	}
	
	/**
	* @Description: TODO(获取用户ID )
	* @param @param jid 10005629@192.168.0.168
	* @param @return    10005629
	 */
	private long getUserId(BareJID jid) {
		try {
			//得到账号ID
			String strUserId = jid.toString();
			int index = strUserId.indexOf("@");
			strUserId = strUserId.substring(0, index);

			return Long.parseLong(strUserId);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return 0;
	}
	/**
	* @Description: TODO(获取用户ID )
	* @param @param jid 10005629@192.168.0.168/android
	* @param @return    10005629
	 */
	private long getUserId(String jid) {
		try {
			
			int index = jid.indexOf("@");
			jid = jid.substring(0, index);

			return Long.parseLong(jid);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return 0;
	}
	/**
	* @Description: TODO(获取字符串的用户ID )
	* @param @param jid
	* @param @return    “0005629”
	 */
	private String getUserIdStr(String jid) {
		try {
			
			int index = jid.indexOf("@");
			jid = jid.substring(0, index);

			return jid;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	
	

	private  MongoClient getMongoClient() {
		try {
			if(null!=mongoClient)
				return mongoClient;
			else{
				System.out.println("mongoClient  is ---null");
				 MongoClientOptions.Builder builder = MongoClientOptions.builder();
				 builder.socketKeepAlive(true);
				 builder.socketTimeout(20000);
				 builder.connectTimeout(20000);
				builder.maxWaitTime(12000000);
				builder.heartbeatFrequency(2000);// 心跳频率
				apiUri = new MongoClientURI(ShikuConfigBean.APIDBURI_VAL,builder);
				//uri.get
				mongoClient = new MongoClient(apiUri);
				return mongoClient;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	
	private  MongoClient getTigaseClient() {
		try {
			if(null!=tigaseClient)
				return tigaseClient;
			else{
				System.out.println("tigaseClient  is ---null");
				 MongoClientOptions.Builder builder = MongoClientOptions.builder();
				 builder.socketKeepAlive(true);
				 builder.socketTimeout(20000);
				 builder.connectTimeout(20000);
				builder.maxWaitTime(12000000);
				builder.heartbeatFrequency(2000);// 心跳频率
				tigaseUri = new MongoClientURI(ShikuConfigBean.USER_DB_URI,builder);
				//uri.get
				tigaseClient = new MongoClient(tigaseUri);
				return tigaseClient;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	private DBCollection getTigUserDB(){
		DBCollection dbCollection=null;
		if(null!=tigaseClient)
			 dbCollection=tigaseClient.getDB(tigaseUri.getDatabase()).getCollection("tig_users");
			else 
				dbCollection=getTigaseClient().getDB(tigaseUri.getDatabase()).getCollection("tig_users");
			return dbCollection;
		
	}
	
	
	
	private DBCollection getCollection(String dbName){
		DBCollection dbCollection=null;
		if(null!=mongoClient)
			 dbCollection=mongoClient.getDB(apiUri.getDatabase()).getCollection(dbName);
			else 
				dbCollection=getMongoClient().getDB(apiUri.getDatabase()).getCollection(dbName);
			return dbCollection;
		
	}
	

}
