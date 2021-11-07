package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Data;

/**
 * 
 * @Description: TODO(付款码支付、二维码收款记录实体)
 * @author Administrator
 * @date 2019年2月26日 下午3:14:18
 * @version V1.0
 */
@Entity(value = "codePay",noClassnameStored=true)
@Data
public class CodePay {
	
	private @Id ObjectId id;
	
	private Integer userId;// 码的所有人id
	
	private String userName;// 码的所有人名称
	
	private Integer toUserId;// 扫码的人Id
	
	private String toUserName;// 扫码的人的名称
	
	private Double money;// 金额
	
	private int type;// 类型  1：付款码    2：二维码收款
	
	private long createTime;// 交易时间
}
