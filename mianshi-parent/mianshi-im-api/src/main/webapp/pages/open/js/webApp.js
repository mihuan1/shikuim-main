var user={};
var webApp={};
$(function(){
	Web.webAppList();
	user.userId=localStorage.getItem("userId");
	user.access_Token=localStorage.getItem("access_Token");
	user.apiKey=localStorage.getItem("apiKey");
	user.telephone=localStorage.getItem("telephone");
	user.status=localStorage.getItem("status");
	$("#uploadFileFrom").attr("action",Config.uploadUrl);
	$("#uploadSmallFileFrom").attr("action",Config.uploadUrl);
	$("#uploadwebInfoImgFileFrom").attr("action",Config.uploadUrl);
	$("#uploadIconFileFrom").attr("action",Config.uploadUrl); 
})
var app={};
var Web={
	mobileApp:function(){
		// href=""
		location.replace("/pages/open/mobileApp.html");
	},
	webApp:function(){
		location.replace("/pages/open/webApp.html");
	},
	// 创建应用UI显示
	createWebApp:function(){
		myFn.invoke({
			url:'/open/getOpenAccount',
			data:{
				userId:localStorage.getItem("userId")
			},
			success:function(result){
				if(result.data==undefined||result.data==""||result.data==null){
					layer.confirm('账号异常，请重新登录', {
					    icon: 3,
					    title: '提示信息'
					  }, function(index) {
					    window.parent.location.replace("/pages/open/login.html");
					  })
				}
				if(result.data.status==1){
					$("#createApp_div").show();
					$("#applist").hide();
				}else if(result.data.status==0){
					layui.layer.alert("账号审核中");
				}else if(result.data.status==-1){
					layui.layer.alert("账号被禁用");
				}else{
					layui.layer.alert("暂未成为开发者，请到账号中心设置");
					// location.replace("/pages/open/index.html");
				}
			}
		})
	},
	// 应用列表
	webAppList:function(){
		var html='<thead style="width: 100%;height: 40px;background-color: #ebeef0;line-height: 40px;"><tr><th>应用名称</th><th>状态</th><th>操作</th></tr></thead><tbody>';
		myFn.invoke({
			url:'/open/appList',
			data:{
				userId:localStorage.getItem("userId"),
				type:2
			},
			success:function(result){
				console.log(result.data);
				for(var i=0;i<result.data.length;i++){
					html+="<tr><td>"+result.data[i].appName+"</td><td>"+(result.data[i].status==-1?"已禁用":result.data[i].status==0?"审核中":"正常")+"</td><td><a href='#' onclick='Web.lookInfo(\""+result.data[i].id+"\")' style='color:#3292ff'>查看</a></td></tr>";
				}
				html+="</tbody>"
				$("#applist_tab").empty();
				$("#applist_tab").append(html);
			}
		})
	},
	uploadSmall:function(){
		var file=$("#photoSmallUpload")[0].files[0];
		$("#uploadSmallFileFrom").ajaxSubmit(function(data){
			var obj = eval("(" + data + ")");
			console.log(obj.url);
			$("#photoSmallUrl").val(obj.url);
			$("#uploadSmall_url").attr("src",obj.url);
		})
	},
	upload:function(){
		var file=$("#photoUpload")[0].files[0];
		$("#uploadFileFrom").ajaxSubmit(function(data){
			var obj = eval("("+data+")");
			console.log(obj.url);
			$("#photoUrl").val(obj.url);
			$("#upload_url").attr("src",obj.url);
		})
	},
	uploadWebInfo:function(){
		var file=$("#photowebInfoImgUpload")[0].files[0];
		$("#uploadwebInfoImgFileFrom").ajaxSubmit(function(data){
			var obj = eval("("+data+")");
			console.log(obj.url);
			$("#photowebInfoImgUrl").val(obj.url);
			$("#uploadwebInfoImg_url").attr("src",obj.url);
		})
	},
	selectwebInfoImgFile:function(){
		$("#photowebInfoImgUpload").click();
	},
	selectSmallFile:function(){
		$("#photoSmallUpload").click();
	},
	// 选择文件
	selectFile:function(){
		$("#photoUpload").click();
	},
	// 下一步
	nextBt:function(){
		if($("#appName").val()==null||$("#appName").val()==undefined||$("#appName").val()==""){
			layui.layer.alert("请输入应用名称");
			return;
		}else if($("#appIntroduction").val()==null||$("#appIntroduction").val()==undefined||$("#appIntroduction").val()==""){
			layui.layer.alert("请输入应用介绍");
			return ;
		}else if($("#appUrl").val()==null||$("#appUrl").val()==undefined||$("#appUrl").val()==""){
			layui.layer.alert("请输入应用官网");
			return ;
		}
		if(!(/^((https|http|ftp|rtsp|mms){0,1}(:\/\/){0,1})www\.(([A-Za-z0-9-~]+)\.)+([A-Za-z0-9-~\/])+$/.test($("#appUrl").val()))){
			layui.layer.alert("请输入正确的网址");
			return ;
		}

		$("#step1").hide();
		$("#step2").show();
		$("#img_two").addClass('img_two');
		$("#title_solid_one").addClass("img_color");
	},
	// 上一步
	upBt:function(){
		$("#step1").show();
		$("#step2").hide();
	},
	lookInfo:function(id){
		var html="";
		myFn.invoke({
			url:'/open/appInfo',
			data:{
				id:id
			},
			success:function(result){
				console.log(result.data);
				webApp.appSecret=result.data.appSecret;
				webApp.status=result.data.status;
				console.log("========"+app.appSecret);
				$("#app_url").attr("src",result.data.webAppImg);
				$("#app_name").empty();
				$("#app_name").append(result.data.webAppName);
				$("#app_id").empty();
				$("#app_id").append(result.data.appId);
				$("#AppSecret").empty();
				$("#AppSecret").append("<a onclick='Web.lookAppSecret()' style='color: #3292ff;cursor:pointer;'>查看</a>");
				html+="<button class='layui-btn layui-btn-danger' onclick='Web.deleteWebApp(\""+result.data.id+"\")'>删除应用</button>";
				$("#app_delete").empty();
				$("#app_delete").append(html);
				$("#app_status").empty();
				$("#app_status").append(result.data.status==0?"审核中":result.data.status==1?"已通过":result.data.status==2?"审核失败":"已禁用");
				$("#isAuthShare").empty();
				$("#isAuthShare").append(result.data.isAuthShare==2?"申请中":result.data.isAuthShare==1?"已获得":"未获得");
				$("#isAuthLogin").empty();
				$("#isAuthLogin").append(result.data.isAuthLogin==2?"申请中":result.data.isAuthLogin==1?"已获得":"未获得");
				$("#isAuthPay").empty();
				$("#isAuthPay").append(result.data.isAuthPay==2?"申请中":result.data.isAuthPay==1?"已获得":"未获得");
				$("#isGroupHelper").empty();
				$("#isGroupHelper").append(result.data.isGroupHelper==2?"申请中":result.data.isGroupHelper==1?"已获得":"未获得");
				$("#isAuthShareOperate").empty();
				$("#isAuthShareOperate").append(result.data.isAuthShare==0?"<a onclick='Web.applicationShare(\""+result.data.id+"\")' style='color:#3292ff;cursor:pointer;'>申请开通</a>":"<a onclick='' style='cursor:pointer;'>--<a>");
				$("#isAuthLoginOperate").empty();
				$("#isAuthLoginOperate").append(result.data.isAuthLogin==0?"<a onclick='Web.applicationLogin(\""+result.data.id+"\")' style='color:#3292ff;cursor:pointer;'>申请开通</a>":"<a onclick='' style='cursor:pointer;'>--</a>");
				$("#isAuthPayOperate").empty();
				$("#isAuthPayOperate").append(result.data.isAuthPay==0?"<a onclick='Web.applicationPay(\""+result.data.id+"\")' style='color:#3292ff;cursor:pointer;'>申请开通</a>":"<a onclick='' style='cursor:pointer;'>--</a>");
				$("#isGroupHelperOperate").empty();
				$("#isGroupHelperOperate").append(result.data.isGroupHelper==0?"<a onclick='Web.applicationGroupHelper(\""+result.data.id+"\")' style='color:#3292ff;cursor:pointer;'>申请开通</a>":"<a onclick='' style='cursor:pointer;'>--</a>");
			}
		})
		$("#applist").hide();
		$("#createApp_div").hide();
		$("#WebAppItem").show();
	},
	deleteWebApp:function(id){
		layer.confirm('确定删除该应用？',{icon:3, title:'提示信息'},function(index){
			layui.layer.closeAll(); 
			myFn.invoke({
				url:'/open/delApp',
				data:{
					id:id,
					accountId:localStorage.getItem("userId")
				},
				success:function(result){
					if(result.resultCode==1){
						Web.webAppList();
						layui.layer.alert("删除成功");
						$("#WebAppItem").hide();
						$("#applist").show();
					}
				}
			})
		})
		
	},
	submit:function(){
		if($("#callbackUrl").val()==null||$("#callbackUrl").val()==""||$("#callbackUrl").val()==undefined){
			layui.layer.alert("请输入授权回调域名");
			return;
		}
		if(!(/^((https|http|ftp|rtsp|mms){0,1}(:\/\/){0,1})www\.(([A-Za-z0-9-~]+)\.)+([A-Za-z0-9-~\/])+$/.test($("#callbackUrl").val()))){
			layui.layer.alert("请输入正确的网址");
			return ;
		}
		
		$("#step1").hide();
		$("#step2").show();
		$("#img_three").addClass('img_three');
		$("#title_solid_two").addClass("img_color");
		myFn.invoke({
			url:'/open/createApp',
			data:{
				accountId:localStorage.getItem("userId"),
				appName:$("#appName").val(),
				appIntroduction:$("#appIntroduction").val(),
				appUrl:$("#appUrl").val(),
				appsmallImg:$("#photoSmallUrl").val(),
				appImg:$("#photoUrl").val(),
				callbackUrl:$("#callbackUrl").val(),
				webInfoImg:$("#photowebInfoImgUrl").val(),
				appType:2
			},
			success:function(result){
				if(result.resultCode==1){
					layui.layer.alert("提交成功");
					Web.webAppList();
					$("#appName").val("");
					$("#appIntroduction").val("");
					$("#appUrl").val("");
					$("#photoSmallUrl").val("");
					$("#photoUrl").val("");
					$("#callbackUrl").val("");
					$("#applist").show();
					$("#createApp_div").hide();
				}else {
					layui.layer.alert(result.resultMsg);
				}
				
			}
		})
	},
	// 获取Appsecret
	getAppSecret:function(){
		if($("#get_password").val()!=null&&$("#get_password").val()!=""&&$("#get_password").val()!=undefined){
			myFn.invoke({
				url:"/open/ckeckOpenAccountt",
				data:{
					telephone:user.telephone,
					password:$.md5($("#get_password").val())
				},
				success:function(result){
					if(result.resultCode==1){
						console.log("获取成功");
						$("#AppSecret").empty();
						if(webApp.status==2){
							layui.layer.closeAll();
							layui.layer.alert("该应用审核失败");
						}else{
							$("#AppSecret").append(webApp.appSecret);
							layui.layer.closeAll();
						}
						
					}else{
						layui.layer.alert("密码错误");
					}
					// layui.layer.closeAll();
				},
				error:function(result){
					layui.layer.alert(result);
				}
			})
			
		}else{
			layui.layer.alert("请输入密码");
		}
		
	},
	// 查看appSecret
	lookAppSecret:function(){
		layui.layer.open({
			title:'输入密码获取AppSecret',
			type:1,
			area: ['300px', '200px'],
			content:'<input id="get_password" class="layui-input" type="password" placeholder="请输入密码" style="margin-top:10%;width:250px;margin-left:25px"><button class="layui-btn" style="margin-left:40%;margin-top:10%" onclick="Web.getAppSecret()">确定</button>'
		});
	},
	// 申请开通分享权限
	applicationShare:function(id){
		myFn.invoke({
			url:'/open/application',
			data:{
				id:id,
				accountId:localStorage.getItem("userId"),
				isAuthShare:2
			},
			success:function(result){
				layui.layer.alert("申请成功，待审核");
			}
		})
	},
	// 申请开通登录权限
	applicationLogin:function(id){
		myFn.invoke({
			url:'/open/application',
			data:{
				id:id,
				accountId:localStorage.getItem("userId"),
				isAuthLogin:2
			},
			success:function(result){
				layui.layer.alert("申请成功，待审核");
			}
		})
	},
	// 申请开通支付权限
	applicationPay:function(id){
		$("#interfaceInfo").hide();
		$("#payCallBack").show();
		$("#applicationPayId").val(id);
		
	},
	// 确认申请开通支付权限
	confirmApplicationPay:function(){
		myFn.invoke({
			url:'/open/application',
			data:{
				id:$("#applicationPayId").val(),
				accountId:localStorage.getItem("userId"),
				payCallBackUrl:$("#payCallBackUrl").val(),
				isAuthPay:2
			},
			success:function(result){
				layui.layer.alert("申请成功，待审核");
				
			}
		})
	},
	// 返回上一级
	upInfo:function(){
		$("#interfaceInfo").show();
		$("#payCallBack").hide();
	},
	// 群助手列表
	getHelperList:function(id){
		var helperHtml="";
		myFn.invoke({
			url:'/open/getHelperList',
			data:{
				openAppId:id
			},
			success:function(result){
				if(result.resultCode==1){
					var obj =null;
					for(var i=0;i<result.data.length;i++){
						obj = JSON.stringify(result.data[i]);
						console.log(obj)
						helperHtml += "<tr><td>"+result.data[i].name+"</td><td>"+result.data[i].desc+"</td><td>"+
						result.data[i].developer+"</td><td><a onclick='Web.updateHelper("+obj
						+")'>修改</a>|<a onclick='Web.deleteHelper(\""+result.data[i].id+"\")'>删除</a></td></tr>"
					}
					$("#GroupHelperList").append(helperHtml);
				}
			}	
		})
	},
	// 申请成为群助手
	applicationGroupHelper:function(id){
		$("#GroupHelperList").empty();
		Web.getHelperList(id);
		$("#interfaceInfo").hide();
		$("#GroupHelper").show();
		$("#addHelper_openAppId").val(id);

		$("#AddGroupHelper").hide();
		$("#WebAppItem").show();
		$("#submit_helper").hide();
		$("#update_helper").hide();

	},
	// 添加群助手
	addHelper:function(){
		$("#AddGroupHelper").show();
		$("#WebAppItem").hide();
		$("#submit_helper").show();
		$("#update_helper").hide();
		
		$("#helName").val("");
		$("#helDesc").val("");
		$("#helDeveloper").val("");
		$("#uploadIconUrl_url").attr("src",obj.url);
		$("#helperLink").val("");
		$("#appPackName").val("");
		$("#callBackClassName").val("");
		$("#iosUrlScheme").val("");
		$("#otherTitle").val("");
		$("#otherDesc").val("");
		$("#otherUrl").val("");
		$("#otherImgUrl").val("");
		$("#otherAppIocn").val("");
		$("#otherAppName").val("");

	},
	// 确认申请
	confirmIsGroupHelper:function(){
		
		var param={};
		param.name = $("#helName").val();
		param.openAppId = $("#addHelper_openAppId").val();
		param.desc = $("#helDesc").val();
		param.iconUrl = $("#photoIconUrl").val();
		param.developer = $("#helDeveloper").val();
		param.type = $("#HelperType").val();
		if(param.type==2){
			param.link = $("#helperLink").val();
			if($("#appPackName").val()!=null&&$("#appPackName").val()!=""&&$("#appPackName").val()!=undefined){
				param.appPackName = $("#appPackName").val();
			}
			if($("#callBackClassName").val()!=null&&$("#callBackClassName").val()!=""&&$("#callBackClassName").val()!=undefined){
				param.callBackClassName = $("#callBackClassName").val();
			}
			if($("#iosUrlScheme").val()!=null&&$("#iosUrlScheme").val()!=""&&$("#iosUrlScheme").val()!=undefined){
				param.iosUrlScheme = $("#iosUrlScheme").val();
			}
			
		}else if(param.type ==3){
			if($("#appPackName").val()!=null&&$("#appPackName").val()!=""&&$("#appPackName").val()!=undefined){
				param.appPackName = $("#appPackName").val();
			}
			if($("#callBackClassName").val()!=null&&$("#callBackClassName").val()!=""&&$("#callBackClassName").val()!=undefined){
				param.callBackClassName = $("#callBackClassName").val();
			}
			if($("#iosUrlScheme").val()!=null&&$("#iosUrlScheme").val()!=""&&$("#iosUrlScheme").val()!=undefined){
				param.iosUrlScheme = $("#iosUrlScheme").val();
			}

			param.title = $("#otherTitle").val();
			param.subTitle = $("#otherDesc").val();
			param.url = $("#otherUrl").val();
			param.imgUrl = $("#otherImgUrl").val();
			param.appIocn = $("#otherAppIocn").val();
			param.appName = $("#otherAppName").val();
		}
		
		myFn.invoke({
			url:'/open/addHelper',
			data:param,
			success:function(result){
				layui.layer.alert("添加成功");
				Web.applicationGroupHelper(param.openAppId);
				
				
			}
		})
	},
	selectHelperType:function(){
		console.log($("#HelperType").val());
		if($("#HelperType").val()==2){
			$("#webUrl").show();
			$("#Other").hide();
			$("#appPackName_hel").show();
			$("#callBackClassName_hel").show();
			$("#iosUrlScheme_hel").show();
		}else if($("#HelperType").val()==3){
			$("#Other").show();
			$("#webUrl").hide();
			$("#appPackName_hel").show();
			$("#callBackClassName_hel").show();
			$("#iosUrlScheme_hel").show();
		}else{
			$("#Other").hide();
			$("#webUrl").hide();
			$("#appPackName_hel").hide();
			$("#callBackClassName_hel").hide();
			$("#iosUrlScheme_hel").hide();
		}	
	},
	selectIconFile:function(){
		$("#photoIconUpload").click();
	},
	uploadIcon:function(){
		var file=$("#photoIconUpload")[0].files[0];
		$("#uploadIconFileFrom").ajaxSubmit(function(data){
			var obj = eval("("+data+")");
			console.log(obj.url);
			$("#photoIconUrl").val(obj.url);
			$("#uploadIconUrl_url").attr("src",obj.url);
		})
	},
	updateHelper:function(data){
		$("#submit_helper").hide();
		$("#update_helper").show();

		$("#helName").val(data.name);
		$("#helDesc").val(data.desc);
		$("#helDeveloper").val(data.developer);
		$("#HelperType").val(data.type);
		$("#updateHelper_helperId").val(data.id);
		if(data.type==2){
			$("#webUrl").show();
			$("#appPackName_hel").show();
			$("#callBackClassName_hel").show();
			$("#iosUrlScheme_hel").show();
			$("#helperLink").val(data.link);
			$("#appPackName").val(data.appPackName);
			$("#callBackClassName").val(data.callBackClassName);
		}else if(data.type==3){
			$("#Other").show();
			$("#appPackName_hel").show();
			$("#callBackClassName_hel").show();
			$("#iosUrlScheme_hel").show();
			$("#appPackName").val(data.appPackName);
			$("#callBackClassName").val(data.callBackClassName);
			$("#iosUrlScheme").val(data.iosUrlScheme);
			$("#otherTitle").val(data.Other.title);
			$("#otherDesc").val(data.Other.subTitle);
			$("#otherUrl").val(data.Other.url);
			$("#otherImgUrl").val(data.Other.imageUrl);
			$("#otherAppIocn").val(data.Other.appIcon);
			$("#otherAppName").val(data.Other.appName);
		}else{
			$("#webUrl").hide();
			$("#Other").hide();
		}
		
		$("#AddGroupHelper").show();
		$("#WebAppItem").hide();
	},
	// 确认更新
	confirmUpdateHelper:function(){
		var param={};
		param.id=$("#updateHelper_helperId").val();
		param.name = $("#helName").val();
		// param.openAppId = $("#addHelper_openAppId").val();
		param.desc = $("#helDesc").val();
		param.iconUrl = $("#photoIconUrl").val();
		param.developer = $("#helDeveloper").val();
		param.type = $("#HelperType").val();
		if(param.type==2){
			param.link = $("#helperLink").val();
			if($("#appPackName").val()!=null&&$("#appPackName").val()!=""&&$("#appPackName").val()!=undefined){
				param.appPackName = $("#appPackName").val();
			}
			if($("#callBackClassName").val()!=null&&$("#callBackClassName").val()!=""&&$("#callBackClassName").val()!=undefined){
				param.callBackClassName = $("#callBackClassName").val();
			}
			if($("#iosUrlScheme").val()!=null&&$("#iosUrlScheme").val()!=""&&$("#iosUrlScheme").val()!=undefined){
				param.iosUrlScheme = $("#iosUrlScheme").val();
			}
			
		}else if(param.type ==3){
			if($("#appPackName").val()!=null&&$("#appPackName").val()!=""&&$("#appPackName").val()!=undefined){
				param.appPackName = $("#appPackName_hel").val();
			}
			if($("#callBackClassName").val()!=null&&$("#callBackClassName").val()!=""&&$("#callBackClassName").val()!=undefined){
				param.callBackClassName = $("#callBackClassName").val();
			}
			if($("#iosUrlScheme").val()!=null&&$("#iosUrlScheme").val()!=""&&$("#iosUrlScheme").val()!=undefined){
				param.iosUrlScheme = $("#iosUrlScheme").val();
			}

			param.title = $("#otherTitle").val();
			param.subTitle = $("#otherDesc").val();
			param.url = $("#otherUrl").val();
			param.imgUrl = $("#otherImgUrl").val();
			param.appIocn = $("#otherAppIocn").val();
			param.appName = $("#otherAppName").val();
		}

		myFn.invoke({
			url:'/open/updateHelper',
			data:param,
			success:function(result){
				if(result.resultCode==1){

					layui.layer.alert("更新成功");
					$("#AddGroupHelper").hide();
					$("#WebAppItem").show();
					Web.applicationGroupHelper($("#addHelper_openAppId").val());
				}else{
					layui.layer.alert("更新失败");
				}
				
			}
		})
	},
	deleteHelper:function(id){
		layer.confirm('确定删除该群助手？',{icon:3, title:'提示信息'},function(index){
			myFn.invoke({
				url:"/open/deleteHelper",
				data:{
					id:id
				},
				success:function(result){
					if(result.resultCode==1){
						layui.layer.alert("删除成功");
						Web.applicationGroupHelper($("#addHelper_openAppId").val());

					}else{
						layui.layer.alert("删除失败");
					}
				}
			})
		})
	}
}

