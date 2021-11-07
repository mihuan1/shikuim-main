package com.shiku.mianshi.controller;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.PageVO;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Friends;

/**
 * 关系接口
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/friends")
public class FriendsController extends AbstractController{

	/** @Description: 新增关注
	* @param toUserId
	* @param fromAddType 1:二维码 2：名片 3：群组 4： 手机号搜索 5： 昵称搜索6:其他方式添加
	* @return
	**/ 
	@RequestMapping("/attention/add")
	public JSONMessage addAtt(@RequestParam Integer toUserId, @RequestParam(defaultValue = "0") Integer fromAddType) {
		try {
			int userId=ReqUtil.getUserId();
			if(userId==toUserId) {
				return JSONMessage.failure("不能添加自己");
			}
			String friendFroms = SKBeanUtils.getUserManager().getUser(toUserId).getSettings().getFriendFromList();
			List<Integer> friendFromList = StringUtil.getIntList(friendFroms, ",");
			if (null == friendFromList || 0 == friendFromList.size())
				return JSONMessage.failure("添加失败,该用户禁止该方式添加好友");
			else {
				if (!fromAddType.equals(0) && !fromAddType.equals(6) && !friendFromList.contains(fromAddType)) {
					return JSONMessage.failure((fromAddType.equals(1) ? "用户禁止二维码添加好友"
							: fromAddType.equals(2) ? "用户禁止名片添加好友"
									: fromAddType.equals(3) ? "用户禁止从群组中添加好友"
											: fromAddType.equals(4) ? "用户禁止手机号搜索添加好友"
													: fromAddType.equals(5) ? "用户禁止昵称搜索添加好友" : "添加好友失败"));
				}
			}
			JSONMessage followUser = SKBeanUtils.getFriendsManager().followUser(userId, toUserId, fromAddType);
			return followUser;
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure("添加好友失败");
		}
	}
	
	/** @Description:（批量添加好友） 
	* @param toUserId
	* @return
	**/ 
	@RequestMapping("/attention/batchAdd")
	public JSONMessage addFriends(@RequestParam(value = "toUserIds") String toUserIds) {
		return SKBeanUtils.getFriendsManager().batchFollowUser(ReqUtil.getUserId(), toUserIds);
	}
	
	//添加黑名单
	@RequestMapping("/blacklist/add")
	public JSONMessage addBlacklist(@RequestParam Integer toUserId) {
		int userId=ReqUtil.getUserId();
		if(userId==toUserId) {
			return JSONMessage.failure("不能操作自己");
		}
		if(SKBeanUtils.getFriendsManager().isBlack(toUserId))
			return JSONMessage.failure("不能重复拉黑好友");
		Object data=SKBeanUtils.getFriendsManager().addBlacklist(ReqUtil.getUserId(), toUserId);
		return JSONMessage.success("加入黑名单成功",data);
	}
	//加好友
	@RequestMapping("/add")
	public JSONMessage addFriends(@RequestParam(value = "toUserId") Integer toUserId) {
		int userId=ReqUtil.getUserId();
		if(userId==toUserId) {
			return JSONMessage.failure("不能添加自己");
		}
		Friends friends = SKBeanUtils.getFriendsManager().getFriends(userId, toUserId);
		if(null!=friends)
			return JSONMessage.failure("对方已经是你的好友!");
		SKBeanUtils.getFriendsManager().addFriends(userId, toUserId);

		return JSONMessage.success("加好友成功");
	}
	
	//修改好友 属性
	@RequestMapping("/update")
	public JSONMessage updateFriends(@RequestParam(value = "toUserId") Integer toUserId,@RequestParam(defaultValue="-1") String chatRecordTimeOut) {
		Friends friends = SKBeanUtils.getFriendsManager().getFriends(ReqUtil.getUserId(), toUserId);
		
		if(null==friends)
			 return JSONMessage.failure("好友不存在!");
		double recordTimeOut=-1;
		recordTimeOut=Double.valueOf(chatRecordTimeOut);
		friends.setChatRecordTimeOut(recordTimeOut);
		SKBeanUtils.getFriendsManager().updateFriends(friends);

		return JSONMessage.success();
	}
	//移出黑名单
	@RequestMapping("/blacklist/delete")
	@ResponseBody
	public JSONMessage deleteBlacklist(@RequestParam Integer toUserId) {
		if(!SKBeanUtils.getFriendsManager().isBlack(toUserId))
			return JSONMessage.failure("好友："+toUserId+"不在我的黑名单中");
		Object data = SKBeanUtils.getFriendsManager().deleteBlacklist(ReqUtil.getUserId(), toUserId);
		return JSONMessage.success("取消拉黑成功", data);
	}
	
	//取消关注
	@RequestMapping("/attention/delete")
	public JSONMessage deleteFollow(@RequestParam(value = "toUserId") Integer toUserId) {
		int userId=ReqUtil.getUserId();
		if(userId==toUserId) {
			return JSONMessage.failure("不能操作自己");
		}
		SKBeanUtils.getFriendsManager().unfollowUser(userId, toUserId);
		return JSONMessage.success("取消关注成功");
	}
	
	//删除好友
	@RequestMapping("/delete")
	public JSONMessage deleteFriends(@RequestParam Integer toUserId) {
		try {
			Integer userId = ReqUtil.getUserId();
			if(userId==toUserId) {
				return JSONMessage.failure("不能操作自己");
			}
			Friends friends = SKBeanUtils.getFriendsManager().getFriends(userId, toUserId);
			if(null==friends)
				return JSONMessage.failure("对方不是你的好友!");
			SKBeanUtils.getFriendsManager().deleteFriends(userId, toUserId);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
		return JSONMessage.success("删除好友成功");
	}
	//修改备注
	@RequestMapping("/remark")
	public JSONMessage friendsRemark(@RequestParam int toUserId, @RequestParam(defaultValue = "") String remarkName,@RequestParam(defaultValue = "") String describe) {
		try {
			SKBeanUtils.getFriendsManager().updateRemark(ReqUtil.getUserId(), toUserId, remarkName,describe);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
		return JSONMessage.success();
	}
	//黑名单列表
	@RequestMapping("/blacklist")
	public JSONMessage queryBlacklist(@RequestParam(defaultValue="0") int pageIndex,@RequestParam(defaultValue="10") int pageSize) {
		List<Friends> data = SKBeanUtils.getFriendsManager().queryBlacklist(ReqUtil.getUserId(),pageIndex,pageSize);

		return JSONMessage.success(null, data);
	}
	
	//适用于web黑名单分页
	@RequestMapping("/queryBlacklistWeb")
	public JSONMessage queryBlacklistWeb(@RequestParam(defaultValue="0") int pageIndex,@RequestParam(defaultValue="10") int pageSize) {
		PageVO queryBlacklistWeb = SKBeanUtils.getFriendsManager().queryBlacklistWeb(ReqUtil.getUserId(),pageIndex,pageSize);
		return JSONMessage.success(null, queryBlacklistWeb);
	}
	
	//粉丝列表
	@RequestMapping("/fans/list")
	public JSONMessage queryFans(@RequestParam(defaultValue = "0") Integer userId) {

		return JSONMessage.success();
	}
	
	//关注列表
	@RequestMapping("/attention/list")
	public JSONMessage queryFollow(@RequestParam(defaultValue = "") Integer userId,@RequestParam(defaultValue = "0")int status) {
		logger.info("/attention/list,userId:"+ReqUtil.getUserId());
		List<Friends> data = SKBeanUtils.getFriendsManager().queryFollow(ReqUtil.getUserId(),status);

		return JSONMessage.success(null, data);
	}
	
	@RequestMapping("/get")
	public JSONMessage getFriends(@RequestParam(defaultValue = "") Integer userId,int toUserId) {
		userId = ReqUtil.getUserId();
		//userId = (null == userId ? ReqUtil.getUserId() : userId);
		Friends data = SKBeanUtils.getFriendsManager().getFriends(userId, toUserId);

		return JSONMessage.success(null, data);
	}
	

	@RequestMapping("/list")
	public JSONMessage queryFriends(@RequestParam(defaultValue="") Integer userId,@RequestParam(defaultValue="") String keyword) {
		//userId = (null == userId ? ReqUtil.getUserId() : userId);
		userId =ReqUtil.getUserId();
		Object data = SKBeanUtils.getFriendsManager().queryFriends(userId);

		return JSONMessage.success(null, data);
	}

	@RequestMapping("/page")
	public JSONMessage getFriendsPage(@RequestParam Integer userId,@RequestParam(defaultValue="") String keyword,
			@RequestParam(defaultValue = "2") int status,
			@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize) {
		userId =ReqUtil.getUserId();
		Object data = SKBeanUtils.getFriendsManager().queryFriends(userId,status,keyword, pageIndex, pageSize);

		return JSONMessage.success(null, data);
	}

	
	/** @Description:是否开启或关闭消息免打扰、阅后即焚、聊天置顶
	* @param userId
	* @param toUserId
	* @param offlineNoPushMsg  0:关闭,1:开启
	* @param type  type = 0  消息免打扰 ,type = 1  阅后即焚 ,type = 2 聊天置顶
	* @return
	**/ 
	@RequestMapping("/update/OfflineNoPushMsg")
	public JSONMessage updateOfflineNoPushMsg(@RequestParam Integer userId,@RequestParam Integer toUserId,@RequestParam(defaultValue="0") int offlineNoPushMsg,@RequestParam(defaultValue = "0")int type){
		try {
			Friends data=SKBeanUtils.getFriendsManager().updateOfflineNoPushMsg(ReqUtil.getUserId(),toUserId,offlineNoPushMsg,type);
			return JSONMessage.success(data);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	@RequestMapping("/friendsAndAttention") //返回好友的userId 和单向关注的userId  及黑名单的userId
	public JSONMessage getFriendsPage(@RequestParam Integer userId,@RequestParam(defaultValue="") String type) {
		Object data = SKBeanUtils.getFriendsManager().friendsAndAttentionUserId(ReqUtil.getUserId(),type);
		return JSONMessage.success(null, data);
	}
	
	@RequestMapping("/newFriend/list")
	public JSONMessage newFriendList(@RequestParam Integer userId,@RequestParam(defaultValue="0") int pageIndex,
			@RequestParam(defaultValue="10") int pageSize) {
		Object data = SKBeanUtils.getFriendsManager().newFriendList(ReqUtil.getUserId(), pageIndex, pageSize);

		return JSONMessage.success(null, data);
	}
	
	@RequestMapping("/newFriendListWeb")
	public JSONMessage newFriendListWeb(@RequestParam Integer userId,@RequestParam(defaultValue="0") int pageIndex,
			@RequestParam(defaultValue="10") int pageSize) {
		try {
			Object data = SKBeanUtils.getFriendsManager().newFriendListWeb(ReqUtil.getUserId(), pageIndex, pageSize);
			return JSONMessage.success(data);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}	
	
	/*
	 * @RequestMapping("/addAllFriendsSys") public JSONMessage
	 * addAllFriendsSys(@RequestParam(defaultValue="10000") Integer toUserId) {
	 * List<Integer> userIds=SKBeanUtils.getUserManager().getAllUserId();
	 * ExecutorService pool = Executors.newFixedThreadPool(10); for (Integer userId
	 * : userIds) { pool.execute(new Runnable() {
	 * 
	 * @Override public void run() {
	 * SKBeanUtils.getFriendsManager().followUser(userId, toUserId, 0); } });
	 * 
	 * } return JSONMessage.success(); }
	 */

}
