var eum=0;
var page=0;

$(function(){

	Pro.promptList(0);
	Pro.limit();
})
// var button='<button onclick="Pro.promptList(0)" class="layui-btn layui-btn-primary layui-btn-sm" style="margin-top: 21%;margin-left: 20%;"><<返回</button>';
var Pro={
	//提示消息管理
	promptList:function(e,pageSize){
		html="";
		if(e==undefined){
			e=0;
		}else if(pageSize==undefined){
			pageSize=Common.limit;
		}
		$.ajax({
			type:"POST",
			url:request("/console/messageList"),
			data:{
				pageIndex:(e==0?"0":e-1),
				pageSize:pageSize,
				keyword:($("#code").val() == null ? null : $("#code").val())
			},
			dataType:'json',
			async:false,
			success:function(result){
				checkRequst(result);
				if(result.data!=null){
					$("#pageCount").val(result.data.allPageCount);
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].code+"</td><td>"+result.data.pageData[i].type
						+"</td><td>"+result.data.pageData[i].zh+"</td><td>"+result.data.pageData[i].en
						+"</td><td>"+(result.data.pageData[i].big5==null?"":result.data.pageData[i].big5)
						+"</td><td align='center' width='150px'><button class='layui-btn layui-btn-danger layui-btn-xs delete' onclick='Pro.deleteErrorMessage(\""
						+result.data.pageData[i].code+"\")'>删除</button><button class='layui-btn layui-btn-primary layui-btn-xs update' onclick='Pro.updateErrorMessage(\""
						+result.data.pageData[i].code+"\")'>修改</button></td></tr>";
					}
                    if($("#code").val()==""||$("#code").val()==undefined){
                        $("#messageList_table").empty();
                        $("#messageList_table").append(html);
					}
					if(localStorage.getItem("role")==1){
						$(".btn_add").hide();
						$(".delete").hide();
						$(".update").hide();
					}
					$(".updateBtn").show();
			    	$("#errorMessageList").show();
					$("#addErrorMessage").hide();
					$("#code").val("");
				}else{
					layer.msg("暂无数据",{"icon":2});
				}
			}
		})
	},

	// 头部搜索
    findPromptList:function(){
        html="";

        $.ajax({
            type:"POST",
            url:request("/console/messageList"),
            data:{
                keyword:($("#code").val() == null ? null : $("#code").val())
            },
            dataType:'json',
            async:false,
            success:function(result){
            	checkRequst(result);
                if(result.data!=null){
                    $("#pageCount").val(result.data.allPageCount);
                    for(var i=0;i<result.data.pageData.length;i++){
                        html+="<tr><td>"+result.data.pageData[i].code+"</td><td>"+result.data.pageData[i].type
                            +"</td><td>"+result.data.pageData[i].zh+"</td><td>"+result.data.pageData[i].en
                            +"</td><td>"+(result.data.pageData[i].big5==null?"":result.data.pageData[i].big5)
                            +"</td><td align='center' width='150px'><button class='layui-btn layui-btn-danger layui-btn-xs delete' onclick='Pro.deleteErrorMessage(\""
                            +result.data.pageData[i].code+"\")'>删除</button><button class='layui-btn layui-btn-primary layui-btn-xs update' onclick='Pro.updateErrorMessage(\""
                            +result.data.pageData[i].code+"\")'>修改</button></td></tr>";
                    }
                    $("#messageList_table").empty();
                    $("#messageList_table").append(html);
                    $(".updateBtn").show();
                    $("#errorMessageList").show();
                    $("#addErrorMessage").hide();
                    // $("#code").val("");
                    Pro.limit(1);
                }else{
                    layer.msg("暂无数据",{"icon":2});
                }
            }
        })
    },


	//删除提示消息
	deleteErrorMessage:function(code){
		layer.confirm('确定删除该提示消息？',{icon:3, title:'提示信息'},function(index){
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
					Pro.promptList(0);
					Pro.limit();
				}
			})
		});
		
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
				$("#code").val("");

			}
		});
	},
	//修改提示消息
	update_errorMessage:function(){
		eum=0;
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
						Pro.promptList(page);
						Pro.limit();
						$("#addErrorMessage").hide();
						$("#errorMessageList").show();
						$(".info").val("");
						$(".insertBtn").show();
					}
				}
			});
		
	},
	//新增提示消息
    addErrorMessage:function(){
    	eum=1;
    	$(".updateBtn").hide();
    	$("#errorMessageList").hide();
		$("#addErrorMessage").show();
		// $("#bacl").empty();
		// $("#back").append(button);

	},
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
                    layer.msg("新增成功",{"icon":1});
                    $("#addErrorMessage").hide();
					$("#errorMessageList").show();
					$(".info").val("");
					$(".updateBtn").show();
                    Pro.promptList(0);
					Pro.limit();

				}else if(result.resultCode == 0){
					layer.alert("您输入的code已存在");
				}
			}
		})
	},
	//新增提示消息时校验code是否重复
	onblurCode:function(){
		if(eum==0){
			return;
		}else if(eum==1){
			if($("#codeNum").val()!=""){
				$.ajax({
					type:"POST",
					url:request("/console/messageList"),
					data:{
						keyword:$("#codeNum").val()
					},
					success:function(result){
						checkRequst(result);
						if(result.data !=null){
							layer.alert("您输入的code已存在");
						}
					}
				})
			}
			
		}
		
	},

	limit:function(index){
            layui.use('laypage', function(){
                var laypage = layui.laypage;
                console.log($("#pageCount").val());
                //执行一个laypage实例
                laypage.render({
                    elem: 'laypage'
                    ,count: $("#pageCount").val()
					,limit:Common.limit
                    ,limits:Common.limits
                    ,layout: ['count', 'prev', 'page', 'next', 'limit', 'refresh', 'skip']
                    ,jump: function(obj){
                        console.log(obj)
                        page=obj.curr;
                        if(index == 1){
                            Pro.promptList(index,obj.limit);
                            index = 0;
						}else
                        	Pro.promptList(page,obj.limit)
                    }
                })
            })
	}
}