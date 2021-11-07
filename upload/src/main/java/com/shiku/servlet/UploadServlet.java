package com.shiku.servlet;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.util.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.shiku.commons.Amr2mp3;
import com.shiku.commons.utils.ConfigUtils;
import com.shiku.commons.utils.FileUtils;
import com.shiku.commons.utils.ResourcesDBUtils;
import com.shiku.commons.vo.FileType;
import com.shiku.commons.vo.JMessage;
import com.shiku.commons.vo.UploadItem;

@WebServlet("/upload/UploadServlet")
public class UploadServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	public UploadServlet() {
		super();
	}

	@Override
	protected JMessage hander(HttpServletRequest request, HttpServletResponse response) {
		long start=System.currentTimeMillis();	
		DiskFileItemFactory factory = new DiskFileItemFactory(1000 * 1024 * 1024, new File(getSystemConfig().getuTemp()));
		ServletFileUpload fileUpload = new ServletFileUpload(factory);
		List<FileItem> multipart = null;

		JMessage jMessage=null;
		int totalCount = 0;
		// int uploadFlag = 0;
		long userId = 0;
		double validTime=0;
		try {
			multipart = (List<FileItem>) fileUpload.parseRequest(request);
			for (FileItem item : multipart) {
				if (item.isFormField()) {
				
					if ("validTime".equals(item.getFieldName())) {
						
						try {
							validTime=Double.valueOf(item.getString());
						}catch(NumberFormatException e) {
							validTime=new Double(-1);
						}
					}
					
					if ("userId".equals(item.getFieldName())) {
						userId = Long.parseLong(item.getString());
					}
					
				} else {
					if (item.getSize() > 0) {
						totalCount++;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == multipart) {
			jMessage = new JMessage(1020101, "表单解析失败");
		}
		// else if (0 == uploadFlag) {
		// jMessage = new JMessage(1010101, "缺少上传标记");
		// }
		else if (0 == totalCount) {
			jMessage = new JMessage(1010101, "缺少上传文件");
		} 
		if(null!=jMessage)
			return jMessage;
			if(1==getSystemConfig().getIsOpenfastDFS()){
				jMessage=fastDFSHander(multipart,validTime);
			}
			else
				jMessage=defHander(multipart, userId,validTime);
			
			int successCount=jMessage.getIntValue("success");
			jMessage.put("total", totalCount);
			jMessage.put("failure", totalCount - successCount);
			jMessage.put("time", System.currentTimeMillis()-start);
	
		
		return jMessage;
	}
	
	protected JMessage fastDFSHander(List<FileItem> multipart,double validTime){
		JMessage message=null;
		int successCount=0;
		
		List<UploadItem> images = Lists.newArrayList();
		List<UploadItem> audios = Lists.newArrayList();
		List<UploadItem> videos = Lists.newArrayList();
		List<UploadItem> others = Lists.newArrayList();
		String oUrl=null;
		String tUrl=null;
		
		
		for (FileItem item : multipart) {
			UploadItem uploadItem;
			
			if (item.isFormField()||item.getSize()<1) 
				continue;
				String fileName = item.getName();
				String formatName = getFormatName(fileName);
				
				FileType fileType =getFileType(formatName);
				String path=uploadToFastDFS(item,validTime);
				if(StringUtils.isEmpty(path))
					continue;
				 oUrl= path;
				 tUrl=path;
				successCount++;
				if (FileType.Image == fileType) {//图片
					try {
						uploadItem = new UploadItem(fileName,oUrl,
								tUrl, (byte) 1, null);
					} catch (Exception e) {
						e.printStackTrace();
						uploadItem = new UploadItem(fileName, null, (byte) 0, e.getMessage());
					}
					images.add(uploadItem);
				} else {//其他
					try {
						uploadItem = new UploadItem(fileName,oUrl,
								tUrl, (byte) 1, null);
						if ((fileName.indexOf(".amr") != -1)&&1==getSystemConfig().getAmr2mp3()) {
								/*File __source = new File(oFile.getPath());
								File __target = new File(oFile.getPath().replaceAll(".amr", ".mp3"));
								Amr2mp3.changeToMp3(__source.getPath(), __target.getPath());
								successCount++;
								oUrl= SystemConfig.getUrl(__target);
								uploadItem = new UploadItem(__target.getName(),oUrl, (byte) 1, null);*/
							}else{
								/*successCount++;
								oUrl= SystemConfig.getUrl(oFile);
								uploadItem = new UploadItem(oFileName, oUrl, (byte) 1, null);*/
							}
					} catch (Exception e) {
						e.printStackTrace();
						uploadItem = new UploadItem(fileName, null, (byte) 0, e.getMessage());
					}
					if (FileType.Audio == fileType)
						audios.add(uploadItem);
					else if (FileType.Video == fileType)
						videos.add(uploadItem);
					else
						others.add(uploadItem);
				}
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("images", images);
		data.put("audios", audios);
		data.put("videos", videos);
		data.put("others", others);

		
		message = new JMessage(1, null, data);
		
		message.put("success", successCount);
		
		return message;
	}
	
	protected JMessage defHander(List<FileItem> multipart,long userId,double validTime){
		JMessage jMessage=null;
		int successCount = 0;
		String oUrl=null;
		String tUrl=null;
		List<UploadItem> images = Lists.newArrayList();
		List<UploadItem> audios = Lists.newArrayList();
		List<UploadItem> videos = Lists.newArrayList();
		List<UploadItem> others = Lists.newArrayList();

		for (FileItem item : multipart) {
			UploadItem uploadItem;
			if (item.isFormField()||item.getSize()<1) 
				continue;
				String oFileName = item.getName();
				String formatName = ConfigUtils.getFormatName(oFileName);
				String newFileName = ConfigUtils.getName(oFileName);
				String fileName = null;
				if(!StringUtils.isEmpty(newFileName) && !newFileName.equals(oFileName)){
					fileName = 32 == newFileName.length() ? oFileName
							: Joiner.on("").join(UUID.randomUUID().toString().replace("-", ""), ".", formatName);
				}else{
					fileName = oFileName;
				}
				/*String fileName = 32 == ConfigUtils.getName(oFileName).length() ? oFileName
						: Joiner.on("").join(UUID.randomUUID().toString().replace("-", ""), ".", formatName);*/
				FileType fileType = getFileType(formatName);
				File[] uploadPath = ConfigUtils.getUploadPath(userId, fileType);
				if (FileType.Image == fileType) {//图片
					File oFile = new File(uploadPath[0], fileName);
					File tFile = new File(uploadPath[1], fileName);
					try {
						FileUtils.transfer(item.getInputStream(), oFile, tFile, formatName);

						successCount++;
						 oUrl= getUrl(oFile);
							 tUrl=getUrl(tFile);
						ResourcesDBUtils.saveFileUrl(1, oUrl, validTime);
						ResourcesDBUtils.saveFileUrl(1, tUrl, validTime);
						log("UploadServlet uploadEd "+oUrl);
						log("UploadServlet uploadEd "+tUrl);
						uploadItem = new UploadItem(oFileName,oUrl,
								tUrl, (byte) 1, null);
					} catch (Exception e) {
						e.printStackTrace();
						uploadItem = new UploadItem(oFileName, null, (byte) 0, e.getMessage());
					}
					images.add(uploadItem);
				} else {//其他
					File oFile = new File(uploadPath[0], fileName);
					try {
						FileUtils.transfer(item.getInputStream(), oFile);
						if ((fileName.indexOf(".amr") != -1)&&1==getSystemConfig().getAmr2mp3()) {
								File __source = new File(oFile.getPath());
								File __target = new File(oFile.getPath().replaceAll(".amr", ".mp3"));
								Amr2mp3.changeToMp3(__source.getPath(), __target.getPath());
								successCount++;
								oUrl= getUrl(__target);
								uploadItem = new UploadItem(__target.getName(),oUrl, (byte) 1, null);
							}else{
								successCount++;
								oUrl= getUrl(oFile);
								uploadItem = new UploadItem(oFileName, oUrl, (byte) 1, null);
							}
						ResourcesDBUtils.saveFileUrl(1, oUrl, -1);
						log("UploadServlet uploadEd "+oUrl);
					} catch (Exception e) {
						e.printStackTrace();
						uploadItem = new UploadItem(oFileName, null, (byte) 0, e.getMessage());
					}
					if (FileType.Audio == fileType)
						audios.add(uploadItem);
					else if (FileType.Video == fileType)
						videos.add(uploadItem);
					else
						others.add(uploadItem);
				}
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("images", images);
		data.put("audios", audios);
		data.put("videos", videos);
		data.put("others", others);

		
		jMessage = new JMessage(1, null, data);
		
		jMessage.put("success", successCount);
		return jMessage;
		
	}
	
	

}
