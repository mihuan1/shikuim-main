var page=0;
var html="";
var messageIds = new Array();
var currentPageIndex;// 当前页码数
var currentCount;// 当前总数
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

    /*//非管理员登录屏蔽操作按钮
    if(localStorage.getItem("IS_ADMIN")==0){
    	$(".msg_btn_div").empty();
    }*/

    //群组列表
    var tableIns = table.render({

      elem: '#message_table'
      ,toolbar: '#toolbarMessageList'
      ,url:request("/console/chat_logs_all")
      ,id: 'message_table'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
            {type:'checkbox',fixed:'left'}// 多选
          ,{field: 'sender', title: '发送者Id',sort: true,width:120}
          ,{field: 'sender_nickname', title: '发送者',sort: true,width:150}
          ,{field: 'receiver', title: '接收者Id', sort: true, width:120}
          ,{field: 'receiver_nickname', title: '接收者',sort: true, width:150}
          ,{field: 'contentType', title: '消息类型',sort: true, width:110,templet : function (d) {
					return Common.msgType(d.contentType);
                }}
          ,{field: 'timeSend',title:'时间',sort: true, width:200,templet: function(d){
          		return UI.getLocalTime(d.timeSend);
          }}
          ,{field: 'content', title: '内容',sort: true, width:380,templet : function (d) {
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
                                if(text.search("https") != -1||text.search("http")!=-1){
                                    var link = "<a target='_blank' href=\""+text+"\">"+text+"</a>";
                                    return link;
                                }else{
                                    return text;
                                }
                            }catch (e) {
                                return text;
                            }
                        }
					}else {
          				return "";
					}
                }}
          // ,{field: 'receiver_nickname', title: '接收者',sort: true, width:150}
          ,{fixed: 'right', width: 100,title:"操作", align:'left', toolbar: '#messageListBar'}
        ]]
		,done:function(res, curr, count){
      		// 非超级管理员处理
      if(localStorage.getItem("role")==1 || localStorage.getItem("role")==4){
				$(".checkDeleteUsersFriends").hide();
				$(".del_msg").remove();
				$(".delete").remove();
        $(".deleteMonthLogs").hide();
        $(".deleteThousandAgoLogs").hide();
			}
        var pageIndex = tableIns.config.page.curr;//获取当前页码
        var resCount = res.count;// 获取table总条数
        currentCount = resCount;
        currentPageIndex = pageIndex;
		}
    });

    //列表操作
    table.on('tool(message_table)', function(obj){
        var layEvent = obj.event,
            data = obj.data;
         if(layEvent === 'delete'){ //删除
         	Msg.checkDeleteMessageImpl(data._id,1);
         }
     });

    //搜索
    $(".search_message").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();
        table.reload("message_table",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
                sender : $("#sender").val()  //搜索的关键字
                ,receiver:$("#receiver").val()
                ,keyWord:$("#keyWord").val()
            }
        })

        $("#sender").val("");
        $("#receiver").val("");
        $("#keyWord").val("");

    });
})

var Msg={

	// 查询消息列表
	findMsgList:function(e,pageSize){
		html="";
		if(e==undefined){
			e=0;
		}else if(pageSize==undefined){
			pageSize=10;
		}
		
		$.ajax({
			type:'POST',
			url:request('/console/chat_logs_all'),
			data:{
				pageIndex:(e==0?"0":e-1),
				pageSize:pageSize
			},
			dataType:'json',
			async:false,
			success:function(result){
        checkRequst(result);
				sum=result.data.pageSize;
				if(result.data.pageSize!=0){
					$("#pageCount").val(result.data.allPageCount);
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].sender+"</td><td>"+result.data.pageData[i].sender_nickname
						+"</td><td>"+result.data.pageData[i].receiver+"</td><td>"+result.data.pageData[i].receiver_nickname
						+"</td><td>"+result.data.pageData[i].type+"</td><td>"+UI.getLocalTime(result.data.pageData[i].timeSend)+"</td><td>"+result.data.pageData[i].content+"</td></tr>";
					}
					$("#message_table").empty();
					$("#message_table").append(html);
				}
			}
		});
	},

    // 多选删除
    checkDeleteMessage:function(){
        // 多选操作
        var checkStatus = layui.table.checkStatus('message_table'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+checkStatus.data) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选
        for (var i = 0; i < checkStatus.data.length; i++){
            messageIds.push(checkStatus.data[i]._id);
        }
        console.log(messageIds);
        if(0 == checkStatus.data.length){
            layer.msg("请勾选要删除的行");
            return;
        }
        Msg.checkDeleteMessageImpl(messageIds.join(","),checkStatus.data.length);
    },
    checkDeleteMessageImpl:function(messageId,checkLength){
        layer.confirm('确定删除指定单聊聊天记录',{icon:3, title:'提示消息',yes:function () {
                Common.invoke({
                    url:request('/console/deleteChatMsgs'),
                    data:{
                        msgId :messageId
                    },
                    success:function(result){
                        if(result.resultCode==1){
                            layer.msg("删除成功",{"icon":1});
                            messageIds = [];
                            // 刷新table
                            Common.tableReload(currentCount,currentPageIndex,checkLength,"message_table");
                            // layui.table.reload("message_table");
                        }
                    }
                })
            },btn2:function () {
                messageIds = [];
            },cancel:function () {
                messageIds = [];
            }});
	},

}
	// 删除一个月前的单聊聊天记录
	$(".deleteMonthLogs").on("click",function(){

		layer.confirm('确定删除一个月前的单聊聊天记录？',{icon:3, title:'提示信息'},function(index){

			Common.invoke({
				url : request('/console/deleteChatMsgs'),
				data : {
					'type' : 1
				},
				successMsg : "删除成功",
				errorMsg : "删除失败,请稍后重试",
				success : function(result) {
					if (1 == result.resultCode){
						layui.table.reload("message_table");
					}
				},
				error : function(result) {
				}
			});

		});

	});
	// 删除最近十万条之前的日志
	$(".deleteThousandAgoLogs").on("click",function(){

		layer.confirm('确定删除十万条之前的单聊聊天记录？',{icon:3, title:'提示信息'},function(index){

			Common.invoke({
				url : request('/console/deleteChatMsgs'),
				data : {
					'type' : 2
				},
				successMsg : "删除成功",
				errorMsg : "删除失败,请稍后重试",
				success : function(result) {
					if (1 == result.resultCode){
						layui.table.reload("message_table");
					}
				},
				error : function(result) {
                    layui.layer.alert("数量小于等于100000")
				}
			});

		});

	});


