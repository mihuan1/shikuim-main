package com.shiku.servlet;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.base.Joiner;
import com.shiku.commons.Amr2mp3;
import com.shiku.commons.utils.ConfigUtils;
import com.shiku.commons.utils.FileUtils;
import com.shiku.commons.vo.FileType;
import com.shiku.commons.vo.JMessage;
import com.shiku.commons.vo.UploadItem;
@WebServlet("/upload/UploadMusicServlet")
public class UploadMusicServlet extends BaseServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected JMessage hander(HttpServletRequest request, HttpServletResponse response) {
		DiskFileItemFactory factory = new DiskFileItemFactory(1000 * 1024 * 1024, new File(getSystemConfig().getuTemp()));
		ServletFileUpload fileUpload = new ServletFileUpload(factory);
		List<FileItem> multipart = null;
		JMessage jMessage=null;
		int validTime=-1;
		int userId=0;
		try {
			multipart = (List<FileItem>) fileUpload.parseRequest(request);
			FileItem item = multipart.get(0).getSize() > 0 ? multipart.get(0) : null;
			for (FileItem fileitem : multipart){
				if ("validTime".equals(item.getFieldName())) {
					validTime=Integer.valueOf(fileitem.getString());
				}
			}
			
			if(1==getSystemConfig().getIsOpenfastDFS())
				jMessage=fastDFSHander(item,validTime);
			else
				jMessage=defHander(multipart, userId);
			
			return jMessage;
		} catch (FileUploadException e) {
			
			e.printStackTrace();
			return errorMessage(e);
		}
			
	}
	
	@Override
	protected JMessage defHander(List<FileItem> multipart, long userId) {
		JMessage jMessage=null;
		String oUrl=null;
		int successCount = 0;
		for (FileItem item : multipart) {
			UploadItem uploadItem;
			if (item.isFormField()||item.getSize()<1) 
				continue;
			String oFileName = item.getName();
			String formatName = ConfigUtils.getFormatName(oFileName);
			String fileName = 32 == ConfigUtils.getName(oFileName).length() ? oFileName
					: Joiner.on("").join(UUID.randomUUID().toString().replace("-", ""), ".", formatName);
			FileType fileType = getFileType(formatName);
			if(fileType==FileType.Image){
				fileType=FileType.MusicPhoto;
				oFileName=fileName;
			}else{
				fileType=FileType.Music;
			}
			File[] uploadPath = ConfigUtils.getUploadPath(fileType);
			File oFile = new File(uploadPath[0], oFileName);
			try {
				FileUtils.transfer(item.getInputStream(), oFile);
				if ((oFileName.indexOf(".amr") != -1)&&1==getSystemConfig().getAmr2mp3()) {
					File __source = new File(oFile.getPath());
					File __target = new File(oFile.getPath().replaceAll(".amr", ".mp3"));
					Amr2mp3.changeToMp3(__source.getPath(), __target.getPath());
					successCount++;
					oUrl= getUrl(__target);
				}else{
					successCount++;
					oUrl= getUrl(oFile);
				}
				log("UploadServlet uploadEd "+oUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}
			jMessage = new JMessage(1, null, oUrl);
			jMessage.put("success", successCount);
			
		}
		return jMessage;
	}
	
}
