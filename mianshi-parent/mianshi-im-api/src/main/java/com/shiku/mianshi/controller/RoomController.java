package com.shiku.mianshi.controller;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import cn.xyz.commons.constants.KConstants.Result;
import cn.xyz.commons.constants.KConstants.ResultCode;
import cn.xyz.commons.constants.KConstants.ResultMsgs;
import cn.xyz.commons.constants.KConstants.Room_Role;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.PageVO;
import cn.xyz.mianshi.model.RoomVO;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.GroupHelper;
import cn.xyz.mianshi.vo.Room;
import cn.xyz.mianshi.vo.Room.Member;
import cn.xyz.mianshi.vo.Room.Notice;
import cn.xyz.mianshi.vo.User;

/**
 * 群组接口
 * 
 * @author Administrator
 *
 */
@Slf4j
@RestController
@RequestMapping("/room")
public class RoomController {

	/*@Resource(name = SKBeanUtils.getRoomManagerImplForIM().BEAN_ID)
	private SKBeanUtils.getRoomManagerImplForIM()ImplForIM SKBeanUtils.getRoomManagerImplForIM();*/

	/**
	 * 新增房间
	 * 
	 * @param room
	 * @param text
	 * @return
	 */
	@RequestMapping("/add")
	public JSONMessage add(@ModelAttribute Room room, @RequestParam(defaultValue = "") String text) {
		List<Integer> idList = StringUtil.isEmpty(text) ? null : JSON.parseArray(text, Integer.class);
		/*if(null!=SKBeanUtils.getRoomManagerImplForIM().exisname(room.getName(),null)){
			return JSONMessage.failure("房间名已经存在");
		}*/
		Object data = SKBeanUtils.getRoomManagerImplForIM().add(SKBeanUtils.getUserManager().getUser(ReqUtil.getUserId()), room, idList);
		return JSONMessage.success(null, data);
		
	}

	/**
	 * 删除房间
	 * 
	 * @param roomId
	 * @return
	 */
	@RequestMapping("/delete")
	public JSONMessage delete(@RequestParam String roomId) {
		try {
			int userId=ReqUtil.getUserId();
			ObjectId roomObjId=new ObjectId(roomId);
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			}
			int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomObjId,userId);
			if(Room_Role.CREATOR!=role)
				return JSONMessage.failure("权限不足!");
			SKBeanUtils.getRoomManagerImplForIM().delete(roomObjId,userId);
			return JSONMessage.success();
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}

	/**
	 * 更新房间
	 * 
	 * @param roomId
	 * @param roomName
	 * @param notice
	 * @param desc
	 * @return
	 */
	@RequestMapping("/update")
	public JSONMessage update(@RequestParam String roomId,
			@ModelAttribute RoomVO roomVO) {
		try {
			int userId=ReqUtil.getUserId();
			ObjectId roomObjId=new ObjectId(roomId);
			if(StringUtil.isEmpty(roomId))
				throw new ServiceException("参数有误");
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			}
			int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomObjId,userId);
			if(0== role ||Room_Role.MEMBER== role)
				return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
			
			User user = SKBeanUtils.getUserManager().getUser(userId);
			roomVO.setRoomId(new ObjectId(roomId));
			return SKBeanUtils.getRoomManagerImplForIM().update(user, roomVO,1,0);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}

	/**
	 * 根据房间Id获取群组
	 * 包括 成员 列表 
	 * 公告列表
	 * @param roomId
	 * @return
	 */
	@RequestMapping("/get")
	public JSONMessage get(@RequestParam(defaultValue = "") String roomId,@RequestParam(defaultValue="0") Integer pageIndex,@RequestParam(defaultValue="500") Integer pageSize) {
		try {

			Room data = SKBeanUtils.getRoomManagerImplForIM().get(new ObjectId(roomId),ReqUtil.getUserId(),pageIndex,pageSize);
			if(null==data)
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			data.setMember(SKBeanUtils.getRoomManagerImplForIM().getMember(new ObjectId(roomId), ReqUtil.getUserId()));
			return JSONMessage.success(data);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	* @Description: TODO(只获取群组属性和群主管理员信息， 不包括 成员列表 和公告列表)
	* @param @param roomId
	* @param @return    参数
	 */
	@RequestMapping("/getRoom")
	public JSONMessage getRoom(@RequestParam String roomId) {
		try {
			ObjectId roomObjId=new ObjectId(roomId);
			Room data = SKBeanUtils.getRoomManagerImplForIM().getRoom(roomObjId);
			if(null==data)
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			data.setMember(SKBeanUtils.getRoomManagerImplForIM().getMember(roomObjId, ReqUtil.getUserId()));
			return JSONMessage.success(data);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	
	}
	
	@RequestMapping("/transfer")
	public JSONMessage transfer(@RequestParam String roomId,@RequestParam(defaultValue="0") Integer toUserId) {
		if(0==toUserId)
			return JSONMessage.failure("请指定 新群主!");
		
		Room room = SKBeanUtils.getRoomManagerImplForIM().getRoom(new ObjectId(roomId));
		if(null==room)
			return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
		if(room.getS() == -1)
			return JSONMessage.failure("该群组已被后台锁定");
		else if(toUserId.equals(room.getUserId()))
			return JSONMessage.failure("不能转让群组给自己");
		else if(!ReqUtil.getUserId().equals(room.getUserId()))
			return JSONMessage.failure("你不是群主 不能申请转让群");
		else if (null==SKBeanUtils.getRoomManagerImplForIM().getMember(new ObjectId(roomId), toUserId)) {
			return JSONMessage.failure("对方不是群成员 不能转让");
		}
		
		SKBeanUtils.getRoomManagerImplForIM().transfer(room, toUserId);
		
		return JSONMessage.success();
	}
	
	/**
	 * 获取房间列表（按创建时间排序）
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@RequestMapping("/list")
	public JSONMessage list(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "") String roomName) {
		Object data =null;
		if(0==SKBeanUtils.getLocalSpringBeanManager().getAppConfig().getIsBeta())
			data=SKBeanUtils.getRoomManagerImplForIM().selectList(pageIndex, pageSize, roomName);
		
		return JSONMessage.success(null, data);
	}

	@RequestMapping("/member/update")
	public JSONMessage updateMember(@RequestParam String roomId, @ModelAttribute Member member, @RequestParam(defaultValue = "") String text) {
		try {
			List<Integer> idList = StringUtil.isEmpty(text) ? null : JSON.parseArray(text, Integer.class);
			User user = SKBeanUtils.getUserManager().getUser(ReqUtil.getUserId());
			ObjectId roomObjId=new ObjectId(roomId);
			if(StringUtil.isEmpty(text)&&null==member.getUserId()) {
				JSONMessage.failureByErrCode(ResultCode.ParamsLack, ConstantUtil.defLanguage);
			}else if(StringUtil.isEmpty(text)&&
					(null==member.getTalkTime()&&StringUtil.isEmpty(member.getRemarkName())
							&&0==member.getRole()&&StringUtil.isEmpty(member.getNickname())
					)) {
				JSONMessage.failureByErrCode(ResultCode.ParamsLack, ConstantUtil.defLanguage);
			}

			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);
			}else{
				if(-1 == room.getS())
					return JSONMessage.failure("该群组已被后台锁定!");
				if (null == idList){
					Member optMember = SKBeanUtils.getRoomManager().getMember(roomObjId, user.getUserId());
					int role= SKBeanUtils.getRoomManager().findMemberAndRole(roomObjId, member.getUserId());
					if(null==optMember)
						return JSONMessage.failure("不是群成员 权限不足!");
					else if(1!=optMember.getRole()&&role==Room_Role.ADMIN)
						return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
					else if(!StringUtil.isEmpty(member.getNickname())&&!optMember.getUserId().equals(member.getUserId()))
						return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
					if(null!=member.getTalkTime()) {
						if(role==Room_Role.CREATOR)
							return JSONMessage.failure("不能禁言群主");
						else if(optMember.getRole()==Room_Role.MEMBER)
							return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
					}

					SKBeanUtils.getRoomManagerImplForIM().updateMember(user, roomObjId, member);
				}else{
					if(1==room.getIsNeedVerify()&&!user.getUserId().equals(room.getUserId()))
						return JSONMessage.failure("邀请群成员 需要群主同意!");
					if(!user.getUserId().equals(room.getUserId())&&1==idList.size()){
						if(null==SKBeanUtils.getFriendsManager().getFriends(user.getUserId(), idList.get(0)))
							return JSONMessage.failure("不是好友不能邀请!");
					}
					SKBeanUtils.getRoomManagerImplForIM().updateMember(user,roomObjId, idList);
				}
			}
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
		return JSONMessage.success();
	}
	
	//退出群组
	@RequestMapping("/member/delete")
	public JSONMessage deleteMember(@RequestParam String roomId, @RequestParam int userId) {
		try {
			User user = SKBeanUtils.getUserManager().getUser(ReqUtil.getUserId());
			ObjectId roomObjId=new ObjectId(roomId);
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			}
			int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomObjId,ReqUtil.getUserId());
			int toRole = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomObjId,userId);
			if(0==role)
				return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
			else if(userId!=user.getUserId()) {
				if(Room_Role.MEMBER==role)
					return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
				if(Room_Role.ADMIN==role&&Room_Role.ADMIN==toRole)
					return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
			}
			SKBeanUtils.getRoomManagerImplForIM().deleteMember(user,roomObjId, userId);
			return JSONMessage.success();
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/** @Description:群消息的置顶和消息免打扰
	* @param offlineNoPushMsg 0:关闭,1:开启
	* @param roomId
	* @param userId
	* @param type = 0  消息免打扰 ,type = 1 聊天置顶
	* @return
	**/ 
	@RequestMapping("/member/setOfflineNoPushMsg")
	public JSONMessage setOfflineNoPushMsg(@RequestParam(defaultValue="0") int offlineNoPushMsg,@RequestParam String roomId,@RequestParam int userId,@RequestParam(defaultValue="0") int type){
		ObjectId roomObjId=new ObjectId(roomId);
		Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
		if(null == room){
			return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
		}
		Integer offlineUserId = (0 != userId ? userId : ReqUtil.getUserId());
		SKBeanUtils.getRoomManagerImplForIM().Memberset(offlineNoPushMsg, roomObjId, offlineUserId,type);
		return JSONMessage.success();
	}
	
	@RequestMapping("/member/get")	
	public JSONMessage getMember(@RequestParam String roomId, @RequestParam(defaultValue="0") int userId) {
		if(0==userId)
			userId=ReqUtil.getUserId();
		ObjectId roomObjId=new ObjectId(roomId);
		Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
		if(null == room){
			return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
		}
		Member data = SKBeanUtils.getRoomManagerImplForIM().getMember(roomObjId, userId);
		if(data==null)
			return JSONMessage.failure("用户："+userId+"没在当前群组");
		if(StringUtil.isEmpty(data.getCall()))
			data.setCall(SKBeanUtils.getRoomManagerImplForIM().getCall(roomObjId));
		if(StringUtil.isEmpty(data.getVideoMeetingNo()))
			data.setVideoMeetingNo(SKBeanUtils.getRoomManagerImplForIM().getVideoMeetingNo(roomObjId));
		return JSONMessage.success(null, data);
	}

	@RequestMapping("/member/list")
	public JSONMessage getMemberList(@RequestParam String roomId,@RequestParam(defaultValue="") String keyword) {
		Object data=null;
		try {
			ObjectId roomObjId=new ObjectId(roomId);
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			}
			int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomObjId,ReqUtil.getUserId());
			if(0==role)
				return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
			 data = SKBeanUtils.getRoomManagerImplForIM().getMemberList(roomObjId,ReqUtil.getUserId(),keyword);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
		
		return JSONMessage.success(null, data);
	}
	
	/** @Description: 群成员列表分页
	* @param roomId
	* @param joinTime
	* @param pageIndex
	* @param pageSize
	* @return
	**/ 
	@RequestMapping("/member/getMemberListByPage")
	public JSONMessage getMemberListByPage(@RequestParam(defaultValue="") String roomId,@RequestParam(defaultValue="0") long joinTime,@RequestParam(defaultValue="") Integer pageIndex,@RequestParam(defaultValue="100") Integer pageSize) {
		try {
			ObjectId roomObjId=new ObjectId(roomId);
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);
			}
			int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomObjId,ReqUtil.getUserId());
			if(0==role)
				return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
			List<Member> memberListByPage = SKBeanUtils.getRoomManagerImplForIM().getMemberListByPageImpl(roomObjId,joinTime,pageSize);
			return JSONMessage.success(memberListByPage);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	@RequestMapping("/notice/list")
	public JSONMessage getNoticeList(@RequestParam String roomId,@RequestParam(defaultValue="") String keyword) {
		Object data = SKBeanUtils.getRoomManagerImplForIM().getNoticeList(new ObjectId(roomId));
		return JSONMessage.success(null, data);
	}
	
	//获取房间
	@RequestMapping("/get/call")
	public JSONMessage getRoomCall(@RequestParam String roomId){
		Object data=SKBeanUtils.getRoomManagerImplForIM().getCall(new ObjectId(roomId));
		return JSONMessage.success(null, data);
		
	}
	
	@RequestMapping("/join")
	public JSONMessage join(@RequestParam String roomId, @RequestParam(defaultValue = "2") int type) {
		try {
			ObjectId roomObjId=new ObjectId(roomId);
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			}
			SKBeanUtils.getRoomManagerImplForIM().join(ReqUtil.getUserId(), new ObjectId(roomId), type);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
		return JSONMessage.success();
	}

	@RequestMapping("/list/his")
	public JSONMessage historyList(@RequestParam(defaultValue = "0") int type,
			@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
		// Object data = SKBeanUtils.getRoomManagerImplForIM().selectHistoryList(ReqUtil.getUserId(),
		// type);
		Object data = SKBeanUtils.getRoomManagerImplForIM().selectHistoryList(ReqUtil.getUserId(), type, pageIndex, pageSize);
		return JSONMessage.success(null, data);
	}
	
	//设置/取消管理员
	@RequestMapping("/set/admin")
	public JSONMessage setAdmin(@RequestParam String roomId,@RequestParam int touserId,@RequestParam int type){
		try {
			ObjectId roomObjId=new ObjectId(roomId);
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			}
			int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomObjId,ReqUtil.getUserId());
			if(Room_Role.CREATOR!=role)
				return JSONMessage.failure("只有群主才可以设置或取消管理员");
			if(touserId == ReqUtil.getUserId())
				return JSONMessage.failure("群主不能设置为管理员");
			SKBeanUtils.getRoomManagerImplForIM().setAdmin(roomObjId, touserId,type,ReqUtil.getUserId());
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
		return JSONMessage.success();
	}
	
	/** @Description:（设置/取消 隐身人、监护人（其他人完全看不到他；隐身人和监控人的区别是，前者不可以说话，后者能说话）） 
	* @param roomId 房间id
	* @param touserId  被指定人
 	* @param type 4:设置隐身人  -1:取消隐身人，5：设置监控人，0：取消监控人
	* @return
	**/ 
	@RequestMapping("/setInvisibleGuardian")
	public JSONMessage setInvisibleGuardian(@RequestParam String roomId,@RequestParam int touserId,@RequestParam int type){
		try {
			ObjectId roomObjId=new ObjectId(roomId);
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			}
			if(SKBeanUtils.getRoomManagerImplForIM().getMember(roomObjId, ReqUtil.getUserId()).getRole()!=1)
				return JSONMessage.failure("只有群主才有操作权限");
			Member member = SKBeanUtils.getRoomManagerImplForIM().getMember(roomObjId,touserId); 
			if(null == member)
				return JSONMessage.failure("该成员不在群组中");
			int role = member.getRole();// 成员角色：1=创建者、2=管理员、3=普通成员、4=隐身人、5=监控人
			if((4 == type && role != 3)||(5 == type && role !=3)){
				return JSONMessage.failure("该成员已经是："+(1 == role ? "群主":2 == role ? "管理员" : 4 == role ? "隐身人" : "监控人"));
			}
			if(-1 == type && role != 4){
				return JSONMessage.failure("该成员不是隐身人");
			}
			if(0 == type && role != 5){
				return JSONMessage.failure("该成员不是监控人");
			}
			SKBeanUtils.getRoomManagerImplForIM().setInvisibleGuardian(new ObjectId(roomId), touserId,type,ReqUtil.getUserId());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return JSONMessage.success();
	}
	
	//添加（群共享）
	@RequestMapping("/add/share")
	public JSONMessage Addshare(@RequestParam ObjectId roomId,@RequestParam int type,@RequestParam long size,@RequestParam int userId
			,@RequestParam String url,@RequestParam String name){
		try {
			Room room = SKBeanUtils.getRoomManager().getRoom(roomId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			}
			int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomId,ReqUtil.getUserId());
			if(0==role)
				return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
			
			Object data=SKBeanUtils.getRoomManagerImplForIM().Addshare(roomId,size,type ,userId, url,name);
			return JSONMessage.success(data);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	
	//查询(群共享)
	@RequestMapping("/share/find")
	public JSONMessage findShare(@RequestParam ObjectId roomId,@RequestParam(defaultValue="0") int pageIndex,@RequestParam(defaultValue="0") long time
			,@RequestParam(defaultValue="0") int userId,@RequestParam(defaultValue="10") int pageSize){
		Room room = SKBeanUtils.getRoomManager().getRoom(roomId);
		if(null == room){
			return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
		}
		int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomId,ReqUtil.getUserId());
		if(0==role)
			return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
		Object data=SKBeanUtils.getRoomManagerImplForIM().findShare(roomId, time, userId, pageIndex, pageSize);
		return JSONMessage.success(null, data);
	}
	
	@RequestMapping("/share/get")
	public JSONMessage getShare(@RequestParam ObjectId roomId,@RequestParam ObjectId shareId){
		Room room = SKBeanUtils.getRoomManager().getRoom(roomId);
		if(null == room){
			return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
		}
		int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomId,ReqUtil.getUserId());
		if(0==role)
			return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
		Object data=SKBeanUtils.getRoomManagerImplForIM().getShare(roomId, shareId);
		return JSONMessage.success(null, data);
	}
	
	//删除
	@RequestMapping("/share/delete")
	public JSONMessage deleteShare(@RequestParam String roomId,@RequestParam String shareId,@RequestParam int userId){
		ObjectId roomObjId=new ObjectId(roomId);
		
		Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
		if(null == room){
			return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
		}
		int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomObjId,ReqUtil.getUserId());
		if(0==role||Room_Role.MEMBER==role)
			return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
		SKBeanUtils.getRoomManagerImplForIM().deleteShare(new ObjectId(roomId),new ObjectId(shareId),userId);
		return JSONMessage.success();
	}
	
	
	//删除群公告
	@RequestMapping("/notice/delete")
	public JSONMessage deleteNotice(@RequestParam(defaultValue = "") String roomId,
			@RequestParam(defaultValue = "") String noticeId) {
		ObjectId roomObjId=new ObjectId(roomId);
	
		try {
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			}
			if (StringUtil.isEmpty(roomId) || StringUtil.isEmpty(noticeId)) {
				return JSONMessage.failure("非法参数错误 null ");
			}
			int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomObjId,ReqUtil.getUserId());
			if(0==role||Room_Role.MEMBER==role)
				return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
			
			SKBeanUtils.getRoomManagerImplForIM().deleteNotice(roomObjId, new ObjectId(noticeId));
			return JSONMessage.success();
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}

	}
	
	/**
	 * 发送群消息
	 * @param jid
	 * @return
	 */
	@RequestMapping("/sendMsg")
	public JSONMessage sendMsg (@RequestParam(defaultValue="") String jidArr,@RequestParam(defaultValue="1") int userId,
			@RequestParam(defaultValue="1") int type,@RequestParam(defaultValue="")String content){
		String[] split = jidArr.split(",");
		
		SKBeanUtils.getRoomManagerImplForIM().sendMsgToRooms(split, userId, type, content);
		
		return JSONMessage.success();
		
	}
	
	/** @Description:（公告列表） 
	* @param roomId
	* @param pageIndex
	* @param pageSize
	* @return
	**/ 
	@RequestMapping("/noticesPage")
	public JSONMessage noticesPage(@RequestParam(defaultValue="") String roomId,@RequestParam(defaultValue = "0") int pageIndex,@RequestParam(defaultValue = "10") int pageSize){
		try {
			ObjectId roomObjId=new ObjectId(roomId);
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			}
			PageVO noticeList = SKBeanUtils.getRoomManagerImplForIM().getNoticeList(roomObjId, pageIndex, pageSize);
			return JSONMessage.success(null, noticeList);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/** @Description:修改群公告
	* @param roomId
	* @param noticeId
	* @param noticeContent
	* @return
	**/ 
	@RequestMapping("/updateNotice")
	public JSONMessage updateNotice(@RequestParam(defaultValue="") String roomId,@RequestParam(defaultValue="") String noticeId,@RequestParam(defaultValue="") String noticeContent){
		try {
			Integer userId = ReqUtil.getUserId();
			ObjectId roomObjId=new ObjectId(roomId);
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				return JSONMessage.failure(ResultMsgs.ROOM_NOT_EXIST);	
			}
			if(StringUtil.isEmpty(noticeContent) || StringUtil.isEmpty(roomId) || StringUtil.isEmpty(noticeId))
				return JSONMessage.failure("参数有误");
			int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomObjId,ReqUtil.getUserId());
			if(0==role||Room_Role.MEMBER==role)
				return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
			Notice updateNotice = SKBeanUtils.getRoomManagerImplForIM().updateNotice(roomObjId, new ObjectId(noticeId), noticeContent,userId);
			return JSONMessage.success(updateNotice);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	
	@RequestMapping("/location/query")
	public JSONMessage queryLocationRoom(String name,double longitude,double latitude,String password,int isQuery){
		try {
			Room room = SKBeanUtils.getRoomManager().queryLocationRoom(name, longitude, latitude, password,isQuery);
			return JSONMessage.success(null,room);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	@RequestMapping("/location/join")
	public JSONMessage joinLocationRoom(String jid){
		try {
			Room room =SKBeanUtils.getRoomManager().joinLocationRoom(jid);
			return JSONMessage.success(room);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	@RequestMapping("/location/exit")
	public JSONMessage exitLocationRoom(String jid){
		try {
			SKBeanUtils.getRoomManager().exitLocationRoom(jid);
			return JSONMessage.success();
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 添加群助手
	 * @param groupHelper
	 * @return
	 */
	@RequestMapping("/addGroupHelper")
	public JSONMessage addGroupHelper(@RequestParam String helperId,@RequestParam String roomId,@RequestParam String roomJid){
		try {
			Integer userId = ReqUtil.getUserId();
			JSONMessage data = SKBeanUtils.getRoomManager().addGroupHelper(helperId,roomId,roomJid,userId);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 添加自动回复关键字
	 * @param roomId
	 * @param keyword
	 * @param value
	 * @return
	 */
	@RequestMapping("/addAutoResponse")
	public JSONMessage addAutoResponse(@RequestParam(defaultValue="") String helperId,@RequestParam(defaultValue="") String roomId,@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="") String value){
		try {
			JSONMessage data = SKBeanUtils.getRoomManager().addAutoResponse(roomId, helperId,keyword, value);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 更新自动回复关键字及回复
	 * @param keyWordId
	 * @param keyword
	 * @param value
	 * @return
	 */
	@RequestMapping("/updateAutoResponse")
	public JSONMessage updateKeyWord(@RequestParam(defaultValue="") String groupHelperId,@RequestParam(defaultValue="") String keyWordId,@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="") String value){
		try {
			JSONMessage data = SKBeanUtils.getRoomManager().updateKeyword(groupHelperId,keyWordId, keyword, value);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 删除自动回复关键字
	 * @param groupHelperId
	 * @param keyWordId
	 * @return
	 */
	@RequestMapping("/deleteAutoResponse")
	public JSONMessage deleteAutoResponse(@RequestParam(defaultValue="") String groupHelperId,@RequestParam(defaultValue="") String keyWordId){
		try {
			JSONMessage data = SKBeanUtils.getRoomManager().deleteAutoResponse(ReqUtil.getUserId(),groupHelperId, keyWordId);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 删除群助手
	 * @param id
	 * @return
	 */
	@RequestMapping("/deleteGroupHelper")
	public JSONMessage deleteGroupHelper(@RequestParam(defaultValue="") String groupHelperId){
		try {
			
			SKBeanUtils.getRoomManager().deleteGroupHelper(ReqUtil.getUserId(),groupHelperId);
			return JSONMessage.success();
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
	}
	
	/**
	 * 查询房间群助手
	 * @param roomId
	 * @return
	 */
	@RequestMapping("/queryGroupHelper")
	public JSONMessage queryGroupHelper(@RequestParam(defaultValue="") String roomId,@RequestParam(defaultValue="") String helperId){
		try {
			Object data = SKBeanUtils.getRoomManager().queryGroupHelper(roomId,helperId);
			return JSONMessage.success(null, data);
		} catch (Exception e) {
			e.printStackTrace();
			return JSONMessage.failure(e.getMessage());
		}
		
	}
	
	/** @Description:群组复制 
	* @param roomId
	* @return
	**/ 
	@RequestMapping("/copyRoom")
	public JSONMessage copyRoom(@RequestParam(defaultValue="") String roomId){
		try {
			Integer userId = ReqUtil.getUserId();
			
			ObjectId roomObjId=new ObjectId(roomId);
			int role = SKBeanUtils.getRoomManagerImplForIM().findMemberAndRole(roomObjId,ReqUtil.getUserId());
			if(Room_Role.CREATOR!=role)
				return JSONMessage.failure(ResultMsgs.PERMISSION_ERROR);
			User user = SKBeanUtils.getUserManager().getUser(userId);
			Room copyRoom = SKBeanUtils.getRoomManager().copyRoom(user, roomId);
			return JSONMessage.success(copyRoom);
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}

	@RequestMapping("/member/talkStatus")
	public JSONObject talkStatus(@RequestParam String roomId) {
		JSONObject jsonObject=new JSONObject();
		try {

			User user = SKBeanUtils.getUserManager().getUser(ReqUtil.getUserId());
			ObjectId roomObjId=new ObjectId(roomId);
			Room room = SKBeanUtils.getRoomManager().getRoom(roomObjId);
			if(null == room){
				jsonObject.put("data",false);
				jsonObject.put("msg",ResultMsgs.ROOM_NOT_EXIST);
			}else{
				if(-1 == room.getS()) {
					jsonObject.put("data",false);
					jsonObject.put("msg","该群组已被后台锁定!");
				}
				Member roomMember=SKBeanUtils.getRoomManagerImplForIM().getMember(roomObjId,user.getUserId());
				log.info("用户信息, roomMember: {}",roomMember);
				if(roomMember.getTalkTime()>-2){
					jsonObject.put("data",true);
				}else{
					jsonObject.put("data",false);
				}
			}
			log.info("用户信息, jsonObject: {}",jsonObject);
			return jsonObject;
		} catch (ServiceException e) {
			e.printStackTrace();
			jsonObject.put("data",false);
			jsonObject.put("msg",e.getMessage());
			return jsonObject;
		}
	}
}
	
