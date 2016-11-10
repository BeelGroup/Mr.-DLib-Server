package org.mrdlib.api.response;

/**
 * 
 * @author Millah
 * 
 * This class handles the representation for the DebugDetails, the algorithms parameter per recommendation set.
 * The XML format is automatically generated through the class structure.
 */
public class DebugDetailsPerSet {
	private String recommendationApproach;
	private double percentageRankingValue;
	
	
	public DebugDetailsPerSet() {
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
