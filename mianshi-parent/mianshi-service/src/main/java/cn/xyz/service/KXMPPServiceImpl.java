package cn.xyz.service;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaIdFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.iqregister.packet.Registration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.mongodb.morphia.Datastore;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import cn.xyz.commons.autoconfigure.KApplicationProperties.MQConfig;
import cn.xyz.commons.autoconfigure.KApplicationProperties.XMPPConfig;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.utils.Md5Util;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;

@Component(value = "xmppService")
public class KXMPPServiceImpl implements ApplicationContextAware {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(KXMPPServiceImpl.class);

    private List<String> sysUserList = Collections.synchronizedList(new ArrayList<String>());


    private DefaultMQProducer chatProducer;

    @Autowired(required = false)
    private MQConfig mqConfig;

    //收红包
    //{
    //  "type":83
    //	"fromUserId":""
    //	"fromUserName":""
    //	"ObjectId":"如果是群聊，则为房间Id"
    //	"timeSend":123
    public static final int OPENREDPAKET = 83;

    // 红包退款
    // {
    //	 "type":86
    //   "fromUserId":""
    //   "fromUserName":""
    // 	 "ObjectId":"如果是群聊，则为房间Id"
    //	 "timeSend":123
    public static final int RECEDEREDPAKET = 86;

    // 转账收款
    // {
    //    "type":88
    //    "fromUserId":""
    //    "fromUserName":""
    //    "ObjectId":""
    //    "timeSend":123
    public static final int RECEIVETRANSFER = 88;

    // 转账退回
    // {
    //    "type":89
    //    "fromUserId":""
    //    "fromUserName":""
    //    "ObjectId":""
    //    "timeSend":123
    public static final int REFUNDTRANSFER = 89;

    // 付款码已付款通知
    // {
    //    "type":90
    //    "fromUserId":""
    //    "fromUserName":""
    //	  "ObjectId":""
    //    "timeSend":123
    public static final int CODEPAYMENT = 90;

    // 付款码已到账通知
    // {
    //    "type":91
    //    "fromUserId":""
    //    "fromUserName":""
    //	  "ObjectId":""
    //    "timeSend":123
    public static final int CODEARRIVAL = 91;

    // 二维码收款已付款通知
    // {
    //    "type":91
    //    "fromUserId":""
    //    "fromUserName":""
    //	  "ObjectId":""
    //    "timeSend":123
    public static final int CODERECEIPT = 92;

    // 二维码收款已到账通知
    // {
    //    "type":93
    //    "fromUserId":""
    //    "fromUserName":""
    //	  "ObjectId":""
    //    "timeSend":123
    public static final int CODEERECEIPTARRIVAL = 93;

    // 第三方应用调取IM支付成功通知
    public static final int OPENPAYSUCCESS = 97;

    //上传文件
    //{
    //"type":401,
    //"content":"文件名",
    //"fromUserId":"上传者",
    //"fromUserName":"",
    //"ObjectId":"文件Id"
    //"timeSend":123
    //}
    public static final int FILEUPLOAD = 401;

    //删除文件
    //{
    //"type":402,
    //"content":"文件名",
    //"fromUserId":"删除者",
    //"fromUserName":"",
    //"ObjectId":"文件Id",
    //"timeSend":123
    //}
    public static final int DELETEFILE = 402;

    /**
     * 后台删除好友（客户端自己封装的xmpp，这里用于后台的用户管理删除好友）
     * {
     * fromUserId:10005
     * "type":515
     * "objectId": 封装 fromUserId  toUserId 用于接收系统号发送xmpp消息的用户
     * }
     */
    public static final int deleteFriends = 515;

    /**
     * 后台加入黑名单（客户端自己封装的xmpp，这里用于后台的用户管理中的加入黑名单）
     * {
     * fromUserId:10005
     * "type":513
     * "objectId": 封装 fromUserId  toUserId 用于接收系统号发送xmpp消息的用户
     * }
     */
    public static final int joinBlacklist = 513;

    /**
     * 后台移除黑名单（客户端自己封装的xmpp，这里用于后台的用户管理中的移除黑名单）
     * {
     * fromUserId:10005
     * "type":514
     * "objectId": 封装 fromUserId  toUserId 用于接收系统号发送xmpp消息的用户
     * }
     */
    public static final int moveBlacklist = 514;

    /**
     * 通讯录批量添加好友
     * {
     * fromUserId:我的新通讯录好友
     * "type":510
     * "toUserId": 我
     * }
     */
    public static final int batchAddFriend = 510;

    /**
     * 用户注册后更新通讯录好友
     * {
     * fromUserId:我的新通讯录好友
     * "type":511
     * "toUserId": 我
     * }
     */
    public static final int registAddressBook = 511;

    /**
     * 后台删除用户用于客户端更新本地数据
     * {
     * fromUserId:系统用户
     * "type":512
     * "toUserId": 被删除用户的所有好友Id
     * "objectId" ： 被删除人的id
     * }
     */
    public static final int consoleDeleteUsers = 512;


    /**
     * 多点登录用户相关操作用于同步数据
     * {
     * fromUserId:自己
     * "type":800
     * "toUserId": 自己
     * "other": 0：修改密码，1：设置支付密码，2：用户隐私设置
     * }
     */
    public static final int multipointLoginDataSync = 800;

    /**
     * 多点登录更新个人资料
     * {
     * fromUserId:自己
     * "type":801
     * "toUserId": 自己
     * }
     */
    public static final int updatePersonalInfo = 801;


    /**
     * 多点登录更新群组相关信息
     * {
     * fromUserId:自己
     * "type":801
     * "toUserId": 自己
     * "objectId": "房间Id",
     * }
     */
    public static final int updateRoomInfo = 802;

    // 修改昵称
    // {
    // "type": 901,
    // "objectId": "房间Id",
    // "fromUserId": 10005,
    // "fromUserName": "10005",
    // "toUserId": 用户Id,
    // "toUserName": "用户昵称",
    // "timeSend": 123
    // }
    public static final int CHANGE_NICK_NAME = 901;

    // 修改房间名
    // {
    // "type": 902,
    // "objectId": "房间Id",
    // "content": "房间名",
    // "fromUserId": 10005,
    // "fromUserName": "10005",
    // "timeSend": 123
    // }
    public static final int CHANGE_ROOM_NAME = 902;

    // 删除成员
    // {
    // "type": 904,
    // "objectId": "房间Id",
    // "fromUserId": 0,
    // "fromUserName": "",
    // "toUserId": 被删除成员Id,
    // "timeSend": 123
    // }
    public static final int DELETE_MEMBER = 904;
    // 删除房间
    // {
    // "type": 903,
    // "objectId": "房间Id",
    // "content": "房间名",
    // "fromUserId": 10005,
    // "fromUserName": "10005",
    // "timeSend": 123
    // }
    public static final int DELETE_ROOM = 903;
    // 禁言
    // {
    // "type": 906,
    // "objectId": "房间Id",
    // "content": "禁言时间",
    // "fromUserId": 10005,
    // "fromUserName": "10005",
    // "toUserId": 被禁言成员Id,
    // "toUserName": "被禁言成员昵称",
    // "timeSend": 123
    // }
    public static final int GAG = 906;
    // 新成员
    // {
    // "type": 907,
    // "objectId": "房间Id",
    // "fromUserId": 邀请人Id,
    // "fromUserName": "邀请人昵称",
    // "toUserId": 新成员Id,
    // "toUserName": "新成员昵称",
    // "content":"是否显示阅读人数",  1:开启  0：关闭
    // "timeSend": 123
    // }
    public static final int NEW_MEMBER = 907;
    // 新公告
    // {
    // "type": 905,
    // "objectId": "房间Id",
    // "content": "公告内容",
    // "fromUserId": 10005,
    // "fromUserName": "10005",
    // "timeSend": 123
    // }
    public static final int NEW_NOTICE = 905;
    //用户离线
    //
    //{
    // "type": 908,
    // "userId":"用户ID"
    // "name":"用户昵称"
    // "coment":"用户离线"
    //}
    public static final int OFFLINE = 908;
    //用户上线
    //{
    // "type": 909,
    // "userId":"用户ID"
    // "name":"用户昵称"
    // "coment":"用户上线"
    //}
    public static final int ONLINE = 909;

    //弹幕
    //{
    //	"type":910,
    //	"formUserId":"用户ID"
    //	"fromUserName":"用户昵称"
    //	"content":"弹幕内容"
    //	"timeSend": 123
    //}
    public static final int BARRAGE = 910;

    //送礼物
    //{
    //	"type":911
    //	"fromUserId":"用户ID"
    //	"fromUserName":"用户昵称"
    //	"content":"礼物"
    //	"timeSend":123
    //}
    public static final int GIFT = 911;

    //直播点赞
    //{
    //	"type":912
    //	}
    public static final int LIVEPRAISE = 912;

    //设置管理员
    //{
    //	"type":913
    //	"fromUserId":"发送者Id"
    //	"fromUserName":"发送者昵称"
    //	"content":"1为启用  0为取消管理员"
    // 	"timeSend":123
    //}
    public static final int SETADMIN = 913;

    //进入直播间
    // {
    //	"type":914
    //	"fromUserId":"发送者Id"
    //	"fromUserName":"发送者昵称"
    //	"objectId":"房间的JID"
    //	"timeSend":123
    //}
    public static final int JOINLIVE = 914;


    /**
     * 显示阅读人数
     * {
     * "type":915
     * "objectId":"房间JId"
     * "content":"是否显示阅读人数" 1：开启 2：关闭
     * }
     */
    public static final int SHOWREAD = 915;

    /**
     * 群组是否需要验证
     * {
     * "type":916
     * "objectId":"房间JId"
     * "content": 1：开启验证   0：关闭验证
     * }
     */
    public static final int RoomNeedVerify = 916;

    /**
     * 房间是否公开
     * {
     * "type":917
     * "objectId":"房间JId"
     * "content": 1：不公开 隐私群   0：公开
     * }
     */
    public static final int RoomIsPublic = 917;

    /**
     * 普通成员 是否可以看到 群组内的成员
     * 关闭 即普通成员 只能看到群主
     * {
     * "type":918
     * "objectId":"房间JId"
     * "content": 1：可见   0：不可见
     * }
     */
    public static final int RoomShowMember = 918;
    /**
     * 群组允许发送名片
     * {
     * "type":919
     * "objectId":"房间JId"
     * "content": 1：   允许发送名片   0：不允许发送
     * }
     */
    public static final int RoomAllowSendCard = 919;

    /**
     * 群组全员禁言
     * {
     * "type":920
     * "objectId":"房间JId"
     * "content": tailTime   禁言截止时间
     * }
     */
    public static final int RoomAllBanned = 920;

    /**
     * 群组允许成员邀请好友
     * {
     * "type":921
     * "objectId":"房间JId"
     * "content": 1：  允许成员邀请好友   0：不允许成员邀请好友
     * }
     */
    public static final int RoomAllowInviteFriend = 921;

    /**
     * 群组允许成员上传群共享文件
     * {
     * "type":922
     * "objectId":"房间JId"
     * "content": 1：  允许成员上传群共享文件   0：不允许成员上传群共享文件
     * }
     */
    public static final int RoomAllowUploadFile = 922;
    /**
     * 群组允许成员召开会议
     * <p>
     * {
     * "type":923
     * "objectId":"房间JId"
     * "content": 1：  允许成员召开会议   0：不允许成员召开会议
     * }
     */
    public static final int RoomAllowConference = 923;

    /**
     * 群组允许成员开启 讲课
     * {
     * "type":924
     * "objectId":"房间JId"
     * "content": 1：  允许成员开启 讲课   0：不允许成员开启 讲课
     * }
     */
    public static final int RoomAllowSpeakCourse = 924;
    /**
     * 群组转让 接口
     * {
     * fromUserId:旧群主ID
     * "type":925
     * "objectId":"房间JId"
     * "toUserId": 新群组用户ID
     * }
     */
    public static final int RoomTransfer = 925;

    /**
     * 房间是否锁定
     * {
     * "type":926
     * "objectId":"房间JId"
     * "content": 1：锁定房间   0：解锁房间
     * }
     */
    public static final int RoomDisable = 926;

    /**
     * 直播间中退出、被踢出直播间
     * {
     * "type":927
     * "objectId":"房间JId"
     * "content": 退出被踢出直播间
     * }
     */
    public static final int LiveRoomSignOut = 927;

    /**
     * 直播间中的禁言、取消禁言
     * {
     * "type":928
     * "objectId":"房间JId"
     * "content": 0：禁言，1：取消禁言
     * }
     */
    public static final int LiveRoomBannedSpeak = 928;

    /**
     * 直播间中设置、取消管理员
     * {
     * "type":929
     * "objectId":"房间JId"
     * "content": 0:设置管理员  1:取消管理员
     * }
     */
    public static final int LiveRoomSettingAdmin = 929;

    /**
     * 群组中设置 隐身人和监控人
     * {
     * "type":930
     * "objectId":"房间JId"
     * "content": 1:设置隐身人  -1:取消隐身人，2：设置监控人，0：取消监控人
     * }
     */
    public static final int SetRoomSettingInvisibleGuardian = 930;

    /**
     * 后台锁定、取消锁定群组
     * {
     * fromUserId:系统用户
     * "type":931
     * "content":1：解锁，-1：锁定
     * "objectId" ： roomJid
     * }
     */
    public static final int consoleProhibitRoom = 931;

    /**
     * 聊天记录超时设置
     * {
     * "type":932
     * "objectId":"房间JId"
     * "content": 1.0:保存一天  -1:永久保存  365.0保存一年
     * }
     */
    public static final int ChatRecordTimeOut = 932;

    /**
     * {
     * "type":933
     * "objectId":"房间JId"
     * "content": 1
     * }
     */
    public static final int LocationRoom = 933;

    /**
     * 修改群公告
     * {
     * "type":934
     * "objectId":"房间JId"
     * "content": notice
     * }
     */
    public static final int ModifyNotice = 934;


    //点赞
    //{
    //	"type":301
    //
    //
    //}
    public static final int PRAISE = 301;
    //评论
    //{
    //	"type":302
    //}
    public static final int COMMENT = 302;

    //朋友圈的提醒
    //{
    //"type":304
    //}
    public static final int REMIND = 304;

    private static ApplicationContext context;

    private static final Logger log = Logger.getLogger(KXMPPServiceImpl.class
            .getName());

    public static KXMPPServiceImpl getInstance() {
        return context.getBean(KXMPPServiceImpl.class);
    }


    @Autowired(required = false)
    private XMPPConfig xmppConfig;

    @Autowired(required = false)
    private Datastore dsForTigase;

    private XMPPTCPConnection connection;

    private XMPPTCPConnectionConfiguration config;

    /**
     * 获取生产者
     *
     * @return
     */
    public DefaultMQProducer getChatProducer() {
        if (null != chatProducer)
            return chatProducer;

        try {
            chatProducer = new DefaultMQProducer("xmppProducer");
            chatProducer.setNamesrvAddr(mqConfig.getNameAddr());
            chatProducer.setVipChannelEnabled(false);
            chatProducer.setCreateTopicKey("xmppMessage");
            chatProducer.setSendMsgTimeout(30000);

            chatProducer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chatProducer;
    }

    /**
     * 发送单聊消息
     *
     * @param messageBean
     */
    public void send(MessageBean messageBean) {
        DefaultMQProducer producer = getChatProducer();
        org.apache.rocketmq.common.message.Message message = null;
        if (StringUtil.isEmpty(messageBean.getMessageId())) {
            messageBean.setMessageId(StringUtil.randomUUID());
        }
        try {
            message = new org.apache.rocketmq.common.message.Message("xmppMessage", messageBean.toString().getBytes());
            SendResult result = producer.send(message);
            if (SendStatus.SEND_OK != result.getSendStatus()) {
                System.out.println("发送失败   " + result.toString());
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送单聊消息
     *
     * @param messageBean
     * @param userIdList
     */
    public void send(MessageBean messageBean, List<Integer> userIdList) {
        DefaultMQProducer producer = getChatProducer();
        org.apache.rocketmq.common.message.Message message = null;
        if (StringUtil.isEmpty(messageBean.getMessageId())) {
            messageBean.setMessageId(StringUtil.randomUUID());
        }

        for (Integer i : userIdList) {
            messageBean.setToUserId(i.toString());
            messageBean.setToUserName(SKBeanUtils.getUserManager().getNickName(i));
            messageBean.setMsgType(0);// 单聊消息
            send(messageBean);
        }
    }


    private synchronized XMPPTCPConnectionConfiguration getConfig() {

        if (null == config) {
            SmackConfiguration.setDefaultReplyTimeout(15000);
            AccountManager.sensitiveOperationOverInsecureConnectionDefault(true);
            PingManager.setDefaultPingInterval(10);
            try {
                config = XMPPTCPConnectionConfiguration.builder()
                        .setSecurityMode(SecurityMode.ifpossible)
                        .setCompressionEnabled(true)
                        .setSendPresence(false)
                        .setXmppDomain(xmppConfig.getServerName())
                        .setHostAddress(InetAddress.getByName(xmppConfig.getHost()))
                        .setPort(xmppConfig.getPort())
                        .setResource("Smack")
                        .build();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return config;
    }

    /**
     * admin xmpp
     *
     * @return
     */
    public synchronized XMPPTCPConnection getAdminConnection() {
        XMPPTCPConnection conn = null;
        try {
            conn = new XMPPTCPConnection(getConfig());
            conn.connect();
        } catch (Exception e) {
            e.printStackTrace();// TODO: handle exception
        }

        return conn;
    }

    public void register() {
        XMPPTCPConnection conn = null;
        try {
            conn = getConnection();
            if (!conn.isConnected()) {
                conn.connect();
            }
            Map<String, String> attributes = new HashMap<>();
            attributes.put("username", xmppConfig.getUsername());
            attributes.put("password", Md5Util.md5Hex(xmppConfig.getPassword()));
            Registration reg = new Registration(attributes);
            reg.setType(IQ.Type.set);
            reg.setTo(conn.getXMPPServiceDomain());
            Stanza nextResultOrThrow = conn.createStanzaCollectorAndSend(new StanzaIdFilter(reg.getStanzaId()), reg).nextResultOrThrow();
            System.out.println(nextResultOrThrow.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        closedConnection(conn);
    }

    /*
    public boolean createXmppAccount(String username,String password) {
        XMPPTCPConnection conn = getConnection();
        AccountManager accountManager = getAccountManager(conn);

        Localpart localpart=null;

        try {
            localpart = Localpart.from(username);
            accountManager.createAccount(localpart, password);

            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }*/
    public boolean createXmppAccountWithV8(String username, String password) {
        XMPPTCPConnection conn = null;
        try {
            conn = getAdminConnection();
            Map<String, String> attributes = new HashMap<>();
            attributes.put("username", username);
            attributes.put("password", password);
            Registration reg = new Registration(attributes);
            reg.setType(IQ.Type.set);
            reg.setTo(conn.getXMPPServiceDomain());

            Stanza nextResultOrThrow = conn.createStanzaCollectorAndSend(new StanzaIdFilter(reg.getStanzaId()), reg).nextResultOrThrow();
            System.out.println("createXmppAccount  " + username + "   " + nextResultOrThrow.toString());
            closedConnection(conn);
            return true;
        } catch (Exception e) {
            closedConnection(conn);
            System.err.println("createXmppAccount  " + username + "   " + e.getMessage());

            return false;
        }
    }

    public synchronized XMPPTCPConnection createConnection() throws Exception {

        if (null == connection) {
            connection = new XMPPTCPConnection(getConfig());
            //connection.setFromMode(FromMode.USER);
            connection.connect();
            try {
                //examineTigaseUser(xmppConfig.getUsername(), DigestUtils.md5Hex(xmppConfig.getPassword()),1);
                connection.login(xmppConfig.getUsername(), DigestUtils.md5Hex(xmppConfig.getPassword()));
            } catch (XMPPException e) {
                //登陆失败 可能是系统 账号不存在  重新注册
                register();
                connection.login(xmppConfig.getUsername(), DigestUtils.md5Hex(xmppConfig.getUsername()));
            }

            /** 设置状态 */
	       /* Presence presence = new Presence(Presence.Type.available);  
	        presence.setStatus("Q我吧");  
	        connection.sendStanza(presence);*/
            connection.addConnectionListener(new MyConnectionListener(connection, true));
        }
        return connection;
    }

    public XMPPTCPConnection getConnection() {
        try {
            if (null == connection)
                connection = createConnection();
            if (!connection.isConnected()) {
                connection.connect();
                connection.login(xmppConfig.getUsername(), Md5Util.md5Hex(xmppConfig.getPassword()));
            }
            if (!connection.isAuthenticated())
                connection.login(xmppConfig.getUsername(), Md5Util.md5Hex(xmppConfig.getPassword()));

        } catch (Exception e) {
            e.printStackTrace();// TODO: handle exception
        }

        return connection;
    }

    public XMPPTCPConnection getConnection(String username, String password) {
        XMPPTCPConnection conn = null;
        try {
            log.info("try-getConnection-1");
            examineTigaseUser(username, password);
            log.info("try-getConnection-2");
            conn = new XMPPTCPConnection(getConfig());
            log.info("try-getConnection-3");
            //conn.setFromMode(FromMode.USER);
            conn.connect();
            log.info("try-getConnection-4");
            conn.login(username, password);
            log.info("try-getConnection-5");
            conn.addConnectionListener(new MyConnectionListener(conn, true));
            log.info("try-getConnection-6");
        } catch (Exception e) {
            log.info("try-getConnection-7");
            log.info(e.getMessage());
            registerAndXmppVersion(username, password);
        }

        log.info("try-getConnection-8");
        return conn;
    }


    public void registerSystemNo(String userId, String password) throws Exception {
        DBCollection collection = dsForTigase.getDB().getCollection("tig_users");
        String user_id = userId + "@" + xmppConfig.getServerName();
        BasicDBObject query = new BasicDBObject("user_id", user_id);
        if (null != collection.findOne(query)) {
            System.out.println(userId + "  已经注册了!");
            return;
        }

        registerAndXmppVersion(userId, password);

        System.out.println("  注册到 Tigase  " + xmppConfig.getServerName() + "," + userId + "," + password);

    }

    public void registerAndXmppVersion(String userId, String password) {
        log.info("registerAndXmppVersion--------");
        DBCollection collection = dsForTigase.getDB().getCollection("tig_users");
        String user_id = userId + "@" + xmppConfig.getServerName();
        BasicDBObject query = new BasicDBObject("user_id", user_id);
        if (null != collection.findOne(query)) {
            log.info("registerAndXmppVersion===========");
            System.out.println(userId + "  已经注册了!");
            return;
        }
        try {
            BasicDBObject jo = new BasicDBObject();
            jo.put("_id", generateId(user_id));
            jo.put("user_id", user_id);
            jo.put("domain", xmppConfig.getServerName());
            jo.put("password", password);
            jo.put("type", "shiku");
            collection.save(jo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("  注册到 Tigase  " + xmppConfig.getServerName() + "," + userId + "," + password);
    }

    public void registerByThread(String userId, String password) throws Exception {
        ThreadUtil.executeInThread(new Callback() {

            @Override
            public void execute(Object obj) {
                KXMPPServiceImpl.getInstance().registerAndXmppVersion(userId, password);
            }
        });
    }

    @Deprecated()
    public void updateToTig(String userId, String password) {
        log.info("进入 updateToTig.....");
        try {
            String user_id = userId + "@" + xmppConfig.getServerName();

            BasicDBObject q = new BasicDBObject();
            q.put("_id", generateId(user_id));
            BasicDBObject o = new BasicDBObject();
            o.put("$set", new BasicDBObject("password", password));
            dsForTigase.getDB().getCollection("tig_users").update(q, o);
            log.info("进入 updateToTig..... 更新完毕");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changePassword(String username, String password, String newPassword) {
        log.info("tigase密码更新.....");
        log.info("tigase密码更新.....password = " + password);
        log.info("tigase密码更新.....newPassword = " + newPassword);
        XMPPTCPConnection con = null;
        try {

            try {
                log.info("J44444444444");
                con = getConnection(username, password);
                log.info("con=" + con);
            } catch (Exception e) {
                log.info("tigase密码更新异常");
                updateToTig(username, newPassword);
                e.printStackTrace();
                return;
            }
            log.info("5555555555555");
            if (null != con && con.isAuthenticated()) {
                log.info("进入 xxxxxx");
                AccountManager accountManager = AccountManager.getInstance(con);
                accountManager.changePassword(newPassword);
                updateToTig(username, newPassword);
                System.out.println("更新密码到Tigase： " + xmppConfig.getServerName() + ", " + username + "  , " + newPassword);
            } else {
                log.info("else0000000000");
                updateToTig(username, newPassword);
            }

        } catch (Exception e) {
            log.info("changePassword 异常");
            updateToTig(username, newPassword);
            e.printStackTrace();
        }
        log.info("777777777777");
        con.disconnect();
    }


    /**
     * 加入群
     *
     * @param roomJid  群的 jid
     * @param userName 用戶id
     * @param password 用戶密碼
     */
    public void joinMucRoom(String roomJid, String userName, String password) {
        XMPPTCPConnection connection = null;
        try {
            connection = getConnection(userName, password);

            String jid = roomJid + getMucChatServiceName(connection);
            MultiUserChatManager muChatManager = MultiUserChatManager.getInstanceFor(connection);
            MultiUserChat muc = muChatManager.getMultiUserChat(JidCreate.entityBareFrom(jid));

            muc.join(Resourcepart.from(userName), password);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closedConnection(connection);

    }

    /**
     * @param @param roomJid    参数
     * @Description: TODO(销毁 解散房间)
     */
    public void destroyMucRoom(String userId, String password, String roomJid) {
        XMPPTCPConnection conn = null;
        try {
            conn = getConnection(userId, password);
            // 创建聊天室
            MultiUserChatManager muChatManager = MultiUserChatManager.getInstanceFor(conn);
            roomJid = roomJid + getMucChatServiceName(conn);
            MultiUserChat muc = muChatManager.getMultiUserChat(JidCreate.entityBareFrom(roomJid));
            muc.destroy("解散群组", JidCreate.entityBareFrom(conn.getUser()));
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }

    }


    public void joinMucRoom(String roomJid) {
        try {
            // 创建聊天室
            XMPPTCPConnection conn = getConnection();
            // 创建聊天室
            MultiUserChatManager muChatManager = MultiUserChatManager.getInstanceFor(conn);
            roomJid = roomJid + getMucChatServiceName(conn);
            MultiUserChat muc = muChatManager.getMultiUserChat(JidCreate.entityBareFrom(roomJid));
            muc.join(Resourcepart.from(conn.getUser().getLocalpart().toString()));
            //muc.invite(message, user, reason);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送 消息 到 群组中
     *
     * @param username
     * @param passwrod
     * @param roomJidArr
     */
    public void sendMsgToMucRoom(MessageBean messageBean, String... roomJidArr) {
        logger.info("发送 消息 到 群组中 MQ");
        DefaultMQProducer producer = getChatProducer();
        org.apache.rocketmq.common.message.Message message = null;
        if (StringUtil.isEmpty(messageBean.getMessageId())) {
            messageBean.setMessageId(StringUtil.randomUUID());
        }
        for (String jid : roomJidArr) {
            try {
                messageBean.setMsgType(1);
                messageBean.setRoomJid(jid);
                message = new org.apache.rocketmq.common.message.Message("xmppMessage", messageBean.toString().getBytes());
                logger.info("发送 消息 到 群组中 message=" + JSON.toJSONString(message));
                SendResult result = producer.send(message);
                logger.info(String.format("发送 消息 到 群组中 result=%s", JSON.toJSONString(result)));
                if (SendStatus.SEND_OK != result.getSendStatus()) {
                    logger.info("发送 消息 到 群组中 MQ   NOT OK");
                    System.out.println(result.toString());
                } else {
                    logger.info("发送 消息 到 群组中 MQ  OK");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 房间名称 text-single muc#roomconfig_roomname
     * 描述 text-single muc#roomconfig_roomdesc
     * 允许占有者更改主题 boolean muc#roomconfig_changesubject
     * 最大房间占有者人数 list-single muc#roomconfig_maxusers
     * 其 Presence 是 Broadcast 的角色 list-multi muc#roomconfig_presencebroadcast
     * 列出目录中的房间 boolean muc#roomconfig_publicroom
     * 房间是持久的 boolean muc#roomconfig_persistentroom
     * 房间是适度的 boolean muc#roomconfig_moderatedroom
     * 房间仅对成员开放 boolean muc#roomconfig_membersonly
     * 允许占有者邀请其他人 boolean muc#roomconfig_allowinvites
     * 需要密码才能进入房间 boolean muc#roomconfig_passwordprotectedroom
     * 密码 text-private muc#roomconfig_roomsecret
     * 能够发现占有者真实 JID 的角色 list-single muc#roomconfig_whois
     * 登录房间对话 boolean muc#roomconfig_enablelogging
     * 仅允许注册的昵称登录 boolean x-muc#roomconfig_reservednick
     * 允许使用者修改昵称 boolean x-muc#roomconfig_canchangenick
     * 允许用户注册房间 boolean x-muc#roomconfig_registration
     * 房间管理员 jid-multi muc#roomconfig_roomadmins
     * 房间拥有者 jid-multi muc#roomconfig_roomowners
     *
     * @param user
     * @param roomName
     * @param subject
     * @return
     **/
    public String createMucRoom(String password, String userId, String roomName, String jid, String roomSubject, String roomDesc) {
        if (StringUtil.isEmpty(jid))
            jid = StringUtil.randomUUID();
        XMPPTCPConnection connection = getConnection(userId, password);

        try {
            String roomJid = jid + getMucChatServiceName(connection);
            // 创建聊天室
            MultiUserChatManager muChatManager = MultiUserChatManager.getInstanceFor(connection);
            MultiUserChat muc = muChatManager.getMultiUserChat(JidCreate.entityBareFrom(roomJid));
            Thread.sleep(100);
            // 获得聊天室的配置表单
            Form form = muc.getConfigurationForm();
            // 根据原始表单创建一个要提交的新表单。
            Form submitForm = form.createAnswerForm();
            // 向要提交的表单添加默认答复
            List<FormField> list = form.getFields();
            for (FormField field : list) {
                if (!FormField.Type.hidden.equals(field.getType()) && field.getVariable() != null) {
                    // 设置默认值作为答复
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }


            // 设置聊天室的新拥有者
            // List owners = new ArrayList();

            // submitForm.setAnswer("muc#roomconfig_roomowners", owners);

            // 设置聊天室的名字
            submitForm.setAnswer("muc#roomconfig_roomname", roomName);
            // 设置聊天室描述
            // if (!TextUtils.isEmpty(roomDesc)) {
            // submitForm.setAnswer("muc#roomconfig_roomdesc", roomDesc);
            // }
            // 登录房间对话
            submitForm.setAnswer("muc#roomconfig_enablelogging", true);
            // 允许修改主题
            // submitForm.setAnswer("muc#roomconfig_changesubject", true);
            // 允许占有者邀请其他人
            // submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            //最大人数
            // List<String> maxusers = new ArrayList<String>();
            // maxusers.add("50");
            // submitForm.setAnswer("muc#roomconfig_maxusers", maxusers);
            // 公开的，允许被搜索到
            // submitForm.setAnswer("muc#roomconfig_publicroom", true);
            // 设置聊天室是持久聊天室，即将要被保存下来
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);

            //是否主持腾出空间(加了这个默认游客进去不能发言)
            // submitForm.setAnswer("muc#roomconfig_moderatedroom", true);
            // 房间仅对成员开放
            // submitForm.setAnswer("muc#roomconfig_membersonly", true);
            // 不需要密码
            // submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",
            // false);
            // 房间密码
            // submitForm.setAnswer("muc#roomconfig_roomsecret", "111");
            // 允许主持 能够发现真实 JID
            // List<String> whois = new ArrayList<String>();
            // whois.add("anyone");
            // submitForm.setAnswer("muc#roomconfig_whois", whois);

            // 管理员
            // <field var='muc#roomconfig_roomadmins'>
            // <value>wiccarocks@shakespeare.lit<alue>
            // <value>hecate@shakespeare.lit<alue>
            // </field>

            // 仅允许注册的昵称登录
            // submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
            // 允许使用者修改昵称
            // submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
            // 允许用户注册房间
            // submitForm.setAnswer("x-muc#roomconfig_registration", false);
            // 发送已完成的表单（有默认值）到服务器来配置聊天室
            muc.sendConfigurationForm(submitForm);

            // muc.changeSubject(roomSubject);
            // mMucChatMap.put(roomJid, muc);
            //mMucChatMap.put(roomJid, muc);
            return jid;
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return jid;
    }


    /**
     * 房间名称 text-single muc#roomconfig_roomname
     * 描述 text-single muc#roomconfig_roomdesc
     * 允许占有者更改主题 boolean muc#roomconfig_changesubject
     * 最大房间占有者人数 list-single muc#roomconfig_maxusers
     * 其 Presence 是 Broadcast 的角色 list-multi muc#roomconfig_presencebroadcast
     * 列出目录中的房间 boolean muc#roomconfig_publicroom
     * 房间是持久的 boolean muc#roomconfig_persistentroom
     * 房间是适度的 boolean muc#roomconfig_moderatedroom
     * 房间仅对成员开放 boolean muc#roomconfig_membersonly
     * 允许占有者邀请其他人 boolean muc#roomconfig_allowinvites
     * 需要密码才能进入房间 boolean muc#roomconfig_passwordprotectedroom
     * 密码 text-private muc#roomconfig_roomsecret
     * 能够发现占有者真实 JID 的角色 list-single muc#roomconfig_whois
     * 登录房间对话 boolean muc#roomconfig_enablelogging
     * 仅允许注册的昵称登录 boolean x-muc#roomconfig_reservednick
     * 允许使用者修改昵称 boolean x-muc#roomconfig_canchangenick
     * 允许用户注册房间 boolean x-muc#roomconfig_registration
     * 房间管理员 jid-multi muc#roomconfig_roomadmins
     * 房间拥有者 jid-multi muc#roomconfig_roomowners
     *
     * @param user
     * @param roomName
     * @param subject
     * @param max
     * @return
     */
    public String createChatRoom(String nickName, String roomName, String subject, String max) {
        String jid = UUID.randomUUID().toString().replaceAll("-", "");
        XMPPTCPConnection conn = null;
        try {
            conn = getConnection();
            MultiUserChatManager muChatManager = MultiUserChatManager.getInstanceFor(conn);
            jid = jid + getMucChatServiceName(conn);
            MultiUserChat muc = muChatManager.getMultiUserChat(JidCreate.entityBareFrom(jid));
            // 创建聊天室  
            muc.create(Resourcepart.from(nickName));
            // 获得聊天室的配置表单
            Form form = muc.getConfigurationForm();
            // 根据原始表单创建一个要提交的新表单。
            Form submitForm = form.createAnswerForm();
            //向要提交的表单添加默认答复
            List<FormField> list = form.getFields();
            for (FormField field : list) {
                if (!FormField.Type.hidden.equals(field.getType()) && field.getVariable() != null) {
                    // 设置默认值作为答复
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }
       
          /* List list =  new ArrayList();  
           list.add(max);  
           submitForm.setAnswer("muc#roomconfig_maxusers", list); */

            //房间名称
            submitForm.setAnswer("muc#roomconfig_roomname", roomName);
            //房间备注
            //         submitForm.setAnswer("muc#roomconfig_roomdesc", "wwh2222");
            // 能够发现占有者真实 JID 的角色
            // submitForm.setAnswer("muc#roomconfig_whois", "anyone");

            // 设置聊天室是持久聊天室，即将要被保存下来
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
            // 房间仅对成员开放
            // submitForm.setAnswer("muc#roomconfig_membersonly", false);
            // 允许占有者邀请其他人
            //submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            // 登录房间对话
            submitForm.setAnswer("muc#roomconfig_enablelogging", true);
            // 仅允许注册的昵称登录
            //submitForm.setAnswer("x-muc#roomconfig_reservednick", true);  
            // 允许使用者修改昵称
            //submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
            // 允许用户注册房间
            //submitForm.setAnswer("x-muc#roomconfig_registration", false);
            muc.sendConfigurationForm(submitForm);
            muc.changeSubject(subject);
            System.out.println("roomJid  ===銆� " + muc.getRoom());
            // jid = muc.getRoom();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jid;
    }


    public String getMucChatServiceName(XMPPTCPConnection connection) {
        return "@muc." + connection.getXMPPServiceDomain();
    }

    /**
     * @param jid
     * @param body
     * @throws Exception
     * @Description:（群控制消息）
     **/
    public void sendMsgToGroupByJid(String jid, MessageBean messageBean) throws Exception {
        ThreadUtil.executeInThread(new Callback() {

            @Override
            public void execute(Object obj) {
                sendMsgToMucRoom(messageBean, jid);
            }
        });
    }

    public void sendManyMsgToGroupByJid(String jid, List<MessageBean> messageList) {
        try {
            for (MessageBean messageBean : messageList) {
                sendMsgToMucRoom(messageBean, jid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void send(List<Integer> userIdList, List<MessageBean> messageList) {

        ThreadUtil.executeInThread(new Callback() {

            @Override
            public void execute(Object obj) {

                try {

                    for (MessageBean messageBean : messageList) {
                        for (int userId : userIdList) {
                            messageBean.setMsgType(0);// 单聊消息
                            if (messageBean.getToUserId().equals(String.valueOf(userId))) {

                                send(messageBean);
                            }
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    System.out.println("发送推送失败!");
                }
            }
        });
    }

    public void examineTigaseUser(String userId, String password) {

        try {
            DBObject q = new BasicDBObject("user_id", userId + "@" + xmppConfig.getServerName());
            DBObject obj = dsForTigase.getDB().getCollection("tig_users").findOne(q);
            if ((null != obj)) {
                log.info("examineTigaseUser == if");
                return;
            } else {
                log.info("examineTigaseUser == else");
                registerAndXmppVersion(userId, password);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    /**
     * 1000 号 发布订阅消息
     *
     * @param messageBean
     */
    public void pubsubMessage(MessageBean messageBean) {
        LeafNode node = getSystemPubSubNode();
        if (null == node) {
            log.warning(" getSystemPubSubNode is null ======> ");
            return;
        }
        try {
            messageBean.setFromUserId("10000");
            messageBean.setFromUserName("系统客服");
            String messageId = StringUtil.randomUUID();

            messageBean.setMessageId(messageId);
            SimplePayload payload = new SimplePayload("<message xmlns='pubsub:message'><body>" + messageBean.toString() + "</body></message>");
            PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>(messageId, payload);
            node.publish(item);
            log.info("pubsubMessage ====> " + item.getPayload().toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 初始化  发布节点
     *
     * @param pubSubManager
     * @param nodeId
     * @return
     */
    public LeafNode initSystemPubSubNode(PubSubManager pubSubManager, String nodeId) {
        LeafNode node = null;
        try {
            ConfigureForm config = new ConfigureForm(DataForm.Type.submit);
            //配置参数
            config.setPersistentItems(true);  //是否持久化
            config.setDeliverPayloads(true);
            config.setAccessModel(AccessModel.open);
            config.setPublishModel(PublishModel.publishers);

            node = (LeafNode) pubSubManager.createNode(nodeId, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return node;
    }

    /**
     * 获取  10000 号 发布节点
     *
     * @return
     */
    public LeafNode getSystemPubSubNode() {
        XMPPTCPConnection conn = null;
        PubSubManager pubSubManager = null;
        String nodeId = "10000";
        LeafNode node = null;
        try {
            try {
                conn = getConnection("10000", Md5Util.md5Hex("10000"));
//				conn = getConnection(nodeId, Md5Util.md5Hex(SKBeanUtils.getSystemAdminMap().get(nodeId)));
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return null;
            }
            pubSubManager = PubSubManager.getInstance(conn, JidCreate.domainBareFrom("pubsub." + connection.getXMPPServiceDomain()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            node = (LeafNode) pubSubManager.getNode(nodeId);
            if (null != node) {
                return node;
            } else {
                node = initSystemPubSubNode(pubSubManager, nodeId);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return node;


    }

    /**
     * @param 参数
     * @Description: TODO(创建 发布者节点)
     */
    public void pubsubSystemNode() {
        XMPPTCPConnection connection = null;
        try {
            String nodeId = "10000";
            try {
                connection = getConnection("10000", Md5Util.md5Hex("10000"));
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return;
            }
            PubSubManager pubSubManager = PubSubManager.getInstance(connection, JidCreate.domainBareFrom("pubsub." + connection.getXMPPServiceDomain()));
            if (!connection.isConnected() && !connection.isAuthenticated())
                return;

            //pubSubManager.deleteNode(nodeId);
            LeafNode node = null;
            try {
                node = (LeafNode) pubSubManager.getNode(nodeId);
                if (null != node) {
                    connection.disconnect();
                    return;
                } else {
                    node = initSystemPubSubNode(pubSubManager, nodeId);
                }

            } catch (XMPPException e) {
                System.err.println(e.getMessage());
            }
			
			/* ConfigureForm nodeConfiguration = node.getNodeConfiguration();
			 System.out.println("nodeConfiguration "+nodeConfiguration.toString());*/
            if (null == node)
                return;
            DiscoverInfo discoverInfo = node.discoverInfo();
            System.out.println("discoverInfo " + discoverInfo);

            System.out.println("================ pubsubSystemNode inited =========>");


        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            System.err.println(e.getMessage());
            //System.exit(1);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //System.exit(1);
        }
        connection.disconnect();


    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;


        // 初始化生产者
        getChatProducer();
        /**
         * 初始化 10000号订阅
         */
		/*ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				// TODO Auto-generated method stub
				getInstance().pubsubSystemNode();
			}
		});*/

    }

    private byte[] generateId(String username) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(username.getBytes());
    }

    private void closedConnection(XMPPTCPConnection conn) {
        closedConnection(conn, null);
    }

    private void closedConnection(XMPPTCPConnection conn, String sysUserId) {
        ThreadUtil.executeInThread(new Callback() {

            @Override
            public void execute(Object obj) {
                try {

                    if (null != conn) {
                        conn.disconnect();
                        if (null != sysUserId) {
                            sysUserList.add(sysUserId);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 15);
    }

    public static class MyConnectionListener implements ConnectionListener {

        private XMPPTCPConnection conn;

        public XMPPTCPConnection getConn() {
            return conn;
        }

        public void setConn(XMPPTCPConnection conn) {
            this.conn = conn;
        }

        public MyConnectionListener() {
            // TODO Auto-generated constructor stub
        }

        public MyConnectionListener(XMPPTCPConnection conn, boolean flag) {
            // boolean
            this.conn = conn;
            if (conn.isAuthenticated()) {

                PingManager pingManager = PingManager.getInstanceFor(conn);
                pingManager.registerPingFailedListener(new PingFailedListener() {

                    @Override
                    public void pingFailed() {
                        log.info("xmpp ping pingFailed=====>");
                    }
                });

            }
        }

        @Override
        public void connectionClosed() {
            log.info((null != conn ? conn.getUser() : "") + " ====> connectionClosed");
            conn = null;

        }

        @Override
        public void connectionClosedOnError(Exception e) {
            log.info((null != conn ? conn.getUser() : "") + " ====> connectionClosedOnError");

            if (null != conn)
                conn.disconnect();
            conn = null;

        }

        @Override
        public void connected(XMPPConnection connection) {
            log.info(connection.getUser() + " ====> connected");
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            //PingManager pingManager = PingManager.getInstanceFor(connection);

            log.info(connection.getUser() + " ====> authenticated  resumed " + resumed);
        }

    }

    public static class MessageBean {
        private Object content;
        private String fileName;
        private String fromUserId = "10005";
        private String fromUserName = "10005";
        private Object objectId;
        private long timeSend = System.currentTimeMillis() / 1000;
        private String toUserId;
        private String toUserName;
        private int fileSize;
        private int type;

        private String messageId;

        private String other;

        private int msgType; // 消息type  0：普通单聊消息    1：群组消息    2：广播消息

        private String roomJid;// 群组jid

        /**
         * 外面的to 消息发送给谁
         */
        private String to;

        public Object getContent() {
            return content;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFromUserId() {
            return fromUserId;
        }

        public String getFromUserName() {
            return fromUserName;
        }

        public Object getObjectId() {
            return objectId;
        }

        public long getTimeSend() {
            return timeSend;
        }

        public String getToUserId() {
            return toUserId;
        }

        public String getToUserName() {
            return toUserName;
        }

        public int getFileSize() {
            return fileSize;
        }

        public void setFileSize(int fileSize) {
            this.fileSize = fileSize;
        }

        public int getType() {
            return type;
        }

        public void setContent(Object content) {
            this.content = content;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public void setFromUserId(String fromUserId) {
            this.fromUserId = fromUserId;
        }

        public void setFromUserName(String fromUserName) {
            this.fromUserName = fromUserName;
        }

        public void setObjectId(Object objectId) {
            this.objectId = objectId;
        }

        public void setTimeSend(long timeSend) {
            this.timeSend = timeSend;
        }

        public void setToUserId(String toUserId) {
            this.toUserId = toUserId;
        }

        public void setToUserName(String toUserName) {
            this.toUserName = toUserName;
        }

        public void setType(int type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }

        public String getOther() {
            return other;
        }

        public void setOther(String other) {
            this.other = other;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public int getMsgType() {
            return msgType;
        }

        public void setMsgType(int msgType) {
            this.msgType = msgType;
        }

        public String getRoomJid() {
            return roomJid;
        }

        public void setRoomJid(String roomJid) {
            this.roomJid = roomJid;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }


    }


}
