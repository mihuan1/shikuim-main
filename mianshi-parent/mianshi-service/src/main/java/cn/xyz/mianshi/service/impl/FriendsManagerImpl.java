package cn.xyz.mianshi.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ExcelUtil;
import cn.xyz.commons.utils.MapUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.PageResult;
import cn.xyz.mianshi.model.PageVO;
import cn.xyz.mianshi.service.FriendsManager;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.NewFriends;
import cn.xyz.mianshi.vo.OfflineOperation;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.mongo.FriendsRepositoryImpl;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;
import cn.xyz.service.RedisServiceImpl;

@Service
public class FriendsManagerImpl extends MongoRepository<Friends, ObjectId> implements FriendsManager {

    private static final String groupCode = "110";

    private static Logger Log = LoggerFactory.getLogger(FriendsManager.class);

    private static RedisServiceImpl getRedisServiceImpl() {
        return SKBeanUtils.getRedisService();
    }

    @Override
    public Datastore getDatastore() {
        return SKBeanUtils.getDatastore();
    }

    @Override
    public Class<Friends> getEntityClass() {
        return Friends.class;
    }

    private static FriendsRepositoryImpl getFriendsRepository() {
        FriendsRepositoryImpl getFriendsRepository = SKBeanUtils.getFriendsRepository();
        return getFriendsRepository;
    }

    ;

    private static UserManagerImpl getUserManager() {
        UserManagerImpl userManager = SKBeanUtils.getUserManager();
        return userManager;
    }

    ;

    @Override
    public Friends addBlacklist(Integer userId, Integer toUserId) {
        // 是否存在AB关系
        Friends friendsAB = getFriendsRepository().getFriends(userId, toUserId);
        Friends friendsBA = getFriendsRepository().getFriends(toUserId, userId);

        if (null == friendsAB) {
            Friends friends = new Friends(userId, toUserId, getUserManager().getNickName(toUserId), Friends.Status.Stranger, Friends.Blacklist.Yes, 0);
            getFriendsRepository().saveFriends(friends);
        } else {
            // 更新关系
            getFriendsRepository().updateFriends(new Friends(userId, toUserId, null, -1, Friends.Blacklist.Yes, (0 == friendsAB.getIsBeenBlack()) ? 0 : friendsAB.getIsBeenBlack()));
            getFriendsRepository().updateFriends(new Friends(toUserId, userId, null, null, (0 == friendsBA.getBlacklist() ? Friends.Blacklist.No : friendsBA.getBlacklist()), 1));
        }
        SKBeanUtils.getTigaseManager().deleteLastMsg(userId.toString(), toUserId.toString());
        //SKBeanUtils.getTigaseManager().deleteLastMsg(toUserId.toString(),userId.toString());
        // 维护好友数据
        deleteFriendsInfo(userId, toUserId);
        // 更新好友设置操作时间
        updateOfflineOperation(userId, toUserId);
        return getFriendsRepository().getFriends(userId, toUserId);
    }

    /**
     * @param userId
     * @param toUserId
     * @Description: 维护用户通讯录好友缓存
     **/
    private void deleteAddressFriendsInfo(Integer userId, Integer toUserId) {
        // 通讯录好友id
        getRedisServiceImpl().delAddressBookFriendsUserIds(userId);
        getRedisServiceImpl().delAddressBookFriendsUserIds(toUserId);
        deleteFriendsInfo(userId, toUserId);
    }

    /**
     * @param userId
     * @param toUserId
     * @Description: 维护用户好友缓存
     **/
    private void deleteFriendsInfo(Integer userId, Integer toUserId) {
        // 好友userIdsList
        getRedisServiceImpl().deleteFriendsUserIdsList(userId);
        getRedisServiceImpl().deleteFriendsUserIdsList(toUserId);
        // 好友列表
        getRedisServiceImpl().deleteFriends(userId);
        getRedisServiceImpl().deleteFriends(toUserId);
    }

    // 后台加入黑名单（后台可以互相拉黑）
    public Friends consoleAddBlacklist(Integer userId, Integer toUserId, Integer adminUserId) {
        // 是否存在AB关系
        Friends friendsAB = getFriendsRepository().getFriends(userId, toUserId);
        Friends friendsBA = getFriendsRepository().getFriends(toUserId, userId);
        if (null == friendsAB) {
            Friends friends = new Friends(userId, toUserId, getUserManager().getNickName(toUserId), Friends.Status.Stranger, Friends.Blacklist.Yes, 0);
            getFriendsRepository().saveFriends(friends);
        } else {
            // 更新关系
            getFriendsRepository().updateFriends(new Friends(userId, toUserId, null, -1, Friends.Blacklist.Yes, (0 == friendsAB.getIsBeenBlack()) ? 0 : friendsAB.getIsBeenBlack()));
            getFriendsRepository().updateFriends(new Friends(toUserId, userId, null, null, (0 == friendsBA.getBlacklist() ? Friends.Blacklist.No : friendsBA.getBlacklist()), 1));
        }
        SKBeanUtils.getTigaseManager().deleteLastMsg(userId.toString(), toUserId.toString());
        ThreadUtil.executeInThread(new Callback() {

            @Override
            public void execute(Object obj) {

                //xmpp推送消息
                MessageBean messageBean = new MessageBean();
                messageBean.setType(KXMPPServiceImpl.joinBlacklist);
                messageBean.setFromUserId(adminUserId + "");
                messageBean.setFromUserName("后台系统管理员");
                MessageBean beanVo = new MessageBean();
                beanVo.setFromUserId(userId + "");
                beanVo.setFromUserName(getUserManager().getNickName(userId));
                beanVo.setToUserId(toUserId + "");
                beanVo.setToUserName(getUserManager().getNickName(toUserId));
                messageBean.setObjectId(JSONObject.toJSONString(beanVo));
                messageBean.setMessageId(StringUtil.randomUUID());
                try {
                    List<Integer> userIdlist = new ArrayList<Integer>();
                    userIdlist.add(userId);
                    userIdlist.add(toUserId);
                    KXMPPServiceImpl.getInstance().send(messageBean, userIdlist);
                } catch (Exception e) {
                }

            }
        });
        // 维护好友数据
        deleteFriendsInfo(userId, toUserId);
        // 更新好友设置操作时间
        updateOfflineOperation(userId, toUserId);
        return getFriendsRepository().getFriends(userId, toUserId);
    }


    public Friends updateFriends(Friends friends) {
        return getFriendsRepository().updateFriends(friends);
    }

    public boolean isBlack(Integer toUserId) {
        Friends friends = getFriends(ReqUtil.getUserId(), toUserId);
        if (friends == null)
            return false;
        return friends.getStatus() == -1 ? true : false;
    }

    public boolean isBlack(Integer userId, Integer toUserId) {
        Friends friends = getFriends(userId, toUserId);
        if (friends == null)
            return false;
        return friends.getStatus() == -1 ? true : false;
    }

    private void saveFansCount(int userId) {
        BasicDBObject q = new BasicDBObject("_id", userId);
        DBCollection dbCollection = getTigaseDatastore().getDB().getCollection("shiku_msgs_count");
        if (0 == dbCollection.count(q)) {
            BasicDBObject jo = new BasicDBObject("_id", userId);
            jo.put("count", 0);// 消息数
            jo.put("fansCount", 1);// 粉丝数
            dbCollection.insert(jo);
        } else {
            dbCollection.update(q, new BasicDBObject("$inc", new BasicDBObject("fansCount", 1)));
        }
    }

    @Override
    public boolean addFriends(Integer userId, Integer toUserId) {

        SKBeanUtils.getRedisService().deleteFriends(userId);
        SKBeanUtils.getRedisService().deleteFriends(toUserId);
        int toUserType = 0;
        List<Integer> toUserRoles = SKBeanUtils.getRoleManager().getUserRoles(toUserId);
        if (toUserRoles.size() > 0 && null != toUserRoles) {
            if (toUserRoles.contains(2))
                toUserType = 2;
        }
        int userType = 0;
        List<Integer> userRoles = SKBeanUtils.getRoleManager().getUserRoles(userId);
        if (userRoles.size() > 0 && null != userRoles) {
            if (userRoles.contains(2))
                userType = 2;
        }
        Friends friends = getFriends(userId, toUserId);
        if (null == friends) {
            getFriendsRepository().saveFriends(new Friends(userId, toUserId, getUserManager().getNickName(toUserId),
                    Friends.Status.Friends, 0, 0, toUserRoles, toUserType, 4));
            saveFansCount(toUserId);
        } else {
            saveFansCount(toUserId);
            Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(userId).field("toUserId")
                    .equal(toUserId);
            UpdateOperations<Friends> ops = getDatastore().createUpdateOperations(Friends.class);
            ops.set("modifyTime", DateUtil.currentTimeSeconds());
            ops.set("status", Friends.Status.Friends);
            ops.set("toUserType", toUserType);
            ops.set("toFriendsRole", toUserRoles);
            getDatastore().findAndModify(q, ops);
        }
        Friends toFriends = getFriends(toUserId, userId);
        if (null == toFriends) {
            getFriendsRepository().saveFriends(new Friends(toUserId, userId, getUserManager().getNickName(userId),
                    Friends.Status.Friends, 0, 0, userRoles, userType, 4));
            saveFansCount(toUserId);
        } else {
            saveFansCount(toUserId);
            Query<Friends> q = getDatastore().createQuery(Friends.class).field("userId").equal(toUserId)
                    .field("toUserId").equal(userId);
            UpdateOperations<Friends> ops = getDatastore().createUpdateOperations(Friends.class);
            ops.set("modifyTime", DateUtil.currentTimeSeconds());
            ops.set("status", Friends.Status.Friends);
            ops.set("toUserType", userType);
            ops.set("toFriendsRole", userRoles);
            getDatastore().findAndModify(q, ops);
        }
        // 更新好友设置操作时间
        updateOfflineOperation(userId, toUserId);
        return true;
    }

    @Override
    public Friends deleteBlacklist(Integer userId, Integer toUserId) {
        // 是否存在AB关系
        Friends friendsAB = getFriendsRepository().getFriends(userId, toUserId);
        Friends friendsBA = getFriendsRepository().getFriends(toUserId, userId);

        if (null == friendsAB) {
            // 无记录
        } else {
            // 陌生人黑名单
            if (Friends.Blacklist.Yes == friendsAB.getBlacklist() && Friends.Status.Stranger == friendsAB.getStatus()) {
                // 物理删除
                getFriendsRepository().deleteFriends(userId, toUserId);
            } else {
                // 恢复关系
                getFriendsRepository().updateFriends(new Friends(userId, toUserId, null, 2, Friends.Blacklist.No, (0 == friendsAB.getIsBeenBlack() ? 0 : friendsAB.getIsBeenBlack())));
                getFriendsRepository().updateFriends(new Friends(toUserId, userId, null, (2 == friendsBA.getStatus() ? 2 : friendsBA.getStatus()), (0 == friendsBA.getBlacklist() ? Friends.Blacklist.No : friendsBA.getBlacklist()), 0));
            }
            // 是否存在AB关系
            friendsAB = getFriendsRepository().getFriends(userId, toUserId);
            // 维护好友数据
            deleteFriendsInfo(userId, toUserId);
            // 更新好友设置操作时间
            updateOfflineOperation(userId, toUserId);
        }

        return friendsAB;
    }

    /**
     * @param userId
     * @param toUserId
     * @return
     * @Description:（后台移除黑名单）
     **/
    public Friends consoleDeleteBlacklist(Integer userId, Integer toUserId, Integer adminUserId) {
        // 是否存在AB关系
        Friends friendsAB = getFriendsRepository().getFriends(userId, toUserId);
        Friends friendsBA = getFriendsRepository().getFriends(toUserId, userId);

        if (null == friendsAB) {
            // 无记录
        } else {
            // 陌生人黑名单
            if (Friends.Blacklist.Yes == friendsAB.getBlacklist() && Friends.Status.Stranger == friendsAB.getStatus()) {
                // 物理删除
                getFriendsRepository().deleteFriends(userId, toUserId);
            } else {
                // 恢复关系
                getFriendsRepository().updateFriends(new Friends(userId, toUserId, null, 2, Friends.Blacklist.No, (0 == friendsAB.getIsBeenBlack() ? 0 : friendsAB.getIsBeenBlack())));
                getFriendsRepository().updateFriends(new Friends(toUserId, userId, null, (2 == friendsBA.getStatus() ? 2 : friendsBA.getStatus()), (0 == friendsBA.getBlacklist() ? Friends.Blacklist.No : friendsBA.getBlacklist()), 0));
            }
            // 是否存在AB关系
            friendsAB = getFriendsRepository().getFriends(userId, toUserId);
        }

        ThreadUtil.executeInThread(new Callback() {

            @Override
            public void execute(Object obj) {

                //xmpp推送消息
                MessageBean messageBean = new MessageBean();
                messageBean.setType(KXMPPServiceImpl.moveBlacklist);
                messageBean.setFromUserId(adminUserId + "");
                messageBean.setFromUserName("后台系统管理员");
                MessageBean beanVo = new MessageBean();
                beanVo.setFromUserId(userId + "");
                beanVo.setFromUserName(getUserManager().getNickName(userId));
                beanVo.setToUserId(toUserId + "");
                beanVo.setToUserName(getUserManager().getNickName(toUserId));
                messageBean.setObjectId(JSONObject.toJSONString(beanVo));
                messageBean.setMessageId(StringUtil.randomUUID());
                try {
                    List<Integer> userIdlist = new ArrayList<Integer>();
                    userIdlist.add(userId);
                    userIdlist.add(toUserId);
                    KXMPPServiceImpl.getInstance().send(messageBean, userIdlist);
                } catch (Exception e) {
                }


            }
        });
        // 维护好友数据
        deleteFriendsInfo(userId, toUserId);
        return friendsAB;
    }

    @Override
    public boolean deleteFriends(Integer userId, Integer toUserId) {
        getFriendsRepository().deleteFriends(userId, toUserId);
        getFriendsRepository().deleteFriends(toUserId, userId);
        SKBeanUtils.getTigaseManager().deleteLastMsg(userId.toString(), toUserId.toString());
        SKBeanUtils.getTigaseManager().deleteLastMsg(toUserId.toString(), userId.toString());
        // 维护好友数据
        deleteFriendsInfo(userId, toUserId);
        // 更新好友设置操作时间
        updateOfflineOperation(userId, toUserId);
        return true;
    }

    /**
     * @param userId
     * @param toUserId
     * @return
     * @Description:（后台删除好友-xmpp发通知）
     **/
    public boolean consoleDeleteFriends(Integer userId, Integer adminUserId, String... toUserIds) {
        for (String strtoUserId : toUserIds) {
            Integer toUserId = Integer.valueOf(strtoUserId);
            getFriendsRepository().deleteFriends(userId, toUserId);
            getFriendsRepository().deleteFriends(toUserId, userId);
            SKBeanUtils.getTigaseManager().deleteLastMsg(userId.toString(), toUserId.toString());
            SKBeanUtils.getTigaseManager().deleteLastMsg(toUserId.toString(), userId.toString());
            ThreadUtil.executeInThread(new Callback() {

                @Override
                public void execute(Object obj) {
                    //以系统号发送删除好友通知
                    MessageBean messageBean = new MessageBean();
                    messageBean.setType(KXMPPServiceImpl.deleteFriends);
                    messageBean.setFromUserId(adminUserId + "");
                    messageBean.setFromUserName("后台系统管理员");
                    MessageBean beanVo = new MessageBean();
                    beanVo.setFromUserId(userId + "");
                    beanVo.setFromUserName(getUserManager().getNickName(userId));
                    beanVo.setToUserId(toUserId + "");
                    beanVo.setToUserName(getUserManager().getNickName(toUserId));
                    messageBean.setObjectId(JSONObject.toJSONString(beanVo));
                    messageBean.setMessageId(StringUtil.randomUUID());
                    messageBean.setContent("系统解除了你们好友关系");
                    messageBean.setMessageId(StringUtil.randomUUID());
                    try {
                        List<Integer> userIdlist = new ArrayList<Integer>();
                        userIdlist.add(userId);
                        userIdlist.add(toUserId);
                        KXMPPServiceImpl.getInstance().send(messageBean, userIdlist);
                    } catch (Exception e) {
                    }
                    // 维护好友缓存
                    deleteFriendsInfo(userId, toUserId);
                }
            });
        }
        return true;
    }


    @SuppressWarnings("unused")
    @Override
    public JSONMessage followUser(Integer userId, Integer toUserId, Integer fromAddType) {
        final String serviceCode = "08";
        JSONMessage jMessage = null;
        User toUser = getUserManager().getUser(toUserId);
        int toUserType = 0;
        List<Integer> toUserRoles = SKBeanUtils.getRoleManager().getUserRoles(toUserId);
        if (toUserRoles.size() > 0 && null != toUserRoles) {
            if (toUserRoles.contains(2))
                toUserType = 2;
        }
        //好友不存在
        if (null == toUser) {
            if (10000 == toUserId)
                return null;
            else
                return JSONMessage.failure("关注失败, 用户不存在!");

        }

        try {
            User user = getUserManager().getUser(userId);
            int userType = 0;
            List<Integer> userRoles = SKBeanUtils.getRoleManager().getUserRoles(userId);
            if (userRoles.size() > 0 && null != userRoles) {
                if (userRoles.contains(2))
                    userType = 2;
            }

            // 是否存在AB关系
            Friends friendsAB = getFriendsRepository().getFriends(userId, toUserId);
            // 是否存在BA关系
            Friends friendsBA = getFriendsRepository().getFriends(toUserId, userId);
            // 获取目标用户设置
            User.UserSettings userSettingsB = getUserManager().getSettings(toUserId);

            // ----------------------------
            // 0 0 0 0 无记录 执行关注逻辑
            // A B 1 0 非正常 执行关注逻辑
            // A B 1 1 拉黑陌生人 执行关注逻辑
            // A B 2 0 关注 重复关注
            // A B 3 0 好友 重复关注
            // A B 2 1 拉黑关注 恢复关系
            // A B 3 1 拉黑好友 恢复关系
            // ----------------------------
            // 无AB关系或陌生人黑名单关系，加关注
            if (null != friendsAB && friendsAB.getIsBeenBlack() == 1) {
                return jMessage = JSONMessage.failure("加好友失败");
            }
            if (null == friendsAB || Friends.Status.Stranger == friendsAB.getStatus()) {
                // 目标用户拒绝关注
                if (0 == userSettingsB.getAllowAtt()) {
                    jMessage = new JSONMessage(groupCode, serviceCode, "01", "关注失败，目标用户拒绝关注");
                }
                // 目标用户允许关注
                else {
                    int statusA = 0;

                    // 目标用户加好需验证，执行加关注。过滤公众号开启好友验证
                    if (1 == userSettingsB.getFriendsVerify() && 2 != toUserType) {
                        // ----------------------------
                        // 0 0 0 0 无记录 执行单向关注
                        // B A 1 0 非正常 执行单向关注
                        // B A 1 1 拉黑陌生人 执行单向关注
                        // B A 2 0 关注 加好友
                        // B A 3 0 好友 加好友
                        // B A 2 1 拉黑关注 加好友
                        // B A 3 1 拉黑好友 加好友
                        // ----------------------------
                        // 无BA关系或陌生人黑名单关系，单向关注
                        if (null == friendsBA || Friends.Status.Stranger == friendsBA.getStatus()) {
                            statusA = Friends.Status.Attention;
                        } else {
                            statusA = Friends.Status.Friends;

                            getFriendsRepository()
                                    .updateFriends(new Friends(toUserId, user.getUserId(), user.getNickname(), Friends.Status.Friends));
                        }
                    }
                    // 目标用户加好友无需验证，执行加好友
                    else {
                        statusA = Friends.Status.Friends;

                        if (null == friendsBA) {
                            getFriendsRepository().saveFriends(new Friends(toUserId, user.getUserId(), user.getNickname(),
                                    Friends.Status.Friends, Friends.Blacklist.No, 0, userRoles, userType, fromAddType));

                            saveFansCount(toUserId);
                        } else
                            getFriendsRepository()
                                    .updateFriends(new Friends(toUserId, user.getUserId(), user.getNickname(), Friends.Status.Friends, userType, userRoles));//改变usertype
                    }

                    if (null == friendsAB) {
                        getFriendsRepository().saveFriends(new Friends(userId, toUserId, toUser.getNickname(), statusA, Friends.Blacklist.No, 0, toUserRoles, toUserType, fromAddType));
                        saveFansCount(toUserId);
                    } else {
                        getFriendsRepository().updateFriends(new Friends(userId, toUserId, toUser.getNickname(), statusA, Friends.Blacklist.No, 0));
                    }

                    if (statusA == Friends.Status.Attention) {
                        HashMap<String, Object> newMap = MapUtil.newMap("type", 1);
                        newMap.put("fromAddType", fromAddType);
                        jMessage = JSONMessage.success("关注成功，已关注目标用户", newMap);
                    } else {
                        HashMap<String, Object> newMap = MapUtil.newMap("type", 2);
                        newMap.put("fromAddType", fromAddType);
                        jMessage = JSONMessage.success("关注成功，已互为好友", newMap);
                    }

                }
            }
            // 有关注或好友关系，重复关注
            else if (Friends.Blacklist.No == friendsAB.getBlacklist()) {
                if (Friends.Status.Attention == friendsAB.getStatus()) {
                    // 开启好友验证后关闭
                    if (0 == userSettingsB.getFriendsVerify()) {
                        Integer statusA = Friends.Status.Friends;
                        if (null == friendsBA) {
                            getFriendsRepository().saveFriends(new Friends(toUserId, user.getUserId(), user.getNickname(), Friends.Status.Friends, Friends.Blacklist.No, 0, userRoles, userType, fromAddType));
                            saveFansCount(toUserId);
                        } else {
                            getFriendsRepository().updateFriends(new Friends(toUserId, user.getUserId(), user.getNickname(), Friends.Status.Friends));
                        }
                        if (null == friendsAB) {
                            getFriendsRepository().saveFriends(new Friends(userId, toUserId, toUser.getNickname(), statusA, Friends.Blacklist.No, 0, toUserRoles, toUserType, fromAddType));
                            saveFansCount(toUserId);
                        } else {
                            getFriendsRepository().updateFriends(new Friends(userId, toUserId, toUser.getNickname(), statusA, Friends.Blacklist.No, 0));
                        }
                        HashMap<String, Object> newMap = MapUtil.newMap("type", 2);
                        newMap.put("fromAddType", fromAddType);
                        jMessage = JSONMessage.success("关注成功，已互为好友", newMap);
                    } else if (1 == userSettingsB.getFriendsVerify()) {
                        HashMap<String, Object> newMap = MapUtil.newMap("type", 1);
                        newMap.put("fromAddType", fromAddType);
                        jMessage = JSONMessage.success("关注成功，已关注目标用户", newMap);
                    }
                } else {
                    HashMap<String, Object> newMap = MapUtil.newMap("type", 2);
                    newMap.put("fromAddType", fromAddType);
                    jMessage = JSONMessage.success("关注成功，已互为好友", newMap);
                }
            }
            // 有关注黑名单或好友黑名单关系，恢复关系
            else {
                getFriendsRepository().updateFriends(new Friends(userId, toUserId, toUser.getNickname(), Friends.Blacklist.No));

                jMessage = null;
            }
            // 维护好友数据
            deleteFriendsInfo(userId, toUserId);
            // 更新好友设置操作时间
            updateOfflineOperation(userId, toUserId);
        } catch (Exception e) {
            Log.error("关注失败", e);
            jMessage = JSONMessage.failure("关注失败");
        }
        return jMessage;
    }

    /**
     * @param userId
     * @param toUserId
     * @Description:更新好友设置操作时间
     **/
    public void updateOfflineOperation(Integer userId, Integer toUserId) {
        Query<OfflineOperation> query = getDatastore().createQuery(OfflineOperation.class).field("userId").equal(userId).field("tag").equal(KConstants.MultipointLogin.TAG_FRIEND).field("friendId").equal(String.valueOf(toUserId));
        if (null == query.get()) {
            getDatastore().save(new OfflineOperation(userId, KConstants.MultipointLogin.TAG_FRIEND, String.valueOf(toUserId), DateUtil.currentTimeSeconds()));
        } else {
            UpdateOperations<OfflineOperation> ops = getDatastore().createUpdateOperations(OfflineOperation.class);
            ops.set("operationTime", DateUtil.currentTimeSeconds());
            getDatastore().update(query, ops);
        }
    }

    // 批量添加好友
    @Override
    public JSONMessage batchFollowUser(Integer userId, String toUserIds) {
        JSONMessage jMessage = null;
        if (StringUtil.isEmpty(toUserIds))
            return null;
        int[] toUserId = StringUtil.getIntArray(toUserIds, ",");
        for (int i = 0; i < toUserId.length; i++) {
            //好友不存在
            if (userId == toUserId[i] || 10000 == toUserId[i])
                continue;
            User toUser = getUserManager().getUser(toUserId[i]);
            if (null == toUser)
                continue;
            int toUserType = 0;
            List<Integer> toUserRoles = SKBeanUtils.getRoleManager().getUserRoles(toUserId[i]);
            if (toUserRoles.size() > 0 && null != toUserRoles) {
                if (toUserRoles.contains(2))
                    toUserType = 2;
            }

            try {
                User user = getUserManager().getUser(userId);
                int userType = 0;
                List<Integer> userRoles = SKBeanUtils.getRoleManager().getUserRoles(userId);
                if (userRoles.size() > 0 && null != userRoles) {
                    if (userRoles.contains(2))
                        userType = 2;
                }

                // 是否存在AB关系
                Friends friendsAB = getFriendsRepository().getFriends(userId, toUserId[i]);
                // 是否存在BA关系
                Friends friendsBA = getFriendsRepository().getFriends(toUserId[i], userId);
                // 获取目标用户设置
                User.UserSettings userSettingsB = getUserManager().getSettings(toUserId[i]);

                if (null != friendsAB && friendsAB.getIsBeenBlack() == 1) {
//					return jMessage = JSONMessage.failure("加好友失败");
//					continue;
                    throw new ServiceException("已被对方添加到黑名单");
                }
                if (null == friendsAB || Friends.Status.Stranger == friendsAB.getStatus()) {
                    // 目标用户拒绝关注
                    if (0 == userSettingsB.getAllowAtt()) {
//						jMessage = new JSONMessage(groupCode, serviceCode, "01", "关注失败，目标用户拒绝关注");
                        continue;
                    }
                    // 目标用户允许关注
                    else {
                        int statusA = 0;
                        statusA = Friends.Status.Friends;

                        if (null == friendsBA) {
                            getFriendsRepository().saveFriends(new Friends(toUserId[i], user.getUserId(), user.getNickname(),
                                    Friends.Status.Friends, Friends.Blacklist.No, 0, userRoles, userType, 4));

                            saveFansCount(toUserId[i]);
                        } else
                            getFriendsRepository()
                                    .updateFriends(new Friends(toUserId[i], user.getUserId(), user.getNickname(), Friends.Status.Friends));

                        if (null == friendsAB) {
                            getFriendsRepository().saveFriends(new Friends(userId, toUserId[i], toUser.getNickname(), statusA, Friends.Blacklist.No, 0, toUserRoles, toUserType, 4));
                            saveFansCount(toUserId[i]);
                        } else {
                            getFriendsRepository().updateFriends(new Friends(userId, toUserId[i], toUser.getNickname(), statusA, Friends.Blacklist.No, 0));
                        }

                    }
                }
                // 有关注或好友关系，重复关注
                else if (Friends.Blacklist.No == friendsAB.getBlacklist()) {
                    if (Friends.Status.Attention == friendsAB.getStatus()) {
                        // 已关注的修改为好友状态
                        getFriendsRepository().updateFriends(new Friends(userId, toUserId[i], toUser.getNickname(), Friends.Status.Friends, toUserType, toUserRoles));
                        // 添加成为好友
                        getFriendsRepository().saveFriends(new Friends(toUserId[i], user.getUserId(), user.getNickname(),
                                Friends.Status.Friends, Friends.Blacklist.No, 0, userRoles, "", userType));
//						continue;
                    }
                } else {
                    // 有关注黑名单或好友黑名单关系，恢复关系
                    getFriendsRepository().updateFriends(new Friends(userId, toUserId[i], toUser.getNickname(), Friends.Blacklist.No));
                    jMessage = null;
                }
                notify(userId, toUserId[i]);
                jMessage = JSONMessage.success();
                // 维护好友数据
                deleteAddressFriendsInfo(userId, toUserId[i]);
                // 更新好友设置操作时间
                updateOfflineOperation(userId, toUserId[i]);
            } catch (Exception e) {
                Log.error("通讯录添加好友失败", e.getMessage());
                jMessage = JSONMessage.failure(e.getMessage());
            }
        }
        return jMessage;
    }


    /**
     * @param userId
     * @param addressBook<userid 用户id, toRemark 备注 >
     * @return
     * @Description:（通讯录自动添加好友）
     **/
    public JSONMessage autofollowUser(Integer userId, Map<String, String> addressBook) {
        final String serviceCode = "08";
        Integer toUserId = Integer.valueOf(addressBook.get("toUserId"));
        String toRemark = addressBook.get("toRemark");

//		final String serviceCode = "08";
        JSONMessage jMessage = null;
        User toUser = getUserManager().getUser(toUserId);
        int toUserType = 0;
        List<Integer> toUserRoles = SKBeanUtils.getRoleManager().getUserRoles(toUserId);
        if (toUserRoles.size() > 0 && null != toUserRoles) {
            if (toUserRoles.contains(2))
                toUserType = 2;
        }
        //好友不存在
        if (10000 == toUser.getUserId())
            return null;
        try {
            User user = getUserManager().getUser(userId);
            int userType = 0;
            List<Integer> userRoles = SKBeanUtils.getRoleManager().getUserRoles(userId);
            if (userRoles.size() > 0 && null != userRoles) {
                if (userRoles.contains(2))
                    userType = 2;
            }

            // 是否存在AB关系
            Friends friendsAB = getFriendsRepository().getFriends(userId, toUserId);
            // 是否存在BA关系
            Friends friendsBA = getFriendsRepository().getFriends(toUserId, userId);
            // 获取目标用户设置
            User.UserSettings userSettingsB = getUserManager().getSettings(toUserId);

            if (null != friendsAB && friendsAB.getIsBeenBlack() == 1) {
                return jMessage = JSONMessage.failure("加好友失败");
            }
            if (null == friendsAB || Friends.Status.Stranger == friendsAB.getStatus()) {
                // 目标用户拒绝关注
                if (0 == userSettingsB.getAllowAtt()) {
                    jMessage = new JSONMessage(groupCode, serviceCode, "01", "关注失败，目标用户拒绝关注");
                }
                // 目标用户允许关注
                else {
                    int statusA = 0;
                    // 目标用户加好友无需验证，执行加好友
//						else {
                    statusA = Friends.Status.Friends;

                    if (null == friendsBA) {
                        getFriendsRepository().saveFriends(new Friends(toUserId, user.getUserId(), user.getNickname(),
                                Friends.Status.Friends, Friends.Blacklist.No, 0, userRoles, "", userType));

                        saveFansCount(toUserId);
                    } else
                        getFriendsRepository()
                                .updateFriends(new Friends(toUserId, user.getUserId(), user.getNickname(), Friends.Status.Friends));
//						}

                    if (null == friendsAB) {
                        getFriendsRepository().saveFriends(new Friends(userId, toUserId, toUser.getNickname(), statusA, Friends.Blacklist.No, 0, toUserRoles, toRemark, toUserType));
                        saveFansCount(toUserId);
                    } else {
                        getFriendsRepository().updateFriends(new Friends(userId, toUserId, toUser.getNickname(), statusA, Friends.Blacklist.No, 0));
                    }

                }
            }
            // 有关注或好友关系，重复关注
            else if (Friends.Blacklist.No == friendsAB.getBlacklist()) {
                if (Friends.Status.Attention == friendsAB.getStatus()) {
                    // 已关注的修改为好友状态
                    getFriendsRepository().updateFriends(new Friends(userId, toUserId, toUser.getNickname(), Friends.Status.Friends, toUserType, toUserRoles));
                    // 添加成为好友
                    getFriendsRepository().saveFriends(new Friends(toUserId, user.getUserId(), user.getNickname(),
                            Friends.Status.Friends, Friends.Blacklist.No, 0, userRoles, "", userType));
                }
            } else {
                // 有关注黑名单或好友黑名单关系，恢复关系
                getFriendsRepository().updateFriends(new Friends(userId, toUserId, toUser.getNickname(), Friends.Blacklist.No));
                jMessage = null;
            }
            notify(userId, toUserId);
            // 维护好友数据
            deleteFriendsInfo(userId, toUserId);
            jMessage = JSONMessage.success();
        } catch (Exception e) {
            Log.error("关注失败", e);
            jMessage = JSONMessage.failure("关注失败");
        }
        return jMessage;
    }

    public void notify(Integer userId, Integer toUserId) {
        ThreadUtil.executeInThread(new Callback() {
            @Override
            public void execute(Object obj) {
                MessageBean messageBean = new MessageBean();
                messageBean.setType(KXMPPServiceImpl.batchAddFriend);
                messageBean.setFromUserId(String.valueOf(userId));
                messageBean.setFromUserName(SKBeanUtils.getUserManager().getNickName(userId));
                messageBean.setToUserId(String.valueOf(toUserId));
                messageBean.setToUserName(SKBeanUtils.getUserManager().getNickName(toUserId));
                messageBean.setContent(toUserId);
                messageBean.setMsgType(0);// 单聊消息
                messageBean.setMessageId(StringUtil.randomUUID());
                try {
                    KXMPPServiceImpl.getInstance().send(messageBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Friends getFriends(int userId, int toUserId) {
        return getFriendsRepository().getFriends(userId, toUserId);
    }

    public void getFriends(int userId, String... toUserIds) {
        for (String strToUserId : toUserIds) {
            Integer toUserId = Integer.valueOf(strToUserId);
            Friends friends = getFriendsRepository().getFriends(userId, toUserId);
            if (null == friends)
                throw new ServiceException("对方不是你的好友");

        }
//		return getFriendsRepository().getFriends(userId, toUserId);
//		return getFriendsRepository().getFriends(userId, toUserId);
    }

    public List<Friends> getFansList(Integer userId) {

        List<Friends> result = getEntityListsByKey("userId", userId);
        result.forEach(friends -> {
            User user = getUserManager().getUser(friends.getToUserId());

            friends.setToNickname(user.getNickname());
        });


        return result;
    }


    @Override
    public Friends getFriends(Friends p) {
        return getFriendsRepository().getFriends(p.getUserId(), p.getToUserId());
    }

    @Override
    public List<Integer> getFriendsIdList(int userId) {
        List<Integer> result = Lists.newArrayList();

        try {
            List<Friends> friendsList = getFriendsRepository().queryFriends(userId);
            friendsList.forEach(friends -> {
                result.add(friends.getToUserId());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public List<Friends> queryBlacklist(Integer userId, int pageIndex, int pageSize) {
        return getFriendsRepository().queryBlacklist(userId, pageIndex, pageSize);
    }

    public PageVO queryBlacklistWeb(Integer userId, int pageIndex, int pageSize) {
        return getFriendsRepository().queryBlacklistWeb(userId, pageIndex, pageSize);
    }

    @Override
    public List<Integer> queryFansId(Integer userId) {
        return getFriendsRepository().queryFansId(userId);
    }

    /**
     * 查询好友是否开启 免打扰
     *
     * @return
     */
    public boolean getFriendIsNoPushMsg(Integer userId, Integer toUserId) {
        DBObject query = new BasicDBObject("userId", userId).append("toUserId", toUserId);
        query.put("offlineNoPushMsg", 1);
        Object field = queryOneField("offlineNoPushMsg", query);
        return null != field;
    }

    @Override
    public List<Friends> queryFollow(Integer userId, int status) {
        List<Friends> userfriends = SKBeanUtils.getRedisService().getFriendsList(userId);
        if (null != userfriends && userfriends.size() > 0) {
            return userfriends;
        } else {
            if (0 == status)
                status = 2;  //好友
            List<Friends> result = getFriendsRepository().queryFollow(userId, status);
            SKBeanUtils.getRedisService().saveFriendsList(userId, result);
            Iterator<Friends> iter = result.iterator();
            while (iter.hasNext()) {
                Friends friends = iter.next();
                User user = getUserManager().getUser(friends.getToUserId());
                if (null == user) {
                    iter.remove();
                    deleteFansAndFriends(friends.getToUserId());
                    continue;
                }
                friends.setToNickname(user.getNickname());
            }
            return result;
        }
    }


    public PageResult<Friends> consoleQueryFollow(Integer userId, Integer toUserId, int status, int page, int limit) {
        PageResult<Friends> result = new PageResult<Friends>();
        result = getFriendsRepository().consoleQueryFollow(userId, toUserId, status, page, limit);
        Iterator<Friends> iter = result.getData().iterator();
        while (iter.hasNext()) {
            Friends friends = iter.next();
            User user = getUserManager().getUser(friends.getToUserId());
            friends.setNickname(getUserManager().getNickName(userId));
            if (null == user) {
                iter.remove();
                deleteFansAndFriends(friends.getToUserId());
                continue;
            }
            friends.setToNickname(user.getNickname());
        }
        return result;
    }


    @Override
    public List<Integer> queryFollowId(Integer userId) {
        return getFriendsRepository().queryFollowId(userId);
    }

    @Override
    public List<Friends> queryFriends(Integer userId) {
        List<Friends> result = getFriendsRepository().queryFriends(userId);

        for (Friends friends : result) {
            User toUser = getUserManager().getUser(friends.getToUserId());
            if (null == toUser) {
                deleteFansAndFriends(friends.getToUserId());
                continue;
            }
            friends.setToNickname(toUser.getNickname());
            //friends.setCompanyId(toUser.getCompanyId());
        }

        return result;
    }


    @Override   //返回好友的userId 和单向关注的userId
    public List<Integer> friendsAndAttentionUserId(Integer userId, String type) {
        List<Friends> result = new ArrayList<Friends>();
        if ("friendList".equals(type) || "blackList".equals(type)) {  //返回好友的userId 和单向关注的userId
            result = getFriendsRepository().friendsOrBlackList(userId, type);
        } else {
            throw new ServiceException("无法识别的参数");
        }
        List<Integer> userIds = new ArrayList<Integer>();
        for (Friends friend : result) {
            userIds.add(friend.getToUserId());
        }
        return userIds;
    }

    @Override
    public PageVO queryFriends(Integer userId, int status, String keyword, int pageIndex, int pageSize) {
        Query<Friends> q = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId);
        if (0 < status)
            q.filter("status", status);
        if (!StringUtil.isEmpty(keyword)) {
            //q.field("toNickname").containsIgnoreCase(keyword);
            q.or(q.criteria("toNickname").containsIgnoreCase(keyword),
                    q.criteria("remarkName").containsIgnoreCase(keyword));
        }
        long total = q.countAll();
        List<Friends> pageData = q.offset(pageIndex * pageSize).limit(pageSize).asList();
        for (Friends friends : pageData) {
            User toUser = getUserManager().getUser(friends.getToUserId());
            if (null == toUser) {
                deleteFansAndFriends(friends.getToUserId());
                continue;
            }
            if (toUser.getUserId() == 10000) {
                continue;
            }
            friends.setToNickname(toUser.getNickname());
            //friends.setCompanyId(toUser.getCompanyId());
        }
        return new PageVO(pageData, total, pageIndex, pageSize);
    }

    public List<Friends> queryFriendsList(Integer userId, int status, int pageIndex, int pageSize) {
        Query<Friends> q = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId);
        if (0 < status)
            q.filter("status", status);
			/*if(!StringUtil.isEmpty(keyword)){
				q.or(q.criteria("nickname").containsIgnoreCase(keyword),
						q.criteria("telephone").contains(keyword));
			}*/
        List<Friends> pageData = q.offset(pageIndex * pageSize).limit(pageSize).asList();
        for (Friends friends : pageData) {
            User toUser = getUserManager().getUser(friends.getToUserId());
            if (null == toUser) {
                deleteFansAndFriends(friends.getToUserId());
                continue;
            }
            friends.setToNickname(toUser.getNickname());
            //friends.setCompanyId(toUser.getCompanyId());
        }
        return pageData;
    }


    /**
     * 取消关注
     */
    @Override
    public boolean unfollowUser(Integer userId, Integer toUserId) {
        // 删除用户关注
        getFriendsRepository().deleteFriends(userId, toUserId);
        // 更新好友设置操作时间
        updateOfflineOperation(userId, toUserId);
        return true;
    }

    @Override
    public Friends updateRemark(int userId, int toUserId, String remarkName, String describe) {
//		Friends friends = new Friends(userId, toUserId);
//		friends.setRemarkName(remarkName);
//		return getFriendsRepository().updateFriends(friends);
        return getFriendsRepository().updateFriendRemarkName(userId, toUserId, remarkName, describe);
    }


    @Override
    public void deleteFansAndFriends(int userId) {
        getFriendsRepository().deleteFriends(userId);
    }

    /* (non-Javadoc)
     * @see cn.xyz.mianshi.service.FriendsManager#newFriendList(int,int,int)
     */
    @Override
    public List<NewFriends> newFriendList(int userId, int pageIndex, int pageSize) {

        Query<NewFriends> query = getDatastore().createQuery(NewFriends.class);
        query.filter("userId", userId);
        query.or(query.criteria("userId").equal(userId),
                query.criteria("toUserId").equal(userId));

        List<NewFriends> pageData = query.order("-modifyTime").offset(pageIndex * pageSize).limit(pageSize).asList();
        Friends friends = null;
        for (NewFriends newFriends : pageData) {
            friends = getFriends(newFriends.getUserId(), newFriends.getToUserId());
            newFriends.setToNickname(getUserManager().getNickName(newFriends.getToUserId()));
			
			/*if(userId==newFriends.getToUserId()){
				friends=getFriends(newFriends.getToUserId(), newFriends.getUserId());
				newFriends.setToNickname(getUserManager().getNickName(newFriends.getUserId()));
			}
			else{
				friends=getFriends(newFriends.getUserId(), newFriends.getToUserId());
				newFriends.setToNickname(getUserManager().getNickName(newFriends.getToUserId()));
			}*/

            if (null != friends)
                newFriends.setStatus(friends.getStatus());
        }
        //long total=query.countAll();
        //return new PageVO(pageData, total, pageIndex, pageSize);

        return pageData;

    }

    @SuppressWarnings("deprecation")
    public PageVO newFriendListWeb(int userId, int pageIndex, int pageSize) {

        Query<NewFriends> query = getDatastore().createQuery(NewFriends.class);
        query.filter("userId", userId);
        query.or(query.criteria("userId").equal(userId),
                query.criteria("toUserId").equal(userId));
        List<NewFriends> pageData = query.order("-modifyTime").offset(pageIndex * pageSize).limit(pageSize).asList();
        Friends friends = null;
        for (NewFriends newFriends : pageData) {
            friends = getFriends(newFriends.getUserId(), newFriends.getToUserId());
            newFriends.setToNickname(getUserManager().getNickName(newFriends.getToUserId()));
            if (null != friends)
                newFriends.setStatus(friends.getStatus());
        }
        return new PageVO(pageData, query.count(), pageIndex, pageSize);
    }

    /* 消息免打扰、阅后即焚、聊天置顶相关修改
     * type = 0  消息免打扰 ,type = 1  阅后即焚 ,type = 2  聊天置顶
     */
    @Override
    public Friends updateOfflineNoPushMsg(int userId, int toUserId, int offlineNoPushMsg, int type) {
        Query<Friends> q = getDatastore().createQuery(getEntityClass()).field("userId").equal(userId).field("toUserId").equal(toUserId);
        UpdateOperations<Friends> ops = getDatastore().createUpdateOperations(getEntityClass());
        switch (type) {
            case 0:
                ops.set("offlineNoPushMsg", offlineNoPushMsg);
                break;
            case 1:
                ops.set("isOpenSnapchat", offlineNoPushMsg);
                break;
            case 2:
                ops.set("openTopChatTime", (offlineNoPushMsg == 0 ? 0 : DateUtil.currentTimeSeconds()));
                break;
            default:
                break;
        }
        // 多点登录下消息免打扰xmpp通知
        if (getUserManager().isOpenMultipleDevices(userId))
            getUserManager().multipointLoginUpdateUserInfo(userId, getUserManager().getNickName(userId), toUserId, getUserManager().getNickName(toUserId), 1);
        return getDatastore().findAndModify(q, ops);
    }


    /**
     * 添加好友统计      时间单位每日，最好可选择：每日、每月、每分钟、每小时
     *
     * @param startDate
     * @param endDate
     * @param counType  统计类型   1: 每个月的数据      2:每天的数据       3.每小时数据   4.每分钟的数据 (小时)
     */
    public List<Object> getAddFriendsCount(String startDate, String endDate, short timeUnit) {

        List<Object> countData = new ArrayList<>();

        long startTime = 0; //开始时间（秒）

        long endTime = 0; //结束时间（秒）,默认为当前时间

        /**
         * 如时间单位为月和天，默认开始时间为当前时间的一年前 ; 时间单位为小时，默认开始时间为当前时间的一个月前;
         * 时间单位为分钟，则默认开始时间为当前这一天的0点
         */
        long defStartTime = timeUnit == 4 ? DateUtil.getTodayMorning().getTime() / 1000
                : timeUnit == 3 ? DateUtil.getLastMonth().getTime() / 1000 : DateUtil.getLastYear().getTime() / 1000;

        startTime = StringUtil.isEmpty(startDate) ? defStartTime : DateUtil.toDate(startDate).getTime() / 1000;
        endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime() / 1000;

        BasicDBObject queryTime = new BasicDBObject("$ne", null);

        if (startTime != 0 && endTime != 0) {
            queryTime.append("$gt", startTime);
            queryTime.append("$lt", endTime);
        }

        BasicDBObject query = new BasicDBObject("createTime", queryTime);

        //获得用户集合对象
        DBCollection collection = SKBeanUtils.getDatastore().getCollection(getEntityClass());

        String mapStr = "function Map() { "
                + "var date = new Date(this.createTime*1000);"
                + "var year = date.getFullYear();"
                + "var month = (\"0\" + (date.getMonth()+1)).slice(-2);"  //month 从0开始，此处要加1
                + "var day = (\"0\" + date.getDate()).slice(-2);"
                + "var hour = (\"0\" + date.getHours()).slice(-2);"
                + "var minute = (\"0\" + date.getMinutes()).slice(-2);"
                + "var dateStr = date.getFullYear()" + "+'-'+" + "(parseInt(date.getMonth())+1)" + "+'-'+" + "date.getDate();";

        if (timeUnit == 1) { // counType=1: 每个月的数据
            mapStr += "var key= year + '-'+ month;";
        } else if (timeUnit == 2) { // counType=2:每天的数据
            mapStr += "var key= year + '-'+ month + '-' + day;";
        } else if (timeUnit == 3) { //counType=3 :每小时数据
            mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour +' : 00';";
        } else if (timeUnit == 4) { //counType=4 :每分钟的数据
            mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour + ':'+ minute;";
        }

        mapStr += "emit(key,1);}";

        String reduce = "function Reduce(key, values) {" +
                "return Array.sum(values);" +
                "}";
        MapReduceCommand.OutputType type = MapReduceCommand.OutputType.INLINE;//
        MapReduceCommand command = new MapReduceCommand(collection, mapStr, reduce, null, type, query);

        int i = 0;
        MapReduceOutput mapReduceOutput = null;
        while (i < 5) {
            i++;
            try {
                mapReduceOutput = collection.mapReduce(command);
                break;
            } catch (MongoSocketWriteException e) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                logger.info("retry getAddFriendsCount mapReduce:{}", i);
            }
        }
        if (null == mapReduceOutput) return countData;
        Iterable<DBObject> results = mapReduceOutput.results();
        Map<String, Double> map = new HashMap<String, Double>();
        for (Iterator iterator = results.iterator(); iterator.hasNext(); ) {
            DBObject obj = (DBObject) iterator.next();

            map.put((String) obj.get("_id"), (Double) obj.get("value"));
            countData.add(JSON.toJSON(map));
            map.clear();

        }

        return countData;
    }

    // 好友之间的聊天记录
    public PageResult<DBObject> chardRecord(Integer sender, Integer receiver, Integer page, Integer limit) {
        DBCollection dbCollection = getTigaseDatastore().getDB().getCollection("shiku_msgs");
        BasicDBObject query = new BasicDBObject();
        BasicDBList queryOr = new BasicDBList();
        if (0 != sender) {
            queryOr.add(new BasicDBObject("sender", sender).append("receiver", receiver).append("direction", 0));
        }
        if (0 != receiver) {
            queryOr.add(new BasicDBObject("sender", receiver).append("receiver", sender).append("direction", 0));
        }
        query.append(MongoOperator.OR, queryOr);

        long total = dbCollection.count(query);
        List<DBObject> pageData = Lists.newArrayList();

        DBCursor cursor = dbCollection.find(query).sort(new BasicDBObject("_id", -1)).skip((page - 1) * limit).limit(limit);

        PageResult<DBObject> result = new PageResult<DBObject>();

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
//				dbObj.put("content", dbObj.get("content"));
            } catch (Exception e) {
                dbObj.put("content", "--");
            }
            pageData.add(dbObj);
        }

        result.setData(pageData);
        result.setCount(total);
        return result;
    }

    /**
     * @param sender
     * @param receiver
     * @Description:（删除好友间的聊天记录）
     **/
    public void delFriendsChatRecord(String... messageIds) {
        for (String messageId : messageIds) {
            DBCollection dbCollection = getTigaseDatastore().getDB().getCollection("shiku_msgs");
            BasicDBObject query = new BasicDBObject();
            query.put("messageId", messageId);
            dbCollection.remove(query);
        }
    }

    /**
     * @param userId
     * @param request
     * @param response
     * @return
     * @Description: 导出好友列表
     **/
    public Workbook exprotExcelFriends(Integer userId, HttpServletRequest request, HttpServletResponse response) {

        String name = getUserManager().getNickName(userId) + "的好友明细";

        String fileName = "friends.xlsx";

        List<Friends> friends;

        List<Friends> friendsList = SKBeanUtils.getRedisService().getFriendsList(userId);
        if (null != friendsList && friendsList.size() > 0) {
            friends = friendsList;
        } else {
            friends = SKBeanUtils.getFriendsRepository().allFriendsInfo(userId);
        }
        List<String> titles = Lists.newArrayList();
        titles.add("toUserId");
        titles.add("toNickname");
        titles.add("remarkName");
        titles.add("telephone");
        titles.add("status");
        titles.add("blacklist");
        titles.add("isBeenBlack");
        titles.add("offlineNoPushMsg");
        titles.add("createTime");

        List<Map<String, Object>> values = Lists.newArrayList();
        for (Friends friend : friends) {
            // 过滤系统10000号不返回
            if (10000 == friend.getToUserId())
                continue;
            Map<String, Object> map = Maps.newConcurrentMap();
            map.put("toUserId", friend.getToUserId());
            map.put("toNickname", friend.getToNickname());
            map.put("telephone", getUserManager().getUser(friend.getToUserId()).getPhone());
            map.put("status", friend.getStatus() == -1 ? "黑名单" : friend.getStatus() == 2 ? "好友" : "关注");
            map.put("blacklist", friend.getBlacklist() == 1 ? "是" : "否");
            map.put("isBeenBlack", friend.getIsBeenBlack() == 1 ? "是" : "否");
            map.put("offlineNoPushMsg", friend.getBlacklist() == 1 ? "是" : "否");
            map.put("createTime", DateUtil.strToDateTime(friend.getCreateTime()));
            values.add(map);
        }

        Workbook workBook = ExcelUtil.generateWorkbook(name, "xlsx", titles, values);
        response.reset();
        try {
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + new String(fileName.getBytes(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return workBook;
    }

    /**
     * @param userId
     * @param toUserId
     * @param type     -1 所有人不显示 1所有人显示  2 所有好友显示   3 手机联系人显示
     * @return
     * @Description:校验是否为好友或通讯录好友
     **/
    public boolean isAddressBookOrFriends(Integer userId, Integer toUserId, int type) {
        boolean flag = false;
        switch (type) {
            case -1:
                break;
            case 1:
                flag = !flag;
                break;
            case 2:
                List<Integer> friendsUserIdsList;
                List<Integer> allFriendsUserIdsList = SKBeanUtils.getRedisService().getFriendsUserIdsList(userId);
                if (null != allFriendsUserIdsList && allFriendsUserIdsList.size() > 0)
                    friendsUserIdsList = allFriendsUserIdsList;
                else {
                    List<Integer> friendsUserIdsDB = SKBeanUtils.getFriendsManager().queryFansId(userId);
                    friendsUserIdsList = friendsUserIdsDB;
                    SKBeanUtils.getRedisService().saveFriendsUserIdsList(userId, friendsUserIdsList);
                }
                flag = friendsUserIdsList.contains(toUserId);
                break;
            case 3:
                List<Integer> addressBookUserIdsList;
                List<Integer> allAddressBookUserIdsList = SKBeanUtils.getRedisService().getAddressBookFriendsUserIds(userId);
                if (null != allAddressBookUserIdsList && allAddressBookUserIdsList.size() > 0)
                    addressBookUserIdsList = allAddressBookUserIdsList;
                else {
                    List<Integer> AddressBookUserIdsDB = SKBeanUtils.getAddressBookManger().getAddressBookUserIds(userId);
                    addressBookUserIdsList = AddressBookUserIdsDB;
                    SKBeanUtils.getRedisService().saveAddressBookFriendsUserIds(userId, addressBookUserIdsList);
                }
                flag = addressBookUserIdsList.contains(toUserId);
                break;
            default:
                break;
        }
        return flag;
    }
}
