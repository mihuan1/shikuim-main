package cn.xyz.mianshi.vo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

@Entity(value = "PaidMerchant", noClassnameStored = true)
@Data
public class PaidMerchant {
	public static final int ENABLE = 0;
	public static final int DISABLE = 1;

	/**
	 *  记录唯一id
	 *  商户ID
	 */
	private @Id ObjectId id;

	private String vendorMerchantId;
	//支付方式:	alipay:支付宝,tenpay:财付通,qqpay:QQ钱包,wxpay:微信支付
	/**
	 * @see cn.xyz.commons.constants.KConstants.ThirdPayType
	 */
	private String payType;
	//商品名称
	private String name;
	//网站名称
	private String sitename;
	//密钥
	private String sign;
	//支付url
	private String payUrl;
	//查询订单url
	private String orderQueryUrl;
	//平台渠道：易支付
	/**
	 * @see cn.xyz.commons.constants.KConstants.PayVendorChannel
	 */
	private String channel;

	/**
	 * 0 启用 1紧用
	 */
	private int enable;

}
