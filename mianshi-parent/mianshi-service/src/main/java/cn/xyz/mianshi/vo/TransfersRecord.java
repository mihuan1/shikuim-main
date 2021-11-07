package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import lombok.Data;

/**
 * 用户微信提现记录  实体
 * @author lidaye
 *
 */
@Data
@Entity(value="transfersRecord",noClassnameStored=true)
public class TransfersRecord {

	private @Id ObjectId id;
	
	private int userId;
	
	private long createTime;
	/**
	 * 订单状态   0 创建   1 支付成功  -1 支付失败
	 */
	private int status;
	
	
	private String appid;
	private String mchId;
	
	private String nonceStr;
	
	/**
	 * 用户微信  openid
	 */
	private String openid;
	
	/**
	 * 商户转账订单号
	 */
	private @Indexed String outTradeNo;
	
	
	
	/**
	 * 提现金额  
	 */
	private String totalFee;
	/**
	 * 手续费
	 */
	private String fee;
	
	/**
	 * 实际到账金额
	 */
	private String realFee;
	
	/**
	 * 微信支付成功时间
	 */
	private String payTime;
	/**
	 * 微信订单号
	 */
	private String payNo;
	
	private String resultCode;
	private String returnCode;
	
	/**
	 * 错误信息
	 */
	private String errCode;
	private String errDes;
}
