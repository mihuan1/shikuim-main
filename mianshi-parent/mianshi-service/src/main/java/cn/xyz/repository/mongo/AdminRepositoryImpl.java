package cn.xyz.repository.mongo;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.StringUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import com.mongodb.WriteResult;

import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.InviteCode;
import cn.xyz.repository.AdminRepository;

@Service
public class AdminRepositoryImpl implements AdminRepository {
	
	
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}


	
	public static AdminRepositoryImpl getInstance() {
		return new AdminRepositoryImpl();
	}

	
	/**
	 * 查询某个用户的推广型的邀请码
	 * @param userId
	 * @return
	 */
	@Override
	public InviteCode findUserInviteCode(int userId) {
		 return getDatastore().createQuery(InviteCode.class).field("userId").equal(userId).field("totalTimes").notEqual(1).get();
	}
	
	
	/**
	 *通过邀请码字符查找对应的邀请码记录
	 */
	@Override
	public InviteCode findInviteCodeByCode(String inviteCode){
		return getDatastore().createQuery(InviteCode.class).field("inviteCode").equal(inviteCode).get();
	}
	
	
	/*
	 * 保存邀请码记录
	 */
	@Override
	public void savaInviteCode(InviteCode inviteCode) {
		getDatastore().save(inviteCode);
	}
	 
	/**
	 * 删除邀请码
	 */
	@Override
	public boolean delInviteCode(ObjectId inviteCodeId) {
		Query<InviteCode> q = getDatastore().createQuery(InviteCode.class).field("_id").equal(inviteCodeId);
		WriteResult inviteCode =  getDatastore().delete(q);
		return !(inviteCode==null);
	}
	@Override
	public void editInviteCode(ObjectId id ,String inviteCode,String defaultfriend,String desc) {
		Query<InviteCode> q = getDatastore().createQuery(InviteCode.class).field("_id").equal(id);
		UpdateOperations<InviteCode> ops= getDatastore().createUpdateOperations(InviteCode.class);
		if(q != null){
			if(!StringUtil.isEmpty(inviteCode)){
				ops.set("inviteCode",inviteCode);
			}
			if(!StringUtil.isEmpty(defaultfriend)){
				ops.set("defaultfriend",defaultfriend);
			}
			if(!StringUtil.isEmpty(desc)){
				ops.set("desc",desc);
			}
			getDatastore().findAndModify(q, ops);
		}else {
			throw new ServiceException("数据不存在！");
		}
	}
}
