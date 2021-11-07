package com.shiku.servlet;

import java.io.File;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;
import com.shiku.commons.Amr2mp3;
import com.shiku.commons.utils.ConfigUtils;
import com.shiku.commons.utils.ResourcesDBUtils;
import com.shiku.commons.vo.JMessage;
import com.shiku.commons.vo.UploadItem;


@WebServlet("/upload/amrToMp3")
public class AmrToMp3Servlet extends BaseServlet  {
	
	private static final long serialVersionUID = 1L;

	public AmrToMp3Servlet() {
		super();
	}

	@Override
	protected JMessage hander(HttpServletRequest request, HttpServletResponse response) {
		long start=System.currentTimeMillis();	
		JMessage jMessage=null;
		try {
			String[] paths=request.getParameterValues("paths");
			jMessage=defHander(paths);
			jMessage.put("total", paths.length);
			jMessage.put("time", System.currentTimeMillis()-start);
			return jMessage;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
			
			
	}

	
	
	private JMessage defHander(String[] paths){
		JMessage jMessage=null;
		List<UploadItem> files = Lists.newArrayList();
		int totalCount = paths.length;
		int successCount = 0;
		String path=null;
		String childPath=null;
		UploadItem uploadItem=null;
			for (int i = 0; i < paths.length; i++) {
				uploadItem=null;
				//System.out.println("解析前： "+paths[i]);
				//URL 中含有域名
				if(paths[i].contains("http:")&&paths[i].contains("//")){
					String tempPath=paths[i].substring(paths[i].indexOf("//")+2);
					childPath=tempPath.substring(tempPath.indexOf("/"));
				}else {
					childPath=paths[i];
				}
				//System.out.println("解析后： "+childPath);
				childPath=childPath.replace("/resources", "");
				path=ConfigUtils.getBasePath()+childPath;
				uploadItem=amrToMp3File(path);
				if(null!=uploadItem){
					files.add(uploadItem);
					successCount++;
				}
			
			}
			jMessage = new JMessage(1, null,files);
			jMessage.put("success", successCount);
			jMessage.put("failure", totalCount - successCount);
			
			return jMessage;
	}

	
	public UploadItem amrToMp3File(String path){
		UploadItem uploadItem=null;
		//String formatName = SystemConfig.getFormatName(path);
		//FileType fileType=SystemConfig.getFileType(formatName);
		File file=new File(path);
		String mp3path=getSystemConfig().getuTemp()+"mp3/";
		//如果文件是 amr
		if(path.indexOf(".amr") != -1){
			File __source = new File(file.getPath());
			mp3path+=file.getName();
			File __target = new File(mp3path.replaceAll(".amr", ".mp3"));
			if(!__target.exists())
				Amr2mp3.changeToMp3(__source.getPath(), __target.getPath());
			String url = ConfigUtils.getUrl(__target);
			ResourcesDBUtils.saveFileUrl(1, url, -1);
			uploadItem = new UploadItem(__target.getName(), url, (byte) 1, null);
			return uploadItem;
		}else{//如果文件不是  amr
			return uploadItem;
		}
		
	}
	
	public boolean deleteImage(String path){
		boolean result=false;
		//	1fc95d99277a47f5b76d9315b8be0897.jpg
		String fileName=null;
		//		d:/data/www/resources/u/6/3000000006/201608/
		String prefixPath=null;
		int fileNameIndex=0;
		//d:/data/www/resources/u/6/3000000006/201608/o
		File oFile=null;
		//	//d:/data/www/resources/u/6/3000000006/201608/t
		File tFile=null;
		fileNameIndex=path.lastIndexOf("/")+1;
		fileName=path.substring(fileNameIndex);
		prefixPath=path.substring(0,fileNameIndex-2);
		oFile=new File(prefixPath+"o/"+fileName);
		tFile=new File(prefixPath+"t/"+fileName);
		
		if(oFile.exists()){
			result=oFile.delete();
			log("删除=====>"+oFile.getAbsolutePath()+"====>"+result);
			
		}
		if(tFile.exists()){
			result=tFile.delete();
			log("删除=====>"+tFile.getAbsolutePath()+"====>"+result);
		}
		return result;
	}



	
}
