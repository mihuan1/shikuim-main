package cn.xyz.mianshi.service.impl;


import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.ex.BizException;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.BankCard;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class BankCardManagerImpl extends MongoRepository<BankCard, ObjectId> {
    @Override
    public Datastore getDatastore() {
        return SKBeanUtils.getDatastore();
    }

    @Override
    public Class<BankCard> getEntityClass() {
        return BankCard.class;
    }

    public BankCard bindCard(Integer uid, String userName, String cardNo, Integer bankBrandId, String openBankAddr) throws BizException {
        BankCard bankCard = new BankCard();
        bankCard.setUid(uid);
        bankCard.setUserName(userName);
        bankCard.setCardNo(cardNo);
        bankCard.setCardName(KConstants.BANK_MAP.get(bankBrandId));
        bankCard.setBankBrandName(KConstants.BANK_MAP.get(bankBrandId));
        bankCard.setBankBrandId(bankBrandId);
        bankCard.setOpenBankAddr(openBankAddr);
        bankCard.setTime(DateUtil.currentTimeSeconds());
        if (null == bankBrandId || StringUtils.isAnyBlank(userName, cardNo, bankCard.getBankBrandName())) {
            throw new BizException("绑定失败，数据有误");
        }
        List<BankCard> bankCards = SKBeanUtils.getBankCardManager().getBankCardList(uid);
        boolean hasSame = bankCards.stream().filter(b -> cardNo.equals(b.getCardNo())).count() > 0;
        if (hasSame) {
            throw new BizException("该卡已存在，请重新绑定");
        }
        return saveBankCard(bankCard);
    }

    public BankCard saveBankCard(BankCard entity) {
        entity.setId(new ObjectId());
        save(entity);
        return entity;
    }

    public List<BankCard> getBankCardList(Integer uid) {
        Query<BankCard> q = createQuery().filter("uid", uid).filter("isDeleted", 0);
        return q.order("-time").asList();
    }

    public BankCard getBankCardForce(ObjectId id) {
        Query<BankCard> q = createQuery().filter("_id", id);
        return q.get();
    }

    public BankCard getBankCard(ObjectId id) {
        Query<BankCard> q = createQuery().filter("_id", id).filter("isDeleted", 0);
        return q.get();
    }

    public void unbindBankCard(Integer uid, ObjectId id) throws BizException {
        BankCard bankCard = get(id);
        if (null == bankCard || null == bankCard.getUid() || !bankCard.getUid().equals(uid))  {
            throw new BizException("卡信息有误");
        }
        if (SKBeanUtils.getConsumeRecordManager().hasWaitDealConsumeRecordByBankCard(id)) {
            throw new BizException("该卡有待完成交易，暂无法解绑");
        }
        UpdateOperations<BankCard> ops = createUpdateOperations();
        ops.set("isDeleted", 1);
        updateAttributeByOps(id, ops);
    }
}
