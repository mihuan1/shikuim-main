package cn.xyz.mianshi.vo;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.NotSaved;

@Entity(value = "company", noClassnameStored = true)
public class Company {

	private @Id  ObjectId id;  //公司id
	
	private @Indexed String companyName;// 公司名称
	
	private @Indexed int createUserId;//创建者的用户id
	
	private @Indexed int deleteUserId; //删除者id,默认0   注：当用户执行删除公司操作后，将userId存入，隐藏相关信息。
	
	private @Indexed List<ObjectId> rootDpartId; //根部门Id,可能有多个
	
	private @Indexed long createTime; //创建时间
	
	private long deleteTime; //删除时间
	
	private @Indexed String noticeContent;// 公司公告（通知）
	
	private long noticeTime; //公告时间
	
	private @Indexed int empNum; //公司员工总数
	
	
	private @Indexed int type = 0; //类型值      5:默认加入的公司
	
	private @NotSaved List<Department> departments;  //公司的部门列表
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public List<Department> getDepartments() {
		return departments;
	}
	
	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}
	
	
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public int getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}
	public int getDeleteUserId() {
		return deleteUserId;
	}
	public void setDeleteUserId(int deleteUserId) {
		this.deleteUserId = deleteUserId;
	}
	public List<ObjectId> getRootDpartId() {
		return rootDpartId;
	}
	public void setRootDpartId(List<ObjectId> rootDpartId) {
		this.rootDpartId = rootDpartId;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public long getDeleteTime() {
		return deleteTime;
	}
	public void setDeleteTime(long deleteTime) {
		this.deleteTime = deleteTime;
	}
	public String getNoticeContent() {
		return noticeContent;
	}
	public void setNoticeContent(String noticeContent) {
		this.noticeContent = noticeContent;
	}
	public long getNoticeTime() {
		return noticeTime;
	}
	public void setNoticeTime(long noticeTime) {
		this.noticeTime = noticeTime;
	}
	public int getEmpNum() {
		return empNum;
	}
	public void setEmpNum(int empNum) {
		this.empNum = empNum;
	}
	
	

}