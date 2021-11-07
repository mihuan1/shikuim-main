package com.shiku.push.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.shiku.push.autoconfigure.ApplicationProperties.PushConfig;
import com.shiku.push.service.ApnsHttp2PushService.PushEnvironment;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.MsgType;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.service.impl.UserManagerImpl;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.ClientConfig;
import cn.xyz.mianshi.vo.Config;
import cn.xyz.mianshi.vo.MsgNotice;
import cn.xyz.mianshi.vo.User.DeviceInfo;

/**
* @Description: TODO(第三方推送工具类)
* @author lidaye
* @date 2018年8月20日 
*/
public abstract class PushServiceUtils {
	protected static final Logger log = LoggerFactory.getLogger(PushServiceUtils.class);
	public static PushConfig getPushConfig(){
		PushConfig pushConfig = (PushConfig) SKBeanUtils.getLocalSpringBeanManager().getPushConfig();
		return pushConfig;
	};
	
	protected static String appPkgName = getPushConfig().getPackageName();
	
	/**
	* @Description: TODO(第三方推送 到  android 设备)
	* @param @param toUserId
	* @param @param notice
	* @param @param deviceInfo
	* @param @return    参数
	 */
	public static boolean pushToAndroid(int toUserId,MsgNotice notice,DeviceInfo deviceInfo){
		try {
			switch (deviceInfo.getPushServer()) {
				case KConstants.PUSHSERVER.XIAOMI:
					XMPushService.pushToRegId(notice, notice.getFileName(),deviceInfo.getPushToken());
					/**
					 * 音视频消息  发送透传通知  
					 */
					// 2019.03.11 小米推送不使用透传消息  zhm
//					if(100==notice.getType()||110==notice.getType()||
//						115==notice.getType()||120==notice.getType()){
//						XMPushService.pushTransToRegId(notice,notice.getFileName(),deviceInfo.getPushToken());
//					}
					break;
				case KConstants.PUSHSERVER.JPUSH:
					Map<String, String> content = Maps.newConcurrentMap();
					content.put("msg", notice.getText());
					content.put("regId", deviceInfo.getPushToken());
					content.put("title", notice.getTitle());
					JPushServices.jpushAndroid(content);
					break;
				case KConstants.PUSHSERVER.HUAWEI:
					HWPushService.sendPushMessage(notice,notice.getFileName(),deviceInfo.getPushToken());
					/**
					 * 音视频消息  发送透传通知
					 */
					if(100==notice.getType()||110==notice.getType()||
							115==notice.getType()||120==notice.getType()){
						HWPushService.sendTransMessage(notice,notice.getFileName(),deviceInfo.getPushToken());
					}
					break;
				case KConstants.PUSHSERVER.FCM:
					GooglePushService.fcmPush(deviceInfo.getPushToken(),notice);
					break;
				case KConstants.PUSHSERVER.MEIZU:
					MZPushService.varnishedMessagePush(deviceInfo.getPushToken(), notice);
					break;
				case KConstants.PUSHSERVER.OPPO:
					OPPOPushService.buildMessage(deviceInfo.getPushToken(), notice);
					break;
				case KConstants.PUSHSERVER.VIVO:
					VIVOPushService.noticeColumnMessagePush(deviceInfo.getPushToken(), notice);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	/**
	* @Description: TODO(第三方推送 到  ios 设备)
	* @param @param toUserId
	* @param @param notice
	* @param @param deviceInfo
	* @param @return    参数
	 */
	public static boolean pushToIos(int toUserId,MsgNotice notice,DeviceInfo deviceInfo){
		UserManagerImpl userManager = SKBeanUtils.getUserManager();
		//未读消息数量
		
		boolean isBeta=false;
		String betaAppId = getPushConfig().getBetaAppId();
		if(!StringUtil.isEmpty(betaAppId)&& betaAppId.equals(deviceInfo.getAppId()))
			isBeta=true;
		
		if(MsgType.TYPE_BACK==notice.getType()) {
			notice.setMsgNum(userManager.getMsgNum(toUserId)-1);
		}else if(99<notice.getType()&&130>notice.getType()){
			if(isBeta) {
				/**
				 * 企业版  取消音视频通话需要 加数量
				 */
				if(MsgType.TYPE_NO_CONNECT_VIDEO!=notice.getType()&&MsgType.TYPE_NO_CONNECT_VOICE!=notice.getType())
					notice.setMsgNum(userManager.getMsgNum(toUserId));
				else 
					notice.setMsgNum(userManager.getMsgNum(toUserId)+1);
			}else {
				
				/**
				 * 正式版  取消视频通话需要 加数量
				 */
				if(MsgType.TYPE_NO_CONNECT_VIDEO!=notice.getType())
					notice.setMsgNum(userManager.getMsgNum(toUserId));
				else 
					notice.setMsgNum(userManager.getMsgNum(toUserId)+1);
			}
		}else 
			notice.setMsgNum(userManager.getMsgNum(toUserId)+1);
		
		userManager.changeMsgNum(toUserId,notice.getMsgNum());
		try {
			switch (deviceInfo.getPushServer()) {
				//百度推送
				case KConstants.PUSHSERVER.BAIDU:
					
					BaiduPushService.PushMessage msg = new BaiduPushService.PushMessage(notice);
					String appId=getPushConfig().getBd_appStore_appId();
					String result = BaiduPushService.pushSingle(2, deviceInfo.getPushToken(), msg,appId);
					System.out.println(notice.getTo()+"=====>"+result);
					break;
				case KConstants.PUSHSERVER.APNS:
					//是否企业测试版 
					
					if(notice.getType()==100||notice.getType()==110||notice.getType()==115||notice.getType()==120){
						//apns 推送
						Config config = SKBeanUtils.getAdminManager().getConfig();
						ClientConfig clientConfig=SKBeanUtils.getAdminManager().getClientConfig();
						if(1==clientConfig.getDisplayRedPacket()&&1==config.getIsOpenVoip()){
							if(!StringUtil.isEmpty(deviceInfo.getVoipToken()))
								ApnsHttp2PushService.pushMsgToUser(deviceInfo.getVoipToken(),notice,PushEnvironment.VOIP);
							else {
								log.info("apns voip push VoipToken is null userId => {}",notice.getTo());
								ApnsHttp2PushService.pushMsgToUser(deviceInfo.getPushToken(),notice,isBeta?PushEnvironment.BETA:PushEnvironment.Pro);
							}
								//ApnsPushService.pushMsgToUser(deviceInfo.getVoipToken(), notice,PushEnvironment.VOIP);
							return true;
						}
					}
					
					ApnsHttp2PushService.pushMsgToUser(deviceInfo.getPushToken(),notice,isBeta?PushEnvironment.BETA:PushEnvironment.Pro);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}

