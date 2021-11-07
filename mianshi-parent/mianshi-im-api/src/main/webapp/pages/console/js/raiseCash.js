var page=0;
var sum=0;

function auditDialog(data, msg) {
    layer.open({
        type: 1
        ,title: false //不显示标题栏
        ,closeBtn: true
        ,area: '300px;'
        ,shade: 0.8
        ,id: 'popup_audit' //设定一个id，防止重复弹出
        ,btn: ['审核通过', '审核拒绝']
        ,btnAlign: 'c'
        ,moveType: 1 //拖拽模式，0或者1
        ,content: msg
        ,yes: function(index, layero){
            Common.invoke({
                url :request('/console/recharge/audit'),
                data : {
                    rechargeId:data.id,
                    auditStatus:0,
                    desc: $("#popupAuditDialog_desc").val()
                },
                successMsg : "操作成功",
                errorMsg :  "操作失败，请稍后重试",
                success : function(result) {
                    if (result && 1 != result.resultCode) {
                        layer.alert(result.resultMsg);
                    } else {
                        // 更新
                        layui.table.reload("raiseCash_table")
                    }
                },
                error : function(result) {
                }
            });
            layer.close(index);
        }
        ,btn2: function(index, layero){
            Common.invoke({
                url :request('/console/recharge/audit'),
                data : {
                    rechargeId:data.id,
                    auditStatus:2,
                    desc: $("#popupAuditDialog_desc").val()
                },
                successMsg : "操作成功",
                errorMsg :  "操作失败，请稍后重试",
                success : function(result) {
                    if (result && 1 != result.resultCode) {
                        layer.alert(result.resultMsg);
                    } else {
                        // 更新
                        layui.table.reload("raiseCash_table")
                    }
                },
                error : function(result) {
                }
            });
            layer.close(index);
            //return false 开启该代码可禁止点击该按钮关闭
        }
    });
}

function initPopAudit(data) {
    Common.invoke({
        url :request('/console/recharge/audit/bankCardInfo'),
        data : {
            bankCardId:data.bankCardId
        },
        errorMsg :  "操作失败，请稍后重试",
        success : function(result) {
            if (result && 1 != result.resultCode) {
                layer.alert(result.resultMsg);
            } else {
                var userName = result.data.userName ? result.data.userName : "";
                var cardNo = result.data.cardNo ? result.data.cardNo : "";
                var bankBrandName = result.data.bankBrandName ? result.data.bankBrandName : "";
                var openBankAddr = result.data.openBankAddr ? result.data.openBankAddr : "";
                var isDeleted = result.data.isDeleted ? (1 == result.data.isDeleted ? "是" : "否") : "";
                var msg = '<div id="popupAuditDialog" style="padding: 50px; line-height: 22px; background-color: #393D49; color: #fff; font-weight: 300;">' +
                    '姓名: ' + userName + '<br><br>卡号: ' + cardNo + '<br><br>银行名: ' + bankBrandName + '<br><br>开户行地址: ' + openBankAddr + '<br><br>卡是否删除: ' + isDeleted + '<div class="layui-input-inline">' +
                    '<br>备注: <input class="layui-input" id="popupAuditDialog_desc" placeholder="拒绝请输入备注" type="text">\n' +
                    '</div></div>'
                auditDialog(data, msg);
            }
        },
        error : function(result) {

        }
    });
}

layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;


    // 用户提现列表
    var tableIns = table.render({

        elem: '#raiseCash_table'
        ,url:request("/console/systemRecharge")+"&type="+2
        ,id: 'raiseCash_table'
        ,page: true
        ,curr: 0
        ,limit:Common.limit
        ,limits:Common.limits
        ,groups: 7
        ,cols: [[ //表头
            {field: 'id', title: '提现记录Id',sort: true,width:150}
            ,{field: 'tradeNo', title: '交易单号',sort: true,width:180}
            ,{field: 'userId', title: '用户Id',sort: true, width:120}
            ,{field: 'userName', title: '用户昵称',sort: true, width:120}
            ,{field: 'money', title: '提现金额',sort: true, width:120}
            ,{field: 'serviceCharge', title: '手续费',sort: true, width:120}
            ,{field: 'operationAmount', title: '实际金额',sort: true, width:120}
            ,{field: 'currentBalance', title: '账户余额',sort: true, width:120}
            ,{field: 'desc', title: '备注',sort: true, width:120}
            ,{field: 'payType', title: '支付方式',sort: true, width:120,templet : function (d) {
                    var payTypeMsg;
                    (d.payType == 1 ? payTypeMsg = "支付宝支付" : (d.payType == 2) ? payTypeMsg = "微信支付" : (d.payType == 3) ? payTypeMsg = "余额支付" :(d.payType == 4) ? payTypeMsg = "系统支付" : payTypeMsg = "其他方式支付")
                    return payTypeMsg;
                }}
            ,{field: 'status', title: '交易状态',sort: true, width:120,templet : function (d) {
                    var statusMsg;
                    (d.status == 0 ? statusMsg = "创建" : (d.status == 1) ? statusMsg = "支付完成" : (d.status == 2) ? statusMsg = "交易完成" :(d.status == -1) ? statusMsg = "交易关闭" : statusMsg = "关闭")
                    return statusMsg;
                }}
            ,{field: 'type', title: '类型',sort: true, width:150, templet : function (d) {
					var statusMsg;
            		(d.type == 1 ? statusMsg = "APP充值" : (d.type == 3) ? statusMsg = "后台充值": (d.type == 2) ? statusMsg = "用户提现" : (d.type == 16) ? statusMsg = "后台手工提现" : statusMsg = "其他方式充值")
					return statusMsg;
                }}
            ,{field: 'time',title:'提现时间',width:195,templet: function(d){
                    return UI.getLocalTime(d.time);
                }}
            ,{field: 'bankCardId', title: '银行卡Id'}
            ,{fixed: 'right', width:90, toolbar: '#auditOperation', title: '审核'}
        ]]
        ,done:function(res, curr, count){
            $("[data-field='bankCardId']").css('display','none');
            checkRequst(res);
            // 初始化时间控件
            ///layui.form.render('select');
            //日期范围
            layui.laydate.render({
                elem: '#raiseCashMsgDate'
                ,range: "~"
                ,done: function(value, date, endDate){  // choose end
                    //console.log("date callBack====>>>"+value); //得到日期生成的值，如：2017-08-18
                    var startDate = value.split("~")[0];
                    var endDate = value.split("~")[1];


                    // Count.loadGroupMsgCount(roomJId,startDate,endDate,timeUnit);
                    table.reload("raiseCash_table",{
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

    // 解决隐藏列后排序后数据错误问题
    table.on('sort(LAY-enterpriseemployee-employee-table)', function (obj) {
        table.reload('LAY-enterpriseemployee-employee-table', {
            initSort: obj
        });
    });

    table.on('tool(raiseCash_table)', function (obj) {
        var data = obj.data;
        if (obj.event === 'click_to_audit') {
            initPopAudit(data);
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
        table.reload("raiseCash_table",{
            where: {
                userId : $("#userId").val(), //搜索的关键字
                // type :$("#complaint_select").val(),
            },
            page: {
                curr: 1 //重新从第 1 页开始
            }
        })
        $("#userId").val("");
        // $("#complaint_select").val(0);
    });


});
var appRecharge={

    // 删除账单记录


    btn_back:function(){
        $("#redEnvelope").show();
        $("#receiveWater").hide();

    }

}