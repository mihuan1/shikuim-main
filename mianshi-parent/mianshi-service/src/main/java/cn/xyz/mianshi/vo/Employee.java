package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.NotSaved;

@Entity(value = "employee", noClassnameStored = true)
@Indexes(value = {@Index(fields = {@Field("userId"),@Field("departmentId"),@Field("companyId"),@Field("role") })  })
public class Employee {
	
	private @Id ObjectId id; //员工id
	private @Indexed int userId; //用户id,用于和用户表关联
	private @Indexed ObjectId departmentId;  //部门Id,表示员工所属部门        
	private @Indexed ObjectId companyId; //公司id，表示员工所属公司
	private byte role; //员工角色：0：普通员工     1：部门管理者(暂时没用)    2：公司管理员    3：公司创建者
	private String position = "员工";  //职位（头衔），如：经理、总监等
	
	private @NotSaved String nickname;  //用户昵称，和用户表一致
	
	//客服模块所需字段
	private int chatNum;//当前会话的人数
	private int isPause;//是否暂停    0：暂停,1:正常
	private @NotSaved int operationType;//操作类型 1.建立会话操作 2.结束回话操作
	private @NotSaved int isCustomer;//是否为客服    0:不是  1:是
	
	public int getIsCustomer() {
		return isCustomer;
	}
	public void setIsCustomer(int isCustomer) {
		this.isCustomer = isCustomer;
	}
	public int getOperationType() {
		return operationType;
	}
	public void setOperationType(int operationType) {
		this.operationType = operationType;
	}
	public int getIsPause() {
		return isPause;
	}
	public void setIsPause(int isPause) {
		this.isPause = isPause;
	}
	public int getChatNum() {
		return chatNum;
	}
	public void setChatNum(int chatNum) {
		this.chatNum = chatNum;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public ObjectId getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(ObjectId departmentId) {
		this.departmentId = departmentId;
	}
	public ObjectId getCompanyId() {
		return companyId;
	}
	public void setCompanyId(ObjectId companyId) {
		this.companyId = companyId;
	}
	public byte getRole() {
		return role;
	}
	public void setRole(byte role) {
		this.role = role;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
}
