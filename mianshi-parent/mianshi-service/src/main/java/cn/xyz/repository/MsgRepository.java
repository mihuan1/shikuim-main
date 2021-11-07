package cn.xyz.repository;

import java.util.List;

import org.bson.types.ObjectId;

import cn.xyz.mianshi.model.AddMsgParam;
import cn.xyz.mianshi.model.MessageExample;
import cn.xyz.mianshi.vo.Msg;

public interface MsgRepository {

//	ObjectId add(int userId, AddMsgParam param);
	Msg add(int userId, AddMsgParam param);

	boolean delete(String... msgId);

	List<Msg> gets(int userId, String ids);

	/**
	 * 获取用户最新商务圈消息
	 * 
	 * @param userId
	 * @param toUserId
	 * @param msgId
	 * @param pageSize
	 * @return
	 */
	List<Msg> getUserMsgList(Integer userId, Integer toUserId, ObjectId msgId, Integer pageSize);

	/**
	 * 获取用户最新商务圈消息
	 * 
	 * @param userId
	 * @param msgId
	 * @param pageSize
	 * @return
	 */
	List<Msg> findByUser(Integer userId,Integer toUserId, ObjectId msgId, Integer pageSize);

	/**
	 * 获取当前登录用户及其所关注用户的最新商务圈消息
	 * 
	 * @param userId
	 * @param toUserId
	 * @param msgId
	 * @param pageSize
	 * @return
	 */
	List<Msg> getMsgList(Integer userId, Integer toUserId, ObjectId msgId, Integer pageSize,Integer pageIndex);

	/**
	 * 获取用户最新商务圈消息Id
	 * 
	 * @param userId
	 * @param toUserId
	 * @param msgId
	 * @param pageSize
	 * @return
	 */
	List<Msg> getUserMsgIdList(int userId, int toUserId, ObjectId msgId, int pageSize);

	/**
	 * 获取当前登录用户及其所关注用户的最新商务圈消息Id
	 * 
	 * @param userId
	 * @param toUserId
	 * @param msgId
	 * @param pageSize
	 * @return
	 */
	List<Msg> getMsgIdList(int userId, int toUserId, ObjectId msgId, int pageSize);

	List<Msg> findByExample(int userId, MessageExample example);

	boolean forwarding(Integer userId, AddMsgParam param);

	Msg get(int userId, ObjectId msgId);

	List<Msg> getSquareMsgList(int userId, ObjectId msgId, Integer pageSize);

	void update(ObjectId msgId, Msg.Op op, int activeValue);
	
}
