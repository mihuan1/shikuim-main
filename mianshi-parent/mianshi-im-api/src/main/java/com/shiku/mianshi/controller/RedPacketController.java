package com.shiku.mianshi.controller;


import cn.xyz.commons.constants.KConstants.ResultCode;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.RedPacket;
import cn.xyz.mianshi.vo.Room;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.AuthServiceUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class RedPacketController extends AbstractController {
    @Resource(name = "dsForRW")
    Datastore dsForRW;


    private JSONMessage checkSendPacket(RedPacket packet, long time, String secret) {
        return null;
    }

    @RequestMapping("/redPacket/sendRedPacket")
    public JSONMessage sendRedPacket(RedPacket packet,
                                     @RequestParam(defaultValue = "0") long time,
                                     @RequestParam(defaultValue = "") String secret) {
        String token = getAccess_token();
        Integer userId = ReqUtil.getUserId();

        if (SKBeanUtils.getUserManager().getUserMoeny(userId) < packet.getMoney()) {
            //余额不足
            return JSONMessage.failure("余额不足,请先充值!");
        } else if (packet.getMoney() < 0.01 || 500 < packet.getMoney()) {
            return JSONMessage.failure("红包总金额在0.01~500之间哦!");
        } else if ((packet.getMoney() / packet.getCount()) < 0.01) {
            return JSONMessage.failure("每人最少 0.01元 !");
        }
        //红包接口授权
        User user = SKBeanUtils.getUserManager().getUser(userId);
        if (StringUtil.isEmpty(user.getPayPassword())) {
            return JSONMessage.failure("请设置支付密码");
        }
        if (!AuthServiceUtils.authRedPacket(user.getPayPassword(), userId + "", token, time, secret)) {
            return JSONMessage.failure("支付密码错误!");
        }
        Object data = SKBeanUtils.getRedPacketManager().sendRedPacket(userId, packet);
        return JSONMessage.success(null, data);
    }

    /**
     * 新版本发送红包
     *
     * @param packet
     * @param time
     * @param secret
     * @return
     */
    @RequestMapping("/redPacket/sendRedPacket/v1")
    public JSONMessage sendRedPacketV1(RedPacket packet,
                                       @RequestParam(defaultValue = "0") long time, @RequestParam(defaultValue = "") String moneyStr,
                                       @RequestParam(defaultValue = "") String secret) {
        String token = getAccess_token();
        Integer userId = ReqUtil.getUserId();
        packet.setMoney(Double.valueOf(moneyStr));
        packet.setUserId(userId);
        if (SKBeanUtils.getUserManager().getUserMoeny(userId) < packet.getMoney()) {
            //余额不足
            return JSONMessage.failure("余额不足,请先充值!");
        } else if (packet.getMoney() < 0.01 || 500 < packet.getMoney()) {
            return JSONMessage.failure("红包总金额在0.01~500之间哦!");
        } else if ((packet.getMoney() / packet.getCount()) < 0.01) {
            return JSONMessage.failure("每人最少 0.01元 !");
        }
        //红包接口授权
        User user = SKBeanUtils.getUserManager().getUser(userId);
        if (StringUtil.isEmpty(user.getPayPassword())) {
            return JSONMessage.failure("请设置支付密码");
        }
        if (!AuthServiceUtils.authRedPacketV1(user.getPayPassword(), userId + "", token, time, moneyStr, secret)) {
            return JSONMessage.failure("支付密码错误!");
        }
//        Room room = null;
//        if (StringUtils.isNotBlank(packet.getRoomJid()))
//            room = SKBeanUtils.getRoomManagerImplForIM().get(new ObjectId(packet.getRoomJid()));
        Object data = SKBeanUtils.getRedPacketManager().sendRedPacket(userId, packet);
        return JSONMessage.success(null, data);
    }

    @RequestMapping("/redPacket/sendRedPacket/v2")
    public JSONMessage sendRedPacketV2(@RequestParam(defaultValue = "") String codeId, @RequestParam(defaultValue = "") String data) {
        String token = getAccess_token();
        Integer userId = ReqUtil.getUserId();
        String code = SKBeanUtils.getRedisService().queryTransactionSignCode(userId, codeId);
        if (StringUtil.isEmpty(code))
            return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
        //红包接口授权
        User user = SKBeanUtils.getUserManager().getUser(userId);
        if (StringUtil.isEmpty(user.getPayPassword())) {
            return JSONMessage.failureByErrCode(ResultCode.PayPasswordNotExist);
        }
        JSONObject jsonObj = AuthServiceUtils.authSendRedPacketByMac(userId.toString(), token, data, code, user.getPayPassword());
        if (null == jsonObj)
            return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
        RedPacket packet = JSONObject.toJavaObject(jsonObj, RedPacket.class);
        if (null == packet)
            return JSONMessage.failureByErrCode(ResultCode.AUTH_FAILED);
        packet.setUserId(userId);
        packet.setUserName(user.getNickname());
        if (SKBeanUtils.getUserManager().getUserMoeny(userId) < packet.getMoney()) {
            //余额不足 return JSONMessage.failure("余额不足,请先充值!"); }else
            if (packet.getMoney() < 0.01 || 500 < packet.getMoney()) {
                return JSONMessage.failure("红包总金额在0.01~500之间哦!");
            } else if ((packet.getMoney() / packet.getCount()) < 0.01) {
                return JSONMessage.failure("每人最少 0.01元 !");
            }
        }
        Object result = SKBeanUtils.getRedPacketManager().sendRedPacket(userId, packet);
        return JSONMessage.success(null, result);
    }


    //获取红包详情
    @RequestMapping("/redPacket/getRedPacket")
    public JSONMessage getRedPacket(String id) {
        JSONMessage result = SKBeanUtils.getRedPacketManager().getRedPacketById(ReqUtil.getUserId(), ReqUtil.parseId(id));
        //System.out.println("获取红包  ====>  "+result);
        return result;
    }

    //回复红包
    @RequestMapping("/redPacket/reply")
    public JSONMessage replyRedPacket(String id, String reply) {
        try {
            if (StringUtil.isEmpty(reply))
                return JSONMessage.failure("回复不能为 null!");
            SKBeanUtils.getRedPacketManager().replyRedPacket(id, reply);
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    //打开红包
    @RequestMapping("/redPacket/openRedPacket")
    public JSONMessage openRedPacket(String id,
                                     @RequestParam(defaultValue = "0") long time,
                                     @RequestParam(defaultValue = "") String secret) {
        String token = getAccess_token();
        Integer userId = ReqUtil.getUserId();
        //红包接口授权
        if (!AuthServiceUtils.authRedPacket(userId + "", token, time, secret)) {
            return JSONMessage.failure("权限验证失败!");
        }
        RedPacket redPacket = null;
        if (StringUtils.isNotBlank(id)) redPacket = SKBeanUtils.getRedPacketManager().get(ReqUtil.parseId(id));
        JSONMessage result = SKBeanUtils.getRedPacketManager().openRedPacketById(userId, ReqUtil.parseId(id));
        //System.out.println("打开红包  ====>  "+result);
        return result;
    }

    //查询发出的红包
    @RequestMapping("/redPacket/getSendRedPacketList")
    public JSONMessage getSendRedPacketList(@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
        Object data = SKBeanUtils.getRedPacketManager().getSendRedPacketList(ReqUtil.getUserId(), pageIndex, pageSize);
        return JSONMessage.success(null, data);
    }

    //查询收到的红包
    @RequestMapping("/redPacket/getRedReceiveList")
    public JSONMessage getRedReceiveList(@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
        Object data = SKBeanUtils.getRedPacketManager().getRedReceiveList(ReqUtil.getUserId(), pageIndex, pageSize);
        return JSONMessage.success(null, data);
    }


}
