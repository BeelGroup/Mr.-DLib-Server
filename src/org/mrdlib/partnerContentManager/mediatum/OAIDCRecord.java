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

    public ArrayList<String> getTitles() {
        return titles;
    }

    public void setTitles(ArrayList<String> titles) {
        this.titles = titles;
    }

    public ArrayList<String> getCreators() {
        return creators;
    }

    public void setCreators(ArrayList<String> creators) {
        this.creators = creators;
    }

    public ArrayList<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(ArrayList<String> subjects) {
        this.subjects = subjects;
    }

    public ArrayList<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(ArrayList<String> descriptions) {
        this.descriptions = descriptions;
    }

    public ArrayList<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(ArrayList<String> publishers) {
        this.publishers = publishers;
    }

    public ArrayList<String> getContributors() {
        return contributors;
    }

    public void setContributors(ArrayList<String> contributors) {
        this.contributors = contributors;
    }

    public ArrayList<String> getDates() {
        return dates;
    }

    public void setDates(ArrayList<String> dates) {
        this.dates = dates;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }

    public ArrayList<String> getFormats() {
        return formats;
    }

    public void setFormats(ArrayList<String> formats) {
        this.formats = formats;
    }

    public ArrayList<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(ArrayList<String> identifiers) {
        this.identifiers = identifiers;
    }

    public ArrayList<String> getSources() {
        return sources;
    }

    public void setSources(ArrayList<String> sources) {
        this.sources = sources;
    }

    public ArrayList<String> getLanguages() {
        return languages;
    }

    public void setLanguages(ArrayList<String> languages) {
        this.languages = languages;
    }

    public ArrayList<String> getRelations() {
        return relations;
    }

    public void setRelations(ArrayList<String> relations) {
        this.relations = relations;
    }

    public ArrayList<String> getCoverages() {
        return coverages;
    }

    public void setCoverages(ArrayList<String> coverages) {
        this.coverages = coverages;
    }

    public ArrayList<String> getRights() {
        return rights;
    }

    public void setRights(ArrayList<String> rights) {
        this.rights = rights;
    }
}
