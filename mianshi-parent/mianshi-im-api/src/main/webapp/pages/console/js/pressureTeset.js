var page=0;

layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

	//群组列表
    var tableIns = table.render({
      elem: '#room_table'
      ,url:request("/console/roomList")
      ,id: 'room_table'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {type:'checkbox',fixed:'left'}// 多选
          ,{field: 'name', title: '群组名称'}
          ,{field: 'userId', title: '创建者Id', sort: true}
          ,{field: 'nickname', title: '创建者昵称'}
          ,{field: 'userSize', title: '群人数',sort: true}
          ,{field: 'createTime',title:'创建时间',sort: true,templet: function(d){
          		return UI.getLocalTime(d.createTime);
          }}
        ]]
		,done:function(res, curr, count){
          checkRequst(res);
          $("#result").hide();
          if(localStorage.getItem("role")==1) {
              $(".sendMsg").hide();
          }

		}
    });
    // console.log(layer.alert(JSON.stringify(table.checkStatus())));
    //搜索
    $(".search_live").on("click",function(){
       
            table.reload("room_table",{
                page: {
                    curr: 1 //重新从第 1 页开始
                },
                where: {
                    keyWorld : $("#roomName").val()  //搜索的关键字
                }
            })
        $("#roomName").val("");
        
    });
    // 给选中的群组赋值
    var arr = new Array();
    var checkData;
    table.on('checkbox(room_table)', function(obj){

        var checkStatus = layui.table.checkStatus('room_table'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+checkStatus.data) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选

        checkData = checkStatus.data;
    });


    //搜索
    $(".sendMsg").on("click",function(){
        console.log(JSON.stringify("checkData : "+checkData));
        if(null == checkData || undefined == checkData){
            layer.alert("请先选择群组")
            return;
        }
        for(var j = 0; j<checkData.length; j++){
            console.log(j+": "+JSON.stringify(checkData[j]));
            arr.push(checkData[j].jid);
        }
        console.log("JID:"+arr);
        $("#result").hide();
        /*if(0 == arr.length){
            layer.alert("请先选择群组")
            return;
        }*/
        // var num = $(".timeInterval").val()
        if("" == $(".timeInterval").val()){
            layer.alert("请填写消息时间间隔")
            return;
        }
        Common.invoke({
            url : request('/console/pressureTest'),
            data : {
                "checkNum" :$(".sendPeopleNum").val(),
                "sendMsgNum" :$(".sendTotal").val(),
                "roomJid":arr.join(","),
                "timeInterval":$(".timeInterval").val()
            },
            successMsg : "发送成功，在手机端查看发送详情",
            errorMsg :  "发送失败，请稍后重试",
            success : function(result) {
                arr=[];
                console.log("返回数据："+JSON.stringify(result.data));
                // layui.layer.alert("返回数据："+JSON.stringify(result.data));
                $("#result").show();
                $(".timeStr").val(result.data.timeStr);
                $(".sendTotalNum").val(result.data.sendAllCount);
                $(".sendTotalNumTime").val(result.data.timeCount);

            },
            error : function(result) {

            }
        });
    });


})
	

	

