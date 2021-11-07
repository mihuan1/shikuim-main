package cn.xyz.commons.utils;

public class ValueUtil {

	public static Integer parse(Integer value) {
		return null == value ? 0 : value;
	}

	public static Long parse(Long value) {
		return null == value ? 0 : value;
	}

	public static String parse(String value) {
		return null == value || "".equals(value.trim()) ? "" : value;
	}

}
