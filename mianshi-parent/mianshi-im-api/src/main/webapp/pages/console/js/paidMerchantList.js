layui.use(['form', 'layer', 'laydate', 'table', 'laytpl'], function () {
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

    //商户列表
    var tableInMerchant = table.render({
        elem: '#merchant_list_table_body'
        , url: request("/console/paid/merchant/list")
        , id: 'merchant_list_table_body'
        , cols: [[ //表头
            {field: 'id', title: '内部商户id', width: 100}
            , {field: 'vendorMerchantId', title: '商户id', sort: 'true', width: 100}
            , {field: 'payType', title: '支付方式', sort: 'true', width: 100}
            , {field: 'name', title: '商品名称', sort: 'true', width: 100}
            , {field: 'sitename', title: '网站名称', sort: 'true', width: 100}
            , {field: 'sign', title: '密钥', sort: 'true', width: 100}
            , {field: 'payUrl', title: '支付地址url', sort: 'true', width: 100}
            , {field: 'orderQueryUrl', title: '查询订单地址url', sort: 'true', width: 100}
            , {
                field: 'enable', title: '是否启用', sort: 'true', width: 100, templet: function (d) {
                    return d.enable == 0 ? "是" : "否";
                }
            }
            , {fixed: 'right', width: 170, title: "操作", toolbar: '#paidMerchantOperationBar'}
        ]]
        , even: true
        , done: function (res, curr, count) {
            $("[data-field='id']").css('display', 'none');
            checkRequst(res);
        }

    });

    table.on('sort(LAY-enterpriseemployee-employee-table)', function (obj) {
        table.reload('LAY-enterpriseemployee-employee-table', {
            initSort: obj
        });
    });

    //列表操作
    table.on('tool(merchant_list_table)', function (obj) {
        var layEvent = obj.event,
            data = obj.data;

        if (layEvent === 'click_to_enable') { //启用
            Merchant.disEnable(data.id, 0);
        } else if (layEvent === 'click_to_disable') {//禁用
            Merchant.disEnable(data.id, 1);
        } else if (layEvent === 'click_to_del') {//删除
            Merchant.del(data.id);
        }
    });
})


//重新渲染表单
function renderTable() {
    layui.use('table', function () {
        var table = layui.table;//高版本建议把括号去掉，有的低版本，需要加()
        table.reload("merchant_list_table_body")
    });
}


var Merchant = {
    //  新增商户
    addMerchant: function () {
        $("#merchantList").hide();
        $("#addMerchant").show();
        $("#vendorMerchantId").val("");
        $("#payType").val("");
        $("#name").val("");
        $("#sitename").val("");
        $("#secret").val("");
        $("#payUrl").val("");
        $("#orderQueryUrl").val("");
        $("#enable").val("");
        // 重新渲染
        layui.form.render();
        $("#addMerchantTitle").empty();
        $("#addMerchantTitle").append("新增商户");
    },
    // 提交新增用户
    commit_addMerchant: function () {
        var date = $("#birthday").val();
        if ($("#vendorMerchantId").val() == "") {
            layui.layer.alert("请输入商户id");
            return;
        }
        if ($("#payType").val() == "") {
            layui.layer.alert("请选择支付方式");
            return;
        }
        if ($("#name").val() == "") {
            layui.layer.alert("请输入商品名称");
            return;
        }
        if ($("#sitename").val() == "") {
            layui.layer.alert("请输入网站名称");
            return;
        }
        if ($("#sign").val() == "") {
            layui.layer.alert("请输入密钥");
            return;
        }
        if ($("#payUrl").val() == "") {
            layui.layer.alert("请输入支付地址url");
            return;
        }
        if ($("#orderQueryUrl").val() == "") {
            layui.layer.alert("请输入查询订单地址url");
            return;
        }
        if ($("#enable").val() == "") {
            layui.layer.alert("请选择是否启用");
            return;
        }

        $.ajax({
            url: request('/console/paid/merchant/add'),
            data: {
                vendorMerchantId: $("#vendorMerchantId").val(),
                payType: $("#payType").val(),
                name: $("#name").val(),
                sitename: $("#sitename").val(),
                sign: $("#sign").val(),
                payUrl: $("#payUrl").val(),
                orderQueryUrl: $("#orderQueryUrl").val(),
                enable: $("#enable").val()
            },
            dataType: 'json',
            async: false,
            success: function (result) {
                checkRequst(result);
                if (result.resultCode == 1) {
                    layer.alert("添加成功");
                } else {
                    layer.alert(result.resultMsg);
                }

            },
            error: function (result) {
                if (result.resultCode == 0) {
                    layer.alert(result.resultMsg);
                }
            }
        })
    },

    // 商户启用禁用
    disEnable: function (merchantId, disEnable) {
        var confMsg = "";
        (disEnable == 0 ? confMsg = '确定启用该商户？' : confMsg = '确定禁用该商户？');
        layer.confirm(confMsg, {icon: 3, title: '提示信息'}, function (index) {

            Common.invoke({
                url: request('/console/paid/merchant/disEnable'),
                data: {
                    merchantId: merchantId,
                    operate: disEnable
                },
                successMsg: "操作成功",
                errorMsg: "操作失败，请稍后重试",
                success: function (result) {
                    renderTable();
                },
                errorCb: function (result) {
                }
            });
        })
    },
    // 商户删除
    del: function (merchantId) {
        layer.confirm("确认删除该商户？", {icon: 3, title: '提示信息'}, function (index) {

            Common.invoke({
                url: request('/console/paid/merchant/del'),
                data: {
                    merchantId: merchantId
                },
                successMsg: "操作成功",
                errorMsg: "操作失败，请稍后重试",
                success: function (result) {
                    renderTable();
                },
                errorCb: function (result) {
                }
            });
        })
    },
    button_back: function () {
        $("#merchantList").show();
        $("#addMerchant").hide();
        renderTable();
    }

}