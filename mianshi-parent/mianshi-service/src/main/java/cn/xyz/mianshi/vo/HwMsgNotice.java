package cn.xyz.mianshi.vo;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @Description: TODO(华为消息推送)
 * @author zhm
 * @date 2019年1月12日 下午4:56:23
 * @version V1.0
 */
@Getter
@Setter
public class HwMsgNotice {
	private String token;
	private MsgNotice msgNotice;
	
	public HwMsgNotice() {
		// TODO Auto-generated constructor stub
	}

	public HwMsgNotice(String token, MsgNotice msgNotice) {
		super();
		this.token = token;
		this.msgNotice = msgNotice;
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
