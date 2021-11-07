package cn.xyz.mianshi.scheduleds;

import javax.annotation.Resource;

import org.mongodb.morphia.Datastore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MsgListScheduled {

	private int pageSize = 50;

	

	@Resource(name = "dsForRW")
	protected Datastore dsForRW;

	@Autowired(required=false)
    private RedissonClient redissonClient;
	public MsgListScheduled() {
		super();
	}

	/*private void syncHotList() {
		RBatch batch = redissonClient.createBatch(BatchOptions.defaults());
		
		
		try {
			
			Set<String> readAllKeySet = Set<String> redissonClient.getMap("msg.id.hot.list:*");
			for (String key : readAllKeySet) {

				long total = resource.zcard(key);
				long pageCount = total / pageSize + 0 == total % pageSize ? 0 : 1;
				for (long i = 0; i < pageCount; i++) {
					Set<String> members = resource.zrevrange(key, i * pageSize, i * pageSize + pageSize - 1);
					List<ObjectId> objIdList = Lists.newArrayList();
					for (String member : members) {
						objIdList.add(new ObjectId(member));
					}

					List<BasicDBObject> objList = Lists.newArrayList();
					try {
						DBObject ref = new BasicDBObject("_id", new BasicDBObject("$in", objIdList));
						DBCursor cursor = dsForRW.getCollection(Msg.class).find(ref)
								.sort(new BasicDBObject("count.total", -1));
						while (cursor.hasNext()) {
							BasicDBObject jo = (BasicDBObject) cursor.next();
							jo.put("msgId", jo.getString("_id"));
							jo.removeField("_id");
							objList.add(jo);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					String hmlKey = String.format(KKeyConstant.HotMsgListTemplate, key.substring(key.indexOf(':') + 1));
					resource.hset(hmlKey, String.valueOf(i), JSON.toJSONString(objList));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
				RedisUtil.close(redisCRUD, resource);
		}

				// System.out.println("最热人才榜：" + key + "\t" + total + "\t" +
				// pageCount);
			
	}

	private void syncLatestList() {
		RedisCRUD redisCRUD = SKBeanUtils.getRedisCRUD();
	
		
			for (String key : resource.hkeys("msg.id.latest.list:*")) {
				long total = resource.llen(key);
				long pageCount = total / pageSize + 0 == total % pageSize ? 0 : 1;
				for (long i = 0; i < pageCount; i++) {
					List<String> ids = resource.lrange(key, i * pageSize, i * pageSize + pageSize - 1);
					List<ObjectId> objIdList = Lists.newArrayList();
					for (String id : ids) {
						objIdList.add(new ObjectId(id));
					}

					List<BasicDBObject> objList = Lists.newArrayList();
					try {
						DBObject ref = new BasicDBObject("_id", new BasicDBObject("$in", objIdList));
						DBCursor cursor = dsForRW.getCollection(Msg.class).find(ref).sort(new BasicDBObject("_id", -1));
						while (cursor.hasNext()) {
							BasicDBObject jo = (BasicDBObject) cursor.next();
							jo.put("msgId", jo.getString("_id"));
							jo.removeField("_id");
							objList.add(jo);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					String lmlKey = String.format(KKeyConstant.LatestMsgListTemplate,
							key.substring(key.indexOf(':') + 1));
					resource.hset(lmlKey, String.valueOf(i), JSON.toJSONString(objList));
				}

				// System.out.println("最新人才榜：" + key + "\t" + total + "\t" +
				// pageCount);
			}
		
		
	}*/

	//@Scheduled(cron = "0 0/1 * * * ?")
	public void execute() {
		long start = System.currentTimeMillis();

		/*syncHotList();
		syncLatestList();*/

		System.out.println("榜单刷新完毕，耗时" + (System.currentTimeMillis() - start) + "毫秒");
	}

}
