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
	
	private int userId; //邀请码所属的用户
	
	private String inviteCode; //邀请码
	
	private short status = 0; //状态值 0,为初始状态未使用   1:已使用    -1: 禁用，一码多用时可以禁用邀请码
	
	private long  createTime; //生成时间,单位 ms
	
	private long  lastuseTime = 0; //用户最后一次使用该邀请码的时间, 单位 ms
	
	private int totalTimes = -1; //总次数 -1 : 不限次数      1:一次（同时也表示该邀请码属于一码一用类型） 其他 >0 的值:表示对应的使用次数 
	
	private int usedTimes = 0; //已使用次数
	
	
	
	public InviteCode() {
		super();
	}

	public InviteCode(int userId, String inviteCode, long createTime, int totalTimes) {
		super();
		this.userId = userId;
		this.inviteCode = inviteCode;
		this.createTime = createTime;
		this.totalTimes = totalTimes;
	}

	
	
	public int getUserId() {
		return userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
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

	

	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		this.status = status;
	}

	public long getLastuseTime() {
		return lastuseTime;
	}

	public void setLastuseTime(long lastuseTime) {
		this.lastuseTime = lastuseTime;
	}

	public int getTotalTimes() {
		return totalTimes;
	}

	public void setTotalTimes(int totalTimes) {
		this.totalTimes = totalTimes;
	}

	public int getUsedTimes() {
		return usedTimes;
	}

	public void setUsedTimes(int usedTimes) {
		this.usedTimes = usedTimes;
	}
	
	
	
	
	
	
	
	
}
