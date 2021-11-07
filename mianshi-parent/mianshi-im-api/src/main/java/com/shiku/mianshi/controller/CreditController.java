package com.shiku.mianshi.controller;

import cn.xyz.commons.ex.BizException;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.utils.SKBeanUtils;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/credit")
public class CreditController extends AbstractController {

    /** @Description:提现申请
     * @param money
     * @return
     **/
    @RequestMapping("/applyDrawings")
    public JSONMessage applyDrawings(@RequestParam Double money,
                                @RequestParam String cardId,
                                @RequestParam(defaultValue="0") long time,
                                @RequestParam(defaultValue="") String secret) {
        try {
            SKBeanUtils.getConsumeRecordManager().applyDrawings(ReqUtil.getUserId(), money, cardId);
            return JSONMessage.success("申请成功");
        } catch (BizException e) {
            return JSONMessage.failure(e.getErrorMessage());
        }
    }

    @RequestMapping("/bindCard")
    public JSONMessage bindCard(@RequestParam String userName,
                                @RequestParam String cardNo,
                                @RequestParam Integer bankBrandId,
                                String openBankAddr,
                                @RequestParam(defaultValue="0") long time,
                                @RequestParam(defaultValue="") String secret) {
        try {
            return JSONMessage.success("绑定成功", SKBeanUtils.getBankCardManager().bindCard(ReqUtil.getUserId(), userName, cardNo, bankBrandId, openBankAddr));
        } catch (BizException e) {
            return JSONMessage.failure(e.getErrorMessage());
        }
    }

    @RequestMapping("/unbindCard")
    public JSONMessage unbindCard(@RequestParam String cardId,
                                @RequestParam(defaultValue="0") long time,
                                @RequestParam(defaultValue="") String secret) {
        try {
            SKBeanUtils.getBankCardManager().unbindBankCard(ReqUtil.getUserId(), new ObjectId(cardId));
        } catch (BizException e) {
            return JSONMessage.failure(e.getErrorMessage());
        }
        return JSONMessage.success("删除成功");
    }

    @RequestMapping("/bindCardList")
    public JSONMessage cardList(@RequestParam(defaultValue="0") long time,
                                @RequestParam(defaultValue="") String secret) {
        return JSONMessage.success(SKBeanUtils.getBankCardManager().getBankCardList(ReqUtil.getUserId()));
    }
}
