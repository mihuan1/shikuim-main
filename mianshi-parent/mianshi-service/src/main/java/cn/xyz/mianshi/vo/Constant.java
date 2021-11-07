package cn.xyz.mianshi.vo;

import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.NotSaved;

@Entity(value="tb_constants",noClassnameStored=true)
public class Constant extends Language{

	@Id
	private ObjectId objId;
	private long createTime;
	private long modifyTime;
	
	@Indexed
	private Integer id=0;
	@Indexed
	private String code;
	@Indexed
	private Integer parent_id=0;
	private Integer more=0;
	
	
	
	@Indexed
	private long userId=0;
	
	@Indexed
	private int status=2;//状态  0 or null 正常 公用   -1 伪删除  1 ：个人生效   2：临时状态
	
	
	
	@NotSaved
	private String parentValue; 
	
	
	
	
	
	
	@NotSaved
	private Map<String,String> map;



	public ObjectId getObjId() {
		return objId;
	}



	public void setObjId(ObjectId objId) {
		this.objId = objId;
	}



	public long getCreateTime() {
		return createTime;
	}



	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}



	public long getModifyTime() {
		return modifyTime;
	}



	public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}



	public Integer getId() {
		return id;
	}



	public void setId(Integer id) {
		this.id = id;
	}



	public String getCode() {
		return code;
	}



	public void setCode(String code) {
		this.code = code;
	}



	public Integer getParent_id() {
		return parent_id;
	}



	public void setParent_id(Integer parent_id) {
		this.parent_id = parent_id;
	}



	public Integer getMore() {
		return more;
	}



	public void setMore(Integer more) {
		this.more = more;
	}



	public long getUserId() {
		return userId;
	}



	public void setUserId(long userId) {
		this.userId = userId;
	}



	public int getStatus() {
		return status;
	}



	public void setStatus(int status) {
		this.status = status;
	}



	public String getParentValue() {
		return parentValue;
	}



	public void setParentValue(String parentValue) {
		this.parentValue = parentValue;
	}



	public Map<String, String> getMap() {
		return map;
	}



	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	
	
	
	

	
}
