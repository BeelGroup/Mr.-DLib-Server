package org.mrdlib.display;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	private List<Snippet> snippetList = new ArrayList<Snippet>();
	private String clickUrl;
	private String fallbackUrl;
	private String language;
	private DebugDetails debugDetails = new DebugDetails();
	

	private int suggestedRank;
	private String collectionShortName;
	private Long collectionId;
	
	//metadata

	private int realRank;
	private double rankingValue;
	private double textRelevancyScore;
	private int bibId;
	private String title;
	private String authorNames;
	private String publishedIn;
	private int year;
	
	public DisplayDocument() {
	}

	public DisplayDocument(String recommendationId, String documentId, String originalDocumentId, int suggestedRank,
			String title, String authorNames, String publishedIn, int year, String clickUrl, String fallbackUrl, String collectionShortName) {
		this.recommendationId = recommendationId;
		this.documentId = documentId;
		this.originalDocumentId = originalDocumentId;
		this.suggestedRank = suggestedRank;
		this.title = title;
		this.authorNames = authorNames;
		this.publishedIn = publishedIn;
		this.year = year;
		this.snippetList.add(new Snippet(clickUrl, title, authorNames, publishedIn, year, "html_plain"));
		this.snippetList.add(new Snippet(clickUrl, title, authorNames, publishedIn, year, "html_fully_formatted"));
		this.snippetList.add(new Snippet(clickUrl, title, authorNames, publishedIn, year, "html_and_css"));
		this.clickUrl = clickUrl;
		this.fallbackUrl = fallbackUrl;
		this.collectionShortName = collectionShortName;
	}
	
	
	public int getBibId() {
		return debugDetails.getBibId();
	}
	
	public double getRankingValue() {
		return debugDetails.getRankingValue();
	}
	
	
	@XmlTransient
	public void setRealRank(int rank) {
		debugDetails.setRealRank(rank);
	}
	
	@XmlTransient
	public void setBibId(int bibId) {
		debugDetails.setBibId(bibId);
	}
	
	@XmlTransient
	public void setRankingValue(double rankingValue) {
		debugDetails.setRankingValue(rankingValue);
	}
	
	public DebugDetails getDebugDetails() {
		return debugDetails;
	}

	@XmlElement(name = "debug_details")
	public void setDebugDetails(DebugDetails debugDetails) {
		this.debugDetails = debugDetails;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @deprecated Use {@link #getTextRelevancyScore()} instead
	 */
	public double getSolrScore() {
		return getTextRelevancyScore();
	}

	public double getTextRelevancyScore() {
		return textRelevancyScore;
	}

	/**
	 * @deprecated Use {@link #setTextRelevancyScore(double)} instead
	 */
	@XmlTransient
	public void setSolrScore(double solrScore) {
		setTextRelevancyScore(solrScore);
	}

	public void setTextRelevancyScore(double solrScore) {
		this.textRelevancyScore = solrScore;
	}

	public void setAuthorNames(String authorNames) {
		this.authorNames = authorNames;
	}

	public void setPublishedIn(String publishedIn) {
		this.publishedIn = publishedIn;
	}

	public void setYear(int year) {
		this.year = year;
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

	public List<Snippet> getSnippetList() {
		return snippetList;
	}

	@XmlElement(name = "snippet")
	public void setSnippetList(List<Snippet> snippetList) {
		this.snippetList = snippetList;
	}

	public String getClickUrl() {
		return clickUrl;
	}

	@XmlElement(name = "click_url")
	public void setClickUrl(String clickUrl) {
		this.clickUrl = clickUrl;
		for(int i=0; i< snippetList.size(); i++)
			this.snippetList.get(i).setClickUrl(clickUrl);
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