package cn.xyz.mianshi.service.impl;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.ErrorMessage;
@Service
public class ErrorMessageManageImpl extends MongoRepository<ErrorMessage,ObjectId> {

	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<ErrorMessage> getEntityClass() {
		return ErrorMessage.class;
	}
	
	/** @Description:（查询所有错误提示） 
	* @param keyword
	* @param pageIndex
	* @param pageSize
	* @return
	**/ 
	public Map<Long, List<ErrorMessage>> findErrorMessage(String keyword,int pageIndex,int pageSize){
		Map<Long, List<ErrorMessage>> map = Maps.newConcurrentMap();
		Query<ErrorMessage> query=getDatastore().createQuery(getEntityClass());
		if(!StringUtil.isEmpty(keyword)){
			query.filter("code", keyword);
		}
		query.offset(pageSize * pageIndex);
		List<ErrorMessage> errorMessages = query.limit(pageSize).asList();
		map.put(query.count(), errorMessages);
		return map;
	}
	
	public boolean deleteErrorMessage(String code){
		Query<ErrorMessage> q=getDatastore().createQuery(getEntityClass());
		q.filter("code", code);
		getDatastore().delete(q);
		return true;
	}
	
	public JSONMessage saveErrorMessage(ErrorMessage errorMessage){
		Query<ErrorMessage> q=getDatastore().createQuery(getEntityClass());
		long count = q.filter("code", errorMessage.getCode()).count();
		if(count>=1)
			throw new ServiceException("当前code已被注册");
		save(errorMessage);
		return JSONMessage.success();
	}
	
	//修改提示消息
	public ErrorMessage updataErrorMessage(String id,ErrorMessage errorMessage) {
		Query<ErrorMessage> q=getDatastore().createQuery(getEntityClass()).field("_id").equal(new ObjectId(id));
		UpdateOperations<ErrorMessage> ops = getDatastore().createUpdateOperations(getEntityClass());
		logger.info(errorMessage.toString());
		
		ops.set("code", (null == errorMessage.getCode()?q.get().getCode():errorMessage.getCode()));
		ops.set("type", (null == errorMessage.getType()?q.get().getType():errorMessage.getType()));
		ops.set("zh", (null == errorMessage.getZh()?q.get().getZh():errorMessage.getZh()));
		ops.set("en", (null == errorMessage.getEn()?q.get().getEn():errorMessage.getEn()));
		ops.set("big5", (null == errorMessage.getBig5()?q.get().getBig5():errorMessage.getBig5()));
		return getDatastore().findAndModify(q, ops);
	}
}
