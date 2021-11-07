var AppConfig={
    // apiUrl : "https://im.shikutech.com",// 接口地址
//    apiUrl : "http://192.168.0.141:8092",// 接口地址
//    apiUrl : "http://120.79.25.45:8092",// 接口地址
    avatarBase : "",// 头像父目录
    apiKey:"im",
}

$(function(){
	myFn.invoke({
		url:'/config',
		success:function(result){
			AppConfig.avatarBase=result.data.downloadAvatarUrl+"avatar/o/";
		}
	
	})
})

var myFn = {
    invoke: function (obj) {
        jQuery.support.cors = true;
        var params = {
            type: "POST",
            url: obj.url,
            data: obj.data,
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            dataType: 'JSON',
            async:false,
            success: function (result) {
                obj.success(result);
            },
            error: function (result) {
                //ownAlert(2,result.resultMsg);
                obj.error(result);
            },
            complete: function () {
            }
        };
        //$.extend(params, obj);
//        params.url = AppConfig.apiUrl + params.url;
        params.url = params.url;
        $.ajax(params);
    },

    // 获取头像
    getImgUrl : function(userId){
        /*if(10000==userId)
            return "/pages/img/im_10000.png";*/
        // var downUrl = AppConfig.avatarBase+"avatar/o/";// 头像访问路径
        return AppConfig.avatarBase + (parseInt(userId) % 10000) + '/' + userId + '.jpg';
    },

    //创建 密钥
    createCommApiSecret : function(appId,appSecret,token){
        var obj = new Object();
        obj.time=myFn.getCurrentSeconds();
       /* var key=AppConfig.apiKey+obj.time+authInterface.userId+authInterface.access_token;
        var md5Key=$.md5(key);
        obj.secret=md5Key;*/
        // 加密规则 md5{apiKey + appId + userId +md5(token + time) +  md5(appSecret)}
        console.log("appId : "+appId+"  ======>   appSecret : " + appSecret +" =====>  token : "+token);
        var md5APPSecret = $.md5(appSecret);
        var md5TokenTime = $.md5(token + myFn.getCurrentSeconds());
        console.log("md5APPSecret : " +md5APPSecret +" ====== >    md5TokenTime : "+md5TokenTime);
        var apiKeyAppIdUserId = AppConfig.apiKey+appId+authInterface.userId;
        console.log(" apiKeyAppIdUserId : =====>  "+ apiKeyAppIdUserId);
        var secret = apiKeyAppIdUserId +md5TokenTime + md5APPSecret;
        console.log(" secret : =====>  "+ secret);
        var md5Key = $.md5(secret);
        console.log("md5Key : " + md5Key);
        obj.secret = md5Key;
        return obj;
    },

    isNil : function(s) {
        return undefined == s ||"undefined"==s|| null == s || $.trim(s) == "" || $.trim(s) == "null"||NaN==s;
    },

    // 当前时间
    getCurrentSeconds : function () {
        return Math.round(new Date().getTime() / 1000);
    },
    
    set: function (key, value, ttl_ms) {
        var data = { value: value, expirse: new Date(ttl_ms).getTime() };
        localStorage.setItem(key, JSON.stringify(data));
    },
    // new Date().getTime()
    get: function (key) {
        var data = JSON.parse(localStorage.getItem(key));
        if (data !== null) {
            debugger
            if (data.expirse != null && data.expirse < myFn.getCurrentSeconds()) {
                localStorage.removeItem(key);
            } else {
                return data.value;
            }
        }
        return null;
    }
    

}