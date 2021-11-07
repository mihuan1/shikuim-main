var page=0;
var sum=0;
var lock=0;
var updateId="";// 修改音乐的Id
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

    //短视频音乐列表
    var tableIns = table.render({

      elem: '#music_table'
      ,url:request("/console/musicList")
      ,id: 'music_table'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {field: 'name', title: '音乐名称',sort: true,width:150}
          ,{field: 'nikeName', title: '创作人',sort: true,width:150}
          ,{field: 'cover', title: '封面图地址',sort: true, width:250}
          ,{field: 'path', title: '音乐地址',sort: true, width:250}
          ,{field: 'length', title: '音乐长度',sort: true, width:120}
          ,{field: 'useCount', title: '使用数量',sort: true, width:120}
          ,{fixed: 'right', width: 250,title:"操作", align:'left', toolbar: '#musicListBar'}
        ]]
		,done:function(res, curr, count){
			checkRequst(res);
			if(count==0&&lock==1){
                // layui.layer.alert("暂无数据",{yes:function(){
                //   renderTable();
                //   layui.layer.closeAll();
                // }});
                layer.msg("暂无数据",{"icon":2});
            	renderTable();
              }
              lock=0; 
		}
    });


    //列表操作
    table.on('tool(music_table)', function(obj){
         var layEvent = obj.event,
            data = obj.data;
            console.log(data);
         if(layEvent === 'delete'){// 删除短视频音乐
         
         	Music.deleteMusic(data.id);
         }else if(layEvent === 'update'){// 更新短视频音乐
         	Music.updateMusic(data);
         }
     });

    //搜索
    $(".search_live").on("click",function(){
       
            table.reload("music_table",{
                where: {
                    keyword : $("#musicName").val()  //搜索的关键字
                },
                page: {
                    curr: 1 //重新从第 1 页开始
                }
            })
            lock=1;
        $("#musicName").val("");
    });
});
//重新渲染表单
function renderTable(){
  layui.use('table', function(){
   var table = layui.table;//高版本建议把括号去掉，有的低版本，需要加()
   // table.reload("user_list");
   table.reload("music_table",{
        where: {
            keyword : $("#musicName").val()  //搜索的关键字
        },
        page: {
            curr: 1 //重新从第 1 页开始
        }
    })
  });
 }

var Music={
	addMusic:function(){
		$("#music_div").hide();
		$("#addMusic").show();
		$("#musicName_add").val("");
        $("#musicNickName").val("");
        $("#musicCover").val("");
        $("#musicPath").val("");
        $("#uploadMusic").val("");
        $("#uploadCover").val("");
        $("#uploadMusicCover").attr("action",Config.getConfig().uploadUrl+"/upload/UploadMusicServlet");
		$("#uploadMusicPath").attr("action",Config.getConfig().uploadUrl+"/upload/UploadMusicServlet");
		$("#uploadMusicPath_update").attr("action",Config.getConfig().uploadUrl+"/upload/UploadMusicServlet");
		$("#uploadMusicCover_update").attr("action",Config.getConfig().uploadUrl+"/upload/UploadMusicServlet");
	},
	selectCover:function(){
		$("#uploadCover").click();
	},
	selectMusic:function(){
		$("#uploadMusic").click();
	},
	uploadCover:function(){
		var file=$("#uploadCover")[0].files[0];
		console.log(file)
		$("#uploadMusicCover").ajaxSubmit(function(data){
			var obj = eval("(" + data + ")");
			// console.log(obj.data);
			$("#musicCover").html(obj.data);
			$("#musicCover").show();
		});
	},
	updateUploadCover:function(){
		var file=$("#uploadCover")[0].files[0];
		console.log(file);
		$("#uploadMusicCover_update").ajaxSubmit(function(data){
			var obj = eval("(" + data + ")");
			// console.log(obj.data);
			$("#musicCover_update").html(obj.data);
			// $("#musicCover").show();
		});
	},
	updateUploadMusic:function(){
		var file=$("#uploadMusic_update")[0].files[0];
		console.log(file)
        //获取录音时长
        var url = URL.createObjectURL(file);
         //经测试，发现audio也可获取视频的时长
        var audioElement = new Audio(url);

        var duration;
        audioElement.addEventListener("loadedmetadata", function (_event) {
            duration = audioElement.duration;
            $("#musicLength_update").html(parseInt(duration));
            console.log("长度"+duration);
        });
        
		$("#uploadMusicPath_update").ajaxSubmit(function(data){
			var obj = eval("(" + data + ")");
			console.log(obj);
			// console.log(obj.data);
			var arr=obj.data.split("/");
			var url="/"+arr[arr.length-3]+"/"+arr[arr.length-2]+"/"+arr[arr.length-1];
			console.log("最后的路径"+url);
			$("#musicPath_update").html("");
			$("#musicPath_update").html(url);
			// $("#musicPath").show();
		});
	},
	uploadMusic:function(){
		var file=$("#uploadMusic")[0].files[0];
		console.log(file)
        //获取录音时长
        var url = URL.createObjectURL(file);
         //经测试，发现audio也可获取视频的时长
        var audioElement = new Audio(url);

        var duration;
        audioElement.addEventListener("loadedmetadata", function (_event) {
            duration = audioElement.duration;
            $("#musicLength").html(parseInt(duration));
            console.log("长度"+duration);
        });
        
		$("#uploadMusicPath").ajaxSubmit(function(data){
			var obj = eval("(" + data + ")");
			console.log(obj);
			// console.log(obj.data);
			var arr=obj.data.split("/");
			var url="/"+arr[arr.length-3]+"/"+arr[arr.length-2]+"/"+arr[arr.length-1];
			console.log("最后的路径"+url);
			$("#musicPath").html(url);
			$("#musicPath").show();
		});
	},
	selectUpdateCover:function(){
		$("#uploadCover_update").click();
	},
	selectUpdateMusic:function(){
		$("#uploadMusic_update").click();
	},
	commit_addMusic:function(){
		Common.invoke({
			url:request('/console/addMusic'),
			data:{
				cover:$("#musicCover").html(),
				length:$("#musicLength").html(),
				name:$("#musicName_add").val(),
				nikeName:$("#musicNickName").val(),
				path:$("#musicPath").html(),
				useCount:0
			},
			success:function(result){
				if(result.resultCode==1){
					$("#musicName_add").val("");
			        $("#musicNickName").val("");
			        $("#musicCover").html("");
			        $("#musicPath").html("");
			        $("#uploadMusic").val("");
        			$("#uploadCover").val("");
        			$("#music_div").show();
					$("#addMusic").hide();
					layui.layer.alert("新增成功");
					layui.table.reload("music_table");
					$("#musicLength").html("");
				}
			}

		})
	},
	// 删除音乐
	deleteMusic:function(id){
		layer.confirm('确定删除该条评论？',{icon:3, title:'提示信息'},function(index){
			Common.invoke({
				url:request('/console/deleteMusic'),
				data:{
					musicInfoId:id
				},
				success:function(result){
					if(result.resultCode==1){
						layui.layer.alert("删除成功");
						layui.table.reload("music_table");
					}
				}
			})
		})
		
	},
	// 修改音乐
	updateMusic:function(data){
		$("#musicList").hide();
		$("#updateMusic").show();
		$("#musicName_update").val(data.name);
		$("#musicNickName_update").val(data.nikeName);
		$("#musicLength_update").html(data.length);
		$("#musicCover_update").html(data.cover);
		$("#musicPath_update").html(data.path);

		$("#uploadMusicCover").attr("action",Config.uploadMusicUrl);
		$("#uploadMusicPath").attr("action",Config.uploadMusicUrl);
		$("#uploadMusicPath_update").attr("action",Config.uploadMusicUrl);
		$("#uploadMusicCover_update").attr("action",Config.uploadMusicUrl);
		updateId=data.id;
	},
	// 提交修改音乐
	commit_updateMusic:function(){
		Common.invoke({
			url:request('/console/updateMusic'),
			data:{
				id:updateId,
				cover:$("#musicCover_update").html(),
				length:$("#musicLength_update").html(),
				name:$("#musicName_update").val(),
				nikeName:$("#musicNickName_update").val(),
				path:$("#musicPath_update").html()
			},
			success:function(result){
				if(result.resultCode==1){
					layui.layer.alert("修改成功");
					$("#musicList").show();
					$("#updateMusic").hide();
					layui.table.reload("music_table");
				}
			}
		})
	},
	btn_back:function(){
		$("#music_div").show();
		$("#musicList").show();
		$("#addMusic").hide();
		$("#updateMusic").hide();
	}
}