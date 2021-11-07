package cn.xyz.mianshi.service.impl;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Black;
import cn.xyz.mianshi.vo.Config;
import cn.xyz.mianshi.vo.Gift;
import cn.xyz.mianshi.vo.Givegift;
import cn.xyz.mianshi.vo.LiveRoom;
import cn.xyz.mianshi.vo.LiveRoom.LiveRoomMember;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;

@Service
public class LiveRoomManagerImpl extends MongoRepository<LiveRoom, ObjectId> {
	
	public final  String mucMsg="mucmsg_";
	
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}
	@Override
	public Class<LiveRoom> getEntityClass() {
		return LiveRoom.class;
	}
	
	private static UserManagerImpl getUserManager(){
		UserManagerImpl userManager = SKBeanUtils.getUserManager();
		return userManager;
	};	
	
	public LiveRoom getLiveRoom(Integer userId){
		Query<LiveRoom> query = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId);
		return query.get();
	}
	
	//创建直播间
	public LiveRoom createLiveRoom(LiveRoom room){
			Query<LiveRoom> query = getDatastore().createQuery(getEntityClass()).field("userId").equal(room.getUserId());
			if(null != query.get())
				throw new ServiceException("不能重复创建直播间");
			room.setNickName(getUserManager().getUser(room.getUserId()).getNickname());
			room.setCreateTime(DateUtil.currentTimeSeconds());
			room.setNotice(room.getNotice());
			room.setNumbers(1);
			room.setUrl(room.getUserId()+"_"+DateUtil.currentTimeSeconds());
			room.setJid(room.getJid());
			room.setUrl(KSessionUtil.getClientConfig().getLiveUrl()+room.getUrl());
			ObjectId id=(ObjectId) save(room).getId();
			LiveRoomMember member=new LiveRoomMember();
			member.setUserId(room.getUserId());
			member.setRoomId(id);
			member.setCreateTime(DateUtil.currentTimeSeconds());
			member.setNickName(getUserManager().getUser(room.getUserId()).getNickname());
			member.setType(1);
			saveEntity(member);
			return room;
	}
	//修改直播间信息
	public void updateLiveRoom(Integer userId,LiveRoom room){
		Query<LiveRoom> query = getDatastore().createQuery(getEntityClass()).field("roomId").equal(room.getRoomId()).field("userId").equal(userId);
		UpdateOperations<LiveRoom> ops=createUpdateOperations();
		
		if(!StringUtil.isEmpty(room.getName()))
			ops.set("name", room.getName());
		if(!StringUtil.isEmpty(room.getUrl()))
			ops.set("url", room.getUrl());
		if(!StringUtil.isEmpty(room.getNotice()))
			ops.set("notice", room.getNotice());
		ops.set("currentState", room.getCurrentState());
		ops.disableValidation();
		
		UpdateResults update = getDatastore().update(query, ops);
		if(update.getUpdatedCount()<=0){
			throw new ServiceException("修改失败");
		}
		
	}
	
	//删除直播间
	public void deleteLiveRoom(ObjectId roomId){
		Query<LiveRoom> query=getDatastore().createQuery(LiveRoom.class).field("_id").equal(roomId);
		LiveRoom liveRoom = query.get();
		if(null !=liveRoom){
			// 删除聊天记录
			getRoomDatastore().getDB().getCollection(mucMsg+liveRoom.getJid()).drop();
			//删除直播间中的成员
			Query<LiveRoomMember> merquery=getDatastore().createQuery(LiveRoomMember.class);
			merquery.filter("roomId", roomId);
			getDatastore().delete(merquery);
			
			//删除群组离线消息记录
			SKBeanUtils.getRoomManagerImplForIM().deleMucHistory(liveRoom.getJid());
			// 删除直播间
			getDatastore().delete(query);
			SKBeanUtils.getRoomManager().destroyRoomToIM(liveRoom.getJid());
		}
		
	}
	
	//开始/结束直播
	public void start(ObjectId roomId,int status){
		Integer currentState = get(roomId).getCurrentState();
		if(1 == currentState) 
			throw new ServiceException("您的直播间已被锁住");
		UpdateOperations<LiveRoom> ops=getDatastore().createUpdateOperations(getEntityClass());
		ops.set("status", status);
		updateAttributeByOps(roomId,ops);
	}
	
	//后台查询所有房间
	public PageResult<LiveRoom> findConsoleLiveRoomList(String name,String nickName,Integer userId,Integer page, Integer limit,int status,int type){
		PageResult<LiveRoom> result=new PageResult<LiveRoom>();
		Query<LiveRoom> query=createQuery();
		if(!StringUtil.isEmpty(name)){
			query.filter("name",name);
		}
		if(!StringUtil.isEmpty(nickName)){
			query.filter("nickName",nickName);
		}
		if(0!=userId){
			query.filter("userId",userId);
		}
		if(1==status){
			query.filter("status",status);
		}
		query.order("-createTime");
		List<LiveRoom> roomList= query.asList(pageFindOption(page, limit, type));
		for (LiveRoom liveRoom : roomList) {
			if(!liveRoom.getUrl().contains("//")){
				liveRoom.setUrl(KSessionUtil.getClientConfig().getLiveUrl()+liveRoom.getUrl());
			}/*else if(DateUtil.currentTimeSeconds()-liveRoom.getCreateTime()>3600){
				roomList.remove(liveRoom);
			}	*/
		}
		result.setData(roomList);
		result.setCount(query.count());
		return result;
	}
	
	//查询所有房间
	public List<LiveRoom> findLiveRoomList(String name,String nickName,Integer userId,Integer page, Integer limit,int status,int type){
		Query<LiveRoom> query=createQuery();
		if(!StringUtil.isEmpty(name)){
			query.filter("name",name);
		}
		if(!StringUtil.isEmpty(nickName)){
			query.filter("nickName",nickName);
		}
		if(0!=userId){
			query.filter("userId",userId);
		}
		if(1==status){
			query.filter("status",status);
		}
		query.order("-createTime");
		List<LiveRoom> roomList= query.asList(pageFindOption(page, limit, type));
		for (LiveRoom liveRoom : roomList) {
			if(!liveRoom.getUrl().contains("//")){
				liveRoom.setUrl(KSessionUtil.getClientConfig().getLiveUrl()+liveRoom.getUrl());
			}
			/*else if(DateUtil.currentTimeSeconds()-liveRoom.getCreateTime()>3600){
				roomList.remove(liveRoom);
			}	*/
		}
		return roomList;
	}
	
	//加入直播间
	public boolean enterIntoLiveRoom(Integer userId,ObjectId roomId){
		Query<LiveRoom> r=getDatastore().createQuery(getEntityClass());
		LiveRoom liveRoom=r.filter("roomId", roomId).get();
		if(!userId.equals(liveRoom.getUserId()) && 0 == liveRoom.getStatus())
			throw new ServiceException("该直播间尚未开播，请刷新再试");
		Query<LiveRoomMember> q=getDatastore().createQuery(LiveRoomMember.class);
		q.filter("roomId",roomId);
		q.filter("userId",userId);
		LiveRoomMember liveRoomMember=q.get();
		Query<Black> b=getDatastore().createQuery(Black.class);
		b.filter("roomId",roomId);
		b.filter("userId",userId);
		//成员是否在黑名单
		if(null == b.get()){
			User user=getUserManager().getUser(userId);
			//房间是否存在改用户
			if(null!=liveRoomMember){
				UpdateOperations<LiveRoomMember> ops = getDatastore().createUpdateOperations(LiveRoomMember.class);
				ops.set("online", 1);
				getDatastore().update(q, ops);
			}else{
				LiveRoomMember member=new LiveRoomMember();
				member.setUserId(userId);
				member.setRoomId(roomId);
				member.setCreateTime(DateUtil.currentTimeSeconds());
				member.setNickName(getUserManager().getUser(userId).getNickname());
				member.setOnline(1);
				member.setType(userId.equals(liveRoom.getUserId())? 1 : 3);
				saveEntity(member);
				//修改直播间总人数
				UpdateOperations<LiveRoom> ops=createUpdateOperations();
				ops.inc("numbers", 1);
				updateAttributeByOps(roomId, ops);
			}
			MessageBean messageBean=new MessageBean();
			messageBean.setType(KXMPPServiceImpl.JOINLIVE);
			messageBean.setContent(liveRoom.getName());
			messageBean.setObjectId(liveRoom.getJid());
			messageBean.setFileName(liveRoom.getRoomId().toString());
			messageBean.setToUserId(user.getUserId()+"");
			messageBean.setToUserName(user.getNickname());
			messageBean.setMessageId(StringUtil.randomUUID());
			// 发送群聊
			SKBeanUtils.getRoomManager().sendGroupMsg(liveRoom.getJid(), messageBean);
			return true;
		}else{
			throw new ServiceException("您已被踢出直播间！");
		}
		
		
		
	}
	//后台退出直播间
	public void exitLiveRoom(Integer userId,ObjectId roomId){
		//删除直播间成员
		Query<LiveRoomMember> q=getDatastore().createQuery(LiveRoomMember.class);
		q.filter("roomId",roomId);
		q.filter("userId",userId);
		LiveRoomMember liveRoomMember=q.get();
		
		User user=getUserManager().getUser(userId);
		LiveRoom liveRoom = get(roomId);
		if(null==liveRoomMember||liveRoomMember.getOnline()==0)
			return;
		
		if(liveRoomMember.getType()==3&&liveRoomMember.getState()!=1){
			getDatastore().delete(q);
			//修改直播间总人数
			UpdateOperations<LiveRoom> ops=createUpdateOperations();
			if(liveRoom.getNumbers()<=0){
				return;
			}else{
				ops.inc("numbers", -1);
				updateAttributeByOps(roomId, ops);
			}
		}else{
			if(liveRoomMember.getType()==1){
				UpdateOperations<LiveRoomMember> ops = getDatastore().createUpdateOperations(LiveRoomMember.class);
				ops.set("online", 0);
				getDatastore().update(q, ops);
				Query<Black> b=getDatastore().createQuery(Black.class);
				b.filter("roomId", roomId);
				getDatastore().delete(b);
			}else{
				UpdateOperations<LiveRoomMember> ops = getDatastore().createUpdateOperations(LiveRoomMember.class);
				ops.set("online", 0);
				getDatastore().update(q, ops);
			}
		}
		MessageBean messageBean=new MessageBean();
		messageBean.setType(KXMPPServiceImpl.LiveRoomSignOut);
		messageBean.setObjectId(liveRoom.getJid());
		messageBean.setFromUserId(user.getUserId()+"");
		messageBean.setFromUserName(user.getNickname());
		messageBean.setToUserId(user.getUserId()+"");
		messageBean.setToUserName(user.getNickname());
		messageBean.setMessageId(StringUtil.randomUUID());
		// 发送群聊通知
		SKBeanUtils.getRoomManager().sendGroupMsg(liveRoom.getJid(), messageBean);
		/*try {
			
			List<Integer> users=findMembersUserIds(roomId);
			KXMPPServiceImpl.getInstance().send(users,messageBean.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}*/
	}
	//踢出直播间
	public void kick(Integer userId,ObjectId roomId){
		/*//删除直播间成员
		Query<LiveRoomMember> q=getDatastore().createQuery(LiveRoomMember.class);
		q.filter("roomId",roomId);
		q.filter("userId", userId);
		getDatastore().delete(q);*/
		LiveRoom liveRoom = get(roomId);
		//修改直播间总人数
		UpdateOperations<LiveRoom> ops=createUpdateOperations();
		if(liveRoom.getNumbers()<=0){
			return;
		}else{
			ops.inc("numbers", -1);
			updateAttributeByOps(roomId, ops);
		}
		User touser=getUserManager().getUser(userId);
		/*if(touser==null){
			throw new ServiceException("用户不在该房间");
		}*/
		User user=getUserManager().getUser(ReqUtil.getUserId());
		
		MessageBean messageBean=new MessageBean();
		messageBean.setType(KXMPPServiceImpl.LiveRoomSignOut);
		messageBean.setObjectId(liveRoom.getJid());
		messageBean.setFromUserId(user.getUserId()+"");
		messageBean.setFromUserName(user.getNickname());
		messageBean.setToUserId(touser.getUserId()+"");
		messageBean.setToUserName(touser.getNickname());
		messageBean.setMessageId(StringUtil.randomUUID());
		try {
			// 发送群聊通知
			SKBeanUtils.getRoomManager().sendGroupMsg(liveRoom.getJid(), messageBean);
			ThreadUtil.executeInThread(new Callback() {
				
				@Override
				public void execute(Object obj) {
					Query<LiveRoomMember> q=getDatastore().createQuery(LiveRoomMember.class);
					q.filter("roomId",roomId);
					q.filter("userId", userId);
					getDatastore().delete(q);
				}
			});
		} catch (Exception e) {	
			// TODO: handle exception
		}
		//添加到黑名单
		Black black=new Black();
		black.setRoomId(roomId);
		black.setUserId(userId);
		black.setTime(DateUtil.currentTimeSeconds());
		saveEntity(black);
	}
	
	//解锁、锁定直播间
	public void operationLiveRoom(ObjectId roomId, int currentState) {
		// 处理直播间的状态
		Query<LiveRoom> query = getDatastore().createQuery(getEntityClass()).field("roomId").equal(roomId);
		UpdateOperations<LiveRoom> ops = createUpdateOperations();
		ops.set("currentState", currentState);
		ops.set("status", 0);// 关闭直播
		getDatastore().update(query, ops);
		// 通知处理
		LiveRoom liveRoom = get(roomId);
		User user = getUserManager().getUser(liveRoom.getUserId());
		MessageBean messageBean = new MessageBean();
		messageBean.setType(KXMPPServiceImpl.RoomDisable);
		messageBean.setObjectId(liveRoom.getJid());
		messageBean.setFromUserId(10005 + "");
		messageBean.setFromUserName("后台管理员");
		messageBean.setToUserId(user.getUserId() + "");
		messageBean.setToUserName(user.getNickname());
		messageBean.setMessageId(StringUtil.randomUUID());
		try {
			// 发送群聊通知
			SKBeanUtils.getRoomManager().sendGroupMsg(liveRoom.getJid(), messageBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//查询房间成员
	public List<LiveRoomMember> findLiveRoomMemberList(ObjectId roomId){
		Query<LiveRoomMember> query=getDatastore().createQuery(LiveRoomMember.class);
		
		if(null!=roomId)
			query.filter("roomId",roomId);
			query.filter("online", 1);
		return query.asList();
	}
	public List<Integer> findMembersUserIds(ObjectId roomId){
		List<Integer> userIds=null;
		BasicDBObject query=new BasicDBObject();
		if(null!=roomId)
			query.append("roomId",roomId);
			query.append("online", 1);
		 userIds = distinct("LiveRoomMember","userId", query);
		return userIds;
	}
	
	//获取单个成员
	public LiveRoomMember getLiveRoomMember(ObjectId roomId,Integer userId){
		Query<LiveRoomMember> q=getDatastore().createQuery(LiveRoomMember.class);
		q.filter("roomId", roomId);
		q.filter("userId", userId);
		LiveRoomMember liveRoomMember=q.get();
		return liveRoomMember;
		
	}
	
	//禁言/取消禁言
	public LiveRoomMember shutup(int state,Integer userId,ObjectId roomId){
		Query<LiveRoomMember> q=getDatastore().createQuery(LiveRoomMember.class);
		q.filter("userId",userId);
		LiveRoom liveRoom=get(roomId);
		LiveRoomMember livemember=new LiveRoomMember();
		livemember=q.get();
		//修改状态
		UpdateOperations<LiveRoomMember> ops = getDatastore().createUpdateOperations(LiveRoomMember.class);
		ops.set("state", state);
		getDatastore().update(q, ops);
		//xmpp推送
//		User user=getUserManager().getUser(userId);
		MessageBean messageBean=new MessageBean();
		messageBean.setType(KXMPPServiceImpl.LiveRoomBannedSpeak);
		if(state==1){
			messageBean.setContent(DateUtil.currentTimeSeconds());
		}else{
			messageBean.setContent(0);
		}
		messageBean.setFromUserId(liveRoom.getUserId().toString());
		messageBean.setFromUserName(liveRoom.getNickName());
		messageBean.setObjectId(liveRoom.getJid());
		messageBean.setToUserId(livemember.getUserId()+"");
		messageBean.setToUserName(livemember.getNickName());
		messageBean.setMessageId(StringUtil.randomUUID());
		// 发送群聊通知
		SKBeanUtils.getRoomManager().sendGroupMsg(liveRoom.getJid(), messageBean);
		/*try {
			List<Integer> userIdlist=findMembersUserIds(roomId);
			KXMPPServiceImpl.getInstance().send(userIdlist,messageBean.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}*/
		return livemember;
	}
	//添加礼物
	public void addGift(String name,String photo,double price,int type){
		Gift gift=new Gift();
		gift.setName(name);
		gift.setPhoto(photo);
		gift.setPrice(price);
		gift.setType(type);
		saveEntity(gift);
	}
	//删除礼物
	public void deleteGift(ObjectId giftId){
		Query<Gift> q=getDatastore().createQuery(Gift.class);
		q.filter("giftId", giftId);
		getDatastore().delete(q);
	}
	
	//后台查询所有的礼物
	public Map<String,Object> consolefindAllgift(String name,int pageIndex,int pageSize){
		Map<String,Object> giftMap = Maps.newConcurrentMap();
		Query<Gift> query=getDatastore().createQuery(Gift.class);
		if(!name.equals("")){
			query.filter("name", name);
		}
		query.offset(pageSize*pageIndex);
		giftMap.put("total", query.count());
		giftMap.put("data", query.limit(pageSize).asList());
//		List<Gift> giftList=query.limit(pageSize).asList();
		return giftMap;
	}
	
	//查询所有的礼物
	public List<Gift> findAllgift(String name,int pageIndex,int pageSize){
		Query<Gift> query=getDatastore().createQuery(Gift.class);
		if(!name.equals("")){
			query.filter("name", name);
		}
		query.offset(pageSize*pageIndex);
		List<Gift> giftList=query.limit(pageSize).asList();
		return giftList;
	}
	
	
	//送礼物
	public synchronized ObjectId giveGift(Integer userId,Integer toUserId,ObjectId giftId,int count,Double price,ObjectId roomId){
		// 礼物对应的总价格
		Double totalMoney = price * count; 
		Query<User> q=getDatastore().createQuery(User.class);
		q.filter("userId",userId);
		User user=new User();
		user=q.get();
		Query<LiveRoom> query=getDatastore().createQuery(getEntityClass());
		query.filter("_id", roomId);
		
		LiveRoom liveRoom=new LiveRoom();
		liveRoom=query.get();
		if(user.getBalance()>=price*count){
			Givegift givegift=new Givegift();
			givegift.setUserId(userId);
			givegift.setToUserId(toUserId);
			givegift.setGiftId(giftId);
			givegift.setCount(count);
			givegift.setPrice(price*count);
			givegift.setTime(DateUtil.currentTimeSeconds());
			saveEntity(givegift);
			//扣除用户的余额
			getUserManager().rechargeUserMoeny(userId, totalMoney, 2);
			//增加主播的余额
			Config config=SKBeanUtils.getAdminManager().getConfig();
			if(null != config)
				getUserManager().rechargeUserMoeny(toUserId, totalMoney*config.getGiftRatio(),1);
			// 系统分成
			
			//xmpp推送消息
			MessageBean messageBean=new MessageBean();
			messageBean.setType(KXMPPServiceImpl.GIFT);
			messageBean.setFromUserId(userId.toString());
			messageBean.setFromUserName(user.getNickname());
			messageBean.setObjectId(liveRoom.getJid());
			messageBean.setContent(giftId.toString());
			messageBean.setMessageId(StringUtil.randomUUID());
			// 发送群聊
			SKBeanUtils.getRoomManager().sendGroupMsg(liveRoom.getJid(), messageBean);
			/*try {
				List<Integer> userIdlist=findMembersUserIds(roomId);
				KXMPPServiceImpl.getInstance().send(userIdlist,messageBean.toString());
			} catch (Exception e) {
				// TODO: handle exception
			}*/
			return giftId;
		}else{
			throw new ServiceException("余额不足");
		}
	}
	
	//主播收到礼物的列表
	public PageResult<Givegift> getGiftList(Integer userId,String startDate,String endDate,Integer page,Integer limit){
		PageResult<Givegift> result=new PageResult<Givegift>();
		double totalMoney = 0;
		Config config=SKBeanUtils.getAdminManager().getConfig();
		Query<Givegift> query;
		if(StringUtil.isEmpty(startDate) && StringUtil.isEmpty(endDate)){
			query = getDatastore().createQuery(Givegift.class).
					filter("toUserId", userId);
		}else{
			long startTime = 0; //开始时间（秒）
			long endTime = 0; //结束时间（秒）,默认为当前时间
			startTime = StringUtil.isEmpty(startDate) ? 0 :DateUtil.toDate(startDate).getTime()/1000;
			DateUtil.getTodayNight();
			endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime()/1000;
			long formateEndtime = DateUtil.getOnedayNextDay(endTime,1,0);
			query = getDatastore().createQuery(Givegift.class).field("time").greaterThan(startTime).field("time").lessThanOrEq(formateEndtime);
		}
		List<Givegift> giveGiftList = query.asList(pageFindOption(page, limit, 1));
		for(Givegift givegift : giveGiftList){
			Query<LiveRoom> liveRoom = getDatastore().createQuery(getEntityClass()).filter("userId",givegift.getToUserId());
			Query<Gift> gift = getDatastore().createQuery(Gift.class).filter("giftId",givegift.getGiftId());
			givegift.setGiftName(gift.get().getName());
			givegift.setLiveRoomName(liveRoom.get().getName());
			givegift.setActualPrice(config.getGiftRatio()*givegift.getPrice());
			// 当前总收入
			totalMoney += givegift.getActualPrice();
			givegift.setUserName(getUserManager().getNickName(givegift.getUserId()));
			givegift.setToUserName(getUserManager().getNickName(givegift.getToUserId()));
		}
		result.setData(giveGiftList);
		result.setCount(query.count());
		result.setTotal(totalMoney);
		return result;
		
	}
	
	//购买礼物的记录
	public List<Givegift> giftdeal(Integer userId,int pageIndex,int pageSize){
		Query<Givegift> query=getDatastore().createQuery(Givegift.class);
		query.filter("userId", userId);
		query.offset(pageSize*pageIndex);
		List<Givegift> givegiftList=query.limit(pageSize).asList();	
		return givegiftList;
	}
	//发送弹幕
	public ObjectId barrage(Integer userId,ObjectId roomId,String text){
		Query<User> q=getDatastore().createQuery(User.class);
		q.filter("userId",userId);
		User user=new User();
		user=q.get();
		LiveRoom liveRoom = get(roomId);
		int price=1;
		ObjectId barrageId=null;
		if(user.getBalance()>=1*price){
			Givegift givegift=new Givegift();
			givegift.setCount(1);
			givegift.setPrice(1.0);
			givegift.setUserId(userId);
			givegift.setTime(DateUtil.currentTimeSeconds());
			
			saveEntity(givegift);
			barrageId=givegift.getGiftId();
			//修改用户账户金额
			user.setBalance(user.getBalance()-1);
			saveEntity(user);
			//xmpp推送
			MessageBean messageBean=new MessageBean();
			messageBean.setType(KXMPPServiceImpl.BARRAGE);
			messageBean.setFromUserId(userId.toString());
			messageBean.setFromUserName(user.getNickname());
			messageBean.setObjectId(liveRoom.getJid());
			messageBean.setContent(text);
			messageBean.setMessageId(StringUtil.randomUUID());
			/*try {
				List<Integer> userIdlist=findMembersUserIds(roomId);
				KXMPPServiceImpl.getInstance().send(userIdlist,messageBean.toString());
			} catch (Exception e) {
				// TODO: handle exception
			}*/
			// 发送群聊
			SKBeanUtils.getRoomManager().sendGroupMsg(liveRoom.getJid(), messageBean);
			return barrageId;
		}else{
		 throw new ServiceException("余额不足请充值");
		}
		
	}
	//设置/取消管理员
	public void setmanage(Integer userId,int type,ObjectId roomId){
		/*LiveRoomMember liveRoomMember=getDatastore().get(LiveRoomMember.class, userId);*/
		LiveRoom liveRoom=get(roomId);
		if(userId == liveRoom.getUserId())
			throw new ServiceException("不能设置主播为管理员");
		Query<LiveRoomMember> q=getDatastore().createQuery(LiveRoomMember.class);
		q.filter("userId", userId);
		if(null != q.get() && 2 == q.get().getType())
			throw new ServiceException("该用户已经是管理员");
		UpdateOperations<LiveRoomMember> ops = getDatastore().createUpdateOperations(LiveRoomMember.class);
		ops.set("type", type);
		getDatastore().update(q, ops);
		//xmpp推送
		MessageBean messageBean=new MessageBean();
		messageBean.setType(KXMPPServiceImpl.LiveRoomSettingAdmin);
		if(type==2){//1为设置管理员
			messageBean.setContent(1);
		}else{
			messageBean.setContent(0);
		}
		messageBean.setFromUserId(liveRoom.getUserId().toString());
		messageBean.setFromUserName(liveRoom.getNickName());
		messageBean.setToUserName(q.get().getNickName());
		messageBean.setToUserId(q.get().getUserId().toString());
		messageBean.setObjectId(liveRoom.getJid());
		messageBean.setMessageId(StringUtil.randomUUID());
		// 发送群聊通知
		SKBeanUtils.getRoomManager().sendGroupMsg(liveRoom.getJid(), messageBean);
		/*try {
			List<Integer> userIdlist=findMembersUserIds(roomId);
			KXMPPServiceImpl.getInstance().send(userIdlist,messageBean.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}*/
		
	}
	//点赞
	public void addpraise(ObjectId roomId){
		LiveRoom liveRoom=get(roomId);
		//xmpp消息
		MessageBean messageBean=new MessageBean();
		messageBean.setType(KXMPPServiceImpl.LIVEPRAISE);
		messageBean.setObjectId(liveRoom.getJid());
		messageBean.setMessageId(StringUtil.randomUUID());
		// 发送群聊
		SKBeanUtils.getRoomManager().sendGroupMsg(liveRoom.getJid(), messageBean);
	}
	
	//定时清除直播间
	public void clearLiveRoom(){
		Query<LiveRoom> query=getDatastore().createQuery(getEntityClass());
		getDatastore().delete(query);
	}

}
