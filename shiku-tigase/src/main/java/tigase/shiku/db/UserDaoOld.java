package tigase.shiku.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;

import tigase.shiku.conf.ShikuConfigBean;
import tigase.shiku.utils.Callback;
import tigase.shiku.utils.ThreadUtil;
import tigase.xmpp.BareJID;
import tigase.xmpp.XMPPResourceConnection;

@Deprecated
public class UserDaoOld {

	private static final Logger log = Logger.getLogger(UserDaoOld.class
			.getName());
	private static UserDaoOld instance = new UserDaoOld();
	
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
	
	public static final List<String> RESOURCES=Arrays.asList("ios","android");
	
	
	
	public static UserDaoOld getInstance() {
		return instance;
	}

	
	private  MongoClient mongoClient;
	private MongoClientURI apiUri;
	
	private  MongoClient tigaseClient;
	
	private  MongoClientURI tigaseUri;
	
	
	
	
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
			System.out.println("UserDao.handleLogin == userId ====>"+userIdStr+" resource "+conn.getResource());
			
			final long userId=Long.valueOf(userIdStr);
			if(0==userId){
				System.out.println("UserDao.handleLogin ====> userId  is zero ");
				return;
			}else if (null==conn.getResource()||"".equals(conn.getResource())) {
				System.out.println("UserDao.handleLogin ====> getResource  is null ");
				return ;
			}
			final String resource=conn.getResource();
			
			
				
			long cuTime=System.currentTimeMillis() / 1000;
			ThreadUtil.executeInThread(new Callback() {
				@Override
				public void execute(Object obj) {
					DBObject query = new BasicDBObject("_id", userId);
					DBObject o = new BasicDBObject("$set", new BasicDBObject("onlinestate", 1));
					getCollection(USER).update(query, o); 
					
					if(!RESOURCES.contains(resource)){
						return;
					}
					BasicDBObject userLogin = (BasicDBObject) getCollection(USERLOGINLOG).findOne(query);
					BasicDBObject loginValues=null;
					BasicDBObject deviceMapObj=null;
					BasicDBObject deviceObj=null;
					if(null==userLogin){
						loginValues=new BasicDBObject("_id", userId);
						loginValues.append("loginLog", null);
						deviceMapObj=initDeviceMap(resource, cuTime);
						loginValues.append("deviceMap", deviceMapObj);
						getCollection(USERLOGINLOG).update(query, new BasicDBObject("$set", loginValues),true,false);
						return;
					}
					
					 deviceMapObj= (BasicDBObject) userLogin.get("deviceMap");
					if(null==deviceMapObj){
						loginValues=new BasicDBObject("_id", userId);
						loginValues.append("loginLog", new BasicDBObject().append("loginTime", cuTime));
						deviceMapObj=initDeviceMap(resource, cuTime);
						loginValues.append("deviceMap", deviceMapObj);
						getCollection(USERLOGINLOG).update(query, new BasicDBObject("$set", loginValues),true,false);
						return;
					}
					if(null==deviceMapObj.get(resource)){
						deviceMapObj.put(resource, initDeviceObj(resource, cuTime));
					}else {
						deviceObj=(BasicDBObject) deviceMapObj.get(resource);
						deviceObj.put("online", 1);
						deviceObj.put("loginTime", cuTime);
						deviceMapObj.replace(resource, deviceObj);
					}
					loginValues=new BasicDBObject("deviceMap", deviceMapObj);
					getCollection(USERLOGINLOG).update(query, new BasicDBObject("$set", loginValues));	
				}
			});
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
			
			if(null==userIdStr||"".equals(userIdStr))
				userIdStr=conn.getUserId().getLocalpart();
			else {
				userIdStr=getUserIdStr(userIdStr);
			}
			System.out.println(" UserDao.closeConnection =====> userId > "+userIdStr+" resource > "+conn.getResource());
			final long userId=Long.valueOf(userIdStr);
			if(0==userId){
				System.out.println("UserDao.closeConnection ====> userId  is zero ");
				return;
			}else if (null==conn.getResource()||"".equals(conn.getResource())) {
				System.out.println("UserDao.closeConnection ====> getResource  is null ");
				return ;
			}
			
			final String resource=conn.getResource();
			long cuTime=System.currentTimeMillis() / 1000;
			ThreadUtil.executeInThread(new Callback() {
				
				@Override
				public void execute(Object obj) {
					DBObject query= new BasicDBObject("_id", userId);
					
					DBObject o = new BasicDBObject("$set", new BasicDBObject("onlinestate", 0));
					getCollection(USER).update(query, o);
					
					if(!RESOURCES.contains(resource)){
						return;
					}
					BasicDBObject userLogin = (BasicDBObject) getCollection(USERLOGINLOG).findOne(query);
					BasicDBObject loginValues=null;
					BasicDBObject deviceMapObj=null;
					BasicDBObject deviceObj=null;
					if(null==userLogin){
						return;
					}
					
					deviceMapObj= (BasicDBObject) userLogin.get("deviceMap");
					if(null==deviceMapObj){
						return;
					}
					if(null==deviceMapObj.get(resource)){
						return;
					}else {
						deviceObj=(BasicDBObject) deviceMapObj.get(resource);
						deviceObj.replace("online", 0);
						deviceObj.replace("loginTime", cuTime);
					}
					loginValues=new BasicDBObject("deviceMap", deviceMapObj);
					getCollection(USERLOGINLOG).update(query, new BasicDBObject("$set", loginValues));
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	
	
	/**
	* @Description: TODO(初始化设备列表)
	* @param @param resource
	* @param @param time
	* @param @return    参数
	 */
	private BasicDBObject initDeviceMap(String resource,long time){
		BasicDBObject deviceMapObj=new BasicDBObject();
		BasicDBObject deviceObj=initDeviceObj(resource, time);
		deviceMapObj.put(resource, deviceObj);
		return deviceMapObj;
	}
	/**
	* @Description: TODO(初始化设备对象)
	* @param @param resource
	* @param @param time
	* @param @return    参数
	 */
	private BasicDBObject initDeviceObj(String resource,long time){
		
		BasicDBObject deviceObj=new BasicDBObject();
			deviceObj.put("loginTime", time);
			deviceObj.put("online", 1);
			deviceObj.put("deviceKey", resource);
		return deviceObj; 	
		
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
		DBObject query = new BasicDBObject("userId", userId);
		query.put("toUserId", toUserId);
		DBCollection collection=getCollection(NEWFRIENDS);
		DBObject obj = collection.findOne(query);
		DBObject dbObj=new BasicDBObject();
		
		long modifyTime=System.currentTimeMillis()/1000;
		if(null==content)
			content="";
		if(500==type)
			dbObj.put("from", from);
		
		if(null==obj){
			dbObj.put("userId", userId);
			dbObj.put("toUserId", toUserId);
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
	
	public int getUserXmppVersion(String jid){
		int xmppVersion=0;
		try {
			jid=jid.split("/")[0];
			BasicDBObject query=new BasicDBObject("user_id",jid);
			
			BasicDBObject result =(BasicDBObject) getTigUserDB().findOne(query);
			if(null==result)
				return xmppVersion;
			else {
				xmppVersion=null==result.get("xmppVersion")?0:result.getInt("xmppVersion");
			}
			//System.out.println("getUserXmppVersion =xmmppVersion== "+result);
			return xmppVersion;
		} catch (Exception e) {
			return xmppVersion;
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
			System.out.println(e.getMessage());
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
			System.out.println(e.getMessage());
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
			System.out.println(e.getMessage());
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
