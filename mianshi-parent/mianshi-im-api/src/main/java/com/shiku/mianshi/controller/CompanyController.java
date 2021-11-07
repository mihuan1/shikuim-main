package com.shiku.mianshi.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.KConstants.Result;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Company;
import cn.xyz.mianshi.vo.Department;
import cn.xyz.mianshi.vo.Employee;
import cn.xyz.mianshi.vo.User;

/**
 * 用于组织架构功能的相关接口
 * @author hsg
 *
 */
@RestController
@RequestMapping("/org")
public class CompanyController extends AbstractController {
	
	

	// 创建公司
	@RequestMapping(value = "/company/create")
	public JSONMessage createCompany(@RequestParam String companyName, @RequestParam int createUserId){
		
		try {
			if(companyName != null && !"".equals(companyName) && createUserId > 0){
				 Company company = SKBeanUtils.getCompanyManager().createCompany(companyName, createUserId);
				 company.setDepartments(SKBeanUtils.getCompanyManager().departmentList(company.getId()) ); //将部门及员工数据封装进公司
				 Object data = company;
				return JSONMessage.success(null, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
			
		}
		return JSONMessage.failure("创建失败");
	
	}

	
	// 根据userId查找是否存在其所属的公司
	@RequestMapping("/company/getByUserId")
	public JSONMessage getCompanyByUserId(){
		List<Company> companys = SKBeanUtils.getCompanyManager().findCompanyByUserId(ReqUtil.getUserId());
		if (companys == null || companys.isEmpty()){  //判断是否存在公司
			return JSONMessage.success();
		}
		for(Iterator<Company> iter = companys.iterator(); iter.hasNext(); ){   //遍历公司
			Company company = iter.next();
			company.setDepartments(SKBeanUtils.getCompanyManager().departmentList(company.getId()) );  //将部门及员工数据封装进公司
		}
		Object data = companys;
		return JSONMessage.success(null, data);
		
	}
	
	/**
	 * 指定管理员
	 * @param companyId  公司id
	 * @param managerId  管理员 id 列表  [1002021,10021212]
	 * @return
	 */
	@RequestMapping("/company/setManager")
	public JSONMessage setCompanyManager(@RequestParam String companyId, @RequestParam String managerId){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		
		ObjectId compId = new ObjectId(companyId);
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMPANY_CREATER))
			return JSONMessage.failure("需要创建者权限");
		
		// 以字符串的形式接收managerId，然后解析转换为int 
		List<Integer> userIds= new ArrayList<Integer>();
		if(managerId.charAt(0)=='[' && managerId.charAt(managerId.length() - 1)==']'){ 
			userIds = JSON.parseArray(managerId, Integer.class);
		}
		
		SKBeanUtils.getCompanyManager().setManager(compId, userIds);
		return JSONMessage.success();	
	}
	
	//管理员列表
	@RequestMapping("/company/managerList")
	public JSONMessage ManagerList(@RequestParam String companyId){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		ObjectId compId = new ObjectId(companyId);
		//权限验证,公司成员可以调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMMON_EMPLOYEE))
			return JSONMessage.failure("需要创建者权限");
		Object data = SKBeanUtils.getCompanyManager().managerList(compId);
		return JSONMessage.success(null, data);
	}
	
	// 修改公司名称、公告
	@RequestMapping("/company/modify")
	public JSONMessage modifyCompany(@RequestParam String companyId, String companyName,@RequestParam(defaultValue = "") String noticeContent){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		ObjectId compId = new ObjectId(companyId);
		
		Company company = new Company();
		company.setId(compId);
		if(companyName != null){
			//权限验证,公司创建者才能修改名称
			if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMPANY_CREATER))
				return JSONMessage.failure("公司创建者才能修改名称");
			company.setCompanyName(companyName);
		}else if(noticeContent != null &&  !"".equals(noticeContent)){ //判断是否存在公告
			//权限验证,公司管理员以上才能修改公告
			if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMPANY_MANAGER))
				return JSONMessage.failure("需要管理员以上权限才能修改公告");
			company.setNoticeContent(noticeContent);
			company.setNoticeTime(DateUtil.currentTimeSeconds());
		}
		Object data = SKBeanUtils.getCompanyManager().modifyCompanyInfo(company);
		return JSONMessage.success(null,data);
		
	}
	
	
	// 查找公司:（通过公司名称的关键字查找）
	/*@RequestMapping("/company/search")
	public JSONMessage changeNotice(@RequestParam String keyworld){
		Object data = companyManager.findCompanyByKeyworld(keyworld);
		return JSONMessage.success(null,data);
	}*/
	
	// 删除公司(即：记录删除者id,将公司信息隐藏)
	@RequestMapping("/company/delete")
	public JSONMessage deleteCompany(@RequestParam String companyId){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		try {
			ObjectId compId = new ObjectId(companyId);
			SKBeanUtils.getCompanyManager().deleteCompany(compId, ReqUtil.getUserId());
			return JSONMessage.success();
		} catch (Exception e) {
			// TODO: handle exception
			return JSONMessage.failure(e.getMessage());
		}
	}
	

	// 创建部门
	@RequestMapping("/department/create")
	public JSONMessage createDepartment(@RequestParam String companyId, @RequestParam String parentId, @RequestParam String departName,@RequestParam int createUserId){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		if(ReqUtil.getUserId()!=createUserId)
			return JSONMessage.failure("非法调用");
		
		ObjectId compId = new ObjectId(companyId);
		//权限验证,需要公司管理员及以上权限
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, createUserId, KConstants.Company.COMPANY_MANAGER))
			return JSONMessage.failure("需要公司管理员及以上权限");
		
		ObjectId parentID = new ObjectId();
		if(parentId.trim() != null){
			parentID = new ObjectId(parentId);
		}
		Object data = SKBeanUtils.getCompanyManager().createDepartment(compId, parentID, departName, createUserId);
		return JSONMessage.success(null,data);
	}
	
	// 修改部门名称
	@RequestMapping("/department/modify")
	public JSONMessage modifyDepartment (@RequestParam String departmentId,@RequestParam  String dpartmentName){
		if(!ObjectId.isValid(departmentId))
			return Result.ParamsAuthFail;
		ObjectId departId = new ObjectId(departmentId);
		//权限验证,需要公司管理员及以上权限
		if(!SKBeanUtils.getCompanyManager().verifyAuthByDepartmentId(departId, ReqUtil.getUserId(), KConstants.Company.COMPANY_MANAGER))
			return JSONMessage.failure("权限不足");
		Department department = new Department();
		department.setId(departId);
		department.setDepartName(dpartmentName);
		Object data = SKBeanUtils.getCompanyManager().modifyDepartmentInfo(department);
		return JSONMessage.success(null,data);
	}
	
	
	// 删除部门
	@RequestMapping("/department/delete")
	public JSONMessage modifyDepartment (@RequestParam String departmentId){
		if(!ObjectId.isValid(departmentId))
			return Result.ParamsAuthFail;
		ObjectId departId = new ObjectId(departmentId);
		//权限验证,需要公司管理员及以上权限
		if(!SKBeanUtils.getCompanyManager().verifyAuthByDepartmentId(departId, ReqUtil.getUserId(), KConstants.Company.COMPANY_MANAGER))
			return JSONMessage.failure("权限不足");
		
		SKBeanUtils.getCompanyManager().deleteDepartment(departId);
		return JSONMessage.success();
	}
	
	// 添加员工
	@RequestMapping("/employee/add")
	public JSONMessage addEmployee (@RequestParam String userId, @RequestParam String companyId,
			              @RequestParam String departmentId){
		if(!ObjectId.isValid(companyId)||!ObjectId.isValid(departmentId))
			return Result.ParamsAuthFail;
		
		ObjectId compId = new ObjectId(companyId);
		//验证权限,本公司员工可以调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMMON_EMPLOYEE))
			return JSONMessage.failure("权限不足,非本公司员工");
		
		// 以字符串的形式接收userId，然后解析转换为int
		List<Integer> userIds= new ArrayList<Integer>();
		char first = userId.charAt(0); 
		char last = userId.charAt(userId.length() - 1); 
		if(first=='[' && last==']'){  // 用于解析web端
			userIds = JSON.parseArray(userId, Integer.class);
		}else{ // 用于解析Android和IOS端
			String[] strs = userId.split(",");
			for(String str : strs){
				if(str != null && !"".equals(str)){
					userIds.add(Integer.parseInt(str));
				}
			}
		}
		
		ObjectId departId = new ObjectId(departmentId);
		Object data = SKBeanUtils.getCompanyManager().addEmployee(compId, departId, userIds);
		return JSONMessage.success(null,data);	
	}
	
	
	// 删除员工
	@RequestMapping("/employee/delete")
	public JSONMessage delEmployee (@RequestParam String userIds, @RequestParam String departmentId){
		if(!ObjectId.isValid(departmentId))
			return Result.ParamsAuthFail;
		ObjectId departId = new ObjectId(departmentId);
		//验证权限,公司管理员及其以上可以调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByDepartmentId(departId, ReqUtil.getUserId(), KConstants.Company.COMPANY_MANAGER))
			return JSONMessage.failure("权限不足");
		
		// 以字符串的形式接收userId，然后解析转换为int 
		List<Integer> uIds= new ArrayList<Integer>();
		char first = userIds.charAt(0); 
		char last = userIds.charAt(userIds.length() - 1); 
		if(first=='[' && last==']'){ // 用于解析web端
			uIds = JSON.parseArray(userIds, Integer.class);
		}else{ // 用于解析Android和IOS端
			uIds.add(Integer.parseInt(userIds));
		}
		
		SKBeanUtils.getCompanyManager().deleteEmployee(uIds, departId);
		return JSONMessage.success();
	}
	
	// 更改员工部门
	@RequestMapping("/employee/modifyDpart")
	public JSONMessage addEmployee (@RequestParam int userId, @RequestParam String companyId, @RequestParam String newDepartmentId){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		ObjectId compId = new ObjectId(companyId);
		
		//验证权限,公司管理员及其以上可以调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMPANY_MANAGER))
			return JSONMessage.failure("权限不足");
		
		Employee employee = new Employee();
		employee.setCompanyId(compId);
		employee.setUserId(userId);
		employee.setDepartmentId(new ObjectId(newDepartmentId));
		Company company = SKBeanUtils.getCompanyManager().getCompany(compId);
		if(company.getCreateUserId() == userId){
			employee.setRole(KConstants.Company.COMPANY_CREATER);
			employee.setPosition("创建者");
		}
		Object data = SKBeanUtils.getCompanyManager().modifyEmpInfo(employee);  //更改该员工的信息
		
		return JSONMessage.success(data);
	}
	
	/**
	* @Title: updateEmployee
	* @Description: 更改员工信息
	* @param @param employee
	* @param @return    参数
	* @return JSONMessage    返回类型
	* @throws
	*/
	@RequestMapping("/employee/updateEmployee")
	public JSONMessage updateEmployee(@Valid Employee employee){
		JSONMessage jsonMessage = Result.ParamsAuthFail;
		
		if(ReqUtil.getUserId()!=employee.getUserId())
			return JSONMessage.failure("非法调用");
		
		//权限验证,本公司员工才能调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(employee.getCompanyId(), ReqUtil.getUserId(), KConstants.Company.COMMON_EMPLOYEE))
			return JSONMessage.failure("权限不足,非本公司员工");
		try {
			Employee employeeInfo =SKBeanUtils.getCompanyManager().changeEmployeeInfo(employee);
			if (!StringUtil.isEmpty(employeeInfo.toString())) {
				return JSONMessage.success("", employeeInfo);
			}else{
				return jsonMessage;
			}
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}

	// 部门员工列表
	@RequestMapping("/departmemt/empList")
	public JSONMessage departEmpList (@RequestParam String departmentId){
		if(!ObjectId.isValid(departmentId))
			return Result.ParamsAuthFail;
		ObjectId departId = new ObjectId(departmentId);
		//权限验证,本公司员工才能调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByDepartmentId(departId, ReqUtil.getUserId(), KConstants.Company.COMMON_EMPLOYEE))
			return JSONMessage.failure("权限不足,非本公司员工");
		Object data = SKBeanUtils.getCompanyManager().departEmployeeList(departId);
		return JSONMessage.success(null,data);
	}
	
	// 公司员工列表
	@RequestMapping("/company/employees")
	public JSONMessage companyEmpList(@RequestParam String companyId){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		ObjectId compId = new ObjectId(companyId);
		//权限验证,本公司员工才能调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMMON_EMPLOYEE))
			return JSONMessage.failure("权限不足,非本公司员工");
		Object data = SKBeanUtils.getCompanyManager().employeeList(compId);
		return JSONMessage.success(null,data);
	}
	
	// 更改员工角色
	@RequestMapping("/employee/modifyRole")
	public JSONMessage addEmployee (@RequestParam int userId, @RequestParam String companyId, @RequestParam byte role){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		ObjectId compId = new ObjectId(companyId);
		//权限验证
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMPANY_MANAGER))
			return JSONMessage.failure("权限不足");
		Employee employee = new Employee();
		employee.setCompanyId(compId);
		employee.setUserId(userId);
		employee.setRole(role);
		Object data = SKBeanUtils.getCompanyManager().modifyEmpInfo(employee);
		
		return JSONMessage.success(null,data);
	}
	
	// 更改员工职位(头衔)
	@RequestMapping("/employee/modifyPosition")
	public JSONMessage modifyPosition (@RequestParam String companyId, @RequestParam String position){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		ObjectId compId = new ObjectId(companyId);
		//权限验证,本公司员工才能调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMMON_EMPLOYEE))
			return JSONMessage.failure("权限不足,非本公司员工");
		
		Employee employee = new Employee();
		employee.setCompanyId(compId);
		employee.setUserId(ReqUtil.getUserId());
		employee.setPosition(position);
		Object data = SKBeanUtils.getCompanyManager().modifyEmpInfo(employee);
		return JSONMessage.success(null,data);
	}
	
	
	// 公司列表
	/*@RequestMapping("/company/list")
	public JSONMessage companyList (@RequestParam(defaultValue = "0") int pageIndex,@RequestParam(defaultValue = "30") int pageSize){
		Object data = SKBeanUtils.getCompanyManager().companyList(pageSize, pageIndex);
		return JSONMessage.success(null, data);
	}*/
		
	// 部门列表
	@RequestMapping("/department/list")
	public JSONMessage departmentList (@RequestParam String companyId){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		ObjectId compId = new ObjectId(companyId);
		//权限验证,本公司员工才能调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMMON_EMPLOYEE))
				return JSONMessage.failure("权限不足,非本公司员工");
		
		Object data = SKBeanUtils.getCompanyManager().departmentList(compId);
		return JSONMessage.success(null,data);
	}

	// 获取公司详情
	@RequestMapping("/company/get")
	public JSONMessage getCompany (@RequestParam String companyId){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		ObjectId compId = new ObjectId(companyId);
		//权限验证,本公司员工才能调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMMON_EMPLOYEE))
				return JSONMessage.failure("权限不足,非本公司员工");
		Object data = SKBeanUtils.getCompanyManager().getCompany(compId);
		return JSONMessage.success(null,data);
	}
	
	// 获取员工
	/*@RequestMapping("/employee/get")
	public JSONMessage getEmployee (@RequestParam String employeeId){
		ObjectId empId = new ObjectId(employeeId);
		Object data = SKBeanUtils.getCompanyManager().getEmployee(empId);
		return JSONMessage.success(null,data);
	}*/
	
	// 获取部门
	@RequestMapping("/department/get")
	public JSONMessage getDpartment(@RequestParam String departmentId){
		if(!ObjectId.isValid(departmentId))
			return Result.ParamsAuthFail;
		ObjectId departId = new ObjectId(departmentId);
		//权限验证,本公司员工才能调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByDepartmentId(departId, ReqUtil.getUserId(), KConstants.Company.COMMON_EMPLOYEE))
				return JSONMessage.failure("权限不足,非本公司员工");
		Object data = SKBeanUtils.getCompanyManager().getDepartmentVO(departId);
		return JSONMessage.success(null,data);
	}
	
	// 员工退出公司
	@RequestMapping("/company/quit")
	public JSONMessage quitCompany(@RequestParam String companyId, @RequestParam int userId){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		ObjectId compId = new ObjectId(companyId);
		//权限验证,本公司员工才能调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMMON_EMPLOYEE))
				return JSONMessage.failure("权限不足,非本公司员工");
		SKBeanUtils.getCompanyManager().empQuitCompany(compId, userId);
		return JSONMessage.success();	
	}
	
	// 获取公司中某个员工角色值
	@RequestMapping("/employee/role")
	public JSONMessage getEmployRole(@RequestParam String companyId, @RequestParam int userId){
		if(!ObjectId.isValid(companyId))
			return Result.ParamsAuthFail;
		ObjectId compId = new ObjectId(companyId);
		//权限验证,本公司员工才能调用
		if(!SKBeanUtils.getCompanyManager().verifyAuthByCompanyId(compId, ReqUtil.getUserId(), KConstants.Company.COMMON_EMPLOYEE))
			return JSONMessage.failure("权限不足,非本公司员工");
		Object data = SKBeanUtils.getCompanyManager().getEmpRole(compId, userId);
		return JSONMessage.success(null,data);	
	}
	
	/**
	* @Title: findEmployee
	* @Description: 判断是否为查询员工是否为客服
	* @param @param employee
	* @param @return    参数
	* @return JSONMessage    返回类型
	* @throws
	*/
	@RequestMapping("/employee/findEmployee")
	public JSONMessage findEmployee(@Valid Employee employee,User.UserSettings userSettings){
		JSONMessage jsonMessage = Result.ParamsAuthFail;
		try {
			if (!StringUtil.isEmpty(employee.toString())) {
				employee = SKBeanUtils.getCompanyManager().findEmployee(employee,userSettings);
				return JSONMessage.success("", employee);
			}else{
				return jsonMessage;
			}
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
}
