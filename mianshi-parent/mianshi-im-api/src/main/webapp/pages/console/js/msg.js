var page=0;
var sum=0;
var lock=0;
var msgIds=new Array();
var commentIds = new Array();
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
    var tableIns = table.render({
      elem: '#msg_table'
      ,url:request("/console/getFriendsMsgList")
      ,id: 'msg_table'
      ,toolbar: '#toolbarDemo'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {type:'checkbox',fixed:'left'}// 多选
          ,{field: 'msgId', title: '消息Id',sort: true,width:150}
          ,{field: 'userId', title: '发送人Id',sort: true,width:150}
          ,{field: 'nickname', title: '发送人昵称',sort: true,width:150}
          ,{field: 'body', title: '内容',sort: true,width:180,templet:function (d) {
          		// console.log("======> 数据 "+JSON.stringify(d.body));
				return JSON.stringify(d.body);
          }}
          ,{field: 'location', title: '地址',sort: true, width:120}
          ,{field: 'model', title: '手机型号',sort: true, width:120}
          ,{field: 'visible', title: '可见类型',sort: true, width:120,templet:function (d) {
          			var visiMsg;
					(d.visible == 1 ? visiMsg = "公开" : (d.visible == 2) ? visiMsg = "私密" : (d.visible == 3) ? visiMsg = "部分可见"
						:(d.visible == 4) ? visiMsg = "不给指定的人看" : (d.visible == 5 ) ? visiMsg = "@提示专属人看" : visiMsg = "暂无类型");
					return visiMsg;
                }}
          ,{field: 'time',title:'发送时间',width:195,templet: function(d){
          		return UI.getLocalTime(d.time);
          }}
          ,{fixed: 'right', width: 400,title:"操作", align:'left', toolbar: '#msgListBar'}
        ]]
		,done:function(res, curr, count){
            checkRequst(res);
          if(count==0&&lock==1){
                // layui.layer.alert("暂无数据",{yes:function(){
                //   renderTable();
                //   layui.layer.closeAll();
                // }});
                layer.msg("暂无数据",{"icon":2});
                renderTable();
              }
              lock=0;
			if(localStorage.getItem("role")==1 || localStorage.getItem("role")==4 || localStorage.getItem("role")==7){
				$(".delete").hide();
				$(".praiseMsg").hide();
				$(".commonMsg").hide();
				$(".shutup").hide();
				$(".cancelShutup").hide();
				$(".locking").hide();
				$(".unlock").hide();
				$(".deleteCommon").hide();
				$(".checkDelete").hide();

			}
            var pageIndex = tableIns.config.page.curr;//获取当前页码
            var resCount = res.count;// 获取table总条数
            currentCount = resCount;
            currentPageIndex = pageIndex;
		}
    });

    //朋友圈列表操作
    table.on('tool(msg_table)', function(obj){
         var layEvent = obj.event,
            data = obj.data;
            // console.log(data);
        if(layEvent === 'delete'){ //删除朋友圈
			// Msg.deleteFriendsMsg(data.userId,data.msgId,obj);
			Msg.checkDeleteFriendsMsg(data.msgId,1);
        }else if(layEvent === 'shutup') {// 锁定
			Msg.shutup(data.msgId,1);
        }else if(layEvent === 'cancelShutup'){// 取消锁定
        	Msg.shutup(data.msgId,0);
		}else if(layEvent === 'locking'){// 锁定该用户
            Msg.lockIng(data.userId,-1)
        }else if(layEvent === 'unlock'){// 解锁该用户
            Msg.lockIng(data.userId,1)
        }else if(layEvent === 'commonMsg'){ //评论详情
        	var tableInsCommon = table.render({
                  elem: '#commentMsg_table'
                  ,toolbar:'#toolbardeleteComment'
			      ,url:request("/console/commonListMsg")+"&msgId="+data.msgId
			      ,id: 'commentMsg_table'
			      ,page: true
			      ,curr: 0
                  ,limit:Common.limit
                  ,limits:Common.limits
			      ,groups: 7
			      ,cols: [[ //表头
                      {type:'checkbox',fixed:'left'}// 多选
			          ,{field: 'commentId', title: '评论Id',sort: true, width:220}
			          ,{field: 'msgId', title: '所属消息Id',sort: true, width:200}
                      ,{field: 'nickname', title: '评论用户昵称',sort: true, width:200}
                      ,{field: 'body', title: '评论内容',sort: true, width:200}
                      ,{field: 'toNickname', title: '被回复用户昵称',sort: true, width:200}
                      ,{field: 'toBody', title: '回复内容',sort: true, width:200}
			          ,{field: 'time', title: '评论时间',sort: true, width:220,templet: function(d){
			          		return UI.getLocalTime(d.time);
			          }}
			          ,{fixed: 'right', width: 240,title:"操作", align:'left', toolbar: '#deleteCommonMsg'}
			        ]]
					,done:function(res, curr, count){
                        checkRequst(res);
                    	$("#friendMsgList").hide();
                    	$("#commonMsg").show();
						$("#save_roomId").val(data.msgId);
                        var pageIndex = tableInsCommon.config.page.curr;//获取当前页码
                        var resCount = res.count;// 获取table总条数
                        currentCount = resCount;
                        currentPageIndex = pageIndex;
					}
			    });
        }else if(layEvent === 'praiseMsg'){// 点赞列表
        	var tableIns1 = table.render({
			      elem: '#praiseMsg_table'
			      ,url:request("/console/praiseListMsg")+"&msgId="+data.msgId
			      ,id: 'praiseMsg_table'
			      ,page: true
			      ,curr: 0
                  ,limit:Common.limit
                  ,limits:Common.limits
			      ,groups: 7
			      ,cols: [[ //表头
			           {field: 'praiseId', title: '点赞Id',sort: true, width:220}
			          ,{field: 'msgId', title: '所属消息Id',sort: true, width:200}
                ,{field: 'userId', title: '点赞用户Id',sort: true, width:200}
			          ,{field: 'nickname', title: '点赞用户昵称',sort: true, width:220}
			          ,{field: 'time', title: '点赞时间',sort: true, width:400,templet: function(d){
			          		return UI.getLocalTime(d.time);
			          }},
					]]
					,done:function(res, curr, count){
                        checkRequst(res);
						$("#friendMsgList").hide();
						$("#praiseMsg").show();
						$("#save_roomId").val(data.msgId);
					}
			    });
        }
      });


    //评论列表操作
    table.on('tool(commentMsg_table)', function(obj){
         var layEvent = obj.event,
            data = obj.data;
         if(layEvent === 'deleteCommon'){ // 删除评论
         	Msg.checkDeleteCommentImpl(data.msgId,data.commentId,1);
         }
    })


    //头部搜索
    $(".search_live").on("click",function(){
            
            table.reload("msg_table",{
                where: {
                    nickname : $("#nickName").val(),  //搜索的关键字
					          userId : $("#userId").val()
                },
                page: {
                    curr: 1 //重新从第 1 页开始
                }
            })
            lock=1;
        $("#nickName").val("");
        $("#userId").val("");
    });

});

//重新渲染表单
function renderTable(){
  layui.use('table', function(){
   var table = layui.table;//高版本建议把括号去掉，有的低版本，需要加()
   // table.reload("user_list");
   table.reload("msg_table",{
        page: {
            curr: 1 //重新从第 1 页开始
        },
        where: {
            nickname : $("#nickName").val(),  //搜索的关键字
            userId : $("#userId").val()
        }
    })
  });
 }

var Msg={
    // 朋友圈多选删除
    checkDeleteMsg:function(){

        var checkStatus = layui.table.checkStatus('msg_table'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+checkStatus.data) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选
        for (var i = 0; i < checkStatus.data.length; i++){
            msgIds.push(checkStatus.data[i].msgId);
        }
        console.log(msgIds);
        if(0 == checkStatus.data.length){
            layer.msg("请勾选要删除的行");
            return;
        }
        Msg.checkDeleteFriendsMsg(msgIds.join(","),checkStatus.data.length);
    },

    // 多选删除朋友圈
    checkDeleteFriendsMsg:function(msgId,checkLength){
        layer.confirm('确定删除指定的朋友圈？',{icon:3, title:'提示信息',yes:function (index) {
                Common.invoke({
                    url:request('/console/deleteFriendsMsg'),
                    data:{
                        messageId:msgId
                    },
                    success:function(result){
                        if(result.resultCode==1){
                            layer.msg("删除成功",{"icon":1});
                        }
                        // 刷新
                        Common.tableReload(currentCount,currentPageIndex,checkLength,"msg_table");
                        // layui.table.reload("msg_table")
                    }
                })
            },btn2:function (index, layero) {
                msgIds =[];
            },cancel:function () {
                msgIds =[];
            }
            })

    },

    // 朋友圈评论多选删除
    checkDeleteComment:function(){

        var checkStatus = layui.table.checkStatus('commentMsg_table'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+JSON.stringify(checkStatus.data)) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选

        var msgId;
        for (var i = 0; i < checkStatus.data.length; i++){
            commentIds.push(checkStatus.data[i].commentId);
            msgId = checkStatus.data[i].msgId;
        }
        console.log(commentIds);
        if(0 == checkStatus.data.length){
            layer.msg("请勾选要删除的行");
            return;
        }
        Msg.checkDeleteCommentImpl(msgId,commentIds.join(","),checkStatus.data.length);
    },

    checkDeleteCommentImpl : function(msgId,msgCommentIds,checkLength){
        console.log("参数 msgId："+msgId +"     "+msgCommentIds)
        layer.confirm('确定删除指定的朋友圈评论？',{icon:3, title:'提示信息',yes:function (index) {
                Common.invoke({
                    url:request('/console/comment/delete'),
                    data:{
                        messageId:msgId,
                        commentId:msgCommentIds
                    },
                    success:function(result){
                        if(result.resultCode==1){
                            layer.msg("删除成功",{"icon":1});
                        }
                        // 刷新
                        Common.tableReload(currentCount,currentPageIndex,checkLength,"commentMsg_table");
                        // layui.table.reload("commentMsg_table")
                    }
                })
        },btn2:function (index, layero) {
                commentIds=[];
            },cancel:function () {
                commentIds=[];
            }
        })
    },

    // 删除评论
    deleteCommonMsg:function(msgId,commentId,obj){
        layer.confirm('确定删除该条评论？',{icon:3, title:'提示信息'},function(index){
            Common.invoke({
                url:request('/console/comment/delete'),
                data:{
                    messageId:msgId,
                    commentId:commentId
				},
                success:function(result){
                    if(result.resultCode==1){
                        layer.msg("删除成功",{"icon":1});
                    }
                    obj.del();
                    layui.table.reload("commentMsg_table")
                }
            })
        })

    },

	// 朋友圈锁定取消
	shutup:function(msgId,state){
		var confMsg,successMsg="";
		(state == 0 ? confMsg = '确定解锁该朋友圈？':confMsg = '确定锁定该朋友圈？');
		(state == 0 ? successMsg = "解锁成功":successMsg ="锁定成功");
		layer.confirm(confMsg,{icon:3, title:'提示信息'},function(index){

            Common.invoke({
                url : request('/console/lockingMsg'),
                data : {
					msgId:msgId,
                    state:state
                },
                successMsg : successMsg,
                errorMsg :  "加载数据失败，请稍后重试",
                success : function(result) {
                    layui.table.reload("msg_table",{
                    })
                },
                error : function(result) {
                }
            });

		})
	},
	// 朋友圈锁定改用户
    lockIng:function(userId,status){
        var confMsg,successMsg="";
        (status == -1 ? confMsg = '确定锁定该用户？':confMsg = '确定解锁该用户？');
        (status == -1 ? successMsg = "锁定成功":successMsg ="解锁成功");
        layer.confirm(confMsg,{icon:3, title:'提示信息'},function(index){

            Common.invoke({
                url : request('/console/changeStatus'),
                data : {
                    userId:userId,
                    status:status
                },
                successMsg : successMsg,
                errorMsg :  "加载数据失败，请稍后重试",
                success : function(result) {
                    layui.table.reload("msg_table",{

                    })
                },
                error : function(result) {
                }
            });
        })
    },

	btn_back:function(){
		$("#friendMsg_div").show();
		$("#friendMsgList").show();
		$("#commonMsg").hide();
		$("#praiseMsg").hide();
		/*$("#liveRoomUser").hide();
		$("#liveRoomGiftWater").hide();*/
	}

}