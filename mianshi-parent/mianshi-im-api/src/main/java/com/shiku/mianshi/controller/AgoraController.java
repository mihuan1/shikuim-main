//package com.shiku.mianshi.controller;
//
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import javax.validation.Valid;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//
//@RestController
//@RequestMapping(value = "/av/call")
//public class AgoraController extends AbstractController {
//
//
//	@Resource
//	IAgoraService agoraService;
//
//
//	@ImResponseBody
//	@RequestMapping(value = "create")
//	public CreateVoiceCallResponse create(@RequestBody @Valid CreateVoiceCallRequest request, HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeyException {
//		long uid = CurrentUserSession.getUserId();
//		logger.info("voicecall.create {} {}", request.getToUser(), uid);
//		return agoraService.createCall(uid, request.getToUser());
//	}
//
//	@ImResponseBody
//	@RequestMapping(value = "stop")
//	public void stop(@RequestBody ChannelRequestBody body, HttpServletRequest req) {
//		String appid = HttpServletHelper.getEnvironment(req).getAppId();
//		long uid = CurrentUserSession.getUserId();
//		agoraService.stopCall(CurrentUserSession.getUserId(), body, appid, MsgEnum.MsgTp.FriendMsg,MsgEnum.FriendMsgTp.VoiceChat, MsgEnum.ActionType.VOICECALLSTOP);
//	}
//
//	@ImResponseBody
//	@RequestMapping(value = "join")
//	public void join(@RequestBody @Valid ChannelRequestBody body, HttpServletRequest req) {
//		logger.info("voice.call {} {}", body.getChannel(), req.getHeader(RequestConstans.USER_ID));
//		long uid = Long.parseLong(req.getHeader(RequestConstans.USER_ID));
//		agoraService.join(uid, body,MsgEnum.MsgTp.FriendMsg);
//	}
//
//	@ImResponseBody
//	@RequestMapping(value = "refuse")
//	public void refuse(@RequestBody ChannelRequestBody body, HttpServletRequest req) {
//		String appid = HttpServletHelper.getEnvironment(req).getAppId();
//		agoraService.refuseCall(CurrentUserSession.getUserId(), body, appid,MsgEnum.MsgTp.FriendMsg,MsgEnum.FriendMsgTp.VoiceChat, MsgEnum.ActionType.VOICECALLREFUSE);
//	}
//
//
//	@ImResponseBody
//	@RequestMapping(value = "heartbreak")
//	public void heartbreak(@RequestBody ChannelRequestBody body, HttpServletRequest req) {
//		String appid = HttpServletHelper.getEnvironment(req).getAppId();
//		agoraService.heartBreak(CurrentUserSession.getUserId(), body, appid,MsgEnum.MsgTp.FriendMsg,MsgEnum.FriendMsgTp.VoiceChat, MsgEnum.ActionType.VOICECALLSTOP);
//	}
//}
