package cn.xyz.mianshi.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author lidaye
 *
 */
@Setter
@Getter
public class PressureParam {

	
	
	private  int checkNum=1;
	
	private int sendMsgNum=1;
	
	private String roomJid="";
	
	private int timeInterval=10;
	
	private List<String> jids;
	
	private AtomicInteger atomic;
	
	private int sendAllCount;
	
	private List<XMPPTCPConnection> conns;
	
	private long startTime;
	
	
	private String timeStr;
	

	
	@Setter
	@Getter
	public static class PressureResult{
		private int sendAllCount;// 发送总条数
		
		private long timeCount;// 总用时
		
		private String TimeStr;// 当前批次
	}
	
}
