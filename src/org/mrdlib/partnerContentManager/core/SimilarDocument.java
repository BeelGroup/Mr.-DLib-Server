package org.mrdlib.partnerContentManager.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mrdlib.api.response.DisplayDocument;

public class SimilarDocument implements ExternalDocumentRepresentation {

private String id;
private String title;
private String url;
private String repositoryName;
private String publisher;
private String year;
private List<String> authors = null;
private long simhash;
private double score;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

/**
* No args constructor for use in serialization
* 
*/
public SimilarDocument() {
}

/**
* 
* @param id
* @param authors
* @param title
* @param score
* @param simhash
* @param year
* @param repositoryName
* @param url
* @param publisher
*/
public SimilarDocument(String id, String title, String url, String repositoryName, String publisher, String year, List<String> authors, int simhash, double score) {
super();
this.id = id;
this.title = title;
this.url = url;
this.repositoryName = repositoryName;
this.publisher = publisher;
this.year = year;
this.authors = authors;
this.simhash = simhash;
this.score = score;
}

public String getId() {
return id;
}

public void setId(String id) {
this.id = id;
}

public String getTitle() {
return title;
}

public void setTitle(String title) {
this.title = title;
}

public String getUrl() {
return url;
}

public void setUrl(String url) {
this.url = url;
}

public String getRepositoryName() {
return repositoryName;
}

public void setRepositoryName(String repositoryName) {
this.repositoryName = repositoryName;
}

public String getPublisher() {
return publisher;
}

public void setPublisher(String publisher) {
this.publisher = publisher;
}

public String getYear() {
return year;
}

public void setYear(String year) {
this.year = year;
}

public List<String> getAuthors() {
return authors;
}

public void setAuthors(List<String> authors) {
this.authors = authors;
}

public long getSimhash() {
return simhash;
}

public void setSimhash(long simhash) {
this.simhash = simhash;
}

public double getScore() {
return score;
}

public void setScore(double score) {
this.score = score;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

@Override
public DisplayDocument convertToMDLDocument() {
	DisplayDocument mdlDoc = new DisplayDocument();
	mdlDoc.setRelevanceScoreFromAlgorithm(this.getScore());
	mdlDoc.setTitle(this.getTitle());
	mdlDoc.setOriginalDocumentId("core-" + this.getId());
	mdlDoc.setAuthorNames(this.getAuthors().toString());
	try{
		mdlDoc.setYear(Integer.parseInt(this.getYear()));
	} catch(NumberFormatException e){
		
	}
	if(this.getPublisher()!=null) mdlDoc.setPublishedIn(this.getPublisher());
	mdlDoc.setClickUrl(this.getUrl());
	return mdlDoc;
	
}

}