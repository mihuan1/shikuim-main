package cn.xyz.mianshi.opensdk.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import lombok.Getter;
import lombok.Setter;
/**
 * 
 * @Description: TODO(审核日志)
 * @author Administrator
 * @date 2018年10月29日 下午4:42:39
 * @version V1.0
 */
@Getter
@Setter
@Entity(value = "SkOpenCheckLog", noClassnameStored = true)
public class SkOpenCheckLog {

	@Id
	private ObjectId id; //记录id
	
	private String accountId;
	
	private String appId;
	
	private Long createTime;
	
	private Long modifyTime;
	
	/**
	 * 操作用户
	 */
	private String operateUser;
	/**
	 * 审核结果 1 审核通过, -1 禁用, 0审核中   2 审核失败
	 */
	private Byte status = 0;
	/**
	 * 失败原因  审核回馈
	 */
	private String reason;
	
	
	public SkOpenCheckLog() {
		
	}


	public SkOpenCheckLog(SkOpenCheckLog skOpenCheckLog) {
		this.accountId = skOpenCheckLog.getAccountId();
		this.appId = skOpenCheckLog.getAppId();
		this.createTime = DateUtil.currentTimeSeconds();
		this.modifyTime = DateUtil.currentTimeSeconds();
		this.operateUser = skOpenCheckLog.getOperateUser();
		if(0 != skOpenCheckLog.getStatus())
			this.status = skOpenCheckLog.getStatus();
		if(!StringUtil.isEmpty(skOpenCheckLog.getReason()))
			this.reason = skOpenCheckLog.getReason();
	}


	public SkOpenCheckLog(String accountId, String appId,String operateUser, String status, String reason) {
		this.accountId = accountId;
		this.appId = appId;
		this.createTime = DateUtil.currentTimeSeconds();
		this.modifyTime = DateUtil.currentTimeSeconds();
		this.operateUser = operateUser;
		if(!status.equals("0"))
			this.status = Byte.valueOf(status);
		if(!StringUtil.isEmpty(reason))
			this.reason = reason;
	}
	
	
	
	
}
