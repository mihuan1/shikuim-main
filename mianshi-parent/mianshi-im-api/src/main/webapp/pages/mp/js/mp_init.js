var mpCommon = {

	timeDelay:0,

	invoke : function(obj) {

		jQuery.support.cors = true;
		mpCommon.createCommApiSecret(obj.data);
		var params = {
			type : "POST",
			url : obj.url,
			data : obj.data,
			contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
			dataType : 'JSON',
			async:false,
			success : function(result) {

				if(1030101==result.resultCode){//缺少访问令牌
					location.replace("/pages/mp/login.html");
					return;
				}else if(1030102==result.resultCode){ //访问令牌无效或过期
					location.replace("/pages/mp/login.html");
					return;
			 	}

			 	obj.success(result);

			},
			error : function(result) {
				if(obj.error)
					obj.error(result);
			},
			complete : function() {
			}
		};
		
		params.data["access_token"] = mpCommon.getLoginData().access_Token;
		$.ajax(params);
	},
	updateHomeCount : function(){
		mpCommon.invoke({
			url : '/mp/getHomeCount',
			data : {},
			success : function(result) {
				if (result.resultCode == 1) {
					$("#msgCount").html(result.data.msgCount);
					$("#fansCount").html(result.data.fansCount);
					$("#userCount").html(result.data.userCount);
				} 
			},
			error : function(result) {
				console.log("update msgCount error ====> ");
			}
		});
	},
	getLoginData : function(){
		var loginData = JSON.parse(localStorage.getItem('loginData'));
		if(loginData==undefined || loginData == null){
			location.replace("/pages/mp/login.html");
			return;
		}
		return loginData;
	},
	//创建 密钥
	createCommApiSecret : function (obj){
		obj.time=mpCommon.getServerTimeSecond();
		var key = mpCommon.getLoginData().apiKey+obj.time+mpCommon.getLoginData().userId
			+mpCommon.getLoginData().access_Token;
		obj.secret=$.md5(key);
		return obj;
	},
	getServerTimeSecond:function(){
    	return Math.round((this.getMilliSeconds()-mpCommon.timeDelay)/1000);
    },
	getMilliSeconds:function(){
		return Math.round(new Date().getTime());
    },
    getCurrentTime:function(callback){
		mpCommon.invoke({
			url : '/getCurrentTime',
			data : {},
			success : function(result) {
				if (1 == result.resultCode) {
					if(callback)
						callback(result.data);
				} 				mpCommon.timeDelay=mpCommon.getMilliSeconds()-result.currentTime;
				console.log("timeDelay   ====> "+mpCommon.timeDelay);
			}
		});
	},
	logout:function(){
		mpCommon.invoke({
			url : '/mp/logout',
			data : {},
			success : function(result) {
				if (1 == result.resultCode) {
					localStorage.clear();
					location.replace("/pages/mp/login.html");
				}
			}
		});
	}

	
};

$(function() {

	mpCommon.getCurrentTime();//获取和服务器时间的差值
	$("#mp_user_nickname").append(mpCommon.getLoginData().nickname);

	mpCommon.updateHomeCount();
	
	/*setInterval(function(){
		mpCommon.updateHomeCount();
	},15000);*/

	UI.index();
});









