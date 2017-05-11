package org.mrdlib.partnerContentManager.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetArticleData{

private String id;
private List<Journal> journals = null;
private List<Object> relations = null;
private List<SimilarDocument> similarities = null;
private List<String> urls = null;
private String fulltextIdentifier;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

/**
* No args constructor for use in serialization
* 
*/
public GetArticleData() {
}

/**
* 
* @param id
* @param fulltextIdentifier
* @param journals
* @param relations
* @param urls
* @param similarities
*/
public GetArticleData(String id, List<Journal> journals, List<Object> relations, List<SimilarDocument> similarities, List<String> urls, String fulltextIdentifier) {
super();
this.id = id;
this.journals = journals;
this.relations = relations;
this.similarities = similarities;
this.urls = urls;
this.fulltextIdentifier = fulltextIdentifier;
}

public String getId() {
return id;
}

public void setId(String id) {
this.id = id;
}

public List<Journal> getJournals() {
return journals;
}

public void setJournals(List<Journal> journals) {
this.journals = journals;
}

public List<Object> getRelations() {
return relations;
}

public void setRelations(List<Object> relations) {
this.relations = relations;
}

public List<SimilarDocument> getSimilarities() {
return similarities;
}

public void setSimilarities(List<SimilarDocument> similarities) {
this.similarities = similarities;
}

public List<String> getUrls() {
return urls;
}

public void setUrls(List<String> urls) {
this.urls = urls;
}

public String getFulltextIdentifier() {
return fulltextIdentifier;
}

public void setFulltextIdentifier(String fulltextIdentifier) {
this.fulltextIdentifier = fulltextIdentifier;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}