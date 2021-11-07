package cn.xyz.mianshi.vo;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.List;

@Data
public class Dztparam {
    private String bean_name;
    private String method_name;
    private String params;

    public String toJson() {
        return JSON.toJSONString(this);
    }

    public List<Param> getParams() {
        return JSON.parseArray(params, Param.class);
    }

    @Data
    public static class Param {
        private String type;
        private String value;
    }
}
