var i=1;
var sum=0;
var pageIndex=0;
var UI={
	// 首页
	index:function(){
		$("#li_one").css("background-color","#4E5465");
		$("#li_two").css("background-color","#393D49");
		$("#li_three").css("background-color","#393D49");
		$("#li_four").css("background-color","#393D49");
		$("#li_five").css("background-color","#393D49");
		$("#li_six").css("background-color","#393D49");
		$("#li_seven").css("background-color","#393D49");
		$("#index").show();
		$("#index_one").show();
		$("#update_menu").hide();
		$("#pushText").hide();
		$("#pushOneText").hide();
		$("#pushManyText").hide();
		$("#menu").hide();
		$("#msg").hide();
		$("#fan").hide();
		$("#index_newMsg").hide();
		$("#index_add").hide();
		$("#index_userSum").hide();
		$("#pushMsg").hide();
		$("#pageIndex").hide();
		$("#newMsg_item").hide();
		$("#msg_item").hide();
		mpCommon.updateHomeCount();
	},
	// 群发消息
	pushText:function(){
		$("#li_one").css("background-color","#393D49");
		$("#li_two").css("background-color","#4E5465");
		$("#li_three").css("background-color","#393D49");
		$("#li_four").css("background-color","#393D49");
		$("#li_five").css("background-color","#393D49");
		$("#li_six").css("background-color","#393D49");
		$("#li_seven").css("background-color","#393D49");
		$("#index_one").hide();
		$("#update_menu").hide();
		$("#pushText").show();
		$("#pushOneText").hide();
		$("#pushManyText").hide();
		$("#menu").hide();
		$("#msg").hide();
		$("#fan").hide();
		$("#index").hide();
		$("#pushMsg").hide();
		$("#msg_item").hide();
	},
	// 群发单条图文消息
	pushOneText:function(){
		$("#li_one").css("background-color","#393D49");
		$("#li_two").css("background-color","#393D49");
		$("#li_three").css("background-color","#4E5465");
		$("#li_four").css("background-color","#393D49");
		$("#li_five").css("background-color","#393D49");
		$("#li_six").css("background-color","#393D49");
		$("#li_seven").css("background-color","#393D49");
		
		$("#pushOneText").show();
		$("#update_menu").hide();
		$("#index_one").hide();
		$("#pushText").hide();
		$("#pushManyText").hide();
		$("#menu").hide();
		$("#msg").hide();
		$("#fan").hide();
		$("#index").hide();
		$("#pushMsg").hide();
		$("#msg_item").hide();
	},
	// 群发多条图文消息
	pushManyText:function(){
		$("#li_one").css("background-color","#393D49");
		$("#li_two").css("background-color","#393D49");
		$("#li_three").css("background-color","#393D49");
		$("#li_four").css("background-color","#4E5465");
		$("#li_five").css("background-color","#393D49");
		$("#li_six").css("background-color","#393D49");
		$("#li_seven").css("background-color","#393D49");
		
		$("#pushManyText").show();
		$("#update_menu").hide();
		$("#index_one").hide();
		$("#pushText").hide();
		$("#pushOneText").hide();
		$("#menu").hide();
		$("#msg").hide();
		$("#fan").hide();
		$("#index").hide();
		$("#pushMsg").hide();
		$("#msg_item").hide();
	},
	// 自定义菜单
	menu:function(num){
		var html="";
		var body="";
		$("#li_one").css("background-color","#393D49");
		$("#li_two").css("background-color","#393D49");
		$("#li_three").css("background-color","#393D49");
		$("#li_four").css("background-color","#393D49");
		$("#li_five").css("background-color","#4E5465");
		$("#li_six").css("background-color","#393D49");
		$("#li_seven").css("background-color","#393D49");
		$("#menu").show();
		$("#update_menu").hide();
		$("#index_one").hide();
		$("#pushText").hide();
		$("#pushOneText").hide();
		$("#pushManyText").hide();
		$("#msg").hide();
		$("#fan").hide();
		$("#index").hide();
		$("#pushMsg").hide();
		$("#menuPage").show();
		$("#msg_item").hide();
		
		if($("#parentId").val()==0){
			$("#menu_url").hide();
		}else{
			$("#menu_url").show();
		}
		
		/*$.ajax({
			type:'POST',
			url : '/mp/menuList',
			dataType:"json",
			async: false,
			success:function(result){
				if(result.data.length==0){
					//alert("暂无数据");
				}else{

					for(var i=0;i<result.data.length;i++){
                        var url="";
						if(result.data[i].url!=undefined){
							url=result.data[i].url;
						}

						html+="<tr><td>"+result.data[i].id+"</td><td>"+result.data[i].name+"</td><td>"+result.data[i].index
						+"</td><td></td><td>"+url
						+"</td><td><button onclick='UI.deleteMenu(\""+result.data[i].id+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>删除</button><button onclick='UI.updateMenu(\""+result.data[i].id+"\",\""+result.data[i].name+"\",\""+result.data[i].index+"\",\""
						+result.data[i].url+"\",\""+result.data[i].desc+"\",\""+result.data[i].menuId+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>修改</button></td><tr>";
						
						body+="<option value='"+result.data[i].id+"'>"+"二级菜单 - "+result.data[i].name+"</option>";
						
						
						
					}
					if(result.data[0].menuList.length>0){
						for(var j=0;j<result.data[0].menuList.length;j++){
							html+="<tr><td>"+result.data[0].menuList[j].id+"</td><td></td><td>"+result.data[0].menuList[j].index
							+"</td><td>"+result.data[0].menuList[j].name+"</td><td>"+result.data[0].menuList[j].url
							+"</td><td><button onclick='UI.deleteMenu(\""+result.data[0].menuList[j].id+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>删除</button><button onclick='UI.updateMenu(\""+result.data[0].menuList[j].id+"\",\""+result.data[0].menuList[j].name+"\",\""+result.data[0].menuList[j].index+"\",\""
							+result.data[0].menuList[j].url+"\",\""+result.data[0].menuList[j].desc+"\",\""+result.data[0].menuList[j].menuId+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>修改</button></td><tr>";
						}
					}
					
					
					
				}
				$("#menu_td").empty();
				$("#menu_td").append(html);
				
				$("#parentId").empty();
				$("#parentId").append("<option value='0'>一级菜单</option>");
				$("#parentId").append(body);
				$("#update_parentId").empty();
				$("#update_parentId").append("<option value='0'>一级菜单</option>");
				$("#update_parentId").append(body);
				$("#menu_name").val("");
				$("#menu_num").val("");
				$("#menu_mark").val("");
				$("#menu_url").val("");
			}
			
		});*/


		mpCommon.invoke({
			url : '/mp/menuList',
			data : {},
			success : function(result) {
				if(result.data.length==0){
					layui.layer.msg("暂无数据");
				}else{

					for(var i=0;i<result.data.length;i++){
                        var url="";
						if(result.data[i].url!=undefined){
							url=result.data[i].url;
						}

						html+="<tr><td>"+result.data[i].id+"</td><td>"+result.data[i].name+"</td><td>"+result.data[i].index
						+"</td><td></td><td>"+url
						+"</td><td><button onclick='UI.deleteMenu(\""+result.data[i].id+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>删除</button><button onclick='UI.updateMenu(\""+result.data[i].id+"\",\""+result.data[i].name+"\",\""+result.data[i].index+"\",\""
						+result.data[i].url+"\",\""+result.data[i].desc+"\",\""+result.data[i].menuId+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>修改</button></td><tr>";
						
						body+="<option value='"+result.data[i].id+"'>"+"二级菜单 - "+result.data[i].name+"</option>";
						
						
						
					}
					if(result.data[0].menuList.length>0){
						for(var j=0;j<result.data[0].menuList.length;j++){
							html+="<tr><td>"+result.data[0].menuList[j].id+"</td><td></td><td>"+result.data[0].menuList[j].index
							+"</td><td>"+result.data[0].menuList[j].name+"</td><td>"+result.data[0].menuList[j].url
							+"</td><td><button onclick='UI.deleteMenu(\""+result.data[0].menuList[j].id+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>删除</button><button onclick='UI.updateMenu(\""+result.data[0].menuList[j].id+"\",\""+result.data[0].menuList[j].name+"\",\""+result.data[0].menuList[j].index+"\",\""
							+result.data[0].menuList[j].url+"\",\""+result.data[0].menuList[j].desc+"\",\""+result.data[0].menuList[j].menuId+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>修改</button></td><tr>";
						}
					}
					
					
					
				}
				$("#menu_td").empty();
				$("#menu_td").append(html);
				
				$("#parentId").empty();
				$("#parentId").append("<option value='0'>一级菜单</option>");
				$("#parentId").append(body);
				$("#update_parentId").empty();
				$("#update_parentId").append("<option value='0'>一级菜单</option>");
				$("#update_parentId").append(body);
				$("#menu_name").val("");
				$("#menu_num").val("");
				$("#menu_mark").val("");
				$("#menu_url").val("");
			},
			error : function(result) {
				layui.layer.msg("加载数据失败");
			}
		});



	},
	// 添加菜单
	addMenu:function(){
		var reg = new RegExp("^[0-9]*$");  
	    var obj = $("#menu_num").val();
		if($("#menu_name").val()==""){
			layui.layer.alert("请输入菜单名");
			return;
		}else if($("#menu_num").val()==""){
			layui.layer.alert("请输入排序");
			return;
		}else if(!reg.test(obj)){
			 layui.layer.alert("排序必须为数字!");  
			 return;
		}
		var url="";
		if($("#menu_url").val()!=""){
			url=$("#menu_url").val();
		}

		/*$.ajax({
			type:'POST',
			url : '/mp/menu/save',
			data:{
				parentId:$("#parentId").val(),
				menuId:$("#menu_menuId").val(),
				name:$("#menu_name").val(),
				index:$("#menu_num").val(),
				desc:$("#menu_mark").val(),
				urls:url
			},
			dataType:"json",
			async: false,
			success:function(data){
				UI.menu();
				$("#menu_url").hide();// 访问地址
				$("#menu_name").val("");
				$("#menu_num").val("");
				$("#menu_mark").val("");
				$("#menu_url").val("");
			}
			
		});*/

		mpCommon.invoke({
			url : '/mp/menu/save',
			data : {
				parentId:$("#parentId").val(),
				menuId:$("#menu_menuId").val(),
				name:$("#menu_name").val(),
				index:$("#menu_num").val(),
				desc:$("#menu_mark").val(),
				urls:url
			},
			success : function(result) {
				UI.menu();
				$("#menu_url").hide();// 访问地址
				$("#menu_name").val("");
				$("#menu_num").val("");
				$("#menu_mark").val("");
				$("#menu_url").val("");
			},
			error : function(result) {
				layui.layer.msg("添加菜单失败");
			}
		});


	},
	// 修改菜单
	updateMenu:function(id,name,index,url,desc,menuId){
		$("#index").hide();
		$("#index_one").hide();
		$("#pushText").hide();
		$("#pushOneText").hide();
		$("#pushManyText").hide();
		$("#menu").hide();
		$("#msg").hide();
		$("#fan").hide();
		$("#index_newMsg").hide();
		$("#index_add").hide();
		$("#index_userSum").hide();
		$("#pushMsg").hide();
		$("#update_menu").show();
		$("#update_id").val(id);
		$("#update_name").val(name);
		$("#update_index").val(index);
		if(url=="undefined"){
			$("#update_urls").val("");
		}else{
			$("#update_urls").val(url);
		}
		if(menuId=="undefined"){
			$("#update_menu_id").val("");
		}else{
			$("#update_menu_id").val(menuId);
		}
		
		if(desc=="undefined"){
			$("#update_desc").val("");
		}else{
			$("#update_desc").val(desc);
		}
		
	},
	// 提交修改菜单
	submit_update:function(){
		var reg = new RegExp("^[0-9]*$");  
	    var obj = $("#update_index").val();
		if($("#update_name").val()==""){
			layui.layer.alert("请输入菜单名");
			return;
		}else if($("#update_index").val()==""){
			layui.layer.alert("请输入排序");
			return;
		}else if(!reg.test(obj)){
			 layui.layer.alert("排序必须为数字!");  
			 return;
		}
		/*$.ajax({
			type:'POST',
			url:'/mp/menu/saveupdate',
			data:{
				id:$("#update_id").val(),
				parentId:$("#update_parentId").val(),
				menuId:$("#update_menu_id").val(),
				name:$("#update_name").val(),
				url:$("#update_urls").val(),
				index:$("#update_index").val(),
				desc:$("#update_desc").val()
			},
			async: false,
			success:function(result){
				layui.layer.alert("修改成功");
				UI.menu(0);
			}
		});*/

		mpCommon.invoke({
			url : '/mp/menu/saveupdate',
			data : {
				id:$("#update_id").val(),
				parentId:$("#update_parentId").val(),
				menuId:$("#update_menu_id").val(),
				name:$("#update_name").val(),
				url:$("#update_urls").val(),
				index:$("#update_index").val(),
				desc:$("#update_desc").val()
			},
			success : function(result) {
				layui.layer.alert("修改成功");
				UI.menu(0);
			},
			error : function(result) {
				layui.layer.msg("修改失败");
			}
		});
	},
	// 消息管理
	msg:function(num){
		if(num==1){
			if(pageIndex>0){
				pageIndex--;
			}else{
				layui.layer.alert("已是第一页");
				return;
			}
		}else if(num==2){
			if(sum!=10){
				layui.layer.alert("已是最后一页");
				return;
			}else{
				pageIndex++;
			}
		}else {
			pageIndex=num;
		}
		
		var html="";
		$("#li_one").css("background-color","#393D49");
		$("#li_two").css("background-color","#393D49");
		$("#li_three").css("background-color","#393D49");
		$("#li_four").css("background-color","#393D49");
		$("#li_five").css("background-color","#393D49");
		$("#li_six").css("background-color","#4E5465");
		$("#li_seven").css("background-color","#393D49");
		
		$("#msg").show();
		$("#update_menu").hide();
		$("#index_one").hide();
		$("#pushText").hide();
		$("#pushOneText").hide();
		$("#pushManyText").hide();
		$("#menu").hide();
		$("#fan").hide();
		$("#index").hide();
		$("#pushMsg").hide();
		$("#msgPage").show();
		$("#msg_item").hide();

		mpCommon.invoke({
			url : '/mp/msgs',
			data : {
				pageIndex:pageIndex,
				pageSize:10
			},
			success : function(result) {
				if(result.data==null){
					html+="<tr><td>暂无数据</td><td></tr>";
				}else{
					sum=result.data.length;
					for(var i=0;i<result.data.length;i++){
						var msg = result.data[i];
						var content = msg.body;
						if(msg.isEncrypt == 1){
							content = msgCommon.decryptMsg(msg.body,msg.messageId,msg.timeSend);
						}
						var sender = result.data[i].sender;
						html+="<tr><td><img width='40px' onerror='this.src=\"/pages/img/ic_avatar.png\"' src='"+myFn.getImgUrl(sender)+"'>"
						+"</td><td>"+sender+"</td><td>"+msg.nickname+"</td><td><a onclick='UI.findMsgList(\""+sender+"\")'>"+msg.count+"条</a></td><td>"+content+"</td><td><button onclick='UI.openMsg(\""+sender+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>发消息</button></td></tr>";
					}
					$("#msg_td").empty();
					$("#msg_td").append(html);
				}
			},
			error : function(result) {
				layui.layer.msg("加载数据失败");
			}
		});
		
	},
	// 粉丝管理
	fan:function(num){
		if(num==1){
			if(pageIndex>0){
				pageIndex--;
			}else{
				layui.layer.alert("已是第一页");
				return;
			}
		}else if(num==2){
			if(sum!=10){
				layui.layer.alert("已是最后一页");
				return;
			}else{
				pageIndex++;
			}
		}else {
			pageIndex=num;
		}
		var html="";
		$("#li_one").css("background-color","#393D49");
		$("#li_two").css("background-color","#393D49");
		$("#li_three").css("background-color","#393D49");
		$("#li_four").css("background-color","#393D49");
		$("#li_five").css("background-color","#393D49");
		$("#li_six").css("background-color","#393D49");
		$("#li_seven").css("background-color","#4E5465");
		
		$("#pageIndex").show();
		$("#fan").show();
		$("#update_menu").hide();
		$("#index_one").hide();
		$("#pushText").hide();
		$("#pushOneText").hide();
		$("#pushManyText").hide();
		$("#menu").hide();
		$("#msg").hide();
		$("#index").hide();
		$("#msg").hide();
		$("#fanPage").show();
		$("#msg_item").hide();

		/*$.ajax({
			type:'POST',
			url : '/mp/fans',
			data:{
				pageIndex:pageIndex,
				pageSize:10
			},
			dataType:"json",
			async: false,
			success:function(result){

				if(result.data.pageData==null){
					html+="<tr><td>暂无数据</td><td></tr>";
				}else{
                    // console.log("粉丝数据："+JSON.stringify(result.data));
					$("#fanTotal").empty();
					$("#fanTotal").append("共"+result.data.total+"条");
					sum=result.data.pageData.length;
					for(var i=0;i<result.data.pageData.length;i++){
						var toUserId = result.data.pageData[i].toUserId;
						html+="<tr><td><img width='40px' onerror='this.src=\"/pages/img/ic_avatar.png\"'  src='"+myFn.getImgUrl(toUserId)+"'></td><td>"+toUserId
						+"</td><td>"+result.data.pageData[i].toNickname+"</td><td><button onclick='UI.deleteUser(\""+toUserId+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>删除</button></td></tr>";
					}
					$("#fan_td").empty();
					$("#fan_td").append(html);
				}
			}
		});*/


		mpCommon.invoke({
			url : '/mp/fans',
			data : {
				pageIndex:pageIndex,
				pageSize:10
			},
			success : function(result) {
				if(result.data.pageData==null){
					html+="<tr><td>暂无数据</td><td></tr>";
				}else{
                    // console.log("粉丝数据："+JSON.stringify(result.data));
					$("#fanTotal").empty();
					$("#fanTotal").append("共"+result.data.total+"条");
					sum=result.data.pageData.length;
					for(var i=0;i<result.data.pageData.length;i++){
						var toUserId = result.data.pageData[i].toUserId;
						html+="<tr><td><img width='40px' onerror='this.src=\"/pages/img/ic_avatar.png\"'  src='"+myFn.getImgUrl(toUserId)+"'></td><td>"+toUserId
						+"</td><td>"+result.data.pageData[i].toNickname+"</td><td><button onclick='UI.deleteUser(\""+toUserId+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>删除</button></td></tr>";
					}
					$("#fan_td").empty();
					$("#fan_td").append(html);
				}
			},
			error : function(result) {
				layui.layer.msg("加载数据失败");
			}
		});
		
		
	},
	// 新消息
	newMsg:function(num){
		if(num==1){
			if(pageIndex>0){
				pageIndex--;
			}else{
				layui.layer.alert("已是第一页");
				return;
			}
		}else if(num==2){
			if(sum!=10){
				layui.layer.alert("已是最后一页");
				return;
			}else{
				pageIndex++;
			}
		}else {
			pageIndex=num;
		}
		var html="";
		$("#index_one").hide();
		$("#index_newMsg").show();
		$("#newMsgPage").show();

		mpCommon.invoke({
			url : '/mp/msgs',
			data : {
				pageIndex:pageIndex,
				pageSize:10
			},
			success : function(result) {
				if(result.data.length==0){
					//html+="<tr><td>暂无数据</td><td></tr>";
				}else{
					sum=result.data.length;
					for(var i=0;i<result.data.length;i++){
                        var msg = result.data[i];
                        var content = msg.body;
                        if(msg.isEncrypt == 1){
                            content = msgCommon.decryptMsg(msg.body,msg.messageId,msg.timeSend);
                        }
						html+="<tr><td>"+msg.sender+"</td><td>"+msg.nickname+"</td><td><a onclick='UI.findMsgList(\""+msg.sender+"\")'>"
						+msg.count+"条</a></td><td>"+content+"</td><td><button onclick='UI.openMsg(\""+msg.sender+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>发消息</button></td></tr>";
					}
					$("#newMsg_tb").empty();
					$("#newMsg_tb").append(html);
				}
			},
			error : function(result) {
				layui.layer.msg("加载数据失败");
			}
		});
	},
	// 打开发消息的面板
	openMsg:function(toUserId){
		$("#index").hide();
		$("#index_one").hide();
		$("#update_menu").hide();
		$("#pushText").hide();
		$("#pushOneText").hide();
		$("#pushManyText").hide();
		$("#menu").hide();
		$("#msg").hide();
		$("#fan").hide();
		$("#index_newMsg").hide();
		$("#index_add").hide();
		$("#index_userSum").hide();
		$("#pushMsg").show();
		$("#msg_item").hide();
//		alert(toUserId);
		
		$("#toUserId").html(toUserId);
	},
	// 新增用户
	newAddUser:function(num){
		
		
		var a="a";
		var html="";
		$("#addPage").show();
		$("#index_one").hide();
		$("#index_add").show();
		if(num==1){
			if(pageIndex>0){
				pageIndex--;
			}else{
//				layui.layer.open({
//					type:1,
//					title:'ssss',
//					offset:{
//						Math.random()*($(window).height()-300)
//						 ,Math.random()*($(window).width()-390)
//					}
//				});
				layui.layer.alert("已是第一页");
				return;
			}
		}else if(num==2){
			if(sum!=10){
				layui.layer.alert("已是最后一页");
				return;
			}else{
				pageIndex++;
			}
		}else {
			pageIndex=num;
		}
		/*$.ajax({
			type:"POST",
			url:"/mp/fans",
			data:{
				pageIndex:pageIndex,
				pageSize:10
			},
			dataType:"json",
			async: false,
			success:function(result){
				if(result.data.pageData==null){
					html+="<tr><td>暂无数据</td><td></tr>";
				}else{
					$("#addTotal").empty();
					$("#addTotal").append("共"+result.data.total+"条");
					sum=result.data.pageData.length;
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].toUserId+"</td><td>"+result.data.pageData[i].toNickname+"</td><td><button onclick='UI.deleteUser(\""+result.data.pageData[i].toUserId+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>删除</button></td></tr>";
					}
					$("#add_tb").empty();
					$("#add_tb").append(html);
				}
			}
		});*/
		

		mpCommon.invoke({
			url : '/mp/fans',
			data : {
				pageIndex:pageIndex,
				pageSize:10
			},
			success : function(result) {
				if(result.data.pageData==null){
					html+="<tr><td>暂无数据</td><td></tr>";
				}else{
					$("#addTotal").empty();
					$("#addTotal").append("共"+result.data.total+"条");
					sum=result.data.pageData.length;
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].toUserId+"</td><td>"+result.data.pageData[i].toNickname+"</td><td><button onclick='UI.deleteUser(\""+result.data.pageData[i].toUserId+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>删除</button></td></tr>";
					}
					$("#add_tb").empty();
					$("#add_tb").append(html);
				}
			},
			error : function(result) {
				layui.layer.msg("加载数据失败");
			}
		});

	},
	// 用户总数
	userSum:function(num){
		if(num==1){
			if(pageIndex>0){
				pageIndex--;
			}else{
				layui.layer.alert("已是第一页");
				return;
			}
		}else if(num==2){
			if(sum!=10){
				layui.layer.alert("已是最后一页");
				return;
			}else{
				pageIndex++;
			}
		}else {
			pageIndex=num;
		}
		var html="";
		$("#index_one").hide();
		$("#userSumPage").show();
		$("#index_userSum").show();
		
		/*$.ajax({
			type:"POST",
			url:"/mp/fans",
			data:{
				pageIndex:pageIndex,
				pageSize:10
			},
			dataType:"json",
			async: false,
			success:function(result){
				sum=result.data.pageData.length;
				if(result.data.pageData==null){
					html+="<tr><td>暂无数据</td><td></tr>";
				}else{
					$("#userTotal").empty();
					$("#userTotal").append("共"+result.data.total+"条");
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].toUserId+"</td><td>"+result.data.pageData[i].toNickname+"</td><td><button onclick='UI.deleteUser(\""+result.data.pageData[i].toUserId+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>删除</button></td></tr>";
					}
					$("#sum_tb").empty();
					$("#sum_tb").append(html);
				}
			}
		});*/

		mpCommon.invoke({
			url : '/mp/fans',
			data : {
				pageIndex:pageIndex,
				pageSize:10
			},
			success : function(result) {
				sum=result.data.pageData.length;
				if(result.data.pageData==null){
					html+="<tr><td>暂无数据</td><td></tr>";
				}else{
					$("#userTotal").empty();
					$("#userTotal").append("共"+result.data.total+"条");
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].toUserId+"</td><td>"+result.data.pageData[i].toNickname+"</td><td><button onclick='UI.deleteUser(\""+result.data.pageData[i].toUserId+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>删除</button></td></tr>";
					}
					$("#sum_tb").empty();
					$("#sum_tb").append(html);
				}
			}
		});

	},
	// 发送群发消息
	pushTextToAll:function() {
		var body=$("#textbody").val();
		if(null == body || "" == body || undefined == body){
			layui.layer.alert("请输入群发内容");
			return;
		}
		
		mpCommon.invoke({
			url : '/mp/textToAll',
			data : {
				title : body
			},
			success : function(result) {
				if (result.resultCode == 1) {
					layui.layer.alert("群发成功");
					$("#textbody").val("");
				} else {
					layui.layer.alert("群发失败");
				}
			},
			error : function(result) {
				layui.layer.alert("群发失败");
			}
		});
	},
	// 消息管理中的发消息
	pusgMsg:function(){
		var userId=$("#toUserId").html();
		// alert(userId);
		var body=$("#Msgbody").val();
		

		mpCommon.invoke({
			url : '/mp/msg/send',
			data : {
				body :body,
				toUserId : userId,
				type:1
			},
			success : function(result) {
				if (result.resultCode == 1) {
					layui.layer.alert("发送成功");
					$("#Msgbody").val("");
				} else {
					layui.layer.alert("发送失败");
				}
			},
			error : function(result) {
				layui.layer.alert("发送失败");
			}
		});
	},
	// 发送单条图文消息
	pushOneToAll:function() {
		var title=$("#pushbody").val()
		if(null == title  || "" == title || undefined == title){
            layui.layer.alert("请输入标题");
            return;
		}
		var sub=$("#pushbodyTitle").val();
        if(null == sub  || "" == sub || undefined == sub){
            layui.layer.alert("请输入小标题");
            return;
        }
		var img=$("#pushbodyImgUrl").val();
        if(null == img  || "" == img || undefined == img){
            layui.layer.alert("请输入图片Url");
            return;
        }
		var url=$("#pushbodyHtmlUrl").val();
        if(null == url  || "" == url || undefined == url){
            layui.layer.alert("请输入网页Url");
            return;
        }
		

		mpCommon.invoke({
			url : '/mp/pushToAll',
			data : {
				title:title,
				sub:sub,
				img:img,
				url:url
			},
			success : function(result) {
				if (result.resultCode == 1) {
					layui.layer.alert("群发成功");
					$("#pushbody").val("");
					$("#pushbodyTitle").val("");
					$("#pushbodyImgUrl").val("");
					$("#pushbodyHtmlUrl").val("");
				} else {
					layui.layer.alert("群发失败");
				}
			},
			error : function(result) {
				layui.layer.alert("群发失败");
			}
		});
	},
	// 发送多条图文消息
	pushManyToAll:function () {

        var bodyVal = $("#body").val();
        var bodyHtmlUrlVal = $("#bodyHtmlUrl").val();
        var bodyImgUrlVal = $("#bodyImgUrl").val();
		if(null == bodyVal || "" == bodyVal || undefined == bodyVal){
            layui.layer.alert("请输入标题");
            return;
		}
        if(null == bodyImgUrlVal || "" == bodyImgUrlVal || undefined == bodyImgUrlVal){
            layui.layer.alert("请输入图片Url");
            return;
        }
        if(null == bodyHtmlUrlVal || "" == bodyHtmlUrlVal || undefined == bodyHtmlUrlVal){
            layui.layer.alert("请输入网页Url");
            return;
        }
        var title=new Array();
		title.push(bodyVal);
		var url=new Array();
		url.push(bodyHtmlUrlVal);
		var img=new Array();
		img.push(bodyImgUrlVal);
		
		for(var j=2;j<=i;j++){
            var fVal = $("#f"+j).val();
            var dVal = $("#d"+j).val();
            var cVal = $("#c"+j).val();
			if(null == fVal || "" == fVal || undefined == fVal){
                layui.layer.alert("请输入标题");
                return;
			}
            if(null == cVal || "" == cVal || undefined == cVal){
                layui.layer.alert("请输入图片Url");
                return;
            }
            if(null == dVal || "" == dVal || undefined == dVal){
            	layui.layer.alert("请输入网页Url");
            	return;
			}

			title.push($("#f"+j).val());
			url.push($("#d"+j).val());
			img.push($("#c"+j).val());
		}

		mpCommon.invoke({
			url : '/mp/manyToAll',
			data : {
				title:title,
				url:url,
				img:img
			},
			success : function(result) {
				if (result.resultCode == 1) {

					$("#body").val("");
					$("#bodyImgUrl").val("");
					$("#bodyHtmlUrl").val("");
                    for(var j=2;j<=i;j++){
                        $("#f"+j).hide();
                        $("#f"+j).val("");
                        $("#d"+j).hide();
                        $("#d"+j).val("");
                        $("#c"+j).hide();
                        $("#c"+j).val("");
                    }
                    title=[];
                    url=[];
                    img=[];
                    i = 1;// 重置
                    layui.layer.alert("群发成功");
				} else {
					alert("群发失败");
				}
			},
			error : function(result) {
				layui.layer.alert("群发失败");
			}
		});
	},
	// 新增
	add:function(){
		i++;
		var table="<div style='margin-top: 1%'><input id='f"+i+"' name='title' class='layui-input' style='width: 20%;display: inline;margin-right: 1.3%' placeholder='请输入标题'>"
		+"<input id='c"+i+"' name='img' class='layui-input' style='width: 20%;display: inline;margin-right: 1.3%' placeholder='请输入图片url'>"
		+"<input id='d"+i+"' name='url' class='layui-input' style='width: 20%;display: inline;margin-right: 1.3%' placeholder='请输入网页url'></div>";

		$("#tb").append(table);

	},
	// 删除用户
	deleteUser:function(userId){
		

		mpCommon.invoke({
			url : '/mp/fans/delete',
			data : {
				toUserId:userId
			},
			success : function(result) {
				if (result.resultCode == 1) {
					layui.layer.alert("删除成功");
					UI.fan(0);
				} else {
					layui.layer.alert("删除失败");
				}
			},
			error : function(result) {
				layui.layer.alert("删除失败");
			}
		});
	},
	findMsgList:function(toUserId){
		$("#newMsg_item").show();
		$("#msg_item").show();
		$("#index_newMsg").hide();
		$("#msg").hide();
		var html="";
		
		mpCommon.invoke({
			url : '/mp/msg/list',
			data : {
				toUserId:toUserId
			},
			success : function(result) {
				if(result.data==null){
					html+="<tr><td>暂无数据</td><td></tr>";
				}else{
					for(var i=0;i<result.data.length;i++){
					    var msg = result.data[i];
						var sender = msg.sender;
						var plaintext = msg.content;
						// 消息解密处理
                        if(1 == msg.isEncrypt){
                            // 明文
                            plaintext = msgCommon.decryptMsg(msg.content,msg.messageId,msg.timeSend);
                            console.log(" msg : "+msg.content+"      role:   "+msg.isEncrypt+"       msgId:  "+msg.messageId+"    timeSend:  "+msg.timeSend +" plaintext:   "+plaintext);
                        }

						html+="<tr><td><img width='40px' onerror='this.src=\"/pages/img/ic_avatar.png\"' src='"+myFn.getImgUrl(sender)+"'></td><td>"+sender+"</td><td>"
							+result.data[i].nickname+"</td><td>"+plaintext+"</td><td><button onclick='UI.openMsg(\""+sender+"\")' class='layui-btn' style='height:30px;line-height:30px;border-radius:3px'>发消息</button></td></tr>";
					}
					$("#newMsg_body").empty();
					$("#newMsg_body").append(html);
					$("#Msg_body").empty();
					$("#Msg_body").append(html);
				}
			},
			error : function(result) {
				layui.layer.alert("加载数据失败");
			}
		});
	},
	// select框变化
	change:function(){
		if($("#parentId").val()==0){
			$("#menu_url").hide();
			$("#menu_menuId").hide();
		}else{
			$("#menu_url").show();
			$("#menu_menuId").show();
		}
	},
	// 删除菜单
	deleteMenu:function(id){

		mpCommon.invoke({
			url : '/mp/menu/delete',
			data : {
				id:id
			},
			success : function(result) {
				layui.layer.alert("删除成功");
				UI.menu(0);
			},
			error : function(result) {
				layui.layer.alert("删除失败");
			}
		});


	},
	limit:function(index){
		layui.use('laypage', function(){
        var laypage = layui.laypage;
        console.log($("#pageCount").val());
        var count=$("#pageCount").val();
        //执行一个laypage实例
        laypage.render({
            elem: 'laypage'
            ,count: count
            ,layout: ['count', 'prev', 'page', 'next', 'limit', 'refresh', 'skip']
            ,jump: function(obj){
            	console.log(obj)
            	if(index==1){
            		Key.keyword_list(1,obj.limit)
            		index=0;
            	}else{
            		Key.keyword_list(obj.curr,obj.limit)
            	}
            	
            }
   		 })
 	   })
	},
	// 返回
	return_btn:function(){
		$("#menu").show();
		$("#update_menu").hide();
	},

}
