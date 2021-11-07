package com.wxpay.utils;

import java.util.Map;
import java.util.Map.Entry;

import cn.xyz.commons.utils.CollectionUtil;
import cn.xyz.commons.utils.StringUtil;  

/**
 * 创建时间：2016年11月3日 上午11:40:17
 * 
 * @author andy
 * @version 2.2
 */

public class XmlUtil {

	private static final String PREFIX_XML = "<xml>";

	private static final String SUFFIX_XML = "</xml>";

	private static final String PREFIX_CDATA = "<![CDATA[";

	private static final String SUFFIX_CDATA = "]]>";

	/**
	 * 转化成xml, 单层无嵌套
	 * 
	 * @param map
	 * @param isAddCDATA
	 * @return
	 */
	public static String xmlFormat(Map<String, String> parm, boolean isAddCDATA) {

		StringBuffer strbuff = new StringBuffer(PREFIX_XML);
		if (CollectionUtil.isNotEmpty(parm)) {
			for (Entry<String, String> entry : parm.entrySet()) {
				strbuff.append("<").append(entry.getKey()).append(">");
				if (isAddCDATA) {
					strbuff.append(PREFIX_CDATA);
					if (!StringUtil.isEmpty(entry.getValue())) {
						strbuff.append(entry.getValue());
					}
					strbuff.append(SUFFIX_CDATA);
				} else {
					if (!StringUtil.isEmpty(entry.getValue())) {
						strbuff.append(entry.getValue());
					}
				}
				strbuff.append("</").append(entry.getKey()).append(">");
			}
		}
		return strbuff.append(SUFFIX_XML).toString();
	}

}
