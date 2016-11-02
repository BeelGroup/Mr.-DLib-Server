package org.mrdlib.display;

public class DebugDetails {
	private int realRank;
	private double rankingValue;
	private double textRelevanceScore;
	private int bibId;
	
	public DebugDetails() {
	}
	
	public int getBibId() {
		return bibId;
	}
	
	public void setBibId(int bibId) {
		this.bibId = bibId;
	}

	public double getTextRelevanceScore() {
		return textRelevanceScore;
	}

	public void setTextRelevanceScore(double textRelevanceScore) {
		this.textRelevanceScore = textRelevanceScore;
	}

	public double getRankingValue() {
		return rankingValue;
	}

	public void setRankingValue(double rankingValue) {
		this.rankingValue = rankingValue;
	}

	public int getRealRank() {
		return realRank;
	}
	
	public void setRealRank(int realRank) {
		this.realRank = realRank;
	}
}
