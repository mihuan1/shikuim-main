package com.shiku.mianshi.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.xyz.commons.autoconfigure.KApplicationProperties.AppConfig;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.model.KSession;
import cn.xyz.mianshi.utils.KSessionUtil;


public abstract class AbstractController {
	
	protected Logger logger=LoggerFactory.getLogger("controller");
	
	@Resource(name = "appConfig")
	protected AppConfig appConfig;
	
	public HttpServletRequest getRequest() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		return request;
	}

	/**
	 * 获取登录用户的用户索引
	 * 
	 * @return 用户索引
	 */
	// public int getUserId() {
	// Object userId = getRequest().getAttribute("userId");
	// return null == userId ? 0 : Integer.parseInt(userId.toString());
	// }
	
	protected KSession getSession() {
		HttpServletRequest request = getRequest();
		String access_token = request.getParameter("access_token");
		return KSessionUtil.getSession(access_token);
	}
	protected String getAccess_token() {
		HttpServletRequest request = getRequest();
		String access_token = request.getParameter("access_token");
		return access_token;
	}
	
	protected String getSecret() {
		HttpServletRequest request = getRequest();
		String secret = request.getParameter("secret");
		return secret;
	}
	
	// 获取ip地址
	public String getRequestIp() {
		HttpServletRequest request = getRequest();
		String requestIp = request.getHeader("x-forwarded-for");

		if (requestIp == null || requestIp.isEmpty() || "unknown".equalsIgnoreCase(requestIp)) {
			requestIp = request.getHeader("X-Real-IP");
		}
		if (requestIp == null || requestIp.isEmpty() || "unknown".equalsIgnoreCase(requestIp)) {
			requestIp = request.getHeader("Proxy-Client-IP");
		}
		if (requestIp == null || requestIp.isEmpty() || "unknown".equalsIgnoreCase(requestIp)) {
			requestIp = request.getHeader("WL-Proxy-Client-IP");
		}
		if (requestIp == null || requestIp.isEmpty() || "unknown".equalsIgnoreCase(requestIp)) {
			requestIp = request.getHeader("HTTP_CLIENT_IP");
		}
		if (requestIp == null || requestIp.isEmpty() || "unknown".equalsIgnoreCase(requestIp)) {
			requestIp = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (requestIp == null || requestIp.isEmpty() || "unknown".equalsIgnoreCase(requestIp)) {
			requestIp = request.getRemoteAddr();
		}
		if (requestIp == null || requestIp.isEmpty() || "unknown".equalsIgnoreCase(requestIp)) {
			requestIp = request.getRemoteHost();
		}

		return requestIp;
	}
	
	protected ObjectId parse(String s) {
		return StringUtil.isEmpty(s) ? null : new ObjectId(s);
	}
	
	protected void referer(HttpServletResponse response,String redirectUrl,int isSession){
		String retUrl =null;
		if(1==isSession)
				retUrl=getRequest().getSession().getAttribute("redirectUrl").toString();
		else retUrl = getRequest().getHeader("Referer");  
		try {
				if(null != retUrl)
					response.sendRedirect(retUrl);
				else
					response.sendRedirect(redirectUrl);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void addReferer(){
		HttpServletRequest request= getRequest();
		String retUrl = request.getHeader("Referer");   
		request.getSession().setAttribute("redirectUrl", retUrl);;
	}
	
	
	protected void writer(HttpServletRequest request,HttpServletResponse response,String redirectUrl){
		try {
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter writer = response.getWriter();
			
			String retUrl = request.getHeader("Referer");  
			writer.write(
					"<script type='text/javascript'>alert('\u64cd\u4f5c\u6210\u529f');</script>");
			if(null != retUrl)
				writer.write(
						"<script type='text/javascript'>window.location.href='"+retUrl+"';</script>");
			else
				writer.write(
						"<script type='text/javascript'>window.location.href='"+redirectUrl+"';</script>");
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}