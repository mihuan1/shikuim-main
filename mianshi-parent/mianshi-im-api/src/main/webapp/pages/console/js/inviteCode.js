layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

		//获取邀请码列表
		table.render({
			elem: '#invita_code_list'
			,height: 700
			,url: request("/console/inviteCodeList")
			,page: true //开启分页
			,cols: [[ //表头
				{field: 'id', title: 'ID', sort: true, fixed: 'left'}
				,{field: 'inviteCode', title: '邀请码'}
				,{field: 'defaultfriend', title: '默认好友手机号',  sort: true}
				,{field: 'desc', title: '备注',  sort: true}
				,{field: 'cout', title: '码下用户数'}
				,{field: 'createTime', title: '创建时间',templet: function(d){
						return Common.formatDate(d.createTime,"yyyy-MM-dd hh:mm:ss",1);
				}}
				,{fixed: 'right',title:"操作", align:'left', toolbar: '#invitaBar'}
			]]
		});

		//搜索邀请码
		$(".search_inviteCode").on("click",function(){

			table.reload("invita_code_list",{
				page: {
					curr: 1 //重新从第 1 页开始
				},
				where: {
					keyworld : $(".invite_code_name").val(),  //搜索的关键字
					defaultfriend : $(".default_friend").val(),  //默认好友ID
				}
			})

		});

		//生成邀请码
		$(".btn_create_register_InviteCode").on("click",function(){
			$("#add_invita_code_form .defaultfriend").val("");
			$("#add_invita_code_form .inviteCode").val("");
			$("#add_invita_code_form .desc").val("");
			layui.layer.open({
				title:"",
				type: 1,
				btn:["创建","取消"],
				area: ['400px', '300px'],
				content: $("#add_code"),
				success : function(layero,index){  //弹窗打开成功后的回调

				},
				yes: function(index, layero){

					var defaultfriend = $("#add_invita_code_form .defaultfriend").val();
					var inviteCode = $("#add_invita_code_form .inviteCode").val();
					var desc = $("#add_invita_code_form .desc").val();
					Common.invoke({
						url : request('/console/create/inviteCode'),
						data : {
							inviteCode:inviteCode,
							desc:desc,
							defaultfriend:defaultfriend,
						},
						successMsg : "创建邀请码成功",
						errorMsg :  "创建邀请码失败，请稍后重试",
						success : function(result) {
							$("#add_code").hide();
							layui.layer.close(index); //关闭弹框

							table.reload("invita_code_list",{
								page: {
									curr: 1 //重新从第 1 页开始
								}
							})

						},
						error : function(result) {

						}
					});

				},
				btn2: function(index, layero){
					$("#add_code").hide();
				},
				cancel: function(){
					$("#add_code").hide();
				}
			});
		});
		//删除邀请码
		table.on('tool(invita_code_list)', function(obj){
			var layEvent = obj.event,
				data = obj.data;
			console.log(data);
			if(layEvent === 'del_inviteCode'){
				layer.confirm('确定删除指定邀请码',{icon:3, title:'提示消息',yes:function () {
						Common.invoke({
							url:request('/console/delInviteCode'),
							data:{
								inviteCodeId:data.id
							},
							success:function(result){
								if(result.resultCode==1){
									layer.msg("删除成功",{"icon":1});
									table.reload("invita_code_list",{
										page: {
											curr: 1 //重新从第 1 页开始
										}
									})
								}
							}
						})
					}})
			}else if(layEvent === "edit_inviteCode"){
				$("#add_invita_code_form .defaultfriend").val(data.defaultfriend);
				$("#add_invita_code_form .inviteCode").val(data.inviteCode);
				$("#add_invita_code_form .desc").val(data.desc);

				layui.layer.open({
					title:"",
					type: 1,
					btn:["更新","取消"],
					area: ['400px', '300px'],
					content: $("#add_code"),
					success : function(layero,index){  //弹窗打开成功后的回调

					},
					yes: function(index, layero){
						Common.invoke({
							url : request('/console/update/updateInviteCode'),
							data : {
								id:data.id,
								inviteCode:$("#add_invita_code_form .inviteCode").val(),
								desc:$("#add_invita_code_form .desc").val(),
								defaultfriend:$("#add_invita_code_form .defaultfriend").val(),
							},
							successMsg : "更新邀请码成功",
							errorMsg :  "更新邀请码失败，请稍后重试",
							success : function(result) {
								$("#add_code").hide();
								layui.layer.close(index); //关闭弹框

								table.reload("invita_code_list",{
									page: {
										curr: 1 //重新从第 1 页开始
									}
								})

							},
							error : function(result) {

							}
						});

					},
					btn2: function(index, layero){
						$("#add_code").hide();
					},
					cancel: function(){
						$("#add_code").hide();
					}
				});
			}
		});

})




