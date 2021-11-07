package cn.xyz.mianshi.service.impl;

import java.text.DecimalFormat;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.stereotype.Service;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.AliPayTransfersRecord;
import cn.xyz.mianshi.vo.ConsumeRecord;
import cn.xyz.mianshi.vo.TransfersRecord;

@Service
public class TransfersRecordManagerImpl extends MongoRepository<TransfersRecord, ObjectId>{

	@Override
	public Datastore getDatastore() {
		// TODO Auto-generated method stub
		return SKBeanUtils.getDatastore();
	}

	@Override
	public Class<TransfersRecord> getEntityClass() {
		// TODO Auto-generated method stub
		return TransfersRecord.class;
	}
	
	/**
	 * 微信提现
	 * @param record
	 */
	public synchronized void  transfersToWXUser(TransfersRecord record) {
		try {
			ConsumeRecord entity=new ConsumeRecord();
		 	entity.setUserId(ReqUtil.getUserId());
			entity.setTime(DateUtil.currentTimeSeconds());
			entity.setType(KConstants.ConsumeType.PUT_RAISE_CASH);
			entity.setDesc("微信提现");
			entity.setStatus(KConstants.OrderStatus.END);
			entity.setTradeNo(record.getOutTradeNo());
			entity.setPayType(KConstants.PayType.BALANCEAY);
			
			DecimalFormat df = new DecimalFormat("#.00");
			double total=Double.valueOf(record.getTotalFee())/100;
			
			total= Double.valueOf(df.format(total));
			
			entity.setMoney(total);
			Double balance = SKBeanUtils.getUserManager().rechargeUserMoeny(record.getUserId(), total, 2);
			entity.setServiceCharge(Double.valueOf(record.getFee()));// 手续费
			entity.setOperationAmount(Double.valueOf(record.getRealFee()));// 实际操作金额
			entity.setCurrentBalance(balance);// 当前余额
			SKBeanUtils.getConsumeRecordManager().saveConsumeRecord(entity);
			save(record);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * 支付宝提现
	 * @param record
	 */
	public synchronized void transfersToAliPay(AliPayTransfersRecord record){
		try {
			ConsumeRecord entity=new ConsumeRecord();
			entity.setUserId(ReqUtil.getUserId());
			entity.setTime(DateUtil.currentTimeSeconds());
			entity.setType(KConstants.ConsumeType.PUT_RAISE_CASH);
			entity.setDesc("支付宝提现");
			entity.setStatus(KConstants.OrderStatus.END);
			entity.setTradeNo(record.getOutTradeNo());
			entity.setPayType(KConstants.PayType.BALANCEAY);
			double total=Double.valueOf(record.getTotalFee());
			entity.setMoney(total);
			entity.setServiceCharge(Double.valueOf(record.getFee()));// 手续费
			entity.setOperationAmount(Double.valueOf(record.getRealFee()));// 实际操作金额
			Double balance = SKBeanUtils.getUserManager().rechargeUserMoeny(record.getUserId(), total, 2);
			entity.setCurrentBalance(balance);// 当前余额
			SKBeanUtils.getConsumeRecordManager().save(entity);
			saveEntity(record);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
