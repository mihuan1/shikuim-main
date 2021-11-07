package cn.xyz.mianshi.service.impl;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Report;

@Service
public class ReportManagerImpl extends MongoRepository<Report, ObjectId>{

	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<Report> getEntityClass() {
		return Report.class;
	}
	
	/** @Description:（删除举报） 
	* @param id
	* @return
	**/ 
	public JSONMessage deleteReport(String id){
		Query<Report> query = getDatastore().createQuery(getEntityClass()).field("_id").equal(id);
		if(null!=query){
			getDatastore().delete(query);
			return JSONMessage.success();
		}else 
			return JSONMessage.failure("暂无举报信息");
	}
	
	public Map<Long, List<Report>> getReport(int type,int sender,int receiver,int pageIndex,int pageSize) {
		Map<Long, List<Report>> map = Maps.newConcurrentMap();
		try {
			if (type == 0) {
				Query<Report> q = getDatastore().createQuery(Report.class);
				if(0!=sender)
					q.field("userId").equal(sender);
				if(0!=receiver)
					q.field("toUserId").equal(receiver);
				q.field("roomId").equal("");
				q.offset(pageSize*pageIndex);
				for(Report report : q.asList()){
					if(KConstants.ReportReason.reasonMap.containsKey(report.getReason()))
						report.setInfo(KConstants.ReportReason.reasonMap.get(report.getReason()));
				}
				List<Report> data = q.limit(pageSize).asList();
				map.put(q.count(), data);
			} else if (type == 1) {
			Query<Report> q = getDatastore().createQuery(Report.class);
			if(0!=sender)
				q.field("userId").equal(sender);
			if(0!=receiver)
				q.field("roomId").equal(receiver);
			q.field("roomId").notEqual("");
			q.offset(pageSize*pageIndex);
			for(Report report : q.asList()){
				if(KConstants.ReportReason.reasonMap.containsKey(report.getReason()))
					report.setInfo(KConstants.ReportReason.reasonMap.get(report.getReason()));
			}
			List<Report> data = q.limit(pageSize).asList();
			map.put(q.count(), data);
		}
		return map;
	} catch (Exception e) {
		e.printStackTrace();
	}
		return map;
}
	
	
}
