//package cn.xyz.mianshi.service.agora;
//
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//
//public interface IAgoraService {
//    CreateVoiceCallResponse createCall(Long self, Long to)throws NoSuchAlgorithmException, InvalidKeyException;
//    void join(long uid, ChannelRequestBody body, MsgEnum.MsgTp msgTp);
//    void heartBreak(long uid, ChannelRequestBody body, String appid, MsgEnum.MsgTp msgTp, MsgEnum.FriendMsgTp friendMsgTp, MsgEnum.ActionType action);
//    void stopCall(long uid, ChannelRequestBody body, String appid, MsgEnum.MsgTp msgTp, MsgEnum.FriendMsgTp friendMsgTp, MsgEnum.ActionType action);
//    void refuseCall(long uid, ChannelRequestBody body, String appid, MsgEnum.MsgTp msgTp, MsgEnum.FriendMsgTp friendMsgTp, MsgEnum.ActionType action);
//}
