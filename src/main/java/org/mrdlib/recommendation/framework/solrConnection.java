package org.mrdlib.recommendation.framework;

import java.net.URLEncoder;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.mrdlib.api.manager.Constants;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.recommendation.algorithm.AlgorithmDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author Millah
 *
 *         This class handles all the communication with solr
 *
 */
public class solrConnection {
	private Logger logger = LoggerFactory.getLogger(solrConnection.class);
	private Constants constants = new Constants();
	private SolrClient solr = null;
	private DBConnection con;

	/**
	 * create a solr connection
	 * 
	 * @throws Exception
	 */
	public solrConnection(DBConnection con) throws Exception {
		String urlString = constants.getSolrWebService().concat(constants.getSolrMrdlib());
		solr = new HttpSolrClient.Builder(urlString).build();
		this.con = con;
	}

	/**
	 * close a solr connection
	 */
	/*
	 * protected void finalize() throws Throwable { solr.close();
	 * super.finalize(); }
	 * 
	 * public void close() throws IOException { solr.close(); }
	 */

	/**
	 * 
	 * get the first 10 related documents of a query document from solr
	 * 
	 * @param document,
	 *            to which similar documents are searched for
	 * @param delimitedRows
	 *            how many rows you want back
	 * @param logginginfo
	 *            to know the type of recommendations needed
	 * @return the delimited rows number of most related documents in a document
	 *         set
	 * @throws Exception
	 */
	public DocumentSet getRelatedDocumentSetByDocument(DocumentSet relatedDocuments) throws Exception {

		DisplayDocument document = relatedDocuments.getRequestedDocument();
		AlgorithmDetails logginginfo = relatedDocuments.getAlgorithmDetails();
		int delimitedRows = relatedDocuments.getDesiredNumberFromAlgorithm();
		List<String> allowedCollections = con.getAccessableCollections(relatedDocuments.getRequestingPartnerId());

		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		DisplayDocument relDocument = new DisplayDocument();
		query.setRequestHandler("/" + MoreLikeThisParams.MLT);
		String fallback_url = "";

		// get only documents which are in the same collection
		String filterquery = constants.getCollectionID() + ":(" + String.join(" ", allowedCollections) + ")";
		query.addFilterQuery(filterquery);

		// get related documents for the given document
		query.setQuery(constants.getDocumentIdInSolr() + ":" + document.getDocumentId());

		// return only "delimitedRows" much
		query.setRows(delimitedRows);
		// if rec_approach is Keyphrases: override default mlt.fl
		if (logginginfo.getName().equals("RelatedDocumentsFromSolrWithKeyphrases")) {
			String similarityParams = getMltFL(logginginfo.getCbfTextFields(), logginginfo.getNgramType(),
					logginginfo.getCbfFeatureCount());
			query.setParam("mlt.fl", similarityParams);
			query.setParam("mlt.df", "2");
		}
		// set display params
		query.setParam("fl", "score,id");
		try {
			response = solr.query(query);

			SolrDocumentList docs = response.getResults();
			// no related documents found
			if (docs.isEmpty()) {
				throw new NoRelatedDocumentsException(document.getOriginalDocumentId(), document.getDocumentId());
			} else {
				relatedDocuments.setSuggested_label("Related Articles");
				relatedDocuments.setNumberOfReturnedResults(docs.getNumFound());
				// for each document add it to documentSet
				for (int i = 0; i < docs.size(); i++) {
					// get the document
					relDocument = con.getDocumentBy(constants.getDocumentId(),
							docs.get(i).getFieldValue(constants.getDocumentIdInSolr()).toString());

					// add the rank
					relDocument.setSuggestedRank(i + 1);

					// add the solrScore
					relDocument.setRelevanceScoreFromAlgorithm(
							Double.parseDouble(docs.get(i).getFieldValue("score").toString()));

					// set gesis specific link
					if (relDocument.getCollectionShortName().equals(constants.getGesis())) {
						if (constants.getEnvironment().equals("api"))
							fallback_url = constants.getGesisCollectionLink()
									.concat(relDocument.getOriginalDocumentId());
						else
							fallback_url = constants.getGesisBetaCollectionLink()
									.concat(relDocument.getOriginalDocumentId());
					} else if (relDocument.getCollectionShortName().contains(constants.getCore()))
						fallback_url = constants.getCoreCollectionLink()
								.concat(relDocument.getOriginalDocumentId().split("-")[1]);
					else if (relDocument.getCollectionShortName().contains(constants.getMediatum()))
						fallback_url = constants.getMediatumCollectionLink()
								.concat(relDocument.getOriginalDocumentId().split("-")[1]);

					relDocument.setFallbackUrl(fallback_url);
					// add it to the collection
					relatedDocuments.addDocument(relDocument);
				}
			}
		} catch (Exception e) {
			logger.debug("test: " + e.getStackTrace());
			throw e;
		}

		return relatedDocuments;
	}

	/**
	 * Helper function to get disambiguate the MLT query from the features of
	 * the recommendation approach
	 * 
	 * @param source
	 *            title, or title_and_abstract
	 * @param type
	 *            unigrams, bigrams, trigrams, unibi, etc.
	 * @param number
	 *            how many features to include in the comparison
	 * @return a string which refers to the Solr column name for comparison
	 */
	private String getMltFL(String source, String type, String number) {
		if (number.equals("0"))
			number = "copy";
		String template = source + "_%s_" + number;
		String uni = String.format(template, "unigrams");
		String bi = String.format(template, "bigrams");
		String tri = String.format(template, "trigrams");
		switch (type) {
		case "unibitri":
			return uni + ", " + bi + ", " + tri;
		case "unibi":
			return uni + ", " + bi;
		case "unitri":
			return uni + ", " + tri;
		case "bitri":
			return bi + ", " + tri;
		default:
			return String.format(template, type + "s");

		}
	}

	/**
	 * Solr accessor method to get random documents
	 * 
	 * @param document
	 *            the document for which we are delivering recommendations
	 * @param delimitedRows
	 *            how many documents to recommend
	 * @param restrictLanguage
	 *            if true, random documents are selected which share the
	 *            language as the <code>document</code>
	 * @param seed
	 *            seed for solr's random function
	 * @return the delimited rows number of most related documents in a document
	 *         set
	 * @throws Exception
	 *             if solr connection fails
	 */
	public DocumentSet getRandomDocumentSet(DocumentSet relatedDocuments, Boolean restrictLanguage, String seed)
			throws Exception {

		DisplayDocument document = relatedDocuments.getRequestedDocument();
		int delimitedRows = relatedDocuments.getDesiredNumberFromAlgorithm();
		List<String> allowedCollections = con.getAccessableCollections(relatedDocuments.getRequestingPartnerId());

		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		DisplayDocument relDocument = new DisplayDocument();

		// Setting solr query to select mode
		query.setRequestHandler("/select");
		String fallback_url = "";
		query.setQuery("*:*");
		// get only documents which are in the same collection
		String filterQuery = constants.getCollectionID() + ":(" + String.join(" ", allowedCollections) + ")";
		query.addFilterQuery(filterQuery);

		// add second filter query if language needs to be restricted
		if (restrictLanguage) {
			query.addFilterQuery(constants.getLanguage() + ":" + document.getLanguage());
		}

		// Get back id and score
		query.setParam("fl", "id,score");

		// return only "delimitedRows" much
		query.setRows(delimitedRows);

		// Using solr's random sort field functionality
		query.setSort(SortClause.asc("random_" + seed));

		try {
			response = solr.query(query);
			SolrDocumentList docs = response.getResults();

			// no related documents found
			if (docs.isEmpty())
				throw new NoRelatedDocumentsException(document.getOriginalDocumentId(), document.getDocumentId());
			else {
				relatedDocuments.setSuggested_label("Related Articles");
				relatedDocuments.setRequestedDocument(document);
				relatedDocuments.setNumberOfReturnedResults(docs.getNumFound());

				// for each document add it to documentSet
				for (int i = 0; i < docs.size(); i++) {
					// get the document
					relDocument = con.getDocumentBy(constants.getDocumentId(),
							docs.get(i).getFieldValue(constants.getDocumentIdInSolr()).toString());

					// add the rank
					relDocument.setSuggestedRank(i + 1);

					// add the solrScore
					relDocument.setRelevanceScoreFromAlgorithm(
							Double.parseDouble(docs.get(i).getFieldValue("score").toString()));

					// set gesis specific link
					if (relDocument.getCollectionShortName().equals(constants.getGesis())) {
						if (constants.getEnvironment().equals("api"))
							fallback_url = constants.getGesisCollectionLink()
									.concat(relDocument.getOriginalDocumentId());
						else
							fallback_url = constants.getGesisBetaCollectionLink()
									.concat(relDocument.getOriginalDocumentId());
					} else if (relDocument.getCollectionShortName().contains(constants.getCore()))
						fallback_url = constants.getCoreCollectionLink()
								.concat(relDocument.getOriginalDocumentId().split("-")[1]);
					else if (relDocument.getCollectionShortName().contains(constants.getMediatum()))
						fallback_url = constants.getMediatumCollectionLink()
								.concat(relDocument.getOriginalDocumentId().split("-")[1]);

					// url = "http://api.mr-dlib.org/trial/recommendations/" +
					// relDocument.getRecommendationId() +
					// "/original_url/&access_key=" +"hash"
					// +"&format=direct_url_forward";

					// relDocument.setClickUrl(url);
					relDocument.setFallbackUrl(fallback_url);
					// add it to the collection
					relatedDocuments.addDocument(relDocument);
				}
			}
		} catch (Exception e) {
			logger.debug("test: " + e.getStackTrace());
			throw e;
		}
		return relatedDocuments;
	}

	public DocumentSet getDocumentsFromSolrByQuery(DocumentSet relatedDocuments) throws Exception {

		DisplayDocument requestedDocument = relatedDocuments.getRequestedDocument();
		AlgorithmDetails logginginfo = relatedDocuments.getAlgorithmDetails();
		int delimitedRows = relatedDocuments.getDesiredNumberFromAlgorithm();

		List<String> allowedCollections = con.getAccessableCollections(relatedDocuments.getRequestingPartnerId());

		String title = requestedDocument.getCleanTitle();
		logger.trace("requesting solr documents for {}, clean_title = {} with parameters {}", requestedDocument, title, logginginfo);
		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		DisplayDocument oneRelatedDocument = new DisplayDocument();
		String queryParser = logginginfo.getQueryParser();
		boolean standard;
		String queryHandler;
		if (queryParser.equals("standardQP")) {
			standard = true;
			queryHandler = "/select";
		} else {
			standard = false;
			queryHandler = "/search";
		}
		query.setRequestHandler(queryHandler);
		query.setFilterQueries(constants.getCollectionID() + ":(" + String.join(" ", allowedCollections) + ")");
		String fallback_url = "";

		// get related documents for the given document title using the standard
		// parser

		if (standard) {
			String[] splitedTitle = title.trim().split("\\s+");
			StringBuffer queryString = new StringBuffer();
			for (int i = 0; i < splitedTitle.length; i++) {
				queryString.append("title:" + splitedTitle[i] + " OR ");
				queryString.append("abstract_en:" + splitedTitle[i] + " OR ");
				queryString.append("abstract_de:" + splitedTitle[i] + " OR ");
				queryString.append("keywords:" + splitedTitle[i]);
				if (i < splitedTitle.length - 1) {
					queryString.append(" OR ");
				}

			}

			if (constants.getDebugModeOn())
				logger.debug("set query with= " + queryString.toString());
			query.set("q", queryString.toString());
			if (constants.getDebugModeOn())
				logger.debug("set query with title: " + queryString.toString());
		} else {
			query.setQuery(title);

		}
		// return only "delimitedRows" much
		query.setRows(delimitedRows);
		if (constants.getDebugModeOn())
			logger.debug("max rows are: " + delimitedRows);
		// set display params
		query.setParam("fl", "score,id");

		try {
			if (constants.getDebugModeOn())
				logger.debug("try to get the response from solr! The query looks like: " + query);
			response = solr.query(query);
			if (constants.getDebugModeOn())
				logger.debug("response seems to be: " + response.toString());
			SolrDocumentList docs = response.getResults();
			if (constants.getDebugModeOn())
				logger.debug("Query Time: " + Integer.toString(response.getQTime()));

			// no related documents found
			if (docs.isEmpty()) {
				if (constants.getDebugModeOn())
					logger.debug("docs.isEmpty() is true");
				throw new NoRelatedDocumentsException("query was performed by title: " + title,
						"query was performed by title: " + title);
			} else {
				if (constants.getDebugModeOn())
					logger.debug("docs.isEmpty() is false");
				relatedDocuments.setSuggested_label("Related Articles");
				relatedDocuments.setNumberOfReturnedResults(docs.getNumFound());
				// for each document add it to documentSet
				for (int i = 0; i < docs.size(); i++) {
					// get the document
					oneRelatedDocument = con.getDocumentBy(constants.getDocumentId(),
							docs.get(i).getFieldValue(constants.getDocumentIdInSolr()).toString());

					// add the rank
					oneRelatedDocument.setSuggestedRank(i + 1);

					// add the solrScore
					oneRelatedDocument.setRelevanceScoreFromAlgorithm(
							Double.parseDouble(docs.get(i).getFieldValue("score").toString()));

					// set gesis specific link
					if (oneRelatedDocument.getCollectionShortName().equals(constants.getGesis()))
						fallback_url = constants.getGesisCollectionLink()
								.concat(oneRelatedDocument.getOriginalDocumentId());
					else if (oneRelatedDocument.getCollectionShortName().contains(constants.getCore()))
						fallback_url = constants.getCoreCollectionLink()
								.concat(oneRelatedDocument.getOriginalDocumentId().split("-")[1]);
					else if (oneRelatedDocument.getCollectionShortName().contains(constants.getMediatum()))
						fallback_url = constants.getMediatumCollectionLink()
								.concat(oneRelatedDocument.getOriginalDocumentId().split("-")[1]);
					else {
						String titleAsUrl = URLEncoder.encode(title, "UTF-8");
						fallback_url = "https://scholar.google.com/scholar?q=" + titleAsUrl;
						logger.debug("the fallback url is: " + fallback_url);
					}

					oneRelatedDocument.setFallbackUrl(fallback_url);
					// oneRelatedDocument.setClickUrl(fallback_url);

					// add it to the collection
					relatedDocuments.addDocument(oneRelatedDocument);
					// logger.debug("added the related document with
					// title: " + oneRelatedDocument.getTitle());
				}
			}
		} catch (NoRelatedDocumentsException f) {
			logger.debug("No related documents found related to " + title);
			throw f;
		}

		return relatedDocuments;
	}

}
