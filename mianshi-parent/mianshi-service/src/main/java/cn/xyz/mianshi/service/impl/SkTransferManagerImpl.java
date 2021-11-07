package cn.xyz.mianshi.service.impl;

import java.util.List;

import cn.xyz.commons.utils.UserUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.ConsumeRecord;
import cn.xyz.mianshi.vo.Transfer;
import cn.xyz.mianshi.vo.TransferReceive;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;

@Service
public class SkTransferManagerImpl extends MongoRepository<Transfer, ObjectId>{

	@Override
	public Datastore getDatastore() {
		return SKBeanUtils.getDatastore();
	}
	private static UserManagerImpl getUserManager(){
		UserManagerImpl userManager = SKBeanUtils.getUserManager();
		return userManager;
	};
	@Override
	public Class<Transfer> getEntityClass() {
		return Transfer.class;
	}
	
	public Transfer saveTransfer(Transfer entity){
		ObjectId id= (ObjectId) save(entity).getId();
		entity.setId(id);
		return entity;
	}
	
	/**
	 * 获取转账信息
	 * @param userId
	 * @param id
	 * @return
	 */
	public JSONMessage getTransferById(Integer userId,ObjectId id){
		Transfer transfer=get(id);
//		Map<String,Object> map=Maps.newHashMap();
//		map.put("transfer", transfer);
		//判断转账是否超时
		if(DateUtil.currentTimeSeconds()>transfer.getOutTime()){
			return JSONMessage.success("该转账已超过24小时",transfer);
		}
		// 判断转账状态是否正常
		if(transfer.getStatus()!=1){
			return JSONMessage.success("该转账已完成或退款",transfer);
		}
		
		return JSONMessage.success(null,transfer);
	}
	
	public synchronized JSONMessage sendTransfer(Integer userId,String money,Transfer transfer){
		if(SKBeanUtils.getUserManager().getUserMoeny(userId)<Double.valueOf(money)){
			return JSONMessage.failure("余额不足,请先充值!");
		}
		transfer.setUserId(userId);
		transfer.setUserName(SKBeanUtils.getUserManager().getUser(userId).getNickname());
		long cuTime=DateUtil.currentTimeSeconds();
		transfer.setCreateTime(cuTime);
		transfer.setOutTime(cuTime+KConstants.Expire.DAY1);
		if(StringUtil.isEmpty(transfer.getRemark())){
			transfer.setRemark("");
		}
		Object data=SKBeanUtils.getSkTransferManager().saveTransfer(transfer);
		//修改金额
		SKBeanUtils.getUserManager().rechargeUserMoeny(userId, transfer.getMoney(), KConstants.MOENY_REDUCE);
		new ThreadUtil();
		//开启一个线程 添加一条消费记录
		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
				String tradeNo=StringUtil.getOutTradeNo();
				//创建消费记录
				ConsumeRecord record=new ConsumeRecord();
				record.setUserId(userId);
				record.setToUserId(transfer.getToUserId());
				record.setTradeNo(tradeNo);
				record.setMoney(transfer.getMoney());
				record.setStatus(KConstants.OrderStatus.END);
				record.setType(KConstants.ConsumeType.SEND_TRANSFER);
				record.setPayType(KConstants.PayType.BALANCEAY); //余额支付
				record.setDesc("转账");
				record.setTime(DateUtil.currentTimeSeconds());
				SKBeanUtils.getConsumeRecordManager().save(record);
			}
		});
		return JSONMessage.success(null,data);
	}
	
	/**
	 * 转账收钱
	 * @param userId
	 * @param id
	 * @return
	 */
	public synchronized JSONMessage receiveTransfer(Integer userId,ObjectId id){
		Transfer transfer=get(id);
		//判断转账是否超时
		if(DateUtil.currentTimeSeconds()>transfer.getOutTime()){
			return JSONMessage.failureAndData("该转账已超过24小时",transfer);
		}
		// 判断转账状态是否已经完成
		if(transfer.getStatus()!=1){
			return JSONMessage.failureAndData("该转账已完成或退款",transfer);
		}
		
		// 判断是否发送给该用户的转账
		if(!transfer.getToUserId().equals(userId)){
			return JSONMessage.failureAndData("收款人不正确",transfer);
		}
		
		User user=getUserManager().getUser(userId);
		UpdateOperations<Transfer> ops=getDatastore().createUpdateOperations(Transfer.class);
		ops.set("status", 2);
		ops.set("receiptTime", DateUtil.currentTimeSeconds());
		updateAttributeByOps(transfer.getId(), ops);
		
		TransferReceive receive=new TransferReceive();
		receive.setMoney(transfer.getMoney());
		receive.setSendId(transfer.getUserId());
		receive.setUserId(userId);
		receive.setSendName(transfer.getUserName());
		receive.setUserName(user.getNickname());
		receive.setTransferId(transfer.getId().toString());
		receive.setTime(DateUtil.currentTimeSeconds());
		 getDatastore().save(receive);
		 
		//修改金额
		getUserManager().rechargeUserMoeny(userId, transfer.getMoney(), KConstants.MOENY_ADD);
		// 发送xmpp消息
		MessageBean messageBean=new MessageBean();
		messageBean.setType(KXMPPServiceImpl.RECEIVETRANSFER);
		messageBean.setFromUserId(user.getUserId().toString());
		messageBean.setFromUserName(UserUtil.getRemarkName(transfer.getUserId(), user.getUserId(), user.getNickname()));
		messageBean.setContent(transfer.getId().toString());
		messageBean.setToUserId(transfer.getUserId().toString());
		messageBean.setMsgType(0);// 单聊消息
		messageBean.setMessageId(StringUtil.randomUUID());
		try {
			KXMPPServiceImpl.getInstance().send(messageBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//开启一个线程 添加一条消费记录
		new Thread(new Runnable() {
			@Override
			public void run() {
				String tradeNo=StringUtil.getOutTradeNo();
				//创建消费记录
				ConsumeRecord record=new ConsumeRecord();
				record.setUserId(userId);
				record.setToUserId(transfer.getUserId());
				record.setTradeNo(tradeNo);
				record.setMoney(transfer.getMoney());
				record.setStatus(KConstants.OrderStatus.END);
				record.setType(KConstants.ConsumeType.RECEIVE_TRANSFER);
				record.setPayType(KConstants.PayType.BALANCEAY); //余额支付
				record.setDesc("接受转账");
				record.setTime(DateUtil.currentTimeSeconds());
				SKBeanUtils.getConsumeRecordManager().save(record);
			}
		}).start();
		
		return JSONMessage.success(receive);
	}
	
	/**
	 * 发起转账列表
	 * @param userId
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public List<Transfer> getTransferList(Integer userId,int pageIndex,int pageSize){
		Query<Transfer> query=getDatastore().createQuery(Transfer.class).field("userId").equal(userId);
		return query.order("-createTime").offset(pageIndex*pageSize).limit(pageSize).asList();
	}
	
	/**
	 * 接受转账列表
	 * @param userId
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public List<TransferReceive> getTransferReceiveList(Integer userId,int pageIndex,int pageSize){
		Query<TransferReceive> query=getDatastore().createQuery(TransferReceive.class).field("userId").equal(userId);
		return query.order("-time").offset(pageIndex*pageSize).limit(pageSize).asList();
	}
}
