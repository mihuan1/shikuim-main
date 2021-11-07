package com.shiku.mianshi;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class ResponseUtil {

	public static void output(ServletResponse response, String text) {
		output((HttpServletResponse) response, text);
	}

	public static void output(HttpServletResponse response, String text) {
		try {
			HttpServletResponse t = (HttpServletResponse) response;
			t.setHeader("Access-Control-Allow-Origin", "*");
			t.setHeader("Access-Control-Allow-Methods",
					"POST, GET, OPTIONS, DELETE");
			t.setHeader("Access-Control-Max-Age", "3600");
			t.setHeader("Access-Control-Allow-Headers", "x-requested-with");

			response.setContentType("application/json; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(text);
			response.getWriter().flush();
			response.getWriter().close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
