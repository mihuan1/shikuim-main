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

@WebServlet("/upload/UploadifyServlet")
public class UploadifyServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	public UploadifyServlet() {
		super();
	}

	@Override
	protected JMessage hander(HttpServletRequest request, HttpServletResponse response) {
		DiskFileItemFactory factory = new DiskFileItemFactory(1024 * 1024, new File(getSystemConfig().getuTemp()));
		ServletFileUpload fileUpload = new ServletFileUpload(factory);
		
		long userId=0;
		JMessage jMessage=null;
		double validTime=-1;
		FileItem item=null;
		try {
			List<FileItem> multipart = (List<FileItem>) fileUpload.parseRequest(request);
			for (FileItem fileitem : multipart){
				if(fileitem.isFormField()) {
					if ("validTime".equals(fileitem.getFieldName())) {
						if(null!=item.getString() && !"null".equals(item.getString()) ) {
							validTime=Double.valueOf(item.getString());
						}
					}
				}else {
					if (fileitem.getSize()>0)
						item=fileitem;
				}
			}
			if (null == item) {
				return new JMessage(1010101, "缺少上传文件");
			}
			
			if(1==getSystemConfig().getIsOpenfastDFS())
				jMessage=fastDFSHander(item,validTime);
			else
				jMessage=defHander(item, userId,validTime);
			
			return jMessage;
		} catch (Exception e) {
			e.printStackTrace();
			return errorMessage(e);
		}
			
	}
	
	protected JMessage fastDFSHander(FileItem item,double validTime){
		try {
			
			String path=uploadToFastDFS(item,validTime);
			JMessage jMessage = new JMessage();
			jMessage.put("path",path);
			jMessage.put("url", path);
			return jMessage;
		} catch (Exception e) {
			e.printStackTrace();
			return errorMessage(e);
		}
	}
	
	protected JMessage defHander(FileItem item,long userId,double validTime){
		try {
			String oFileName = item.getName();
			String formatName = ConfigUtils.getFormatName(oFileName);
			String fileName = Joiner.on("").join(UUID.randomUUID().toString().replace("-", ""), ".", formatName);
			FileType fileType = ConfigUtils.getFileType(formatName);
			File[] uploadPath = ConfigUtils.getUploadPath(fileType);
			File oFile = new File(uploadPath[0], fileName);
			
			FileUtils.transfer(item.getInputStream(), oFile);

			JMessage jMessage = new JMessage();
			jMessage.put("path", oFile.getPath());
			String url=getUrl(oFile);
			ResourcesDBUtils.saveFileUrl(1, url, validTime);
			jMessage.put("url", url);
			log("UploadifyServlet uploadEd "+url);
			return jMessage;
		} catch (Exception e) {
			e.printStackTrace();
			return errorMessage(e);
		}
	}

}
