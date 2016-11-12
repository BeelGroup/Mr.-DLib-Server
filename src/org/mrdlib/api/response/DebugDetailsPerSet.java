package org.mrdlib.api.response;

import org.mrdlib.recommendation.algorithm.RelatedDocuments;

/**
 * 
 * @author Millah
 * 
 *         This class handles the representation for the DebugDetails, the
 *         algorithms parameter per recommendation set. The XML format is
 *         automatically generated through the class structure.
 */
public class DebugDetailsPerSet {
	private String recommendationApproach;
	private double percentageRankingValue;

	private int numberOfCandidatesToReRank;// number of items choosed to rerank 
	private String reRankingCombination;
	private String rankingOrder;
	private RelatedDocuments rdg; // sid changes
	private long numberOfReturnedResults; // currently: numberFromAlgReturns
	private int desiredNumberFromAlgorithm; //implement!!
	private int numberOfDisplayedRecommendations = 1; 
	private boolean fallback; // Sid
	private int recommendationAlgorithmId;
	private int bibliometricId = -1;
	private boolean bibliometricReRanking = true;
	private boolean shuffled = false;
	private boolean removedDuplicates = true; //find a solution, that is modular
	// NEED accesskey
	// NEED timestamps: start; afterChooseOfAlg; afterUserModel; afterAlg;
	// afterReRank; end?
	// NEED metric
	// NEED type
	// NEED source

	public DebugDetailsPerSet() {
	}	
	
	public int getNumberOfDisplayedRecommendations() {
		return numberOfDisplayedRecommendations;
	}

	public void setNumberOfDisplayedRecommendations(int numberOfDisplayedRecommendations) {
		this.numberOfDisplayedRecommendations = numberOfDisplayedRecommendations;
	}

	public int getDesiredNumberFromAlgorithm() {
		return desiredNumberFromAlgorithm;
	}

	public void setDesiredNumberFromAlgorithm(int desiredNumberFromAlgorithm) {
		this.desiredNumberFromAlgorithm = desiredNumberFromAlgorithm;
	}

	public boolean isShuffled() {
		return shuffled;
	}

	public void setShuffled(boolean shuffled) {
		this.shuffled = shuffled;
	}

	public boolean isRemovedDuplicates() {
		return removedDuplicates;
	}

	public void setRemovedDuplicates(boolean removedDuplicates) {
		this.removedDuplicates = removedDuplicates;
	}

	public int getNumberOfCandidatesToReRank() {
		return numberOfCandidatesToReRank;
	}
	
	public void setNumberOfCandidatesToReRank(int numberOfCandidatesToReRank) {
		this.numberOfCandidatesToReRank = numberOfCandidatesToReRank;
	}

	public String getReRankingCombination() {
		return reRankingCombination;
	}

	public void setReRankingCombination(String reRankingCombination) {
		this.reRankingCombination = reRankingCombination;
	}

	public String getRankingOrder() {
		return rankingOrder;
	}

	public void setRankingOrder(String rankingOrder) {
		this.rankingOrder = rankingOrder;
	}

	public RelatedDocuments getRdg() {
		return rdg;
	}

	public void setRdg(RelatedDocuments rdg) {
		this.rdg = rdg;
		this.recommendationApproach = rdg.algorithmLoggingInfo.getName();
	}
	
	public long getNumberOfReturnedResults() {
		return numberOfReturnedResults;
	}

	public void setNumberOfReturnedResults(long numberOfReturnedResults) {
		this.numberOfReturnedResults = numberOfReturnedResults;
	}
	
	public boolean isFallback() {
		return fallback;
	}

	public void setFallback(boolean fallback) {
		this.fallback = fallback;
	}

	public String getRecommendationAlgorithmId() {
		return Integer.toString(recommendationAlgorithmId);
	}

	public void setRecommendationAlgorithmId(int recommendationAlgorithmId) {
		this.recommendationAlgorithmId = recommendationAlgorithmId;
	}

	public int getBibliometricId() {
		return bibliometricId;
	}

	public void setBibliometricId(int bibliometricId) {
		this.bibliometricId = bibliometricId;
	}

	public boolean isBibliometricReRanking() {
		return bibliometricReRanking;
	}

	public void setBibliometricReRanking(boolean bibliometricReRanking) {
		this.bibliometricReRanking = bibliometricReRanking;
	}

	public String getRecommendationApproach() {
		return recommendationApproach;
	}

	public void setRecommendationApproach(String recommendationApproach) {
		this.recommendationApproach = recommendationApproach;
	}

	public double getPercentageRankingValue() {
		return percentageRankingValue;
	}

	public void setPercentageRankingValue(double percentageRankingValue) {
		this.percentageRankingValue = percentageRankingValue;
	}

}
