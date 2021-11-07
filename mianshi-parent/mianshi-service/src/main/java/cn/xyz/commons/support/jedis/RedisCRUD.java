package cn.xyz.commons.support.jedis;

import java.util.List;
import java.util.Set;



public interface RedisCRUD {

	
	
    /**
     * 设置缓存
     *
     * @param key   缓存key
     * @param value 缓存value
     * @return 
     */
    void set(String key, String value);

    void set(String key, String value, String nxxx, String expx, int time);

    /**
     * 设置缓存对象
     *
     * @param key 缓存key
     * @param obj 缓存value
     */
     void setObject(String key, Object obj, int expireTime);

    /**
     * 获取指定key的缓存
     *
     * @param key---JSON.parseObject(value, User.class);
     */
    <T> T getObject(String key, Class<T> clazz);

    /**
     * 判断当前key值 是否存在
     *
     * @param key
     */
    boolean hasKey(String key);


    /**
     * 设置缓存，并且自己指定过期时间
     *
     * @param key
     * @param value
     * @param expireTime 过期时间
     */
    void setWithExpireTime(String key, String value, int expireTime);


    /**
     * 获取指定key的缓存
     *
     * @param key
     */
    String get(String key);

    /**
     * 删除指定key的缓存
     *
     * @param key
     */
    void delete(String key);

    void delete(String ... keys);
    
    /**
      * 删除指定前缀的 key
     * @param pattern
     */
    void deleteKeysByPattern(String pattern);
    
    
    public void del(final String... keys);

    /**
     * TTL key  以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)
     * @param key
     * @return
     */
    long ttl(String key);

    /**
     * 根据参数 count 的值，移除列表中与参数 value 相等的元素。
     *
     * count 的值可以是以下几种：
     *
     * count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。
     * count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
     * count = 0 : 移除表中所有与 value 相等的值。
     * @param key
     * @param count
     * @param value
     * @return 被移除元素的数量。
     * 因为不存在的 key 被视作空表(empty list)，所以当 key 不存在时， LREM 命令总是返回 0 。
     */
    Long lrem(String key, long count, String value);

    /**
     * 将一个或多个值 value 插入到列表 key 的表头
     * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表头： 比如说，对空列表 mylist 执行命令 LPUSH mylist a b c ，
     * 列表的值将是 c b a ，这等同于原子性地执行 LPUSH mylist a 、 LPUSH mylist b 和 LPUSH mylist c 三个命令。
     * 如果 key 不存在，一个空列表会被创建并执行 LPUSH 操作。
     * 当 key 存在但不是列表类型时，返回一个错误。
     * 在Redis 2.4版本以前的 LPUSH 命令，都只接受单个 value 值。
     * @param key
     * @param strings
     * @return  执行 LPUSH 命令后，列表的长度。
     */
    Long lpush(String key, String ... strings);

    Long lpush(String key, int seconds, String ... strings);

    /**
     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
     * 可以对一个已经带有生存时间的 key 执行 EXPIRE 命令，新指定的生存时间会取代旧的生存时间。
     * @param key
     * @param seconds
     * @return 设置成功返回 1 。
     * 当 key 不存在或者不能为 key 设置生存时间时(比如在低于 2.1.3 版本的 Redis 中你尝试更新 key 的生存时间)，返回 0 。
     */
    void expire(String key, int seconds);

    /**
     * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 stop 指定。
     * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     * @param key
     * @param start
     * @param end
     * @return  一个列表，包含指定区间内的元素。
     */
    List<String> lrange(String key, long start, long end);

    Iterable<String> keys(String pattern);

    Long zcard(final String key);

    Set<String> zrevrange(final String key, final long start, final long end);

    Long hset(final String key, final String field, final String value);

    Long llen(final String key);

    Long zrank(final String key, final String member);
    
    String hget(final String key, final String field);

    Set<String> zrangeByScore(final String key, final double min, final double max);

    Long zrem(final String key, final String... members);

    Long zadd(final String key, final double score, final String member);

    Double zscore(final String key, final String member);

    String rpop(final String key);

    void ltrim(final String key, final int start, final int end);
   
    public Boolean keyExists(String key);
    public Set<String> hkeys(final String key);
 
   
}
