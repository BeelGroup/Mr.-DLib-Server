package org.mrdlib.api.manager;

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

	private String normalConfigPath = "config.properties";
	private String secretConfigPath = "config.secret.properties";
	private String environmentConfigPath = "config.environment.properties";

	// environment
	private String environment;

	// api keys
	private String coreAPIKey;

    // service paths
    private String doc2vecSearchRoute;
    private String doc2vecDocumentRoute;

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
	private String titleSearch;
	private String loggings;
	private String reRankingBibliometrics;
	private String bibPersons;
	private String stereotypeRecommendations;
	private String recommendationAlgorithm;
	private String keyphrases;
	private String cbfDetails;
	private String stereotypeRecommendationDetails;
	private String mostPopularRecommendationDetails;
	private String algorithmRerankingBibliometrics;
	private String bibliometrics;
	private String recommendationStatisticsReRankingBibliometric;
	private String application;
	private String organization;
	private String partnerPrefixes;
	private String accessRightsTable;

	// db table log rank stats
	private String recommendationStatisticsRecommendationSetId;
	private String percentageOfRecommendationsWithBibliometricDisplay;
	private String minimumBibDisplay;
	private String maximumBibDisplay;
	private String meanBibDisplay;
	private String medianBibDisplay;
	private String modeBibDisplay;
	private String percentageOfRecommendationsWithBibliometricRerank;
	private String minimumBibRerank;
	private String maximumBibRerank;
	private String meanBibRerank;
	private String medianBibRerank;
	private String modeBibRerank;

	// db table bibliometrics document
	private String bibliometricPersonsId;
	private String personIdInBibliometricPers;
	private String bibliometricIdInBibliometricPers;
	private String metricValuePers;

	// db table logging
	private String loggingId;
	private String request;
	private String documentIdInLogging;
	private String recommendationIdInLogging;
	private String titleSearchIdInLogging;
	private String requestReceived;
	private String responseDelivered;
	private String totalProcessingTime;
	private String statusCode;
	private String debugDetails;
	private String ipHash;
	private String ip;
	private String partnerId;
	private String requestingAppId;
	private String processingAppId;
	private String appVersion;
	private String appLang;


	// db table recommendations
	private String recommendationId;
	private String documentIdInRecommendations;
	private String externalOriginalDocumentId;
	private String recommendationSetIdInRecommendations;
	private String clicked;
	private String rankAfterAlgorithm;
	private String rankAfterReRanking;
	private String rankAfterShuffling;
	private String rankDelivered;
	private String textRelevanceScoreInRecommendations;
	private String finalRankingScore;
	private String relativeRelevanceScore;

	// db table recommendation sets
	private String recommendationSetsId;
	private String loggingIdInRecommendationSets;
	private String numberOfReturnedResults;
	private String deliveredRecommendations;
	private String algorithmId;
	private String trigger;
	private String recommendationSetReceivedTime;
	private String maximumOriginalRank;
	private String recommendationPreparationTime;
	private String userModellingTime;
	private String recommendationFrameworkTime;
	private String postProcessingTime;
	private String accessKey;
	private String minimumRelevanceScoreDisplay;
	private String maximumRelevanceScoreDisplay;
	private String meanRelevanceScoreDisplay;
	private String medianRelevanceScoreDisplay;
	private String modeRelevanceScoreDisplay;
	private String minimumFinalScoreDisplay;
	private String maximumFinalScoreDisplay;
	private String meanFinalScoreDisplay;
	private String medianFinalScoreDisplay;
	private String modeFinalScoreDisplay;
	private String externalRecommendationSetId;
	private String externalAlgorithmId;

	// db table external id
	private String documentIdInExternalIds;
	private String externalName;
	private String externalId;

	// db table bibliometrics document
	private String bibliometricDocumentsId;
	private String documentIdInBibliometricDoc;
	private String bibliometricIdInBibliometricDocument;
	private String metricValue;

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
	private String languageDetected;
	private String year;
	private String type;
	private String keywords;
	private String license;
	private String fulltextFormat;

	// table collection
	private String collectionID;
	private String collectionShortName;
	private String collectionName;
	private String organizationInCollection;

	// table application
	private String applicationId;
	private String applicationPublicName;
	private String organizationInApplication;
	private String applicationFullName;

	// table organization
	private String organizationId;
	private String organizationName;
	private String organizationPublicName;

	// table partnerPrefixes
	private String organizationIdInPartnerPrefixes;
	private String prefix;
	
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
	private String dataQualityPerson;

	// db table stereotype_recommendations
	private String documentIdInStereotype;
	private String collectionIdInStereotype;
	private String stereotypeCategory;

	// db table z_recommendation_algorithms
	private String recommendationAlgorithmId;
	private String recommendationClass;
	private String languageRestriction;
	private String bibReRankingApplied;
	private String shuffled;
	private String desiredRecommendationsInRecommendationAlgorithms;

	
	//db table access rights
	private String accessingOrganization;
	private String accessedOrganization;

	// db table z_recommendation_algorithms__details_cbf
	private String cbfId;
	private String cbfFeatureType;
	private String cbfFeatureCount;
	private String cbfFields;
	private String cbfNgramType;
	private String cbfCorpusUsed;
	private String cbfDimensions;
	private String inputType;
	private String searchMode;

	// db table z_recommendation_algorithms__details_stereotype
	private String stereotypeRecommendationDetailsId;
	private String stereotypeCategoryInStereotypeDetails;

	// db table z_recommendation_algorithms__details_most_popular
	private String mostPopularRecommendationDetailsId;
	private String mostPopularCategoryInMostPopularDetails;

	// db table z_recommendation_algorithms_reranking_bibliometrics
	private String algorithmRerankingBibliometricsId;
	private String numberOfCandidatesToRerank;
	private String rerankingOrder;
	private String bibliometricIdInAlgorithmRerankingBibliometrics;
	private String rerankingCombindation;
	private String fallbackReranking;

	// db table z_bibliometrics
	private String bibliometricId;
	private String metric;
	private String dataType;
	private String dataSource;

	// db table keyphrases
	private String sourceInKeyphrases;
	private String gramity;
	private String documentIdInKeyphrases;

	// db table document_title_searches
	private String titleSearchString;
	private String titleSearchId;
	private String originalSearchString;

	// solr
	private String solrWebService;
	private String solrMrdlib;
	private String solrCollectionShortName;
	private String documentIdInSolr;

	// collections
	private String gesis;
	private String gesisCollectionLink;
	private String gesisBetaCollectionLink;
	private String core;
	private String coreCollectionLink;
	private String mediatum;
	private String mediatumCollectionLink;

	// settings
	private boolean debugModeOn = false;
	private int numberOfRetries;

	// mendeleyCrawler settings
	private String mendeleyConfigPath;

	// email settings for the account used to send notifications
	private String emailNotificationUsername;
	private String emailNotificationPassword;
	private String emailNotificationHost;
	private String emailNotificationAddress;

	private String probabilitiesConfigPath;



	// load the config file
	public Constants() {

		InputStream input = null;
		Properties prop;
	
		try {
			input = getClass().getClassLoader().getResourceAsStream(secretConfigPath);
			prop = new Properties();
			prop.load(input);

			// secret configuration settings (access data)
			this.coreAPIKey = prop.getProperty("coreAPIKey");
			this.dbClass = prop.getProperty("dbClass");
			this.db = prop.getProperty("db");
			this.url = prop.getProperty("url");
			this.user = prop.getProperty("user");
			this.password = prop.getProperty("password");

			this.emailNotificationUsername = prop.getProperty("emailNotificationUsername");
			this.emailNotificationPassword = prop.getProperty("emailNotificationPassword");
			this.emailNotificationHost = prop.getProperty("emailNotificationHost");
			this.emailNotificationAddress = prop.getProperty("emailNotificationAddress");

			// normal configuration settings
			input = getClass().getClassLoader().getResourceAsStream(normalConfigPath);
			prop = new Properties();
			prop.load(input);


			this.doc2vecSearchRoute = prop.getProperty("doc2vecSearchRoute");
			this.doc2vecDocumentRoute = prop.getProperty("doc2vecDocumentRoute");


			this.documents = prop.getProperty("documents");
			this.persons = prop.getProperty("persons");
			this.docPers = prop.getProperty("doc_pers");
			this.collections = prop.getProperty("collections");
			this.abstracts = prop.getProperty("abstracts");
			this.bibDocuments = prop.getProperty("bibliometricDocuments");
			this.externalIds = prop.getProperty("externalIds");
			this.recommendations = prop.getProperty("recommendations");
			this.recommendationSets = prop.getProperty("recommendationSets");
			this.titleSearch = prop.getProperty("titleSearch");
			this.loggings = prop.getProperty("loggings");
			this.reRankingBibliometrics = prop.getProperty("reRankingBibliometrics");
			this.bibPersons = prop.getProperty("bibliometricPersons");
			this.stereotypeRecommendations = prop.getProperty("stereotypeRecommendations");
			this.recommendationAlgorithm = prop.getProperty("recommendationAlgorithm");
			this.keyphrases = prop.getProperty("keyphrases");
			this.cbfDetails = prop.getProperty("cbfDetails");
			this.stereotypeRecommendationDetails = prop.getProperty("stereotypeRecommendationDetails");
			this.mostPopularRecommendationDetails = prop.getProperty("mostPopularRecommendationDetails");
			this.algorithmRerankingBibliometrics = prop.getProperty("algorithmRerankingBibliometrics");
			this.bibliometrics = prop.getProperty("bibliometrics");
			this.recommendationStatisticsReRankingBibliometric = prop
					.getProperty("recommendationStatisticsReRankingBibliometric");
			this.application = prop.getProperty("application");
			this.organization = prop.getProperty("organization");
			this.partnerPrefixes = prop.getProperty("partnerPrefixes");
			this.accessRightsTable = prop.getProperty("accessRightsTable");

			this.recommendationStatisticsRecommendationSetId = prop
					.getProperty("recommendationStatisticsRecommendationSetId");
			this.percentageOfRecommendationsWithBibliometricDisplay = prop
					.getProperty("percentageOfRecommendationsWithBibliometricDisplay");
			this.minimumBibDisplay = prop.getProperty("minimumBibDisplay");
			this.maximumBibDisplay = prop.getProperty("maximumBibDisplay");
			this.meanBibDisplay = prop.getProperty("meanBibDisplay");
			this.medianBibDisplay = prop.getProperty("medianBibDisplay");
			this.modeBibDisplay = prop.getProperty("modeBibDisplay");
			this.percentageOfRecommendationsWithBibliometricRerank = prop
					.getProperty("percentageOfRecommendationsWithBibliometricRerank");
			this.minimumBibRerank = prop.getProperty("minimumBibRerank");
			this.maximumBibRerank = prop.getProperty("maximumBibRerank");
			this.meanBibRerank = prop.getProperty("meanBibRerank");
			this.medianBibRerank = prop.getProperty("medianBibRerank");
			this.modeBibRerank = prop.getProperty("modeBibRerank");

			this.loggingId = prop.getProperty("loggingId");
			this.request = prop.getProperty("request");
			this.documentIdInLogging = prop.getProperty("documentIdInLogging");
			this.recommendationIdInLogging = prop.getProperty("recommendationIdInLogging");
			this.titleSearchIdInLogging = prop.getProperty("titleSearchIdInLogging");
			this.requestReceived = prop.getProperty("requestReceived");
			this.responseDelivered = prop.getProperty("responseDelivered");
			this.totalProcessingTime = prop.getProperty("totalProcessingTime");
			this.statusCode = prop.getProperty("statusCode");
			this.debugDetails = prop.getProperty("debugDetails");
			this.ipHash = prop.getProperty("ipHash");
			this.ip = prop.getProperty("ip");
			this.partnerId = prop.getProperty("partnerId");
			this.requestingAppId = prop.getProperty("requestingAppId");
			this.processingAppId = prop.getProperty("processingAppId");
			this.appVersion = prop.getProperty("appVersion");
			this.appLang = prop.getProperty("appLang");

			this.recommendationId = prop.getProperty("recommendationId");
			this.documentIdInRecommendations = prop.getProperty("documentIdInRecommendations");
			this.externalOriginalDocumentId = prop.getProperty("externalOriginalDocumentId");
			this.recommendationSetIdInRecommendations = prop.getProperty("recommendationSetIdInRecommendations");
			this.algorithmId = prop.getProperty("algorithmId");
			this.clicked = prop.getProperty("clicked");
			this.rankAfterAlgorithm = prop.getProperty("rankAfterAlgorithm");
			this.rankAfterReRanking = prop.getProperty("rankAfterReRanking");
			this.rankAfterShuffling = prop.getProperty("rankAfterShuffling");
			this.rankDelivered = prop.getProperty("rankDelivered");
			this.textRelevanceScoreInRecommendations = prop.getProperty("textRelevanceScoreInRecommendations");
			this.finalRankingScore = prop.getProperty("finalRankingScore");
			this.relativeRelevanceScore = prop.getProperty("relativeRelevanceScore");

			this.recommendationSetsId = prop.getProperty("recommendationSetsId");
			this.loggingIdInRecommendationSets = prop.getProperty("loggingIdInRecommendationSets");
			this.numberOfReturnedResults = prop.getProperty("numberOfReturnedResults");
			this.deliveredRecommendations = prop.getProperty("deliveredRecommendations");
			this.trigger = prop.getProperty("trigger");
			this.recommendationSetReceivedTime = prop.getProperty("recommendationSetReceivedTime");
			this.maximumOriginalRank = prop.getProperty("maximumOriginalRank");
			this.recommendationPreparationTime = prop.getProperty("recommendationPreparationTime");
			this.userModellingTime = prop.getProperty("userModellingTime");
			this.recommendationFrameworkTime = prop.getProperty("recommendationFrameworkTime");
			this.postProcessingTime = prop.getProperty("postProcessingTime");
			this.accessKey = prop.getProperty("accessKey");
			this.minimumRelevanceScoreDisplay = prop.getProperty("minimumRelevanceScoreDisplay");
			this.maximumRelevanceScoreDisplay = prop.getProperty("maximumRelevanceScoreDisplay");
			this.meanRelevanceScoreDisplay = prop.getProperty("meanRelevanceScoreDisplay");
			this.medianRelevanceScoreDisplay = prop.getProperty("medianRelevanceScoreDisplay");
			this.modeRelevanceScoreDisplay = prop.getProperty("modeRelevanceScoreDisplay");
			this.minimumFinalScoreDisplay = prop.getProperty("minimumFinalScoreDisplay");
			this.maximumFinalScoreDisplay = prop.getProperty("maximumFinalScoreDisplay");
			this.meanFinalScoreDisplay = prop.getProperty("meanFinalScoreDisplay");
			this.medianFinalScoreDisplay = prop.getProperty("medianFinalScoreDisplay");
			this.modeFinalScoreDisplay = prop.getProperty("modeFinalScoreDisplay");
			this.externalRecommendationSetId = prop.getProperty("externalRecommendationSetId");
			this.externalAlgorithmId = prop.getProperty("externalAlgorithmId");

			this.documentIdInExternalIds = prop.getProperty("documentIdInExternalIds");
			this.externalName = prop.getProperty("externalName");
			this.externalId = prop.getProperty("externalId");

			this.titleSearchString = prop.getProperty("titleSearchString");
			this.titleSearchId = prop.getProperty("titleSearchId");
			this.originalSearchString = prop.getProperty("originalSearchString");

			this.bibliometricPersonsId = prop.getProperty("bibliometricPersonsId");
			this.personIdInBibliometricPers = prop.getProperty("personIdInBibliometricPers");
			this.bibliometricIdInBibliometricPers = prop.getProperty("bibliometricIdInBibliometricPers");
			this.metricValuePers = prop.getProperty("metricValuePers");

			this.bibliometricDocumentsId = prop.getProperty("bibliometricDocumentsId");
			this.documentIdInBibliometricDoc = prop.getProperty("documentIdInBibliometricDoc");
			this.bibliometricIdInBibliometricDocument = prop.getProperty("bibliometricIdInBibliometricDocument");
			this.metricValue = prop.getProperty("metricValue");

			this.bibliometricId = prop.getProperty("bibliometricId");
			this.dataType = prop.getProperty("dataType");
			this.dataSource = prop.getProperty("dataSource");
			this.metric = prop.getProperty("metric");

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

			this.applicationId = prop.getProperty("applicationId");
			this.applicationPublicName = prop.getProperty("applicationPublicName");
			this.organizationInApplication = prop.getProperty("organizationIdInApplication");
			this.applicationFullName = prop.getProperty("applicationFullName");

			this.organizationId = prop.getProperty("organizationId");
			this.organizationName = prop.getProperty("organizationName");
			this.organizationPublicName = prop.getProperty("organizationPublicName");

			this.documentID = prop.getProperty("documentId");
			this.idOriginal = prop.getProperty("idOriginal");
			this.documentCollectionID = prop.getProperty("documentCollectionId");
			this.title = prop.getProperty("title");
			this.titleClean = prop.getProperty("titleClean");
			this.authors = prop.getProperty("authors");
			this.publishedIn = prop.getProperty("publication");
			this.language = prop.getProperty("language");
			this.languageDetected = prop.getProperty("languageDetected");
			this.year = prop.getProperty("year");
			this.type = prop.getProperty("type");
			this.keywords = prop.getProperty("keywords");
			this.license = prop.getProperty("license");
			this.fulltextFormat = prop.getProperty("fulltextFormat");
			this.documentAdded = prop.getProperty("added");
			this.documentDeleted = prop.getProperty("deleted");
			this.documentChecked = prop.getProperty("checked");
			this.checkInterval = Long.parseLong(prop.getProperty("checkInterval"));

			this.accessingOrganization = prop.getProperty("accessingOrganization");
			this.accessedOrganization = prop.getProperty("accessedOrganization");
			
			this.collectionID = prop.getProperty("collectionId");
			this.collectionShortName = prop.getProperty("collectionShortName");
			this.collectionName = prop.getProperty("collectionName");
			this.organizationInCollection = prop.getProperty("organizationInCollection"); // TODO fix

			this.prefix = prop.getProperty("prefix");
			this.organizationIdInPartnerPrefixes = prop.getProperty("organizationIdInPartnerPrefixes");
			
			this.personID = prop.getProperty("personId");
			this.firstname = prop.getProperty("firstname");
			this.middlename = prop.getProperty("middlename");
			this.surname = prop.getProperty("surname");
			this.unstructured = prop.getProperty("unstructured");

			this.documentIDInDocPers = prop.getProperty("documentIdInDocPers");
			this.personIDInDocPers = prop.getProperty("personIdInDocPers");
			this.rank = prop.getProperty("authorRank");
			this.dataQualityPerson = prop.getProperty("dataQuality");

			this.documentIdInStereotype = prop.getProperty("documentIdInStereotype");
			this.collectionIdInStereotype = prop.getProperty("collectionIdInStereotype");
			this.stereotypeCategory = prop.getProperty("stereotypeCategory");

			this.recommendationAlgorithmId = prop.getProperty("recommendationAlgorithmId");
			this.recommendationClass = prop.getProperty("recommendationClass");
			this.languageRestriction = prop.getProperty("languageRestriction");
			this.bibReRankingApplied = prop.getProperty("bibReRankingApplied");
			this.shuffled = prop.getProperty("shuffled");
			this.desiredRecommendationsInRecommendationAlgorithms = prop
					.getProperty("desiredRecommendationsInRecommendationAlgorithms");

			this.cbfId = prop.getProperty("cbfId");
			this.cbfFeatureType = prop.getProperty("cbfFeatureType");
			this.cbfFeatureCount = prop.getProperty("cbfFeatureCount");
			this.cbfFields = prop.getProperty("cbfFields");
			this.cbfNgramType = prop.getProperty("cbfNgramType");
			this.cbfDimensions = prop.getProperty("cbfDimensions");
			this.cbfCorpusUsed = prop.getProperty("cbfCorpusUsed");
			this.searchMode = prop.getProperty("searchMode");
			this.inputType = prop.getProperty("inputType");

			this.stereotypeRecommendationDetailsId = prop.getProperty("stereotypeRecommendationDetailsId");
			this.stereotypeCategoryInStereotypeDetails = prop.getProperty("stereotypeCategoryInStereotypeDetails");

			this.mostPopularCategoryInMostPopularDetails = prop.getProperty("mostPopularCategoryInMostPopularDetails");
			this.mostPopularRecommendationDetailsId = prop.getProperty("mostPopularRecommendationDetailsId");

			this.sourceInKeyphrases = prop.getProperty("sourceInKeyphrases");
			this.gramity = prop.getProperty("gramity");
			this.documentIdInKeyphrases = prop.getProperty("documentIdInKeyphrases");

			this.algorithmRerankingBibliometricsId = prop.getProperty("algorithmRerankingBibliometricsId");
			this.numberOfCandidatesToRerank = prop.getProperty("numberOfCandidatesToRerank");
			this.rerankingOrder = prop.getProperty("rerankingOrder");
			this.bibliometricIdInAlgorithmRerankingBibliometrics = prop
					.getProperty("bibliometricIdInAlgorithmRerankingBibliometrics");
			this.rerankingCombindation = prop.getProperty("rerankingCombindation");
			this.fallbackReranking = prop.getProperty("fallbackReranking");

			this.solrWebService = prop.getProperty("solrWebService");
			this.solrCollectionShortName = prop.getProperty("solrCollectionShortName");
			this.documentIdInSolr = prop.getProperty("documentIdInSolr");

			this.gesisCollectionLink = prop.getProperty("gesisCollectionLink");
			this.gesisBetaCollectionLink = prop.getProperty("gesisBetaCollectionLink");
			this.gesis = prop.getProperty("gesis");
			this.core = prop.getProperty("core");
			this.coreCollectionLink = prop.getProperty("coreCollectionLink");
			this.mediatumCollectionLink = prop.getProperty("mediatumCollectionLink");
			this.mediatum = prop.getProperty("mediatum");
			
			this.mendeleyConfigPath = prop.getProperty("mendeleyConfigPath");
			this.probabilitiesConfigPath = prop.getProperty("probabilityConfigPath");

			this.numberOfRetries = Integer.parseInt(prop.getProperty("numberOfRetries"));
			this.debugModeOn = Boolean.parseBoolean(prop.getProperty("debugModeOn"));


			// server-specific settings
			input = getClass().getClassLoader().getResourceAsStream(environmentConfigPath);
			prop = new Properties();
			prop.load(input);

			this.environment = prop.getProperty("environment");
			this.solrMrdlib = prop.getProperty("solrMrdlib");

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

	
	private long checkInterval;

	public long getCheckInterval() {
		return checkInterval;
	}
	
	private String documentChecked;

	public String getChecked() {
		return documentChecked;
	}
	
	private String documentAdded;

	public String getAdded() {
		return documentAdded;
	}
	
	private String documentDeleted;

	public String getDeleted() {
		return documentDeleted;
	}

	public String getCoreAPIKey() {
		return coreAPIKey;
	}

    public String getDoc2VecSearchRoute() {
	return doc2vecSearchRoute;
    }

    public String getDoc2VecDocumentRoute() {
	return doc2vecDocumentRoute;
    }

	public String getDataQualityPerson() {
		return dataQualityPerson;
	}

	public String getFallbackReranking() {
		return fallbackReranking;
	}

	public String getMinimumRelevanceScoreDisplay() {
		return minimumRelevanceScoreDisplay;
	}

	public String getMaximumRelevanceScoreDisplay() {
		return maximumRelevanceScoreDisplay;
	}

	public String getMeanRelevanceScoreDisplay() {
		return meanRelevanceScoreDisplay;
	}

	public String getMedianRelevanceScoreDisplay() {
		return medianRelevanceScoreDisplay;
	}

	public String getModeRelevanceScoreDisplay() {
		return modeRelevanceScoreDisplay;
	}

	public String getMinimumFinalScoreDisplay() {
		return minimumFinalScoreDisplay;
	}

	public String getMaximumFinalScoreDisplay() {
		return maximumFinalScoreDisplay;
	}

	public String getMeanFinalScoreDisplay() {
		return meanFinalScoreDisplay;
	}

	public String getMedianFinalScoreDisplay() {
		return medianFinalScoreDisplay;
	}

	public String getModeFinalScoreDisplay() {
		return modeFinalScoreDisplay;
	}

	public String getRecommendationStatisticsReRankingBibliometric() {
		return recommendationStatisticsReRankingBibliometric;
	}

	public String getRecommendationStatisticsRecommendationSetId() {
		return recommendationStatisticsRecommendationSetId;
	}

	public String getPercentageOfRecommendationsWithBibliometricDisplay() {
		return percentageOfRecommendationsWithBibliometricDisplay;
	}

	public String getMinimumBibDisplay() {
		return minimumBibDisplay;
	}

	public String getMaximumBibDisplay() {
		return maximumBibDisplay;
	}

	public String getMeanBibDisplay() {
		return meanBibDisplay;
	}

	public String getMedianBibDisplay() {
		return medianBibDisplay;
	}

	public String getModeBibDisplay() {
		return modeBibDisplay;
	}

	public String getPercentageOfRecommendationsWithBibliometricRerank() {
		return percentageOfRecommendationsWithBibliometricRerank;
	}

	public String getMinimumBibRerank() {
		return minimumBibRerank;
	}

	public String getMaximumBibRerank() {
		return maximumBibRerank;
	}

	public String getMeanBibRerank() {
		return meanBibRerank;
	}

	public String getMedianBibRerank() {
		return medianBibRerank;
	}

	public String getModeBibRerank() {
		return modeBibRerank;
	}

	public String getBibliometricIdInBibliometricDocument() {
		return bibliometricIdInBibliometricDocument;
	}

	public String getBibliometrics() {
		return bibliometrics;
	}

	public String getBibliometricId() {
		return bibliometricId;
	}

	public String getAlgorithmRerankingBibliometrics() {
		return algorithmRerankingBibliometrics;
	}

	public String getAlgorithmRerankingBibliometricsId() {
		return algorithmRerankingBibliometricsId;
	}

	public String getNumberOfCandidatesToRerank() {
		return numberOfCandidatesToRerank;
	}

	public String getRerankingOrder() {
		return rerankingOrder;
	}

	public String getBibliometricIdInAlgorithmRerankingBibliometrics() {
		return bibliometricIdInAlgorithmRerankingBibliometrics;
	}

	public String getRerankingCombindation() {
		return rerankingCombindation;
	}

	public String getShuffled() {
		return shuffled;
	}

	public String getRankAfterAlgorithm() {
		return rankAfterAlgorithm;
	}

	public String getRankAfterReRanking() {
		return rankAfterReRanking;
	}

	public String getRankAfterShuffling() {
		return rankAfterShuffling;
	}

	public String getRankDelivered() {
		return rankDelivered;
	}

	public String getFinalRankingScore() {
		return finalRankingScore;
	}

	public String getEnvironment() {
		return environment;
	}

	public String getBibPersons() {
		return bibPersons;
	}

	public String getBibliometricPersonsId() {
		return bibliometricPersonsId;
	}

	public String getPersonIdInBibliometricPers() {
		return personIdInBibliometricPers;
	}

	public String getBibliometricIdInBibliometricPers() {
		return bibliometricIdInBibliometricPers;
	}

	public String getMetricValuePers() {
		return metricValuePers;
	}

	public String getReRankingBibliometrics() {
		return reRankingBibliometrics;
	}

	public String getAlgorithmId() {
		return algorithmId;
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

	public String getCoreCollectionLink() {
		return coreCollectionLink;
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

	public String getCore() {
		return core;
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

	public String getOrganizationInCollection() {
		return organizationInCollection;
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

	public String getLicense() {
		return license;
	}

	public String getFulltextFormat() {
		return fulltextFormat;
	}

	public String getLanguage() {
		return language;
	}
	
	public String getLanguageDetected() {
		return languageDetected;
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

	public String getProbabilitiesPath() {
		return probabilitiesConfigPath;
	}

	public String getStereotypeRecommendations() {
		return stereotypeRecommendations;
	}

	public String getDocumentIdinStereotypeRecommendations() {
		return documentIdInStereotype;
	}

	public String getCollectionIDinStereotypeRecommendations() {
		return collectionIdInStereotype;
	}

	public String getRecommendationAlgorithmId() {
		return recommendationAlgorithmId;
	}

	public String getRecommendationAlgorithm() {
		return recommendationAlgorithm;
	}

	public String getSourceInKeyphrases() {
		return sourceInKeyphrases;
	}

	public String getGramity() {
		return gramity;
	}

	public String getKeyphrases() {
		return keyphrases;
	}

	public String getDocumentIdInKeyphrases() {
		return documentIdInKeyphrases;
	}

	public int getNumberOfRetries() {
		return numberOfRetries;
	}

	public String getTextRelevanceScoreInRecommendations() {
		return textRelevanceScoreInRecommendations;
	}

	/**
	 * @return the numberOfReturnedResults
	 */
	public String getNumberOfReturnedResults() {
		return numberOfReturnedResults;
	}

	public String getCbfId() {
		return cbfId;
	}

	public String getCbfDetails() {
		return cbfDetails;
	}

	public String getCbfFeatureType() {
		return cbfFeatureType;
	}

	public String getCbfDimensions() {
		return cbfDimensions;
	}
	public String getCbfCorpusUsed() {
		return cbfCorpusUsed;
	}

	public String getCbfFeatureCount() {
		return cbfFeatureCount;
	}

	public String getCbfFields() {
		return cbfFields;
	}

	public String getCbfNgramType() {
		return cbfNgramType;
	}

	public String getRecommendationClass() {
		return recommendationClass;
	}

	public String getLanguageRestrictionInRecommenderAlgorithm() {
		return languageRestriction;
	}

	public String getBibReRankingApplied() {
		return bibReRankingApplied;
	}

	public String getPreparationTime() {
		return recommendationPreparationTime;
	}

	public String getUserModellingTime() {
		return userModellingTime;
	}

	public String getRecFrameworkTime() {
		return recommendationFrameworkTime;
	}

	public String getPostProcessingTime() {
		return postProcessingTime;
	}

	public String getProcessingTimeTotal() {
		return totalProcessingTime;
	}

	public String getStereotypeCategory() {
		return stereotypeCategory;
	}

	public String getStereotypeRecommendationDetailsId() {
		return stereotypeRecommendationDetailsId;
	}

	public String getStereotypeRecommendationDetails() {
		return stereotypeRecommendationDetails;
	}

	public String getStereotypeCategoryInStereotypeDetails() {
		return stereotypeCategoryInStereotypeDetails;
	}

	/**
	 * @return the mostPopularRecommendationDetails
	 */
	public String getMostPopularRecommendationDetails() {
		return mostPopularRecommendationDetails;
	}

	/**
	 * @return the mostPopularRecommendationDetailsId
	 */
	public String getMostPopularRecommendationDetailsId() {
		return mostPopularRecommendationDetailsId;
	}

	/**
	 * @return the mostPopularCategoryInMostPopularDetails
	 */
	public String getMostPopularCategoryInMostPopularDetails() {
		return mostPopularCategoryInMostPopularDetails;
	}

	public String getDocumentIdInSolr() {
		return documentIdInSolr;
	}

	public String getDesiredRecommendationsInRecommendationAlgorithms() {
		return desiredRecommendationsInRecommendationAlgorithms;
	}

	public String getGesisBetaCollectionLink() {
		return gesisBetaCollectionLink;
	}

	public String getRecommendationSetReceivedTime() {
		return recommendationSetReceivedTime;
	}

	public String getDocumentTitleSearchTable() {
		return titleSearch;
	}

	public String getRecommendationIdInLogging() {
		return recommendationIdInLogging;
	}

	public String getTitleSearchIdInLogging() {
		return titleSearchIdInLogging;
	}

	/**
	 * @return the titleSearchString
	 */
	public String getTitleSearchString() {
		return titleSearchString;
	}

	/**
	 * @return the titleSearchId
	 */
	public String getTitleSearchId() {
		return titleSearchId;
	}

	public String getOriginalSearchString() {
		return originalSearchString;
	}

	public String getRelativeRelevanceScore() {
		return relativeRelevanceScore;
	}

	public String getInputType() {
		return inputType;
	}

	public String getSearchMode() {
		return searchMode;
	}

	public String getApplication() {
		return application;
	}

	public String getOrganization() {
		return organization;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public String getApplicationPublicName() {
		return applicationPublicName;
	}

	public String getOrganizationInApplication() {
		return organizationInApplication;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public String getOrganizationPublicName() {
		return organizationPublicName;
	}

	public String getPartnerId() {
		return partnerId;
	}

	public String getRequestingAppId() {
		return requestingAppId;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public String getAppLang() {
		return appLang;
	}

	public String getPartnerPrefixes() {
		return partnerPrefixes;
	}

	public String getOrganizationIdInPartnerPrefixes() {
		return organizationIdInPartnerPrefixes;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getAccessingOrganization() {
		return accessingOrganization;
	}

	public String getAccessedOrganization() {
		return accessedOrganization;
	}

	public String getAccessRightsTable() {
		return accessRightsTable;
	}

	public String getExternalOriginalDocumentId() {
		return externalOriginalDocumentId;
	}

	public String getExternalRecommendationSetId() {
		return externalRecommendationSetId;
	}

	public String getExternalAlgorithmId() {
		return externalAlgorithmId;
	}

	public String getProcessingAppId() {
		return processingAppId;
	}

	public String getMediatumCollectionLink() {
		return mediatumCollectionLink;
	}
	public String getMediatum() {
		return mediatum;
	}

	public String getApplicationFullName() {
		return applicationFullName;
	}

	public String getEmailNotificationUsername() {
		return emailNotificationUsername;
	}

	public String getEmailNotificationPassword() {
		return emailNotificationPassword;
	}

	public String getEmailNotificationHost() {
		return emailNotificationHost;
	}

	public String getEmailNotificationAddress() {
		return emailNotificationAddress;
	}
}
