package cn.xyz.commons.support.jedis;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import cn.xyz.mianshi.utils.SKBeanUtils;

@ConditionalOnClass(RedissonClient.class)
@Component
public class RedissonClientImpl implements RedisCRUD {

	@Autowired
    private RedissonClient redissonClient;
	
	
	 private RBucket<Object> getRedisBucket(String key) {
	        return redissonClient.getBucket(key);
	 }
	 
	@Override
	public void set(String key, String value) {
		// TODO Auto-generated method stub
		 redissonClient.getBucket(key).set(value);
	}

	@Override
	public void set(String key, String value, String nxxx, String expx, int time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObject(String key, Object obj, int expireTime) {
		
		redissonClient.getBucket(key).set(obj, expireTime, TimeUnit.SECONDS);;
	}

	@Override
	public <T> T getObject(String key, Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasKey(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setWithExpireTime(String key, String value, int expireTime) {
		// TODO Auto-generated method stub
		redissonClient.getBucket(key).set(value, expireTime, TimeUnit.SECONDS);;
	}

	@Override
	public String get(String key) {
		// TODO Auto-generated method stub
		 RBucket<String> bucket = redissonClient.getBucket(key);
		return bucket.get();
	}

	@Override
	public void delete(String key) {
		// TODO Auto-generated method stub
		 redissonClient.getBucket(key).delete();
	}

	@Override
	public void delete(String... keys) {
		// TODO Auto-generated method stub
		for (String str : keys) {
			redissonClient.getBucket(str).delete();
		}
	}

	@Override
	public void del(String... keys) {
		// TODO Auto-generated method stub
		for (String str : keys) {
			redissonClient.getBucket(str).delete();
		}
	}
	
	@Override
	public  void deleteKeysByPattern(String pattern){		
		Iterable<String> keys  = keys(pattern);
		for(String key : keys) {
			SKBeanUtils.getRedisCRUD().delete(key);
		}
	}

	@Override
	public long ttl(String key) {
		// TODO Auto-generated method stub
		RBucket<Object> bucket = redissonClient.getBucket(key);
		if(bucket.isExists()) {
			long ttl = bucket.remainTimeToLive();
			if(ttl>0)
				return ttl/1000;
		}
		return 0;
	}

	@Override
	public Long lrem(String key, long count, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long lpush(String key, String... strings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long lpush(String key, int seconds, String... strings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void expire(String key, int seconds) {
		// TODO Auto-generated method stub
		redissonClient.getBucket(key).expire(seconds, TimeUnit.SECONDS);
	}

	@Override
	public List<String> lrange(String key, long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable <String> keys(String pattern) {
		// TODO Auto-generated method stub
		return redissonClient.getKeys().getKeysByPattern(pattern);
	}

	@Override
	public Long zcard(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long hset(String key, String field, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long llen(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zrank(String key, String member) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String hget(String key, String field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zrem(String key, String... members) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zadd(String key, double score, String member) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double zscore(String key, String member) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String rpop(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void ltrim(String key, int start, int end) {
		// TODO Auto-generated method stub
		 redissonClient.getList(key).trim(start, end);
	}

	@Override
	public Boolean keyExists(String key) {
		// TODO Auto-generated method stub
		return redissonClient.getBucket(key).isExists();
	}

	@Override
	public Set<String> hkeys(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
