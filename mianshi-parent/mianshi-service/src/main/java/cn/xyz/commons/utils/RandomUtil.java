package cn.xyz.commons.utils;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/** 
* @author: hsg 
* @date: 2018年7月31日 下午5:24:14 
* @version: 1.0  
* @Description:  用于产生随机字符、昵称等的工具类
*/
public final class RandomUtil {
	
		

		/**
		 * 生成指定范围的随机数
		 * 
		 */
		public static Integer getRandomNum(int min,int max) {
			Random random = new Random();
			int s = random.nextInt(max)%(max-min+1) + min;
			return s;
		}
	
        /**    
                *生成随机的中文组合
         * @param len 需要生成的文字长度
         * @return   
         */
	    public static String getRandomZh(int len) {
	        String ret = "";
	        for (int i = 0; i < len; i++) {
	            String str = null;
	           int hightPos, lowPos; // 定义高低位
	            Random random = new Random();
	            hightPos = (176 + Math.abs(random.nextInt(39))); // 获取高位值
	            lowPos = (161 + Math.abs(random.nextInt(93))); // 获取低位值
	            byte[] b = new byte[2];
	            b[0] = (new Integer(hightPos).byteValue());
	            b[1] = (new Integer(lowPos).byteValue());
	            try {
	                str = new String(b, "GBK"); // 转成中文
	            } catch (UnsupportedEncodingException ex) {
	                ex.printStackTrace();
	            }
	            ret += str;
	        }
	        return ret;
	   }
	    
	    
    
	  /**
	   * 生成随机用户名，字母和数字的组合  
	   * @param length  生成的字符长度
	   * @return
	   */
	  public static String getRandomEnAndNum(int length) {  
	             
	   String val = "";  
	   Random random = new Random();  
	               
	     //参数length，表示生成几位随机数  
	     for(int i = 0; i < length; i++) {  
	          
	         String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";  
	         //输出字母还是数字  
	         if( "char".equalsIgnoreCase(charOrNum) ) {  
	             //输出是大写字母还是小写字母  
	             int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;  
	             val += (char)(random.nextInt(26) + temp);  
	         } else if( "num".equalsIgnoreCase(charOrNum) ) {  
	             val += String.valueOf(random.nextInt(10));  
	         }  
	     }  
	     return val;  
	  }        
	
	

	  /**
	   * 不重复字符生成器（通过一个不重复的数生成一个不重复的随机字符）算法原理：
	   * 1) 获取id: 1127738   2) 使用自定义进制转为：gpm6   3) 转为字符串，并在后面加'o'字符：gpm6o   4）在后面随机产生若干个随机数字字符：gpm6o7 
	    * 转为自定义进制后就不会出现o这个字符，然后在后面加个'o'，这样就能确定唯一性。最后在后面产生一些随机字符进行补全。
	   * @author hsg
	   */
	  /** 自定义进制(0,1没有加入,容易与o,l混淆) */
	  private static final char[] r=new char[]{'q', 'w', 'e', '8', 'a', 's', '2', 'd', 'z', 'x', '9', 'c', '7', 'p', '5', 'i', 'k', '3', 'm', 'j', 'u', 'f', 'r', '4', 'v', 'y', 'l', 't', 'n', '6', 'b', 'g', 'h'};
	  /** (不能与自定义进制有重复) */
	  private static final char b='o';
      /** 进制长度 */
      private static final int binLen=r.length;
      /** 序列最小长度 */
      private static final int s=6;

      /**
	      * 根据ID生成六位随机码
	   * @param id 不重复的数
	   * @return 不重复的随机码
       */
      public static String idToSerialCode(long id) {
          char[] buf=new char[32];
          int charPos=32;

          while((id / binLen) > 0) {
              int ind=(int)(id % binLen);
              buf[--charPos]=r[ind];
              id /= binLen;
          }
          buf[--charPos]=r[(int)(id % binLen)];
          String str=new String(buf, charPos, (32 - charPos));
          // 不够长度的自动随机补全
          if(str.length() < s) {
              StringBuilder sb=new StringBuilder();
              sb.append(b);
              Random rnd=new Random();
              for(int i=1; i < s - str.length(); i++) {
              sb.append(r[rnd.nextInt(binLen)]);
              }
              str+=sb.toString();
          }
          return str;
      }
      
      
      //邀请码转为id
      public static long codeToId(String code) {
          char chs[]=code.toCharArray();
          long res=0L;
          for(int i=0; i < chs.length; i++) {
              int ind=0;
              for(int j=0; j < binLen; j++) {
                  if(chs[i] == r[j]) {
                      ind=j;
                      break;
                  }
              }
              if(chs[i] == b) {
                  break;
              }
              if(i > 0) {
                  res=res * binLen + ind;
              } else {
                  res=ind;
              }
              // System.out.println(ind + "-->" + res);
          }
          return res;
      }

	  
	  
	  
	
	
}
