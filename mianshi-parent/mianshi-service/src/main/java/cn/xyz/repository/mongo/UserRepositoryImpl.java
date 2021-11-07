package cn.xyz.repository.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateOpsImpl;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ValueUtil;
import cn.xyz.mianshi.model.UserExample;
import cn.xyz.mianshi.model.UserQueryExample;
import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Comment;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.LiveRoom;
import cn.xyz.mianshi.vo.Msg;
import cn.xyz.mianshi.vo.Praise;
import cn.xyz.mianshi.vo.Role;
import cn.xyz.mianshi.vo.Room;
import cn.xyz.mianshi.vo.Room.Member;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.User.LoginLog;
import cn.xyz.mianshi.vo.User.UserLoginLog;
import cn.xyz.mianshi.vo.User.UserSettings;
import cn.xyz.repository.UserRepository;

@Service
public class UserRepositoryImpl extends MongoRepository<User, Integer> implements UserRepository {

    @Override
    public Datastore getDatastore() {
        return SKBeanUtils.getDatastore();
    }

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    public static UserRepositoryImpl getInstance() {
        return new UserRepositoryImpl();
    }

    @Override
    public synchronized Map<String, Object> addUser(int userId, UserExample example) {
        example.setAccount(userId + StringUtil.randomCode());
        BasicDBObject jo = new BasicDBObject();
        jo.put("_id", userId);// 索引
        jo.put("userKey", DigestUtils.md5Hex(example.getPhone()));// 索引
        jo.put("username", "");
        jo.put("password", example.getPassword());
        jo.put("payPassword", example.getPayPassWord());
        jo.put("userType", ValueUtil.parse(example.getUserType()));

        jo.put("telephone", example.getTelephone());// 索引
        jo.put("phone", example.getPhone());// 索引
        jo.put("account", example.getAccount());
        jo.put("areaCode", example.getAreaCode());// 索引
        jo.put("name", ValueUtil.parse(example.getName()));// 索引
        if (10000 == userId)
            jo.put("nickname", "客服公众号");// 索引
        else
            jo.put("nickname", ValueUtil.parse(example.getNickname()));// 索引
        jo.put("description", ValueUtil.parse(example.getDescription()));
        jo.put("birthday", ValueUtil.parse(example.getBirthday()));// 索引
        jo.put("sex", ValueUtil.parse(example.getSex()));// 索引
        jo.put("loc", new BasicDBObject("lng", example.getLongitude()).append(
                "lat", example.getLatitude()));// 索引

        jo.put("countryId", ValueUtil.parse(example.getCountryId()));
        jo.put("provinceId", ValueUtil.parse(example.getProvinceId()));
        jo.put("cityId", ValueUtil.parse(example.getCityId()));
        jo.put("areaId", ValueUtil.parse(example.getAreaId()));

        jo.put("money", 0.00);
        jo.put("moneyTotal", 0.00);
        jo.put("balance", 0.00);
        jo.put("totalRecharge", 0.00);

        jo.put("level", 0);
        jo.put("vip", 0);

        jo.put("friendsCount", 0);
        jo.put("fansCount", 0);
        jo.put("attCount", 0);
        jo.put("msgNum", 0);

        jo.put("createTime", DateUtil.currentTimeSeconds());
        jo.put("modifyTime", DateUtil.currentTimeSeconds());

        jo.put("idcard", "");
        jo.put("idcardUrl", "");

        jo.put("isAuth", example.getIsSmsRegister() == 1 ? 1 : 0);
        jo.put("status", 1);
        jo.put("onlinestate", 0);
        jo.put("regInviteCode", example.getInviteCode());
        // 初始化登录日志
        //jo.put("loginLog", User.UserLoginLog.init(example, true));
        // 初始化用户设置
        jo.put("settings", User.UserSettings.getDefault());
        // 1、新增用户
        getDatastore().getDB().getCollection("user").save(jo);
        initUserLogin(userId, example);
        try {
            // 2、缓存用户认证数据到
            Map<String, Object> data = KSessionUtil.loginSaveAccessToken(userId, userId, null);
            data.put("userId", userId);
            data.put("nickname", jo.getString("nickname"));
            data.put("name", jo.getString("name"));
            data.put("createTime", jo.getLong("createTime"));
            // 3、缓存用户数据

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void initUserLogin(int userId, UserExample example) {
        UserLoginLog loginLog = new UserLoginLog();
        loginLog.setUserId(userId);
        LoginLog log = UserLoginLog.init(example, true);
        loginLog.setLoginLog(log);
        loginLog.setDeviceMap(Maps.newHashMap());
        getDatastore().save(loginLog);
    }

    @Override
    public void addUser(User user) {
        getDatastore().save(user);
    }

    @Override
    public void addUser(int userId, String password) {
        BasicDBObject jo = new BasicDBObject();
        jo.put("_id", userId);// 索引
        if (null != getDatastore().getCollection(getEntityClass()).findOne(jo))
            return;
        jo.put("userKey", DigestUtils.md5Hex(userId + ""));// 索引
        jo.put("username", String.valueOf(userId));
        jo.put("password", DigestUtils.md5Hex(password));

        if (userId == 10000) {
            jo.put("userType", ValueUtil.parse(2));
            Role role = new Role(userId, String.valueOf(userId), (byte) 2, (byte) 1, 0);
            getDatastore().save(role);
        } else {
            jo.put("userType", ValueUtil.parse(1));
        }


        //jo.put("companyId", ValueUtil.parse(0));  当前版本用户表中不在维护公司Id
        jo.put("telephone", "86" + String.valueOf(userId));// 索引
        jo.put("areaCode", "86");// 索引
        jo.put("name", String.valueOf(userId));// 索引
        if (10000 == userId) {
            jo.put("nickname", "客服公众号");// 索引
            jo.put("phone", userId);
        } else if (1100 == userId)
            jo.put("nickname", "系统通知");
        else
            jo.put("nickname", String.valueOf(userId));// 索引

        jo.put("description", String.valueOf(userId));
        jo.put("birthday", ValueUtil.parse(userId));// 索引
        jo.put("sex", ValueUtil.parse(userId));// 索引
        jo.put("loc", new BasicDBObject("lng", 10.00).append(
                "lat", 10.00));// 索引

        jo.put("countryId", ValueUtil.parse(0));
        jo.put("provinceId", ValueUtil.parse(0));
        jo.put("cityId", ValueUtil.parse(400300));
        jo.put("areaId", ValueUtil.parse(0));

        jo.put("money", 0);
        jo.put("moneyTotal", 0);

        jo.put("level", 0);
        jo.put("vip", 0);

        jo.put("friendsCount", 0);
        jo.put("fansCount", 0);
        jo.put("attCount", 0);

        jo.put("createTime", DateUtil.currentTimeSeconds());
        jo.put("modifyTime", DateUtil.currentTimeSeconds());

        jo.put("idcard", "");
        jo.put("idcardUrl", "");

        jo.put("isAuth", 0);
        jo.put("status", 1);
        jo.put("onlinestate", 1);
        // 初始化登录日志
        //jo.put("loginLog", User.LoginLog.init(example, true));
        // 初始化用户设置
        jo.put("settings", User.UserSettings.getDefault());

        // 1、新增用户
        getDatastore().getDB().getCollection("user").save(jo);

    }


    @Override
    public List<User> findByTelephone(List<String> telephoneList) {
        Query<User> query = getDatastore().createQuery(getEntityClass()).filter(
                "telephone in", telephoneList);
        return query.asList();
    }


    /**
     * hsg
     * 2018-07-28
     * 检索用户数据
     *
     * @param strKeyworld 字符型关键词，用于匹配昵称、手机号等
     * @param userId      用于匹配用户id
     * @param onlinestate 匹配在线状态
     * @param userType    匹配用户类型     用户类型：1=普通用户；2=公众号 ；3=机器账号，由系统自动生成；4=客服账号
     * @return userList     返回值为检索到的用户列表
     */
    public List<User> searchUsers(int pageIndex, int pageSize, String strKeyworld, short onlinestate, short userType) {
        List<User> users = new ArrayList<User>();
        @SuppressWarnings("deprecation")
        List<Role> robots = getDatastore().createQuery(Role.class).field("role").equal(userType).order("-createTime").offset(pageIndex * pageSize).limit(pageSize).asList();
        robots.forEach(robot -> {
            User user = SKBeanUtils.getUserManager().getUser(robot.getUserId());
            if (null != user)
                users.add(user);
        });
        return users;
    }


    @Override
    public long getCount(String telephone) {
        return getDatastore().createQuery(getEntityClass()).field("telephone")
                .equal(telephone).countAll();
    }


    @Override
    public User.LoginLog getLogin(int userId) {


        UserLoginLog userLoginLog = getDatastore().createQuery(UserLoginLog.class).field("_id").equal(userId).get();
        if (null == userLoginLog || null == userLoginLog.getLoginLog()) {
            UserLoginLog loginLog = new UserLoginLog();
            loginLog.setUserId(userId);
            loginLog.setLoginLog(new LoginLog());
            getDatastore().save(loginLog);
            return loginLog.getLoginLog();
        } else {
            return userLoginLog.getLoginLog();
        }


    }


    @Override
    public User.UserSettings getSettings(int userId) {
        UserSettings settings = null;
        User user = null;
        user = getDatastore().createQuery(getEntityClass()).field("_id").equal(userId).get();
        if (null == user)
            return null;
        settings = user.getSettings();
        return null != settings ? settings : new UserSettings();

    }


    @Override
    public User getUser(int userId) {
        Query<User> query = getDatastore().createQuery(getEntityClass()).field("_id")
                .equal(userId);
        return query.get();
    }


    @Override
    public User getUser(String telephone) {
        Query<User> query = getDatastore().createQuery(getEntityClass()).field("telephone")
                .equal(telephone);

        return query.get();
    }


    public boolean getUserByAccount(String account, Integer userId) {
        Query<User> query = getDatastore().createQuery(getEntityClass()).field("_id").notEqual(userId);
        query.or(query.criteria("account").equal(account), query.criteria("phone").equal(account));
        return null == query.get();
    }


    @Override
    public User getUser(String areaCode, String userKey, String password) {
        Query<User> query = getDatastore().createQuery(getEntityClass());
        if (!StringUtil.isEmpty(areaCode))
            query.field("areaCode").equal(areaCode);
        if (!StringUtil.isEmpty(userKey)) {
            // 支持通讯号
            query.or(query.criteria("userKey").equal(userKey), query.criteria("encryAccount").equal(userKey));
        }
        if (!StringUtil.isEmpty(password))
            query.field("password").equal(password);

        return query.get();
    }

    @Override
    public User getUserv1(String userKey, String password) {
        Query<User> query = getDatastore().createQuery(getEntityClass());
        if (!StringUtil.isEmpty(userKey))
            query.field("userKey").equal(userKey);
        if (!StringUtil.isEmpty(password))
            query.field("password").equal(password);

        return query.get();
    }

    @Override
    public List<DBObject> queryUser(UserQueryExample example) {
        List<DBObject> list = Lists.newArrayList();
        // Query<User> query = mongoDs.find(getEntityClass());
        // Query<User> query =mongoDs.createQuery(getEntityClass());
        // query.filter("_id<", param.getUserId());
        DBObject ref = new BasicDBObject();
        if (null != example.getUserId())
            ref.put("_id", new BasicDBObject("$lt", example.getUserId()));
        if (!StringUtil.isEmpty(example.getNickname()))
            ref.put("nickname", Pattern.compile(example.getNickname()));
        if (null != example.getSex())
            ref.put("sex", example.getSex());
        if (null != example.getStartTime())
            ref.put("birthday",
                    new BasicDBObject("$gte", example.getStartTime()));
        if (null != example.getEndTime())
            ref.put("birthday", new BasicDBObject("$lte", example.getEndTime()));
        DBObject fields = new BasicDBObject();
        fields.put("userKey", 0);
        fields.put("password", 0);
        fields.put("money", 0);
        fields.put("moneyTotal", 0);
        fields.put("status", 0);

        DBCursor cursor = getDatastore().getDB().getCollection("user")
                .find(ref, fields).sort(new BasicDBObject("_id", -1))
                .limit(example.getPageSize());
        while (cursor.hasNext()) {
            DBObject obj = cursor.next();
            obj.put("userId", obj.get("_id"));
            obj.removeField("_id");

            list.add(obj);
        }

        return list;
    }

    @Override
    public List<DBObject> findUser(int pageIndex, int pageSize) {
        List<DBObject> list = Lists.newArrayList();
        DBObject fields = new BasicDBObject();
        fields.put("userKey", 0);
        fields.put("password", 0);
        fields.put("money", 0);
        fields.put("moneyTotal", 0);
        fields.put("status", 0);
        DBCursor cursor = getDatastore().getDB().getCollection("user")
                .find(null, fields).sort(new BasicDBObject("_id", -1))
                .skip(pageIndex * pageSize).limit(pageSize);
        while (cursor.hasNext()) {
            DBObject obj = cursor.next();
            obj.put("userId", obj.get("_id"));
            obj.removeField("_id");
            list.add(obj);
        }

        return list;
    }


    @Override
    public void updateLogin(int userId, String serial) {
        DBObject value = new BasicDBObject();

        value.put("serial", serial);


        DBObject q = new BasicDBObject("_id", userId);
        DBObject o = new BasicDBObject("$set", new BasicDBObject("loginLog",
                value));
        getDatastore().getDB().getCollection("user").update(q, o);
    }

    @Override
    public void updateLogin(int userId, UserExample example) {
        BasicDBObject loc = new BasicDBObject(2);
        loc.put("loc.lng", example.getLongitude());
        loc.put("loc.lat", example.getLatitude());

        DBObject values = new BasicDBObject();
		/*values.put("loginLog.isFirstLogin", 0);
		values.put("loginLog.loginTime", DateUtil.currentTimeSeconds());
		values.put("loginLog.apiVersion", example.getApiVersion());
		values.put("loginLog.osVersion", example.getOsVersion());
		values.put("loginLog.model", example.getModel());
		values.put("loginLog.serial", example.getSerial());
		values.put("loginLog.latitude", example.getLatitude());
		values.put("loginLog.longitude", example.getLongitude());
		values.put("loginLog.location", example.getLocation());
		values.put("loginLog.address", example.getAddress());*/
        values.put("loc.lng", example.getLongitude());
        values.put("loc.lat", example.getLatitude());
        values.put("appId", example.getAppId());
        values.put("active", DateUtil.currentTimeSeconds());

        DBObject q = new BasicDBObject("_id", userId);
        DBObject o = new BasicDBObject("$set", values);
        // ("loginLog",
        // loginLog)).append
        getDatastore().getCollection(getEntityClass()).update(q, o);

    }

    public void updateUserLoginLog(int userId, UserExample example) {
        DBObject query = new BasicDBObject("_id", userId);


        DBObject values = new BasicDBObject();
        DBObject object = getDatastore().getCollection(UserLoginLog.class).findOne(query);
        if (null == object)
            values.put("_id", userId);


        BasicDBObject loginLog = new BasicDBObject("isFirstLogin", 0);
        loginLog.put("loginTime", DateUtil.currentTimeSeconds());
        loginLog.put("apiVersion", example.getApiVersion());
        loginLog.put("osVersion", example.getOsVersion());
        loginLog.put("model", example.getModel());
        loginLog.put("serial", example.getSerial());
        loginLog.put("latitude", example.getLatitude());
        loginLog.put("longitude", example.getLongitude());
        loginLog.put("location", example.getLocation());
        loginLog.put("address", example.getAddress());
        values.put("loginLog", loginLog);

        getDatastore().getCollection(UserLoginLog.class)
                .update(query, new BasicDBObject(MongoOperator.SET, values), true, false);


        //updateAttribute(userId, "appId", example.getAppId());
    }

    public void updateLoginLogTime(int userId) {
        DBObject query = new BasicDBObject("_id", userId);


        DBObject values = new BasicDBObject();
        DBObject object = getDatastore().getCollection(UserLoginLog.class).findOne(query);
        BasicDBObject loginLog = null;
        if (null == object || null == object.get("loginLog")) {
            values.put("_id", userId);

            loginLog = new BasicDBObject("isFirstLogin", 0);
            loginLog.put("loginTime", DateUtil.currentTimeSeconds());
            values.put("loginLog", loginLog);
            getDatastore().getCollection(UserLoginLog.class)
                    .update(query, new BasicDBObject(MongoOperator.SET, values), true, false);
        } else {
            values.put("loginLog.loginTime", DateUtil.currentTimeSeconds());
            getDatastore().getCollection(UserLoginLog.class)
                    .update(query, new BasicDBObject(MongoOperator.SET, values), true, false);

        }

    }

    @Override
    public User updateUser(int userId, UserExample example) {
        Query<User> q = getDatastore().createQuery(getEntityClass()).field("_id").equal(userId);
        UpdateOperations<User> ops = getDatastore().createUpdateOperations(getEntityClass());
        User oldUser = SKBeanUtils.getUserManager().getUser(userId);
        boolean updateName = false;
        List<Integer> userRoles = SKBeanUtils.getRoleManager().getUserRoles(userId);
        if (null != example.getUserType()) {
            if (userRoles.size() == 0 || userRoles.contains(2) || userRoles.contains(0)) {

                boolean flag = userRoles.contains(2);
                if (example.getUserType() == 2 && !flag) {
                    ops.set("userType", example.getUserType());
                    Role role = new Role(userId, example.getPhone(), (byte) 2, (byte) 1, 0);
                    getDatastore().save(role);
                    SKBeanUtils.getRoleManager().updateFriend(userId, 2);
                } else if (example.getUserType() == 0) {
                    ops.set("userType", example.getUserType());
                    Query<Role> query = getDatastore().createQuery(Role.class).field("userId").equal(userId);
                    if (null != query.get())
                        getDatastore().delete(query);
                }
                SKBeanUtils.getRoleManager().updateFriend(userId, example.getUserType());

            } else {
                if (example.getUserType() == 2)
                    throw new ServiceException("该用户已经有其他角色");
            }
        }
        if (!StringUtil.isEmpty(example.getAccount())) {
            if (0 < oldUser.getSetAccountCount()) {
                throw new ServiceException("账号只能修改一次 ");
            }
            if (example.getAccount().length() > 18) {
                throw new ServiceException("通讯号最长十八位");
            }
//			User queryOne = queryOne("account",example.getAccount());
            boolean userByAccount = getUserByAccount(example.getAccount(), userId);
            if (!userByAccount) {
                throw new ServiceException("账号已存在 ");
            }
            ops.set("account", example.getAccount());
            ops.inc("setAccountCount", 1);
            ops.set("encryAccount", DigestUtils.md5Hex(example.getAccount()));
            ops.set("modifyTime", DateUtil.currentTimeSeconds());
            User user = getDatastore().findAndModify(q, ops);
            KSessionUtil.deleteUserByUserId(userId);
            return user;
        }
        if (!StringUtil.isEmpty(example.getNickname())) {
            ops.set("nickname", example.getNickname());
            updateName = true;
        }


        if (!StringUtil.isEmpty(example.getTelephone())) {
            ops.set("userKey", DigestUtils.md5Hex(example.getPhone()));
            ops.set("telephone", example.getTelephone());
        }
        if (!StringUtil.isEmpty(example.getPhone()))
            ops.set("phone", example.getPhone());

        if (!StringUtil.isEmpty(example.getPayPassWord()))
            ops.set("payPassword", example.getPayPassWord());
        if (!StringUtil.isEmpty(example.getMsgBackGroundUrl()))
            ops.set("msgBackGroundUrl", example.getMsgBackGroundUrl());

        if (!StringUtil.isEmpty(example.getDescription()))
            ops.set("description", example.getDescription());
        if (null != example.getBirthday())
            ops.set("birthday", example.getBirthday());
        if (null != example.getSex())
            ops.set("sex", example.getSex());
        if (null != example.getCountryId())
            ops.set("countryId", example.getCountryId());
        if (null != example.getProvinceId())
            ops.set("provinceId", example.getProvinceId());
        if (null != example.getCityId())
            ops.set("cityId", example.getCityId());
        if (null != example.getAreaId())
            ops.set("areaId", example.getAreaId());

        if (null != example.getName())
            ops.set("name", example.getName());

        if (null != example.getIdcard())
            ops.set("idcard", example.getIdcard());
        if (null != example.getIdcardUrl())
            ops.set("idcardUrl", example.getIdcardUrl());
        if (-1 < example.getMultipleDevices())
            ops.set("multipleDevices", example.getMultipleDevices());
        if (0 < example.getLongitude())
            ops.set("loc.lng", example.getLongitude());
        if (0 < example.getLatitude())
            ops.set("loc.lat", example.getLatitude());

        ops.set("modifyTime", DateUtil.currentTimeSeconds());

        User user = getDatastore().findAndModify(q, ops);
        // 删除redis中的用户
        KSessionUtil.deleteUserByUserId(userId);
        // 修改用户昵称时 同步该用户创建的群主昵称
        if (updateName) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    BasicDBObject quserId = new BasicDBObject().append("userId", userId);
                    DBObject values = new BasicDBObject();
                    values.put("nickname", example.getNickname());
                    DBObject q = new BasicDBObject("$set", values);
                    BasicDBObject qtoUserId = new BasicDBObject().append("toUserId", userId);
                    DBObject values1 = new BasicDBObject();
                    values1.put("toNickname", example.getNickname());
                    DBObject o = new BasicDBObject("$set", values1);
                    //修改群组中的创建人名称//修改nickname
                    SKBeanUtils.getImRoomDatastore().getCollection(Room.class).update(quserId, q, false, true);
                    SKBeanUtils.getImRoomDatastore().getCollection(Member.class).update(quserId, q, false, true);
                    //修改好友名称
                    getDatastore().getCollection(Friends.class).update(qtoUserId, o, false, true);
                    //修改朋友圈中的用户名称
                    DBObject p = new BasicDBObject("$set", values);
                    getDatastore().getCollection(Msg.class).update(quserId, p, false, true);
                    // 朋友圈评论、点赞的用户名称
                    getDatastore().getCollection(Comment.class).update(quserId, p, false, true);
                    getDatastore().getCollection(Praise.class).update(quserId, p, false, true);

                    // 修改创建的直播间中的nickName
                    DBObject liveRoomValues = new BasicDBObject();
                    liveRoomValues.put("nickName", example.getNickname());
                    DBObject liveQuery = new BasicDBObject("$set", liveRoomValues);
                    getDatastore().getCollection(LiveRoom.class).update(quserId, liveQuery, false, true);
                    getDatastore().getCollection(LiveRoom.LiveRoomMember.class).update(quserId, liveQuery, false, true);
                    // 维护redis 用户相关数据
                    updateUserRelevantInfo(userId);
                }

            }).start();


        }

        return user;

    }

    /**
     * @param userId
     * @Description: 维护用户相关数据
     **/
    public void updateUserRelevantInfo(Integer userId) {
        // 好友名称(维护自己好友列表的数据)
        List<Integer> toUserIds = SKBeanUtils.getFriendsRepository().queryFansId(userId);
        toUserIds.forEach(toUserId -> {
            SKBeanUtils.getRedisService().deleteFriends(toUserId);
        });

        // 加入的群Ids
        List<ObjectId> roomIdList = SKBeanUtils.getRoomManagerImplForIM().getRoomIdList(userId);
        roomIdList.forEach(str -> {
            SKBeanUtils.getRedisService().deleteRoom(str.toString());
        });

        // 发送的朋友圈评论
        List<ObjectId> msgIds = SKBeanUtils.getMsgCommentRepository().getCommentIds(userId);
        msgIds.forEach(msgId -> {
            SKBeanUtils.getRedisService().deleteMsgComment(msgId.toString());
        });
        // 发送的朋友圈点赞
        List<ObjectId> strMsgIds = SKBeanUtils.getMsgPraiseRepository().getPraiseIds(userId);
        strMsgIds.forEach(msgId -> {
            SKBeanUtils.getRedisService().deleteMsgPraise(msgId.toString());
        });
    }

    @Override
    public User updateSettings(int userId, User.UserSettings userSettings) {
        Query<User> q = getDatastore().createQuery(getEntityClass()).field("_id")
                .equal(userId);
        UpdateOperations<User> ops = getDatastore().createUpdateOperations(getEntityClass());
        if (null != new Integer(userSettings.getAllowAtt()))
            ops.set("settings.allowAtt", userSettings.getAllowAtt());
        if (null != new Integer(userSettings.getAllowGreet()))
            ops.set("settings.allowGreet", userSettings.getAllowGreet());
        if (-1 != userSettings.getFriendsVerify())
            ops.set("settings.friendsVerify", userSettings.getFriendsVerify());
        //是否开启客服模式
        if (null != new Integer(userSettings.getAllowAtt())) {
            ops.set("settings.openService", userSettings.getOpenService());
        }
        if (null != new Integer(userSettings.getCloseTelephoneFind())) {
            ops.set("settings.closeTelephoneFind", userSettings.getCloseTelephoneFind());
        }
        if (!"0".equals(userSettings.getChatRecordTimeOut())) {
            ops.set("settings.chatRecordTimeOut", userSettings.getChatRecordTimeOut());
        }
        if (0 != userSettings.getChatSyncTimeLen()) {
            ops.set("settings.chatSyncTimeLen", userSettings.getChatSyncTimeLen());
            SKBeanUtils.getUserManager().multipointLoginDataSync(userId, q.get().getNickname(), KConstants.MultipointLogin.SYNC_PRIVATE_SETTINGS);
        }
        if (-1 != userSettings.getIsEncrypt()) {
            ops.set("settings.isEncrypt", userSettings.getIsEncrypt());
            SKBeanUtils.getUserManager().multipointLoginDataSync(userId, q.get().getNickname(), KConstants.MultipointLogin.SYNC_PRIVATE_SETTINGS);
        }
        if (-1 != userSettings.getIsTyping()) {
            ops.set("settings.isTyping", userSettings.getIsTyping());
            SKBeanUtils.getUserManager().multipointLoginDataSync(userId, q.get().getNickname(), KConstants.MultipointLogin.SYNC_PRIVATE_SETTINGS);
        }
        if (-1 != userSettings.getIsUseGoogleMap())
            ops.set("settings.isUseGoogleMap", userSettings.getIsUseGoogleMap());
        if (-1 != userSettings.getIsVibration()) {
            ops.set("settings.isVibration", userSettings.getIsVibration());
            SKBeanUtils.getUserManager().multipointLoginDataSync(userId, q.get().getNickname(), KConstants.MultipointLogin.SYNC_PRIVATE_SETTINGS);
        }
        if (-1 != userSettings.getMultipleDevices())
            ops.set("settings.multipleDevices", userSettings.getMultipleDevices());
        if (-1 != userSettings.getIsKeepalive())
            ops.set("settings.isKeepalive", userSettings.getIsKeepalive());
        if (-1 != userSettings.getPhoneSearch())
            ops.set("settings.phoneSearch", userSettings.getPhoneSearch());
        if (-1 != userSettings.getNameSearch())
            ops.set("settings.nameSearch", userSettings.getNameSearch());
        if (0 != userSettings.getShowLastLoginTime())
            ops.set("settings.showLastLoginTime", userSettings.getShowLastLoginTime());
        if (0 != userSettings.getShowTelephone())
            ops.set("settings.showTelephone", userSettings.getShowTelephone());
        if (null != userSettings.getFriendFromList())
            ops.set("settings.friendFromList", userSettings.getFriendFromList());
        if (null != userSettings.getFilterCircleUserIds())
            ops.set("settings.filterCircleUserIds", userSettings.getFilterCircleUserIds());
        return getDatastore().findAndModify(q, ops);
    }

    public UserSettings getUserSetting(Integer userId) {
        Query<UserSettings> query = getDatastore().createQuery(UserSettings.class).field("userId").equal(userId);
        return query.get();
    }

    @Override
    public User updateUser(User user) {
        Query<User> q = getDatastore().createQuery(getEntityClass()).field("_id")
                .equal(user.getUserId());
        UpdateOpsImpl<User> ops = (UpdateOpsImpl<User>) getDatastore().createUpdateOperations(getEntityClass());
        if (!StringUtil.isNullOrEmpty(user.getTelephone())) {
            ops.set("userKey", DigestUtils.md5Hex(user.getTelephone()));
            ops.set("telephone", user.getTelephone());
        }
        if (!StringUtil.isNullOrEmpty(user.getUsername()))
            ops.set("username", user.getUsername());
		/*if (!StringUtil.isNullOrEmpty(user.getPassword()))
			ops.set("password", user.getPassword());*/

        if (null != user.getUserType())
            ops.set("userType", user.getUserType());

        if (!StringUtil.isNullOrEmpty(user.getName()))
            ops.set("name", user.getName());
        if (!StringUtil.isNullOrEmpty(user.getNickname()))
            ops.set("nickname", user.getNickname());
        if (!StringUtil.isNullOrEmpty(user.getDescription()))
            ops.set("description", user.getDescription());
        if (null != user.getBirthday())
            ops.set("birthday", user.getBirthday());
        if (null != user.getSex())
            ops.set("sex", user.getSex());

        if (null != user.getCountryId())
            ops.set("countryId", user.getCountryId());
        if (null != user.getProvinceId())
            ops.set("provinceId", user.getProvinceId());
        if (null != user.getCityId())
            ops.set("cityId", user.getCityId());
        if (null != user.getAreaId())
            ops.set("areaId", user.getAreaId());

        if (null != user.getLevel())
            ops.set("level", user.getLevel());
        if (null != user.getVip())
            ops.set("vip", user.getVip());
		
		/*if(null!=user.getActive()){
			ops.set("active", user.getActive());
		}*/
        // if (null != user.getFriendsCount())
        // ops.set("friendsCount", user.getFriendsCount());
        // if (null != user.getFansCount())
        // ops.set("fansCount", user.getFansCount());
        // if (null != user.getAttCount())
        // ops.set("attCount", user.getAttCount());

        // ops.set("createTime", null);
        ops.set("modifyTime", DateUtil.currentTimeSeconds());

        if (!StringUtil.isNullOrEmpty(user.getIdcard()))
            ops.set("idcard", user.getIdcard());
        if (!StringUtil.isNullOrEmpty(user.getIdcardUrl()))
            ops.set("idcardUrl", user.getIdcardUrl());

        if (null != user.getIsAuth())
            ops.set("isAuth", user.getIsAuth());
        if (null != user.getStatus())
            ops.set("status", user.getStatus());
        return getDatastore().findAndModify(q, ops);
    }

    @Override
    public void updatePassword(String telephone, String password) {
        Query<User> q = getDatastore().createQuery(getEntityClass()).field("telephone")
                .equal(telephone);
        UpdateOperations<User> ops = getDatastore().createUpdateOperations(getEntityClass());
        ops.set("password", password);
        getDatastore().findAndModify(q, ops);
    }

    @Override
    public void updatePassowrd(int userId, String password) {
        Query<User> q = getDatastore().createQuery(getEntityClass()).field("_id")
                .equal(userId);
        UpdateOperations<User> ops = getDatastore().createUpdateOperations(getEntityClass());
        ops.set("password", password);
        getDatastore().findAndModify(q, ops);
        // 更新redis中的数据
        KSessionUtil.deleteUserByUserId(userId);
    }

}
