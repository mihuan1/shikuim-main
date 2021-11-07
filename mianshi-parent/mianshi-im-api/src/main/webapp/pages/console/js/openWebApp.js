layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

        //app列表
	    var tableIns = table.render({

	      elem: '#openWebApp_table'
	      ,url:request("/console/openAppList")+"&type=2"
	      ,id: 'openWebApp_table'
	      ,page: true
	      ,curr: 0
          ,limit:Common.limit
          ,limits:Common.limits
	      ,groups: 7
	      ,cols: [[ //表头
	           {field: 'accountId', title: '申请用户Id',width:120}
	          ,{field: 'appName', title: '应用名称',width:150}
	          ,{field: 'appIntroduction', title: '应用简介', sort: true, width:150}
	          ,{field: 'appUrl', title: '应用官网', width:150} 
	          ,{field: 'createTime',title:'申请时间',sort: true, width:200,templet: function(d){
	          		return UI.getLocalTime(d.createTime);
	          }}
	          ,{fixed: 'right', width: 200,title:"操作", align:'left', toolbar: '#openWebAppListBar'}
	        ]]
			,done:function(res, curr, count){
				checkRequst(res);
                if(localStorage.getItem("role")==1){
                    $(".detail").hide();
                    $(".del").hide();
                }
			}
	    });

	    // 表格操作
	    table.on('tool(openWebApp_table)', function(obj){
	        var layEvent = obj.event,
	            data = obj.data;
	        if(layEvent === 'detail'){ //app详情
	        	WebApp.appDetail(data.id);
	        }else if(layEvent === 'del'){// 删除
	        	WebApp.deleteWebApp(data.id,data.accountId,obj);

	        }
        });

	    // 搜索
        $(".search_openWebApp").on("click",function(){
	        table.reload("openWebApp_table",{
	            page: {
	                curr: 1 //重新从第 1 页开始
	            },
	            where: {
	                keyWorld : $(".openApp_keyword").val()  //搜索的关键字
	            }
	        })

		    $(".openApp_keyword").val('');
    	});
});

var WebApp={
	// 应用详情
	appDetail:function(id){
		Common.invoke({
			url:request('/console/openAppDetail'),
			data:{
				id:id
			},
			success:function(result){
				$("#app_Id").empty();
				$("#app_Id").append(result.data.id);
				$("#status").empty();
				$("#status").append(result.data.status==0?"申请中":result.data.status==1?"已通过":result.data.status==2?"审核失败":"已禁用");
				if(result.data.status==1){
					$("#approvedAPP").hide();
					$("#reasonFailure").hide();
					$("#disable").show();
				}else if(result.data.status==-1){
					$("#approvedAPP").show();
					$("#reasonFailure").show();
					$("#disable").hide();
				}
				$("#accountId").empty();
				$("#accountId").append(result.data.accountId);
				$("#appName").empty();
				$("#appName").append(result.data.appName);
				$("#appIntroduction").empty();
				$("#appIntroduction").append(result.data.appIntroduction);
				$("#appUrl").empty();
				$("#appUrl").append(result.data.appUrl);
				$("#webInfoImg").empty();
				$("#webInfoImg").append(result.data.webInfoImg)
				$("#appsmallImg").empty();
				$("#appsmallImg").append(result.data.appsmallImg);
				$("#appImg").empty();
				$("#appImg").append(result.data.appImg);
				$("#appId").empty();
				$("#appId").append(result.data.appId);
				$("#appSecret").empty();
				$("#appSecret").append(result.data.appSecret);
				$("#createTime").empty();
				$("#createTime").append(UI.getLocalTime(result.data.createTime));
				$("#isAuthShare").empty();
				$("#isAuthShare").append(result.data.isAuthShare==2?"申请中":result.data.isAuthShare==1?"已授权":"未获得");
				$("#isAuthLogin").empty();
				$("#isAuthLogin").append(result.data.isAuthLogin==2?"申请中":result.data.isAuthLogin==1?"已授权":"未获得");
				$("#isAuthPay").empty();
				$("#isAuthPay").append(result.data.isAuthPay==2?"申请中":result.data.isAuthPay==1?"已授权":"未获得");
				if(result.data.isAuthShare==2){
					$("#shareOperate").empty();
					$("#shareOperate").append("<button onclick='WebApp.checkShare(\""+result.data.id+"\")' class='layui-btn layui-btn-danger layui-btn-xs'>通过审核</button>");
				}else {
					$("#shareOperate").empty();
				}
				if(result.data.isAuthLogin==2){
					$("#loginOperate").empty();
					$("#loginOperate").append("<button onclick='WebApp.checkLogin(\""+result.data.id+"\")' class='layui-btn layui-btn-danger layui-btn-xs'>通过审核</button>");
				}else {
					$("#loginOperate").empty();
				}
				if(result.data.isAuthPay==2){
					$("#payOperate").empty();
					$("#payOperate").append("<button onclick='WebApp.checkPay(\""+result.data.id+"\")' class='layui-btn layui-btn-danger layui-btn-xs'>通过审核</button>");
				}else {
					$("#payOperate").empty();
				}

				$("#openWebAppList").hide();
	        	$(".applicationList").hide();
	        	$("#openApp_ApplicationList").hide();
	        	$("#appDetail").show();
	        	$(".btn_openApp").show();
			}
		})
	},
	// 删除应用
	deleteWebApp:function(id,accountId,obj){
		layer.confirm('确定删除该应用？',{icon:3, title:'提示信息'},function(index){
			Common.invoke({
				url:request('/console/deleteWebApp'),
				data:{
					id:id,
					accountId:accountId
				},
				success:function(result){
					if(result.resultCode==1){
						layui.layer.alert("删除成功");
						obj.del();
					}
				}
			})
		})
	},
	// 申请列表
	applicationList:function(index){
		var html="";
		$("#openWebAppList").hide();
		$("#openApp_ApplicationList").show();
		$(".applicationList").hide();
		$(".btn_openApp").show();
		Common.invoke({
			url:request('/console/openAppList'),
			data:{
				pageIndex:index,
				pageSize:10,
				type:2,
				status:0
			},
			success:function(result){
				console.log(result);
				if(result.resultCode==1){
					// $("#pageCount").val(result.data.length);
					for(var i=0;i<result.data.length;i++){
						html+="<tr><td>"+result.data[i].accountId+"</td><td>"+result.data[i].appName+"</td><td>"
						+result.data[i].appIntroduction+"</td><td>"+result.data[i].appUrl+"</td><td>"
						+result.data[i].createTime+"</td><td><a class='layui-btn layui-btn-primary layui-btn-xs' onclick='WebApp.appDetail(\""
						+result.data[i].id+"\")'>详情</a><a class='layui-btn layui-btn-danger layui-btn-xs' onclick='WebApp.deleteWebApp(\""
						+result.data[i].id+"\",\""+result.data[i].accountId+"\")'>删除</a></td></tr>";
					}
					$("#openWebApp_Applicationtbody").empty();
					$("#openWebApp_Applicationtbody").append(html);
				}
			}
		})
	},
	back:function(){

		$("#openWebAppList").show();
		$("#appDetail").hide();
		$(".btn_openApp").hide();
		$(".applicationList").show();
		$("#openApp_ApplicationList").hide();
	},
	// 审核
	approvedAPP:function(status){
		Common.invoke({
			url:request('/console/approvedAPP'),
			data:{
				id:$("#app_Id").html(),
				userId:localStorage.getItem("account"),
				status:status
			},
			success:function(result){
				if(result.resultCode==1){
					if(status==1){
						layui.layer.alert("审核通过");
						WebApp.appDetail($("#app_Id").html());
					}else if(status==2){
						layui.layer.alert("审核失败");
						WebApp.appDetail($("#app_Id").html());
					}else if(status==-1){

						layui.layer.alert("禁用成功");
						WebApp.appDetail($("#app_Id").html());
					}else{
						layui.layer.alert("状态错误");
					}
				}
				
			}
		})
	},
	// 审核通过分享权限
	checkShare:function(id){
		Common.invoke({
			url:request('/console/checkPermission'),
			data:{
				id:id,
				accountId:localStorage.getItem("userId"),
				isAuthShare:1
			},
			success:function(result){
				if(result.resultCode==1){
					WebApp.appDetail(id);
					layui.layer.alert("审核通过");
				}
			}
		})
	},
	// 审核通过登录权限
	checkLogin:function(id){
		Common.invoke({
			url:request('/console/checkPermission'),
			data:{
				id:id,
				accountId:localStorage.getItem("userId"),
				isAuthLogin:1
			},
			success:function(result){
				if(result.resultCode==1){
					WebApp.appDetail(id);
					layui.layer.alert("申请成功，待审核");
				}
			}
		})
	},
	// 审核通过支付权限
	checkPay:function(id){
		Common.invoke({
			url:request('/console/checkPermission'),
			data:{
				id:id,
				accountId:localStorage.getItem("userId"),
				isAuthPay:1
			},
			success:function(result){
				if(result.resultCode==1){
					WebApp.appDetail(id);
					layui.layer.alert("申请成功，待审核");
				}
			}
		})
	}  
}