package cn.xyz.mianshi.service;

import java.util.List;
import java.util.Map;

import cn.xyz.mianshi.vo.CommonText;
import cn.xyz.mianshi.vo.User;

public interface CustomerManager {
	
	Map<String, Object> registerUser(String companyId,String departmentId,String ip);

	
	User getUser(String customerId);
	
	/**
	* @Title: commonTextAdd
	* @Description: 添加常用语
	* @param @param commonText
	* @param @return    参数
	* @return CommonText    返回类型
	* @throws
	*/
	public CommonText commonTextAdd(CommonText commonText);

	/**
	* @Title: deleteCommonTest
	* @Description: 删除常用语
	* @param @param id
	* @param @return    参数
	* @return boolean    返回类型
	* @throws
	*/
	public boolean deleteCommonTest(String commonTextId);
	
	/**
	* @Title: CommonTextGetByCompanyId
	* @Description: 通过公司id查询常用语
	* @param @param companyId
	* @param @param pageIndex
	* @param @param pageSize
	* @param @return    参数
	* @return List<CommonText>    返回类型
	* @throws
	*/
	public List<CommonText> commonTextGetByCompanyId(String companyId,int pageIndex,int pageSize);
	
	/**
	 * @Title: CommonTextGetByCompanyId
	 * @Description: 通过userId查询常用语
	 * @param userId
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public List<CommonText> commonTextGetByUserId(int userId, int pageIndex,int pageSize);
	
	
	
	/**
	* @Title: commonTextModify
	* @Description: 修改常用语
	* @param @param commonText
	* @param @return    参数
	* @return CommonText    返回类型
	* @throws
	*/
	public CommonText commonTextModify(CommonText commonText);
	
}