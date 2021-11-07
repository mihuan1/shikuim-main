package cn.xyz.commons.constants;

import java.util.Arrays;
import java.util.List;

/**
* @Description: TODO(消息类型常量)
* @author lidaye
* @date 2018年2月24日 
*/
public interface MsgType {
	

////////////////////////////以下为在聊天界面显示的类型/////////////////////////////////
public static final int TYPE_TEXT = 1; // 文字
public static final int TYPE_IMAGE = 2;// 图片
public static final int TYPE_VOICE = 3;// 语音
public static final int TYPE_LOCATION = 4; // 位置
public static final int TYPE_GIF = 5;  // gif
public static final int TYPE_VIDEO = 6;// 视频
public static final int TYPE_SIP_AUDIO = 7;// 音频
public static final int TYPE_CARD = 8;// 名片
public static final int TYPE_FILE = 9;// 文件
public static final int TYPE_TIP = 10;// 自己添加的消息类型,代表系统的提示

public static final int TYPE_READ = 26;    // 是否已读的回执类型

public static final int TYPE_RED = 28;     // 红包消息
public static final int TYPE_TRANSFER = 29;// 转账消息
public static final int TYPE_IMAGE_TEXT = 80;     // 单条图文消息
public static final int TYPE_IMAGE_TEXT_MANY = 81;// 多条图文消息
public static final int TYPE_LINK = 82; // 链接
public static final int TYPE_SHARE_LINK = 87; // 分享进来的链接
public static final int TYPE_83 = 83;   // 某个成员领取了红包
public static final int TYPE_SHAKE = 84;  // 戳一戳
public static final int TYPE_CHAT_HISTORY = 85;  // 聊天记录
public static final int TYPE_RED_BACK = 86;  // 红包退回通知
public static final int TYPE_RECEIVETRANSFER = 88;// 转账领取
public static final int TYPE_REFUNDTRANSFER = 89;// 转账退回
public static final int CODEPAYMENT = 90;// 付款码已付款通知
public static final int CODEARRIVAL = 91;// 付款码已到账通知

public static final int TYPE_SEND_ONLINE_STATUS = 200;// 在线情况
public static final int TYPE_INPUT = 201;// 正在输入消息
public static final int TYPE_BACK = 202; // 撤回消息

////////////////////////////音视频通话/////////////////////////////////
public static final int TYPE_IS_CONNECT_VOICE = 100;// 发起语音通话
public static final int TYPE_CONNECT_VOICE = 102;// 接听语音通话
public static final int TYPE_NO_CONNECT_VOICE = 103;// 拒绝语音通话 || 对来电不响应(30s)内
public static final int TYPE_END_CONNECT_VOICE = 104;// 结束语音通话

public static final int TYPE_IS_CONNECT_VIDEO = 110;// 发起视频通话
public static final int TYPE_CONNECT_VIDEO = 112;// 接听视频通话
public static final int TYPE_NO_CONNECT_VIDEO = 113;// 拒绝视频通话 || 对来电不响应(30s内)
public static final int TYPE_END_CONNECT_VIDEO = 114;// 结束视频通话

public static final int TYPE_IS_MU_CONNECT_Video = 115;// 视频会议邀请
public static final int TYPE_IS_MU_CONNECT_VOICE = 120;// 音频会议邀请

public static final int TYPE_IN_CALLING = 123;// 通话中...
public static final int TYPE_IS_BUSY = 124;// 忙线中...

// 暂未用到
public static final int TYPE_VIDEO_IN = 116;            // 视频会议进入
public static final int TYPE_VIDEO_OUT = 117;           // 视频会议退出
public static final int TYPE_OK_MU_CONNECT_VOICE = 121; // 音频会议进入了
public static final int TYPE_EXIT_VOICE = 122;          // 音频会议退出了

////////////////////////////朋友圈消息/////////////////////////////////
public static final int DIANZAN = 301; // 朋友圈点赞
public static final int PINGLUN = 302; // 朋友圈评论
public static final int ATMESEE = 304; // 提醒我看

////////////////////////////新朋友消息/////////////////////////////////
public static final int TYPE_SAYHELLO = 500;// 打招呼
public static final int TYPE_PASS = 501;    // 同意加好友
public static final int TYPE_FEEDBACK = 502;// 回话
public static final int TYPE_FRIEND = 508;//   直接成为好友
public static final int TYPE_BLACK = 507; //   黑名单
public static final int TYPE_REFUSED = 509;//  取消黑名单
public static final int TYPE_DELALL = 505;//   彻底删除
public static final int TYPE_CONTACT_BE_FRIEND = 510;   // 对方通过 手机联系人 添加我 直接成为好友
public static final int TYPE_NEW_CONTACT_REGISTER = 511;// 我之前上传给服务端的联系人表内有人注册了，更新 手机联系人
public static final int TYPE_REMOVE_ACCOUNT = 512;// 用户被后台删除，用于客户端更新本地数据 ，from是系统管理员 to是被删除人的userId，

// 未用到
public static final int TYPE_NEWSEE = 503;// 新关注
public static final int TYPE_DELSEE = 504;// 删除关注
public static final int TYPE_RECOMMEND = 506;// 新推荐好友

////////////////////////////群组协议/////////////////////////////////
public static final int TYPE_MUCFILE_ADD = 401; // 群文件上传
public static final int TYPE_MUCFILE_DEL = 402; // 群文件删除
public static final int TYPE_MUCFILE_DOWN = 403;// 群文件下载

public static final int TYPE_CHANGE_NICK_NAME = 901; // 修改昵称
public static final int TYPE_CHANGE_ROOM_NAME = 902; // 修改房间名
public static final int TYPE_DELETE_ROOM = 903;// 删除房间
public static final int TYPE_DELETE_MEMBER = 904;// 退出、被踢出群组
public static final int TYPE_NEW_NOTICE = 905; // 新公告
public static final int TYPE_GAG = 906;// 禁言/取消禁言
public static final int NEW_MEMBER = 907; // 增加新成员
public static final int TYPE_SEND_MANAGER = 913;// 设置/取消管理员

public static final int TYPE_CHANGE_SHOW_READ = 915; // 设置群已读消息
public static final int TYPE_GROUP_VERIFY = 916; // 群组验证消息
public static final int TYPE_GROUP_LOOK = 917; // 群组是否公开
public static final int TYPE_GROUP_SHOW_MEMBER = 918; // 群组是否显示群成员列表
public static final int TYPE_GROUP_SEND_CARD = 919; // 群组是否允许发送名片
public static final int TYPE_GROUP_ALL_SHAT_UP = 920; // 全体禁言
public static final int TYPE_GROUP_ALLOW_NORMAL_INVITE = 921; // 允许普通成员邀请人入群
public static final int TYPE_GROUP_ALLOW_NORMAL_UPLOAD = 922; // 允许普通成员上传群共享
public static final int TYPE_GROUP_ALLOW_NORMAL_CONFERENCE = 923; // 允许普通成员发起会议
public static final int TYPE_GROUP_ALLOW_NORMAL_SEND_COURSE = 924;// 允许普通成员发送讲课
public static final int TYPE_GROUP_TRANSFER = 925; // 转让群组

public static final int TYPE_UPDATE_ROLE = 930;// 设置/取消隐身人，监控人，
public static final int TYPE_DISABLE_GROUP = 931;// 群组被后台锁定/解锁

////////////////////////////直播协议/////////////////////////////////
public static final int TYPE_SEND_DANMU = 910;// 弹幕
public static final int TYPE_SEND_GIFT = 911; // 礼物
public static final int TYPE_SEND_HEART = 912;// 点赞
public static final int TYPE_SEND_ENTER_LIVE_ROOM = 914;// 加入直播间
// 以前直播间和群组共用了部分协议，现独立出来
public static final int TYPE_LIVE_LOCKING = 926; // 锁定直播间(后台可锁定用户直播间)
public static final int TYPE_LIVE_EXIT_ROOM = 927;// 退出、被踢出直播间
public static final int TYPE_LIVE_SHAT_UP = 928;// 禁言/取消禁言
public static final int TYPE_LIVE_SET_MANAGER = 929;// 设置/取消管理员



public static final List<Integer> FileTypeArr=
Arrays.asList(TYPE_IMAGE,TYPE_VOICE,
		TYPE_GIF,TYPE_VIDEO,TYPE_FILE);
}

