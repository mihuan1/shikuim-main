package tigase.shiku.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Md5Util {

	public static String md5Hex(String data) {
		try {
			StringBuffer sb = new StringBuffer();
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(data.getBytes());
			byte b[] = digest.digest();

			int i;
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					sb.append("0");
				sb.append(Integer.toHexString(i));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

}
