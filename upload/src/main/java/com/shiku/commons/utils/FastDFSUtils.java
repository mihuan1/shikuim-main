package com.shiku.commons.utils;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;

import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerGroup;
import org.csource.fastdfs.TrackerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.shiku.commons.vo.FastDFSFile;

/**
* @Description: TODO(用一句话描述该文件做什么)
* @author lidaye
* @date 2018年5月22日 
*/
public class FastDFSUtils implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = -4462272673174266738L;
    private static TrackerClient trackerClient;
    private static TrackerServer trackerServer;
    private static StorageClient storageClient;
    private static Logger logger = LoggerFactory.getLogger(FastDFSUtils.class);
   /* static {
        try {
        	initServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
    public static void initServer(Environment env) throws MyException{
    	String[] szTrackerServers;
        String[] parts;

      
        String connect_timeout = env.getProperty("connect_timeout", ClientGlobal.DEFAULT_CONNECT_TIMEOUT+"");
        ClientGlobal.g_connect_timeout =Integer.valueOf(connect_timeout);
        if (ClientGlobal.g_connect_timeout < 0) {
        	ClientGlobal.g_connect_timeout = ClientGlobal.DEFAULT_CONNECT_TIMEOUT;
        }
        ClientGlobal.g_connect_timeout *= 1000; //millisecond
        String network_timeout = env.getProperty("network_timeout", ClientGlobal.DEFAULT_NETWORK_TIMEOUT+"");
        ClientGlobal.g_network_timeout = Integer.valueOf(network_timeout);
        if (ClientGlobal.g_network_timeout < 0) {
        	ClientGlobal.g_network_timeout = ClientGlobal.DEFAULT_NETWORK_TIMEOUT;
        }
        ClientGlobal.g_network_timeout *= 1000; //millisecond

        ClientGlobal.g_charset = env.getProperty("charset");
        if (ClientGlobal.g_charset == null || ClientGlobal.g_charset.length() == 0) {
        	ClientGlobal.g_charset = "ISO8859-1";
        }
        String trackerServers = env.getProperty("tracker_server");
        if(StringUtils.isEmpty(trackerServers)){
        	throw new MyException("item \"tracker_server\" in " +env.getDefaultProfiles()+ " not found");
        }
        szTrackerServers = trackerServers.split(",");
        if (szTrackerServers == null) {
        	throw new MyException("item \"tracker_server\" in " +env.getDefaultProfiles()+ " not found");
        }

        InetSocketAddress[] tracker_servers = new InetSocketAddress[szTrackerServers.length];
        for (int i = 0; i < szTrackerServers.length; i++) {
          parts = szTrackerServers[i].split("\\:", 2);
          if (parts.length != 2) {
            throw new MyException("the value of item \"tracker_server\" is invalid, the correct format is host:port");
          }

          tracker_servers[i] = new InetSocketAddress(parts[0].trim(), Integer.parseInt(parts[1].trim()));
        }
        ClientGlobal.g_tracker_group = new TrackerGroup(tracker_servers);
        String httpPort = env.getProperty("http.tracker_http_port", 80+"");
        ClientGlobal.g_tracker_http_port = Integer.valueOf(httpPort);
        String token =env.getProperty("http.anti_steal_token", false+"");
        ClientGlobal.g_anti_steal_token =Boolean.valueOf(token);
        if (ClientGlobal.g_anti_steal_token) {
        	ClientGlobal.g_secret_key = env.getProperty("http.secret_key");
        }
      
    }

    public static synchronized void initServer(){
    	try {
    		 //clientGloble读配置文件
    		
           /* ClassPathResource resource = new ClassPathResource("fdfs_client.conf");
            ClientGlobal.init(resource.getClassLoader().getResource("fdfs_client.conf").getPath());*/
    		if(null!=storageClient) {
    			return ;
    		}
    		
            //trackerclient
            trackerClient = new TrackerClient();
            trackerServer = trackerClient.getConnection();
            //storageclient
            storageClient = new StorageClient(trackerServer,null); 
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
    private static StorageClient getStorageClient() throws IOException{
    	if(null!=storageClient)
    		return storageClient;
    	else {
    		logger.error(" storageClient is null");
    		initServer();
    	}
    	if(null==trackerClient||null==trackerServer) {
    		initServer();
    	}
    	if(null!=trackerServer.getSocket()&&!trackerServer.getSocket().isConnected())
    		initServer();
    	return storageClient;
    }
    /**
     * fastDFS文件上传
     * @param file 上传的文件 FastDFSFile
     * @return String 返回文件的绝对路径
     */
    public static synchronized String uploadFile(FastDFSFile file){
        String path = null;
        int errCount=0;
        try {
            //文件扩展名
            String ext = FilenameUtils.getExtension(file.getName());
            //mata list是表文件的描述
            NameValuePair[] mata_list = new NameValuePair[3];
            mata_list[0] = new NameValuePair("fileName",file.getName());
            mata_list[1] = new NameValuePair("fileExt",ext);
            mata_list[2] = new NameValuePair("fileSize",String.valueOf(file.getSize()));
            String parts[]=null;
            try {
            	 parts  = getStorageClient().upload_file(file.getContent(), ext, mata_list);
			} catch (Exception e) {
				e.printStackTrace();
				 errCount++;
		          if(errCount<3)
		            	 parts  = getStorageClient().upload_file(file.getContent(), ext, mata_list);
		            return null;
			}
           
            if (parts == null){
            	 return null;
            }
          /*  for (int i = 0; i < parts.length; i++) {
				System.out.println("===> "+parts[i]);
			}*/
            path=parts[0] + "/" + parts[1];
            return path;
        } catch (Exception e) {
            e.printStackTrace();
             return null;
        } 
        
    }
    
    public static String getFileInfo(String path){
    	try {
    		FileInfo info = getStorageClient().get_file_info(getGroupFormFilePath(path), getFileNameFormFilePath(path));
    		String result=JSONObject.toJSONString(info);
    		logger.info("== getFileInfo > "+result);
    		return result;
    	} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public static String getMetadata(String path){
    	try {
    		NameValuePair[] metadata = getStorageClient().get_metadata(getGroupFormFilePath(path), getFileNameFormFilePath(path));
        	/*for (int i = 0; i < metadata.length; i++) {
    			System.out.println(JSONObject.toJSONString(metadata[i]));
    		}*/
    		String result=JSONObject.toJSONString(metadata);
    		logger.info("== getMetadata > "+result);
        	return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    	
    }
    
    /**
    * @Description: TODO(删除文件   )
    * @param @param path  文件地址    
    * 		group1/M00/00/00/wKgAi1sDwgGAO157AFpTVyqM9V8683.mp4
    * @param @return    参数
     */
    public static boolean deleteFile(String path){
    	try {
    		int result = getStorageClient().delete_file(getGroupFormFilePath(path), getFileNameFormFilePath(path));
    		logger.info("===delete file > "+path+" > "+(result==0?" 成功 ":" 失败"));
    		
    		return 0==result;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
    	
    }

    /**
     * fastDFS文件下载
     * @param groupName 组名
     * @param remoteFileName 文件名
     * @param specFileName 真实文件名
     * @return ResponseEntity<byte[]>
     */
    public static ResponseEntity<byte[]> downloadFile(String groupName, String remoteFileName, String specFileName){
        byte[] content = null;
        HttpHeaders headers = new HttpHeaders();
        try {
            content = getStorageClient().download_file(groupName, remoteFileName);
            headers.setContentDispositionFormData("attachment",  new String(specFileName.getBytes("UTF-8"),"iso-8859-1"));
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<byte[]>(content, headers, HttpStatus.CREATED);
    }

    /**
     * 根据fastDFS返回的path得到文件的组名
     * @param path fastDFS返回的path
     * @return
     */
    public static String getGroupFormFilePath(String path){
        return path.split("/")[0];
    }

    /**
     * 根据fastDFS返回的path得到文件名
     * @param path fastDFS返回的path
     * @return
     */
    public static String getFileNameFormFilePath(String path) {
        return path.substring(path.indexOf("/")+1);
    }
}

