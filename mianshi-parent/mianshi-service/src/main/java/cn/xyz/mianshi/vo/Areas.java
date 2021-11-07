package cn.xyz.mianshi.vo;

import org.mongodb.morphia.annotations.Entity;

@Entity(value="tb_areas")
public class Areas extends Constant {
	
	private int type;
	
	private String zip;
	

	private String ab;
	

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}
	

	public String getAb() {
		return ab;
	}

	public void setAb(String ab) {
		this.ab = ab;
	}

	

	

}
