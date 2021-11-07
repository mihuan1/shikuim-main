package com.shiku.mianshi.controller;

import cn.xyz.commons.ex.BizException;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.PaidMerchant;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/console/paid/merchant")
public class PaidMerchantController extends AbstractController {


    @RequestMapping("/add")
    public JSONMessage add(@ModelAttribute PaidMerchant paidMerchant) {
        SKBeanUtils.getPaidMerchantManager().save(paidMerchant);
        return JSONMessage.success("添加成功");
    }

    @RequestMapping("/del")
    public JSONMessage del(@RequestParam String merchantId) {
        SKBeanUtils.getPaidMerchantManager().delPaidMerchant(new ObjectId(merchantId));
        return JSONMessage.success("删除成功");
    }

    @RequestMapping("/disEnable")
    public JSONMessage enable(@RequestParam String merchantId, @RequestParam Integer operate) {
        try {
            if (0 == operate) {
                SKBeanUtils.getPaidMerchantManager().enablePaidMerchant(new ObjectId(merchantId));
            } else {
                SKBeanUtils.getPaidMerchantManager().disablePaidMerchant(new ObjectId(merchantId));
            }
        } catch (BizException e) {
            return JSONMessage.failure(e.getErrorMessage());
        }
        return JSONMessage.success("操作成功");
    }

    @RequestMapping("/list")
    public JSONMessage list() {
        return JSONMessage.success(SKBeanUtils.getPaidMerchantManager().getForceAllPaidMerchants());
    }
}
