package org.mrdlib.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.mrdlib.recommendation.RelatedDocumentGenerator;

/**
 * 
 * @author Millah
 * 
 *         This class handles the representation of the documentSet. The XML
 *         format is automatically generated through the class structure.
 *
 */

public class DocumentSet {

	private List<DisplayDocument> documentList = new ArrayList<DisplayDocument>();

	private String recommendationSetId;
	private String suggestedLabel;

	private int numberOfSolrRows;// number of items extracted from the database
	private String rankingMethod;
	private double percentageRankingValue;
	private RelatedDocumentGenerator rdg;
	private String recommendationApproach;
	private DisplayDocument requestedDocument;

	public DisplayDocument getRequestedDocument() {
		return requestedDocument;
	}

	@XmlTransient
	public void setRequestedDocument(DisplayDocument requestedDocument) {
		this.requestedDocument = requestedDocument;
	}

	public DocumentSet() {
	}

	public DocumentSet(List<DisplayDocument> documentList, String recommendationSetId, String suggestedLabel) {
		this.documentList = documentList;
		this.recommendationSetId = recommendationSetId;
		this.suggestedLabel = suggestedLabel;
	}

	public DocumentSet sortDescForRankingValue(boolean onlySolr) {
		this.avoidZeroRankingValue();
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((b, a) -> Double.compare(a.getRankingValue(), b.getRankingValue()))
				.collect(Collectors.toList()));
		if (onlySolr)
			this.rankingMethod = "only_solr_desc";
		else
			this.rankingMethod = "sort_only_based_on_bibliometrics_desc";
		return this;
	}

	public DocumentSet sortAscForRankingValue(boolean onlySolr) {
		this.avoidZeroRankingValue();
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((a, b) -> Double.compare(a.getRankingValue(), b.getRankingValue()))
				.collect(Collectors.toList()));
		if (onlySolr)
			this.rankingMethod = "only_solr_asc";
		else
			this.rankingMethod = "sort_only_based_on_bibliometrics_asc";
		return this;
	}

	public DocumentSet sortDescForLogRankingValueTimesSolrScore() {
		this.avoidZeroRankingValue();
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((b, a) -> Double.compare((a.getSolrScore() * Math.log(a.getRankingValue())),
						b.getSolrScore() * Math.log(b.getRankingValue())))
				.collect(Collectors.toList()));
		this.rankingMethod = "log_text_relevance_times_bibliometrics_desc";
		return this;
	}

	public DocumentSet sortAscForLogRankingValueTimesSolrScore() {
		this.avoidZeroRankingValue();
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((a, b) -> Double.compare((a.getSolrScore() * Math.log(a.getRankingValue())),
						b.getSolrScore() * Math.log(b.getRankingValue())))
				.collect(Collectors.toList()));
		this.rankingMethod = "log_text_relevance_times_bibliometrics_asc";
		return this;
	}

	public DocumentSet sortDescForRootRankingValueTimesSolrScore() {
		this.avoidZeroRankingValue();
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((b, a) -> Double.compare((a.getSolrScore() * Math.sqrt(a.getRankingValue())),
						b.getSolrScore() * Math.sqrt(b.getRankingValue())))
				.collect(Collectors.toList()));
		this.rankingMethod = "root_text_relevance_times_bibliometrics_desc";
		return this;
	}

	public DocumentSet sortAscForRootRankingValueTimesSolrScore() {
		this.avoidZeroRankingValue();
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((a, b) -> Double.compare((a.getSolrScore() * Math.sqrt(a.getRankingValue())),
						b.getSolrScore() * Math.sqrt(b.getRankingValue())))
				.collect(Collectors.toList()));
		this.rankingMethod = "root_text_relevance_times_bibliometrics_asc";
		return this;
	}

	public DocumentSet sortDescForRankingValueTimesSolrScore() {
		this.avoidZeroRankingValue();
		this.setDocumentList(this.getDocumentList().stream().sorted((b, a) -> Double
				.compare((a.getSolrScore() * a.getRankingValue()), b.getSolrScore() * b.getRankingValue()))
				.collect(Collectors.toList()));
		this.rankingMethod = "text_relevance_times_bibliometrics_desc";
		return this;
	}

	public DocumentSet sortAscForRankingValueTimesSolrScore() {
		this.avoidZeroRankingValue();
		this.setDocumentList(this.getDocumentList().stream().sorted((a, b) -> Double
				.compare((a.getSolrScore() * a.getRankingValue()), b.getSolrScore() * b.getRankingValue()))
				.collect(Collectors.toList()));
		this.rankingMethod = "text_relevance_times_bibliometrics_asc";
		return this;
	}

	public DocumentSet refreshRankReal() {
		DisplayDocument current = null;

		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			current.setRealRank(i + 1);
		}
		return this;
	}

	public DocumentSet refreshRankSuggested() {
		DisplayDocument current = null;

		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			current.setSuggestedRank(i + 1);
		}
		return this;
	}

	public DocumentSet refreshRankBoth() {
		DisplayDocument current = null;

		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			current.setRealRank(i + 1);
			current.setSuggestedRank(i + 1);
		}
		return this;
	}

	public DocumentSet shuffle() {
		Collections.shuffle(this.getDocumentList());
		this.refreshRankSuggested();
		return this;

	}

	private void avoidZeroRankingValue() {
		DisplayDocument current = null;
		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			if (current.getRankingValue() == -1)
				current.setRankingValue(0);
			current.setRankingValue(current.getRankingValue() + 2);
		}
	}

	public void setPercentageRankingValue(double percentageRankingValue) {
		this.percentageRankingValue = percentageRankingValue;
	}

	public double getPercentageRankingValue() {
		return percentageRankingValue;
	}

	public String getRankingMethod() {
		return rankingMethod;
	}
	
	@XmlTransient
	public void setRankingMethod(String rankingMethod) {
		this.rankingMethod = rankingMethod;
	}

	public int getNumberOfSolrRows() {
		return numberOfSolrRows;
	}

	@XmlTransient
	public void setNumberOfSolrRows(int numberOfSolrRows) {
		this.numberOfSolrRows = numberOfSolrRows;
	}

	public int getSize() {
		return documentList.size();
	}

	public void addDocument(DisplayDocument document) {
		boolean newDocument = true;
		DisplayDocument current;
		for (int i = 0; i < this.documentList.size(); i++) {
			current = this.documentList.get(i);
			//System.out.println(current.getTitle());

			// if the document is the same, do not add as duplicate
			if (equalDocuments(document, current)) {
				if (Integer.parseInt(current.getDocumentId()) < Integer.parseInt(document.getDocumentId())) {
					this.documentList.remove(i);
					this.documentList.add(document);
				}
				newDocument = false;
			}
		}
		if (equalDocuments(document, this.requestedDocument))
			newDocument = false;

		if (newDocument)
			this.documentList.add(document);
	}

	private boolean equalDocuments(DisplayDocument document1, DisplayDocument document2) {
		if (calculateTitleClean(document1.getTitle()).equals(calculateTitleClean(document2.getTitle())))
			return true;
		else
			return false;
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

	@XmlAttribute(name = "set_id")
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

	public String getRecommendationApproach() {
		return this.recommendationApproach;
	}

	public void setRecommendationApproach(String recommendationApproach) {
		this.recommendationApproach = recommendationApproach;
	}

	public RelatedDocumentGenerator getRDG() {
		return this.rdg;
	}

	@XmlTransient
	public void setRDG(RelatedDocumentGenerator rdg) {
		this.rdg = rdg;
		this.recommendationApproach = rdg.loggingInfo.get("name");
	}

	public void calculatePercentageRankingValue() {
		int rankingValueCount = 0;
		for (int i = 0; i < this.getSize(); i++) {
			if (this.getDocumentList().get(i).getRankingValue() != -1) {
				rankingValueCount++;
			}
		}
		this.percentageRankingValue = (double) rankingValueCount / this.getSize();
	}

	private String calculateTitleClean(String s) {
		s = s.replaceAll("[^a-zA-Z0-9]", "");
		s = s.toLowerCase();
		return s;
	}
}
