var page=0;
var sum=0;
var currentPageIndex;// 当前页码数
var currentCount;// 当前总数
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

	//直播间列表
    var tableInsLiveRoom = table.render({

      elem: '#liveRoom_table'
      ,url:request("/console/liveRoomList")
      ,id: 'liveRoom_table'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {field: 'name', title: '直播间名称',sort: true,width:120}
          ,{field: 'notice', title: '房间公告',sort: true,width:120}
          ,{field: 'userId', title: '创建者Id',sort: true, width:120}
          ,{field: 'nickName', title: '创建者昵称',sort: true, width:120}
          ,{field: 'url', title: '推流地址',sort: true, width:300}
          ,{field: 'currentState', title: '直播间当前状态',sort: true, width:150,templet : function (d) {
					if(d.currentState == 0)
						return "正常";
					else if(d.currentState == 1)
						return "被锁定";
                }}
          ,{field: 'createTime',title:'创建时间',width:195,templet: function(d){
          		return UI.getLocalTime(d.createTime);
          }}
          ,{fixed: 'right', width: 250,title:"操作", align:'left', toolbar: '#liveRoomListBar'}
        ]]
		,done:function(res, curr, count){
			checkRequst(res);
			if(localStorage.getItem("role")==1 || localStorage.getItem("role")==7){
				$(".delete").hide();
				$(".chatMessage").hide();
				$(".member").hide();
				$(".btn_addLive").hide();
			}
            var pageIndex = tableInsLiveRoom.config.page.curr;//获取当前页码
            var resCount = res.count;// 获取table总条数
            currentCount = resCount;
            currentPageIndex = pageIndex;
		}
    });

    //列表操作
    table.on('tool(liveRoom_table)', function(obj){
         var layEvent = obj.event,
            data = obj.data;
            console.log(data);
        if(layEvent === 'disable'){ //锁定直播间
			Live.deleteLiveRoom(data.roomId,1);
        }else if(layEvent === 'relieveDisable'){// 解锁的直播间
            Live.deleteLiveRoom(data.roomId,0);
		}else if(layEvent === 'member'){ //成员管理
        	var tableIns1 = table.render({
			  elem: '#liveRoomMember_table'
			  ,url:request("/console/liveRoomUserManager")+"&roomId="+data.roomId
			  ,id: 'liveRoomMember_table'
			  ,page: true
			  ,curr: 0
			  ,limit:Common.limit
			  ,limits:Common.limits
			  ,groups: 7
			  ,cols: [[ //表头
				   {field: 'userId', title: '用户Id',sort: true, width:220}
				  ,{field: 'nickName', title: '昵称',sort: true, width:200}
				  ,{field: 'state',title:"状态",sort: true, width:150,templet: function(d){
						if(d.state==1){
							return  "禁言";
						}else{
							return "正常";
						}
					}}
				  ,{field: 'createTime', title: '加入时间',sort: true, width:400,templet: function(d){
						return UI.getLocalTime(d.createTime);
				  }}
				  ,{fixed: 'right', width: 240,title:"操作", align:'left', toolbar: '#liveRoomMemberListBar'}
				]]
				,done:function(res, curr, count){
					checkRequst(res);
					$("#liveRoomList").hide();
					$("#liveRoomUser").show();
					$("#save_roomId").val(data.roomId);
				}
			});
        }else if(layEvent === 'chatMessage'){// 聊天记录
        	var tableIns1 = table.render({

			      elem: '#liveRoomMsg_table'
			      ,url:request("/console/roomMsgDetail")+"&room_jid_id="+data.jid
			      ,id: 'liveRoomMsg_table'
			      ,page: true
			      ,curr: 0
                  ,limit:Common.limit
                  ,limits:Common.limits
			      ,groups: 7
			      ,cols: [[ //表头
			           {field: 'room_jid_id', title: '房间Id',sort: true, width:220}
			          ,{field: 'sender', title: '发送者Id',sort: true, width:98}
			          ,{field: 'fromUserName', title: '发送者',sort: true, width:220}
			          ,{field: 'ts', title: '时间',sort: true, width:400,templet: function(d){
			          		return UI.getLocalTime(d.timeSend);
			          }}
			          ,{field: 'content',title:'内容',sort: true,width:220}]]
					,done:function(res, curr, count){
						checkRequst(res);
						$("#liveRoomList").hide();
						$("#liveRoomMsg").show();
					}
			    });
        }
      });


    //列表操作
    table.on('tool(liveRoomMember_table)', function(obj){
         var layEvent = obj.event,
            data = obj.data;
         if(layEvent === 'remove'){ // 剔除
         	Live.deleteRoomUser(data.userId,$("#save_roomId").val(),obj);

         } else if(layEvent === 'shutup'){// 禁言
         	Live.shutup(data.userId,$("#save_roomId").val(),1);
         }else if(layEvent === 'cancelShutup'){
             Live.shutup(data.userId,$("#save_roomId").val(),0);
         }
    })

    //搜索
    $(".search_live").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();
		table.reload("liveRoom_table",{
			where: {
				name : $("#roomName").val()  //搜索的关键字
			},
			page: {
				curr: 1 //重新从第 1 页开始
			}
		})
	$("#roomName").val("");
    });

});
var Live={

	// 新增直播间
	addLiveRoom:function(){
		$("#liveRoom_div").hide();
		$("#addLiveRoom").show();
		$("#liveRoomName").val("");
        $("#liveRoomNotic").val("");
	},
	commit_addLiveRoom:function(){
		if($("#liveRoomName").val()==""){
			layer.alert("直播间名称不能为空");
			return ;
		}else if($("#liveRoomNotic").val()==""){
			layer.alert("直播间说明不能为空");
			return ;
		}
		Common.invoke({
			url:request('/console/saveNewLiveRoom'),
			data:{
				userId:localStorage.getItem("account"),
				name:$("#liveRoomName").val(),
				notice:$("#liveRoomNotic").val()
			},
			success:function(result){
				if(result.resultCode == 1){
					layer.alert("添加直播间成功");
					$("#addLiveRoom").hide();
					$("#liveRoom_div").show();
                    /*layui.table.reload("liveRoom_table",{
                        page: {
                            curr: 1 //重新从第 1 页开始
                        },
                        where: {
                        }
                    })*/
					Common.tableReload(currentCount,currentPageIndex,1,"liveRoom_table");
				}else if(result.resultCode == 0){
					layer.alert(result.resultMsg);
				}
			},

		})
	},


	// 锁定解锁直播间
	deleteLiveRoom:function(id,currentState){
        var confMsg,successMsg="";
        (currentState == 1 ? confMsg = '确定锁定该直播间？':confMsg = '确定解锁该直播间？');
        (currentState == 1 ? successMsg = "锁定成功":successMsg ="解锁成功");
		layer.confirm(confMsg,{icon:3, title:'提示信息'},function(index){
			Common.invoke({
	            url:request('/console/operationLiveRoom'),
				data:{
					liveRoomId:id,
					currentState:currentState
				},
				successMsg : successMsg,
				errorMsg :  "加载数据失败，请稍后重试",
				success : function(result) {
					layui.table.reload("liveRoom_table")
				},
				error : function(result) {
				}

		})
		})
		
	},

	// 踢出直播间成员
	deleteRoomUser:function(userId,roomId,obj){
		layer.confirm('确定踢出该成员？',{icon:3, title:'提示信息'},function(index){
			Common.invoke({
				url:request('/console/deleteRoomUser'),
				data:{
					userId:userId,
					liveRoomId:roomId
				},
				success:function(result){
					if(result.resultCode==1){
						layer.alert("踢出成功");
                        obj.del();
                        layui.table.reload("liveRoomMember_table")
					}
				}
			})

		});
		
	},
	// 用户禁言
	shutup:function(userId,roomId,state){
		var confMsg,successMsg="";
		(state == 0 ? confMsg = '确定取消禁言该成员？':confMsg = '确定禁言该成员？');
		(state == 0 ? successMsg = "取消禁言成功":successMsg ="禁言成功");
		layer.confirm(confMsg,{icon:3, title:'提示信息'},function(index){

            Common.invoke({
                url : request('/console/shutup'),
                data : {
                    'userId':userId,
                    'state':state,
                    'roomId':roomId
                },
                successMsg : successMsg,
                errorMsg :  "加载数据失败，请稍后重试",
                success : function(result) {
                    layui.table.reload("liveRoomMember_table",{

                    })
                },
                error : function(result) {

                }
            });

		})

	},
	btn_back:function(){
		$("#liveRoom_div").show();
		$("#liveRoomList").show();
		$("#addLiveRoom").hide();
		$("#liveRoomMsg").hide();
		$("#liveRoomUser").hide();
		$("#liveRoomGiftWater").hide();
	}

}