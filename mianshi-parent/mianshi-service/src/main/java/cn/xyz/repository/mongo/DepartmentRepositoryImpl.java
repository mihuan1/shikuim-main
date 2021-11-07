package cn.xyz.repository.mongo;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Department;
import cn.xyz.repository.DepartmentRepository;

/**
 * 
 * 组织架构功能部门数据操纵接口的实现
 * @author hsg
 *
 */

@Service
public class DepartmentRepositoryImpl extends MongoRepository<Department,ObjectId> implements DepartmentRepository{
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}


	@Override
	public Class<Department> getEntityClass() {
		return Department.class;
	}
	
	//创建部门,返回值为部门Id
	@Override 
	public ObjectId addDepartment(Department department) {
		//存入数据，并获取id
		 ObjectId departmentId = (ObjectId) getDatastore().save(department).getId();
		 return departmentId;
	}
	
	
	//修改部门信息
	@Override
	public Department modifyDepartment(Department department) {
		
		ObjectId departmentId = department.getId();
		
		if(departmentId == null){
			return null;
		}
		
		Query<Department> query = getDatastore().createQuery(getEntityClass()).field("_id").equal(departmentId);
		UpdateOperations<Department> ops = getDatastore().createUpdateOperations(getEntityClass());
		
		if(null != department.getDepartName())
			ops.set("departName", department.getDepartName());
		if(0 <= department.getEmpNum())
			ops.set("empNum", department.getEmpNum());
		Department depart = getDatastore().findAndModify(query, ops);
		
		
		return depart;
		
		
	}
	
	
	//根据部门Id查找部门
	@Override 
	public Department findDepartmentById(ObjectId departmentId) {
		Query<Department> query = getDatastore().createQuery(getEntityClass()).field("_id").equal(departmentId);
		return query.get();
	}

	
	//删除部门
	@Override
	public void deleteDepartment(ObjectId departmentId) {
		//根据id找到部门
		Query<Department> query = getDatastore().createQuery(getEntityClass()).field("_id").equal(departmentId);
		//删除记录
		if(query != null)
		getDatastore().delete(query);
	}

	
	//部门列表(公司的所有部门，包含员工,分页)
	@Override
	public List<Department> departmentList(ObjectId companyId, int pageSize, int pageIndex) {
		Query<Department> query = getDatastore().createQuery(getEntityClass()).field("companyId").equal(companyId);
		List<Department> departments = query.offset(pageIndex * pageSize).limit(pageSize).asList();
		
		return departments;
	}

	//根据id查找部门
	@Override
	public Department findById(ObjectId departmentId) {
		Query<Department> query = getDatastore().createQuery(getEntityClass()).field("_id").equal(departmentId);
		return query.get();
	}

	//公司部门列表，封装员工数据
	@Override
	public List<Department> departmentList(ObjectId companyId) {
		Query<Department> query = getDatastore().createQuery(getEntityClass()).field("companyId").equal(companyId);
		return query.order("createTime").asList();  //按创建时间升序排列
	}

	
	//根据公司id修改根部门信息
	@Override
	public Department modifyRootDepartByCompId(ObjectId companyId, Department depart) {
		//查找根部门
		Query<Department> dQuery = getDatastore().createQuery(getEntityClass()).field("companyId").equal(companyId).field("type").equal(1);  //type:1   1:根部门
		depart.setId(dQuery.get().getId());
		//更新信息
        UpdateOperations<Department> ops = getDatastore().createUpdateOperations(getEntityClass());
		if(null != depart.getDepartName())
			ops.set("departName", depart.getDepartName());
		if(0 <= depart.getEmpNum())
			ops.set("empNum", depart.getEmpNum());
		
		 getDatastore().findAndModify(dQuery, ops);
		
		return null;
	}

	
	//根据部门名称，查找某个公司的部门,精准查找
	@Override
	public Department findOneByName(ObjectId companyId, String departName) {
		Query<Department> query = getDatastore().createQuery(getEntityClass()).field("companyId").equal(companyId).field("departName").equal(departName);
		return query.get();
	}

    //通过部门id得到公司id
	@Override
	public ObjectId getCompanyId(ObjectId departmentId) {
		Query<Department> dQuery = getDatastore().createQuery(getEntityClass()).field("_id").equal(departmentId);
		return dQuery.get().getCompanyId();
	}

	
	//查找某个公司中某个特定状态值的部门
	@Override
	public List<Department> findByType(ObjectId companyId, int type) {
		Query<Department> query = getDatastore().createQuery(getEntityClass()).field("companyId").equal(companyId).field("type").equal(type);
		return query.asList();
	}

	
	//查找某个部门的子部门
	@Override
	public List<Department> findChildDepartmeny(ObjectId departmentId) {
		Query<Department> query = getDatastore().createQuery(getEntityClass()).field("parentId").equal(departmentId);
		return query.asList();
	}


}
