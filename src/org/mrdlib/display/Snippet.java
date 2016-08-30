package org.mrdlib.display;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
/**
 * 
 * @author Millah
 * 
 * This class handles the representation of the snippet part of the Document.java and serves as a wrapper.
 * The XML format is automatically generated through the class structure.
 *
 */
public class Snippet {
	private String content;
	private String format;
	
	public Snippet(String title, String authorNames, String publishedIn, int year, String format) {
		this.content = formatContent(title, authorNames, publishedIn, year);
		this.format = format;
	}
	
	//put it in XML format
	private String formatContent(String title, String authorNames, String publishedIn, int year) {
		return "<span class='mdl-title'>" + title + "</span>. <span class='mdl-authors'>"
				+ authorNames + "</span>. <span class='mdl-journal'>" + publishedIn
				+ "</span>. <span class='mdl-volume_and_number>6:66</span>. <span class='mdl-year'>"
				+ year + "</span>";
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
