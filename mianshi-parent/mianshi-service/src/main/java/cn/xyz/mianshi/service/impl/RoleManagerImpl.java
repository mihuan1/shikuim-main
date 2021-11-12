package cn.xyz.mianshi.service.impl;

import java.util.ArrayList;
import java.util.List;

import cn.xyz.commons.vo.JSONMessage;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.Role;
import cn.xyz.mianshi.vo.Room;
import cn.xyz.mianshi.vo.User;

@Service
public class RoleManagerImpl extends MongoRepository<Role, ObjectId>{
	
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<Role> getEntityClass() {
		return Role.class;
	}
	
	public int getUserRoleByUserId(Integer userId){
		return (int) queryOneField("role", new BasicDBObject("userId", userId));
	}

	public Role getUserRole(Integer userId,String phone,Integer type){
		Query<Role> query = getDatastore().createQuery(getEntityClass());
		if(0 != userId)
			query.filter("userId", userId);
		if(!StringUtil.isEmpty(phone))		
			query.filter("phone", phone);
		if(null != type && 5 == type){
			Integer num = type++;
			query.or(query.criteria("role").equal(type),query.criteria("role").equal(num),query.criteria("role").equal(1),query.criteria("role").equal(4)
					,query.criteria("role").equal(7));
		}
		return query.get();
	}
	
	public List<Role> getUserRoles(Integer userId,String phone,Integer type){
		Query<Role> query = getDatastore().createQuery(getEntityClass());
		if(0 != userId)
			query.filter("userId", userId);
		if(!StringUtil.isEmpty(phone))		
			query.filter("phone", phone);
		if(0 != type)
			query.filter("role", type);
		return query.asList();
	}
	
	public List<Integer> getUserRoles(Integer userId){
		List<Integer> roleType = new ArrayList<Integer>();
		Query<Role> query = getDatastore().createQuery(getEntityClass());
		query.field("userId").equal(userId).field("status").notEqual(-1);
		List<Role> asList = query.asList();
		asList.forEach(role -> {
			roleType.add((int) role.getRole());
		});
		return roleType;
	}
	
	// 后台管理管理员模块
	public PageResult<Role> adminList(String keyWorld,int page,int limit,Integer type,Integer userId){
		PageResult<Role> result = new PageResult<Role>();
		Query<Role> query = getDatastore().createQuery(getEntityClass());
		if(0 == type){
			query.or(query.criteria("role").equal(5),query.criteria("role").equal(6));
			query.field("userId").notEqual(userId); //排除自己
		}else if(4 == type)
			query.filter("role", 4);// 客服
		else if(7 == type)
			query.filter("role", 7);// 财务
		else if(3 == type)
			query.filter("role", 3);// 机器人
		else if(1 == type)
			query.filter("role", 1);// 游客
		else if(2 == type)
			query.filter("role", 2);// 公众号
		if (!StringUtil.isEmpty(keyWorld)) {
			query.criteria("phone").containsIgnoreCase(keyWorld);
		}
		List<Role> roles = query.order("-createTime").asList(pageFindOption(page, limit, 1));
		roles.forEach(role ->{
			role.setNickName(SKBeanUtils.getUserManager().getNickName(role.getUserId()));
		});
		result.setData(roles);
		result.setCount(query.count());
		
		return result;
	}
	
	/** @Description:（设置管理员） 
	* @param areaCode
	* @param account
	* @param password
	* @param role
	**/ 
	public void addAdmin(String telePhone, String phone, byte role, Integer type) {
		User accountUser = SKBeanUtils.getUserManager().getUser(telePhone);
		if (null == accountUser)
			throw new ServiceException("用户不存在");
		
		Role userRole = getUserRole(0, phone,null);
		if(null != userRole){
			throw new ServiceException("该账号已经是" + (userRole.getRole() == 5 ? "管理员" : userRole.getRole() == 6 ? "系统管理员" : userRole.getRole() == 1 ? "游客" :
				userRole.getRole() == 4 ? "客服" : userRole.getRole() == 2 ? "公众号" : userRole.getRole() == 3 ? "机器人" : "有其他身份"));
		}
		/*if(null != userRole){
			byte roles = userRole.getRole();
			if(0 == type){
				if (userRole.getRole() == 5 || userRole.getRole() == 6 || userRole.getRole() == 1)
					throw new ServiceException("该账号已经是" + (userRole.getRole() == 5 ? "管理员" : userRole.getRole() == 6 ? "系统管理员" : "游客" ));
			}else if(4 == type){
				if (userRole.getRole() == 4)
					throw new ServiceException("该账号已经是客服人员");
			}else if(7 == type){
				if (userRole != null && userRole.getRole() == 7)
					throw new ServiceException("该账号已经是财务人员");
			}else if(1 == type){
				if (userRole != null && userRole.getRole() == 1)
					throw new ServiceException("该账号已经是游客");
				else if(userRole.getRole() == 5 || userRole.getRole() == 6)
					throw new ServiceException("该账号已经是"+(userRole.getRole() == 5 ? "管理员" : "超级管理员"));
			}
		}*/
		Role accountRole = null;
		if(type == 4)
			accountRole = new Role(accountUser.getUserId(), accountUser.getPhone(), role, (byte) 1, 0,promotionUrl(accountUser.getUserId()));
		else
			accountRole = new Role(accountUser.getUserId(), accountUser.getPhone(), role, (byte) 1, 0);
		getDatastore().save(accountRole);
		updateFriend(accountUser.getUserId(),null);
	}
	
	public void delAdminById(String adminId,Integer type) {
		if(type == 3){
			ThreadUtil.executeInThread(new Callback() {
				
				@Override
				public void execute(Object obj) {
					if(!StringUtil.isEmpty(adminId)){
						String[] admins = StringUtil.getStringList(adminId,",");
						SKBeanUtils.getUserManager().deleteUser(admins);
						for (String userId : admins) {
							Query<Role> query = getDatastore().createQuery(getEntityClass()).field("userId").equal(Integer.valueOf(userId)).field("role").equal(type);
							getDatastore().delete(query);
						}
					}
				}
			});
		}else if(type == 2){
			Query<User> userQuery = getDatastore().createQuery(User.class).field("userId").equal(Integer.valueOf(adminId));
			UpdateOperations<User> ops = getDatastore().createUpdateOperations(User.class);
			ops.set("userType", 0);
			getDatastore().update(userQuery, ops);
			Query<Role> query = getDatastore().createQuery(getEntityClass()).field("userId").equal(Integer.valueOf(adminId)).field("role").equal(type);
			getDatastore().delete(query);
			updateFriend(Integer.valueOf(adminId),0);
			// 删除redis中的用户
			KSessionUtil.deleteUserByUserId(Integer.valueOf(adminId));
		}else{
			Query<Role> query = getDatastore().createQuery(getEntityClass()).field("userId").equal(Integer.valueOf(adminId)).field("role").equal(type);
			getDatastore().delete(query);
			updateFriend(Integer.valueOf(adminId),0);
		}
			
	}
	
	
	public Role modifyRole(Role role){
		
		Query<Role> q = getDatastore().createQuery(getEntityClass()).field("userId").equal(role.getUserId()).field("role").equal(role.getRole());
		UpdateOperations<Role> ops = getDatastore().createUpdateOperations(getEntityClass());
		
		
		if(role.getRole() != 0) {
			ops.set("role", role.getRole());
		}
		
		if(role.getStatus() != 0) {
			ops.set("status", role.getStatus());
		}
		
		if(0 != role.getLastLoginTime())
			ops.set("lastLoginTime", role.getLastLoginTime());
		
		if(!StringUtil.isEmpty(role.getPromotionUrl())){
			ops.set("promotionUrl", role.getPromotionUrl());
			// 维护群组的推广链接
			Query<Room> query = getRoomDatastore().createQuery(Room.class).field("userId").equal(role.getUserId());
			UpdateOperations<Room> roomOps = getDatastore().createUpdateOperations(Room.class);
			roomOps.set("promotionUrl", role.getPromotionUrl());
			getDatastore().update(query, roomOps);
//			List<Role> roles = getUserRoles(role.getUserId(), null, 0);
			/*Query<Friends> queryFriends = getDatastore().createQuery(Friends.class).field("toUserId").equal(role.getUserId());
			UpdateOperations<Friends> friendsOps = getDatastore().createUpdateOperations(Friends.class);
			ops.set("toFriendsRole", role.getPromotionUrl());
			getDatastore().update(queryFriends, friendsOps);*/
		}
		Role findAndModify = getDatastore().findAndModify(q, ops);
		updateFriend(role.getUserId(),null);
		return findAndModify;
	}
	
	private String promotionUrl(Integer userId){
		/**
		 * 示例：http://www.duoyewu.com/tn/?pid=10000&com=2
			pid后面的数字是你的推广ID,该ID很重要。
			com后面的数字有3个参数：
			1. 直接跳转到招商页面
			2. 直接跳转到首页
			3 .直接跳转到注册页面
		 */
		String promotionUrl = SKBeanUtils.getSystemConfig().getPromotionUrl();
		if(StringUtil.isEmpty(promotionUrl))
			throw new ServiceException("请先在后台管理中设置在线咨询链接");
		return new StringBuffer().append(promotionUrl).append(userId).toString();
	}
	
	// 修改好友关系表中的toUserType,toUserType中只维护0和2;
	public void updateFriend(Integer toUserId,Integer userType){
		List<Integer> roles = getUserRoles(toUserId);
		Query<Friends> query = getDatastore().createQuery(Friends.class).field("toUserId").equal(toUserId);
		UpdateOperations<Friends> ops = getDatastore().createUpdateOperations(Friends.class);
		ops.set("toFriendsRole", roles);
		if(null != userType){
			if(0 == userType){
				Query<Role> delQuery = getDatastore().createQuery(Role.class).field("userId").equal(toUserId);
				if(null != delQuery.get())
					getDatastore().delete(delQuery);
			}
			ops.set("toUserType", userType);
		}
		getDatastore().update(query, ops);
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				// TODO Auto-generated method stub
				List<Integer> queryFansIdByUserId = SKBeanUtils.getFriendsRepository().queryFansIdByUserId(toUserId);
				logger.info("updateFriend === userId "+JSONObject.toJSONString(queryFansIdByUserId));
				queryFansIdByUserId.forEach(userId ->{
					SKBeanUtils.getRedisService().deleteFriends(userId);
				});
			}
		});
	}

	public void deleteAllRoles(Integer userId){
		Datastore datastore = getDatastore();
		Query<Role> query = datastore.createQuery(getEntityClass()).field("userId").equal(userId);
		datastore.delete(query);
	}
}
