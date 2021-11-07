package cn.xyz.commons.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 数字 操作的工具类
 * @author lidaye
 *
 */
public class NumberUtil {
	
	

	  private static int ROUND_HALF_UP = BigDecimal.ROUND_HALF_UP;//四舍五入
	    
	  
	  public static void main(String[] args) {
		double d=0.001;
		System.out.println(getCeil(d, 2));
	  }
	  
	  /**
	   * 小数向上取整
	   * d 20.356
	   * n 2
	   *  return 20.36
	   *  
	   * @param d
	   * @param n
	   * @return
	   */
	  public static double getCeil(double d,int n){
		  BigDecimal b = new BigDecimal(String.valueOf(d));
		  b = b.divide(BigDecimal.ONE,n,BigDecimal.ROUND_CEILING);
		  return b.doubleValue();
	  }
	  
	  public static int getRandomByMinAndMax(int min,int max){
			Random rand = new Random();
			int randNum=0;
			if(min==0)
			 randNum = rand.nextInt(max+1);
			else randNum = rand.nextInt(max)+min;
			return randNum;
		}
		 public static int getNum(int start,int end) {  
		        return (int)(Math.random()*(end-start+1)+start);  
		 } 
		
		//整数的四舍五入
		public static int rounding(int num){
			int n = (num+5)/10*10;
			return n;
		}
		/**
		* @Title: percentage
		* @Description: 精确到某位数
		* @param @param current 除数
		* @param @param all 被除数
		* @param @param num  精确到几位
		* @return String    返回类型
		* @throws
		*/
		public static String percentage(long current,long all,int num){
			  NumberFormat nt = NumberFormat.getPercentInstance();  
	          //设置百分数精确度num即保留num位小数  
	          nt.setMinimumFractionDigits(num);  
	          double baifen = (double)current/all;  
			return nt.format(baifen);
		}
	    /**
	     * 将数字字符串转化为BigDecimal
	     * 
	     * @param str 数字字符串
	     * 
	     * @return BigDecimal
	     */
	    public static BigDecimal getBigDecimalForStr(String str){
	        return new BigDecimal(str);
	    }
	    
	    /**
	     * 将数字字符串转化为double,并保留指定小数位
	     * 
	     * @param str 数字字符串
	     * @param scale 指定要保留的小数位(可为空)
	     * 
	     * @return double
	     */
	    public static double getBigDecimalForStrReturnDouble(String str,Integer scale){
	        BigDecimal one = getBigDecimalForStr(str);
	        if(null != scale){
	            return one.setScale(scale, ROUND_HALF_UP).doubleValue();
	        }
	        return one.doubleValue();
	    }
	    
	    /**
	     * 将double类型数字转化为BigDecimal
	     * 
	     * @param str 数字字符串
	     * 
	     * @return BigDecimal
	     */
	    public static BigDecimal getBigDecimalForDouble(double one){
	        return getBigDecimalForStr(one + "");
	    }
	    
	    /**
	     * 获取指定小数位的double
	     * 
	     * @param one 数字
	     * 
	     * @return double
	     */
	    public static double getScaleDouble(double one,Integer scale){
	        return getBigDecimalForStrReturnDouble(one + "", scale);
	    }
	    
	    /**
	     * 获取2位小数点的double
	     * 
	     * @param one 数字
	     * @return double
	     */
	    public static double getScaleDouble(double one){
	        return getScaleDouble(one, 2);
	    }
	    
	    /**
	     * 获取货币格式化字符串(￥#.###.##)
	     * 
	     * @param one 数字
	     * 
	     * @return String
	     */
	    public static String getCurrencyFormat(BigDecimal one){
	        NumberFormat currency = NumberFormat.getCurrencyInstance(); //建立货币格式化引用 
	        return currency.format(one);
	    }
	    
	    /**
	     * 获取货币格式化字符串(￥#.###.##)
	     * 
	     * @param one 数字
	     * 
	     * @return String
	     */
	    public static String getCurrencyFormat(double one){
	        return getCurrencyFormat(getBigDecimalForStr(one + ""));
	    }
	    
	    /**
	     * 两个BigDecimal数字相加
	     * 
	     * @param one 第一个数字
	     * @param two 第二个数字
	     * 
	     * @return BigDecimal
	     */
	    public static BigDecimal add(BigDecimal one,BigDecimal two){
	        return one.add(two);//相加
	    }
	    
	    /**
	     * 两个数字字符串相加
	     * 
	     * @param oneNumber 第一个数字字符串
	     * @param twoNumber 第二个数字字符串
	     * 
	     * @return BigDecimal
	     */
	    public static BigDecimal add(String oneNumber,String twoNumber){
	        BigDecimal one = new BigDecimal(oneNumber);
	        BigDecimal two = new BigDecimal(twoNumber);
	        return add(one, two);
	    }
	    
	    /**
	     * 两个double数字相加
	     * 
	     * @param oneNumber 第一个数字
	     * @param twoNumber 第二个数字
	     * 
	     * @return BigDecimal
	     */
	    public static BigDecimal add(double oneNumber,double twoNumber){
	        return add(oneNumber + "", twoNumber + "");
	    }
	    
	    /**
	     * 两个double数字相加并保留指定小数位
	     * 
	     * @param one 第一个数字
	     * @param two 第二个数字
	     * @param scale 指定要保留的小数位(可为空)
	     * 
	     * @return BigDecimal
	     */
	    public static double add(double one,double two,Integer scale){
	        BigDecimal b = add(one, two);
	        if(null != scale){
	            return b.setScale(scale, ROUND_HALF_UP).doubleValue();
	        }
	        return b.doubleValue();
	    }
	    
	    /**
	     * 两个数字字符串相加并保留指定小数位
	     * 
	     * @param oneNumber 第一个数字字符串
	     * @param twoNumber 第二个数字字符串
	     * @param scale 指定要保留的小数位(可为空)
	     * 
	     * @return double
	     */
	    public static double addReturnDouble(String oneNumber,String twoNumber,Integer scale){
	        BigDecimal b = add(oneNumber, twoNumber);
	        if(null != scale){
	            return b.setScale(scale, ROUND_HALF_UP).doubleValue();
	        }
	        return b.doubleValue();
	    }
	    
	    /**
	     * 两个BigDecimal数字相减
	     * 
	     * @param one 第一个数
	     * @param two 第二个数
	     * @param scale 指定小数位
	     * 
	     * @return BigDecimal
	     */
	    public static BigDecimal subtract(BigDecimal one,BigDecimal two){        
	        return one.subtract(two);//相减
	    }
	    
	    /**
	     * 两个BigDecimal数字相减并保留指定小数位
	     * 
	     * @param one 第一个数
	     * @param two 第二个数
	     * @param scale 指定小数位(可为空)
	     * 
	     * @return double
	     */
	    public static double subtractReturnDouble(BigDecimal one,BigDecimal two,Integer scale){
	        BigDecimal b = subtract(one, two);
	        if(null != scale){
	            return b.setScale(scale, ROUND_HALF_UP).doubleValue();
	        }
	        return b.doubleValue();
	    }
	    
	    /**
	     * 两个数字字符串相减
	     * 
	     * @param oneNumber 第一个数字字符串
	     * @param twoNumber 第二个数字字符串
	     * @param scale 指定小数位
	     * 
	     * @return BigDecimal
	     */
	    public static BigDecimal subtract(String oneNumber,String twoNumber){
	        BigDecimal one = new BigDecimal(oneNumber);
	        BigDecimal two = new BigDecimal(twoNumber);
	        return subtract(one, two);
	    }
	    
	    /**
	     * 两个数字相减并保留指定小数位
	     * 
	     * @param oneNumber 第一个数字
	     * @param twoNumber 第二个数字
	     * @param scale 指定小数位(可为空)
	     * 
	     * @return double
	     */
	    public static double subtractReturnDouble(double oneNumber,double twoNumber,Integer scale){
	        BigDecimal one = new BigDecimal(oneNumber + "");
	        BigDecimal two = new BigDecimal(twoNumber + "");
	        return subtractReturnDouble(one, two, scale);
	    }
	    
	    /**
	     * 获得16个长度的十六进制的UUID
	     * @return UUID
	     */
	    public static synchronized String get16UUID(){
	        UUID id=UUID.randomUUID();
	        String[] idd=id.toString().split("-");
	        return idd[0]+idd[1]+idd[2];
	    }
	    
	    /** 
	     * @Description:是否纯数字 
	    * @param str
	    * @return
	    **/ 
	    public static boolean isNum(String str){
	        Pattern pattern = Pattern.compile("^-?[0-9]+");
	        return (pattern.matcher(str).matches() ? true : false);
	    }
	    
	    
	    /**
	     * @Description:是否正整数
	     */
	    public static boolean isNumeric(String string){
	        Pattern pattern = Pattern.compile("[0-9]*");
	        return pattern.matcher(string).matches();   
	    }
}
