package cn.xyz.mianshi.lable;

import java.util.List;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;

@Service
public class LabelManagerImpl extends MongoRepository <Label, Integer> implements LabelManager {

	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getLocalSpringBeanManager().getDatastore();
	}

	@Override
	public Class<Label> getEntityClass() {
		return Label.class;
	}
	
    @Autowired
    LabelRepositoryImpl repository;

    @Override
    public Label createLabel(Integer userId) {
        Label label = new Label();
        label.setId(new ObjectId());
        label.setUserId(userId);
        String code = UUID.randomUUID().toString().replace("-", "");
        label.setCode(code);
         repository.createLabel(label);
        return label;
    }

    @Override
    public Label createLabelByParams(Integer userId, String logo, String name){
        Label label = new Label();
        label.setId(new ObjectId());
        label.setUserId(userId);
        String code = UUID.randomUUID().toString().replace("-", "");
        label.setCode(code);
        label.setLogo(logo);
        label.setName(name);
        Object object =  repository.createLabel(label);

        return label;
    }

    @Override
    public Label getLabel(ObjectId labelId) {
        return repository.getLabel(labelId);
    }

    @Override
    public Label getLabelByCode(String code) {
        return  repository.getLabelByCode(code);
    }

    @Override
    public List<Label> getLabelList(Integer userId) {
        return repository.getLabelList(userId);
    }

    @Override
    public Label updateLabel(Label label) {
        return repository.updateLabel(label);
    }

    @Override
    public Object saveLabel(ObjectId id, String logo, String name) {
        return repository.saveLabel(id,logo,name);
    }

    @Override
    public Label queryLabelByName(String name) {
        return repository.queryLabelByName(name);
    }


}
