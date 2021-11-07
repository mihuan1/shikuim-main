package tigase.shiku.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class FileUtil {

	public static String readAll(InputStream in) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		StringBuffer sb = new StringBuffer();
		String ln = null;

		while (null != (ln = reader.readLine()))
			sb.append(ln);

		return sb.toString();
	}

	public static String readAll(InputStream in, String charsetName) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetName));
		StringBuffer sb = new StringBuffer();
		String ln = null;

		while (null != (ln = reader.readLine()))
			sb.append(ln);

		return sb.toString();
	}

	public static String readAll(BufferedReader reader) {
		try {
			StringBuffer sb = new StringBuffer();
			String ln = null;

			while (null != (ln = reader.readLine()))
				sb.append(ln);

			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	
}
