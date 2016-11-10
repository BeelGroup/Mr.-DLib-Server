package org.mrdlib.api.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.mrdlib.api.manager.Constants;
import org.mrdlib.recommendation.algorithm.RelatedDocuments;

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

	private Constants constants;

	// metadata of the algorithm
	private int numberOfSolrRows;// number of items extracted from the database
	private String rankingMethod;
	private RelatedDocuments rdg;
	private DisplayDocument requestedDocument;
	private long numberOfReturnedResults;
	private boolean fallback;
	private int recommendationAlgorithmId;

	private DebugDetailsPerSet debugDetailsPerSet = new DebugDetailsPerSet();



	public DisplayDocument getRequestedDocument() {
		return requestedDocument;
	}

	public DebugDetailsPerSet getDebugDetailsPerSet() {
		return debugDetailsPerSet;
	}

	@XmlElement(name = "debug_details")
	public void setDebugDetailsPerSet(DebugDetailsPerSet debugDetailsPerSet) {
		if (constants.getDebugModeOn())
			this.debugDetailsPerSet = debugDetailsPerSet;
	}

	@XmlTransient
	public void setRequestedDocument(DisplayDocument requestedDocument) {
		this.requestedDocument = requestedDocument;
	}

	public DocumentSet(Constants constants) {
		this.constants = constants;
	}

	public DocumentSet(List<DisplayDocument> documentList, String recommendationSetId, String suggestedLabel,
			Constants constants) {
		this.documentList = documentList;
		this.recommendationSetId = recommendationSetId;
		this.suggestedLabel = suggestedLabel;
		this.constants = constants;
	}

	/**
	 * 
	 * sorts the documentset list desc for the ranking value
	 * 
	 * @param only
	 *            text Releveance, boolean if the random approach choosed only
	 *            Text relevance
	 * @return resorted documentset
	 */
	public DocumentSet sortDescForRankingValue(boolean onlyTextRelevance) {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((b, a) -> Double.compare(a.getRankingValue(), b.getRankingValue()))
				.collect(Collectors.toList()));
		if (onlyTextRelevance)
			this.rankingMethod = "only_solr_desc";
		else
			this.rankingMethod = "sort_only_based_on_bibliometrics_desc";
		return this;
	}

	/**
	 * 
	 * sorts the documentset list asc for the ranking value
	 * 
	 * @param only
	 *            text Releveance, boolean if the random approach choosed only
	 *            Text relevance
	 * @return resorted documentset
	 */
	public DocumentSet sortAscForRankingValue(boolean onlyTextRelevance) {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((a, b) -> Double.compare(a.getRankingValue(), b.getRankingValue()))
				.collect(Collectors.toList()));
		if (onlyTextRelevance)
			this.rankingMethod = "only_solr_asc";
		else
			this.rankingMethod = "sort_only_based_on_bibliometrics_asc";
		return this;
	}

	/**
	 * 
	 * sorts the documentset list desc for the log (ranking value) * text
	 * relevance
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet sortDescForLogRankingValueTimesTextRelevance() {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(
				this.getDocumentList().stream()
						.sorted((b, a) -> Double.compare((a.getTextRelevancyScore() * Math.log(a.getRankingValue())),
								b.getTextRelevancyScore() * Math.log(b.getRankingValue())))
						.collect(Collectors.toList()));
		this.rankingMethod = "log_text_relevance_times_bibliometrics_desc";
		return this;
	}

	/**
	 * 
	 * sorts the documentset list asc for the log (ranking value) * text
	 * relevance
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet sortAscForLogRankingValueTimesTextRelevance() {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(
				this.getDocumentList().stream()
						.sorted((a, b) -> Double.compare((a.getTextRelevancyScore() * Math.log(a.getRankingValue())),
								b.getTextRelevancyScore() * Math.log(b.getRankingValue())))
						.collect(Collectors.toList()));
		this.rankingMethod = "log_text_relevance_times_bibliometrics_asc";
		return this;
	}

	/**
	 * 
	 * sorts the documentset list desc for the root (ranking value) * text
	 * relevance
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet sortDescForRootRankingValueTimesTextRelevance() {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(
				this.getDocumentList().stream()
						.sorted((b, a) -> Double.compare((a.getTextRelevancyScore() * Math.sqrt(a.getRankingValue())),
								b.getTextRelevancyScore() * Math.sqrt(b.getRankingValue())))
						.collect(Collectors.toList()));
		this.rankingMethod = "root_text_relevance_times_bibliometrics_desc";
		return this;
	}

	/**
	 * 
	 * sorts the documentset list asc for the root (ranking value) * text
	 * relevance
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet sortAscForRootRankingValueTimesTextRelevance() {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(
				this.getDocumentList().stream()
						.sorted((a, b) -> Double.compare((a.getTextRelevancyScore() * Math.sqrt(a.getRankingValue())),
								b.getTextRelevancyScore() * Math.sqrt(b.getRankingValue())))
						.collect(Collectors.toList()));
		this.rankingMethod = "root_text_relevance_times_bibliometrics_asc";
		return this;
	}

	/**
	 * 
	 * sorts the documentset list desc for the ranking value * text relevance
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet sortDescForRankingValueTimesTextRelevance() {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((b, a) -> Double.compare((a.getTextRelevancyScore() * a.getRankingValue()),
						b.getTextRelevancyScore() * b.getRankingValue()))
				.collect(Collectors.toList()));
		this.rankingMethod = "text_relevance_times_bibliometrics_desc";
		return this;
	}

	/**
	 * 
	 * sorts the documentset list asc for the ranking value * text relevance
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet sortAscForRankingValueTimesTextRelevance() {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((a, b) -> Double.compare((a.getTextRelevancyScore() * a.getRankingValue()),
						b.getTextRelevancyScore() * b.getRankingValue()))
				.collect(Collectors.toList()));
		this.rankingMethod = "text_relevance_times_bibliometrics_asc";
		return this;
	}

	/**
	 * 
	 * refreshes the real calculated rank, according to the new position in the
	 * list (used after reranking)
	 * 
	 * @return refreshed documentset
	 */
	public DocumentSet refreshRankReal() {
		DisplayDocument current = null;

		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			current.setRealRank(i + 1);
		}
		return this;
	}

	/**
	 * 
	 * refreshes the suggested rank, according to the new position in the list
	 * (used after shuffling)
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet refreshRankSuggested() {
		DisplayDocument current = null;

		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			current.setSuggestedRank(i + 1);
		}
		return this;
	}

	/**
	 * 
	 * refreshes the both the real and suggested rank, according to the new
	 * position in the list
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet refreshRankBoth() {
		DisplayDocument current = null;

		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			current.setRealRank(i + 1);
			current.setSuggestedRank(i + 1);
		}
		return this;
	}

	/**
	 * 
	 * shuffles the list and refreshed the suggested rank
	 * 
	 * @return shuffled documentset
	 */
	public DocumentSet shuffle() {
		Collections.shuffle(this.getDocumentList());
		this.refreshRankSuggested();
		return this;

	}

	/**
	 * 
	 * creates valid ranking values, in case no ranking value is present in the
	 * database, and shifts every other ranking to +2, so the consistency is
	 * preserved
	 */
	private void avoidZeroRankingValue() {
		DisplayDocument current = null;
		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			if (current.getRankingValue() == -1)
				current.setRankingValue(0);
			current.setRankingValue(current.getRankingValue() + 2);
		}
	}

	@XmlTransient
	public void setPercentageRankingValue(double percentageRankingValue) {
		debugDetailsPerSet.setPercentageRankingValue(percentageRankingValue);
	}

	public double getPercentageRankingValue() {
		return debugDetailsPerSet.getPercentageRankingValue();
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

	/**
	 * 
	 * adds a new document to the documentset, but don't doubles (same
	 * cleantitle) or same as document that requested
	 * 
	 * @param displaydocument
	 */
	public void addDocument(DisplayDocument document) {
		boolean newDocument = true;
		DisplayDocument current;
		for (int i = 0; i < this.documentList.size(); i++) {
			current = this.documentList.get(i);
			// System.out.println(current.getTitle());

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

	/**
	 * 
	 * check if the documents are equal based on the cleantitle
	 * @param displayDocument
	 * @param displayDocument
	 * @return boolean, true if equal
	 */
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
		return debugDetailsPerSet.getRecommendationApproach();
	}

	@XmlTransient
	public void setRecommendationApproach(String recommendationApproach) {
		debugDetailsPerSet.setRecommendationApproach(recommendationApproach);
	}

	public RelatedDocuments getRDG() {
		return this.rdg;
	}

	@XmlTransient
	public void setRDG(RelatedDocuments rdg) {
		this.rdg = rdg;
		debugDetailsPerSet.setRecommendationApproach(rdg.loggingInfo.get("name"));
	}

	public long getNumberOfReturnedResults() {
		return numberOfReturnedResults;
	}

	@XmlTransient
	public void setNumberOfReturnedResults(long numberOfReturnedResults) {
		this.numberOfReturnedResults = numberOfReturnedResults;
	}
	
	public void calculatePercentageRankingValue() {
		int rankingValueCount = 0;
		for (int i = 0; i < this.getSize(); i++) {
			if (this.getDocumentList().get(i).getRankingValue() != -1) {
				rankingValueCount++;
			}
		}
		debugDetailsPerSet.setPercentageRankingValue((double) rankingValueCount / this.getSize());
	}

	/**
	 * 
	 * calculate cleanTitle of a String, only numbers and letters are valid characters
	 * 
	 * @param String to clean
	 * @return clean String
	 */
	private String calculateTitleClean(String s) {
		s = s.replaceAll("[^a-zA-Z0-9]", "");
		s = s.toLowerCase();
		return s;
	}

	/**
	 * @return the recommendationAlgorithmId
	 */
	public String getRecommendationAlgorithmId() {
		return Integer.toString(recommendationAlgorithmId);
	}

	/**
	 * @param recommendationAlgorithmId the recommendationAlgorithmId to set
	 */
	public void setRecommendationAlgorithmId(int recommendationAlgorithmId) {
		this.recommendationAlgorithmId = recommendationAlgorithmId;
	}

	/**
	 * @return the fallback
	 */
	public boolean isFallback() {
		return fallback;
	}

	/**
	 * @param fallback the fallback to set
	 */
	public void setFallback(boolean fallback) {
		this.fallback = fallback;
	}
}
