package cn.xyz.mianshi.vo;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.NotSaved;

@Entity(value = "department", noClassnameStored = true)
public class Department {
	
	private @Id ObjectId id; //部门id
	private @Indexed ObjectId companyId; //公司id，表示该部门所属的公司
	private @Indexed ObjectId parentId; //parentId 表示上一级的部门ID
	private @Indexed String departName; //部门名称
	private @Indexed int createUserId; //创建者userId
	private long createTime; //创建时间
	private int empNum = -1; //部门总人数
	
	private @Indexed int type = 0; //类型值  0:普通部门  1:根部门  2:分公司    5:默认加入的部门  6.客服部门
	
	
	//此属性用于封装部门员工列表
	private @NotSaved List<Employee> employees;  //部门员工列表
	
	//此属性用于封装该部门的子部门
	private @NotSaved List<Department> childDepartment; //子部门
	
	
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public List<Employee> getEmployees() {
		return employees;
	}
	public void setEmployees(List<Employee> employees) {
		this.employees = employees;
	}
	public ObjectId getCompanyId() {
		return companyId;
	}
	public void setCompanyId(ObjectId companyId) {
		this.companyId = companyId;
	}
	public ObjectId getParentId() {
		return parentId;
	}
	public void setParentId(ObjectId parentId) {
		this.parentId = parentId;
	}
	public String getDepartName() {
		return departName;
	}
	public void setDepartName(String departName) {
		this.departName = departName;
	}
	public int getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public int getEmpNum() {
		return empNum;
	}
	public void setEmpNum(int empNum) {
		this.empNum = empNum;
	}
	public List<Department> getChildDepartment() {
		return childDepartment;
	}
	public void setChildDepartment(List<Department> childDepartment) {
		this.childDepartment = childDepartment;
	}
	
	
}
