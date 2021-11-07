package com.shiku.commons.vo;

public enum FileType {
	Image("image"), Audio("audio"), Video("video"),Music("music"),MusicPhoto("music"), Other("other");

	private String baseName;

	private FileType(String baseName) {
		this.baseName = baseName;
	}

	public String getBaseName() {
		return baseName;
	}
}
