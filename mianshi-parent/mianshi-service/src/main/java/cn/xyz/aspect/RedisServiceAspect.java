package cn.xyz.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RedisServiceAspect {

	private Logger logger=LoggerFactory.getLogger(RedisServiceAspect.class);
	
	
	
		@Autowired
		private RedissonClient  redissonClient;
		///
		//// cn.xyz.service.RedisServiceImpl
		
		@Pointcut("execution(* cn.xyz.service.RedisServiceImpl.* (..)) || execution(* cn.xyz.commons.support.jedis.RedissonClientImpl.* (..))")
		public void redisServiceAspect() {
	
	
	   }


	 
	   
	   	@Around("redisServiceAspect()")
	    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
	   		Object result=null;
	   		try {
	   			result=joinPoint.proceed();
			}catch (RedisException e) {
				restartRedissonClient();
				throw e;
			}catch (Exception e) {
				throw e;
			}
	   		return result;
	   
	   	}
	   	
	   	
	   	public void restartRedissonClient() {
	   		try {
	   			logger.info("restart redis redissonClient ====>");
	   			Config redissonConfig = redissonClient.getConfig();
		   		//redissonClient.shutdown();
		   		redissonClient=Redisson.create(redissonConfig);
			} catch (Exception e) {
				logger.error("restartRedissonClient Exception {}",e.getMessage());
			}
	   		
	   	}
	   	
	   	
	   	/* @AfterThrowing(pointcut="execution(* cn.xyz.service.RedisServiceImpl.* (..)) or execution(* cn.xyz.service.RedissonClientImpl.* (..))",
		 throwing="ex")
		public Object around(Throwable ex) throws Throwable {
			return null;
		
		}*/
	

	 

	  
}
