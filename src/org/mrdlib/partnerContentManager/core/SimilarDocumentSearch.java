package org.mrdlib.partnerContentManager.core;

import java.util.HashMap;
import java.util.Map;

public class SimilarDocumentSearch {

private String status;
private GetArticleData data;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

/**
* No args constructor for use in serialization
* 
*/
public SimilarDocumentSearch() {
}

/**
* 
* @param status
* @param data
*/
public SimilarDocumentSearch(String status, GetArticleData data) {
super();
this.status = status;
this.data = data;
}

public String getStatus() {
return status;
}

public void setStatus(String status) {
this.status = status;
}

public GetArticleData getData() {
return data;
}

public void setData(GetArticleData data) {
this.data = data;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}