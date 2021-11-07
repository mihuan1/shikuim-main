/*var browser = {
    versions: function () {
        var u = navigator.userAgent,
            app = navigator.appVersion;
        return {
            trident: u.indexOf('Trident') > -1, /!*IE内核*!/
            presto: u.indexOf('Presto') > -1, /!*opera内核*!/
            webKit: u.indexOf('AppleWebKit') > -1, /!*苹果、谷歌内核*!/
            gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, /!*火狐内核*!/
            mobile: !!u.match(/AppleWebKit.*Mobile.*!/), /!*是否为移动终端*!/
            ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), /!*ios终端*!/
            android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, /!*android终端或者uc浏览器*!/
            iPhone: u.indexOf('iPhone') > -1, /!*是否为iPhone或者QQHD浏览器*!/
            iPad: u.indexOf('iPad') > -1, /!*是否iPad*!/
            webApp: u.indexOf('Safari') == -1, /!*是否web应该程序，没有头部与底部*!/
            souyue: u.indexOf('souyue') > -1,
            superapp: u.indexOf('superapp') > -1,
            weixin: u.toLowerCase().indexOf('micromessenger') > -1,
            Safari: u.indexOf('Safari') > -1
        };
    }(),
    language: (navigator.browserLanguage || navigator.language).toLowerCase()
};*/
    var requestData = {
        userTocken : null,
        isPCRequest : false,
        appId : null,
        callbackUrl : null,
        webAppName : null
    }

    var downLoad ={

        versions : function () {

            // 客户端校验给一个授权回调域名
            if (navigator.userAgent.match(/android/i)) {
                var flag = false;
                var openApp = window.open("shikuimapp://www.shikuandroid.com:80/?appId="+requestData.appId+"&callbackUrl="+requestData.callbackUrl);
                // var openApp = window.open("shikuimapp://www.shikuandroid.com:80/?appId="+requestData.appId+"&redirectURL="+requestData.callbackUrl);

                var lock=setInterval(function() {
                    if(openApp.closed) {
                        clearInterval(lock);
                        flag = true;
                    }
                }, 1000);
                window.setTimeout(function () {
                    if(!flag){
                        window.location.href = "https://www.pgyer.com/shiku2";// 下载地址
                    }

                }, 2000)

            }

            if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
              /*  $('.modal-body').html("数据："+requestData.appId+requestData.callbackUrl);
                $("#errorMsgByTelephone").modal('show');*/
                window.open("shikuimapp://www.shikuios.com:80/?appId="+requestData.appId+"&callbackUrl="+requestData.callbackUrl);//ios app协议
                window.setTimeout(function () {
                    window.location.href = "https://itunes.apple.com/cn/app/%E8%A7%86%E9%85%B7%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AF/id1160132242?mt=8";
                }, 2000)
            }
        },

    }

    // 页面加载函数
    window.onload=function (){

        // 校验是否在开放平台开通登录权限
        myFn.invoke({
            url: '/console/authInterface',
            data: {
                appId: requestData.appId,
                // appSecret: appSecret,
                type: 1
            },
            success: function (result) {
                if (0 == result.resultCode) {
                    if(!requestData.isPCRequest) {
                        $('.modal-body').html(result.resultMsg);
                        $("#errorMsgByTelephone").modal('show');
                        return;
                    }else{
                        alert(result.resultMsg);
                        return;
                    }
                }
            },
            error : function () {
                $('.modal-body').html("网络异常请重试");
                $("#errorMsgByTelephone").modal('show');
            }
        })


        if(/Android|webOS|iPhone|iPod|BlackBerry/i.test(navigator.userAgent)) {
            // 一键登录按钮
            $('#onekeyLogin').show();
        } else {
            requestData.isPCRequest = true;
            // 一键登录按钮
            $('#onekeyLogin').hide();
        }

    }

    function GetRequest() {
        // var url = location.search; //获取url中"?"符后的字串
        var url = decodeURIComponent(location.search);// url urlDncode 解码
        var theRequest = new Object();
        if (url.indexOf("?") != -1) {
            var str = url.substr(1);
            strs = str.split("&");
            for(var i = 0; i < strs.length; i ++) {
                theRequest[strs[i].split("=")[0]]=unescape(strs[i].split("=")[1]);
            }
        }
        return theRequest;
    }
