package cn.xyz.mianshi.service.impl;


import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.ex.BizException;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.model.PageVO;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.BankCard;
import cn.xyz.mianshi.vo.ConsumeRecord;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.mongo.ConsumeRecordRepositoryImpl;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mongodb.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ConsumeRecordManagerImpl extends MongoRepository<ConsumeRecord, ObjectId>{
	
	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getLocalSpringBeanManager().getDatastore();
	}
	@Override
	public Class<ConsumeRecord> getEntityClass() {
		return ConsumeRecord.class;
	}
	
	@Autowired
	ConsumeRecordRepositoryImpl repository;

	/**
	 * 监控新交易
	 * @return
	 */
	public boolean newTradeMonitor() {
		Query<ConsumeRecord> q=repository.createQuery().field("time").greaterThan(DateUtil.currentTimeSeconds() - DateUtil.ONE_DAY_SECONDS);
		q.or(q.criteria("notifyWeb").equal(false), q.criteria("notifyWeb").doesNotExist());
		List<ConsumeRecord> records = q.asList(pageFindOption(1, 3, 1));
		boolean result = CollectionUtils.isNotEmpty(records);
		if (result) {
			UpdateOperations<ConsumeRecord> ops = createUpdateOperations();
			ops.set("notifyWeb", true);
			records.forEach(c -> updateAttributeByOps(c.getId(), ops));
		}
		return result;
	}

	public boolean hasWaitDealConsumeRecordByBankCard(ObjectId bankCardId) {
		Query<ConsumeRecord> q = repository.createQuery();
		q.field("bankCardId").equal(bankCardId.toString()).field("auditStatus").equal(KConstants.AuditStatusCons.AUDIT_WAIT);
		return CollectionUtils.isNotEmpty(q.asList());
	}

	/**
	 * @param id
	 * @param auditStatus
	 * @param fundsChangeType
	 * @see KConstants.FundsChangeType
	 */
	public void updateAuditStatus(ObjectId id, Byte auditStatus, String fundsChangeType, String descInput) throws BizException {
		if (null == auditStatus) return;
		ConsumeRecord originalRecord = get(id);
		if (KConstants.AuditStatus.AUDIT_FAIL.getId() == originalRecord.getAuditStatus()) {
			throw new BizException("审核已拒绝，无法再次审核");
		}
		boolean useInputDesc = StringUtils.isNotBlank(descInput);
		UpdateOperations<ConsumeRecord> ops = createUpdateOperations();
		ops.set("auditStatus", auditStatus);
		String desc = null;
		switch (auditStatus) {
			case KConstants.AuditStatusCons.AUDIT_WAIT:
				desc = String.format(KConstants.AuditStatus.AUDIT_WAIT.getName(), fundsChangeType);
				break;
			case KConstants.AuditStatusCons.AUDIT_PASS:
				desc = String.format(KConstants.AuditStatus.AUDIT_PASS.getName(), fundsChangeType);
				break;
			case KConstants.AuditStatusCons.AUDIT_FAIL:
				if (useInputDesc) descInput = "审核拒绝:" + descInput;
				desc = String.format(KConstants.AuditStatus.AUDIT_FAIL.getName(), fundsChangeType);
				ConsumeRecord record = new ConsumeRecord();
				record.setUserId(originalRecord.getUserId());
				record.setTradeNo(StringUtil.getOutTradeNo());
				record.setMoney(originalRecord.getMoney());
				record.setStatus(KConstants.OrderStatus.END);
				record.setType(KConstants.ConsumeType.SYSTEM_RETURN);
				record.setPayType(KConstants.PayType.SYSTEMPAY);
				record.setDesc("系统归还");
				record.setTime(DateUtil.currentTimeSeconds());
				record.setOperationAmount(originalRecord.getOperationAmount());
				Double balance = SKBeanUtils.getUserManager().rechargeUserMoeny(record.getUserId(), record.getOperationAmount(), KConstants.MOENY_ADD);
				record.setCurrentBalance(balance);
				save(record);
				break;
			default:
				break;

		}
		if (KConstants.AuditStatusCons.AUDIT_FAIL == auditStatus && useInputDesc) ops.set("desc", descInput);
		else if(null != desc) ops.set("desc", desc);
		updateAttributeByOps(id, ops);
	}
	
	public void saveConsumeRecord(ConsumeRecord entity){
		if(null==entity.getId())
			save(entity);
		else  update(entity.getId(), entity);
	}
	
	public PageResult<ConsumeRecord> getConsumeRecordByTradeNo(String tradeNo){
		PageResult<ConsumeRecord> result = new PageResult<ConsumeRecord>();
		Query<ConsumeRecord> q=repository.createQuery().filter("tradeNo", tradeNo);
		ConsumeRecord record = q.get();
		if(null != record){
			record.setUserName(SKBeanUtils.getUserManager().getNickName(record.getUserId()));
		}else{
			throw new ServiceException("无该消费记录");
		}
		List<ConsumeRecord> records = new ArrayList<ConsumeRecord>();
		records.add(record);
		result.setData(records);
		result.setCount(q.count());
		return result;
	}

	public ConsumeRecord getConsumeRecordByNo(String tradeNo){
		Query<ConsumeRecord> q=repository.createQuery();
		if(!StringUtil.isEmpty(tradeNo))
			q.filter("tradeNo", tradeNo);
		return q.get();
	}
	
	public ConsumeRecord getConsumeReCord(Integer userId,ObjectId id){
		Query<ConsumeRecord> query = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId).field("_id").equal(id);
		return query.get();
	}
	public Object reChargeList(Integer userId ,int pageIndex,int pageSize){
		Query<ConsumeRecord> q=repository.createQuery();
		q.filter("type", KConstants.MOENY_ADD);
		if(0!=userId)
			q.filter("userId", userId);
		List<ConsumeRecord> pageData = q.asList(pageFindOption(pageIndex, pageSize, 0));
		long total=q.count();
		return new PageVO(pageData, total,pageIndex, pageSize);
	}
	
	
	public PageResult<DBObject> consumeRecordList(Integer userId,int page,int limit,byte state,String startDate,String endDate,int type){
		
		PageResult<DBObject>  result = new PageResult<DBObject>();
		List<DBObject> consumeRecords = new ArrayList<>();
		Map<String, Object> totalVO = Maps.newConcurrentMap();
		final DBCollection collection = SKBeanUtils.getDatastore().getDB().getCollection("ConsumeRecord");
		List<DBObject> pipeline=new ArrayList<>();
		BasicDBObject basicDBObject = new BasicDBObject("userId",userId).append("status", KConstants.OrderStatus.END);
		if(0 != type)
			basicDBObject.append("type", type);
		if(!StringUtil.isEmpty(startDate) && !StringUtil.isEmpty(endDate)){
			long startTime = 0; //开始时间（秒）
			long endTime = 0; //结束时间（秒）,默认为当前时间
			startTime = StringUtil.isEmpty(startDate) ? 0 :DateUtil.toDate(startDate).getTime()/1000;
			endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime()/1000;
			long formateEndtime = DateUtil.getOnedayNextDay(endTime,1,0);
			basicDBObject.append("time", new BasicDBObject(MongoOperator.GT,startTime)).append("time", new BasicDBObject(MongoOperator.LT,formateEndtime));
		}
		page = page > 0 ? page - 1 : page;
		DBCursor dbCursor = getDatastore().getDB().getCollection("ConsumeRecord").find(basicDBObject).sort(new BasicDBObject("time",-1))
			.skip((page)*limit).limit(limit);
		while (dbCursor.hasNext()) {
			DBObject obj = dbCursor.next();
			consumeRecords.add(obj);
		}
		result.setCount(dbCursor.count());
		result.setData(consumeRecords);
		DBObject match=new BasicDBObject("$match", basicDBObject);
		DBObject group=new BasicDBObject("$group", new  BasicDBObject("_id", "$type")
				.append("sum",new BasicDBObject("$sum","$money")));
		pipeline.add(match);
		pipeline.add(group);
		AggregationOptions options=AggregationOptions.builder().build();
		Cursor cursor = collection.aggregate(pipeline,options);
		// 总充值、提现、转出、转入、发送红包、接收红包
		double totalTecharge = 0, totalCash = 0, totalTransfer = 0, totalAccount = 0, sendPacket = 0, receivePacket = 0;
		try {
			while (cursor.hasNext()) {
				BasicDBObject dbObject = (BasicDBObject) cursor.next();
				// 充值
				if(dbObject.get("_id").equals(1) || dbObject.get("_id").equals(3)){
					totalTecharge = StringUtil.addDouble(totalTecharge, (double)dbObject.get("sum"));
				}
				// 提现
				if(dbObject.get("_id").equals(2) || dbObject.get("_id").equals(16)){
					totalCash = StringUtil.addDouble(totalCash, (double)dbObject.get("sum"));
				}
				// 转出
				if(dbObject.get("_id").equals(7)){
					totalTransfer = StringUtil.addDouble(totalTransfer, (double)dbObject.get("sum"));
				}
				// 转入
				if(dbObject.get("_id").equals(8)){
					totalAccount = StringUtil.addDouble(totalAccount, (double)dbObject.get("sum"));
				}
				// 总红包： 发出红包 接收红包
				if(dbObject.get("_id").equals(4)){
					sendPacket = StringUtil.addDouble(sendPacket, (double)dbObject.get("sum"));
				}
				// 接收红包
				if(dbObject.get("_id").equals(5)){
					receivePacket = StringUtil.addDouble(receivePacket, (double)dbObject.get("sum"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			cursor.close();
		}
		totalVO.put("totalTecharge", new DecimalFormat("#.00").format(totalTecharge));
		totalVO.put("totalCash", new DecimalFormat("#.00").format(totalCash));
		totalVO.put("totalTransfer", new DecimalFormat("#.00").format(totalTransfer));
		totalVO.put("sendPacket", new DecimalFormat("#.00").format(sendPacket));
		totalVO.put("receivePacket", new DecimalFormat("#.00").format(receivePacket));
		totalVO.put("totalAccount", new DecimalFormat("#.00").format(totalAccount));
		result.setTotalVo(JSONObject.toJSONString(totalVO));
		log.info("当前总充值 totalTecharge :{}  总提现 totalCash :{}  总转出 totalTransfer :{}  总转入 totalAccount :{}  总发送红包 sendPacket :{}  总接收红包 receivePacket :{}"
				,totalTecharge,totalCash,totalTransfer,totalAccount,sendPacket,receivePacket);
//		log.info("pushMessage error code:%s comment:%s", result.code(), result.comment());
		return result;
	}
	
	public PageResult<ConsumeRecord> consumeRecordList(Integer userId,int page,int limit,byte state){
	
		PageResult<ConsumeRecord>  result = new PageResult<ConsumeRecord>();
		Query<ConsumeRecord> q = repository.createQuery().order("-time");
		q.filter("userId", userId);
		q.field("money").greaterThan(0);
		q.filter("status", KConstants.OrderStatus.END);
		result.setData(q.asList(pageFindOption(page, limit, state)));
		result.setCount(q.count());
		return result;
	}
	
	public ConsumeRecord consumeRecordList(String tradeNo){
		
		Query<ConsumeRecord> q = repository.createQuery().order("-time");
		q.filter("tradeNo", tradeNo);
		q.filter("status", KConstants.OrderStatus.END);
		return q.get();
	}
	
	public PageResult<ConsumeRecord> friendRecordList(Integer userId,int toUserId,
			int page,int limit,byte start){
		
		PageResult<ConsumeRecord>  result = new PageResult<ConsumeRecord>();
		Query<ConsumeRecord> q = repository.createQuery().order("-time");
		
		if(0!=userId)
			q.filter("userId", userId);
		if(0!=toUserId)
			q.filter("toUserId", toUserId);
		
			q.field("money").greaterThan(0);
			q.filter("status", KConstants.OrderStatus.END);
			q.field("type").greaterThan(3);
			result.setData(q.asList(pageFindOption(page, limit, start)));
			
			result.setCount(q.count());
			return result;
	}
	
	
	/** @Description:（用户充值记录） 
	* @param userId
	* @param type
	* @param page
	* @param limit
	* @return
	**/ 
	public PageResult<ConsumeRecord> recharge(int userId,int type,int page,int limit,String startDate,String endDate,String tradeNo){
		double totalMoney = 0;
		PageResult<ConsumeRecord> result = new PageResult<ConsumeRecord>();
		Query<ConsumeRecord> query = getDatastore().createQuery(getEntityClass()).order("-time");
		if(0 != type && 2 == type)
			query.or(query.criteria("type").equal(KConstants.ConsumeType.PUT_RAISE_CASH),query.criteria("type").equal(KConstants.ConsumeType.SYSTEM_HANDCASH));// 过滤用户充值和后台
		else 
			query.or(query.criteria("type").equal(KConstants.ConsumeType.USER_RECHARGE),
					query.criteria("type").equal(KConstants.ConsumeType.SYSTEM_RECHARGE),
					query.criteria("type").equal(KConstants.ConsumeType.SYSTEM_RETURN));// 过滤用户充值和后台
		if(0 != userId)
			query.filter("userId", userId);
		if(!StringUtil.isEmpty(tradeNo))
			query.filter("tradeNo", tradeNo);
		if(!StringUtil.isEmpty(startDate) && !StringUtil.isEmpty(endDate)){
			long startTime = 0; //开始时间（秒）
			long endTime = 0; //结束时间（秒）,默认为当前时间
			startTime = StringUtil.isEmpty(startDate) ? 0 :DateUtil.toDate(startDate).getTime()/1000;
			endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime()/1000;
			long formateEndtime = DateUtil.getOnedayNextDay(endTime,1,0);
			query.field("time").greaterThan(startTime).field("time").lessThanOrEq(formateEndtime);
		}
		List<ConsumeRecord> recordList = query.asList(pageFindOption(page, limit, 1));
		for(ConsumeRecord record : recordList){
			BigDecimal bd1 = new BigDecimal(Double.toString(totalMoney)); 
	        BigDecimal bd2 = new BigDecimal(Double.toString(record.getMoney())); 
			totalMoney =  bd1.add(bd2).doubleValue();
			record.setUserName(SKBeanUtils.getUserManager().getNickName(record.getUserId()));
		}	
		result.setCount(query.count());
		log.info("当前总金额："+totalMoney);
		result.setTotal(totalMoney);
		result.setData(recordList);
		return result;
	}
	
	/**
	 * 用户付款记录
	 * @param userId
	 * @param type
	 * @param page
	 * @param limit
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public PageResult<ConsumeRecord> payment(int userId,int type,int page,int limit,String startDate,String endDate){
		double totalMoney = 0;
		PageResult<ConsumeRecord> result = new PageResult<ConsumeRecord>();
		Query<ConsumeRecord> query = getDatastore().createQuery(getEntityClass()).order("-time");
		if(0 != type)
			query.filter("type", type);
		else 
			query.or(query.criteria("type").equal(KConstants.ConsumeType.SEND_PAYMENTCODE),query.criteria("type").equal(KConstants.ConsumeType.SEND_QRCODE));// 过滤用户付款码付款和二维码付款
		if(0 != userId)
			query.filter("userId", userId);
		if(!StringUtil.isEmpty(startDate) && !StringUtil.isEmpty(endDate)){
			long startTime = 0; //开始时间（秒）
			long endTime = 0; //结束时间（秒）,默认为当前时间
			startTime = StringUtil.isEmpty(startDate) ? 0 :DateUtil.toDate(startDate).getTime()/1000;
//			DateUtil.getTodayNight();
			endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime()/1000;
			long formateEndtime = DateUtil.getOnedayNextDay(endTime,1,0);
			query.field("time").greaterThan(startTime).field("time").lessThanOrEq(formateEndtime);
		}
		List<ConsumeRecord> recordList = query.asList(pageFindOption(page, limit, 1));
		for(ConsumeRecord record : recordList){
			BigDecimal bd1 = new BigDecimal(Double.toString(totalMoney)); 
	        BigDecimal bd2 = new BigDecimal(Double.toString(record.getMoney())); 
			totalMoney =  bd1.add(bd2).doubleValue();
			record.setUserName(SKBeanUtils.getUserManager().getNickName(record.getUserId()));
		}
		result.setCount(query.count());
		log.info("当前总金额："+totalMoney);
		result.setTotal(totalMoney);
		result.setData(recordList);
		return result;
	}

	public void applyDrawings(Integer uid, Double money, String cardId) throws BizException {
		// 核验用户是否存在
		User user = SKBeanUtils.getUserManager().getUser(uid);
		Double serviceCharge = BigDecimal.valueOf(money * 0.01).setScale(2, BigDecimal.ROUND_UP).doubleValue();
		Double operationAmount = money + serviceCharge;
		if (null == user) {
			throw new BizException("申请提现失败, 用户不存在!");
		} else {
			Double balance = user.getBalance();
			if (money < 100) {
				serviceCharge = 1.0;
				operationAmount = money + serviceCharge;
			}
			Double subMoney = BigDecimal.valueOf(balance).subtract(BigDecimal.valueOf(operationAmount)).doubleValue();
			if (subMoney < 0) {
				Double validMoney = BigDecimal.valueOf(balance-serviceCharge).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
				throw new BizException("余额不足, 最多可提现:" + (validMoney < 0 ? 0 : validMoney));
			}
		}
		BankCard bankCard = SKBeanUtils.getBankCardManager().getBankCard(new ObjectId(cardId));
		if (null == bankCard) throw new BizException("申请提现失败, 银行卡不存在或已删除!");
		if (!uid.equals(bankCard.getUid())) throw new BizException("申请提现失败, 必须是自己的银行卡!");

		boolean hasPaid = false;
		String tradeNo = StringUtil.getOutTradeNo();
		// 创建交易记录
		ConsumeRecord record = new ConsumeRecord();
		record.setUserId(uid);
		record.setTradeNo(tradeNo);
		record.setMoney(money);
		record.setStatus(KConstants.OrderStatus.END);
		record.setType(KConstants.ConsumeType.PUT_RAISE_CASH);
		record.setPayType(KConstants.PayType.SYSTEMPAY);
		record.setDesc(String.format(hasPaid ? KConstants.AuditStatus.AUDIT_PASS.getName() : KConstants.AuditStatus.AUDIT_WAIT.getName(), KConstants.FundsChangeType.DRAWINGS));
		record.setTime(DateUtil.currentTimeSeconds());
		record.setOperationAmount(operationAmount);
		record.setServiceCharge(serviceCharge);
		record.setBankCardId(cardId);
		record.setAuditStatus(hasPaid ? KConstants.AuditStatusCons.AUDIT_PASS : KConstants.AuditStatusCons.AUDIT_WAIT);
		try {
			Double balance = SKBeanUtils.getUserManager().rechargeUserMoeny(uid, operationAmount, KConstants.MOENY_REDUCE);
			record.setCurrentBalance(balance);
			save(record);
		} catch (Exception e) {
			throw new BizException(e.getMessage());
		}
	}

	public List<ConsumeRecord> queryPayingYipayRecord() {
		Query<ConsumeRecord> q = createQuery().filter("payVendorChannel", KConstants.PayVendorChannel.YIPAY).filter("status", 0).field("time").greaterThan(DateUtil.currentTimeSeconds() - 60 * 10);
		return q.asList();
	}
}
