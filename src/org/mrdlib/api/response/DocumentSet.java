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

	private DebugDetailsPerSet debugDetailsPerSet = new DebugDetailsPerSet();


	public DocumentSet(List<DisplayDocument> documentList, String recommendationSetId, String suggestedLabel,
			Constants constants) {
		this.documentList = documentList;
		this.recommendationSetId = recommendationSetId;
		this.suggestedLabel = suggestedLabel;
		this.constants = constants;
	}
	
	/**
	 * 
	 * sorts the documentset list desc for the final score
	 *  
	 * @return resorted documentset
	 */
	public DocumentSet sortDescForFinalValue() {
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((b, a) -> Double.compare(a.getFinalScore(), b.getFinalScore())).collect(Collectors.toList()));
		this.setRankingOrder("desc");
		return this;
	}
	
	/**
	 * 
	 * sorts the documentset list asc for the final score
	 *  
	 * @return resorted documentset
	 */
	public DocumentSet sortAscForFinalValue() {
		this.setDocumentList(this.getDocumentList().stream()
				.sorted((b, a) -> Double.compare(b.getFinalScore(), a.getFinalScore())).collect(Collectors.toList()));
		this.setRankingOrder("asc");
		return this;
	}
	
	/**
	 * 
	 * calculates the final score only from bibliometrics
	 * 
	 * @return documentset with final score
	 */
	public DocumentSet calculateFinalScoreOnlyBibScore() {
		this.avoidZeroRankingValue();
		DisplayDocument current = new DisplayDocument();
		for(int i = 0; i < this.getSize(); i++) {
			current = this.documentList.get(i);
			current.setFinalScore(current.getBibScore());
		}
		this.setReRankingCombination("bibliometrics_only");
		return this;
	}

	/**
	 * 
	 * calculates the final score only from Relevance Score From Algorithm
	 * 
	 * @return documentset with final score
	 */
	public DocumentSet calculateFinalScoreOnlyRelevanceScore() {
		this.avoidZeroRankingValue();
		DisplayDocument current = new DisplayDocument();
		for(int i = 0; i < this.getSize(); i++) {
			current = this.documentList.get(i);
			current.setFinalScore(current.getRelevanceScoreFromAlgorithm());
		}

		this.setReRankingCombination("standard_only");
		return this;
	}
	
	/**
	 * 
	 * calculates the final score from relevance score from algorithm times log(bib score)
	 * 
	 * @return documentset with final score
	 */
	public DocumentSet calculateFinalScoreForRelevanceScoreTimesLogBibScore() {
		this.avoidZeroRankingValue();
		DisplayDocument current = new DisplayDocument();
		for(int i = 0; i < this.getSize(); i++) {
			current = this.documentList.get(i);
			current.setFinalScore(current.getRelevanceScoreFromAlgorithm() * Math.log(current.getBibScore()));
		}
		this.setReRankingCombination("standard_*_log_bibliometrics_score");
		return this;
	}

	/**
	 * 
	 * calculates the final score from relevance score from algorithm times root(bib score)
	 * 
	 * @return documentset with final score
	 */
	public DocumentSet calculateFinalScoreForRelevanceScoreTimesRootBibScore() {
		this.avoidZeroRankingValue();
		DisplayDocument current = new DisplayDocument();
		for(int i = 0; i < this.getSize(); i++) {
			current = this.documentList.get(i);
			current.setFinalScore(current.getRelevanceScoreFromAlgorithm() * Math.sqrt(current.getBibScore()));
		}
		this.setReRankingCombination("standard_*_root_bibliometrics_score");
		return this;
	}
	
	/**
	 * 
	 * calculates the final score from relevance score from algorithm times bib score
	 * 
	 * @return documentset with final score
	 */
	public DocumentSet calculateFinalScoreForRelevanceScoreTimesBibScore() {
		this.avoidZeroRankingValue();
		DisplayDocument current = new DisplayDocument();
		for(int i = 0; i < this.getSize(); i++) {
			current = this.documentList.get(i);
			current.setFinalScore(current.getRelevanceScoreFromAlgorithm() * current.getBibScore());
		}
		this.setReRankingCombination("standard_*_bibliometrics_score");
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
			current.setSuggestedRank(i + 1);
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
		this.setShuffled(true);
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
		DisplayDocument current;
		
		if (equalDocuments(document, this.requestedDocument))
			return;
		
		for (int i = 0; i < this.documentList.size(); i++) {
			current = this.documentList.get(i);
			
			// if the document is the same, do not add as duplicate
			if (equalDocuments(document, current)) {
				if (Integer.parseInt(current.getDocumentId()) < Integer.parseInt(document.getDocumentId())) {
					this.documentList.remove(i);
					this.documentList.add(document);
				}
				return;
			}
		}
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

	//~~~~~~~~~~~~~~~~~GETTER AND SETTER~~~~~~~~~~~~~~~~~
	public DebugDetailsPerSet getDebugDetailsPerSet() {
		return debugDetailsPerSet;
	}

	@XmlElement(name = "debug_details")
	public void setDebugDetailsPerSet(DebugDetailsPerSet debugDetailsPerSet) {
		if (constants.getDebugModeOn())
			this.debugDetailsPerSet = debugDetailsPerSet;
	}
	
	/**
	 * @return the recommendationAlgorithmId
	 */
	public String getRecommendationAlgorithmId() {
		return this.debugDetailsPerSet.getRecommendationAlgorithmId();
	}

	/**
	 * @param recommendationAlgorithmId
	 *            the recommendationAlgorithmId to set
	 */
	@XmlTransient
	public void setRecommendationAlgorithmId(int recommendationAlgorithmId) {
		this.debugDetailsPerSet.setRecommendationAlgorithmId(recommendationAlgorithmId);
	}

	/**
	 * @return the fallback
	 */
	public boolean isFallback() {
		return this.debugDetailsPerSet.isFallback();
	}

	/**
	 * @param fallback
	 *            the fallback to set
	 */
	@XmlTransient
	public void setFallback(boolean fallback) {
		this.debugDetailsPerSet.setFallback(fallback);
	}
	
	public boolean isBibliometricReRanking() {
		return this.debugDetailsPerSet.isBibliometricReRanking();
	}

	@XmlTransient
	public void setBibliometricReRanking(boolean bibliometricReRanking) {
		this.debugDetailsPerSet.setBibliometricReRanking(bibliometricReRanking);
	}

	public int getBibliometricId() {
		return this.debugDetailsPerSet.getBibliometricId();
	}

	@XmlTransient
	public void setBibliometricId(int bibliometricId) {
		this.debugDetailsPerSet.setBibliometricId(bibliometricId);
	}

	public String getRankingOrder() {
		return this.debugDetailsPerSet.getRankingOrder();
	}

	@XmlTransient
	public void setRankingOrder(String rankingOrder) {
		this.debugDetailsPerSet.setRankingOrder(rankingOrder);
	}

	public DisplayDocument getRequestedDocument() {
		return requestedDocument;
	}

	@XmlTransient
	public void setRequestedDocument(DisplayDocument requestedDocument) {
		this.requestedDocument = requestedDocument;
	}

	public DocumentSet(Constants constants) {
		this.constants = constants;
	}
	
	@XmlTransient
	public void setPercentageRankingValue(double percentageRankingValue) {
		debugDetailsPerSet.setPercentageRankingValue(percentageRankingValue);
	}

	public double getPercentageRankingValue() {
		return debugDetailsPerSet.getPercentageRankingValue();
	}

	public String getReRankingCombination() {
		return this.debugDetailsPerSet.getReRankingCombination();
	}

	@XmlTransient
	public void setReRankingCombination(String reRankingCombination) {
		this.debugDetailsPerSet.setReRankingCombination(reRankingCombination);
	}

	public int getNumberOfCandidatesToReRank() {
		return this.debugDetailsPerSet.getNumberOfCandidatesToReRank();
	}

	@XmlTransient
	public void setNumberOfCandidatesToReRank(int numberOfCandidatesToReRank) {
		this.debugDetailsPerSet.setNumberOfCandidatesToReRank(numberOfCandidatesToReRank);
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
		return this.debugDetailsPerSet.getRdg();
	}

	@XmlTransient
	public void setRDG(RelatedDocuments rdg) {
		this.debugDetailsPerSet.setRdg(rdg);
	}

	public long getNumberOfReturnedResults() {
		return this.debugDetailsPerSet.getNumberOfReturnedResults();
	}

	@XmlTransient
	public void setNumberOfReturnedResults(long numberOfReturnedResults) {
		this.debugDetailsPerSet.setNumberOfReturnedResults(numberOfReturnedResults);
	}
	
	public boolean isShuffled() {
		return this.debugDetailsPerSet.isShuffled();
	}
	
	@XmlTransient
	public void setShuffled(boolean shuffled) {
		this.debugDetailsPerSet.setShuffled(shuffled);
	}

	public boolean isRemovedDuplicates() {
		return this.debugDetailsPerSet.isRemovedDuplicates();
	}

	@XmlTransient
	public void setRemoveDuplicates(boolean removedDuplicates) {
		this.debugDetailsPerSet.setRemovedDuplicates(removedDuplicates);
	}
	
	public int getDesiredNumberFromAlgorithm() {
		return this.debugDetailsPerSet.getDesiredNumberFromAlgorithm();
	}
	
	@XmlTransient
	public void setDesiredNumberFromAlgorithm(int desiredNumberFromAlgorithm) {
		this.debugDetailsPerSet.setDesiredNumberFromAlgorithm(desiredNumberFromAlgorithm);
	}
	
	public int getNumberOfDisplayedRecommendations() {
		return this.debugDetailsPerSet.getNumberOfDisplayedRecommendations();
	}

	@XmlTransient
	public void setNumberOfDisplayedRecommendations(int numberOfDisplayedRecommendations) {
		this.debugDetailsPerSet.setNumberOfDisplayedRecommendations(numberOfDisplayedRecommendations);
	}
}
