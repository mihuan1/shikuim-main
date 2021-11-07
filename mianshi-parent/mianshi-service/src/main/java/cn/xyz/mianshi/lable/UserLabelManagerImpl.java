package cn.xyz.mianshi.lable;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.stereotype.Service;

import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.SKBeanUtils;

@Service
public class UserLabelManagerImpl extends MongoRepository <UserLabel, ObjectId> implements UserLabelManager{

	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<UserLabel> getEntityClass() {
		return UserLabel.class;
	}

	private static UserLabelRepositoryImpl getUserLabelRepository(){
		UserLabelRepositoryImpl userLabelManager = SKBeanUtils.getUserLabelRepository();
		return userLabelManager;
	};
	

    //添加群标识码
    @Override
    public Object addLabel(Integer userId,String labelId,String name,String logo,String code,long date) {

        UserLabel userLabel = new UserLabel();
        userLabel.setId(new ObjectId());
        userLabel.setUserId(userId);
        userLabel.setLabelId(labelId);
        userLabel.setCode(code);
        userLabel.setDate(date);
        userLabel.setName(name);
        userLabel.setLogo(logo);
        return getUserLabelRepository().addLabel(userLabel);
    }

    //获取用户群标识码列表
    @Override
    public List<UserLabel> getUserLabels(Integer userId) {
        return getUserLabelRepository().getUserLabels(userId);
    }

    @Override
    public UserLabel queryUserLabel(Integer userId, String labelId) {
        return getUserLabelRepository().getUserLabel(userId,labelId);
    }

    @Override
    public UserLabel queryUserLabelByCode(Integer userId, String code) {
        return  getUserLabelRepository().queryUserLabel(userId,code);
    }
}
