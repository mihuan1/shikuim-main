package cn.xyz.commons.utils;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;




/**
 * DES加密类
 *  采用对称加密
 *  加密后的内容一般采用base64进行传输
 */
/**
 * IvParameterSpec(byte[] iv)
 * iv:具有IV的缓冲区
 * IvParameterSpec(byte[] iv,int offset,int len)
 * iv:
 * offset:iv中的偏移量iv[offset]
 * len:IV字节的数目
 */

/**
 * 1.CBS为工作模式
 * DES一共有电子密码本模式（ECB）、加密分组链接模式（CBC）、加密反馈模式（CFB）和输出反馈模式（OFB）四种模式
 * 2.PKCS5Padding为填充模式
 * 3.cipher.init(ipher.ENCRYPT_MODE, key, zeroIv)，zeroIv为初始化向量
 *
 * 注意:三者缺一不可，如果不指定，程序会调用默认实现，而默认实现与平台有关，
 * 可能导致在客户端中加密的内容与服务器加密的内容不一致
 *
 */
public class DES {
    private static byte[] iv = {1,2,3,4,5,6,7,8};

    public static String encryptDES(String encryptString, String encryptKey) throws Exception {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
        // 加密
        byte[] encryptedData = cipher.doFinal(encryptString.getBytes());
        //System.out.println(new String(encryptedData,"DES"));
        return new String(Base64.getEncoder().encode(encryptedData));
    }

    public static String decryptDES(String decryptString, String decryptKey) throws Exception {
        byte[] byteMi = Base64.getDecoder().decode(decryptString);
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
        // 解密
        byte decryptedData[] = cipher.doFinal(byteMi);
        return new String(decryptedData,"UTF-8");
    }

   public static void main(String[] args) {
	   String data="123456";
     	String timeSend="1536313412";
     	System.out.println(timeSend);
     	String apikey="5e29f483c48848";
     	String messageId="cdf5157133f64b409e69d60c4329bb72";
     	String key=Md5Util.md5Hex(apikey+timeSend+messageId);
     	try {
     		String encrypt = encryptDES(data,key);
           System.out.println(encrypt);
           System.out.println(decryptDES(encrypt,key));
		} catch (Exception e) {
			e.printStackTrace();
		}
   }
}
