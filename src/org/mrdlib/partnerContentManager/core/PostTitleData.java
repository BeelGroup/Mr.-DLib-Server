package org.mrdlib.partnerContentManager.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mrdlib.api.response.DisplayDocument;

public class PostTitleData implements ExternalDocumentRepresentation {

	private String id;
	private List<String> authors = null;
	private List<String> contributors = null;
	private String datePublished;
	private List<String> identifiers = null;
	private String publisher;
	private List<String> relations = null;
	private List<Repository> repositories = null;
	private List<String> subjects = null;
	private String title;
	private List<String> topics = null;
	private List<Object> types = null;
	private String year;
	private String fulltextIdentifier;
	private String oai;
	private String description;
	private Language language;
	private List<Journal> journals = null;
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public PostTitleData() {
	}

	/**
	 * 
	 * @param id
	 * @param fulltextIdentifier
	 * @param journals
	 * @param relations
	 * @param urls
	 */
	public PostTitleData(String id, List<String> authors, List<String> contributors, String datePublished,
			List<String> identifiers, String publisher, List<String> relations, List<Repository> repositories,
			List<String> subjects, String title, List<String> topics, List<Object> types, String year,
			String fulltextIdentifier, String oai, String description, Language language, List<Journal> journals) {
		super();
		this.id = id;
		this.authors = authors;
		this.contributors = contributors;
		this.datePublished = datePublished;
		this.identifiers = identifiers;
		this.publisher = publisher;
		this.relations = relations;
		this.repositories = repositories;
		this.subjects = subjects;
		this.title = title;
		this.topics = topics;
		this.types = types;
		this.year = year;
		this.fulltextIdentifier = fulltextIdentifier;
		this.oai = oai;
		this.description = description;
		this.language = language;
		this.journals = journals;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public List<String> getContributors() {
		return contributors;
	}

	public void setContributors(List<String> contributors) {
		this.contributors = contributors;
	}

	public String getDatePublished() {
		return datePublished;
	}

	public void setDatePublished(String datePublished) {
		this.datePublished = datePublished;
	}

	public List<String> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(List<String> identifiers) {
		this.identifiers = identifiers;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public List<String> getRelations() {
		return relations;
	}

	public void setRelations(List<String> relations) {
		this.relations = relations;
	}

	public List<Repository> getRepositories() {
		return repositories;
	}

	public void setRepositories(List<Repository> repositories) {
		this.repositories = repositories;
	}

	public List<String> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<String> subjects) {
		this.subjects = subjects;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getTopics() {
		return topics;
	}

	public void setTopics(List<String> topics) {
		this.topics = topics;
	}

	public List<Object> getTypes() {
		return types;
	}

	public void setTypes(List<Object> types) {
		this.types = types;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getFulltextIdentifier() {
		return fulltextIdentifier;
	}

	public void setFulltextIdentifier(String fulltextIdentifier) {
		this.fulltextIdentifier = fulltextIdentifier;
	}

	public String getOai() {
		return oai;
	}

	public void setOai(String oai) {
		this.oai = oai;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public List<Journal> getJournals() {
		return journals;
	}

	public void setJournals(List<Journal> journals) {
		this.journals = journals;
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
		mdlDoc.setOriginalDocumentId("core-" + this.getId());
		mdlDoc.setTitle(this.getTitle());
		mdlDoc.setAuthorNames(this.getAuthors().toString());
		try {
			mdlDoc.setYear(Integer.parseInt(this.getYear()));
		} catch (NumberFormatException e) {

		}
		if (this.getPublisher() != null)
			mdlDoc.setPublishedIn(this.getPublisher());
		mdlDoc.setRelevanceScoreFromAlgorithm(1.00);
		return mdlDoc;
	}

}