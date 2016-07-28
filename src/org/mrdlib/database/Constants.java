package org.mrdlib.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 * @author Millah
 * 
 * This class reads in the constants from a property file, which is defined in configPath.
 */

public class Constants {

	private String configPath = "config.properties";	//JB: where is the file config.properties?

	//db connection properties
	private String dbClass;
	private String db;
	private String url;
	private String user;
	private String password;
	
	//db tables
	private String documents;
	private String persons;
	private String docPers;
	private String collections;
	private String abstracts;
	
	//db table abstract
	private String abstractId;
	private String abstractDocumentId;
	private String abstractLanguage;
	private String abstr;

	//db table document
	private String documentID;
	private String idOriginal;
	private String documentCollectionID;
	private String title;
	private String titleClean;
	private String authors;
	private String publishedIn;
	private String language;
	private String year;
	private String type;
	private String keywords;
	
	//table collection
	private String collectionID;
	private String collectionShortName;
	private String collectionName;
	private String organization;
	
	//db table person
	private String personID;
	private String firstname;
	private String middlename;
	private String surname;
	private String unstructured;
	
	//db table doc_pers
	private String documentIDInDocPers;
	private String personIDInDocPers;
	private String rank;
	
	//solr
	private String solrWebService;
	private String solrMrdlib;
	private String solrCollectionShortName;
	
	//collections
	private String gesis;
	private String gesisCollectionLink;


	// load the config file
	public Constants() {

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = getClass().getClassLoader().getResourceAsStream(configPath);
			prop.load(input);

			// get the property value
			this.dbClass = prop.getProperty("dbClass");
			this.db = prop.getProperty("db");
			this.url = prop.getProperty("url");
			this.user = prop.getProperty("user");
			this.password = prop.getProperty("password");
			
			this.documents = prop.getProperty("documents");
			this.persons = prop.getProperty("persons");
			this.docPers = prop.getProperty("doc_pers");
			this.collections = prop.getProperty("collections");
			this.abstracts = prop.getProperty("abstracts");

			this.abstractId =prop.getProperty("abstractId");
			this.abstractDocumentId = prop.getProperty("abstractDocumentId");
			this.abstractLanguage = prop.getProperty("abstractLanguage");
			this.abstr = prop.getProperty("abstract");
			
			this.documentID = prop.getProperty("documentId");
			this.idOriginal = prop.getProperty("idOriginal");
			this.documentCollectionID = prop.getProperty("documentCollectionId");
			this.title = prop.getProperty("title");
			this.titleClean = prop.getProperty("titleClean");
			this.authors = prop.getProperty("authors");
			this.publishedIn = prop.getProperty("publication");
			this.language = prop.getProperty("language");
			this.year = prop.getProperty("year");
			this.type = prop.getProperty("type");
			this.keywords = prop.getProperty("keywords");
			
			this.collectionID = prop.getProperty("collectionId");
			this.collectionShortName = prop.getProperty("collectionShortName");
			this.collectionName = prop.getProperty("collectionName");
			this.organization = prop.getProperty("organization");
			
			this.personID = prop.getProperty("personId");
			this.firstname = prop.getProperty("firstname");
			this.middlename = prop.getProperty("middlename");
			this.surname = prop.getProperty("surname");
			this.unstructured = prop.getProperty("unstructured");
			
			this.documentIDInDocPers = prop.getProperty("documentIdInDocPers");
			this.personIDInDocPers = prop.getProperty("personIdInDocPers");
			this.rank = prop.getProperty("authorRank");
			
			this.solrWebService = prop.getProperty("solrWebService");
			this.solrMrdlib = prop.getProperty("solrMrdlib");
			this.solrCollectionShortName = prop.getProperty("solrCollectionShortName");
			
			this.gesisCollectionLink = prop.getProperty("gesisCollectionLink");
			this.gesis = prop.getProperty("gesis");

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}	
	public String getSolrCollectionShortName() {
		return solrCollectionShortName;
	}
	public String getGesisCollectionLink() {
		return gesisCollectionLink;
	}

	public String getSolrWebService() {
		return solrWebService;
	}

	public String getSolrMrdlib() {
		return solrMrdlib;
	}
	public String getGesis() {
		return gesis;
	}
	
	public String getRank() {
		return rank;
	}

	public String getAbstractId() {
		return abstractId;
	}

	public String getAbstractDocumentId() {
		return abstractDocumentId;
	}

	public String getAbstractLanguage() {
		return abstractLanguage;
	}

	public String getAbstr() {
		return abstr;
	}

	public String getCollectionID() {
		return collectionID;
	}

	public String getCollectionShortName() {
		return collectionShortName;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public String getOrganization() {
		return organization;
	}
	
	public String getCollections() {
		return collections;
	}

	public String getPersonID() {
		return personID;
	}

	public String getType() {
		return type;
	}

	public String getKeywords() {
		return keywords;
	}

	public String getLanguage() {
		return language;
	}

	public String getTitleClean() {
		return titleClean;
	}

	public String getDocumentCollectionID() {
		return documentCollectionID;
	}

	public String getDocumentIDInDocPers() {
		return documentIDInDocPers;
	}

	public String getPersonIDInDocPers() {
		return personIDInDocPers;
	}
	
	public String getDocuments() {
		return documents;
	}

	public String getPersons() {
		return persons;
	}

	public String getDocPers() {
		return docPers;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getMiddlename() {
		return middlename;
	}

	public String getSurname() {
		return surname;
	}

	public String getUnstructured() {
		return unstructured;
	}

	public String getDbClass() {
		return dbClass;
	}

	public String getDb() {
		return db;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getDocumentId() {
		return documentID;
	}

	public String getIdOriginal() {
		return idOriginal;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthors() {
		return authors;
	}

	public String getPublishedId() {
		return publishedIn;
	}

	public String getYear() {
		return year;
	}
	public String getAbstracts() {
		return abstracts;
	}
}