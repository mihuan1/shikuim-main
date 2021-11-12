var page=0;
var lock=0;
var userIds = new Array();
var toUserIds = new Array();
var messageIds = new Array();
var nickName;
var userId;
var regeditPhoneOrName;
var currentPageIndex;// 当前页码数
var currentCount;// 当前总数
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;


    //根据系统邀请码类型移除对应邀请码的按钮
    if(localStorage.getItem("registerInviteCode") != 2){
        $(".create_populer_inviteCode").remove();
    }else{
        $(".btn_create_register_InviteCode").remove();
    }

    //用户列表
    var tableInUser = table.render({
        elem: '#user_list'
        ,toolbar: '#toolbarUsers'
        ,url:request("/console/userList")
        ,id: 'user_list'
        ,page: true
        ,curr: 0
        ,limit:Common.limit
        ,limits:Common.limits
        ,groups: 7
        ,cols: [[ //表头
            {type:'checkbox',fixed:'left'}// 多选
            ,{field: 'userId', title: '用户Id',sort:'true'}
            ,{field: 'nickname', title: '昵称',sort:'true'}
            ,{field: 'telephone', title: '手机号码',sort:'true',templet(d){
                    if(0 == Config.getConfig().regeditPhoneOrName){
                        return d.telephone;
                    }else{
                        return d.phone;
                    }
                }}
            ,{field: 'account', title: '通讯号',sort:'true'}
            ,{field: 'regInviteCode', title: '邀请码',sort:'true'}
            ,{field: 'model', title: '登录设备',sort:'true',templet(d){
                    if(null != d.loginLog){
                        var model = d.loginLog.model;
                        if(null == model || undefined == model || "" == model){
                            return "其他设备";
                        }else{
                            return model;
                        }
                    }
                    return "其他设备"

                }}
            ,{field: 'isAuth', title: '短信注册',sort:'true',templet: function(d){
                    return d.isAuth==1?"是":"否";
                }}
            ,{field: 'balance', title: '账户余额',sort:'true',templet:function (d) {
                    var money = d.balance.toFixed(2);
                    return money;
                }}
            ,{field: 'createTime', title: '注册时间',sort:'true',templet: function(d){
                    return UI.getLocalTime(d.createTime);
                }}
            ,{field: 'onlinestate', title: '在线状态',sort:'true',templet: function(d){
                    return (d.onlinestate==0?"离线":"在线");
                }}

            ,{field: 'loginLog', title: '最后上线时间',sort:'true',templet: function(d){
                    // console.log("log    :"+JSON.stringify(d.loginLog));
                    if(d.loginLog==undefined){
                        return "";
                    }else{
                        if(d.loginLog.loginTime==0){
                            return "";
                        }else{
                            return UI.getLocalTime(d.loginLog.loginTime);
                        }

                    }

                }}

            ,{field: 'loginLog', title: '最后离线时间',sort:'true',templet: function(d){
                    if(d.loginLog==undefined){
                        return "";
                    }else{
                        if(d.loginLog.offlineTime==0){
                            return "";
                        }else{
                            return UI.getLocalTime(d.loginLog.offlineTime);
                        }

                    }

                }}
            ,{fixed: 'right', width: 460,title:"操作", align:'left', toolbar: '#userListBar'}
        ]]
        ,done:function(res, curr, count){
            checkRequst(res);
            //日期范围
            layui.laydate.render({
                elem: '#userListMsgDate'
                ,range: "~"
                ,done: function(value, date, endDate){  // choose end
                    //console.log("date callBack====>>>"+value); //得到日期生成的值，如：2017-08-18
                    var startDate = value.split("~")[0];
                    var endDate = value.split("~")[1];


                    // Count.loadGroupMsgCount(roomJId,startDate,endDate,timeUnit);
                    table.reload("user_list",{
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


            if(count==0&&lock==1){
                layer.msg("暂无数据",{"icon":2});
                renderTable();
            }
            lock=0;
            $("#userList").show();
            $("#user_table").show();
            $("#addUser").hide();
            $("#autoCreateUser").hide();
            $("#exportUser").hide();
            if(localStorage.getItem("role")==1 || localStorage.getItem("role")==4 || localStorage.getItem("role")==7){
                $("#checkDeleteUsersId").hide();
                $(".btn_addUser").hide();
                $(".btn_createUser").hide();
                $(".export_rand_user").hide();
                $(".new_registerUser").hide();
                $(".delete").hide();
                $(".update").hide();
                $(".randUser").hide();
                $(".locking").hide();
                $(".cancelLocking").hide();
                $(".recharge").hide();
                $(".bill").hide();
                $(".friends").hide();
                $(".google").hide();
                $(".deleteFriends").hide();
                $(".createInviteCode").hide();
                $(".handCash").hide();
            }
            var pageIndex = tableInUser.config.page.curr;//获取当前页码
            var resCount = res.count;// 获取table总条数
            currentCount = resCount;
            currentPageIndex = pageIndex;
        }

    });
    $(".nickName").val('');

    //列表操作
    table.on('tool(user_list)', function(obj){
        var layEvent = obj.event,
            data = obj.data;

        if(layEvent === 'delete'){ //删除
            if(data.userId<=10000){
                return;
            }
            User.checkDeleteUsersImpl(data.userId,1);
        }else if(layEvent === 'update'){// 修改用户
            if(data.userId<=10000){
                return;
            }
            User.updateUser(obj.data,obj.data.userId);

        }else if(layEvent === 'randUser'){// 重置密码
            if(data.userId<=10000){
                return;
            }
            User.ResetPassword(obj.data.userId);

        }else if(layEvent === 'locking'){// 锁定
            if(data.userId<=10000){
                return;
            }
            User.lockIng(data.userId,-1)

        }else if(layEvent === 'cancelLocking'){// 解锁
            User.lockIng(data.userId,1)

        }else if(layEvent==='recharge'){ //后台充值

            layer.prompt({title: '请输入充值金额', formType: 0,value: '1'}, function(money, index){
                // 充值金额（正整数）的正则校验
                if(!/^(?!00)(?:[0-9]{1,3}|1000)$/.test(money)){
                    layer.msg("请输入 1-1000 的整数",{"icon":2});
                    return;
                }
                if(100000 < money){
                    layer.msg("每次只能充值100000元",{"icon":2});
                    return;
                }
                Common.invoke({
                    url :request('/console/Recharge'),
                    data : {
                        money:money,
                        userId:data.userId
                    },
                    successMsg : "充值成功",
                    errorMsg :  "充值失败，请稍后重试",
                    success : function(result) {

                        var data = result.data; //DataSort(result.data);
                        layer.close(index); //关闭弹框
                        // 更新用户列表
                        layui.table.reload("user_list")

                    },
                    error : function(result) {

                    }
                });

            });

        }else if(layEvent==='handCash'){// 手工提现
            layer.prompt({title: '请输入提现金额', formType: 0,value: '1'}, function(money, index){

                /*// 提现金额为两位小数
                if(0 == money){
                    layer.msg("请输入1-1000 的金额",{"icon":2});
                    return;
                }
                var pattern = /^(\d|[1-9]\d|1000)(\.\d{1,2})?$/;
                if(!pattern.test(money)){
                    layer.msg("请输入 1-1000 的金额",{"icon":2});
                    return;
                }*/
                if(50 < money || 10000 > money){
                    layer.msg("每次只能提现10000元",{"icon":2});
                    return;
                }
                Common.invoke({
                    url :request('/console/handCash'),
                    data : {
                        money:money,
                        userId:data.userId
                    },
                    successMsg : "提现成功",
                    errorMsg :  "提现失败，请稍后重试",
                    success : function(result) {

                        var data = result.data; //DataSort(result.data);
                        layer.close(index); //关闭弹框
                        // 更新用户列表
                        layui.table.reload("user_list")

                    },
                    error : function(result) {

                    }
                });

            });
        }else if(layEvent==='bill'){ //用户账单
            /*localStorage.setItem("currClickUser", data.userId);
            layer.open({
              title : "",
                 type: 2,
                 skin: 'layui-layer-rim', //加上边框
                 area: ['750px', '500px'], //宽高
                 content: 'userBill.html'
                 ,success: function(index, layero){
                 }
               });*/
            // 置空时间范围
            $("#userBillMsgDate").val('');
            // $(".billType").val('0');
            $(".user_btn_div").hide();
            $(".billInfo").show();
            $(".userBillName").val(data.nickname);
            $(".userBillName").empty();
            $(".userBillName").append($(".userBillName").val());// 用户账单中的小标题用户昵称
            //用户账单
            var tableIns = layui.table.render({
                elem: '#myBill_table'
                ,url:request("/console/userBill")+"&userId="+data.userId
                ,id: 'myBill_table'
                ,page: true
                ,curr: 0
                ,limit:Common.limit
                ,limits:Common.limits
                ,groups: 7
                ,cols: [[ //表头
                    {field: 'tradeNo', title: '交易单号',sort:'true', width:200}
                    ,{field: 'money', title: '金额',sort:'true', width:100}
                    ,{field: 'serviceCharge', title: '手续费',sort: true, width:120}
                    ,{field: 'operationAmount', title: '实际金额',sort: true, width:120}
                    ,{field: 'currentBalance', title: '账户余额',sort: true, width:120}
                    ,{field: 'time', title: '交易时间',sort:'true', width:200,templet: function(d){
                            return  UI.getLocalTime(d.time);
                        }}
                    ,{field: 'type', title: '类型',sort:'true', width:150,templet: function(d){
                            if (d.type==1){return "用户充值";}
                            else if(d.type==2){return "用户提现";}
                            else if(d.type==3){return "后台充值";}
                            else if(d.type==4){return "发送红包";}
                            else if(d.type==5){return "领取红包";}
                            else if(d.type==6){return "红包退款";}
                            else if(d.type==7){return "转账";}
                            else if(d.type==8){return "接受转账";}
                            else if(d.type==9){return "转账退回";}
                            else if(d.type==10){return "付款码付款";}
                            else if(d.type==11){return "付款码收款";}
                            else if(d.type==12){return "二维码收款 付款方";}
                            else if(d.type==13){return "二维码收款 收款方";}
                            else if(d.type==16){return "后台手工提现";}
                        }}
                    ,{field: 'payType', title: '支付方式',sort:'true', width:150,templet: function(d){
                            if (d.payType==1){return "支付宝支付";}
                            else if(d.payType==2){return "微信支付";}
                            else if(d.payType==3){return "余额支付";}
                            else if(d.payType==4){return "系统支付";}
                            else{return "其他方式支付"}
                        }}
                    ,{fixed: 'right', width: 250,title:"操作", align:'left', toolbar: '#billOperation'}

                ]]
                ,done:function(res, curr, count){
                    checkRequst(res);
                    $("#user_table").hide();
                    $("#myBill").show();
                    console.log("res : "+JSON.stringify(res))
                    var totalInfo = JSON.parse(res.totalVo);
                    $(".totalRecharge").empty().text((0 == totalInfo.totalTecharge ? 0:totalInfo.totalTecharge));
                    $(".totalCash").empty().text((0 == totalInfo.totalCash ? 0:totalInfo.totalCash));
                    $(".totalTransfer").empty().text((0 == totalInfo.totalTransfer ? 0:totalInfo.totalTransfer));
                    $(".totalAccount").empty().text((0 == totalInfo.totalAccount ? 0:totalInfo.totalAccount));
                    $(".sendPacket").empty().text((0 == totalInfo.sendPacket ? 0:totalInfo.sendPacket));
                    $(".receivePacket").empty().text((0 == totalInfo.receivePacket ? 0:totalInfo.receivePacket));
                    //日期范围
                    layui.laydate.render({
                        elem: '#userBillMsgDate'
                        ,range: "~"
                        ,done: function(value, date, endDate){  // choose end
                            //console.log("date callBack====>>>"+value); //得到日期生成的值，如：2017-08-18
                            var startDate = value.split("~")[0];
                            var endDate = value.split("~")[1];

                            layui.table.reload("myBill_table",{
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
                }
            });
        }else if(layEvent==='friends'){// 好友管理
            nickName = data.nickname;
            userId = data.userId; //给当前操作的那一行数据对应的用户的userId 记录，后面使用
            $(".disUserName").val(data.nickname);
            $(".disUserName").empty();
            $(".disUserName").append($(".disUserName").val());// 好友管理中的小标题用户昵称
            var tableInsFriends = table.render({
                elem: '#myFriends_table'
                ,toolbar: '#toolbarUsersFriends'
                ,url:request("/console/friendsList")+"&userId="+data.userId
                ,id: 'myFriends_table'
                ,page: true
                ,curr: 0
                ,limit:Common.limit
                ,limits:Common.limits
                ,groups: 7
                ,cols: [[ //表头
                    {type:'checkbox',fixed:'left'}// 多选
                    ,{field: 'toUserId', title: '好友Id', width:120,sort: true}
                    ,{field: 'toNickname', title: '好友昵称', width:150,sort: true}
                    ,{field: 'status', title: '状态',sort: true, width:120,templet : function (d) {
                            if(d.status == -1)
                                return "黑名单";
                            else if(d.status == 1)
                                return "关注"
                            else if(d.status == 2)
                                return "好友";
                        }}
                    ,{field: 'blacklist', title: '是否拉黑', width:120,sort: true,templet : function (d) {
                            if(d.blacklist == 1)
                                return "是";
                            else
                                return "否";
                        }}
                    ,{field: 'isBeenBlack', title: '是否被拉黑', width:120,sort: true,templet : function (d) {
                            if(d.isBeenBlack == 1)
                                return "是";
                            else
                                return "否";
                        }}
                    ,{field: 'offlineNoPushMsg', title: '是否消息免打扰',width: 150,sort: true,templet : function (d) {
                            if(d.offlineNoPushMsg == 1)
                                return "是";
                            else
                                return "否";
                        }}
                    ,{field: 'createTime', title: '成为好友的时间',width: 170,sort: true,templet: function(d){
                            return UI.getLocalTime(d.createTime);}}
                    ,{fixed: 'right', width: 250,title:"操作", align:'left', toolbar: '#delFriends'}
                ]]
                ,done:function(res, curr, count){
                    checkRequst(res);
                    $("#user_table").hide();
                    $("#myFriends").show();
                    $(".user_btn_div").hide();
                    if(localStorage.getItem("role")!=6){
                        $(".friendsInfo").hide();
                        $(".google").hide();
                    }
                    var pageIndex = tableInsFriends.config.page.curr;//获取当前页码
                    var resCount = res.count;// 获取table总条数
                    currentCount = resCount;
                    currentPageIndex = pageIndex;
                }
            });

        }
        else if(layEvent==='google'){
            layui.layer.open({
                title:"",
                type: 1,
                btn:["确定","取消"],
                area: ['700px', '700px'],
                content: $("#add_google_code"),
                success : function(layero,index){  //弹窗打开成功后的回调
                    $("#googleimg").attr("src",request('/console/google/qrcreate')+"&nickName="+localStorage.getItem("nickname"));

                },
                yes: function(index, layero){

                    $("#add_google_code").hide();
                    layui.layer.close(index); //关闭弹框
                },
                btn2: function(index, layero){
                    $("#add_google_code").hide();
                },
                cancel: function(){
                    $("#add_google_code").hide();
                }
            });
        }
        else if(layEvent==='inviteCode'){  //用户邀请码

            nickName = data.nickname;
            userId = data.userId; //给当前操作的那一行数据对应的用户的userId 记录，后面使用
            $(".disUserName").val(data.nickname);
            $(".disUserName").empty();
            $(".disUserName").append($(".disUserName").val());// 好友管理中的小标题用户昵称
            //邀请码列表
            var tableIns = table.render({

                elem: '#myInviteCode_table'
                ,url: request("/console/inviteCodeList")+"&userId="+data.userId
                ,id: 'invite_code_table'
                ,page: true
                ,curr: 0
                ,limit:10
                ,limits:[10,20,30,40,50,100,1000,10000,100000]
                ,groups: 7
                ,cols: [[ //表头
                    {field: 'inviteCode', title: '邀请码',width:100}
                    ,{field: 'userId', title: '所属用户Id',width:110}
                    ,{field: 'status', title: '状态',width:100,templet:function(d){
                            if(d.status==0){
                                return "未使用";
                            }else{
                                return "已使用";
                            }
                        }}
                    ,{field: 'totalTimes', title: '类型',width:200,templet:function(d){
                            if(d.totalTimes==-1){
                                return "推广型(一码多用)";
                            }else if(d.totalTimes==1){
                                return "邀请注册型(一码一用)";
                            }
                        }}
                    ,{field: 'createTime',title:'生成时间',sort: true, width:180,templet: function(d){
                            return Common.formatDate(d.createTime,"yyyy-MM-dd hh:mm:ss",1);
                        }}
                    ,{field: 'usedTimes', title: '使用次数',width:100}
                    ,{field: 'lastuseTime',title:'最后使用时间',sort: true, width:180,templet: function(d){
                            if(d.lastuseTime==0){
                                return '--';
                            }else{
                                return Common.formatDate(d.lastuseTime,"yyyy-MM-dd hh:mm:ss",1);
                            }

                        }}
                    ,{fixed: 'right', width: 100,title:"操作", align:'left', toolbar: '#inviteCodeBar'}
                ]]
                ,done:function(res, curr, count){
                    checkRequst(res);
                    $("#user_table").hide();
                    $("#myInviteCode").show();
                    $(".user_btn_div").hide();
                }
            });


        }

    });

    // 好友管理
    table.on('tool(myFriends_table)', function(obj){
        var layEvent = obj.event,
            data = obj.data;
        // console.log(data);
        if(layEvent === 'deleteFriends'){// 删除好友
            // User.deleteFrends(data.userId,data.toUserId,obj);
            if(-1 == data.status)
                return;
            User.checkDeleteUsersFriendsImpl(data.userId,data.toUserId,1);
        }else if(layEvent==='chatRecord'){// 聊天记录

            var tableInsChatRecord = table.render({
                elem: '#friendsChatRecord_table'
                ,toolbar: '#toolbarUsersChatRecord'
                ,url:request("/console/friendsChatRecord")+"&userId="+data.userId+"&toUserId="+data.toUserId
                ,id: 'friendsChatRecord_table'
                ,page: true
                ,curr: 0
                ,limit:Common.limit
                ,limits:Common.limits
                ,groups: 7
                ,cols: [[ //表头
                    {type:'checkbox',fixed:'left'}// 多选
                    ,{field: 'sender', title: '用户Id', width:120,sort: true}
                    ,{field: 'sender_nickname', title: '发送者', width:120,sort: true}
                    ,{field: 'receiver', title: '好友Id', width:120,sort: true}
                    ,{field: 'receiver_nickname', title: '接收者', width:150,sort: true}
                    ,{field: 'contentType', title: '消息类型', width:150,sort: true,templet : function (d) {
                            return Common.msgType(d.contentType);
                        }}
                    ,{field: 'timeSend', title: '发送时间',width: 170,sort: true,templet: function(d){
                            return UI.getLocalTime(d.timeSend);}}
                    ,{field: 'content', title: '消息内容', width:250,sort: true,templet:function (d) {
                            if(!Common.isNil(d.content)){
                                if(1 == d.isEncrypt && localStorage.getItem("role")==6){
                                    var desContent = Common.decryptMsg(d.content,d.messageId,d.timeSend);
                                    if(desContent.search("https") != -1||desContent.search("http")!=-1){
                                        var link = "<a target='_blank' href=\""+desContent+"\">"+desContent+"</a>";
                                        return link;
                                    }else{
                                        return desContent;
                                    }
                                }else{
                                    var text = (Object.prototype.toString.call(d.content) === '[object Object]' ? JSON.stringify(d.content) : d.content)
                                    try {
                                        if(text.search("https") != -1||text.search("http")!=-1){
                                            var link = "<a target='_blank' href=\""+text+"\">"+text+"</a>";
                                            return link;
                                        }else{
                                            return text;
                                        }
                                    }catch (e) {
                                        return text;
                                    }

                                }
                            }else{
                                return "";
                            }

                        }}
                    ,{fixed: 'right', width: 200,title:"操作", align:'left', toolbar: '#delChartRecord'}
                ]]
                ,done:function(res, curr, count){
                    checkRequst(res);
                    $("#user_table").hide();
                    $("#myFriends").hide();
                    $("#friendsChatRecord").show();
                    var pageIndex = tableInsChatRecord.config.page.curr;//获取当前页码
                    var resCount = res.count;// 获取table总条数
                    currentCount = resCount;
                    currentPageIndex = pageIndex;
                }
            });
        }else if(layEvent==='joinBlacklist'){ //加入黑名单
            User.joinMoveBlacklist(data.userId,data.toUserId,0);
        }else if(layEvent==='moveBlacklist'){ //移除黑名单
            User.joinMoveBlacklist(data.userId,data.toUserId,1);
        }


    });

    // 删除好友间的聊天记录
    table.on('tool(friendsChatRecord_table)', function(obj){
        var layEvent = obj.event,
            data = obj.data;
        console.log(data);
        if(layEvent === 'deletechatRecord'){
            // User.delFriendsChatRecord(data.messageId,obj);
            User.toolbarUsersChatRecordImpl(data.messageId,1);
        }
    });

    // 账单明细
    table.on('tool(myBill_table)', function(obj){
        var layEvent = obj.event,
            data = obj.data;
        if(layEvent === 'billDetails'){
            console.log("账单 明细  "+JSON.stringify(data));
            var tableBillInfo;
            // 红包相关
            if(4 == data.type || 5 == data.type || 6 == data.type){
                if(null == data.redPacketId || "" == data.redPacketId || undefined == data.redPacketId){
                    layer.msg("暂无明细",{"icon":2});
                    return;
                }
                tableBillInfo = table.render({
                    elem: '#myBill_table_Info'
                    ,url:request("/console/redPacketList")+"&redPacketId="+data.redPacketId
                    ,id: 'myBill_table_Info'
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
                        $("#user_table").hide();
                        $("#myBill").hide();
                        $("#myBillInfo").show();
                    }
                });

            }else if(1 == data.type || 3 == data.type || 2 == data.type || 16 == data.type || 10 == data.type || 12 == data.type || 11 == data.type){
                // 充值和提现相关处理 // 10  12
                var titleMsg,moneyInfo,timeInfo;
                if(1 == data.type || 3 == data.type){
                    titleMsg = "充值记录Id";
                    moneyInfo = "充值金额";
                    timeInfo = "充值时间";
                }else if(2 == data.type || 16 == data.type){
                    titleMsg = "提现记录Id";
                    moneyInfo = "提现金额";
                    timeInfo = "提现时间";
                }else if(10 == data.type || 12 == data.type){
                    moneyInfo = "付款金额";
                }else if(11 == data.type){
                    moneyInfo = "收款金额";
                }
                tableBillInfo = table.render({
                    elem: '#myBill_table_Info'
                    ,url:request("/console/consumeRecordInfo")+"&tradeNo="+data.tradeNo
                    ,id: 'myBill_table_Info'
                    ,page: true
                    ,curr: 0
                    ,limit:Common.limit
                    ,limits:Common.limits
                    ,groups: 7
                    ,cols: [[ //表头
                        {field: 'id', title: titleMsg,sort: true,width:150}
                        ,{field: 'tradeNo', title: '交易单号',sort: true,width:180}
                        ,{field: 'userId', title: '用户Id',sort: true, width:120}
                        ,{field: 'userName', title: '用户昵称',sort: true, width:120,templet : function (d) {
                                var userName;
                                (d.userName == "" ? userName = "测试用户" : userName = d.userName);
                                return userName;
                            }}
                        ,{field: 'money', title: moneyInfo,sort: true, width:120}
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
                                (d.type == 10 ? statusMsg = "付款码付款" : (d.type == 12) ? statusMsg = "二维码付款" : statusMsg = "其他方式付款")
                                return statusMsg;
                            }}
                        ,{field: 'time',title:timeInfo,width:195,templet: function(d){
                                return UI.getLocalTime(d.time);
                            }}
                    ]]
                    ,done:function(res, curr, count){
                        checkRequst(res);
                        $("#user_table").hide();
                        $("#myBill").hide();
                        $("#myBillInfo").show();
                    }
                });
            }else {
                // 转账相关详情
                tableBillInfo = table.render({
                    elem: '#myBill_table_Info'
                    ,url:request("/console/consumeRecordInfo")+"&tradeNo="+data.tradeNo
                    ,id: 'myBill_table_Info'
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
                        $("#user_table").hide();
                        $("#myBill").hide();
                        $("#myBillInfo").show();
                    }
                });
            }

        }
    });

    // 红包账单明细
    table.on('tool(myBill_table_Info)', function(obj){
        var layEvent = obj.event,
            data = obj.data;
        if(layEvent === 'receiveWaterInfo'){
            console.log("红包流水"+JSON.stringify(data));
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
                    $("#myBillInfo").hide();
                    $("#receiveWater").show();
                }
            });
        }
    });

    //搜索用户
    $(".search_user").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();

        table.reload("user_list",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
                onlinestate:$("#status").val(),// 在线状态
                keyWorld : $(".nickName").val()  //搜索的关键字
            }
        })
        lock=1;
        $(".nickName").val('');
        $("#myFriends").hide();
    });

    //搜索账单
    $(".search_bill").on("click",function(){
        console.log(123)
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();

        table.reload("myBill_table",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
                type:$("#operationType").val(),// 账单类型
            }
        })
        $("#type").val("");
    });


    //搜索邀请码
    $(".search_inviteCode").on("click",function(){

        table.reload("invite_code_table",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
                keyworld : $(".invite_code_name").val(),  //搜索的关键字
                state: $(".inviteCodeStatus").val()
            }
        })

    });
    //删除邀请码
    table.on('tool(myInviteCode_table)', function(obj){
        var layEvent = obj.event,
            data = obj.data;
        console.log(data);
        if(layEvent === 'del_inviteCode'){
            User.checkDeleteInviteCode(data.userId,data.id);
        }
    });
    //生成注册型邀请码
    $(".btn_create_register_InviteCode").on("click",function(){

        layui.layer.open({
            title: '生成邀请码'
            ,type : 1
            ,offset: 'auto'
            ,area: '270px'
            ,btn: ['确定', '取消']
            ,content:'<div id="CreateInviteCode" style="margin:20px auto;">'
                +  '<div class="layui-inline" style="margin-left:20px;">'
                +      '<div class="layui-input-inline" style="min-width:200px;">'
                +         '<input id="inviteCodeNum" lay-verify="required|number" placeholder="生成的邀请码数量(1-3000) " autocomplete="off" class="layui-input" type="text">'
                +      '</div>'
                +  '</div>'
                +'</div>'
            ,yes: function(index, layero){
                //按钮【确定】 的回调
                var inviteCodeNum = $("#inviteCodeNum").val();
                var reg = /^[0-9]*[1-9][0-9]*$/;
                if(!reg.test(inviteCodeNum)){
                    layer.msg("生成数量必须是1-3000的数字",{icon: 2});
                    return;
                }else if(!(parseInt(inviteCodeNum)<=3000)){
                    layer.msg("生成数量必须是1-3000的数字",{icon: 2});
                    return;
                }

                Common.invoke({
                    url : request('/console/create/inviteCode'),
                    data : {
                        'nums':inviteCodeNum,
                        'userId':userId,
                        'type': 1
                    },
                    successMsg : "邀请码生成完成",
                    errorMsg : "邀请码生成失败,请稍后重试",
                    success : function(result) {
                        if (1 == result.resultCode){
                            //重载表格
                            table.reload("invite_code_table",{
                                page: {
                                    curr: 1 //重新从第 1 页开始
                                },
                                where: {
                                    keyworld : $(".invite_code_name").val(),  //搜索的关键字
                                    state: $(".inviteCodeStatus").val()
                                }
                            })

                        }
                    },
                    error : function(result) {
                    }
                });


                layui.layer.closeAll(); //关闭弹框
                layer.msg('开始生成邀请码');

            }
            ,btn2: function(index, layero){
                //按钮【取消】的回调

                //return false 开启该代码可禁止点击该按钮关闭
            },
            success: function(){

                layui.form.render();
            }

        })

    });


})

//导出用户数据
$(".export_rand_user").on("click",function(){

    layui.layer.open({
        title: '导出用户数据'
        ,type : 1
        ,offset: 'auto'
        ,area: ['370px','200px']
        ,btn: ['导出', '取消']
        ,content:  '<form class="layui-form" action="/console/exportData">'
            + '<div class="layui-form-item">'
            + 	'<div class="layui-inline"  style="width:80%"">'
            + 		'<label class="layui-form-label" style="width:100%">导出由系统自动生成的随机用户</label>'
            +     	'<div class="layui-input-inline" style="min-width:200px;margin-top:20px; display:none;">'
            +	      	    '<select name="userType">'
            +         	   '<option value="1">普通用户</option>'
            +	        	   '<option value="3" selected>随机用户(系统自动生成)</option>'
            +	      	    '</select>'
            +	        '</div>'
            +	     '</div>'
            + '</div>'
            +  '<button id="exportData_submit"  class="layui-btn" type="submit" lay-submit="" style="display:none">导出</button>'
            +'</from>'
        ,success: function(index, layero){
            layui.form.render();
        }
        ,yes: function(index, layero){
            //确定按钮的回调
            $("#exportData_submit").click();
            layui.layer.close(index); //关闭弹框
        }
        ,btn2: function(index, layero){
            //按钮【取消】的回调

            //return false 开启该代码可禁止点击该按钮关闭
        }

    });

});//layui END


//重新渲染表单
function renderTable(){
    layui.use('table', function(){
        var table = layui.table;//高版本建议把括号去掉，有的低版本，需要加()
        // table.reload("user_list");
        table.reload("user_list",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
                onlinestate:$("#status").val(),// 在线状态
                keyWorld : $(".nickName").val()  //搜索的关键字
            }
        })
    });
}


var loginTime="";
var offlineTime="";

var User={
    user_list:function(e,pageSize){
        var html="";
        if(e==undefined){
            e=0;
        }else if(pageSize==undefined){
            pageSize=10;
        }
        $.ajax({
            type:'POST',
            url:request('/console/userList'),
            data:{
                pageIndex:(e==0?"0":e-1),
                pageSize:pageSize
            },
            dataType:'json',
            async:false,
            success:function(result){
                checkRequst(result);
                if(result.data.pageData.length!=0){
                    console.log(result.data.allPageCount);
                    $("#pageCount").val(result.data.allPageCount);
                    for(var i=0;i<result.data.pageData.length;i++){
                        if(result.data.pageData[i].loginLog!=undefined){
                            if(result.data.pageData[i].loginLog.loginTime!=0){
                                loginTime=UI.getLocalTime(result.data.pageData[i].loginLog.loginTime);
                            }
                            if(result.data.pageData[i].loginLog.offlineTime!=0){
                                offlineTime=UI.getLocalTime(result.data.pageData[i].loginLog.offlineTime);
                            }

                        }
                        html+="<tr><td>"+result.data.pageData[i].userId+"</td><td style='width:100px'>"
                            +result.data.pageData[i].nickname+"</td><td>"+result.data.pageData[i].telephone+"</td><td>"+UI.getLocalTime(result.data.pageData[i].createTime)
                            +"</td><td>"+(result.data.pageData[i].onlinestate==0?"离线":"在线")+"</td><td>"+loginTime+"</td><td>"+offlineTime+"</td>";


                        html+="<td><button onclick='User.deleteUser(\""+result.data.pageData[i].userId
                            +"\")' class='layui-btn layui-btn-danger layui-btn-xs'>删除</button><button onclick='User.updateUser(\""
                            +result.data.pageData[i].userId+"\")' class='layui-btn layui-btn-primary layui-btn-xs'>修改</button><button onclick='User.ResetPassword(\""
                            +result.data.pageData[i].userId+"\")' class='layui-btn layui-btn-primary layui-btn-xs'>重置密码</button></td>";


                        html+="</tr>";
                    }

                    $("#userList_table").empty();
                    $("#userList_table").append(html);
                    $("#userList").show();
                    $("#user_table").show();
                    $("#addUser").hide();
                    $("#autoCreateUser").hide();
                    $("#exportUser").hide()
                }

            }
        })
    },


    //  新增用户
    addUser:function(){
        $(".password").show();
        $("#userList").hide();
        $("#addUser").show();
        $("#userId").val(0);
        $("#userName").val("");
        $("#telephone").val("");
        $("#password").val("");
        $("#sex").val("");
        $("#isPublic").val("");
        // 重新渲染
        layui.form.render();
        $("#addUserTitle").empty();
        $("#addUserTitle").append("新增用户");
        // 获取当前注册类型
        Common.invoke({
            url:request('/console/config'),
            data:{},
            success:function(result){
                if(result.resultCode==1){
                    console.log("config regeditPhoneOrName is "+JSON.stringify(result.data.regeditPhoneOrName));
                    regeditPhoneOrName = result.data.regeditPhoneOrName;
                }
            }
        })
    },
    // 提交新增用户
    commit_addUser:function(){
        var date = $("#birthday").val();
        if($("#userName").val()==""){
            layui.layer.alert("请输入昵称");
            return;
        }
        if($("#telephone").val()==""){
            layui.layer.alert("请输入手机号码");
            return;
        }else{
            // regeditPhoneOrName 1:用户名，0：手机号
            if(0 == regeditPhoneOrName){
                var patrn = /^[0-9]*$/;
                if (patrn.exec($("#telephone").val()) == null || $("#telephone").val() == "") {
                    layui.layer.alert("请使用手机号注册");
                    return;
                }
            }
        }
        if(0 == $("#userId").val() && $("#password").val()==""){
            layui.layer.alert("请输入密码");
            return;
        }
        if("" == date || null == date){
            layui.layer.alert("请选择出生日期");
            return;
        }
        if($("#sex").val()==""){
            layui.layer.alert("请选择性别");
            return;
        }
        if("" == $("#isPublic").val() || null == $("#isPublic").val()){
            layui.layer.alert("是否设置为公众号");
            return;
        }

        $.ajax({
            url:request('/console/updateUser'),
            data:{
                userId:$("#userId").val(),
                nickname:$("#userName").val(),
                telephone:$("#telephone").val(),
                password:$("#password").val(),
                userType:$("#isPublic").val(),
                sex:$("#sex").val(),
                isAdmin:1,
                birthday:Date.parse(date)/1000
            },
            dataType:'json',
            async:false,
            success:function(result){
                checkRequst(result);
                if(result.resultCode==1){
                    if($("#userId").val()==0){
                        layer.alert("添加成功");
                        $("#userList").show();
                        $("#addUser").hide();
                        layui.table.reload("user_list",{
                            page: {
                                curr: 1 //重新从第 1 页开始
                            },
                            where: {

                            }
                        })

                    }else{
                        layer.alert("修改成功");
                        $("#userList").show();
                        $("#addUser").hide();
                        renderTable();
                    }

                }else{
                    layer.alert(result.resultMsg);
                }

            },
            error:function(result){
                if(result.resultCode==0){
                    layer.alert(result.resultMsg);
                }
            }
        })
    },
    // 修改用户
    updateUser:function(data,userId){
        $(".password").hide();
        $("#birthday").val("");
        Common.invoke({
            url:request('/console/getUpdateUser'),
            data:{
                userId:userId
            },
            success:function(result){
                var userType = result.data.userType;

                console.log("type:"+result.data.userType);
                if(result.data!=null){
                    $("#userId").val(result.data.userId);
                    $("#userName").val(result.data.nickname);
                    $("#telephone").val(result.data.phone);
                    $('#isPublic').val(result.data.userType);
                    $("#sex").val(result.data.sex);
                    $("#birthday").val(UI.getLocalTime(result.data.birthday));
                }
                $("#addUserTitle").empty();
                $("#addUserTitle").append("修改用户");
                $("#userList").hide();
                $("#addUser").show();
                layui.form.render();
            }
        });

    },

    // 多选删除用户
    checkDeleteUsers:function(){
        // 多选操作
        var checkStatus = layui.table.checkStatus('user_list'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+checkStatus.data) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选
        for (var i = 0; i < checkStatus.data.length; i++){
            userIds.push(checkStatus.data[i].userId);
        }
        console.log(userIds);
        if(0 == checkStatus.data.length){
            layer.msg("请勾选要删除的行");
            return;
        }
        User.checkDeleteUsersImpl(userIds.join(","),checkStatus.data.length);
    },
    checkDeleteInviteCode:function (userID,inviteCodeId){
        layer.confirm('确定删除指定邀请码',{icon:3, title:'提示消息',yes:function () {
                Common.invoke({
                    url:request('/console/delInviteCode'),
                    data:{
                        userId:userId,
                        inviteCodeId:inviteCodeId
                    },
                    success:function(result){
                        if(result.resultCode==1){
                            layer.msg("删除成功",{"icon":1});
                            Common.tableReload(currentCount,currentPageIndex,checkLength,"myInviteCode_table");
                            location.reload();
                        }
                    }
                })
            }});
    },
    checkDeleteUsersImpl:function(userId,checkLength){
        layer.confirm('确定删除指定用户',{icon:3, title:'提示消息',yes:function () {
                Common.invoke({
                    url:request('/console/deleteUser'),
                    data:{
                        userId:userId
                    },
                    success:function(result){
                        if(result.resultCode==1){
                            layer.msg("删除成功",{"icon":1});
                            userIds = [];
                            // renderTable();
                            Common.tableReload(currentCount,currentPageIndex,checkLength,"user_list");
                        }
                    }
                })
            },btn2:function () {
                userIds = [];
            },cancel:function () {
                userIds = [];
            }});
    },

    // 多选删除用户好友
    checkDeleteUsersFriends:function(){
        // 多选操作
        var checkStatus = layui.table.checkStatus('myFriends_table'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+checkStatus.data) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选
        var userId;
        for (var i = 0; i < checkStatus.data.length; i++){
            toUserIds.push(checkStatus.data[i].toUserId);
            userId = checkStatus.data[i].userId;
        }
        console.log("userId: "+userId+"------"+toUserIds);
        if(0 == checkStatus.data.length){
            layer.msg("请勾选要删除的行");
            return;
        }
        User.checkDeleteUsersFriendsImpl(userId,toUserIds.join(","),checkStatus.data.length);
    },
    checkDeleteUsersFriendsImpl:function(userId,toUserId,checkLength){
        layer.confirm('确定删除指定好友',{icon:3, title:'提示消息',yes:function () {
                Common.invoke({
                    url:request('/console/deleteFriends'),
                    data:{
                        userId:userId,
                        toUserIds:toUserId,
                        adminUserId:localStorage.getItem("account")
                    },
                    success:function(result){
                        if(result.resultCode==1){
                            layer.msg("删除成功",{"icon":1});
                            toUserIds = [];
                            // layui.table.reload("myFriends_table");
                            Common.tableReload(currentCount,currentPageIndex,checkLength,"myFriends_table");
                        }
                    }
                })
            },btn2:function () {
                toUserIds = [];
            },cancel:function () {
                toUserIds = [];
            }});
    },

    // 导出好友明细
    exportExcelFriends : function(){
        layui.layer.open({
            title: '导出好友数据'
            ,type : 1
            ,offset: 'auto'
            ,area: ['300px','180px']
            ,btn: ['导出', '取消']
            ,content:  '<form class="layui-form" action="/console/exportExcelByFriends">'
                + '<div class="layui-form-item">'
                + 	'<div class="layui-inline">'
                +	'<input type="hidden" name="userId" value='+userId+'>'
                + 		'<label class="layui-form-label" style="width: 90%;margin-top: 20px" >导出用户 “'+nickName+'” 的好友列表</label>'
                +	     '</div>'
                + '</div>'
                +  '<button id="exportFriends_submit"  class="layui-btn" type="submit" lay-submit="" style="display:none">导出</button>'
                +'</from>'
            ,success: function(index, layero){
                layui.form.render();
            }
            ,yes: function(index, layero){
                $("#exportFriends_submit").click();
                layui.layer.close(index); //关闭弹框
            }
            ,btn2: function(index, layero){
                //按钮【取消】的回调

                //return false 开启该代码可禁止点击该按钮关闭
            }

        });

    },


    // 多选删除聊天记录
    toolbarUsersChatRecord:function(){
        // 多选操作
        var checkStatus = layui.table.checkStatus('friendsChatRecord_table'); //idTest 即为基础参数 id 对应的值
        console.log("新版："+checkStatus.data) //获取选中行的数据
        console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
        console.log("新版："+checkStatus.isAll ) //表格是否全选
        for (var i = 0; i < checkStatus.data.length; i++){
            messageIds.push(checkStatus.data[i].messageId);
        }
        console.log(messageIds);
        if(0 == checkStatus.data.length){
            layer.msg("请勾选要删除的行");
            return;
        }
        User.toolbarUsersChatRecordImpl(messageIds.join(","),checkStatus.data.length);
    },
    toolbarUsersChatRecordImpl:function(messageId,checkLength){
        layer.confirm('确定删除指定聊天记录',{icon:3, title:'提示消息',yes:function () {
                Common.invoke({
                    url:request('/console/delFriendsChatRecord'),
                    data:{
                        messageId :messageId
                    },
                    success:function(result){
                        if(result.resultCode==1){
                            layer.msg("删除成功",{"icon":1});
                            messageIds = [];
                            Common.tableReload(currentCount,currentPageIndex,checkLength,"friendsChatRecord_table");

                            // layui.table.reload("friendsChatRecord_table");
                        }
                    }
                })
            },btn2:function () {
                messageIds = [];
            },cancel:function () {
                messageIds = [];
            }});
    },

    // 重置密码
    ResetPassword:function(userId){

        // $(".randUser").on("click",function(){

        layui.layer.open({
            title:"重置密码",
            type: 1,
            btn:["确定","取消"],
            area: ['300px'],
            content: '<div id="mdifyPassword" class="layui-form" style="margin:20px 40px 10px 40px;;">'
                +   '<div class="layui-form-item">'
                +      '<div class="layui-input-block" style="margin: 0 auto;">'
                +        '<input type="password" required  lay-verify="required" placeholder="新的密码" autocomplete="off" class="layui-input admin_passwd">'
                +      '</div>'
                +    '</div>'
                +   '<div class="layui-form-item">'
                +      '<div class="layui-input-block" style="margin: 0 auto;">'
                +        '<input type="password" required  lay-verify="required" placeholder="确认密码" autocomplete="off" class="layui-input admin_rePasswd">'
                +      '</div>'
                +    '</div>'
                +'</div>'

            ,yes: function(index, layero){ //确定按钮的回调

                var newPasswd = $("#mdifyPassword .admin_passwd").val();
                var reNewPasswd = $("#mdifyPassword .admin_rePasswd").val();
                if(newPasswd!=reNewPasswd){
                    layui.layer.msg("两次密码输入不一致",{"icon":2});
                    return;
                }

                Common.invoke({
                    url : request('/console/updateUserPassword'),
                    data : {
                        "userId" : userId,
                        "password": $.md5(newPasswd)
                    },
                    successMsg : "重置密码成功",
                    errorMsg :  "重置密码失败，请稍后重试",
                    success : function(result) {
                        layui.layer.close(index); //关闭弹框
                        // location.replace("/pages/console/login.html");
                    },
                    error : function(result) {

                    }
                });

            }

        });

        // });
    },
    // 最新注册用户
    newRegisterUser:function(){
        html="";
        Common.invoke({
            url:request('/console/newRegisterUser'),
            data:{
                pageIndex:0
            },
            success:function(result){
                if(result.data!=null){
                    for(var i=0;i<result.data.pageData.length;i++){

                        if(result.data.pageData[i].loginLog!=undefined){
                            if(result.data.pageData[i].loginLog.loginTime!=0){
                                loginTime=UI.getLocalTime(result.data.pageData[i].loginLog.loginTime);
                            }
                            if(result.data.pageData[i].loginLog.offlineTime!=0){
                                offlineTime=UI.getLocalTime(result.data.pageData[i].loginLog.offlineTime);
                            }
                        }
                        html+="<tr><td>"+result.data.pageData[i].userId+"</td><td>"+result.data.pageData[i].nickname
                            +"</td><td>"+result.data.pageData[i].telephone+"</td><td>"+UI.getLocalTime(result.data.pageData[i].createTime)
                            +"</td><td>"+(result.data.pageData[i].onlinestate==0?"离线":"在线")
                            +"</td><td>"+loginTime+"</td><td>"+offlineTime+"</td><td style='width:25%'><button onclick='User.deleteUser(\""
                            +result.data.pageData[i].userId+"\")' class='layui-btn layui-btn-danger layui-btn-xs'>删除</button><button onclick='User.updateUser(\""
                            +result.data.pageData[i].userId+"\")' class='layui-btn layui-btn-primary layui-btn-xs'>修改</button><button onclick='User.ResetPassword(\""
                            +result.data.pageData[i].userId+"\")' class='layui-btn layui-btn-primary layui-btn-xs'>重置密码</button></td></tr>";
                    }
                    $("#userList_table").empty();
                    $("#userList_table").append(html);
                }
            }
        })
    },
    // 自动生成用户
    autoCreateUser:function(){
        $("#user_table").hide();
        $("#autoCreateUser").show();
    },
    // 提交自动生成用户
    commit_autoCreateUser:function(){
        if($("#userNum").val() <= 0){
            layer.alert("生成的用户数量至少为1个");
        }
        Common.invoke({
            url:request('/console/autoCreateUser'),
            data:{
                userNum:$("#userNum").val()
            },
            success:function(result){
                if(result.resultCode==1){
                    layer.alert("随机生成用户成功");
                    renderTable();
                    $("#userNum").val("")
                    $("#autoCreateUser").hide();
                    $("#user_table").show();

                }
            }
        })
    },
    // 导出用户
    exportUser:function(){
        $("#user_table").hide();
        $("#exportUser").show();
    },
    // 提交
    commit_exportUser:function(){
        Common.invoke({
            url:request('/console/exportData'),
            data:{
                userType:$("#userType").val()
            },
            success:function(result){
                if(result.resultCode==1){
                    layer.alert("导出成功");
                }
            }
        })
    },

    // 用户锁定解锁
    lockIng:function(userId,status){
        var confMsg,successMsg="";
        (status == -1 ? confMsg = '确定锁定该用户？':confMsg = '确定解锁该用户？');
        (status == -1 ? successMsg = "锁定成功":successMsg ="解锁成功");
        layer.confirm(confMsg,{icon:3, title:'提示信息'},function(index){

            Common.invoke({
                url : request('/console/changeStatus'),
                data : {
                    userId:userId,
                    status:status
                },
                successMsg : successMsg,
                errorMsg :  "加载数据失败，请稍后重试",
                success : function(result) {
                    layui.table.reload("user_list",{

                    })
                },
                errorCb : function(result) {
                }
            });
        })
    },

    // 加入移除黑名单
    joinMoveBlacklist:function(userId,toUserId,status){
        var confMsg,successMsg="";
        (status == 0 ? confMsg = '确定加入黑名单？':confMsg = '确定移除黑名单？');
        (status == 0 ? successMsg = "加入成功":successMsg ="移除成功");
        layer.confirm(confMsg,{icon:3, title:'提示信息'},function(index){

            Common.invoke({
                url : request('/console/blacklist/operation'),
                data : {
                    userId:userId,
                    toUserId:toUserId,
                    type:status,
                    adminUserId:localStorage.getItem("account")
                },
                successMsg : successMsg,
                errorMsg :  "加载数据失败，请稍后重试",
                success : function(result) {
                    layui.table.reload("myFriends_table")
                },
                error : function(result) {
                }
            });
        })
    },
    button_back:function(){
        $("#userList").show();
        $("#user_table").show();
        $(".user_btn_div").show();
        $("#myBillInfo").hide();
        $(".billInfo").hide();
        $("#autoCreateUser").hide();
        $("#exportUser").hide();
        $("#addUser").hide();
        $("#myFriends").hide();
        $("#myInviteCode").hide();
        $("#myBill").hide();
    },
    // 好友聊天记录的返回按钮
    button_back_chatRecord:function(){
        $("#myFriends").show();
        $("#friendsChatRecord").hide();
    },

    // 账单明细的返回按钮
    button_back_userBillInfo:function(){
        $("#myBill").show();
        $("#myBillInfo").hide();
    },

    // 红包明细的返回按钮
    button_back_redBillInfo:function(){
        $("#myBillInfo").show();
        $("#receiveWater").hide();
    }

}