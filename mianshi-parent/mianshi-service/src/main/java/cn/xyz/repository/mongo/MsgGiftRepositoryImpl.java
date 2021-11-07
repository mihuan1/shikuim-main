package cn.xyz.repository.mongo;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.mianshi.model.AddGiftParam;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Givegift;
import cn.xyz.mianshi.vo.Msg;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.MsgGiftRepository;

@Service
public class MsgGiftRepositoryImpl extends MongoRepository<Givegift, ObjectId> implements MsgGiftRepository {
	
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<Givegift> getEntityClass() {
		return Givegift.class;
	}
	
	@Override
	public List<ObjectId> add(Integer userId, ObjectId msgId,
			List<AddGiftParam> paramList) {
		User user = SKBeanUtils.getUserManager().getUser(userId);

		List<ObjectId> giftIdList = Lists.newArrayList();
		List<Givegift> entities = Lists.newArrayList();
		int activeValue = 0;

		for (AddGiftParam param : paramList) {
			Double price =1.0;
			/* goodsService.getGiftGoods(param.getGoodsId())
					.getPrice();*/
			activeValue += price * param.getCount();

			Givegift gift = new Givegift(param.getCount(),ObjectId.get(), msgId,
					user.getNickname(),price,DateUtil.currentTimeSeconds(), user.getUserId(),user.getUserId());

			giftIdList.add(gift.getGiftId());
			entities.add(gift);
		}

		// 缓存礼物
		RedissonClient redissonClient = SKBeanUtils.getLocalSpringBeanManager().getRedissonClient();
		try {
					String key = String.format("msg:%1$s:gift", msgId.toString());

					for (Givegift gift : entities) {
						String string = gift.toString();
						redissonClient.getQueue(key).add(string);
						
					}
					redissonClient.getList(key).trim(0, 500);
					redissonClient.getBucket(key).expire(43200, TimeUnit.SECONDS);
					redissonClient.shutdown();
					
					
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
				
			

		getDatastore().save(entities);
		SKBeanUtils.getMsgRepository().update(msgId, Msg.Op.Gift, activeValue);

		return giftIdList;
	}

	@Override
	public List<DBObject> findByGift(ObjectId msgId) {
		List<DBObject> objList = Lists.newArrayList();

		StringBuffer sbMap = new StringBuffer();
		sbMap.append(" function() { ");
		sbMap.append(" 	emit({ ");
		sbMap.append(" 		id : this.id ");
		sbMap.append(" 	}, { ");
		sbMap.append(" 		count : this.count ");
		sbMap.append(" 	}); ");
		sbMap.append(" } ");

		StringBuffer sbReduce = new StringBuffer();
		sbReduce.append(" function (key, values) { ");
		sbReduce.append(" 	var total = 0; ");
		sbReduce.append(" 	for (var i = 0; i < values.length; i++) { ");
		sbReduce.append(" 		total += values[i].count; ");
		sbReduce.append(" 	} ");
		sbReduce.append(" 	return total; ");
		sbReduce.append(" } ");

		DBCollection inputCollection = getDatastore().getDB().getCollection("gift");
		String map = sbMap.toString();
		String reduce = sbReduce.toString();
		DBObject query = new BasicDBObject("msgId", msgId);

		DBCursor cursor = inputCollection
				.mapReduce(map, reduce, "resultCollection", query)
				.getOutputCollection().find();

		while (cursor.hasNext()) {
			DBObject tObj = cursor.next();

			DBObject dbObj = (BasicDBObject) tObj.get("_id");
			dbObj.put("count", tObj.get("value"));

			objList.add(dbObj);
		}

		return objList;
	}

	@Override
	public List<DBObject> findByUser(ObjectId msgId) {
		List<DBObject> objList = Lists.newArrayList();

		StringBuffer sbMap = new StringBuffer();
		sbMap.append(" function() { ");
		sbMap.append(" 	emit({ ");
		sbMap.append(" 		userId : this.userId, ");
		sbMap.append(" 		nickname : this.nickname ");
		sbMap.append(" 	}, { ");
		sbMap.append(" 		price : this.price, ");
		sbMap.append(" 		count : this.count ");
		sbMap.append(" 	}); ");
		sbMap.append(" } ");

		StringBuffer sbReduce = new StringBuffer();
		sbReduce.append(" function (key, values) { ");
		sbReduce.append(" 	var result = 0; ");
		sbReduce.append(" 	for (var i = 0; i < values.length; i++) { ");
		sbReduce.append(" 		result += values[i].price * values[i].count; ");
		sbReduce.append(" 	} ");
		sbReduce.append(" 	return result; ");
		sbReduce.append(" } ");

		DBCollection inputCollection = getDatastore().getDB().getCollection("gift");
		String map = sbMap.toString();
		String reduce = sbReduce.toString();
		DBObject query = new BasicDBObject("msgId", msgId);

		DBCursor cursor = inputCollection
				.mapReduce(map, reduce, "resultCollection", query)
				.getOutputCollection().find();

		while (cursor.hasNext()) {
			DBObject tObj = cursor.next();

			DBObject dbObj = (BasicDBObject) tObj.get("_id");
			dbObj.put("money", tObj.get("value"));

			objList.add(dbObj);
		}

		return objList;
	}

	@Override
	public List<Givegift> find(ObjectId msgId, ObjectId giftId, int pageIndex,
			int pageSize) {
		List<Givegift> giftList = getDatastore().find(getEntityClass()).field("msgId")
				.equal(msgId).order("-_id").offset(pageIndex * pageSize)
				.limit(pageSize).asList();

		return giftList;
		/*String key = String.format("msg:%1$s:gift", msgId.toString());
		boolean exists = SKBeanUtils.getRedisCRUD().keyExists(key);
		// 赞没有缓存、加载所有赞到缓存
		if (!exists) {
			List<Givegift> giftList = getDatastore().find(getEntityClass()).field("msgId")
					.equal(msgId).order("-_id").limit(pageSize).asList();
					
					for (Givegift gift : giftList) {
						String string = gift.toString();
						SKBeanUtils.getRedisCRUD().lpush(key, string);
					}
					SKBeanUtils.getRedisCRUD().expire(key, 43200);// 重置过期时间
					
				
		}
		long start = pageIndex * pageSize;
		long end = pageIndex * pageSize + pageSize - 1;
		List<String> textList = SKBeanUtils.getRedisCRUD().lrange(key, start, end);
				

		// 缓存未命中、超出缓存范围
		if (0 == textList.size()) {
			List<Givegift> giftList = getDatastore().find(getEntityClass()).field("msgId")
					.equal(msgId).order("-_id").offset(pageIndex * pageSize)
					.limit(pageSize).asList();

			return giftList;
		} else {
			try {
				List<Givegift> giftList = Lists.newArrayList();
				for (String text : textList) {
					// JSON.parseObject(text, Gift.class)
					Givegift gift = new ObjectMapper().readValue(text, getEntityClass());
					giftList.add(gift);
				}
				return giftList;
			} catch (Exception e) {
				throw new ServiceException("赞缓存解析失败");
			}
		}*/
	}
}
