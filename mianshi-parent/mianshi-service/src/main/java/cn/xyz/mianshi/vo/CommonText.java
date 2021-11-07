package cn.xyz.mianshi.vo;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
/**
* @ClassName: CommonText
* @Description: 公司常用语
* @data 2017年12月4日 下午6:17:27
*
*/
@Entity(value = "commonText", noClassnameStored = true)
public class CommonText implements Serializable{

	/**
	* @Fields serialVersionUID : 序列化
	*/
	private static final long serialVersionUID = 7516807726776310662L;
	
	private @Id ObjectId id; //记录id
	
	private int createUserId; //创建者用户id
	
	private String content; //常用语内容
	
	private long createTime; //创建时间
	
	private int modifyUserId; //修改者Id，初始值为创建者id
	
	private  String  companyId;//公司id
	
	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public int getCreateUserId() {
		return createUserId;
	}
	
	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	public int getModifyUserId() {
		return modifyUserId;
	}
	
	public void setModifyUserId(int modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	
	
}
