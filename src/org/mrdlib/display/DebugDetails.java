package org.mrdlib.display;

public class DebugDetails {
	private int realRank;
	private double rankingValue;
	private double solrScore;
	private int bibId;
	
	public DebugDetails() {
	}
	
	public int getBibId() {
		return bibId;
	}
	
	public void setBibId(int bibId) {
		this.bibId = bibId;
	}

	public double getSolrScore() {
		return solrScore;
	}

	public void setSolrScore(double solrScore) {
		this.solrScore = solrScore;
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
