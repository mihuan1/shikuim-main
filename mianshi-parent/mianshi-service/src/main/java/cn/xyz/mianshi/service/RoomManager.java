package cn.xyz.mianshi.service;

import java.util.List;

import org.bson.types.ObjectId;

import cn.xyz.mianshi.vo.Room;
import cn.xyz.mianshi.vo.Room.Member;
import cn.xyz.mianshi.vo.Room.Share;
import cn.xyz.mianshi.vo.User;

public interface RoomManager {
	public static final String BEAN_ID = "RoomManagerImpl";

	Room add(User user, Room room, List<Integer> idList);

	void delete( ObjectId roomId,Integer userId);

//	JSONMessage update(User user, RoomVO roomVO,int isAdmin);

	Room get(ObjectId roomId,int userId,Integer pageIndex,Integer pageSize);

	Room get(String roomJid,int userId);
	 
	Room exisname(Object roomname,ObjectId roomId);

	List<Room> selectList(int pageIndex, int pageSize, String roomName);

	Object selectHistoryList(int userId, int type);

	Object selectHistoryList(int userId, int type, int pageIndex, int pageSize);

	void deleteMember(User user, ObjectId roomId, int userId);

	void updateMember(User user, ObjectId roomId, Room.Member member);

	void updateMember(User user, ObjectId roomId, List<Integer> idList);
	
	void Memberset(Integer offlineNoPushMsg,ObjectId roomId,int userId,int type);

	Member getMember(ObjectId roomId, int userId);

	List<Room.Member> getMemberList(ObjectId roomId,Integer userId,String keyword);

	void join(int userId, ObjectId roomId, int type);

	void setAdmin(ObjectId roomId,int touserId,int type,int userId);

	Share Addshare(ObjectId roomId,long size,int type ,int userId, String url,String name);
	
	List<Room.Share> findShare(ObjectId roomId,long time,int userId,int pageIndex,int pageSize);
	
	Object getShare(ObjectId roomId,ObjectId shareId);
	
	void deleteShare(ObjectId roomId,ObjectId shareId,int userId);
	
	String getCall(ObjectId roomId);
	
	String getVideoMeetingNo(ObjectId roomId);

	Long countRoomNum();
}
