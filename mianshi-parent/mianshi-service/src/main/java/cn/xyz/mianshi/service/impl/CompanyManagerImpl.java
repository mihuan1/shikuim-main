package cn.xyz.mianshi.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.service.CompanyManager;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Company;
import cn.xyz.mianshi.vo.Department;
import cn.xyz.mianshi.vo.Employee;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.DepartmentRepository;
import cn.xyz.repository.mongo.CompanyRepositoryImpl;
import cn.xyz.repository.mongo.EmployeeRepositoryImpl;

@Service
public class CompanyManagerImpl implements CompanyManager {

	private static CompanyRepositoryImpl getCompanyRepository() {
		CompanyRepositoryImpl companyRepository = SKBeanUtils.getCompanyRepository();
		return companyRepository;
	};
	
	private static DepartmentRepository getDepartmentRepository() {
		DepartmentRepository companyRepository = SKBeanUtils.getDepartmentRepository();
		return companyRepository;
	};
	
	private static EmployeeRepositoryImpl getEmployeeRepository() {
		EmployeeRepositoryImpl employeeRepository = SKBeanUtils.getEmployeeRepository();
		return employeeRepository;
	};
	
	/**
	 * 创建公司 （添加演示数据版）
	 */
	@Override
	public Company createCompany(String companyName, int createUserId) {
		
		//检查该用户是否创建过公司
		/*if(null != getCompanyRepository().findCompanyByCreaterUserId(createUserId)){
			throw new ServiceException("已创建过公司");
		}*/
		
		//检查是否有重名的公司
		if(null != getCompanyRepository().findOneByName(companyName)){
			throw new ServiceException("公司名称已存在");
		}
		
		ObjectId rootDpartId = new ObjectId();
		//添加公司记录
		Company company =  getCompanyRepository().addCompany(companyName, createUserId, rootDpartId); 
		
		//给该公司默认添加一条根部门记录
		Department department = new Department();
		department.setCompanyId(company.getId());
		department.setParentId(null);  //根部门的ParentId 为null
		department.setDepartName(companyName);
		department.setCreateUserId(createUserId); //根部门的创建者即公司的创建者
		department.setCreateTime(DateUtil.currentTimeSeconds());
		department.setEmpNum(0);
		department.setType(1);  //1:根部门
		
		rootDpartId = getDepartmentRepository().addDepartment(department); //添加根部门记录
		List<ObjectId> rootList = new ArrayList<ObjectId>();
		rootList.add(rootDpartId);
		
		//给该公司创建两个部门(人事部,财务部)
		Department personDepart = new Department();
		personDepart.setCompanyId(company.getId());
		personDepart.setParentId(rootDpartId);  //ParentId 为根部门的id
		personDepart.setDepartName("人事部");
		personDepart.setCreateUserId(createUserId); //创建者即公司的创建者
		personDepart.setCreateTime(DateUtil.currentTimeSeconds());
		personDepart.setEmpNum(1);
		ObjectId personDepartId = getDepartmentRepository().addDepartment(personDepart); //添加部门记录
		
		Department financeDepart = new Department();
		financeDepart.setCompanyId(company.getId());
		financeDepart.setParentId(rootDpartId);  //ParentId 为根部门的id
		financeDepart.setDepartName("财务部");
		financeDepart.setCreateUserId(createUserId); //创建者即公司的创建者
		financeDepart.setCreateTime(DateUtil.currentTimeSeconds());
		financeDepart.setEmpNum(0);
		getDepartmentRepository().addDepartment(financeDepart); //添加部门记录
		
		//客服部
		Department Customer = new Department();
		Customer.setCompanyId(company.getId());
		Customer.setParentId(rootDpartId);  //ParentId 为根部门的id
		Customer.setDepartName("客服部");
		Customer.setCreateUserId(createUserId); //创建者即公司的创建者
		Customer.setCreateTime(DateUtil.currentTimeSeconds());
		Customer.setEmpNum(0);
		Customer.setType(6);
		getDepartmentRepository().addDepartment(Customer); //添加部门记录
		
		//给创建者添加员工记录，将其置于人事部门中
		Employee employee = new Employee();
		employee.setDepartmentId(personDepartId);
		employee.setRole(KConstants.Company.COMPANY_CREATER);   //3：公司创建者(超管)
		employee.setUserId(createUserId);
		employee.setCompanyId(company.getId());
		employee.setPosition("创建者");
		getEmployeeRepository().addEmployee(employee);
		
		company.setRootDpartId(rootList);
		//将根部门id存入公司记录
		getCompanyRepository().modifyCompany(company);
		
		
		return company;
	}
	
	
	
	
	
	//根据userId 反向查找公司
	@Override
	public List<Company> findCompanyByUserId(int userId) {
		//首先查找员工相关记录
		List<Employee> employees = getEmployeeRepository().findByUserId(userId);
		if (employees == null){
			return null;
		}
			
		List<Company> companys = new ArrayList<Company>(); //用于存放公司的集合
		//遍历员工记录
		for(Iterator<Employee> iter = employees.iterator(); iter.hasNext();){
			Employee emp = iter.next();
			ObjectId companyId = emp.getCompanyId();
			Company comp = getCompanyRepository().findById(companyId);
			if(comp.getDeleteUserId() == 0){  //排除掉 执行删除操作，从而被隐藏起来的公司
				companys.add(comp);
			}
		}
		//判断是否存在公司数据
		
		return companys;
		
	}
	
	
	
	/**
	 * 根据id查找公司
	 */
	@Override
	public Company getCompany(ObjectId companyId){
		return getCompanyRepository().findById(companyId);
	}
	
	
	/**
	 * 设置管理员
	 * managerId:管理员userId
	 */
	@Override
	public void setManager(ObjectId companyId, List<Integer> managerId) {
		Employee employee = new Employee();
		for(Iterator<Integer> iter = managerId.iterator();iter.hasNext(); ){
			int userId = iter.next();
			
			employee.setUserId(userId);
			employee.setCompanyId(companyId);
			employee.setRole(KConstants.Company.COMPANY_MANAGER); //2:公司管理者
			getEmployeeRepository().modifyEmployees(employee);
		}
		
	}
	
	
	//管理员列表
	@Override
	public List<Employee> managerList(ObjectId companyId) {
		int role = 2; //2:公司管理员
		List<Employee> list = getEmployeeRepository().findByRole(companyId, role);
		return list;
	}
	
	
	//修改公司信息
	@Override
	public Company modifyCompanyInfo(Company company) {
		//修改公司名称的同时.修改根部门名称
		if(company.getCompanyName() != null && !"".equals(company.getCompanyName()) ){
			//判断公司名称是否重复
			if(getCompanyRepository().findOneByName(company.getCompanyName()) != null)
				throw new ServiceException("公司名称已存在");
			Department department = new Department();
			department.setDepartName(company.getCompanyName());
			getDepartmentRepository().modifyRootDepartByCompId(company.getId(), department);  //修改根部门名称
		}
		return getCompanyRepository().modifyCompany(company);
	}
	

	//通过关键字查找公司
	@Override
	public List<Company> findCompanyByKeyworld(String keyworld) {
		List<Company> companys = getCompanyRepository().findCompanyByName(keyworld);
		if(companys.size()==0){
			return null;
		}
		return companys;
	}
	
	
	//删除公司(即隐藏公司,不真正删除)
	@Override
	public void deleteCompany(ObjectId companyId, int userId) {
		//只有公司创建者才能执行删除操作
		Company company = getCompanyRepository().findById(companyId);
		if(company.getCreateUserId() != userId){ //判断是否为创建者userId
			throw new ServiceException("只有创建者才能删除");
		}
		if(0!=company.getDeleteUserId()) {
			throw new ServiceException("该公司不存在");
		}
		company.setDeleteUserId(userId);
		company.setDeleteTime(DateUtil.currentTimeSeconds());
		getCompanyRepository().modifyCompany(company);
	}
	
	
	//公司列表  
	@Override
	public List<Company> companyList(int pageSize, int pageIndex) {
		return getCompanyRepository().companyList(pageSize, pageIndex);
	}
	
	
	
	//创建部门
	@Override
	public Department createDepartment(ObjectId companyId, ObjectId parentId, String departName, int createUserId) {
		//检查部门名称是否重复
		if(getDepartmentRepository().findOneByName(companyId, departName) != null)
			throw new ServiceException("部门名称重复");
		Department department = new Department();
		department.setCompanyId(companyId);
		department.setParentId(parentId);
		department.setEmpNum(0);
		department.setDepartName(departName);
		department.setCreateUserId(createUserId);
		department.setCreateTime(DateUtil.currentTimeSeconds());
		ObjectId departmentId = getDepartmentRepository().addDepartment(department);
		
		return getDepartmentRepository().findDepartmentById(departmentId);
	}

	//修改部门信息
	@Override
	public Department modifyDepartmentInfo(Department department) {
		if(department.getDepartName() != null && ! "".equals(department.getDepartName()) ){
			ObjectId companyId = getDepartmentRepository().getCompanyId(department.getId()); //通过部门id得到公司id
			int departType = getDepartmentRepository().findById(department.getId()).getType();
			if( departType == 1 || departType == 4 || departType == 5 ){ //判断是否为特殊部门    1:表示根部门  4:演示数数据中的普通部门  5: 演示数据中的让用户加入的部门 
				throw new ServiceException("没有相关权限，不能更改");
			}
			if(getDepartmentRepository().findOneByName(companyId, department.getDepartName()) != null)  //判断名称是否重复
				throw new ServiceException("部门名称重复");
		}
		Department depart = getDepartmentRepository().modifyDepartment(department);
		return depart;
	}
	
	//删除部门
	@Override
	public void deleteDepartment(ObjectId departmentId) {
		//做相关的判断，特殊部门不能删除
		int departType = getDepartmentRepository().findById(departmentId).getType();
		if(departType == 1 || departType == 4 || departType == 5){ //1:  根部门    4:演示数数据中的普通部门  5: 演示数据中的让用户加入的部门 
			throw new ServiceException("该部门不能删除");
		}
		//首先删除该部门的员工记录
		getEmployeeRepository().delEmpByDeptId(departmentId);
		
		getDepartmentRepository().deleteDepartment(departmentId); //删除部门记录
	}
	
	
	//部门列表（包括员工数据）
	@Override
	public List<Department> departmentList(ObjectId companyId) {
		 
		List<Department> departments = getDepartmentRepository().departmentList(companyId); //查找出该公司所有的部门数据
		for(Iterator<Department> iter = departments.iterator();iter.hasNext(); ){  
			Department department = iter.next();
			//查找该部门中的员工,并封装到department中
			List<Employee> employees = getEmployeeRepository().departEmployeeList(department.getId());
			setUserNickname(employees); //将用户昵称封装到员工数据
			department.setEmployees(employees); //将员工数据封装进部门中
		}
		return departments;
	}
	public List<Department> departmentList(List<Department> departments) {
		 
		for(Iterator<Department> iter = departments.iterator();iter.hasNext(); ){  
			Department department = iter.next();
			//查找该部门中的员工,并封装到department中
			List<Employee> employees = getEmployeeRepository().departEmployeeList(department.getId());
			setUserNickname(employees); //将用户昵称封装到员工数据
			department.setEmployees(employees); //将员工数据封装进部门中
		}
		return departments;
	}
	
	
	//添加员工(支持多个) 
	@Override
	public List<Employee> addEmployee(ObjectId companyId, ObjectId departmentId, List<Integer> userId) {
		//判断是否能够添加
		List<Employee> compEmps = getEmployeeRepository().compEmployeeList(companyId); //得到公司的所有员工
		for(Iterator<Integer> uids = userId.iterator(); uids.hasNext();){  //遍历userId，和公司的所有员工进行比对
			int uid = uids.next();
			for(Iterator<Employee> iter = compEmps.iterator(); iter.hasNext(); ){
				Employee emp = iter.next();	
				if(emp.getUserId() == uid){  //判断员工是否存在
					throw new ServiceException("不能重复添加");
				}
			}
			
		}
		
		//正常添加员工
		List<Employee> employees = getEmployeeRepository().addEmployees(userId, companyId, departmentId);  //存入员工记录
		//修改公司记录里的员工数目
		Company company = getCompanyRepository().findById(companyId);
		company.setEmpNum(company.getEmpNum()+userId.size());
		getCompanyRepository().modifyCompany(company);
		
		//修改部门记录里的员工数目
		Department department = getDepartmentRepository().findById(departmentId);
		department.setEmpNum(department.getEmpNum()+userId.size());
		getDepartmentRepository().modifyDepartment(department);
		
		setUserNickname(employees);//将用户昵称封装到员工数据
		return employees;
	}
	
	
	//删除员工
	@Override
	public void deleteEmployee(List<Integer> userIds, ObjectId departmentId) {
		//修改部门记录里的员工数目    -1
		Department department = getDepartmentRepository().findById(departmentId);
		userIds.forEach(delUserId ->{
			if(delUserId.equals(department.getCreateUserId()))
				throw new ServiceException("公司创建者不能被删除");
		});
		getEmployeeRepository().deleteEmployee(userIds, departmentId); //删除员工记录
		department.setEmpNum(department.getEmpNum()-1);
		getDepartmentRepository().modifyDepartment(department);
		
		//修改公司记录里的员工数目
		Company company = getCompanyRepository().findById(department.getCompanyId());
		company.setEmpNum(company.getEmpNum()-1);
		getCompanyRepository().modifyCompany(company);
	}
	
	//客服模块，更改员工信息
	@Override
	public Employee changeEmployeeInfo(Employee employee) {
		if (!StringUtil.isEmpty(employee.toString())) {
			//根据公司，部门，用户id查出员工信息
			Employee employeeData = getEmployeeRepository().findEmployee(employee);
			if (null != employeeData && !"".equals(employeeData)) {
				// 根据员工id查询员工信息
				Employee employeeInfo = getEmployeeRepository().findById(employeeData.getId());
				if (employeeInfo.getChatNum() == 5 && employee.getOperationType() == 1) {
					throw new ServiceException("当前会话人数过多请稍后重试！");
				} else {
					//该员工的当前会话人数
					int num = employeeInfo.getChatNum();
					// 建立会话
					if (employee.getOperationType() == 1) {
						employee.setChatNum(num + 1);
					} else if (employee.getOperationType() == 2) {
						// 结束回话
						employee.setChatNum(num - 1);
					} else {
						employee.setChatNum(employeeInfo.getChatNum());
					}
					// 如果只是修改会话人数，保证会话状态正常
					if (employee.getOperationType() != 0 && employee.getIsPause() == 0) {
						employee.setIsPause(employeeInfo.getIsPause());
					}
					if (employee.getChatNum() < 0) {
						//throw new ServiceException("当前会话人数异常！");
						return null;
					} else {
						Employee emp = modifyEmpInfo(employee);
						return emp;
					}
				}
			} else {
				throw new ServiceException("该用户不属于客服部门,请重试！");
			}
		} else {
			throw new ServiceException("参数有误,请重试！");
		}
	}
	
	

	@Override
	public Employee modifyEmpInfo(Employee employee) {
		Employee emp = getEmployeeRepository().modifyEmployees(employee);
		String nickname = SKBeanUtils.getUserRepository().getUser(emp.getUserId()).getNickname();
		emp.setNickname(nickname);
		return emp;
	}
	
	
	//员工列表(公司的所有员工)
	@Override
	public List<Employee> employeeList(ObjectId companyId) {
		List<Employee> employees = getEmployeeRepository().compEmployeeList(companyId);
		setUserNickname(employees);//将用户昵称封装到员工数据
		return employees;
	}
	
	//部门员工列表
	public List<Employee> departEmployeeList(ObjectId departmentId){
		List<Employee> employees = getEmployeeRepository().departEmployeeList(departmentId);
		setUserNickname(employees); //将用户昵称封装到员工数据
		return  employees;
	}


	
	//获取员工详情
	@Override
	public Employee getEmployee(ObjectId employeeId) {
		Employee emp =  getEmployeeRepository().findById(employeeId);
		String nickname = SKBeanUtils.getUserRepository().getUser(emp.getUserId()).getNickname();
		emp.setNickname(nickname);
		return emp;
	}

	
	//获取部门详情
	@Override
	public Department getDepartmentVO(ObjectId departmentId) {
		Department department = getDepartmentRepository().findById(departmentId);
		return department;
	}

	
	
	//此方法用于获取user表中的用户昵称，并封装进员工数据中
	public void setUserNickname(List<Employee> employees){
		for(Iterator<Employee> iter = employees.iterator();iter.hasNext();){  //遍历员工数据
			Employee employee = iter.next();
			User user = SKBeanUtils.getUserRepository().getUser(employee.getUserId());
			String nickname =null!=user?user.getNickname():"";
			employee.setNickname(nickname);
		}
	}
	

	
	//此方法用于将客户自动随机加入默认的公司,便于客户体验组织架构功能
	@Override
	public Company autoJoinCompany(int userId){
		Employee employee = new Employee();
		List<Company> companys = getCompanyRepository().findByType(5);  //查找默认加入的公司
		if(companys == null || companys.isEmpty()){
			return null;
		}
		//随机选择一个公司
		Random random = new Random();
		int comp = random.nextInt(companys.size()); 
		employee.setCompanyId(companys.get(comp).getId());  //设置 员工记录的companyId 为默认随机加入的公司的id
		
		
		//随机选择该公司下的一个部门
		List<Department> departments = getDepartmentRepository().findByType(companys.get(comp).getId(), 5);
		
		int dept = random.nextInt(departments.size());
		employee.setDepartmentId(departments.get(dept).getId()); //设置 员工记录的departmentId 为随机加入的部门的id
		
		employee.setUserId(userId);
		employee.setRole(KConstants.Company.COMMON_EMPLOYEE);
		getEmployeeRepository().addEmployee(employee);
		//将公司数据返回
		return getCompanyRepository().findById(companys.get(comp).getId());
	}
	
	
	//员工退出公司
	@Override
	public void empQuitCompany(ObjectId companyId, int userId) {
		//判断员工身份，若为创建者直接删除公司(隐藏公司)，非创建者将该员工从公司中移除
		int employeeRole = getEmployeeRepository().findRole(companyId, userId);//获取员工角色值
		if(employeeRole == 3){    //  3 : 表示公司的创建者
			//删除（隐藏公司）
			deleteCompany(companyId, userId);
		}else{
			//删除员工记录
			getEmployeeRepository().delEmpByCompId(companyId, userId);
		}
		
	}


	//获取公司中某位员工的角色值
	@Override
	public int getEmpRole(ObjectId companyId, int userId) {
		return getEmployeeRepository().findRole(companyId, userId);
	}




	/**
	 * 根据用户id来修改员工信息
	 */
	@Override
	public Employee modifyEmployeesByuserId(int userId) {
		Employee employee = getEmployeeRepository().modifyEmployeesByuserId(userId);
		if (!StringUtil.isEmpty(employee.toString())) {
			return employee;
		}else{
			throw new ServiceException("修改员工信息出错！");
		}
	}

	
	/**
	 * 查询员工是否为客服
	 */
	@Override
	public Employee findEmployee(Employee employee,User.UserSettings userSettings) {
		if (!StringUtil.isEmpty(String.valueOf(employee.getCompanyId()))
				|| !StringUtil.isEmpty(String.valueOf(employee.getDepartmentId()))
				|| !StringUtil.isEmpty(String.valueOf(employee.getCompanyId()))) {
			Employee employeeInfo = getEmployeeRepository().findEmployee(employee, userSettings);
			if (null != employeeInfo && !"".equals(employeeInfo)) {
				// 判断当前会话人数是否为0,否则不能关闭客服模式
				if (employeeInfo.getChatNum() > 0) {
					throw new RuntimeException("当前还有" + employeeInfo.getChatNum() + "个客户正在会话，请结束会话再试!");
				} else {
					// 维护用户表中的是否开启客服模式字段
					if (userSettings.getOpenService() == 0 || userSettings.getOpenService() == 1) {
						User user = SKBeanUtils.getUserRepository().updateSettings(employee.getUserId(), userSettings);
						employeeInfo.setIsCustomer(user.getSettings().getOpenService());
						// 如果关闭客服模式则维护客服服务状态
						if (userSettings.getOpenService() == 0) {
							getEmployeeRepository().modifyEmployees(employee);
						}
					} else {
						throw new RuntimeException("参数有误，请重新再试!");
					}
				}
				return employeeInfo;
			} else {
				throw new RuntimeException("该员工不属于客服部门!");
			}
		} else {
			throw new ServiceException("缺少必要参数，请重新再试!");
		}
	}


	/**
	 * 权限验证,通过公司id
	 * 
	 *  操作者userId
	 * 权限验证类型 type   3 : 公司创建者   2 : 公司管理员   1: 部门管理者 
	 */
	public boolean verifyAuthByCompanyId (ObjectId companyId, int userId, byte type) {
		//role  值  0：普通员工     1：部门管理者    2：管理员    3：公司创建者(超管)
		/*for(byte type : types) {
		}*/
		return ( getEmployeeRepository().findRole(companyId, userId) >= type);
	}
	
	
	/**
	 * 权限验证,通过部门id
	 * 
	 *  操作者userId
	 * 权限验证类型 type   3 : 公司创建者   2 : 公司管理员   1: 部门管理者 
	 */
	public boolean verifyAuthByDepartmentId (ObjectId departmentId, int userId, byte type) {
		//role  值  0：普通员工     1：部门管理者    2：管理员    3：公司创建者(超管)
		return ( getEmployeeRepository().findRoleByDepartmentId(departmentId, userId) >= type);
	}
	
	
	
	
}