package org.mrdlib.partnerContentManager.core;

import java.util.HashMap;
import java.util.Map;

public class Language {

private String code;
private long id;
private String name;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

/**
* No args constructor for use in serialization
* 
*/
public Language() {
}

/**
* 
* @param id
* @param name
* @param code
*/
public Language(String code, long id, String name) {
super();
this.code = code;
this.id = id;
this.name = name;
}

public String getCode() {
return code;
}

public void setCode(String code) {
this.code = code;
}

public long getId() {
return id;
}

public void setId(long id) {
this.id = id;
}

public String getName() {
return name;
}

public void setName(String name) {
this.name = name;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}