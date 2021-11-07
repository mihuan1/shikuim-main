package com.shiku.servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;

import com.google.common.base.Joiner;
import com.shiku.commons.utils.ConfigUtils;
import com.shiku.commons.utils.FastDFSUtils;
import com.shiku.commons.utils.FileUtils;
import com.shiku.commons.utils.ResourcesDBUtils;
import com.shiku.commons.vo.FastDFSFile;
import com.shiku.commons.vo.FileType;
import com.shiku.commons.vo.JMessage;
@WebServlet("/upload/copyFileServlet")
public class CopyFileServlet extends BaseServlet{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CopyFileServlet() {
		super();
	}
	
	@Override
	protected JMessage hander(HttpServletRequest request, HttpServletResponse response) {
		long start=System.currentTimeMillis();	
		response.addHeader("Access-Control-Allow-Origin", "*");
		JMessage jMessage=null;
		String paths=request.getParameter("paths");
		
		log("copyFile hander old url   "+paths);
		String childPath=null;
		double validTime=0.0;
		validTime=Double.valueOf(request.getParameter("validTime"));
		try {
				childPath=FileUtils.getAbsolutePath(paths);
				if(1==getSystemConfig().getIsOpenfastDFS()&&FastDFSUtils.getGroupFormFilePath(childPath).startsWith("group")){
					jMessage=fastDFSHander(validTime,paths);
				}else{
					jMessage=defHander(validTime,paths);
				}
	
			return jMessage;
		} catch (Exception e) {
			e.printStackTrace();
			return errorMessage(e);
		}
	}
	
	private JMessage fastDFSHander(double validTime,String... paths) throws IOException, Exception{
		JMessage jMessage=null;
		int totalCount = paths.length;
		int successCount = 0;
		String childPath=null;
		String path=null;
		String url=null;
		for (String str : paths) {
			childPath=FileUtils.getAbsolutePath(str);
			String groupName=FastDFSUtils.getGroupFormFilePath(childPath);
			String remoteFileName=FastDFSUtils.getFileNameFormFilePath(childPath);
			File f=new File(remoteFileName);
			ResponseEntity<byte[]> content =FastDFSUtils.downloadFile(groupName, remoteFileName, "");
			FastDFSFile file=new FastDFSFile(content.getBody(),remoteFileName,f.length());
			path=FastDFSUtils.uploadFile(file);
			url=ConfigUtils.getFastDFSUrl(path);
			ResourcesDBUtils.saveFileUrl(2, url, validTime);
			successCount++;
		}
	
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("path", path);
		data.put("url", url);
		data.put("failure", totalCount-successCount);
		data.put("success", successCount);
		jMessage = new JMessage(1, null,data);
		log("fastDfs复制路径    "+url);
		return jMessage;
	}
	
	
	
	protected JMessage defHander(double validTime,String... paths) throws IOException, Exception{
		JMessage jMessage=null;
		int totalCount = paths.length;
		int successCount = 0;
		String path=null;
		String childPath=null;
		String url=null;
			for (int i = 0; i < paths.length; i++) {
				//System.out.println("解析前： "+paths[i]);
				//URL 中含有域名
				if(paths[i].startsWith("http://")||paths[i].startsWith("https://")){
					String tempPath=paths[i].substring(paths[i].indexOf("//")+2);
					childPath=tempPath.substring(tempPath.indexOf("/"));
				}else {
					childPath=paths[i];
				}
				childPath=childPath.replace("/resources", "");
				//childPath=childPath.replace("es", "");
				//System.out.println("解析后： "+childPath);
				path=copyFile(childPath);
				url=ConfigUtils.getUrl(path);
				ResourcesDBUtils.saveFileUrl(1, url, validTime);
			}
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("path", path);
			data.put("url", url);
			data.put("failure", totalCount-successCount);
			data.put("success", successCount);
			jMessage = new JMessage(1, null,data);
			
			log("复制后的文件路径   "+url);
		return jMessage;
	}
	
	/**
	* @Description: TODO(复制文件)
	* @param @param path 文件路径
	* @param @param isAvatar  是否为头像
	* @param @return    参数
	 * @throws Exception 
	 * @throws IOException 
	 */
	private String copyFile(String childPath) throws IOException, Exception{
		String result=null;
		String formatName = ConfigUtils.getFormatName(childPath);
		
		//.png
		FileType fileType=ConfigUtils.getFileType(formatName);
		String path=ConfigUtils.getBasePath()+childPath;
		//如果文件是图片
		
		result=copyImage(path,0);
		
		return result;
	}
	
	public String copyImage(String path,int isAvatar) throws IOException, Exception{
//		path=path.replace("\\", "/");// windows上文件路径
		File oldFile=new File(path);
		String result = null;
		String fileName=null;
		String formatName=null;
		String prefixPath=null;
		int fileNameIndex=0;
		File oFile=null;
		fileNameIndex=path.lastIndexOf("/")+1;
		formatName= ConfigUtils.getFormatName(path.substring(fileNameIndex));
		// 过滤无后缀文件
		if(null == formatName)
			fileName = path.substring(fileNameIndex);
		else
			fileName=Joiner.on("").join(UUID.randomUUID().toString().replace("-", ""), ".", formatName);
		if(1!=isAvatar){
			prefixPath=path.substring(0,fileNameIndex-1);
			oFile=new File(prefixPath+"/"+fileName);
			//tFile=new File(prefixPath+"/"+fileName);
		}else{
			prefixPath=path.substring(0,fileNameIndex-5);
			oFile=new File(path);
		}

		FileUtils.copyfile(oFile,oldFile);
		result=oFile.getPath();
		return result;
	}
}
