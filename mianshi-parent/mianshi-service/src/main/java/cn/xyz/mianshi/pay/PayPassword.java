package cn.xyz.mianshi.pay;

import com.shiku.utils.encrypt.DES;
import com.shiku.utils.encrypt.MD5;

/**
 * 所有参与的6字节支付密码明文都通过位运算转换成128位16字节的不可打印字节数组，
 * 要求是把6字节可打印字符串转成16字节不可打印字节数组，
 */
public class PayPassword {
    public static byte[] encode(String payPassword) {
        return MD5.encrypt(payPassword);
    }

    /**
     * 在服务器保存的支付密码md5,
     *
     * @param userId 用户自己的userId,
     * @param key    {@link PayPassword#encode(java.lang.String)}不可打印的支付密码,
     */
    public static String md5(String userId, byte[] key) {
        return MD5.encryptHex(DES.encrypt(userId.getBytes(), key));
    }
}
