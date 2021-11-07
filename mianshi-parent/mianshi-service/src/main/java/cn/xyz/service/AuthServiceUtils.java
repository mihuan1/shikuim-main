package cn.xyz.service;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.Md5Util;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.utils.SKBeanUtils;
import com.alibaba.fastjson.JSONObject;
import com.shiku.utils.Base64;
import com.shiku.utils.encrypt.AES;
import com.shiku.utils.encrypt.MAC;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 各种 加密 权限验证的类
 *
 * @author lidaye
 */
@Slf4j
public class AuthServiceUtils {

    private static String apiKey = null;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceUtils.class);

    public static String getApiKey() {
        if (null == apiKey) {
            apiKey = SKBeanUtils.getLocalSpringBeanManager().getApplicationConfig().getAppConfig().getApiKey();
        }
        return apiKey;
    }

    /**
     * 检验接口请求时间
     *
     * @param time
     * @return
     */
    public static boolean authRequestTime(long time) {
        long currTime = DateUtil.currentTimeSeconds();
        //允许 3分钟时差
        if (((currTime - time) < 180 && (currTime - time) > -180)) {
            return true;
        } else {
            System.out.println(String.format("====> authRequestTime error server > %s client %s", currTime, time));
            return false;
        }
    }


    /**
     * 检验 开放的 不需要 token 的接口
     *
     * @param time
     * @return
     */
    public static boolean authOpenApiSecret(long time, String secret) {
        /**
         * 判断  系统配置是否要校验
         */
        if (0 == SKBeanUtils.getSystemConfig().getIsAuthApi()) {
            return true;
        }

        if (!authRequestTime(time)) {
            return false;
        }
        if (StringUtil.isEmpty(secret)) {
            return false;
        }
        /**
         * 密钥
         md5(apikey+time)
         */

        /**
         *  apikey+time
         */
        String key = getApiKey() + time;

        return secret.equals(Md5Util.md5Hex(key));

    }

    /**
     * 普通接口授权
     *
     * @param userId
     * @param time
     * @param token
     * @param secret
     * @return
     */
    public static boolean authRequestApi(String userId, long time, String token, String secret, String url) {
        if (KConstants.filterSet.contains(url)) {
            return true;
        }

        /**
         * 判断  系统配置是否要校验
         */
        if (0 == SKBeanUtils.getSystemConfig().getIsAuthApi()) {
            return true;
        }
        if (!authRequestTime(time)) {
            return false;
        }
        if (StringUtil.isEmpty(secret)) {
            return false;
        }
        String secretKey = getRequestApiSecret(userId, time, token);
        if (!secretKey.equals(secret)) {
            log.info("authRequestApi request -> userId:{}, time:{}, token:{}, secret:{}, url:{}, rightSecretKey:{}",
                    userId, time, token, secret, url, secretKey);
            return false;
        } else {
            return true;
        }

    }

    public static String getRequestApiSecret(String userId, long time, String token) {

        /**
         * 密钥
         md5(apikey+time+userid+token)
         */


        /**
         *  apikey+time+userid+token
         */
        String key = getApiKey() + time + userId + token;

        return Md5Util.md5Hex(key);

    }

    /**
     * 发送短信验证码 授权
     *
     * @param userId
     * @param time
     * @return
     */
    public static boolean authSendTelMsgSecret(String userId, long time, String secret) {

        /**
         * 密钥
         md5(apikey+time+userid+token)
         */


        /**
         *  apikey+time+userid+token
         */
        String key = getApiKey() + time + userId;

        return secret.equals(Md5Util.md5Hex(key));

    }

    public static boolean authRedPacket(String payPassword, String userId, String token, long time, String secret) {

        if (!authRequestTime(time)) {
            return false;
        }
        if (StringUtil.isEmpty(secret)) {
            return false;
        }
        if (StringUtil.isEmpty(payPassword)) {
            return false;
        }

        String secretKey = getRedPacketSecret(payPassword, userId, token, time);

        if (!secretKey.equals(secret)) {
            log.info("authRedPacket request -> userId:{}, time:{}, token:{}, secret:{}, payPassword:{}, rightSecretKey:{}",
                    userId, time, token, secret, payPassword, secretKey);
            return false;
        } else {
            return true;
        }

    }

    public static boolean authRedPacketV1(String payPassword, String userId, String token, long time, String money, String secret) {
        if (!authRequestTime(time)) {
            return false;
        }
        if (StringUtil.isEmpty(secret)) {
            return false;
        }
        if (StringUtil.isEmpty(payPassword)) {
            return false;
        }

        String secretKey = getRedPacketSecretV1(payPassword, userId, token, time, money);

        if (!secretKey.equals(secret)) {
            log.info("authRedPacketV1 request -> userId:{}, time:{}, token:{}, secret:{}, payPassword:{}, rightSecretKey:{}",
                    userId, time, token, secret, payPassword, secretKey);
            return false;
        } else {
            return true;
        }

    }

    public static boolean authRedPacket(String userId, String token, long time, String secret) {
        if (!authRequestTime(time)) {
            return false;
        }
        if (StringUtil.isEmpty(secret)) {
            return false;
        }

        String secretKey = getRedPacketSecret(userId, token, time);

        if (!secretKey.equals(secret)) {
            log.info("authRedPacket request -> userId:{}, time:{}, token:{}, secret:{}, rightSecretKey:{}",
                    userId, time, token, secret, secretKey);
            return false;
        } else {
            return true;
        }

    }

    /**
     * 检验授权 红包相关接口
     *
     * @param payPassword
     * @param userId
     * @param token
     * @param time
     * @return
     */
    public static String getRedPacketSecret(String payPassword, String userId, String token, long time) {

        /**
         * 密钥
         md5( md5(apikey+time) +userid+token)
         */

        /**
         * apikey+time+money
         */
        String apiKey_time = getApiKey() + time;

        /**
         * userid+token
         */
        String userid_token = userId + token;
        /**
         * payPassword
         */
        String md5payPassword = payPassword;
        /**
         * md5(apikey+time+money)
         */
        String md5ApiKey_time = Md5Util.md5Hex(apiKey_time);

        /**
         *  md5(apikey+time+money) +userid+token+payPassword
         */
        String key = md5ApiKey_time + userid_token + md5payPassword;

        return Md5Util.md5Hex(key);

    }

    public static String getRedPacketSecretV1(String payPassword, String userId, String token, long time, String money) {
        /**
         * 密钥
         md5( md5(apikey+time+money) +userid+token)
         */

        /**
         * apikey+time+money
         */
        String apiKey_time_money = getApiKey() + time + money;

        /**
         * userid+token
         */
        String userid_token = userId + token;
        /**
         * payPassword
         */
        String md5payPassword = payPassword;
        /**
         * md5(apikey+time+money)
         */
        String md5ApiKey_time_money = Md5Util.md5Hex(apiKey_time_money);

        /**
         *  md5(apikey+time+money) +userid+token+payPassword
         */
        String key = md5ApiKey_time_money + userid_token + md5payPassword;

        return Md5Util.md5Hex(key);

    }

    public static String getRedPacketSecret(String userId, String token, long time) {

        /**
         * 密钥
         md5( md5(apikey+time) +userid+token)
         */

        /**
         * apikey+time
         */
        String apiKey_time = getApiKey() + time;

        /**
         * userid+token
         */
        String userid_token = userId + token;

        /**
         * md5(apikey+time)
         */
        String md5ApiKey_time = Md5Util.md5Hex(apiKey_time);

        /**
         *  md5(apikey+time) +userid+token+payPassword
         */
        String key = md5ApiKey_time + userid_token;

        return Md5Util.md5Hex(key);

    }

    /**
     * 发消息、群发消息、发群组消息
     *
     * @param userId
     * @param time
     * @param content
     * @param secret
     * @return
     */
    public static boolean authSendMsg(String userId, long time, String content, String secret) {
        if (!authRequestTime(time)) {
            return false;
        }
        if (StringUtil.isEmpty(secret)) {
            return false;
        }
        /**
         * 密钥
         md5(apikey+userid+time+content)
         */


        /**
         *  apikey+userid+time+content
         */
        String key = getApiKey() + userId + time + content;

        String secretKey = Md5Util.md5Hex(key);

        if (!secretKey.equals(secret)) {
            log.info("authSendMsg request -> userId:{}, time:{}, content:{}, secret:{}, rightSecretKey:{}",
                    userId, time, content, secret, secretKey);
            return false;
        } else {
            return true;
        }

    }

    public static boolean authWxTransferPay(String payPassword, String userId, String token, String amount, String openid, long time, String secret) {
        if (!authRequestTime(time)) {
            return false;
        }
        if (StringUtil.isEmpty(secret)) {
            return false;
        }
        if (StringUtil.isEmpty(payPassword)) {
            return false;
        }
        String secretKey = getWxTransferPaySecret(payPassword, userId, token, amount, openid, time);
        if (!secretKey.equals(secret)) {
            log.info("authWxTransferPay request -> userId:{}, time:{}, token:{}, secret:{}, payPassword:{}, amount:{}, openid:{}, rightSecretKey:{}",
                    userId, time, token, secret, payPassword, amount, openid, secretKey);
            return false;
        } else {
            return true;
        }

    }

    /**
     * 微信 提现 的 加密 认证方法
     *
     * @return
     */
    public static String getWxTransferPaySecret(String payPassword, String userId, String token, String amount, String openid, long time) {
        /**
         * 提现密钥
         md5(apiKey+openid+userid + md5(token+amount+time) )
         */

        /**
         * apiKey+openid+userid
         */
        String apiKey_openid_userId = getApiKey() + openid + userId;

        /**
         * token+amount+time
         */
        String token_amount_time = token + amount + time;

        /**
         * md5(token+amount+time)
         */
        String md5Token = Md5Util.md5Hex(token_amount_time);

        /**
         * md5(payPassword)
         */
        String md5PayPassword = payPassword;

        /**
         * apiKey+openid+userid + md5(token+amount+time)
         */
        String key = apiKey_openid_userId + md5Token + md5PayPassword;

        return Md5Util.md5Hex(key);
    }


    /**
     * @param appId
     * @param appSecret
     * @param time
     * @param secret
     * @return
     * @Description:（应用授权的 加密 认证方法）
     **/
    public static boolean getAppAuthorization(String appId, String appSecret, long time, String secret) {
        boolean flag = false;
        if (!authRequestTime(time)) {
            return flag;
        }
        if (StringUtil.isEmpty(appId)) {
            return flag;
        }
        if (StringUtil.isEmpty(appSecret)) {
            return flag;
        }
        String secretKey = getAppAuthorizationSecret(appId, time, appSecret);
        if (!secretKey.equals(secret)) {
            log.info("getAppAuthorization request -> appId:{}, time:{}, appSecret:{}, secret:{}, rightSecretKey:{}",
                    appId, time, appSecret, secret, secretKey);
            return flag;
        } else {
            return !flag;
        }
    }

    public static boolean getAuthInterface(String appId, String userId, String token, long time, String appSecret, String secret) {
        boolean flag = false;
        if (!authRequestTime(time)) {
            return flag;
        }
        if (StringUtil.isEmpty(appId)) {
            return flag;
        }
        if (StringUtil.isEmpty(appSecret)) {
            return flag;
        }
        String secretKey = getAuthInterfaceSecret(appId, userId, token, time, appSecret);
        if (!secretKey.equals(secret)) {
            log.info("getAuthInterface request -> userId:{}, time:{}, token:{}, secret:{}, appId:{}, appSecret:{}, rightSecretKey:{}",
                    userId, time, token, secret, appId, appSecret, secretKey);
            return flag;
        } else {
            return !flag;
        }
    }

    public static String getAppAuthorizationSecret(String appId, long time, String appSecret) {
        // secret=md5(appId+md5(time)+md5(appSecret))
        /**
         * md5(time)
         */
        String times = String.valueOf(time);
        String md5Time = Md5Util.md5Hex(times);

        /**
         * appId+md5(time)
         */
        String AppIdMd5time = appId + md5Time;

        /**
         * appId+md5(time)+md5(appSecret)
         */
        String md5AppSecret = Md5Util.md5Hex(appSecret);

        String secret = AppIdMd5time + md5AppSecret;


        String key = Md5Util.md5Hex(secret);

        return key;
    }

    public static String getAuthInterfaceSecret(String appId, String userId, String token, long time, String appSecret) {
        // secret=md5(apikey+appId+userid+md5(token+time)+md5(appSecret))

        /**
         * md5(appSecret)
         */
        String md5AppSecret = Md5Util.md5Hex(appSecret);

        /**
         * md5(token+time)
         */

        String tokenTime = token + time;
        String md5TokenTime = Md5Util.md5Hex(tokenTime);

        /**
         * apikey+appId+userId
         */

        String apiKeyAppIdUserId = getApiKey() + appId + userId;

        String secret = apiKeyAppIdUserId + md5TokenTime + md5AppSecret;

        String key = Md5Util.md5Hex(secret);
        return key;
    }

    // 校验付款码付款接口加密
    public static boolean authPaymentCode(String paymentCode, String userId, String money, String token, long time, String secret) {
        if (StringUtil.isEmpty(paymentCode)) {
            return false;
        }
        if (StringUtil.isEmpty(userId)) {
            return false;
        }
        if (StringUtil.isEmpty(money)) {
            return false;
        }
        if (StringUtil.isEmpty(token)) {
            return false;
        }
        String secretKey = getPaymentCodeSecret(paymentCode, userId, money, token, time);
        if (secretKey.equals(secret)) {
            log.info("authPaymentCode request -> userId:{}, time:{}, token:{}, secret:{}, paymentCode:{}, money:{}, rightSecretKey:{}",
                    userId, time, token, secret, paymentCode, money, secretKey);
            return true;
        } else {
            return false;
        }
    }

    public static String getPaymentCodeSecret(String paymentCode, String userId, String money, String token, long time) {

        // 付款码付款加密 secret = md5(md5(apiKey+time+money+paymentCode)+userId+token)


        /**
         * md5(apikey+time+money+paymentCode)
         */
        String Apikey_time_money_paymentCode = getApiKey() + time + money + paymentCode;

        String md5Apikey_time_money_paymentCode = Md5Util.md5Hex(Apikey_time_money_paymentCode);
        /**
         * userId+token
         */
        String userId_token = userId + token;

        String secret = md5Apikey_time_money_paymentCode + userId_token;

        String key = Md5Util.md5Hex(secret);
        return key;

    }

    public static boolean authQRCodeReceipt(String userId, String token, String money, long time, String payPassword, String secret) {
        if (StringUtil.isEmpty(userId)) {
            return false;
        }
        if (StringUtil.isEmpty(token)) {
            return false;
        }
        if (StringUtil.isEmpty(money)) {
            return false;
        }
        if (!authRequestTime(time)) {
            return false;
        }
        if (StringUtil.isEmpty(payPassword)) {
            return false;
        }
        String secretKey = getQRCodeReceiptSecret(userId, token, money, time, payPassword);
        if (secretKey.equals(secret)) {
            log.info("authQRCodeReceipt request -> userId:{}, time:{}, token:{}, secret:{}, money:{}, payPassword:{}, rightSecretKey:{}",
                    userId, time, token, secret, money, payPassword, secretKey);
            return true;
        } else {
            return false;
        }
    }

    public static String getQRCodeReceiptSecret(String userId, String token, String money, long time, String payPassword) {
        // 二维码收款加密 secret = md5(md5(apiKey+time+money+payPassword)+userId+token)

        /**
         * md5(apiKey+time+money)
         */
        String apiKey_time_money_payPassword = getApiKey() + time + money + payPassword;

        String md5Apikey_time_money_payPassword = Md5Util.md5Hex(apiKey_time_money_payPassword);

        /**
         * userId_token
         */
        String userId_token = userId + token;

        String secret = md5Apikey_time_money_payPassword + userId_token;

        String key = Md5Util.md5Hex(secret);
        return key;
    }

    public static boolean authPaymentSecret(String userId, String token, String payPassword, long time, String secret) {
        if (StringUtil.isEmpty(userId)) {
            return false;
        }
        if (StringUtil.isEmpty(token)) {
            return false;
        }
        if (!authRequestTime(time)) {
            return false;
        }
        if (StringUtil.isEmpty(payPassword)) {
            return false;
        }
        String secretKey = getPaymentSecret(userId, token, time, payPassword);
        if (secretKey.equals(secret)) {
            log.info("authPaymentSecret request -> userId:{}, time:{}, token:{}, secret:{}, payPassword:{}, rightSecretKey:{}",
                    userId, time, token, secret, payPassword, secretKey);
            return true;
        } else {
            return false;
        }

    }

    public static String getPaymentSecret(String userId, String token, long time, String payPassword) {
        // 付款加密规则
        // md5(userId+token+md5(apiKey+time+payPassword))

        /**
         * userId_token
         */
        String userId_token = userId + token;
        /**
         * md5(apiKey+time+payPassword)
         */
        String apiKey_time_payPassword = apiKey + time + payPassword;

        String Md5ApiKey_time_payPassword = Md5Util.md5Hex(apiKey_time_payPassword);

        String secret = userId_token + Md5ApiKey_time_payPassword;

        String key = Md5Util.md5Hex(secret);
        return key;

    }

    public static boolean authTransactiongetCode(String userId, String token, String salt, String mac, String payPwd) {
        String macValue = getApiKey() + userId + token + salt;
        return mac.equals(MAC.encodeBase64(macValue.getBytes(), payPwd));
    }


    public static boolean checkUserUploadKeySign(String privateKey, String publicKey, String macKey, String payPwd) {
        byte[] priKeyArr = Base64.decode(privateKey);
        byte[] pubKeyArr = Base64.decode(publicKey);
        byte[] macVue = Arrays.copyOf(priKeyArr, priKeyArr.length + pubKeyArr.length);
        System.arraycopy(pubKeyArr, 0, macVue, priKeyArr.length, pubKeyArr.length);
        return macKey.equals(MAC.encodeBase64(macVue, payPwd.getBytes()));
    }

    private static JSONObject decodePayData(String data, byte[] decode) {
        String jsonStr;
        try {
            jsonStr = AES.decryptStringFromBase64(data, decode);
        } catch (Exception e) {
            logger.error("AES 解密失败  ====》  {}", e.getMessage());
            return null;
        }

        JSONObject jsonObj = JSONObject.parseObject(jsonStr);
        logger.info(jsonStr);
        return jsonObj;
    }

    public static JSONObject authSendRedPacketByMac(String userId, String token, String data, String code, String payPwd) {
        byte[] decode = Base64.decode(code);
        JSONObject jsonObj = decodePayData(data, decode);
        if (null == jsonObj)
            return null;
        /*
         *
         * {"moneyStr":"1","toUserId":"10017133","time":"1562833566942",
         * "access_token":"0fdc4014d5c6416aa86a7ce3f496518c",
         * "mac":"y0j1O+FA17UBpZ8wWydKJQ==","count":"1",
         * "greetings":"恭喜发财,万事如意","type":"1"}
         * */

        String mac = jsonObj.getString("mac");
        if (StringUtil.isEmpty(mac))
            return null;

        int type = jsonObj.getIntValue("type");
        int count = jsonObj.getIntValue("count");
        String moneyStr = jsonObj.getString("moneyStr");
        String greetings = jsonObj.getString("greetings");
        String roomJid = jsonObj.getString("roomJid");
        long time = jsonObj.getLongValue("time");

        int toUserId = jsonObj.getIntValue("toUserId");

        StringBuilder secretKey = new StringBuilder();
        //apiKey + 自己的userId +token统一拼接在开头
        secretKey.append(getApiKey()).append(userId).append(token);

        //type + moneyStr + count + greetings + toUserId
        secretKey.append(type).append(moneyStr)
                .append(count).append(greetings);
        if (!StringUtil.isEmpty(roomJid))
            secretKey.append(roomJid);
        else
            secretKey.append(toUserId);
        secretKey.append(time).append(payPwd);

        boolean falg = mac.equals(MAC.encodeBase64(secretKey.toString().getBytes(), decode));
        if (!falg)
            return null;
        jsonObj.put("money", moneyStr);
        /*
         * RedPacket packet=new RedPacket(); packet.setUserId(Integer.valueOf(userId));
         * packet.setCount(count); packet.setType(type); packet.setGreetings(greetings);
         * packet.setMoney(Double.valueOf(moneyStr)); if(!StringUtil.isEmpty(roomJid))
         * packet.setRoomJid(roomJid); else packet.setToUserId(toUserId);
         */

        return jsonObj;
    }

    public static JSONObject authSendTransfer(String userId, String token, String data, String code, String payPwd) {
        byte[] decode = Base64.decode(code);
        JSONObject jsonObj = decodePayData(data, decode);
        if (null == jsonObj)
            return null;
        String mac = jsonObj.getString("mac");
        StringBuilder macStrBuf = new StringBuilder();
        macStrBuf.append(getApiKey()).append(userId).append(token);
        macStrBuf.append(jsonObj.get("toUserId")).append(jsonObj.get("money"));
        if (!StringUtil.isEmpty(jsonObj.getString("remark")))
            macStrBuf.append(jsonObj.getString("remark"));

        macStrBuf.append(jsonObj.get("time")).append(payPwd);
        if (mac.equals(MAC.encodeBase64(macStrBuf.toString().getBytes(), decode))) {
            return jsonObj;
        } else
            return null;
    }

    /*
     * 二维码 收款    扫码 付款
     * */
    public static JSONObject authQrCodeTransfer(String userId, String token, String data, String code, String payPwd) {
        byte[] decode = Base64.decode(code);
        JSONObject jsonObj = decodePayData(data, decode);
        if (null == jsonObj)
            return null;
        String mac = jsonObj.getString("mac");
        StringBuilder macStrBuf = new StringBuilder();
        macStrBuf.append(getApiKey()).append(userId).append(token);

        macStrBuf.append(jsonObj.get("toUserId")).append(jsonObj.get("money"));
        if (!StringUtil.isEmpty(jsonObj.getString("desc")))
            macStrBuf.append(jsonObj.getString("desc"));

        macStrBuf.append(jsonObj.get("time")).append(payPwd);
        if (mac.equals(MAC.encodeBase64(macStrBuf.toString().getBytes(), decode))) {
            return jsonObj;
        } else
            return null;
    }

    /*
     * 商户 下单付款
     * */
    public static JSONObject authOrderPay(String userId, String token, String data, String code, String payPwd) {
        byte[] decode = Base64.decode(code);
        JSONObject jsonObj = decodePayData(data, decode);
        if (null == jsonObj)
            return null;
        String mac = jsonObj.getString("mac");
        StringBuilder macStrBuf = new StringBuilder();
        macStrBuf.append(getApiKey()).append(userId).append(token);

        macStrBuf.append(jsonObj.get("appId")).append(jsonObj.get("prepayId"));
        macStrBuf.append(jsonObj.getString("sign"))
                .append(jsonObj.getString("money"));

        macStrBuf.append(jsonObj.get("time")).append(payPwd);
        if (mac.equals(MAC.encodeBase64(macStrBuf.toString().getBytes(), decode))) {
            return jsonObj;
        } else
            return null;
    }

    /*
     * 微信取现付款
     * */
    public static JSONObject authWxWithdrawalPay(String userId, String token, String data, String code, String payPwd) {
        byte[] decode = Base64.decode(code);
        JSONObject jsonObj = decodePayData(data, decode);
        if (null == jsonObj)
            return null;
        String mac = jsonObj.getString("mac");
        StringBuilder macStrBuf = new StringBuilder();
        macStrBuf.append(getApiKey()).append(userId).append(token);

        macStrBuf.append(jsonObj.get("amount"));

        macStrBuf.append(jsonObj.get("time")).append(payPwd);
        if (mac.equals(MAC.encodeBase64(macStrBuf.toString().getBytes(), decode))) {
            return jsonObj;
        } else
            return null;
    }

    /*
     * 支付宝取现付款
     * */
    public static JSONObject authAliWithdrawalPay(String userId, String token, String data, String code, String payPwd) {
        byte[] decode = Base64.decode(code);
        JSONObject jsonObj = decodePayData(data, decode);
        if (null == jsonObj)
            return null;
        String mac = jsonObj.getString("mac");
        StringBuilder macStrBuf = new StringBuilder();
        macStrBuf.append(getApiKey()).append(userId).append(token);

        macStrBuf.append(jsonObj.get("amount"));

        macStrBuf.append(jsonObj.get("time")).append(payPwd);
        if (mac.equals(MAC.encodeBase64(macStrBuf.toString().getBytes(), decode))) {
            return jsonObj;
        } else
            return null;
    }


}
