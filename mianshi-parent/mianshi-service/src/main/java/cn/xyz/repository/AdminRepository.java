package cn.xyz.repository;

import org.bson.types.ObjectId;

import cn.xyz.mianshi.vo.InviteCode;

public interface AdminRepository {

	InviteCode findUserInviteCode(int userId);


	InviteCode findInviteCodeByCode(String inviteCode);


	boolean delInviteCode( ObjectId inviteCodeId);


	void savaInviteCode(InviteCode inviteCode);
	void editInviteCode(ObjectId id ,String inviteCode,String defaultfriend,String desc);
}
