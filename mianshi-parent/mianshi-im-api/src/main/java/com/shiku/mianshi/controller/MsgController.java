package com.shiku.mianshi.controller;

import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import cn.xyz.commons.constants.KConstants.Result;
import cn.xyz.commons.constants.KConstants.ResultMsgs;
import cn.xyz.commons.constants.MsgType;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.model.AddCommentParam;
import cn.xyz.mianshi.model.AddGiftParam;
import cn.xyz.mianshi.model.AddMsgParam;
import cn.xyz.mianshi.model.MessageExample;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Msg;

/**
 * 商务圈接口
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/b/circle/msg")
public class MsgController extends AbstractController {

	private static Logger logger = LoggerFactory.getLogger(MsgController.class);

	@RequestMapping(value = "/hot")
	public JSONMessage getHostMsgList(@RequestParam(defaultValue = "0") int cityId,
			@RequestParam(defaultValue = "0") Integer pageIndex) {
		Object data = SKBeanUtils.getMsgListRepository().getHotList(cityId, pageIndex, 0);
		return JSONMessage.success(null, data);
	}

	@RequestMapping(value = "/latest")
	public JSONMessage getLatestMsgList(@RequestParam(defaultValue = "0") int cityId,
			@RequestParam(defaultValue = "0") Integer pageIndex) {
		Object data = SKBeanUtils.getMsgListRepository().getLatestList(cityId, pageIndex, 0);
		return JSONMessage.success(null, data);
	}
	//评论
	@RequestMapping(value = "/comment/add")
	public JSONMessage addComment(@ModelAttribute AddCommentParam param) {
		JSONMessage jMessage;

		if (StringUtil.isEmpty(param.getMessageId())) {
			jMessage = Result.ParamsAuthFail;
		} else {
			try {
				if(StringUtil.isEmpty(param.getBody()))
					return JSONMessage.failure("评论内容不能为空");
				Msg msg = SKBeanUtils.getMsgRepository().get(0,parse(param.getMessageId()));
				if(null==msg){
					return JSONMessage.failure("内容不存 或已被删除!");
				}else if(1 == msg.getIsAllowComment())
					return JSONMessage.failure("该朋友圈禁止评论");
				ObjectId data = SKBeanUtils.getMsgCommentRepository().add(ReqUtil.getUserId(), param);
				jMessage = null == data ? JSONMessage.failure(null) : JSONMessage.success(null, data);
			} catch (Exception e) {
				logger.error("评论失败", e);
				jMessage = JSONMessage.error(e);
			}
		}

		return jMessage;
	}

	@RequestMapping(value = "/gift/add")
	public JSONMessage addGift(@RequestParam String messageId, @RequestParam String gifts) {
		JSONMessage jMessage;

		if (StringUtil.isEmpty(messageId) || StringUtil.isEmpty(gifts)) {
			jMessage = Result.ParamsAuthFail;
		} else {
			try {
				Object data = SKBeanUtils.getMsgGiftRepository().add(ReqUtil.getUserId(), new ObjectId(messageId),
						JSON.parseArray(gifts, AddGiftParam.class));
				jMessage = null == data ? JSONMessage.failure(null) : JSONMessage.success(null, data);
			} catch (Exception e) {
				logger.error("送礼物失败", e);
				jMessage = JSONMessage.error(e);
			}
		}

		return jMessage;
	}

	@RequestMapping(value = "/add")
	public JSONMessage addMsg(@ModelAttribute AddMsgParam param) {
		JSONMessage jMessage = Result.ParamsAuthFail;

		if (0 == param.getType() || 0 == param.getFlag() || 0 == param.getVisible()) {
		} else if (MsgType.TYPE_TEXT == param.getType() && StringUtil.isEmpty(param.getText())) {
		} else if (MsgType.TYPE_IMAGE == param.getType() && StringUtil.isEmpty(param.getImages())) {
		} else if (MsgType.TYPE_VOICE == param.getType() && StringUtil.isEmpty(param.getAudios())) {
		} else if (MsgType.TYPE_FILE == param.getType() && StringUtil.isEmpty(param.getFiles())) {
		} else if (MsgType.TYPE_SHARE_LINK == param.getType() && StringUtil.isEmpty(param.getSdkUrl())){
		} else {
			try {
//				ObjectId data = SKBeanUtils.getMsgRepository().add(ReqUtil.getUserId(), param);
				Object data = SKBeanUtils.getMsgRepository().add(ReqUtil.getUserId(), param);
				jMessage = JSONMessage.success(null, data);
			} catch (Exception e) {
				logger.error("发商务圈消息失败", e);

				jMessage = JSONMessage.error(e);
			}
		}

		return jMessage;
	}
	
	@RequestMapping(value = "/praise/add")
	public JSONMessage addPraise(@RequestParam String messageId) {
		JSONMessage jMessage;

		if (StringUtil.isEmpty(messageId)) {
			jMessage = Result.ParamsAuthFail;
		} else {
			try {
				ObjectId data = SKBeanUtils.getMsgPraiseRepository().add(ReqUtil.getUserId(), new ObjectId(messageId));
				jMessage = null == data ? JSONMessage.failure(null) : JSONMessage.success(null, data);
			} catch (Exception e) {
				logger.error("赞失败", e);

				jMessage = JSONMessage.error(e);
			}
		}

		return jMessage;
	}

	@RequestMapping(value = "/comment/delete")
	public JSONMessage deleteComment(@RequestParam String messageId, String commentId) {
		JSONMessage jMessage;
		ObjectId objectId;
		if (StringUtil.isEmpty(messageId) || StringUtil.isEmpty(commentId)) {
			return Result.ParamsAuthFail;
		} 
		try {
			objectId = new ObjectId(messageId);
		} catch (Exception e) {
			return Result.ParamsAuthFail;
		}
		 
		try {
			Object data = SKBeanUtils.getMsgRepository().get(ReqUtil.getUserId(),objectId);
			if(null != data){
				boolean ok = SKBeanUtils.getMsgCommentRepository().delete(objectId,commentId);
				jMessage = ok ? JSONMessage.success() : JSONMessage.failure(null);
			}else {
				jMessage = JSONMessage.failure(null);
			}
			
		} catch (Exception e) {
			logger.error("删除评论失败", e);
			jMessage = JSONMessage.error(e);
		}
		

		return jMessage;
	}

	@RequestMapping(value = "/delete")
	public JSONMessage deleteMsg(@RequestParam String messageId) {
		JSONMessage jMessage;
		ObjectId objectId;
		if (StringUtil.isEmpty(messageId)) {
			return Result.ParamsAuthFail;
		}
		try {
			try {
				objectId = new ObjectId(messageId);
			} catch (Exception e) {
				return Result.ParamsAuthFail;
			}
			Object data = SKBeanUtils.getMsgRepository().get(ReqUtil.getUserId(),objectId);
			if(null==data) {
				JSONMessage.failure(ResultMsgs.DATA_NOT_EXIST);
			}
			boolean ok = SKBeanUtils.getMsgRepository().delete(messageId);
				jMessage = ok ? JSONMessage.success() : JSONMessage.failure(null);
		} catch (Exception e) {
			logger.error("删除商务圈消息失败", e);
			jMessage = JSONMessage.error(e);
		}
		
		return jMessage;
	}

	@RequestMapping(value = "/praise/delete")
	public JSONMessage deletePraise(@RequestParam String messageId) {
		JSONMessage jMessage;
		ObjectId objectId =null;
		if (StringUtil.isEmpty(messageId)) {
			jMessage = Result.ParamsAuthFail;
		} else {
			try {
				objectId = new ObjectId(messageId);
			} catch (Exception e) {
				return Result.ParamsAuthFail;
			} 
			boolean ok = SKBeanUtils.getMsgPraiseRepository().delete(ReqUtil.getUserId(),objectId);

			jMessage = ok ? JSONMessage.success() : JSONMessage.failure(null);
		}

		return jMessage;
	}
	
	/** @Description:（取消收藏） 
	* @param messageId
	* @return
	**/ 
	@RequestMapping(value = "/deleteCollect")
	public JSONMessage deleteCollect(@RequestParam(defaultValue="") String messageId) {
		try {
			if(StringUtil.isEmpty(messageId))
				return JSONMessage.failure("参数为空");
			
			SKBeanUtils.getMsgPraiseRepository().deleteCollect(ReqUtil.getUserId(), messageId);
			return JSONMessage.success();
		} catch (ServiceException e) {
			return JSONMessage.failure(e.getMessage());
		}
	}

	@RequestMapping(value = "/forwarding")
	public JSONMessage forwardingMsg(@ModelAttribute AddMsgParam param) {
		JSONMessage jMessage;

		if (StringUtil.isEmpty(param.getText()) || StringUtil.isEmpty(param.getMessageId())) {
			jMessage = Result.ParamsAuthFail;
		} else {
			try {
				Object data = SKBeanUtils.getMsgRepository().forwarding(ReqUtil.getUserId(), param);
				jMessage = null == data ? JSONMessage.failure(null) : JSONMessage.success(null, data);
			} catch (Exception e) {
				logger.error("转发商务圈消息失败", e);

				jMessage = JSONMessage.error(e);
			}
		}

		return jMessage;
	}

	@RequestMapping(value = "/comment/list")
	public JSONMessage getCommentList(@RequestParam String messageId,
			@RequestParam(value = "commentId", defaultValue = "") String commentId,
			@RequestParam(value = "pageIndex", defaultValue = "0") int pageIndex,
			@RequestParam(value = "pageSize", defaultValue = "100") int pageSize) {
		JSONMessage jMessage = null;
		ObjectId objectId=null;
		ObjectId commentObjId=null;
		try {
			 objectId = new ObjectId(messageId);
			 if(!StringUtil.isEmpty(commentId))
				 commentObjId=new ObjectId(commentId);
		} catch (Exception e) {
			return Result.ParamsAuthFail;
		} 
		try {
			Object data = SKBeanUtils.getMsgCommentRepository().find(objectId,
					commentObjId, pageIndex, pageSize);
			jMessage = JSONMessage.success(null, data);
		} catch (Exception e) {
			logger.error("获取评论列表失败", e);
			jMessage = JSONMessage.error(e);
		}

		return jMessage;
	}

	@RequestMapping(value = "/ids")
	public JSONMessage getFriendsMsgIdList(@RequestParam(value = "userId", defaultValue = "") Integer userId,
			@RequestParam(defaultValue = "") String messageId,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		JSONMessage jMessage;

		try {
			ObjectId msgId = !ObjectId.isValid(messageId) ? null : new ObjectId(messageId);
			Object data = SKBeanUtils.getMsgRepository().getMsgIdList(null == userId ? ReqUtil.getUserId() : userId, 0, msgId, pageSize);
			jMessage = JSONMessage.success(null, data);
		} catch (Exception e) {
			logger.error("获取当前登录用户及其所关注用户的最新商务圈消息Id失败", e);

			jMessage = JSONMessage.error(e);
		}

		return jMessage;
	}

	@RequestMapping(value = "/list")
	public JSONMessage getFriendsMsgList(@RequestParam(value = "userId", defaultValue = "") Integer userId,
			@RequestParam(value = "messageId", defaultValue = "") String messageId,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
			@RequestParam(value = "pageIndex", defaultValue = "0") Integer pageIndex) {
		JSONMessage jMessage;
		try {
			Object data = SKBeanUtils.getMsgRepository().getMsgList(ReqUtil.getUserId(), null == userId ? ReqUtil.getUserId() : userId,
					!ObjectId.isValid(messageId) ? null : new ObjectId(messageId), pageSize,pageIndex);
			jMessage = JSONMessage.success(null, data);
		} catch (Exception e) {
			logger.error("获取朋友圈列表失败", e);
			jMessage = JSONMessage.error(e);
		}
		return jMessage;
	}
	
	@RequestMapping(value = "/pureVideo")
	public JSONMessage getPureVideoMsgList(@RequestParam(defaultValue = "10") Integer pageSize,
			@RequestParam(defaultValue = "0") Integer pageIndex,@RequestParam(defaultValue = "") String lable){
		try {
			List<Msg> data = SKBeanUtils.getMsgRepository().getPureVideo(pageIndex,pageSize,lable);
			data.forEach(msg -> {
			msg.setComments(SKBeanUtils.getMsgCommentRepository().find(msg.getMsgId(), null, 0, 10));
			msg.setPraises(SKBeanUtils.getMsgPraiseRepository().find(msg.getMsgId(), null, 0, 10));
			msg.setGifts(SKBeanUtils.getMsgGiftRepository().find(msg.getMsgId(), null, 0, 10));
			msg.setIsPraise(SKBeanUtils.getMsgPraiseRepository().exists(ReqUtil.getUserId(), msg.getMsgId()) ? 1 : 0);
			msg.setIsCollect(SKBeanUtils.getMsgPraiseRepository().existsCollect(ReqUtil.getUserId(), msg.getMsgId()) ? 1 : 0);
		});
			return JSONMessage.success(data);
		} catch (Exception e) {
			return JSONMessage.failure(e.getMessage());
		}
	}

	@RequestMapping(value = "/gift/gbgift")
	public JSONMessage getGiftGroupByGfit(@RequestParam String messageId) {
		JSONMessage jMessage;

		try {
			Object data = SKBeanUtils.getMsgGiftRepository().findByGift(new ObjectId(messageId));

			jMessage = JSONMessage.success(null, data);
		} catch (Exception e) {
			logger.error("获取礼物列表失败", e);
			jMessage = JSONMessage.error(e);
		}

		return jMessage;
	}

	@RequestMapping(value = "/gift/gbuser")
	public JSONMessage getGiftGroupByUser(@RequestParam String messageId) {
		JSONMessage jMessage;

		try {
			Object data = SKBeanUtils.getMsgGiftRepository().findByUser(new ObjectId(messageId));

			jMessage = JSONMessage.success(null, data);
		} catch (Exception e) {
			logger.error("获取礼物列表失败", e);
			jMessage = JSONMessage.error(e);
		}

		return jMessage;
	}

	@RequestMapping(value = "/gift/list")
	public JSONMessage getGiftList(@RequestParam String messageId,
			@RequestParam(value = "giftId", defaultValue = "") String giftId,
			@RequestParam(value = "pageIndex", defaultValue = "0") Integer pageIndex,
			@RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
		JSONMessage jMessage;

		try {
			Object data = SKBeanUtils.getMsgGiftRepository().find(new ObjectId(messageId), new ObjectId(giftId), pageIndex, pageSize);

			jMessage = JSONMessage.success(null, data);
		} catch (Exception e) {
			logger.error("获取礼物列表失败", e);
			jMessage = JSONMessage.error(e);
		}

		return jMessage;
	}
	//获取单条商务圈
	@RequestMapping(value = "/get")
	public JSONMessage getMsgById(@RequestParam String messageId) {
		JSONMessage jMessage;

		if (StringUtil.isEmpty(messageId)||!ObjectId.isValid(messageId))
			jMessage = Result.ParamsAuthFail;
		else
			try {
				Object data = SKBeanUtils.getMsgRepository().get(ReqUtil.getUserId(), new ObjectId(messageId));
				jMessage = JSONMessage.success(null, data);
			} catch (Exception e) {
				logger.error("获取商务圈消息失败", e);

				jMessage = JSONMessage.error(e);
			}

		return jMessage;
	}

	@RequestMapping(value = "/gets")
	public JSONMessage getMsgByIds(@RequestParam String ids) {
		JSONMessage jMessage;

		if (StringUtil.isEmpty(ids))
			jMessage = Result.ParamsAuthFail;
		else
			try {
				Object data = SKBeanUtils.getMsgRepository().gets(ReqUtil.getUserId(), ids);
				jMessage = JSONMessage.success(null, data);
			} catch (Exception e) {
				logger.error("批量获取商务圈消息失败", e);

				jMessage = JSONMessage.error(e);
			}

		return jMessage;
	}

	@RequestMapping(value = "/praise/list")
	public JSONMessage getPraiseList(@RequestParam String messageId,
			@RequestParam(value = "praiseId", defaultValue = "") String praiseId,
			@RequestParam(value = "pageIndex", defaultValue = "0") Integer pageIndex,
			@RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
		JSONMessage jMessage;

		try {
			if (StringUtil.isEmpty(messageId)) {
				jMessage = Result.ParamsAuthFail;
			} else {
				Object data = SKBeanUtils.getMsgPraiseRepository().find(new ObjectId(messageId), StringUtil.isEmpty(praiseId) ? null : new ObjectId(praiseId),pageIndex, pageSize);

				jMessage = JSONMessage.success(null, data);
			}
		} catch (Exception e) {
			logger.error("获取点赞列表失败", e);

			jMessage = JSONMessage.error(e);
		}

		return jMessage;
	}

	@RequestMapping("/square")
	public JSONMessage getSquareMsgList(@RequestParam(value = "messageId", defaultValue = "") String _id,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		ObjectId msgId = !ObjectId.isValid(_id) ? null : new ObjectId(_id);
		Object data = SKBeanUtils.getMsgRepository().getSquareMsgList(0, msgId, pageSize);

		return JSONMessage.success(null, data);
	}

	@RequestMapping(value = "/user/ids")
	public JSONMessage getUserMsgIdList(@RequestParam(value = "userId", defaultValue = "") Integer userId,
			@RequestParam(value = "messageId", defaultValue = "") String messageId,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		Object data = SKBeanUtils.getMsgRepository().getUserMsgIdList(null == userId ? ReqUtil.getUserId() : userId, userId,
				!ObjectId.isValid(messageId) ? null : new ObjectId(messageId), pageSize);

		return JSONMessage.success(null, data);
	}

	@RequestMapping(value = "/user")
	public JSONMessage getUserMsgList(@RequestParam(value = "userId", defaultValue = "") Integer userId,
			@RequestParam(value = "messageId", defaultValue = "") String messageId,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		Object data = SKBeanUtils.getMsgRepository().getUserMsgList(ReqUtil.getUserId(), null == userId ? ReqUtil.getUserId() : userId,
				!ObjectId.isValid(messageId) ? null : new ObjectId(messageId), pageSize);

		return JSONMessage.success(null, data);
	}

	@RequestMapping("/query")
	public JSONMessage queryByExample(@ModelAttribute MessageExample example) {
		Object data = SKBeanUtils.getMsgRepository().findByExample(ReqUtil.getUserId(), example);

		return JSONMessage.success(null, data);
	}

}
