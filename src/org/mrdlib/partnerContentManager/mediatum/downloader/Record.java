package org.mrdlib.partnerContentManager.mediatum.downloader;

import java.util.ArrayList;

/**
 * Data structure of an OAI Dublin Core record, implementing http://www.openarchives.org/OAI/2.0/oai_dc.xsd.
 */
public class Record {

    private ArrayList<String> title;
    private ArrayList<String> creator;
    private ArrayList<String> subject;
    private ArrayList<String> description;
    private ArrayList<String> publisher;
    private ArrayList<String> contributor;
    private ArrayList<String> date;
    private ArrayList<String> type;
    private ArrayList<String> format;
    private ArrayList<String> identifier;
    private ArrayList<String> source;
    private ArrayList<String> language;
    private ArrayList<String> relation;
    private ArrayList<String> coverage;
    private ArrayList<String> rights;

    public ArrayList<String> getTitle() {
        return title;
    }

    public void setTitle(ArrayList<String> title) {
        this.title = title;
    }

    public ArrayList<String> getCreator() {
        return creator;
    }

    public void setCreator(ArrayList<String> creator) {
        this.creator = creator;
    }

    public ArrayList<String> getSubject() {
        return subject;
    }

    public void setSubject(ArrayList<String> subject) {
        this.subject = subject;
    }

    public ArrayList<String> getDescription() {
        return description;
    }

    public void setDescription(ArrayList<String> description) {
        this.description = description;
    }

    public ArrayList<String> getPublisher() {
        return publisher;
    }

    public void setPublisher(ArrayList<String> publisher) {
        this.publisher = publisher;
    }

    public ArrayList<String> getContributor() {
        return contributor;
    }

    public void setContributor(ArrayList<String> contributor) {
        this.contributor = contributor;
    }

    public ArrayList<String> getDate() {
        return date;
    }

    public void setDate(ArrayList<String> date) {
        this.date = date;
    }

    public ArrayList<String> getType() {
        return type;
    }

    public void setType(ArrayList<String> type) {
        this.type = type;
    }

    public ArrayList<String> getFormat() {
        return format;
    }

    public void setFormat(ArrayList<String> format) {
        this.format = format;
    }

    public ArrayList<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(ArrayList<String> identifier) {
        this.identifier = identifier;
    }

    public ArrayList<String> getSource() {
        return source;
    }

    public void setSource(ArrayList<String> source) {
        this.source = source;
    }

    public ArrayList<String> getLanguage() {
        return language;
    }

    public void setLanguage(ArrayList<String> language) {
        this.language = language;
    }

    public ArrayList<String> getRelation() {
        return relation;
    }

    public void setRelation(ArrayList<String> relation) {
        this.relation = relation;
    }

    public ArrayList<String> getCoverage() {
        return coverage;
    }

    public void setCoverage(ArrayList<String> coverage) {
        this.coverage = coverage;
    }

    public ArrayList<String> getRights() {
        return rights;
    }

    public void setRights(ArrayList<String> rights) {
        this.rights = rights;
    }
}
