package org.mrdlib;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Millah
 * 
 * This class handles the presentation of the documentSet.
 * The XML format is automatically generated through the class structure.
 *
 */

@XmlRootElement(name = "related_articles")
public class DocumentSet {
	
	private List<Document> documentList = new ArrayList<Document>();
	
	private String recommendationSetId;
	private String suggestedLabel;
	
	public DocumentSet() {}

	public DocumentSet(List<Document> documentList, String recommendationSetId, String suggestedLabel) {
		this.documentList = documentList;
		this.recommendationSetId = recommendationSetId;
		this.suggestedLabel = suggestedLabel;
	}

	public int getSize() {
		return documentList.size();
	}
	
	public void addDocument(Document document) {
		documentList.add(document);
	}
	
	public List<Document> getDocumentList() {
		return documentList;
	}
	@XmlElement(name = "related_article")
	public void setDocumentList(List<Document> documentList) {
		this.documentList = documentList;
	}
	public String getRecommendationSetId() {
		return recommendationSetId;
	}
	@XmlAttribute(name = "recommendation_set_id")
	public void setRecommendationSetId(String recommendationSetId) {
		this.recommendationSetId = recommendationSetId;
	}
	public String getSuggested_label() {
		return suggestedLabel;
	}
	@XmlAttribute(name = "suggested_label")
	public void setSuggested_label(String suggestedLabel) {
		this.suggestedLabel = suggestedLabel;
	}
}
