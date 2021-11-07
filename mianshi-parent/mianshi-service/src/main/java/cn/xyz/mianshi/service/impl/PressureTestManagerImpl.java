package cn.xyz.mianshi.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.types.ObjectId;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration.Builder;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import cn.xyz.commons.autoconfigure.KApplicationProperties.XMPPConfig;
import cn.xyz.commons.utils.NumberUtil;
import cn.xyz.commons.utils.RandomUtil;
import cn.xyz.mianshi.model.PressureParam;
import cn.xyz.mianshi.model.PressureThread;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MyConnectionListener;
import lombok.extern.slf4j.Slf4j;

/**
 * @version:（1.0）
 * @ClassName PressureTest
 * @Description: （压力测试）
 * @date:2018年11月13日下午5:46:54
 */
@Slf4j
@Service
public class PressureTestManagerImpl {

    @Autowired(required = false)
    private XMPPConfig xmppConfig;

    private Map<String, List<XMPPTCPConnection>> userConnMap = Maps.newConcurrentMap();

    Builder builder = null;

    private int runStatus = 0;//任务 运行状态   0  无任务   1  运行中

    final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private Builder InitializeBuilder() {
        if (null == builder) {
            try {
                builder = XMPPTCPConnectionConfiguration.builder()
                        .setSecurityMode(SecurityMode.ifpossible)
                        .setCompressionEnabled(true)
                        .setSendPresence(true)
                        .setXmppDomain(xmppConfig.getServerName())
                        .setHost(xmppConfig.getHost())
                        .setPort(5222);

//				    .build();
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
			/*XMPPTCPConnection.setUseStreamManagementDefault(false);
			XMPPTCPConnection.setUseStreamManagementResumptiodDefault(false);
			XMPPTCPConnection.setUseStreamManagementResumptionDefault(false);*/
        }
        return builder;
    }


    /**
     *	1. 群内生成1000个用户
     *
     *  2. 模拟四百个用户发送消息   ===》 机器人  不够自动创建
     *
     *  3. 可以 发送总条数、每秒条数
     */


    /**
     * @param checkNum
     * @param jids
     * @Description:（创建一定数量的机器人）
     **/
    public void createRobot(int checkNum, List<String> jids) {
        // 筛选群内离线的人，不够checkNum则创建机器人
        for (String jid : jids) {
            List<Integer> offlineUsers = new ArrayList<Integer>();
            ObjectId roomId = SKBeanUtils.getRoomManager().getRoomId(jid);
            List<Integer> memberIds = SKBeanUtils.getRoomManager().getCommonMemberIdList(roomId);
            int createNum = (int) (checkNum - memberIds.size());
            if (createNum > 0) {
                List<Integer> addRobots = SKBeanUtils.getUserManager().addRobot(createNum, true, roomId);
                offlineUsers.addAll(memberIds);
                offlineUsers.addAll(addRobots);
                log.info("群：" + jid + "   需要创建机器人的个数：" + createNum);
            } else {
                /**
                 * 人数 大于 三倍 随机取
                 */
                if (3 < (memberIds.size() / checkNum)) {
                    for (int i = 0; i < checkNum; i++) {
                        Integer num = NumberUtil.getRandomByMinAndMax(1, memberIds.size());
                        while (offlineUsers.contains(num)) {
                            num = NumberUtil.getRandomByMinAndMax(1, memberIds.size());
                        }
                        offlineUsers.add(memberIds.get(num - 1));
                        memberIds.remove(num);
                    }
                } else {
                    offlineUsers.addAll(memberIds);
                }
            }
            // 人数足够 checkNum 后生成xmpp conn
            List<XMPPTCPConnection> connList = Collections.synchronizedList(new ArrayList<>());
            int i = 0;
            for (Integer userId : offlineUsers) {
                i++;
                Object pwd = SKBeanUtils.getUserManager().queryOneFieldById("password", userId);
                XMPPTCPConnection conn = getConnection(userId.toString(), pwd.toString());
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (null != conn && conn.isAuthenticated())
                    connList.add(conn);
                if (i >= checkNum)
                    break;
            }
            System.out.println("群：" + jid + "   选中的conn：" + connList.toString());
            userConnMap.put(jid, connList);
        }
    }


    public PressureParam.PressureResult mucTest(PressureParam param) {
        try {
            if (1 == runStatus) {
                log.error("已有压测 任务 运行中  请稍后 请求 。。。。。。");
                return null;
            }
            System.out.println("压力测试：" + " roomJids: " + JSONObject.toJSONString(param.getJids()) + " checkNum: " + param.getCheckNum() + " sendMsgNum: " + param.getSendMsgNum()
                    + "  消息时间间隔:" + param.getTimeInterval());
            runStatus = 1;
            closeConnection();
            param.setAtomic(new AtomicInteger(0));

            String format = new SimpleDateFormat("MM-dd HH:mm").format(System.currentTimeMillis());

            param.setTimeStr(format);
            createRobot(param.getCheckNum(), param.getJids());
            List<MultiUserChat> mucChats = null;
            List<XMPPTCPConnection> connList = null;
            List<XMPPTCPConnection> conns = null;

            List<PressureThread> threads = Collections.synchronizedList(new ArrayList<>());
            for (String jid : param.getJids()) {
                connList = userConnMap.get(jid);
                conns = Collections.synchronizedList(new ArrayList<>());
                for (int i = 0; i < param.getCheckNum(); i++) {
                    // 每个群组中的发送人数
                    if (connList.size() > i)
                        conns.add(connList.get(i));
                }
                mucChats = Collections.synchronizedList(new ArrayList<>());
                for (XMPPTCPConnection conn : conns) {
                    String strJid = jid + getMucChatServiceName(conn);

                    MultiUserChat muc = null;
                    try {

                        MultiUserChatManager muChatManager = MultiUserChatManager.getInstanceFor(conn);
                        muc = muChatManager.getMultiUserChat(JidCreate.entityBareFrom(strJid));
                        muc.join(Resourcepart.from(conn.getUser().getLocalpart().toString()));
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        try {
                            muc.join(Resourcepart.from(conn.getUser().getLocalpart().toString()));
                        } catch (Exception e2) {
                            System.err.println(e2.getMessage());
                        }
                    }
                    if (null != muc && null != muc.getNickname())
                        mucChats.add(muc);
                }
                threads.add(new PressureThread(jid, param, mucChats));

            }
            param.setStartTime(System.currentTimeMillis());// 开始时间

            Set<ScheduledFuture> threadFutures = new HashSet<ScheduledFuture>();
            threads.forEach(th -> {
                ScheduledFuture<?> scheduledFuture = threadPool.scheduleAtFixedRate(th, 1000, param.getTimeInterval(), TimeUnit.MILLISECONDS);
                threadFutures.add(scheduledFuture);
            });

            PressureParam.PressureResult result = null;
            while (runStatus == 1) {
                if (param.getAtomic().get() >= param.getSendAllCount()) {
                    try {
                        //threadPool.shutdown();

                        threadFutures.stream().forEach(th -> {
                            th.cancel(false);
                        });

                        param.setConns(null);
                        result = new PressureParam.PressureResult();
                        result.setTimeCount((System.currentTimeMillis() - param.getStartTime()) / 1000);
                        result.setSendAllCount(param.getSendAllCount());
                        result.setTimeStr(param.getTimeStr());
                        log.info("任务执行完毕 ：" + JSONObject.toJSONString(param));

                        runStatus = 2;
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                } else {
                    Thread.sleep(1000);
                }
            }
            runStatus = 0;
            return result;
        } catch (Exception e) {
            runStatus = 0;
            return new PressureParam.PressureResult();
        }
		
		/*try {
			Thread.sleep(60000);
		} catch (Exception e) {
			e.printStackTrace();
		}*/


    }


    private void closeConnection() {
        if (0 == userConnMap.size())
            return;
        for (String jid : userConnMap.keySet()) {
            List<XMPPTCPConnection> list = userConnMap.get(jid);
            for (XMPPTCPConnection conn : list) {
                conn.disconnect();
            }
        }
        userConnMap = Maps.newConcurrentMap();
    }


    public String getMucChatServiceName(XMPPTCPConnection connection) {
        return "@muc." + connection.getXMPPServiceDomain();
    }


    private XMPPTCPConnectionConfiguration getConfig() {
        XMPPTCPConnectionConfiguration config = null;
        Builder builder = InitializeBuilder();
        if (null != builder) {
            try {
                config = builder.setResource(RandomUtil.getRandomEnAndNum(5))
                        //.setCompressionEnabled(true)
                        //.setResource("Smack")
                        .build();
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }

        }

        return config;
    }


    public XMPPTCPConnection getConnection(String username, String password) {
        XMPPTCPConnection connection = null;
        try {

            connection = new XMPPTCPConnection(getConfig());
            connection.setReplyTimeout(30000);
            connection.connect();
            connection.login(username, password);
            connection.addConnectionListener(new MyConnectionListener(connection, false));

        } catch (SmackException e) {
            log.info(" =====》     XMPP超时异常：" + username);
//			e.printStackTrace();
            return null;
        } catch (Exception e) {
            if (e instanceof SASLErrorException) {
                log.info(" ==== 》XMPP认证异常");
                DBCollection collection = SKBeanUtils.getTigaseDatastore().getDB().getCollection("tig_users");
                BasicDBObject query = new BasicDBObject();
                String userId = username + "@" + xmppConfig.getServerName();
                query.put("user_id", userId);
                if (null == collection.findOne(query)) {
                    KXMPPServiceImpl.getInstance().registerAndXmppVersion(username, password);
                }
            }
//			e.printStackTrace();
            return null;
        }
        return connection;
    }


}
