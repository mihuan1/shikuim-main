package com.shiku.mianshi.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import cn.xyz.commons.utils.HttpUtil;
import cn.xyz.commons.utils.Md5Util;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Menu;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;

@RestController
public class MpController {
	
	//公众号菜单列表
	@RequestMapping("/public/menu/list")
	public JSONMessage getMenuList(int userId) {
		userId = 0 != userId ? userId : ReqUtil.getUserId();
		List<Menu> data = SKBeanUtils.getMenuManager().getMenu(userId);
		return JSONMessage.success(null, data);
	}
	
	//单条图文
	@RequestMapping("/pulic/pushToAll")
	public void pushToAll() throws Exception{
		Integer userId=ReqUtil.getUserId();
		JSONObject jsonObj=new JSONObject();
		jsonObj.put("title", "夸夸群：今天你想被夸什么");
//		jsonObj.put("sub", "Test a single text message");
		jsonObj.put("sub", "抓紧点开浏览吧");
		jsonObj.put("img", "http://www.wallcoo.com/nature/Daily_snap_shot_1920x1200/wallpapers/1440x900/snap_shot_friends_forever.jpg");
		String spareURL = "http://img.alicdn.com/imgextra/i1/2966936406/TB22LMmpOAKL1JjSZFoXXagCFXa_%21%212966936406.jpg";// 备用地址
		jsonObj.put("url", HttpUtil.testWsdlConnection("https://mp.weixin.qq.com/s?__biz=MjM5ODA0NTc4MA==&mid=2652769987&idx=1&sn=d8c8dad1431f5e129caf429f7d1f95fa&chksm=bd3a1ff88a4d96eeddd6c632c001f9e26649ba53f14a383e58cfe5a51a8928123d0518d94fe8&scene=0&xtrack=1#rd", spareURL));
		List<Integer> toUserIdList = Lists.newArrayList();
		User touser=SKBeanUtils.getUserManager().getUser(userId);
		toUserIdList.add(userId);
		User user=SKBeanUtils.getUserManager().getUser(10000);
		user.setPassword(Md5Util.md5Hex("10000"));
		MessageBean messageBean=new MessageBean();
		messageBean.setType(80);
		messageBean.setFromUserId(user.getUserId().toString());
		messageBean.setFromUserName(user.getNickname());
		messageBean.setToUserId(touser.getUserId().toString());
		messageBean.setToUserName(touser.getUsername());
		messageBean.setContent(jsonObj.toString());
		messageBean.setMsgType(0);// 单聊消息
		messageBean.setMessageId(StringUtil.randomUUID());
		try {
			
			KXMPPServiceImpl.getInstance().send(messageBean);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//多条图文
	@RequestMapping("/public/manyToAll")
	public void manyToAll() throws Exception{
		Integer userId=ReqUtil.getUserId();
		JSONObject jsonObj=null;
		String[] title={"我的生活难道就只能这样了吗？","一个人要怎么逃离单调乏味的生活","如何看待他人的突风猛进与自己的原地踏步"};
		String spareURL = "http://img.alicdn.com/imgextra/i1/2966936406/TB22LMmpOAKL1JjSZFoXXagCFXa_%21%212966936406.jpg";// 备用地址
		String[] url={HttpUtil.testWsdlConnection("https://mp.weixin.qq.com/s?__biz=MjM5ODA0NTc4MA==&mid=2652769941&idx=2&sn=252c088e8da1dddc2aba1e8f406738a2&chksm=bd3a1fae8a4d96b8f6e885716caec3a1c0eb82182c94f6041d1febd782936ab0b1c2c4ae1c30&mpshare=1&scene=1&srcid=#rd", spareURL),HttpUtil.testWsdlConnection("https://mp.weixin.qq.com/s?__biz=MjM5ODA0NTc4MA==&mid=2652769941&idx=4&sn=9ace44b09bda9fa074b9434e9fae1fd2&chksm=bd3a1fae8a4d96b8220f1870ecb25f8fe2fed8323df9a740f939877d11b29044b72ea115551a&mpshare=1&scene=1&srcid=#rd", spareURL),HttpUtil.testWsdlConnection("https://mp.weixin.qq.com/s?__biz=MjM5ODA0NTc4MA==&mid=2652770099&idx=2&sn=301e89c48c7a2c11fc8017c74d8dcdb0&chksm=bd3a1e088a4d971ead7697506eff3351227b582ec1adaaee62a20f071be224df96442b2e83cf&mpshare=1&scene=1&srcid=#rd", spareURL)};
		String[] img={"http://pic13.nipic.com/20110425/668573_150157400119_2.jpg","http://img.daimg.com/uploads/allimg/120528/3-12052Q95604205.jpg","http://img3.redocn.com/20120528/Redocn_2012052800520295.jpg"};
		List<Object> list=new ArrayList<Object>();
		for(int i=0;i<title.length;i++){
			jsonObj=new JSONObject();
			jsonObj.put("title",title[i]);
			jsonObj.put("url", url[i]);
			jsonObj.put("img", img[i]);
			list.add(jsonObj);
		}
		List<Integer> toUserIdList = Lists.newArrayList();
		User touser=SKBeanUtils.getUserManager().getUser(userId);
		toUserIdList.add(touser.getUserId());
		User user=SKBeanUtils.getUserManager().getUser(10000);
		user.setPassword(Md5Util.md5Hex("10000"));
		MessageBean messageBean=new MessageBean();
		messageBean.setType(81);
		messageBean.setFromUserId(user.getUserId().toString());
		messageBean.setFromUserName(user.getNickname());
		messageBean.setToUserId(touser.getUserId().toString());
		messageBean.setToUserName(touser.getUsername());
		messageBean.setContent(list.toString());
		messageBean.setMsgType(0);// 单聊消息
		messageBean.setMessageId(StringUtil.randomUUID());
		try {
			if(user.getUserId()==10000){
				KXMPPServiceImpl.getInstance().send(messageBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//公众号搜索
	@RequestMapping("/public/search/list")
	public JSONMessage searchToMp(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int limit,
			@RequestParam(defaultValue = "") String keyWorld){
		
		Object data = SKBeanUtils.getUserManager().queryPublicUser(page, limit, keyWorld);
		return JSONMessage.success(data);
	}
		
}
