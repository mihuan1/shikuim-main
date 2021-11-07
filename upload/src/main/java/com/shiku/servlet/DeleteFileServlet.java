package com.shiku.servlet;

import java.io.File;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.shiku.commons.utils.ConfigUtils;
import com.shiku.commons.utils.FastDFSUtils;
import com.shiku.commons.utils.FileUtils;
import com.shiku.commons.vo.FileType;
import com.shiku.commons.vo.JMessage;


@WebServlet("/upload/deleteFileServlet")
public class DeleteFileServlet extends BaseServlet  {
	
	private static final long serialVersionUID = 1L;

	public DeleteFileServlet() {
		super();
	}

	@Override
	protected JMessage hander(HttpServletRequest request, HttpServletResponse response) {
		long start=System.currentTimeMillis();	
		response.addHeader("Access-Control-Allow-Origin", "*");
		JMessage jMessage=null;
		String[] paths=request.getParameterValues("paths");
		
		if(1==getSystemConfig().getIsOpenfastDFS())
			jMessage=fastDFSHander(paths);
		else
			jMessage=defHander(paths);
		
			jMessage.put("total", paths.length);
			jMessage.put("time", System.currentTimeMillis()-start);
		return jMessage;
	}
	
	private JMessage fastDFSHander(String[] paths){
		JMessage jMessage=null;
		int totalCount = paths.length;
		int successCount = 0;
		String childPath=null;
		boolean flag=false;
		for (String str : paths) {
			
			childPath=FileUtils.getAbsolutePath(str);
			flag=FastDFSUtils.deleteFile(childPath);
			if(flag){
				successCount++;
			}
		}
		jMessage = new JMessage(1, null, "");
		jMessage.put("success", successCount);
		jMessage.put("failure", totalCount - successCount);
		return jMessage;
	}
	
	private JMessage defHander(String[] paths){
			JMessage jMessage=null;
			int totalCount = paths.length;
			int successCount = 0;
			//String path=null;
			String childPath=null;
				for (int i = 0; i < paths.length; i++) {
					//System.out.println("解析前： "+paths[i]);
					//URL 中含有域名
					if(paths[i].contains("http:")&&paths[i].contains("//")){
						String tempPath=paths[i].substring(paths[i].indexOf("//")+2);
						childPath=tempPath.substring(tempPath.indexOf("/"));
					}else {
						childPath=paths[i];
					}
					childPath=childPath.replace("/resources", "");
					//System.out.println("解析后： "+childPath);
					if(FileUtils.deleteFile(childPath))
						successCount++;
				
				}
				jMessage = new JMessage(1, null, "");
				jMessage.put("success", successCount);
				jMessage.put("failure", totalCount - successCount);
			
			return jMessage;
	}
	
	
}
