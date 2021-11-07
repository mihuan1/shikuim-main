package cn.xyz.commons.utils;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 *  DES加密 解密算法
 * @author lidaye
 *
 */
public class DesUtil {

	private final static String DES = "DES";
    private final static String ENCODE = "UTF-8";
  
    private final static  byte[] ivByte= {1,2,3,4,5,6,7,8};
    // 算法名称/加密模式/填充方式    
    public static final String CIPHER_ALGORITHM = "DES/CBC/PKCS5Padding";  

	//主要看填充方式 PKCS5Padding  PKCS7Padding  还有其他的填充方式没用过
	// public static final String CIPHER_ALGORITHM_CBC = "DES/CBC/ZerosPadding";    

    public static void main(String[] args) throws Exception {
    	String data="123456";
     	String timeSend="1536313412";
     	System.out.println(timeSend);
     	String apikey="5e29f483c48848";
     	String messageId="cdf5157133f64b409e69d60c4329bb72";
     	String key=Md5Util.md5Hex(apikey+timeSend+messageId);
     	try {
     		String encrypt = encrypt(data,key);
           System.out.println(encrypt);
           System.out.println(decrypt(encrypt,key));
		} catch (Exception e) {
			e.printStackTrace();
		}

    }

   

    /**
     * Description 根据键值进行加密
     * 
     * @param data
     * @param key
     *            加密键byte数组
     * @return
     * @throws Exception
     */
    public static String encrypt(String data, String key) throws Exception {
        byte[] bt = encrypt(data.getBytes(ENCODE), key.getBytes(ENCODE));
        return  new String(Base64.getEncoder().encode(bt));
    }

    /**
     * Description 根据键值进行解密
     * 
     * @param data
     * @param key
     *            加密键byte数组
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String data, String key) throws IOException,
            Exception {
        if (data == null)
            return null;
        byte[] buf = Base64.getDecoder().decode(data);
        byte[] bt = decrypt(buf, key.getBytes(ENCODE));
        return new String(bt, ENCODE);
    }

    /**
     * Description 根据键值进行加密
     * 
     * @param data
     * @param key
     *            加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
    	
        // 初始化向量
        IvParameterSpec iv = new IvParameterSpec(ivByte);

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, iv);

        return cipher.doFinal(data);
    }

    /**
     * Description 根据键值进行解密
     * 
     * @param data
     * @param key
     *            加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
    	 // 初始化向量
        IvParameterSpec iv = new IvParameterSpec(ivByte);

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, iv);

        return cipher.doFinal(data);
    }
}
