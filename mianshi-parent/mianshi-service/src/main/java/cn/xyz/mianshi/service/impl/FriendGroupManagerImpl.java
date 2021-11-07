package cn.xyz.mianshi.service.impl;


import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.stereotype.Service;

import com.mongodb.WriteResult;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.KConstants.Result;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.FriendGroup;
import cn.xyz.mianshi.vo.OfflineOperation;

@Service
public class FriendGroupManagerImpl extends MongoRepository<FriendGroup, ObjectId>{
	
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<FriendGroup> getEntityClass() {
		return FriendGroup.class;
	}
	private static UserManagerImpl getUserManager(){
		UserManagerImpl userManager = SKBeanUtils.getUserManager();
		return userManager;
	};
	public void autoCreateGroup(Integer userId){
		FriendGroup group=new FriendGroup();
		group.setUserId(userId);
		group.setGroupName("家人");
		FriendGroup group1=new FriendGroup();
		group1.setUserId(userId);
		group1.setGroupName("同事");
		saveGroup(group);
		saveGroup(group1);
	}
	
	public FriendGroup saveGroup(FriendGroup group){
		if(null==group.getGroupId()){
			group.setCreateTime(DateUtil.currentTimeSeconds());
			group.setGroupId(new ObjectId());
			save(group);
		}else{
			update(group.getGroupId(), group);
		}
		// 多点登录同步好友标签
		int userId = group.getUserId();
		if(getUserManager().isOpenMultipleDevices(userId))
			multipointLoginUpdateFriendsGroup(userId, getUserManager().getNickName(userId));
		return group;
	}
	
	// 多点登录同步好友标签通知
	public void multipointLoginUpdateFriendsGroup(Integer userId,String nickName){
		Datastore datastore = SKBeanUtils.getDatastore();
		getUserManager().multipointLoginDataSync(userId, nickName, KConstants.MultipointLogin.SYNC_LABEL);
		Query<OfflineOperation> query = datastore.createQuery(OfflineOperation.class).field("userId").equal(userId);
		query.field("friendId").equal(String.valueOf(userId)).field("tag").equal(KConstants.MultipointLogin.TAG_LABLE);
		if(null == query.get())
			datastore.save(new OfflineOperation(userId, KConstants.MultipointLogin.TAG_LABLE, String.valueOf(userId), DateUtil.currentTimeSeconds()));
		else{
			UpdateOperations<OfflineOperation> ops = datastore.createUpdateOperations(OfflineOperation.class);
			ops.set("operationTime", DateUtil.currentTimeSeconds());
			datastore.update(query, ops);
		}
	}
	
	public FriendGroup queryGroupName(Integer userId,String groupName){
		Query<FriendGroup> query=createQuery();
		query.filter("userId", userId);
		query.filter("groupName", groupName);
		return query.get();
	}
	//更新分组的名称
	public void updateGroupName(Integer userId,ObjectId groupId,String groupName) throws ServerException{
		updateAttributeByIdAndKey(groupId, "groupName", groupName);
		Query<FriendGroup> query = getDatastore().createQuery(getEntityClass()).field("groupId").equal(groupId).field("userId").equal(userId);
		UpdateOperations<FriendGroup> ops = getDatastore().createUpdateOperations(getEntityClass());
		ops.set("groupName", groupName);
		
		UpdateResults update = getDatastore().update(query, ops);
		
		if(update.getUpdatedCount()>0){
			// 多点登录同步好友标签
			if(getUserManager().isOpenMultipleDevices(userId))
				multipointLoginUpdateFriendsGroup(userId, getUserManager().getNickName(userId));
		}else{
			throw new ServerException("修改失败");
		}
		
	}
	/**
	* @Description: TODO(修改好友的分组ID)
	* @param @param userId
	* @param @param toUserId
	* @param @param groupId    参数
	 * @throws ServerException 
	 */
	public void updateFriendGroup(int userId, Integer toUserId,List<String> groupIdStrList) throws ServerException{
		try {
			Query<FriendGroup> query=createQuery();
			query.filter("userId", userId);
			List<FriendGroup> groupList = query.asList();
			ThreadUtil.executeInThread(new Callback() {
				@Override
				public void execute(Object obj) {
					//修改后的 分组Id	
					List<ObjectId> groupIdList=new ArrayList<ObjectId>();
					for (String str : groupIdStrList) {
						if(!StringUtil.isEmpty(str)&&
								(ObjectId.isValid(str))) {
							groupIdList.add(new ObjectId(str));
						}
					}
					UpdateOperations<FriendGroup> ops=null;
					for (FriendGroup friendGroup : groupList) {
						//好友Id 不在分组中
						if(!friendGroup.getUserIdList().contains(toUserId)){
							//修改后的分组 Id 中没有当前分组 不处理
							if(!groupIdList.contains(friendGroup.getGroupId()))
								continue;
							friendGroup.getUserIdList().add(toUserId);
							ops=createUpdateOperations();
							ops.set("userIdList", friendGroup.getUserIdList());
							updateAttributeByOps(friendGroup.getGroupId(), ops);
						}else{
							//好友Id 在分组中
							//并且当前分组在 修改后的分组ID 中 不处理
							if(groupIdList.contains(friendGroup.getGroupId()))
								continue;
							friendGroup.getUserIdList().remove(toUserId);
							ops=createUpdateOperations();
							ops.set("userIdList", friendGroup.getUserIdList());
							updateAttributeByOps(friendGroup.getGroupId(), ops);
						}
					}
				}
			});
			// 多点登录同步好友标签
			if(getUserManager().isOpenMultipleDevices(userId))
				multipointLoginUpdateFriendsGroup(userId, getUserManager().getNickName(userId));
		
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(e.getMessage());
		}
	}
	
	public void updateGroupUserList(Integer userId,ObjectId groupId,List<Integer> userIdList) throws ServerException{
		Query<FriendGroup> query = getDatastore().createQuery(getEntityClass()).field("groupId").equal(groupId).field("userId").equal(userId);
		UpdateOperations<FriendGroup> ops = getDatastore().createUpdateOperations(getEntityClass());
		ops.set("userIdList", userIdList);
		UpdateResults update=getDatastore().update(query, ops);
		if(update.getUpdatedCount()>0){
			// 多点登录同步好友标签
			if(getUserManager().isOpenMultipleDevices(userId))
				multipointLoginUpdateFriendsGroup(userId, getUserManager().getNickName(userId));
		}else {
			throw new ServerException("修改失败");
		}
		
	}
	
	public void deleteGroup(Integer userId,ObjectId groupId) throws ServerException{
		Query<FriendGroup> query = getDatastore().createQuery(getEntityClass()).field("groupId").equal(groupId).field("userId").equal(userId);
		WriteResult deleteByQuery = deleteByQuery(query);
		if(deleteByQuery.getN()>0){
			// 多点登录同步好友标签
			if(getUserManager().isOpenMultipleDevices(userId))
				multipointLoginUpdateFriendsGroup(userId, getUserManager().getNickName(userId));
		}else {
			throw new ServerException("修改失败");
		}
		
	}
	
	public List<FriendGroup> queryGroupList(long userId){
		/*if(getUserManager().isOpenMultipleDevices((int) userId))
			multipointLoginUpdateFriendsGroup((int) userId, getUserManager().getNickName((int) userId));*/
		return getEntityListsByKey("userId", userId);
	}
}
