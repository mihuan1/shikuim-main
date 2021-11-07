package cn.xyz.mianshi.vo;

import java.text.DecimalFormat;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.NotSaved;

//消费记录实体
@Entity(value = "ConsumeRecord", noClassnameStored = true)
public class ConsumeRecord {

	private @Id ObjectId id; //记录id
	
	private @Indexed String tradeNo; //交易单号
	
	private @Indexed int userId; //用户Id
	
	/**
	 * 对方用户Id
	 * 接受转账时 为 转账人的ID
	 * 
	 * 发送转账时 为 接受放的 ID
	 */
	private @Indexed int toUserId; 
	
	private Double money; //金额
	
	private Double startMoney;
	
	private Double endMoney;
	
	private long time; //时间
	
	/*
	 * 类型  1:用户充值, 2:用户提现, 3:后台充值, 4:发红包, 5:领取红包, 
	 * 6:红包退款  7:转账   8:接受转账   9:转账退回   10:付款码付款  
	 *  11:付款码到账   12:二维码付款  13:二维码到账  14:第三方调取IM支付通知
	 */
	private @Indexed int type; 
	
	private @Indexed ObjectId orderId; //type=2 消费时会有订单Id
	
	private String desc;  //消费备注
	
	private int payType;  //支付方式  1：支付宝支付 , 2：微信支付, 3：余额支付, 4:系统支付
	
	private @Indexed int status; //交易状态 0：创建  1：支付完成  2：交易完成  -1：交易关闭
	
	private Double serviceCharge;// 手续费
	
	private Double currentBalance;// 当前余额
	
	private Double operationAmount;// 实际操作金额
	
	private ObjectId redPacketId;// 红包id

	private byte auditStatus; //审核状态 0:审核通过 1:待审核 2:审核拒绝 .默认通过

	private String bankCardId;

	/**
	 * @see cn.xyz.commons.constants.KConstants.PayVendorChannel
	 */
	private String payVendorChannel;

	/**
	 * 支付平台渠道商户id
	 */
	private String payMerchantId;

	private String payVendorTradeNo;

	private boolean notifyWeb;
	
	@NotSaved
	private String userName;// 用户昵称

	public boolean isNotifyWeb() {
		return notifyWeb;
	}

	public void setNotifyWeb(boolean notifyWeb) {
		this.notifyWeb = notifyWeb;
	}

	public String getPayVendorTradeNo() {
		return payVendorTradeNo;
	}

	public void setPayVendorTradeNo(String payVendorTradeNo) {
		this.payVendorTradeNo = payVendorTradeNo;
	}

	public String getPayMerchantId() {
		return payMerchantId;
	}

	public void setPayMerchantId(String payMerchantId) {
		this.payMerchantId = payMerchantId;
	}

	public String getPayVendorChannel() {
		return payVendorChannel;
	}

	public void setPayVendorChannel(String payVendorChannel) {
		this.payVendorChannel = payVendorChannel;
	}

	public String getBankCardId() {
		return bankCardId;
	}

	public void setBankCardId(String bankCardId) {
		this.bankCardId = bankCardId;
	}

	public byte getAuditStatus() {
		return auditStatus;
	}

	public void setAuditStatus(byte auditStatus) {
		this.auditStatus = auditStatus;
	}

	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public Double getMoney() {
		if(0<money){
			DecimalFormat df = new DecimalFormat("#.00");
			 money = Double.valueOf(df.format(money));
		}
		return money;
	}
	public void setMoney(Double money) {
		if(0<money){
			DecimalFormat df = new DecimalFormat("#.00");
			 money= Double.valueOf(df.format(money));
		}
		 
		this.money = money;
	}
	public Double getStartMoney() {
		return startMoney;
	}
	public void setStartMoney(Double startMoney) {
		this.startMoney = startMoney;
	}
	public Double getEndMoney() {
		return endMoney;
	}
	public void setEndMoney(Double endMoney) {
		this.endMoney = endMoney;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public ObjectId getOrderId() {
		return orderId;
	}
	public void setOrderId(ObjectId orderId) {
		this.orderId = orderId;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getPayType() {
		return payType;
	}
	public void setPayType(int payType) {
		this.payType = payType;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Integer getToUserId() {
		return toUserId;
	}
	public void setToUserId(int toUserId) {
		this.toUserId = toUserId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	/**
	 * @return the serviceCharge
	 */
	public Double getServiceCharge() {
		return serviceCharge;
	}
	/**
	 * @param serviceCharge the serviceCharge to set
	 */
	public void setServiceCharge(Double serviceCharge) {
		this.serviceCharge = serviceCharge;
	}
	/**
	 * @return the currentBalance
	 */
	public Double getCurrentBalance() {
		return currentBalance;
	}
	/**
	 * @param currentBalance the currentBalance to set
	 */
	public void setCurrentBalance(Double currentBalance) {
		this.currentBalance = currentBalance;
	}
	/**
	 * @return the operationAmount
	 */
	public Double getOperationAmount() {
		return operationAmount;
	}
	/**
	 * @param operationAmount the operationAmount to set
	 */
	public void setOperationAmount(Double operationAmount) {
		this.operationAmount = operationAmount;
	}
	public ObjectId getRedPacketId() {
		return redPacketId;
	}
	public void setRedPacketId(ObjectId redPacketId) {
		this.redPacketId = redPacketId;
	}
	
}
