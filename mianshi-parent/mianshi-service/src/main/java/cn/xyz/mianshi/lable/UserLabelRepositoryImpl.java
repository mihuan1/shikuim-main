package cn.xyz.mianshi.lable;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Service;

import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;


@Service
public class UserLabelRepositoryImpl extends MongoRepository<UserLabel,ObjectId> implements UserLabelRepository{
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<UserLabel> getEntityClass() {
		return UserLabel.class;
	}

    @Override
    public Object addLabel(UserLabel userLabel) {
        return getDatastore().save(userLabel);
    }

    @Override
    public UserLabel getUserLabel(Integer userId, String labelId) {
        Query<UserLabel> q = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId).field("labelId").equal(labelId);
        UserLabel userLabel = q.get();
        return userLabel;
    }

    @Override
    public List<UserLabel> getUserLabels(Integer userId) {
        Query<UserLabel> q = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId);
        return q.asList();
    }

    @Override
    public UserLabel queryUserLabel(Integer userId, String code) {
        Query<UserLabel> q = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId).field("code").equal(code);
        return q.get();
    }
}
