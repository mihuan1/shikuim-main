package cn.xyz.repository.mongo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.CriteriaContainer;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.model.AddMsgParam;
import cn.xyz.mianshi.model.MessageExample;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Comment;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.Givegift;
import cn.xyz.mianshi.vo.Msg;
import cn.xyz.mianshi.vo.Msg.Body;
import cn.xyz.mianshi.vo.Msg.Resource;
import cn.xyz.mianshi.vo.Praise;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.MsgRepository;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;

@Service
public class MsgRepositoryImpl extends MongoRepository<Msg,ObjectId> implements MsgRepository {
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<Msg> getEntityClass() {
		return Msg.class;
	}
	
//	@Override
	public Msg add(int userId, AddMsgParam param) {
		User user = SKBeanUtils.getUserManager().getUser(userId);
		Msg entity = Msg.build(user, param);
		// 保存商务圈消息
		getDatastore().save(entity);
		// 如果musicId不为空维护音乐使用次数
		if(!StringUtil.isEmpty(param.getMusicId())){
			SKBeanUtils.getLocalSpringBeanManager().getMusicManager().updateUseCount(new ObjectId(param.getMusicId()));
		}
		//提醒朋友列表-.-
		if(null!=param.getUserRemindLook()){
			ThreadUtil.executeInThread(new Callback() {
				
				@Override
				public void execute(Object obj) {
					for(int i=0;i<param.getUserRemindLook().size();i++){
						push(userId,param.getUserRemindLook().get(i),entity.getMsgId());
					}
				}
			});
		}
		
//		return entity.getMsgId();
		return entity;
	}

	private void push(int userId,int toUserId,ObjectId msgId){
		// xmpp推送
		User user = SKBeanUtils.getUserManager().getUser(userId);
		Query<Msg> q=getDatastore().createQuery(getEntityClass());
		Msg msg=q.filter("msgId", msgId).get();
		int type=msg.getBody().getType();
		String url=null;
		if(type==1){
			url=msg.getBody().getText();
		}else if(type==2){
			url=msg.getBody().getImages().get(0).getTUrl();
		}else if(type==3){
			url=msg.getBody().getAudios().get(0).getOUrl();
		}else if(type==4){
			url=msg.getBody().getImages().get(0).getOUrl();
		}
		String t=String.valueOf(type);
		String u=String.valueOf(msgId);
		String mm=u+","+t+","+url;
		MessageBean messageBean=new MessageBean();
		messageBean.setType(KXMPPServiceImpl.REMIND);
		messageBean.setFromUserId(String.valueOf(userId));
		messageBean.setFromUserName(user.getNickname());
		messageBean.setContent("");
		messageBean.setObjectId(mm);
		messageBean.setToUserId(String.valueOf(toUserId));
		messageBean.setMsgType(0);// 单聊消息
		messageBean.setMessageId(StringUtil.randomUUID());
		try {
		/*	List<Integer> praiseuserIdlist=new ArrayList<Integer>();
			DBObject d=new BasicDBObject("msgId",msgId);
			praiseuserIdlist=distinct("s_praise", "userId", d);
			
			List<Integer> userIdlist=new ArrayList<Integer>();
			userIdlist=distinct("s_comment","userId", d);
			
			userIdlist.addAll(praiseuserIdlist);
			
			userIdlist.add(msg.getUserId());
			
			HashSet<Integer> hs=new HashSet<Integer>(userIdlist);
			List<Integer> list=new ArrayList<Integer>(hs);
			
			//移出集合中当前操作人
			for (int i = 0; i < list.size(); i++) {   
			       if (list.get(i).equals(userId)) {   
			    	   list.remove(i);   
			       }   
			    } */
			KXMPPServiceImpl.getInstance().send(messageBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean delete(String... msgIds) {
		for(String msgId : msgIds){
			ObjectId objMsgId = new ObjectId(msgId);
			Query<Msg> query=getDatastore().createQuery(getEntityClass());
			query.field(Mapper.ID_KEY).equal(objMsgId);
			Msg msg=query.get();
			Body body=null;
			if(null!=msg)
				body=msg.getBody();
			try {
				// 删除消息主体
				getDatastore().delete(query);
				if(null!=body){
					if(null!=body.getImages())
						deleteResource(body.getImages());
					if(null!=body.getAudios())
						deleteResource(body.getAudios());
					if(null!=body.getVideos())
						deleteResource(body.getVideos());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			ThreadUtil.executeInThread(new Callback() {
				
				@Override
				public void execute(Object obj) {
					// TODO Auto-generated method stub
					if(null!=msg){
						// 删除评论
						getDatastore().delete(getDatastore().createQuery(Comment.class).field("msgId").equal(objMsgId));
						// 删除赞
						getDatastore().delete(getDatastore().createQuery(Praise.class).field("msgId").equal(objMsgId));
						// 删除礼物
						getDatastore().delete(getDatastore().createQuery(Givegift.class).field("msgId").equal(objMsgId));
					}
				}
			});
		}
		return true;
	}
	
	public List<Resource> deleteResource(List<Resource> resources){
		for (Resource resource : resources) {
			try {
				ConstantUtil.deleteFile(resource.getOUrl());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return resources;
	}
	private List<Msg> fetchAndAttach(int userId, Query<Msg> query) {
		List<Msg> msgList = query.asList();
		msgList.forEach(msg -> {
				if(null!=msg.getBody()) {
					if(msg.getBody().getType() == 5) {
						if(null!=msg.getBody().getFiles()&&null!=msg.getBody().getFiles().get(0))
							msg.setFileName(msg.getBody().getFiles().get(0).getoFileName());
					}
				}
			msg.setComments(getComments(msg.getMsgId().toString()));
			msg.setPraises(getPraises(msg.getMsgId().toString()));
			msg.setGifts(SKBeanUtils.getMsgGiftRepository().find(msg.getMsgId(), null, 0, 10));
			msg.setIsPraise(SKBeanUtils.getMsgPraiseRepository().exists(ReqUtil.getUserId(), msg.getMsgId()) ? 1 : 0);
			msg.setIsCollect(SKBeanUtils.getMsgPraiseRepository().existsCollect(ReqUtil.getUserId(), msg.getMsgId()) ? 1 : 0);
		});
		
		return msgList;
	}
	
	/** @Description: 获取最新二十条评论 
	* @param msgId
	* @return
	**/ 
	private List<Comment> getComments(String msgId){
		List<Comment> msgComment = SKBeanUtils.getRedisService().getMsgComment(msgId);
		if(null != msgComment && msgComment.size() > 0){
			return msgComment;
		}else {
			List<Comment> commonListMsg = SKBeanUtils.getMsgCommentRepository().find(new ObjectId(msgId), null, 0, 20);
			if(null != commonListMsg && commonListMsg.size() > 0)
				SKBeanUtils.getRedisService().saveMsgComment(msgId, commonListMsg);
			return commonListMsg;
			
		}
	}
	
	private List<Praise> getPraises(String msgId){
		List<Praise> msgPraise = SKBeanUtils.getRedisService().getMsgPraise(msgId);
		if(null != msgPraise && msgPraise.size() > 0){
			return msgPraise;
		}else {
			List<Praise> praiseListMsg = SKBeanUtils.getMsgPraiseRepository().find(new ObjectId(msgId), null, 0, 20);
			if(null != praiseListMsg && praiseListMsg.size() > 0)
				SKBeanUtils.getRedisService().saveMsgPraise(msgId, praiseListMsg);
			return praiseListMsg;
		}
	}
	

	@Override
	public List<Msg> findByExample(int userId, MessageExample example) {
		List<Integer> userIdList = SKBeanUtils.getFriendsManager().queryFollowId(userId);
		userIdList.add(userId);

		Query<Msg> query = getDatastore().createQuery(getEntityClass());

		if (!StringUtil.isEmpty(example.getBodyTitle()))
			query.field("body.title").contains(example.getBodyTitle());
		if (0 != example.getCityId())
			query.field("cityId").equal(example.getCityId());
		if (0 != example.getFlag())
			query.field("flag").equal(example.getFlag());
		if (ObjectId.isValid(example.getMsgId()))
			query.field(Mapper.ID_KEY).lessThan(new ObjectId(example.getMsgId()));
		query.filter("userId in", userIdList);
		query.field("visible").greaterThan(0);

		query.order("-_id").limit(example.getPageSize());

		return fetchAndAttach(userId, query);
	}

	@Override
	public List<Msg> gets(int userId, String ids) {
		List<ObjectId> idList = Lists.newArrayList();
		JSON.parseArray(ids, String.class).forEach(id -> {
			idList.add(new ObjectId(id));
		});

		Query<Msg> query = getDatastore().createQuery(getEntityClass()).filter("_id in", idList);
		query.order("-_id").asList();

		return fetchAndAttach(userId, query);
	}

	@Override
	public List<Msg> getUserMsgList(Integer userId, Integer toUserId, ObjectId msgId, Integer pageSize) {
		List<Msg> list = Lists.newArrayList();

		// 获取登录用户最新消息
		if (null == toUserId || userId.intValue() == toUserId.intValue()) {
			list = findByUser(userId,toUserId, msgId, pageSize);
		}
		// 获取某用户最新消息
		else {
			// 获取BA关系
			Friends friends = SKBeanUtils.getFriendsManager().getFriends(new Friends(toUserId, userId));

			// 陌生人
			if (null == friends || (Friends.Blacklist.No == friends.getBlacklist() && Friends.Status.Stranger == friends.getStatus())) {
				list = findByUser(userId,toUserId, msgId, 10);
			}
			// 关注或好友
			else if (Friends.Blacklist.No == friends.getBlacklist()) {
				list = findByUser(userId,toUserId, msgId, pageSize);
			}
			// 黑名单
			else {
				// 不返回
			}
		}

		return list;
	}

	@Override
	public List<Msg> findByUser(Integer userId,Integer toUserId, ObjectId msgId, Integer pageSize) {
		Query<Msg> query = getDatastore().createQuery(getEntityClass()).field("userId").equal(toUserId);
		if (null != msgId)
			query.field(Mapper.ID_KEY).lessThan(msgId);
		
		List<Integer> users=new ArrayList<Integer>();
		users.add(userId);
		
		Query<Msg> query2=getDatastore().createQuery(getEntityClass());
		Query<Msg> query3=getDatastore().createQuery(getEntityClass());
		Query<Msg> query4=getDatastore().createQuery(getEntityClass());
		
		CriteriaContainer criteria1 = query.criteria("visible").equal(1).criteria("userNotLook").hasNoneOf(users);
		CriteriaContainer criteria2=query2.criteria("userId").equal(userId);
		CriteriaContainer criteria3=query3.criteria("visible").equal(3).criteria("userLook").hasAnyOf(users);
		CriteriaContainer criteria4=query4.criteria("visible").equal(4).criteria("userNotLook").hasNoneOf(users);
		
		query.or(criteria1,criteria2,criteria3,criteria4);
		query.order("-_id").limit(pageSize);

		return fetchAndAttach(userId, query);
	}

	@Override
	public List<Msg> getUserMsgIdList(int userId, int toUserId, ObjectId msgId, int pageSize) {
		Query<Msg> query = getDatastore().createQuery(getEntityClass()).retrievedFields(true, "userId", "nickname").field("userId").equal(userId);
		if (null != msgId)
			query.field(Mapper.ID_KEY).lessThan(msgId);
		query.order("-_id").limit(pageSize);

		return query.asList();
	}

	@Override
	public boolean forwarding(Integer userId, AddMsgParam param) {
		return true;
	}

	@Override
	public Msg get(int userId, ObjectId msgId) {
		String key = String.format("msg:%1$s", msgId.toString());
		boolean exists = SKBeanUtils.getRedisCRUD().keyExists(key);

		if (!exists) {
			Msg msg = getDatastore().createQuery(getEntityClass()).field(Mapper.ID_KEY).equal(msgId).get();
			if(null==msg)
				return msg;
			else if(0==userId)
				return msg;
			
					String value = msg.toString();
					SKBeanUtils.getRedisCRUD().setWithExpireTime(key, value, 43200);

		}

		String text =SKBeanUtils.getRedisCRUD().get(key);

		Msg msg;
		// 缓存未命中、超出缓存范围
		if (null == text || "".equals(text)) {
			msg = getDatastore().createQuery(getEntityClass()).field(Mapper.ID_KEY).equal(msgId).get();
			if(null==msg)
				return msg;
		} else {
			// msg = JSON.parseObject(text, getEntityClass());
			try {
				msg = new ObjectMapper().readValue(text, getEntityClass());
			} catch (Exception e) {
				throw new ServiceException("消息缓存解析失败");
			}
		}

		msg.setComments(SKBeanUtils.getMsgCommentRepository().find(msg.getMsgId(), null, 0, 20));
		msg.setPraises(SKBeanUtils.getMsgPraiseRepository().find(msg.getMsgId(), null, 0, 20));
		msg.setGifts(SKBeanUtils.getMsgGiftRepository().find(msg.getMsgId(), null, 0, 20));
		msg.setIsPraise(SKBeanUtils.getMsgPraiseRepository().exists(userId, msg.getMsgId()) ? 1 : 0);
		msg.setIsCollect(SKBeanUtils.getMsgPraiseRepository().existsCollect(ReqUtil.getUserId(), msg.getMsgId()) ? 1 : 0);
		return msg;
	}

	@Override
	public List<Msg> getMsgIdList(int userId, int toUserId, ObjectId msgId, int pageSize) {
		List<Integer> userIdList = SKBeanUtils.getFriendsManager().queryFollowId(userId);
		userIdList.add(userId);

		Query<Msg> query = getDatastore().createQuery(getEntityClass()).retrievedFields(true, "userId", "nickname").filter("userId in", userIdList);

		if (null != msgId)
			query.field(Mapper.ID_KEY).lessThan(msgId);
		query.order("-_id").limit(pageSize);

		return query.asList();
	}

	@Override
	public List<Msg> getMsgList(Integer userId, Integer toUserId, ObjectId msgId, Integer pageSize,Integer pageIndex) {
		List<Integer> userIdList;
		List<Integer> friendsUserIds = SKBeanUtils.getRedisService().getFriendsUserIdsList(userId);
		if(null != friendsUserIds && friendsUserIds.size() > 0 ){
			userIdList = friendsUserIds;
		}else{
			userIdList = SKBeanUtils.getFriendsManager().queryFansId(userId);
			SKBeanUtils.getRedisService().saveFriendsUserIdsList(userId,userIdList);
		}
		userIdList.add(userId);
		
		List<Integer> users=new ArrayList<Integer>();
		users.add(userId);
		
		Query<Msg> query1 = getDatastore().createQuery(getEntityClass()).filter("userId in", userIdList).field("state").notEqual(1);
		User user=SKBeanUtils.getUserManager().getUser(userId);
		if(null!=user.getSettings()&&null!=user.getSettings().getFilterCircleUserIds()) {
			query1.field("userId").notIn(user.getSettings().getFilterCircleUserIds());
		}
		if (null != msgId)
			query1.field(Mapper.ID_KEY).lessThan(msgId);
		Query<Msg> query2=getDatastore().createQuery(getEntityClass());
		Query<Msg> query3=getDatastore().createQuery(getEntityClass());
		Query<Msg> query4=getDatastore().createQuery(getEntityClass());
		
		CriteriaContainer criteria1 = query1.criteria("visible").equal(1).criteria("userNotLook").hasNoneOf(users);
		CriteriaContainer criteria2=query2.criteria("userId").equal(userId);
		CriteriaContainer criteria3=query3.criteria("visible").equal(3).criteria("userLook").hasAnyOf(users);
		CriteriaContainer criteria4=query4.criteria("visible").equal(4).criteria("userNotLook").hasNoneOf(users);
		query1.or(criteria1,criteria2,criteria3,criteria4);
		
		query1.order("-_id").offset(pageIndex*pageSize).limit(pageSize);

		return fetchAndAttach(userId, query1);
	}
	
	/** @Description:（后台获取朋友圈列表） 
	* @param pageSize
	* @param pageIndex
	* @return
	**/ 
	public PageResult<Msg> getMsgList(Integer page,Integer limit,String nickname,Integer userId) {
		PageResult<Msg> result=new PageResult<Msg>();
		try {
			Query<Msg> query = getDatastore().createQuery(getEntityClass()).order("-time");
			if(!StringUtil.isEmpty(nickname))
				query.criteria("nickname").contains(nickname);
			else if(0 != userId)
				query.field("userId").equal(userId);
			List<Msg> msgList = query.asList(pageFindOption(page, limit, 1));
			for(Msg msg : msgList){
				User user = SKBeanUtils.getUserManager().get(msg.getUserId());
				if(null == user){
					// 过滤废弃的测试账号朋友圈数据
					ThreadUtil.executeInThread(new Callback() {
						
						@Override
						public void execute(Object obj) {
							Query<Msg> erryQuery = getDatastore().createQuery(getEntityClass()).field("userId").equal(msg.getUserId());
							getDatastore().delete(erryQuery);
						}
					});
					
				}else{
					msg.setUserStatus(user.getStatus());
				}
			}
			result.setCount(query.count());
			result.setData(msgList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/** @Description: 短视频查询
	* @param pageIndex
	* @param pageSize
	* @param lable 1：美食，2：景点，3：文化，4：玩乐，5：酒店，6：购物，7：运动，8：其他 (包含旧视频)
	* @return
	**/ 
	public List<Msg> getPureVideo(Integer pageIndex,Integer pageSize,String lable){
		Query<Msg> query = getDatastore().createQuery(getEntityClass());
		User user=SKBeanUtils.getUserManager().getUser(ReqUtil.getUserId());
		if(null!=user.getSettings()&&null!=user.getSettings().getFilterCircleUserIds()) {
			query.field("userId").notIn(user.getSettings().getFilterCircleUserIds());
		}
		query.field("body.videos").notEqual(null);
		if(!StringUtil.isEmpty(lable) && "8".equals(lable))
			query.field("body.lable").equal(null);
		else if(!StringUtil.isEmpty(lable))
			query.field("body.lable").equal(lable);
		List<Msg> data = query.order("-time").asList(pageFindOption(pageIndex, pageSize,0));
		
		return data;
	}

	@Override
	public List<Msg> getSquareMsgList(int userId, ObjectId msgId, Integer pageSize) {
		Query<Msg> query = getDatastore().createQuery(getEntityClass());
		if (null != msgId)
			query.field(Mapper.ID_KEY).lessThan(msgId);
		query.order("-_id").limit(pageSize);

		return query.asList();
	}

	@Override
	public void update(ObjectId msgId, Msg.Op op, int activeValue) {
		Query<Msg> q = getDatastore().createQuery(getEntityClass()).field("_id").equal(msgId);
		UpdateOperations<Msg> ops = getDatastore().createUpdateOperations(getEntityClass()).inc(op.getKey(), activeValue).inc("count.total", activeValue);
		// 更新消息
		getDatastore().findAndModify(q, ops);  
	}
	
	/** @Description:（锁定解锁朋友圈） 
	* @param msgId
	**/ 
	public void lockingMsg(ObjectId msgId,int state){
		Query<Msg> query=getDatastore().createQuery(getEntityClass()).filter("msgId", msgId);
		if(null == query)
			throw new ServiceException("Msg is null, msgId:"+msgId);
		UpdateOperations<Msg> ops = getDatastore().createUpdateOperations(getEntityClass());
		ops.set("state", state);
		getDatastore().update(query, ops);
	}
	
	/** @Description:（朋友圈评论） 
	* @param msgId
	**/ 
	public PageResult<Comment> commonListMsg(ObjectId msgId,Integer page,Integer limit){
		PageResult<Comment> result = new PageResult<Comment>();
		Query<Comment> query=getDatastore().createQuery(Comment.class).filter("msgId", msgId).order("time");
		/*if(null == query)
			throw new ServiceException("Comment is null, msgId:"+msgId);*/
		result.setCount(query.count());
		result.setData(query.asList(pageFindOption(page, limit, 1)));
		return result;
	}
	
	/** @Description:（朋友圈点赞） 
	* @param msgId
	**/ 
	public PageResult<Praise> praiseListMsg(ObjectId msgId,Integer page,Integer limit){
		PageResult<Praise> result = new PageResult<Praise>();
		Query<Praise> query=getDatastore().createQuery(Praise.class).filter("msgId", msgId);
		/*if(null == query)
			throw new ServiceException("Comment is null, msgId:"+msgId);*/
		result.setCount(query.count());
		result.setData(query.asList(pageFindOption(page, limit, 1)));
		return result;
	}
	
}
