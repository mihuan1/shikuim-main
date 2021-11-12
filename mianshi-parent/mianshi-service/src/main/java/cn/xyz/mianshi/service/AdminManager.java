package cn.xyz.mianshi.service;


import org.bson.types.ObjectId;

import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.opensdk.entity.SkOpenApp;
import cn.xyz.mianshi.vo.Admin;
import cn.xyz.mianshi.vo.AreaConfig;
import cn.xyz.mianshi.vo.CenterConfig;
import cn.xyz.mianshi.vo.ClientConfig;
import cn.xyz.mianshi.vo.Config;
import cn.xyz.mianshi.vo.InviteCode;
import cn.xyz.mianshi.vo.ServerListConfig;
import cn.xyz.mianshi.vo.SysApiLog;
import cn.xyz.mianshi.vo.TotalConfig;
import cn.xyz.mianshi.vo.UrlConfig;


public interface AdminManager {

	Config getConfig();
	
	ClientConfig getClientConfig();
	
	Config initConfig();
	
	ClientConfig initClientConfig();
	
	void setConfig(Config dbObj);
	
	void setClientConfig(ClientConfig dbObj);

	PageResult<SysApiLog> apiLogList(String keyWorld, int page, int limit) throws Exception;

	void deleteApiLog(String apiLogId,int type);
	
	void addServerList(ServerListConfig server);
	
	PageResult<ServerListConfig> getServerList(ObjectId id,int pageIndex,int limit);
	
	PageResult<ServerListConfig> findServerByArea(String area);
	
	void updateServer(ServerListConfig server);
	
	void deleteServer(ObjectId id);
	
	PageResult<AreaConfig> areaConfigList(String area,int pageIndex,int limit);
	
	void addAreaConfig(AreaConfig areaConfig);
	
	void updateAreaConfig(AreaConfig areaConfig);
	
	void deleteAreaConfig(ObjectId id);
	
	void addUrlConfig(UrlConfig urlConfig);
	
	void deleteUrlConfig(ObjectId id);
	
	PageResult<UrlConfig> findUrlConfig(ObjectId id,String type);
	
	UrlConfig findUrlConfig(String area);
	
	void addCenterConfig(CenterConfig centerConfig);
	
	PageResult<CenterConfig> findCenterConfig(String type,ObjectId id);
	
	CenterConfig findCenterCofigByArea(String clientA,String clientB);
	
	void deleteCenter(ObjectId id);
	
	void addTotalConfig(TotalConfig totalConfig);
	

	void addAdmin(String account, String password, byte role);

	Admin findAdminByAccount(String account);

	void delAdminById(ObjectId adminId);

	Admin modifyAdmin(Admin admin);

	PageResult<Admin> adminList(String keyWorld,ObjectId adminId, int page, int limit);

	Admin findAdminById(ObjectId adminId);
	
	boolean changePasswd(ObjectId adminId, String newPwd);
	
	PageResult<SkOpenApp> openAppList(int status,int type,int pageIndex,int limit,String keyworld);
	
	void createInviteCode(String inviteCode,String defaultfriend,String desc);
	void updateInviteCode (String id ,String inviteCode,String defaultfriend,String desc);
	PageResult<InviteCode> inviteCodeList(String keyworld, String defaultfriend,int page, int limit);

	boolean delInviteCode( String inviteCodeId);
	void updateUserGoogle (String id ,String googlecode);
//	InviteCode findUserPopulInviteCode(int userId);
	
	
}
