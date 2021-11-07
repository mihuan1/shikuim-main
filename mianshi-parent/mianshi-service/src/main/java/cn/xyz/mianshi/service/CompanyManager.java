package cn.xyz.mianshi.service;

import java.util.List;

import org.bson.types.ObjectId;

import cn.xyz.mianshi.vo.Company;
import cn.xyz.mianshi.vo.Department;
import cn.xyz.mianshi.vo.Employee;
import cn.xyz.mianshi.vo.User;

public interface CompanyManager {
	
	//创建公司
	Company createCompany(String companyName, int createUserId);
	
	//根据userId 反向查找公司
	List<Company> findCompanyByUserId(int userId);
	
	//根据id查找公司
	Company getCompany(ObjectId companyId);
	
	//设置管理员
	void setManager(ObjectId companyId, List<Integer> managerId);
	
	//管理员列表
	List<Employee> managerList(ObjectId companyId);
	
	//修改公司信息
	Company modifyCompanyInfo(Company company);
	
	
	//通过关键字查找公司
	List<Company> findCompanyByKeyworld(String keyworld);
	
	//删除公司(即隐藏公司,不真正删除)
	void deleteCompany(ObjectId companyId,int userId);
	
	//公司列表
	List<Company> companyList(int pageSize, int pageIndex);
	
	
	
	/**部门相关*/
	
	//创建部门
	Department createDepartment(ObjectId companyId,ObjectId parentId,String departName,int createUserId);
	
	//修改部门信息
	Department modifyDepartmentInfo(Department department);
	
	//删除部门
	void deleteDepartment(ObjectId departmentId);
	
	//部门列表（包括员工数据）
	List<Department> departmentList(ObjectId companyId);
	
	//获取部门详情
	Department getDepartmentVO (ObjectId departmentId);
		
	
	/**员工相关**/
	
	//添加员工(支持多个)
	List<Employee> addEmployee (ObjectId companyId, ObjectId departmentId, List<Integer> userId);
	
	//删除员工
	void deleteEmployee(List<Integer> userIds, ObjectId departmentId);
	
	//更改员工信息
	Employee changeEmployeeInfo(Employee employee);
	
	//修改员工数据
	Employee modifyEmpInfo(Employee employee);
	
	/**
	* @Title: modifyEmployeesByuserId
	* @Description: 根据用户id来修改员工信息
	* @param @param userId
	* @param @return    参数
	* @return Employee    返回类型
	* @throws
	*/
	public Employee modifyEmployeesByuserId(int userId);
	
    /**
    * @Title: findEmployee
    * @Description: 查询员工是否为客服
    * @param @param employee
    * @param @return    参数
    * @return int    返回类型
    * @throws
    */
    public Employee findEmployee(Employee employee,User.UserSettings userSettings);
	
	//员工列表(公司的所有员工)
	List<Employee> employeeList (ObjectId companyId);
	
	//部门员工列表
	List<Employee> departEmployeeList(ObjectId departmentId);
	
	
	//获取员工详情
	Employee getEmployee(ObjectId employeeId);
	
	//员工退出公司
	void empQuitCompany(ObjectId companyId, int userId);
	
	//获取公司中某位员工的角色值
	int getEmpRole(ObjectId companyId, int userId);
	
	
	
	//此方法用于将客户自动加入默认的公司便于，客户体验组织架构功能
	Company autoJoinCompany(int userId);
	
	
}