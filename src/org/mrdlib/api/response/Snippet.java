package org.mrdlib.api.response;

import java.io.Serializable;
import java.util.Formatter;

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
		String formattedTitle = title != null ? "<span class='mdl-title'>" + title + "</span>. " : ""; 
		String formattedAuthors = authorNames != null ? "<span class='mdl-authors'>" + authorNames + "</span>. "  : ""; 
		String formattedPublishedIn = publishedIn != null ? "<span class='mdl-journal'>" + publishedIn + "</span>. "  : "";
		String formattedYear = year > 0 ? "<span class='mdl-year'>" + year	+ "</span>." : "";
		
		return constructCDATAString(formattedTitle, formattedAuthors, formattedPublishedIn, formattedYear);
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

		//Should use String formatter, separate this presentation out to CSS, or separate View code out completely
		String fontHTMLTag = "<font color='#000000' size='4' face='Arial, Helvetica, sans-serif'>";		

		String formattedTitle = title != null ? "<a href='" + clickUrl + "'>" + fontHTMLTag + title + "</font></a>. " : ""; 
		String formattedAuthors = authorNames != null ? fontHTMLTag + authorNames + ". "  : fontHTMLTag; 
		String formattedPublishedIn = publishedIn != null ? "<i>" + publishedIn + "</i>. "  : "";
		String formattedYear = year > 0 ? year + "</font>" : "</font>";

		return constructCDATAString(formattedTitle, formattedAuthors, formattedPublishedIn, formattedYear);	
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
		String formattedTitle = title != null ? "<a href='" + clickUrl + "'>" + title + "</a>. " : ""; 
		String formattedAuthors = authorNames != null ? authorNames + ". "  : ""; 
		String formattedPublishedIn = publishedIn != null ? publishedIn + ". "  : "";
		String formattedYear = year > 0 ? year	+ "." : "";
		
		return constructCDATAString(formattedTitle, formattedAuthors, formattedPublishedIn, formattedYear);	
	}

	private String constructCDATAString(String title, String authorNames, String publishedIn, String year){
		return "<![CDATA[" + title + authorNames + publishedIn + year + "]]>";
	}
	
	/**
	 * 
	 * set the clickURL, also in the already build content
	 * 
	 * @param click
	 *            URL
	 */
	@XmlTransient
	public void setClickUrl(String clickUrl) {
		this.clickUrl = clickUrl;
		if (content.contains("<a href="))
			this.content = content.substring(0, content.lastIndexOf("<a href='") + 9) + clickUrl
					+ content.substring(content.lastIndexOf("<a href='") + 9);
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
