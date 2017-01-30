package org.mrdlib.api.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.mrdlib.api.manager.Constants;
import org.mrdlib.recommendation.algorithm.AlgorithmDetails;

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

	private DebugDetailsPerSet debugDetailsPerSet = new DebugDetailsPerSet();
	
	public DocumentSet() {}

	public DocumentSet(List<DisplayDocument> documentList, String recommendationSetId, String suggestedLabel,
			Constants constants) {
		this.documentList = documentList;
		this.recommendationSetId = recommendationSetId;
		this.suggestedLabel = suggestedLabel;
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
		for (int i = 0; i < this.getSize(); i++) {
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
		for (int i = 0; i < this.getSize(); i++) {
			current = this.documentList.get(i);
			current.setFinalScore(current.getRelevanceScoreFromAlgorithm());
		}

		this.setReRankingCombination("standard_only");
		return this;
	}

	/**
	 * 
	 * calculates the final score from relevance score from algorithm times
	 * log(bib score)
	 * 
	 * @return documentset with final score
	 */
	public DocumentSet calculateFinalScoreForRelevanceScoreTimesLogBibScore() {
		this.avoidZeroRankingValue();
		DisplayDocument current = new DisplayDocument();
		for (int i = 0; i < this.getSize(); i++) {
			current = this.documentList.get(i);
			current.setFinalScore(current.getRelevanceScoreFromAlgorithm() * Math.log(current.getBibScore()));
		}
		this.setReRankingCombination("standard_*_log_bibliometrics_score");
		return this;
	}

	/**
	 * 
	 * calculates the final score from relevance score from algorithm times
	 * root(bib score)
	 * 
	 * @return documentset with final score
	 */
	public DocumentSet calculateFinalScoreForRelevanceScoreTimesRootBibScore() {
		this.avoidZeroRankingValue();
		DisplayDocument current = new DisplayDocument();
		for (int i = 0; i < this.getSize(); i++) {
			current = this.documentList.get(i);
			current.setFinalScore(current.getRelevanceScoreFromAlgorithm() * Math.sqrt(current.getBibScore()));
		}
		this.setReRankingCombination("standard_*_root_bibliometrics_score");
		return this;
	}

	/**
	 * 
	 * calculates the final score from relevance score from algorithm times bib
	 * score
	 * 
	 * @return documentset with final score
	 */
	public DocumentSet calculateFinalScoreForRelevanceScoreTimesBibScore() {
		this.avoidZeroRankingValue();
		DisplayDocument current = new DisplayDocument();
		for (int i = 0; i < this.getSize(); i++) {
			current = this.documentList.get(i);
			current.setFinalScore(current.getRelevanceScoreFromAlgorithm() * current.getBibScore());
		}
		this.setReRankingCombination("standard_*_bibliometrics_score");
		return this;
	}

	public void calculateRankingStatistics(int displayNumber) {
		calculateBibScoreStatistics(displayNumber);
		calculateRelevanceScoreStatistics(displayNumber);
		calculateFinalScoreStatistics(displayNumber);
	}
	
	private void calculateFinalScoreStatistics(int displayNumber) {
		List<Double> rankValues = new ArrayList<Double>();

		//fill a list with only the relevance Scores. For easier reading and better editing.
		for (int i = 0; i < this.getSize(); i++)
			rankValues.add(this.getDisplayDocument(i).getFinalScore());
		
		calculateRankingStatistics("final", rankValues.subList(0, displayNumber));
	}
	
	private void calculateRelevanceScoreStatistics(int displayNumber) {
		List<Double> rankValues = new ArrayList<Double>();

		//fill a list with only the relevance Scores. For easier reading and better editing.
		for (int i = 0; i < this.getSize(); i++)
			rankValues.add(this.getDisplayDocument(i).getRelevanceScoreFromAlgorithm());
		
		calculateRankingStatistics("relevance", rankValues.subList(0, displayNumber));
	}
	
	private void calculateBibScoreStatistics(int displayNumber) {
		if(this.getReRankingCombination().equals("standard_only"))
			return;

		List<Double> rankValues = new ArrayList<Double>();

		//fill a list with only the bibScores. For easier reading and better editing.
		//Subtract -2 everywhere since it was added to avoid calculation errors.
		for (int i = 0; i < this.getSize(); i++)
			rankValues.add(this.getDisplayDocument(i).getBibScore()-2);
		
		calculateRankingStatistics("bibliometricDisplay", rankValues.subList(0, displayNumber));
		calculateRankingStatistics("bibliometricRerank", rankValues);
	}

	private void calculateRankingStatistics(String type, List<Double> rankValues) {
		Statistics stats = new Statistics();

		rankValues = rankValues.stream().sorted((a, b) -> Double.compare(a, b)).collect(Collectors.toList());

		double currentScore;
		double sum = 0;
		double min = Double.MAX_VALUE;
		double max = -1;
		double rankingValueCount = 0;
		HashMap<Double, Integer> modeCount = new HashMap<Double, Integer>();
		double mostFrequent = -1;
		double mostFrequentCount = 0;

		for (int i = 0; i < rankValues.size(); i++) {
			currentScore = rankValues.get(i);

			if (currentScore > 0) {
				rankingValueCount++;
			} else {
				currentScore = 0;
				rankValues.set(i, 0.0);
			}

			sum = sum + currentScore;
			max = Math.max(max, currentScore);
			min = Math.min(min, currentScore);

			// mode
			if (modeCount.containsKey(currentScore)) {
				modeCount.put(currentScore, modeCount.get(currentScore) + 1);
				if (modeCount.get(currentScore) > mostFrequentCount) {
					mostFrequent = currentScore;
					mostFrequentCount = modeCount.get(currentScore);
				}
			} else {
				modeCount.put(currentScore, 1);
			}
		}
		// median
		if (rankValues.size() % 2 != 0)
			stats.setRankVMedian(rankValues.get((rankValues.size() - 1) / 2));
		else
			stats.setRankVMedian(
					(rankValues.get(rankValues.size() / 2) + rankValues.get((rankValues.size() / 2) - 1)) / 2);

		stats.setType(type);
		stats.setRankVMean(sum / rankValues.size());
		stats.setRankVMin(min);
		stats.setRankVMax(max);
		stats.setPercentageRankingValue(rankingValueCount / (double) rankValues.size());
		stats.setRankVMode(mostFrequent);

		this.addRankingStats(stats);
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
			current = this.getDisplayDocument(i);
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
			current = this.getDisplayDocument(i);
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
			current = this.getDisplayDocument(i);
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
			current = this.getDisplayDocument(i);
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
		this.setShuffled(true);
		this.setRankAfterShuffling();
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
			current = this.getDisplayDocument(i);
			if (current.getBibScore() == -1)
				current.setBibScore(0);
			current.setBibScore(current.getBibScore() + 2);
		}
	}
	
	public int calculateCountOfBibliometricValue() {
		DisplayDocument current = null;
		int count = 0;
		for (int i = 0; i < this.getSize(); i++) {
			current = this.getDisplayDocument(i);
			if (current.getBibScore() > 0) {
				count++;
			}
		}
		return count;
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

		if (this.requestedDocument != null) {
			if (equalDocuments(document, this.requestedDocument)) {
				return;
			}
		}

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
		if (document1.calculateCleanTitle().equals(document2.calculateCleanTitle()))
			return true;
		else
			return false;
	}

	public List<DisplayDocument> getDocumentList() {
		return documentList;
	}

	public void eliminateDuplicates() {
		for (int i = 0; i < this.getSize(); i++) {
			for (int j = i; j < this.getSize(); j++) {
				if (this.getDisplayDocument(i).calculateCleanTitle().equals(this.getDisplayDocument(j).calculateCleanTitle())) {
					this.removeDisplayDocument(j);
					j--;
				}
			}
		}
	}

	public void removeDisplayDocument(int i) {
		this.getDocumentList().remove(i);
	}

	public DisplayDocument getDisplayDocument(int i) {
		return this.getDocumentList().get(i);
	}

	// ~~~~~~~~~~~~~~~~~GETTER AND SETTER~~~~~~~~~~~~~~~~~
	public DebugDetailsPerSet getDebugDetailsPerSet() {
		return debugDetailsPerSet;
	}

	@XmlElement(name = "debug_details")
	public void setDebugDetailsPerSet(DebugDetailsPerSet debugDetailsPerSet) {
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

	public String getBibliometric() {
		return this.debugDetailsPerSet.getBibliometric();
	}

	@XmlTransient
	public void setBibliometric(String bibliometric) {
		this.debugDetailsPerSet.setBibliometric(bibliometric);
	}

	public String getBibType() {
		return this.debugDetailsPerSet.getBibType();
	}

	@XmlTransient
	public void setBibType(String bibType) {
		this.debugDetailsPerSet.setBibType(bibType);
	}

	public String getBibSource() {
		return this.debugDetailsPerSet.getBibSource();
	}

	@XmlTransient
	public void setBibSource(String bibSource) {
		this.debugDetailsPerSet.setBibSource(bibSource);
	}

	public Long getStartTime() {
		return this.debugDetailsPerSet.getStartTime();
	}

	@XmlTransient
	public void setStartTime(Long startTime) {
		this.debugDetailsPerSet.setStartTime(startTime);
	}

	public Long getAfterAlgorithmChoosingTime() {
		return this.debugDetailsPerSet.getAlgorithmChoosingTime();
	}

	@XmlTransient
	public void setAfterAlgorithmChoosingTime(Long afterAlgorithmChoosingTime) {
		this.debugDetailsPerSet.setAlgorithmChoosingTime(afterAlgorithmChoosingTime);
	}

	public Long getAfterUserModelTime() {
		return this.debugDetailsPerSet.getUserModelTime();
	}

	@XmlTransient
	public void setAfterUserModelTime(Long afterUserModelTime) {
		this.debugDetailsPerSet.setUserModelTime(afterUserModelTime);
	}

	public Long getAfterAlgorithmExecutionTime() {
		return this.debugDetailsPerSet.getAlgorithmExecutionTime();
	}

	@XmlTransient
	public void setAfterAlgorithmExecutionTime(Long afterAlgorithmExecutionTime) {
		this.debugDetailsPerSet.setAlgorithmExecutionTime(afterAlgorithmExecutionTime);
	}

	public Long getAfterRerankTime() {
		return this.debugDetailsPerSet.getRerankTime();
	}

	@XmlTransient
	public void setAfterRerankTime(Long afterRerankTime) {
		this.debugDetailsPerSet.setRerankTime(afterRerankTime);
	}

	public String getAccessKeyHash() {
		return this.debugDetailsPerSet.getAccessKeyHash();
	}

	@XmlTransient
	public void setAccessKeyHash(String accessKeyHash) {
		this.debugDetailsPerSet.setAccessKeyHash(accessKeyHash);
	}

	public AlgorithmDetails getAlgorithmDetails() {
		return this.debugDetailsPerSet.getAlgoDetails();
	}

	@XmlTransient
	public void setAlgorithmDetails(AlgorithmDetails algoDetails) {
		this.debugDetailsPerSet.setAlgoDetails(algoDetails);
	}

	public List<Statistics> getRankStats() {
		return this.debugDetailsPerSet.getRankStats();
	}

	@XmlTransient
	public void setRankStats(List<Statistics> rankStats) {
		this.debugDetailsPerSet.setRankStats(rankStats);
	}

	public void addRankingStats(Statistics stats) {
		this.debugDetailsPerSet.addRankingStats(stats);
	}
	public boolean isFallbackRanking() {
		return this.debugDetailsPerSet.isFallbackRanking();
	}
	@XmlTransient
	public void setFallbackRanking(boolean fallbackRanking) {
		this.debugDetailsPerSet.setFallbackRanking(fallbackRanking);
	}
}
