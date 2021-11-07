package tigase.shiku.db;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shiku.utils.StringUtil;

import tigase.shiku.conf.ShikuConfigBean;


/**
 * @author lidaye
 *
 */
public class RedisService {
	
	private  Logger logger = LoggerFactory.getLogger(RedisService.class.getName());
	
	private static RedisService instance = new RedisService();
	public static RedisService getInstance() {
		return instance;
	}
	
	public static final String GET_USERID_BYTOKEN = "loginToken:userId:%s";
	private RedissonClient redissonClient;
	
	private String redisUrl;
	
	private String password;
	
	private int database;
	
	private boolean isCluster;
	
	/**
	 * @return the redissonClient
	 */
	public RedissonClient getRedissonClient() {
		if(null==redissonClient)
			init(redisUrl, password, database, isCluster);
		return redissonClient;
	}
	public synchronized void init(String redisUrl,String password,int database,boolean isCluster) {
    	try {
    		Config config = new Config();
    		config.setCodec(new JsonJacksonCodec()); 
            
            if(isCluster) {
            	logger.info("redisson Cluster start ");
            	String[] nodes =redisUrl.split(",");
                 ClusterServersConfig serverConfig = config.useClusterServers();
                serverConfig.addNodeAddress(nodes);
                serverConfig.setKeepAlive(true);
                
                if(!StringUtil.isEmpty(password)) {
                    serverConfig.setPassword(password);
                }
           }else {
        	 logger.info("redisson Single start ");
            	  SingleServerConfig serverConfig = config.useSingleServer()
                  		.setAddress(redisUrl)
                  		.setDatabase(database);
            	  serverConfig.setKeepAlive(true);
                  
                   if(!StringUtil.isEmpty(password)) {
                      serverConfig.setPassword(password);
                  }
            }
            
            
          
             redissonClient= Redisson.create(config);
             
             logger.info("redisson create end ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public boolean authUser(String user,String token) {
		if(StringUtil.isEmpty(token))
			return false;
		String key = String.format(GET_USERID_BYTOKEN,token);
		RBucket<String> bucket = redissonClient.getBucket(key);
		/*
		 * String userId = bucket.get();
		 * logger.info("auth redis result ==> {} ",userId);
		 */
		return user.equals(bucket.get());
	}

}
