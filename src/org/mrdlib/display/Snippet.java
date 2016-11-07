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

	/**
	 * 
	 * creates a snippet where css-classes are given for customization
	 * 
	 * @param title
	 * @param authorsNames
	 * @param publishedIn
	 * @param year
	 * @return snippet as String
	 */
	private String formatContentHtmlCss(String title, String authorNames, String publishedIn, int year) {
		return "<![CDATA[<span class='mdl-title'>" + title + "</span>. <span class='mdl-authors'>" + authorNames
				+ "</span>. <span class='mdl-journal'>" + publishedIn + "</span>. <span class='mdl-year'>" + year
				+ "</span>]]>";
	}

	/**
	 * 
	 * creates a snippet where the css is already given
	 * 
	 * @param title
	 * @param authorsNames
	 * @param publishedIn
	 * @param year
	 * @return snippet as String
	 */
	private String formatContentHtmlFully(String title, String authorNames, String publishedIn, int year) {
		return "<![CDATA[<a href='" + clickUrl + "'><font color='#000000' size='5' face='Arial, Helvetica, sans-serif'>" + title
				+ ".</font></a><font color='#000000' size='5' face='Arial, Helvetica, sans-serif'>" + authorNames
				+ ". <i>" + publishedIn + "</i>. " + year + ".</font>]]>";
	}

	/**
	 * 
	 * creates a snippet without any css
	 * 
	 * @param title
	 * @param authorsNames
	 * @param publishedIn
	 * @param year
	 * @return snippet as String
	 */
	private String formatContentHtmlPlain(String title, String authorNames, String publishedIn, int year) {
		return "<![CDATA[<a href='" + clickUrl + "'>" + title + "</a>. " + authorNames + ". " + publishedIn + ".  " + year + ".]]>";
	}

	/**
	 * 
	 * set the clickURL, also in the already build content
	 * 
	 * @param click URL
	 */
	@XmlTransient
	public void setClickUrl(String clickUrl) {
		this.clickUrl = clickUrl;
		if(content.contains("<a href="))
			this.content = content.substring(0, content.lastIndexOf("<a href='")+9) + clickUrl + content.substring(content.lastIndexOf("<a href='")+9);
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
