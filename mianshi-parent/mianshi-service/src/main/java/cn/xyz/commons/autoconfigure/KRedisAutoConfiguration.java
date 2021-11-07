package cn.xyz.commons.autoconfigure;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.xyz.commons.autoconfigure.KApplicationProperties.RedisConfig;
import cn.xyz.commons.utils.StringUtil;

@Configuration
public class KRedisAutoConfiguration {

    @Autowired
    private RedisConfig redisConfig;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonSingle() {
    	
    	RedissonClient redissonClient=null;
    	
    	
    	try {
    		Config config = new Config();
    		
            config.setCodec(new JsonJacksonCodec()); 
            
            if(redisConfig.getIsCluster()==1) {
            	System.out.println("redisson Cluster start ");
            	String[] nodes =redisConfig.getAddress().split(",");
                 ClusterServersConfig serverConfig = config.useClusterServers();
                serverConfig.addNodeAddress(nodes);
                serverConfig.setKeepAlive(true);
                serverConfig.setPingConnectionInterval(redisConfig.getPingConnectionInterval());
                serverConfig.setPingTimeout(redisConfig.getPingTimeout());
                serverConfig.setTimeout(redisConfig.getTimeout());
                serverConfig.setConnectTimeout(redisConfig.getConnectTimeout());
                if(!StringUtil.isEmpty(redisConfig.getPassword())) {
                    serverConfig.setPassword(redisConfig.getPassword());
                }
           }else {
        	   System.out.println("redisson Single start ");
            	  SingleServerConfig serverConfig = config.useSingleServer()
                  		.setAddress(redisConfig.getAddress())
                  		.setDatabase(redisConfig.getDatabase());
            	  serverConfig.setKeepAlive(true);
                  serverConfig.setPingConnectionInterval(redisConfig.getPingConnectionInterval());
                  serverConfig.setPingTimeout(redisConfig.getPingTimeout());
                  serverConfig.setTimeout(redisConfig.getTimeout());
                  serverConfig.setConnectTimeout(redisConfig.getConnectTimeout());
                  serverConfig.setConnectionMinimumIdleSize(redisConfig.getConnectionMinimumIdleSize());
                  
                  serverConfig.setConnectionPoolSize(redisConfig.getConnectionPoolSize());
                  
                   if(!StringUtil.isEmpty(redisConfig.getPassword())) {
                      serverConfig.setPassword(redisConfig.getPassword());
                  }
            }
            
            
          
             redissonClient= Redisson.create(config);
             
             System.out.println("redisson create end ");
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        return redissonClient; 
        
    }
   /* @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxWaitMillis(10000);
        config.setMaxTotal(100);
       
        //config.setMaxIdle(-1);
        config.setTestOnCreate(true);
        config.setTestWhileIdle(true);
        config.setTestOnReturn(true);
        return config;
    }

    @Bean
    public JedisPool jedisPool(JedisPoolConfig jedisPoolConfig) {
        return new JedisPool(jedisPoolConfig, redisConfig.getHost(), redisConfig.getPort(),
                5000,("".equals(redisConfig.getPassword())?null:redisConfig.getPassword()), redisConfig.getDatabase(), null);
    }*/

//    @Bean
//    public JedisTemplate jedisTemplate(JedisPoolConfig jedisPoolConfig, JedisPool jedisPool) {
//        if (properties.getHost().contains(",")) {
//            Set<HostAndPort> nodes = new HashSet<HostAndPort>();
//            // 配置redis集群
//            for (String host : properties.getHost().split(",")) {
//                String[] detail = host.split(":");
//                nodes.add(new HostAndPort(detail[0], Integer.parseInt(detail[1])));
//            }
//            return new JedisTemplate(new JedisCluster(nodes, jedisPoolConfig));
//        } else {
//            return new JedisTemplate(jedisPool);
//        }
//    }

   /* @Bean(value="redisCRUD")
    @ConditionalOnProperty(name = "im.redisConfig.isCluster", havingValue = "false")
    public RedisUtil jedis(JedisPool jedisPool){
    	RedisUtil redis=new RedisUtil(jedisPool);
        return redis;
    }*/

   /* @Bean(value="redisCRUD")
    @ConditionalOnProperty(name = "im.redisConfig.isCluster", havingValue = "true")
    public JedisCluster jedisCluster(JedisPoolConfig jedisPoolConfig){
        Set<HostAndPort> nodes = new HashSet<>();
        // 配置redis集群
        for (String host : redisConfig.getHost().split(",")) {
            String[] detail = host.split(":");
            nodes.add(new HostAndPort(detail[0], Integer.parseInt(detail[1])));
        }
        return new JedisCluster(nodes, jedisPoolConfig);
    }*/

}
