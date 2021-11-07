package cn.xyz.commons.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Create by zq
 * DES加密类
 * 采用对称加密
 * 加密后的内容一般采用base64进行传输
 * <p>
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
 * <p>
 * 注意:三者缺一不可，如果不指定，程序会调用默认实现，而默认实现与平台有关，
 * 可能导致在客户端中加密的内容与服务器加密的内容不一致
 * 
 * 客户端 服务端 统一加密格式("DESede/CBC/PKCS5Padding") 测试
 */
public class DESTest {

    private static byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8};

    /**
     * DESede 要求密钥长度为128 || 192 bits 因此我们需要对生成的密钥进行截取在加解密
     * 但是ios端并未对密钥进行截取(即ios端用长度为三十多个字节的密钥进行加解密，也能够解密Android端发送过去的消息)
     * 同时，Android用截取的密钥解密ios端发送过来的消息，也能正常解密
     * 结论：ios端内部可能对密钥进行了处理，根据加密的方式内部进行了截取
     */
    public static String encryptDES(String encryptString, String encryptKey) throws Exception {
        encryptKey = encryptKey.substring(0, 24);
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
        // 加密
        byte[] encryptedData = cipher.doFinal(encryptString.getBytes());
//        return Base64.encode(encryptedData);
        return new String(Base64.getEncoder().encode(encryptedData));
    }

    public static String decryptDES(String decryptString, String decryptKey) throws Exception {
        decryptKey = decryptKey.substring(0, 24);
//        byte[] byteMi = new Base64().decode(decryptString);
//        byte[] byteMi = new Base64().decode(decryptString);
        byte[] byteMi = Base64.getDecoder().decode(decryptString);
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
        // 解密
        byte decryptedData[] = cipher.doFinal(byteMi);
        return new String(decryptedData);
    }
    
    public static void main(String[] args) {
    	String data="查查自己";
     	String timeSend="1560320005";
     	System.out.println(timeSend);
     	String apikey="5e29f483c48848";
     	String messageId="0cf47751172c40cb9512aa99b7b08f0d";
     	String key=Md5Util.md5Hex(apikey+timeSend+messageId);
     	try {
     		String encrypt = encryptDES(data,key);
           System.out.println(encrypt);
           System.out.println(decryptDES(encrypt,key));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /*
    Todo 之前的Key写死为12345678，现在Key为一个变量，且长度大于8 So 换一种加解密写法

    Todo 将DES修改为DESede(3des)加密之后，下面的加解密都失败了，将上面的方法重新启用并微调(截取密钥、修改算法)
     */
/*
    private static final String DES = "DES";
    private static final String ENCODE = "UTF-8";

    private static final byte[] ivByte = {1, 2, 3, 4, 5, 6, 7, 8};
    // 算法名称/加密模式/填充方式
    private static final String CIPHER_ALGORITHM = "DES/CBC/PKCS5Padding";

    public static String encryptDES(String encryptString, String encryptKey) throws Exception {
        byte[] bytes = encrypt(encryptString.getBytes(ENCODE), encryptKey.getBytes(ENCODE));
        String str1 = android.util.Base64.encodeToString(bytes, 0);
        String str2 = str1.replace("\n", "");// 不知道为什么最后总是带有"\n"
        return str2;
    }

    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 初始化向量
        IvParameterSpec iv = new IvParameterSpec(ivByte);

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec desKeySpec = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);

        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        return cipher.doFinal(data);
    }

    public static String decryptDES(String decryptString, String decryptKey) throws Exception {
        byte[] bytes = android.util.Base64.decode(decryptString, 0);
        byte[] decryptBytes = decrypt(bytes, decryptKey.getBytes(ENCODE));
        return new String(decryptBytes, ENCODE);
    }

    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 初始化向量
        IvParameterSpec iv = new IvParameterSpec(ivByte);

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec desKeySpec = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

        return cipher.doFinal(data);
    }
*/

}
