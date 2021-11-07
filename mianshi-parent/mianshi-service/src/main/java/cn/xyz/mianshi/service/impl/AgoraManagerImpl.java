package cn.xyz.mianshi.service.impl;

import cn.xyz.commons.IdWorker;
import cn.xyz.commons.autoconfigure.KApplicationProperties;
import cn.xyz.commons.ex.BizException;
import cn.xyz.mianshi.model.AgoraProperty;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.PaidMerchant;
import cn.xyz.sdk.io.agora.tool.AgoraRtcTokenTool;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AgoraManagerImpl {
    public AgoraProperty getAgoraProperty(Integer uid, String channel) {
        if (null == uid || uid <= 0) {
            throw new RuntimeException("uid为空，未登录用户");
        }
        channel = StringUtils.isBlank(channel) ? IdWorker.getId() + "" : channel;
        String agoraAppId = SKBeanUtils.getAgoraConfig().getAppId();
        String agoraCertificate = SKBeanUtils.getAgoraConfig().getCertificate();
        AgoraProperty agoraProperty = AgoraProperty.builder()
                .uid(uid)
                .appId(agoraAppId)
                .channel(channel)
                .ownToken(AgoraRtcTokenTool.getUserAgoraToken(uid, channel, agoraAppId, agoraCertificate))
                .build();
        log.info("Agora build AgoraProperty -> {}", JSON.toJSONString(agoraProperty));
        return agoraProperty;
    }
}