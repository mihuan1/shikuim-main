package com.shiku.commons.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串有关的工具类
 * @author Administrator
 *
 */
public class StringUtils {
	
	public boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
   }
	
}
