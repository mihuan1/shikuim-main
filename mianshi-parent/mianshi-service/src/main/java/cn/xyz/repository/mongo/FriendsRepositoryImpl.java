package cn.xyz.repository.mongo;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.model.PageVO;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.repository.FriendsRepository;
import cn.xyz.service.RedisServiceImpl;

@Service
public class FriendsRepositoryImpl extends MongoRepository<Friends, ObjectId> implements FriendsRepository {
	
	@Override
	public Class<Friends> getEntityClass() {
		// TODO Auto-generated method stub
		return Friends.class;
	}
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	
	private static RedisServiceImpl getRedisServiceImpl(){
		return SKBeanUtils.getRedisService();
	}
	
	@Override
	public void deleteFriends(int userId) {
		Query<Friends> q = getDatastore().createQuery(Friends.class);
		q.or(q.criteria("userId").equal(userId),q.criteria("toUserId").equal(userId));
		List<Friends> asList = q.asList();
		if(asList.size() > 0){
			asList.forEach(friends ->{
				getRedisServiceImpl().deleteFriends(friends.getUserId());
				getRedisServiceImpl().deleteFriends(friends.getToUserId());
				getRedisServiceImpl().deleteFriendsUserIdsList(friends.getUserId());
				getRedisServiceImpl().deleteFriendsUserIdsList(friends.getToUserId());
			});
		}
		getDatastore().delete(q);
	}
	

	@Override
	public Friends deleteFriends(int userId, int toUserId) {
		Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(userId).field("toUserId").equal(toUserId);

		return getDatastore().findAndDelete(q);
	}
	
	@Override
	public Friends getFriends(int userId, int toUserId) {
		Query<Friends> q = getDatastore().createQuery(Friends.class);
		q.field("userId").equal(userId);
		q.field("toUserId").equal(toUserId);
		Friends friends = q.get();
		Query<Friends> p = getDatastore().createQuery(Friends.class);
		p.field("userId").equal(toUserId);
		p.field("toUserId").equal(userId);
		Friends tofriends=p.get();
		if(null==friends)
			return friends;
		else if(null==tofriends)
			friends.setIsBeenBlack(0);
		else
			friends.setIsBeenBlack(tofriends.getBlacklist());
		return friends;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<Friends> queryBlacklist(int userId,int pageIndex,int pageSize) {
		Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(userId).field("blacklist").equal(1);
		q.offset(pageIndex * pageSize).limit(pageSize);
		return q.asList();
	}
	
	@SuppressWarnings("deprecation")
	public PageVO queryBlacklistWeb(int userId,int pageIndex,int pageSize) {
		Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(userId).field("blacklist").equal(1);
		q.offset(pageIndex * pageSize).limit(pageSize);
		
		return new PageVO(q.asList(), q.count(), pageIndex, pageSize);
	}


	@SuppressWarnings("deprecation")
	@Override
	public List<Integer> queryFansId(int userId) {
		Query<Friends> q = getDatastore().createQuery(Friends.class).retrievedFields(true, "toUserId").field("userId").equal(userId);
/*		q.filter("status !=", 0);
		q.filter("status !=", 1);*/
		q.field("status").equal(Friends.Status.Friends);
		List<Integer> result = Lists.newArrayList();
		List<Friends> fList = q.asList();

		fList.forEach(fans -> {
			result.add(fans.getToUserId());
		});

		return result;
	}
	
	@SuppressWarnings("deprecation")
	public List<Integer> queryFansIdByUserId(int userId) {
		Query<Friends> q = getDatastore().createQuery(Friends.class).retrievedFields(true, "userId").field("toUserId").equal(userId);
 		q.field("status").equal(Friends.Status.Friends);
		List<Integer> result = Lists.newArrayList();
		List<Friends> fList = q.asList();
		for(Friends friends : fList){
			result.add(friends.getUserId());
		}
		return result;
	}

	@Override
	public List<Friends> queryFollow(int userId,int status) {
		Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(userId);
		if(0<status)
			q.filter("status",status);
		q.filter("status !=", 0);
		// q.or(q.criteria("status").equal(Friends.Status.Attention),
		// q.criteria("status").equal(Friends.Status.Friends));

		return q.asList();

	}
	
	
	/** @Description:（后台好友管理） 
	* @param userId
	* @param toUserId
	* @param status
	* @param page
	* @param limit
	* @return
	**/ 
	public PageResult<Friends> consoleQueryFollow(int userId,int toUserId,int status,int page,int limit) {
		PageResult<Friends> result = new PageResult<Friends>();
		Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(userId).order("-createTime");
		if(0<status)
			q.filter("status",status);
		q.filter("status !=", 0);
		if(0 != toUserId)
			q.filter("toUserId",toUserId);
		q.field("toUserId").notEqual(10000);// 系统号好友不返回
		result.setCount(q.count());
		result.setData(q.asList(pageFindOption(page, limit, 1)));
		return result;

	}
	
	public List<Friends> allFriendsInfo(int userId) {
		Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(userId).order("-createTime");
		q.filter("status !=", 0);
		q.field("toUserId").notEqual(10000);// 系统号好友不返回
		return q.asList();

	}

	@Override
	public List<Integer> queryFollowId(int userId) {
		Query<Friends> q = getDatastore().createQuery(Friends.class).retrievedFields(true, "toUserId").field("userId").equal(userId);
		q.filter("status !=", 0);
		
		List<Integer> result = Lists.newArrayList();
		List<Friends> fList = q.asList();

		fList.forEach(friends -> {
			result.add(friends.getToUserId());
		});

		return result;
	}

	@Override
	public List<Friends> queryFriends(int userId) {
		Query<Friends> q = getDatastore().createQuery(Friends.class);
		q.field("userId").equal(userId);
		q.field("status").equal(Friends.Status.Friends);
		q.field("blacklist").notEqual(1);
		return q.asList();
	}
	
	@Override
	public List<Friends> friendsOrBlackList(int userId,String type) {
		Query<Friends> q = getDatastore().createQuery(Friends.class);
		q.field("userId").equal(userId);
		if("friendList".equals(type)){  //返回好友和单向关注的用户列表
			q.filter("status !=",Friends.Status.Stranger); //返回非陌生人列表(好友和单向关注)
			q.filter("blacklist !=", Friends.Blacklist.Yes); //排除加入黑名单的用户
		}else if("blackList".equals(type)){ //返回黑名单的用户列表
			q.field("blacklist").equal(Friends.Blacklist.Yes); 
		}
		return q.asList();
	}



	@Override
	public Object saveFriends(Friends friends) {
		String chatRecordTimeOut=SKBeanUtils.getUserManager().getSettings(friends.getUserId()).getChatRecordTimeOut();
		friends.setChatRecordTimeOut(Double.valueOf(chatRecordTimeOut));
		return getDatastore().save(friends);
	}

	@Override
	public Friends updateFriends(Friends friends) {
		Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(friends.getUserId()).field("toUserId").equal(friends.getToUserId());

		UpdateOperations<Friends> ops = getDatastore().createUpdateOperations(Friends.class);
		ops.set("modifyTime", DateUtil.currentTimeSeconds());
		if (null != friends.getBlacklist())
			ops.set("blacklist", friends.getBlacklist());
		if (null != friends.getIsBeenBlack())
			ops.set("isBeenBlack", friends.getIsBeenBlack());
		if (null != friends.getStatus())
			ops.set("status", friends.getStatus());
		if (!StringUtil.isEmpty(friends.getToNickname()))
			ops.set("toNickname", friends.getToNickname());
		/*if (!StringUtil.isEmpty(friends.getRemarkName()))
			ops.set("remarkName", friends.getRemarkName());*/
		if(0!=friends.getChatRecordTimeOut())
			ops.set("chatRecordTimeOut", friends.getChatRecordTimeOut());
		if(null != friends.getToFriendsRole())
			ops.set("toFriendsRole", friends.getToFriendsRole());
		if(0 != friends.getToUserType())
			ops.set("toUserType", friends.getToUserType());
		ops.set("modifyTime", DateUtil.currentTimeSeconds());
		return getDatastore().findAndModify(q, ops);
	}
	
	public Friends updateFriendRemarkName(Integer userId,Integer toUserId,String remarkName,String describe) {
		Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(userId).field("toUserId").equal(toUserId);
		if(SKBeanUtils.getUserManager().isOpenMultipleDevices(userId)){
			SKBeanUtils.getUserManager().multipointLoginUpdateUserInfo(userId, SKBeanUtils.getUserManager().getNickName(userId),toUserId,SKBeanUtils.getUserManager().getNickName(toUserId),1);
		}
		// 陌生人备注
		if(null == q.get()){
			// 添加陌生人
			Friends friends = new Friends();
			friends.setUserId(userId);
			friends.setToUserId(toUserId);
			friends.setRemarkName(remarkName);
			friends.setStatus(Friends.Status.Stranger);
			friends.setCreateTime(DateUtil.currentTimeSeconds());
			// 描述
			friends.setDescribe(describe);
			getDatastore().save(friends);
			return friends;
		}else{
			UpdateOperations<Friends> ops = getDatastore().createUpdateOperations(Friends.class);
			ops.set("modifyTime", DateUtil.currentTimeSeconds());
			if(null == remarkName)
				ops.set("remarkName", q.get().getToNickname());
			else
				ops.set("remarkName", remarkName);
			// 描述
			ops.set("describe", describe);
			// 维护reids好友数据
			SKBeanUtils.getRedisService().deleteFriends(userId);
			return getDatastore().findAndModify(q, ops);
		}
	}
}
