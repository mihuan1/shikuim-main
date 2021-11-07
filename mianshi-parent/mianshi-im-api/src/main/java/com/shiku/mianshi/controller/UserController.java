package com.shiku.mianshi.controller;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.KConstants.ResultMsgs;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.WXUserUtils;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.ConfigVO;
import cn.xyz.mianshi.model.UserExample;
import cn.xyz.mianshi.model.UserQueryExample;
import cn.xyz.mianshi.model.UserSettingVO;
import cn.xyz.mianshi.service.impl.UserManagerImpl;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.*;
import cn.xyz.mianshi.vo.User.DeviceInfo;
import cn.xyz.service.AuthServiceUtils;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.util.AliPayUtil;
import com.google.common.collect.Maps;
import com.wxpay.utils.WXPayUtil;
import com.wxpay.utils.WxPayDto;
import org.apache.http.client.ClientProtocolException;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("/user")
public class UserController extends AbstractController {

    private static UserManagerImpl getUserManager() {
        UserManagerImpl userManager = SKBeanUtils.getUserManager();
        return userManager;
    }

    @RequestMapping(value = "/register")
    public JSONMessage register(@Valid UserExample example) {
        try {
            example.setPhone(example.getTelephone());
            example.setTelephone(example.getAreaCode() + example.getTelephone());
            Object data = getUserManager().registerIMUser(example);
            return JSONMessage.success(null, data);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }
    }


    /**
     * 第三方sdk注册
     *
     * @param example
     * @param type
     * @param loginInfo
     * @return
     */
    @RequestMapping(value = "/registerSDK")
    public JSONMessage registerSDK(@ModelAttribute UserExample example, @RequestParam int type, @RequestParam String loginInfo) {
        try {
            example.setPhone(example.getTelephone());
            example.setTelephone(example.getAreaCode() + example.getTelephone());
            Object data = getUserManager().registerIMUser(example, type, loginInfo);
            return JSONMessage.success(null, data);
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 绑定手机号码
     *
     * @param telephone
     * @param type
     * @param loginInfo
     * @return
     */
    @RequestMapping(value = "/bindingTelephone")
    public JSONMessage bindingTelephone(@RequestParam String telephone, @RequestParam int type, @RequestParam String loginInfo, @RequestParam String password) {
        try {
            User user = getUserManager().getUser(telephone);

            if (user != null) {
                if (!user.getPassword().equals(password)) {
                    return JSONMessage.failure("账号密码错误");
                }
                SdkLoginInfo sdkLoginInfo = getUserManager().findSdkLoginInfo(type, loginInfo);

                if (sdkLoginInfo == null)
                    getUserManager().addSdkLoginInfo(type, user.getUserId(), loginInfo);

                UserExample example = new UserExample();
                example.setPassword(user.getPassword());
                example.setUserId(user.getUserId());
                example.setIsSdkLogin(1);
                Object data = getUserManager().login(example);
                return JSONMessage.success(data);
            } else {
                // 账号不存在
                return JSONMessage.failureByErrCode(KConstants.ResultCode.SdkLoginNotExist, "zh");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure(e.getMessage());
        }

    }

    /**
     * 解除绑定
     *
     * @param type
     * @return
     */
    @RequestMapping(value = "/unbind")
    public JSONMessage unbind(@RequestParam(defaultValue = "") int type) {
        JSONMessage result = getUserManager().unbind(type, ReqUtil.getUserId());
        return result;
    }

    /**
     * 获取用户绑定信息
     *
     * @return
     */
    @RequestMapping(value = "/getBindInfo")
    public JSONMessage getBingInfo() {
        Object data = getUserManager().getBindInfo(ReqUtil.getUserId());
        return JSONMessage.success(null, data);
    }

    /**
     * 获取微信 openid
     *
     * @param code
     * @return
     */
    @RequestMapping(value = "/getWxOpenId")
    public JSONMessage getWxOpenId(@RequestParam String code) {
        Object data = getUserManager().getWxOpenId(code);
        if (data != null) {
            return JSONMessage.success(data);
        } else {
            return JSONMessage.failure("获取openId失败");
        }

    }

    /**
     * 第三方sdk登录
     *
     * @param example
     * @param type
     * @param loginInfo
     * @return
     */
    @RequestMapping(value = "/sdkLogin")
    public JSONMessage sdkLogin(@ModelAttribute UserExample example, @RequestParam int type, @RequestParam String loginInfo) {
        SdkLoginInfo sdkLoginInfo = getUserManager().findSdkLoginInfo(type, loginInfo);
        if (sdkLoginInfo != null) {
            User user = getUserManager().get(sdkLoginInfo.getUserId());
            example.setPassword(user.getPassword());
            example.setUserId(user.getUserId());
            example.setIsSdkLogin(1);
            Object data = getUserManager().login(example);
            return JSONMessage.success(data);
        } else {
            // 未绑定手机号码
            return JSONMessage.failureByErrCode(KConstants.ResultCode.UNBindingTelephone, "zh");
        }
    }

    /**
     * 扫描二维码登录
     *
     * @param qrCodeKey
     * @param type=     1:客户端扫描到二维码  2：客户端确认登录
     * @return
     */
    @RequestMapping(value = "/qrCodeLogin")
    public JSONMessage qrCodeLogin(@RequestParam String qrCodeKey, @RequestParam int type) {
        Map<String, String> map = (Map<String, String>) SKBeanUtils.getRedisService().queryQRCodeKey(qrCodeKey);
        if (null != map) {
            if (type == 1) {
                map.put("status", "1");
                map.put("QRCodeToken", "");
                SKBeanUtils.getRedisService().saveQRCodeKey(qrCodeKey, map);
            } else if (type == 2) {
                map.put("status", "2");
                map.put("QRCodeToken", getAccess_token());
                map.put("userId", ReqUtil.getUserId().toString());
                SKBeanUtils.getRedisService().saveQRCodeKey(qrCodeKey, map);
            }
            return JSONMessage.success();
        } else {
            return JSONMessage.failure("二维码已过期");
        }

    }

    @RequestMapping(value = "/login")
    public JSONMessage login(@ModelAttribute UserExample example) {
        try {
            Object data = getUserManager().login(example);
            return JSONMessage.success(null, data);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    @RequestMapping(value = "/login/v1")
    public JSONMessage loginv1(@ModelAttribute UserExample example) {
        //example.setTelephone(example.getAreaCode()+example.getTelephone());
        Object data = getUserManager().login(example);
        return JSONMessage.success(null, data);
    }

    @RequestMapping(value = "/login/auto")
    public JSONMessage loginAuto(@RequestParam String access_token, @RequestParam(defaultValue = "0") int userId,
                                 @RequestParam(defaultValue = "") String serial, @RequestParam(defaultValue = "") String appId,
                                 @RequestParam(defaultValue = "0.0") double latitude, @RequestParam(defaultValue = "0.0") double longitude) {
        Object data = getUserManager().loginAuto(access_token, userId, serial, appId, latitude, longitude);
        return JSONMessage.success(null, data);
    }

    @RequestMapping(value = "/logout")
    public JSONMessage logout(@RequestParam String access_token,
                              @RequestParam(defaultValue = "86") String areaCode, String telephone,
                              @RequestParam(defaultValue = "") String deviceKey, @RequestParam(defaultValue = "") String devicekey) {

        if (StringUtil.isEmpty(deviceKey) && !StringUtil.isEmpty(devicekey)) {
            deviceKey = devicekey;
        }
        getUserManager().logout(access_token, areaCode, telephone, deviceKey);

        return JSONMessage.success();
    }

    @RequestMapping(value = "/outtime")
    public JSONMessage outtime(@RequestParam String access_token, @RequestParam int userId) {
        getUserManager().outtime(access_token, userId);
        return JSONMessage.success();
    }

    @RequestMapping("/update")
    public JSONMessage updateUser(@ModelAttribute UserExample param) {
        try {
            Integer userId = ReqUtil.getUserId();
            User data = getUserManager().updateUser(userId, param);
            if (getUserManager().isOpenMultipleDevices(userId))
                getUserManager().multipointLoginUpdateUserInfo(userId, SKBeanUtils.getUserManager().getUser(userId).getNickname(), null, null, 0);
            return JSONMessage.success(data);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
    }

    // 校验旧密码是否正确
    @RequestMapping("/checkPayPassword")
    public JSONMessage checkPayPassword(@RequestParam String payPassword) {
        User user = getUserManager().getUser(ReqUtil.getUserId());
        if (user.getPayPassword().equals(payPassword)) {
            return JSONMessage.success();
        } else {
            return JSONMessage.failure("旧密码输入有误");
        }
    }

    // 设置、修改支付密码
    @RequestMapping("/update/payPassword")
    public JSONMessage updateUserPayPassword(@RequestParam(defaultValue = "") String oldPayPassword, @RequestParam(defaultValue = "") String payPassword) {
        Integer userId = ReqUtil.getUserId();
        User user = getUserManager().getUser(userId);
        UserExample param = new UserExample();
        User updateUser = null;
        if (user.getPayPassword() != null && oldPayPassword != null) {
            if (!oldPayPassword.equals(payPassword)) {
                if (user.getPayPassword().equals(oldPayPassword)) {
                    param.setPayPassWord(payPassword);
                    updateUser = getUserManager().updateUser(userId, param);
                } else {
                    return JSONMessage.failure("旧密码输入错误");
                }
            } else {
                return JSONMessage.failure("新旧密码一致,请重新输入");
            }
        } else if (user.getPayPassword() == null) {
            param.setPayPassWord(payPassword);
            updateUser = getUserManager().updateUser(userId, param);
        }
        if (null != updateUser)
            getUserManager().multipointLoginDataSync(userId, updateUser.getNickname(), KConstants.MultipointLogin.SYNC_PAY_PASSWORD);
        return JSONMessage.success(updateUser);

    }

    /**
     * 忘记支付密码
     */
    @RequestMapping("/payPassword/reset")
    public JSONMessage resetPayPassword(@RequestParam(defaultValue = "86") String areaCode,
                                        @RequestParam(defaultValue = "") String telephone,
                                        @RequestParam(defaultValue = "") String randcode, @RequestParam(defaultValue = "") String newPassword) {
        try {
            telephone = areaCode + telephone;
            if (StringUtil.isEmpty(telephone) || (StringUtil.isEmpty(randcode)) || StringUtil.isEmpty(newPassword)) {
                return JSONMessage.failure("缺少必填参数");
            } else {
                if (!SKBeanUtils.getSMSService().isAvailable(telephone, randcode))
                    return JSONMessage.failure("短信验证码不正确!");

                Integer userId = ReqUtil.getUserId();

                UserExample param = new UserExample();
                param.setPayPassWord(newPassword);
                User updateUser = getUserManager().updateUser(userId, param);
                if (null != updateUser)
                    getUserManager().multipointLoginDataSync(userId, updateUser.getNickname(), KConstants.MultipointLogin.SYNC_PAY_PASSWORD);
                SKBeanUtils.getSMSService().deleteSMSCode(telephone);
            }
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
        return JSONMessage.success();
    }

    @RequestMapping("/changeMsgNum")
    public JSONMessage changeMsgNum(@RequestParam int num) {
        getUserManager().changeMsgNum(ReqUtil.getUserId(), num);
        return JSONMessage.success();
    }

    //销毁 已经过期的聊天记录
    @RequestMapping("/destroyMsgRecord")
    public JSONMessage destroyMsg(@RequestParam(defaultValue = "0") int userId) {
        getUserManager().destroyMsgRecord(ReqUtil.getUserId());
        return JSONMessage.success();
    }


    //设置消息免打扰
    @RequestMapping("/update/OfflineNoPushMsg")
    public JSONMessage updatemessagefree(@RequestParam int offlineNoPushMsg) {
        User data = getUserManager().updatemessagefree(offlineNoPushMsg);
        if (null != data)
            return JSONMessage.success(null, data);
        else
            return JSONMessage.failure("设置免打扰失败");
    }

    // 获取视频会议地址
    @RequestMapping("/openMeet")
    public JSONMessage openMeet(@RequestParam(defaultValue = "0") int toUserId,
                                @RequestParam(defaultValue = "CN") String area) {
        User user = getUserManager().get(toUserId);
        CenterConfig centerConfig = SKBeanUtils.getAdminManager().findCenterCofigByArea(area, user.getArea());
        String meetUrl = SKBeanUtils.getAdminManager().getClientConfig().getJitsiServer();

        ConfigVO configVo = new ConfigVO();
        if (centerConfig == null) {
            meetUrl = SKBeanUtils.getAdminManager().serverDistribution(area, configVo).getJitsiServer();
        } else {
            meetUrl = SKBeanUtils.getAdminManager().serverDistribution(centerConfig.getArea(), configVo).getJitsiServer();
        }

        Map<String, String> result = new HashMap<String, String>();
        result.put("meetUrl", meetUrl);
        return JSONMessage.success(result);
    }


    @RequestMapping("/channelId/set")
    public JSONMessage setChannelId(@RequestParam String deviceId, String channelId, @RequestParam(defaultValue = "") String appId) {
        if (StringUtil.isEmpty(channelId))
            return JSONMessage.success();
        String iosPushServer = SKBeanUtils.getAdminManager().getConfig().getIosPushServer();
        if (!KConstants.PUSHSERVER.BAIDU.equals(iosPushServer))
            return JSONMessage.success();
		/*String appStoreAppId = SKBeanUtils.getLocalSpringBeanManager().getPushConfig().getAppStoreAppId();
		if(!StringUtil.isEmpty(appId)&&!appId.equals(appStoreAppId))
			return JSONMessage.success();*/
        Integer userId = ReqUtil.getUserId();
        DeviceInfo info = new DeviceInfo();

        info.setPushServer(KConstants.PUSHSERVER.BAIDU);
        info.setPushToken(channelId);
        info.setDeviceKey(KConstants.DeviceKey.IOS);
        if ("2".equals(deviceId)) {
			/*Map<String, String> pushMap = info.getPushMap();
			if(null==pushMap) {
				pushMap=Maps.newLinkedHashMap();
				pushMap.put(KConstants.PUSHSERVER.BAIDU, deviceId);
			}
			info.setPushMap(pushMap);*/
            KSessionUtil.saveIosPushToken(userId, info);
        } else {
            info.setDeviceKey(KConstants.DeviceKey.Android);
            KSessionUtil.saveAndroidPushToken(userId, info);
        }
        getUserManager().savePushToken(userId, info);

        return JSONMessage.success();
    }

    /**
     * 极光推送设置regId
     *
     * @param deviceId
     * @param regId
     * @return
     */
    @RequestMapping("/jPush/setRegId")
    public JSONMessage setJPushRegId(@RequestParam(defaultValue = "") String deviceId, String regId) {
        if (StringUtil.isEmpty(regId))
            return JSONMessage.success();
        Integer userId = ReqUtil.getUserId();
        DeviceInfo info = new DeviceInfo();
        info.setDeviceKey(KConstants.DeviceKey.Android);
        info.setPushServer(KConstants.PUSHSERVER.JPUSH);
        info.setPushToken(regId);
        getUserManager().savePushToken(userId, info);
        KSessionUtil.saveAndroidPushToken(userId, info);
        return JSONMessage.success();
    }

    @RequestMapping("/jPush/setJPushIOSRegId")
    public JSONMessage setJPushIOSRegId(@RequestParam(defaultValue = "") String deviceId, String regId) {
        if (StringUtil.isEmpty(regId))
            return JSONMessage.success();
        Integer userId = ReqUtil.getUserId();
        DeviceInfo info = new DeviceInfo();
        info.setDeviceKey(KConstants.DeviceKey.IOS);
        info.setPushServer(KConstants.PUSHSERVER.JPUSH);
        info.setPushToken(regId);
        getUserManager().savePushToken(userId, info);
//		KSessionUtil.saveAndroidPushToken(userId, info);
        KSessionUtil.saveIosPushToken(userId, info);
        return JSONMessage.success();
    }


    /**
     * 小米推送设置regId
     *
     * @param deviceId
     * @param regId
     * @return
     */
    @RequestMapping("/xmpush/setRegId")
    public JSONMessage setRegId(@RequestParam(defaultValue = "") String deviceId, String regId) {
        if (StringUtil.isEmpty(regId))
            return JSONMessage.success();
        Integer userId = ReqUtil.getUserId();
        DeviceInfo info = new DeviceInfo();
        info.setDeviceKey(KConstants.DeviceKey.Android);
        info.setPushServer(KConstants.PUSHSERVER.XIAOMI);
        info.setPushToken(regId);
        getUserManager().savePushToken(userId, info);
        KSessionUtil.saveAndroidPushToken(userId, info);
        return JSONMessage.success();
    }

    /**
     * apns推送设置token
     *
     * @param deviceId
     * @param token
     * @param isVoip
     * @param appId
     * @return
     */
    @RequestMapping("/apns/setToken")
    public JSONMessage setApnsToken(@RequestParam(defaultValue = "") String deviceId, String token,
                                    @RequestParam(defaultValue = "1") int isVoip, @RequestParam(defaultValue = "") String appId) {
        if (StringUtil.isEmpty(token))
            return JSONMessage.failure("null Token");
        Integer userId = ReqUtil.getUserId();
        //String pushServer=KConstants.PUSHSERVER.APNS;
        if (0 == isVoip) {
            String iosPushServer = SKBeanUtils.getAdminManager().getConfig().getIosPushServer();
            if (!KConstants.PUSHSERVER.APNS.equals(iosPushServer)) {
                return JSONMessage.success();
            }
			/*String appStoreAppId = SKBeanUtils.getLocalSpringBeanManager().getPushConfig().getAppStoreAppId();
			if(!KConstants.PUSHSERVER.APNS.equals(iosPushServer)) {
				if(appStoreAppId.equals(appId))
					return JSONMessage.success();
			}*/

			/*String iosPushServer = SKBeanUtils.getAdminManager().getConfig().getIosPushServer();
			if(!KConstants.PUSHSERVER.APNS.equals(iosPushServer))
				return JSONMessage.success();*/
        } else {
            getUserManager().saveVoipPushToken(userId, token);
            return JSONMessage.success();
        }

        DeviceInfo info = new DeviceInfo();
        info.setPushServer(KConstants.PUSHSERVER.APNS);
        info.setDeviceKey(KConstants.DeviceKey.IOS);
        info.setPushToken(token);
        info.setAppId(appId);
        DeviceInfo iosPushToken = KSessionUtil.getIosPushToken(userId);
        if (null != iosPushToken)
            info.setVoipToken(iosPushToken.getVoipToken());
        KSessionUtil.saveIosPushToken(userId, info);
        getUserManager().savePushToken(userId, info);
        return JSONMessage.success();
    }

    /**
     * 华为推送设置token
     *
     * @param deviceId
     * @param token
     * @param adress
     * @return
     */
    @RequestMapping("/hwpush/setToken")
    public JSONMessage setHWToken(@RequestParam(defaultValue = "") String deviceId, String token, String adress) {
        if (StringUtil.isEmpty(token))
            return JSONMessage.failure("null Token");
        DeviceInfo info = new DeviceInfo();
        Integer userId = ReqUtil.getUserId();
        info.setDeviceKey(KConstants.DeviceKey.Android);
        info.setPushServer(KConstants.PUSHSERVER.HUAWEI);
        info.setPushToken(token);
        info.setAdress(adress);
        getUserManager().savePushToken(userId, info);
        KSessionUtil.saveAndroidPushToken(userId, info);
        return JSONMessage.success();
    }

    /**
     * google推送设置token
     *
     * @param token
     * @return
     */
    @RequestMapping("/fcmPush/setToken")
    public JSONMessage setFCMToken(@RequestParam(defaultValue = "") String token) {
        if (StringUtil.isEmpty(token))
            return JSONMessage.failure("null Token");
        DeviceInfo info = new DeviceInfo();
        Integer userId = ReqUtil.getUserId();
        info.setDeviceKey(KConstants.DeviceKey.Android);
        info.setPushServer(KConstants.PUSHSERVER.FCM);
        info.setPushToken(token);
        getUserManager().savePushToken(userId, info);
        KSessionUtil.saveAndroidPushToken(userId, info);

        return JSONMessage.success();

    }

    /**
     * 魅族推送设置pushId
     *
     * @param pushId
     * @return
     */
    @RequestMapping("/MZPush/setPushId")
    public JSONMessage setMZPushId(@RequestParam(defaultValue = "") String pushId) {
        if (StringUtil.isEmpty(pushId))
            return JSONMessage.failure("null pushId");
        DeviceInfo info = new DeviceInfo();
        Integer userId = ReqUtil.getUserId();
        info.setDeviceKey(KConstants.DeviceKey.Android);
        info.setPushServer(KConstants.PUSHSERVER.MEIZU);
        info.setPushToken(pushId);
        getUserManager().savePushToken(userId, info);
        KSessionUtil.saveAndroidPushToken(userId, info);

        return JSONMessage.success();
    }

    @RequestMapping("/VIVOPush/setPushId")
    public JSONMessage setVIVOPushId(@RequestParam(defaultValue = "") String pushId) {
        if (StringUtil.isEmpty(pushId))
            return JSONMessage.failure("null pushId");
        DeviceInfo info = new DeviceInfo();
        Integer userId = ReqUtil.getUserId();
        info.setDeviceKey(KConstants.DeviceKey.Android);
        info.setPushServer(KConstants.PUSHSERVER.VIVO);
        info.setPushToken(pushId);
        getUserManager().savePushToken(userId, info);
        KSessionUtil.saveAndroidPushToken(userId, info);

        return JSONMessage.success();
    }

    @RequestMapping("/OPPOPush/setPushId")
    public JSONMessage setOPPOPushId(@RequestParam(defaultValue = "") String pushId) {
        if (StringUtil.isEmpty(pushId))
            return JSONMessage.failure("null pushId");
        DeviceInfo info = new DeviceInfo();
        Integer userId = ReqUtil.getUserId();
        info.setDeviceKey(KConstants.DeviceKey.Android);
        info.setPushServer(KConstants.PUSHSERVER.OPPO);
        info.setPushToken(pushId);
        getUserManager().savePushToken(userId, info);
        KSessionUtil.saveAndroidPushToken(userId, info);

        return JSONMessage.success();
    }

    @RequestMapping(value = "/get")
    public JSONMessage getUser(@RequestParam(defaultValue = "") String userId) {
        try {
            int loginedUserId = ReqUtil.getUserId();
            int toUserId = 0;
            User user = null;
            try {
                toUserId = Integer.valueOf(userId);
            } catch (Exception e) {
                user = getUserManager().getUserByAccount(userId);
            }
            if (null == user) {
                toUserId = 0 == toUserId ? loginedUserId : toUserId;
                try {
                    user = getUserManager().getUser(loginedUserId, toUserId);
                } catch (ServiceException e) {
                    user = getUserManager().getUserByAccount(userId);
                }
            }
            if (null == user)
                user = getUserManager().getUserByAccount(userId);

            user.setOnlinestate(SKBeanUtils.getRedisService().queryUserOnline(toUserId));
            if (loginedUserId != user.getUserId()) {
                user.buildNoSelfUserVo();
            } else {
                //查找出该用户的推广型邀请码(一码多用)
                InviteCode myInviteCode = SKBeanUtils.getAdminManager().findUserPopulInviteCode(user.getUserId());
                user.setMyInviteCode((myInviteCode == null ? "" : myInviteCode.getInviteCode()));
            }
            user.setBalance(null);
            //user.setPhone(null);
            // 客户端请求不将用户支付密码返回,只返回用户是否设置了密码
            if (StringUtil.isEmpty(user.getPayPassword())) {
                user.setPayPassword("0");// 用户未设置密码
            } else {
                user.setPayPassword("1");// 用户已设置密码
            }

            //是否实名
            List<BankCard> bankCardList = SKBeanUtils.getBankCardManager().getBankCardList(user.getUserId());
            if(bankCardList==null||bankCardList.size()==0){
                user.setFourElements(0);
            }else {
                user.setFourElements(1);
            }

            return JSONMessage.success(user);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    @RequestMapping(value = "/getPrivateKey")
    public JSONMessage getPrivateKey() {
        try {
            Object privateKey = getUserManager().queryOneFieldById("privateKey", ReqUtil.getUserId());
            if (null == privateKey)
                return JSONMessage.success();
            JSONObject resultObj = new JSONObject();
            resultObj.put("privateKey", privateKey);
            return JSONMessage.success(resultObj);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    @RequestMapping(value = "/getByAccount")
    public JSONMessage getUserByAccount(@RequestParam(defaultValue = "") String account) {
        try {
            if (StringUtil.isEmpty(account)) {
                return JSONMessage.failure("缺少通讯号!");
            }
            int loginedUserId = ReqUtil.getUserId();

            User user = getUserManager().getUserByAccount(account);
            try {
                if (null == user)
                    user = getUserManager().getUser(loginedUserId, Integer.valueOf(account));
            } catch (Exception e) {
                return JSONMessage.failure("通讯号错误!");
            }

            //user.setOnlinestate(SKBeanUtils.getRedisService().queryUserOnline(user.getUserId()));
            if (loginedUserId != user.getUserId()) {
                user.buildNoSelfUserVo();
                user.setShowLastLoginTime(0);
                user.setBalance(null);
                user.setPhone(null);
                user.setTelephone(null);
            } else {
                //查找出该用户的推广型邀请码(一码多用)
                InviteCode myInviteCode = SKBeanUtils.getAdminManager().findUserPopulInviteCode(user.getUserId());
                user.setMyInviteCode((myInviteCode == null ? "" : myInviteCode.getInviteCode()));
            }


            // 客户端请求不将用户支付密码返回,只返回用户是否设置了密码
            /*
             * if(StringUtil.isEmpty(user.getPayPassword())){ user.setPayPassword("0");//
             * 用户未设置密码 }else{ user.setPayPassword("1");// 用户已设置密码 }
             */

            return JSONMessage.success(user);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

    }


    @RequestMapping(value = "/query")
    public JSONMessage queryUser(@ModelAttribute UserQueryExample param) {
        Object data = getUserManager().query(param);
        return JSONMessage.success(null, data);
    }


    @RequestMapping("/password/reset")
    public JSONMessage resetPassword(@RequestParam(defaultValue = "86") String areaCode,
                                     @RequestParam(defaultValue = "") String telephone,
                                     @RequestParam(defaultValue = "") String randcode, @RequestParam(defaultValue = "") String newPassword) {
        try {
            telephone = areaCode + telephone;
            if (StringUtil.isEmpty(telephone) || (StringUtil.isEmpty(randcode)) || StringUtil.isEmpty(newPassword)) {
                return JSONMessage.failure("缺少必填参数");
            } else {
                if (!SKBeanUtils.getSMSService().isAvailable(telephone, randcode))
                    return JSONMessage.failure("短信验证码不正确!");
                getUserManager().resetPassword(telephone, newPassword);
                Integer userId = ReqUtil.getUserId();
                KSessionUtil.deleteUserByUserId(userId);
                SKBeanUtils.getSMSService().deleteSMSCode(telephone);
            }
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
        return JSONMessage.success();
    }


    @RequestMapping("/password/update")
    public JSONMessage updatePassword(@RequestParam("oldPassword") String oldPassword,
                                      @RequestParam("newPassword") String newPassword) {
        JSONMessage jMessage;

        if (StringUtil.isEmpty(oldPassword) || StringUtil.isEmpty(newPassword)) {
            jMessage = KConstants.Result.ParamsAuthFail;
        } else {
            Integer userId = ReqUtil.getUserId();
            getUserManager().updatePassword(userId, oldPassword, newPassword);
            KSessionUtil.deleteUserByUserId(userId);
            jMessage = JSONMessage.success();
        }
        return jMessage;
    }


    @RequestMapping(value = "/settings")
    public JSONMessage getSettings(@RequestParam int userId) {
        Object data = getUserManager().getSettings(0 == userId ? ReqUtil.getUserId() : userId);
        return JSONMessage.success(null, data);
    }

    @RequestMapping(value = "/settings/update")
    public JSONMessage updateSettings(@ModelAttribute UserSettingVO userSettings) {
        Integer userId = ReqUtil.getUserId();
        String recordTime = userSettings.getChatRecordTimeOut().replace("天", "");
        userSettings.setChatRecordTimeOut(recordTime);
        Object data = getUserManager().updateSettings(userId, userSettings);
        KSessionUtil.deleteUserByUserId(userId);
        return JSONMessage.success(null, data);
    }

    @RequestMapping(value = "/sendMsg")
    public JSONMessage sendMessage(@RequestParam(defaultValue = "") String jid,
                                   @RequestParam(defaultValue = "1") int chatType, @RequestParam(defaultValue = "2") int type
            , @RequestParam(defaultValue = "") String content, @RequestParam(defaultValue = "") String fileName) {
        if (StringUtil.isEmpty(jid) || StringUtil.isEmpty(content)) {
            return JSONMessage.success();
        }
        getUserManager().sendMessage(jid, chatType, type, content, fileName);

        return JSONMessage.success();
    }


    @RequestMapping(value = "/bind/wxcode")
    public JSONMessage bindWxopenid(@RequestParam(defaultValue = "") String code) {
        if (StringUtil.isEmpty(code)) {
            return JSONMessage.failure("缺少code");
        }
        int userId = ReqUtil.getUserId();
        Object reuslt = getUserManager().bindWxopenid(userId, code);
        if (null == reuslt) {
            return JSONMessage.failure("获取openid 失败");
        }
        return JSONMessage.success(reuslt);
    }

    /**
     * 获取支付宝授权authInfo
     *
     * @return
     */
    @RequestMapping(value = "/bind/getAliPayAuthInfo")
    public JSONMessage bindAliCode() {
        String content = "apiname=com.alipay.account.auth&app_id=" + AliPayUtil.APP_ID + "&app_name=mc&auth_type=AUTHACCOUNT&biz_type=openservice&method=alipay.open.auth.sdk.code.get&pid=" + AliPayUtil.PID + "&product_id=APP_FAST_LOGIN&scope=kuaijie&target_id=" + System.currentTimeMillis() + "&sign_type=RSA2";
        String sign;
        Map<String, String> map = Maps.newLinkedHashMap();
        int userId = ReqUtil.getUserId();
        User user = SKBeanUtils.getUserManager().get(userId);
        try {
            sign = AlipaySignature.rsaSign(content, AliPayUtil.APP_PRIVATE_KEY, AliPayUtil.CHARSET, "RSA2");
            String enCodesign = URLEncoder.encode(sign, "UTF-8");
            String authInfo = content + "&sign=" + enCodesign;
            if (!StringUtil.isEmpty(user.getAliUserId())) {
                map.put("aliUserId", user.getAliUserId());
            }
            map.put("authInfo", authInfo);
            return JSONMessage.success(null, map);
        } catch (Exception e) {
            e.printStackTrace();
            return JSONMessage.failure("获取支付宝授权authInfo失败");
        }
    }

    /**
     * 保存支付宝用户Id
     *
     * @param aliUserId
     * @return
     */
    @RequestMapping(value = "/bind/aliPayUserId")
    public JSONMessage bindAliPayUserId(@RequestParam(defaultValue = "") String aliUserId) {
        if (StringUtil.isEmpty(aliUserId)) {
            return JSONMessage.failure("缺少code");
        }
        int userId = ReqUtil.getUserId();
        SKBeanUtils.getUserManager().bindAliUserId(userId, aliUserId);
        return JSONMessage.success();
    }

    /**
     * 充值
     *
     * @param payType
     * @param price
     * @param time
     * @param secret
     * @return
     */
    @RequestMapping(value = "/recharge/getSign")
    public JSONMessage getSign(HttpServletRequest request, @RequestParam int payType, @RequestParam String price,
                               @RequestParam(defaultValue = "0") long time,
                               @RequestParam(defaultValue = "") String secret) {
        String token = getAccess_token();
        Integer userId = ReqUtil.getUserId();
        //充值接口授权
        if (!AuthServiceUtils.authRedPacket(userId + "", token, time, secret)) {
            return JSONMessage.failure("权限验证失败!");
        }
        Map<String, String> map = Maps.newLinkedHashMap();
        String orderInfo = "";
        if (0 < payType) {
            String orderNo = StringUtil.getOutTradeNo();
            ConsumeRecord entity = new ConsumeRecord();
            entity.setMoney(new Double(price));
            if (10 < entity.getMoney()) {
                return JSONMessage.failure("单次充值  最多 10 元");
            }
            entity.setUserId(ReqUtil.getUserId());
            entity.setTime(DateUtil.currentTimeSeconds());
            entity.setType(KConstants.ConsumeType.USER_RECHARGE);
            entity.setDesc("余额充值");
            entity.setStatus(KConstants.OrderStatus.CREATE);
            entity.setTradeNo(orderNo);
            entity.setPayType(payType);

            if (KConstants.PayType.ALIPAY == payType) {
                orderInfo = AliPayUtil.getOrderInfo("余额充值", "余额充值", price, orderNo);
                SKBeanUtils.getConsumeRecordManager().saveConsumeRecord(entity);
                map.put("orderInfo", orderInfo);
                System.out.println("orderInfo>>>>>" + orderInfo);
                return JSONMessage.success(null, map);
            } else {
                WxPayDto tpWxPay = new WxPayDto();
                //tpWxPay.setOpenId(openId);
                tpWxPay.setBody("余额充值");
                tpWxPay.setOrderId(orderNo);
                tpWxPay.setSpbillCreateIp(request.getRemoteAddr());
                tpWxPay.setTotalFee(price);
                SKBeanUtils.getConsumeRecordManager().saveConsumeRecord(entity);
                Object data = WXPayUtil.getPackage(tpWxPay);
                return JSONMessage.success(null, data);
            }
        }
        return JSONMessage.failure("没有选择支付类型");
    }

    @RequestMapping(value = "/getUserMoeny")
    public JSONMessage getUserMoeny() throws Exception {
        Integer userId = ReqUtil.getUserId();
        Map<String, Object> data = Maps.newHashMap();
        Double balance = getUserManager().getUserMoeny(userId);
        if (null == balance)
            balance = 0.0;
        data.put("balance", balance);
        return JSONMessage.success(null, data);

    }


    @RequestMapping(value = "/getOnLine")
    public JSONMessage getOnlinestateByUserId(Integer userId) {
        userId = null != userId ? userId : ReqUtil.getUserId();
        Object data = getUserManager().getOnlinestateByUserId(userId);
        return JSONMessage.success(null, data);
    }

    /**
     * @param @param  param
     * @param @return 参数
     * @Description: TODO(用户举报)
     */
    @RequestMapping("/report")
    public JSONMessage report(@RequestParam(defaultValue = "0") Integer toUserId, @RequestParam(defaultValue = "") String roomId,
                              @RequestParam(defaultValue = "") String webUrl, @RequestParam(defaultValue = "0") int reason) {
        getUserManager().report(ReqUtil.getUserId(), toUserId, reason, roomId, webUrl);
        return JSONMessage.success();
    }

    @RequestMapping("/checkReportUrl")
    public JSONMessage checkReportUrl(@RequestParam(defaultValue = "") String webUrl, HttpServletResponse response) throws IOException {
        boolean flag;
        if (StringUtil.isEmpty(webUrl)) {
            return JSONMessage.failure("webUrl is null");
        } else {
            try {
                // urlEncode 解码
                webUrl = URLDecoder.decode(webUrl);
                flag = getUserManager().checkReportUrlImpl(webUrl);
                return JSONMessage.success(flag);
            } catch (ServiceException e) {
//				response.sendRedirect("/pages/report/prohibit.html");
                return JSONMessage.failure(e.getMessage());
            }
        }

    }

    //添加收藏
    @RequestMapping("/emoji/add")
    public JSONMessage addEmoji(@RequestParam(defaultValue = "") String emoji, @RequestParam(defaultValue = "") String url, @RequestParam(defaultValue = "") String roomJid, @RequestParam(defaultValue = "") String msgId, @RequestParam(defaultValue = "") String type) {
        try {
            if (!StringUtil.isEmpty(emoji)) {
                Emoji newEmoji = getUserManager().addNewEmoji(emoji);
                return JSONMessage.success(newEmoji);
            } else {
                Object data = null;
                if (!StringUtil.isEmpty(msgId)) {
                    data = getUserManager().addCollection(ReqUtil.getUserId(), roomJid, msgId, type);
                } else {
                    data = getUserManager().addEmoji(ReqUtil.getUserId(), url, type);
                }
                return JSONMessage.success(null, data);
            }

        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }

    }

    //收藏表情列表
    @RequestMapping("/emoji/list")
    public JSONMessage EmojiList(@RequestParam Integer userId, @RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
        Object data = getUserManager().emojiList(userId);
        return JSONMessage.success(null, data);
    }

    //收藏列表
    @RequestMapping("/collection/list")
    public JSONMessage collectionList(@RequestParam Integer userId, @RequestParam(defaultValue = "0") int type, @RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
        Object data = getUserManager().emojiList(userId, type, pageSize, pageIndex);
        return JSONMessage.success(null, data);
    }

    //取消收藏
    @RequestMapping("/emoji/delete")
    public JSONMessage deleteEmoji(@RequestParam String emojiId) {
        getUserManager().deleteEmoji(ReqUtil.getUserId(), emojiId);
        return JSONMessage.success();
    }

    //添加消息录制
    @RequestMapping("/course/add")
    public JSONMessage addMessagecourse(@RequestParam Integer userId, @RequestParam String messageIds
            , @RequestParam long createTime, @RequestParam String courseName, @RequestParam(defaultValue = "0") String roomJid) {

        List<String> list = Arrays.asList(messageIds.split(","));
        getUserManager().addMessageCourse(userId, list, createTime, courseName, roomJid);
        return JSONMessage.success();
    }

    //查询课程
    @RequestMapping("/course/list")
    public JSONMessage getCourseList(@RequestParam Integer userId) {
        Object data = getUserManager().getCourseList(userId);
        return JSONMessage.success(null, data);
    }

    //修改课程
    @RequestMapping("/course/update")
    public JSONMessage updateCourse(@ModelAttribute Course course, @RequestParam(defaultValue = "") String courseMessageId) {
        try {
            getUserManager().updateCourse(course, courseMessageId);
        } catch (Exception e) {
            return JSONMessage.failure(e.getMessage());
        }
        return JSONMessage.success();
    }

    //删除课程
    @RequestMapping("/course/delete")
    public JSONMessage deleteCourse(@RequestParam ObjectId courseId) {

        boolean ok = getUserManager().deleteCourse(ReqUtil.getUserId(), courseId);
        if (!ok)
            return JSONMessage.failure(ResultMsgs.DATA_NOT_EXIST);
        return JSONMessage.success();
    }

    //获取详情
    @RequestMapping("/course/get")
    public JSONMessage getCourse(@RequestParam String courseId) {
        Object data = getUserManager().getCourse(courseId);
        return JSONMessage.success(null, data);
    }

    //屏蔽某人的朋友圈
    @RequestMapping("/filterUserCircle")
    public JSONMessage filterUserCircle(@RequestParam(defaultValue = "0") Integer type, @RequestParam(defaultValue = "0") Integer toUserId) {
        if (1 == type) {
            getUserManager().filterCircleUser(toUserId);
        } else if (-1 == type) {
            getUserManager().cancelFilterCircleUser(toUserId);
        }
        return JSONMessage.success();
    }


    //获取微信用户的openid
    @RequestMapping("/wxUserOpenId")
    public void getOpenId(HttpServletResponse res, HttpServletRequest request, String code) throws ClientProtocolException, IOException, ServletException {

        String openid = "";
        String token = "";
        openid = getUserManager().getWxOpenId(code).getString("openid");

//		String tokenurl="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
//		tokenurl=tokenurl.replace("APPID", "wxd3f39f42d3e92536").replace("APPSECRET", "3f15b6b7b7f79e310eaa68893387c2a2");
//		HttpGet httpget=HttpClientConnectionManager.getGetMethod(tokenurl);
//		CloseableHttpClient  httpclient1=HttpClients.createDefault();
//		HttpResponse response1 = httpclient1.execute(httpget);
//		String jsonStr1=EntityUtils.toString(response1.getEntity(),"utf-8");
//		System.out.println("jsonStr1:====>"+jsonStr1);
//		JSONObject jsonTexts1=(JSONObject) JSON.parse(jsonStr1);
//		if(jsonTexts1.get("access_token")!=null){
//			token=jsonTexts1.getString("access_token").toString();
//		}

        token = getUserManager().getWxToken();

        System.out.println("openId:======>" + openid);
        System.out.println("access_token" + token);
        request.getSession().setAttribute("openid", openid);
        request.getSession().setAttribute("token", token);
        request.getRequestDispatcher("/user/getUserInfo").forward(request, res);
    }

    @RequestMapping("/getWxUser")
    @ResponseBody
    public WxUser getWxUser(String openid) {
        return getUserManager().getWxUser(openid, null);
    }

    @RequestMapping("/getWxUserbyId")
    @ResponseBody
    public WxUser getWxUser(Integer userId) {
        return getUserManager().getWxUser(null, userId);
    }

    //获取微信用户的详细信息
    @RequestMapping("/getUserInfo")
    public void getUserInfo(HttpServletRequest request, HttpServletResponse response) throws ClientProtocolException, IOException, ServletException {
        String openid = request.getSession().getAttribute("openid").toString();
        if (StringUtil.isEmpty(openid)) {
            response.sendError(417, "openId 获取 错误");
            return;
        }
        String token = request.getSession().getAttribute("token").toString();
        WxUser wxUser = getUserManager().getWxUser(openid, null);

        if (wxUser != null) {
            response.sendRedirect(SKBeanUtils.getLocalSpringBeanManager().getApplicationConfig().getAppConfig().getWxChatUrl() + "?openid=" + openid);
        } else {

            JSONObject jsonObject = WXUserUtils.getWxUserInfo(token, openid);
            if (jsonObject != null) {
//				WxUser wxUser1=getUserManager().addwxUser(jsonObject);
                response.sendRedirect(SKBeanUtils.getLocalSpringBeanManager().getApplicationConfig().getAppConfig().getWxChatUrl() + "?openid=" + openid);
            }
        }


    }

    /**
     * 获取开始时间和结束时间
     *
     * @param request
     * @return
     */
    public Map<String, Long> getTimes(Integer sign) {
        Long startTime = null;
        Long endTime = DateUtil.currentTimeSeconds();
        Map<String, Long> map = Maps.newLinkedHashMap();

        if (sign == -3) {//最近一个月
            startTime = endTime - (KConstants.Expire.DAY1 * 30);
        } else if (sign == -2) {//最近7天
            startTime = endTime - (KConstants.Expire.DAY1 * 7);
        } else if (sign == -1) {//最近48小时
            startTime = endTime - (KConstants.Expire.DAY1 * 2);
        }
        // 表示今天
        else if (sign == 0) {
            startTime = DateUtil.getTodayMorning().getTime() / 1000;
        } else if (sign == 3) {
            startTime = DateUtil.strYYMMDDToDate("2000-01-01").getTime() / 1000;
        }

        map.put("startTime", startTime);
        map.put("endTime", endTime);
        return map;
    }

    /**
     * @param offlineTime
     * @return
     * @Description:获取最新的好友群组相关操作记录
     **/
    @RequestMapping("/offlineOperation")
    public JSONMessage offineOperation(@RequestParam(defaultValue = "0") String offlineTime) {
        try {
            Long startTime = new Double(Double.parseDouble(offlineTime)).longValue();
            List<OfflineOperation> offlineOperation = getUserManager().getOfflineOperation(ReqUtil.getUserId(), startTime);
            return JSONMessage.success(offlineOperation);
        } catch (ServiceException e) {
            return JSONMessage.failure(e.getMessage());
        }
    }
}
