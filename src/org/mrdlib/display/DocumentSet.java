package org.mrdlib.display;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author Millah
 * 
 * This class handles the representation of the documentSet.
 * The XML format is automatically generated through the class structure.
 *
 */

public class DocumentSet {
	
	private List<DisplayDocument> documentList = new ArrayList<DisplayDocument>();
	
	private String recommendationSetId;
	private String suggestedLabel;
	
	public DocumentSet() {}

	public DocumentSet(List<DisplayDocument> documentList, String recommendationSetId, String suggestedLabel) {
		this.documentList = documentList;
		this.recommendationSetId = recommendationSetId;
		this.suggestedLabel = suggestedLabel;
	}

	public int getSize() {
		return documentList.size();
	}
	
	public void addDocument(DisplayDocument document) {
		documentList.add(document);
	}
	
	public List<DisplayDocument> getDocumentList() {
		return documentList;
	}
	@XmlElement(name = "related_article")
	public void setDocumentList(List<DisplayDocument> documentList) {
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
