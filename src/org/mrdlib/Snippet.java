package org.mrdlib;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class Snippet {
	private String content;
	private String format;
	
	public Snippet(String content, String format) {
		this.content = content;
		this.format = format;
	}

	public String getContent() {
		return content;
	}
	@XmlValue
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getFormat() {
		return format;
	}
	@XmlAttribute(name="format")
	public void setFormat(String format) {
		this.format = format;
	}
}
