layui.use(['form'], function() {
    var form = layui.form;

    form.on('submit(openUser_login)', function(obj) {
    	
    	obj.field.password = $.md5(obj.field.password); 
    	
    	$.post("/open/login",obj.field, function(data) {
            if (data.resultCode == 1) {
                layer.msg("登录成功",{icon: 1});
                console.log("Login data:"+JSON.stringify(data))
                localStorage.setItem("telephone",data.data.telephone);
				localStorage.setItem("status",data.data.status);
				localStorage.setItem("access_token",data.data.access_Token);
				localStorage.setItem("userId",data.data.userId);
				localStorage.setItem("apiKey",data.data.apiKey);

                setTimeout(function() {
                    location.replace("/pages/open/index.html");
                }, 1000);
                
            } else if(data.resultCode == 0) {
                layer.closeAll('loading');
                //layer.msg(data.resultMsg,{icon: 2});
                layer.msg(data.resultMsg,{icon: 2});
                
            }
        }, "json");
        return false;
    })
})