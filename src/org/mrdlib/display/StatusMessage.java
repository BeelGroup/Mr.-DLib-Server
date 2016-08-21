package org.mrdlib.display;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class StatusMessage {
	
	private String content;
	private String language;
	
	public StatusMessage(String content, String language) {
		this.content = content;
		this.language = language;
	}
	
	public StatusMessage(String content) {
		this.content = content;
		this.language = "en";
	}
	
	public StatusMessage() {}
	
	public String getContent() {
		return content;
	}
	@XmlValue
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getLanguage() {
		return language;
	}
	@XmlAttribute(name="lang")
	public void setLanguage(String language) {
		this.language = language;
	}

}
