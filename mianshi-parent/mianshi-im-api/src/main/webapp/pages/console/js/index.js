/** 主页相关的 js **/
var $,tab,dataStr,layer;
layui.config({
	base : "/pages/console/js/"
}).extend({
	"bodyTab" : "bodyTab"
})
layui.use(['bodyTab','form','element','layer','jquery'],function(){
	var form = layui.form,
		element = layui.element;
		$ = layui.$;
    	layer = parent.layer === undefined ? layui.layer : top.layer;
		tab = layui.bodyTab({
			openTabNum : "50",  //最大可打开窗口数量
			url : "json/navs.json" //获取菜单json地址
		});

	//右上角显示当前登录管理员账号
	var name;
	var role = localStorage.getItem("role");
	if(1 == role )
		name = "游客";
	else if(4 == role)
        name = "客服";
	else if(5 == role)
		name = "管理员";
    else if(6 == role)
        name = "超级管理员";
	else if(7 == role)
		name = "财务";
	console.log("昵称："+localStorage.getItem("nickname"));
	if(undefined == localStorage.getItem("nickname") || null == localStorage.getItem("nickname")){
		console.log("缓存清空后重新登录......")
        location.replace("/pages/console/login.html");
	}
	$(".adminName").append(name+":"+localStorage.getItem("nickname"));

	// 客服和财务登录隐藏压力测试
    if(localStorage.getItem("role")==4 || localStorage.getItem("role")==7){
    	$(".pressureTest").hide();
	}

	//修改密码
    $("#changeMyPasswd").on("click",function(){

        //layer.msg("changeMyPasswd  Test");

        //var newPasswd = "111111";

        layui.layer.open({
            title:"修改密码",
            type: 1,
            btn:["确定","取消"],
            area: ['300px'],
            content: '<div id="mdifyPassword" class="layui-form" style="margin:20px 40px 10px 40px;;">'
        	+   '<div class="layui-form-item">'
            +      '<div class="layui-input-block" style="margin: 0 auto;">'
            +        '<input type="password" required  lay-verify="required" placeholder="请输入旧密码" autocomplete="off" class="layui-input admin_oldPasswd">'
            +      '</div>'
            +    '</div>'
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
            	
            	var oldPasswd = $("#mdifyPassword .admin_oldPasswd").val();
                var newPasswd = $("#mdifyPassword .admin_passwd").val();
                var reNewPasswd = $("#mdifyPassword .admin_rePasswd").val();
                if(newPasswd!=reNewPasswd){
                    layui.layer.msg("两次密码输入不一致",{"icon":2});
                    return;
                }

                Common.invoke({
                    url : request('/console/updateAdminPassword'),
                    data : {
                    	"oldPassword":$.md5(oldPasswd),
                        "userId" : localStorage.getItem("account"),
                        "password": $.md5(newPasswd)
                    },
                    successMsg : "修改密码成功",
                    errorMsg :  "修改密码失败，请稍后重试",
                    success : function(result) {
                        layui.layer.close(index); //关闭弹框
                        location.replace("/pages/console/login.html");
                    },
                    error : function(result) {

                    }
                });




            }


        });


    });

	//通过顶部菜单获取左侧二三级菜单   注：此处只做演示之用，实际开发中通过接口传参的方式获取导航数据
	function getData(json){
		$.getJSON(tab.tabConfig.url,function(data){
			if(json == "contentManagement"){
				dataStr = data.contentManagement;
				//重新渲染左侧菜单
				tab.render();
			}else if(json == "memberCenter"){
				dataStr = data.memberCenter;
				//重新渲染左侧菜单
				tab.render();
			}else if(json == "systemeSttings"){
				dataStr = data.systemeSttings;
				//重新渲染左侧菜单
				tab.render();
			}else if(json == "seraphApi"){
                dataStr = data.seraphApi;
                //重新渲染左侧菜单
                tab.render();
            }
		})
	}
	console.log(localStorage.getItem("telephone"));
	
	//页面加载时判断左侧菜单是否显示
	//通过顶部菜单获取左侧菜单
	$(".topLevelMenus li,.mobileTopLevelMenus dd").click(function(){
		if($(this).parents(".mobileTopLevelMenus").length != "0"){
			$(".topLevelMenus li").eq($(this).index()).addClass("layui-this").siblings().removeClass("layui-this");
		}else{
			$(".mobileTopLevelMenus dd").eq($(this).index()).addClass("layui-this").siblings().removeClass("layui-this");
		}
		$(".layui-layout-admin").removeClass("showMenu");
		$("body").addClass("site-mobile");
		getData($(this).data("menu"));
		//渲染顶部窗口
		tab.tabMove();
	})

	//隐藏左侧导航
	$(".hideMenu").click(function(){
		if($(".topLevelMenus li.layui-this a").data("url")){
			layer.msg("此栏目状态下左侧菜单不可展开");  //主要为了避免左侧显示的内容与顶部菜单不匹配
			return false;
		}
		$(".layui-layout-admin").toggleClass("showMenu");
		//渲染顶部窗口
		tab.tabMove();
	})

	//通过顶部菜单获取左侧二三级菜单   注：此处只做演示之用，实际开发中通过接口传参的方式获取导航数据
	getData("contentManagement");

	//手机设备的简单适配
    $('.site-tree-mobile').on('click', function(){
		$('body').addClass('site-mobile');
	});
    $('.site-mobile-shade').on('click', function(){
		$('body').removeClass('site-mobile');
	});

	// 添加新窗口
	$("body").on("click",".layui-nav .layui-nav-item a:not('.mobileTopLevelMenus .layui-nav-item a')",function(){
		//如果不存在子级
		if($(this).siblings().length == 0){
			addTab($(this));
			$('body').removeClass('site-mobile');  //移动端点击菜单关闭菜单层
		}
		$(this).parent("li").siblings().removeClass("layui-nav-itemed");
	})

	//清除缓存
	$(".clearCache").click(function(){
		window.sessionStorage.clear();
        window.localStorage.clear();
        var index = layer.msg('清除缓存中，请稍候',{icon: 16,time:false,shade:0.8});
        setTimeout(function(){
            layer.close(index);
            layer.msg("缓存清除成功！");
        },1000);
    })

	//刷新后还原打开的窗口
    /*if(cacheStr == "true") {
        if (window.sessionStorage.getItem("menu") != null) {
            menu = JSON.parse(window.sessionStorage.getItem("menu"));
            curmenu = window.sessionStorage.getItem("curmenu");
            var openTitle = '';
            for (var i = 0; i < menu.length; i++) {
                openTitle = '';
                if (menu[i].icon) {
                    if (menu[i].icon.split("-")[0] == 'icon') {
                        openTitle += '<i class="seraph ' + menu[i].icon + '"></i>';
                    } else {
                        openTitle += '<i class="layui-icon">' + menu[i].icon + '</i>';
                    }
                }
                openTitle += '<cite>' + menu[i].title + '</cite>';
                openTitle += '<i class="layui-icon layui-unselect layui-tab-close" data-id="' + menu[i].layId + '">&#x1006;</i>';
                element.tabAdd("bodyTab", {
                    title: openTitle,
                    content: "<iframe src='" + menu[i].href + "' data-id='" + menu[i].layId + "'></frame>",
                    id: menu[i].layId
                })
                //定位到刷新前的窗口
                if (curmenu != "undefined") {
                    if (curmenu == '' || curmenu == "null") {  //定位到后台首页
                        element.tabChange("bodyTab", '');
                    } else if (JSON.parse(curmenu).title == menu[i].title) {  //定位到刷新前的页面
                        element.tabChange("bodyTab", menu[i].layId);
                    }
                } else {
                    element.tabChange("bodyTab", menu[menu.length - 1].layId);
                }
            }
            //渲染顶部窗口
            tab.tabMove();
        }
    }else{
		window.sessionStorage.removeItem("menu");
		window.sessionStorage.removeItem("curmenu");
	}*/
})

//打开新窗口
function addTab(_this){
	tab.tabAdd(_this);
}

function quitLogin(){
    console.log("退出登录");
    Common.invoke({
        url:request("/console/logout"),
        data:{

        },
        async:false,
        success:function(result){
            
        }
    })
    localStorage.clear;
    sessionStorage.clear;
    window.location.href = "/pages/console/login.html";
}

//60秒监控一次
setInterval('new_trad_monitor()',5000);

function new_trad_monitor() {
    Common.invoke({
        url:request("/console/trade/monitor"),
        data:{

        },
        async:false,
        success:function(result){
			if(result.data) {
                $("#new-trad-voice")[0].play();
			}
        }
    }, true)
}

/*window.onload = function () {
    $("#new-trad-voice").removeAttr("muted");
}*/
// function clickTest(){
//     layui.bodyTab({
//         // openTabNum : "50",  //最大可打开窗口数量
//         url : "/pages/console/pressureTest.html" //获取菜单json地址
//     });
//     // $("#iframe").attr("src","pressureTest.html");
//     // document.getElementById("iframe").src="/pages/console/pressureTest.html";
//     // layui.layer.form().render();
//
// }