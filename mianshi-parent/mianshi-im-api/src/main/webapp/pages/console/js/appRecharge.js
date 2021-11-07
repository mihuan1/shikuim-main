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
        $(".appRecharge_div").empty();
    }
    // APP充值列表列表
    var tableIns = table.render({

        elem: '#appRecharge_table'
        ,url:request("/console/systemRecharge")
        ,page: true
        ,curr: 0
        ,limit:Common.limit
        ,limits:Common.limits
        ,groups: 7
        ,cols: [[ //表头
            {field: 'id', title: '充值记录Id',sort: true,width:150}
            ,{field: 'tradeNo', title: '交易单号',sort: true,width:180}
            ,{field: 'userId', title: '用户Id',sort: true, width:120}
            ,{field: 'userName', title: '用户昵称',sort: true, width:120,templet : function (d) {
                    var userName;
                    (d.userName == "" ? userName = "测试用户" : userName = d.userName);
                    return userName;
                }}
            ,{field: 'money', title: '充值金额',sort: true, width:120}
            ,{field: 'desc', title: '备注',sort: true, width:120}
            ,{field: 'payType', title: '支付方式',sort: true, width:120,templet : function (d) {
                    var payTypeMsg;
                    (d.payType == 1 ? payTypeMsg = "支付宝支付" : (d.payType == 2) ? payTypeMsg = "微信支付" : (d.payType == 3) ? payTypeMsg = "余额支付" : (d.payType == 4) ? payTypeMsg = "系统支付": payTypeMsg = "其他方式支付")
                    return payTypeMsg;
                }}
            ,{field: 'status', title: '交易状态',sort: true, width:120,templet : function (d) {
                    var statusMsg;
                    (d.status == 0 ? statusMsg = "创建" : (d.status == 1) ? statusMsg = "支付完成" : (d.status == 2) ? statusMsg = "交易完成" :(d.status == -1) ? statusMsg = "交易关闭" : statusMsg = "关闭")
                    return statusMsg;
                }}
            ,{field: 'type', title: '类型',sort: true, width:150, templet : function (d) {
					var statusMsg;
            		(d.type == 1 ? statusMsg = "APP充值" : (d.type == 3) ? statusMsg = "后台充值" : statusMsg = "其他方式充值")
					return statusMsg;
                }}
            ,{field: 'time',title:'充值时间',width:195,templet: function(d){
                    return UI.getLocalTime(d.time);
                }}
        ]]
        ,done:function(res, curr, count){
            checkRequst(res);
            // 初始化时间控件
            ///layui.form.render('select');
            //日期范围
            layui.laydate.render({
                elem: '#appRechargeMsgDate'
                ,range: "~"
                ,done: function(value, date, endDate){  // choose end
                    //console.log("date callBack====>>>"+value); //得到日期生成的值，如：2017-08-18
                    var startDate = value.split("~")[0];
                    var endDate = value.split("~")[1];


                    // Count.loadGroupMsgCount(roomJId,startDate,endDate,timeUnit);
                    table.reload("appRecharge_table",{
                        page: {
                            curr: 1 //重新从第 1 页开始
                        },
                        where: {
                            // userId : data.userId,  //搜索的关键字
                            startDate : startDate,
                            endDate : endDate
                        }
                    })
                }
                ,max: 0
            });
            $(".current_total").empty().text((0==res.total ? 0:res.total));
            if(localStorage.getItem("IS_ADMIN")==0){
                $(".btn_addLive").hide();
                $(".delete").hide();
                $(".chatMsg").hide();
                $(".member").hide();
            }
        }
    });

    // 列表操作
    table.on('tool(redEnvelope_table)', function(obj){
        var layEvent = obj.event,
            data = obj.data;
        console.log(data);
        if(layEvent === 'delete'){// 红包领取详情

        }
    });

    //首页搜索
    $(".search_live").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();
        table.reload("appRecharge_table",{
            where: {
                userId : $("#userId").val(), //搜索的关键字
                type :$("#complaint_select").val(),
            },
            page: {
                curr: 1 //重新从第 1 页开始
            }
        })
        $("#userId").val("");
        $("#complaint_select").val(0);
    });


});
var appRecharge={

    // 删除账单记录


    btn_back:function(){
        $("#redEnvelope").show();
        $("#receiveWater").hide();

    }

}
