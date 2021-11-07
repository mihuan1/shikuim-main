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
        $(".transfer_div").empty();
    }

    // 转账列表
    var tableIns = table.render({

        elem: '#transfer_table'
        ,url:request("/console/transferList")
        ,page: true
        ,curr: 0
        ,limit:Common.limit
        ,limits:Common.limits
        ,groups: 7
        ,cols: [[ //表头
            {field: 'id', title: '转账Id',sort: true,width:200}
            ,{field: 'userId', title: '转账用户Id',sort: true,width:120}
            ,{field: 'userName', title: '转账用户昵称',sort: true, width:150,templet : function (d) {
                    var userName;
                    (d.userName == "" ? userName = "测试用户" : userName = d.userName);
                    return userName;
                }}
            ,{field: 'toUserId', title: '收取用户Id',sort: true, width:120}
            ,{field: 'money', title: '转账金额',sort: true, width:120}
            ,{field: 'remark', title: '转账说明',sort: true, width:150,templet:function (d) {
                    var remarkInfo;
                    "null" == d.remark ? remarkInfo="" : remarkInfo = d.remark;
                    return remarkInfo;
                }}
            ,{field: 'status', title: '转账状态',sort: true, width:120,templet : function (d) {
                    var statusMsg;
                    (d.status == 1 ? statusMsg = "发出" : (d.status == 2) ? statusMsg = "已收款" : statusMsg = "已退款")
                    return statusMsg;
                }}
            ,{field: 'createTime', title: '转账时间',sort: true, width:200,templet : function (d) {
                    return UI.getLocalTime(d.createTime);
                }}
            
        ]]
        ,done:function(res, curr, count){
            checkRequst(res);
            // 初始化时间控件
            ///layui.form.render('select');
            //日期范围
            layui.laydate.render({
                elem: '#transferMsgDate'
                ,range: "~"
                ,done: function(value, date, endDate){  // choose end
                    //console.log("date callBack====>>>"+value); //得到日期生成的值，如：2017-08-18
                    var startDate = value.split("~")[0];
                    var endDate = value.split("~")[1];


                    // Count.loadGroupMsgCount(roomJId,startDate,endDate,timeUnit);
                    table.reload("transfer_table",{
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

    //首页搜索
    $(".search_transfer").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();
        table.reload("transfer_table",{
            where: {
                userId : $(".transfer_keyword").val() //搜索的关键字
            },
            page: {
                curr: 1 //重新从第 1 页开始
            }
        })
        $(".transfer_keyword").val("");
    });


});