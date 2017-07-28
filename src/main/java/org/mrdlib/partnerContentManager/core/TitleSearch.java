package org.mrdlib.partnerContentManager.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TitleSearch {

private String status;
private List<PostTitleData> data = null;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

/**
* No args constructor for use in serialization
* 
*/
public TitleSearch() {
}

/**
* 
* @param status
* @param data
*/
public TitleSearch(String status, List<PostTitleData> data) {
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

public List<PostTitleData> getData() {
return data;
}

public void setData(List<PostTitleData> data) {
this.data = data;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}