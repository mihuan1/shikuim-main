package cn.xyz.mianshi.service.impl;


import cn.xyz.commons.ex.BizException;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.BankCard;
import cn.xyz.mianshi.vo.PaidMerchant;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PaidMerchantManagerImpl extends MongoRepository<PaidMerchant, ObjectId> {
    @Override
    public Datastore getDatastore() {
        return SKBeanUtils.getDatastore();
    }

    @Override
    public Class<PaidMerchant> getEntityClass() {
        return PaidMerchant.class;
    }

    public PaidMerchant getRandomPaidMerchant(String payType) {
        List<PaidMerchant> paidMerchants = getPaidMerchants(payType);
        if (CollectionUtils.isEmpty(paidMerchants)) return null;
        Collections.shuffle(paidMerchants);
        return paidMerchants.get(0);
    }

    public PaidMerchant getPaidMerchantByVendorMerchantId(String vendorMerchantId) {
        Query<PaidMerchant> q = createQuery().filter("vendorMerchantId", vendorMerchantId);
        List<PaidMerchant> list = q.asList();
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    public PaidMerchant getPaidMerchantById(String id) {
        Query<PaidMerchant> q = createQuery().filter("_id", new ObjectId(id));
        List<PaidMerchant> list = q.asList();
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    public List<PaidMerchant> getPaidMerchants(String payType) {
        Query<PaidMerchant> q = createQuery().filter("payType", payType).filter("enable", 0);
        return q.asList();
    }

    public List<PaidMerchant> getForceAllPaidMerchants() {
        Query<PaidMerchant> q = createQuery().field("enable").notEqual(-1);
        return q.asList();
    }

    public void disablePaidMerchant(ObjectId id) throws BizException {
        PaidMerchant paidMerchant = get(id);
        if (null == paidMerchant)  {
            throw new BizException("禁用失败，商户不存在");
        }
        if (1 == paidMerchant.getEnable()) return;
        UpdateOperations<PaidMerchant> ops = createUpdateOperations();
        ops.set("enable", 1);
        updateAttributeByOps(id, ops);
    }

    public void enablePaidMerchant(ObjectId id) throws BizException {
        PaidMerchant paidMerchant = get(id);
        if (null == paidMerchant)  {
            throw new BizException("启用失败，商户不存在");
        }
        if (0 == paidMerchant.getEnable()) return;
        UpdateOperations<PaidMerchant> ops = createUpdateOperations();
        ops.set("enable", 0);
        updateAttributeByOps(id, ops);
    }

    public void delPaidMerchant(ObjectId id) {
        deleteById(id);
    }
}
