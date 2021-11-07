package cn.xyz.commons.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class KTaglibUtils {

	public static String format(Long milliseconds, String pattern) {
		Date date = new Date(milliseconds);
		return new SimpleDateFormat(pattern).format(date);
	}

}
