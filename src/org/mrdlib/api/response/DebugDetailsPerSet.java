package org.mrdlib.api.response;

import org.mrdlib.recommendation.algorithm.AlgorithmDetails;

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

	private boolean bibliometricReRanking = true;

	private int numberOfCandidatesToReRank;
	private String reRankingCombination;
	private String rankingOrder;
	private int bibliometricId = -1;
	private String bibliometric;
	private String bibType;
	private String bibSource;

	private AlgorithmDetails algoDetails; 
	private long numberOfReturnedResults; // currently: numberFromAlgReturns
	private int desiredNumberFromAlgorithm; // implement!!
	private int numberOfDisplayedRecommendations = 1;
	private boolean fallback; // Sid
	private int recommendationAlgorithmId;
	
	private boolean shuffled = false;

	private boolean removedDuplicates = false;
	
	private Long startTime;
	private Long algorithmChoosingTime;
	private Long userModelTime;
	private Long algorithmExecutionTime;
	private Long rerankTime;

	private String accessKeyHash;
	// NEED timestamps: end?

	public DebugDetailsPerSet() {
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getAlgorithmChoosingTime() {
		return algorithmChoosingTime;
	}

	public void setAlgorithmChoosingTime(Long afterAlgorithmChoosingTime) {
		this.algorithmChoosingTime = afterAlgorithmChoosingTime;
	}

	public Long getUserModelTime() {
		return userModelTime;
	}

	public void setUserModelTime(Long afterUserModelTime) {
		this.userModelTime = afterUserModelTime;
	}

	public Long getAlgorithmExecutionTime() {
		return algorithmExecutionTime;
	}

	public void setAlgorithmExecutionTime(Long afterAlgorithmExecutionTime) {
		this.algorithmExecutionTime = afterAlgorithmExecutionTime;
	}

	public Long getRerankTime() {
		return rerankTime;
	}

	public void setRerankTime(Long afterRerankTime) {
		this.rerankTime = afterRerankTime;
	}

	public String getBibliometric() {
		return bibliometric;
	}

	public void setBibliometric(String bibliometric) {
		this.bibliometric = bibliometric;
	}

	public String getBibType() {
		return bibType;
	}

	public void setBibType(String bibType) {
		this.bibType = bibType;
	}

	public String getBibSource() {
		return bibSource;
	}

	public void setBibSource(String bibSource) {
		this.bibSource = bibSource;
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

	/**
	 * @return the accessKeyHash
	 */
	public String getAccessKeyHash() {
		return accessKeyHash;
	}

	/**
	 * @param accessKeyHash
	 *            the accessKeyHash to set
	 */
	public void setAccessKeyHash(String accessKeyHash) {
		this.accessKeyHash = accessKeyHash;
	}

	public AlgorithmDetails getAlgoDetails() {
		return algoDetails;
	}

	public void setAlgoDetails(AlgorithmDetails details) {
		this.algoDetails = details;
	}

}
