package cn.xyz.repository;

import java.util.List;

import org.bson.types.ObjectId;

import cn.xyz.mianshi.model.AddCommentParam;
import cn.xyz.mianshi.vo.Comment;

public interface MsgCommentRepository {

	ObjectId add(int userId, AddCommentParam param);

	boolean delete(ObjectId msgId, String commentId);

	List<Comment> find(ObjectId msgId, ObjectId commentId, int pageIndex, int pageSize);

}
