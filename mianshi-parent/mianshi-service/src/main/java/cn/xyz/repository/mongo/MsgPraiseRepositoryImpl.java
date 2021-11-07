package cn.xyz.repository.mongo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import cn.xyz.commons.support.Callback;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Collect;
import cn.xyz.mianshi.vo.Emoji;
import cn.xyz.mianshi.vo.Msg;
import cn.xyz.mianshi.vo.Praise;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.MsgPraiseRepository;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;

@Service()
public class MsgPraiseRepositoryImpl extends MongoRepository<Praise, ObjectId> implements MsgPraiseRepository {
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<Praise> getEntityClass() {
		return Praise.class;
	}
	
	private final String s_praise = "s_praise";
	
	@Override
	public ObjectId add(int userId, ObjectId msgId) {
		User user = SKBeanUtils.getUserManager().getUser(userId);

		if (!exists(userId, msgId)) {
			Praise entity = new Praise(ObjectId.get(), msgId, user.getUserId(), user.getNickname(),
					DateUtil.currentTimeSeconds());
			// 更新缓存
			SKBeanUtils.getRedisService().deleteMsgPraise(msgId.toString());
			// 持久化赞
			getDatastore().save(entity);
			// 更新消息：赞+1、活跃度+1
			SKBeanUtils.getMsgRepository().update(msgId, Msg.Op.Praise, 1);

			ThreadUtil.executeInThread(new Callback() {
				@Override
				public void execute(Object obj) {
					push(userId, msgId);
				}
			});

			return entity.getPraiseId();
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void push(int userId,ObjectId msgId){
		// xmpp推送
		User user = SKBeanUtils.getUserManager().getUser(userId);
		Query<Msg> q=getDatastore().createQuery(Msg.class);
		Msg msg=q.filter("msgId", msgId).get();
		int type=msg.getBody().getType();
		String url=null;
		if(null!=msg.getBody()) {
			if(type==1){
				url=msg.getBody().getText();
			}else if(type==2){
				url=msg.getBody().getImages().get(0).getTUrl();
			}else if(type==3){
				url=msg.getBody().getAudios().get(0).getOUrl();
			}else if(type==4){
				url=msg.getBody().getVideos().get(0).getOUrl();
			}
		}
		
		String t=String.valueOf(type);
		String u=String.valueOf(msgId);
		String mm=u+","+t+","+url;
		MessageBean messageBean=new MessageBean();
		messageBean.setType(KXMPPServiceImpl.PRAISE);
		messageBean.setFromUserId(String.valueOf(userId));
		messageBean.setFromUserName(user.getNickname());
		messageBean.setContent("");
		messageBean.setObjectId(mm);
		messageBean.setMessageId(StringUtil.randomUUID());
		try {
			List<Integer> praiseuserIdlist=new ArrayList<Integer>();
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
			    } 
			KXMPPServiceImpl.getInstance().send(messageBean,list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean delete(int userId, ObjectId msgId) {
		// 取消点赞
		Query<Praise> q = getDatastore().createQuery(Praise.class).field("msgId")
				.equal(msgId).field("userId").equal(userId);
		if(null!=q.get()){
			getDatastore().findAndDelete(q);
			SKBeanUtils.getRedisService().deleteMsgPraise(msgId.toString());
			// 更新消息：赞-1、活跃度-1
			SKBeanUtils.getMsgRepository().update(msgId, Msg.Op.Praise, -1);
			return true;
		}else{
			return false;
		}

		
	}
	// 取消朋友圈收藏
	public void deleteCollect(int userId, String msgId) {
		// 删除收藏
		Query<Collect> q = getDatastore().createQuery(Collect.class).field("msgId")
				.equal(new ObjectId(msgId)).field("userId").equal(userId);
		getDatastore().delete(q);
		Query<Emoji> query = getDatastore().createQuery(Emoji.class).field("collectMsgId")
				.equal(msgId).field("userId").equal(userId);
		getDatastore().delete(query);
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				if(null != query.get())
					ConstantUtil.deleteFile(query.get().getUrl());
			}
		});
	}


	/* (non-Javadoc)
	 *	
	 */
	@SuppressWarnings("deprecation")
	@Override
	public List<Praise> find(ObjectId msgId, ObjectId praiseId, int pageIndex, int pageSize) {
		List<Praise> praiseList;
		if(null != praiseId)
			praiseList = getDatastore().find(Praise.class).field("praiseId").equal(praiseId).order("-time").offset(pageIndex * pageSize).limit(pageSize).asList();
		else{
			// 倒序查询  正序返回
			praiseList = getDatastore().find(Praise.class).field("msgId").equal(msgId).order("-time").offset(pageIndex * pageSize).limit(pageSize).asList();
			/*Collections.sort(praiseList,new Comparator<Praise>() {

				@Override
				public int compare(Praise o1, Praise o2) {
					if (o1.getTime() > o2.getTime()) {
						return 1;
					}else if(o1.getTime() == o2.getTime()){
						return 0;
					}
					return -1;
				}
			});*/
		}

		return praiseList;
	}

	@SuppressWarnings("unchecked")
	public List<ObjectId> getPraiseIds(Integer userId){
		List<ObjectId> msgIds =distinct(s_praise,"msgId",new BasicDBObject("userId", userId));
		return msgIds;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean exists(int userId, ObjectId msgId) {
		Query<Praise> q = getDatastore().createQuery(Praise.class).field("msgId")
				.equal(msgId).field("userId").equal(userId);
		
		long count = q.countAll();
		return 0 != count;
	}
	
	@SuppressWarnings("deprecation")
	public boolean existsCollect(int userId, ObjectId msgId) {
		Query<Collect> q = getDatastore().createQuery(Collect.class).field("msgId")
				.equal(msgId).field("userId").equal(userId);
		long count = q.countAll();
		return 0 != count;
	}
}
