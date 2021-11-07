/*集群配置js*/
var area="";
layui.use(['element','form'], function(){

  //非超级管理员登录屏蔽操作按钮
  if(localStorage.getItem("role")==1 || localStorage.getItem("role")==5){
      $(".save").remove();
      $(".entranceBtn").remove();
      $(".centreBtn").remove();
      $("#areaList_toolbar").remove();
  }

  var element = layui.element;
  var form =layui.form;
  //点击事件监听
  element.on('tab(demo)', function(data){
   
    if(data.index==0){// 点击地区节点
      
      // Clu.findUrlConfig_total();
      $("#areaList").show();
      $("#add_area").hide();
      $("#server_list").hide();
      $("#server_tab").show();
      $("#server_add").hide();
      layui.table.reload("area_list");
    }else if(data.index==1){// 点击指定入口
      console.log(data);
       Clu.findUrlConfig();
       Clu.areaConfigList();
       $("#addUrlConfig").hide();
       $("#urlConfigList").show();
        $("#server_list").hide();
    }else if(data.index==2){// 点击指定中心
       Clu.areaConfigList();
       Clu.findByType_center();
       $("#addCenterConfig").hide();
       $("#centerConfigList").show();
       $("#server_list").hide();
      // renderForm();
    }

  });
});
//重新渲染表单
function renderForm(){
  layui.use('form', function(){
   var form = layui.form;//高版本建议把括号去掉，有的低版本，需要加()
   form.render();
  });
 }


 layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
    layer = parent.layer === undefined ? layui.layer : top.layer,
    $ = layui.jquery,
    laydate = layui.laydate,
    laytpl = layui.laytpl,
    table = layui.table;

    //非管理员登录屏蔽操作按钮
  //   if(localStorage.getItem("IS_ADMIN")==0){
  //     $(".liveRoom_btn_div").empty();
  //     $(".delete").remove();
   // $(".chatMsg").remove();
   // $(".member").remove();
  //   }
  
   var tableIns = table.render({

      elem: '#area_list'
      ,url:request("/console/areaConfigList")
      ,id: 'area_list'
      ,page: true
      ,curr: 0
      ,limit:10
      ,limits:[10,20,30,40,50]
      ,groups: 7
      ,cols: [[ //表头
           {field: 'area', title: '地区代码',width:550}
          ,{field: 'name', title: '地区名称',width:550}
          ,{fixed: 'right', width: 300,title:"操作", align:'left', toolbar: '#areaList_toolbar'}
        ]]
       ,done:function(res, curr, count){
          checkRequst(res);
          console.log("结束");
       }
    }); 
    //列表操作
    table.on('tool(area_list)', function(obj){
        var layEvent = obj.event,
        data = obj.data;
        console.log(data);
        if(layEvent === 'delete'){ //删除

            layer.confirm('确定删除该地区？',{icon:3, title:'提示信息'},function(index){
               Clu.deleteAreaConfig(data.id);
               obj.del();
               layer.closeAll();
            })

        } else if(layEvent === 'update'){ //修改
         
            Clu.updateAreaConfig(data);
        } else if(layEvent === 'serverList'){// 服务器群
            console.log("服务器群"+data.id);
            $("#server_Id").val(data.id);
            Clu.serverList(data.area);
            Clu.areaConfigList(data.id);
            $("#areaList").hide();
            $("#server_list").show();
        }
      });

    form.on('select(type)', function(data){
        // console.log(data);
        console.log("hsgfakfdafgfghsakhdgfaksfghsadgk");
        Clu.findByType();
    });
    // form.on('select(center_type)', function(data){
     
    //     console.log(data);
    //     Clu.findByType_center();
    // });
   

    //搜索
    $(".search_live").on("click",function(){
       
            table.reload("liveRoom_table",{
                page: {
                    curr: 1 //重新从第 1 页开始
                },
                where: {
                    name : $("#roomName").val()  //搜索的关键字
                }
            })
        $("#roomName").val("");
    });
});

 var Clu={
    // 服务器列表
    serverList:function(area){
      var html="";
      var type="";
      Common.invoke({
        url:request("/console/findServerByArea"),
        data:{
          area:area
        },
        success:function(result){
          
          if(result.data!=null){
              // if(index==1){// 服务器列表
                for(var i=0;i<result.data.count;i++){

                   $.ajax({
                      type:'POST',
                      url:request('/console/areaConfigList'),
                      data:{
                        id:result.data.data[i].area
                      },
                      async:false,
                      success:function(data){
                        checkRequst(data);
                        area=data.data[0].name;
                      }
                    })

                   if(result.data.data[i].type==1){
                      type="xmpp服务器";
                   }else if(result.data.data[i].type==2){
                      type="http服务器";
                   }else if(result.data.data[i].type==3){
                      type="视频服务器";
                   }else if(result.data.data[i].type==4){
                      type="直播服务器";
                   }else if(result.data.data[i].type==5){
                      type="上传服务器";
                   }else if(result.data.data[i].type==6){
                      type="下载服务器";
                   }else if(result.data.data[i].type==0){
                      type="全部类型";
                   }

                  html+="<tr><td>"+result.data.data[i].id+"</td><td>"+result.data.data[i].name+"</td><td>"+result.data.data[i].url
                  +"</td><td>"+result.data.data[i].port+"<td>"+result.data.data[i].count+"</td><td>"+result.data.data[i].maxPeople+"</td><td>"
                  +area+"</td><td>"+type+"</td><td>"
                  +(result.data.data[i].status==1?"正常":"禁用")
                  +"</td><td><a class='layui-btn layui-btn-danger layui-btn-xs' onclick='Clu.deleteServer(\""
                  +result.data.data[i].id+"\",\""+result.data.data[i].area+"\")'>删除</a><a class='layui-btn layui-btn-primary layui-btn-xs' onclick='Clu.updateServer(\""
                  +result.data.data[i].id+"\")'>修改</a></td></tr>";
                }
                
                $("#server_tbody").empty();
                $("#server_tbody").append(html);
              
            }
            
        }
      })
    },
    findServerList:function(index){
      
      var html="<div style='width:250px;'><ul>";
      Common.invoke({
        url:request('/console/serverList'),
        data:{

        },
        success:function(data){
          for(var i=0;i<data.data.count;i++){
            html+="<li><span style='margin-left:50px'>"+data.data.data[i].name+"</span><input name='checkbox2' value='"+data.data.data[i].id+"' type='checkbox' style='float:right;margin-right:50px'></li>";
          }
          html+="</ul><button class='layui-btn' onclick='Clu.checkboxed("+index+")' style='margin-left:50px'>保存</button></div>";
          // // $("#Ids").empty();
          // $("#Ids").after(html);
          // renderForm();
          layui.layer.open({
            title:'服务器列表',
            type:1,
            content:html
          });
        }
      })
      
    },
    // 弹出层根据地区
    findServerList_area:function(index){
      var html="<div style='width:250px;'><ul>";
      if($("#areaUrlConfig").val()!=""){
          Common.invoke({
            url:request('/console/findServerByArea'),
            data:{
                area:$(".areaUrlConfig").val()
            },
            success:function(data){
              for(var i=0;i<data.data.count;i++){
                html+="<li><span style='margin-left:50px'>"+data.data.data[i].name+"</span><input name='checkbox2' value='"+data.data.data[i].id+"' type='checkbox' style='float:right;margin-right:50px'></li>";
              }
              html+="</ul><button class='layui-btn' onclick='Clu.checkboxed("+index+")' style='margin-left:50px'>保存</button></div>";
              // // $("#Ids").empty();
              // $("#Ids").after(html);
              // renderForm();
              layui.layer.open({
                title:'服务器列表',
                type:1,
                content:html
              });
            }
        })
      }
      
      
    },
    // 新增服务器
    addServer:function(){
        $("#server_tab").hide();
        $("#server_add").show();
        $(".add_id").val("");
        $(".add_name").val("");
        $(".add_url").val("");
        $(".add_port").val("");
        $(".add_count").val("");
        $(".add_status").val("");
    },
    // 提交新增服务器
    commitAddServer:function(){
      console.log("aaa");
      console.log($(".add_name").val());
      if($(".add_name").val()==""){
          layui.layer.alert("请输入机器名称");
      }else if($(".add_url").val()==""){
          layui.layer.alert("请输入url");
      }else if($(".add_port").val()==""){
          layui.layer.alert("请输入端口");
      }
      console.log("地区名称"+$(".serverAdd_area").val());
      if($(".add_id").val()!=""){
          Clu.commit_updateServer($(".serverAdd_area").val());
        }else {
            Common.invoke({
                url:request("/console/addServerList"),
                data:{
                  name:$(".add_name").val(),
                  url:$(".add_url").val(),
                  port:$(".add_port").val(),
                  count:$(".add_count").val(),
                  maxPeople:$(".add_maxPeople").val(),
                  area:$(".serverAdd_area").val(),
                  type:$(".add_type").val(),
                  status:$(".add_status").val()
                },
                success:function(result){
                  if(result.resultCode==1){
                    $(".add_name").val("");
                    $(".add_url").val("");
                    $(".add_port").val("");
                    $(".add_count").val("");
                    $(".add_status").val("");
                    $("#server_tab").show();
                    $("#server_add").hide();
                     Clu.serverList($("#server_Id").val());
                     $("#areaList").show();
                      $("#server_tab").show();
                      $("#server_add").hide();
                      layui.table.reload("area_list");
                    // table.reload("ServerList",);
                  }
                }
            })
        }
    },
    // 地区配置
    areaConfigList:function(id){
      if(id==undefined){
        Common.invoke({
          url:request('/console/areaConfigList'),
          data:{
            
          },
          success:function(result){
              var html="";
              for(var i=0;i<result.data.length;i++){
                html+="<option value='"+result.data[i].area+"'>"+result.data[i].name+"</option>"
              }
               $(".serverAdd_area").empty();
               $(".areaUrlConfig").empty();
               $(".areaUrl").empty();
              $(".serverAdd_area").append(html);
              $(".areaUrlConfig").append(html);
              $(".areaUrl").append(html);
              $(".urlConfigIds").empty();
              $(".urlConfigIds").append(html);
              $(".center_clientA").empty();
              $(".center_clientA").append(html);
              $(".center_clientB").empty();
              $(".center_clientB").append(html);
              renderForm();
          }
      })
      }else{
        Common.invoke({
          url:request('/console/areaConfigList'),
          data:{
            id:id
          },
          success:function(result){
              var html="";
              for(var i=0;i<result.data.length;i++){
                area=result.data[i].name;
                html+="<option value='"+result.data[i].area+"'>"+result.data[i].name+"</option>"
              }
               $(".serverAdd_area").empty();
              $(".serverAdd_area").append(html);
              $(".areaUrlConfig").empty();
              $(".areaUrlConfig").append(html);
              renderForm();
          }
      })
      }
    },
    addAreaConfig:function(){
       $("#areaList").hide();
       $("#add_area").show();
       $(".area").val("");
       $(".name").val("");
       //Addr[0]="中国_CN";
       //console.log();
       for (area in Addr) {
          $("#country").append("<option value='"+area+"'>"+area+"</option>");
          renderForm(); //这个很重要
        }
    },
    // // 修改地区配置
    updateAreaConfig:function(data){
      $("#areaList").hide();
      $("#area_id").val(data.id);
      $(".area").val(data.area);
      $(".name").val(data.name);
      $("#add_area").show();
    },
    // 提交添加地区
    commit_areaConfig:function(){
      var area='';
      // 国家不能为空
      if($("#country").val()==null||$("#country").val()==""||$("#country").val()==undefined){
        return ;
      }
      // 请输入地区名称不能为空
      if($(".name").val()==""||$(".name").val()==null||$(".name").val()==undefined){
        console.log("ssssss");
        return ;
      }

      if($("#province").val()==""||$("#province").val()==null||$("#province").val()==undefined){
        // 省份为空
        area=$("#country").val().split("_")[1];
      }else{
        if($("#city").val()==""||$("#city").val()==null||$("#city").val()==undefined){
          // 城市为空
          area=$("#country").val().split("_")[1]+","+$("#province").val();
        }else{
          // 都不为空
          area=$("#country").val().split("_")[1]+","+$("#province").val()+","+$("#city").val();
        }
      }

      if($("#area_id").val()!=""){
        Common.invoke({
          url:request('/console/addAreaConfig'),
          data:{
            id:$("#area_id").val(),
            area:area,
            name:$(".name").val()
          },
          success:function(result){
            console.log(result);
            renderForm();
          }
        })
      }else{
          Common.invoke({
            url:request('/console/addAreaConfig'),
            data:{
              area:area,
              name:$(".name").val()
            },
            success:function(result){
              console.log(result);
              renderForm();
            }
          })
      }
      $("#areaList").show();
      $("#add_area").hide();
      $("#server_list").hide();
      layui.table.reload("area_list");
    },
    // 删除地区配置
    deleteAreaConfig:function(id){
     
          Common.invoke({
            url:request("/console/deleteAreaConfig"),
            data:{
              id:id
            },
            success:function(result){
              console.log("=====删除地区成功=====");
            }
        })
      
    },
    // 复选框选中
    checkboxed:function(index){
      var ids="";
      var htm="";
      $("input[name='checkbox2']:checked").each(function(){
        console.log("checkbox2组选中项的值："+$(this).val());//遍历选中项的值
        ids+=$(this).val()+",";
        $.ajax({
          type:'POST',
          url:request('/console/serverList'),
          data:{
            id:$(this).val()
          },
          async:false,
          success:function(result){
            checkRequst(result);
            console.log(result);
            console.log();
            htm+=result.data.data[0].name+",";
          }
        })
      });
      ids=ids.slice(0,ids.length-1);
      htm=htm.slice(0,htm.length-1);
      if(index==1){
        $("#Ids").val(ids);
        $("#Ids_name").val(htm);
      }else if(index==2){
        $(".urlConfigIds").val(ids);
        $(".urlConfigIds_name").val(htm);
      }
      
      layer.closeAll();
    },
    // 新增入口
    addUrlConfig:function(){
      $("#urlConfigList").hide();
      $("#addUrlConfig").show();
      $(".areaUrlConfig").val("");
      $(".nameUrlConfig").val("");
      $(".Ids").val("");
      $("#urlConfig_id").val("");
    },
    // 保存入口配置
    commit_urlConfig:function(){
        if($("#urlConfig_id").val()!=""){
            Common.invoke({
              url:request('/console/addUrlConfig'),
              data:{
                id:$("#urlConfig_id").val(),
                type:$(".type").val(),
                area:$(".areaUrlConfig").val(),
                name:$(".nameUrlConfig").val(),
                toArea:$(".areaUrl").val()
              },
              success:function(result){
                console.log("success");
              }
          })
        }else{
          Common.invoke({
            url:request('/console/addUrlConfig'),
            data:{
              type:$(".type").val(),
              area:$(".areaUrlConfig").val(),
              name:$(".nameUrlConfig").val(),
              toArea:$(".areaUrl").val()
            },
            success:function(result){
              console.log("success");
            }
          })
        }

    },
    // 根据服务器类型查询服务器地址
    findByType:function(){
      Common.invoke({
        url:request("/console/findUrlConfig"),
        data:{
          type:$(".type").val()
        },
        success:function(result){
          console.log("result");
          if(result.data.data[0]!=null){
            console.log(result.data);
            $(".areaUrlConfig").val(result.data.data[0].area);
            // $(".areaUrl").val(result.data.data[0].area);
            $(".nameUrlConfig").val(result.data.data[0].name);
            // $(".Ids").val(result.data.ids);
            // $("#urlConfig_id").val(result.data.id);
          }else{
            $(".areaUrlConfig").val("");
            $(".nameUrlConfig").val("");
            $(".Ids").val("");
            $("#urlConfig_id").val("");
          }
        }
      })
    },
    // 
    // findById:function(id){
        
    // },
    // 查询入口
    findUrlConfig:function(callback){
      var html="";
      var url="";
      Common.invoke({
        url:request("/console/findUrlConfig"),
        data:{

        },
        success:function(result){
          if(result.data.data.length!=0){
             for(var i=0;i<result.data.data.length;i++){
              var type="";
                if(result.data.data[i].type==1){
                  type="xmpp服务器";
                }else if(result.data.data[i].type==2){
                  type="http服务器";
                }else if(result.data.data[i].type==3){
                  type="视频服务器";
                }else if(result.data.data[i].type==4){
                  type="直播服务器";
                }else if(result.data.data[i].type==0){
                  type="全部类型";
                }else if(result.data.data[i].type==5){
                  type="上传服务器";
                }else if(result.data.data[i].type==6){
                  type="下载服务器";
                }
                // Clu.areaConfigList(result.data.data[i].area);
                // $.ajax({
                //   type:'POST',
                //   url:request('/console/areaConfigList'),
                //   data:{
                //     id:result.data.data[i].area
                //   },
                //   async:false,
                //   success:function(data){
                //     area=data.data[0].name;
                //   }
                // })
                // $.ajax({
                //   type:'POST',
                //   url:request('/console/areaConfigList'),
                //   data:{
                //     id:result.data.data[i].url
                //   },
                //   async:false,
                //   success:function(data){
                //     url=data.data[0].name;
                //   }
                // })
                html+="<tr><td>"+result.data.data[i].id+"</td><td width='10%'>"+type+"</td><td>"+result.data.data[i].area
                +"</td><td>"+result.data.data[i].toArea+"</td><td><a class='layui-btn layui-btn-danger layui-btn-xs' onclick='Clu.deleteUrlConfig(\""
                +result.data.data[i].id+"\")'>删除</a><a class='layui-btn layui-btn-primary layui-btn-xs' onclick='Clu.updateUrlConfig(\""+
                result.data.data[i].id+"\")'>修改</a></td></tr>"
             }
             $("#url_tab").empty();
             $("#url_tab").append(html);
          }
          
        }
      })
    },
    // 总配置入口
    findUrlConfig_total:function(){
        var xmpp="";
        var http="";
        var video="";
        var live="";
      Common.invoke({
        url:request("/console/findUrlConfig"),
        data:{

        },
        success:function(result){
          if(result.data.data.length!=0){
              for(var i=0;i<result.data.data.length;i++){
                  if(result.data.data[i].type==1){// xmpp
                    xmpp+="<option value='"+result.data.data[i].id+"'>"+result.data.data[i].name+"</option>";
                  }else if(result.data.data[i].type==2){// http
                    http+="<option value='"+result.data.data[i].id+"'>"+result.data.data[i].name+"</option>";
                  }else if(result.data.data[i].type==3){// video
                    video+="<option value='"+result.data.data[i].id+"'>"+result.data.data[i].name+"</option>";
                  }else if(result.data.data[i].type==4){// live
                    live+="<option value='"+result.data.data[i].id+"'>"+result.data.data[i].name+"</option>";
                  }
              }
              $(".xmppConfig").empty();
              $(".xmppConfig").append(xmpp);
              $(".httpConfig").empty();
              $(".httpConfig").append(http);
              $(".videoConfig").empty();
              $(".videoConfig").append(video);
              $(".liveConfig").empty();
              $(".liveConfig").append(live);
              renderForm();
            }
        }
      })
    },
    addCenter:function(){
      $("#centerConfigList").hide();
      $("#addCenterConfig").show();
      $("#center_id").val("");
      $(".center_name").val("");
      $(".center_clientA").val("");
      $(".center_clientB").val("");

    },
    // 添加中心服务器配置
    addCenterConfig:function(){
      if($("#center_id").val()!=""){
        $.ajax({
          type:'POST',
          url:request('/console/addcenterConfig'),
          data:{
            id:$("#center_id").val(),
            type:$(".center_type").val(),
            name:$(".center_name").val(),
            clientA:$(".center_clientA").val(),
            clientB:$(".center_clientB").val(),
            area:$(".urlConfigIds").val(),
            status:$(".center_status").val()
          },
          async:false,
          success:function(result){
              checkRequst(result);
              console.log(result);
              $("#center_id").val("");
          }
        })
      }else {
        Common.invoke({
          url:request('/console/addcenterConfig'),
          data:{
            type:$(".center_type").val(),
            name:$(".center_name").val(),
            clientA:$(".center_clientA").val(),
            clientB:$(".center_clientB").val(),
            area:$(".urlConfigIds").val(),
            status:$(".center_status").val()
          },
          success:function(result){
            console.log(result);
          }
        })
      }
    },
    // 根据type查询中心服务器配置
    findByType_center:function(id){
      var html="";
      Common.invoke({
        url:request('/console/findCenterConfig'),
        data:{
          id:id
        },
        success:function(result){
          console.log(result);
          if(result.data.count!=0){
            if(id!=""&&id!=undefined){
              $(".center_type").find("option[value='"+result.data.data[0].type+"']").attr("selected",true);
              $("#center_id").val(result.data.data[0].id);
              $(".center_name").val(result.data.data[0].name);
              $(".center_clientA").val(result.data.data[0].clientA);
              $(".center_clientB").val(result.data.data[0].clientB);
              $(".urlConfigIds").find("option[value='"+result.data.data[0].area+"']").attr("selected",true);
              $(".center_status").val(result.data.data[0].status);
              $("#centerConfigList").hide();
              $("#addCenterConfig").show();
            }else {
              for(var i=0;i<result.data.data.length;i++){
                  var type="";
                  if(result.data.data[i].type==1){
                    type="xmpp服务器";
                  }else if(result.data.data[i].type==2){
                    type="http服务器";
                  }else if(result.data.data[i].type==3){
                    type="视频服务器";
                  }else if(result.data.data[i].type==4){
                    type="直播服务器";
                  }else if(result.data.data[i].type==5){
                    type="上传服务器";
                  }else if(result.data.data[i].type==6){
                    type="下载服务器";
                  }else if(result.data.data[i].type==0){
                    type="全部类型";
                  }
                  html+="<tr><td>"+result.data.data[i].name+"</td><td>"+type+"</td><td>"+result.data.data[i].clientA
                  +"</td><td>"+result.data.data[i].clientB+"</td><td>"+result.data.data[i].area+"</td><td>"+(result.data.data[i].status==1?"正常":"禁用")
                  +"</td><td><a class='layui-btn layui-btn-primary layui-btn-xs' onclick='Clu.findByType_center(\""+result.data.data[i].id+"\")'>修改</a><a onclick='Clu.deleteCenter(\""+result.data.data[i].id+"\")' class='layui-btn layui-btn-danger layui-btn-xs'>删除</a></td></tr>";
              }
              $("#center_tab").empty();
              $("#center_tab").append(html);
              // $("#center_id").val(result.data.data[0].id);
              // $(".center_name").val(result.data.data[0].name);
              // $(".center_clientA").val(result.data.data[0].clientA);
              // $(".center_clientB").val(result.data.data[0].clientB);
              // console.log("根据type查询中心服务器配置中的地区id"+result.data.data[0].area);
              // $(".urlConfigIds").find("option[value='"+result.data.data[0].area+"']").attr("selected",true);
              // $(".center_status").val(result.data.data[0].status);
              if(result.data.data[0].status==1){
                $("#zc").selected=true;
              }else {
                $("#jy").selected=true;
              }
            }
          }else{
            $("#center_id").val("");
            $(".center_name").val("");
            $(".center_clientA").val("");
            $(".center_clientB").val("");
            $(".urlConfigIds").val("");
            $(".center_status").val("");
          }
          renderForm();
        }

      })
    },
    // 删除中心服务器
    deleteCenter:function(id){
      layui.layer.confirm('确定删除该服务器',{icon:3, title:'提示消息'},function(index){
          Common.invoke({
            url:request('/console/deleteCenter'),
            data:{
              id:id
            },
            success:function(result){
                
                Clu.areaConfigList();
                Clu.findByType_center();
               $("#addCenterConfig").hide();
               $("#centerConfigList").show();
               $("#server_list").hide();
               layui.layer.alert("删除成功");

            }
        })
      })
      
    },
    // 删除服务器
    deleteServer:function(id,area){
      console.log("删除服务器id"+id);
      layui.layer.confirm('确定删除该服务器',{icon:3, title:'提示消息'},function(index){
        Common.invoke({
          url:request('/console/deleteServer'),
          data:{
            id:id
          },
          success:function(result){
            console.log("success");
            Clu.serverList(area);
            layer.closeAll();
          }
        })
      })
      
    },
    // 修改服务器
    updateServer:function(id){
      $("#server_tab").hide();
      $.ajax({
        type:"POST",
        url:request("/console/serverList"),
        data:{
          id:id
        },
        dataType:'json',
        async:false,
        success:function(result){
          checkRequst(result);
          console.log(result);
          $(".add_id").val(result.data.data[0].id);
          $(".add_name").val(result.data.data[0].name);
          $(".add_url").val(result.data.data[0].url);
          $(".add_port").val(result.data.data[0].port);
          $(".add_count").val(result.data.data[0].count);
          
          $(".serverAdd_area").find("option[value='"+result.data.data[0].area+"']").attr("selected",true);
          if(result.data.data[0].status==1){
            $("#server_add_zc").selected=true;
          }else {
            $("#server_add_jy").selected=true;
          }
        }
      })
      $("#server_add").show();
    },
    commit_updateServer:function(area){
      Common.invoke({
          url:request('/console/updateServer'),
          data:{
            id:$(".add_id").val(),
            name:$(".add_name").val(),
            url:$(".add_url").val(),
            port:$(".add_port").val(),
            count:$(".add_count").val(),
            maxPeople:$(".add_maxPeople").val(),
            area:$(".serverAdd_area").val(),
            type:$(".add_type").val(),
            status:$(".add_status").val()
          },
          success:function(result){
            layui.layer.alert("修改服务器成功");
            $("#server_add").hide();
            $("#server_tab").show();
            Clu.serverList(area);
              console.log("====修改服务器成功===>"+result);
          }
      })
    },
    // 保存总配置
    addTotalConfig:function(){
      Common.invoke({
          url:request('/console/addTotalConfig'),
          data:{
            area:$(".total_area").val(),
            xmppConfig:$(".xmppConfig").val(),
            liveConfig:$(".httpConfig").val(),
            httpConfig:$(".videoConfig").val(),
            videoConfig:$(".liveConfig").val(),
            name:$(".total_name").val(),
            status:$(".total_status").val()
          },
          success:function(result){
            console.log("==========保存总配置成功=========");
          }
      })
    },
    // 删除入口配置
    deleteUrlConfig:function(id){
      layer.confirm('确定删除该地区？',{icon:3, title:'提示信息'},function(index){
        Common.invoke({
            url:request('/console/deleteUrlConfig'),
            data:{
              id:id
            },
            success:function(result){
              Clu.findUrlConfig();
             Clu.areaConfigList();
             $("#addUrlConfig").hide();
             $("#urlConfigList").show();
              $("#server_list").hide();
              layui.layer.alert("删除入口配置成功");
            }
        })
        layer.closeAll();
      })
    },
    // 修改入口配置
    updateUrlConfig:function(id){
      console.log("进入修改");
      $("#urlConfig_id").val(id);
        $("#urlConfigList").hide();
        $.ajax({
           type:'POST',
           url:request('/console/findUrlConfig'),
           data:{
            id:id
           },
           async:false,
           success:function(result){
            checkRequst(result);
            console.log("修改指定入口");
            console.log(result);
            // if(result.data.data[0].type==1){
            //   $(".type").find("option[value='1']").attr("selected",true);
            // }else if(result.data.data[0].type==2){
            //   $(".type").find("option[value='2']").attr("selected",true);
            // }else if(result.data.data[0].type==3){
            //    $(".type").find("option[value='3']").attr("selected",true);
            // }else if(result.data.data[0].type==4){
            //   $(".type").find("option[value='4']").attr("selected",true);
            // }
            $(".type").find("option[value='"+result.data.data[0].type+"']").attr("selected",true);
            $(".areaUrlConfig").find("option[value='"+result.data.data[0].area+"']").attr("selected",true);
            $(".areaUrl").find("option[value='"+result.data.data[0].url+"']").attr("selected",true);
            
            renderForm();
           }
        })
        $("#addUrlConfig").show();
    },
    // 返回
    back:function(){
        $("#areaList").show();
        $("#add_area").hide();
        $("#server_tab").show();
        $("#server_add").hide();
        $("#urlConfigList").show();
        $("#addUrlConfig").hide();
        $("#server_list").hide();
        $("#centerConfigList").show();
        $("#addCenterConfig").hide();
    }
 }

let country='';
layui.form.on('select(country)',function(data){
  console.log(data.value);
  $("#province").empty();
  $("#province").append("<option value=''>请选择省份</option>");
  $("#city").empty();
  $("#city").append("<option value=''>请选择城市</option>");
  country=data.value;
  for(s in Addr[data.value]){
    // console.log(s);
    $("#province").append("<option value='"+s+"'>"+s+"</option>");
    renderForm();
  }
})

layui.form.on('select(province)',function(data){
  console.log(data.value);
  $("#city").empty();
  $("#city").append("<option value=''>请选择城市</option>");
  for(c in Addr[country][data.value]){
    // console.log(Addr[country][data.value][c]);
    $("#city").append("<option value='"+Addr[country][data.value][c]+"'>"+Addr[country][data.value][c]+"</option>");
    renderForm();
  }
})