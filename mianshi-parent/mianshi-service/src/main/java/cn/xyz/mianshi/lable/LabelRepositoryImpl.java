package cn.xyz.mianshi.lable;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;


@Service
public class LabelRepositoryImpl extends MongoRepository<Label,ObjectId> implements LabelRepository{
	
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<Label> getEntityClass() {
		return Label.class;
	}

    @Override
    public Object createLabel(Label label) {
       return getDatastore().save(label);
    }

    @Override
    public List<Label> getLabelList(Integer userId) {
        Query<Label> q = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId);
        return q.asList();
    }

    @Override
    public Label getLabel(ObjectId labelId) {
        Query<Label> q = getDatastore().createQuery(getEntityClass()).field("id").equal(labelId);
        return q.get();
    }

    @Override
    public Label getLabelByCode(String code) {
        Query<Label> q = getDatastore().createQuery(getEntityClass()).field("code").equal(code);
        return q.get();
    }

    @Override
    public Label updateLabel(Label label) {
        Query<Label> q = getDatastore().createQuery(getEntityClass()).field("code").equal(label.getCode());
        if(q.get() == null)
            return  null;

        UpdateOperations<Label> ops = getDatastore().createUpdateOperations(getEntityClass());

        if(null != label.getName())
            ops.set("name",label.getName());
        if(null != label.getLogo())
            ops.set("logo",label.getLogo());
        if(null != label.getMark())
            ops.set("mark",label.getMark());

        return getDatastore().findAndModify(q, ops);
    }

    @Override
    public Object saveLabel(ObjectId id, String logo, String name) {
        Query<Label> q = getDatastore().createQuery(getEntityClass()).field("id").equal(id);

        UpdateOperations<Label> ops = getDatastore().createUpdateOperations(getEntityClass());

        if(null != name)
            ops.set("name",name);
        if(null != logo)
            ops.set("logo",logo);

        return getDatastore().findAndModify(q, ops);
    }

    @Override
    public Label queryLabelByName(String name) {
        Query<Label> q = getDatastore().createQuery(getEntityClass()).field("name").equal(name);
        return  q.get();
    }

}
