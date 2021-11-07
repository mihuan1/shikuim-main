package cn.xyz.mianshi.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;

/**
 * Tigase 相关的管理类
 * @author root
 *
 */

@Service(TigaseManagerImpl.BEAN_ID)
public class TigaseManagerImpl extends MongoRepository<Object, ObjectId>{
	
	public static final String BEAN_ID = "TigaseManagerImpl";
	@Override
	public Datastore getDatastore() {
		// TODO Auto-generated method stub
		return SKBeanUtils.getTigaseDatastore();
	}

	@Override
	public Class<Object> getEntityClass() {
		// TODO Auto-generated method stub
		return Object.class;
	}
	  public  void updateMsgIsReadStatus(String msgId){
	    	if(null==msgId)
				return;
			BasicDBObject query = new BasicDBObject("messageId",msgId);
			SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_msgs").
			update(query, new BasicDBObject("$set",new BasicDBObject("isRead", 1)));
			
	  }
	
	public void deleteLastMsg(String userId,String jid){
		DBCollection collection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_lastChats");
		BasicDBObject query=new BasicDBObject("jid", jid);
		if(!StringUtil.isEmpty(userId))
			query.append("userId", userId);
		collection.remove(query);
	}
	
	/**
	 * 获取单聊消息数据源
	 */
	public DBCollection getMsgRepostory() { 
		DBCollection dbCollection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_msgs");
		return dbCollection;
	}
	
	
	/**
	 * 获取单聊消息数量
	 * @return
	 */
	public long getMsgCountNum() {
		BasicDBObject query = new BasicDBObject();
		return getMsgRepostory().count();
	}
	
	
	
	
	/**
	 * 单聊消息数量统计      时间单位  每日、每月、每分钟、每小时
	 * @param startDate
	 * @param endDate
	 * @param counType  统计类型   1: 每个月的数据      2:每天的数据       3.每小时数据   4.每分钟的数据 (小时)   
	 */
	public List<Object> getChatMsgCount(String startDate, String endDate, short counType){
		
		List<Object> countData = new ArrayList<>();
		
		long startTime = 0; //开始时间（秒）
		long endTime = 0; //结束时间（秒）,默认为当前时间
		
		/**
		 * 如时间单位为月和天，默认开始时间为当前时间的一年前 ; 时间单位为小时，默认开始时间为当前时间的一个月前;
		 * 时间单位为分钟，则默认开始时间为当前这一天的0点
		 */
		long defStartTime = counType==4? DateUtil.getTodayMorning().getTime()/1000 
				: counType==3 ? DateUtil.getLastMonth().getTime()/1000 : DateUtil.getLastYear().getTime()/1000;
		
		startTime = StringUtil.isEmpty(startDate) ? defStartTime :DateUtil.toDate(startDate).getTime()/1000;
		endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime()/1000;
				
		BasicDBObject queryTime = new BasicDBObject("$ne",null);
		
		if(startTime!=0 && endTime!=0){
			queryTime.append("$gt", startTime);
			queryTime.append("$lt", endTime);
		}
		
		BasicDBObject query = new BasicDBObject("timeSend",queryTime);
		//获得单聊消息集合对象
		DBCollection collection = getMsgRepostory();
		if(null==collection.findOne())
			return countData;
		//collection.find(query).sort(orderBy)
		
		String mapStr = "function Map() { "   
	            + "var date = new Date(this.timeSend*1000);" 
	            +  "var year = date.getFullYear();"
				+  "var month = (\"0\" + (date.getMonth()+1)).slice(-2);"  //month 从0开始，此处要加1
				+  "var day = (\"0\" + date.getDate()).slice(-2);"
				+  "var hour = (\"0\" + date.getHours()).slice(-2);"
				+  "var minute = (\"0\" + date.getMinutes()).slice(-2);"
				+  "var dateStr = date.getFullYear()"+"+'-'+"+"(parseInt(date.getMonth())+1)"+"+'-'+"+"date.getDate();";
				
				if(counType==1){ // counType=1: 每个月的数据
					mapStr += "var key= year + '-'+ month;";
				}else if(counType==2){ // counType=2:每天的数据
					mapStr += "var key= year + '-'+ month + '-' + day;";
				}else if(counType==3){ //counType=3 :每小时数据
					mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour +' : 00';";
				}else if(counType==4){ //counType=4 :每分钟的数据
					mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour + ':'+ minute;";
				}
	           
				mapStr += "emit(key,1);}";
		
		 String reduce = "function Reduce(key, values) {" +
			                "return Array.sum(values);" +
	                    "}";
		 MapReduceCommand.OutputType type =  MapReduceCommand.OutputType.INLINE;//
		 MapReduceCommand command = new MapReduceCommand(collection, mapStr, reduce,null, type,query);



		int i = 0;
		MapReduceOutput mapReduceOutput = null;
		while (i < 5) {
			i++;
			try {
				mapReduceOutput = collection.mapReduce(command);
				break;
			} catch (MongoSocketWriteException e) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				logger.info("retry getChatMsgCount mapReduce:{}", i);
			}
		}
		if (null == mapReduceOutput) return countData;
		 Iterable<DBObject> results = mapReduceOutput.results();
		 Map<String,Double> map = new HashMap<String,Double>();
		for (Iterator<DBObject> iterator = results.iterator(); iterator.hasNext();) {
			DBObject obj = (DBObject) iterator.next();
			 
			map.put((String)obj.get("_id"),(Double)obj.get("value"));
			countData.add(JSON.toJSON(map));
			map.clear();
		}
		return countData;
	}
	
	
	
	
	/**
	 * 群聊消息数量统计      时间单位  每日、每月、每分钟、每小时
	 * @param startDate
	 * @param endDate
	 * @param counType  统计类型   1: 每个月的数据      2:每天的数据       3.每小时数据   4.每分钟的数据 (小时)   
	 */
	public List<Object> getGroupMsgCount(String roomId,String startDate, String endDate, short counType){
		
		//获得群聊消息集合对象
		DBCollection collection = SKBeanUtils.getImRoomDatastore().getDB().getCollection("mucmsg_"+roomId);
		
		if(collection==null || collection.count()==0){
			System.out.println("暂无数据");
			throw new ServiceException("暂无数据");
		}
		
		List<Object> countData = new ArrayList<Object>();
		
		long startTime = 0; //开始时间（秒）
		
		long endTime = 0; //结束时间（秒）,默认为当前时间
		
		/**
		 * 如时间单位为月和天，默认开始时间为当前时间的一年前 ; 时间单位为小时，默认开始时间为当前时间的一个月前;
		 * 时间单位为分钟，则默认开始时间为当前这一天的0点
		 */
		long defStartTime = counType==4? DateUtil.getTodayMorning().getTime()/1000 
				: counType==3 ? DateUtil.getLastMonth().getTime()/1000 : DateUtil.getLastYear().getTime()/1000;
		
		startTime = StringUtil.isEmpty(startDate) ? defStartTime :DateUtil.toDate(startDate).getTime()/1000;
		endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime()/1000;
				
		BasicDBObject queryTime = new BasicDBObject("$ne",null);
		
		if(startTime!=0 && endTime!=0){
			queryTime.append("$gt", startTime);
			queryTime.append("$lt", endTime);
		}
		
		BasicDBObject query = new BasicDBObject("timeSend",queryTime);
		
		
		
		String mapStr = "function Map() { "   
	            + "var date = new Date(this.timeSend*1000);" 
	            +  "var year = date.getFullYear();"
				+  "var month = (\"0\" + (date.getMonth()+1)).slice(-2);"  //month 从0开始，此处要加1
				+  "var day = (\"0\" + date.getDate()).slice(-2);"
				+  "var hour = (\"0\" + date.getHours()).slice(-2);"
				+  "var minute = (\"0\" + date.getMinutes()).slice(-2);"
				+  "var dateStr = date.getFullYear()"+"+'-'+"+"(parseInt(date.getMonth())+1)"+"+'-'+"+"date.getDate();";
				
				if(counType==1){ // counType=1: 每个月的数据
					mapStr += "var key= year + '-'+ month;";
				}else if(counType==2){ // counType=2:每天的数据
					mapStr += "var key= year + '-'+ month + '-' + day;";
				}else if(counType==3){ //counType=3 :每小时数据
					mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour +' : 00';";
				}else if(counType==4){ //counType=4 :每分钟的数据
					mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour + ':'+ minute;";
				}
	           
				mapStr += "emit(key,1);}";
		
		 String reduce = "function Reduce(key, values) {" +
			                "return Array.sum(values);" +
	                    "}";
		 MapReduceCommand.OutputType type =  MapReduceCommand.OutputType.INLINE;//
		 MapReduceCommand command = new MapReduceCommand(collection, mapStr, reduce,null, type,query);

		int i = 0;
		MapReduceOutput mapReduceOutput = null;
		while (i < 5) {
			i++;
			try {
				mapReduceOutput = collection.mapReduce(command);
				break;
			} catch (MongoSocketWriteException e) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				logger.info("retry getAddFriendsCount mapReduce:{}", i);
			}
		}
		if (null == mapReduceOutput) return countData;
		 Iterable<DBObject> results = mapReduceOutput.results();
		 Map<String,Double> map = new HashMap<String,Double>();
		for (Iterator iterator = results.iterator(); iterator.hasNext();) {
			DBObject obj = (DBObject) iterator.next();
			 
			map.put((String)obj.get("_id"),(Double)obj.get("value"));
			countData.add(JSON.toJSON(map));
			map.clear();
			//System.out.println("======>>>群消息统计 "+JSON.toJSON(obj));
			
		}
		
		return countData;
	}

	
	
	
	
	
	
	
	
}
