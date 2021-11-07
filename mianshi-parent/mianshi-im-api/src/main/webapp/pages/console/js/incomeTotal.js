var page=0;
var sum=0;

layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

     
    //非管理员登录屏蔽操作按钮
    if(localStorage.getItem("IS_ADMIN")==0){
    	$(".liveRoom_btn_div").empty();
    	$(".delete").remove();
		$(".chatMsg").remove();
		$(".member").remove();
    }
    /*if(localStorage.getItem("IS_ADMIN")==0){
		$(".btn_addLive").hide();
		$(".delete").hide();
		$(".chatMsg").hide();
		$(".member").hide();
	}*/

	//直播间列表
    var tableIns = table.render({

      elem: '#incomeTotal_table'
      ,url:request("/console/liveRoomList")
      ,id: 'incomeTotal_table'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {field: 'nickName', title: '主播昵称',sort: true}
          // ,{field: 'name', title: '直播间名称',sort: true}
          ,{fixed: 'right', width: 300,title:"操作", align:'left', toolbar: '#incomeListBar'}
        ]]
		,done:function(res, curr, count){
            $(".timeComponent").hide();

		}
    });

    //列表操作
    table.on('tool(incomeTotal_table)', function(obj){
         var layEvent = obj.event,
            data = obj.data;
        if(layEvent === 'giftWater'){// 直播间礼物流水
            $("#giftTotalMsgDate").val("");
        	var tableIns1 = table.render({
			      elem: '#liveRoomGiftWater_table'
			      ,url:request("/console/getGiftList")+"&userId="+data.userId
			      ,id: 'liveRoomGiftWater_table'
			      ,page: true
			      ,curr: 0
                  ,limit:Common.limit
                  ,limits:Common.limits
			      ,groups: 7
			      ,cols: [[ //表头
					   {field: 'liveRoomName', title: '直播间名称',sort: true, width:220}
			       	  ,{field: 'giftId', title: '礼物id',sort: true, width:220}
			       	  ,{field: 'giftName', title: '礼物名称',sort: true, width:220}
			          ,{field: 'price', title: '价格',sort: true, width:220}
                      ,{field: 'actualPrice', title: '实际收入',sort: true, width:220}
			          ,{field: 'userId', title: '赠送人Id',sort: true, width:220}
			          ,{field: 'userName', title: '赠送人昵称',sort: true, width:220}
			          ,{field: 'toUserId', title: '接收人Id',sort: true, width:220}
			          ,{field: 'toUserName', title: '接收人昵称',sort: true, width:220}
			          ,{field: 'time', title: '赠送时间',sort: true, width:400,templet: function(d){
			          		return UI.getLocalTime(d.time);
			          }}]]
					,done:function(res, curr, count){
            checkRequst(res);
			      		// 初始化时间控件
						///layui.form.render('select');
						//日期范围
						layui.laydate.render({
							elem: '#giftTotalMsgDate'
							,range: "~"
							,done: function(value, date, endDate){  // choose end
								//console.log("date callBack====>>>"+value); //得到日期生成的值，如：2017-08-18
								var startDate = value.split("~")[0];
								var endDate = value.split("~")[1];


								// Count.loadGroupMsgCount(roomJId,startDate,endDate,timeUnit);
                                table.reload("liveRoomGiftWater_table",{
                                    page: {
                                        curr: 1 //重新从第 1 页开始
                                    },
                                    where: {
                                        userId : data.userId,  //搜索的关键字
                                   		startDate : startDate,
                                        endDate : endDate
                                    }
                                })
							}
							,max: 0
						});
						$(".current_total").empty().text((0==res.total ? 0:res.total));
						$(".timeComponent").show();
                    	$("#incomeList").hide();
						$("#liveRoomGiftWater").show();

					}
			    });
        }
      });


    //列表操作
    // table.on('tool(liveRoomMember_table)', function(obj){
    //      var layEvent = obj.event,
    //         data = obj.data;
    //      if(layEvent === 'remove'){ // 剔除
    //      	Live.deleteRoomUser(data.userId,$("#save_roomId").val());
    //      } else if(layEvent === 'shutup'){// 禁言
    //
    //      }
    // })

    //搜索
    $(".search_live").on("click",function(){
       
            table.reload("incomeTotal_table",{
                page: {
                    curr: 1 //重新从第 1 页开始
                },
                where: {
                    nickName : $("#incomeName").val()  //搜索的关键字
                }
            })
        $("#incomeName").val("");
    });
});
var Live={
		//
	// member_list:function(id){
	// 	layui.use('laypage', function(){
     //    var laypage = layui.laypage;
     //    console.log($("#member_pageCount").val());
     //    //执行一个laypage实例
     //    laypage.render({
     //        elem: 'member_live'
     //        ,count: $("#member_pageCount").val()
     //        ,layout: ['count', 'prev', 'page', 'next', 'limit', 'refresh', 'skip']
     //        ,jump: function(obj){
     //        	console.log(obj)
     //        	Live.liveRoomUserList(obj.curr,id,obj.limit)
     //        }
   	// 	 })
    	// })
	//
	// },

	btn_back:function(){
        $(".timeComponent").hide();
		$("#incomeList").show();
		$("#liveRoomList").show();
		$("#addLiveRoom").hide();
		$("#liveRoomMsg").hide();
		$("#liveRoomUser").hide();
		$("#liveRoomGiftWater").hide();
	}

}