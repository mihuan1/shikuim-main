
// 热门应用
var strPopularApp={
    lifeCircle : 0,// 生活圈
    videoMeeting : 0,// 视频会议
    liveVideo : 0,// 视频直播
    shortVideo : 0,// 短视频
    peopleNearby : 0,// 附近的人
    scan : 0// 扫一扫
};

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
    $(".apiUrl").val(nullData(data.apiUrl));        
    $(".downloadAvatarUrl").val(nullData(data.downloadAvatarUrl));    
    $(".downloadUrl").val(nullData(data.downloadUrl));        
    $(".uploadUrl").val(nullData(data.uploadUrl));  

    $(".jitsiServer").val(nullData(data.jitsiServer));    
    $(".liveUrl").val(nullData(data.liveUrl));
    $(".appleId").val(nullData(data.appleId));
    $(".website").val(nullData(data.website));
    $(".headBackgroundImg").val(nullData(data.headBackgroundImg));
    $(".hideSearchByFriends").val(data.hideSearchByFriends);
    $(".isCommonFindFriends").val(data.isCommonFindFriends);
    $(".isCommonCreateGroup").val(data.isCommonCreateGroup);
    $(".isOpenRoomSearch").val(data.isOpenRoomSearch);
    $(".isOpenPositionService").val(data.isOpenPositionService);
    $(".isOpenAPNSorJPUSH").val(data.isOpenAPNSorJPUSH);
    $(".haikeInviteCode").val(data.haikeInviteCode);

    // 热门APP回显
    var appInfo ="";
    if(!Common.isNil(data.popularAPP)){
        appInfo= JSON.parse(data.popularAPP);
    }
    
    for(var appName in appInfo){
        // $(".lifeCircle").prop("checked",(appInfo['lifeCircle'] == 0) ? false : true);
        $("."+appName).prop("checked",(appInfo[appName] == 0) ? false : true);
        for(var name in strPopularApp){
            if(appName == name){
                strPopularApp[name] = appInfo[appName];
                console.log("strPopularApp:     "+JSON.stringify(strPopularApp));
            }
        }
    }

    $(".companyName").val(nullData(data.companyName));
    $(".copyright").val(nullData(data.copyright));
    $(".androidVersion").val(nullData(data.androidVersion));
    $(".androidAppUrl").val(nullData(data.androidAppUrl));
    $(".androidExplain").val(nullData(data.androidExplain));

    $(".iosVersion").val(nullData(data.iosVersion));
    $(".iosAppUrl").val(nullData(data.iosAppUrl));
    $(".iosExplain").val(nullData(data.iosExplain));

    $(".pcVersion").val(nullData(data.pcVersion));
    $(".pcAppUrl").val(nullData(data.pcAppUrl));
    $(".pcExplain").val(nullData(data.pcExplain));

    $(".macVersion").val(nullData(data.macVersion));
    $(".macAppUrl").val(nullData(data.macAppUrl));
    $(".macExplain").val(nullData(data.macExplain));

    $(".androidDisable").val(nullData(data.androidDisable));
    $(".iosDisable").val(nullData(data.iosDisable));
    $(".pcDisable").val(nullData(data.pcDisable));
    $(".macDisable").val(nullData(data.macDisable));

    $(".displayRedPacket").val(data.displayRedPacket);
    $(".isOpenReadReceipt").val(data.isOpenReadReceipt);//是否开启已读消息回执
    $(".isOpenRegister").val(data.isOpenRegister);
    $(".showContactsUser").val(data.showContactsUser);

    //重新渲染select 选择框
    layui.form.render();

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

        Common.invoke({
            url : request('/console/clientConfig'),
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

    form.on('checkbox(appCheckbox)', function(data){
        console.log(data.value); //复选框value值，也可以通过data.elem.value得到
        /*  console.log(data.elem); //得到checkbox原始DOM对象
          console.log(data.elem.checked); //是否被选中，true或者false
          console.log(data.value); //复选框value值，也可以通过data.elem.value得到
          console.log(data.othis); //得到美化后的DOM对象*/
        if(data.elem.checked){
            strPopularApp[data.elem.name] = 1;
            console.log("当前勾选的热门应用"+JSON.stringify(strPopularApp));
        }else{
            strPopularApp[data.elem.name] = 0;
            console.log("当前勾选的热门应用"+JSON.stringify(strPopularApp));
        }

    });
    
    form.on("submit(systemConfig)",function(data){
       
       var systemConfig = {};
        systemConfig.id = 10000;
        systemConfig.distance = 0;
        systemConfig.XMPPHost = $(".XMPPHost").val();  
        systemConfig.XMPPDomain = $(".XMPPDomain").val();  
        systemConfig.apiUrl = $(".apiUrl").val();        
        systemConfig.downloadAvatarUrl = $(".downloadAvatarUrl").val();    
        systemConfig.downloadUrl = $(".downloadUrl").val();        
        systemConfig.uploadUrl = $(".uploadUrl").val();  

        systemConfig.jitsiServer = $(".jitsiServer").val();    
        systemConfig.liveUrl = $(".liveUrl").val();
        systemConfig.appleId = $(".appleId").val();
        systemConfig.website = $(".website").val();
        systemConfig.headBackgroundImg = $(".headBackgroundImg").val();
        systemConfig.hideSearchByFriends = $(".hideSearchByFriends").val();
        systemConfig.isCommonFindFriends = $(".isCommonFindFriends").val();
        systemConfig.isCommonCreateGroup = $(".isCommonCreateGroup").val();
        systemConfig.isOpenRoomSearch = $(".isOpenRoomSearch").val();
        systemConfig.isOpenPositionService = $(".isOpenPositionService").val();
        systemConfig.isOpenAPNSorJPUSH = $(".isOpenAPNSorJPUSH").val();

        systemConfig.popularAPP = JSON.stringify(strPopularApp);// 热门应用

        systemConfig.companyName = $(".companyName").val();
        systemConfig.copyright = $(".copyright").val();

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

        // pc
        if($(".pcVersion").val()=="" || $(".pcVersion").val() == null){
            systemConfig.pcVersion = 0;
        }else {
            systemConfig.pcVersion = $(".pcVersion").val();
        }
        systemConfig.pcAppUrl = $(".pcAppUrl").val();
        systemConfig.pcExplain = $(".pcExplain").val();

        // mac
        if($(".macVersion").val()=="" || $(".macVersion").val() == null){
            systemConfig.macVersion = 0;
        }else {
            systemConfig.macVersion = $(".macVersion").val();
        }
        systemConfig.macAppUrl = $(".macAppUrl").val();
        systemConfig.macExplain = $(".macExplain").val();

        systemConfig.androidDisable = $(".androidDisable").val();
        systemConfig.iosDisable = $(".iosDisable").val();
        systemConfig.pcDisable = $(".pcDisable").val();
        systemConfig.macDisable = $(".macDisable").val();

        systemConfig.displayRedPacket = $(".displayRedPacket").val();
        systemConfig.isOpenRegister = $(".isOpenRegister").val();
        systemConfig.showContactsUser = $(".showContactsUser").val();
        systemConfig.isOpenReadReceipt = $(".isOpenReadReceipt").val();
        // 邀请码
        systemConfig.haikeInviteCode = $(".haikeInviteCode").val();

        
        //弹出loading
        //var index = top.layer.msg('数据提交中，请稍候',{icon: 16,time:false,shade:0.8});
        Common.invoke({
            url : request('/console/clientConfig/set'),
            data : systemConfig,
            successMsg : "系统配置修改成功",
            errorMsg : "修改系统配置失败,请检查网络",
            success : function(result) {
               
            },
            error : function(result) {

            }

        });
       
        return false;
    });

})