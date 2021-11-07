package cn.xyz.service.config;

import cn.xyz.commons.autoconfigure.BaseProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix="inner.service.config")
public class InnerServiceConfigProperties extends BaseProperties {

	public InnerServiceConfigProperties() {
		// TODO Auto-generated constructor stub
	}

	private String host;
	private String port;
	private String redPacketUriSendRedPacket;
	private String redPacketUriOpenRedPacket;
	private String redPacketUriGetRedPacket;
}
