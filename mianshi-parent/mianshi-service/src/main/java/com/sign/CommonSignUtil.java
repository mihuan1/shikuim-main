package com.sign;

import cn.xyz.commons.utils.Md5Util;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class CommonSignUtil {

    /**
     * 签名生成算法
     *
     * @param  params 请求参数集，所有参数必须已转换为字符串类型
     * @param               secret 签名密钥
     * @return 签名
     */
    public static String getSignMd5(Map params, String secret) {
        // 先将参数以其参数名的字典序升序进行排序
        Map<String, Object> sortMap = sortByKey(params, false);

        // 遍历排序后的字典，将所有参数按"key=value"格式拼接在一起
        StringBuilder basestring = new StringBuilder(asUrlParams(sortMap));

        basestring.append(secret);
        String origin = basestring.toString();
//        System.out.println("加密前：" + origin);
        // 使用MD5对待签名串求签
        return Md5Util.md5Hex(origin);
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map, boolean isDesc) {
        Map<K, V> result = Maps.newLinkedHashMap();
        if (isDesc) {
            map.entrySet().stream().sorted(Map.Entry.<K, V>comparingByKey().reversed())
                    .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        } else {
            map.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        }
        return result;
    }

    public static String asUrlParams(Map<String, Object> source) {
        Map<String, String> tmp = Maps.newLinkedHashMap();
        source.forEach((k, v) -> {
            String value = v + "";
            if (k != null && StringUtils.isNotBlank(value)) {
                tmp.put(k, value);
            }
        });
        return Joiner.on("&").useForNull("").withKeyValueSeparator("=").join(tmp);
    }

    public static String getSignByObjectMd5(Map map, String secret) {
        map.remove("sign");
        map.remove("sign_type");
        map.remove("signType");
        return getSignMd5(map, secret);
    }

    public static String getSignByObjectMd5(Object obj, String secret) {
        Map map = JSON.parseObject(JSON.toJSONString(obj), Map.class);
        return getSignByObjectMd5(map, secret);
    }

    public static String getHoleParamHasSignMD5(Object obj, String secret) {
        Map map = JSON.parseObject(JSON.toJSONString(obj), Map.class);
        String sign = getSignByObjectMd5(map, secret);
        map.put("sign", sign);
        map.put("sign_type", "MD5");
        return asUrlParams(map);
    }
}
