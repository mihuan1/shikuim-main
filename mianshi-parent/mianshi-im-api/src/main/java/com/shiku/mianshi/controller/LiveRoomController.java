package com.shiku.mianshi.controller;

import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.LiveRoom;


@RestController
@RequestMapping("/liveRoom")
public class LiveRoomController extends AbstractController{
	
	//获取直播间详情
	@RequestMapping(value = "/get")
	public JSONMessage getLiveRoom(@RequestParam(defaultValue="") String roomId) {
		Object data=null;
		try {
			LiveRoom room=SKBeanUtils.getLiveRoomManager().get(new ObjectId(roomId));
			if(!room.getUrl().contains("//"))
				room.setUrl(KSessionUtil.getClientConfig().getLiveUrl()+room.getUrl());
			data=room;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSONMessage.success(null,data);
	}
	
	//获取直播间详情
	@RequestMapping(value = "/getLiveRoom")
	public JSONMessage getMyLiveRoom(@RequestParam(defaultValue = "0") Integer userId) {
		Object data = null;
		try {
			LiveRoom room = SKBeanUtils.getLiveRoomManager().getLiveRoom(userId);
//			if (room != null && room.getUrl().contains("//"))
//				room.setUrl(KSessionUtil.getClientConfig().getLiveUrl() + room.getUrl());
			data = room;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSONMessage.success(data);
	}
	
	//获取所有的直播房间
	@RequestMapping(value = "/list")
	public JSONMessage findLiveRoomList(@RequestParam(defaultValue="") String name,@RequestParam(defaultValue="") String nickName,
			@RequestParam(defaultValue="0") Integer userId,@RequestParam(defaultValue="0") Integer pageIndex,
			@RequestParam(defaultValue="10") Integer pageSize,@RequestParam(defaultValue="-1") Integer status) {
		
		try {
			Object data=SKBeanUtils.getLiveRoomManager().findLiveRoomList(name, nickName, userId, pageIndex, pageSize,status,0);
			return JSONMessage.success(null,data);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	
	
	//创建直播间
	@RequestMapping(value = "/create")
	public JSONMessage createLiveRoom(@ModelAttribute LiveRoom room) {
		Object data=null;
		try {
			room.setUserId(ReqUtil.getUserId());
			data=SKBeanUtils.getLiveRoomManager().createLiveRoom(room);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return JSONMessage.success(null,data);
	}
	//更新直播间
	@RequestMapping(value = "/update")
	public JSONMessage updateLiveRoom(@ModelAttribute LiveRoom room) {
		Map<String,Object> data=new HashMap<String,Object>();
		try {
			SKBeanUtils.getLiveRoomManager().updateLiveRoom(ReqUtil.getUserId(),room);
			return JSONMessage.success();
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
		
		
	}
	//删除直播间
	@RequestMapping(value = "/delete")
	public JSONMessage deleteLiveRoom(@RequestParam(defaultValue="")String roomId) {
		try {
			LiveRoom room = SKBeanUtils.getLiveRoomManager().getLiveRoom(ReqUtil.getUserId());
			if(room.getRoomId().toString().equals(roomId)){
				SKBeanUtils.getLiveRoomManager().deleteLiveRoom(new ObjectId(roomId));
				return JSONMessage.success();
			}else{
				return JSONMessage.failure(null);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
		
		
	}
	//开始/结束直播
	@RequestMapping(value="/start")
	public JSONMessage start(@RequestParam String roomId,@RequestParam int status){
		try {
			SKBeanUtils.getLiveRoomManager().start(new ObjectId(roomId), status);
			return JSONMessage.success();
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	//查询房间成员
	@RequestMapping(value = "/memberList")
	public JSONMessage findLiveRoomMemberList(@RequestParam(defaultValue="") String roomId) {
		Object data=null;
		ObjectId id=null;
		try {
			if(!StringUtil.isEmpty(roomId))
				id=new ObjectId(roomId);
			data=SKBeanUtils.getLiveRoomManager().findLiveRoomMemberList(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return JSONMessage.success(null,data);
	}
	//获取单个成员
	@RequestMapping(value="/get/member")
	public JSONMessage getLiveRoomMember(@RequestParam String roomId,@RequestParam Integer userId){
		Object data=null;
		data=SKBeanUtils.getLiveRoomManager().getLiveRoomMember(new ObjectId(roomId), userId);
		if(data==null){
			return JSONMessage.failure("用户不在该房间");
		}else{
			return JSONMessage.success(null, data);
		}
		
		
	}
	//加入直播间
	@RequestMapping(value = "/enterInto")
	public JSONMessage enterIntoLiveRoom(@RequestParam(defaultValue="")String roomId) {
		/*Map<String,Object> data=new HashMap<String,Object>();*/
		boolean red=true;
		try {
			red=SKBeanUtils.getLiveRoomManager().enterIntoLiveRoom(ReqUtil.getUserId(), new ObjectId(roomId));
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
		if(red==false){
			return JSONMessage.failure(null);
		}else{
			return JSONMessage.success();
		}
		
	}
	//退出直播间
	@RequestMapping(value = "/quit")
	public JSONMessage exitLiveRoom(@RequestParam(defaultValue="")String roomId,@RequestParam(defaultValue="0")Integer userId) {
		try {
			if(userId==ReqUtil.getUserId()){
				SKBeanUtils.getLiveRoomManager().exitLiveRoom(userId, new ObjectId(roomId));
				return JSONMessage.success();
			}else{
				return JSONMessage.failure(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(null);
		}
		
	}
	
	//踢出直播间
	@RequestMapping(value="/kick")
	public JSONMessage kick(@RequestParam String roomId,@RequestParam Integer userId){
		try {
			SKBeanUtils.getLiveRoomManager().kick(userId, new ObjectId(roomId));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSONMessage.success();
		
	}

	//开启/取消禁言
	@RequestMapping("/shutup")
	public JSONMessage shutup(@RequestParam int state,@RequestParam Integer userId,@RequestParam String roomId){
		try {
			SKBeanUtils.getLiveRoomManager().shutup(state, userId,new ObjectId(roomId));
		} catch (Exception e) {
			e.printStackTrace();	
		}
		return JSONMessage.success();
	}
	
	
	//发送弹幕
	@RequestMapping("/barrage")
	public JSONMessage barrage(@RequestParam Integer userId,@RequestParam ObjectId roomId,@RequestParam String text){
		JSONObject data=new JSONObject();
		ObjectId givegiftId;
		try {
			givegiftId=SKBeanUtils.getLiveRoomManager().barrage(userId,roomId,text);
			data.put("givegiftId",givegiftId);
			return JSONMessage.success(null,data);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
		/*if(judge==true){
			return JSONMessage.success();
		}else{
			return JSONMessage.failure("余额不足");
		}*/
		
	}
	
	//显示所有礼物
	@RequestMapping(value="/giftlist")
	public JSONMessage giftlist(@RequestParam(defaultValue="") String name ,@RequestParam(defaultValue="0") Integer pageIndex,@RequestParam(defaultValue="10") Integer pageSize){
		try {
			Object data=null;
			data=SKBeanUtils.getLiveRoomManager().findAllgift(name,pageIndex, pageSize);
			if(null == data)
				return JSONMessage.failure("暂无礼物！");
			return JSONMessage.success(data);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	//送礼物
	@RequestMapping(value="/give")
	public JSONMessage give(@RequestParam Integer userId,@RequestParam Integer toUserId,@RequestParam String giftId,@RequestParam int count,
			@RequestParam Double price,@RequestParam String roomId){
			JSONObject data=new JSONObject();
			ObjectId giftid;
		try {
			giftid=SKBeanUtils.getLiveRoomManager().giveGift(ReqUtil.getUserId(),toUserId ,new ObjectId(giftId), count, price,new ObjectId(roomId));
			data.put("giftId",giftid);
			return JSONMessage.success(null,data);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	
	/*//收到的礼物列表
	@RequestMapping(value="/getList")
	public JSONMessage get(@RequestParam Integer userId){
		Object data=null;
		data=SKBeanUtils.getLiveRoomManager().getList(userId);
		return JSONMessage.success(null, data);
	}*/
	
	//查询购买礼物的记录
	@RequestMapping(value="/giftdeal")
	public JSONMessage giftdeal(@RequestParam Integer userId,@RequestParam(defaultValue="10") Integer pageSize,@RequestParam(defaultValue="0") Integer pageIndex){
		Object data=null;
		try {
			data=SKBeanUtils.getLiveRoomManager().giftdeal(userId, pageIndex, pageSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSONMessage.success(null, data);
	}
	
	//设置管理员
	@RequestMapping(value="/setmanage")
	public JSONMessage setManage(@RequestParam Integer userId,@RequestParam int type,@RequestParam String roomId){
		try {
			SKBeanUtils.getLiveRoomManager().setmanage(userId,type,new ObjectId(roomId));
			return JSONMessage.success();
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	//点赞
	@RequestMapping(value="/praise")
	public JSONMessage addpraise(@RequestParam String roomId){
		try {
			SKBeanUtils.getLiveRoomManager().addpraise(new ObjectId(roomId));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSONMessage.success();
	}
	
	//清除过期直播间
	@RequestMapping(value="/clear")
	public JSONMessage clearLiveRoom(){
		try {
			SKBeanUtils.getLiveRoomManager().clearLiveRoom();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return JSONMessage.success();
	}
	
}
