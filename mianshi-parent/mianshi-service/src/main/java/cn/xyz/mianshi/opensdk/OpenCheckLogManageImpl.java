package cn.xyz.mianshi.opensdk;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Service;

import cn.xyz.commons.support.Callback;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.opensdk.entity.SkOpenCheckLog;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;
@Service
public class OpenCheckLogManageImpl extends MongoRepository<SkOpenCheckLog,ObjectId> {

	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<SkOpenCheckLog> getEntityClass() {
		return SkOpenCheckLog.class;
	}
	
	/** @Description:（保存操作日志） 
	* @param skOpenCheckLog
	**/ 
	public void saveOpenCheckLogs(String accountId, String appId,String operateUser, String status, String reason) {
		SkOpenCheckLog skOpenCheckLog = new SkOpenCheckLog(accountId,appId,operateUser,status,reason);
		ThreadUtil.executeInThread(new Callback() {

			@Override
			public void execute(Object obj) {
				SKBeanUtils.getOpenCheckLogManage().save(skOpenCheckLog);
			}
		});
	}
	
	/**
	 * 审核日志列表
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public PageResult<SkOpenCheckLog> getOpenCheckLogList(int pageIndex,int pageSize){
		Query<SkOpenCheckLog> query=getDatastore().createQuery(SkOpenCheckLog.class);
		PageResult<SkOpenCheckLog> data=new PageResult<>();
		data.setCount(query.count());
		data.setData(query.asList(pageFindOption(pageIndex, pageSize, 1)));
		return data;
	}
	
	/**
	 * 根据Id删除日志
	 * @param id
	 */
	public void delOpenCheckLog(ObjectId id){
		Query<SkOpenCheckLog> query=getDatastore().createQuery(SkOpenCheckLog.class).field("_id").equal(id);
		getDatastore().delete(query);
	}
}
