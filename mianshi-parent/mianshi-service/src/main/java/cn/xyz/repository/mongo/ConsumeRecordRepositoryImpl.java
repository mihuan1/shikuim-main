package cn.xyz.repository.mongo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.stereotype.Service;

import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.ConsumeRecord;

@Service
public class ConsumeRecordRepositoryImpl extends MongoRepository<ConsumeRecord, ObjectId>{

	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<ConsumeRecord> getEntityClass() {
		return ConsumeRecord.class;
	}


}
