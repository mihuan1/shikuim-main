package com.shiku.mianshi.filter;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "authorizationfilter")
public class AuthorizationFilterProperties {

	private List<String> requestUriList;

	public List<String> getRequestUriList() {
		return requestUriList;
	}

	public void setRequestUriList(List<String> requestUriList) {
		this.requestUriList = requestUriList;
	}

}
