layui.use(['element','form'],function(){
	var element=layui.element;
	var form=layui.form;

  $("#user").append(localStorage.getItem("telephone"));

	//点击事件监听
  	element.on('nav(demo)', function(data){
  		console.log(data);
  		if(data.context.type==3){
        document.getElementById("iframes").src="/pages/open/mobileApp.html";
      }else if(data.context.type==5){
        document.getElementById("iframes").src="/pages/open/userInfo.html";
      }

  	});

})
$(function(){
  document.getElementById("iframes").src="/pages/open/mobileApp.html";
  
})

$("#exit").click(function(){
    layer.confirm('确定退出？',{icon:3, title:'提示信息'},function(index){
        location.replace("/pages/open/login.html");
    })
})

