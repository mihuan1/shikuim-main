package com.shiku.servlet;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.base.Joiner;
import com.shiku.commons.utils.ConfigUtils;
import com.shiku.commons.utils.FileUtils;
import com.shiku.commons.utils.ResourcesDBUtils;
import com.shiku.commons.vo.FileType;
import com.shiku.commons.vo.JMessage;

@WebServlet("/upload/UploadVoiceServlet")
public class UploadVoiceServlet extends BaseServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected JMessage hander(HttpServletRequest request, HttpServletResponse response) {
		DiskFileItemFactory factory = new DiskFileItemFactory(1024 * 1024, new File(getSystemConfig().getuTemp()));
		ServletFileUpload fileUpload = new ServletFileUpload(factory);
		List<FileItem> multipart = null;

		JMessage jMessage=null;
		long userId = 0;
		FileItem item = null;
		double validTime=0.0;

		try {
			multipart = (List<FileItem>) fileUpload.parseRequest(request);
			if (null == multipart) 
				return new JMessage(1020101, "表单解析失败");
			for (FileItem fileitem : multipart){
				if(fileitem.isFormField()) {
					if ("userId".equals(fileitem.getFieldName())) {
						userId = Long.parseLong(fileitem.getString());
					}else if ("validTime".equals(fileitem.getFieldName())) {
						if(null!=item.getString() && !"null".equals(item.getString()) ) {
							validTime=Double.valueOf(item.getString());
						}
					}
				}else {
					if (fileitem.getSize()>0)
						item=fileitem;
				}
			}
			
				
		} catch (Exception e) {
			e.printStackTrace();
			return errorMessage(e);
		}

		
		 if (null == item) {
			jMessage = new JMessage(1010101, "缺少上传文件");
		} else if (0 == userId) {
			jMessage = new JMessage(1010101, "缺少请求参数");
		} 
		 if(null!=jMessage)
			 return jMessage;
		 
		 	if(1==getSystemConfig().getIsOpenfastDFS())
				jMessage=fastDFSHander(item,validTime);
			else
				jMessage=defHander(item, userId,validTime);
		 	
			return jMessage;
	}
	
	@Override
	protected JMessage fastDFSHander(FileItem item,double validTime) {
		JMessage jMessage=null;
		
		String formatName ="wav"; //SystemConfig.getFormatName(item.getName());
		String path=null;

		try {
			item.setFieldName(System.currentTimeMillis()+".wav");
			log("item name > "+ item.getName());
			path=uploadToFastDFS(item,validTime);
			jMessage = new JMessage();
			jMessage.put("path", path);
			jMessage.put("url", path);
			
			return jMessage;
		} catch (Exception e) {
			e.printStackTrace();
			return jMessage;
		}
	}
	
	protected JMessage defHander(FileItem item, long userId,double validTime) {
		JMessage jMessage=null;
		
		File[] uploadPath = ConfigUtils.getUploadPath(userId, FileType.Audio);
		String formatName ="wav"; //SystemConfig.getFormatName(item.getName());
		String fileName = Joiner.on("").join(UUID.randomUUID().toString().replace("-", ""), ".", formatName);
		File oFile = new File(uploadPath[0], fileName);

		try {
			FileUtils.transfer(item.getInputStream(), oFile);
			String oUrl= ConfigUtils.getUrl(oFile);
			log("UploadAvatarServlet uploadEd "+oUrl);
			jMessage = new JMessage();
			jMessage.put("path", oFile.getPath());
			jMessage.put("url", oUrl);
			ResourcesDBUtils.saveFileUrl(1, oUrl, validTime);
			
			return jMessage;
		} catch (Exception e) {
			e.printStackTrace();
			return jMessage;
		}
	}
	
	
	
}
