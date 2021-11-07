package cn.xyz.mianshi.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.mongodb.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import cn.xyz.commons.autoconfigure.KApplicationProperties.AppConfig;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.utils.FileUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.vo.Areas;
import cn.xyz.mianshi.vo.Constant;
import cn.xyz.mianshi.vo.Language;
import cn.xyz.mianshi.vo.SmsRecord;

@Component
public class ConstantUtil implements ApplicationContextAware {

	/*private static Map<Integer, TaskVO> mapTask = Maps.newHashMap();
	private static Map<Integer, ConstantVO> mapConstant = Maps.newHashMap();
	private static Map<Integer, ConstantVO> mapCity = Maps.newHashMap();*/
	
	private static Map<String, Constant> errorMsgMap = Maps.newHashMap();
	
	public static Datastore dsForRW;
	
	@Resource(name = "dsForRW")
	private  Datastore ds;
	
	
	public static AppConfig appConfig;
	
	@Autowired(required=false)
	private AppConfig appConf;
	
	public static BasicDBObject staticProjection;
	
	public final static String defLanguage="zh";
	
	static{
		staticProjection=new BasicDBObject("id", 1);
		staticProjection.append(defLanguage, 1);
		staticProjection.append("zh", 1);
		staticProjection.append("_id", 0);
	}
	
	public static int getAppDefDistance(){
		return appConfig.getDistance();
	}
	
	/**
	 * 通过url 删除文件
	 * @param url
	 * @throws Exception 
	 */
	public static void deleteFile(String... paths){
		try {
			String domain =  appConfig.getUploadDomain();
			//调用删除方法将文件从服务器删除
			FileUtil.deleteFileToUploadDomain(domain,paths);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
	}
	
	
	/**
	 * 
	 * @param 复制文件
	 * @param language
	 * @return
	 */
	public static String copyFile(int validTime,String... paths) {
		String url=null;
		String domain=appConfig.getUploadDomain();
		try {
			url = FileUtil.copyFileToUploadDomain(domain, validTime, paths);
		} catch (ServiceException e) {
			throw new ServiceException("文件服务器连接超时");
		} 
		return url;
	}

	
	

	public static Constant getCityName(Integer id,String language) {
		//return mapCity.get(id);
		DBCollection collection=dsForRW.getDB().getCollection("tb_areas");
		
		 if(StringUtil.isEmpty(language))
				language="zh";
		 BasicDBObject query=new BasicDBObject("id", id);
		 BasicDBObject projection=staticProjection;	
		 	projection.append(language, 1);
		 BasicDBObject obj= (BasicDBObject) collection.findOne(query,projection);
			if(null==obj)
				return null;
			Constant constant=new Constant();
			constant.setId(obj.getInt("id"));
			if(!StringUtil.isEmpty(obj.getString(language)))
				constant.setValue(obj.getString(language));
			else constant.setValue(obj.getString(defLanguage));
			constant.setName(obj.getString("zh"));
			return constant;
	}
	public static Areas getAreasByName(String name) {
		DBCollection collection=dsForRW.getDB().getCollection("tb_areas");
		 BasicDBObject query=new BasicDBObject("zh", name);
		 BasicDBObject projection=staticProjection;	
			projection.append("type", 1);
			projection.append("code", 1);
			projection.append("ab", 1);
			
		 BasicDBObject obj= (BasicDBObject) collection.findOne(query,projection);
			if(null==obj)
				return null;
			Areas areas=new Areas();
			areas.setId(obj.getInt("id"));
			areas.setType(obj.getInt("type"));
			areas.setValue(obj.getString("zh"));
			areas.setCode(obj.getString("code"));
			areas.setAb(obj.getString("ab"));
			areas.setName(areas.getValue());
			return areas;
	}
	public static Areas getAreasByCode(String code) {
		DBCollection collection=dsForRW.getDB().getCollection("tb_areas");
		 BasicDBObject query=new BasicDBObject("code", code);
		 query.append("type", 1);
		 BasicDBObject projection=staticProjection;	
			projection.append("type", 1);
			projection.append("code", 1);
			projection.append("ab", 1);
			
		 BasicDBObject obj= (BasicDBObject) collection.findOne(query,projection);
			if(null==obj)
				return null;
			Areas areas=new Areas();
			areas.setId(obj.getInt("id"));
			areas.setType(obj.getInt("type"));
			areas.setValue(obj.getString("zh"));
			areas.setCode(obj.getString("code"));
			areas.setAb(obj.getString("ab"));
			areas.setName(areas.getValue());
			return areas;
	}
	public static Areas getAreasById(Integer id) {
		DBCollection collection=dsForRW.getDB().getCollection("tb_areas");
		 BasicDBObject query=new BasicDBObject("id", id);
		 query.append("type", 1);
		 BasicDBObject projection=staticProjection;	
			projection.append("type", 1);
			projection.append("code", 1);
			projection.append("ab", 1);
		 BasicDBObject obj= (BasicDBObject) collection.findOne(query,projection);
			if(null==obj)
				return null;
			Areas areas=new Areas();
			areas.setId(obj.getInt("id"));
			areas.setType(obj.getInt("type"));
			areas.setValue(obj.getString("zh"));
			areas.setCode(obj.getString("code"));
			areas.setAb(obj.getString("ab"));
			areas.setName(areas.getValue());
			return areas;
	}
	public static Areas getAreasByCountry(String country) {
		DBCollection collection=dsForRW.getDB().getCollection("tb_areas");
		 BasicDBObject query=new BasicDBObject("ab", country);
		 query.append("type", 1);
		 BasicDBObject projection=staticProjection;	
			projection.append("type", 1);
			projection.append("code", 1);
			projection.append("ab", 1);
		 BasicDBObject obj= (BasicDBObject) collection.findOne(query,projection);
			if(null==obj)
				return null;
			Areas areas=new Areas();
			areas.setId(obj.getInt("id"));
			areas.setType(obj.getInt("type"));
			areas.setValue(obj.getString("zh"));
			areas.setCode(obj.getString("code"));
			areas.setAb(obj.getString("ab"));
			areas.setName(areas.getValue());
			return areas;
	}
	public static Constant getMsgByCode(String code) {
		 return getMsgByCode(code, defLanguage);
	}
	public static Constant getMsgByCode(String code,String language) {
		 if(StringUtil.isEmpty(language))
				language=defLanguage;
		Constant constant=errorMsgMap.get(code);
		if(null!=constant&&null!=constant.getMap()){
			constant.setValue(constant.getMap().get(language));
			if(StringUtil.isEmpty(constant.getValue()))
				constant.setValue(constant.getMap().get(defLanguage));
			return constant;
		}
		DBCollection collection=dsForRW.getDB().getCollection("message");
		 BasicDBObject query=new BasicDBObject("code", code);
		BasicDBObject projection=new BasicDBObject(language, 1);
		projection.append("code",1);
			BasicDBObject obj= (BasicDBObject) collection.findOne(query,projection);
			if(null==obj)
				return null;
			 constant=new Constant();
			constant.setCode(obj.getString("code"));
			if(!StringUtil.isEmpty(obj.getString(language)))
				constant.setValue(obj.getString(language));
			else constant.setValue(obj.getString(defLanguage));
			constant.setName(obj.getString("zh"));
			return constant;
	}
	public static List<Constant> getMsgByType(String type,String language) {
		//return mapConstant.get(id);
		List<Constant> list=Lists.newArrayList();
		DBCollection collection=dsForRW.getDB().getCollection("message");
		 if(StringUtil.isEmpty(language))
				language="zh";
		 BasicDBObject query=new BasicDBObject("type", type);
		BasicDBObject projection=new BasicDBObject(language, 1);
		projection.append("code",1);
		DBCursor cursor =collection.find(query,projection);
		if(null==cursor)
			return null;
		Constant constant=null;
		BasicDBObject obj =null;
		while (cursor.hasNext()) {
				obj = (BasicDBObject) cursor.next();
				constant=new Constant();
				constant.setCode(obj.getString("code"));
				if(!StringUtil.isEmpty(obj.getString(language)))
					constant.setValue(obj.getString(language));
				else constant.setValue(obj.getString(defLanguage));
				constant.setName(obj.getString("zh"));
				list.add(constant);
		}
		
		return list;
	}
	public static Map<String, String> getMsgMapByType(String type,String language) {
		//return mapConstant.get(id);
		Map<String, String> map=Maps.newHashMap();
		DBCollection collection=dsForRW.getDB().getCollection("message");
		 if(StringUtil.isEmpty(language))
				language="zh";
		 BasicDBObject query=new BasicDBObject("type", type);
		BasicDBObject projection=new BasicDBObject(language, 1);
		projection.append("code",1);
		DBCursor cursor =collection.find(query,projection);
		if(null==cursor)
			return null;
		BasicDBObject obj =null;
		String value=null;
		while (cursor.hasNext()) {
				obj = (BasicDBObject) cursor.next();
				value=!StringUtil.isEmpty(obj.getString(language))?obj.getString(language):obj.getString(defLanguage);
				map.put(obj.getString("code"),value);
		}
		return map;
	}
	public static SmsRecord getSmsPrice(String areaCode) {
		DBCollection collection=dsForRW.getDB().getCollection("sms_country");
		 BasicDBObject query=new BasicDBObject("prefix", areaCode);
		BasicDBObject projection=new BasicDBObject("price", 1);
		projection.append("zh",1);
		DBCursor cursor =collection.find(query,projection);
		if(null==cursor)
			return null;
		BasicDBObject obj =null;
		SmsRecord record=null;
		while (cursor.hasNext()) {
				obj = (BasicDBObject) cursor.next();
				record=new SmsRecord();
				record.setPrice(obj.getDouble("price"));
				record.setCountry(obj.getString("zh"));
		}
		return record;
	}
	/*public static KCountry getCoutry(String areaCode) {
		DBCollection collection=dsForRW.getDB().getCollection("sms_country");
		 BasicDBObject query=new BasicDBObject("prefix", areaCode);
		 
		BasicDBObject projection=new BasicDBObject("price", 1);
		projection.append("zh",1);
		DBCursor cursor =collection.find(query,projection);
		if(null==cursor)
			return null;
		BasicDBObject obj =null;
		KCountry country=null;
		while (cursor.hasNext()) {
				obj = (BasicDBObject) cursor.next();
				country=new KCountry();
				country.setPrice(obj.getDouble("price"));
				//country.setCountry(obj.getString("zh"));
				country.setZh(obj.getString("zh"));
		}
		return country;
	}*/
	
	public static String format(Long milliseconds, String pattern) {
		Date date = new Date(milliseconds*1000);
		String dateStr=new SimpleDateFormat(pattern).format(date);
		return dateStr;
	}
	

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		System.out.println(" ConstantUtil----init--- Start");
		dsForRW=ds;
		appConfig=appConf;
		
	}
	public static void updateErrorMessage(Constant constant) {
		errorMsgMap.put(constant.getCode(), constant);
	}
	public static void deleteErrorMessage(String code) {
		errorMsgMap.remove(code);
	}
	public static void initMsgMap(){
		System.out.println(" ConstantUtil----MsgMap--- init");
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				DBCursor cursor = dsForRW.getDB().getCollection("message").find();
				Constant constant=null;
				BasicDBObject result=null;
				Map<String,String> map=null;
				while (cursor.hasNext()) {
					 result = (BasicDBObject) cursor.next();
					 constant=new Constant();
					 map=Maps.newHashMap();
					constant.setCode(result.getString("code"));
					constant.setName(result.getString("zh"));
					constant.setValue(result.getString("zh"));
					for (Language language : appConfig.getLanguages()) {
						map.put(language.getKey(), result.getString(language.getKey()));
					}
					//constant.setParentValue(ConstantUtil.getFunctionById(result.getInt("parent_id"), "").getValue());
					constant.setMap(map);
					errorMsgMap.put(constant.getCode(), constant);
				}
				System.out.println(" ConstantUtil----MsgMap--- End");
				System.out.println(" ConstantUtil----MsgMap--- "+errorMsgMap.toString());
			}
		});
		
		
	}

}
