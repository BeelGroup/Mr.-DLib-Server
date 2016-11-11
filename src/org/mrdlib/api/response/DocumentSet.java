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

	private String recommendationSetId; // ok
	private String suggestedLabel; // ok

	private DisplayDocument requestedDocument;

	private Constants constants;

	// metadata of the algorithm MOVE TO DEBUGDETAILS
	private int numberOfCandidatesToReRank;// number of items choosed to rerank
											// //ok
	private String reRankingCombination; // ok
	private String rankingOrder; // ok
	private RelatedDocuments rdg; // sid changes
	private long numberOfReturnedResults; // currently: numberFromAlgReturns
											// //NEED: numberFromAlgReturns;
											// desiredNumberFromAlg;
											// numberOfRecWeDisplay
	private boolean fallback; // Sid
	private int recommendationAlgorithmId; // ok
	private int bibliometricId; // ok
	private boolean bibliometricReRanking = true;
	// NEED boolean shuffled
	// NEED boolearn removeDuplicates
	// NEED accesskey
	// NEED timestamps: start; afterChooseOfAlg; afterUserModel; afterAlg;
	// afterReRank; end?
	// NEED metric
	// NEED type
	// NEED source

	private DebugDetailsPerSet debugDetailsPerSet = new DebugDetailsPerSet();

	public boolean isBibliometricReRanking() {
		return bibliometricReRanking;
	}

	public void setBibliometricReRanking(boolean bibliometricReRanking) {
		this.bibliometricReRanking = bibliometricReRanking;
	}

	public int getBibliometricId() {
		return bibliometricId;
	}

	public void setBibliometricId(int bibliometricId) {
		this.bibliometricId = bibliometricId;
	}

	public String getRankingOrder() {
		return rankingOrder;
	}

	public void setRankingOrder(String rankingOrder) {
		this.rankingOrder = rankingOrder;
	}

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
	 * @return resorted documentset
	 */
	public DocumentSet sortDescForRankingValue() {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((b, a) -> Double.compare(a.getBibScore(), b.getBibScore())).collect(Collectors.toList()));
		this.reRankingCombination = "bibliometrics_only";
		this.rankingOrder = "desc";
		return this;
	}

	/**
	 * 
	 * sorts the documentset list asc for the ranking value
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet sortAscForRankingValue() {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((a, b) -> Double.compare(a.getBibScore(), b.getBibScore())).collect(Collectors.toList()));
		this.reRankingCombination = "bibliometrics_only";
		this.rankingOrder = "asc";
		return this;
	}

	/**
	 * 
	 * sorts the documentset list asc for the algorithm relevance
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet sortAscForTextRelevance() {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(this.getDocumentList().stream().sorted(
				(a, b) -> Double.compare(a.getRelevanceScoreFromAlgorithm(), b.getRelevanceScoreFromAlgorithm()))
				.collect(Collectors.toList()));
		this.reRankingCombination = "standard_only";
		this.rankingOrder = "asc";
		this.bibliometricReRanking = false;

		return this;
	}
	
	/**
	 * 
	 * sorts the documentset list desc for the algorithm relevance
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet sortDescForTextRelevance() {
		this.avoidZeroRankingValue();
		// lambda exspression for sorting
		this.setDocumentList(this.getDocumentList().stream().sorted(
				(a, b) -> Double.compare(b.getRelevanceScoreFromAlgorithm(), a.getRelevanceScoreFromAlgorithm()))
				.collect(Collectors.toList()));
		this.reRankingCombination = "standard_only";
		this.rankingOrder = "desc";
		this.bibliometricReRanking = false;

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
						.sorted((b, a) -> Double.compare(
								(a.getRelevanceScoreFromAlgorithm() * Math.log(a.getBibScore())),
								b.getRelevanceScoreFromAlgorithm() * Math.log(b.getBibScore())))
						.collect(Collectors.toList()));
		this.reRankingCombination = "standard_*_log_bibliometrics_score";
		this.rankingOrder = "desc";
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
						.sorted((a, b) -> Double.compare(
								(a.getRelevanceScoreFromAlgorithm() * Math.log(a.getBibScore())),
								b.getRelevanceScoreFromAlgorithm() * Math.log(b.getBibScore())))
						.collect(Collectors.toList()));
		this.reRankingCombination = "standard_*_log_bibliometrics_score";
		this.rankingOrder = "asc";
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
						.sorted((b, a) -> Double.compare(
								(a.getRelevanceScoreFromAlgorithm() * Math.sqrt(a.getBibScore())),
								b.getRelevanceScoreFromAlgorithm() * Math.sqrt(b.getBibScore())))
						.collect(Collectors.toList()));
		this.reRankingCombination = "standard_*_root_bibliometrics_score";
		this.rankingOrder = "desc";
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
						.sorted((a, b) -> Double.compare(
								(a.getRelevanceScoreFromAlgorithm() * Math.sqrt(a.getBibScore())),
								b.getRelevanceScoreFromAlgorithm() * Math.sqrt(b.getBibScore())))
						.collect(Collectors.toList()));
		this.reRankingCombination = "standard_*_root_bibliometrics_score";
		this.rankingOrder = "asc";
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
				.sorted((b, a) -> Double.compare((a.getRelevanceScoreFromAlgorithm() * a.getBibScore()),
						b.getRelevanceScoreFromAlgorithm() * b.getBibScore()))
				.collect(Collectors.toList()));
		this.reRankingCombination = "standard_*_bibliometrics_score";
		this.rankingOrder = "desc";
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
				.sorted((a, b) -> Double.compare((a.getRelevanceScoreFromAlgorithm() * a.getBibScore()),
						b.getRelevanceScoreFromAlgorithm() * b.getBibScore()))
				.collect(Collectors.toList()));
		this.reRankingCombination = "standard_*_bibliometrics_score";
		this.rankingOrder = "asc";
		return this;
	}

	/**
	 * 
	 * sets the rankAfterAlgorithm property
	 * 
	 * @return refreshed documentset
	 */
	public DocumentSet setRankAfterAlgorithm() {
		DisplayDocument current = null;

		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			current.setRankAfterAlgorithm(i + 1);
		}
		return this;
	}

	/**
	 * 
	 * sets the rankAfterReRanking property (used after shuffling)
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet setRankAfterReRanking() {
		DisplayDocument current = null;

		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			current.setRankAfterReRanking(i + 1);
		}
		return this;
	}

	/**
	 * 
	 * sets the rankAfterShuffling property
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet setRankAfterShuffling() {
		DisplayDocument current = null;

		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			current.setRankAfterShuffling(i + 1);
		}
		return this;
	}

	/**
	 * 
	 * sets the RankDelivered property
	 * 
	 * @return resorted documentset
	 */
	public DocumentSet setRankDelivered() {
		DisplayDocument current = null;

		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDocumentList().get(i);
			current.setRankDelivered(i + 1);
		}
		return this;
	}

	/**
	 * 
	 * shuffles the list and refreshed the rank delivered
	 * 
	 * @return shuffled documentset
	 */
	public DocumentSet shuffle() {
		Collections.shuffle(this.getDocumentList());
		this.setRankDelivered();
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
			if (current.getBibScore() == -1)
				current.setBibScore(0);
			current.setBibScore(current.getBibScore() + 2);
		}
	}

	@XmlTransient
	public void setPercentageRankingValue(double percentageRankingValue) {
		debugDetailsPerSet.setPercentageRankingValue(percentageRankingValue);
	}

	public double getPercentageRankingValue() {
		return debugDetailsPerSet.getPercentageRankingValue();
	}

	public String getReRankingCombination() {
		return reRankingCombination;
	}

	@XmlTransient
	public void setReRankingCombination(String reRankingCombination) {
		this.reRankingCombination = reRankingCombination;
	}

	public int getNumberOfCandidatesToReRank() {
		return numberOfCandidatesToReRank;
	}

	@XmlTransient
	public void setNumberOfCandidatesToReRank(int numberOfCandidatesToReRank) {
		this.numberOfCandidatesToReRank = numberOfCandidatesToReRank;
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
	 * 
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
			if (this.getDocumentList().get(i).getBibScore() != -1) {
				rankingValueCount++;
			}
		}
		debugDetailsPerSet.setPercentageRankingValue((double) rankingValueCount / this.getSize());
	}

	/**
	 * 
	 * calculate cleanTitle of a String, only numbers and letters are valid
	 * characters
	 * 
	 * @param String
	 *            to clean
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
	 * @param recommendationAlgorithmId
	 *            the recommendationAlgorithmId to set
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
	 * @param fallback
	 *            the fallback to set
	 */
	public void setFallback(boolean fallback) {
		this.fallback = fallback;
	}
}
