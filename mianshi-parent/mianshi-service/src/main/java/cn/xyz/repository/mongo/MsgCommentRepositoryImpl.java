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
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.model.AddCommentParam;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Comment;
import cn.xyz.mianshi.vo.Msg;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.MsgCommentRepository;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;

@Service
public class MsgCommentRepositoryImpl extends MongoRepository<Msg, ObjectId> implements MsgCommentRepository {
	
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<Msg> getEntityClass() {
		return Msg.class;
	}

	private final String s_comment="s_comment";// 评论表名称
	
	@Override
	public ObjectId add(int userId, AddCommentParam param) {
		User user = SKBeanUtils.getUserManager().getUser(userId);
		ObjectId commentId = ObjectId.get();
		Comment entity = new Comment(commentId, new ObjectId(
				param.getMessageId()), user.getUserId(), user.getNickname(),
				param.getBody(), param.getToUserId(), param.getToNickname(),
				param.getToBody(), DateUtil.currentTimeSeconds());
		/*// 缓存评论
		String key = String.format("msg:%1$s:comment",
				param.getMessageId());
		SKBeanUtils.getRedisCRUD().del(key);*/
		SKBeanUtils.getRedisService().deleteMsgComment(param.getMessageId());
		
		// 保存评论
		getDatastore().save(entity);
		// 更新消息：评论数+1、活跃度+1
		SKBeanUtils.getMsgRepository().update(new ObjectId(param.getMessageId()),
				Msg.Op.Comment, 1);
		
		//新线程进行xmpp推送
		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
				tack(userId,param);
			}
		});
	
		return entity.getCommentId();
	}

	@SuppressWarnings("unchecked")
	private void tack(int userId, AddCommentParam param){
		User user = SKBeanUtils.getUserManager().getUser(userId);
		// xmpp推送
				Query<Msg> q=getDatastore().createQuery(getEntityClass());
				Msg msg=q.filter("msgId", new ObjectId(param.getMessageId())).get();
				int type=msg.getBody().getType();
				
				String url=null;
				if(type==1){
					url=msg.getBody().getText();
				}else if(type==2){
					url=msg.getBody().getImages().get(0).getTUrl();
				}else if(type==3){
					url=msg.getBody().getAudios().get(0).getOUrl();
				}else if(type==4){
					url=msg.getBody().getVideos().get(0).getOUrl();
				}
				String u=String.valueOf(type);
				String us=param.getMessageId()+","+u+","+url;
				MessageBean messageBean=new MessageBean();
				messageBean.setType(KXMPPServiceImpl.COMMENT);//类型为42
				messageBean.setFromUserId(String.valueOf(userId));//评论者的Id
				messageBean.setFromUserName(user.getNickname());//评论者的昵称
				/*messageBean.setToUserId(String.valueOf(param.getToUserId()));//被回复者的ID
				if(StringUtil.isEmpty(param.getToNickname())){
					messageBean.setToUserName(param.getToNickname());//被回复者的昵称
				}*/
				messageBean.setObjectId(us);//id,type,url
				messageBean.setContent(param.getBody());//评论内容
				messageBean.setMessageId(StringUtil.randomUUID());
				try {
					List<Integer> praiseuserIdlist=new ArrayList<Integer>();
					DBObject d=new BasicDBObject("msgId",new ObjectId(param.getMessageId()));
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
					for(Integer toUserId : list){
						messageBean.setToUserId(toUserId.toString());//被回复者的ID
						if(!StringUtil.isEmpty(param.getToNickname())){
							messageBean.setToUserName(param.getToNickname());//被回复者的昵称
						}
						KXMPPServiceImpl.getInstance().send(messageBean);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
	}
	
	@Override
	public boolean delete(ObjectId msgId, String commentId) {
		try {
			String[] commentIds = StringUtil.getStringList(commentId);
			for (String commId : commentIds) {
				if(!ObjectId.isValid(commId))
					continue;
				// 删除评论
				Query<Comment> query = getDatastore().createQuery(Comment.class).field(MongoOperator.ID).equal(new ObjectId(commId));
				if(null != query.get()){
					getDatastore().findAndDelete(query);
					// 更新消息：评论数-1、活跃度-1
					SKBeanUtils.getMsgRepository().update(msgId, Msg.Op.Comment, -1);
				}
				
			}
			// 清除缓存
			SKBeanUtils.getRedisService().deleteMsgComment(msgId.toString());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	public List<Comment> find(ObjectId msgId, ObjectId commentId, int pageIndex, int pageSize) {
		List<Comment> commentList;
		if(null != commentId)
			commentList = getDatastore().find(Comment.class).field("commentId").equal(commentId).order("-time").offset(pageIndex * pageSize).limit(pageSize).asList();
		else{
			commentList = getDatastore().find(Comment.class).field("msgId").equal(msgId).order("-time").offset(pageIndex * pageSize).limit(pageSize).asList();
			// 倒序查询  正序返回
			/*Collections.sort(commentList,new Comparator<Comment>() {

				@Override
				public int compare(Comment o1, Comment o2) {
					if (o1.getTime() > o2.getTime()) {
						return 1;
					}else if(o1.getTime() == o2.getTime()){
						return 0;
					}
					return -1;
				}
			});*/
		}
		return commentList;
	}
	
	@SuppressWarnings("unchecked")
	public List<ObjectId> getCommentIds(Integer userId){
		List<ObjectId> msgIds =distinct(s_comment,"msgId",new BasicDBObject("userId", userId));
		return msgIds;
	}
}
