package org.mrdlib.recommendation.algorithm;

import org.mrdlib.api.manager.UnknownException;

public class AlgorithmDetails {

	private String name;
	private String recommendationClass;
	private boolean languageRestriction;

	private boolean contentBased;
	private String cbfTextFields;
	private String cbfFeatureType;
	private String cbfFeatureCount;

	private boolean humanCuratedRecommendations = false;
	private String category;
	
	private boolean fallback = false;
	private int recommendationAlgorithmId;
	
	public AlgorithmDetails(String name, String recommendationClass, boolean languageRestriction) {
		this.name = name;
		this.recommendationClass = recommendationClass;
		this.languageRestriction = languageRestriction;
	}

	public AlgorithmDetails(String name, String recommendationClass, boolean languageRestriction, String category) {
		if (recommendationClass != "stereotypes" && recommendationClass != "most_popular") {
			throw new UnknownException(
					"This Algorithm Details constructor can only be used for Stereotype or Most Popular Recommendation approaches");
		} else {
			this.name = name;
			this.recommendationClass = recommendationClass;
			this.languageRestriction = languageRestriction;
			this.category = category;
			this.setHumanCuratedRecommendations(true);
		}
	}

	public AlgorithmDetails(String name, String recommendationClass, boolean languageRestriction, String cbfTextFields,
			String cbfFeatureType, String cbfFeatureCount) {
		super();
		this.name = name;
		this.recommendationClass = recommendationClass;
		this.languageRestriction = languageRestriction;
		this.cbfTextFields = cbfTextFields;
		this.cbfFeatureType = cbfFeatureType;
		this.cbfFeatureCount = cbfFeatureCount;
		this.setContentBased(true);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the recommendationClass
	 */
	public String getRecommendationClass() {
		return recommendationClass;
	}

	/**
	 * @return the languageRestriction
	 */
	public boolean isLanguageRestriction() {
		return languageRestriction;
	}

	/**
	 * @return the contentBased
	 */
	public boolean isContentBased() {
		return contentBased;
	}

	/**
	 * @return the cbfTextFields
	 */
	public String getCbfTextFields() {
		return cbfTextFields;
	}

	/**
	 * @return the cbfFeatureType
	 */
	public String getCbfFeatureType() {
		return cbfFeatureType;
	}

	/**
	 * @return the cbfFeatureCount
	 */
	public String getCbfFeatureCount() {
		return cbfFeatureCount;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param recommendationClass
	 *            the recommendationClass to set
	 */
	public void setRecommendationClass(String recommendationClass) {
		this.recommendationClass = recommendationClass;
	}

	/**
	 * @param languageRestriction
	 *            the languageRestriction to set
	 */
	public void setLanguageRestriction(boolean languageRestriction) {
		this.languageRestriction = languageRestriction;
	}

	/**
	 * @param contentBased
	 *            the contentBased to set
	 */
	public void setContentBased(boolean contentBased) {
		this.contentBased = contentBased;
	}

	/**
	 * @param cbfTextFields
	 *            the cbfTextFields to set
	 */
	public void setCbfTextFields(String cbfTextFields) {
		this.cbfTextFields = cbfTextFields;
	}

	/**
	 * @param cbfFeatureType
	 *            the cbfFeatureType to set
	 */
	public void setCbfFeatureType(String cbfFeatureType) {
		this.cbfFeatureType = cbfFeatureType;
	}

	/**
	 * @param cbfFeatureCount
	 *            the cbfFeatureCount to set
	 */
	public void setCbfFeatureCount(String cbfFeatureCount) {
		this.cbfFeatureCount = cbfFeatureCount;
	}

	

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the humanCuratedRecommendations
	 */
	public boolean isHumanCuratedRecommendations() {
		return humanCuratedRecommendations;
	}

	/**
	 * @param humanCuratedRecommendations
	 *            the humanCuratedRecommendations to set
	 */
	public void setHumanCuratedRecommendations(boolean humanCuratedRecommendations) {
		this.humanCuratedRecommendations = humanCuratedRecommendations;
	}

	/**
	 * @return the fallback
	 */
	public boolean isFallback() {
		return fallback;
	}

	/**
	 * @param fallback the fallback to set
	 */
	public void setFallback(boolean fallback) {
		this.fallback = fallback;
	}

	/**
	 * @return the recommendationAlgorithmId
	 */
	public int getRecommendationAlgorithmId() {
		return recommendationAlgorithmId;
	}

	/**
	 * @param recommendationAlgorithmId the recommendationAlgorithmId to set
	 */
	public void setRecommendationAlgorithmId(int recommendationAlgorithmId) {
		this.recommendationAlgorithmId = recommendationAlgorithmId;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AlgorithmDetails [" + (name != null ? "name=" + name + ", " : "")
				+ (recommendationClass != null ? "recommendationClass=" + recommendationClass + ", " : "")
				+ "languageRestriction=" + languageRestriction + ", contentBased=" + contentBased + ", "
				+ (cbfTextFields != null ? "cbfTextFields=" + cbfTextFields + ", " : "")
				+ (cbfFeatureType != null ? "cbfFeatureType=" + cbfFeatureType + ", " : "")
				+ (cbfFeatureCount != null ? "cbfFeatureCount=" + cbfFeatureCount + ", " : "")
				+ "humanCuratedRecommendations=" + humanCuratedRecommendations + ", "
				+ (category != null ? "category=" + category + ", " : "") + "fallback=" + fallback
				+ ", recommendationAlgorithmId=" + recommendationAlgorithmId + "]";
	}
}
