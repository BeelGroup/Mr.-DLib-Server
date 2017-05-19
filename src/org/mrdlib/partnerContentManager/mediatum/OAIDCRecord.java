package org.mrdlib.partnerContentManager.mediatum;

import java.util.ArrayList;

/**
 * Data structure of an OAI Dublin Core record, implementing http://www.openarchives.org/OAI/2.0/oai_dc.xsd.
 */
public class OAIDCRecord {
	private ArrayList<String> titles;
    private ArrayList<String> creators;
    private ArrayList<String> subjects;
    private ArrayList<String> descriptions;
    private ArrayList<String> publishers;
    private ArrayList<String> contributors;
    private ArrayList<String> dates;
    private ArrayList<String> types;
    private ArrayList<String> formats;
    private ArrayList<String> identifiers;
    private ArrayList<String> sources;
    private ArrayList<String> languages;
    private ArrayList<String> relations;
    private ArrayList<String> coverages;
    private ArrayList<String> rights;

    public OAIDCRecord() {
    	titles = new ArrayList<String>();
    	creators = new ArrayList<String>();
    	subjects = new ArrayList<String>();
    	descriptions = new ArrayList<String>();
    	publishers = new ArrayList<String>();
    	contributors = new ArrayList<String>();
    	dates = new ArrayList<String>();
    	types = new ArrayList<String>();
    	formats = new ArrayList<String>();
    	identifiers = new ArrayList<String>();
    	sources = new ArrayList<String>();
    	languages = new ArrayList<String>();
    	relations = new ArrayList<String>();
    	coverages = new ArrayList<String>();
    	rights = new ArrayList<String>();
    }
    
    public ArrayList<String> getTitles() {
        return titles;
    }

    public void setTitles(ArrayList<String> titles) {
        this.titles = titles;
    }
    
    public void addTitle(String title) {
    	this.titles.add(title);
    }

    public ArrayList<String> getCreators() {
        return creators;
    }

    public void setCreators(ArrayList<String> creators) {
        this.creators = creators;
    }
    
    public void addCreator(String creator) {
    	this.creators.add(creator);
    }

    public ArrayList<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(ArrayList<String> subjects) {
        this.subjects = subjects;
    }
    
    public void addSubject(String subject) {
    	this.subjects.add(subject);
    }

    public ArrayList<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(ArrayList<String> descriptions) {
        this.descriptions = descriptions;
    }
    
    public void addDescription(String description) {
    	this.descriptions.add(description);
    }

    public ArrayList<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(ArrayList<String> publishers) {
        this.publishers = publishers;
    }
    
    public void addPublisher(String publisher) {
    	this.publishers.add(publisher);
    }

    public ArrayList<String> getContributors() {
        return contributors;
    }

    public void setContributors(ArrayList<String> contributors) {
        this.contributors = contributors;
    }
    
    public void addContributor(String contributor) {
    	this.contributors.add(contributor);
    }

    public ArrayList<String> getDates() {
        return dates;
    }

    public void setDates(ArrayList<String> dates) {
        this.dates = dates;
    }
    
    public void addDate(String date) {
    	this.dates.add(date);
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }
    
    public void addType(String type) {
    	this.types.add(type);
    }

    public ArrayList<String> getFormats() {
        return formats;
    }

    public void setFormats(ArrayList<String> formats) {
        this.formats = formats;
    }
    
    public void addFormat(String format) {
    	this.formats.add(format);
    }

    public ArrayList<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(ArrayList<String> identifiers) {
        this.identifiers = identifiers;
    }
    
    public void addIdentifier(String identifier) {
    	this.identifiers.add(identifier);
    }

    public ArrayList<String> getSources() {
        return sources;
    }

    public void setSources(ArrayList<String> sources) {
        this.sources = sources;
    }
    
    public void addSource(String source) {
    	this.sources.add(source);
    }

    public ArrayList<String> getLanguages() {
        return languages;
    }

    public void setLanguages(ArrayList<String> languages) {
        this.languages = languages;
    }
    
    public void addLanguage(String language) {
    	this.languages.add(language);
    }

    public ArrayList<String> getRelations() {
        return relations;
    }

    public void setRelations(ArrayList<String> relations) {
        this.relations = relations;
    }
    
    public void addRelation(String relation) {
    	this.relations.add(relation);
    }

    public ArrayList<String> getCoverages() {
        return coverages;
    }

    public void setCoverages(ArrayList<String> coverages) {
        this.coverages = coverages;
    }
    
    public void addCoverage(String coverage) {
    	this.coverages.add(coverage);
    }

    public ArrayList<String> getRights() {
        return rights;
    }

    public void setRights(ArrayList<String> rights) {
        this.rights = rights;
    }
    
    public void addRight(String right) {
    	this.rights.add(right);
    }
    
    private String arrayListToString(ArrayList<String> arrayList) {
    	StringBuilder stringBuilder = new StringBuilder();
    	
    	int i = 0;
    	for (String string : arrayList)
    	{
    		stringBuilder.append(string);
    		i++;
    		
    		if (i < arrayList.size()) {
    			stringBuilder.append(", ");
    		}
    	}

    	return stringBuilder.toString();
    }
    
    /**
     * Custom toString() implementation.
     */
    public String toString() {
    	String oaidcRecord = "[";
    	
    	oaidcRecord += "titles = " + arrayListToString(titles) + ", ";
    	oaidcRecord += "creators = " + arrayListToString(creators) + ", ";
    	oaidcRecord += "subjects = " + arrayListToString(subjects) + ", ";
    	oaidcRecord += "descriptions = " + arrayListToString(descriptions) + ", ";
    	oaidcRecord += "publishers = " + arrayListToString(publishers) + ", ";
    	oaidcRecord += "contributors = " + arrayListToString(contributors) + ", ";
    	oaidcRecord += "dates = " + arrayListToString(dates) + ", ";
    	oaidcRecord += "types = " + arrayListToString(types) + ", ";
    	oaidcRecord += "formats = " + arrayListToString(formats) + ", ";
    	oaidcRecord += "identifiers = " + arrayListToString(identifiers) + ", ";
    	oaidcRecord += "sources = " + arrayListToString(sources) + ", ";
    	oaidcRecord += "languages = " + arrayListToString(languages) + ", ";
    	oaidcRecord += "relations = " + arrayListToString(relations) + ", ";
    	oaidcRecord += "coverages = " + arrayListToString(coverages) + ", ";
    	oaidcRecord += "rights = " + arrayListToString(rights);
    	
    	oaidcRecord += "]";
    	
    	return oaidcRecord;
    }
    
}
