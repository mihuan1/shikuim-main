package cn.xyz.repository.mongo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.stereotype.Service;

import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.RedPacket;

@Service
public class RedPacketRepositoryImpl extends MongoRepository<RedPacket, ObjectId>{

	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<RedPacket> getEntityClass() {
		return RedPacket.class;
	}


}
