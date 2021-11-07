package cn.xyz.repository.mongo;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.CommonText;
import cn.xyz.mianshi.vo.Company;
import cn.xyz.mianshi.vo.Customer;
import cn.xyz.mianshi.vo.Employee;
import cn.xyz.repository.CustomerRepository;

/**
 * 客服模块数据操纵接口的实现
 * @author hsg
 *
 */

@Service
public class CustomerRepositoryImpl extends MongoRepository<Company, ObjectId> implements CustomerRepository {
	
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<Company> getEntityClass() {
		return Company.class;
	}
	
	public static CustomerRepositoryImpl getInstance(){
		return new CustomerRepositoryImpl();
	}
	
	@Override
	public Map<String, Object> addCustomer(Customer customer) {
		BasicDBObject jo = new BasicDBObject();
		jo.put("customerId", customer.getCustomerId());// 索引
		jo.put("userKey", DigestUtils.md5Hex(customer.getIp()));
		jo.put("ip",customer.getIp());// 索引
		jo.put("macAddress","");
		jo.put("createTime", DateUtil.currentTimeSeconds());
		jo.put("companyId", customer.getCompanyId());
		
		// 1、新增客户记录
		getDatastore().getDB().getCollection("customer").save(jo);
		
		try {
			//2、缓存用户认证数据到
			Map<String, Object> data =KSessionUtil.loginSaveAccessToken(customer.getCustomerId(),customer.getCustomerId(), null);
			data.put("customerId", customer.getCustomerId());
			//data.put("nickname",jo.getString("nickname"));
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	
	@Override
	public Integer findUserByIp(String ip) {
		
		Query<Customer> query = getDatastore().createQuery(Customer.class);
		if (!StringUtil.isEmpty(ip)){
			query.field("userKey").equal(DigestUtils.md5Hex(ip));
		}
		
		if(query.get()!=null){
			return query.get().getCustomerId();
		}else{
			return null;
		}
				
	}
	
	/**
	 * 获取处于可分配态的客服人员
	 */
	@Override
	public Map<Integer,Integer> findWaiter(ObjectId companyId,ObjectId departmentId){
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		Query<Employee> query = getDatastore().createQuery(Employee.class).field("companyId").equal(companyId)
				.field("departmentId").equal(departmentId).field("isPause").equal(1);
		if(query == null)
			return null;
		List<Employee> emps= query.asList();
		
		for(Iterator<Employee> iter = emps.iterator(); iter.hasNext(); ){
			Employee emp = iter.next();	
			map.put(emp.getUserId(), emp.getChatNum());
		}
		return map;
	}
		
	/**
	 * 添加常用语
	 */
	@Override
	public CommonText commonTextAdd(CommonText commonText) {
		commonText.setCreateTime(DateUtil.currentTimeSeconds());//创建时间
		commonText.setCreateUserId(ReqUtil.getUserId());//创建人
		commonText.setModifyUserId(ReqUtil.getUserId());//修改人
		getDatastore().save(commonText);
		return commonText;
	}
	
	/**
	 * 删除常用语
	 */
	@Override
	public boolean deleteCommonText(String commonTextId) {
		ObjectId commonTextIds = new ObjectId(commonTextId);	
		Query<CommonText> query = getDatastore().createQuery(CommonText.class).filter("_id", commonTextIds);
		//CommonText commonText = query.get();获取查询出的对象
		getDatastore().delete(query);
		return true;
	}
	
	/**
	 * 根据公司id查询常用语
	 */
	@Override
	public List<CommonText> commonTextGetByCommpanyId(String companyId, int page, int limit) {
		ObjectId companyIds = new ObjectId(companyId);	
		Query<CommonText> query = getDatastore().createQuery(CommonText.class);
		query.filter("companyId", companyIds);
		//根据创建时间倒叙
		List<CommonText> commonTextList = query.asList(SKBeanUtils.getCustomerRepository().pageFindOption(page, limit, 0));
				
		return commonTextList;
	}
	
	
	/**
	 * 根据userId查询常用语
	 */
	@Override
	public List<CommonText> commonTextGetByUserId(int userId, int page, int limit) {
		Query<CommonText> query = getDatastore().createQuery(CommonText.class);
		query.filter("companyId", "0").filter("createUserId", userId);
		//根据创建时间倒叙
		List<CommonText> commonTextList = query.asList(SKBeanUtils.getCustomerRepository().pageFindOption(page, limit, 0));
		return commonTextList;
	}
	
	
	/**
	 * 修改常用语
	 */
	@Override
	public CommonText commonTextModify(CommonText commonText) {
		if (!StringUtil.isEmpty(commonText.getId().toString())) {
			//根据常用语id来查询出数据
			Query<CommonText> query = getDatastore().createQuery(CommonText.class).field("_id").equal(commonText.getId());
			//修改
			UpdateOperations<CommonText> uo = getDatastore().createUpdateOperations(CommonText.class);
			//赋值
			if (null!=commonText.getContent()) {
				uo.set("content", commonText.getContent());
			}
			uo.set("modifyUserId", ReqUtil.getUserId());
			uo.set("createTime", DateUtil.currentTimeSeconds());
			commonText = getDatastore().findAndModify(query, uo);
		}
		return commonText;
		
	}

}