$(function(){
	UserInfo.getUser();
	$("#userBase").addClass("changebtn");
	$("#userBase_a").addClass("changColor");
	$("#uploadSmallFileFrom").attr("action",Config.uploadUrl);
})

var UserInfo={
	getUser:function(){
		myFn.invoke({
			url:'/open/getOpenAccount',
			data:{
				userId:localStorage.getItem("userId")
			},
			success:function(result){
				console.log("个人信息");
				console.log(result.data)
				if(result.data==undefined||result.data==""||result.data==null){
					layer.confirm('账号异常，请重新登录', {
					    icon: 3,
					    title: '提示信息'
					  }, function(index) {
					    window.parent.location.replace("/pages/open/login.html");
					  })
				}
				$("#email").empty();
				if(result.data.mail!=null&&result.data.mail!=""&&result.data.mail!=undefined){
					$("#email").append(result.data.mail);
				}else{
					$("#email").append("暂无");
				}
				$("#realName").empty();
				if(result.data.realName!=null&&result.data.realName!=""&&result.data.realName!=undefined){
					$("#realName").append(result.data.realName);
				}else {
					$("#realName").append("暂无");
				}
				$("#idCard").empty();
				if(result.data.idCard!=null&&result.data.idCard!=""&&result.data.idCard!=undefined){
					$("#idCard").append(result.data.idCard);
				}else{
					$("#idCard").append("暂无");
				}
				$("#address").empty();
				if(result.data.address!=null&&result.data.address!=""&&result.data.address!=undefined){
					$("#address").append(result.data.address);
				}else {
					$("#address").append("暂无");
				}
				$("#telephone").empty();
				$("#telephone").append(result.data.telephone);
				
			}
		})
	},
	changebtn1:function(e){
		$(e).addClass("changebtn");
		$("#userBase_a").addClass("changColor");
		$("#renzhen").removeClass("changebtn");
		$("#updatePassword_li").removeClass("changebtn");
		$("#renzhen_a").removeClass("changColor");
		$("#updatePassword_a").removeClass("changColor");
		$("#baseInfo").show();
		$("#baseInfo").addClass("showorhide");
		$("#developer").hide();
		$("#updatePassword").hide();
		$("#updateUserInfo").hide();
	},
	changebtn2:function(e){
		
		var html="";
		myFn.invoke({
			url:'/open/getOpenAccount',
			data:{
				userId:localStorage.getItem("userId")
			},
			success:function(result){
				if(result.data.status==1){
					$("#stutas_p").empty();
					$("#stutas_p").append("已认证");
					$("#stutas_a").empty();
					$("#stutas_a").append('<a href="javascript:void(0);" onclick="UserInfo.findDeveloper()" class="layui-btn" style="float: right;border-color: #44b549;min-width: 60px;">查看详情</a>');
				}else if(result.data.status==-1){
					$("#stutas_p").empty();
					$("#stutas_p").append("已禁用");
					$("#stutas_a").empty();
				}else if(result.data.status==0){
					$("#stutas_p").empty();
					$("#stutas_p").append("审核中");
					$("#stutas_a").empty();
				}else if(result.data.status==2){
					$("#stutas_p").empty();
					$("#stutas_p").append("审核失败");
					$("#stutas_a").empty();
					$("#stutas_a").append('<a href="javascript:void(0);" onclick="UserInfo.applyDeveloper()" class="layui-btn" style="float: right;border-color: #44b549;min-width: 60px;">申请开发者权限</a>');
				}else {
					$("#stutas_p").empty();
					$("#stutas_p").append("未申请");
					$("#stutas_a").empty();
					$("#stutas_a").append('<a href="javascript:void(0);" onclick="UserInfo.applyDeveloper()" class="layui-btn" style="float: right;border-color: #44b549;min-width: 60px;">申请开发者权限</a>');
				}
			}
		})
		$(e).addClass("changebtn");
		$("#renzhen_a").addClass("changColor");
		$("#userBase").removeClass("changebtn");
		$("#updatePassword_li").removeClass("changebtn");
		$("#userBase_a").removeClass("changColor");
		$("#updatePassword_a").removeClass("changColor");
		$("#baseInfo").hide();
		$("#developer").show();
		$("#developer").addClass("showorhide");
		$("#updatePassword").hide();
		$("#updateUserInfo").hide();
	},
	changebtn3:function(e){
		$(e).addClass("changebtn");
		$("#updatePassword_a").addClass("changColor");
		$("#userBase").removeClass("changebtn");
		$("#renzhen").removeClass("changebtn");
		$("#userBase_a").removeClass("changColor");
		$("#renzhen_a").removeClass("changColor");
		$("#baseInfo").hide();
		$("#developer").hide();
		$("#updateUserInfo").hide();
		$("#updatePassword").show();
		$("#updatePassword").addClass("showorhide");
	},
	// 修改个人信息
	updateUserInfo:function(){
		myFn.invoke({
			url:'/open/getOpenAccount',
			data:{
				userId:localStorage.getItem("userId")
			},
			success:function(result){
				if(result.data.mail!=undefined){
					$("#up_mail").val(result.data.mail);
				}
				if(result.data.idCard!=undefined){
					$("#up_idCard").val(result.data.idCard);
				}
				if(result.data.telephone!=undefined){
					$("#up_telephone").val(result.data.telephone);
				}
				if(result.data.address!=undefined){
					$("#up_address").val(result.data.address);
				}
				if(result.data.realName!=undefined){
					$("#up_realName").val(result.data.realName);
				}
				if(result.data.companyName!=undefined){
					$("#up_companyName").val(result.data.companyName);
				}
			}
		})
		$("#baseInfo").hide();
		$("#updateUserInfo").show();
		$("#updateUserInfo").addClass("showorhide");
	},
	// 提交修改个人信息
	submitUserInfo:function(){
		if(!(/^[1-9]\d{5}(18|19|([23]\d))\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/.test($("#up_idCard").val()))){
			layui.layer.alert("请输入正确的身份证号");
			return;
		}
		if(!(/^[A-Za-z\d]+([-_.][A-Za-z\d]+)*@([A-Za-z\d]+[-.])+[A-Za-z\d]{2,4}$/.test($("#up_mail").val()))){
			layui.layer.alert("请输入正确的邮箱");
			return ;
		}
		if(!(/^(((13[0-9]{1})|(15[0-9]{1})|(17[0-9]{1})|(18[0-9]{1})|(19[0-9]{1}))+\d{8})$/.test($("#up_telephone").val()))){
			if(!(/^86+(((13[0-9]{1})|(15[0-9]{1})|(17[0-9]{1})|(18[0-9]{1})|(19[0-9]{1}))+\d{8})$/.test($("#up_telephone").val()))){
				layui.layer.alert("请输入正确的手机号");
				return ;
			}
		}
		myFn.invoke({
			url:'/open/perfectUserInfo',
			data:{
				userId:localStorage.getItem("userId"),
				mail:$("#up_mail").val(),
				idCard:$("#up_idCard").val(),
				telephone:$("#up_telephone").val(),
				address:$("#up_address").val(),
				realName:$("#up_realName").val(),
				companyName:$("#up_companyName").val(),
				businessLicense:$("#photoSmallUrl").val()
			},
			success:function(result){
				layui.layer.alert("修改成功");			
				$("#up_mail").val("");
				$("#up_idCard").val("");
				$("#up_telephone").val("");
				$("#up_address").val("");
				$("#up_realName").val("");
				$("#up_companyName").val("");
				UserInfo.getUser();
				$("#baseInfo").show();
				$("#updateUserInfo").hide();
			}
		})
	},
	// 选择文件
	selectSmallFile:function(){
		$("#photoSmallUpload").click();
	},
	// 上传
	upload:function(){
		// var file=$("#photoUpload")[0].files[0];
		$("#uploadSmallFileFrom").ajaxSubmit(function(data){
			var obj = eval("("+data+")");
			console.log(obj.url);
			$("#photoSmallUrl").val(obj.url);
			$("#uploadSmall_url").attr("src",obj.url);
		})
	},
	// 修改密码
	updatePassword:function(){
		if($("#newpass").val()!=$("#newpass_one").val()){
			layui.layer.alert("两次密码输入不一致");
			return ;
		}
		myFn.invoke({
			url:'/open/updatePassword',
			data:{
				userId:localStorage.getItem("userId"),
				oldPassword:$.md5($("#oldpass").val()),
				newPassword:$.md5($("#newpass").val())
			},
			success:function(result){
				layui.layer.alert("修改成功");
				$("#oldpass").val("");
				$("#newpass").val("");
				$("#newpass_one").val("");
			}
		})
	},
	// 申请成为开发者
	applyDeveloper:function(){
		myFn.invoke({
			url:'/open/getOpenAccount',
			data:{
				userId:localStorage.getItem("userId")
			},
			// async:false,
			success:function(result){
				if(result.data.realName!=undefined&&result.data.realName!=null&&result.data.idCard!=undefined&&result.data.idCard!=null){
					myFn.invoke({
						url:'/open/applyDeveloper',
						data:{
							userId:localStorage.getItem("userId"),
							status:0
						},
						success:function(result){
							layui.layer.alert("申请已提交，请等待审核");
							UserInfo.changebtn2();
						}
					})
				}else {
					layui.layer.alert("请先完善个人信息");
					return;
				}
			}
		})
	},
	// 查看申请成为开发者详情
	findDeveloper:function(){
		myFn.invoke({
			url:'/open/getOpenAccount',
			data:{
				userId:localStorage.getItem("userId")
			},
			success:function(result){
				$("#createTime").empty();
				$("#createTime").append(UI.getLocalTime(result.data.createTime));
				$("#status").empty();
				$("#status").append(result.data.status==1?"审核通过":result.data.status==0?"审核中":"审核失败");
				$("#userInfo_tab").show();
				$("#userInfo_Item").hide();
			},
			error:function(result){

			}
		})
	}
}