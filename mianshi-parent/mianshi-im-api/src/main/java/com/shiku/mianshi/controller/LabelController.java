package com.shiku.mianshi.controller;


import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.lable.Label;
import cn.xyz.mianshi.utils.SKBeanUtils;


@RestController
@RequestMapping("/label")
public class LabelController extends AbstractController {

    //创建群标识码
    @RequestMapping("/create")
    public JSONMessage create(@RequestParam Integer userId,@RequestParam(defaultValue="") String logo,@RequestParam String name) {
        if(null == userId)
            return JSONMessage.failure("userId is null");

        Label newLabel ;

        if(null == name && null == logo)
        {
            newLabel =  SKBeanUtils.getLabelManager().createLabel(userId);
        }
        else
        {
            Object data = SKBeanUtils.getLabelManager().queryLabelByName(name);

            if(data != null)
                return JSONMessage.failure("群标识码名已经使用");
            newLabel =  SKBeanUtils.getLabelManager().createLabelByParams(userId,logo,name);

        }

        addLabel(userId,newLabel.getCode(),0);
        return JSONMessage.success();
    }

    //获取群标识码
    @RequestMapping("/getlabel")
    public JSONMessage getLabel(@RequestParam ObjectId labelId) {
        if(null == labelId)
            return JSONMessage.failure("id is null");
       Object data =  SKBeanUtils.getLabelManager().getLabel(labelId);
        return JSONMessage.success(null,data);
    }
    //获取群标识码列表
    @RequestMapping("/getlabels")
    public JSONMessage getLabelList(@RequestParam Integer userId) {
        if(null == userId)
            return JSONMessage.failure("userId is null");
        Object data =SKBeanUtils.getLabelManager().getLabelList(userId);
        return JSONMessage.success(null,data);
    }

    //跟新群标识码
    @RequestMapping("/update")
    public JSONMessage updateLabel(@ModelAttribute Label label) {
        if(null == label.getCode())
            return JSONMessage.failure("label code  is null");

        Object data =SKBeanUtils.getLabelManager().updateLabel(label);
        if(null == data)
            return JSONMessage.failure("label can not find");
        else
             return JSONMessage.success(null,data);
    }

    //更新群标识码
    @RequestMapping("/save")
    public JSONMessage saveLabel(@RequestParam ObjectId id,String name, String logo) {
       if(null == id)
         return JSONMessage.failure("id is null");

        Object data =SKBeanUtils.getLabelManager().saveLabel(id,name,logo);
        return JSONMessage.success(null,data);
    }

    //添加群标识码
    @RequestMapping("/add")
    public JSONMessage addLabel(@RequestParam Integer userId,@RequestParam(defaultValue="") String code,@RequestParam long date) {

        if(null == userId)
            return JSONMessage.failure("user Id is null");

        if(null == code)
            return JSONMessage.failure("label code is null");

       Label label =SKBeanUtils.getLabelManager().getLabelByCode(code);

        if(label == null)//无效群标识码
              return JSONMessage.failure("无效群标识码");


       Object object =SKBeanUtils.getUserLabelManager().queryUserLabel(userId,label.getId().toString());
       if(object != null)
           return JSONMessage.failure("群标识码已添加");

        Object data =SKBeanUtils.getUserLabelManager().addLabel(userId,label.getId().toString(),label.getName(),label.getLogo(),code,date);

        if(data == null)
            return JSONMessage.failure(" 群标识码添加失败");
        else
            return JSONMessage.success(null,data);
    }

    //通过群标识码名称添加
    @RequestMapping("/addByName")
    public JSONMessage addByName(@RequestParam Integer userId,
    		@RequestParam(defaultValue="") String name,@RequestParam(defaultValue="0") long date) {
        if(null == userId)
            return JSONMessage.failure("user Id is null");
        if(null == name)
            return JSONMessage.failure("群标识码名不能为空");

        Label label =SKBeanUtils.getLabelManager().queryLabelByName(name);

        if(label == null)
            return JSONMessage.failure("群标识码不存在");

        Object object =SKBeanUtils.getUserLabelManager().queryUserLabel(userId,label.getId().toString());
        if(object != null)
            return JSONMessage.failure("群标识码已添加");

        Object data =SKBeanUtils.getUserLabelManager().addLabel(userId,label.getId().toString(),label.getName(),label.getLogo(),label.getCode(),date);

        if(data == null)
            return JSONMessage.failure(" 群标识码添加失败");
        else
            return JSONMessage.success(null,data);
    }



    //获取群标识码列表
    @RequestMapping("/getUserLabels")
    public JSONMessage getUserLabels(@RequestParam Integer userId) {
        if (null == userId)
            return JSONMessage.failure("user id is null");
        Object data =SKBeanUtils.getUserLabelManager().getUserLabels(userId);
        return JSONMessage.success(null,data);
    }


    //查收群标识码
    @RequestMapping("/open")
    public JSONMessage  openLabel(Integer userId,String code){
       Object data = SKBeanUtils.getUserLabelManager().queryUserLabelByCode(userId,code);
        return  JSONMessage.success(null,data);
    }

    //查询群标识码名是否已用
    @RequestMapping("/queryLabelByName")
    public JSONMessage  queryLabelByName(Integer userId,String name){
        Object data = SKBeanUtils.getLabelManager().queryLabelByName(name);
        if(data == null)
            return JSONMessage.success();
        else
            return JSONMessage.failure("群标识码名已添加");
    }

    //判断是否已经添加
    @RequestMapping("/isBuyLabel")
    public JSONMessage  isBuyLabel(Integer userId,String name){
        Label label =SKBeanUtils.getLabelManager().queryLabelByName(name);

        if(label == null)//无效群标识码
            return JSONMessage.failure("群标识码不存在");

        Object object =SKBeanUtils.getUserLabelManager().queryUserLabel(userId,label.getId().toString());
        if(object != null)
            return JSONMessage.failure("群标识码已添加");

        return JSONMessage.success();
    }
}
