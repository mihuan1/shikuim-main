package cn.xyz.commons.utils;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import cn.xyz.mianshi.utils.KSessionUtil;

public class NetworkUtil {
	
	/**
	 * https://ipinfo.io/ip
	 * 
	 * http://ip-api.com/json
	 */

	 /** 
     * 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址; 
     *  
     * @param request 
     * @return 
     * @throws IOException 
     */  
    public final static String getIpAddress(HttpServletRequest request)  {  
	        // 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址  
	  
	        String ip = request.getHeader("X-Forwarded-For");  
	        try {
	        	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
		            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
		                ip = request.getHeader("Proxy-Client-IP");  
		            }  
		            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
		                ip = request.getHeader("WL-Proxy-Client-IP");  
		            }  
		            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
		                ip = request.getHeader("HTTP_CLIENT_IP");  
		            }  
		            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
		                ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
		            }  
		            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
		                ip = request.getRemoteAddr();  
		            }  
		        } else if (ip.length() > 15) {  
		            String[] ips = ip.split(",");  
		            for (int index = 0; index < ips.length; index++) {  
		                String strIp = (String) ips[index];  
		                if (!("unknown".equalsIgnoreCase(strIp))) {  
		                    ip = strIp;  
		                    break;  
		                }  
		            }  
		        }
			} catch (Exception e) {
				e.printStackTrace();
				ip = request.getRemoteAddr();  
			}
	          
	        return ip;  
	  } 
    
    public final static String getAddresByIp(String ip){
    	if(ip.startsWith("192")||ip.startsWith("127"))
    		return null;
    	String address=null;
    	address=KSessionUtil.getAddressByIp(ip);
    	if(null!=address)
    		return address;
    	HttpUtil.Request request = new HttpUtil.Request();
		request.setSpec("https://www.boip.net/api/"+ip);
		request.setMethod(HttpUtil.RequestMethod.GET);
    	request.getData().put("ip", ip);
		try {
			address= HttpUtil.asBean(request);
			if(!StringUtil.isEmpty(address.split(",")[0]))
				KSessionUtil.setAddressByIp(ip, address);
		} catch (Exception e) {
			 e.printStackTrace();
		}
		return address;
    }
}
