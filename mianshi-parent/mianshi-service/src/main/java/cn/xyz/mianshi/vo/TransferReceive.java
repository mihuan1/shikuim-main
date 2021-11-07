package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import lombok.Data;

/**
 * 
 * @Description: TODO(转账收款实体)
 * @author zhm
 * @date 2019年2月18日 下午3:52:18
 * @version V1.0
 */
@Entity(value = "TransferReceive", noClassnameStored = true)
@Data
public class TransferReceive {
	
	private @Id ObjectId id;
	
	private @Indexed String transferId;// 转账Id
	
	private @Indexed Integer userId;// 接受者用户ID
	
	private @Indexed Integer sendId;// 发送者用户Id
	
	private String userName;// 接受者用户名称
	
	private String sendName;// 转账发送者昵称
	
	private Double money;// 接受转账金额
	
	private long time;// 接受转账时间
}
