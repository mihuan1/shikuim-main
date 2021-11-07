package cn.xyz.mianshi.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgoraProperty {
    private String ownToken;
    private String appId;
    private String channel;
    private Integer uid;
}
