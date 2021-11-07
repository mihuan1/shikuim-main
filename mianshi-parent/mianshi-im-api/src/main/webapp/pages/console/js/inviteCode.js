layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

	
	if(localStorage.getItem("IS_ADMIN")==0){
    	// $(".search_group").hide();
    	$(".btn_addRoom").hide();
    	$(".chatRecord").hide();
    	$(".member").hide();
    	$(".randUser").hide();
    	$(".modifyConf").hide();
    	$(".msgCount").hide();
    	$(".sendMsg").hide();
    	$(".del").hide();
    }

    

   



    




})
	



