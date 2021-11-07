package cn.xyz.repository;

import java.util.List;

import org.bson.types.ObjectId;

import cn.xyz.mianshi.vo.Praise;

public interface MsgPraiseRepository {

	ObjectId add(int userId, ObjectId msgId);

	boolean delete(int userId, ObjectId msgId);

	List<Praise> find(ObjectId msgId, ObjectId praiseId, int pageIndex, int pageSize);

	boolean exists(int userId, ObjectId msgId);

}
