package cn.xyz.mianshi.opensdk;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.opensdk.entity.SkOpenApp;
import cn.xyz.mianshi.opensdk.until.SkOpenUtil;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;
@Service
public class OpenWebManageImpl extends MongoRepository<SkOpenApp, ObjectId>{

	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<SkOpenApp> getEntityClass() {
		// TODO Auto-generated method stub
		return SkOpenApp.class;
	}


	
	/**
	 * 申请获取权限
	 * @param skOpenWeb
	 */
	public void openAccess(SkOpenApp skOpenWeb){
		Query<SkOpenApp> query=SKBeanUtils.getDatastore().createQuery(SkOpenApp.class).field("_id").equal(skOpenWeb.getId());
		if(!StringUtil.isEmpty(skOpenWeb.getAccountId()))
			query.field("accountId").equal(skOpenWeb.getAccountId());
		UpdateOperations<SkOpenApp> ops=getDatastore().createUpdateOperations(getEntityClass());
		if(skOpenWeb.getIsAuthShare()!=0)
			ops.set("isAuthShare", skOpenWeb.getIsAuthShare());
		if(skOpenWeb.getIsAuthLogin()!=0)
			ops.set("isAuthLogin", skOpenWeb.getIsAuthLogin());
		if(skOpenWeb.getIsAuthPay()!=0){
			ops.set("isAuthPay", skOpenWeb.getIsAuthPay());
			ops.set("payCallBackUrl", skOpenWeb.getPayCallBackUrl());
		}
			
		SKBeanUtils.getDatastore().update(query, ops);
	}
	
	// 校验网页APP
	public SkOpenApp checkWebAPPByAppId(String appId){
		Query<SkOpenApp> query = SKBeanUtils.getDatastore().createQuery(SkOpenApp.class).field("appId").equal(appId);
		return query.get();
	}
}
