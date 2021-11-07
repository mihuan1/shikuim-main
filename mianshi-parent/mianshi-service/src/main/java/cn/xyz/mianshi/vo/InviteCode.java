package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * 邀请码实体
 * @author hsg
 *
 */

@Entity(value = "inviteCode", noClassnameStored = true)
public class InviteCode {
	
	@Id
	private ObjectId id;
	
	private String inviteCode; //邀请码
	private String defaultfriend;//默认成为好友的人
	private String desc; //备注
	private long  createTime; //生成时间,单位 ms
	private long  cout; //数量
	
	
	
	public InviteCode() {
		super();
	}

	public InviteCode(String inviteCode, String defaultfriend,String desc,long createTime) {
		super();
		this.inviteCode = inviteCode;
		this.createTime = createTime;
		this.defaultfriend = defaultfriend;
		this.desc = desc;
		this.cout = 0;
	}

	public long getCout() {
		return cout;
	}
	public void setCout(long cout) {
		this.cout = cout;
	}
	public String  getDefaultfriend() {
		return defaultfriend;
	}
	public void setDefaultfriend(String defaultfriend) {
		this.defaultfriend = defaultfriend;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
}
