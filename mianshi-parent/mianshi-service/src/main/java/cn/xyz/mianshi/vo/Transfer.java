package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import lombok.Data;
/**
 * 
 * @Description: TODO(转账实体)
 * @author zhm
 * @date 2019年2月18日 下午3:05:26
 * @version V1.0
 */
@Entity(value="Transfer",noClassnameStored=true)
@Data
public class Transfer {
	private @Id ObjectId id;
	//发送者用户Id
	private @Indexed Integer userId;
	
	private Integer toUserId;// 发送给那个人
	//转账发送者昵称
	private String userName;
	
	// 转账说明
	private String remark;
	
	//转账时间
	private long createTime;
	
	//转账金额
	private Double money;
	
	//超时时间
	private long outTime;
	
	//转账状态
	private @Indexed int status=1;// 1 ：发出  2：已收款  -1：已退款 
	
	private long receiptTime;// 收款时间
	
}
