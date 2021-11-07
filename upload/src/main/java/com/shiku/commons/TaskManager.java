package com.shiku.commons;

import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shiku.UploadApplication;
import com.shiku.commons.utils.ConfigUtils;
import com.shiku.commons.utils.ResourcesDBUtils.Expire;


/**
* @Description: TODO(用一句话描述该文件做什么)
* @author lidaye
* @date 2018年5月24日 
*/
public class TaskManager {
	
	private static final Logger log = LoggerFactory.getLogger(UploadApplication.class);
	 /**  
	  * 无延迟  
	  */  
	 public static final long NO_DELAY = 0;   
	 /**  
	  * 定时器  
	  */  
	 private Timer timer;  
	 
	 
	public void onStartup(){
		log.info("=== HttpServlet  init  openTask "+ConfigUtils.getSystemConfig().getOpenTask());
 		if(0==ConfigUtils.getSystemConfig().getOpenTask())
 			return;
 		timer=new Timer();
 		FileDeleteTask task = new FileDeleteTask();
 		timer.schedule(task, Expire.HOUR12*1000,Expire.HOUR12*1000);
	}
	
	

}

