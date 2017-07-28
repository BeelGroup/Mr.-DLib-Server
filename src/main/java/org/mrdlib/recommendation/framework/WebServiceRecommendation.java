package org.mrdlib.recommendation.framework;

public class WebServiceRecommendation{

    public WebServiceRecommendation() {}
    private String id;
    private double similarity;

    public String getId() {
	return id;
    }
    public double getSimilarity() {
	return similarity;
    }
    public void setId(String id) {
	this.id = id;
    }
    public void setSimilarity(double similarity) {
	this.similarity = similarity;
    }
}
