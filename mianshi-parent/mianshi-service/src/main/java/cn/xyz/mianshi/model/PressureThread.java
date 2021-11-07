package cn.xyz.mianshi.model;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.ping.PingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;


public class PressureThread implements Runnable{
	
	
	public static Logger logger = LoggerFactory.getLogger(PressureThread.class);
	
	private PressureParam param;
	
	private String jid;
	
	private String roomName;
	
	private AtomicInteger mySendCount;
	private List<MultiUserChat> mucChats=Collections.synchronizedList(new ArrayList<>());
	
	public PressureThread() {
		// TODO Auto-generated constructor stub
	}
	
	public PressureThread(String jid,PressureParam param,List<MultiUserChat> mucChats) { this.jid=jid;
		this.param=param;
		this.mucChats=mucChats;
		this.mySendCount=new AtomicInteger(0);
	}
	@Override
	public void run() {
//		double timeSend = getTimeSend(System.currentTimeMillis());
		if(param.getAtomic().get()>=param.getSendAllCount()) {
			return;
		}
		int i=mySendCount.get();
		if(i>=param.getSendMsgNum()) {
			return;
		}
		roomName=SKBeanUtils.getRoomManager().getRoomName(jid);
		
		String content = "=== ";
		// TODO Auto-generated method stub
		MultiUserChat muc =null;
		MessageBean messageBean =null;
		String userId=null;
		long timeSend = 0;
			// 获取connList size 大小的随机数  随机发送消息
		int nextInt = new Random().nextInt(mucChats.size());
		muc=mucChats.get(nextInt);
	 	messageBean= new MessageBean();
		userId=muc.getNickname().toString();
		messageBean.setFromUserId(userId);
		messageBean.setFromUserName(userId);
		messageBean.setToUserId(jid);
		messageBean.setType(1);
		timeSend = DateUtil.getSysCurrentTimeMillis_sync()+i;
		messageBean.setTimeSend(getTimeSend(timeSend));
		messageBean.setMessageId(StringUtil.randomUUID());
		messageBean.setContent(param.getTimeStr()+" "+userId+" "+ content + i);// 批次 + userId + 消息序号
		try {
			muc.sendMessage(messageBean.toString());
			logger.info(" timeStr {}  {}  === {}  sendMsg muc  {} count {}  {}  time {}",param.getTimeStr(),userId,roomName,i,param.getAtomic().incrementAndGet(),messageBean.getTimeSend());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}finally {
			mySendCount.incrementAndGet();		
		}
		
		/*
		 * for (MultiUserChat musc : mucChats) { XMPPTCPConnection conn =
		 * (XMPPTCPConnection) musc.getXmppConnection();
		 * PingManager.getInstanceFor(conn).setPingInterval(100); }
		 */
		
	}
	
	public String getMucChatServiceName(XMPPTCPConnection conn){
		return "@muc."+conn.getXMPPServiceDomain();
	}
	
	private double getTimeSend(long ts){
		double time =(double)ts;
		DecimalFormat dFormat = new DecimalFormat("#.000");
		return new Double(dFormat.format(time/1000));
	}
	
	public static class MessageBean {
		private Object content;
		private String fileName;
		private String fromUserId = "10005";
		private String fromUserName = "10005";
		private Object objectId;
		private double timeSend ;
		private String toUserId;
		private String toUserName;
		private int fileSize;
		private int type;
		
		private String messageId;
		
		private String other;

		public Object getContent() {
			return content;
		}

		public String getFileName() {
			return fileName;
		}

		public String getFromUserId() {
			return fromUserId;
		}

		public String getFromUserName() {
			return fromUserName;
		}

		public Object getObjectId() {
			return objectId;
		}

		public double getTimeSend() {
			return timeSend;
		}

		public String getToUserId() {
			return toUserId;
		}

		public String getToUserName() {
			return toUserName;
		}
		
		public int getFileSize() {
			return fileSize;
		}

		public void setFileSize(int fileSize) {
			this.fileSize = fileSize;
		}

		public int getType() {
			return type;
		}

		public void setContent(Object content) {
			this.content = content;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public void setFromUserId(String fromUserId) {
			this.fromUserId = fromUserId;
		}

		public void setFromUserName(String fromUserName) {
			this.fromUserName = fromUserName;
		}

		public void setObjectId(Object objectId) {
			this.objectId = objectId;
		}

		public void setTimeSend(double timeSend) {
			this.timeSend = timeSend;
		}

		public void setToUserId(String toUserId) {
			this.toUserId = toUserId;
		}

		public void setToUserName(String toUserName) {
			this.toUserName = toUserName;
		}

		public void setType(int type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return JSON.toJSONString(this);
		}

		public String getOther() {
			return other;
		}

		public void setOther(String other) {
			this.other = other;
		}

		public String getMessageId() {
			return messageId;
		}

		public void setMessageId(String messageId) {
			this.messageId = messageId;
		}

	}
	public static String getFullString() {
		return new SimpleDateFormat("MM-dd HH:mm").format(currentTimeSeconds());
	}
	public static long currentTimeSeconds() {
		return System.currentTimeMillis();
	}
}
