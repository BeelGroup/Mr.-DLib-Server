package org.mrdlib.partnerContentManager.mediatum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.mrdlib.partnerContentManager.gesis.Abstract;
import org.mrdlib.partnerContentManager.gesis.Person;
import org.mrdlib.partnerContentManager.gesis.Tuple;

/**
 * Abstract class resembling Mr. DLibs database. Classes for holding converted partner content must extend it.
 * Getters and setters may be overwritten to handle specialities of partner's content.
 * 
 * @author wuestehube
 *
 */
public abstract class ConvertedPartnerContent {

	// TODO: compare with database, possibly edit
	private String documentPath;
	private String id;
	private String title;
	private String fulltitle;
	private String cleantitle;
	private String language;
	private ArrayList<Abstract> abstr = new ArrayList<Abstract>();
	private int year;
	private int facetYear;
	private Set<String> keywords = new HashSet<String>();
	private String type;
	private Set<String> typeSet = new HashSet<String>();
	private String publishedIn;
	private int publishedInRank;
	private LinkedHashSet<Person> authors = new LinkedHashSet<Person>();
	
	// TODO: possibly remove
	private Map<String, String> typeMap = new HashMap<String, String>();
	private Map<String, String> languageMap = new HashMap<String, String>();
	private Map<Tuple, String> typeResolveMap = new HashMap<Tuple, String>();
	
	public String getDocumentPath() {
		return documentPath;
	}
	
	public void setDocumentPath(String documentPath) {
		this.documentPath = documentPath;
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
	
	public String getFulltitle() {
		return fulltitle;
	}
	
	public void setFulltitle(String fulltitle) {
		this.fulltitle = fulltitle;
	}
	
	public String getCleantitle() {
		return cleantitle;
	}
	
	public void setCleantitle(String cleantitle) {
		this.cleantitle = cleantitle;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public ArrayList<Abstract> getAbstr() {
		return abstr;
	}
	
	public void setAbstr(ArrayList<Abstract> abstr) {
		this.abstr = abstr;
	}
	
	public int getYear() {
		return year;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public int getFacetYear() {
		return facetYear;
	}
	
	public void setFacetYear(int facetYear) {
		this.facetYear = facetYear;
	}
	
	public Set<String> getKeywords() {
		return keywords;
	}
	
	public void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Set<String> getTypeSet() {
		return typeSet;
	}
	
	public void setTypeSet(Set<String> typeSet) {
		this.typeSet = typeSet;
	}
	
	public String getPublishedIn() {
		return publishedIn;
	}
	
	public void setPublishedIn(String publishedIn) {
		this.publishedIn = publishedIn;
	}
	
	public int getPublishedInRank() {
		return publishedInRank;
	}
	
	public void setPublishedInRank(int publishedInRank) {
		this.publishedInRank = publishedInRank;
	}
	
	public LinkedHashSet<Person> getAuthors() {
		return authors;
	}
	
	public void setAuthors(LinkedHashSet<Person> authors) {
		this.authors = authors;
	}
	
	public Map<String, String> getTypeMap() {
		return typeMap;
	}
	
	public void setTypeMap(Map<String, String> typeMap) {
		this.typeMap = typeMap;
	}
	
	public Map<String, String> getLanguageMap() {
		return languageMap;
	}
	
	public void setLanguageMap(Map<String, String> languageMap) {
		this.languageMap = languageMap;
	}
	
	public Map<Tuple, String> getTypeResolveMap() {
		return typeResolveMap;
	}
	
	public void setTypeResolveMap(Map<Tuple, String> typeResolveMap) {
		this.typeResolveMap = typeResolveMap;
	}
	
}
