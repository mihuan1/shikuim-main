package com.shiku.commons.utils;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
* @Description: TODO(文件资源数据库操作工具 类)
* @author lidaye
* @date 2018年5月24日 
*/
public class ResourcesDBUtils {
	
	private static final Logger log = LoggerFactory.getLogger(ResourcesDBUtils.class);
	private static MongoCollection dbCollection;
	
	 //private static MongoClient mongoClient;
	
	static final String dbName="resources";
	 
	 public interface Expire {
			static final int DAY1 = 86400;
			static final int DAY7 = 604800;
			static final int HOUR12 = 43200;
			static final int HOUR=3600;
	}
	 static{
		 String urIStr=ConfigUtils.getSystemConfig().getDbUri();
		 if(StringUtils.isEmpty(urIStr)){
			 log.error("===> error msg dbUri is null =====>");
		 }
		 MongoClient mongoClient=MongoDBUtil.getMongoClient(urIStr);
		 
		 MongoDatabase db = mongoClient.getDatabase(dbName);
			
		 dbCollection=db.getCollection(dbName);
	 }
	 
	 private static MongoCollection getCollection(){
		 if(null!=dbCollection)
			 return dbCollection;
		 try {
			 String urIStr=ConfigUtils.getSystemConfig().getDbUri();
			 if(StringUtils.isEmpty(urIStr)){
				 log.error("===> error msg dbUri is null =====>");
			 }
			 MongoClient mongoClient=MongoDBUtil.getMongoClient(urIStr);
			 
			 MongoDatabase db = mongoClient.getDatabase(dbName);
				
			 dbCollection=db.getCollection(dbName);
			 return dbCollection;
		} catch (Exception e) {
			e.printStackTrace();
			return dbCollection;
		}
		
		 
		
		 
	 }
	 
	 /**
	 * @Description: TODO(保存文件的 url 到数据库)
	 * @param @param type  1 本机文件系统  2 fastDfs
	 * @param @param path  文件的url
	 * @param @param validTime   文件的有效期   0/-1 为永久 有效期       1<validTime 有效期 多少天
	  */
	 public static void saveFileUrl(int type,String url,double validTime){
		 long cuTime=System.currentTimeMillis()/1000;
		 long endTime=-1;
		 
		 if(validTime>0)
				endTime=cuTime+(long)(Expire.DAY1* validTime);
		 else endTime=-1;
		 
		/**
		 * 获取文件的真实地址  不带 域名
		 */
		String path=FileUtils.getAbsolutePath(url);
		if(1==type) {
			path=ConfigUtils.getBasePath()+"/"+path;
		}
		
		 Document document=new Document("createTime", cuTime);
		 
		 
		 document.append("endTime", endTime);
		 document.append("url", url);
		 document.append("path", path);
		 document.append("type", type);
		 document.append("status", 1);
		
		getCollection().insertOne(document);
		 
	 }
	 
	 /**
	 * @Description: TODO(删除数据库的文件)
	 * @param @param path  文件的path
	  */
	 public static void deleteFile(String path){
		
		 
		 Document query=new Document("path", path);
		
		
		getCollection().deleteOne(query);
		 
	 }
	 
	 /**
	 * @Description: TODO(修改文件的状态  )
	 * @param @param path  文件的url
	 * 
	  */
	 public static void updateFileStatus(String path,int status){
		
		 
		 Document query=new Document("url", path);
		
		 Document values=new Document("status", path);
		
		getCollection().updateOne(query, new Document("$set", values));
		 
	 }
	 
	 
	 public static void runDeleteFileTask(){
		
		 long cuTime=System.currentTimeMillis()/1000;
		 Document query=new Document("endTime", new Document("$gt", 0).append("$lt", cuTime));
		 MongoCollection collection = getCollection();
		 long count = collection.count(query);
		 MongoCursor<Document> cursor= collection.find(query).iterator();
		 Document resultDoc=null;
		 String path=null;
		 String url="";
		 int type=1;
		 log.info(" runDeleteFileTask query count {} ",count);
		 while (cursor.hasNext()) {
				resultDoc=cursor.next();
				if(null==resultDoc)
					continue;
				log.info(" run delete task {} ",resultDoc);
				path=resultDoc.getString("path");
				 type = resultDoc.getInteger("type");
				if(StringUtils.isEmpty(path))
					path=resultDoc.getString("url");
				if(StringUtils.isEmpty(path))
					continue;
				if(2==type)
					FastDFSUtils.deleteFile(path);
				else {
					FileUtils.deleteFile(path);
				}
			}
		    getCollection().deleteMany(query);
	}

}

