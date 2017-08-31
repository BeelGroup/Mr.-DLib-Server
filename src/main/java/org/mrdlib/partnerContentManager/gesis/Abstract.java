package org.mrdlib.partnerContentManager.gesis;

/**
 * 
 * @author Millah
 *
 * a class which wraps the structure of an abstract (containing content and language)
 *
 */
public class Abstract {
	private String content;
	private String language;
	private String languageDetected;

	public Abstract(String content, String language) {
		this.content = content;
		this.language = language;
	}
	
	public Abstract(String content) {
		this.content = content;
		
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

	public void setLanguageDetected(String languageDetected) {
		this.languageDetected = languageDetected;
	}

	public String getLanguageDetected() {
		return languageDetected;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Abstract{");
		sb.append("content = ").append(getContent());
		sb.append(", language = ").append(getLanguage());
		sb.append(", languageDetected = ").append(getLanguageDetected());
		return sb.append("}").toString();
	}

}
