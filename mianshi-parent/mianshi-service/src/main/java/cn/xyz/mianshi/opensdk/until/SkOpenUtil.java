package cn.xyz.mianshi.opensdk.until;

import org.apache.commons.codec.digest.DigestUtils;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.NumberUtil;

public class SkOpenUtil {
	
	public static String getAppId(){
		final String sk = "sk";
		return sk + NumberUtil.get16UUID();
	}
	
	// 暂定这样
	public static String getAppScrect(String appId){
		return DigestUtils.md5Hex(appId+DateUtil.currentTimeSeconds());
	}

}
