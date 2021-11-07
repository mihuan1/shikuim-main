layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

        //开发者列表
	    var tableIns = table.render({

	      elem: '#openDeveloper_table'
	      ,url:request("/console/developerList")
	      ,id: 'openDeveloper_table'
	      ,page: true
	      ,curr: 0
          ,limit:Common.limit
          ,limits:Common.limits
	      ,groups: 7
	      ,cols: [[ //表头
	           {field: 'userId', title: '用户Id',width:120}
	          ,{field: 'mail', title: '绑定邮箱账号',width:200}
	          ,{field: 'telephone', title: '手机号', sort: true, width:150}
	          ,{field: 'realName', title: '真实姓名', width:150}
	          ,{field: 'verifyTime', title: '审核认证时间',sort: true, width:150,templet: function(d){
	          		return (d.verifyTime!=undefined?UI.getLocalTime(d.verifyTime):"");
	          }}
	          ,{field: 'endTime', title: '到期时间',sort: true, width:200} 
	          ,{field: 'createTime',title:'申请时间',sort: true, width:200,templet: function(d){
	          		return UI.getLocalTime(d.createTime);
	          }}
	          ,{fixed: 'right', width: 200,title:"操作", align:'left', toolbar: '#openDeveloperListBar'}
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
	    table.on('tool(openDeveloper_table)', function(obj){
	        var layEvent = obj.event,
	            data = obj.data;
	        if(layEvent === 'detail'){ //开发者详情
	        	
	        	Developer.developerDetail(data.userId);
	        	
	        }else if(layEvent === 'del'){// 删除
	        	
	        	Developer.deleteDeveloper(data.id);
	        	obj.del();
	        }
        });
	    // 搜索
        $(".search_openDeveloper").on("click",function(){
	        table.reload("openDeveloper_table",{
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
// $(function(){
// 	Developer.limit();
// })
var OpenAccount_id="";
var Developer={
	// 开发者详情
	developerDetail:function(userId){
		Common.invoke({
			url:request('/console/developerDetail'),
			data:{
				userId:userId
			},
			success:function(result){
				console.log(result);
				OpenAccount_id=result.data.id;
				$("#status").empty();
				$("#status").append(result.data.status==0?"申请中":result.data.status==1?"已通过":result.data.status==-1?"已禁用":result.data.status==2?"审核失败":"未申请");
				if(result.data.status!=0&&result.data.status!=-1&&result.data.status!=2){
					$("#approvedDeveloper").hide();
					$("#reasonFailure").hide();

				}
				$("#userId").empty();
				$("#userId").append(result.data.userId);
				$("#realName").empty();
				$("#realName").append(result.data.realName);
				$("#telephone").empty();
				$("#telephone").append(result.data.telephone);
				$("#mail").empty();
				$("#mail").append(result.data.mail);
				$("#idCard").empty();
				$("#idCard").append(result.data.idCard);
				$("#address").empty();
				$("#address").append(result.data.address);
				$("#companyName").empty();
				$("#companyName").append(result.data.companyName);
				$("#businessLicense").empty();
				$("#businessLicense").append(result.data.businessLicense);
				$("#createTime").empty();
				$("#createTime").append(UI.getLocalTime(result.data.createTime));
				if(result.data.endTime!=undefined){
					$("#endTime").empty();
					$("#endTime").append(UI.getLocalTime(result.data.endTime));
				}
				
			}
		})

		$("#openDeveloperList").hide();
		$("#developerDetail").show();
		$(".applicationList").hide();
		$("#openDeveloper_ApplicationList").hide();
		$(".btn_openDeveloper").show();
	},
	// 删除开发者
	deleteDeveloper:function(id){
		layer.confirm('确定删除该开发者？',{icon:3, title:'提示信息'},function(index){
			Common.invoke({
				url:request('/console/deleteDeveloper'),
				data:{
					id:id
				},
				success:function(result){
					layui.layer.alert("删除成功");
				}
			})
		})
		
	},
	// 申请列表
	applicationList:function(){
		var html="";
		Common.invoke({
			url:request('/console/developerList'),
			data:{
				status:0
			},
			success:function(result){
				console.log(result)
				for(var i=0;i<result.data.length;i++){
					$("#pageCount").val(result.data.length);
					html+="<tr><td>"+result.data[i].userId+"</td><td>"+result.data[i].mail+"</td><td>"
					+result.data[i].telephone+"</td><td>"+result.data[i].realName+"</td><td>"
					+(result.data[i].verifyTime==undefined?"":UI.getLocalTime(result.data[i].verifyTime))
					+"</td><td>"+(result.data[i].endTime==undefined?"":UI.getLocalTime(result.data[i].endTime))
					+"</td><td>"+UI.getLocalTime(result.data[i].createTime)
					+"</td><td><a class='layui-btn layui-btn-primary layui-btn-xs' onclick='Developer.developerDetail(\""
					+result.data[i].userId+"\")'>详情</a><a class='layui-btn layui-btn-danger layui-btn-xs' onclick='Developer.deleteDeveloper(\""
					+result.data[i].id+"\")'>删除</a></td></tr>"
				}
				$("#openDeveloper_Applicationtbody").empty();
				$("#openDeveloper_Applicationtbody").append(html);
			}
		})
		// Developer.limit();
		$("#openDeveloperList").hide();
		$("#openDeveloper_ApplicationList").show();
		$("#developerDetail").hide();
		$(".applicationList").hide();
		$(".btn_openDeveloper").show();
	},
	back:function(){
		$("#openDeveloper_ApplicationList").hide();
		$("#developerDetail").hide();
		$("#openDeveloperList").show();
		$(".btn_openDeveloper").hide();
		$(".applicationList").show();
	},
	// 开发者通过审核、审核失败、禁用
	approvedDeveloper:function(status){
		Common.invoke({
			url:request("/console/checkDeveloper"),
			data:{
				id:OpenAccount_id,
				status:status
			},
			success:function(result){
				if(result.resultCode==1){
					if(status==1){
						layui.layer.alert("审核通过");
					}else if(status==2){
						layui.layer.alert("审核失败");
					}else if(status==-1){
						layui.layer.alert("已禁用");
					}
					
				}
				Developer.developerDetail($("#userId").html());
				
			}
		})
	},
	// 分页
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
            		Developer.applicationList(1)
            		index=0;
            	}else{
            		Developer.applicationList(obj.curr)
            	}
            	
            }
   		 })
 	   })
	}



}