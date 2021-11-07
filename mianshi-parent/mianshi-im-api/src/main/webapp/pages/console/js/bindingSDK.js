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
        $(".bindingSDK_div").empty();
    }

    // 转账列表
    var tableIns = table.render({

        elem: '#bindingSDK_table'
        ,url:request("/console/getSdkLoginInfoList")
        ,page: true
        ,curr: 0
        ,limit:Common.limit
        ,limits:Common.limits
        ,groups: 7
        ,cols: [[ //表头
            {field: 'id', title: '绑定Id',sort: true,width:200}
            ,{field: 'userId', title: '绑定用户Id',sort: true,width:120}
            ,{field: 'type', title: '第三方登录类型',sort: true, width:150,templet : function (d) {
             	return (d.type==1?"QQ":d.type==2?"微信":"其他");       
            }}
            ,{field: 'loginInfo', title: '登录标识',sort: true, width:200}
            ,{field: 'createTime', title: '绑定时间',sort: true, width:200,templet : function (d) {
                    return UI.getLocalTime(d.createTime);
            }}
            ,{fixed: 'right', width: 100,title:"操作", align:'left', toolbar: '#bindingSDKListBar'}
            
        ]]
        ,done:function(res, curr, count){
            checkRequst(res);
        }
    });

    //列表操作
    table.on('tool(bindingSDK_table)', function(obj){
        var layEvent = obj.event,
            data = obj.data;

         if(layEvent === 'delete'){// 删除
         	layer.confirm('确定解绑指定用户',{icon:3, title:'提示消息',yes:function () {
	            layer.closeAll();
	            SDK.deleteBindingSDK(data.id);
	            obj.del();

	        }});
         }
     });

    //首页搜索
    $(".search_bindingSDK").on("click",function(){
        table.reload("bindingSDK_table",{
            where: {
                userId : $(".bindingSDK_keyword").val() //搜索的关键字
            },
            page: {
                curr: 1 //重新从第 1 页开始
            }
        })
        $(".bindingSDK_keyword").val("");
    });


});
//重新渲染表单
function renderTable(){
  layui.use('table', function(){
	   var table = layui.table;//高版本建议把括号去掉，有的低版本，需要加()
	   table.reload("bindingSDK_table");
  });
 }

var SDK={
	// 删除第三方绑定
	deleteBindingSDK:function(id){
		Common.invoke({
			url:request('/console/deleteSdkLoginInfo'),
			data:{
				id:id
			},
			success:function(result){
				if(result.resultCode==1)
					layui.layer.alert("删除成功");
				renderTable();
			}
		})
	}
}