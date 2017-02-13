package org.mrdlib.recommendation.framework;

import java.net.URLEncoder;

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

/**
 * 
 * @author Millah
 *
 *         This class handles all the communication with solr
 *
 */
public class solrConnection {
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
	public DocumentSet getRelatedDocumentSetByDocument(DisplayDocument document, int delimitedRows,
			AlgorithmDetails logginginfo) throws Exception {
		DocumentSet relatedDocuments = new DocumentSet();
		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		DisplayDocument relDocument = new DisplayDocument();
		query.setRequestHandler("/" + MoreLikeThisParams.MLT);
		String fallback_url = "";

		// get only documents which are in the same collection
		String filterquery = "";
		if (document.getCollectionShortName() == constants.getGesis())
			filterquery = constants.getSolrCollectionShortName() + ":" + document.getCollectionShortName();
	 
		query.addFilterQuery(filterquery);

		// get related documents for the given document
		query.setQuery(constants.getDocumentIdInSolr() + ":" + document.getDocumentId());

		// return only "delimitedRows" much
		query.setRows(delimitedRows);
		// if rec_approach is Keyphrases: override default mlt.fl
		if (logginginfo.getName().equals("RelatedDocumentsFromSolrWithKeyphrases")) {
			String similarityParams = getMltFL(logginginfo.getCbfTextFields(), logginginfo.getCbfFeatureType(),
					logginginfo.getCbfFeatureCount());
			query.setParam("mlt.fl", similarityParams + ", keywords, published_in");
			query.setParam("mlt.df", "2");
		}
		// set display params
		query.setParam("fl", "score,id");
		// System.out.println(query);
		// System.out.println(timeNow);
		try {
			response = solr.query(query);

			SolrDocumentList docs = response.getResults();
			System.out.println("Query Time: " + Integer.toString(response.getQTime()));
			// no related documents found
			if (docs.isEmpty()) {
				// System.out.println("In here");
				throw new NoRelatedDocumentsException(document.getOriginalDocumentId(), document.getDocumentId());
			} else {
				long timeNow = System.currentTimeMillis();
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
					if (relDocument.getCollectionShortName().equals(constants.getGesis()))
						fallback_url = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());
					else if (relDocument.getCollectionShortName().contains(constants.getCore()))
						fallback_url = constants.getCoreCollectionLink()
								.concat(relDocument.getOriginalDocumentId().split("-")[1]);

					// url = "http://api.mr-dlib.org/trial/recommendations/" +
					// relDocument.getRecommendationId() +
					// "/original_url?access_key=" +"hash"
					// +"&format=direct_url_forward";

					// relDocument.setClickUrl(url);
					relDocument.setFallbackUrl(fallback_url);
					// add it to the collection
					relatedDocuments.addDocument(relDocument);
				}
				System.out.printf("Time for adding docs to list\t");
				System.out.println(System.currentTimeMillis() - timeNow);
			}
		} catch (Exception e) {
			System.out.println("test: " + e.getStackTrace());
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
		String template = source + "_%s_" + number;
		String uni = String.format(template, "unigrams");
		String bi = String.format(template, "bigrams");
		String tri = String.format(template, "trigrams");
		switch (type) {
		case "unibitri":
			return uni + "," + bi + "," + tri;
		case "unibi":
			return uni + "," + bi;
		case "unitri":
			return uni + "," + tri;
		case "bitri":
			return bi + "," + tri;
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
	public DocumentSet getRandomDocumentSet(DisplayDocument document, int delimitedRows, Boolean restrictLanguage,
			String seed) throws Exception {

		DocumentSet relatedDocuments = new DocumentSet();
		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		DisplayDocument relDocument = new DisplayDocument();

		// Setting solr query to select mode
		query.setRequestHandler("/select");
		String fallback_url = "";
		query.setQuery("*:*");
		String filterQuery = "";
		// get only documents which are in the same collection
		if (document.getCollectionShortName() == constants.getGesis())
			filterQuery= constants.getSolrCollectionShortName() + ":" + document.getCollectionShortName();
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
					if (relDocument.getCollectionShortName().equals(constants.getGesis()))
						fallback_url = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());
					else if (relDocument.getCollectionShortName().contains(constants.getCore()))
						fallback_url = constants.getCoreCollectionLink()
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
			System.out.println("test: " + e.getStackTrace());
			throw e;
		}
		return relatedDocuments;
	}

	public DocumentSet getDocumentsFromSolrByQuery(DisplayDocument requestedDocument, int delimitedRows,
			AlgorithmDetails logginginfo) throws Exception {
		System.out.println("reached solr connection class and im in method getDocumentsFromSolrByQuery");
		String title = requestedDocument.getTitle();
		DocumentSet relatedDocuments = new DocumentSet();
		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		DisplayDocument oneRelatedDocument = new DisplayDocument();
		query.setRequestHandler("/select");
		String fallback_url = "";

		// get related documents for the given document title
		String[] splitedTitle = title.split("\\s+");
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
		System.out.println("set query with= " + queryString.toString());
		query.set("q", queryString.toString());
		System.out.println("set query with title: " + queryString.toString());

		// return only "delimitedRows" much
		query.setRows(delimitedRows);
		System.out.println("max rows are: " + delimitedRows);

		// if rec_approach is Keyphrases: override default mlt.fl
		if (logginginfo.getName().equals("RelatedDocumentsFromSolrWithKeyphrases")) {
			System.out.println("if with RelatedDocumentsFromSolrWithKeyphrases");
			String similarityParams = getMltFL(logginginfo.getCbfTextFields(), logginginfo.getCbfFeatureType(),
					logginginfo.getCbfFeatureCount());
			query.setParam("mlt.fl", similarityParams);
			query.setParam("mlt.df", "2");
		}

		// set display params
		query.setParam("fl", "score,id");

		try {
			System.out.println("try to get the response from solr! The query looks like: " + query);
			response = solr.query(query);
			System.out.println("response seems to be: " + response.toString());
			SolrDocumentList docs = response.getResults();
			System.out.println("Query Time: " + Integer.toString(response.getQTime()));

			// no related documents found
			if (docs.isEmpty()) {
				System.out.println("docs.isEmpty() is true");
				throw new NoRelatedDocumentsException("query was performed by title: " + title,
						"query was performed by title: " + title);
			} else {
				System.out.println("docs.isEmpty() is false");
				long timeNow = System.currentTimeMillis();
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
					else if(oneRelatedDocument.getCollectionShortName().contains(constants.getCore()))
						fallback_url = constants.getCoreCollectionLink()
								.concat(oneRelatedDocument.getOriginalDocumentId().split("-")[1]);
					else{
						String titleAsUrl = URLEncoder.encode(title, "UTF-8");
						fallback_url = "https://scholar.google.com/scholar?q=" + titleAsUrl;
						System.out.println("the fallback url is: " + fallback_url);
					}

					oneRelatedDocument.setFallbackUrl(fallback_url);
					oneRelatedDocument.setClickUrl(fallback_url);

					// add it to the collection
					relatedDocuments.addDocument(oneRelatedDocument);
					//System.out.println("added the related document with title: " + oneRelatedDocument.getTitle());
				}
				System.out.printf("Time for adding docs to list\t");
				System.out.println(System.currentTimeMillis() - timeNow);
			}
		} catch (NoRelatedDocumentsException f) {
			System.out.println("No related documents found related to " + title);
			throw f;
		}

		return relatedDocuments;
	}

}
