var page=0;
var sum=0;
var IS_ADMIN=0;
var telephone="";
var UI={
	// 加载页面公共方法
	load_html:function(url){
		var html="<iframe frameborder='0' width='99.9%'' height='99.9%' src='"+url+"'></iframe>";
		$("#index_body").empty();
		$("#index_body").append(html);
		
	},
	// 查询消息列表
	findMsgList:function(e){
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
			url:request('/console/chat_logs_all'),
			data:{
				pageIndex:page,
			},
			success:function(result){
				sum=result.data.pageSize;
				if(result.data.pageSize!=0){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].sender+"</td><td>"+result.data.pageData[i].sender_nickname
						+"</td><td>"+result.data.pageData[i].receiver+"</td><td>"+result.data.pageData[i].receiver_nickname
						+"</td><td>"+UI.getLocalTime(result.data.pageData[i].timeSend)+"</td><td>"+result.data.pageData[i].content+"</td></tr>";
					}
					$("#message_table").empty();
					$("#message_table").append(html);
				}
			}
		});
	},
	// 时间转换
	getLocalTime:function(time){
		 var date = new Date(time * 1000);//时间戳为10位需*1000，时间戳为13位的话不需乘1000
        var Y = date.getFullYear() + '-';
        var M = (date.getMonth()+1 < 10 ? '0'+(date.getMonth()+1) : date.getMonth()+1) + '-';
        var D = date.getDate() + ' ';
        var h = date.getHours() + ':';
        var m = (date.getMinutes()<10?'0'+(date.getMinutes()):date.getMinutes()) + ':';
        var s = (date.getSeconds()<10?'0'+(date.getSeconds()):date.getSeconds());
        return Y+M+D+h+m+s;
	},
	// 提交修改config
	commit_config:function(){
		Common.invoke({
			url:request('/config/set'),
			data:{
				XMPPDomain:$("#XMPPDomain").val(),
				liveUrl:$("#liveUrl").val(),
				apiUrl:$("#apiUrl").val(),
				downloadAvatarUrl:$("#downloadAvatarUrl").val(),
				downloadUrl:$("#downloadUrl").val(),
				uploadUrl:$("#uploadUrl").val(),
				freeswitch:$("#freeswitch").val(),
				jitsiServer:$("#jitsiServer").val(),
				displayRedPacket:$("#displayRedPacket").val(),
				fileValidTime:$("#fileValidTime").val(),
				chatRecordTimeOut:$("#chatRecordTimeOut").val(),
				closeTelephoneFind:$("#closeTelephoneFind").val(),
				telephoneLogin:$("#telephoneLogin").val(),
				userIdLogin:$("#userIdLogin").val(),
				showContactsUser:$("#showContactsUser").val(),
				androidVersion:$("#androidVersion").val(),
				androidAppUrl:$("#androidAppUrl").val(),
				androidExplain:$("#androidExplain").val(),
				iosVersion:$("#iosVersion").val(),
				iosAppUrl:$("#iosAppUrl").val(),
				iosExplain:$("#iosExplain").val()
			},
			success:function(result){
				UI.load_html("system_config.html");
			}
		});
	},
	// 红包列表
	redEnveiope_list:function(){
		var html="";
		Common.invoke({
			url:request('/console/redPacketList'),
			data:{
				pageIndex:0
			},
			success:function(result){
				var status="";

				if(result.data.pageData.length!=0){
					for(var i=0;i<result.data.pageData.length;i++){
						if(result.data.pageData[i].status==1){
							status="发出";
						}else if(result.data.pageData[i].status==2){
							status="已领完";
						}else if(result.data.pageData[i].status==-1){
							status="已退款";
						}else if(result.data.pageData[i].status==3){
							status="未领完退款";
						}

						html+="<tr><td>"+result.data.pageData[i].userId+"</td><td>"
						+result.data.pageData[i].userName+"</td><td>"+result.data.pageData[i].money
						+"</td><td>"+UI.getLocalTime(result.data.pageData[i].sendTime)+"</td><td>"+status+"</td></tr>";
					}
					$("#redEnvelope_table").empty();
					$("#redEnvelope_table").append(html);
				}
			}
		})
	},
	// 用户列表
	user_list:function(e){
		var html="";
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
			url:request('/console/userList'),
			data:{
				pageIndex:page
			},
			success:function(result){
				if(result.data.pageData.length!=0){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].userId+"</td><td>"
						+result.data.pageData[i].nickname+"</td><td>"+result.data.pageData[i].createTime
						+"</td><td>"+result.data.pageData[i].onlinestate+"</td><td style='width:25%'><button onclick='UI.deleteUser(\""+result.data.pageData[i].userId+"\")' class='layui-btn'>删除</button><button onclick='UI.updateUser(\""+result.data.pageData[i].userId+"\")' class='layui-btn'>修改</button><button class='layui-btn'>重置密码</button></td></tr>";
					}

					$("#userList_table").empty();
					$("#userList_table").append(html);
				}

			}
		})
	},
	// 搜索用户
	findUserByname:function(){
		var html="";
		Common.invoke({
			url:request('/console/userList'),
			data:{
				keyWorld:$("#nickName").val(),
				onlinestate:$("#status").val()
			},
			success:function(result){
				if(result.data.pageData.length!=0){
					$("#userList_table").empty();
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].nickname
					+"</td><td>"+result.data.pageData[i].createTime+"</td><td>"+result.data.pageData[i].onlinestate
					+"</td><td style='width:25%'><button class='layui-btn'>删除</button><button onclick='UI.updateUser(\""+result.data.pageData[i].userId+"\")' class='layui-btn'>修改</button><button class='layui-btn'>重置密码</button></td></tr>";
					}
				}
				$("#userList_table").empty();
				$("#userList_table").append(html);
				$("#nickName").val("");
				$("#status").val("");
			}
		});
	},

	//新增提示消息
    addErrorMessage:function(){
    	$(".updateBtn").hide();
    	$("#errorMessageList").hide();
		$("#addErrorMessage").show();
	},
	//  新增用户
	addUser:function(){
		$("#userList").hide();
		$("#addUser").show();
		$("#userId").val(0);
		// $("#new").show();
		// $("#update").hide();
	},
	// 提交新增用户
	commit_addUser:function(){
		if($("#userName").val()==""){

			alert("请输入必填参数");
			return;
		}else if($("#telephone").val()==""){
			alert("请输入必填参数");
			return;
		}else if($("#password").val()==""){
			alert("请输入必填参数");
			return ;
		}
		Common.invoke({
			url:request('/console/updateUser'),
			data:{
				userId:$("#userId").val(),
				nickname:$("#userName").val(),
				telephone:$("#telephone").val(),
				password:$("#password").val(),
				userType:$("#isPublic").val()
			},
			success:function(result){
				if(result.resultCode){
					layer.alert("修改成功");
				}
				
			}
		})
	},

	//新增提示消息时校验code是否重复
	onblurCode:function(){
		$.ajax({
			type:"POST",
			url:request("/console/messageList"),
			data:{
				keyword:$("#codeNum").val()
			},
			success:function(result){
				checkRequst(result);
				if(result.resultCode == 1){
					layer.alert("您输入的code已存在");
				}
			}
		})
	},
	//新增提示消息
	commit_errorMessage:function(){
		if($("#codeNum").val()==""){
			layer.alert("请输入必填参数");
			return;
		}else if($("#type").val()==""){
			layer.alert("请输入必填参数");
			return;
		}else if($("#zh").val()==""){
			layer.alert("请输入必填参数");
			return ;
		}else if($("#en").val()==""){
			layer.alert("请输入必填参数");
			return ;
		}
		$.ajax({
			type:"POST",
			url:request("/console/saveErrorMessage"),
			data:{
				code:$("#codeNum").val(),
				type:$("#type").val(),
				zh:$("#zh").val(),
				en:$("#en").val(),
				big5:($("#big5").val()==null?null:$("#big5").val())
			},
			success:function(result){
				checkRequst(result);
				if(result.resultCode == 1){
					layer.alert("新增成功");
					UI.promptList(0);
					$("#errorMessageList").show();
					$("#addErrorMessage").hide();
					$(".info").val("");
					$(".updateBtn").show();
				}
			}
		})
	},
	
	
	// 修改用户
	updateUser:function(userId){
		Common.invoke({
			url:request('/console/getUpdateUser'),
			data:{
				userId:userId
			},
			success:function(result){
				if(result.data!=null){
					$("#userId").val(result.data.userId);
					$("#userName").val(result.data.nickname);
					$("#telephone").val(result.data.phone);
					$("#password").val(result.data.password);
					$("#isPublic").val(result.data.userType);
				}
				$("#userList").hide();
				$("#addUser").show();
				// $("#new").hide();
				// $("#update").show();
			}
		});
	},
	// 删除用户
	deleteUser:function(userId){
		Common.invoke({
			url:request('/console/deleteUser'),
			data:{
				userId:userId
			},
			success:function(result){
				if(result.resultCode==1){
					layer.alert("删除成功");
					UI.user_list(0);
				}
			}
		})
	},
	//搜索消息
	findMessage:function(e){
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
			url:request('/console/chat_logs_all'),
			data:{
				sender:$("#sender").val(),
				receiver:$("#receiver").val(),
				pageIndex:page
			},
			success:function(result){
				sum=result.data.pageSize;
				if(result.data.pageSize!=0){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].sender+"</td><td>"+result.data.pageData[i].sender_nickname
						+"</td><td>"+result.data.pageData[i].receiver+"</td><td>"+result.data.pageData[i].receiver_nickname
						+"</td><td>"+UI.getLocalTime(result.data.pageData[i].timeSend)+"</td><td>"+result.data.pageData[i].content+"</td></tr>";
					}
					$("#message_table").empty();
					$("#message_table").append(html);
				}
			}
		});
	},
	// 清空消息
	deleteMsg:function(){

		Common.invoke({
			url:request('/console/chat_logs_all/del'),
			data:{
				sender:$("#sender").val(),
				receiver:$("#receiver").val()
			},
			success:function(result){
				if(result.resultCode==1){
					alert("success");
				}
			}
		})
	},
	// 群组列表
	roomList:function(e){
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
		}else{
			page=0;
		}
		Common.invoke({
			url:request('/console/roomList'),
			data:{
				pageIndex:page
			},
			success:function(result){
				if(result.data!=null){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].name+"</td><td>"+result.data.pageData[i].desc+"</td><td>"
						+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].nickname+"</td><td>"+result.data.pageData[i].createTime
						+"</td><td><a onclick='UI.deleteRoom(\""+result.data.pageData[i]._id+"\")' style='margin-right:10px'>删除</a><a onclick='UI.findRoomMsg(\""+result.data.pageData[i]._id+"\")' style='margin-right:10px'>聊天记录</a><a onclick='UI.roomUserList(\""+result.data.pageData[i]._id+"\")' style='margin-right:10px'>人员管理</a><a style='margin-right:10px'>添加随机用户</a><a style='margin-right:10px'>修改配置</a><a style='margin-right:10px'>消息统计</a></td></tr>";
					}
					$("#roomList_table").empty();
					$("#roomList_table").append(html);
				}
			}
		});
	},
	// 搜索群组
	findRoomByName:function(e){
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
			url:request('/console/roomList'),
			data:{
				roomName:$("#roomName").val(),
				pageIndex:page
			},
			success:function(result){
				if(result.data!=null){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].name+"</td><td>"+result.data.pageData[i].desc+"</td><td>"
						+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].nickname+"</td><td>"+result.data.pageData[i].createTime
						+"</td><td><a onclick='UI.deleteRoom(\""+result.data.pageData[i]._id+"\")' href='javascript:' style='margin-right:10px'>删除</a><a href='' style='margin-right:10px'>聊天记录</a><a style='margin-right:10px'>人员管理</a><a style='margin-right:10px'>添加随机用户</a><a style='margin-right:10px'>修改配置</a><a style='margin-right:10px'>消息统计</a></td></tr>";
					}
					$("#roomList_table").empty();
					$("#roomList_table").append(html);
				}
			}
		});
	},
	// 新增群组
	addRoom:function(){
		$("#roomList").hide();
		$("#roomMsgList").hide();
		$("#addRoom").show();
	},
	// 提交新增群组
	commit_addRoom:function(){
		Common.invoke({
			url:request('/console/addRoom'),
			data:{
				userId:10005,
				name:$("#add_roomName").val(),
				desc:$("#add_desc").val()
			},
			success:function(result){
				if(result.resultCode==1){
					layer.alert("新增成功");
					UI.roomList(0);

					$("#roomList").show();
					$("#roomMsgList").hide();
					$("#addRoom").hide();
				}
			}
		});
	},
	// 删除群组
	deleteRoom:function(id){
		var html="";
		Common.invoke({
			url:request('/console/deleteRoom'),
			data:{
				roomId:id
			},
			success:function(result){
				if(result.resultCode==1){
					layer.alert("删除成功");
					UI.roomList(0);
				}
			}
		})
	},
	// 查看群组聊天记录
	findRoomMsg:function(jid){
		html="";
		Common.invoke({
			url:request('/console/groupchat_logs_all'),
			data:{
				room_jid_id:jid
			},
			success:function(result){
				if(result.data!=null){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].room_jid_id+"</td><td>"+result.data.pageData[i].sender
						+"</td><td>"+result.data.pageData[i].fromUserName+"</td><td>"+result.data.pageData[i].timeSend+"</td><td>"
						+result.data.pageData[i].context+"</td></tr>";
					}
					$("#room_table").hide();
					$("#addRoom").hide();
					$("#roomMsgList").show();
					$("#roomMsg_table").empty();
					$("#roomMsg_table").append(html);
				}
			}
		})
	},
	// 群成员管理
	roomUserList:function(roomId){
		html="";
		Common.invoke({
			url:request('/console//roomUserManager'),
			data:{
				id:roomId
			},
			success:function(result){
				if(result.data.pageData!=null){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].nickname+"</td><td>"+result.data.pageData[i].role+"</td><td>"+result.data.pageData[i].offlineNoPushMsg+"</td><td>"+result.data.pageData[i].createTime+"</td><td><a>删除</a><a>禁言</a><a>...</a></td></tr>";
					}
					$("#roomUser_table").empty();
					$("#roomUser_table").append(html);
					$("#room_table").hide();
					$("#roomMsgList").hide();
					$("#roomUserList").show();
				}
			}
		})
	},
	// 直播间列表
	liveRoomList:function(e){
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
			url:request('/console/liveRoomList'),
			data:{
				pageIndex:page
			},
			success:function(result){
				if(result.data.pageData!=null){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].name+"</td><td>"+result.data.pageData[i].notice
						+"</td><td>"+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].nickName
						+"</td><td>"+result.data.pageData[i].url+"</td><td>"+result.data.pageData[i].createTime+"</td><td><a>删除</a><a>聊天记录</a><a>禁止送礼<a><a>禁播</a><a>人员管理</a></td></tr>";
					}
					$("#liveRoom_table").empty();
					$("#liveRoom_table").append(html);
				}
			}
		})
	},
	// 礼物列表
	giftList:function(e){
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
			url:request('/console/giftList'),
			data:{
				pageIndex:page
			},
			success:function(result){
				if(result.data.pageData!=null){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].name+"</td><td>"+result.data.pageData[i].photo
						+"</td><td>"+result.data.pageData[i].price+"</td><td>"+result.data.pageData[i].type
						+"</td><td onclick='UI.deleteGift(\""+result.data.pageData[i].giftId+"\")'>删除</td></tr>";
					}
					$("#giftList_table").empty();
					$("#giftList_table").append(html);
				}
			}
		})
	},
	// 删除礼物
	deleteGift:function(giftId){
		Common.invoke({
			url:request('/console/delete/gift'),
			data:{
				giftId:giftId
			},
			success:function(result){
				if(result.resultCode==1){
					UI.giftList(0);
				}
			}
		})
	},
	// 举报列表
	complaint:function(e){
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
			url:request('/console/beReport'),
			data:{
				pageIndex:page
			},
			success:function(result){
				if(result.data.pageData!=null){
					
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr align='center'><td>"+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].toUserId
						+"</td><td>"+result.data.pageData[i].info+"</td><td>"+result.data.pageData[i].time
						+"</td><td><button class='layui-btn'>删除</button></td></tr>";
					}
					
					$("#complaint_table").empty();
					$("#complaint_table").append(html);
				}

			}
		})
	},
	// 举报切换事件
	changeSelect:function(){
		if($("#complaint_select").val()==0){
			layer.alert("人");
			UI.complaint();
		}else if($("#complaint_select").val()==1){
			layer.alert("群组");
			UI.complaint();
		}

	},
	// 删除敏感词
	deleteKeyWord:function(id){
		Common.invoke({
			url:request('/console/deletekeyword'),
			data:{
				id:id
			},
			success:function(result){
				if(result.resultCode==1){
					layer.alert("删除成功");
					UI.keyword_list(0);
				}
			}
		})
	},
	// 新增敏感词
	addKeyWord:function(){
		$("#keyWordList").hide();
		$("#addKeyWord").show();
	},
	// 提交新增敏感词
	commit_keyWord:function(){ 
		Common.invoke({
			url:request('/console/addkeyword'),
			data:{
				word:$("#addKeyValue").val()
			},
			success:function(result){
				if(result.resultCode==1){
					UI.keyword_list(0);
					$("#keyWordList").show();
					$("#addKeyWord").hide();
				}
			}
		})
	},
	// 搜索关键词
	findKeyWord:function(){
		html="";
		Common.invoke({
			url:request('/console/keywordfilter'),
			data:{
				word:$("#keyName").val()
			},
			success:function(result){
				if(result.data.pageData!=null){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td align='center'>"+result.data.pageData[i].word+"</td><td align='center'><button onclick='UI.deleteKeyWord(\""+result.data.pageData[i]._id+"\")' class='layui-btn'>删除</button></td></tr>"
					}
					$("#keywordList_table").empty();
					$("#keywordList_table").append(html);
					$("#keyName").val("");
				}
			}
		});
	},
	// 敏感词列表
	keyword_list:function(e){
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
			url:request('/console/keywordfilter'),
			data:{
				pageIndex:page
			},
			success:function(result){
				if(result.data.pageData!=null){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td align='center'>"+result.data.pageData[i].word+"</td><td align='center'><button onclick='UI.deleteKeyWord(\""+result.data.pageData[i]._id+"\")' class='layui-btn'>删除</button></td></tr>"
					}
					$("#keywordList_table").empty();
					$("#keywordList_table").append(html);
				}
			}
		})
	},
	
	//提示消息管理
	promptList:function(e){
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
		$.ajax({
			type:"POST",
			url:request("/console/messageList"),
			data:{
				pageIndex:0,
				keyword:($("#code").val() == null ? null : $("#code").val()),
			},
			success:function(result){
				checkRequst(result);
				if(result.data.pageData!=null){
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].code+"</td><td>"+result.data.pageData[i].type
						+"</td><td>"+result.data.pageData[i].zh+"</td><td width='50px'>"+result.data.pageData[i].en
						+"</td><td>"+(result.data.pageData[i].big5==null?"":result.data.pageData[i].big5)+"</td><td align='center' width='150px'><button class='layui-btn' onclick='UI.deleteErrorMessage(\""+result.data.pageData[i].code+"\")'>删除</button><button class='layui-btn' onclick='UI.updateErrorMessage(\""+result.data.pageData[i].code+"\")'>修改</button></td></tr>";
					}
					$("#messageList_table").empty();
					$("#messageList_table").append(html);
				}
			}
		})
	},

	//删除提示消息
	deleteErrorMessage:function(code){
		$.ajax({
			type:"POST",
			url:request("/console/deleteErrorMessage"),
			data:{
				code:code
			},
			success:function(result){
				checkRequst(result);
				if(result.resultCode==1)
					layer.alert("删除成功");
					UI.promptList(0);
			}
		})
	},
	
	//查询单条提示消息
	updateErrorMessage:function(code){
		$(".insertBtn").hide();
		$.ajax({
			type:"POST",
			url:request('/console/messageList'),
			data:{
				keyword:code
			},
			success:function(result){
				checkRequst(result);
				if(result.data!=null){
					for(var i = 0; i < result.data.pageData.length; i++){
						$("#_id").val(result.data.pageData[i].id);
						$("#codeNum").val(result.data.pageData[i].code);
						$("#type").val(result.data.pageData[i].type);
						$("#zh").val(result.data.pageData[i].zh);
						$("#en").val(result.data.pageData[i].en);
						$("#big5").val(result.data.pageData[i].big5);
					}
				}
				$("#errorMessageList").hide();
				$("#addErrorMessage").show();
			}
		});
	},
	
	//修改提示消息
	update_errorMessage:function(){
		$.ajax({
			type:"POST",
			url:request("/console/messageUpdate"),
			data:{
				id:$("#_id").val(),
				code:$("#codeNum").val(),
				type:$("#type").val(),
				zh:$("#zh").val(),
				en:$("#en").val(),
				big5:$("#big5").val()
			},
			success:function(result){
				checkRequst(result);
				if(result.data!=null){
					layer.alert("修改提示消息成功");
					UI.promptList(0);
					$("#addErrorMessage").hide();
					$("#errorMessageList").show();
					$(".info").val("");
					$(".insertBtn").show();
				}
			}
		});
	}
	
}