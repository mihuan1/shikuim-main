package cn.xyz.commons.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.google.common.collect.Maps;

import cn.xyz.commons.ex.ServiceException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public final class HttpUtil {
	
	

	public static class Request {
		private Map<String, Object> data = Maps.newHashMap();
		private RequestMethod method;
		private String spec;

		public Request() {
			super();
		}

		public Request(Map<String, Object> data, String spec) {
			super();
			this.data = data;
			this.spec = spec;
		}

		public Request(Map<String, Object> data, RequestMethod method, String spec) {
			super();
			this.data = data;
			this.method = method;
			this.spec = spec;
		}

		public Map<String, Object> getData() {
			return data;
		}

		public RequestMethod getMethod() {
			return method;
		}

		public String getSpec() {
			return spec;
		}

		public void setData(Map<String, Object> data) {
			this.data = data;
		}

		public void setMethod(RequestMethod method) {
			this.method = method;
		}

		public void setSpec(String spec) {
			this.spec = spec;
		}

	}

	public static enum RequestMethod {
		DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE
	}

	private static byte[] getBytes(Map<String, Object> data) throws Exception {
		StringBuffer sb = new StringBuffer();
		for (String key : data.keySet())
			sb.append(key).append('=').append(data.get(key)).append('&');
		return sb.substring(0, sb.length() - 1).toString().getBytes("UTF-8");
	}

	public static String get(Request request) throws Exception {
		URL url = new URL(buildSpec(request));
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setDoOutput(true);
		urlConn.setDoInput(true);
		urlConn.setRequestMethod("GET");

		return FileUtil.readAll(urlConn.getInputStream());
	}

	private static String buildSpec(Request request) {
		Map<String, Object> params = request.getData();
		StringBuffer sb = new StringBuffer();
		sb.append(request.getSpec());
		if (!params.isEmpty())
			sb.append("?");
		for (String key : params.keySet()) {
			sb.append(key).append('=').append(params.get(key)).append('&');
		}
		String spec = sb.substring(0, sb.length() - 1);
		System.out.println(spec);
		return spec;
	}

	public static String asString(Request request) throws Exception {
		URL url = new URL(request.getSpec());
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setDoOutput(true);
		urlConn.setDoInput(true);
		// urlConn.setRequestMethod("POST");
		// urlConn.setRequestMethod(method);
		urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		urlConn.setUseCaches(false);
		urlConn.setInstanceFollowRedirects(true);
		if (null != request.getData()) {
			OutputStream out = urlConn.getOutputStream();
			out.write(getBytes(request.getData()));
			out.flush();
			out.close();
		}

		return FileUtil.readAll(urlConn.getInputStream());
	}
	public static String  asBean(Request request) throws Exception {
		return asString(request);
	}
	@SuppressWarnings("unchecked")
	public static <T> T asBean(Request request, Class<?> clazz) throws Exception {
		String text = asString(request);

		return (T) JSON.parseObject(text, clazz);
	}

	public static <T> T asBean(Map<String, Object> data, String spec, Class<?> clazz) throws Exception {
		return asBean(new Request(data, spec), clazz);
	}

	public static byte[] getBytes(Object object) {
		SerializeWriter out = new SerializeWriter();
		JSONSerializer.write(out, object);
		return out.toBytes("UTF-8");
	}
	
	
	
	
    /* 
     * 以下为向服务器发送 HTTP 请求
     * 
     *  */
	
	
    private static Log log = LogFactory.getLog(HttpUtil.class);
	
	/**
	 *  UTF-8
	 */
	public static final String URL_PARAM_DECODECHARSET_UTF8 = "UTF-8";
	
	/**
	 *  GBK
	 */
	public static final String URL_PARAM_DECODECHARSET_GBK = "GBK";
	
	private static final String URL_PARAM_CONNECT_FLAG = "&";
	
	private static final String EMPTY = "";

	private static MultiThreadedHttpConnectionManager connectionManager = null;

	private static int connectionTimeOut = 25000;

	private static int socketTimeOut = 25000;

	private static int maxConnectionPerHost = 20;

	private static int maxTotalConnections = 20;

	private static HttpClient client;

	static{
		connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setConnectionTimeout(connectionTimeOut);
		connectionManager.getParams().setSoTimeout(socketTimeOut);
		connectionManager.getParams().setDefaultMaxConnectionsPerHost(maxConnectionPerHost);
		connectionManager.getParams().setMaxTotalConnections(maxTotalConnections);
		client = new HttpClient(connectionManager);
	}
	
	
	/**
	 * POST
	 * @param url
	 * 			URL
	 * @param params
	 * 			
	 * @param enc
	 * 			
	 * @return
	 * 			
	 * @throws IOException
	 * 			
	 */
	public static String URLPost(String url, Map<String, Object> params, boolean isJson){
		String enc = URL_PARAM_DECODECHARSET_UTF8;
		String response = EMPTY;		
		PostMethod postMethod = null;
		try {
			postMethod = new PostMethod(url);
			String contentType = "application/x-www-form-urlencoded;charset=" + enc;
			if (isJson) contentType = "application/json;charset=UTF-8";
			postMethod.setRequestHeader("Content-Type", contentType); //设置请求头
			if(null!=params){
				Set<String> keySet = params.keySet();
				for(String key : keySet){
					Object value = params.get(key);
					postMethod.addParameter(key, value.toString());
				}	
			}
			
			int statusCode = client.executeMethod(postMethod);
			if(statusCode == HttpStatus.SC_OK) {
				response = IOUtils.toString(postMethod.getResponseBodyAsStream(), "utf-8");
				System.out.println("result:"+response);
			}else{
				log.error(String.format("请求失败:%s, url:%s, params:%s", postMethod.getStatusCode(), url, JSON.toJSONString(params)));
			}
		}catch(HttpException e){
			log.error("HttpException", e);
			e.printStackTrace();
		}catch(IOException e){
			log.error("IOException", e);
			e.printStackTrace();
		}finally{
			if(postMethod != null){
				postMethod.releaseConnection();
				postMethod = null;
			}
		}
		
		return response;
	}

	public static String postJsonBody(String url, String json) {
		CloseableHttpClient httpClient = null;
		HttpPost httpPost = new HttpPost(url);
		String body = null;
		CloseableHttpResponse response = null;
		try {
			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(5000).build())
					.build();
			httpPost.setEntity(new StringEntity(json, "UTF-8"));
			httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
			response = httpClient.execute(httpPost);
			body = EntityUtils.toString(response.getEntity(), "UTF-8");
			log.info(String.format("postJsonBody ----> url:%s, json:%s, response code:%s, %s", url, json, response.getStatusLine().getStatusCode(), body));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return body;
	}

	
	/**
	 * GET提交方式
	 * @param url
	 * 			URL
	 * @param params
	 * 			
	 * @param enc
	 * 			
	 * @return
	 * 			
	 * @throws IOException
	 * 			
	 */
	public static String URLGet(String url, Map<String, String> params ){
		String enc = URL_PARAM_DECODECHARSET_UTF8;
		String response = EMPTY;
		GetMethod getMethod = null;		
		StringBuffer strtTotalURL = new StringBuffer(EMPTY);
		
	    if(strtTotalURL.indexOf("?") == -1) {
	      strtTotalURL.append(url).append("?").append(getUrl(params, enc));
	    } else {
	    	strtTotalURL.append(url).append("&").append(getUrl(params, enc));
	    }
	    log.debug("GETURL = \n" + strtTotalURL.toString());
	    
		try {
			getMethod = new GetMethod(strtTotalURL.toString());
			getMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=" + enc);
		
			int statusCode = client.executeMethod(getMethod);
			if(statusCode == HttpStatus.SC_OK) {
				response = IOUtils.toString(getMethod.getResponseBodyAsStream(), "utf-8");
			}else{
				log.debug(" = " + getMethod.getStatusCode());
			}
		}catch(HttpException e){
			log.error("HttpException", e);
			e.printStackTrace();
		}catch(IOException e){
			log.error("IOException", e);
			e.printStackTrace();
		}finally{
			if(getMethod != null){
				getMethod.releaseConnection();
				getMethod = null;
			}
		}
		
		return response;
	}	

	
	/**
	 * 
	 * @param map
	 * 			Map
	 * @param valueEnc
	 * 			UR
	 * @return
	 * 			URL
	 */
	private static String getUrl(Map<String, String> map, String valueEnc) {
		
		if (null == map || map.keySet().size() == 0) {
			return (EMPTY);
		}
		StringBuffer url = new StringBuffer();
		Set<String> keys = map.keySet();
		for (Iterator<String> it = keys.iterator(); it.hasNext();) {
			String key = it.next();
			if (map.containsKey(key)) {
				String val = map.get(key);
				String str = val != null ? val : EMPTY;
				try {
					str = URLEncoder.encode(str, valueEnc);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				url.append(key).append("=").append(str).append(URL_PARAM_CONNECT_FLAG);
			}
		}
		String strURL = EMPTY;
		strURL = url.toString();
		if (URL_PARAM_CONNECT_FLAG.equals(EMPTY + strURL.charAt(strURL.length() - 1))) {
			strURL = strURL.substring(0, strURL.length() - 1);
		}
		
		return (strURL);
	}
	
	/** @Description: 检测当前URL是否404（有效）
	 * @param address
	 * @param spareAddress
	 * @return
	 * @throws Exception
	 **/  
    public static String testWsdlConnection(String address,String spareAddress) throws Exception {
		int status = 404;
		try {
			URL urlObj = new URL(address);
			HttpURLConnection oc = (HttpURLConnection) urlObj.openConnection();
			oc.setUseCaches(false);
			oc.setConnectTimeout(100); // 设置超时时间
			status = oc.getResponseCode();// 请求状态
			if (404 == status) {
				return spareAddress;
			}else{
				return address;
			}
		} catch (ServiceException e) {
			e.printStackTrace();
			return spareAddress;
		} catch (UnknownHostException e) {
			return spareAddress;
		} catch (SSLHandshakeException e) {
			return spareAddress;
		}
	}

	public static String readBodyByServletRequest(HttpServletRequest request) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try {
			br = request.getReader();
			String str;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
}
