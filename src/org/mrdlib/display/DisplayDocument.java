package org.mrdlib.display;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
/**
 * 
 * @author Millah
 * 
 * This class handles the representation of the document information of one document.
 * The XML format is automatically generated through the class structure.
 *
 */
public class DisplayDocument implements Serializable {

	private static final long serialVersionUID = 1L;

	// attributes
	private String recommendationId;
	private String documentId;
	private String originalDocumentId;
	private String accessKeyHash;
	
	// elements
	private Snippet snippet;
	private String clickUrl;
	private String fallbackUrl;
	private String language;
	

	private int suggestedRank;
	private String collectionShortName;
	private Long collectionId;
	
	//metadata
	private int realRank;
	private double rankingValue;
	private double solrScore;
	private int bibId;
	private String title;
	private String authorNames;
	private String publishedIn;
	private int year;
	
	public DisplayDocument() {
	}

	public DisplayDocument(String recommendationId, String documentId, String originalDocumentId, int suggestedRank,
			String title, String authorNames, String publishedIn, int year, String format, String clickUrl, String fallbackUrl, String collectionShortName) {
		this.recommendationId = recommendationId;
		this.documentId = documentId;
		this.originalDocumentId = originalDocumentId;
		this.suggestedRank = suggestedRank;
		this.title = title;
		this.authorNames = authorNames;
		this.publishedIn = publishedIn;
		this.year = year;
		this.snippet = new Snippet(title, authorNames, publishedIn, year, format);
		this.clickUrl = clickUrl;
		this.fallbackUrl = fallbackUrl;
		this.collectionShortName = collectionShortName;
	}
	
	public String getTitle() {
		return title;
	}

	public String getAuthorNames() {
		return authorNames;
	}

	public String getPublishedIn() {
		return publishedIn;
	}

	public int getYear() {
		return year;
	}

	public int getBibId() {
		return bibId;
	}
	
	@XmlTransient
	public void setBibId(int bibId) {
		this.bibId = bibId;
	}

	public double getSolrScore() {
		return solrScore;
	}

	public void setSolrScore(double solrScore) {
		this.solrScore = solrScore;
	}

	public double getRankingValue() {
		return rankingValue;
	}

	public void setRankingValue(double rankingValue) {
		this.rankingValue = rankingValue;
	}

	public int getRealRank() {
		return realRank;
	}

	@XmlTransient
	public void setRealRank(int realRank) {
		this.realRank = realRank;
	}

	public Long getCollectionId() {
		return collectionId;
	}
	@XmlTransient
	public void setCollectionId(Long collectionId) {
		this.collectionId = collectionId;
	}

	public String getCollectionShortName() {
		return collectionShortName;
	}
	@XmlTransient
	public void setCollectionShortName(String collectionShortName) {
		this.collectionShortName = collectionShortName;
	}
	
	public String getRecommendationId() {
		return recommendationId;
	}
	@XmlAttribute(name = "recommendation_id")
	public void setRecommendationId(String recommendationId) {
		this.recommendationId = recommendationId;
	}

	public String getDocumentId() {
		return documentId;
	}

	@XmlAttribute(name = "document_id")
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getOriginalDocumentId() {
		return originalDocumentId;
	}

	@XmlAttribute(name = "original_document_id")
	public void setOriginalDocumentId(String originalDocumentId) {
		this.originalDocumentId = originalDocumentId;
	}

	public int getSuggestedRank() {
		return suggestedRank;
	}

	@XmlElement(name = "suggested_rank")
	public void setSuggestedRank(int suggestedRank) {
		this.suggestedRank = suggestedRank;
	}

	public Snippet getSnippet() {
		return snippet;
	}

	@XmlElement(name = "snippet")
	public void setSnippet(Snippet snippet) {
		this.snippet = snippet;
	}

	public String getClickUrl() {
		return clickUrl;
	}

	@XmlElement(name = "click_url")
	public void setClickUrl(String clickUrl) {
		this.clickUrl = clickUrl;
	}

	public String getFallbackUrl() {
		return fallbackUrl;
	}

	@XmlElement(name = "fallback_url")
	public void setFallbackUrl(String fallbackUrl) {
		this.fallbackUrl = fallbackUrl;
	}
	
	public String getAccessKeyHash() {
		return accessKeyHash;
	}

	@XmlTransient
	public void setAccessKeyHash(String accessKeyHash) {
		this.accessKeyHash = accessKeyHash;
	}

	public String getLanguage() {
		return language;
	}
	
	@XmlTransient
	public void setLanguage(String language) {
		this.language = language;
	}
}