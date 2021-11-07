package cn.xyz.mianshi.service.impl;


import cn.xyz.commons.utils.DateUtil;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.FriendsterWebsite;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendsterWebsiteManagerImpl extends MongoRepository<FriendsterWebsite, ObjectId> {

    @Override
    public Datastore getDatastore() {
        return SKBeanUtils.getDatastore();
    }

    @Override
    public Class<FriendsterWebsite> getEntityClass() {
        return FriendsterWebsite.class;
    }

    public FriendsterWebsite saveFriendsterWebsite(FriendsterWebsite entity) {
        entity.setId(new ObjectId());
        entity.setTime(DateUtil.currentTimeMilliSeconds());
        save(entity);
        return entity;
    }

    public List<FriendsterWebsite> getFriendsterWebsiteList() {
        Query<FriendsterWebsite> q = createQuery();
        return q.order("-time").asList();
    }

    public PageResult<FriendsterWebsite> queryFriendsterWebsiteList(int pageIndex, int pageSize) {
        PageResult<FriendsterWebsite> result=new PageResult<>();
        Query<FriendsterWebsite> query=getDatastore().createQuery(FriendsterWebsite.class);
        query.order("-time");
        result.setCount(query.count());
        result.setData(query.asList(pageFindOption(pageIndex, pageSize,1)));
        return result;
    }

    public FriendsterWebsite getFriendsterWebsite(String id) {
        Query<FriendsterWebsite> q = createQuery().filter("_id", new ObjectId(id));
        return q.get();
    }
}
