package cn.xyz.repository.mongo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.xyz.commons.constants.KKeyConstant;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Msg;
import cn.xyz.repository.MsgListRepository;

@Service
public class MsgListRepositoryImpl extends MongoRepository<Msg, ObjectId> implements MsgListRepository {
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<Msg> getEntityClass() {
		return Msg.class;
	}

	
	
	@Override
	public String getHotId(int cityId, Object userId) {
		return null;
	}

	@Override
	public Object getHotList(int cityId, int pageIndex, int pageSize) {
		String key = String.format(KKeyConstant.HotMsgListTemplate, cityId);
		String hget = SKBeanUtils.getRedisCRUD().hget(key, String.valueOf(pageIndex));
		return JSON.parse(hget);
	}

	@Override
	public String getLatestId(int cityId, Object userId) {
		String key = String.format(KKeyConstant.UserLatestMsgIdTemplate, cityId);
	
			return SKBeanUtils.getRedisCRUD().hget(key, String.valueOf(userId));
		
	}

	@Override
	public Object getLatestList(int cityId, int pageIndex, int pageSize) {
		String key = String.format(KKeyConstant.LatestMsgListTemplate, cityId);
		String hget = SKBeanUtils.getRedisCRUD().hget(key, String.valueOf(pageIndex));
		return JSON.parse(hget);
	}

}
