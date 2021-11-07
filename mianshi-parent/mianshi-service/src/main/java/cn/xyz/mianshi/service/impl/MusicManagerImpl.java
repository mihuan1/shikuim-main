package cn.xyz.mianshi.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.MusicInfo;

@Service
public class MusicManagerImpl extends MongoRepository<MusicInfo,ObjectId>{

	@Override
	public Datastore getDatastore() {
		// TODO Auto-generated method stub
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<MusicInfo> getEntityClass() {
		// TODO Auto-generated method stub
		return MusicInfo.class;
	}
	
	public List<MusicInfo> queryMusicInfo(int pageIndex,int pageSize,String keyword) {
		Query<MusicInfo> query=createQuery();
		if(!StringUtil.isEmpty(keyword))
			query.or(query.criteria("name").contains(keyword),
					query.criteria("nikeName").contains(keyword));
		query.order("-useCount");
		List<MusicInfo> resultList=query.asList(pageFindOption(pageIndex, pageSize,0));
		return resultList;
	}
	
	/**
	 * 添加短视频音乐
	 * @param musicInfo
	 */
	public void addMusicInfo(MusicInfo musicInfo){
		MusicInfo entity = new MusicInfo();
		if (!StringUtil.isEmpty(musicInfo.getCover()))
			entity.setCover(musicInfo.getCover());
		if(!StringUtil.isEmpty(musicInfo.getName()))
			entity.setName(musicInfo.getName());
		if(!StringUtil.isEmpty(musicInfo.getNikeName()))
			entity.setNikeName(musicInfo.getNikeName());
		if(!StringUtil.isEmpty(musicInfo.getPath()))
			entity.setPath(musicInfo.getPath());
		
		entity.setLength(musicInfo.getLength());
		entity.setUseCount(musicInfo.getUseCount());
		
		saveEntity(entity);
	}
	
	/**
	 * 删除短视频音乐
	 * @param id
	 */
	public void deleteMusicInfo(ObjectId id){
		Query<MusicInfo> query=getDatastore().createQuery(MusicInfo.class).field("_id").equal(id);
		MusicInfo musicInfo=query.get();
		try {
			// 删除音乐文件
			deleteResource(musicInfo.getPath());
			// 删除头像文件
			deleteResource(musicInfo.getCover());
			// 删除短视频音乐主体
			deleteByQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 修改
	 * @param musicInfo
	 */
	public void updateMusicInfo(MusicInfo musicInfo){
		Query<MusicInfo> query=getDatastore().createQuery(getEntityClass()).field("_id").equal(musicInfo.getId());
		UpdateOperations<MusicInfo> ops=getDatastore().createUpdateOperations(getEntityClass());
		if(!StringUtil.isEmpty(musicInfo.getCover()))
			ops.set("cover", musicInfo.getCover());
		if(!StringUtil.isEmpty(musicInfo.getName()))
			ops.set("name", musicInfo.getName());
		if(!StringUtil.isEmpty(musicInfo.getNikeName()))
			ops.set("nikeName", musicInfo.getNikeName());
		if(!StringUtil.isEmpty(musicInfo.getPath()))
			ops.set("path", musicInfo.getPath());
		if(musicInfo.getLength()!=0)
			ops.set("length", musicInfo.getLength());
		ops.set("useCount", musicInfo.getUseCount());
		
		getDatastore().update(query, ops);
	}
	
	/**
	 * 维护音乐使用次数
	 * @param id
	 */
	public void updateUseCount(ObjectId id){
		Query<MusicInfo> query=getDatastore().createQuery(getEntityClass()).field("_id").equal(id);
		UpdateOperations<MusicInfo> ops=getDatastore().createUpdateOperations(getEntityClass());
		ops.set("useCount", query.get().getUseCount()+1);
		getDatastore().update(query, ops);
	}
	
	/**
	 * 删除文件
	 * @param url
	 */
	public void deleteResource(String url){
		
		ConstantUtil.deleteFile(url);
	}
}
