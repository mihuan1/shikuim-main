var page=0;
var sum=0;
$(function(){

	Gift.giftList(0);
	Gift.limit();

})
// var button='<button onclick="Gift.giftList(0)" class="layui-btn layui-btn-primary layui-btn-sm" style="margin-top: 21%;margin-left: 20%;"><<返回</button>';
var Gift={
	// 礼物列表
	giftList:function(e,pageSize){
		html="";
		if(e==undefined){
			e=0;
		}else if(pageSize==undefined){
			pageSize=15;
		}
		$.ajax({
			type:'POST',
			url:request('/console/giftList'),
			data:{
				pageIndex:(e==0?"0":e-1),
				pageSize:pageSize
			},
			dataType:'json',
			async:false,
			success:function(result){
				checkRequst(result);
				if(result.data.pageData!=null){
					$("#pageCount").val(result.data.allPageCount);
					for(var i=0;i<result.data.pageData.length;i++){
						html+="<tr><td>"+result.data.pageData[i].name+"</td><td>"+result.data.pageData[i].photo
						+"</td><td>"+result.data.pageData[i].price+"</td><td>"+result.data.pageData[i].type
						+"</td><td><button onclick='Gift.deleteGift(\""+result.data.pageData[i].giftId+"\")' class='layui-btn layui-btn-danger layui-btn-xs delete'>删除</button></td></tr>";
					}
					if($("#giftName").val()==""||$("#giftName").val()==undefined){
                        $("#giftList_table").empty();
                        $("#giftList_table").append(html);
					}


					if(localStorage.getItem("role")==1 || localStorage.getItem("role")==7){
						$(".add_gift").hide();
						$(".delete").hide();
					}
					$("#giftName").val("");
					$("#gift_list").show();
					$("#addGift").hide();
					$("#giftUrl").val("");
					$("#giftPrice").val("");
					// $("#back").empty();
					// $("#back").append("&nbsp;");
				}
			}
		})
	},
	findGiftList:function(){
		var htm="";
		$.ajax({
			type:'POST',
			url:request('/console/giftList'),
			data:{
				name:$("#giftName").val()
			},
			dataType:'json',
			async:false,
			success:function(result){
				checkRequst(result);
				if(result.data.pageData!=null){
					$("#pageCount").val(result.data.allPageCount);
					for(var i=0;i<result.data.pageData.length;i++){
						htm+="<tr><td>"+result.data.pageData[i].name+"</td><td>"+result.data.pageData[i].photo
						+"</td><td>"+result.data.pageData[i].price+"</td><td>"+result.data.pageData[i].type
						+"</td><td><button onclick='Gift.deleteGift(\""+result.data.pageData[i].giftId+"\")' class='layui-btn layui-btn-danger layui-btn-xs delete'>删除</button></td></tr>";
					}
					$("#giftList_table").empty();
					$("#giftList_table").append(htm);
					if(localStorage.getItem("IS_ADMIN")==0){
						$(".add_gift").hide();
						$(".delete").hide();
					}
					$("#gift_list").show();
					$("#addGift").hide();
					$("#giftUrl").val("");
					$("#giftPrice").val("");
                    Gift.limit(1);

				}
			}
		})
	},
	// 删除礼物
	deleteGift:function(giftId){
		layer.confirm('确定删除该礼物？',{icon:3, title:'提示信息'},function(index){
			Common.invoke({
			url:request('/console/delete/gift'),
			data:{
				giftId:giftId
			},
			success:function(result){
				if(result.resultCode==1){
					layer.alert("删除礼物成功");
					Gift.giftList(0);
					Gift.limit();// 初始化分页
				}
			}
		})
		});
		
	},
	// 新增礼物
	addGift:function(){
		$("#gift_list").hide();
		$("#addGift").show();
        $("#add_giftName").val("")
        $("#giftUrl").val("")
        $("#giftPrice").val("")
		// $("#back").empty();
		// $("#back").append(button);
	},
	commit_addGift:function(){
		console.log($("#add_giftName").val());
		if($("#add_giftName").val()==""){
			layer.alert("请输入礼物名称");
			return;
		}else if($("#giftUrl").val()==""){
			layer.alert("请输入礼物路径");
			return;

		}else if($("#giftPrice").val()==""){
			layer.alert("请输入礼物价格");
			return;
		}else if($("#giftPrice").val()!=""){
            // 充值金额（正整数）的正则校验
            if(!/^(?!00)(?:[0-9]{1,3}|1000)$/.test($("#giftPrice").val())){
                layer.msg("礼物价格必须为 1-1000 的整数",{"icon":2});
                return;
            }
        }else if($("#giftType").val()==""){
			layer.alert("请选择礼物类型");
			return;
		}
		Common.invoke({
			url:request('/console/add/gift'),
			data:{
				name:$("#add_giftName").val(),
				photo:$("#giftUrl").val(),
				price:$("#giftPrice").val(),
				type:$("#giftType").val()
			},
			success:function(result){
				if(result.resultCode==1){
					layer.alert("新增礼物成功");
					Gift.giftList(0);
					Gift.limit();
					$("#addGift").hide();
					$("#gift_list").show();
				}
			}
		})
	},

	//分页
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
                    if(index==1){
                    	Gift.giftList(1,obj.limit);
                    	index=0;
                    }else {
                    	Gift.giftList(obj.curr,obj.limit);
                    }
                }
            })
        })
    }
}