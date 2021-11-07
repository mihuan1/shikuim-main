package cn.xyz.mianshi.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.mianshi.service.MPService;
import cn.xyz.mianshi.utils.SKBeanUtils;

@Service
public class MPServiceImpl implements MPService {

	DBObject getLastBody(int sender, int receiver) {
		DBCollection dbCollection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_msgs");
		BasicDBObject q = new BasicDBObject();
		q.put("sender", sender);
		q.put("receiver", receiver);
		DBObject dbObj = dbCollection.findOne(q,new BasicDBObject(),new BasicDBObject("ts",-1));
		if(null==dbObj)
			return null;
		return dbObj;
	}

	/**
	 *  消息分组分页查询
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Object getMsgList(int userId, int pageIndex, int pageSize) {
		List<BasicDBObject> msgList = Lists.newArrayList();
		DBCollection dbCollection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_msgs");
		// 分组条件
		DBObject groupFileds =new BasicDBObject();
		groupFileds.put("sender", "$sender");
		// 过滤条件
		Map<String, Integer> map =new HashMap<>();
		map.put("receiver", userId);
		map.put("direction", 0);
		map.put("isRead", 0);
		DBObject macth=new BasicDBObject("$match",new BasicDBObject(map));
		
		DBObject fileds = new BasicDBObject("_id", groupFileds);
		fileds.put("count", new BasicDBObject("$sum",1));
		DBObject group = new BasicDBObject("$group", fileds);
		DBObject limit=new BasicDBObject("$limit",pageSize);
		DBObject skip=new BasicDBObject("$skip",pageIndex*pageSize);
		AggregationOutput out= dbCollection.aggregate(Arrays.asList(macth,group,skip,limit));
		Iterable<DBObject> result=out.results();
		List<DBObject> list=(List<DBObject>) result;
		
		for(int i=0;i<list.size();i++){
			try {
				BasicDBObject dbObj=(BasicDBObject) list.get(i).get("_id");
				dbObj.append("count", list.get(i).get("count"));
				int sender = dbObj.getInt("sender");
				int receiver = userId;
				String nickname="";
				if(null!=SKBeanUtils.getUserManager().getUser(sender))
					nickname = SKBeanUtils.getUserManager().getUser(sender).getNickname();
				else
					continue;
				int count = dbObj.getInt("count");

				dbObj.put("nickname", nickname);
				dbObj.put("count", count);
				dbObj.put("sender", sender);
				dbObj.put("receiver", receiver);
				DBObject lastBody = getLastBody(sender, receiver);
				dbObj.put("body", lastBody.get("content"));
				String unescapeHtml3 = StringEscapeUtils.unescapeHtml3((String) lastBody.get("body"));
				JSONObject body = JSONObject.parseObject(unescapeHtml3);
				if (null != body.get("isEncrypt") && "1".equals(body.get("isEncrypt").toString())) {
					dbObj.put("isEncrypt", 1);
				} else {
					dbObj.put("isEncrypt", 0);
				}
				dbObj.put("messageId", lastBody.get("messageId"));
				dbObj.put("timeSend", lastBody.get("timeSend"));
				msgList.add(dbObj);
			} catch (ServiceException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

		return msgList;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object getMsgList(int sender, int receiver, int pageIndex, int pageSize) {
		List<DBObject> msgList = Lists.newArrayList();
		BasicDBObject q = new BasicDBObject();
		q.put("sender", sender);
		q.put("receiver", receiver);
		q.put("direction", 0);
		q.put("isRead", 0);
		DBCollection dbCollection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_msgs");
		DBCursor cursor = dbCollection.find(q);
		while (cursor.hasNext()) {
			DBObject dbObj = cursor.next();
			dbObj.put("nickname", SKBeanUtils.getUserManager().getUser(sender).getNickname());
			dbObj.put("content",dbObj.get("content").toString());
			// 处理body
			String unescapeHtml3 = StringEscapeUtils.unescapeHtml3((String) dbObj.get("body"));
			JSONObject body = JSONObject.parseObject(unescapeHtml3);
			if (null != body.get("isEncrypt") && "1".equals(body.get("isEncrypt").toString())) {
				dbObj.put("isEncrypt", 1);
			} else {
				dbObj.put("isEncrypt", 0);
			}
			dbObj.put("timeSend", dbObj.get("timeSend"));
			System.out.println("dbobj : "+JSONObject.toJSONString(dbObj));
			msgList.add(dbObj);
		}
		return msgList;
	}

}
