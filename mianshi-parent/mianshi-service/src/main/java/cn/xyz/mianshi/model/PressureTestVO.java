package cn.xyz.mianshi.model;

/** @version:（1.0） 
* @ClassName	PressureTestVO
* @Description: （压力测试data） 
* @date:2018年11月22日下午7:25:16  
*/ 
public class PressureTestVO {
	
	private long timeCost; // 发送用时
	
	private long totalNum; // 总条数
	
	private long sendNum; // 每秒发送条数
	
	private String groupName;// 群组名称
	
	private String jid;// 群组jid
	
	public PressureTestVO(long timeCost, long totalNum, long sendNum, String groupName, String jid) {
		super();
		this.timeCost = timeCost;
		this.totalNum = totalNum;
		this.sendNum = sendNum;
		this.groupName = groupName;
		this.jid = jid;
	}
	
	
	public PressureTestVO() {
		
	}

	public long getTimeCost() {
		return timeCost;
	}
	public void setTimeCost(long timeCost) {
		this.timeCost = timeCost;
	}
	public long getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(long totalNum) {
		this.totalNum = totalNum;
	}
	public long getSendNum() {
		return sendNum;
	}
	public void setSendNum(long sendNum) {
		this.sendNum = sendNum;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getJid() {
		return jid;
	}
	public void setJid(String jid) {
		this.jid = jid;
	}
	
}
