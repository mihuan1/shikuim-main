//package cn.xyz.mianshi.service.agora.impl;
//
//import cn.xyz.mianshi.service.agora.IAgoraService;
//import com.alibaba.fastjson.JSON;
//import io.agora.media.AccessToken;
//import io.agora.media.SimpleTokenBuilder;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import redis.clients.jedis.JedisCommands;
//
//import javax.annotation.Resource;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//
///**
// * 声网服务类，
// */
//@Service
//@Slf4j
//public class AgoraServiceImpl implements IAgoraService {
//
//    @Resource JedisCommands jedis;
//    @Resource MsgService msgService;
//
//
//    /**
//     * 生成一个简单的token,用于加入会话，过期时间 6小时
//     */
//    private String getJoinToken(String channelId, long uid)throws NoSuchAlgorithmException, InvalidKeyException {
//        String appid = SysConfig._this.getProperty("argora.appid");
//        String appKey = SysConfig._this.getProperty("argora.appkey");
//        log.info(MsgConstant.logPrefix + "AgoraService.getJoinToken,appid:{},appKey:{},channelId:{},uid:{}", appid, appKey, channelId, uid);
//        int expire = (int) (System.currentTimeMillis() / 1000L) + 60 * 60 * 6;
//        SimpleTokenBuilder token = new SimpleTokenBuilder(appid, appKey, channelId, uid + "");
//        token.initPrivileges(SimpleTokenBuilder.Role.Role_Attendee);
//        token.setPrivilege(AccessToken.Privileges.kJoinChannel, expire);
//        return token.buildToken();
//    }
//    /**
//     * 生成通话id,用全局生成器生成
//     * @return long
//     */
//    private long buildChannelId() {
//        return IdWorker.next();
//    }
//
//
//    private String splitChannel(String ch) {
//        if (ch == null) {
//            return null;
//        }
//        String[] tmps = ch.split(":");
//        return tmps[0];
//    }
//
//    /**
//     * 打电话，创建会话
//     *
//     * @param self
//     * @param to
//     * @return
//     * @throws Exception
//     */
//    @Override
//    public CreateVoiceCallResponse createCall(Long self, Long to)throws NoSuchAlgorithmException, InvalidKeyException {
//        if (Objects.equals(to,0L)) {
//            throw new BusinessException(BusinessErrorCode.VOICE_CALL_PARAM_ERROR);
//        }
//        String toKey = AgoraConstant.getCallUidRedisKey(to);
//        if (jedis.exists(toKey)) {
//            throw new BusinessException(BusinessErrorCode.VOICE_CALL_BUSYING);
//        }
//        String appid = SysConfig._this.getProperty("argora.appid");
//        String channel = String.valueOf(buildChannelId());
//        CreateVoiceCallResponse response = new CreateVoiceCallResponse();
//        response.setAppId(appid);
//        response.setChannel(channel);
//        response.setMyToken(getJoinToken(channel, self));
//        response.setToToken(getJoinToken(channel, to));
//
//        //占个位，打电话的和接电话的人都做一个标识
//        jedis.setex(AgoraConstant.getCallUidRedisKey(self), AgoraConstant.AGORAREDISTIMEOUT, channel);
//        //在创建时，就把对方绑起来，安全点。哈哈
//        jedis.setex(AgoraConstant.getCallUidRedisKey(to), AgoraConstant.AGORAREDISTIMEOUT, channel);
//
//        Map<String, String> channelValue = new HashMap<>();
//        channelValue.put("from", String.valueOf(self));
//        channelValue.put("to", String.valueOf(to));
//
//        jedis.hmset(AgoraConstant.getCallChannelKey(channel), channelValue);
//        jedis.expire(AgoraConstant.getCallChannelKey(channel), 60 * 60 * 24);//1天
//
//        log.info("agora.createCall from={},to={},ch={}", self, to, channel);
//        return response;
//
//    }
//
//    @Override
//    public void join(long uid, ChannelRequestBody body,MsgEnum.MsgTp msgTp ) {
//        log.info("agora.join uid:{},body: {},msgTp:{}", uid, body,msgTp);
//        String channel = body.getChannel();
//        channel = splitChannel(channel);
//        Map<String, String> channelValue = jedis.hgetAll(AgoraConstant.getCallChannelKey(channel));
//        if (channelValue == null || channelValue.size() == 0) {
//            log.error("agora.join channel不存在");
//            throw new BusinessException(BusinessErrorCode.CHANNEL_ERROR);
//        }
//        long from = Long.parseLong(channelValue.get("from"));
//        long to = Long.parseLong(channelValue.get("to"));
//        //接通就删除这个打电话的消息
//        msgService.delMsgs(to, msgTp.getId(), Collections.singletonList(Long.parseLong(channel)));
//        boolean isSelf = from == uid || to == uid;
//        if (!isSelf) {
//            log.error("agora.join channel 操作不是自己的通话:uid:{}" , uid);
//            throw new BusinessException(BusinessErrorCode.CHANNEL_NOT_SELF);
//        }
//
//        jedis.setex(AgoraConstant.getCallUidRedisKey(from), AgoraConstant.AGORAHEARTBREAK, channel);
//        jedis.setex(AgoraConstant.getCallUidRedisKey(to), AgoraConstant.AGORAHEARTBREAK, channel);
//        //设置标识，用来在心跳中检查通话人数，过期时间12小时
//        jedis.setex(AgoraConstant.getCallStartPrefix(channel), 60 * 60 * 12, channel);
//    }
//    @Override
//    public void heartBreak(long uid, ChannelRequestBody body, String appid,MsgEnum.MsgTp msgTp, MsgEnum.FriendMsgTp friendMsgTp, MsgEnum.ActionType action) {
//        log.info("agora.heartBreak uid:{},body: {}, appid:{},msgTp:{},friendMsgTp:{},action:{}", uid, body,appid,msgTp,friendMsgTp,action);
//        String channel = body.getChannel();
//        channel = splitChannel(channel);
//        Map<String, String> channelValue = jedis.hgetAll(AgoraConstant.getCallChannelKey(channel));
//        if (channelValue == null || channelValue.size() == 0) {
//            log.error("agora.heartBreak channel不存在");
//            return;
//        }
//        long from = Long.parseLong(channelValue.get("from"));
//        long to = Long.parseLong(channelValue.get("to"));
//        boolean isSelf = from == uid || to == uid;
//        if (!isSelf) {
//            log.error("agora.heartBreak channel 操作不是自己的通话,uid:{}" , uid);
//            return;
//        }
//        //在通话接通时开始检查通道中有几个用户在通话，只有一个时将另一个的通话关闭
//        if (jedis.get(AgoraConstant.getCallStartPrefix(channel)) != null) {
//            log.info("agora.heartBreak start to check call status,channel is exist in redis");
//            if (jedis.get(AgoraConstant.getCallUidRedisKey(from)) == null) {
//                log.error("agora.heartBreak channel from uid is down");
//                try {
//                    stopCall(from, body, appid,msgTp,friendMsgTp,action);
//                } catch (Exception e) {
//                    log.error("agora.heartBreak error when stop call fromUid:{},body{},appid:{}", from, JSON.toJSONString(body), appid);
//                }
//                return;
//            } else if (jedis.get(AgoraConstant.getCallUidRedisKey(to)) == null) {
//                log.error("agora.heartBreak channel to uid is down");
//                try {
//                    stopCall(from, body, appid,msgTp,friendMsgTp,action);
//                } catch (Exception e) {
//                    log.error("agora.heartBreak error when stop call toUid:{},body{},appid:{}", to, JSON.toJSONString(body), appid);
//                }
//                return;
//            }
//        }
//        jedis.setex(AgoraConstant.getCallUidRedisKey(from), AgoraConstant.AGORAHEARTBREAK, channel);
//        jedis.setex(AgoraConstant.getCallUidRedisKey(to), AgoraConstant.AGORAHEARTBREAK, channel);
//    }
//    @Override
//    public void stopCall(long uid, ChannelRequestBody body, String appid,MsgEnum.MsgTp msgTp,MsgEnum.FriendMsgTp friendMsgTp,MsgEnum.ActionType action) {
//        String channel = body.getChannel();
//        log.info("agora.stopCall uid:{},body: {}, appid:{},msgTp:{},friendMsgTp:{},action:{}", uid, body,appid,msgTp,friendMsgTp,action);
//        channel = splitChannel(channel);
//        Map<String, String> channelValue = jedis.hgetAll(AgoraConstant.getCallChannelKey(channel));
//        if (channelValue == null || channelValue.size() == 0) {
//            log.error("agora.stopCall channel不存在");
//            return;
//        }
//        log.info("agora {}", JSON.toJSONString(channelValue));
//        long from = Long.parseLong(channelValue.get("from"));
//        long to = Long.parseLong(channelValue.get("to"));
//        String hasRefuse = channelValue.get(AgoraConstant.AGORA_HASREFUSE);
//
//        //删除rediskey
//        jedis.del(AgoraConstant.getCallChannelKey(channel));
//        jedis.del(AgoraConstant.getCallUidRedisKey(from));
//        jedis.del(AgoraConstant.getCallUidRedisKey(to));
//
//        //把请求打电话的消息删除了
//        msgService.delMsgs(to, msgTp.getId() ,Collections.singletonList(Long.parseLong(channel)));
//        //有通话且结束人是主动方
//        if (uid == from && body.getType() == 1 && !"1".equals(hasRefuse)) {
//            SocketMsg msg = new SocketMsg();
//            msg.setAppid(Long.parseLong(appid));
//            msg.setMsgType(msgTp.getId());
//            msg.setMsgId(IdWorker.next());
//            msg.setMsgVariety(friendMsgTp.getId());
//            msg.setMsgBody(new MsgBody());
//            msg.getMsgBody().setFromUid(uid);
//            msg.getMsgBody().setToUid(to);
//            MsgDetail md = new MsgDetail();
//            md.setText(channel);
//            md.setAction(action.getId());
//            msg.getMsgBody().setMsgDetail(md);
//            msgService.revMsg(msg);
//        }
//
//
//    }
//    @Override
//    public void refuseCall(long uid, ChannelRequestBody body, String appid,MsgEnum.MsgTp msgTp,MsgEnum.FriendMsgTp friendMsgTp,MsgEnum.ActionType action) {
//        log.info("agora.heartBreak uid:{},body: {}, appid:{},msgTp:{},friendMsgTp:{},action:{}", uid, body,appid,msgTp,friendMsgTp,action);
//        String channel = body.getChannel();
//        channel = splitChannel(channel);
//        jedis.del(AgoraConstant.getCallUidRedisKey(uid));//解除一下占线状态
//        Map<String, String> channelValue = jedis.hgetAll(AgoraConstant.getCallChannelKey(channel));
//        if (channelValue == null || channelValue.size() == 0) {
//            log.error("agora.stopCall channel不存在:{}",channel);
//            return;
//        }
//
//        long from = Long.parseLong(channelValue.get("from"));
//        long to = Long.parseLong(channelValue.get("to"));
//        if (uid != to) {
//            log.error("agora.stopCall 不能拒绝别人的通话 self:{} ,to:{}" , uid ,to);
//            return;
//        }
//
//        //打一个标志位
//        jedis.hset(AgoraConstant.getCallChannelKey(channel), AgoraConstant.AGORA_HASREFUSE, "1");
//        SocketMsg msg = new SocketMsg();
//
//
//        msg.setAppid(Long.parseLong(appid));
//        msg.setMsgType(msgTp.getId());
//        msg.setMsgId(IdWorker.next());
//        msg.setMsgVariety(friendMsgTp.getId());
//        msg.setMsgBody(new MsgBody());
//        msg.getMsgBody().setFromUid(uid);
//        msg.getMsgBody().setToUid(from);//打电话的人
//        MsgDetail md = new MsgDetail();
//        md.setText(channel);
//        md.setAction(action.getId());
//        msg.getMsgBody().setMsgDetail(md);
//        msgService.revMsg(msg);
//    }
//}
