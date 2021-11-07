var apiLogIds = new Array();
var currentPageIndex;// 当前页码数
var currentCount;// 当前总数
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

	//接口日志列表
    var tableIns = table.render({

      elem: '#sysApiLog_table'
      ,toolbar: '#toolbarApilog'
      ,url:request("/console/ApiLogList")
      ,id: 'sysApiLogs_table'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {type:'checkbox',fixed:'left'}// 多选
           ,{field: 'apiId', title: '接口Id',width:200}
           ,{field: 'totalTime', title: '耗时(毫秒)',sort: true, width:100}
           ,{field: 'time',title:'调用时间',sort: true, width:160,templet: function(d){
          		// return Common.formatDate(d.time,"yyyy-MM-dd hh:mm:ss");
          		return UI.getLocalTime(d.time);
          }}
          ,{field: 'fullUri', title: '完整url', width:370}
          ,{field: 'userId', title: '调用者Id',width:100}
          ,{field: 'clientIp', title: '调用端ip',sort: true, width:120}
          ,{field: 'userAgent', title: '调用端信息',sort: true, width:200}
          ,{field: 'stackTrace', title: '错误信息',sort: true, width:200}
          ,{fixed: 'right', width: 80,title:"操作", align:'left', toolbar: '#sysApiLog_Bar'}
        ]]
		,done:function(res, curr, count){
      checkRequst(res);
			$(".group_name").val("");
			if(localStorage.getItem("role")==1){
                $(".checkDeleteUsersFriends").hide();
                $(".del").remove();
                $(".deleteSevenDaysAgoLogs").hide();
		    }
            var pageIndex = tableIns.config.page.curr;//获取当前页码
            var resCount = res.count;// 获取table总条数
            currentCount = resCount;
            currentPageIndex = pageIndex;
		}
  });


  //列表操作
  table.on('tool(sysApiLogs_table)', function(obj){
      var layEvent = obj.event,
          data = obj.data;
          console.log("apiLogId ======>>> "+data.id);
      if(layEvent === 'del'){ //删除
          ApiLog.toolbarApilogImpl(data.id,1);
      }

  });


    //搜索
    $(".search_sysApiLog").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();
        table.reload("sysApiLogs_table",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
                keyWorld : $(".sysApiLog_name").val()  //搜索的关键字
            }
        })
    });

    // 删除七天前的日志
    $(".deleteSevenDaysAgoLogs").on("click",function(){

        layer.confirm('确定删除七天前的接口日志？',{icon:3, title:'提示信息'},function(index){

            Common.invoke({
                url : request('/console/delApiLog'),
                data : {
                    'type' : 1
                },
                successMsg : "删除接口日志成功",
                errorMsg : "删除接口日志失败,请稍后重试",
                success : function(result) {
                    if (1 == result.resultCode){
                        layui.table.reload("sysApiLogs_table");
                    }
                },
                error : function(result) {
                }
            });

            layer.closeAll('dialog');

        });

    });

})
var ApiLog = {
    toolbarApilog:function () {
        // 多选操作
        var checkStatus = layui.table.checkStatus('sysApiLogs_table'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+checkStatus.data) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选
        for (var i = 0; i < checkStatus.data.length; i++){
            apiLogIds.push(checkStatus.data[i].id);
        }
        if(0 == checkStatus.data.length){
            layer.msg("请勾选要删除的行");
            return;
        }
        console.log(apiLogIds);
        ApiLog.toolbarApilogImpl(apiLogIds.join(","),checkStatus.data.length);
    },
    toolbarApilogImpl:function (apiLogId,checkLength) {
        layer.confirm('确定删除指定的日志记录',{icon:3, title:'提示消息',yes:function () {
                Common.invoke({
                    url:request('/console/delApiLog'),
                    data:{
                        apiLogId :apiLogId,
                    },
                    success:function(result){
                        if(result.resultCode==1){
                            layer.msg("删除成功",{"icon":1});
                            apiLogIds = [];
                            // layui.table.reload("sysApiLogs_table");
                            Common.tableReload(currentCount,currentPageIndex,checkLength,"sysApiLogs_table");
                        }
                    }
                })
            },btn2:function () {
                apiLogIds = [];
            },cancel:function () {
                apiLogIds = [];
            }});
    }

}

	
	
