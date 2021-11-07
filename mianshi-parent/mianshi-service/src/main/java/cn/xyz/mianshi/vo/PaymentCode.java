package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Data;

/**
 * 
 * @Description: TODO(用户付款码记录)
 * @author zhm
 * @date 2019年2月19日 下午12:31:19
 * @version V1.0
 */
@Entity(value ="paymentCode",noClassnameStored=true)
@Data
public class PaymentCode {
	private @Id ObjectId id;
	
	private Integer userId;// 用户Id
	
	private String paymentCode;// 付款码
	
	private int status;// 1:完成
	
	private double money;// 金额
	
	private long time;
}
