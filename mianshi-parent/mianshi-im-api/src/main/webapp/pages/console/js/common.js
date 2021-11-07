/**
 * 公共的 js
 */
var ivKey=[1,2,3,4,5,6,7,8];
var iv=getStrFromBytes(ivKey);
var ConfigData=null;
function getStrFromBytes (arr) {
    var r = "";
    for(var i=0;i<arr.length;i++){
        r += String.fromCharCode(arr[i]);
    }
    return r;
}
function request(url){
    console.log("服务器时间差  "+localStorage.getItem("currentTime"));
    
    let time=parseInt(Math.round(((new Date().getTime()))))+parseInt(localStorage.getItem("currentTime"));
    time=parseInt(time/1000);
    var obj=localStorage.getItem("apiKey")+time+localStorage.getItem("account")+localStorage.getItem("access_Token");
    url=url+"?secret="+$.md5(obj)+"&time="+time+"&access_token="+localStorage.getItem("access_Token");
    return url;
}

function checkRequst(result){
    // 访问令牌过期或无效说
    if(result.resultCode==1030102){
        layer.confirm(result.resultMsg,{icon:2, title:'提示消息',yes:function () {
          
          window.top.location.href="/pages/console/login.html";
        },btn2:function () {

          window.top.location.href="/pages/console/login.html";
        },cancel:function () {
          
          window.top.location.href="/pages/console/login.html";
        }});
    }else if(result.resultCode==0){
        if(!Common.isNil(result.resultMsg)){
            layer.msg(result.resultMsg,{icon: 2});
        }
        
    }
}

var Config={
	getConfig:function(){
		if(ConfigData==null){
			$.ajax({
				type:'POST',
				url:'/config',
				data:{},
				async:false,
				success:function(result){
					ConfigData=result.data;
				}
			})
		}
		return ConfigData;
	}
}

var Common = {
	// layui table重载刷新 解决零界值删除不刷新到上页的问题(支持删除，多选删除)
	// 	currentCount ：当前table总数量, currentPageIndex ： 当前页数, checkLength ：选中的个数, tableId : table的ID){
	tableReload : function(currentCount,currentPageIndex,checkLength,tableId){
		var remainderIndex = (currentCount - checkLength) % Common.limit;
        console.log("currentCount : "+currentCount+"  checkLength : "+checkLength+"  Common.limit : "+Common.limit+"  currentPageIndex : "+currentPageIndex+"  remainderIndex : "+remainderIndex);
        if(0 == remainderIndex)
            currentPageIndex = currentPageIndex - 1;
        layui.table.reload(tableId,{
            page: {
                curr: currentPageIndex, //重新从当前页开始
            }
		})
	},

	// 分页公共参数 
    limit:15,
    limits:[15,50,100,1000,10000],

	/** 调用接口通用方法  */
	invoke : function(obj, notShowLoad){
		jQuery.support.cors = true;
		if (!notShowLoad) layer.load(1); //显示等待框
        
		var params = {
			type : "POST",
			url : obj.url,
			data : obj.data,
			contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
			dataType : 'JSON',
            async:obj.async==false?false:true,
			success : function(result) {
				layer.closeAll('loading');
				if(1==result.resultCode){
                    obj.success(result);
					if(obj.successMsg!=undefined&&obj.successMsg!=""&&obj.successMsg!=null){
                        layer.msg(obj.successMsg,{icon:1});
                    }			
				}else if(-1==result.resultCode){
					//缺少访问令牌
					layer.msg("缺少访问令牌",{icon: 3});
					window.location.href = "/pages/console/login.html";
				}else if(1030102==result.resultCode){// 访问令牌过期
                    checkRequst(result);
                }else{
					if(!Common.isNil(result.resultMsg))
						layer.msg(result.resultMsg,{icon: 2,time: 2000});
					else
						layer.msg(obj.errorMsg,{icon: 2,time: 2000});

                    // obj.error(result);
				}
				return;
					
			},
			error : function(result) {
				layer.closeAll('loading');
				if(!Common.isNil(result.resultMsg)){
					layer.msg(result.resultMsg,{icon: 2});
				}else{
					layer.msg(obj.errorMsg,{icon: 2});
				}
				// obj.error(result);//执行失败的回调函数
				return;
			},
			complete : function(result) {
				layer.closeAll('loading');
			}
		}
		params.data["access_token"] = localStorage.getItem("access_Token");;
		$.ajax(params);
	}
	,isNil : function(s) {
		return undefined == s || null == s || $.trim(s) == "" || $.trim(s) == "null";
	},
	formatDate : function (time,fmt,type) { //type : 类型 0:时间为秒  1:时间为毫秒
		var date = new Date((type==0?(time * 1000):time));
	    var o = {
	        "M+": date.getMonth() + 1, //月份 
	        "d+": date.getDate(), //日 
	        "h+": date.getHours(), //小时 
	        "m+": date.getMinutes(), //分 
	        "s+": date.getSeconds(), //秒 
	        "q+": Math.floor((date.getMonth() + 3) / 3), //季度 
	        "S": date.getMilliseconds() //毫秒 
	    };
	    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
	    for (var k in o)
	    	if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
	    return fmt;
	},
	// 消息类型
	msgType : function(type) {
        if(1 == type){
            return "文本消息";
        }else if(2 == type ){
            return "图片消息";
        }else if(3 == type){
            return "语音消息";
        }else if(4 == type){
            return "位置消息";
        }else if(5 == type){
            return "动画消息";
        }else if(6 == type){
            return "视频消息";
        }else if(7 == type){
            return "音频消息";
        }else if(8 == type){
            return "名片消息";
        }else if(9 == type){
            return "文件消息";
        }else if(10 == type){
            return "提醒消息";
        }else if (28 == type) {
			return "红包消息";
		}else if(83 == type){
            return "领取红包消息";
		}else if(86 == type){
            return "红包退回消息";
        }else if(29 == type){
            return "转账消息";
        }else if(80 == type){
            return "单条图文消息";
        }else if(81 == type){
            return "多条图文消息";
        }else if(84 == type){
            return "戳一戳消息";
        }else if(85 == type){
            return "聊天记录消息";
        }else if(88 == type){
            return "转账被领取消息";
        }else if(89 == type){
            return "转账已退回消息";
        }else if(90 == type){
            return "付款码已付款通知消息";
        }else if(91 == type){
            return "付款码已到账通知消息";
        }else if(92 == type){
            return "收款码已付款通知消息";
        }else if(93 == type){
            return "收款码已到账通知消息";
        }else if(96 == type){
            return "双向撤回消息";
        }else if(201 == type){
            return "正在输入消息";
        }else if(202 == type){
            return "撤回消息";
        }else if(100 == type){
            return "发起语音通话消息";
        }else if(102 == type){
            return "接听语音通话消息";
        }else if(103 == type){
            return "拒绝语音通话消息";
        }else if(104 == type){
            return "结束语音通话消息";
        }else if(110 == type){
            return "发起视频通话消息";
        }else if(112 == type){
            return "接听视频通话消息";
        }else if(113 == type){
            return "拒绝视频通话消息";
        }else if(114 == type){
            return "结束视频通话消息";
        }else if(115 == type){
            return "会议邀请消息";
        }else if(901 == type){
            return "修改昵称消息";
        }else if(902 == type){
            return "修改房间名消息";
        }else if(903 == type){
            return "解散房间消息";
        }else if(904 == type){
            return "退出房间消息";
        }else if(905 == type){
            return "新公告消息";
        }else if(906 == type){
            return "禁言、取消禁言消息";
        }else if(907 == type){
            return "增加成员消息";
        }else if(913 == type){
            return "设置、取消管理员消息";
        }else if(915 == type){
            return "设置群已读消息";
        }else if(916 == type){
            return "群验证消息";
        }else if(917 == type){
            return "群组是否公开消息";
        }else if(918 == type){
            return "是否展示群成员列表消息";
        }else if(919 == type){
            return "允许发送名片消息";
        }else if(920 == type){
            return "全员禁言消息";
        }else if(921 == type){
            return "允许普通群成员邀请好友加群";
        }else if(922 == type){
            return "允许普通成员上传群共享";
        }else if(923 == type){
            return "允许普通成员发起会议";
        }else if(924 == type){
            return "允许普通成员发送讲课";
        }else if(925 == type){
            return "转让群组";
        }else if(930 == type){
            return "设置、取消隐身人，监控人";
        }else if(931 == type){
            return "群组被后台锁定/解锁";
        }
        else{
			return "其他消息类型";
		}
    },
	// 消息解密
    decryptMsg:function(content,msgId,timeSend) {
	    timeSend = parseInt(timeSend);
        console.log("content:"+content+"  msgId: "+msgId+"  timeSend: "+timeSend);
        var key=Common.getMsgKey(msgId,timeSend)
        var desContent = content.replace(" ", "");
        var content
        try {
            content=Common.decryptDES(desContent,key);
        }catch (e) {
            console.log("des解密失败：  "+e)
            return content;
        }
        return content;
    },
    getMsgKey:function(msgId,timeSend){
        var key= localStorage.getItem("apiKey")+timeSend+msgId;
        return $.md5(key);
    },
    decryptDES:function(message,key){
        console.log("key1: "+key);
        //把私钥转换成16进制的字符串
        var keyHex = CryptoJS.enc.Utf8.parse(key);

        //把需要解密的数据从16进制字符串转换成字符byte数组
        var decrypted = CryptoJS.TripleDES.decrypt({
            ciphertext: CryptoJS.enc.Base64.parse(message)
        }, keyHex, {
            iv:CryptoJS.enc.Utf8.parse(iv),
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });
        //console.log("decryptDES   "+ decrypted);
        //以utf-8的形式输出解密过后内容
        var result = decrypted.toString(CryptoJS.enc.Utf8);
        console.log("decryptDES   "+ result);
        return result;
    },

}; /*End Common*/