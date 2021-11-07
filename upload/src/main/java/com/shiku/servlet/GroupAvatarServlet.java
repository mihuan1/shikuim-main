package com.shiku.servlet;

import java.io.File;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.base.Joiner;
import com.shiku.commons.utils.ConfigUtils;
import com.shiku.commons.utils.FileUtils;
import com.shiku.commons.vo.JMessage;
import com.shiku.commons.vo.UploadItem;

/** @version:（1.0） 
* @ClassName	GroupAvatarServlet
* @Description: 群组自定义头像
* @date:2019年4月2日下午8:07:56  
*/ 
@WebServlet("/upload/GroupAvatarServlet")
public class GroupAvatarServlet extends BaseServlet{

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
		String jid = null;
		FileItem item = null;
		
		try {
			multipart = (List<FileItem>) fileUpload.parseRequest(request);
			for (FileItem tem : multipart) {
				if (tem.isFormField()) {
					if ("jid".equals(tem.getFieldName())) {
						jid = tem.getString();
					}
				}else {
					if (tem.getSize()>0)
						item=tem;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == multipart) {
			jMessage = new JMessage(1020101, "表单解析失败");
		} else if (null == item) {
			jMessage = new JMessage(1010101, "缺少上传文件");
		} else if (null == jid) {
			jMessage = new JMessage(1010101, "缺少请求参数");
		} 
		if(null!=jMessage)
			return jMessage;
		
		File[] uploadPath = ConfigUtils.getGroupAvatarPath(jid);// 获取地址
		String formatName =ConfigUtils.getFormatName(item.getName());
		String fileName = Joiner.on("").join(jid, ".","jpg" );
		File oFile = new File(uploadPath[0], fileName);
		File tFile = new File(uploadPath[1], fileName);

		try {
			if("png".equals(formatName))
				FileUtils.transferFromPng(item.getInputStream(), oFile, tFile, formatName);
			else
				FileUtils.transfer(item.getInputStream(), oFile, tFile, formatName);
			String oUrl= getUrl(oFile);
			String tUrl=getUrl(tFile);
			
			log("UploadAvatarServlet uploadEd "+oUrl);
			log("UploadAvatarServlet uploadEd "+tUrl);
			jMessage = new JMessage(1, null,
					new UploadItem(null, oUrl, tUrl));
		} catch (Exception e) {
			e.printStackTrace();

			jMessage = new JMessage(0, null);
		}
		return jMessage;
	}
	
}
