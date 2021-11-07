package com.shiku.mianshi.controller;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.xyz.commons.constants.KConstants.Result;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.FriendGroup;
/**
* @Description: TODO(好友分组接口)
* @author lidaye
* @date 2018年6月7日
 */
@RestController
public class FriendGroupController extends AbstractController{

	//好友分组列表
	@RequestMapping("/friendGroup/list")
	public JSONMessage friendGroupList() {
		Object data=null;
		try {
			data=SKBeanUtils.getFriendGroupManager().queryGroupList(ReqUtil.getUserId());
			return JSONMessage.success(null, data);
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failureAndData(e.getMessage(), data);
		}
	}
	
	@RequestMapping("/friendGroup/add")
	public JSONMessage friendGroupAdd(@ModelAttribute FriendGroup group) {
		Object data=null;
		try {
			if(null!=SKBeanUtils.getFriendGroupManager().queryGroupName(ReqUtil.getUserId(),group.getGroupName())){
				//分组名称已存在
				return JSONMessage.failure("分组名称已存在");
			}
			if(0==group.getUserId())
				group.setUserId(ReqUtil.getUserId());
			
			data=SKBeanUtils.getFriendGroupManager().saveGroup(group);
			return JSONMessage.success(data);
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	//更新分组的好友列表
	@RequestMapping("/friendGroup/updateGroupUserList")
	public JSONMessage updateGroupUserList(@RequestParam(defaultValue="")String groupId,
			@RequestParam(defaultValue="")String userIdListStr) {
		try {
			if(!ObjectId.isValid(groupId))
				return Result.ParamsAuthFail;
			List<Integer> userIdList=StringUtil.getIntList(userIdListStr, ",");
			SKBeanUtils.getFriendGroupManager().updateGroupUserList(ReqUtil.getUserId(),parse(groupId), userIdList);
			return JSONMessage.success();
			
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	@RequestMapping("/friendGroup/updateFriend")
	public JSONMessage updateFriend(@RequestParam Integer toUserId,@RequestParam(defaultValue="") String groupIdStr) {
		try {
			List<String> groupIdList = StringUtil.getListBySplit(groupIdStr,",");
			SKBeanUtils.getFriendGroupManager().updateFriendGroup(ReqUtil.getUserId(), toUserId,groupIdList);
			return JSONMessage.success();
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	@RequestMapping("/friendGroup/update")
	public JSONMessage friendGroupUpdate(@RequestParam String groupId,@RequestParam String groupName) {
		try {
			if(!ObjectId.isValid(groupId))
				return Result.ParamsAuthFail;
			SKBeanUtils.getFriendGroupManager().updateGroupName(ReqUtil.getUserId(),parse(groupId), groupName);
			return JSONMessage.success();
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	@RequestMapping("/friendGroup/delete")
	public JSONMessage friendGroupDelete(@RequestParam String groupId) {
		try {
			if(!ObjectId.isValid(groupId))
				return Result.ParamsAuthFail;
			SKBeanUtils.getFriendGroupManager().deleteGroup(ReqUtil.getUserId(),parse(groupId));
			return JSONMessage.success();
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	

}
