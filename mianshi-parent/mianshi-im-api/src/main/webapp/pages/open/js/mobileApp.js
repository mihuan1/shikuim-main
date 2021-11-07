layui.use(['element','form'],function(){
	var element=layui.element;
	var form=layui.form;

  // $("#user").append(localStorage.getItem("telephone"));

	//点击事件监听
  	element.on('nav(happy)', function(data){
  		console.log(data);
  		if(data.context.type==1){
        	alert("ssss");
      	}

  	});

})
var app={};
var user={};

$(function(){
	user.userId=localStorage.getItem("userId");
	user.access_Token=localStorage.getItem("access_Token");
	user.apiKey=localStorage.getItem("apiKey");
	user.telephone=localStorage.getItem("telephone");
	user.status=localStorage.getItem("status");
	App.appList();
	$("#uploadFileFrom").attr("action",Config.uploadUrl);
	$("#uploadSmallFileFrom").attr("action",Config.uploadUrl); 
	$("#uploadIconFileFrom").attr("action",Config.uploadUrl); 
})

var App={
	// 创建应用UI显示
	createApp:function(){
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
	appList:function(){
		var html='<thead style="width: 100%;height: 40px;background-color: #ebeef0;line-height: 40px;"><tr><th>应用名称</th><th>状态</th><th>操作</th></tr></thead><tbody>';
		myFn.invoke({
			type:'POST',
			url:'/open/appList',
			data:{
				userId:localStorage.getItem("userId"),
				type:1
			},
			success:function(result){
				console.log(result.data);
				for(var i=0;i<result.data.length;i++){
					html+="<tr><td>"+result.data[i].appName+"</td><td>"+(result.data[i].status==-1?"已禁用":result.data[i].status==0?"审核中":result.data[i].status==1?"正常":"审核失败")+"</td><td><a href='#' onclick='App.lookInfo(\""+result.data[i].id+"\")' style='color:#3292ff'>查看</a></td></tr>";
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
	checkbox_ios:function(){
		// ios被选中
		if($("#iosApp").is(':checked')==true){
			$(".form_son_ios").show();
		}else {
			$(".form_son_ios").hide();
		}
		
	},
	checkbox_iPhone:function(){
		if($("#iPhoneApp").is(':checked')==true){
			$("#iPhone_item").show();
		}else {
			$("#iPhone_item").hide();
		}
	},
	checkbox_iPad:function(){
		if($("#iPadApp").is(':checked')==true){
			$("#iPad_item").show();
		}else {
			$("#iPad_item").hide();
		}
	},
	checkbox_android:function(){
		if($("#androidApp").is(':checked')==true){
			$("#android_item").show();
		}else {
			$("#android_item").hide();
		}
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
	// 提交审核
	submit:function(){
		console.log($("#iosApp").prop('checked'));
		if($("#iosApp").prop('checked')==true){
			if($("#iPhoneApp").prop('checked')!=true){
				layui.layer.alert("请选择至少一个设备类型");
				return ;
			}else if($("#iosAppId").val()==null||$("#iosAppId").val()==""||$("#iosAppId").val()==undefined){
				layui.layer.alert("请输入Bundle ID");
				return ;
			}
		}else if($("#androidApp").prop('checked')==true){
			if($("#androidSign").val()==null||$("#androidSign").val()==""||$("#androidSign").val()==undefined){
				layui.layer.alert("请输入应用签名");
				return ;
			}else if($("#androidAppId").val()==null||$("#androidAppId").val()==""||$("#androidAppId").val()==undefined){
				layui.layer.alert("请输入应用包名");
				return ;
			}
		}else{
			layui.layer.alert("请至少选择一个应用平台");
			return ;
		}
		$("#step1").hide();
		$("#form").show();
		$("#form2").hide();
		$("#img_three").addClass('img_three');
		$("#title_solid_two").addClass("img_color");
	},
	// 确认并提交审核
	commit:function(){
		// if($("#sub_telephone").val()==null||$("#sub_telephone").val()==undefined||$("#sub_telephone").val()==""){
		// 	layui.layer.alert("请输入电话号码");
		// 	return ;
		// }else {
		// 	if(!(/^(((13[0-9]{1})|(15[0-9]{1})|(17[0-9]{1})|(18[0-9]{1})|(19[0-9]{1}))+\d{8})$/.test($("#sub_telephone").val()))){
		// 		layui.layer.alert("请输入正确的电话号码");
		// 		return;
		// 	}
		// }
		if($("#sub_password").val()==null||$("#sub_password").val==undefined||$("#sub_password").val()==""){
			layui.layer.alert("请输入密码");
			return ;
		}
		myFn.invoke({
			url:'/open/createApp',
			data:{
				appName:$("#appName").val(),
				appIntroduction:$("#appIntroduction").val(),
				appUrl:$("#appUrl").val(),
				appsmallImg:$("#photoSmallUrl").val(),
				appImg:$("#photoUrl").val(),
				iosDownloadUrl:$("#iosDownloadUrl").val(),
				iosAppId:$("#iosAppId").val(),
				iosBataAppId:$("#iosBataAppId").val(),
				androidDownloadUrl:$("#androidDownloadUrl").val(),
				androidSign:$("#androidSign").val(),
				androidAppId:$("#androidAppId").val(),
				accountId:localStorage.getItem("userId"),
				telephone:$("#sub_telephone").val(),
				password:$("#sub_password").val(),
				appType:1
			},
			success:function(result){
				if(result.resultCode==1){
					layui.layer.alert("创建成功");
					App.appList();
					$("#createApp_div").hide();
					$("#applist").show();

					//清空输入内容
					$("#appName").val("");
					$("#appIntroduction").val("");
					$("#appUrl").val("");
					$("#photoSmallUrl").val("");
					$("#photoUrl").val("");
					$("#iosDownloadUrl").val("");
					$("#iosAppId").val("");
					$("#iosBataAppId").val("");
					$("#androidDownloadUrl").val("");
					$("#androidSign").val("");
					$("#androidAppId").val("");
					$("#sub_telephone").val("");
				}else{
					layui.layer.alert(result.resultMsg);
				}
			}
		})
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
				app.appSecret=result.data.appSecret;
				app.status=result.data.status;
				console.log("========"+app.appSecret);
				$("#app_url").attr("src",result.data.appImg);
				$("#app_name").empty();
				$("#app_name").append(result.data.appName);
				$("#app_id").empty();
				$("#app_id").append(result.data.appId);
				$("#AppSecret").empty();
				$("#AppSecret").append("<a onclick='App.lookAppSecret()' style='color: #3292ff;cursor:pointer;'>查看</a>");
				html+="<button class='layui-btn layui-btn-danger' onclick='App.deleteApp(\""+result.data.id+"\")'>删除应用</button>";
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
				$("#isAuthShareOperate").append(result.data.isAuthShare==0?"<a onclick='App.applicationShare(\""+result.data.id+"\")' style='color:#3292ff;cursor:pointer;'>申请开通</a>":"<a onclick='' style='cursor:pointer;'>--<a>");
				$("#isAuthLoginOperate").empty();
				$("#isAuthLoginOperate").append(result.data.isAuthLogin==0?"<a onclick='App.applicationLogin(\""+result.data.id+"\")' style='color:#3292ff;cursor:pointer;'>申请开通</a>":"<a onclick='' style='cursor:pointer;'>--</a>");
				$("#isAuthPayOperate").empty();
				$("#isAuthPayOperate").append(result.data.isAuthPay==0?"<a onclick='App.applicationPay(\""+result.data.id+"\")' style='color:#3292ff;cursor:pointer;'>申请开通</a>":"<a onclick='' style='cursor:pointer;'>--</a>");
				$("#isGroupHelperOperate").empty();
				$("#isGroupHelperOperate").append(result.data.isGroupHelper==0?"<a onclick='App.applicationGroupHelper(\""+result.data.id+"\")' style='color:#3292ff;cursor:pointer;'>申请开通</a>":"<a onclick='' style='cursor:pointer;'>--</a>");
			}
		})
		$("#applist").hide();
		$("#createApp_div").hide();
		$("#appItem").show();
	},
	deleteApp:function(id){
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
						layui.layer.alert("删除成功");
						App.appList();
						$("#appItem").hide();
						$("#applist").show();
					}else {
						layui.layer.alert("删除失败");
					}
				}
			})
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
						if(app.status==2){
							layui.layer.closeAll();
							layui.layer.alert("该应用审核失败");
						}else{
							$("#AppSecret").append(app.appSecret);
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
			content:'<input id="get_password" class="layui-input" type="password" placeholder="请输入密码" style="margin-top:10%;width:250px;margin-left:25px"><button class="layui-btn" style="margin-left:40%;margin-top:10%" onclick="App.getAppSecret()">确定</button>'
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
	applicationPay:function(id){
		$("#interfaceInfo").hide();
		$("#payCallBack").show();
		$("#applicationPayId").val(id);
	},
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
	// 申请成为群助手
	applicationGroupHelper:function(id){
		$("#GroupHelperList").empty();
		App.getHelperList(id);
		$("#interfaceInfo").hide();
		$("#GroupHelper").show();
		$("#addHelper_openAppId").val(id);

		$("#AddGroupHelper").hide();
		$("#appItem").show();
		$("#submit_helper").hide();
		$("#update_helper").hide();
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
						helperHtml += "<tr><td>"+result.data[i].name+"</td><td>"+result.data[i].desc+"</td><td>"+
						result.data[i].developer+"</td><td><a onclick='App.updateHelper("+obj
						+")'>修改</a>|<a onclick='App.deleteHelper(\""+result.data[i].id+"\")'>删除</a></td></tr>"
					}
					$("#GroupHelperList").append(helperHtml);
				}
			}	
		})
	},
	// 添加群助手
	addHelper:function(){
		$("#AddGroupHelper").show();
		$("#appItem").hide();
		$("#submit_helper").show();
		$("#update_helper").hide();

		$("#helName").val("");
		$("#helDesc").val("");
		$("#helDeveloper").val("");
		$("#uploadIconUrl_url").attr("src","");
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
				App.applicationGroupHelper(param.openAppId);
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
			$("#iosUrlScheme").val(data.iosUrlScheme);
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
		$("#appItem").hide();
	},
	// 确认更新
	confirmUpdateHelper:function(){
		var param={};
		param.id=$("#addHelper_openAppId").val();
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
					App.applicationGroupHelper($("#addHelper_openAppId").val());
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
					layui.layer.alert("删除成功");
					App.getHelperList($("#addHelper_openAppId").val());
				}
			})
		})
	}
}