var currentPageIndex;// 当前页码数
var currentCount;// 当前总数
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

	 // 客服列表
    var tableIns = table.render({

      elem: '#admin_list'
      ,url:request("/console/adminList")+"&adminId="+localStorage.getItem("adminId")+"&type="+"4"+"&userId="+localStorage.getItem("account")
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
          ,{field: 'role', title: '角色',sort:'true', width:100,templet: function(d){
          		if(d.role==4){return "客服"}
          }}
          ,{field: 'promotionUrl', title: '推广链接', width:260}
      	  ,{field: 'status', title: '状态',sort:'true', width:100,templet: function(d){
          		if(d.status==1){return "正常"}
          		else if(d.status==-1){return "禁用"}
          }}
          ,{field: 'createTime', title: '创建时间',sort:'true', width:200,templet: function(d){
          		return UI.getLocalTime(d.createTime);
          }}
          ,{field: 'lastLoginTime', title: '最后登录时间',sort:'true', width:200,templet: function(d){
          		
      			if(d.lastLoginTime==0 || d.lastLoginTime=='0'){ return "---";}
      			else{ return UI.getLocalTime(d.lastLoginTime);}
          }}
          ,{fixed: 'right', width: 300,title:"操作", align:'left', toolbar: '#adminListBar'}
        ]]
        ,done:function(res){
            checkRequst(res);
            if(localStorage.getItem("role")==1 || localStorage.getItem("role")==4){
                $(".delete").hide();
                $(".randUser").hide();
                $(".locking").hide();
                $(".cancelLocking").hide();
                $(".promotionUrl").hide();
                $(".btn_addAdmin").hide();
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
        if(layEvent === 'delete'){ //删除

         	 layer.confirm('确定删除该客服？',{icon:3, title:'提示信息'},function(index){
         			
         			Common.invoke({
					      url : request('/console/delAdmin'),
					      data : {
                            "adminId" :data.userId,
                              "type" :data.role
			          	},
					    successMsg : "删除客服成功",
					    errorMsg :  "删除客服失败，请稍后重试",
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

            layui.layer.open({
                title:"重置客服 "+data.phone+" 的密码",
                type: 1,
                btn:["确定","取消"],
                area: ['310px'],
                content: '<div id="changePassword" class="layui-form" style="margin:20px 40px 10px 40px;;">'
                         +   '<div class="layui-form-item">'
                         +      '<div class="layui-input-block" style="margin: 0 auto;">'
                         +        '<input type="password" required  lay-verify="required" placeholder="新的客服密码" autocomplete="off" class="layui-input admin_passwd">'
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
                    /*updateAdmin(localStorage.getItem("adminId"),data,"修改客服密码", function(){
                        layui.layer.close(index); //关闭弹框
                    });*/
                }


             });

        }else if(layEvent === 'locking'){// 锁定
            updateAdmin(localStorage.getItem("account"),data.userId,-1,"禁用客服",data.role,function(){
                        //layui.layer.close(index); //关闭弹框
                   		table.reload("admin_list",{
  			                page: {
  			                    curr: 1 //重新从第 1 页开始
  			                }
			                })
			      });

		}else if(layEvent === 'cancelLocking'){// 解锁
            updateAdmin(localStorage.getItem("account"),data.userId,1,"解禁客服",data.role,function(){
                        //layui.layer.close(index); //关闭弹框
                   		table.reload("admin_list",{
			                page: {
			                    curr: 1 //重新从第 1 页开始
			                }
			            })
			      });
        }else if(layEvent === 'promotionUrl'){
            // 重置推广链接
            layui.layer.open({
                title:"修改客服 "+data.phone+" 的推广链接",
                type: 1,
                btn:["确定","取消"],
                area: ['400px'],
                content: '<form id="changepromotionUrl" class="layui-form" style="margin-left: -30px;margin-top: 40px">'
                +'<div class="layui-form-item">'
                +'<label class="layui-form-label">推广链接</label>'
                +'<div class="layui-input-block">'
                +'<input type="text" required  lay-verify="required" placeholder="新的推广链接" autocomplete="off" class="layui-input updatePromotionUrl">'
                +'</div>'
                +'</div>'
                +'</form>',
                success : function(){  //弹窗打开成功后的回调
                    $(".updatePromotionUrl").val(data.promotionUrl);
                }
                ,yes: function(index, layero){ //确定按钮的回调
                    updateAdmin(localStorage.getItem("account"),data.userId,0,"修改推广链接",data.role,function(){
                        layui.layer.close(index); //关闭弹框
                        table.reload("admin_list",{
                            page: {
                                curr: 1 //重新从第 1 页开始
                            }
                        })
                    });
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
                callback();
            },
            error : function(result) {

            }
        });
    }

    //更新客服数据通用方法
    function updateAdmin(adminId,userId,status,infoStr,role,callback) {
        console.log(+adminId+" "+userId+"  "+status+"  "+role);

	    Common.invoke({
		      url : request('/console/modifyAdmin'),
		      data : {
                "adminId":adminId,
                "userId": userId,
                "status": status,
                "promotionUrl":$(".updatePromotionUrl").val(),
                "role":role
            // "password":(!Common.isNil(password) ? password : null)
          },
		      successMsg : infoStr+"成功",
		      errorMsg :  infoStr+"失败，请稍后重试",
		      success : function(result) {
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


    //add admin
    $(".btn_addAdmin").on("click",function(){
    		$(".admin_accunt").val("");
    		// $(".admin_passwd").val("");
    		// $(".admin_role").val("");

			layui.layer.open({
            title:"",
            type: 1,
            btn:["创建","取消"],
            area: ['400px', '200px'],
            content: $("#add_admin"),
		        success : function(layero,index){  //弹窗打开成功后的回调

          		 layui.form.render('select');
          	},
        	  yes: function(index, layero){

      					var account = $("#add_admin_form .admin_accunt").val();
      					// var passwd = $("#add_admin_form .admin_passwd").val();
      					// var role = $("#add_admin_form .admin_role").val();

      					Common.invoke({
    					      url : request('/console/addAdmin'),
    					      data : {
                                telePhone:account,
                                // adminTelePhone:localStorage.getItem("adminId"),
    					      	// password:$.md5(passwd),
    					      	role:4,
                                type:4
    					      },
    					      successMsg : "创建客服成功",
    					      errorMsg :  "创建客服失败，请稍后重试",
    					      success : function(result) {

    					      	$("#add_admin").hide();
    					        layui.layer.close(index); //关闭弹框

    					      	table.reload("admin_list",{
  					                page: {
  					                    curr: 1 //重新从第 1 页开始
  					                }
    					        })

    					      },
    					      error : function(result) {

    					      }
    				    });

  					},
  					btn2: function(index, layero){
  						$("#add_admin").hide();
  					},
  					cancel: function(){ 
  						$("#add_admin").hide();
  					}


        });

    });


})



    
 


