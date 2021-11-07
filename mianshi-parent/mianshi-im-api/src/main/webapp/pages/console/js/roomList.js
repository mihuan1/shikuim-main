var page=0;
var lock=0;
var messageIds = new Array();
var userIds = new Array();
var roomJid;
var roomId;
var roomName;
var consoleAdmin = localStorage.getItem("account");
var currentPageIndex;// 群聊天记录中的当前页码数
var currentCount;// 群聊天记录中的当前总数
var roomControl = new Object();// 群控制消息
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

	//群组列表
    var tableInRoom = table.render({
      elem: '#room_table'
      ,url:request("/console/roomList")
      ,id: 'room_table'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
	  ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {field: 'name', title: '群组名称',width:120}
          ,{field: 'desc', title: '群组说明',width:110}
          ,{field: 'userId', title: '创建者Id', sort: true, width:120}
          ,{field: 'nickname', title: '创建者昵称', width:120}
          ,{field: 'userSize', title: '群人数',sort: true, width:100} 
          ,{field: 's', title: '状态',sort: true, width:100,templet:function (d) {
					if(1 == d.s){
						return "正常";
					}else {
						return "被锁定";
					}
                }}
          ,{field: 'createTime',title:'创建时间',sort: true, width:200,templet: function(d){
          		return UI.getLocalTime(d.createTime);
          }}
          ,{fixed: 'right', width: 600,title:"操作", align:'left', toolbar: '#roomListBar'}
        ]]
		,done:function(res, curr, count){
            checkRequst(res);
          /*  $(".deleteMonthLogs").hide();
      		  $(".deleteThousandAgoLogs").hide();
            // 查询聊天记录
            $(".keyWord").hide();
            $(".search_keyWord").hide();*/

			$(".group_name").val("");
			$(".leastNumbers").val("");
          $(".keyWord").addClass("keyWord");

			if(count==0&&lock==1){
                layer.msg("暂无数据",{"icon":2});
            	renderTable();
              }
              lock=0; 
			if(localStorage.getItem("role")==1 || localStorage.getItem("role")==4){
		    	$(".btn_addRoom").hide();
		    	$(".member").hide();
		    	$(".randUser").hide();
		    	$(".modifyConf").hide();
		    	$(".msgCount").hide();
		    	$(".sendMsg").hide();
		    	$(".del").hide();
		    	$(".deleteMonthLogs").hide();
		    	$(".deleteThousandAgoLogs").hide();
		    	$(".locking").hide();
		    	$(".cancelLocking").hide();
		    }
            if(localStorage.getItem("role")==1){
                $(".chatRecord").hide();
			}
            var pageIndex = tableInRoom.config.page.curr;//获取当前页码
            var resCount = res.count;// 获取table总条数
            currentCount = resCount;
            currentPageIndex = pageIndex;

		}
    });


	$("#room_table_div").show();
	$("#roomMsgList").hide();
	$("#roomUserList").hide();
	$("#pushToRoom").hide();
	$("#addRandomUser").hide();
	$("#updateRoom").hide();
	$("#addRoom").hide();

	/*table.on('rowDouble(room_table)', function(obj){
	  console.log(obj);
	});*/

    //列表操作
    table.on('tool(room_table)', function(obj){
        var layEvent = obj.event,
        data = obj.data;
        if(layEvent === 'chatRecord'){ //聊天记录
            $(".keyWord").css("display","inline");
        	roomJid = data.jid;
            // 查询聊天记录
            $(".group_name").hide();
            $(".leastNumbers").hide();
            $(".search_group").hide();
            $(".btn_addRoom").hide();
            $(".keyWord").show();
            $(".search_keyWord").show();
            $(".deleteMonthLogs").show();
            $(".deleteThousandAgoLogs").show();
		    var tableInsRoom = table.render({
			      elem: '#room_msg'
                  ,toolbar: '#toolbarGroupMessageList'
			      ,url:request("/console/groupchat_logs_all")+"&room_jid_id="+data.jid
			      ,id: 'room_msg'
			      ,page: true
			      ,curr: 0
                  ,limit:Common.limit
                  ,limits:Common.limits
			      ,groups: 7
			      ,cols: [[ //表头
                   	   {type:'checkbox',fixed:'left'}// 多选
			          ,{field: 'room_jid_id', title: '房间Id',sort: true, width:220}
			          ,{field: 'sender', title: '发送者Id',sort: true, width:100}
			          ,{field: 'fromUserName', title: '发送者',sort: true, width:220}
			          ,{field: 'type', title: '消息类型',sort: true, width:220,templet:function (d) {
                            return Common.msgType(d.contentType);
                        }}
			          ,{field: 'content', title: '内容',sort: true, width:400,templet:function (d) {
							if(!Common.isNil(d.content)){
                                if(1 == d.isEncrypt && localStorage.getItem("role")==6){
                                    var desContent = Common.decryptMsg(d.content,d.messageId,d.timeSend);
                                    if(desContent.search("https") != -1||desContent.search("http")!=-1){
                                        var link = "<a target='_blank' href=\""+desContent+"\">"+desContent+"</a>";
                                        return link;
                                    }else{
                                        return desContent;
                                    }
                                }else{
                                    var text = (Object.prototype.toString.call(d.content) === '[object Object]' ? JSON.stringify(d.content) : d.content)
                                    try {
                                        if(text.search("https") != -1 || text.search("http")!=-1){
                                            var link = "<a target='_blank' href=\""+text+"\">"+text+"</a>";
                                            return link;
                                        }else{
                                            return text;
                                        }
                                    }catch (e) {
                                        return text;
                                    }
                                }
							}else{
								return "";
							}

                        }}
			          ,{field: 'timeSend',title:'时间',sort: true,width:220,templet: function(d){
			          		return UI.getLocalTime(d.timeSend);
			          }}
			          ,{fixed: 'right', width: 100,title:"操作", align:'left', toolbar: '#roomMessageListBar'}
			        ]]
					,done:function(res, curr, count){
            			checkRequst(res);
						$("#roomMsgList").show();
						$("#room_table_div").hide();
						if(localStorage.getItem("role")==4){
                            $(".deleteMessage").hide();
                            $(".groupChatdelete").hide();
						}
                    var pageIndex = tableInsRoom.config.page.curr;//获取当前页码
                    var resCount = res.count;// 获取table总条数
                    currentCount = resCount;
                    currentPageIndex = pageIndex;
					}
			    });

        } else if(layEvent === 'member'){ //成员管理
        	// console.log(JSON.stringify(data))
            roomId = data.id;
            roomName = data.name;
        	var tableInsMember = table.render({
			      elem: '#room_user'
                  ,toolbar: '#toolbarMembers'
			      ,url:request("/console/roomUserManager")+"&id="+data.id
			      ,id: 'room_user'
			      ,page: true
			      ,curr: 0
                  ,limit:Common.limit
                  ,limits:Common.limits
			      ,groups: 7
			      ,cols: [[ //表头
                       {type:'checkbox',fixed:'left'}// 多选
			          ,{field: 'userId', title: '成员UserId', width:200}
			          ,{field: 'nickname', title: '成员昵称', width:220}
			          ,{field: 'role', title: '成员角色', width:120,templet: function(d){
			          		if(d.role==1){
			          			return "群主";
			          		}else if(d.role==2){
			          			return "管理员";
			          		}else if(d.role==3){
			          			return "成员";
			          		}else if(d.role == 4){
			          			return "隐身人";
							}else if(d.role == 5){
			          			return "监控人";
							}
			          }}
			          ,{field: 'offlineNoPushMsg', title: '是否屏蔽消息', width:200,templet: function(d){
			          		return (d.offlineNoPushMsg==0?"否":"是");
			          }}
			          ,{field: 'createTime',title:'加入时间',width:220,templet: function(d){
			          		return UI.getLocalTime(d.createTime);
			          }}
			          ,{fixed: 'right', width: 120,title:"操作", align:'left', toolbar: '#roomMemberListBar'}
			        ]]
					,done:function(res, curr, count){
            			checkRequst(res);
						$("#roomUserList").show();
						$("#room_table_div").hide();
						$("#save_roomId").val(data.id);
                    	if(localStorage.getItem("role")!=6){
                        	$(".exportFriends").remove();
                    	}
                        var pageIndex = tableInsMember.config.page.curr;//获取当前页码
                        var resCount = res.count;// 获取table总条数
                        currentCount = resCount;
                        currentPageIndex = pageIndex;
			      }
			    });

        }else if(layEvent === 'randUser'){ //添加随机用户

            Room.addRandomUser(data.id);

        } else if(layEvent === 'modifyConf'){ //修改配置
        	Room.updateRoom(data.id);

        } else if(layEvent === 'msgCount'){ //消息统计
        	Count.loadGroupMsgCount(data.jid);

        } else if(layEvent === 'sendMsg'){ //发送消息
        	Room.pushToRoom(data.id,data.jid);
        }else if(layEvent === 'locking'){ // 锁定群组
			Room.lockIng(consoleAdmin,data.id,-1);
        }else if(layEvent === 'cancelLocking'){// 解锁
            Room.lockIng(consoleAdmin,data.id,1);
        }else if(layEvent === 'del'){ //删除

            Room.deleteRoom(data.id,obj,localStorage.getItem("account"));
        }


    });


     table.on('tool(room_user)', function(obj){
        var layEvent = obj.event,
            data = obj.data;
        if(layEvent === 'deleteMember'){ // 删除群成员
    		Room.toolbarMembersImpl($("#save_roomId").val(),data.userId,1);

    	}
     })
     // 删除消息
     table.on('tool(room_msg)', function(obj){
        var layEvent = obj.event,
            data = obj.data;

        if(layEvent === 'deleteMessage'){ //聊天记录
        	console.log(data);
            Room.toolbarGroupMessageListImpl(data.room_jid_id,data._id,1);
        }
     })

    //搜索
    $(".search_group").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();
        // 校验群人数
        var numbers = $(".leastNumbers").val();
        if(null != numbers && "" != numbers && undefined != numbers){
            var reg = /^[0-9]\d*$/;
            if(!reg.test(numbers)){
                layer.alert("请输入有效的群人数");
                return;
            }
        }

        table.reload("room_table",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
                keyWorld : $(".group_name").val(),  //搜索的关键字
                leastNumbers : $(".leastNumbers").val()
            }
        })
        lock=1;
    });

    //关键字聊天记录搜索
    $(".search_keyWord").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();
        table.reload("room_msg",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
                keyWord : $(".keyWord").val()  //搜索的关键字
            }
        })
        $(".keyWord").val("")
        lock=1;
    });

})

//重新渲染表单
function renderTable(){
  layui.use('table', function(){
   var table = layui.table;//高版本建议把括号去掉，有的低版本，需要加()
   // table.reload("user_list");
    table.reload("room_table",{
        page: {
            curr: 1 //重新从第 1 页开始
        },
        where: {
            keyWorld : $(".group_name").val(),  //搜索的关键字
            leastNumbers : $(".leastNumbers").val()  //搜索的关键字
        }
    })
  });
 }
	
var button='<button onclick="Room.button_back()" class="layui-btn layui-btn-primary layui-btn-sm" style="margin-top: 35px;margin-left: 50px;"><<返回</button>';


var Room={

		// 新增群组
		addRoom:function(){
			$("#roomList").hide();
			$("#roomMsgList").hide();
			$("#addRoom").show();
			
		},
		// 提交新增群组
		commit_addRoom:function(){
			if($("#add_roomName").val()==""){
				layer.alert("请输入群名称");
				return ;
			}else if($("#add_desc").val()==""){
				layer.alert("请输入群说明");
				return;
			}
			Common.invoke({
				url:request('/console/addRoom'),
				data:{
					userId:localStorage.getItem("account"),// 让当前登录后台管理系统的系统管理员创建房间
					name:$("#add_roomName").val(),
					desc:$("#add_desc").val()
				},
				success:function(result){
					if(result.resultCode==1){
                        layer.msg("新增成功",{"icon":1});
						$("#roomList").show();
						$("#roomMsgList").hide();
						$("#addRoom").hide();
						$("#add_roomName").val("");
						$("#add_desc").val("");
                        // UI.roomList(0);
                        layui.table.reload("room_table",{
                            page: {
                                curr: 1 //重新从第 1 页开始
                            },
                            where: {

                            }
                        })
					}
				}
			});
		},

     // 删除群组
     deleteRoom:function(id,obj,userId){
         layer.confirm('确定删除该群组？',{icon:3, title:'提示信息'},function(index){
             $.ajax({
                 type:'POST',
                 url:request('/console/deleteRoom'),
                 data:{
                     roomId:id,
                     userId:userId
                 },
                 async:false,
                 success : function(result){
                     checkRequst(result);
                     if(result.resultCode==1){
                         layer.alert("删除成功");
                         // layui.table.reload("room_table");
                         Common.tableReload(currentCount,currentPageIndex,1,"room_table");
                     }
                     if(result.resultCode==0){
                         layer.alert(result.resultMsg);
                     }
                 },

             })
         });
     },

		// 群成员管理
		roomUserList:function(e,roomId){
			html="";
			if(e==1){
				if(page>0){
					page--;
				}
			}else if(e==2){
				if(sum<10){
					layui.layer.alert("已是最后一页");
				}else{
					page++;
				}
			}
			Common.invoke({
				url:request('/console/roomUserManager'),
				data:{
					id:roomId,
					pageIndex:page
				},
				success:function(result){
					if(result.data.pageData!=null){
						sum=result.data.pageData.length;
						for(var i=0;i<result.data.pageData.length;i++){

							html+="<tr><td>"+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].nickname+"</td><td>"
							+result.data.pageData[i].role+"</td><td>"+(result.data.pageData[i].offlineNoPushMsg==0?"否":"是")+"</td><td>"
							+UI.getLocalTime(result.data.pageData[i].createTime)+"</td><td><button onclick='Room.deleteMember(\""+roomId+"\",\""+result.data.pageData[i].userId+"\")' class='layui-btn layui-btn-danger layui-btn-xs'>删除</button></td></tr>";
						}
						var tab="<a href='javascript:void(0);' onclick='Room.roomUserList(1,\""+roomId+"\")' class='layui-laypage-prev layui-disabled' data-page='0'>上一页</a>"
						+"<a href='javascript:void(0);' onclick='Room.roomUserList(2,\""+roomId+"\")' class='layui-laypage-next' data-page='2'>下一页</a>";
						$("#roomUser_table").empty();
						$("#roomUser_table").append(html);
						$("#roomUserList_div").empty();
						$("#roomUserList_div").append(tab);
						$("#room_table_div").hide();
						$("#roomMsgList").hide();
						$("#roomUserList").show();
						$("#back").empty();
						$("#back").append(button);

					}
				}
			})
		},
		// 删除群成员
		deleteMember:function(roomId,userId,obj){
			layer.confirm('确定删除该群成员？',{icon:3, title:'提示信息'},function(index){
				Common.invoke({
					url:request('/console/deleteMember'),
					data:{
						userId:userId,
						roomId:roomId
					},
					success:function(result){
						if(result.resultCode==1){
							
							layer.alert("删除成功");
							obj.del();
							// Room.roomUserList(0,roomId);
						}
					}
				})
			})
			
		},

	// 批量移出群成员
    toolbarMembers:function(){
        // 多选操作
        var checkStatus = layui.table.checkStatus('room_user'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+checkStatus.data) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选
		for (var i = 0; i < checkStatus.data.length; i++){
            userIds.push(checkStatus.data[i].userId);
		}
        if(0 == checkStatus.data.length){
            layer.msg("请勾选要移出的行");
            return;
        }
        console.log(userIds);
        Room.toolbarMembersImpl($('#save_roomId').val(),userIds.join(","),checkStatus.data.length);
	},

    toolbarMembersImpl:function(roomId,userId,checkLength){
        layer.confirm('确定移出指定群成员',{icon:3, title:'提示消息',yes:function () {
                Common.invoke({
                    url:request('/console/deleteMember'),
                    data:{
                        roomId :roomId,
                        userId :userId,
                        adminUserId :localStorage.getItem("account")
                    },
                    success:function(result){
                        if(result.resultCode==1){
                            layer.msg("移出成功",{"icon":1});
                            userIds = [];
                            // renderTable();
                            Common.tableReload(currentCount,currentPageIndex,checkLength,"room_user");
                            // layui.table.reload("room_user");
                        }else if(result.resultCode==0){
                            layui.table.reload("room_user");
                            layer.msg(result.resultMsg);
						}
                    },

                })
            },btn2:function () {
                userIds = [];
            },cancel:function () {
                userIds = [];
         }});
	},


    // 批量删除群成员（等同于批量删除用户）
    toolbarDeleteMembers:function(){
        // 多选操作
        var checkStatus = layui.table.checkStatus('room_user'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+checkStatus.data) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选
        for (var i = 0; i < checkStatus.data.length; i++){
            userIds.push(checkStatus.data[i].userId);
        }
        if(0 == checkStatus.data.length){
            layer.msg("请勾选要删除的行");
            return;
        }
        console.log(userIds);
        Room.toolbarDeleteMembersImpl(userIds.join(","));
    },

    toolbarDeleteMembersImpl:function(userId){
        layer.confirm('确定删除指定群成员用户,<br>删除后该系统会注销此用户',{icon:3, title:'提示消息',yes:function () {
                Common.invoke({
                    url:request('/console/deleteUser'),
                    data:{
                        userId :userId
                    },
                    success:function(result){
                        if(result.resultCode==1){
                            layer.msg("删除成功",{"icon":1});
                            userIds = [];
                            // renderTable();
                            layui.table.reload("room_user");
                        }else if(result.resultCode==0){
                            layer.msg(result.resultMsg);
                        }
                    },

                })
            },btn2:function () {
                userIds = [];
            },cancel:function () {
                userIds = [];
            }});
    },

	// 导出群成员
	exprotExcelByMember:function(){
        layui.layer.open({
            title: '数据导出'
            ,type : 1
            ,offset: 'auto'
            ,area: ['300px','180px']
            ,btn: ['导出', '取消']
            ,content:  '<form class="layui-form" action="/console/exportExcelByGroupMember">'
            + '<div class="layui-form-item">'
            + 	'<div class="layui-inline">'
            +	'<input type="hidden" name="roomId" value='+roomId+'>'
            + 		'<label class="layui-form-label" style="width: 90%;margin-top: 20px" >导出群组 "'+roomName+'" 的群成员列表</label>'
            +	     '</div>'
            + '</div>'
            +  '<button id="exportGroupMember_submit"  class="layui-btn" type="submit" lay-submit="" style="display:none">导出</button>'
            +'</from>'
            ,success: function(index, layero){
                layui.form.render();
            }
            ,yes: function(index, layero){
                $("#exportGroupMember_submit").click();
                layui.layer.close(index); //关闭弹框
            }
            ,btn2: function(index, layero){
                //按钮【取消】的回调

                //return false 开启该代码可禁止点击该按钮关闭
            }

        });
	},

	// 刷新table
    reloadTable:function(){
		// 刷新父级页面
        parent.layui.table.reload("room_user")
    },

	// 邀请用户加入群组 inviteJoinRoom
    inviteJoinRoom:function(){
		console.log("joinRoom :       "+roomId);
        localStorage.setItem("roomId", roomId);
        layer.open({
            title : "",
            type: 2,
            skin: 'layui-layer-rim', //加上边框
            area: ['1050px', '700px'], //宽高
            content: 'inviteJoinRoom.html'
            ,success: function(index, layero){

            }
        });
	},

	// 群发消息
	pushToRoom:function(id,jid){
		// $("#roomList").hide();
		// $("#roomMsgList").hide();
		html="";
		$("#room_table_div").hide();
		$("#pushToRoom").show();
		$("#push_roomJid").val(jid);
		Common.invoke({
			url:request('/console/getRoomMember'),
			data:{
				roomId:id
			},
			success:function(result){
				if(result.data!=null){
					for(var i=0;i<result.data.members.length;i++){
						if(result.data.members[i].role<3){
							html+="<option value='"+result.data.members[i].userId+"'>"+result.data.members[i].nickname+"</option>";
						}
					}
					$("#push_sender").empty();
					$("#push_sender").append(html);
					$("#back").empty();
					$("#back").append(button);
				}
			}
		})
	},

    // 群组锁定解锁
    lockIng:function(userId,roomId,status){
        var confMsg,successMsg="";
        (status == -1 ? confMsg = '确定锁定该群组？':confMsg = '确定解锁该群组？');
        (status == -1 ? successMsg = "锁定成功":successMsg ="解锁成功");
        layer.confirm(confMsg,{icon:3, title:'提示信息'},function(index){

            Common.invoke({
                url : request('/console/updateRoom'),
                data : {
                    userId:userId,
					roomId:roomId,
                    s:status
                },
                successMsg : successMsg,
                errorMsg :  "加载数据失败，请稍后重试",
                success : function(result) {
                    layui.table.reload("room_table")
                },
                error : function(result) {
                }
            });
        })
    },


		// 发送
		commit_push:function(){
		    if(null == $("#push_context").val() || "" == $("#push_context").val()){
                layer.alert("请输入要发送内容");
                return;
            }

			Common.invoke({
				url:request('/console/sendMsg'),
				data:{
					jidArr:$("#push_roomJid").val(),
					userId:$("#push_sender").val(),
					type:1,
					content:$("#push_context").val()
				},
				success:function(result){
					layer.alert("发送成功");
                    $("#push_context").val("")
				}
			})
		},
		// 添加随机用户
		addRandomUser:function(roomId){
			$("#room_table_div").hide();
			$("#addRandomUser").show();
			$("#roomId").html(roomId);
			$("#back").empty();
			$("#back").append(button);
		},
		commit_addRandomUser:function(){
			Common.invoke({
				url:request('/console/autoCreateUser'),
				data:{
					userNum:$("#addRandomUserSum").val(),
					roomId:$("#roomId").html()
				},
				success:function(result){
					if(result.resultCode==1){
						layer.alert("添加成功");
						// Room.roomList(0);
						$("#room_table_div").show();
						$("#addRandomUser").hide();
					}
				}
			})
		},
		// 修改群配置
		updateRoom:function(roomId){
			
			Common.invoke({
				url:request('/console/getRoomMember'),
				data:{
					roomId:roomId
				},
				success:function(result){
					if(result.data!=null){
					    roomControl = result.data;// 群控制参数
						$("#updateRoom_id").val(result.data.id);
						$("#update_roomId").html(result.data.id);
						$("#update_roomJid").html(result.data.jid);
						$("#name").val(result.data.name);
						$("#desc").val(result.data.desc);
						$("#maxUserSize").val(result.data.maxUserSize);
						$("#isLook").val(result.data.isLook);
						$("#showRead").val(result.data.showRead);
						$("#isNeedVerify").val(result.data.isNeedVerify);
						$("#showMember").val(result.data.showMember);
						$("#allowSendCard").val(result.data.allowSendCard);
						// $("#allowHostUpdate").val(result.data.allowHostUpdate);
						$("#allowInviteFriend").val(result.data.allowInviteFriend);
						$("#allowUploadFile").val(result.data.allowUploadFile);
						$("#allowConference").val(result.data.allowConference);
						$("#allowSpeakCourse").val(result.data.allowSpeakCourse);
						$("#isAttritionNotice").val(result.data.isAttritionNotice);
						// 渲染复选框
                        layui.form.render();

						$("#room_table_div").hide();
						$("#updateRoom").show();
						$("#updateRoom1").show();

						$("#back").empty();
						$("#back").append(button);
					}

				}
			})
		},

	// 多选删除群组聊天记录
    toolbarGroupMessageList:function(){
        // 多选操作
        var checkStatus = layui.table.checkStatus('room_msg'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+checkStatus.data) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选
        var userId;
        for (var i = 0; i < checkStatus.data.length; i++){
            messageIds.push(checkStatus.data[i]._id);
            roomJid = checkStatus.data[i].room_jid_id
        }
        console.log("roomJid"+roomJid+"------"+messageIds);
        if(0 == checkStatus.data.length){
            layer.msg("请勾选要删除的行");
            return;
        }
        Room.toolbarGroupMessageListImpl(roomJid,messageIds.join(","),checkStatus.data.length);
	},

    toolbarGroupMessageListImpl:function(room_jid_id,messageId,checkLength){
        layer.confirm('确定删除指定群聊聊天记录',{icon:3, title:'提示消息',yes:function () {
			Common.invoke({
				url:request('/console/groupchat_logs_all/del'),
				data:{
					msgId :messageId,
                    room_jid_id:room_jid_id
				},
				success:function(result){
					if(result.resultCode==1)
					{
						layer.msg("删除成功",{"icon":1});
						messageIds = [];
						// Common.tableReload(currentCount,currentPageIndex,"room_msg")
                        Common.tableReload(currentCount,currentPageIndex,checkLength,"room_msg");
					}
				}
			})
		},btn2:function () {
			messageIds = [];
		},cancel:function () {
			messageIds = [];
        }});
	},

	// 返回
	button_back:function(){
        $(".group_name").show();
        $(".leastNumbers").show();
        $(".search_group").show();
        $(".btn_addRoom").show();
		$(".deleteMonthLogs").hide();
		$(".deleteThousandAgoLogs").hide();
		$(".keyWord").hide();
		$("#room_table_div").show();
		$("#roomList").show();
		$("#roomMsgList").hide();
		$("#roomUserList").hide();

		$("#addRoom").hide();
		$("#pushToRoom").hide();
		$("#addRandomUser").hide();
		$("#updateRoom").hide();
		$("#updateRoom1").hide();
		$("#back").empty();
		$("#back").append("&nbsp;");
		layui.table.reload("room_table");
	},

}
	// 删除一个月前的日志
	$(".deleteMonthLogs").on("click",function(){
		layer.confirm('确定删除一个月前的群聊聊天记录？',{icon:3, title:'提示信息'},function(index){
			Common.invoke({
				url : request('/console/groupchatMsgDel'),
				data : {
					'roomJid':roomJid,
					'type' : 0
				},
				successMsg : "删除成功",
				errorMsg : "删除失败,请稍后重试",
				success : function(result) {
					if (1 == result.resultCode){
						layui.table.reload("room_msg");
					}
				},
				error : function(result) {
				}
			});

		});

	});
	// 删除最近十万条之前的日志
	$(".deleteThousandAgoLogs").on("click",function(){
		layer.confirm('确定删除十万条之前的群聊聊天记录？',{icon:3, title:'提示信息'},function(index){
			Common.invoke({
				url : request('/console/groupchatMsgDel'),
				data : {
					'roomJid':roomJid,
					'type' : 1
				},
				successMsg : "删除成功",
				errorMsg : "删除失败,请稍后重试",
				success : function(result) {
					if (1 == result.resultCode){
						layui.table.reload("room_msg");
					}
				},
				error : function(result) {
                    layui.layer.alert("数量小于等于100000")
				}
			});

		});

	});

	// 修改群属性
	function updateGroupConfig(userId,roomId,roomName,desc,maxUserSize,callback){
		console.log("userId："+userId+"---"+"roomId："+roomId+"---"+"roomName："+roomName+"---"+"desc："+desc+"---"+"maxUserSize："+maxUserSize);
		Common.invoke({
			url : request('/console/updateRoom'),
			data : {
				"userId" : userId,
				"roomId": roomId,
				"roomName": (null == roomName ? null : roomName),
				"desc": (null == desc ? null : desc),
				"maxUserSize": (null == maxUserSize ? null : maxUserSize)
			},
			successMsg : "修改成功",
			errorMsg :  "修改失败，请稍后重试",
			success : function(result) {
				callback();
			},
			error : function(result) {

			}
		});
	}

	// 修改群控制
	function updateConfig(userId,roomId,paramName,paramVal,callback){
		console.log("userId："+userId+"---"+"roomId："+roomId+"---"+"paramName："+paramName+"---"+"paramVal："+paramVal);
		var newParamName = paramName;
		console.log("参数名称："+newParamName);
		obj={
            "userId" : userId,
            "roomId": roomId
		}
		obj[newParamName]=paramVal;
		Common.invoke({
			url : request('/console/updateRoom'),
			data :obj,
			successMsg : "修改成功",
			errorMsg :  "修改失败，请稍后重试",
			success : function(result) {
				callback();
			},
			error : function(result) {

			}
		});
	}

	// 最新的群配置

	// 修改群名称
	$("#name").on("click",function(){
        var oldName =  $("#name").val();
        layui.layer.open({
            title:"群组名称修改",
            type: 1,
            btn:["确定","取消"],
            area: ['310px'],
            content: '<div id="changePassword" class="layui-form" style="margin:20px 40px 10px 40px;;">'
            +   '<div class="layui-form-item">'
            +      '<div class="layui-input-block" style="margin: 0 auto;">'
            +        '<input type="text" value="'+oldName+'" required  lay-verify="required" placeholder="新的群组名称" autocomplete="off" class="layui-input changeRoomName">'
            +      '</div>'
            +    '</div>'
            +'</div>'
            ,yes: function(index, layero){ //确定按钮的回调
               	var roomId = $("#update_roomId").html();
				var roomName = $(".changeRoomName").val();
                updateGroupConfig(localStorage.getItem("account"),roomId,roomName,null,null,function () {
                    layui.layer.close(index); //关闭弹框
					$("#name").val(roomName);
                })
            }
        });
	});
	// 修改群描述
	$("#desc").on("click",function(){
        var oldDesc =  $("#desc").val();
		layui.layer.open({
			title:"群组描述修改",
			type: 1,
			btn:["确定","取消"],
			area: ['310px'],
			content: '<div id="changePassword" class="layui-form" style="margin:20px 40px 10px 40px;;">'
			+   '<div class="layui-form-item">'
			+      '<div class="layui-input-block" style="margin: 0 auto;">'
			+        '<input type="text" value="'+oldDesc+'" required  lay-verify="required" placeholder="新的群组描述" autocomplete="off" class="layui-input changeDesc">'
			+      '</div>'
			+    '</div>'
			+'</div>'
			,yes: function(index, layero){ //确定按钮的回调
                var roomId = $("#update_roomId").html();
                var desc = $(".changeDesc").val();
                updateGroupConfig(localStorage.getItem("account"),roomId,null,desc,null,function () {
                    layui.layer.close(index); //关闭弹框
                    $("#desc").val(desc);
                })
			}
		});
	});

	// 修改群最大人数
	$("#maxUserSize").on("click",function(){
		var oldMaxUserSize = $("#maxUserSize").val();
		layui.layer.open({
			title:"群组最大人数修改",
			type: 1,
			btn:["确定","取消"],
			area: ['310px'],
			content: '<div id="changePassword" class="layui-form" style="margin:20px 40px 10px 40px;;">'
			+   '<div class="layui-form-item">'
			+      '<div class="layui-input-block" style="margin: 0 auto;">'
			+        '<input type="text" value="'+oldMaxUserSize+'" required lay-verify="required" placeholder="请输入群最大人数" autocomplete="off" class="layui-input changeMaxnum">'
			+      '</div>'
			+    '</div>'
			+'</div>'
			,yes: function(index, layero){ //确定按钮的回调
                var roomId = $("#update_roomId").html();
                var changeMaxnum = $(".changeMaxnum").val();
                if(changeMaxnum > 10000){
                	layui.layer.alert("最高上限10000人")
					return;
                }
            updateGroupConfig(localStorage.getItem("account"),roomId,null,null,changeMaxnum,function () {
                layui.layer.close(index); //关闭弹框
                $("#maxUserSize").val(changeMaxnum);
            })
		}
		});
});

// 群控制消息 lay-filter ： test
layui.form.on('select(test)', function(data){
    console.log(data);
    console.log(data.elem.id);
    console.log(data.value);
    var elemId = data.elem.id;
    var elemVal = data.elem.value;
    // console.log("roomControl: "+JSON.stringify(roomControl));
    var paramValue = roomControl[data.elem.id];
    // console.log("paramValue : "+paramValue);
    // 避免重复提交
    if(paramValue == data.elem.value)
        return;
    else{
        // 更新内存中的值
        roomControl[data.elem.id] = data.elem.value;
        // console.log("new roomControl: "+JSON.stringify(roomControl));
    }
    var roomId = $("#update_roomId").html();
    updateConfig(localStorage.getItem("account"),roomId,elemId,elemVal,function () {
        // $("#"+elemId).val(elemVal);
    });
	});