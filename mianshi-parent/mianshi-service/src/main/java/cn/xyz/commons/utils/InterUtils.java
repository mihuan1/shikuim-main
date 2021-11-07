package cn.xyz.commons.utils;

/**
 * Created by Administrator on 2017/9/22.
 */

public class InterUtils {

    /**
     * 加密
     *
     * @param input 加密前的字符
     * @return 加密后的字符
     */
    public static String encrypt(int time,String input) {
      int pwd = 0 ;
        char[] chars = input.toCharArray();
       // int time=pwdTime(System.currentTimeMillis());
        char k;
        int dk;
        for (char aChar : chars) {
             k = aChar;
             dk = k^time;
            pwd +=  dk;
        }

        return String.valueOf(pwd);
    }



    public static int pwdTime(long time) {
        int pwd = (int) ((time) % 19999);

        return pwd;
    }
    
    public static boolean verifyToken(long inTime ,String token,String url){
    	int time=pwdTime(inTime);
    	String encryptStr=encrypt(time, url.substring(1));
    	return token.equals(encryptStr);
    }
}
