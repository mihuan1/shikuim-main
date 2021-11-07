package cn.xyz.commons.utils;

import java.util.HashMap;

public final class MapUtil extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	public static HashMap<String, Object> newMap(String key, Object value) {
		return new MapUtil(key, value);
	}

	public MapUtil() {
		super();
	}

	public MapUtil(String key, Object value) {
		super();
		put(key, value);
	}
	
}
