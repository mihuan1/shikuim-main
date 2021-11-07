package com.shiku.commons.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.csource.fastdfs.ClientGlobal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.google.common.base.Joiner;
import com.shiku.commons.SystemConfig;
import com.shiku.commons.vo.FileType;


@Configuration
@ConfigurationProperties
public  class ConfigUtils {
	
	@Autowired
    private  SystemConfig config;
	
	private static SystemConfig systemConfig;
	
    private static List<String> images = new ArrayList<String>();
    private static List<String> audios = new ArrayList<String>();
    private static List<String> videos = new ArrayList<String>();
	@PostConstruct
    public void initBean(){
    	ConfigUtils.systemConfig = config;
    	if(images.size() == 0){
    		String imageFilter = getSystemConfig().getImageFilter();
    		images = getListBySplit(imageFilter);
    	}
    	if(audios.size() == 0){
    		String imageFilter = getSystemConfig().getAudioFilter();
    		audios = getListBySplit(imageFilter);
    	}
    	if(videos.size() == 0){
    		String imageFilter = getSystemConfig().getVideoFilter();
    		videos = getListBySplit(imageFilter);
    	}
    }
    
	/**
	* @return systemConfig
	*/
	public static SystemConfig getSystemConfig() {
		return systemConfig;
	}
	
	 public static String configInfo() {
		    return "{"
		      + "\n  domain = " + getSystemConfig().getDomain()
		      + "\n  isBackDomain = " + getSystemConfig().getIsBackDomain()
		      + "\n  isOpenfastDFS = " +getSystemConfig().getIsOpenfastDFS()
		      + "\n  fastdfsDomain = " + getSystemConfig().getFastdfsDomain()
		      + "\n  dbUri = " + getSystemConfig().getDbUri()
		      + "\n  openTask = " + getSystemConfig().getOpenTask()
		      + "\n  basePath = " + getBasePath()
		      + "\n  beginIndex = " + getSystemConfig().getBeginIndex()
		      + "\n  uTemp = " + getSystemConfig().getuTemp()
		      + "\n  nTemp = " + getSystemConfig().getnTemp()
		      + "\n  oTemp = " + getSystemConfig().getoTemp()
		      + "\n  tTemp = " + getSystemConfig().gettTemp()
		      + "\n  imageFilter = " + getSystemConfig().getImageFilter()
		      + "\n  audioFilter = " + getSystemConfig().getAudioFilter()
		      + "\n  videoFilter = " + getSystemConfig().getVideoFilter()
		      + "\n  amr2mp3 = " + getSystemConfig().getAmr2mp3()
		      + "\n}";
	}
	 
	 public static String getFastDFSConfigInfo(){
		 return ClientGlobal.configInfo();
	 }
	
	 /*static {
		try {
			InputStream inStream = SystemConfig.class
					.getResourceAsStream(isWindows() ? "/SystemConfig-win.properties" : "/SystemConfig.properties");
			Properties props = new Properties();
			props.load(inStream);

			domain = props.getProperty("domain");
			fastdfsDomain = props.getProperty("fastdfsDomain");
			isBackDomain=Integer.parseInt(props.getProperty("isBackDomain","1"));
			isOpenfastDFS=Integer.parseInt(props.getProperty("isOpenfastDFS","1"));
			openTask=Integer.parseInt(props.getProperty("openTask","0"));
			dbUri=props.getProperty("dbUri");
			if(StringUtils.isEmpty(dbUri)){
				System.out.println("===> error msg dbUri is null =====>");
			}
					
			basePath=props.getProperty("basePath");
			nTemp = props.getProperty("nTemp");
			oTemp = props.getProperty("oTemp");
			tTemp = props.getProperty("tTemp");
			uTemp = props.getProperty("uTemp");
			beginIndex = Integer.parseInt(props.getProperty("beginIndex"));
			amr2mp3=Integer.parseInt(props.getProperty("amr2mp3","1"));
			
			
			if (props.contains("amr2mp3")) {
				amr2mp3 = 0 == Integer.parseInt(props.getProperty("amr2mp3")) ? false : true;
			} else {
				amr2mp3 = false;
			}

			imageFilter = props.getProperty("imageFilter");
			videoFilter = props.getProperty("videoFilter");
			audioFilter = props.getProperty("audioFilter");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	

	public static FileType getFileType(String formatName) {
		if(StringUtils.isEmpty(formatName))
			return null;
		FileType fileType = FileType.Other;
		if (images.contains(formatName.toLowerCase()))
			fileType = FileType.Image;
		else if (audios.contains(formatName.toLowerCase()))
			fileType = FileType.Audio;
		else if (videos.contains(formatName.toLowerCase()))
			fileType = FileType.Video;

		return fileType;
	}
	
	public static List<String> getListBySplit(String str) {
		List<String> list = new ArrayList<String>();
		if (str == null || str.trim().equalsIgnoreCase(""))
			return list;
		String[] strs = str.split("\\|");
		for (String temp : strs) {
			if (temp != null && !temp.trim().equalsIgnoreCase("")) {
				list.add(temp.trim());
			}
		}
		return list;
	}

	public static String getFormatName(String fileName) {
		int index = fileName.lastIndexOf('.');

//		return -1 == index ? "jpg" : fileName.substring(index + 1);
		return -1 == index ? null : fileName.substring(index + 1);
	}

	public static String getName(String oFileName) {
		int endIndex = oFileName.indexOf('.');

		return -1 == endIndex ? oFileName : oFileName.substring(0, endIndex);
//		return -1 == endIndex ? null : oFileName.substring(0, endIndex);
	}

	public static File[] getUploadPath(FileType fileType) {
		String baseName = fileType.getBaseName();
		String subName = DateUtils.getYMDString();
		File[] uploadPath = new File[2];

		if (FileType.Image == fileType) {
			File oPath = new File(String.format(getSystemConfig().getoTemp(), baseName, subName));
			File tPath = new File(String.format(getSystemConfig().gettTemp(), baseName, subName));

			if (!oPath.exists())
				oPath.mkdir();
			if (!tPath.exists())
				tPath.mkdir();
			
			 oPath = new File(oPath,"o");
			 tPath = new File(tPath,"t");
			 if (!oPath.exists())
					oPath.mkdir();
			 if (!tPath.exists())
					tPath.mkdir();
			uploadPath = new File[] { oPath, tPath };
		}else if(FileType.Music == fileType){
			File nPath = new File(String.format(getSystemConfig().getnTemp(),baseName,baseName));
			if (!nPath.exists())
				nPath.mkdir();

			uploadPath = new File[] { nPath, null };
		}else if(FileType.MusicPhoto == fileType){
			File nPath = new File(String.format(getSystemConfig().getnTemp(),baseName,"mPhoto"));
			if (!nPath.exists())
				nPath.mkdir();

			uploadPath = new File[] { nPath, null };
		}else {
			File nPath = new File(String.format(getSystemConfig().getnTemp(), baseName, subName));

			if (!nPath.exists())
				nPath.mkdir();

			uploadPath = new File[] { nPath, null };
		}

		return uploadPath;
	}

	public static File[] getUploadPath(long userId, FileType fileType) {
		File[] uploadPath = new File[2];

		// /data/www/resources/u/0/
		File uPath = new File(String.format(getSystemConfig().getnTemp(), "u", String.valueOf(userId % 10000)));
		if (!uPath.exists())
			uPath.mkdir();
		// /data/www/resources/u/0/100000
		uPath = new File(uPath, String.valueOf(userId));
		if (!uPath.exists())
			uPath.mkdir();

		// /data/www/resources/u/0/100000/201412
		File nPath = new File(uPath, DateUtils.getYMString());
		if (!nPath.exists())
			nPath.mkdir();

		// /data/www/resources/u/0/100000/201412/o
		// /data/www/resources/u/0/100000/201412/t
		if (FileType.Image == fileType) {
			File oPath = new File(nPath, "o");
			File tPath = new File(nPath, "t");

			if (!oPath.exists())
				oPath.mkdir();
			if (!tPath.exists())
				tPath.mkdir();

			uploadPath = new File[] { oPath, tPath };
		} else {
			uploadPath = new File[] { nPath, null };
		}

		return uploadPath;
	}

	public static File[] getUploadPath(String baseName, long userId) {
		String subName = String.valueOf(userId % 10000);

		File oPath = new File(String.format(getSystemConfig().getoTemp(), baseName, subName));
		File tPath = new File(String.format(getSystemConfig().gettTemp(), baseName, subName));

		if (!oPath.exists())
			oPath.mkdir();
		if (!tPath.exists())
			tPath.mkdir();

		return new File[] { oPath, tPath };
	}
	public static File[] getAvatarPath(long userId) {
		String subName = String.valueOf(userId % 10000);

		File oPath = new File(getBasePath()+"/avatar/o/"+subName+"/");
		File tPath = new File(getBasePath()+"/avatar/t/"+subName+"/");

		if (!oPath.exists())
			oPath.mkdir();
		if (!tPath.exists())
			tPath.mkdir();

		return new File[] { oPath, tPath };
	}
	
	// 群组头型默认地址
	public static File[] getGroupAvatarPath(String jid) {
		int jidHashCode = jid.hashCode();
		String oneLevelName = String.valueOf(Math.abs(jidHashCode%10000));
		String twoLevelName = String.valueOf(Math.abs(jidHashCode%20000));
		File oPath = new File(getBasePath()+"/avatar/o/"+oneLevelName+"/"+twoLevelName);
		File tPath = new File(getBasePath()+"/avatar/t/"+oneLevelName+"/"+twoLevelName);
		if (!oPath.exists())
			oPath.mkdirs();
		if (!tPath.exists())
			tPath.mkdirs();

		return new File[] { oPath, tPath };
	}
	public static String getBasePath() {
		return getSystemConfig().getBasePath();
	}
	public static String getUrl(String path) {
		if(1==getSystemConfig().getIsBackDomain())
			return Joiner.on("").join(getSystemConfig().getDomain(),path.substring(getSystemConfig().getBeginIndex())).replace('\\', '/');
		else
			return Joiner.on("").join("", path.substring(getSystemConfig().getBeginIndex())).replace('\\', '/');
	}
	public static String getUrl(File file) {
		if(1==getSystemConfig().getIsBackDomain())
			return Joiner.on("").join(getSystemConfig().getDomain(), file.getPath().substring(getSystemConfig().getBeginIndex())).replace('\\', '/');
		else
			return Joiner.on("").join("", file.getPath().substring(getSystemConfig().getBeginIndex())).replace('\\', '/');
	}
	public static String getFastDFSUrl(String path) {
		if(1==getSystemConfig().getIsBackDomain())
			return Joiner.on("").join(getSystemConfig().getFastdfsDomain()+"/", path).replace('\\', '/');
		else
			return Joiner.on("").join("", path).replace('\\', '/');
	}

	public static boolean isWindows() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.startsWith("windows");
	}
}
