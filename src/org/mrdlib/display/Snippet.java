package org.mrdlib.display;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
/**
 * 
 * @author Millah
 * 
 * This class handles the presentation of the snippet part of the Document.java and serves as a wrapper.
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
		return "&lt;span class='mdl-title'&gt;" + title + "&lt;/span&gt;. &lt;span class='mdl-authors'&gt;"
				+ authorNames + "&lt;/span&gt;. &lt;span class='mdl-journal'&gt;" + publishedIn
				+ "&lt;/span&gt;. &lt;span class='mdl-volume_and_number'&gt;6:66&lt;/span&gt;. &lt;span class='mdl-year'&gt;"
				+ year + "&lt;/span&gt;";
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
