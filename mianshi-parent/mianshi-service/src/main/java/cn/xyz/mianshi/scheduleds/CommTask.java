package cn.xyz.mianshi.scheduleds;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import cn.xyz.mianshi.vo.*;
import com.yipay.util.YipayUtil;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.TaskManagementConfigUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import cn.xyz.commons.autoconfigure.KApplicationProperties.AppConfig;
import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.MsgType;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.service.impl.ConsumeRecordManagerImpl;
import cn.xyz.mianshi.service.impl.UserManagerImpl;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@EnableScheduling
public class CommTask implements ApplicationListener<ApplicationContextEvent>{

	
	@Resource(name = "dsForRW")
	private Datastore dsForRW;
	
	public static final int STATUS_START=1;//红包发出状态
	public static final int STATUS_END=2;//已领完红包状态
	public static final int STATUS_RECEDE=-1;//已退款红包状态
	
	public static final int TRANSFER_START=1;// 转账发出状态
	public static final int TRANSFER_RECEDE=-1;// 转账退款状态
	//public static final int STATUS_RECEDE=3;//已退款红包状态
	@Autowired
	private UserManagerImpl userManager;
	
	@Autowired
	private ConsumeRecordManagerImpl recordManager;
	@Autowired(required=false)
	private AppConfig appConfig;
	@Resource(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
	private ScheduledAnnotationBeanPostProcessor scheduledProcessor;
	 public CommTask() {
			super();
	 }
	 
	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		 if(event.getApplicationContext().getParent() != null)
			 return;
		 //root application context 没有parent，他就是老大.
		 //需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。
		
				
				if(0==appConfig.getOpenTask()){
						
						ThreadUtil.executeInThread(new Callback() {
							@Override
							public void execute(Object obj) {
								 try {
										Thread.currentThread().sleep(10000);
										scheduledProcessor.destroy();
										log.info("====定时任务被关闭了=======》");
								 	} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
							}
						});
						
				   }else log.info("====定时任务开启中=======》");
	}

	/**
	 刷新用户充值状态
	 */
	@Scheduled(cron = "0 */5 * * * ?")
	public void refreshRechargeStatus() {
		List<ConsumeRecord> records = SKBeanUtils.getConsumeRecordManager().queryPayingYipayRecord();
		if (CollectionUtils.isEmpty(records)) return;
		records.parallelStream().forEach(r -> {
			PaidMerchant paidMerchant = SKBeanUtils.getPaidMerchantManager().getPaidMerchantById(r.getPayMerchantId());
			JSONObject trade = YipayUtil.getSuccessTradeByOrder(paidMerchant, r.getTradeNo());
			if (null != trade) {
				r.setStatus(KConstants.OrderStatus.END);
				Double balance = SKBeanUtils.getUserManager().rechargeUserMoeny(r.getUserId(), r.getMoney(), KConstants.MOENY_ADD);
				r.setOperationAmount(r.getMoney());
				r.setCurrentBalance(balance);
				r.setPayVendorTradeNo(trade.getString("trade_no"));
				SKBeanUtils.getConsumeRecordManager().update(r.getId(), r);
			}
		});
	}
	
	 
	 /**
	一个小时执行   一次的定时任务
	 */
	@Scheduled(cron = "0 0 0/1 * * ?")
	public void executeHourTask() {
		long start = System.currentTimeMillis();
		// 刷新红包
		autoRefreshRedPackect();
		log.info("刷新红包成功,耗时" + (System.currentTimeMillis() - start) + "毫秒");
		// 刷新转账
		autoRefreshTransfer();
		log.info("刷新转账成功,耗时" + (System.currentTimeMillis() - start) + "毫秒");
		
		refreshUserStatusHour();
		
		deleteChatMsgRecord();
		
	}
	 /**
	 每天执行一次定时任务     每天凌晨4:00定时删除 0 0 0 * *
	 */
	@Scheduled(cron = "0 0 4 * * ?")
	public void executeDayTask(){
		
		refreshUserStatus();
		
		deleteOutTimeMucMsg();
		
		deleteMucHistory();
		
		//删除系统日志
		deleteSysLogs();
	}
	/**
	5分钟 采集一次用户在线状态统计
	 */
	@Scheduled(cron = "0 0/5 * * * ?")
 	public void refreshUserStatusCount(){
		DBObject q = new BasicDBObject("onlinestate",1);
		
		long count =dsForRW.getCollection(User.class).getCount(q);
		
		UserStatusCount userCount=new UserStatusCount();
		//long count=(long)(Math.random()*(1000-100+1)+100);
		userCount.setType(1);
		userCount.setCount(count);
		userCount.setTime(DateUtil.currentTimeSeconds());
		userManager.saveEntity(userCount);
		log.info("刷新用户状态统计======》 {}" ,count);
	}
	/**
	一个小时 采集一次用户在线状态统计
	 */
	//@Scheduled(cron = "0 0 0/1 * * ?")
 	public void refreshUserStatusHour(){
		
		long currentTime =new Date().getTime()/1000;
		//DBObject q =null;
		Query<UserStatusCount> q=null;
		long startTime=currentTime-KConstants.Expire.HOUR;
			
		long endTime=currentTime;
		//q = new BasicDBObject();
			q=dsForRW.createQuery(UserStatusCount.class);
			q.enableValidation();
			
			System.out.println("当前时间:"+DateUtil.TimeToStr(new Date()));
			q.field("time").greaterThanOrEq(startTime);
			q.field("time").lessThan(endTime);
			q.field("type").equal(1);
			UserStatusCount userStatusCount = q.order("-count").get();
			if(null!=userStatusCount) {
				UserStatusCount uCount=new UserStatusCount();
				uCount.setTime(startTime);
				uCount.setType(2);
				uCount.setCount(userStatusCount.getCount());
				userManager.saveEntity(uCount);
				log.info("最高在线用户======》  {}",uCount.getCount());
			}
				
	}
	
	
	
	
	
	
	
	
	
	
	

	/**
	 将所有用户在线状态   设为不在线
	 */
	//@Scheduled(cron = "0 0 4 * * ?")
	public void refreshUserStatus(){
		BasicDBObject q = new BasicDBObject("_id",new BasicDBObject(MongoOperator.GT,1000));
		q.append("onlinestate", 1);
		DBObject values = new BasicDBObject();
		values.put(MongoOperator.SET,new BasicDBObject("onlinestate",0));
		dsForRW.getCollection(User.class).update(q, values, false, true);
	}
	/**
	 执行  更新 昨天的 用户在线状态统计 情况
	 */
	@Scheduled(cron = "0 0 2 * * ?")
 	public void refreshUserStatusDay(){
		Date yesterday=DateUtil.getYesterdayMorning();
		//long currentTime =new Date().getTime()/1000;
		//DBObject q =null;
		Query<UserStatusCount> q=null;
		long startTime=yesterday.getTime()/1000;
		long endTime=startTime+KConstants.Expire.DAY1;
		q=dsForRW.createQuery(UserStatusCount.class);
		q.enableValidation();
		log.info("Day_Count 当前时间:"+DateUtil.TimeToStr(new Date()));
		q.field("time").greaterThanOrEq(startTime);
		q.field("time").lessThan(endTime);
		q.field("type").equal(1);
		UserStatusCount userStatusCount = q.order("-count").get();
		if(null!=userStatusCount) {
			UserStatusCount uCount=new UserStatusCount();
			uCount.setTime(startTime);
			uCount.setType(3);
			uCount.setCount(userStatusCount.getCount());
			userManager.saveEntity(uCount);
			log.info("Day_Count 最高在线用户======》   {}",uCount.getCount());
		}
	}
	/**
	定时删除  过期的单聊聊天记录
	 */
	//@Scheduled(cron = "0 0 0/1 * * ?")
	public void deleteChatMsgRecord(){
		DBCollection dbCollection=null;
		DBCollection lastdbCollection=null;
		try{
			System.out.println("=====> deleteChatMsgRecord "+DateUtil.TimeToStr(new Date()));
			dbCollection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_msgs");
			lastdbCollection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_lastChats");
			BasicDBObject query = new BasicDBObject();
			BasicDBObject lastquery=new BasicDBObject();
			query.append("deleteTime", new BasicDBObject(MongoOperator.GT,0)
					.append(MongoOperator.LT, DateUtil.currentTimeSeconds()))
					.append("isRead",1);
			
			BasicDBObject base=(BasicDBObject)dbCollection.findOne(query);
			
			BasicDBList queryOr = new BasicDBList();
			if(base!=null){
				queryOr.add(new BasicDBObject("jid", String.valueOf(base.get("sender"))).append("userId", base.get("receiver").toString()));
				queryOr.add(new BasicDBObject("userId",String.valueOf(base.get("sender"))).append("jid", base.get("receiver").toString()));
				lastquery.append(MongoOperator.OR, queryOr);
			}
			
			// 删除文件
			query.append("contentType",new BasicDBObject(MongoOperator.IN, MsgType.FileTypeArr));
			List<String> fileList= dbCollection.distinct("content", query);
			
			for (String url : fileList) {
				ConstantUtil.deleteFile(url);
			}
			query.remove("contentType");
			dbCollection.remove(query); //将消息记录中的数据删除	
			
			//将消息记录中的数据删除
			dbCollection.remove(query);
			query.remove("messageId");
			query.remove("sender");
			
			// 重新查询一条消息记录插入
			BasicDBList baslist = new BasicDBList();
			if(base!=null){
				baslist.add(new BasicDBObject("receiver", base.get("sender")));
				baslist.add(new BasicDBObject("sender", base.get("sender")));
				query.append(MongoOperator.OR,baslist);
			}
			query.remove("sender");
			query.remove("deleteTime");
			query.remove("isRead");
			DBObject lastMsgObj=dbCollection.find(query).sort(new BasicDBObject("timeSend", -1)).limit(1).one();
			
			if(lastMsgObj!=null){
				BasicDBObject values=new BasicDBObject();
				values.put("messageId", lastMsgObj.get("messageId"));
				values.put("timeSend",new Double(lastMsgObj.get("timeSend").toString()).longValue());
				values.put("content", lastMsgObj.get("content"));
				if(!lastquery.isEmpty())
					lastdbCollection.update(lastquery,new BasicDBObject(MongoOperator.SET, values) ,false,true);
			}else{
				if(!lastquery.isEmpty())
					lastdbCollection.remove(lastquery);
			}
			
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	/**
	删除过期的 群组聊天消息
	 */
	public void deleteOutTimeMucMsg() {
		DBCollection dbCollection=null;
		// 最后一条聊天消息
		DBCollection lastdbCollection=null;
		
		BasicDBObject query = null;
		BasicDBObject lastquery=null;
		try {
			System.out.println("=====> deleteMucMsgRecord "+DateUtil.TimeToStr(new Date()));
			Set<String> set=SKBeanUtils.getImRoomDatastore().getDB().getCollectionNames();
			for(String s:set){
				if(s.startsWith("mucmsg_")){
					lastquery=new BasicDBObject();
					query=new BasicDBObject();
					query.append("deleteTime", new BasicDBObject(MongoOperator.GT,0)
							.append(MongoOperator.LT, DateUtil.currentTimeSeconds()));
					dbCollection = SKBeanUtils.getImRoomDatastore().getDB().getCollection(s);
					lastdbCollection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("shiku_lastChats");
					
					BasicDBObject base=(BasicDBObject) dbCollection.findOne(query);
					if(base!=null)
						lastquery.put("jid", base.get("room_jid_id"));
					/*
					 * if(base!=null) query.put("room_jid_id", base.get("room_jid_id"));
					 */
					
					// 删除文件
					query.append("contentType",new BasicDBObject(MongoOperator.IN, MsgType.FileTypeArr));
					List<String> fileList= dbCollection.distinct("content", query);
					
					for (String url : fileList) {
						ConstantUtil.deleteFile(url);
					}
					
					// 将消息记录中的数据删除
					query.remove("contentType");
					dbCollection.remove(query); 
					
					query.remove("deleteTime");
					DBObject lastMsgObj=dbCollection.find(query).sort(new BasicDBObject("timeSend", -1)).limit(1).one();
					BasicDBObject values=new BasicDBObject();
					if (lastMsgObj!=null) {
						values.put("messageId", lastMsgObj.get("messageId"));
						values.put("timeSend",new Double(lastMsgObj.get("timeSend").toString()).longValue());
						values.put("content", lastMsgObj.get("content"));
						if(!lastquery.isEmpty())
							lastdbCollection.update(lastquery,new BasicDBObject(MongoOperator.SET, values) ,false,true);
					}else{
						if(!lastquery.isEmpty())
							lastdbCollection.remove(lastquery);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	删除 tigase 超过100条的聊天历史记录
	 */	
	public void deleteMucHistory() {
		long start=System.currentTimeMillis();
		final DBCollection collection = SKBeanUtils.getTigaseManager().getTigaseDatastore().getDB().getCollection("muc_history");
		List<DBObject> pipeline=new ArrayList<>();
		DBObject group=new BasicDBObject("$group", new  BasicDBObject("_id", "$room_jid")
				.append("sum",new BasicDBObject("$sum", 1)));
		
		
		DBObject match=new BasicDBObject("$match", new  BasicDBObject("sum",new BasicDBObject("$gt", 100))
				);
		pipeline.add(group);
		pipeline.add(match);
		AggregationOptions options=AggregationOptions.builder().build();
		Cursor cursor = collection.aggregate(pipeline,options);
		Set<String> jidSet=new HashSet<String>();
		try {
			while (cursor.hasNext()) {
				BasicDBObject dbObject = (BasicDBObject) cursor.next();
				jidSet.add(dbObject.getString("_id"));
				log.info("超过100条的群组记录   {}",dbObject.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			cursor.close();
		}
		jidSet.stream().forEach(jid->{
			try {
				DBObject query=new BasicDBObject("room_jid",jid);
				DBObject projection=new BasicDBObject("timestamp",1);
				Iterator<DBObject> iterator = collection.find(query,projection)
						.sort(new BasicDBObject("timestamp", -1))
						.skip(100).limit(1).iterator();
				if(iterator.hasNext()) {
					Object timestamp=iterator.next().get("timestamp");
					query.put("timestamp",new BasicDBObject(MongoOperator.LT,timestamp));
					collection.remove(query);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		});
		log.info("timeCount  ---> "+(System.currentTimeMillis()-start));
	}
	
	
	/** 
	* @Description:（每天凌晨定时清除十五天前的系统日志） 
	**/ 
	public void deleteSysLogs(){
		long beginTime = DateUtil.getOnedayNextDay(DateUtil.currentTimeSeconds(), 15, 1);
		Datastore datastore = SKBeanUtils.getDatastore();
		Query<SysApiLog> query = datastore.createQuery(SysApiLog.class);
		query.field("time").lessThanOrEq(beginTime);
		log.info("累积清除   "+DateUtil.strToDateTime(beginTime)+"  前的  "+ query.count() +"  条系统日志记录");
		datastore.delete(query);
	}
	// 定时清除直播间
	//@Scheduled(cron = "0 0 0 0/7 * ?")
	public void clearLiveRoom(){
		Query<LiveRoom> query=dsForRW.createQuery(LiveRoom.class);
		query.field("createTime").lessThan(DateUtil.currentTimeSeconds()-(KConstants.Expire.DAY7));
		log.info("=========定时删除直播间========  "+query.count());
		for(LiveRoom liveRoom:query.asList()){
			SKBeanUtils.getLiveRoomManager().deleteLiveRoom(liveRoom.getRoomId());
		}
	}
	
	
	//红包超时未领取 退回余额
	private void autoRefreshRedPackect(){
		//q.put("status", new BasicDBObject(MongoOperator.NE,STATUS_RECEDE).append(MongoOperator.NE,STATUS_END));
		long currentTime=DateUtil.currentTimeSeconds();
		DBObject obj=null;
		Integer userId=0;
		Integer toUserId=0;
		String roomJid="";
		ObjectId redPackectId=null;
		Double money=0.0;
		DBObject values = new BasicDBObject();
		List<DBObject> objs=new ArrayList<DBObject>();
		DBObject q = new BasicDBObject("outTime",new BasicDBObject(MongoOperator.LT,currentTime));
		q.put("over",new BasicDBObject(MongoOperator.GT,0));
		q.put("status",STATUS_START);//只查询发出状态的红包
		DBCursor cursor =dsForRW.getCollection(RedPacket.class).find(q);
		
			while (cursor.hasNext()) {
				 obj = (BasicDBObject) cursor.next();
				objs.add(obj);
			}
		if(0<objs.size()){
			values.put(MongoOperator.SET,new BasicDBObject("status", STATUS_RECEDE));
			dsForRW.getCollection(RedPacket.class).update(q, values,false,true);
		}
		for (DBObject dbObject : objs) {
			 userId= (Integer) dbObject.get("userId");
			 money =(Double) dbObject.get("over");
			 roomJid=(String) dbObject.get("roomJid");
			 redPackectId=(ObjectId) dbObject.get("_id");
			 toUserId=(Integer) dbObject.get("toUserId");
			 recedeMoney(userId,toUserId,roomJid,money,redPackectId);
		}
			
		log.info("红包超时未领取的数量 ======> "+objs.size());
		
	}
	
	private void recedeMoney(Integer userId,Integer toUserId,String roomJid,Double money,ObjectId id){
		if(0<money){
			DecimalFormat df = new DecimalFormat("#.00");
			 money= Double.valueOf(df.format(money));
		}else 
			return;
		//实例化一天交易记录
		ConsumeRecord record=new ConsumeRecord();
		String tradeNo=StringUtil.getOutTradeNo();
		record.setTradeNo(tradeNo);
		record.setMoney(money);
		record.setUserId(userId);
		record.setToUserId(toUserId);
		record.setType(KConstants.ConsumeType.REFUND_REDPACKET);
		record.setPayType(KConstants.PayType.BALANCEAY);
		record.setTime(DateUtil.currentTimeSeconds());
		record.setStatus(KConstants.OrderStatus.END);
		record.setDesc("红包退款");
		Double balance = userManager.rechargeUserMoeny(userId, money, KConstants.MOENY_ADD);
		record.setRedPacketId(id);
		record.setOperationAmount(money);
		record.setCurrentBalance(balance);
		recordManager.saveConsumeRecord(record);
		User toUser=userManager.get(toUserId);
		MessageBean messageBean=new MessageBean();
		messageBean.setType(KXMPPServiceImpl.RECEDEREDPAKET);
		if(toUser!=null){
			messageBean.setFromUserId(toUser.getUserId().toString());
			messageBean.setFromUserName(toUser.getNickname());
		}else {
			messageBean.setFromUserId("10100");
			messageBean.setFromUserName("支付公众号");
		}
		
		if(roomJid!=null){
			messageBean.setObjectId(roomJid);
		}
		messageBean.setContent(id.toString());
		messageBean.setToUserId(userId.toString());
		messageBean.setMsgType(0);// 单聊消息
		messageBean.setMessageId(StringUtil.randomUUID());
		try {
			KXMPPServiceImpl.getInstance().send(messageBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log.info(userId+"  发出的红包,剩余金额   "+money+"  未领取  退回余额!");
	}

	// 转账超时未领取 退回余额
	public void autoRefreshTransfer(){
		long currentTime=DateUtil.currentTimeSeconds();
		DBObject obj=null;
		List<DBObject> objs=new ArrayList<DBObject>();
		DBObject values = new BasicDBObject();
		Integer userId=0;
		Double money=0.0;
		Integer toUserId=0;
		ObjectId transferId=null;
		DBObject q = new BasicDBObject("outTime",new BasicDBObject(MongoOperator.LT,currentTime));
		q.put("status",TRANSFER_START);//只查询发出状态的转账
		
		DBCursor cursor =dsForRW.getCollection(Transfer.class).find(q);
		while (cursor.hasNext()) {
			 obj = (BasicDBObject) cursor.next();
			objs.add(obj);
		}
		
		if(0<objs.size()){
			values.put(MongoOperator.SET,new BasicDBObject("status", TRANSFER_RECEDE));
			dsForRW.getCollection(Transfer.class).update(q, values,false,true);
		}
		
		for (DBObject dbObject : objs) {
			 userId= (Integer) dbObject.get("userId");
			 money =(Double) dbObject.get("money");
			 toUserId=(Integer) dbObject.get("toUserId");
			 transferId=(ObjectId) dbObject.get("_id");
			 transferRecedeMoney(userId, toUserId, money, transferId);
		}
		log.info("转账超时未领取的数量 ======> "+objs.size());
	}
	
	// 转账退回
	public void transferRecedeMoney(Integer userId,Integer toUserId,double money,ObjectId transferId){
		if(0<money){
			// 格式化数据
			DecimalFormat df = new DecimalFormat("#.00");
			 money= Double.valueOf(df.format(money));
		}else 
			return;
		
		ConsumeRecord record=new ConsumeRecord();
		String tradeNo=StringUtil.getOutTradeNo();
		record.setTradeNo(tradeNo);
		record.setMoney(money);
		record.setUserId(userId);
		record.setToUserId(toUserId);
		record.setType(KConstants.ConsumeType.REFUND_TRANSFER);
		record.setPayType(KConstants.PayType.BALANCEAY);
		record.setTime(DateUtil.currentTimeSeconds());
		record.setStatus(KConstants.OrderStatus.END);
		record.setDesc("转账退款");
		
		Double balance = userManager.rechargeUserMoeny(userId, money, KConstants.MOENY_ADD);
		record.setCurrentBalance(balance);
		record.setOperationAmount(money);
		recordManager.saveConsumeRecord(record);
		
		User sysUser=userManager.get(1100);
		Transfer transfer=SKBeanUtils.getDatastore().createQuery(Transfer.class).field("_id").equal(transferId).get();
		transfer.setId(null);
		MessageBean messageBean=new MessageBean();
		messageBean.setType(KXMPPServiceImpl.REFUNDTRANSFER);
		
		messageBean.setFromUserId(sysUser.getUserId().toString());
		messageBean.setFromUserName(sysUser.getNickname());
		
		messageBean.setContent(JSONObject.toJSONString(transfer));
		messageBean.setToUserId(userId.toString());
		messageBean.setMsgType(0);// 单聊消息
		messageBean.setMessageId(StringUtil.randomUUID());
		try {
			KXMPPServiceImpl.getInstance().send(messageBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log.info(userId+"  发出转账,剩余金额   "+money+"  未收钱  退回余额!");
	}
}
