var currentPageIndex;// 当前页码数
var currentCount;// 当前总数
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

	 //游客列表
    var tableIns = table.render({

      elem: '#admin_list'
      ,url:request("/console/adminList")+"&adminId="+localStorage.getItem("adminId")+"&type="+"2"+"&userId="+localStorage.getItem("account")
      ,id: 'admin_list'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {field: 'userId', title: 'userId', width:150}
          ,{field: 'phone', title: '账号', width:150}
          ,{field: 'nickName', title: '昵称', width:150}
          ,{field: 'role', title: '角色',sort:'true', width:150,templet: function(d){
          		if(d.role==2){return "公众号"}
          }}
          ,{field: 'createTime', title: '创建时间',sort:'true', width:200,templet: function(d){
          		return UI.getLocalTime(d.createTime);
          }}
          ,{fixed: 'right', width: 250,title:"操作", align:'left', toolbar: '#adminListBar'}
        ]]
        ,done:function(res){
          checkRequst(res);
            if(localStorage.getItem("role")==1){
                $(".delete").hide();
                $(".randUser").hide();
                $(".locking").hide();
                $(".cancelLocking").hide();
            }
            var pageIndex = tableIns.config.page.curr;//获取当前页码
            var resCount = res.count;// 获取table总条数
            currentCount = resCount;
            currentPageIndex = pageIndex;
        }
    });

    
    //列表操作
    table.on('tool(admin_list)', function(obj){
        var layEvent = obj.event, data = obj.data;
        console.log("delete:"+JSON.stringify(data));
        if(layEvent === 'delete'){ //删除
            // 过滤系统号操作
            if(data.userId <= 10000)
                return;
         	layer.confirm('确定删除该公众号？',{icon:3, title:'提示信息'},function(index){
                Common.invoke({
                      url : request('/console/delAdmin'),
                      data : {
                        "adminId" :data.userId,
                          "type" :data.role
                    },
                    successMsg : "删除公众号成功",
                    errorMsg :  "删除公众号失败，请稍后重试",
                    success : function(result) {
                        layer.close(index); //关闭弹框
                        // obj.del();
                        Common.tableReload(currentCount,currentPageIndex,1,"admin_list");
                    },
                    error : function(result) {

                    }
                });
         	 })

        }else if(layEvent === 'randUser'){// 重置密码
            if(data.userId <= 10000)
                return;
            layui.layer.open({
                title:"重置公众号"+data.phone+" 的密码",
                type: 1,
                btn:["确定","取消"],
                area: ['310px'],
                content: '<div id="changePassword" class="layui-form" style="margin:20px 40px 10px 40px;;">'
                         +   '<div class="layui-form-item">'
                         +      '<div class="layui-input-block" style="margin: 0 auto;">'
                         +        '<input type="password" required  lay-verify="required" placeholder="新的公众号密码" autocomplete="off" class="layui-input admin_passwd">'
                         +      '</div>'
                         +    '</div>'
                         +   '<div class="layui-form-item">'
                         +      '<div class="layui-input-block" style="margin: 0 auto;">'
                         +        '<input type="password" required  lay-verify="required" placeholder="确认密码" autocomplete="off" class="layui-input admin_rePasswd">'
                         +      '</div>'
                         +    '</div>'
                         +'</div>'

                ,yes: function(index, layero){ //确定按钮的回调

                    var newPasswd = $("#changePassword .admin_passwd").val();
                    var reNewPasswd = $("#changePassword .admin_rePasswd").val();
                    if(newPasswd!=reNewPasswd){
                      layui.layer.msg("两次密码输入不一致",{"icon":2});
                      return;
                    }
                    data.password = $.md5(newPasswd);
                    updateRole(data.userId,data.password,function () {
                        layui.layer.close(index); //关闭弹框
                    });
                    /*updateAdmin(localStorage.getItem("adminId"),data,"修改管理员密码", function(){
                        layui.layer.close(index); //关闭弹框
                    });*/
                }


             });

        }

     });

    function updateRole(userId,newPassword,callback){
        console.log("userId"+userId+"---"+"password"+newPassword);
        Common.invoke({
            url : request('/console/updateUserPassword'),
            data : {
                "userId" : userId,
                "password": newPassword
            },
            successMsg : "重置密码成功",
            errorMsg :  "重置密码失败，请稍后重试",
            success : function(result) {
                // layui.layer.close(index); //关闭弹框
                // // location.replace("/pages/console/login.html");
                callback();
            },
            error : function(result) {

            }
        });
    }

    //搜索
    $(".search_admin").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();
        console.log(" ======>>>>>>> search Admin Test "+$(".admin_keyword").val());

        table.reload("admin_list",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
                keyWorld : $(".admin_keyword").val()  //搜索的关键字
            }
        })
        $(".admin_keyword").val("");
    });

})



    
 


