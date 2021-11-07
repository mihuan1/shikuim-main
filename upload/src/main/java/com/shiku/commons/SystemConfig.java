package com.shiku.commons;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties
public  class SystemConfig {
	
	
	public  int beginIndex;
	public  String domain;
	public  int isBackDomain=1;//上传的文件是否返回域名  默认返回   1:返回    0：不返回
	
	/**
	 * 文件 保存数据库的 uri
	 */
	public  String dbUri;
	public  int isOpenfastDFS=0;//是否开启fastDFs 文件系统
	
	public  String fastdfsDomain;
	
	/**
	 * 开启定时任务   删除文件
		  0  关闭     1 开启 
		 在部署 多个 upload 项目的情况下 
		只需要 一个 项目 执行定时任务就可以了
	 */
	public  int openTask=0;
	
	public  String basePath;
	
	
	public  String uTemp;
	public  String nTemp;
	public  String oTemp;
	public  String tTemp;
	
	
	public  String imageFilter;
	public  String audioFilter;
	public  String videoFilter;
	
	public  int amr2mp3;
	
	
	public String getAudioFilter() {
		return audioFilter;
	}

	public void setAudioFilter(String audioFilter) {
		this.audioFilter = audioFilter;
	}

	public int getBeginIndex() {
		return beginIndex;
	}

	public void setBeginIndex(int beginIndex) {
		this.beginIndex = beginIndex;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public int getIsBackDomain() {
		return isBackDomain;
	}

	public void setIsBackDomain(int isBackDomain) {
		this.isBackDomain = isBackDomain;
	}

	public String getDbUri() {
		return dbUri;
	}

	public void setDbUri(String dbUri) {
		this.dbUri = dbUri;
	}

	public int getIsOpenfastDFS() {
		return isOpenfastDFS;
	}

	public void setIsOpenfastDFS(int isOpenfastDFS) {
		this.isOpenfastDFS = isOpenfastDFS;
	}

	public String getFastdfsDomain() {
		return fastdfsDomain;
	}

	public void setFastdfsDomain(String fastdfsDomain) {
		this.fastdfsDomain = fastdfsDomain;
	}

	public int getOpenTask() {
		return openTask;
	}

	public void setOpenTask(int openTask) {
		this.openTask = openTask;
	}

	public String getImageFilter() {
		return imageFilter;
	}

	public void setImageFilter(String imageFilter) {
		this.imageFilter = imageFilter;
	}

	public String getnTemp() {
		return nTemp;
	}

	public void setnTemp(String nTemp) {
		this.nTemp = nTemp;
	}

	public String getoTemp() {
		return oTemp;
	}

	public void setoTemp(String oTemp) {
		this.oTemp = oTemp;
	}

	public String gettTemp() {
		return tTemp;
	}

	public void settTemp(String tTemp) {
		this.tTemp = tTemp;
	}

	public String getuTemp() {
		return uTemp;
	}

	public void setuTemp(String uTemp) {
		this.uTemp = uTemp;
	}

	public String getVideoFilter() {
		return videoFilter;
	}

	public void setVideoFilter(String videoFilter) {
		this.videoFilter = videoFilter;
	}

	public int getAmr2mp3() {
		return amr2mp3;
	}

	public void setAmr2mp3(int amr2mp3) {
		this.amr2mp3 = amr2mp3;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	/**
	* @return basePath
	*/
	public String getBasePath() {
		return basePath;
	}
	
}
