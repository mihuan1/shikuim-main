package tigase.shiku;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import tigase.conf.Configurable;
import tigase.conf.ConfigurationException;
import tigase.server.ComponentInfo;
import tigase.server.Message;
import tigase.server.Packet;
import tigase.server.PacketFilterIfc;
import tigase.server.QueueType;
import tigase.shiku.conf.ShikuConfigBean;
import tigase.stats.StatisticsList;
import tigase.xml.Element;
import tigase.xml.XMLNodeIfc;
import tigase.xmpp.JID;
import tigase.xmpp.StanzaType;

public class ShikuKeywordFilter implements PacketFilterIfc,Configurable{
	
	
	public static List<String> keyWords=null;
	
	private  Logger logger = LoggerFactory.getLogger(ShikuKeywordFilter.class.getName());
	
	@Override
	public Packet filter(Packet packet) {
		StanzaType type = packet.getType();
		
		 if (Message.ELEM_NAME!= packet.getElemName()) {
			return packet;
		}else if (null==(packet.getElement().findChildStaticStr(Message.MESSAGE_BODY_PATH))){
			return packet;
		}else if ((type != null) && (type != StanzaType.chat) && (type != StanzaType.groupchat)) {
			return packet;
		}
		long startTime = System.currentTimeMillis();
		String body=packet.getElement().getChildCData(Message.MESSAGE_BODY_PATH);
		
		body = body.replaceAll("&quot;", "\"").replaceAll("&quot;", "\"");
		JSONObject bodyObj=null;
		try {
			bodyObj=JSON.parseObject(body);
			
		} catch (Exception e) {
			return packet;
		}
		int contentType=0;
		contentType=bodyObj.getIntValue("type");
		//已经过滤过了  即返回
		if(1==bodyObj.getIntValue("filter"))
			return packet;
		//过滤群聊的已读回执
		/*if(StanzaType.groupchat==packet.getType()&&26==type)
			return null;*/
		if(ShikuConfigBean.shikuMsgSendTime==1)
			setNewTimeSend(packet, body, bodyObj);
		
		if(0==ShikuConfigBean.OPENKEYWORD_VAL)
			return packet;
		
		String content=null;
		
		if(contentType!=1)
			return packet;
		content=bodyObj.getString("content");
		if(content==null)
			return packet;
		if(filterKeyWords(content)){
			if(ShikuConfigBean.isDeBugMode()) {
				long endTime = System.currentTimeMillis();
				logger.info("ShikuKeywordFilter  输入字符不规范   {} ",content);
				long time=(endTime-startTime);
				if(time>1000)
					logger.info("程序运行时间： {}  ms",time);
			}
			return null;
		}else{
			/*if(ShikuConfigBean.isDeBugMode()) {
				long endTime = System.currentTimeMillis();
				System.out.println("程序运行时间："+(endTime-startTime)+"ms");
			}*/
			return packet;
		}
		
	}
	
	@Override
	public void processPacket(Packet packet, Queue<Packet> results) {
		// TODO Auto-generated method stub
		/*String body=packet.getElement().getChildCData(Message.MESSAGE_BODY_PATH);
		if(body==null)
			
		body = body.replaceAll("&quot;", "\"").replaceAll("&quot;", "\"");
		JSONObject bodyObj=JSON.parseObject(body);
		
		System.out.println("filter  "+packet.getElement().getChildCData(Message.MESSAGE_BODY_PATH));
		Packet packet2=setNewTimeSend(packet.copyElementOnly(), body, bodyObj);
		System.out.println("filter 2 "+packet2.getElement().getChildCData(Message.MESSAGE_BODY_PATH));
		
		results.offer(packet2);*/
	}
	
	private boolean filterKeyWords(String keyword){
		
		for (String key : ShikuKeywordFilter.keyWords) {
			   if(contain2(key, keyword))
		         return true;
			   
			   /*if(-1!=keyword.indexOf(key)||keyword.equalsIgnoreCase(key))
					return true;*/
			
		}
		
		return false;
	}
	
	/*** 
     * 是否包含指定字符串,不区分大小写 
     * @param source : 原字符串 
     * @param keyword 
     * @param replacement 
     * @return 
     */  
	private boolean contain2(String source, String keyword) {
		Pattern p = Pattern.compile(source, Pattern.CASE_INSENSITIVE);  
        Matcher m = p.matcher(keyword);  
       return m.find();
	}
	
	
		///设置 timesend 为服务器的时间
		private Packet setNewTimeSend(Packet copyPacket,String body,JSONObject jsonObj){
			Long time = System.currentTimeMillis(); 
			double timeSend=getTimeSend(time);
			//替换timeSend 成服务器的时间
			body=replaceTimeSend(timeSend, body, jsonObj);
			
			
			List<Element> children = copyPacket.getElement().getChildren();
			Element removeEle=null;
			for (Element element : children) {
				if("body".equals(element.getName())){
					removeEle=element;
					break;
				}
			}
			children.remove(removeEle);
			
			Element newBody = new Element("body",body);
			children.add(newBody);
			
			List<XMLNodeIfc> childNew=new ArrayList<XMLNodeIfc>();
			for (Element element : children) {
				childNew.add(element);
			}
			//System.out.println("setNewTimeSend childNew "+childNew);
			copyPacket.getElement().setChildren(childNew);
			return copyPacket;
		};
	private JSONObject bodyToJson(String body){
		JSONObject jsonObj=null;
		try {
			jsonObj= JSON.parseObject(body.replaceAll("&quot;", "\"").replaceAll("&quot;", "\""));
			return jsonObj;
		} catch (JSONException e) {
			return null;
		}
	}
	private double getTimeSend(long ts){
		double time =(double)ts;
		DecimalFormat dFormat = new DecimalFormat("#.000");
		return new Double(dFormat.format(time/1000));
	}
	private String replaceTimeSend(Double timeSend,String body,JSONObject jsonObj){
		String oldTime=jsonObj.getString("timeSend");
		if(oldTime==null){
			return body;
		}
		jsonObj.put("timeSend", timeSend);
		jsonObj.put("filter", 1);
		return jsonObj.toString();
	}

	@Override
	public void init(String name, QueueType qType) {
		// TODO Auto-generated method stub
		logger.info("===ShikuKeywordFilter====>  init");
	}

	@Override
	public void initializationCompleted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JID getComponentId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComponentInfo getComponentInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInitializationComplete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, Object> getDefaults(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperties(Map<String, Object> properties) throws ConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getStatistics(StatisticsList list) {
		// TODO Auto-generated method stub
		
	}
	
	

}
