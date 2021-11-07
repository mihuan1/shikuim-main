package com.shiku.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.shiku.commons.SystemConfig;
import com.shiku.commons.utils.ConfigUtils;
import com.shiku.commons.utils.FastDFSUtils;
import com.shiku.commons.utils.ResourcesDBUtils;
import com.shiku.commons.vo.FastDFSFile;
import com.shiku.commons.vo.FileType;
import com.shiku.commons.vo.JMessage;

/**
* @Description: TODO(用一句话描述该文件做什么)
* @author lidaye
* @date 2018年5月22日 
*/
public abstract class BaseServlet extends HttpServlet {
	
	
	public SystemConfig getSystemConfig(){
		
		return ConfigUtils.getSystemConfig();
	}
	
	public String getUrl(File file){
		return ConfigUtils.getUrl(file);
	}
	
	public String getFormatName(String fileName){
		return ConfigUtils.getFormatName(fileName);
	}
	
	public FileType  getFileType(String formatName){
		return ConfigUtils.getFileType(formatName);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(req, resp);
	}
	

	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log("=== > hander "+getClass().toString());
		
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		
		JMessage message=null;
		try {
			 message= hander(request, response);
			 if(null==message)
				 message=new JMessage(-1, "服务器异常！");
		} catch (Exception e) {
			message=new JMessage(-1, e.getMessage());
		}
		
		
		 doWriter(response, message);
	}
	
	

	protected abstract  JMessage  hander(HttpServletRequest request, HttpServletResponse response);
	
	protected JMessage errorMessage(Exception e) {
		return new JMessage(-1, "服务器异常");
	}
	
	protected void doWriter(HttpServletResponse response,JMessage message) {
		try {
			String s = JSON.toJSONString(message);
			PrintWriter out = response.getWriter();
			out.write(s);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	* @Description: TODO(上传文件到  fastDFS)
	* @param @param item
	* @param @return   文件地址url
	 */
	protected String uploadToFastDFS(FileItem item,double validTime) {
		FastDFSFile file=new FastDFSFile();
		String fileName=StringUtils.isEmpty(item.getName())?item.getFieldName():item.getName();
		file.setName(fileName);
		log("===> uploadToFastDFS upload fileName > "+fileName+" validTime > "+validTime);
		file.setSize(item.getSize());
		file.setContent(item.get());
		String path=FastDFSUtils.uploadFile(file);
		
		ResourcesDBUtils.saveFileUrl(2,path,validTime);
		
		path=ConfigUtils.getFastDFSUrl(path);
		
		log("uploadToFastDFS uploadEd "+path);
		return path;
	}
	
	protected String copyFile(File file,double validTime) throws Exception{
		FastDFSFile fstfile=new FastDFSFile();
		String fileName=file.getName();
		fstfile.setName(fileName);
		log("===> uploadToFastDFS upload fileName > "+fileName+" validTime > "+validTime);
		fstfile.setSize(file.length());
		
		FileInputStream inputStream=new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
		byte arr[]=new byte[1024];
		int n;
		 while ((n = inputStream.read(arr)) != -1) {
			 bos.write(arr, 0, n);
		 }
		 inputStream.close();
		 byte[] data = bos.toByteArray();
		 bos.close();
		fstfile.setContent(data);
		
		String path=FastDFSUtils.uploadFile(fstfile);
		ResourcesDBUtils.saveFileUrl(2,path,validTime);
		path=ConfigUtils.getFastDFSUrl(path);
		log("复制文件   uploadToFastDFS uploadEd "+path);
		return path;
		
	}

	/**
	* @Description: TODO(fastdfs上传   多个文件)
	* @param @param multipart
	* @param @param userId
	* @param @return    参数
	 */
	protected JMessage fastDFSHander(List<FileItem> multipart){
		return null;
	}
	/**
	* @Description: TODO(fastdfs上传   一个文件)
	* @param @param multipart
	* @param @param userId
	* @param @return    参数
	 */
	protected JMessage fastDFSHander(FileItem item,double validTime){
		return null;
	}
	
	/**
	* @Description: TODO(系统默认的上传  多个文件)
	* @param @param multipart
	* @param @param userId
	* @param @return    参数
	 */
	protected JMessage defHander(List<FileItem> multipart,long userId){
		return null;
	}
	/**
	* @Description: TODO(系统默认的上传   一个文件)
	* @param @param multipart
	* @param @param userId
	* @param @return    参数
	 */
	protected JMessage defHander(FileItem item,long userId){
		return null;
	}
}

