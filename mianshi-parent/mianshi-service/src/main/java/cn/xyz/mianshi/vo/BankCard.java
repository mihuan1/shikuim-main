package cn.xyz.mianshi.vo;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

@Entity(value = "BankCard", noClassnameStored = true)
@Data
public class BankCard {

	/**
	 *  卡记录唯一id
	 */
	private @Id ObjectId id;

	/**
	 *  UID
	 */
	private @Indexed Integer uid;

	/**
	 *  持卡人姓名
	 */
	private String userName;

	/**
	 *  卡号
	 */
	private String cardNo;

	/**
	 *  卡名(xx纪念卡)
	 */
	private String cardName;

	/**
	 *  卡种(0-储蓄卡 1-信用卡)
	 *  @see cn.xyz.commons.constants.KConstants.BankCardType
	 */
	private byte cardType;

	/**
	 *  银行名(中国建设银行)
	 */
	private String bankBrandName;

	/**
	 *  银行id
	 * @see cn.xyz.commons.constants.KConstants.Bank.id
	 */
	private Integer bankBrandId;

	/**
	 *  开户行地址
	 */
	private String openBankAddr;

	/**
	 * 绑定时间
	 */
	private long time;

	/**
	 * 是否删除(0-否 1-是)
	 */
//	@JsonIgnore
	private byte isDeleted;

	public static void main(String[] args) {
		BankCard bankCard = new BankCard();
		bankCard.setId(new ObjectId());
		bankCard.setUid(234);
		bankCard.setUserName("姓名");
		bankCard.setCardNo("23044873134");
		bankCard.setCardName("白金纪念卡");
		bankCard.setCardType((byte)0);
		bankCard.setBankBrandName("中国建设银行");
		bankCard.setBankBrandId(102);
		bankCard.setOpenBankAddr("北京市xx路");
		bankCard.setIsDeleted((byte)0);
		System.out.println(JSON.toJSONString(bankCard));
	}

}
