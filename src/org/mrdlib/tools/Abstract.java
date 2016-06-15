package org.mrdlib.tools;

public class Abstract {
	private String content;
	private String language;

	public Abstract(String content, String language) {
		this.content = content;
		this.language = language;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

}
