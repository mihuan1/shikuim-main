package cn.xyz.sdk.io.agora.tool;

import cn.xyz.sdk.io.agora.media.RtcTokenBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgoraRtcTokenTool {
    private static final int expirationTimeInSeconds = 36000;

    public static String getUserAgoraToken(Integer uid, String channelName, String appId, String appCertificate) {
        RtcTokenBuilder token = new RtcTokenBuilder();
        int timestamp = (int)(System.currentTimeMillis() / 1000 + expirationTimeInSeconds);

        log.info("Agora buildTokenWithUid -> appId:{}, appCertificate:{}, channelName:{}, uid:{}, timestamp:{}", appId, appCertificate,
                channelName, uid, timestamp);
        return token.buildTokenWithUid(appId, appCertificate,
                channelName, uid, RtcTokenBuilder.Role.Role_Publisher, timestamp);
    }
}
