package org.mrdlib;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Millah
 * 
 *         This class reads in the constants from a property file, which is
 *         defined in configPath.
 */

public class Constants {

	private String tomcatConfigPath = "config.properties";

	// db connection properties
	private String dbClass;
	private String db;
	private String url;
	private String user;
	private String password;

	// db tables
	private String documents;
	private String persons;
	private String docPers;
	private String collections;
	private String abstracts;
	private String bibDocuments;
	private String bibDocumentsSubCounts;
	private String externalIds;
	private String recommendations;
	private String recommendationSets;
	private String loggings;

	// db table logging
	private String loggingId;
	private String request;
	private String documentIdInLogging;
	private String requestReceived;
	private String responseDelivered;
	private String statusCode;
	private String debugDetails;
	private String ipHash;
	private String ip;

	// db table recommendations
	private String recommendationId;
	private String documentIdInRecommendations;
	private String recommendationSetIdInRecommendations;
	private String clicked;
	private String rankReal;
	private String rankCurrent;

	// db table recommendation sets
	private String recommendationSetsId;
	private String loggingIdInRecommendationSets;
	private String deliveredRecommendations;
	private String trigger;
	private String maximumOriginalRank;
	private String accessKey;

	// db table external id
	private String documentIdInExternalIds;
	private String externalName;
	private String externalId;

	// db table bibliometrics document
	private String bibliometricDocumentsId;
	private String documentIdInBibliometricDoc;
	private String metric;
	private String metricValue;
	private String dataType;
	private String dataSource;

	// db table bibliometrics document sub count
	private String bilbiometricSubCountId;
	private String BibliometricDocIdInBibliometricDocSubCount;
	private String country;
	private String countryCount;
	private String subdiscipline;
	private String subdisciplineCount;
	private String academicStatus;
	private String academicStatusCount;
	private String subjectArea;
	private String subjectAreaCount;
	private String userRole;
	private String userRoleCount;

	// db table abstract
	private String abstractId;
	private String abstractDocumentId;
	private String abstractLanguage;
	private String abstr;

	// db table document
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

	// table collection
	private String collectionID;
	private String collectionShortName;
	private String collectionName;
	private String organization;

	// db table person
	private String personID;
	private String firstname;
	private String middlename;
	private String surname;
	private String unstructured;

	// db table doc_pers
	private String documentIDInDocPers;
	private String personIDInDocPers;
	private String rank;

	// solr
	private String solrWebService;
	private String solrMrdlib;
	private String solrCollectionShortName;

	// collections
	private String gesis;
	private String gesisCollectionLink;

	// settings
	private boolean debugModeOn = false;

	// mendeleyCrawler settings
	private String mendeleyConfigPath;

	// load the config file
	public Constants() {

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = getClass().getClassLoader().getResourceAsStream(tomcatConfigPath);
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
			this.bibDocuments = prop.getProperty("bibliometricDocuments");
			this.externalIds = prop.getProperty("externalIds");
			this.recommendations = prop.getProperty("recommendations");
			this.recommendationSets = prop.getProperty("recommendationSets");
			this.loggings = prop.getProperty("loggings");

			this.loggingId = prop.getProperty("loggingId");
			this.request = prop.getProperty("request");
			this.documentIdInLogging = prop.getProperty("documentIdInLogging");
			this.requestReceived = prop.getProperty("requestReceived");
			this.responseDelivered = prop.getProperty("responseDelivered");
			this.statusCode = prop.getProperty("statusCode");
			this.debugDetails = prop.getProperty("debugDetails");
			this.ipHash = prop.getProperty("ipHash");
			this.ip = prop.getProperty("ip");

			this.recommendationId = prop.getProperty("recommendationId");
			this.documentIdInRecommendations = prop.getProperty("documentIdInRecommendations");
			this.recommendationSetIdInRecommendations = prop.getProperty("recommendationSetIdInRecommendations");
			this.clicked = prop.getProperty("clicked");
			this.rankReal = prop.getProperty("rankReal");
			this.rankCurrent = prop.getProperty("rankCurrent");

			this.recommendationSetsId = prop.getProperty("recommendationSetsId");
			this.loggingIdInRecommendationSets = prop.getProperty("loggingIdInRecommendationSets");
			this.deliveredRecommendations = prop.getProperty("deliveredRecommendations");
			this.trigger = prop.getProperty("trigger");
			this.maximumOriginalRank = prop.getProperty("maximumOriginalRank");
			this.accessKey = prop.getProperty("accessKey");

			this.documentIdInExternalIds = prop.getProperty("documentIdInExternalIds");
			this.externalName = prop.getProperty("externalName");
			this.externalId = prop.getProperty("externalId");

			this.bibliometricDocumentsId = prop.getProperty("bibliometricDocumentsId");
			this.documentIdInBibliometricDoc = prop.getProperty("documentIdInBibliometricDoc");
			this.metric = prop.getProperty("metric");
			this.metricValue = prop.getProperty("metricValue");
			this.dataType = prop.getProperty("dataType");
			this.dataSource = prop.getProperty("dataSource");

			this.bilbiometricSubCountId = prop.getProperty("bilbiometricSubCountId");
			this.BibliometricDocIdInBibliometricDocSubCount = prop
					.getProperty("BibliometricDocIdInBibliometricDocSubCount");
			this.country = prop.getProperty("country");
			this.countryCount = prop.getProperty("countryCount");
			this.subdiscipline = prop.getProperty("subdiscipline");
			this.subdisciplineCount = prop.getProperty("subdisciplineCount");
			this.academicStatus = prop.getProperty("academicStatus");
			this.academicStatusCount = prop.getProperty("academicStatusCount");
			this.subjectArea = prop.getProperty("subjectArea");
			this.subjectAreaCount = prop.getProperty("subjectAreaCount");
			this.userRole = prop.getProperty("userRole");
			this.userRoleCount = prop.getProperty("userRoleCount");

			this.abstractId = prop.getProperty("abstractId");
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

			this.mendeleyConfigPath = prop.getProperty("mendeleyConfigPath");

			String debugModeOn = prop.getProperty("debugModeOn");
			if (debugModeOn.equals("true"))
				this.debugModeOn = true;
			else
				this.debugModeOn = false;

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

	public String getRecommendationSetIdInRecommendations() {
		return recommendationSetIdInRecommendations;
	}

	public String getDocumentIdInRecommendations() {
		return documentIdInRecommendations;
	}

	public String getDeliveredRecommendations() {
		return deliveredRecommendations;
	}

	public String getRecommendations() {
		return recommendations;
	}

	public String getRecommendationSets() {
		return recommendationSets;
	}

	public String getLoggings() {
		return loggings;
	}

	public String getLoggingId() {
		return loggingId;
	}

	public String getRequest() {
		return request;
	}

	public String getDocumentIdInLogging() {
		return documentIdInLogging;
	}

	public String getRequestReceived() {
		return requestReceived;
	}

	public String getResponseDelivered() {
		return responseDelivered;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getDebugDetails() {
		return debugDetails;
	}

	public String getIpHash() {
		return ipHash;
	}

	public String getIp() {
		return ip;
	}

	public String getRecommendationId() {
		return recommendationId;
	}

	public String getClicked() {
		return clicked;
	}

	public String getRankReal() {
		return rankReal;
	}

	public String getRankCurrent() {
		return rankCurrent;
	}

	public String getRecommendationSetsId() {
		return recommendationSetsId;
	}

	public String getLoggingIdInRecommendationSets() {
		return loggingIdInRecommendationSets;
	}

	public String getTrigger() {
		return trigger;
	}

	public String getMaximumOriginalRank() {
		return maximumOriginalRank;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public String getPublishedIn() {
		return publishedIn;
	}

	public String getBibliometricDocumentsId() {
		return bibliometricDocumentsId;
	}

	public String getBibDocumentsSubCounts() {
		return bibDocumentsSubCounts;
	}

	public String getBilbiometricSubCountId() {
		return bilbiometricSubCountId;
	}

	public String getBibliometricDocIdInBibliometricDocSubCount() {
		return BibliometricDocIdInBibliometricDocSubCount;
	}

	public String getCountry() {
		return country;
	}

	public String getCountryCount() {
		return countryCount;
	}

	public String getSubdiscipline() {
		return subdiscipline;
	}

	public String getSubdisciplineCount() {
		return subdisciplineCount;
	}

	public String getAcademicStatus() {
		return academicStatus;
	}

	public String getAcademicStatusCount() {
		return academicStatusCount;
	}

	public String getSubjectArea() {
		return subjectArea;
	}

	public String getSubjectAreaCount() {
		return subjectAreaCount;
	}

	public String getUserRole() {
		return userRole;
	}

	public String getUserRoleCount() {
		return userRoleCount;
	}

	public String getMetricValue() {
		return metricValue;
	}

	public String getBibDocuments() {
		return bibDocuments;
	}

	public String getExternalIds() {
		return externalIds;
	}

	public String getDocumentIdInExternalIds() {
		return documentIdInExternalIds;
	}

	public String getExternalName() {
		return externalName;
	}

	public String getExternalId() {
		return externalId;
	}

	public String getDocumentIdInBibliometricDoc() {
		return documentIdInBibliometricDoc;
	}

	public String getMetric() {
		return metric;
	}

	public String getDataType() {
		return dataType;
	}

	public String getDataSource() {
		return dataSource;
	}

	public String getMendeleyConfigPath() {
		return mendeleyConfigPath;
	}

	public boolean getDebugModeOn() {
		return debugModeOn;
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