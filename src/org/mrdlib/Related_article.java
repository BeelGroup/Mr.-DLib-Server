package org.mrdlib;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "related_article")
public class Related_article implements Serializable {

	private static final long serialVersionUID = 1L;

	// attributes
	private String recommendation_id;
	private String document_id;
	private String original_document_id;
	private int suggested_rank;

	// elements
	private String snippet_plain;
	private String snippet_html;
	private String click_url;
	private String fallback_url;

	public Related_article() {
	}

	public Related_article(String recommendation_id, String document_id, String original_document_id, int suggested_rank,
			String snippet_plain, String snippet_html, String click_url, String fallback_url) {
		super();
		this.recommendation_id = recommendation_id;
		this.document_id = document_id;
		this.original_document_id = original_document_id;
		this.suggested_rank = suggested_rank;
		this.snippet_plain = snippet_plain;
		this.snippet_html = snippet_html;
		this.click_url = click_url;
		this.fallback_url = fallback_url;
	}

	public String getRecommendation_id() {
		return recommendation_id;
	}

	@XmlAttribute
	public void setRecommendation_id(String recommendation_id) {
		this.recommendation_id = recommendation_id;
	}

	public String getDocument_id() {
		return document_id;
	}

	@XmlAttribute
	public void setDocument_id(String document_id) {
		this.document_id = document_id;
	}

	public String getOriginal_document_id() {
		return original_document_id;
	}

	@XmlAttribute
	public void setOriginal_document_id(String original_document_id) {
		this.original_document_id = original_document_id;
	}

	public int getSuggested_rank() {
		return suggested_rank;
	}

	@XmlAttribute
	public void setSuggested_rank(int suggested_rank) {
		this.suggested_rank = suggested_rank;
	}

	public String getSnippet_plain() {
		return snippet_plain;
	}

	@XmlElement
	public void setSnippet_plain(String snippet_plain) {
		this.snippet_plain = snippet_plain;
	}

	public String getSnippet_html() {
		return snippet_html;
	}

	@XmlElement
	public void setSnippet_html(String snippet_html) {
		this.snippet_html = snippet_html;
	}

	public String getClick_url() {
		return click_url;
	}

	@XmlElement
	public void setClick_url(String click_url) {
		this.click_url = click_url;
	}

	public String getFallback_url() {
		return fallback_url;
	}

	@XmlElement
	public void setFallback_url(String fallback_url) {
		this.fallback_url = fallback_url;
	}
}
