/*** 举报管理js  **/

var page=0;
var sum=0;
var consoleAdmin = localStorage.getItem("account");
$(function(){
	Com.complaint(0);
	Com.limit();
});



var Com={
	// 举报列表
	complaint:function(e,pageSize){
		html="";
		if(e==undefined){
			e=0;
		}else if(pageSize==undefined){
			pageSize=Common.limit;
		}
		$.ajax({
			url:request('/console/beReport'),
			data:{
				sender:($("#sender").val()==""?"":$("#sender").val()),
				receiver:($("#receiver").val()==""?"":$("#receiver").val()),
				type:$("#complaint_select").val(),
				pageIndex:(e==0?"0":e-1),
				pageSize:pageSize
			},
			dataType:'json',
			async:false,
			success:function(result){
                checkRequst(result);
				if(!Common.isNil(result.data.pageData)){
					$("#pageCount").val(result.data.allPageCount);
					var lockingMsg;
					var lockingRoomMsg;
					for(var i=0;i<result.data.pageData.length;i++){
						if($("#complaint_select").val()==0){
							(result.data.pageData[i].toUserStatus == -1 ? lockingMsg = "解锁被举报用户" : lockingMsg = "锁定被举报用户" )
							html+="<tr align='center'><td>"+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].userName+"</td><td>"+result.data.pageData[i].toUserId
						+"</td><td>"+result.data.pageData[i].toUserName+"</td><td>"+result.data.pageData[i].info+"</td><td>"+UI.getLocalTime(result.data.pageData[i].time)
						+"</td><td><button onclick='Com.deleteComplaint(\""+result.data.pageData[i].id
						+"\")' class='layui-btn layui-btn-danger layui-btn-xs delete' style='width: 50px'>删除</button>"
						+"<button onclick='Com.lockIng(\""+result.data.pageData[i].toUserId+"\",\""+(result.data.pageData[i].toUserStatus == -1 ? 1 : -1 )+"\")' class='layui-btn layui-btn-primary layui-btn-xs locking'>"+lockingMsg+"</button></td></tr>";
						}else if($("#complaint_select").val()==1){
                            (result.data.pageData[i].roomStatus == -1 ? lockingRoomMsg = "解锁被举报群组" : lockingRoomMsg = "锁定被举报群组" )
                            html+="<tr align='center'><td>"+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].userName+"</td><td>"+result.data.pageData[i].roomId
						+"</td><td>"+result.data.pageData[i].roomName+"</td><td>"+result.data.pageData[i].info+"</td><td>"+UI.getLocalTime(result.data.pageData[i].time)
						+"</td><td><button onclick='Com.deleteComplaint(\""+result.data.pageData[i].id
						+"\")' class='layui-btn layui-btn-danger layui-btn-xs delete'>删除</button>"
                                +"<button onclick='Com.roomlockIng(\""+consoleAdmin+"\",\""+result.data.pageData[i].roomId+"\",\""+(result.data.pageData[i].roomStatus == -1 ? 1 : -1 )+"\")' class='layui-btn layui-btn-primary layui-btn-xs roomLocking'>"+lockingRoomMsg+"</button></td></tr>";
						}else if($("#complaint_select").val()==2){
                            (result.data.pageData[i].webStatus == -1 ? lockingRoomMsg = "解锁被举报网页" : lockingRoomMsg = "锁定被举报网页" )
                            html+="<tr align='center'><td>"+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].userName+"</td>"
                        +"<td>"+result.data.pageData[i].webUrl+"</td><td>"+result.data.pageData[i].info+"</td><td>"+UI.getLocalTime(result.data.pageData[i].time)
                        +"</td><td><button onclick='Com.deleteComplaint(\""+result.data.pageData[i].id
                        +"\")' class='layui-btn layui-btn-danger layui-btn-xs delete'>删除</button>"
                                +"<button onclick='Com.webLockIng(\""+result.data.pageData[i].id+"\",\""+(result.data.pageData[i].webStatus == -1 ? 1 : -1)+"\")' class='layui-btn layui-btn-primary layui-btn-xs roomLocking'>"+lockingRoomMsg+"</button></td></tr>";
                        }
					}
					if($("#complaint_select").val()==0){
                        $("#td_value").show();
						$("#td_value").empty();
						$("#td_value").append("被举报人Id");
                        $('#receiver').attr('placeholder','被举报人Id');

                    }else if($("#complaint_select").val()==1){
                        $("#td_value").show();
					    $("#td_value").empty();
						$("#td_value").append("被举报群组roomId");
                        $('#receiver').attr('placeholder','被举报群组roomId');
					}else if($("#complaint_select").val()==2){
                        $("#td_value").hide();
                        $('#receiver').attr('placeholder','被举报的网页地址');
                    }

                    if($("#complaint_select").val()==0){
					    $("#td_nameValue").empty();
                        $("#td_nameValue").append("被举报人昵称");
                    }else if($("#complaint_select").val()==1){
                        $("#td_nameValue").empty();
                        $("#td_nameValue").append("被举报群组昵称");
                    }else if($("#complaint_select").val()==2){
                        $("#td_nameValue").empty();
                        $("#td_nameValue").append("网页地址");
                    }
                    if(($("#sender").val()==""||$("#sender").val()==undefined)||
					($("#receiver").val()==""||$("#receiver").val()==undefined)){
                        $("#complaint_table").empty();
                        $("#complaint_table").append(html);
					}
					if(localStorage.getItem("role")==1){
						$(".delete").hide();
						$(".locking").hide();
						$(".roomLocking").hide();

					}
					$("#sender").val("");
					$("#receiver").val("");
				}

			}
		})
	},

    // 举报列表
    findComplaint:function(){
	    // console.log("1111"+($("#sender").val()),
	    // console.log("1111"+($("#receiver").val()),
        var a = $("#sender").val();
        var b = $("#receiver").val();
        console.log("1111a"+a);
        console.log("1111b"+b);
        html="";
        $.ajax({
            url:request('/console/beReport'),
            data:{
                sender:($("#sender").val()==""?"":$("#sender").val()),
                receiver:($("#receiver").val()==""?"":$("#receiver").val()),
                type:$("#complaint_select").val(),

            },
            dataType:'json',
            async:false,
            success:function(result){
                checkRequst(result);
                if(!Common.isNil(result.data.pageData)){
                    $("#pageCount").val(result.data.allPageCount);
                    var lockingMsg;
                    var lockingRoomMsg;
                    for(var i=0;i<result.data.pageData.length;i++){
                        if($("#complaint_select").val()==0){
                            (result.data.pageData[i].toUserStatus == -1 ? lockingMsg = "解锁被举报用户" : lockingMsg = "锁定被举报用户" )
                            html+="<tr align='center'><td>"+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].toUserId
                                +"</td><td>"+result.data.pageData[i].info+"</td><td>"+UI.getLocalTime(result.data.pageData[i].time)
                                +"</td><td><button onclick='Com.deleteComplaint(\""+result.data.pageData[i].id
                                +"\")' class='layui-btn layui-btn-danger layui-btn-xs delete' style='width: 50px'>删除</button>"
                                +"<button onclick='Com.lockIng(\""+result.data.pageData[i].toUserId+"\",\""+(result.data.pageData[i].toUserStatus == -1 ? 1 : -1 )+"\")' class='layui-btn layui-btn-primary layui-btn-xs locking'>"+lockingMsg+"</button></td></tr>";
                        }else{
                            (result.data.pageData[i].roomStatus == -1 ? lockingRoomMsg = "解锁被举报群组" : lockingRoomMsg = "锁定被举报群组" )
                            html+="<tr align='center'><td>"+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].roomId
                                +"</td><td>"+result.data.pageData[i].info+"</td><td>"+UI.getLocalTime(result.data.pageData[i].time)
                                +"</td><td><button onclick='Com.deleteComplaint(\""+result.data.pageData[i].id
                                +"\")' class='layui-btn layui-btn-danger layui-btn-xs delete'>删除</button>"
                                +"<button onclick='Com.roomlockIng(\""+consoleAdmin+"\",\""+result.data.pageData[i].roomId+"\",\""+(result.data.pageData[i].roomStatus == -1 ? 1 : -1 )+"\")' class='layui-btn layui-btn-primary layui-btn-xs roomLocking'>"+lockingRoomMsg+"</button></td></tr>";
                        }
                    }
                    if($("#complaint_select").val()==0){
                        $("#td_value").empty();
                        $("#td_value").append("被举报的人");
                    }else if($("#complaint_select").val()==1){
                        $("#td_value").empty();
                        $("#td_value").append("被举报的群组");
                    }
                    $("#complaint_table").empty();
                    $("#complaint_table").append(html);
                    if(localStorage.getItem("role")==1){
                        $(".delete").hide();
                        $(".locking").hide();
                    }
                    Com.limit(1);
                }

            }
        })
    },


	// 删除
	deleteComplaint:function(id){
		layer.confirm('确定删除该举报内容？',{icon:3, title:'提示信息'},function(index){
			Common.invoke({
				url:request('/console/deleteReport'),
				data:{
					id:id
				},
				success:function(result){
					if(result.resultCode==1){
                        layer.msg("删除成功",{"icon":1});
						Com.complaint(0);
						Com.limit();
					}
				}
			})
		})
		
	},
    // 用户锁定解锁
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
                    /*layui.table.reload("user_list",{

                    })*/
                    Com.complaint(0);
                    Com.limit();
                },
                error : function(result) {
                }
            });
        })
    },

    // 群组锁定解锁
    roomlockIng:function(userId,roomId,status){
	    console.log(" ======》 "+userId+"   =====  "+roomId+"  ====="+status)
        var confMsg,successMsg="";
        (status == -1 ? confMsg = '确定锁定该群组？':confMsg = '确定解锁该群组？');
        (status == -1 ? successMsg = "锁定成功":successMsg ="解锁成功");
        layer.confirm(confMsg,{icon:3, title:'提示信息'},function(index){

            Common.invoke({
                url : request('/console/updateRoom'),
                data : {
                    userId:userId,
                    roomId:roomId,
                    s:status
                },
                successMsg : successMsg,
                errorMsg :  "加载数据失败，请稍后重试",
                success : function(result) {
                    // layui.table.reload("room_table")
                    Com.complaint(0);
                    Com.limit();
                },
                error : function(result) {
                }
            });
        })
    },
    // 网页锁定
    webLockIng:function(webUrlId,webUrlStatus){
	    console.log(" ===== webUrlId ==== : "+webUrlId +" ==== webUrlStatus ==== : "+webUrlStatus);
        // layer.alert("锁定成功");
        var confMsg,successMsg="";
        (webUrlStatus == -1 ? confMsg = '确定锁定该网页地址？':confMsg = '确定解锁该网页地址？');
        (webUrlStatus == -1 ? successMsg = "锁定成功":successMsg ="解锁成功");
        layer.confirm(confMsg,{icon:3, title:'提示信息'},function(index){

            Common.invoke({
                url : request('/console/isLockWebUrl'),
                data : {
                    webUrlId:webUrlId,
                    webStatus:webUrlStatus,
                },
                successMsg : successMsg,
                errorMsg :  "加载数据失败，请稍后重试",
                success : function(result) {
                    // layui.table.reload("room_table")
                    Com.complaint(0);
                    Com.limit();
                },
                error : function(result) {
                }
            });
        })
    },

	limit:function (index) {
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
					if(index == 1){
                        Com.complaint(index,obj.limit)
						index = 0;
                    }else{
                        Com.complaint(obj.curr,obj.limit)
					}

                }
            })
        });
    }
	
}