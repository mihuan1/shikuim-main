var page=0;
var sum=0;
var lock=0;
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;


	console.log("页面加载");
    // 红包列表
    var tableIns = table.render({

        elem: '#redEnvelope_table'
        ,url:request("/console/redPacketList")
        ,id: 'redEnvelope_table'
        ,page: true
        ,curr: 0
        ,limit:Common.limit
        ,limits:Common.limits
        ,groups: 7
        ,cols: [[ //表头
            {field: 'userId', title: '用户Id',sort: true,width:150}
            ,{field: 'userName', title: '昵称',sort: true,width:180}
            ,{field: 'money', title: '红包金额',sort: true, width:120}
            ,{field: 'count', title: '红包个数',sort: true, width:120}
            ,{field: 'status', title: '状态',sort: true, width:150, templet : function (d) {
					var statusMsg;
            		(d.status == 1 ? statusMsg="发出" : (d.status == 2) ? statusMsg = "已领完" :(d.status == -1) ? statusMsg = "已退款" : (d.status == 3) ? statusMsg = "未领完退款" : "")
					return statusMsg;
                }}
            ,{field: 'sendTime',title:'发送时间',width:195,templet: function(d){
                    return UI.getLocalTime(d.sendTime);
                }}
            ,{fixed: 'right', width: 250,title:"操作", align:'left', toolbar: '#redPageListBar'}
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

            if(localStorage.getItem("role")==1){
                $(".receiveWaterInfo").hide();
            }
        }
    });

    // 列表操作
    table.on('tool(redEnvelope_table)', function(obj){
        var layEvent = obj.event,
            data = obj.data;
        console.log(data);
        if(layEvent === 'receiveWaterInfo'){// 红包领取详情
            var tableIns1 = table.render({
                elem: '#receiveWater_table'
                ,url:request("/console/receiveWater")+"&redId="+data.id
                ,id: 'receiveWater_table'
                ,page: true
                ,curr: 0
                ,limit:10
                ,limits:[10,20,30,40,50,100,1000,10000]
                ,groups: 7
                ,cols: [[ //表头
                    {field: 'id', title: '领取流水Id',sort: true}
                    ,{field: 'redId', title: '红包Id',sort: true}
                    ,{field: 'sendName', title: '发送红包用户昵称',sort: true}
                    ,{field: 'userName', title: '领取红包用户昵称',sort: true}
                    ,{field: 'reply', title: '红包回复语'}
                    ,{field: 'money', title: '领取金额',sort: true}
                    ,{field: 'time', title: '领取时间',sort: true,templet: function(d){
                            return UI.getLocalTime(d.time);
                        }}]]
                ,done:function(res, curr, count){
                    checkRequst(res);
                    $("#redEnvelope").hide();
                    $("#receiveWater").show();
                }
            });
        }
    });

    //首页搜索
    $(".search_live").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();
        table.reload("redEnvelope_table",{
            where: {
                userName : $("#toUserName").val()  //搜索的关键字
            },
            page: {
                curr: 1 //重新从第 1 页开始
            }
        })
        lock=1;
        $("#toUserName").val("");
    });

});

//重新渲染表单
function renderTable(){
  layui.use('table', function(){
   var table = layui.table;//高版本建议把括号去掉，有的低版本，需要加()
   // table.reload("user_list");
   table.reload("redEnvelope_table",{
       where: {
                userName : $("#toUserName").val()  //搜索的关键字
            },
            page: {
                curr: 1 //重新从第 1 页开始
            }
    })
  });
 }
var Red={

    btn_back:function(){
        $("#redEnvelope").show();
        $("#receiveWater").hide();
    }

}