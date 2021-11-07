layui.use(['form'], function() {
    var form = layui.form;
    
    // checkLogin();
    //提交
    form.on('submit(sysUser_login)', function(obj) {
        // obj.field.verkey = codeKey;1562899956759
        
        obj.field.password = $.md5(obj.field.password); 
        console.log("登录密码："+obj.field.password);

        layer.load(1);

        $.post("/console/login",obj.field, function(data) {
            if (data.resultCode == 1) {
                $.ajax({
                    url:'/getCurrentTime',
                    data:{

                    },
                    async:false,
                    success:function(result){
                        localStorage.setItem("currentTime",result.data-Math.round(((new Date().getTime()))));
                    }
                })
                layer.msg("登录成功",{icon: 1});
                console.log("Login data:"+JSON.stringify(data))
                   localStorage.setItem("access_Token",data.data.access_Token);
                   localStorage.setItem("role",data.data.role);
                   localStorage.setItem("account",data.data.account);
                   localStorage.setItem("adminId",data.data.adminId);
                   localStorage.setItem("apiKey",data.data.apiKey);
                   localStorage.setItem("nickname",data.data.nickname);
                   localStorage.setItem("registerInviteCode",data.data.registerInviteCode); //系统邀请码模式

                setTimeout(function() {
                    location.replace("/pages/console/index.html");
                }, 1000);
                
            } else if(data.resultCode == 0) {
                layer.closeAll('loading');
                //layer.msg(data.resultMsg,{icon: 2});
                layer.msg(data.resultMsg,{icon: 2});
                
            }
        }, "json");

        return false; //阻止表单跳转。如果需要表单跳转，去掉这段即可。

    });


    //表单输入效果
    $(".loginBody .input-item").click(function(e){
        e.stopPropagation();
        $(this).addClass("layui-input-focus").find(".layui-input").focus();
    })
    $(".loginBody .layui-form-item .layui-input").focus(function(){
        $(this).parent().addClass("layui-input-focus");
    })
    $(".loginBody .layui-form-item .layui-input").blur(function(){
        $(this).parent().removeClass("layui-input-focus");
        if($(this).val() != ''){
            $(this).parent().addClass("layui-input-active");
        }else{
            $(this).parent().removeClass("layui-input-active");
        }
    })
    
    
});