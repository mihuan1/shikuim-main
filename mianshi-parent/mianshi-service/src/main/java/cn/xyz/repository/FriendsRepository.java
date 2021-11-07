package cn.xyz.repository;

import java.util.List;

import cn.xyz.mianshi.vo.Friends;

public interface FriendsRepository {




	Friends deleteFriends(int userId, int toUserId);
	void deleteFriends(int userId);


	Friends getFriends(int userId, int toUserId);

	List<Friends> queryBlacklist(int userId,int pageIndex,int pageSize);


	List<Integer> queryFansId(int userId);

	List<Friends> queryFollow(int userId,int status);

	List<Integer> queryFollowId(int userId);

	List<Friends> queryFriends(int userId);
	
	List<Friends> friendsOrBlackList(int userId,String type);




	Object saveFriends(Friends friends);

	Friends updateFriends(Friends friends);


}
