package com.shiku.commons.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleOp;
import com.shiku.UploadApplication;
import com.shiku.commons.vo.FileType;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import it.sauronsoftware.jave.InputFormatException;

public final class FileUtils {
	
	private static final Logger log = LoggerFactory.getLogger(UploadApplication.class);
	
	/**
	* @Description: TODO(删除文件)
	* @param @param path 文件路径
	* @param @param isAvatar  是否为头像
	* @param @return    参数
	 */
	public static boolean deleteFile(String childPath){
		boolean result=false;
		String formatName = ConfigUtils.getFormatName(childPath);
		
		//.png
		FileType fileType=ConfigUtils.getFileType(formatName);
		String path=ConfigUtils.getBasePath()+childPath;
		//如果文件是图片
		if(FileType.Image == fileType){
			return deleteImage(path, childPath.startsWith("/avatar")?1:0);
		}
		else{//如果文件不是图片
			File file=new File(path);
			if(file.exists()){
				result=file.delete();
				log.info("删除=====>"+file.getAbsolutePath()+"====>"+result);;
			}
			return result;
		}
		
	}
	
	public static boolean deleteImage(String path,int isAvatar){
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
		if(1!=isAvatar){
			prefixPath=path.substring(0,fileNameIndex-2);
			oFile=new File(prefixPath+"o/"+fileName);
			tFile=new File(prefixPath+"t/"+fileName);
		}
		else{
			prefixPath=path.substring(0,fileNameIndex-5);
			if(path.contains("/o/")){
				oFile=new File(path);
				tFile=new File(path.replace("/o/", "/t/"));
			}else{
				tFile=new File(path);
				oFile=new File(path.replace("/t/", "/o/"));
			}
		}
		
		
		if(null!=oFile&&oFile.exists()){
			result=oFile.delete();
			log.info("删除=====>"+oFile.getAbsolutePath()+"====>"+result);
			
		}
		if(null!=tFile&&tFile.exists()){
			result=tFile.delete();
			log.info("删除=====>"+tFile.getAbsolutePath()+"====>"+result);
		}
		return result;
	}
	
	/**
	* @Description: TODO(根据文件 url 获取 系统中 绝对的文件路径)
	* @param @param path  文件地址  url
	* @param @return    参数   文件的真实路径   不带 域名
	* 
	* 示例
	* 
	* http://192.168.0.139/group1/M00/00/00/wKgAi1sEBvWAcwbaAAKOO3qxyM4141.png
	* 
	* 返回  group1/M00/00/00/wKgAi1sEBvWAcwbaAAKOO3qxyM4141.png
	* 
	* /group1/M00/00/00/wKgAi1sEBvWAcwbaAAKOO3qxyM4141.png
	* 返回 
	* group1/M00/00/00/wKgAi1sEBvWAcwbaAAKOO3qxyM4141.png
	* 
	 */
	public static String getAbsolutePath(String path){
		String result=null;
		if(path.startsWith("http://")||path.startsWith("https://")){
			String tempPath=path.substring(path.indexOf("//")+2);
			result=tempPath.substring(tempPath.indexOf("/")+1);
		}else if(path.startsWith("/")){
			result=path.substring(path.indexOf("/")+1);
		}else {
			result=path;
		}
		
		return result;
	}

	public static String readAll(InputStream in) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in,
				"UTF-8"));
		StringBuffer sb = new StringBuffer();
		String ln = null;

		while (null != (ln = reader.readLine()))
			sb.append(ln);

		return sb.toString();
	}

	public static String readAll(BufferedReader reader) {
		try {
			StringBuffer sb = new StringBuffer();
			String ln = null;

			while (null != (ln = reader.readLine()))
				sb.append(ln);

			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	//传输压缩一个文件
	@SuppressWarnings("resource")
	public static void transfer(InputStream in, File oFile, File tFile,
			String formatName) throws Exception {
		/*if(!oFile.exists())
			oFile.mkdirs();
		if(!tFile.exists())
			tFile.mkdirs();*/
		in.mark(0);
		FileOutputStream out = new FileOutputStream(oFile, false);
		int length = 0;
		byte[] buf = new byte[1024];

		while ((length = in.read(buf)) != -1) {
			out.write(buf, 0, length);
		}

		in.reset();
		BufferedImage src=null;
//		try {
		 src = ImageIO.read(in);
		 if(null == src)
			 throw new Exception("文件异常，请上传正确有效格式的文件");
		/*} catch (Exception e) {
			System.out.println("FileUtils transfer ImageIO.read "+e.getMessage() );
		}*/
		

		double scaleF = 100f / (src.getWidth() > src.getHeight() ? src
				.getWidth() : src.getHeight());
		int destWidth = (int) (src.getWidth() * scaleF);
		int destHeight = (int) (src.getHeight() * scaleF);

		ResampleOp resampleOp = new ResampleOp(destWidth, destHeight);
		resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.VerySharp);
		BufferedImage dest = resampleOp.filter(src, null);

		FileOutputStream tOut = new FileOutputStream(tFile, false);
		
		
		ImageIO.write(dest, formatName, tOut);

		in.close();
		out.flush();
		out.close();
		tOut.flush();
		tOut.close();
	}
	
	//处理png 格式的图片  需要转成 jpg
	public static void transferFromPng(InputStream in, File oFile, File tFile,
			String formatName) throws Exception {
		
		/*if(!oFile.exists())
			oFile.mkdirs();
		if(!tFile.exists())
			tFile.mkdirs();*/
		in.mark(0);
		FileOutputStream out = new FileOutputStream(oFile, false);
		int length = 0;
		byte[] buf = new byte[1024];

		while ((length = in.read(buf)) != -1) {
			out.write(buf, 0, length);
		}

		in.reset();
		BufferedImage src=null;
		try {
			 src = ImageIO.read(in);
		} catch (Exception e) {
			log.info("FileUtils transfer ImageIO.read "+e.getMessage() );
		}
		try {
			 	double scaleF = 100f / (src.getWidth() > src.getHeight() ? src
						.getWidth() : src.getHeight());
				int destWidth = (int) (src.getWidth() * scaleF);
				int destHeight = (int) (src.getHeight() * scaleF);
				
				 BufferedImage to = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);  
		         Graphics2D g2d = to.createGraphics();  
		         to = g2d.getDeviceConfiguration().createCompatibleImage(destWidth, destHeight,  
		                 Transparency.TRANSLUCENT);  
		         g2d.dispose();  
		         g2d = to.createGraphics();  
		      
		         Image from = src.getScaledInstance(destWidth, destHeight, src.SCALE_AREA_AVERAGING);  
		         g2d.drawImage(from, 0, 0, null);  
		         g2d.dispose();  
		         ImageIO.write(to, formatName, tFile);
		} catch (Exception e) {
			log.info(e.getMessage());
		}finally {
			in.close();
			out.flush();
			out.close();
		}
		

		
		
	}
	//传输压缩一两个文件
	public static void transfer2(final InputStream in,final File oFile,final File tFile,
					final String formatName) throws Exception {
		new Thread(new Runnable() {
					@Override
					public void run() {
						
						try {
							in.mark(0);
							FileOutputStream out = new FileOutputStream(oFile, false);
							int length = 0;
							byte[] buf = new byte[4096];

							while ((length = in.read(buf)) != -1) {
								out.write(buf, 0, length);
							}

							in.reset();

							BufferedImage src = ImageIO.read(in);

							double scaleF = 100f / (src.getWidth() > src.getHeight() ? src
									.getWidth() : src.getHeight());
							int destWidth = (int) (src.getWidth() * scaleF);
							int destHeight = (int) (src.getHeight() * scaleF);

							ResampleOp resampleOp = new ResampleOp(destWidth, destHeight);
							resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.VerySharp);
							BufferedImage dest = resampleOp.filter(src, null);

							FileOutputStream tOut = new FileOutputStream(tFile, false);
							ImageIO.write(dest, formatName, tOut);

							in.close();
							out.flush();
							out.close();
							tOut.flush();
							tOut.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			}).start();
				
				
				
	}	
	
		//传输压缩一两个文件
		public static void transferTwo(final InputStream in, File oFile,final File tFile,
				final String formatName) throws Exception {
			
			in.mark(0);
			FileOutputStream out = new FileOutputStream(oFile, false);
			/*	int length = 0;
				byte[] buf = new byte[1024];
	
				while ((length = in.read(buf)) != -1) {
					out.write(buf, 0, length);
				}*/
					//in.reset();
			BufferedImage oSrc = ImageIO.read(in);
			in.reset();
			
			double scaleO = 100f / (oSrc.getWidth() > oSrc.getHeight() ? oSrc
					.getWidth() : oSrc.getHeight());
			int oWidth = (int) (oSrc.getWidth() * scaleO);
			int oHeight = (int) (oSrc.getHeight() * scaleO);
			
			ResampleOp resampleOp = new ResampleOp(oWidth, oHeight);
			resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.VerySharp);
			BufferedImage oImg = resampleOp.filter(oSrc, null);

			ImageIO.write(oImg, formatName, out);
			out.flush();
			out.close();
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					try {
						BufferedImage tSrc;	tSrc = ImageIO.read(in);
						in.close();
						double scaleT = 800f / (tSrc.getWidth() > tSrc.getHeight() ? tSrc
								.getWidth() : tSrc.getHeight());
						int tWidth = (int) (tSrc.getWidth() * scaleT);
						int tHeight = (int) (tSrc.getHeight() * scaleT);
						
						ResampleOp resampleOpT = new ResampleOp(tWidth, tHeight);
						resampleOpT.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.VerySharp);
						BufferedImage tImg = resampleOpT.filter(tSrc, null);
						FileOutputStream tOut = new FileOutputStream(tFile, false);
						ImageIO.write(tImg, formatName, tOut);
						tOut.flush();
						tOut.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			
			
			
		}
	public static void transfer(InputStream in, File originalFile)
			throws Exception {
		FileOutputStream out = new FileOutputStream(originalFile);
		int length = 0;
		byte[] buf = new byte[1024];

		while ((length = in.read(buf)) != -1) {
			out.write(buf, 0, length);
		}

		in.close();
		out.flush();
		out.close();
	}
	public static void copyfile(File file,File oldFile) throws Exception{
		FileOutputStream out = new FileOutputStream(file);
		InputStream in=new FileInputStream(oldFile);
		byte buffer[]=new byte[1024];
		int cnt=0;
		while((cnt=in.read(buffer))>0){  
			out.write(buffer, 0, cnt);  
        }
		in.close();
		out.flush();
		out.close();
	}
	
	public static void changeToMp3(String sourcePath, String targetPath) {
		File source = new File(sourcePath);
		File target = new File(targetPath);
		AudioAttributes audio = new AudioAttributes();
		Encoder encoder = new Encoder();

		audio.setCodec("libmp3lame");
		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat("mp3");
		attrs.setAudioAttributes(audio);

		try {
			encoder.encode(source, target, attrs);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InputFormatException e) {
			e.printStackTrace();
		} catch (EncoderException e) {
			e.printStackTrace();
		}
	}
	// public static void amr2mp3(String sourcePath, String targetPath) {
	// File source = new File(sourcePath);
	// File target = new File(targetPath);
	// amr2mp3(source, target);
	// }
	//
	// public static void amr2mp3(File source, File target) {
	// AudioAttributes audio = new AudioAttributes();
	// Encoder encoder = new Encoder();
	//
	// audio.setCodec("libmp3lame");
	// EncodingAttributes attrs = new EncodingAttributes();
	// attrs.setFormat("mp3");
	// attrs.setAudioAttributes(audio);
	//
	// try {
	// encoder.encode(source, target, attrs);
	// } catch (IllegalArgumentException e) {
	// e.printStackTrace();
	// } catch (InputFormatException e) {
	// e.printStackTrace();
	// } catch (EncoderException e) {
	// e.printStackTrace();
	// }
	// }
}
