package org.mrdlib.display;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

/**
 * 
 * @author Millah
 * 
 *         This class handles the representation of the snippet part of the
 *         Document.java and serves as a wrapper. The XML format is
 *         automatically generated through the class structure.
 *
 */
public class Snippet implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String content;
	private String format;
	private String clickUrl;

	public Snippet(String clickUrl, String title, String authorNames, String publishedIn, int year, String format) {
		this.format = format;
		this.clickUrl = clickUrl;

		if (format.equals("html_plain"))
			this.content = formatContentHtmlPlain(title, authorNames, publishedIn, year);
		else if (format.equals("html_fully_formatted"))
			this.content = formatContentHtmlFully(title, authorNames, publishedIn, year);
		else {
			this.content = formatContentHtmlCss(title, authorNames, publishedIn, year);
			this.format = "html_and_css";
		}
	}

	// put it in XML format
	private String formatContentHtmlCss(String title, String authorNames, String publishedIn, int year) {
		return "<span class='mdl-title'>" + title + "</span>. <span class='mdl-authors'>" + authorNames
				+ "</span>. <span class='mdl-journal'>" + publishedIn + "</span>. <span class='mdl-year'>" + year
				+ "</span>";
	}

	private String formatContentHtmlFully(String title, String authorNames, String publishedIn, int year) {
		return "<a href=" + clickUrl + "><font color='#000000' size='3' face='Arial, Helvetica, sans-serif'>" + title
				+ ".</font></a><font color='#000000' size='3' face='Arial, Helvetica, sans-serif'>" + authorNames
				+ ". <i>" + publishedIn + "</i>. " + year + ".</font>";
	}

	private String formatContentHtmlPlain(String title, String authorNames, String publishedIn, int year) {
		return "<a href=" + clickUrl + ">" + title + "</a>. " + authorNames + ". " + publishedIn + ".  " + year + ".";
	}

	@XmlTransient
	public void setClick_url(String clickUrl) {
		this.clickUrl = clickUrl;
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

	@XmlAttribute(name = "format")
	public void setFormat(String format) {
		this.format = format;
	}
}
