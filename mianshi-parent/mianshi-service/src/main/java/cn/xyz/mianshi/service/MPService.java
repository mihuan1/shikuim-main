package cn.xyz.mianshi.service;

public interface MPService {

	Object getMsgList(int userId, int pageIndex, int pageSize);

	Object getMsgList(int sender, int receiver, int pageIndex, int pageSize);
}
