package com.shiku.mianshi.advice;

import java.io.EOFException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.shiku.mianshi.ResponseUtil;

import cn.xyz.commons.ex.ServiceException;

@ControllerAdvice
public class ExceptionHandlerAdvice {

	private Logger logger=LoggerFactory.getLogger(ExceptionHandlerAdvice.class);
	
	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public void handleErrors(HttpServletRequest request,
			HttpServletResponse response, Exception e) throws Exception {
		

		int resultCode = 1020101;
		String resultMsg = "接口内部异常";
		String detailMsg = "";
		logger.info(request.getRequestURI() + "错误：");
		if (e instanceof MissingServletRequestParameterException
				|| e instanceof BindException) {
			resultCode = 1010101;
			resultMsg = "请求参数验证失败，缺少必填参数或参数错误";
		} else if (e instanceof ServiceException) {
			ServiceException ex = ((ServiceException) e);

			resultCode = null == ex.getResultCode() ? 0 : ex.getResultCode();
			resultMsg = ex.getMessage();
		} else if (e instanceof ClientAbortException) {
			resultCode=-1;
		}else if(e instanceof EOFException){
			detailMsg = e.getMessage();
		}else {
			detailMsg = e.getMessage();
		}
		logger.info(resultMsg);

		Map<String, Object> map = Maps.newHashMap();
		map.put("resultCode", resultCode);
		map.put("resultMsg", resultMsg);
		map.put("detailMsg", detailMsg);

		String text = JSON.toJSONString(map);

		ResponseUtil.output(response, text);
	}

}
