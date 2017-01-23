package org.mrdlib.api.response;

import java.io.Serializable;

public class Statistics implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String type;
	private double percentageRankingValue;
	private double rankVMin;
	private double rankVMax;
	private double rankVMean;
	private double rankVMedian;
	private double rankVMode;
	
	
	public Statistics() {}
	
	
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public double getPercentageRankingValue() {
		return percentageRankingValue;
	}
	public void setPercentageRankingValue(double percentageRankingValue) {
		this.percentageRankingValue = percentageRankingValue;
	}
	public double getRankVMin() {
		return rankVMin;
	}
	public void setRankVMin(double rankVMin) {
		this.rankVMin = rankVMin;
	}
	public double getRankVMax() {
		return rankVMax;
	}
	public void setRankVMax(double rankVMax) {
		this.rankVMax = rankVMax;
	}
	public double getRankVMean() {
		return rankVMean;
	}
	public void setRankVMean(double rankVMean) {
		this.rankVMean = rankVMean;
	}
	public double getRankVMedian() {
		return rankVMedian;
	}
	public void setRankVMedian(double rankVMedian) {
		this.rankVMedian = rankVMedian;
	}
	public double getRankVMode() {
		return rankVMode;
	}
	public void setRankVMode(double rankVMode) {
		this.rankVMode = rankVMode;
	}

}
