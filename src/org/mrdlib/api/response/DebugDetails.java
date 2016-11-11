package org.mrdlib.api.response;
/**
 * 
 * @author Millah
 * 
 * This class handles the representation for the DebugDetails, the algorithms parameter per recommendation.
 * The XML format is automatically generated through the class structure.
 */

public class DebugDetails {
	//rankings
	private int rankAfterAlgorithm = -1;
	private int rankAfterReRanking = -1;
	private int rankAfterShuffling = -1;
	private int rankDelivered = -1;
	
	private double relevanceScoreFromAlgorithm;
	private double bibScore;
	private double finalScore;
	
	//private int realRank;  //currently: afterReRank  |||  NEED: rankAfterAlg; rankAfterReRank; rankAfterShuffling; rankAfterDelivered
	//private double rankingValue; //currently: bibScore  ||| NEED: relevanceScoreFromAlg; metricValue; rankingValueFinal
	//private double textRelevanceScore; //currently: relevanceScoreFromAlg
	private int bibDocId; // ok
	
	public DebugDetails() {
	}

	public int getBibDocId() {
		return bibDocId;
	}
	
	public void setBibDocId(int bibDocId) {
		this.bibDocId = bibDocId;
	}

	public int getRankAfterAlgorithm() {
		return rankAfterAlgorithm;
	}

	public void setRankAfterAlgorithm(int rankAfterAlgorithm) {
		this.rankAfterAlgorithm = rankAfterAlgorithm;
	}

	public int getRankAfterReRanking() {
		return rankAfterReRanking;
	}

	public void setRankAfterReRanking(int rankAfterReRanking) {
		this.rankAfterReRanking = rankAfterReRanking;
	}

	public int getRankAfterShuffling() {
		return rankAfterShuffling;
	}

	public void setRankAfterShuffling(int rankAfterShuffling) {
		this.rankAfterShuffling = rankAfterShuffling;
	}

	public int getRankDelivered() {
		return rankDelivered;
	}

	public void setRankDelivered(int rankDelivered) {
		this.rankDelivered = rankDelivered;
	}

	public double getRelevanceScoreFromAlgorithm() {
		return relevanceScoreFromAlgorithm;
	}

	public void setRelevanceScoreFromAlgorithm(double relevanceScoreFromAlgorithm) {
		this.relevanceScoreFromAlgorithm = relevanceScoreFromAlgorithm;
	}

	public double getBibScore() {
		return bibScore;
	}

	public void setBibScore(double bibScore) {
		this.bibScore = bibScore;
	}

	public double getFinalScore() {
		return finalScore;
	}

	public void setFinalScore(double finalScore) {
		this.finalScore = finalScore;
	}

}
