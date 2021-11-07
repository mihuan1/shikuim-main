package com.shiku.commons;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shiku.UploadApplication;
import com.shiku.commons.utils.ResourcesDBUtils;

/**
* @Description: TODO(定时删除文件 )
* @author lidaye
* @date 2018年5月24日 
*/
public class FileDeleteTask extends TimerTask {
	private static final Logger log = LoggerFactory.getLogger(UploadApplication.class);
	public FileDeleteTask() {
		log.info(getClass()+"======> init >");
	}
	
	@Override
	public void run() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		log.info("=== 执行定时任务 删除 run =====> "+dateString);
		
		ResourcesDBUtils.runDeleteFileTask();
		
		
	}
	
	@Override
	public boolean cancel() {
		// TODO Auto-generated method stub
		return super.cancel();
	}

}

