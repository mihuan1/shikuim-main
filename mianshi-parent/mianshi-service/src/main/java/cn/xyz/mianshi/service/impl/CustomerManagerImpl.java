package cn.xyz.mianshi.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.service.CustomerManager;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.CommonText;
import cn.xyz.mianshi.vo.Customer;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.mongo.CustomerRepositoryImpl;
import cn.xyz.service.KXMPPServiceImpl;

@Service
public class CustomerManagerImpl implements CustomerManager {

	public static CustomerRepositoryImpl getCustomerRepository(){
		CustomerRepositoryImpl customerRepository = SKBeanUtils.getCustomerRepository();
		return customerRepository;
	}
	
	@Override
	public Map<String, Object> registerUser(String companyId,String departmentId,String ip) {
		Map<String, Object> data = new HashMap<String, Object>();
		Integer customerId = 0;
		customerId = getCustomerRepository().findUserByIp(ip);
		if (customerId!=null && customerId!=0){ //判断ip地址是否注册过
			try {
				//2、缓存用户认证数据到
				data =KSessionUtil.loginSaveAccessToken(customerId,customerId,null);
				data.put("customerId", customerId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{ //没有注册
			
			//生成userId
			customerId = SKBeanUtils.getUserManager().createUserId();
			Customer customer = new Customer();
			customer.setCustomerId(customerId);
			customer.setIp(ip);
			customer.setCompanyId(companyId);
			// 新增客户
			data = getCustomerRepository().addCustomer(customer);
		}	
		//分配客服号
		Integer serviceId =  allocation(companyId,departmentId);
		if (null != data) {
			data.put("serviceId", serviceId);
			try {
				//注册到tigsae
				KXMPPServiceImpl.getInstance().registerByThread(customerId.toString(), DigestUtils.md5Hex(customerId.toString()));
			} catch (Exception e){
				e.printStackTrace();
			}
			return data;
		}
		throw new ServiceException("用户注册失败");
	}
	
	
	
	/**
	 * 分配客服号
	 * @param companyId
	 * @param departmentId
	 * @return
	 */
	public synchronized Integer  allocation(String companyId,String departmentId) {    
		ObjectId compId = new ObjectId(companyId);
		ObjectId departId = new ObjectId(departmentId);
		//到员工表中找到可分配状态的客服人员   map  key：userId  value:当前接待的客户数
		Map<Integer,Integer> map = getCustomerRepository().findWaiter(compId, departId);
		
		int minValue = -1; //用于存放map中最小的value值
		int minKey = 0; //记录最小的value对应的key
		
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			if(SKBeanUtils.getUserManager().getOnlinestateByUserId(entry.getKey())==0){ //在线状态，离线0  在线 1
				continue;
			}else{
				
				if(minValue == -1){ //首次将第一个value的值赋给maxValue
					minValue = entry.getValue();
					minKey = entry.getKey();
				}
				if(entry.getValue()==0){ //如果某个客服会话数为0，直接分配此客服
					minValue = entry.getValue();
					minKey = entry.getKey();
					break;
				}
				if(entry.getValue()<minValue){ //判断当前值是否小于上一个最小值
					minValue = entry.getValue();
					minKey = entry.getKey();
				}
				
			}	
		}
		
		return minKey;
		
	}





	/**
	 * 添加常用语
	 */
	@Override
	public CommonText commonTextAdd(CommonText commonText) {
		if (!StringUtil.isEmpty(commonText.toString())) {
			getCustomerRepository().commonTextAdd(commonText);
			return commonText;
		}else{
			throw new ServiceException("添加常用语失败！");
		}
	}


	/**
	 * 删除常用语
	 */
	@Override
	public boolean deleteCommonTest(String commonTextId) {
		boolean a = getCustomerRepository().deleteCommonText(commonTextId);
		if (true == a) {
			return true;
		}else {
			throw new ServiceException("删除常用语失败！");
		}
	}

	/**
	 * 通过公司id查询常用语
	 */
	@Override
	public List<CommonText> commonTextGetByCompanyId(String companyId, int pageIndex, int pageSize) {
		List<CommonText> commonTextList = getCustomerRepository().commonTextGetByCommpanyId(companyId, pageIndex, pageSize);
		return commonTextList;
		
	}
	
	/**
	 * 通过userId 查询常用语
	 */
	@Override
	public List<CommonText> commonTextGetByUserId(int userId, int pageIndex, int pageSize) {
		List<CommonText> commonTextList = getCustomerRepository().commonTextGetByUserId(userId,  pageIndex,  pageSize);
	    return commonTextList;
	}
	

	/**
	 * 修改常用语
	 */
	@Override
	public CommonText commonTextModify(CommonText commonText) {
		if (null!=getCustomerRepository().commonTextModify(commonText)) {
			return commonText;
		}else {
			throw new ServiceException("修改常用语失败！");
		}
	}



	@Override
	public User getUser(String customerId) {
		
		return null;
	}

	
	

}