package cn.xyz.mianshi.vo;

import org.mongodb.morphia.annotations.NotSaved;

public class Language {
	
	
	private String zh;
	private String en;
	private String big5;
	private String ms;//马来语
	
	
	
	private String key="zh"; //zh en
	private String name="中文";//中文 英文
	@NotSaved
	private String value;
	
	

	
	public String getZh() {
		return zh;
	}
	public void setZh(String zh) {
		this.zh = zh;
	}
	public String getEn() {
		return en;
	}
	public void setEn(String en) {
		this.en = en;
	}
	public String getBig5() {
		return big5;
	}
	public void setBig5(String big5) {
		this.big5 = big5;
	}
	public String getMs() {
		return ms;
	}
	public void setMs(String ms) {
		this.ms = ms;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	

}
