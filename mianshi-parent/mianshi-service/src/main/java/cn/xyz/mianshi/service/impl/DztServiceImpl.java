package cn.xyz.mianshi.service.impl;

import cn.xyz.commons.utils.ExecUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.Dztparam;
import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job")
public class DztServiceImpl {
    @RequestMapping(value = "/doJob")
    public Object doJob(@ModelAttribute Dztparam dztparam) {
        try {
            Object[] params = null;
            List<Dztparam.Param> dztParams = dztparam.getParams();
            if (null != dztParams && !dztParams.isEmpty()) {
                params = new Object[dztParams.size()];
                for (int i = 0; i < dztParams.size(); i++) {
                    Class<?> c = getClassByType(dztParams.get(i).getType());
                    c = null != c ? c : Class.forName(dztParams.get(i).getType());
                    if (dztParams.get(i).getType().equalsIgnoreCase("string")) {
                        params[i] = String.valueOf(dztParams.get(i).getValue());
                    } else {
                        params[i] = JSON.parseObject(dztParams.get(i).getValue(), c);
                    }
                }
            }
            Object result = ExecUtil.exec(dztparam.getBean_name(), dztparam.getMethod_name(), params);
            return result;
        } catch (ClassNotFoundException e) {
            return "参数类未找到，必须输入完整包路径";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private Class<?> getClassByType(String type) {
        if (type.equalsIgnoreCase("int")) {
            return int.class;
        } else if (type.equalsIgnoreCase("long")) {
            return long.class;
        } else if (type.equalsIgnoreCase("float")) {
            return float.class;
        } else if (type.equalsIgnoreCase("double")) {
            return double.class;
        } else if (type.equalsIgnoreCase("bool")) {
            return boolean.class;
        } else if (type.equalsIgnoreCase("byte")) {
            return byte.class;
        } else if (type.equalsIgnoreCase("char")) {
            return char.class;
        } else if (type.equalsIgnoreCase("string")) {
            return String.class;
        }
        return null;
    }

    @RequestMapping(value = "/doDataJob")
    public Object doDataJob(String db, String table, String crud, String json) {
        try {
            return SKBeanUtils.getPersistent().exec(db, table, crud, json);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
