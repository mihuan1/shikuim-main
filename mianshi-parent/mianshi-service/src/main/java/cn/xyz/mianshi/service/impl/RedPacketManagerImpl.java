package cn.xyz.mianshi.service.impl;


import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.utils.*;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.ConsumeRecord;
import cn.xyz.mianshi.vo.RedPacket;
import cn.xyz.mianshi.vo.RedReceive;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;
import com.google.common.collect.Maps;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class RedPacketManagerImpl extends MongoRepository<RedPacket, ObjectId> {

    @Override
    public Datastore getDatastore() {
        return SKBeanUtils.getDatastore();
    }

    @Override
    public Class<RedPacket> getEntityClass() {
        return RedPacket.class;
    }

    private static UserManagerImpl getUserManager() {
        UserManagerImpl userManager = SKBeanUtils.getUserManager();
        return userManager;
    }


    public RedPacket saveRedPacket(RedPacket entity) {
        entity.setId(new ObjectId());
        save(entity);
        return entity;
    }

    public Object sendRedPacket(int userId, RedPacket packet) {
        packet.setUserName(SKBeanUtils.getUserManager().getNickName(userId));
        packet.setOver(packet.getMoney());
        long cuTime = DateUtil.currentTimeSeconds();
        packet.setSendTime(cuTime);
        packet.setOutTime(cuTime + KConstants.Expire.DAY1);
        Object data = saveRedPacket(packet);
        //修改金额
        Double balance = SKBeanUtils.getUserManager().rechargeUserMoeny(userId, packet.getMoney(), KConstants.MOENY_REDUCE);

        //开启一个线程 添加一条消费记录
        ThreadUtil.executeInThread(obj -> {
            String tradeNo = StringUtil.getOutTradeNo();
            //创建充值记录
            ConsumeRecord record = new ConsumeRecord();
            record.setUserId(userId);
            record.setToUserId(packet.getToUserId());
            record.setTradeNo(tradeNo);
            record.setMoney(packet.getMoney());
            record.setStatus(KConstants.OrderStatus.END);
            record.setType(KConstants.ConsumeType.SEND_REDPACKET);
            record.setPayType(KConstants.PayType.BALANCEAY); //余额支付
            record.setDesc("红包发送");
            record.setTime(DateUtil.currentTimeSeconds());
            record.setRedPacketId(packet.getId());
            record.setOperationAmount(packet.getMoney());
            record.setCurrentBalance(balance);
            SKBeanUtils.getConsumeRecordManager().save(record);
        });

        return data;
    }

    public synchronized JSONMessage getRedPacketById(Integer userId, ObjectId id) {
        RedPacket packet = get(id);
        Map<String, Object> map = Maps.newHashMap();
        map.put("packet", packet);
        //判断红包是否超时
        if (DateUtil.currentTimeSeconds() > packet.getOutTime()) {
            map.put("list", getRedReceiveListWithRemarkName(packet.getId(), userId));
            return JSONMessage.failureAndData("该红包已超过24小时!", map);
        }
		/*if(1==packet.getType()&&packet.getUserId().equals(userId)){
			map.put("list", getRedReceivesByRedId(packet.getId()));
			return JSONMessage.failureAndData(null, map); //你已经领过了 !
		}*/

        //判断红包是否已领完
        if (packet.getCount() > packet.getReceiveCount()) {
            //判断当前用户是否领过该红包
            if (null == packet.getUserIds() || !packet.getUserIds().contains(userId)) {
                map.put("list", getRedReceiveListWithRemarkName(packet.getId(), userId));
                return JSONMessage.success(null, map);
            } else {
                map.put("list", getRedReceiveListWithRemarkName(packet.getId(), userId));
                return JSONMessage.failureAndData(null, map); //你已经领过了 !
            }
        } else {//红包已经领完了
            map.put("list", getRedReceiveListWithRemarkName(packet.getId(), userId));
            return JSONMessage.failureAndData(null, map);
        }
    }

    public List<RedReceive> getRedReceiveListWithRemarkName(ObjectId redId, Integer loginUid) {
        List<RedReceive> redReceives = getRedReceivesByRedId(redId);
        redReceives.stream().filter(r -> !loginUid.equals(r.getUserId())).forEach(r -> {
            String remark = UserUtil.getRemarkName(loginUid, r.getUserId(), r.getUserName());
            r.setUserName(remark);
        });
        return redReceives;
    }

    public synchronized JSONMessage openRedPacketById(Integer userId, ObjectId id) {
        RedPacket packet = get(id);
        Map<String, Object> map = Maps.newHashMap();
        map.put("packet", packet);
        //判断红包是否超时
        if (DateUtil.currentTimeSeconds() > packet.getOutTime()) {
            map.put("list", getRedReceiveListWithRemarkName(packet.getId(), userId));
            return JSONMessage.failureAndData("该红包已超过24小时!", map);
        }
        //判断红包是否已领完
        if (packet.getCount() > packet.getReceiveCount()) {
            //判断当前用户是否领过该红包
            //
            if (null == packet.getUserIds() || !packet.getUserIds().contains(userId)) {
                packet = openRedPacket(userId, packet);
                map.put("packet", packet);
                map.put("list", getRedReceiveListWithRemarkName(packet.getId(), userId));
                return JSONMessage.success(null, map);
            } else {
                map.put("list", getRedReceiveListWithRemarkName(packet.getId(), userId));
                return JSONMessage.failureAndData(null, map); //你已经领过了 !
            }
        } else { //你手太慢啦  已经被领完了
            map.put("list", getRedReceiveListWithRemarkName(packet.getId(), userId));
            return JSONMessage.failureAndData("你手太慢啦  已经被领完了!", map);
        }
    }

    private synchronized RedPacket openRedPacket(Integer userId, RedPacket packet) {
        int overCount = packet.getCount() - packet.getReceiveCount();
        User user = getUserManager().getUser(userId);
        Double money = 0.0;
        //普通红包
        if (1 == packet.getType()) {
            if (1 == packet.getCount() - packet.getReceiveCount()) {
                //剩余一个  领取剩余红包
                money = packet.getOver();
            } else {
                money = packet.getMoney() / packet.getCount();
                //保留两位小数
                DecimalFormat df = new DecimalFormat("#.00");
                money = Double.valueOf(df.format(money));
            }
        } else  //拼手气红包或者口令红包
            money = getRandomMoney(overCount, packet.getOver());


        // 保留两位小数
        Double over = (packet.getOver() - money);
        DecimalFormat df = new DecimalFormat("#.00");
        packet.setOver(Double.valueOf(df.format(over)));
        packet.getUserIds().add(userId);
        UpdateOperations<RedPacket> ops = createUpdateOperations();
        ops.set("receiveCount", packet.getReceiveCount() + 1);
        ops.set("over", packet.getOver());
        ops.set("userIds", packet.getUserIds());
        if (0 == packet.getOver()) {
            ops.set("status", 2);
            packet.setStatus(2);
        }
        updateAttributeByOps(packet.getId(), ops);


        //实例化一个红包接受对象
        RedReceive receive = new RedReceive();
        receive.setMoney(money);
        receive.setUserId(userId);
        receive.setSendId(packet.getUserId());
        receive.setRedId(packet.getId());
        receive.setTime(DateUtil.currentTimeSeconds());
        receive.setUserName(getUserManager().getUser(userId).getNickname());
        receive.setSendName(getUserManager().getUser(packet.getUserId()).getNickname());
        ObjectId id = (ObjectId) getDatastore().save(receive).getId();
        receive.setId(id);

        //修改金额
        Double balance = getUserManager().rechargeUserMoeny(userId, money, KConstants.MOENY_ADD);
        final Double num = money;
        MessageBean messageBean = new MessageBean();
        messageBean.setType(KXMPPServiceImpl.OPENREDPAKET);
        messageBean.setFromUserId(user.getUserId().toString());
        messageBean.setFromUserName(UserUtil.getRemarkName(packet.getUserId(), user.getUserId(), user.getNickname()));
        if (packet.getRoomJid() != null) {
            messageBean.setObjectId(packet.getRoomJid());
            if (0 == packet.getOver()) {
                messageBean.setFileSize(1);
                messageBean.setFileName(packet.getSendTime() + "");
            }
        }
        messageBean.setContent(packet.getId().toString());
        messageBean.setToUserId(packet.getUserId() + "");
        messageBean.setMsgType(0);// 单聊消息
        messageBean.setMessageId(StringUtil.randomUUID());
        try {
            KXMPPServiceImpl.getInstance().send(messageBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //开启一个线程 添加一条消费记录
        ThreadUtil.executeInThread(obj -> {
            String tradeNo = StringUtil.getOutTradeNo();
            //创建充值记录
            ConsumeRecord record = new ConsumeRecord();
            record.setUserId(userId);
            record.setToUserId(packet.getUserId());
            record.setTradeNo(tradeNo);
            record.setMoney(num);
            record.setStatus(KConstants.OrderStatus.END);
            record.setType(KConstants.ConsumeType.RECEIVE_REDPACKET);
            record.setPayType(KConstants.PayType.BALANCEAY); //余额支付
            record.setDesc("红包接受");
            record.setOperationAmount(num);
            record.setCurrentBalance(balance);
            record.setRedPacketId(packet.getId());
            record.setTime(DateUtil.currentTimeSeconds());
            SKBeanUtils.getConsumeRecordManager().save(record);
        });
        return packet;
    }

    //发送领取红包消息  即 添加消费记录
    public void sendOpenMessageAndCreateRecord() {

    }

    private synchronized Double getRandomMoney(int remainSize, Double remainMoney) {
        // remainSize 剩余的红包数量
        // remainMoney 剩余的钱
        Double money = 0.0;
        if (remainSize == 1) {
            remainSize--;
            money = (double) Math.round(remainMoney * 100) / 100;
            System.out.println("=====> " + money);
            return money;
        }
        Random r = new Random();
        double min = 0.01; //
        double max = remainMoney / remainSize * 2;
        money = r.nextDouble() * max;
        money = money <= min ? 0.01 : money;
        money = Math.floor(money * 100) / 100;
        System.out.println("=====> " + money);
        remainSize--;
        remainMoney -= money;
        DecimalFormat df = new DecimalFormat("#.00");
        return Double.valueOf(df.format(money));
    }

    public void replyRedPacket(String id, String reply) {
        Integer userId = ReqUtil.getUserId();
        Query query = getDatastore().createQuery(RedReceive.class).field("userId").equal(userId);
        query.filter("redId", new ObjectId(id));
        UpdateOperations<RedReceive> operations = getDatastore().createUpdateOperations(RedReceive.class);
        operations.set("reply", reply);
        getDatastore().update(query, operations);
    }

    //根据红包Id 获取该红包的领取记录
    public synchronized List<RedReceive> getRedReceivesByRedId(ObjectId redId) {
        return (List<RedReceive>) getEntityListsByKey(RedReceive.class, "redId", redId, "-time");
    }

    //发送的红包
    public List<RedPacket> getSendRedPacketList(Integer userId, int pageIndex, int pageSize) {
        Query<RedPacket> q = createQuery().field("userId").equal(userId);
        return q.order("-sendTime").offset(pageIndex * pageSize).limit(pageSize).asList();
    }

    //收到的红包
    public List<RedReceive> getRedReceiveList(Integer userId, int pageIndex, int pageSize) {
        return (List<RedReceive>) getEntityListsByKey(RedReceive.class, "userId", userId, "-time", pageIndex, pageSize);
    }


    //发送的红包
    public PageResult<RedPacket> getRedPacketList(String userName, int pageIndex, int pageSize, String redPacketId) {
        PageResult<RedPacket> result = new PageResult<>();
        Query<RedPacket> q = createQuery().order("-sendTime");
        if (!StringUtil.isEmpty(userName))
            q.field("userName").equal(userName);
        if (!StringUtil.isEmpty(redPacketId))
            q.field("_id").equal(new ObjectId(redPacketId));
        result.setCount(q.count());
        result.setData(q.asList(pageFindOption(pageIndex, pageSize, 1)));
        return result;
    }

    //发送的红包
    public PageResult<RedReceive> receiveWater(String redId, int pageIndex, int pageSize) {
        PageResult<RedReceive> result = new PageResult<>();
        Query<RedReceive> q = getDatastore().createQuery(RedReceive.class).field("redId").equal(new ObjectId(redId)).order("-time");
        result.setCount(q.count());
        result.setData(q.asList(pageFindOption(pageIndex, pageSize, 1)));
        return result;
    }
}
