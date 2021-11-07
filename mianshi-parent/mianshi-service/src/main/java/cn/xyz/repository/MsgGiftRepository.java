package cn.xyz.repository;

import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.DBObject;

import cn.xyz.mianshi.model.AddGiftParam;
import cn.xyz.mianshi.vo.Givegift;

public interface MsgGiftRepository {

	/**
	 * 新增礼物
	 * 
	 * @param userId
	 * @param msgId
	 * @param paramList
	 * @return
	 */
	List<ObjectId> add(Integer userId, ObjectId msgId, List<AddGiftParam> paramList);

	/**
	 * 根据礼物分组
	 * 
	 * @param msgId
	 * @return
	 */
	List<DBObject> findByGift(ObjectId msgId);

	/**
	 * 根据用户分组
	 * 
	 * @param msgId
	 * @return
	 */
	List<DBObject> findByUser(ObjectId msgId);

	List<Givegift> find(ObjectId msgId, ObjectId giftId, int pageIndex, int pageSize);

}
