package cn.xyz.repository;

import java.util.List;

import org.bson.types.ObjectId;

import cn.xyz.mianshi.vo.Employee;
import cn.xyz.mianshi.vo.User;

/**
 * 组织架构功能员工相关的数据操纵接口
 * @author hsg
 *
 */
public interface EmployeeRepository {
	
	
	//添加员工（单个）
	ObjectId addEmployee(Employee employee);
	
	//添加员工（多个）
	List<Employee> addEmployees(List<Integer> userId, ObjectId companyId, ObjectId departmentId);
	
	//修改员工信息
	Employee modifyEmployees(Employee employee);
	
	/**
	* @Title: modifyEmployeesByuserId
	* @Description:根据用户id来修改员工信息
	* @param @param userId
	* @param @return    参数
	* @return Employee    返回类型
	* @throws
	*/
	Employee modifyEmployeesByuserId(int userId);
	
	//根据id查找员工
	Employee findById(ObjectId employeeId);
	
	//通过userId查找员工
    List<Employee> findByUserId(int userId);
	
    //查找公司中某个角色的所有员工
    List<Employee> findByRole(ObjectId companyId,int role);
	
	//删除整个部门的员工
    void delEmpByDeptId(ObjectId departmentId);
    
    //删除员工(单个)
    void deleteEmployee(List<Integer> userIds, ObjectId departmentId);
    
    //根据公司ID查询员工(员工列表)
    List<Employee> compEmployeeList (ObjectId companyId);
    
    //根据部门ID查询员工(部门员工列表) 不分页
    List<Employee> departEmployeeList (ObjectId departmentId);
    
    //查找公司中某个员工的角色
    byte findRole(ObjectId companyId, int userId);
    
    
    //查找公司中某个员工的角色,通过部门id
    byte findRoleByDepartmentId(ObjectId companyId, int userId);
    
    //删除员工(根据公司id）
    void delEmpByCompId(ObjectId companyId, int userId);
    
    /**
    * @Title: findEmployee
    * @Description: 查询员工是否为客服
    * @param @param employee
    * @param @return    参数
    * @return int    返回类型
    * @throws
    */
    Employee findEmployee(Employee employee,User.UserSettings userSettings);
    
    /**
    * @Title: findEmployee
    * @Description: 根据公司，部门，用户id来查询出员工信息
    * @param @param employee
    * @param @return    参数
    * @return Employee    返回类型
    * @throws
    */
    Employee findEmployee(Employee employee);
}
