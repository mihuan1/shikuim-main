package cn.xyz.mianshi.vo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
/**
 * 礼物类
 * @author Administrator
 *
 */
@Entity(value="s_gift",noClassnameStored=true)
public class Gift {
	@Id
	private ObjectId giftId;//礼物id
	private String name;//礼物名称
	private String photo;//礼物图片
	private	double price;//礼物价格
	private int type;//礼物类型
	
	public Gift() {}

	public Gift(ObjectId giftId, String name, String photo, double price, int type) {
		this.giftId = giftId;
		this.name = name;
		this.photo = photo;
		this.price = price;
		this.type = type;
	}

	public ObjectId getGiftId() {
		return giftId;
	}

	public void setGiftId(ObjectId giftId) {
		this.giftId = giftId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
}
