package cn.xyz.mianshi.service;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;

import cn.xyz.mianshi.model.UserExample;
import cn.xyz.mianshi.model.UserQueryExample;
import cn.xyz.mianshi.vo.Course;
import cn.xyz.mianshi.vo.CourseMessage;
import cn.xyz.mianshi.vo.Emoji;
import cn.xyz.mianshi.vo.SdkLoginInfo;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.WxUser;

public interface UserManager {

	User createUser(String telephone, String password);

	void createUser(User user);

	User.UserSettings getSettings(int userId);

	User getUser(int userId);
	
	User getUser(int userId, int toUserId);

	User getUser(String telephone);
	
	String getNickName(int userId);

	int getUserId(String accessToken);

	boolean isRegister(String telephone);

	User login(String telephone, String password);

	Map<String, Object> login(UserExample example);
	
	Map<String, Object> loginAuto(String access_token, int userId, String serial,
			                       String appId,double latitude,double longitude);

	void logout(String access_token,String areaCode,String userKey,String deviceKey);
	
	void outtime(String access_token,int userId);

	List<DBObject> query(UserQueryExample param);

	Map<String, Object> register(UserExample example);
	
	
	Map<String, Object> registerIMUser(UserExample example) throws Exception;
	
	Map<String, Object> registerIMUser(UserExample example,int type,String loginInfo) throws Exception;
	
	void addUser(int userId,String password);

	void resetPassword(String telephone, String password);

	void updatePassword(int userId, String oldPassword, String newPassword);

	User updateSettings(int userId,User.UserSettings userSettings);

	User updateUser(int userId, UserExample example);

	List<DBObject> findUser(int pageIndex, int pageSize);

	List<Integer> getAllUserId();
	
	//消息免打扰
	User updataOfflineNoPushMsg(int userId,int OfflineNoPushMsg);
	
	// 添加收藏
	List<Object> addCollection(int userId,String roomJid,String msgId,String type);
	
	// 添加收藏表情
	Object addEmoji(int userId,String url,String type);
	
	// 收藏列表
	List<Emoji> emojiList(int userId,int type,int pageSize,int pageIndex);
	
	List<Emoji> emojiList(int userId);
	
	//取消收藏
	void deleteEmoji(Integer userId,String emojiId);
	
	//添加消息课程
	void addMessageCourse(int userId,List<String> messageIds,long createTime,String courseName,String roomJid);
	
	//通过userId获取用户课程
	List<Course> getCourseList(int userId);
	
	//修改课程
	void updateCourse(Course course,String courseMessageId);
	
	//删除课程
	boolean deleteCourse(Integer userId,ObjectId courseId);
	
	//发送课程
	List<CourseMessage> getCourse(String courseId);
	
	//void updateContent(ObjectId courseMessageId);
	//添加微信公众号用户
	WxUser addwxUser(JSONObject jsonObject);
	
	JSONObject getWxOpenId(String code);
	
	String getWxToken();
	
	Integer createInviteCodeNo(int createNum);
	
	SdkLoginInfo addSdkLoginInfo(int type,Integer userId,String loginInfo);
	
	SdkLoginInfo findSdkLoginInfo(int type,String loginInfo);
}
