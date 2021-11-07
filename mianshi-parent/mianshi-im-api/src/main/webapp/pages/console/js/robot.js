var adminIds = new Array();
var currentPageIndex;// 当前页码数
var currentCount;// 当前总数
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

	 //管理员列表
    var tableIns = table.render({

      elem: '#robot_table'
      ,toolbar:'#checkRobot'
      ,url:request("/console/adminList")+"&adminId="+localStorage.getItem("adminId")+"&type="+"3"+"&userId="+localStorage.getItem("account")
      ,id: 'robot_table'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {type:'checkbox',fixed:'left'}// 多选
          ,{field: 'userId', title: 'userId', width:150}
          ,{field: 'phone', title: '账号', width:150}
          ,{field: 'nickName', title: '昵称', width:150}
          ,{field: 'role', title: '角色',sort:'true', width:150,templet: function(d){
          		if(d.role==3){return "机器人"}
          }}
          ,{field: 'createTime', title: '创建时间',sort:'true', width:200,templet: function(d){
          		return UI.getLocalTime(d.createTime);
          }}
          ,{fixed: 'right', width: 250,title:"操作", align:'left', toolbar: '#robotPageListBar'}
        ]]
        ,done:function(res, curr, count){
            checkRequst(res);
            if(localStorage.getItem("role")==1){
                $(".delete").hide();
                $(".updatePassword").hide();
            }
            var pageIndex = tableIns.config.page.curr;//获取当前页码
            var resCount = res.count;// 获取table总条数
            currentCount = resCount;
            currentPageIndex = pageIndex;
        }
    });

    
    //列表操作
    table.on('tool(robot_table)', function(obj){
        var layEvent = obj.event, data = obj.data;
        console.log("delete:"+JSON.stringify(data));
        if(layEvent === 'delete'){ //删除

         	 Robot.checkDeleteRobotImpl(data.userId,data.role,1);

        }else if(layEvent === 'updatePassword'){// 重置密码

            layui.layer.open({
                title:"重置机器人 "+data.phone+" 的密码",
                type: 1,
                btn:["确定","取消"],
                area: ['310px'],
                content: '<div id="changePassword" class="layui-form" style="margin:20px 40px 10px 40px;;">'
                         +   '<div class="layui-form-item">'
                         +      '<div class="layui-input-block" style="margin: 0 auto;">'
                         +        '<input type="password" required  lay-verify="required" placeholder="新的机器人密码" autocomplete="off" class="layui-input admin_passwd">'
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
    $(".search_live").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();
        console.log(" ======>>>>>>> search Admin Test "+$("#toUserName").val());

        table.reload("robot_table",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
                keyWorld : $("#toUserName").val()  //搜索的关键字
            }
        })
        $("#toUserName").val("");
    });


})
var Robot = {
    // 机器人多选删除
    checkDeleteRobot:function(){
        var checkStatus = layui.table.checkStatus('robot_table'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+checkStatus.data) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选
        var role;
        for (var i = 0; i < checkStatus.data.length; i++){
            adminIds.push(checkStatus.data[i].userId);
            role = checkStatus.data[i].role;
        }
        console.log(adminIds);
        if(0 == checkStatus.data.length){
            layer.msg("请勾选要删除的行");
            return;
        }
        Robot.checkDeleteRobotImpl(adminIds.join(","),role,checkStatus.data.length);
    },

    checkDeleteRobotImpl:function(adminId,role,checkLength){
        console.log("参数：adminIds:"+adminId+"   role: "+role);
        layer.confirm('确定删除指定的机器人？',{icon:3, title:'提示信息',yes:function (index) {

                Common.invoke({
                    url : request('/console/delAdmin'),
                    data : {
                        "adminId" :adminId,
                        "type" :role
                    },
                    successMsg : "删除机器人成功",
                    errorMsg :  "删除机器人失败，请稍后重试",
                    success : function(result) {
                        adminIds = [];
                        // layui.table.reload("robot_table")
                        // layer.close(index); //关闭弹框
                        Common.tableReload(currentCount,currentPageIndex,checkLength,"robot_table");
                    },
                    error : function(result) {

                    }
                });
            },btn2:function (index, layero) {
                adminIds =[];
            },cancel:function () {
                adminIds =[];
            }
        })

    },
}



    
 


