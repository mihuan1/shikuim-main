package cn.xyz.repository;

import java.util.List;

import org.bson.types.ObjectId;

import cn.xyz.mianshi.vo.Department;

/**
 * 组织架构功能部门相关的数据操纵接口
 * @author hsg
 *
 */
public interface DepartmentRepository {
	
	//创建部门
	ObjectId addDepartment(Department department);
	
	//修改部门信息
	Department modifyDepartment(Department department);
	
	//根据Id查找部门
	Department findDepartmentById(ObjectId departmentId);
	
	//删除部门
	void deleteDepartment(ObjectId departmentId);
	
	//部门列表(公司的所有部门,支持分页)
	List<Department> departmentList(ObjectId companyId, int pageSize, int pageIndex);
	
	//公司部门列表，封装了员工数据
	List<Department> departmentList(ObjectId companyId);
	
	//根据id查找部门
	Department findById(ObjectId departmentId);
	
	//根据公司id修改根部门信息
	Department modifyRootDepartByCompId (ObjectId companyId,Department depart);
	
	//根据部门名称，查找某个公司的部门
	Department findOneByName(ObjectId companyId, String departmentName);
	
	//通过部门id得到公司id
	ObjectId getCompanyId(ObjectId departmentId);
	
	//查找某个公司中某个特定状态值的部门
	List<Department> findByType(ObjectId companyId, int type);
	
	//查找某个部门的子部门
	List<Department> findChildDepartmeny(ObjectId departmentId);
}
