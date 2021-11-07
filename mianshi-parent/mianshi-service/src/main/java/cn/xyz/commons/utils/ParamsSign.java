package cn.xyz.commons.utils;

import java.net.URLEncoder;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * HTTP请求参数签名
 * 
 * @author luorc
 * 
 */
public class ParamsSign {

	private static char[] base64EncodeChars = { 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
			't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', '+', '/' };

	public static String value(TreeMap<String, String> params, String app_secret) {
		try {
			String signStr = "";
			for (String pName : params.keySet()) {
				signStr = signStr + "&" + pName + "="
						+ (String) params.get(pName);
			}
			byte[] hmacSHA1Bytes = hmacSHA1Encrypt(signStr.substring(1),
					app_secret);
			return URLEncoder.encode(encryptBASE64(hmacSHA1Bytes), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "error";
	}

	private static byte[] hmacSHA1Encrypt(String encryptText, String encryptKey)
			throws Exception {
		String MAC_NAME = "HmacSHA1";
		String ENCODING = "UTF-8";
		byte[] data = encryptKey.getBytes(ENCODING);
		SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
		Mac mac = Mac.getInstance(MAC_NAME);
		mac.init(secretKey);
		byte[] text = encryptText.getBytes(ENCODING);
		return mac.doFinal(text);
	}

	private static String encryptBASE64(byte[] key) throws Exception {
		return new String(base64Encode(key));
	}

	private static String base64Encode(byte[] data) {
		StringBuffer sb = new StringBuffer();
		int len = data.length;
		int i = 0;

		while (i < len) {
			int b1 = data[(i++)] & 0xFF;
			if (i == len) {
				sb.append(base64EncodeChars[(b1 >>> 2)]);
				sb.append(base64EncodeChars[((b1 & 0x3) << 4)]);
				sb.append("==");
				break;
			}
			int b2 = data[(i++)] & 0xFF;
			if (i == len) {
				sb.append(base64EncodeChars[(b1 >>> 2)]);
				sb.append(base64EncodeChars[((b1 & 0x3) << 4 | (b2 & 0xF0) >>> 4)]);
				sb.append(base64EncodeChars[((b2 & 0xF) << 2)]);
				sb.append("=");
				break;
			}
			int b3 = data[(i++)] & 0xFF;
			sb.append(base64EncodeChars[(b1 >>> 2)]);
			sb.append(base64EncodeChars[((b1 & 0x3) << 4 | (b2 & 0xF0) >>> 4)]);
			sb.append(base64EncodeChars[((b2 & 0xF) << 2 | (b3 & 0xC0) >>> 6)]);
			sb.append(base64EncodeChars[(b3 & 0x3F)]);
		}
		return sb.toString();
	}
}