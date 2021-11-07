package cn.xyz.mianshi.service.impl;


import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.ex.BizException;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import cn.xyz.mianshi.vo.ConsumeRecord;
import cn.xyz.mianshi.vo.PaidMerchant;
import com.sign.CommonSignUtil;
import com.yipay.dto.YipayPayRedirectParam;
import com.yipay.util.YipayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class VendorPayManagerImpl {

    public static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    public String getPayRedirectUrl(Integer uid, String payType, double money) throws BizException {
        if (money <= 0) throw new BizException("交易金额太低");
        PaidMerchant paidMerchant = getRandomPaidMerchant(payType);
        if (null == paidMerchant) throw new BizException("该付款方式维护中，请切换付款方式");
        YipayPayRedirectParam redirectParam = new YipayPayRedirectParam();
        redirectParam.setPid(paidMerchant.getVendorMerchantId());
        redirectParam.setType(payType);
        redirectParam.setOut_trade_no(StringUtil.getOutTradeNo());
        String host = SKBeanUtils.getAdminManager().getClientConfig().getApiUrl();

        String connector = host.endsWith("/") ? "" : "/";
        redirectParam.setNotify_url(host + connector + "vendor/pay/user/recharge/yiPayCallBack");
        redirectParam.setReturn_url(host + connector + "vendor/pay/user/recharge/yiPayPageJump");
        redirectParam.setName(paidMerchant.getName());
        redirectParam.setMoney(money + "");
        redirectParam.setSitename(paidMerchant.getSitename());

        ConsumeRecord record = new ConsumeRecord();
        record.setUserId(uid);
        record.setTradeNo(redirectParam.getOut_trade_no());
        record.setMoney(money);
        record.setStatus(KConstants.OrderStatus.CREATE);
        record.setType(KConstants.ConsumeType.USER_RECHARGE);
        record.setPayType(KConstants.PAY_TYPE_MAP.getOrDefault(payType, KConstants.PayType.ALIPAY));
        record.setDesc("充值");
        record.setTime(DateUtil.currentTimeSeconds());
        record.setOperationAmount(money);
        record.setServiceCharge(0.0);
        record.setAuditStatus(KConstants.AuditStatusCons.AUDIT_PASS);
        record.setCurrentBalance(SKBeanUtils.getUserManager().getUserMoeny(uid));
        record.setPayVendorChannel(KConstants.PayVendorChannel.YIPAY);
        record.setPayMerchantId(paidMerchant.getId().toString());
        SKBeanUtils.getConsumeRecordManager().save(record);
        String payRedirectUrl = YipayUtil.getPayRedirectUrl(redirectParam, paidMerchant.getPayUrl(), paidMerchant.getSign());
        log.info("==========>>>> payRedirectUrl= " + payRedirectUrl);
        return payRedirectUrl;
    }

    private PaidMerchant getRandomPaidMerchant(String payType) {
        return SKBeanUtils.getPaidMerchantManager().getRandomPaidMerchant(payType);
    }


    public String doPayCallback(String pid, String trade_no, String out_trade_no, String type, String name, Double money, String trade_status, String sign, String sign_type) {
        log.info("易支付回调数据开始");
        try {
            if (!"MD5".equals(sign_type)) {
                log.error("易支付回调，不支持的签名方式");
                return "不支持的签名方式";
            }
            Map map = new HashMap(9);
            map.put("pid", pid);
            map.put("trade_no", trade_no);
            map.put("out_trade_no", out_trade_no);
            map.put("type", type);
            map.put("name", name);
            map.put("money", money);
            map.put("trade_status", trade_status);
            PaidMerchant paidMerchant = SKBeanUtils.getPaidMerchantManager().getPaidMerchantByVendorMerchantId(pid);
            if (null == paidMerchant) {
                log.error("易支付回调，商户id无对应商户");
                return "id无对应账户";
            }
            if (!CommonSignUtil.getSignByObjectMd5(map, paidMerchant.getSign()).equals(sign)) {
                log.error("易支付回调，签名错误");
                return "签名错误";
            }
            ConsumeRecord entity=SKBeanUtils.getConsumeRecordManager().getConsumeRecordByNo(out_trade_no);
            if(null==entity)
                log.info("交易订单号不存在！-----"+out_trade_no);
            else if(0!=entity.getStatus())
                log.info(out_trade_no+"===status==="+entity.getStatus()+"=======交易已处理或已取消!");
            else if(TRADE_SUCCESS.equals(trade_status)){
                boolean flag=entity.getOperationAmount().equals(money);
                if(flag){
                    entity.setStatus(KConstants.OrderStatus.END);
                    Double balance = SKBeanUtils.getUserManager().rechargeUserMoeny(entity.getUserId(), entity.getMoney(), KConstants.MOENY_ADD);
                    entity.setOperationAmount(entity.getMoney());
                    entity.setCurrentBalance(balance);
                    entity.setPayVendorTradeNo(trade_no);
                    SKBeanUtils.getConsumeRecordManager().update(entity.getId(), entity);
                    log.info(out_trade_no+"========>>微信支付成功!");
                    return "付款成功";
                }else{
                    log.info("易支付数据返回错误!");
                    log.info("localhost:Money---------"+entity.getMoney() +",callback money:"+money);
                    return ("付款失败，数据返回错误");
                }
            }else{
                log.info("易支付失败======");
            }
            return ("付款失败");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ("付款失败:" + e.getMessage());
        }
    }


}
