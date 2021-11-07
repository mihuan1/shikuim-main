package cn.xyz.mianshi.service;

import java.util.List;

import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.PageVO;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.NewFriends;

public interface FriendsManager {

	Friends addBlacklist(Integer userId, Integer toUserId);

	
	void deleteFansAndFriends(int userId);
	boolean addFriends(Integer userId, Integer toUserId);

	Friends deleteBlacklist(Integer userId, Integer toUserId);

	boolean deleteFriends(Integer userId, Integer toUserId);

	JSONMessage followUser(Integer userId, Integer toUserId, Integer fromAddType);
	
	JSONMessage batchFollowUser(Integer userId, String toUserId);


	Friends getFriends(Friends friends);
	
	public Friends getFriends(int userId, int toUserId);
	
	List<Integer> getFriendsIdList(int userId);
	

	List<Friends> queryBlacklist(Integer userId,int pageIndex, int pageSize);

	List<Integer> queryFansId(Integer userId);

	List<Friends> queryFollow(Integer userId,int status);

	List<Integer> queryFollowId(Integer userId);

	List<Friends> queryFriends(Integer userId);

	PageVO queryFriends(Integer userId,int status,String keyword, int pageIndex, int pageSize);


	boolean unfollowUser(Integer userId, Integer toUserId);

	Friends updateRemark(int userId, int toUserId, String remarkName ,String describe);
	
	List<NewFriends> newFriendList(int userId,int pageIndex,int pageSize);

	List<Integer> friendsAndAttentionUserId(Integer userId, String type);
	// 消息免打扰、阅后即焚、聊天置顶
	Friends updateOfflineNoPushMsg(int userId,int toUserId,int offlineNoPushMsg ,int type);
}
