package cn.xyz.mianshi.vo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
* @Description: TODO(好友分组)
* @author lidaye
* @date 2018年6月7日 
*/
@Entity(value="friendGroup",noClassnameStored=true)
public class FriendGroup {
	
	@Id
	private ObjectId groupId;
	
	private String groupName;//分组名称
	
	private int userId; //用户ID
	
	private long createTime;
	
	private List<Integer> userIdList=new ArrayList<Integer>();//好友的用户Id

	public ObjectId getGroupId() {
		return groupId;
	}

	public void setGroupId(ObjectId groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public List<Integer> getUserIdList() {
		return userIdList;
	}

	public void setUserIdList(List<Integer> userIdList) {
		this.userIdList = userIdList;
	}
	
	

}

