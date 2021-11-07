package com.shiku.mianshi.controller;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.KConstants.Result;
import cn.xyz.commons.ex.BizException;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.*;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.*;
import cn.xyz.mianshi.opensdk.entity.SkOpenAccount;
import cn.xyz.mianshi.opensdk.entity.SkOpenApp;
import cn.xyz.mianshi.opensdk.entity.SkOpenCheckLog;
import cn.xyz.mianshi.service.impl.LiveRoomManagerImpl;
import cn.xyz.mianshi.service.impl.RoomManagerImplForIM;
import cn.xyz.mianshi.service.impl.UserManagerImpl;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.*;
import cn.xyz.mianshi.vo.LiveRoom.LiveRoomMember;
import cn.xyz.mianshi.vo.Room.Member;
import cn.xyz.mianshi.vo.User.UserLoginLog;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 酷聊后台管理
 *
 * @author luorc
 */
@RestController
@RequestMapping("/console")
public class AdminController extends AbstractController {

    public static final String LOGIN_USER_KEY = "LOGIN_USER";

    @Resource(name = "dsForRW")
    private Datastore dsForRW;

    @Resource(name = "dsForTigase")
    private Datastore dsForTigase;

    @Resource(name = "dsForRoom")
    private Datastore dsForRoom;

    private static RoomManagerImplForIM getRoomManagerImplForIM() {
        RoomManagerImplForIM roomManagerImplForIM = SKBeanUtils.getRoomManagerImplForIM();
        return roomManagerImplForIM;
    }


    private static LiveRoomManagerImpl getLiveRoomManager() {
        LiveRoomManagerImpl liveRoomManagerImpl = SKBeanUtils.getLiveRoomManager();
        return liveRoomManagerImpl;
    }


    private static UserManagerImpl getUserManager() {
        UserManagerImpl userManagerImpl = SKBeanUtils.getUserManager();
        return userManagerImpl;
    }


    @RequestMapping(value = "/config")
    public JSONMessage getConfig() {
        Config config = SKBeanUtils.getAdminManager().getConfig();
        config.setDistance(ConstantUtil.getAppDefDistance());
        return JSONMessage.success(null, config);
    }

    // 设置服务端配置
    @RequestMapping(value = "/config/set", method = RequestMethod.POST)
    public JSONMessage setConfig(@ModelAttribute Config config) throws Exception {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().setConfig(config);
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    // 设置客户端配置
    @RequestMapping(value = "/clientConfig/set")
    public JSONMessage setClientConfig(@ModelAttribute ClientConfig clientConfig) throws Exception {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().setClientConfig(clientConfig);
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    @RequestMapping(value = "/clientConfig")
    public JSONMessage getClientConfig() {
        ClientConfig clientConfig = SKBeanUtils.getAdminManager().getClientConfig();
        return JSONMessage.success(null, clientConfig);
    }

    @RequestMapping(value = "/chat_logs", method = {RequestMethod.GET})
    public ModelAndView chat_logs(@RequestParam(defaultValue = "0") long startTime,
                                  @RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "0") int pageIndex,
                                  @RequestParam(defaultValue = "10") int pageSize, HttpServletRequest request) {
        // User user = getUser();

        DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
        BasicDBObject q = new BasicDBObject();
        // q.put("sender", user.getUserId());
        if (0 != startTime)
            q.put("ts", new BasicDBObject("$gte", startTime));
        if (0 != endTime)
            q.put("ts", new BasicDBObject("$lte", endTime));

        long total = dbCollection.count(q);
        List<DBObject> pageData = Lists.newArrayList();

        DBCursor cursor = dbCollection.find(q).skip(pageIndex * pageSize).limit(pageSize);
        while (cursor.hasNext()) {
            BasicDBObject dbObj = (BasicDBObject) cursor.next();
            if (1 == dbObj.getInt("direction")) {
                int sender = dbObj.getInt("receiver");
                dbObj.put("receiver_nickname", getUserManager().getUser(sender).getNickname());
            }
            pageData.add(dbObj);
        }
        request.setAttribute("page", new PageVO(pageData, total, pageIndex, pageSize));
        return new ModelAndView("chat_logs");
    }

    @RequestMapping(value = "/chat_logs_all")
    public JSONMessage chat_logs_all(@RequestParam(defaultValue = "0") long startTime,
                                     @RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "0") int sender,
                                     @RequestParam(defaultValue = "0") int receiver, @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String keyWord, HttpServletRequest request) {
        DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
        BasicDBObject q = new BasicDBObject();
        if (0 == receiver) {
            q.put("receiver", new BasicDBObject("$ne", 10005));
            q.put("direction", 0);
        } else {
            q.put("direction", 0);
            q.put("receiver", BasicDBObjectBuilder.start("$eq", receiver).add("$ne", 10005).get());
        }
        if (0 == sender) {
            q.put("sender", new BasicDBObject("$ne", 10005));
            q.put("direction", 0);
        } else {
            q.put("direction", 0);
            q.put("sender", BasicDBObjectBuilder.start("$eq", sender).add("$ne", 10005).get());
        }
        if (!StringUtil.isEmpty(keyWord)) {
            q.put("content", new BasicDBObject(MongoOperator.REGEX, keyWord));
        }

        if (0 != startTime)
            q.put("ts", new BasicDBObject("$gte", startTime));
        if (0 != endTime)
            q.put("ts", new BasicDBObject("$lte", endTime));

        long total = dbCollection.count(q);
        List<DBObject> pageData = Lists.newArrayList();

        DBCursor cursor = dbCollection.find(q).sort(new BasicDBObject("_id", -1)).skip((page - 1) * limit).limit(limit);
        PageResult<DBObject> result = new PageResult<>();
        while (cursor.hasNext()) {
            BasicDBObject dbObj = (BasicDBObject) cursor.next();
            @SuppressWarnings("deprecation")
            String unescapeHtml3 = StringEscapeUtils.unescapeHtml3((String) dbObj.get("body"));
            JSONObject body = JSONObject.parseObject(unescapeHtml3);
            if (null != body.get("isEncrypt") && "1".equals(body.get("isEncrypt").toString())) {
                dbObj.put("isEncrypt", 1);
            } else {
                dbObj.put("isEncrypt", 0);
            }
            try {
                dbObj.put("sender_nickname", getUserManager().getNickName(dbObj.getInt("sender")));
            } catch (Exception e) {
                dbObj.put("sender_nickname", "未知");
            }
            try {
                dbObj.put("receiver_nickname", getUserManager().getNickName(dbObj.getInt("receiver")));
            } catch (Exception e) {
                dbObj.put("receiver_nickname", "未知");
            }
            try {
                dbObj.put("content",
                        JSON.parseObject(dbObj.getString("body").replace("&quot;", "\""), Map.class).get("content"));
            } catch (Exception e) {
                dbObj.put("content", "--");
            }

            pageData.add(dbObj);

        }
        result.setData(pageData);
        result.setCount(total);
        return JSONMessage.success(result);
    }

    @RequestMapping(value = "/chat_logs_all/del", method = {RequestMethod.POST})
    public JSONMessage chat_logs_all_del(@RequestParam(defaultValue = "0") long startTime,
                                         @RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "0") int sender,
                                         @RequestParam(defaultValue = "0") int receiver, @RequestParam(defaultValue = "0") int pageIndex,
                                         @RequestParam(defaultValue = "25") int pageSize, HttpServletRequest request, HttpServletResponse response) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }
        DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
        BasicDBObject q = new BasicDBObject();

        if (0 == sender) {
            q.put("sender", new BasicDBObject("$ne", 10005));
        } else {
            q.put("sender", BasicDBObjectBuilder.start("$eq", sender).add("$ne", 10005).get());
        }
        if (0 == receiver) {
            q.put("receiver", new BasicDBObject("$ne", 10005));
        } else {
            q.put("receiver", BasicDBObjectBuilder.start("$eq", receiver).add("$ne", 10005).get());
        }
        if (0 != startTime)
            q.put("ts", new BasicDBObject("$gte", startTime));
        if (0 != endTime)
            q.put("ts", new BasicDBObject("$lte", endTime));
        dbCollection.remove(q);
        return JSONMessage.success();

    }

    @RequestMapping(value = "/deleteChatMsgs")
    public JSONMessage deleteChatMsgs(@RequestParam(defaultValue = "") String msgId,
                                      @RequestParam(defaultValue = "0") int type) {
        // 判断权限
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }

        DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
        BasicDBObject q = new BasicDBObject();
        try {
            if (0 == type) {
                if (StringUtil.isEmpty(msgId))
                    return JSONMessage.failure("参数有误");
                else {
                    String[] msgIds = StringUtil.getStringList(msgId);
                    for (String strMsgId : msgIds) {
                        q.put("_id", new ObjectId(strMsgId));
                        dbCollection.remove(q);
                    }
                }
            } else if (1 == type) {
                // 删除一个月前的聊天记录
                long onedayNextDay = DateUtil.getOnedayNextDay(DateUtil.currentTimeSeconds(), 30, 1);
                System.out.println("上个月的时间：" + onedayNextDay);
                q.put("timeSend", new BasicDBObject("$lte", onedayNextDay));
                dbCollection.remove(q);
            } else if (2 == type) {
                final int num = 100000;
                int count = dbCollection.find().count();
                if (count <= num)
                    throw new ServiceException("数量小于等于" + num);
                // 删除十万条前的聊天记录
                DBCursor cursor = dbCollection.find().sort(new BasicDBObject("timeSend", -1)).skip(num).limit(count);
                List<DBObject> list = cursor.toArray();
                for (DBObject dbObject : list) {
                    dbCollection.remove(dbObject);
                }
                logger.info("超过" + num + "的条数有：" + list.size());
            }
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getErrMessage());
        }
        return JSONMessage.success();
    }

    @RequestMapping(value = "/deleteRoom")
    public JSONMessage deleteRoom(@RequestParam(defaultValue = "") String roomId, @RequestParam(defaultValue = "0") Integer userId) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }

            DBCollection dbCollection = dsForTigase.getCollection(Room.class);
            getRoomManagerImplForIM().delete(new ObjectId(roomId), userId);
            dbCollection.remove(new BasicDBObject("_id", new ObjectId(roomId)));
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    // 邀请用户加入群成员
    @RequestMapping(value = "/inviteJoinRoom")
    public JSONMessage inviteJoinRoom(@RequestParam(defaultValue = "") String roomId, @RequestParam(defaultValue = "") String userIds, @RequestParam(defaultValue = "") Integer inviteUserId) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            if (StringUtil.isEmpty(userIds)) {
                return JSONMessage.failure("请选择邀请的人");
            }
            Room room = SKBeanUtils.getRoomManager().getRoom(new ObjectId(roomId));
            if (null == room)
                return JSONMessage.failure("群组不存在 或已解散!");
            else if (-1 == room.getS())
                return JSONMessage.failure("该群组已被后台锁定!");
            else {
                List<Integer> userIdList = StringUtil.getIntList(userIds, ",");
                if (room.getMaxUserSize() < room.getUserSize() + userIdList.size())
                    return JSONMessage.failure("群人数已满 不能加入!");
                User user = new User();
                user.setUserId(inviteUserId);
                user.setNickname("后台管理员");
                getRoomManagerImplForIM().consoleJoinRoom(user, new ObjectId(roomId), userIdList);
                return JSONMessage.success();
            }
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 删除房间成员
     *
     * @param roomId
     * @param userId
     * @param pageIndex
     * @return
     */
    @RequestMapping(value = "/deleteMember")
    public JSONMessage deleteMember(@RequestParam String roomId, @RequestParam String userId,
                                    @RequestParam(defaultValue = "0") int pageIndex, @RequestParam String adminUserId) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }

            if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(adminUserId))
                return JSONMessage.failure("参数有误");
            else {
                User user = getUserManager().getUser(Integer.parseInt(adminUserId));
                if (null != user) {
                    String[] userIds = StringUtil.getStringList(userId);
                    for (String strUserids : userIds) {
                        int strUserId = Integer.parseInt(strUserids);
                        getRoomManagerImplForIM().deleteMember(user, new ObjectId(roomId), strUserId);
                    }
                } else {
                    return JSONMessage.failure("用户不存在");
                }
            }
            return JSONMessage.success();
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    @RequestMapping(value = "/deleteUser")
    public JSONMessage deleteUser(@RequestParam(defaultValue = "") String userId) {
        try {

            return JSONMessage.failure("权限不足,请联系技术删除");
            // 权限校验
            /*
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            if (!StringUtil.isEmpty(userId)) {
                String[] strUserIds = StringUtil.getStringList(userId, ",");
                getUserManager().deleteUser(strUserIds);
            }
            return JSONMessage.success();
            */
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    public User getUser() {
        Object obj = RequestContextHolder.getRequestAttributes().getAttribute(LOGIN_USER_KEY,
                RequestAttributes.SCOPE_SESSION);
        return null == obj ? null : (User) obj;
    }

    @RequestMapping(value = "/groupchat_logs", method = {RequestMethod.GET})
    public ModelAndView groupchat_logs(@RequestParam(defaultValue = "") String room_jid_id,
                                       @RequestParam(defaultValue = "0") long startTime, @RequestParam(defaultValue = "0") long endTime,
                                       @RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize,
                                       HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("groupchat_logs");
        Object historyList = getRoomManagerImplForIM().selectHistoryList(getUser().getUserId(), 0, pageIndex, pageSize);
        if (!StringUtil.isEmpty(room_jid_id)) {
            DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_muc_msgs");

            BasicDBObject q = new BasicDBObject();
            // q.put("room_jid_id", room_jid_id);
            if (0 != startTime)
                q.put("ts", new BasicDBObject("$gte", startTime));
            if (0 != endTime)
                q.put("ts", new BasicDBObject("$lte", endTime));
            long total = dbCollection.count(q);
            java.util.List<DBObject> pageData = Lists.newArrayList();

            DBCursor cursor = dbCollection.find(q).sort(new BasicDBObject("ts", -1)).skip(pageIndex * pageSize)
                    .limit(pageSize);
            while (cursor.hasNext()) {
                pageData.add(cursor.next());
            }
            mav.addObject("page", new PageVO(pageData, total, pageIndex, pageSize));
        }
        mav.addObject("historyList", historyList);
        return mav;
    }

    /**
     * 群聊记录
     *
     * @param startTime
     * @param endTime
     * @param room_jid_id
     * @param page
     * @param limit
     * @param request
     * @return
     */
    @RequestMapping(value = "/groupchat_logs_all")
    public JSONMessage groupchat_logs_all(@RequestParam(defaultValue = "0") long startTime,
                                          @RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "") String room_jid_id,
                                          @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String keyWord,
                                          HttpServletRequest request) {
        DBCollection dbCollection = dsForRoom.getDB().getCollection("mucmsg_" + room_jid_id);

        BasicDBObject q = new BasicDBObject();
        if (0 != startTime)
            q.put("ts", new BasicDBObject("$gte", startTime));
        if (0 != endTime)
            q.put("ts", new BasicDBObject("$lte", endTime));
        if (!StringUtil.isEmpty(keyWord))
            q.put("content", new BasicDBObject(MongoOperator.REGEX, keyWord));

        long total = dbCollection.count(q);
        List<DBObject> pageData = Lists.newArrayList();
        PageResult<DBObject> result = new PageResult<>();
        DBCursor cursor = dbCollection.find(q).sort(new BasicDBObject("ts", -1)).skip((page - 1) * limit).limit(limit);
        while (cursor.hasNext()) {
            BasicDBObject dbObj = (BasicDBObject) cursor.next();
            @SuppressWarnings("deprecation")
            String unescapeHtml3 = StringEscapeUtils.unescapeHtml3((String) dbObj.get("body"));
            JSONObject body = JSONObject.parseObject(unescapeHtml3);
            if (null != body.get("isEncrypt") && "1".equals(body.get("isEncrypt").toString())) {
                dbObj.put("isEncrypt", 1);
            } else {
                dbObj.put("isEncrypt", 0);
            }
            try {
                Map<?, ?> params = JSON.parseObject(dbObj.getString("body").replace("&quot;", "\""), Map.class);
                dbObj.put("content", params.get("content"));
                dbObj.put("fromUserName", params.get("fromUserName"));
            } catch (Exception e) {
                dbObj.put("content", "--");
            }
            pageData.add(dbObj);

        }

        result.setData(pageData);
        result.setCount(total);
        return JSONMessage.success(result);
    }

    @RequestMapping(value = "/groupchat_logs_all/del")
    public JSONMessage groupchat_logs_all_del(@RequestParam(defaultValue = "0") long startTime,
                                              @RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "") String msgId,
                                              @RequestParam(defaultValue = "") String room_jid_id, @RequestParam(defaultValue = "0") int pageIndex,
                                              @RequestParam(defaultValue = "25") int pageSize, HttpServletRequest request, HttpServletResponse response) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }
        DBCollection dbCollection = dsForRoom.getDB().getCollection("mucmsg_" + room_jid_id);

        BasicDBObject q = new BasicDBObject();
        if (StringUtil.isEmpty(msgId))
            return JSONMessage.failure("参数有误");
        else {
            String[] msgIds = StringUtil.getStringList(msgId);
            for (String strMsgId : msgIds) {
                q.put("_id", new ObjectId(strMsgId));
                dbCollection.remove(q);
            }
        }
        if (0 != startTime)
            q.put("ts", new BasicDBObject("$gte", startTime));
        if (0 != endTime)
            q.put("ts", new BasicDBObject("$lte", endTime));

        dbCollection.remove(q);
        return JSONMessage.success();
    }

    @RequestMapping(value = "/groupchatMsgDel")
    public JSONMessage groupchatMsgDel(@RequestParam(defaultValue = "") String roomJid,
                                       @RequestParam(defaultValue = "0") int type) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }
        DBCollection dbCollection = dsForRoom.getDB().getCollection("mucmsg_" + roomJid);
        BasicDBObject q = new BasicDBObject();
        try {
            if (0 == type) {
                // 删除一个月前的聊天记录
                long onedayNextDay = DateUtil.getOnedayNextDay(DateUtil.currentTimeSeconds(), 30, 1);
                logger.info("上个月的时间：" + onedayNextDay);
                q.put("timeSend", new BasicDBObject("$lte", onedayNextDay));
                dbCollection.remove(q);
            } else if (1 == type) {
                final int num = 100000;
                int count = dbCollection.find().count();
                if (count <= num)
                    throw new ServiceException("数量小于等于" + num);
                // 删除十万条前的聊天记录
                DBCursor cursor = dbCollection.find().sort(new BasicDBObject("timeSend", -1)).skip(num).limit(count);
                List<DBObject> list = cursor.toArray();
                for (DBObject dbObject : list) {
                    dbCollection.remove(dbObject);
                }
                logger.info("超过" + num + "的条数有：" + list.size());
            }
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getErrMessage());
        }
        return JSONMessage.success();
    }

    @RequestMapping(value = {"", "/"})
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("userStatus");
    }

    @RequestMapping(value = "/login", method = {RequestMethod.GET})
    public void openLogin(HttpServletRequest request, HttpServletResponse response) {

        String path = request.getContextPath() + "/pages/console/login.html";
        try {
            response.sendRedirect(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @Description: 后台管理登录：允许超级管理员，管理员，游客，客服，财务登录
     **/
    @RequestMapping(value = "/login", method = {RequestMethod.POST})
    public JSONMessage login(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final Integer code = 86;
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String areaCode = request.getParameter("areaCode");
        HashMap<String, Object> map = new HashMap<>();
        User user = getUserManager().getUser((StringUtil.isEmpty(areaCode) ? (code + account) : (areaCode + account)));
        if (null == user)
            return JSONMessage.failure("账号不存在");
        Role userRole = SKBeanUtils.getRoleManager().getUserRole(user.getUserId(), null, 5);
        /*
         * List<Role> userRoles =
         * SKBeanUtils.getRoleManager().getUserRoles(user.getUserId(), null, 0);
         * if(userRoles.size()>0 && null != userRoles){
         * if(userRoles.contains(o)) }
         */
        if (null == userRole)
            return JSONMessage.failure("权限不足");
        if (null != userRole && -1 == userRole.getStatus())
            return JSONMessage.failure("您的账号已被禁用");
        /*
         * if (userRole.getRole() !=1 && userRole.getRole() !=5 &&
         * userRole.getRole() !=6) return JSONMessage.failure("权限不足");
         */
        if (user != null && password.equals(user.getPassword())) {

            Map<String, Object> tokenMap = KSessionUtil.adminLoginSaveToken(user.getUserId().toString(), null);

            map.put("access_Token", tokenMap.get("access_Token"));
            map.put("adminId", user.getTelephone());
            map.put("account", user.getUserId() + "");
            map.put("apiKey", appConfig.getApiKey());
            map.put("role", userRole.getRole() + "");
            map.put("nickname", user.getNickname());

            map.put("registerInviteCode", SKBeanUtils.getAdminManager().getConfig().getRegisterInviteCode());
            // 维护最后登录时间
            // updateLastLoginTime(admin.getId(),admin.getPassword(),admin.getRole(),admin.getState(),DateUtil.currentTimeSeconds());
            updateLastLoginTime(user.getUserId());
            return JSONMessage.success(map);
        }
        return JSONMessage.failure("帐号或密码错误！");
    }

    private void updateLastLoginTime(Integer userId) {
        Role role = new Role(userId);
        SKBeanUtils.getRoleManager().modifyRole(role);
    }

    @RequestMapping(value = "/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getSession().removeAttribute(LOGIN_USER_KEY);
        KSessionUtil.removeAdminToken(ReqUtil.getUserId());
        response.sendRedirect("/console/login");
    }

    @RequestMapping(value = "/pushToAll")
    public void pushToAll(HttpServletResponse response, @RequestParam int fromUserId, @RequestParam String body) {

        MessageBean mb = JSON.parseObject(body, MessageBean.class);
        mb.setFromUserId(fromUserId + "");
        mb.setTimeSend(DateUtil.currentTimeSeconds());
        mb.setMsgType(0);
        mb.setMessageId(StringUtil.randomUUID());
        ThreadUtil.executeInThread(obj -> {
            DBCursor cursor = dsForRW.getDB().getCollection("user").find(null, new BasicDBObject("_id", 1))
                    .sort(new BasicDBObject("_id", -1));
            while (cursor.hasNext()) {
                BasicDBObject dbObj = (BasicDBObject) cursor.next();
                int userId = dbObj.getInt("_id");
                try {
                    mb.setToUserId(String.valueOf(userId));
                    KXMPPServiceImpl.getInstance().send(mb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(
                    "<script type='text/javascript'>alert('\u6279\u91CF\u53D1\u9001\u6D88\u606F\u5DF2\u5B8C\u6210\uFF01');window.location.href='/pages/qf.jsp';</script>");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/roomList")
    public JSONMessage roomList(@RequestParam(defaultValue = "") String keyWorld,
                                @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "15") int limit, @RequestParam(defaultValue = "0") int leastNumbers) {

        PageResult<Room> result = new PageResult<>();

        Query<Room> query = dsForRoom.createQuery(Room.class);

        if (!StringUtil.isEmpty(keyWorld)) {
            query.criteria("name").containsIgnoreCase(keyWorld);
        }
        if (leastNumbers > 0)
            query.field("userSize").greaterThan(leastNumbers);

        result.setData(query.order("-createTime").asList(getRoomManagerImplForIM().pageFindOption(page, limit, 1)));
        result.setCount(query.count());

        return JSONMessage.success(result);
    }

    @RequestMapping(value = "/getRoomMember")
    public JSONMessage getRoom(@RequestParam(defaultValue = "") String roomId) {
        Room room = SKBeanUtils.getRoomManagerImplForIM().consoleGetRoom(new ObjectId(roomId));
        return JSONMessage.success(null, room);
    }

    /**
     * 直播间聊天记录
     *
     * @param pageIndex
     * @param pageSize
     * @param room_jid_id
     * @return
     */
    @RequestMapping(value = "/roomMsgDetail")
    public JSONMessage roomDetail(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "") String room_jid_id) {

        DBCollection dbCollection = dsForRoom.getDB().getCollection("mucmsg_" + room_jid_id);
        BasicDBObject q = new BasicDBObject();
        q.put("contentType", 1);
        if (!StringUtil.isEmpty(room_jid_id))
            q.put("room_jid_id", room_jid_id);
        logger.info("消息 总条数" + dbCollection.find(q).count());
        long total = dbCollection.count(q);
        List<DBObject> pageData = Lists.newArrayList();
        DBCursor cursor = dbCollection.find(q).sort(new BasicDBObject("_id", 1)).skip((page - 1) * limit).limit(limit);
        while (cursor.hasNext()) {
            BasicDBObject dbObj = (BasicDBObject) cursor.next();
            try {
                Map<?, ?> params = JSON.parseObject(dbObj.getString("body").replace("&quot;", "\""), Map.class);
                dbObj.put("content", params.get("content"));
                dbObj.put("fromUserName", params.get("fromUserName"));
            } catch (Exception e) {
                dbObj.put("content", "--");
            }
            pageData.add(dbObj);
        }
        PageResult<DBObject> result = new PageResult<>();
        result.setData(pageData);
        result.setCount(total);
        return JSONMessage.success(result);
    }

    /**
     * 直播间收到的礼物列表
     *
     * @param userId
     * @param startDate
     * @param endDate
     * @param page
     * @param limit
     * @return
     */
    @RequestMapping(value = "/getGiftList")
    public JSONMessage get(@RequestParam Integer userId, @RequestParam(defaultValue = "") String startDate,
                           @RequestParam(defaultValue = "") String endDate, @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int limit) {
        try {
            PageResult<Givegift> result = SKBeanUtils.getLiveRoomManager().getGiftList(userId, startDate, endDate, page,
                    limit);
            return JSONMessage.success(result);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    @RequestMapping(value = "/userList")
    public JSONMessage userList(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String onlinestate,
                                @RequestParam(defaultValue = "") String keyWorld, @RequestParam(defaultValue = "") String startDate,
                                @RequestParam(defaultValue = "") String endDate, Integer userType) {
        Query<User> query = getUserManager().createQuery();

        if (!StringUtil.isEmpty(keyWorld)) {
            // Integer 最大值2147483647
            boolean flag = NumberUtil.isNum(keyWorld);
            if (flag) {
                int length = keyWorld.length();
                if (length > 9) {
                    query.or(query.criteria("nickname").containsIgnoreCase(keyWorld),
                            query.criteria("telephone").containsIgnoreCase(keyWorld));
                } else {
                    query.or(query.criteria("nickname").containsIgnoreCase(keyWorld),
                            query.criteria("telephone").containsIgnoreCase(keyWorld),
                            query.criteria("_id").equal(Integer.valueOf(keyWorld)));
                }
            } else {
                query.or(query.criteria("nickname").containsIgnoreCase(keyWorld),
                        query.criteria("telephone").containsIgnoreCase(keyWorld));
            }
        }
        if (!StringUtil.isEmpty(onlinestate)) {
            query.filter("onlinestate", Integer.valueOf(onlinestate));
        }
        if (null != userType) {
            query.filter("userType", userType);
        }
        if (!StringUtil.isEmpty(startDate) && !StringUtil.isEmpty(endDate)) {
            long startTime = 0; //开始时间（秒）
            long endTime = 0; //结束时间（秒）,默认为当前时间
            startTime = StringUtil.isEmpty(startDate) ? 0 : DateUtil.toDate(startDate).getTime() / 1000;
            endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime() / 1000;
            long formateEndtime = DateUtil.getOnedayNextDay(endTime, 1, 0);
            query.field("createTime").greaterThan(startTime).field("createTime").lessThanOrEq(formateEndtime);
        }
        // 排序、分页
        List<User> pageData = query.order("-createTime").asList(getUserManager().pageFindOption(page, limit, 1));
        pageData.forEach(userInfo -> {
            Query<UserLoginLog> loginLog = SKBeanUtils.getDatastore().createQuery(UserLoginLog.class).field("userId")
                    .equal(userInfo.getUserId());
            if (null != loginLog.get())
                userInfo.setLoginLog(loginLog.get().getLoginLog());
        });
        PageResult<User> result = new PageResult<>();
        result.setData(pageData);
        result.setCount(query.count());
        return JSONMessage.success(result);
    }

    /**
     * 最新注册用户
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @SuppressWarnings("deprecation")
    @RequestMapping(value = "/newRegisterUser")
    public JSONMessage newRegisterUser(@RequestParam(defaultValue = "0") int pageIndex,
                                       @RequestParam(defaultValue = "10") int pageSize) {
        Query<User> query = getUserManager().createQuery();
        long total = query.count();
        List<User> pageData = query.order("-createTime").offset(pageIndex * pageSize).limit(pageSize).asList();
        PageVO page = new PageVO(pageData, total, pageIndex, pageSize);
        return JSONMessage.success(null, page);
    }

    /**
     * 重置密码
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/restPwd")
    public JSONMessage restPwd(@RequestParam(defaultValue = "0") Integer userId) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }
        if (0 < userId)
            getUserManager().resetPassword(userId, Md5Util.md5Hex("123456"));
        return JSONMessage.success();
    }

    /**
     * 修改admin密码
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/updateAdminPassword")
    public JSONMessage updatePassword(@RequestParam(defaultValue = "") String oldPassword, @RequestParam(defaultValue = "0") Integer userId, String password) {
        try {
            User user = SKBeanUtils.getUserManager().getUser(userId);
            if (user.getPassword().equals(oldPassword)) {
                getUserManager().resetPassword(userId, password);
                return JSONMessage.success();
            } else {
                return JSONMessage.failure("旧密码输入错误");
            }

        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 修改用户密码
     *
     * @param userId
     * @param password
     * @return
     */
    @RequestMapping(value = "/updateUserPassword")
    public JSONMessage updateUserPassword(@RequestParam(defaultValue = "0") Integer userId, String password) {
        try {
            getUserManager().resetPassword(userId, password);
            return JSONMessage.success();
        } catch (Exception e) {

            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 好友列表
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/friendsList")
    public JSONMessage friendsList(@RequestParam(defaultValue = "0") Integer userId,
                                   @RequestParam(defaultValue = "0") Integer toUserId, @RequestParam(defaultValue = "0") int status,
                                   @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
        try {
            PageResult<Friends> friendsList = SKBeanUtils.getFriendsManager().consoleQueryFollow(userId, toUserId,
                    status, page, limit);
            return JSONMessage.success(friendsList);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 删除好友
     *
     * @param userId
     * @param toUserIds
     * @return
     */
    @RequestMapping("/deleteFriends")
    public JSONMessage deleteFriends(@RequestParam(defaultValue = "0") Integer userId,
                                     @RequestParam(defaultValue = "") String toUserIds, @RequestParam(defaultValue = "") Integer adminUserId) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }

            if (StringUtil.isEmpty(toUserIds))
                JSONMessage.failure("参数为空");
            else {
                String[] toUserId = StringUtil.getStringList(toUserIds, ",");

                SKBeanUtils.getFriendsManager().getFriends(userId, toUserId);
                /*
                 * Friends friends =
                 * SKBeanUtils.getFriendsManager().getFriends(userId, toUserId);
                 * if (null == friends) return JSONMessage.failure("对方不是你的好友!");
                 */
                SKBeanUtils.getFriendsManager().consoleDeleteFriends(userId, adminUserId, toUserId);
            }
            return JSONMessage.success();

        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    @RequestMapping(value = "/getUpdateUser")
    public JSONMessage updateUser(@RequestParam(defaultValue = "0") Integer userId) {
        User user = null;
        if (0 == userId)
            user = new User();
        else {
            user = getUserManager().getUser(userId);
            List<Integer> userRoles = SKBeanUtils.getRoleManager().getUserRoles(userId);
            System.out.println("用户角色：" + JSONObject.toJSONString(userRoles));
            if (null != userRoles) {
                for (Integer role : userRoles) {
                    if (role.equals(2)) {
                        user.setUserType(2);
                    } else {
                        user.setUserType(0);
                    }
                }
            }
        }
        return JSONMessage.success(user);
    }

    @RequestMapping(value = "/updateUser")
    public JSONMessage saveUserMsg(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(defaultValue = "0") Integer userId, @ModelAttribute UserExample example) throws Exception {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }

        if (!StringUtil.isEmpty(example.getTelephone())) {
            example.setPhone(example.getTelephone());
            example.setTelephone(example.getAreaCode() + example.getTelephone());
        }
        // 后台注册用户(后台注册传的密码没有加密，这里进行加密)
        if (!StringUtil.isEmpty(example.getPassword()))
            example.setPassword(DigestUtils.md5Hex(example.getPassword()));

        Object uid;
        // 保存到数据库
        if (0 == userId) {
            Map<String, Object> saveUser = getUserManager().registerIMUser(example);
            uid = saveUser.get("userId");
        } else {
            getUserManager().updateUser(userId, example);
            uid = userId;
            // 修改好友关系表中的toUserType
//			SKBeanUtils.getRoleManager().updateFriend(userId, example.getUserType());
        }

        return JSONMessage.success(uid);
    }

    /**
     * @param pageIndex
     * @param pageSize
     * @return
     * @Description:（红包记录）
     **/
    @RequestMapping("/redPacketList")
    public JSONMessage getRedPacketList(@RequestParam(defaultValue = "") String userName,
                                        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String redPacketId) {
        try {
            PageResult<RedPacket> result = SKBeanUtils.getRedPacketManager().getRedPacketList(userName, page, limit, redPacketId);
            return JSONMessage.success(result);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getErrMessage());
        }
    }

    @RequestMapping("/receiveWater")
    public JSONMessage receiveWater(@RequestParam(defaultValue = "") String redId,
                                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
        try {
            PageResult<RedReceive> result = SKBeanUtils.getRedPacketManager().receiveWater(redId, page, limit);
            return JSONMessage.success(result);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getErrMessage());
        }
    }

    @RequestMapping(value = "/editRoom")
    public ModelAndView addRomm(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(defaultValue = "") String id) throws Exception {
        ModelAndView mav = new ModelAndView("editRoom");
        if (StringUtil.isEmpty(id)) {
            mav.addObject("o", new Room());
            mav.addObject("action", "addRoom");
        } else {
            mav.addObject("o", getRoomManagerImplForIM().getRoom(parse(id)));
            mav.addObject("action", "updateRoom");
        }

        return mav;
    }

    @RequestMapping(value = "/addRoom")
    public JSONMessage addRomm(HttpServletRequest request, HttpServletResponse response, @ModelAttribute Room room,
                               @RequestParam(defaultValue = "") String ids) throws Exception {
        List<Integer> idList = StringUtil.isEmpty(ids) ? null : JSON.parseArray(ids, Integer.class);
        if (null == room.getId()) {
            User user = getUserManager().getUser(room.getUserId());
            String jid = SKBeanUtils.getXmppService().createMucRoom(user.getPassword(), user.getUserId().toString(),
                    room.getName(), null, room.getSubject(), room.getDesc());
            room.setJid(jid);
            getRoomManagerImplForIM().add(user, room, idList);
        }

        return JSONMessage.success();
    }

    @RequestMapping(value = "/updateRoom")
    public JSONMessage updateRoom(HttpServletRequest request, HttpServletResponse response,
                                  @ModelAttribute RoomVO roomVo) throws Exception {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }

            User user = getUserManager().get(roomVo.getUserId());
            if (null == user)
                return JSONMessage.failure("操作失败");
            getRoomManagerImplForIM().update(user, roomVo, 1, 1);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

        return JSONMessage.success();
    }

    @RequestMapping(value = "/roomUserManager")
    public JSONMessage roomUserManager(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String id) {

        try {
            PageResult<Member> result = null;
            if (!StringUtil.isEmpty(id)) {
                result = getRoomManagerImplForIM().getMemberListByPage(new ObjectId(id), page, limit);
            }
            return JSONMessage.success(result);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    @RequestMapping(value = "/roomMemberList")
    public JSONMessage roomMemberList(@RequestParam String id) {
        Object data = getRoomManagerImplForIM().getMemberList(new ObjectId(id), ReqUtil.getUserId(), "");
        return JSONMessage.success(null, data);
    }

    @RequestMapping(value = "/sendMessage", method = {RequestMethod.POST})
    public ModelAndView sendMssage(@RequestParam String body, Integer from, Integer to, Integer count) {
        ModelAndView mav = new ModelAndView("qf");
        try {

            logger.info("body=======>  " + body);
            // String msg = new String(body.getBytes("iso8859-1"),"utf-8");
            if (null == from) {
                List<Friends> uList = SKBeanUtils.getFriendsManager().queryFriendsList(to, 0, 0, count);
                new Thread(() -> {
                    User user = null;
                    MessageBean messageBean = null;
                    for (Friends friends : uList) {
                        try {
                            user = getUserManager().getUser(friends.getToUserId());
                            messageBean = new MessageBean();
                            messageBean.setType(1);
                            messageBean.setContent(body);
                            messageBean.setFromUserId(user.getUserId() + "");
                            messageBean.setFromUserName(user.getNickname());
                            messageBean.setMessageId(UUID.randomUUID().toString());
                            messageBean.setToUserId(to.toString());
                            messageBean.setMsgType(0);
                            messageBean.setMessageId(StringUtil.randomUUID());
                            KXMPPServiceImpl.getInstance().send(messageBean);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                new Thread(() -> {
                    User user = getUserManager().get(from);
                    MessageBean messageBean = new MessageBean();
                    messageBean.setContent(body);
                    messageBean.setFromUserId(user.getUserId().toString());
                    messageBean.setFromUserName(user.getNickname());
                    messageBean.setToUserId(to.toString());
                    messageBean.setMsgType(0);
                    messageBean.setMessageId(StringUtil.randomUUID());
                    KXMPPServiceImpl.getInstance().send(messageBean);
                }).start();
            }
            return mav;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mav;
    }

    @RequestMapping(value = "/addAllUser")
    public JSONMessage updateTigaseDomain() throws Exception {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }

        Cursor attach = dsForRW.getDB().getCollection("user").find();
        String userId = "";
        String password = "";
        while (attach.hasNext()) {
            DBObject fileobj = attach.next();
            DBObject ref = new BasicDBObject();
            ref.put("user_id", fileobj.get("_id") + "@" + SKBeanUtils.getXMPPConfig().getServerName());
            DBObject obj = dsForTigase.getDB().getCollection("tig_users").findOne(ref);
            userId = fileobj.get("_id").toString().replace(".", "0");
            password = fileobj.get("password").toString();
            if (null != obj) {
                logger.info(fileobj.get("_id").toString() + "  已注册");
            } else {
                String user_id = userId + "@" + SKBeanUtils.getXMPPConfig().getServerName();
                BasicDBObject jo = new BasicDBObject();
                jo.put("_id", generateId(user_id));
                jo.put("user_id", user_id);
                jo.put("domain", SKBeanUtils.getXMPPConfig().getServerName());
                jo.put("password", password);
                jo.put("type", "shiku");
                dsForTigase.getDB().getCollection("tig_users").save(jo);
                logger.info(user_id + "  注册到Tigase：" + SKBeanUtils.getXMPPConfig().getServerName());
            }
        }
        return JSONMessage.success();
    }

    private byte[] generateId(String username) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(username.getBytes());
    }

    @RequestMapping(value = "/initSysUser")
    public JSONMessage initSysUser(@RequestParam(defaultValue = "0") int userId) throws Exception {
        if (0 < userId) {
            getUserManager().addUser(userId, String.valueOf(userId));

            KXMPPServiceImpl.getInstance().registerSystemNo(String.valueOf(userId),
                    Md5Util.md5Hex(String.valueOf(userId)));
        } else {
            // SKBeanUtils.getAdminManager().initSystemNo();
        }
        return JSONMessage.success();
    }

    /**
     * 直播间列表
     *
     * @param name
     * @param nickName
     * @param userId
     * @param page
     * @param limit
     * @param status
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/liveRoomList")
    public JSONMessage liveRoomList(@RequestParam(defaultValue = "") String name,
                                    @RequestParam(defaultValue = "") String nickName, @RequestParam(defaultValue = "0") Integer userId,
                                    @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer limit,
                                    @RequestParam(defaultValue = "-1") Integer status) throws Exception {
        PageResult<LiveRoom> result = new PageResult<>();
        try {
            result = getLiveRoomManager().findConsoleLiveRoomList(name, nickName, userId, page, limit, status, 1);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
        return JSONMessage.success(result);
    }

    @RequestMapping(value = "/addLiveRoom", method = {RequestMethod.GET})
    public ModelAndView addLiveRoom() {
        ModelAndView mav = new ModelAndView("addliveRoom");
        mav.addObject("o", new LiveRoom());
        return mav;
    }

    /**
     * 保存新增直播间
     *
     * @param request
     * @param response
     * @param liveRoom
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/saveNewLiveRoom", method = {RequestMethod.POST})
    public JSONMessage saveNewLiveRoom(HttpServletRequest request, HttpServletResponse response, LiveRoom liveRoom) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            User user = getUserManager().getUser(liveRoom.getUserId());
            String jid = SKBeanUtils.getXmppService().createMucRoom(user.getPassword(), user.getUserId().toString(),
                    liveRoom.getName(), null, liveRoom.getNotice(), liveRoom.getNotice());
            liveRoom.setJid(jid);
            getLiveRoomManager().createLiveRoom(liveRoom);
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }

        return JSONMessage.success();
    }

    /**
     * 删除直播间
     *
     * @param liveRoomId
     * @return
     */
    @RequestMapping(value = "/deleteLiveRoom", method = {RequestMethod.POST})
    public JSONMessage deleteLiveRoom(@RequestParam String liveRoomId) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }

            DBCollection dbCollection = dsForTigase.getCollection(LiveRoom.class);
            getLiveRoomManager().deleteLiveRoom(new ObjectId(liveRoomId));
            /* liveRoomManager.deleteLiveRoom(new ObjectId(liveRoomId)); */
            dbCollection.remove(new BasicDBObject("_id", new ObjectId(liveRoomId)));
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 锁定、解锁直播间
     *
     * @param liveRoomId
     * @param currentState
     * @return
     */
    @RequestMapping(value = "/operationLiveRoom")
    public JSONMessage operationLiveRoom(@RequestParam String liveRoomId,
                                         @RequestParam(defaultValue = "0") int currentState) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }

            getLiveRoomManager().operationLiveRoom(new ObjectId(liveRoomId), currentState);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 查询直播间人员
     *
     * @param pageIndex
     * @param name
     * @param nickName
     * @param userId
     * @param pageSize
     * @param roomId
     * @return
     */
    @RequestMapping(value = "/liveRoomUserManager")
    public JSONMessage liveRoomManager(@RequestParam(defaultValue = "0") int pageIndex,
                                       @RequestParam(defaultValue = "") String name, @RequestParam(defaultValue = "") String nickName,
                                       @RequestParam(defaultValue = "0") Integer userId, @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(defaultValue = "") String roomId) {
        List<LiveRoomMember> pageData = Lists.newArrayList();
        pageData = getLiveRoomManager().findLiveRoomMemberList(new ObjectId(roomId));
        PageResult<LiveRoomMember> result = new PageResult<>();
        result.setData(pageData);
        result.setCount(pageData.size());
        return JSONMessage.success(result);
    }

    /**
     * 删除直播间成员
     *
     * @param userId
     * @param liveRoomId
     * @param response
     * @param pageIndex
     * @return
     */
    @RequestMapping(value = "/deleteRoomUser")
    public JSONMessage deleteliveRoomUserManager(@RequestParam Integer userId,
                                                 @RequestParam(defaultValue = "") String liveRoomId, HttpServletResponse response,
                                                 @RequestParam(defaultValue = "0") int pageIndex) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            getLiveRoomManager().kick(userId, new ObjectId(liveRoomId));
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 禁言
     *
     * @param userId
     * @param state
     * @param roomId
     * @return
     */
    @RequestMapping(value = "/shutup")
    public JSONMessage shutup(@RequestParam Integer userId, @RequestParam int state, @RequestParam ObjectId roomId) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }

            LiveRoomMember shutup = getLiveRoomManager().shutup(state, userId, roomId);
            System.out.println(JSONObject.toJSONString(shutup));
            return JSONMessage.success(shutup);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 禁播
     */
    @RequestMapping(value = "/banplay")
    public void ban() {
        try {

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 礼物列表
     *
     * @param name
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/giftList")
    public JSONMessage giftList(@RequestParam(defaultValue = "") String name,
                                @RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Map<String, Object> pageData = getLiveRoomManager().consolefindAllgift(name, pageIndex, pageSize);
            if (null != pageData) {
                long total = (long) pageData.get("total");
                List<Gift> giftList = (List<Gift>) pageData.get("data");
                return JSONMessage.success(null, new PageVO(giftList, total, pageIndex, pageSize, total));
            }
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
        return null;
    }

    /**
     * 添加礼物
     *
     * @return
     */
    @RequestMapping(value = "/add/gift", method = {RequestMethod.GET})
    public ModelAndView getAddGiftPage() {
        ModelAndView mav = new ModelAndView("addGift");
        mav.addObject("o", new LiveRoom());
        return mav;
    }

    /**
     * 添加礼物
     *
     * @param request
     * @param response
     * @param name
     * @param photo
     * @param price
     * @param type
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/add/gift", method = {RequestMethod.POST})
    public JSONMessage addGift(HttpServletRequest request, HttpServletResponse response, @RequestParam String name,
                               @RequestParam String photo, @RequestParam double price, @RequestParam int type) throws IOException {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            getLiveRoomManager().addGift(name, photo, price, type);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 删除礼物
     *
     * @param giftId
     * @return
     */
    @RequestMapping(value = "/delete/gift")
    public JSONMessage deleteGift(@RequestParam String giftId) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }
        getLiveRoomManager().deleteGift(new ObjectId(giftId));
        return JSONMessage.success();

    }

    /**
     * 查询提示信息
     *
     * @param keyword
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/messageList")
    public JSONMessage messageList(String keyword, @RequestParam(defaultValue = "0") int pageIndex,
                                   @RequestParam(defaultValue = "10") int pageSize) {
        try {
            long totalNum = 0;
            Map<Long, List<ErrorMessage>> errorMessage = SKBeanUtils.getErrorMessageManage().findErrorMessage(keyword,
                    pageIndex, pageSize);
            if (null != errorMessage.keySet()) {
                for (Long total : errorMessage.keySet()) {
                    if (total == 0) {
                        return JSONMessage.success("暂无提示信息");
                    }
                    totalNum = total;
                }
            }
            return JSONMessage.success(null,
                    new PageVO(errorMessage.get(totalNum), totalNum, pageIndex, pageSize, totalNum));
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 新增提示消息
     *
     * @param errorMessage
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/saveErrorMessage")
    public JSONMessage saveErrorMessage(ErrorMessage errorMessage) throws IOException {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            if (null == errorMessage)
                return JSONMessage.failure("参数有误");
            return SKBeanUtils.getErrorMessageManage().saveErrorMessage(errorMessage);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 修改提示消息
     *
     * @param errorMessage
     * @param id
     * @return
     */
    @RequestMapping(value = "/messageUpdate", method = {RequestMethod.POST})
    public JSONMessage messageUpdate(ErrorMessage errorMessage, String id) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }
        if (null == errorMessage)
            return JSONMessage.failure("参数有误： errorMessage " + errorMessage);
        ErrorMessage data = SKBeanUtils.getErrorMessageManage().updataErrorMessage(id, errorMessage);
        if (null == data)
            return JSONMessage.failure("修改提示消息失败");
        else
            return JSONMessage.success("修改提示消息成功", data);
    }

    /**
     * 删除提示消息
     *
     * @param code
     * @return
     */
    @RequestMapping(value = "/deleteErrorMessage")
    public JSONMessage deleteErrorMessage(@RequestParam(defaultValue = "") String code) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }
        if (StringUtil.isEmpty(code))
            return JSONMessage.failure("参数有误,code: " + code);
        boolean falg = SKBeanUtils.getErrorMessageManage().deleteErrorMessage(code);
        if (!falg)
            return JSONMessage.failure("删除提示消息失败");
        else
            return JSONMessage.success();
    }

    /**
     * 关键词列表
     *
     * @param word
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/keywordfilter")
    public JSONMessage keywordfilter(@RequestParam(defaultValue = "") String word,
                                     @RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
        Query<KeyWord> query = dsForRW.createQuery(KeyWord.class);
        if (!StringUtil.isEmpty(word)) {
            query.filter("word", word);
        }
        List<KeyWord> list = null;
        long total = 0;
        list = query.order("-createTime").asList(new FindOptions().skip(pageIndex * pageSize).limit(pageSize));
        total = query.count();
        return JSONMessage.success(null, new PageVO(list, total, pageIndex, pageSize, total));
    }

    @RequestMapping("/sendMsg")
    public JSONMessage sendMsg(@RequestParam(defaultValue = "") String jidArr,
                               @RequestParam(defaultValue = "1") int userId, @RequestParam(defaultValue = "1") int type,
                               @RequestParam(defaultValue = "") String content, String objectId, @RequestParam(defaultValue = "0") int fileSize) {
        // 权限校验
		/*byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
		if(role!=KConstants.Admin_Role.SUPER_ADMIN&&role!=KConstants.Admin_Role.ADMIN){
			return JSONMessage.failure("权限不足");
		}*/
        String[] split = jidArr.split(",");

        SKBeanUtils.getRoomManagerImplForIM().sendMsgToRooms(split, userId, type, content, objectId, fileSize);

        return JSONMessage.success();

    }

    /**
     * 添加敏感词
     *
     * @param response
     * @param id
     * @param word
     * @throws IOException
     */
    @RequestMapping(value = "/addkeyword", method = {RequestMethod.POST})
    public void addkeyword(HttpServletResponse response, @RequestParam(defaultValue = "") String id,
                           @RequestParam String word) throws IOException {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return;
        }
        KeyWord keyword = null;
        if (StringUtil.isEmpty(id)) {
            keyword = new KeyWord();
            keyword.setWord(word);
            keyword.setCreateTime(DateUtil.currentTimeSeconds());
            dsForRW.save(keyword);
        } else {
            Query<KeyWord> query = dsForRW.createQuery(KeyWord.class);
            UpdateOperations<KeyWord> ops = dsForRW.createUpdateOperations(KeyWord.class);
            ops.set("word", word);
            ops.set("createTime", DateUtil.currentTimeSeconds());
            dsForRW.update(query, ops);
        }
        response.sendRedirect("/console/keywordfilter");
    }

    /**
     * 删除敏感词
     *
     * @param response
     * @param id
     * @return
     */
    @RequestMapping(value = "/deletekeyword", method = {RequestMethod.POST})
    public JSONMessage deletekeyword(HttpServletResponse response, @RequestParam String id) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }
        Query<KeyWord> query = dsForRW.createQuery(KeyWord.class);
        query.field("_id").equal(new ObjectId(id));
        dsForRW.delete(query.get());
        return JSONMessage.success();
    }

    /**
     * 删除聊天记录
     *
     * @param request
     * @param response
     * @param startTime
     * @param endTime
     * @param room_jid_id
     */
    @RequestMapping(value = "/deleteMsgGroup", method = {RequestMethod.POST})
    public void deleteMsgGroup(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(defaultValue = "0") long startTime, @RequestParam(defaultValue = "0") long endTime,
                               @RequestParam(defaultValue = "") String room_jid_id) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return;
        }
        if (room_jid_id != null) {
            DBCollection dbCollection = dsForRoom.getDB().getCollection("mucmsg_" + room_jid_id);
            BasicDBObject query = new BasicDBObject();
            if (0 != startTime) {
                query.put("ts", new BasicDBObject("$gte", startTime));
            }
            if (0 != endTime) {
                query.put("ts", new BasicDBObject("$gte", endTime));
            }
            DBCursor cursor = dbCollection.find(query);
            if (cursor.size() > 0) {

                BasicDBObject dbObj = (BasicDBObject) cursor.next();
                // 解析消息体

                Map body = JSON.parseObject(dbObj.getString("body").replace("&quot;", "\""), Map.class);
                int contentType = (int) body.get("type");

                if (contentType == 2 || contentType == 3 || contentType == 5 || contentType == 6 || contentType == 7
                        || contentType == 9) {
                    String paths = (String) body.get("content");
                    Query<Emoji> que = dsForRW.createQuery(Emoji.class);
                    List<Emoji> list = que.asList();
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getUrl() == paths) {
                            return;
                        } else {
                            try {
                                // 调用删除方法将文件从服务器删除
                                ConstantUtil.deleteFile(paths);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                dbCollection.remove(query); // 将消息记录中的数据删除
            }
        } else {
            List jidList = dsForRoom.getCollection(Room.class).distinct("jid", new BasicDBObject());

            for (int j = 0; j < jidList.size(); j++) {
                DBCollection dbCollection = dsForRoom.getDB().getCollection("mucmsg_" + jidList.get(j));
                BasicDBObject query = new BasicDBObject();
                if (0 != startTime) {
                    query.put("ts", new BasicDBObject("$gte", startTime));
                }
                if (0 != endTime) {
                    query.put("ts", new BasicDBObject("$gte", endTime));
                }
                DBCursor cursor = dbCollection.find(query);
                if (cursor.size() > 0) {
                    BasicDBObject dbObj = (BasicDBObject) cursor.next();
                    // 解析消息体
                    Map body = JSON.parseObject(dbObj.getString("body").replace("&quot;", "\""),
                            Map.class);
                    int contentType = (int) body.get("type");

                    if (contentType == 2 || contentType == 3 || contentType == 5 || contentType == 6 || contentType == 7
                            || contentType == 9) {
                        String paths = (String) body.get("content");
                        Query<Emoji> que = dsForRW.createQuery(Emoji.class);
                        List<Emoji> list = que.asList();
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).getUrl() == paths) {
                                return;
                            } else {
                                try {
                                    // 调用删除方法将文件从服务器删除
                                    ConstantUtil.deleteFile(paths);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    dbCollection.remove(query); // 将消息记录中的数据删除
                }
            }

        }

        referer(response, "/console/groupchat_logs_all?room_jid_id=" + room_jid_id, 0);
    }

    /**
     * 后台自动创建用户
     *
     * @param userNum 需要生成的数量
     * @param roomId
     */
    @RequestMapping(value = "/autoCreateUser")
    public JSONMessage autoCreateUser(@RequestParam(defaultValue = "0") int userNum,
                                      @RequestParam(defaultValue = "") String roomId) {
        try {
            if (userNum > 0)
                getUserManager().autoCreateUserOrRoom(userNum, roomId);
            else
                return JSONMessage.failure("至少输入1个");
            return JSONMessage.success();
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 导出自动创建的用户数据到 excel
     */
    @RequestMapping(value = "/exportData")
    public JSONMessage exportData(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(defaultValue = "3") short userType) {
        String fileName = "users.xlsx";

        int maxNum = 30000; // 最多导出3万条数据
        short onlinestate = -1;

        List<User> userList = getUserManager().findUserList(0, maxNum, "", onlinestate, userType);

        String name = "系统自动创建的账号";
        List<String> titles = Lists.newArrayList();
        titles.add("userId");
        titles.add("nickname");
        titles.add("telephone");
        titles.add("password");
        titles.add("sex");
        titles.add("createTime");

        List<Map<String, Object>> values = Lists.newArrayList();
        for (User user : userList) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("userId", user.getUserId());
            map.put("nickname", user.getNickname());
            map.put("telephone", user.getTelephone());
            map.put("password", user.getUserType() == 3 ? "" + (user.getUserId() - 1000) / 2 : user.getPassword());
            map.put("sex", user.getSex() == 1 ? "女" : "男");
            map.put("createTime", Calendar.getInstance());
            values.add(map);
        }

        Workbook workBook = ExcelUtil.generateWorkbook(name, "xlsx", titles, values);
        try {
            response.reset();
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + new String(fileName.getBytes(), StandardCharsets.UTF_8));
            ServletOutputStream out = response.getOutputStream();
            workBook.write(out);
            // 弹出下载对话框
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return JSONMessage.success();
    }

    @RequestMapping(value = "/exportExcelByFriends")
    public JSONMessage exportExcelByFriends(HttpServletRequest request, HttpServletResponse response,
                                            @RequestParam(defaultValue = "0") Integer userId) {
        try {
            Workbook workBook = SKBeanUtils.getFriendsManager().exprotExcelFriends(userId, request, response);
            ServletOutputStream out = response.getOutputStream();
            workBook.write(out);
            // 弹出下载对话框
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return JSONMessage.success();
    }

    /**
     * @param request
     * @param response
     * @param roomId
     * @return
     * @Description:导出群成员
     **/
    @RequestMapping(value = "/exportExcelByGroupMember")
    public JSONMessage exportExcelByGroupMember(HttpServletRequest request, HttpServletResponse response,
                                                @RequestParam(defaultValue = "") String roomId) {
        try {
            Workbook workBook = SKBeanUtils.getRoomManagerImplForIM().exprotExcelGroupMembers(roomId, request,
                    response);
            ServletOutputStream out = response.getOutputStream();
            workBook.write(out);
            // 弹出下载对话框
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return JSONMessage.success();
    }

    /**
     * 统计用户注册信息
     */
    @RequestMapping(value = "/getUserRegisterCount")
    public JSONMessage getUserRegisterCount(@RequestParam(defaultValue = "0") int pageIndex,
                                            @RequestParam(defaultValue = "100") int pageSize, @RequestParam(defaultValue = "2") short timeUnit,
                                            @RequestParam(defaultValue = "") String startDate, @RequestParam(defaultValue = "") String endDate) {

        try {

            Object data = getUserManager().getUserRegisterCount(startDate.trim(), endDate.trim(), timeUnit);
            return JSONMessage.success(null, data);

        } catch (MongoCommandException e) {
            return JSONMessage.success(0);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 用户，群组，单聊消息，好友关系数量 统计
     */
    @RequestMapping(value = "/countNum")
    public JSONMessage countNum(HttpServletRequest request, HttpServletResponse response) {

        try {
            long userNum = getUserManager().count();
            long roomNum = getRoomManagerImplForIM().countRoomNum();
            long msgNum = SKBeanUtils.getTigaseManager().getMsgCountNum();
            long friendsNum = SKBeanUtils.getFriendsManager().count();

            Map<String, Long> dataMap = new HashMap<>();
            dataMap.put("userNum", userNum);
            dataMap.put("roomNum", roomNum);
            dataMap.put("msgNum", msgNum);
            dataMap.put("friendsNum", friendsNum);
            return JSONMessage.success(null, dataMap);

        } catch (MongoCommandException e) {
            return JSONMessage.success(0);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 统计单聊消息数量
     */
    @RequestMapping(value = "/chatMsgCount")
    public JSONMessage chatMsgCount(@RequestParam(defaultValue = "0") int pageIndex,
                                    @RequestParam(defaultValue = "100") int pageSize, @RequestParam(defaultValue = "2") short timeUnit,
                                    @RequestParam(defaultValue = "") String startDate, @RequestParam(defaultValue = "") String endDate) {

        try {

            Object data = SKBeanUtils.getTigaseManager().getChatMsgCount(startDate.trim(), endDate.trim(), timeUnit);
            return JSONMessage.success(null, data);

        } catch (MongoCommandException e) {
            return JSONMessage.success(0);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 统计群聊聊消息数量
     */
    @RequestMapping(value = "/groupMsgCount")
    public JSONMessage groupMsgCount(@RequestParam String roomId, @RequestParam(defaultValue = "0") int pageIndex,
                                     @RequestParam(defaultValue = "100") int pageSize, @RequestParam(defaultValue = "2") short timeUnit,
                                     @RequestParam(defaultValue = "") String startDate, @RequestParam(defaultValue = "") String endDate) {

        try {

            Object data = SKBeanUtils.getTigaseManager().getGroupMsgCount(roomId, startDate.trim(), endDate.trim(),
                    timeUnit);
            return JSONMessage.success(null, data);

        } catch (MongoCommandException e) {
            return JSONMessage.success(0);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 统计添加好友数量
     */
    @RequestMapping(value = "/addFriendsCount")
    public JSONMessage addFriendsCount(@RequestParam(defaultValue = "0") int pageIndex,
                                       @RequestParam(defaultValue = "100") int pageSize, @RequestParam(defaultValue = "2") short timeUnit,
                                       @RequestParam(defaultValue = "") String startDate, @RequestParam(defaultValue = "") String endDate) {

        try {

            Object data = SKBeanUtils.getFriendsManager().getAddFriendsCount(startDate.trim(), endDate.trim(),
                    timeUnit);
            return JSONMessage.success(null, data);

        } catch (MongoCommandException e) {
            return JSONMessage.success(0);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 统计创建群组数量
     */
    @RequestMapping(value = "/addRoomsCount")
    public JSONMessage addRoomsCount(@RequestParam(defaultValue = "0") int pageIndex,
                                     @RequestParam(defaultValue = "100") int pageSize, @RequestParam(defaultValue = "2") short timeUnit,
                                     @RequestParam(defaultValue = "") String startDate, @RequestParam(defaultValue = "") String endDate) {

        try {

            Object data = SKBeanUtils.getRoomManager().addRoomsCount(startDate.trim(), endDate.trim(), timeUnit);
            return JSONMessage.success(null, data);

        } catch (MongoCommandException e) {
            return JSONMessage.success(0);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 用户在线数量统计
     *
     * @param pageIndex
     * @param pageSize
     * @param sign
     * @param startDate
     * @param endDate
     * @param timeUnit
     * @throws Exception
     */
    @RequestMapping(value = "/getUserStatusCount")
    public JSONMessage getUserStatusCount(@RequestParam(defaultValue = "0") int pageIndex,
                                          @RequestParam(defaultValue = "100") int pageSize, @RequestParam(defaultValue = "2") short timeUnit,
                                          @RequestParam(defaultValue = "") String startDate, @RequestParam(defaultValue = "") String endDate)
            throws Exception {

        try {

            Object data = SKBeanUtils.getUserManager().userOnlineStatusCount(startDate.trim(), endDate.trim(),
                    timeUnit);
            return JSONMessage.success(null, data);

        } catch (MongoCommandException e) {
            return JSONMessage.success(0);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param type     (type = 0查询被举报的用户,type=1查询被举报的群主,type=2查询被举报的网页)
     * @param pageSize
     * @return
     * @Description:（被举报的用户和群组列表）
     **/
    @SuppressWarnings("static-access")
    @RequestMapping(value = "/beReport")
    public JSONMessage beReport(@RequestParam(defaultValue = "0") int type,
                                @RequestParam(defaultValue = "0") int sender, @RequestParam(defaultValue = "") String receiver,
                                @RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "25") int pageSize) {
        Map<String, Object> dataMap = Maps.newConcurrentMap();
        JSONMessage jsonMessage = new JSONMessage();
        try {
            dataMap = SKBeanUtils.getUserManager().getReport(type, sender, receiver, pageIndex, pageSize);
            logger.info("举报详情：" + JSONObject.toJSONString(dataMap.get("data")));
            if (!dataMap.isEmpty()) {
                List<Report> reportList = (List<Report>) dataMap.get("data");
                long total = (long) dataMap.get("count");
                return jsonMessage.success(null, new PageVO(reportList, total, pageIndex, pageSize, total));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return jsonMessage.failure(e.getMessage());
        }
        return jsonMessage;

    }

    @RequestMapping("/isLockWebUrl")
    public JSONMessage isLockWebUrl(@RequestParam(defaultValue = "") String webUrlId, @RequestParam(defaultValue = "-1") int webStatus) {
        if (StringUtil.isEmpty(webUrlId))
            return JSONMessage.failure("webUrl is null");
        Query<Report> query = SKBeanUtils.getDatastore().createQuery(Report.class).field("_id").equal(new ObjectId(webUrlId));
        if (null == query.get())
            return JSONMessage.failure("暂无该链接的举报数据");
        UpdateOperations<Report> ops = SKBeanUtils.getDatastore().createUpdateOperations(Report.class);
        ops.set("webStatus", webStatus);
        SKBeanUtils.getDatastore().update(query, ops);
        return JSONMessage.success();
    }

    /**
     * 删除举报
     *
     * @param response
     * @param id
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/deleteReport")
    public JSONMessage deleteReport(HttpServletResponse response, @RequestParam String id) throws IOException {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }

        BasicDBObject query = new BasicDBObject("_id", parse(id));
        getUserManager().getDatastore().getDB().getCollection("Report").remove(query);
        return JSONMessage.success();
    }

    /**
     * api 调用日志
     *
     * @param keyWorld
     * @param page
     * @param limit
     * @return
     */
    @RequestMapping(value = "/ApiLogList")
    public JSONMessage apiLogList(@RequestParam(defaultValue = "") String keyWorld,
                                  @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "15") int limit) {

        try {
            PageResult<SysApiLog> data = SKBeanUtils.getAdminManager().apiLogList(keyWorld, page, limit);
            return JSONMessage.success(data);

        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 删除 api 日志
     *
     * @param apiLogId
     * @param type
     * @return
     */
    @RequestMapping(value = "/delApiLog")
    public JSONMessage delApiLog(@RequestParam(defaultValue = "") String apiLogId,
                                 @RequestParam(defaultValue = "0") int type) {

        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }

            SKBeanUtils.getAdminManager().deleteApiLog(apiLogId, type);
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param limit
     * @param page
     * @return
     * @Description:（后台朋友圈列表）
     **/
    @RequestMapping(value = "/getFriendsMsgList")
    public JSONMessage getFriendsMsgList(@RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "10") Integer limit, @RequestParam(defaultValue = "") String nickname,
                                         @RequestParam(defaultValue = "0") Integer userId) {
        try {
            PageResult<Msg> data = SKBeanUtils.getMsgRepository().getMsgList(page, limit, nickname, userId);
            return JSONMessage.success(data);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param userId
     * @param messageId
     * @return
     * @Description:（删除朋友圈）
     **/
    @RequestMapping(value = "/deleteFriendsMsg")
    public JSONMessage deleteMsg(@RequestParam String messageId) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }

        JSONMessage jMessage;
        if (StringUtil.isEmpty(messageId)) {
            jMessage = Result.ParamsAuthFail;
        } else {
            try {
                String[] messageIds = StringUtil.getStringList(messageId);
                boolean ok = SKBeanUtils.getMsgRepository().delete(messageIds);
                jMessage = ok ? JSONMessage.success() : JSONMessage.failure(null);
            } catch (Exception e) {
                logger.error("删除商务圈消息失败", e);
                jMessage = JSONMessage.error(e);
            }
        }
        return jMessage;
    }

    /**
     * @param userId
     * @param state
     * @param roomId
     * @return
     * @Description:（锁定朋友圈）
     **/
    @RequestMapping(value = "/lockingMsg")
    public JSONMessage lockingMsg(@RequestParam String msgId, @RequestParam int state) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }

            SKBeanUtils.getMsgRepository().lockingMsg(new ObjectId(msgId), state);
            return JSONMessage.success();
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param userId
     * @param state
     * @param roomId
     * @return
     * @Description:（朋友圈评论）
     **/
    @RequestMapping(value = "/commonListMsg")
    public JSONMessage commonListMsg(@RequestParam String msgId, @RequestParam(defaultValue = "0") Integer page,
                                     @RequestParam(defaultValue = "10") Integer limit) {
        try {
            PageResult<Comment> result = SKBeanUtils.getMsgRepository().commonListMsg(new ObjectId(msgId), page, limit);
            return JSONMessage.success(result);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param userId
     * @param state
     * @param roomId
     * @return
     * @Description:（朋友圈点赞）
     **/
    @RequestMapping(value = "/praiseListMsg")
    public JSONMessage praiseListMsg(@RequestParam String msgId, @RequestParam(defaultValue = "0") Integer page,
                                     @RequestParam(defaultValue = "10") Integer limit) {
        try {
            PageResult<Praise> result = SKBeanUtils.getMsgRepository().praiseListMsg(new ObjectId(msgId), page, limit);
            return JSONMessage.success(result);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param messageId
     * @param commentId
     * @return
     * @Description:（删除评论）
     **/
    @RequestMapping(value = "/comment/delete")
    public JSONMessage deleteComment(@RequestParam String messageId, @RequestParam String commentId) {
        JSONMessage jMessage;
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }
        if (StringUtil.isEmpty(messageId) || StringUtil.isEmpty(commentId)) {
            jMessage = Result.ParamsAuthFail;
        } else {
            try {
                boolean ok = SKBeanUtils.getMsgCommentRepository().delete(new ObjectId(messageId), commentId);
                jMessage = ok ? JSONMessage.success() : JSONMessage.failure(null);
            } catch (Exception e) {
                logger.error("删除评论失败", e);
                jMessage = JSONMessage.error(e);
            }
        }
        return jMessage;
    }

    /**
     * @param userId
     * @param status
     * @return
     * @Description:（用户账号锁定解锁）
     **/
    @RequestMapping("/changeStatus")
    public JSONMessage changeStatus(@RequestParam int userId, @RequestParam int status) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
            return JSONMessage.failure("权限不足");
        }
        getUserManager().changeStatus(userId, status);
        return JSONMessage.success();
    }

    /**
     * @param userId
     * @param status
     * @param type
     * @return
     * @Description:（系统充值记录）
     **/
    @RequestMapping("/systemRecharge")
    public JSONMessage systemRecharge(@RequestParam(defaultValue = "0") int userId,
                                      @RequestParam(defaultValue = "0") int type, @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String startDate,
                                      @RequestParam(defaultValue = "") String endDate, @RequestParam(defaultValue = "") String tradeNo) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.FINANCE) {
                return JSONMessage.failure("权限不足");
            }
            PageResult<ConsumeRecord> result = SKBeanUtils.getConsumeRecordManager().recharge(userId, type, page, limit,
                    startDate, endDate, tradeNo);
            return JSONMessage.success(result);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getErrMessage());
        }
    }

    /**
     * 后台充值
     *
     * @param money
     * @param type
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/Recharge")
    public JSONMessage Recharge(Double money, int userId) throws Exception {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN) {
            return JSONMessage.failure("权限不足");
        }
		/*if(1 != money)
			return JSONMessage.failure("每次只能充值一元");*/
        // 核验用户是否存在
        if (null == getUserManager().getUser(userId)) {
            return JSONMessage.failure("充值失败, 用户不存在!");
        }

        String tradeNo = StringUtil.getOutTradeNo();

//		Map<String, Object> data = Maps.newHashMap();
        // 创建充值记录
        ConsumeRecord record = new ConsumeRecord();
        record.setUserId(userId);
        record.setTradeNo(tradeNo);
        record.setMoney(money);
        record.setStatus(KConstants.OrderStatus.END);
        record.setType(KConstants.ConsumeType.SYSTEM_RECHARGE);
        record.setPayType(KConstants.PayType.SYSTEMPAY); // type = 3 ：管理后台充值
        record.setDesc("后台余额充值");
        record.setTime(DateUtil.currentTimeSeconds());
        record.setOperationAmount(money);
        try {
            Double balance = getUserManager().rechargeUserMoeny(userId, money, KConstants.MOENY_ADD);
            record.setCurrentBalance(balance);
            SKBeanUtils.getConsumeRecordManager().save(record);
//			data.put("balance", balance);
            return JSONMessage.success(balance);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * @param money
     * @param userId
     * @return
     * @Description:手工提现
     **/
    @RequestMapping("/handCash")
    public JSONMessage handCash(Double money, int userId) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN) {
            return JSONMessage.failure("权限不足");
        }
		/*if(1 != money)
			return JSONMessage.failure("每次只能提现一元");*/
        // 核验用户是否存在
        if (null == getUserManager().getUser(userId)) {
            return JSONMessage.failure("提现失败, 用户不存在!");
        } else {
            Double balance = getUserManager().getUser(userId).getBalance();
            if (balance < money)
                return JSONMessage.failure("余额不足");
        }
        String tradeNo = StringUtil.getOutTradeNo();
        // 创建充值记录
        ConsumeRecord record = new ConsumeRecord();
        record.setUserId(userId);
        record.setTradeNo(tradeNo);
        record.setMoney(money);
        record.setStatus(KConstants.OrderStatus.END);
        record.setType(KConstants.ConsumeType.SYSTEM_HANDCASH);
        record.setPayType(KConstants.PayType.SYSTEMPAY); // type = 3 ：管理后台充值
        record.setDesc("后台手工提现");
        record.setTime(DateUtil.currentTimeSeconds());
        record.setOperationAmount(money);
        try {
            Double balance = getUserManager().rechargeUserMoeny(userId, money, KConstants.MOENY_REDUCE);
            record.setCurrentBalance(balance);
            SKBeanUtils.getConsumeRecordManager().save(record);
            return JSONMessage.success(balance);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 用户账单
     *
     * @param money
     * @param type
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/userBill")
    public JSONMessage userBill(@RequestParam int userId, int page, int limit, @RequestParam(defaultValue = "") String startDate,
                                @RequestParam(defaultValue = "") String endDate, @RequestParam(defaultValue = "0") int type) throws Exception {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.FINANCE) {
                return JSONMessage.failure("权限不足");
            }
            // 核验用户是否存在
            if (null == getUserManager().getUser(userId)) {
                return JSONMessage.failure("用户不存在!");
            }
            PageResult<DBObject> result = SKBeanUtils.getConsumeRecordManager().consumeRecordList(userId, page,
                    limit, (byte) 1, startDate, endDate, type);
            return JSONMessage.success(result);

        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    @RequestMapping(value = "/consumeRecordInfo")
    public JSONMessage consumeRecordInfo(String tradeNo) {
        try {
            PageResult<ConsumeRecord> recordInfo = SKBeanUtils.getConsumeRecordManager().getConsumeRecordByTradeNo(tradeNo);
            return JSONMessage.success(recordInfo);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 添加服务器
     *
     * @param server
     * @return
     */
    @RequestMapping(value = "/addServerList")
    public JSONMessage addServerList(@ModelAttribute ServerListConfig server) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().addServerList(server);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 获取服务器列表
     *
     * @param id
     * @param pageIndex
     * @param limit
     * @return
     */
    @RequestMapping(value = "/serverList")
    public JSONMessage serverList(@RequestParam(defaultValue = "") String id,
                                  @RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int limit) {
        PageResult<ServerListConfig> result = SKBeanUtils.getAdminManager()
                .getServerList((!StringUtil.isEmpty(id) ? new ObjectId(id) : null), pageIndex, limit);
        return JSONMessage.success(null, result);
    }

    @RequestMapping(value = "/findServerByArea")
    public JSONMessage findServerByArea(@RequestParam(defaultValue = "") String area) {
        PageResult<ServerListConfig> result = SKBeanUtils.getAdminManager().findServerByArea(area);
        return JSONMessage.success(null, result);
    }

    /**
     * 修改服务器
     *
     * @param server
     * @return
     */
    @RequestMapping(value = "/updateServer")
    public JSONMessage updateServer(@ModelAttribute ServerListConfig server) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().updateServer(server);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 删除服务器
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteServer")
    public JSONMessage deleteServer(@RequestParam String id) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().deleteServer(new ObjectId(id));
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 地区配置列表
     *
     * @param area
     * @param pageIndex
     * @param limit
     * @return
     */
    @RequestMapping(value = "/areaConfigList")
    public JSONMessage areaConfigList(@RequestParam(defaultValue = "") String area,
                                      @RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int limit) {
        PageResult<AreaConfig> result = SKBeanUtils.getAdminManager().areaConfigList(area, pageIndex, limit);
        return JSONMessage.success(result);
    }

    /**
     * 添加地区配置
     *
     * @param area
     * @return
     */
    @RequestMapping(value = "/addAreaConfig")
    public JSONMessage addAreaConfig(@ModelAttribute AreaConfig area) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().addAreaConfig(area);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 修改地区配置
     *
     * @param area
     * @return
     */
    @RequestMapping(value = "/updateAreaConfig")
    public JSONMessage updateAreaConfig(@ModelAttribute AreaConfig area) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().updateAreaConfig(area);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 删除地区配置
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteAreaConfig")
    public JSONMessage deleteAreaConfig(@RequestParam String id) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().deleteAreaConfig(new ObjectId(id));
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 添加入口配置
     *
     * @param urlConfig
     * @return
     */
    @RequestMapping(value = "/addUrlConfig")
    public JSONMessage addUrlConfig(@ModelAttribute UrlConfig urlConfig) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().addUrlConfig(urlConfig);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 查询入口配置
     *
     * @param id
     * @param type
     * @return
     */
    @RequestMapping(value = "/findUrlConfig")
    public JSONMessage findUrlConfig(@RequestParam(defaultValue = "") String id,
                                     @RequestParam(defaultValue = "") String type) {
        PageResult<UrlConfig> result = SKBeanUtils.getAdminManager()
                .findUrlConfig((!StringUtil.isEmpty(id) ? new ObjectId(id) : null), type);
        return JSONMessage.success(null, result);

    }

    /**
     * 删除入口
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteUrlConfig")
    public JSONMessage deleteUrlConfig(@RequestParam(defaultValue = "") String id) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().deleteUrlConfig(!StringUtil.isEmpty(id) ? new ObjectId(id) : null);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 中心服务器
     *
     * @param centerConfig
     * @return
     */
    @RequestMapping(value = "/addcenterConfig")
    public JSONMessage addCenterConfig(@ModelAttribute CenterConfig centerConfig) {
        try {
            SKBeanUtils.getAdminManager().addCenterConfig(centerConfig);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 查询中心服务器
     *
     * @param type
     * @param id
     * @return
     */
    @RequestMapping(value = "/findCenterConfig")
    public JSONMessage findCentConfig(@RequestParam(defaultValue = "") String type,
                                      @RequestParam(defaultValue = "") String id) {
        PageResult<CenterConfig> result = SKBeanUtils.getAdminManager().findCenterConfig(type,
                (!StringUtil.isEmpty(id) ? new ObjectId(id) : null));
        return JSONMessage.success(null, result);
    }

    /**
     * 删除中心服务器
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteCenter")
    public JSONMessage deleteCenter(@RequestParam String id) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().deleteCenter(new ObjectId(id));
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.success();
        }

    }

    /**
     * 保存总配置
     *
     * @param totalConfig
     * @return
     */
    @RequestMapping(value = "/addTotalConfig")
    public JSONMessage addTotalConfig(@ModelAttribute TotalConfig totalConfig) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().addTotalConfig(totalConfig);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    @RequestMapping(value = "/addAdmin")
    public JSONMessage addAdmin(@RequestParam(defaultValue = "86") Integer areaCode, @RequestParam String telePhone,
                                @RequestParam byte role, @RequestParam(defaultValue = "0") Integer type) {

        try {
            // 权限校验
            byte userRole = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (userRole != KConstants.Admin_Role.SUPER_ADMIN && userRole != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            // 核验账号是否重复
            // User user = getUserManager().getUser(areaCode+account);
            /*
             * Admin admin =
             * SKBeanUtils.getAdminManager().findAdminByAccount(account);
             * if(admin!=null) { return JSONMessage.failure("该账号已存在"); }
             * SKBeanUtils.getAdminManager().addAdmin(account, password, role);
             */
            SKBeanUtils.getRoleManager().addAdmin(areaCode + telePhone, telePhone, role, type);
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    @RequestMapping(value = "/adminList")
    public JSONMessage adminList(@RequestParam String adminId, @RequestParam(defaultValue = "") String keyWorld,
                                 @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit,
                                 @RequestParam(defaultValue = "0") int type, @RequestParam(defaultValue = "0") Integer userId) {
        try {

            Role userRole = SKBeanUtils.getRoleManager().getUserRole(getUserManager().getUser(adminId).getUserId(),
                    null, 5);
            if (userRole.getRole() != KConstants.Admin_Role.ADMIN && userRole.getRole() != KConstants.Admin_Role.SUPER_ADMIN) {
                return JSONMessage.failure("权限不足");
            } else {
                if (userRole.getRole() == type) {
                    return JSONMessage.failure("权限不足");
                }
            }
            if (userRole != null && userRole.getRole() == KConstants.Admin_Role.ADMIN || userRole.getRole() == KConstants.Admin_Role.SUPER_ADMIN ||
                    userRole.getRole() == KConstants.Admin_Role.TOURIST || userRole.getRole() == KConstants.Admin_Role.CUSTOMER ||
                    userRole.getRole() == KConstants.Admin_Role.FINANCE) {
                PageResult<Role> result = SKBeanUtils.getRoleManager().adminList(keyWorld, page, limit, type, userId);
                return JSONMessage.success(result);
            } else {
                return JSONMessage.failure("权限不足");
            }
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param adminId
     * @param toAdminId
     * @param admin
     * @return
     * @Description:（后台角色禁用解禁）
     **/
    @RequestMapping(value = "/modifyAdmin")
    public JSONMessage modifyAdmin(@RequestParam Integer adminId, @RequestParam(defaultValue = "") String password,
                                   @ModelAttribute Role role) {

        try {
            // 权限校验
            byte userRole = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (userRole != KConstants.Admin_Role.SUPER_ADMIN && userRole != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }

            if (!StringUtil.isEmpty(password)) {
                User user = SKBeanUtils.getUserRepository().getUser(adminId);
                if (!password.equals(user.getPassword()))
                    return JSONMessage.failure("密码有误");
            }
            Role oAdmin = SKBeanUtils.getRoleManager().getUserRole(adminId, null, 5);
            if (oAdmin != null && oAdmin.getRole() == 6 || oAdmin.getRole() == 5) { // role
                // =
                // 1
                // 超级管理员
                Object result = SKBeanUtils.getRoleManager().modifyRole(role);
                return JSONMessage.success(result);
            } else {
                return JSONMessage.failure("权限不足");
            }

        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param adminId
     * @return
     * @Description:（删除管理角色）
     **/
    @RequestMapping(value = "/delAdmin")
    public JSONMessage deleteAdmin(@RequestParam String adminId, @RequestParam Integer type) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getRoleManager().delAdminById(adminId, type);
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * @param userId
     * @param toUserId
     * @return
     * @Description:（好友的聊天记录）
     **/
    @RequestMapping("/friendsChatRecord")
    public JSONMessage friendsChatRecord(@RequestParam(defaultValue = "0") Integer userId,
                                         @RequestParam(defaultValue = "0") Integer toUserId, @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int limit) {
        try {
            PageResult<DBObject> result = SKBeanUtils.getFriendsManager().chardRecord(userId, toUserId, page, limit);
            return JSONMessage.success(result);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param messageId
     * @return
     * @Description:（删除好友间的聊天记录）
     **/
    @RequestMapping("/delFriendsChatRecord")
    public JSONMessage delFriendsChatRecord(@RequestParam(defaultValue = "") String messageId) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            if (StringUtil.isEmpty(messageId))
                return JSONMessage.failure("参数有误");
            String[] strMessageIds = StringUtil.getStringList(messageId);
            SKBeanUtils.getFriendsManager().delFriendsChatRecord(strMessageIds);
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param toUserId
     * @param type     0 ： 加入黑名单， 1：移除黑名单
     * @return
     * @Description:（黑名单操作）
     **/
    @SuppressWarnings("static-access")
    @RequestMapping("/blacklist/operation")
    public JSONMessage blacklistOperation(@RequestParam Integer userId, @RequestParam Integer toUserId,
                                          @RequestParam(defaultValue = "0") Integer type, @RequestParam(defaultValue = "") Integer adminUserId) {
        JSONMessage jsonMessage = null;
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            if (0 == type) {
                if (SKBeanUtils.getFriendsManager().isBlack(userId, toUserId))
                    return jsonMessage.failure("不能重复拉黑好友");
                Friends data = SKBeanUtils.getFriendsManager().consoleAddBlacklist(userId, toUserId, adminUserId);
                return jsonMessage.success("加入黑名单成功", data);
            } else if (1 == type) {
                if (!SKBeanUtils.getFriendsManager().isBlack(userId, toUserId))
                    return jsonMessage.failure("好友：" + toUserId + "不在我的黑名单中");
                Friends data = SKBeanUtils.getFriendsManager().consoleDeleteBlacklist(userId, toUserId, adminUserId);
                return jsonMessage.success("取消拉黑成功", data);
            }
        } catch (Exception e) {
            return jsonMessage.failure(e.getMessage());
        }
        return jsonMessage;

    }

    /**
     * 开放平台app列表
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/openAppList")
    public JSONMessage openAppList(@RequestParam(defaultValue = "-2") int status, @RequestParam(defaultValue = "0") int type,
                                   @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit,
                                   @RequestParam(defaultValue = "") String keyWorld) {
        try {
            PageResult<SkOpenApp> list = SKBeanUtils.getAdminManager().openAppList(status, type, page, limit, keyWorld);
            return JSONMessage.success(list);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 开放平台app详情
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/openAppDetail")
    public JSONMessage openAppDetail(@RequestParam(defaultValue = "") String id) {
        try {
            Object data = SKBeanUtils.getOpenAppManage().appInfo(new ObjectId(id));
            return JSONMessage.success(data);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 开放平台通过审核、禁用应用
     *
     * @param id
     * @param userId
     * @param status
     * @return
     */
    @RequestMapping(value = "/approvedAPP")
    public JSONMessage approved(@RequestParam(defaultValue = "") String id,
                                @RequestParam(defaultValue = "") String userId, @RequestParam(defaultValue = "0") int status,
                                @RequestParam(defaultValue = "") String reason) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getOpenAppManage().approvedAPP(id, status, userId, reason);
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 开放平台通过审核app权限
     *
     * @param skOpenApp
     * @return
     */
    @RequestMapping(value = "/checkPermission")
    public JSONMessage checkPermission(@ModelAttribute SkOpenApp skOpenApp) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getOpenAppManage().openAccess(skOpenApp);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

//	/**
//	 * 开放平台通过审核web权限
//	 *
//	 * @param skOpenWeb
//	 * @return
//	 */
//	@RequestMapping(value = "/checkWebPermission")
//	public JSONMessage checkWebPermission(@ModelAttribute SkOpenWeb skOpenWeb) {
//		try {
//			SKBeanUtils.getOpenWebAppManage().openAccess(skOpenWeb);
//			return JSONMessage.success();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return JSONMessage.failure(e.getMessage());
//		}
//	}

    /**
     * 开放平台删除应用
     *
     * @param id
     * @param accountId
     * @return
     */
    @RequestMapping(value = "/deleteOpenApp")
    public JSONMessage deleteOpenApp(@RequestParam(defaultValue = "") String id,
                                     @RequestParam(defaultValue = "") String accountId) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getOpenAppManage().deleteAppById(new ObjectId(id), accountId);
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 开放平台审核日志列表
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/checkLogList")
    public JSONMessage checkLogList(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int limit) {
        try {
            PageResult<SkOpenCheckLog> data = SKBeanUtils.getOpenCheckLogManage().getOpenCheckLogList(page, limit);
            return JSONMessage.success(data);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 开放平台删除日志
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/delOpenCheckLog")
    public JSONMessage delOpenCheckLog(@RequestParam(defaultValue = "") String id) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getOpenCheckLogManage().delOpenCheckLog(new ObjectId(id));
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 开放平台开发者列表
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/developerList")
    public JSONMessage developerList(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "-2") int status,
                                     @RequestParam(defaultValue = "") String keyWorld) {
        try {
            PageResult<SkOpenAccount> data = SKBeanUtils.getOpenAccountManage().developerList(page, limit, status,
                    keyWorld);
            return JSONMessage.success(data);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 开放平台开发者详情
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/developerDetail")
    public JSONMessage developerDetail(@RequestParam(defaultValue = "") Integer userId) {
        try {
            Object data = SKBeanUtils.getOpenAccountManage().getOpenAccount(userId);
            return JSONMessage.success(data);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 开放平台删除开发者
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteDeveloper")
    public JSONMessage deleteDeveloper(@RequestParam(defaultValue = "") String id) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getOpenAccountManage().deleteDeveloper(new ObjectId(id));
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 开放平台审核开发者、禁用
     *
     * @param id
     * @param userId
     * @param status
     * @return
     */
    @RequestMapping(value = "/checkDeveloper")
    public JSONMessage checkDeveloper(@RequestParam(defaultValue = "") String id,
                                      @RequestParam(defaultValue = "") String userId, @RequestParam(defaultValue = "0") int status) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getOpenAccountManage().checkDeveloper(new ObjectId(id), status);
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    // 校验第三方网站是否有对应权限
    @RequestMapping(value = "/authInterface")
    public JSONMessage authInterface(@RequestParam(defaultValue = "") String appId, @RequestParam(defaultValue = "1") int type) {
        try {
            SKBeanUtils.getOpenAppManage().authInterfaceWeb(appId, type);
            return JSONMessage.success();

        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    //生成邀请码
    @RequestMapping(value = "/create/inviteCode")
    public JSONMessage createInviteCode(@RequestParam(defaultValue = "20") int nums, @RequestParam int userId, @RequestParam short type) {
        try {
            SKBeanUtils.getAdminManager().createInviteCode(nums, userId);
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    // 邀请码列表
    @RequestMapping(value = "/inviteCodeList")
    public JSONMessage inviteCodeList(@RequestParam(defaultValue = "0") int userId,
                                      @RequestParam(defaultValue = "") String keyworld, @RequestParam(defaultValue = "-1") short state,
                                      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
        try {
            PageResult<InviteCode> data = SKBeanUtils.getAdminManager().inviteCodeList(userId, keyworld, state, page,
                    limit);
            return JSONMessage.success(data);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    // 删除邀请码
    @RequestMapping(value = "/delInviteCode")
    public JSONMessage delInviteCode(@RequestParam(defaultValue = "") int userId,
                                     @RequestParam(defaultValue = "") String inviteCodeId) {

        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            boolean data = SKBeanUtils.getAdminManager().delInviteCode(userId, inviteCodeId);
            return JSONMessage.success(data);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param checkNum     参与发送的人数
     * @param roomJid      房间jid
     * @param sendMsgNum   发送总条数
     * @param timeInterval 每条消息间隔时间
     * @return
     * @Description:（压力测试）
     **/
    @RequestMapping("/pressureTest")
    public JSONMessage pressureTest(@ModelAttribute PressureParam param, HttpServletRequest request) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            if (param.getTimeInterval() < 10)
                param.setTimeInterval(10);
            System.out.println("pressureTest ====> " + request.getSession().getCreationTime() + " "
                    + request.getSession().getId());
            List<String> jids = StringUtil.getListBySplit(param.getRoomJid(), ",");
            param.setJids(jids);
            param.setSendAllCount(param.getSendMsgNum() * jids.size());
            PressureParam.PressureResult result = SKBeanUtils.getPressureTestManager().mucTest(param);
            if (null == result) {
                return JSONMessage.failure("已有压测 任务 运行中  请稍后 请求 。。。。。。");
            }
            return JSONMessage.success(result);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 短视频音乐列表
     *
     * @param page
     * @param limit
     * @param keyword
     * @return
     */
    @RequestMapping(value = "/musicList")
    public JSONMessage queryMusicList(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") Integer limit, @RequestParam(defaultValue = "") String keyword) {
        PageResult<MusicInfo> result = SKBeanUtils.getLocalSpringBeanManager().getAdminManager().queryMusicInfo(page,
                limit, keyword);
        return JSONMessage.success(result);
    }

    /**
     * 添加短视频音乐
     *
     * @param musicInfo
     * @return
     */
    @RequestMapping(value = "/addMusic")
    public JSONMessage addMusic(@ModelAttribute MusicInfo musicInfo) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getLocalSpringBeanManager().getMusicManager().addMusicInfo(musicInfo);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 删除短视频音乐
     *
     * @param musicInfoId
     * @return
     */
    @RequestMapping(value = "/deleteMusic")
    public JSONMessage deleteMusic(@RequestParam(defaultValue = "") String musicInfoId) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getLocalSpringBeanManager().getMusicManager().deleteMusicInfo(new ObjectId(musicInfoId));
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 修改短视频音乐
     *
     * @param musicInfo
     * @return
     */
    @RequestMapping(value = "/updateMusic")
    public JSONMessage updateMusic(@ModelAttribute MusicInfo musicInfo) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getLocalSpringBeanManager().getMusicManager().updateMusicInfo(musicInfo);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 转账记录
     *
     * @param userId
     * @param page
     * @param limit
     * @return
     */
    @RequestMapping(value = "/transferList")
    public JSONMessage transferList(@RequestParam(defaultValue = "") String userId,
                                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "15") int limit,
                                    @RequestParam(defaultValue = "") String startDate, @RequestParam(defaultValue = "") String endDate) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.FINANCE) {
            return JSONMessage.failure("权限不足");
        }
        PageResult<Transfer> result = SKBeanUtils.getAdminManager().queryTransfer(page, limit, userId, startDate,
                endDate);
        return JSONMessage.success(result);
    }

    /**
     * 付款记录
     *
     * @param userId
     * @param page
     * @param limit
     * @return
     */
    @RequestMapping(value = "/paymentCodeList")
    public JSONMessage paymentCodeList(@RequestParam(defaultValue = "0") int userId,
                                       @RequestParam(defaultValue = "0") int type, @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "15") int limit, @RequestParam(defaultValue = "") String startDate,
                                       @RequestParam(defaultValue = "") String endDate) {
        // 权限校验
        byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
        if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.FINANCE) {
            return JSONMessage.failure("权限不足");
        }
        PageResult<ConsumeRecord> result = SKBeanUtils.getConsumeRecordManager().payment(userId, type, page, limit,
                startDate, endDate);
        return JSONMessage.success(result);
    }

    /**
     * 获取第三方绑定列表
     *
     * @param userId
     * @param page
     * @param limit
     * @return
     */
    @RequestMapping(value = "/getSdkLoginInfoList")
    public JSONMessage getSdkLoginInfoList(@RequestParam(defaultValue = "") String userId,
                                           @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "15") int limit) {
        try {
            PageResult<SdkLoginInfo> result = SKBeanUtils.getAdminManager().getSdkLoginInfoList(page, limit, userId);
            return JSONMessage.success(result);
        } catch (NumberFormatException e) {
            logger.info("error : {}" + e.getMessage());
            return JSONMessage.success();
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * 解除第三方绑定
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteSdkLoginInfo")
    public JSONMessage deleteSdkLoginInfo(@RequestParam(defaultValue = "") String id) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getAdminManager().deleteSdkLoginInfo(new ObjectId(id));
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    /**
     * @param appId
     * @param callbackUrl
     * @param response
     * @Description:授权登录
     **/
    @RequestMapping(value = "/oauth/authorize")
    public void authorizeUrl(String appId, String callbackUrl, HttpServletResponse response) {
        try {
            Map<String, String> webInfo = SKBeanUtils.getOpenAppManage().authorizeUrl(appId, callbackUrl);
            String webAppName = webInfo.get("webAppName");
            response.sendRedirect("/pages/websiteAuthorh/index.html" + "?" + "appId=" + appId + "&" + "callbackUrl=" + callbackUrl + "&webAppName=" + URLEncoder.encode(webAppName, "UTF-8") + "&webAppsmallImg=" + webInfo.get("webAppsmallImg"));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    /**
     * 发送系统通知
     *
     * @param type
     * @param body
     * @return
     */
    @RequestMapping(value = "/sendSysNotice")
    public JSONMessage sendSysNotice(@RequestParam(defaultValue = "0") Integer type, @RequestParam(defaultValue = "") String body, @RequestParam(defaultValue = "") String title, @RequestParam(defaultValue = "") String url) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            if (StringUtil.isEmpty(body) || StringUtil.isEmpty(title))
                return JSONMessage.failure("标题或内容不能为空");
            SKBeanUtils.getAdminManager().sendSysNotice(type, body, title, url);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }

    @RequestMapping("/recharge/audit/bankCardInfo")
    public JSONMessage auditBankCardInfo(@RequestParam String bankCardId) {
        return JSONMessage.success(SKBeanUtils.getBankCardManager().getBankCardForce(new ObjectId(bankCardId)));
    }

    /**
     * 提现审核
     *
     * @param rechargeId
     * @param auditStatus
     * @return
     */
    @RequestMapping("/recharge/audit")
    public JSONMessage rechargeAudit(@RequestParam String rechargeId,
                                     @RequestParam byte auditStatus, String desc) {
        try {
            SKBeanUtils.getConsumeRecordManager().updateAuditStatus(ReqUtil.parseId(rechargeId), auditStatus, KConstants.FundsChangeType.DRAWINGS, desc);
        } catch (BizException e) {
            return JSONMessage.failure(e.getMessage());
        }
        return JSONMessage.success("操作成功");
    }

    @RequestMapping("/trade/monitor")
    public JSONMessage tradeMonitor() {
        return JSONMessage.success(SKBeanUtils.getConsumeRecordManager().newTradeMonitor());
    }

    /**
     * 朋友圈页自定义网站列表
     *
     * @param page
     * @param limit
     * @return
     */
    @RequestMapping(value = "/friendsterWebsiteList")
    public JSONMessage queryReceiptQRList(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") Integer limit) {
        PageResult<FriendsterWebsite> result = SKBeanUtils.getLocalSpringBeanManager().getFriendsterWebsiteManager().queryFriendsterWebsiteList(page,
                limit);
        return JSONMessage.success(result);
    }

    /**
     * 添加朋友圈页自定义网站
     *
     * @return
     */
    @RequestMapping(value = "/addFriendsterWebsite")
    public JSONMessage addReceiptQR(@ModelAttribute FriendsterWebsite friendsterWebsite) {
        try {
            if (null == friendsterWebsite || StringUtils.isAnyBlank(friendsterWebsite.getIcon(), friendsterWebsite.getTitle(), friendsterWebsite.getUrl())) {
                return JSONMessage.failure("请补全信息再提交");
            }
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getLocalSpringBeanManager().getFriendsterWebsiteManager().saveFriendsterWebsite(friendsterWebsite);
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 删除朋友圈页自定义网站
     *
     * @return
     */
    @RequestMapping(value = "/deleteFriendsterWebsite")
    public JSONMessage deleteReceiptQR(@RequestParam(defaultValue = "") String id) {
        try {
            // 权限校验
            byte role = (byte) SKBeanUtils.getRoleManager().getUserRoleByUserId(ReqUtil.getUserId());
            if (role != KConstants.Admin_Role.SUPER_ADMIN && role != KConstants.Admin_Role.ADMIN) {
                return JSONMessage.failure("权限不足");
            }
            SKBeanUtils.getLocalSpringBeanManager().getFriendsterWebsiteManager().deleteById(new ObjectId(id));
            return JSONMessage.success();
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }

    }
}