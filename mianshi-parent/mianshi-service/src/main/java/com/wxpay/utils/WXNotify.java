package com.wxpay.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WXNotify {
	
	protected void notify(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		
		//把如下代码贴到的你的处理回调的servlet 或者.do 中即可明白回调操作
		System.out.print("微信支付回调数据开始");
		//示例报文
//		String xml = "<xml><appid><![CDATA[wxb4dc385f953b356e]]></appid><bank_type><![CDATA[CCB_CREDIT]]></bank_type><cash_fee><![CDATA[1]]></cash_fee><fee_type><![CDATA[CNY]]></fee_type><is_subscribe><![CDATA[Y]]></is_subscribe><mch_id><![CDATA[1228442802]]></mch_id><nonce_str><![CDATA[1002477130]]></nonce_str><openid><![CDATA[o-HREuJzRr3moMvv990VdfnQ8x4k]]></openid><out_trade_no><![CDATA[1000000000051249]]></out_trade_no><result_code><![CDATA[SUCCESS]]></result_code><return_code><![CDATA[SUCCESS]]></return_code><sign><![CDATA[1269E03E43F2B8C388A414EDAE185CEE]]></sign><time_end><![CDATA[20150324100405]]></time_end><total_fee>1</total_fee><trade_type><![CDATA[JSAPI]]></trade_type><transaction_id><![CDATA[1009530574201503240036299496]]></transaction_id></xml>";
		String inputLine;
		String notityXml = "";
		String resXml = "";

		try {
			while ((inputLine = request.getReader().readLine()) != null) {
				notityXml += inputLine;
			}
			request.getReader().close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("接收到的报文：" + notityXml);
		
		Map m = parseXmlToList2(notityXml);
		WxPayResult wpr = new WxPayResult();
		wpr.setAppid(m.get("appid").toString());
		wpr.setBankType(m.get("bank_type").toString());
		wpr.setCashFee(m.get("cash_fee").toString());
		wpr.setFeeType(m.get("fee_type").toString());
		wpr.setIsSubscribe(m.get("is_subscribe").toString());
		wpr.setMchId(m.get("mch_id").toString());
		wpr.setNonceStr(m.get("nonce_str").toString());
		wpr.setOpenid(m.get("openid").toString());
		wpr.setOutTradeNo(m.get("out_trade_no").toString());
		wpr.setResultCode(m.get("result_code").toString());
		wpr.setReturnCode(m.get("return_code").toString());
		wpr.setSign(m.get("sign").toString());
		wpr.setTimeEnd(m.get("time_end").toString());
		wpr.setTotalFee(m.get("total_fee").toString());
		wpr.setTradeType(m.get("trade_type").toString());
		wpr.setTransactionId(m.get("transaction_id").toString());
		
		if("SUCCESS".equals(wpr.getResultCode())){
			//支付成功
			resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
			+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
		}else{
			resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
			+ "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
		}

		System.out.println("微信支付回调数据结束");

		BufferedOutputStream out = new BufferedOutputStream(
				response.getOutputStream());
		out.write(resXml.getBytes());
		out.flush();
		out.close();

	}
	
	/**
	 * description: 解析微信通知xml
	 * 
	 * @param xml
	 * @return
	 * @author ex_yangxiaoyi
	 * @see
	 */
	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	public static Map parseXmlToList2(String strXML)  throws Exception{
		Map data = new HashMap<>();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		String FEATURE = null;
	
		   try {

			      FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
			      documentBuilderFactory.setFeature(FEATURE, true);

			      FEATURE = "http://xml.org/sax/features/external-general-entities";
			      documentBuilderFactory.setFeature(FEATURE, false);

			      FEATURE = "http://xml.org/sax/features/external-parameter-entities";
			      documentBuilderFactory.setFeature(FEATURE, false);

			      FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
			      documentBuilderFactory.setFeature(FEATURE, false);

			      documentBuilderFactory.setXIncludeAware(false);
			      documentBuilderFactory.setExpandEntityReferences(false);

			   } catch (ParserConfigurationException e) {
			      System.out.println("ParserConfigurationException was thrown. The feature '" +
			            FEATURE + "' is probably not supported by your XML processor.");

			   }
			   DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
			   InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
			   org.w3c.dom.Document doc = documentBuilder.parse(stream);
			   doc.getDocumentElement().normalize();
			   NodeList nodeList = doc.getDocumentElement().getChildNodes();
			   for (int idx=0; idx<nodeList.getLength(); ++idx) {
			      Node node = nodeList.item(idx);
			      if (node.getNodeType() == Node.ELEMENT_NODE) {
			         org.w3c.dom.Element element = (org.w3c.dom.Element) node;
			         data.put(element.getNodeName(), element.getTextContent());
			      }
			   }
			   try {
			      stream.close();
			   }
			   catch (Exception ex) {

			   }
			   return data;
			}

}
