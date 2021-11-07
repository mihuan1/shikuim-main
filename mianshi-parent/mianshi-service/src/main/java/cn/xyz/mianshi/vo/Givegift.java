package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.NotSaved;

import cn.xyz.commons.utils.JSONUtil;

/**
 * 送礼物记录
 * @author Administrator
 *
 */

@Entity(value = "givegift", noClassnameStored = true)
public class Givegift {
	private @Id ObjectId id;// 送礼物记录Id
	private int count;// 礼物数量
	private ObjectId giftId;// 礼物Id
	private @Indexed ObjectId msgId;// 送礼物所属消息Id
	private String nickname;// 送礼物用户昵称
	private Double price;// 礼物价格
	private Double actualPrice;// 实收金额
	private long time;// 送礼物时间
	private @Indexed int userId;// 送礼物用户Id
	private int toUserId;//接收礼物用户Id
	@NotSaved
	private String giftName;// 礼物名称
	@NotSaved
	private String liveRoomName;// 直播间名称
	@NotSaved
	private String userName;// 送礼物用户昵称
	@NotSaved
	private String toUserName;// 接收礼物用户昵称
	
	public Givegift() {}
	
	public Givegift(int count, ObjectId giftId,ObjectId msgId, String nickname, Double price, long time,
			int userId, int toUserId) {
		this.count = count;
		this.giftId = giftId;
		this.msgId = msgId;
		this.nickname = nickname;
		this.price = price;
		this.time = time;
		this.userId = userId;
		this.toUserId = toUserId;
	}

	public int getCount() {
		return count;
	}

	public ObjectId getGiftId() {
		return giftId;
	}

	public ObjectId getMsgId() {
		return msgId;
	}

	public String getNickname() {
		return nickname;
	}
	
	public Double getPrice() {
		return price;
	}
	
	public long getTime() {
		return time;
	}

	public int getUserId() {
		return userId;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setGiftId(ObjectId giftId) {
		this.giftId = giftId;
	}

	public void setMsgId(ObjectId msgId) {
		this.msgId = msgId;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getToUserId() {
		return toUserId;
	}

	public void setToUserId(int toUserId) {
		this.toUserId = toUserId;
	}

	@Override
	public String toString() {
		return JSONUtil.toJSONString(this);
	}

	public String getGiftName() {
		return giftName;
	}

	public void setGiftName(String giftName) {
		this.giftName = giftName;
	}

	public Double getActualPrice() {
		return actualPrice;
	}

	public void setActualPrice(Double actualPrice) {
		this.actualPrice = actualPrice;
	}

	public String getLiveRoomName() {
		return liveRoomName;
	}

	public void setLiveRoomName(String liveRoomName) {
		this.liveRoomName = liveRoomName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getToUserName() {
		return toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

}
