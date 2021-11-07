package com.shiku.push.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.springframework.core.io.ClassPathResource;

public class GooglePushUtil extends PushServiceUtils{
	
	public InputStream getJson() throws FileNotFoundException{
		if(getPushConfig().getFCM_keyJson().startsWith("classpath:")) {
			ClassPathResource resource = new ClassPathResource(getPushConfig().getFCM_keyJson());
			String path = resource.getClassLoader().getResource(getPushConfig().getFCM_keyJson().replace("classpath:", "")).getPath();
			getPushConfig().setFCM_keyJson(path);
			return this.getClass().getResourceAsStream(getPushConfig().getFCM_keyJson());
		}
		return new FileInputStream(new File(getPushConfig().getFCM_keyJson()));
	}
}
