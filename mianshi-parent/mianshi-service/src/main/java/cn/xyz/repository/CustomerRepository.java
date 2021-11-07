package cn.xyz.repository;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import cn.xyz.mianshi.vo.CommonText;
import cn.xyz.mianshi.vo.Customer;

/**
 * 客服模块相关的数据操纵接口
 * @author hsg
 *
 */
public interface CustomerRepository {
	
	Map<String, Object> addCustomer(Customer customer);

	Integer findUserByIp(String ip);

	Map<Integer, Integer> findWaiter(ObjectId com, ObjectId departmentId);
	
	
	/**
	* @Title: commonTextAdd
	* @Description: 添加常用语
	* @param @param commonText
	* @param @return    参数
	* @return CommonText    返回类型
	* @throws
	*/
	CommonText commonTextAdd(CommonText commonText);
	
	/**
	* @Title: deleteCommonText
	* @Description: 删除常用语
	* @param @param id
	* @param @return    参数
	* @return Integer    返回类型
	* @throws
	*/
	boolean deleteCommonText(String commonTextId);
	
	/**
	* @Title: commonTextGetByCommpanyId
	* @Description: 查询常用语,根据公司id
	* @param @param companyId
	* @param @param pageIndex
	* @param @param pageSize
	* @param @return    参数
	* @return List<CommonText>    返回类型
	* @throws
	*/
	List<CommonText> commonTextGetByCommpanyId(String companyId,int pageIndex,int pageSize);
	
	
	/**
	* @Title: commonTextGetByUserId
	* @Description: 查询常用语,根据userId
	* @param @param userId
	* @param @param pageIndex
	* @param @param pageSize
	* @param @return    参数
	* @return List<CommonText>    返回类型
	* @throws
	*/
	List<CommonText> commonTextGetByUserId(int userId,int pageIndex,int pageSize);
	
	/**
	* @Title: commonTextModify
	* @Description: 修改常用语
	* @param @param commonText
	* @param @return    参数
	* @return CommonText    返回类型
	* @throws
	*/
	CommonText commonTextModify(CommonText commonText);
	
	
}
