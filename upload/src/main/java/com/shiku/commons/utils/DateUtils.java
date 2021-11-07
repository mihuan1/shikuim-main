package com.shiku.commons.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtils {

	public static String getYMString() {
		return new SimpleDateFormat("yyyyMM").format(new Date());
	}

	public static String getYMDString() {
		return new SimpleDateFormat("yyyyMMdd").format(new Date());
	}
}
