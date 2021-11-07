/**
 * 系统配置页面相关的js
 */
var obj;
function num(){
	obj = $(".giftRatio").val();
	var reg = new RegExp("^[0-9]+(.[0-9]{0,2})?$", "g");
	(!reg.test(obj)?layer.alert("直播礼物分成比率请保持小数点后两位有效数字"):"");
}

$(function () {
    //非超级管理员登录屏蔽操作按钮
    if(localStorage.getItem("role")==1 || localStorage.getItem("role")==5){
        $(".save").remove();
    }
})

//填充数据方法
function fillParameter(data){
    //判断字段数据是否存在
    function nullData(data){
        if(data == '' || data == "undefined" || data==null){
            return "";
        }else{
            return data;
        }
    }


    //数据回显
    $(".XMPPHost").val(nullData(data.XMPPHost));  
    $(".XMPPDomain").val(nullData(data.XMPPDomain));
    $(".XMPPTimeout").val(nullData(data.XMPPTimeout));      
    $(".apiUrl").val(nullData(data.apiUrl));        
    $(".downloadAvatarUrl").val(nullData(data.downloadAvatarUrl));    
    $(".downloadUrl").val(nullData(data.downloadUrl));        
    $(".uploadUrl").val(nullData(data.uploadUrl));

    $(".jitsiServer").val(nullData(data.jitsiServer));    
    $(".liveUrl").val(nullData(data.liveUrl));
    // 直播分成比率
    $(".giftRatio").val(nullData(data.giftRatio));
    $(".promotionUrl").val(nullData(data.promotionUrl));
    $(".defaultTelephones").val(nullData(data.defaultTelephones));
    $(".privacyPolicyPrefix").val(nullData(data.privacyPolicyPrefix));

      // $(".displayRedPacket").val(data.displayRedPacket);
  
    $(".fileValidTime").val(nullData(data.fileValidTime));
    
    $(".chatRecordTimeOut").val(data.chatRecordTimeOut);
    $(".telephoneSearchUser").val(data.telephoneSearchUser);
    $(".telephoneLogin").val(data.isTelephoneLogin);
    
    $(".isOpenReceipt").val(data.isOpenReceipt);//是否开启消息回执
    $(".isOpenOnlineStatus").val(data.isOpenOnlineStatus);//是否开启客户端在线状态

    $(".isOpenSMSCode").val(data.isOpenSMSCode);//是否开启短信验证码

    $(".userIdLogin").val(data.isUserIdLogin);
    
    $(".regeditPhoneOrName").val(data.regeditPhoneOrName); //使用手机号或者用户名注册

    $(".registerInviteCode").val(data.registerInviteCode);  //注册邀请码
 
    $(".nicknameSearchUser").val(data.nicknameSearchUser);

    // $(".showContactsUser").val(data.showContactsUser);

    $(".isKeyWord").val(data.isKeyWord);// 关键词
    $(".isSaveMsg").val(data.isSaveMsg);// 保存单聊消息
    $(".isSaveMucMsg").val(data.isSaveMucMsg);// 保存群聊消息
    $(".isMsgSendTime").val(data.isMsgSendTime);// 强制同步消息发送时间
    $(".roamingTime").val(data.roamingTime);// 默认漫游时长
    $(".outTimeDestroy").val(data.outTimeDestroy);// 默认过期销毁时长
    $(".language").val(data.language);// 客户端默认语种
    $(".authApi").val(data.isAuthApi);
    $(".isFriendsVerify").val(data.isFriendsVerify);// 是否需要好友验证
    $(".isEncrypt").val(data.isEncrypt);// 是否加密传输
    $(".isMultiLogin").val(data.isMultiLogin);// 是否支持多点登录
    $(".isVibration").val(data.isVibration);// 是否振动
    $(".isTyping").val(data.isTyping); // 让对方知道我正在输入
    $(".isUseGoogleMap").val(data.isUseGoogleMap);// 使用Google地图
    $(".phoneSearch").val(data.phoneSearch);// 使用Google地图
    $(".nameSearch").val(data.nameSearch);// 使用Google地图
    $(".isKeepalive").val(data.isKeepalive);// 使用Google地图
    $(".showLastLoginTime").val(data.showLastLoginTime);// 使用Google地图
    $(".showTelephone").val(data.showTelephone);// 使用Google地图
    /** 建立群组默认参数设置 **/
    $(".maxUserSize").val(data.maxUserSize);
    $(".isLook").val(data.isLook);
    $(".isAttritionNotice").val(data.isAttritionNotice);// 群组减员发送通知
    $(".showRead").val(data.showRead);
    $(".isNeedVerify").val(data.isNeedVerify);
    $(".showMember").val(data.showMember);
    $(".allowSendCard").val(data.allowSendCard);
    $(".allowInviteFriend").val(data.allowInviteFriend);
    $(".allowUploadFile").val(data.allowUploadFile);
    $(".allowConference").val(data.allowConference);
    $(".allowSpeakCourse").val(data.allowSpeakCourse);


    $(".SMSType").val(data.sMSType);// 短信服务支持
    // $(".iosPushServer").val(data.iosPushServer);// IOS推送平台
    $(".imgVerificationCode").val(data.imgVerificationCode);// 短信服务支持
    $(".isAutoAddressBook").val(data.isAutoAddressBook);// 通讯录自动添加好友
    $(".isSaveRequestLogs").val(data.isSaveRequestLogs);// 是否保存接口请求日志
    $(".isOpenCluster").val(data.isOpenCluster);
    $(".isOpenVoip").val(data.isOpenVoip);// 是否开启voip推送
    $(".isOpenGoogleFCM").val(data.isOpenGoogleFCM);// 是否开启Google推送
    //重新渲染
    layui.form.render();
    // layui.form.render('checkbox','appCheckbox');
}

layui.use(['form','jquery',"layer"],function() {
    var form = layui.form,
        $ = layui.jquery,
        layer = parent.layer === undefined ? layui.layer : top.layer;

        
    //获取当前系统配置
    if(window.sessionStorage.getItem("systemConfig")){
        var systemConfig = JSON.parse(window.sessionStorage.getItem("systemConfig"));
        fillParameter(systemConfig);
    }else{
        /*$.ajax({
            url : "../json/systemParameter.json",
            type : "get",
            dataType : "json",
            success : function(data){
                fillParameter(data);
            }
        })*/

        Common.invoke({
            url :request('/console/config'),
            data : {},
            successMsg : false,
            errorMsg : "获取数据失败,请检查网络",
            success : function(result) {
                fillParameter(result.data);
            },
            error : function(result) {
            }

        });
    }
    //非管理员登录屏蔽操作按钮
    if(localStorage.getItem("IS_ADMIN")==0){
      $(".save").remove();
    }

    //提交保存配置
    form.on("submit(systemConfig)",function(data){
       
       var systemConfig = {
           
       };

        systemConfig.id = 10000;
        systemConfig.distance = 0;
        systemConfig.XMPPHost = $(".XMPPHost").val();  
        systemConfig.XMPPDomain = $(".XMPPDomain").val();  
        systemConfig.XMPPTimeout = $(".XMPPTimeout").val();    
        systemConfig.apiUrl = $(".apiUrl").val();        
        systemConfig.downloadAvatarUrl = $(".downloadAvatarUrl").val();    
        systemConfig.downloadUrl = $(".downloadUrl").val();        
        systemConfig.uploadUrl = $(".uploadUrl").val();  

        systemConfig.jitsiServer = $(".jitsiServer").val();    
        systemConfig.liveUrl = $(".liveUrl").val();
        //直播分成比例
       systemConfig.giftRatio = $(".giftRatio").val();
        if($(".giftRatio").val() == ""){
        	layer.alert("直播礼物分成比率不能为空");
        	return;
        }else if(obj > 1){
        	layer.alert("直播礼物分成比率要小于等于1");
        	return;
        }else if(obj == 0){
        	layer.alert("直播礼物分成比率不能为0");
        	return;
        }
        systemConfig.promotionUrl = $(".promotionUrl").val();
        
        systemConfig.defaultTelephones = $(".defaultTelephones").val();

        systemConfig.privacyPolicyPrefix = $(".privacyPolicyPrefix").val();

        systemConfig.fileValidTime = $(".fileValidTime").val();
        
        systemConfig.chatRecordTimeOut = $(".chatRecordTimeOut").val();

        systemConfig.telephoneSearchUser = $(".telephoneSearchUser").val();

        systemConfig.nicknameSearchUser = $(".nicknameSearchUser").val();

        systemConfig.isTelephoneLogin = $(".telephoneLogin").val();

        systemConfig.isUserIdLogin = $(".userIdLogin").val();
        
        systemConfig.regeditPhoneOrName = $(".regeditPhoneOrName").val();

        systemConfig.registerInviteCode = $(".registerInviteCode").val();
        
        systemConfig.isAuthApi = $(".authApi").val();

        systemConfig.isOpenCluster = $(".isOpenCluster").val();

        systemConfig.isOpenVoip = $(".isOpenVoip").val();

        systemConfig.isOpenGoogleFCM = $(".isOpenGoogleFCM").val();

        if($(".androidVersion").val()=="" || $(".androidVersion").val() == null){
            systemConfig.androidVersion = 0;
        }else {
            systemConfig.androidVersion = $(".androidVersion").val();
        }
        
        systemConfig.androidAppUrl = $(".androidAppUrl").val();
        systemConfig.androidExplain = $(".androidExplain").val();

        if($(".iosVersion").val()=="" || $(".iosVersion").val() == null){
            systemConfig.iosVersion = 0;
        }else {
            systemConfig.iosVersion = $(".iosVersion").val();
        }
        systemConfig.iosAppUrl = $(".iosAppUrl").val();
        systemConfig.iosExplain = $(".iosExplain").val();
        systemConfig.isKeyWord = $(".isKeyWord").val();
        systemConfig.isSaveMsg = $(".isSaveMsg").val();
        systemConfig.isSaveMucMsg = $(".isSaveMucMsg").val();
        systemConfig.isMsgSendTime = $(".isMsgSendTime").val();
        systemConfig.roamingTime = $(".roamingTime").val();
        systemConfig.outTimeDestroy = $(".outTimeDestroy").val();
        systemConfig.language = $(".language").val();
        systemConfig.isFriendsVerify = $(".isFriendsVerify").val();
        systemConfig.isEncrypt = $(".isEncrypt").val();
        systemConfig.isMultiLogin = $(".isMultiLogin").val();
        systemConfig.isVibration = $(".isVibration").val();
        systemConfig.isTyping = $(".isTyping").val();
        systemConfig.isUseGoogleMap = $(".isUseGoogleMap").val();
        systemConfig.phoneSearch = $(".phoneSearch").val();
        systemConfig.nameSearch = $(".nameSearch").val();
        systemConfig.isKeepalive = $(".isKeepalive").val();
        systemConfig.showLastLoginTime = $(".showLastLoginTime").val();
        systemConfig.showTelephone = $(".showTelephone").val();

        /** 建立群组默认参数设置 **/
        systemConfig.maxUserSize = $(".maxUserSize").val();
        systemConfig.isLook = $(".isLook").val();
        systemConfig.showRead = $(".showRead").val();
        systemConfig.isNeedVerify = $(".isNeedVerify").val();
        systemConfig.isAttritionNotice = $(".isAttritionNotice").val();
        systemConfig.showMember = $(".showMember").val();
        systemConfig.allowSendCard = $(".allowSendCard").val();
        systemConfig.allowInviteFriend = $(".allowInviteFriend").val();
        systemConfig.allowUploadFile = $(".allowUploadFile").val();
        systemConfig.allowConference = $(".allowConference").val();
        systemConfig.allowSpeakCourse = $(".allowSpeakCourse").val();

        systemConfig.isOpenSMSCode = $(".isOpenSMSCode").val();
        systemConfig.isOpenReceipt = $(".isOpenReceipt").val();
        systemConfig.isOpenOnlineStatus = $(".isOpenOnlineStatus").val();
        // systemConfig.isOpenReadReceipt = $(".isOpenReadReceipt").val();
        systemConfig.SMSType = $(".SMSType").val();
        systemConfig.imgVerificationCode = $(".imgVerificationCode").val();

        // systemConfig.iosPushServer = $(".iosPushServer").val();
        systemConfig.isAutoAddressBook = $(".isAutoAddressBook").val();
        systemConfig.isSaveRequestLogs = $(".isSaveRequestLogs").val();
        //弹出loading
        //var index = top.layer.msg('数据提交中，请稍候',{icon: 16,time:false,shade:0.8});
        Common.invoke({
            url : request('/console/config/set'),
            data : systemConfig,
            successMsg : "系统配置修改成功",
            errorMsg : "修改系统配置失败,请检查网络",
            success : function(result) {
                // layer.msg("系统配置修改成功");
                localStorage.setItem("registerInviteCode",systemConfig.registerInviteCode); //更新系统邀请码模式
            },
            error : function(result) {

            }

        });
       
        return false;
    });

})



