package cn.xyz.repository.mongo;





import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Company;
import cn.xyz.repository.CompanyRepository;

/**
 * 组织架构功能数据操纵接口的实现
 * @author hsg
 *
 */
@Service
public class CompanyRepositoryImpl extends MongoRepository<Company, ObjectId> implements CompanyRepository {
	
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<Company> getEntityClass() {
		return Company.class;
	}
	
	public static CompanyRepositoryImpl getInstance(){
		return new CompanyRepositoryImpl();
	}
	
	 //创建公司
	@Override   
	public Company addCompany(String companyName, int createUserId, ObjectId rootDpartId) {
		
		Company company = new Company();
		List<ObjectId> list = new ArrayList<ObjectId>();
		list.add(rootDpartId);
		
		company.setCompanyName(companyName);
		company.setCreateUserId(createUserId);
		company.setDeleteUserId(0);
		company.setCreateTime(DateUtil.currentTimeSeconds());
		company.setRootDpartId(list);
		company.setNoticeContent("");
		company.setDeleteTime(0);
		company.setNoticeTime(0);
		company.setEmpNum(1);
		
		//存入公司数据
		ObjectId companyId = (ObjectId) getDatastore().save(company).getId();
		company.setId(companyId);
		
		return company;
	}

	
	//根据创建者Id查找公司
	@Override
	public Company findCompanyByCreaterUserId(int createUserId) {
		//根据创建者Id查找公司，同时排除掉deleteUserId != 0 的数据(deleteUserId != 0 :表示已经删除）
		Query<Company> query = getDatastore().createQuery(getEntityClass()).field("createUserId").equal(createUserId).filter("deleteUserId  ==", 0);
		return query.get();
	}

	
	//修改公司信息
	@Override
	public Company modifyCompany(Company company) {
		ObjectId companyId = company.getId();
		if(companyId == null){
			return null;
		}
		
		Query<Company> query = getDatastore().createQuery(getEntityClass()).field("_id").equal(companyId);
		UpdateOperations<Company> ops = getDatastore().createUpdateOperations(getEntityClass());
		if(null != company.getCompanyName())
			ops.set("companyName", company.getCompanyName());
		if(0 != company.getCreateUserId())
			ops.set("createUserId", company.getCreateUserId());
		if(0 != company.getDeleteUserId())
			ops.set("deleteUserId", company.getDeleteUserId());
		if(null != company.getRootDpartId())
			ops.set("rootDpartId", company.getRootDpartId());
		if(0 != company.getCreateTime())
			ops.set("createTime", company.getCreateTime());
		if(null != company.getNoticeContent()){
			ops.set("noticeContent", company.getNoticeContent());
			ops.set("noticeTime", DateUtil.currentTimeSeconds());
		}
		if(0 != company.getDeleteTime())
			ops.set("deleteTime", company.getDeleteTime());
		if(0 != company.getEmpNum())
			ops.set("empNum", company.getEmpNum());
		
		Company comp = getDatastore().findAndModify(query, ops);
		
		return comp;
	}

	
	//通过公司名称的关键字模糊查找公司
	@Override
	public List<Company> findCompanyByName(String keyworld) {
		
		Query<Company> query = getDatastore().createQuery(getEntityClass());
		
		//忽略大小写进行模糊匹配
		query.criteria("companyName").containsIgnoreCase(keyworld);
		List<Company> companys = query.asList();
		
		//除去执行过删除操作,被隐藏的公司
		for (Iterator<Company> iter = companys.iterator(); iter.hasNext();) {  
			Company company = iter.next();  
            if (company.getDeleteUserId() != 0) {   //将DeleteUserId不为0的数据剔除
                iter.remove();  
            }  
        }  
		
		return companys;
	}
	
	//根据公司id查找公司
	@Override
	public Company findById(ObjectId companyId){
		Query<Company> query = getDatastore().createQuery(getEntityClass()).field("_id").equal(companyId);
		if(query == null){
			return null;
		}
		
		return query.get();
	}


	
	
	//获得所有公司
	@Override
	public List<Company> companyList(int pageSize, int pageIndex) {
		//查找没有被隐藏起来的公司
		Query<Company> query = getDatastore().createQuery(getEntityClass()).field("deleteUserId").equal(0);
		List<Company> companys = query.offset(pageIndex * pageSize).limit(pageSize).asList();
		
		return companys;
	}

	
	//根据公司名称查找公司，精准查找
	@Override
	public Company findOneByName(String companyName) {
		//查找公司名称完全匹配，且没有被隐藏起来的公司
		Query<Company> query = getDatastore().createQuery(getEntityClass()).field("companyName").equal(companyName).field("deleteUserId").equal(0);
		return query.get();
	}

	
	//返回某个特定状态值的公司
	@Override
	public List<Company> findByType(int type) {
		Query<Company> query = getDatastore().createQuery(getEntityClass()).field("type").equal(type);
		return query.asList();
	}

}
