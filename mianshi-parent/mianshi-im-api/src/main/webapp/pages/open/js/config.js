var Config={
	uploadUrl:"",
	apiKey:"im"
}
$(function(){
	myFn.getConfig();
})

var myFn = {
	invoke : function(obj) {
		jQuery.support.cors = true;
		if(!obj.data.secret){
			obj.data=createCommApiSecret(obj.data);
		}
		var params = {
			type : "POST",
			url : obj.url,
			data : obj.data,
			contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
			dataType : 'JSON',
			async:false,
			success : function(result) {
				obj.success(result);
			},
			error : function(result) {
				obj.error(result);
			},
			complete : function() {
			}
		};
		params.url = params.url;
		params.data["access_token"] = localStorage.getItem("access_token");
		$.ajax(params);
	},
	isNil : function(s) {
		return undefined == s || null == s || $.trim(s) == "" || $.trim(s) == "null";
	},
	getConfig:function(){
		$.ajax({
		    url:'/config',
		    async:false,
		    success:function(result){
		      console.log(result.data.uploadUrl);
		      Config.uploadUrl=result.data.uploadUrl+"upload/UploadifyServlet";
		    }
		  });
	}
}


function createCommApiSecret(obj){
		obj.time=Math.round(((new Date().getTime())/1000));
		var key="";
		if(!myFn.isNil(obj.userId)&&!myFn.isNil(obj.access_token)){
			key = Config.apiKey+obj.time+obj.userId+obj.access_token;
		}else{
			key = Config.apiKey+obj.time+localStorage.getItem("userId")+localStorage.getItem("access_token");
		}
		
		var md5Key=$.md5(key);
		obj.secret=md5Key;
		return obj;
}