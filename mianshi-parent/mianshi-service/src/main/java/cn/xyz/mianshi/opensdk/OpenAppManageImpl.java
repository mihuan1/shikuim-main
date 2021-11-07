package cn.xyz.mianshi.opensdk;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.DesUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.opensdk.entity.OpenLoginInfo;
import cn.xyz.mianshi.opensdk.entity.SkOpenApp;
import cn.xyz.mianshi.opensdk.until.SkOpenUtil;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.GroupHelper;
import cn.xyz.mianshi.vo.Helper;
import cn.xyz.mianshi.vo.Room;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
@Service
public class OpenAppManageImpl extends MongoRepository<SkOpenApp,ObjectId> {

	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<SkOpenApp> getEntityClass() {
		return SkOpenApp.class;
		
	}
	/**
	 * 创建应用
	 * @param skOpenApp
	 * @return
	 */
	public synchronized SkOpenApp createApp(SkOpenApp skOpenApp) {
		Query<SkOpenApp> query = getDatastore().createQuery(getEntityClass());
		if (!StringUtil.isEmpty(skOpenApp.getAppName())) {
			query.field("appName").equal(skOpenApp.getAppName());
			query.field("appType").equal(skOpenApp.getAppType());
			if (null != query.get())
				throw new ServiceException("已经存在该名称的应用，基于应用名称唯一原则，请重新提交一个新名称。如果你认为已有名称侵犯了你的合法权益，可以进行侵权投诉");
		}
		SkOpenApp entity = new SkOpenApp(skOpenApp);
		getDatastore().save(entity);
		SKBeanUtils.getOpenCheckLogManage().saveOpenCheckLogs(query.get().getAccountId(), null, query.get().getAccountId(), String.valueOf(0), null);
		return entity;
	}
	
	/**
	 * 删除应用
	 * @param id
	 */
	public void deleteAppById(ObjectId id,String accountId){
		Query<SkOpenApp> query=getDatastore().createQuery(SkOpenApp.class).field("_id").equal(id).field("accountId").equal(accountId);
		
		Query<Helper> helQuery = getDatastore().createQuery(Helper.class).field("openAppId").equal(id.toString());
		if(null != helQuery){
			for(Helper heObject:helQuery.asList()){
				Query<GroupHelper> groupHelQuery = getDatastore().createQuery(GroupHelper.class).field("helperId").equal(heObject.getId().toString());
				getDatastore().delete(groupHelQuery);
			}
		}
		getDatastore().delete(helQuery);
		getDatastore().delete(query);
		
	}
	
	/**
	 * app列表
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public List<SkOpenApp> appList(String userId,Integer type,Integer pageIndex,Integer pageSize){
		Query<SkOpenApp> query=getDatastore().createQuery(SkOpenApp.class).field("accountId").equal(userId).field("appType").equal(type);
		return query.asList(pageFindOption(pageIndex, pageSize,0));
	}
	
	/**
	 * app详情
	 * @param id
	 * @return
	 */
	public SkOpenApp appInfo(ObjectId id){
		Query<SkOpenApp> query=getDatastore().createQuery(SkOpenApp.class).field("_id").equal(id);
		return query.get();
	}
	
	/**
	 *  通过审核/申请获取权限 
	 * @param openAccountId
	 * @param skOpenApp
	 */
	public void openAccess(SkOpenApp skOpenApp){
		Query<SkOpenApp> query=getDatastore().createQuery(SkOpenApp.class).field("_id").equal(skOpenApp.getId());
		if(!StringUtil.isEmpty(skOpenApp.getAccountId()))
			query.field("accountId").equal(skOpenApp.getAccountId());
		UpdateOperations<SkOpenApp> ops=getDatastore().createUpdateOperations(getEntityClass());
		if(skOpenApp.getIsAuthShare()!=0)
			ops.set("isAuthShare", skOpenApp.getIsAuthShare());
		if(skOpenApp.getIsAuthLogin()!=0)
			ops.set("isAuthLogin", skOpenApp.getIsAuthLogin());
		if(skOpenApp.getIsAuthPay()!=0){
			ops.set("isAuthPay", skOpenApp.getIsAuthPay());
		}
		if(skOpenApp.getIsGroupHelper()!=0){
			ops.set("isGroupHelper", skOpenApp.getIsGroupHelper());
		}
		if(!StringUtil.isEmpty(skOpenApp.getHelperName())){
			ops.set("helperName", skOpenApp.getHelperName());
		}
		if(!StringUtil.isEmpty(skOpenApp.getHelperDesc())){
			ops.set("helperDesc", skOpenApp.getHelperDesc());
		}
		if(!StringUtil.isEmpty(skOpenApp.getHelperDeveloper())){
			ops.set("helperDeveloper", skOpenApp.getHelperDeveloper());
		}
		if(!StringUtil.isEmpty(skOpenApp.getPayCallBackUrl())){
			ops.set("payCallBackUrl", skOpenApp.getPayCallBackUrl());
		}
			
		getDatastore().update(query, ops);
	}
	
	/**
	 * 通过审核、禁用 APP
	 * @param id
	 */
	public void approvedAPP(String id,Integer status,String userId,String reason){
		Query<SkOpenApp> query=getDatastore().createQuery(getEntityClass()).field("_id").equal(new ObjectId(id));
		UpdateOperations<SkOpenApp> ops=getDatastore().createUpdateOperations(getEntityClass());
		ops.set("status", status);
		String appId=null;
		if(!StringUtil.isEmpty(query.get().getAppId())){
			appId=query.get().getAppId();
		}
		if(1 == status){
			appId = SkOpenUtil.getAppId();
			ops.set("appId", appId);
			ops.set("appSecret", SkOpenUtil.getAppScrect(appId));
		}
		
		getDatastore().update(query, ops);
		SKBeanUtils.getOpenCheckLogManage().saveOpenCheckLogs(query.get().getAccountId(),appId,userId,String.valueOf(status),reason);
		
	}
	
	public void allowAccess(){
		
	}
	
	// 校验授权app
	public int authorization(String appId,String appSecret){
		int flag = 0;
		Query<SkOpenApp> query = createQuery().field("appId").equal(appId);
		if(null == query.get())
			throw new ServiceException("APP不存在！");
		if(null != query.get() && query.get().getStatus() < 1)
			throw new ServiceException("APP状态异常！");
		if(query.get().getAppSecret().equals(appSecret)){
			flag = 1;
			return flag;
		}else{
			throw new ServiceException("授权校验失败！");
		}
	}
	
	public void authInterfaceWeb(String appId,int type){
		Query<SkOpenApp> query=getDatastore().createQuery(SkOpenApp.class).field("appId").equal(appId);
		if(null == query.get())
			throw new ServiceException("该第三方网站平台尚未申请");
		switch (type) {
		case 1:
			if(query.get().getIsAuthLogin()!=1){
				throw new ServiceException("登录权限未开通");
			}
			break;
		case 2:
			if(query.get().getIsAuthShare()!=1){
				throw new ServiceException("分享权限未开通");
			}
			break;
		case 3:
			if(query.get().getIsAuthPay()!=1){
				throw new ServiceException("支付权限未开通");
			}
			break;
		default:
			break;
		}
		
	}
	
	public int authInterface(String appId,String appSecret,int type){
		int flag=0;
		Query<SkOpenApp> query=SKBeanUtils.getDatastore().createQuery(SkOpenApp.class).field("appId").equal(appId).field("appSecret").equal(appSecret);
		SkOpenApp openApp = query.get();
		switch (type) {
		case 1:
			if(openApp.getIsAuthLogin()!=(byte)1){
				throw new ServiceException("登录权限未开通");
			}else{
				flag=1;
				
			}
			break;
		case 2:
			if(openApp.getIsAuthShare()!=(byte)1){
				throw new ServiceException("分享权限未开通");
			}else{
				flag=1;
				
			}
			break;
		case 3:
			if(openApp.getIsAuthPay()!=(byte)1){
				throw new ServiceException("支付权限未开通");
			}else{
				flag=1;
				
			}
			break;
		default:
			break;
		}
		return flag;
		
	}
	
	public Map<String, String> authorizeUrl(String appId,String callbackUrl){
		Query<SkOpenApp> query=getDatastore().createQuery(SkOpenApp.class).field("appId").equal(appId);
		SkOpenApp skOpenWeb = query.get();
		if(null == skOpenWeb)
			throw new ServiceException("该网站未在开放平台尚未申请");
		if(!callbackUrl.equals(skOpenWeb.getCallbackUrl()))
			throw new ServiceException("回调地址和申请填写的回调地址不符合");
		Map<String, String> webInfo = Maps.newConcurrentMap();
		webInfo.put("webAppName", skOpenWeb.getAppName());
		webInfo.put("webAppsmallImg", skOpenWeb.getAppsmallImg());
		return webInfo;
	}
	
	public String codeAuthorCheckImpl(String appId,String state) throws Exception{
		Query<SkOpenApp> query=getDatastore().createQuery(SkOpenApp.class).field("appId").equal(appId);
		if(null == query.get())
			throw new ServiceException("该应用未在开放平台尚未申请");
		String time = String.valueOf(DateUtil.currentTimeSeconds());
		String userId = KSessionUtil.getUserIdBytoken(state);
		if(StringUtil.isEmpty(userId))
			throw new ServiceException("state无效");
//		String key = time.substring(2, time.length());
		String desUserId = DesUtil.encrypt(userId, time);
		// 规则code = appId位数 + appId + time + des(userId)
		String str = appId.length()+appId+time+desUserId;
		String encodeBuffer = (new BASE64Encoder()).encodeBuffer(str.getBytes());  
		return encodeBuffer;
	}
	
	public Map<String,String> codeOauthImpl(String code) throws Exception {
		// base64解密 
		long currTime = DateUtil.currentTimeSeconds();
		byte[] decodeBuffer = (new BASE64Decoder()).decodeBuffer(code);  
		logger.info("=== 解密 code === : "+decodeBuffer.toString());
		// 规则code = appId位数 + appId + time + des(userId)
		String strByte = new String(decodeBuffer);
		// 时间容错三分钟：18sk598d214577e943e41551404075s1hkrmNjyhiau95u5Rp4DQ==
		String codeTime = strByte.substring(20, 20+(String.valueOf(currTime).length()));
		Long endTime = Long.valueOf(codeTime);
		if((currTime-endTime) > 180 || currTime-endTime < -180){
			logger.info(String.format("====> codeOauthImpl error server > %s client %s", currTime,endTime));
			throw new ServiceException("参数 code 已过期失效");
		} 
		String appId = strByte.substring(2, 20);
		Query<SkOpenApp> query = getDatastore().createQuery(SkOpenApp.class).field("appId").equal(appId);
		if(null == query.get())
			throw new ServiceException("应用不存在！");
		String desUserId = strByte.substring(30, strByte.length());
		// des解密userId
		User user = SKBeanUtils.getUserManager().getUser(Integer.valueOf(DesUtil.decrypt(desUserId, codeTime)));
		if(null == user)
			throw new ServiceException("暂无授权用户");
		// 数据存档
		Query<OpenLoginInfo> find = getDatastore().createQuery(OpenLoginInfo.class).field("userId").equal(user.getUserId());
		String openId = null;
		if(null == find.get()){
			// openId = base64(userId 位数+userId+sk) 
			String substrAppId = appId.substring(2, appId.length());
			String openIdByte = user.getUserId().toString().length()+user.getUserId().toString()+substrAppId;
			logger.info("==== openId ====  length: "+user.getUserId().toString().length() + " userId : "+user.getUserId().toString()+"  subSK: "+substrAppId);
			logger.info("==== base64 ====  "+openIdByte);
			openId = (new BASE64Encoder()).encodeBuffer(openIdByte.getBytes());// base64
			OpenLoginInfo loginInfo = new OpenLoginInfo(user.getUserId(), openId, appId, query.get().getAppName(), 1);
			getDatastore().save(loginInfo);
		}else{
			openId = find.get().getOpenId();
		}
		Map<String,String> map = Maps.newConcurrentMap();
		map.put("nickName", user.getNickname());
		map.put("sex", user.getSex().toString());
		map.put("birthday",user.getBirthday().toString());
		map.put("provinceId", user.getProvinceId().toString());
		map.put("cityId", user.getCityId().toString());
		//var imgUrl = AppConfig.avatarBase + (parseInt(userId) % 10000) + "/" + userId + ".jpg";
		String imgUrl = SKBeanUtils.getAdminManager().getClientConfig().getDownloadAvatarUrl();
		String userImgUrl = imgUrl+"avatar/o/"+(user.getUserId() % 10000) + "/" + user.getUserId() + ".jpg";
		map.put("image", userImgUrl);
		map.put("openId", openId);
		return  map;
	}
	
	public SkOpenApp getOpenAppByAppId(String appId){
		Query<SkOpenApp> query = getDatastore().createQuery(SkOpenApp.class).field("appId").equal(appId);
		return query.get();
	}
	
	/**
	 * 添加群助手
	 * @param entity
	 */
	public void addHelper(Helper entity){
		entity.setCreateTime(DateUtil.currentTimeSeconds());
		SKBeanUtils.getDatastore().save(entity);
	}
	
	/**
	 * 获取群助手列表
	 * @param openAppId
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public List<Helper> getHelperList(String openAppId,Integer pageIndex,Integer pageSize){
		Query<Helper> query = SKBeanUtils.getDatastore().createQuery(Helper.class);
		if(!StringUtil.isEmpty(openAppId)){
			query.field("openAppId").equal(openAppId);
		}
		return query.asList(pageFindOption(pageIndex, pageSize,0));
	}
	
	public void updateHelper(Helper entity){
		Query<Helper> query = SKBeanUtils.getDatastore().createQuery(Helper.class).field("_id").equal(entity.getId());
		UpdateOperations<Helper> ops = SKBeanUtils.getDatastore().createUpdateOperations(Helper.class);
		if(!StringUtil.isEmpty(entity.getName())){
			ops.set("name", entity.getName());
		}
		if(!StringUtil.isEmpty(entity.getDesc())){
			ops.set("desc", entity.getDesc());
		}
		if(!StringUtil.isEmpty(entity.getDeveloper())){
			ops.set("developer", entity.getDeveloper());
		}
		if(!StringUtil.isEmpty(entity.getIconUrl())){
			ops.set("iconUrl", entity.getIconUrl());
		}
		if(!StringUtil.isEmpty(entity.getLink())){
			ops.set("link", entity.getLink());
		}
		if(!StringUtil.isEmpty(entity.getAppPackName())){
			ops.set("appPackName", entity.getAppPackName());
		}
		if(!StringUtil.isEmpty(entity.getCallBackClassName())){
			ops.set("callBackClassName", entity.getCallBackClassName());
		}
		if(null!=entity.getOther()){
			ops.set("other", entity.getOther());
		}
		ops.set("type", entity.getType());
		
		SKBeanUtils.getDatastore().update(query, ops);
	}
	
	/**
	 * 删除群助手
	 * @param id
	 */
	public void deleteHelper(Integer userId,ObjectId id){
		Query<Helper> query = SKBeanUtils.getDatastore().createQuery(Helper.class).field("_id").equal(id);
		if(null != query.get()){
			System.out.println(query.get().getOpenAppId());
			Query<SkOpenApp> queryOpenApp = SKBeanUtils.getDatastore().createQuery(SkOpenApp.class).field("_id").equal(new ObjectId(query.get().getOpenAppId()));
			if(queryOpenApp.get()!=null){
				// 判断是否为本人操作
				if(Integer.valueOf(queryOpenApp.get().getAccountId()).equals(userId)){
					
					Query<GroupHelper> groupHelQuery = SKBeanUtils.getDatastore().createQuery(GroupHelper.class).field("helperId").equal(query.get().getId().toString());
					SKBeanUtils.getDatastore().delete(groupHelQuery);
					
					SKBeanUtils.getDatastore().delete(query);
				}
			}else{
				throw new ServiceException("应用不存在，删除失败");
			}
			
		}else {
			throw new ServiceException("群助手不存在，删除失败");
		}
	}
	
	/**
	 * 发送群助手消息
	 * @param roomId
	 * @param userId
	 * @param title
	 * @param desc
	 * @param imgUrl
	 * @param type
	 * @param url
	 */
	public JSONMessage sendMsgByGroupHelper(String roomId,Integer userId,@RequestParam(defaultValue="") String title,
			@RequestParam(defaultValue="") String desc,String imgUrl,Integer type,@RequestParam(defaultValue="") String url,String appId){
		Query<GroupHelper> query = SKBeanUtils.getDatastore().createQuery(GroupHelper.class).field("roomId").equal(roomId);
		GroupHelper groupHelper = null;
		if(null==query.get()){
			return JSONMessage.failure("群助手不存在");
		}else{
			groupHelper = query.get();
		}
		Room room = SKBeanUtils.getRoomManager().get(new ObjectId(roomId));
		if(null==room){
			return JSONMessage.failure("房间不存在");
		}
		User user = SKBeanUtils.getUserManager().get(userId);
		if(user==null){
			return JSONMessage.failure("用户不存在");
		}
		
		SkOpenApp openApp = SKBeanUtils.getOpenAppManage().getOpenAppByAppId(appId);
		if(openApp==null){
			return JSONMessage.failure("该应用未在第三方平台注册");
		}
		JSONObject body = new JSONObject();
		
		MessageBean messageBean=new MessageBean();
		if(type==1){// 发送图文
			body.put("appName", openApp.getAppName());
			body.put("appIcon", openApp.getAppImg());
			body.put("title", title);
			body.put("subTitle", desc);
			body.put("imageUrl", imgUrl);
			body.put("url", url);
			messageBean.setObjectId(body.toString());
			messageBean.setType(87);
		}else if(type==2){// 发送图片
			messageBean.setContent(imgUrl);
			messageBean.setType(2);
		}
		
		messageBean.setFromUserId(user.getUserId().toString());
		messageBean.setFromUserName(user.getNickname());
		messageBean.setRoomJid(groupHelper.getRoomJid());
		
		// 发送群聊通知
		try {
			KXMPPServiceImpl.getInstance().sendMsgToGroupByJid(groupHelper.getRoomJid(),messageBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSONMessage.success();
	}
}
