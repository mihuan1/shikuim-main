layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;
		
		//日志列表
	    var tableIns = table.render({

	      elem: '#openCheckLog_table'
	      ,url:request("/console/checkLogList")
	      ,id: 'openCheckLog_table'
	      ,page: true
	      ,curr: 0
          ,limit:Common.limit
          ,limits:Common.limits
	      ,groups: 7
	      ,cols: [[ //表头
	           {field: 'accountId', title: '申请用户Id',width:120}
	          ,{field: 'appId', title: 'appId',width:150}
	          ,{field: 'operateUser', title: '操作用户', width:150}
	          ,{field: 'status', title: '审核结果',sort: true, width:150,templet:function(d){
	          		return (d.status==1?"审核通过":d.status==2?"审核失败":d.status==0?"审核中":"禁用");
	          }}
	          ,{field: 'reason', title: '审核回馈',sort: true, width:200} 
	          ,{field: 'createTime',title:'操作时间',sort: true, width:200,templet: function(d){
	          		return UI.getLocalTime(d.createTime);
	          }}
	          ,{fixed: 'right', width: 200,title:"操作", align:'left', toolbar: '#openCheckLogListBar'}
	        ]]
			,done:function(res, curr, count){
				checkRequst(res);
                if(localStorage.getItem("role")==1){
                    $(".del").hide();
				}
			}
	    });
	    // 表格操作
	    table.on('tool(openCheckLog_table)', function(obj){
	        var layEvent = obj.event,
	            data = obj.data;
	        if(layEvent === 'del'){ //删除日志
	        	layer.confirm('确定删除该日志？',{icon:3, title:'提示信息'},function(index){
	        		CheckLog.deleteLog(data.id);
	        		obj.del();
	        	})	
	        }
        })
});

var CheckLog={
	// 删除日志
	deleteLog:function(id){
		Common.invoke({
			url:request('/console/delOpenCheckLog'),
			data:{
				id:id
			},
			success:function(result){
				layui.layer.alert("删除成功");
			}
		})
	}
}