var page=0;
var sum=0;
// var button='<button onclick="Key.keyword_list(0)" class="layui-btn layui-btn-primary layui-btn-sm" style="margin-top: 21%;margin-left: 20%;"><<返回</button>';
$(function(){
	Key.keyword_list(0);
	Key.limit();
})



var Key={
	// 敏感词列表
	keyword_list:function(e,pageSize){
		html="";
		if(e==undefined){
			e=0;
		}else if(pageSize==undefined){
			pageSize=Common.limit;
		}
		$.ajax({
			type:'POST',
			url:request('/console/keywordfilter'),
			data:{
				pageIndex:(e==0?"0":e-1),
				pageSize:pageSize
			},
			dataType:'json',
			async:false,
			success:function(result){
				checkRequst(result);
				if(result.data.pageData.length!=0){
					$("#pageCount").val(result.data.allPageCount);

					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr align='center'><td>"+result.data.pageData[i].word
						+"</td><td>"+UI.getLocalTime(result.data.pageData[i].createTime)+"</td><td><button onclick='Key.deleteKeyWord(\""+result.data.pageData[i].id+"\")' class='layui-btn layui-btn-danger layui-btn-xs delete'>删除</button></td></tr>"
					}
                    if($("#keyName").val()==""||$("#keyName").val()==undefined){
                        $("#keywordList_table").empty();
                        $("#keywordList_table").append(html);
					}
					if(localStorage.getItem("role")==1){
						$(".btn_add").hide();
						$(".delete").hide();
					}
                    $("#keyName").val("");
					$("#keyWordList").show();
					$("#addKeyWord").hide();
					// $("#back").empty();
					// $("#back").append("&nbsp;");

				}
			}
		})
	},
	// 搜索关键词
	findKeyWord:function(){
		html="";
		Common.invoke({
			url:request('/console/keywordfilter'),
			data:{
				word:$("#keyName").val()
			},
			success:function(result){
				if(result.data.pageData!=null){
                    $("#pageCount").val(result.data.allPageCount);
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr align='center'><td>"+result.data.pageData[i].word
						+"</td><td>"+UI.getLocalTime(result.data.pageData[i].createTime)+"</td><td><button onclick='Key.deleteKeyWord(\""+result.data.pageData[i]._id+"\")' class='layui-btn layui-btn-danger layui-btn-xs'>删除</button></td></tr>"
					}
					$("#keywordList_table").empty();
					$("#keywordList_table").append(html);
					// $("#keyName").val("");
					Key.limit(1);
					// $("#back").empty();
					// $("#back").append("&nbsp;");
				}
			}
		});
	},
	// 新增敏感词
	addKeyWord:function(){
		$("#keyWordList").hide();
		$("#addKeyWord").show();
		// $("#back").empty();
		// $("#back").append(button);

	},
	// 提交新增敏感词
	commit_keyWord:function(){ 
		$.ajax({
			type:'POST',
			url:request('/console/addkeyword'),
			data:{
				word:$("#addKeyValue").val()
			},
			async:false,
			success:function(result){
				checkRequst(result);
				if(result.resultCode==1){
					Key.keyword_list();
					Key.limit();
					$("#keyWordList").show();
					$("#addKeyWord").hide();
					$("#addKeyValue").val("");
				}
			}
		})
	},
	// 删除敏感词
	deleteKeyWord:function(id){
		layer.confirm('确定删除该敏感词？',{icon:3, title:'提示信息'},function(index){
			Common.invoke({
				url:request('/console/deletekeyword'),
				data:{
					id:id
				},
				success:function(result){
					if(result.resultCode==1){
                        layer.msg("删除成功",{"icon":1});
						Key.keyword_list();
						Key.limit();
					}
				}
			})
		});
		
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
			,limit:Common.limit
            ,limits:Common.limits
            ,layout: ['count', 'prev', 'page', 'next', 'limit', 'refresh', 'skip']
            ,jump: function(obj){
            	console.log(obj)
            	if(index==1){
            		Key.keyword_list(1,obj.limit)
            		index=0;
            	}else{
            		Key.keyword_list(obj.curr,obj.limit)
            	}
            	
            }
   		 })
 	   })
	}
}