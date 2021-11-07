var page=0;
var sum=0;
var updateId="";
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

    //自定义网站列表
    var tableIns = table.render({

      elem: '#friendsterWebsite_table'
      ,url:request("/console/friendsterWebsiteList")
      ,id: 'friendsterWebsite_table'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {field: 'id', title: 'id',sort: true,width:150}
          ,{field: 'title', title: '网站标题',sort: true,width:150}
          ,{field: 'url', title: '网站网址',sort: true, width:250}
          ,{field: 'icon', title: '网站图标',sort: true, width:250}
          ,{fixed: 'right', title:"操作", align:'left', toolbar: '#friendsterWebsiteListBar'}
        ]]
		,done:function(res, curr, count){
			checkRequst(res);
		}
    });


    //列表操作
    table.on('tool(friendsterWebsite_table)', function(obj){
         var layEvent = obj.event,
            data = obj.data;
            console.log(data);
         if(layEvent === 'delete'){
         	FriendsterWebsite.deleteFriendsterWebsite(data.id);
         }
     });
});
//重新渲染表单
function renderTable() {
    layui.use('table', function () {
        var table = layui.table;//高版本建议把括号去掉，有的低版本，需要加()
        table.reload("friendsterWebsite_table", {
            where: {
                keyword: $("#friendsterWebsiteName").val()  //搜索的关键字
            },
            page: {
                curr: 1 //重新从第 1 页开始
            }
        })
    });
}

var FriendsterWebsite={
    addFriendsterWebsite:function(){
        $("#friendsterWebsite_div").hide();
        $("#addFriendsterWebsite").show();
        $("#title").val("");
        $("#friendsterWebsiteImage").html("");
        $("#url").val("");
        $("#icon").val("");
        $("#uploadFriendsterWebsiteImage").attr("action",Config.getConfig().uploadUrl+"/upload/UploadServlet");
    },
    selectCover:function(){
        $("#icon").click();
    },
    uploadImg:function(){
        var file=$("#icon")[0].files[0];
        console.log(file)
        $("#uploadFriendsterWebsiteImage").ajaxSubmit(function(data){
            var obj = eval("(" + data + ")");
            $("#friendsterWebsiteImage").html(obj.data.images[0].oUrl);
            $("#friendsterWebsiteImage").show();
        });
    },
	commit_addFriendsterWebsite:function(){
		Common.invoke({
			url:request('/console/addFriendsterWebsite'),
			data:{
                title:$("#title").val(),
                url:$("#url").val(),
                icon:$("#friendsterWebsiteImage").html()
            },
			success:function(result){
				if(result.resultCode==1){
					$("#money").val("");
			        $("#qrUrl").val("");
			        $("#qrType").val("");
        			$("#friendsterWebsite_div").show();
					$("#addFriendsterWebsite").hide();
					layui.layer.alert("新增成功");
					layui.table.reload("friendsterWebsite_table");
				}
			}

		})
	},
	// 删除
	deleteFriendsterWebsite:function(id){
		layer.confirm('确定删除该条记录？',{icon:3, title:'提示信息'},function(index){
			Common.invoke({
				url:request('/console/deleteFriendsterWebsite'),
				data:{
					id:id
				},
				success:function(result){
					if(result.resultCode==1){
						layui.layer.alert("删除成功");
						layui.table.reload("friendsterWebsite_table");
					}
				}
			})
		})
		
	},
	btn_back:function(){
		$("#friendsterWebsite_div").show();
		$("#friendsterWebsiteList").show();
		$("#addFriendsterWebsite").hide();
		$("#updateFriendsterWebsite").hide();
	}
}