package org.mrdlib.recommendation.framework;

import java.io.IOException;
import java.util.HashMap;

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
	protected void finalize() throws Throwable {
		solr.close();
		super.finalize();
	}

	public void close() throws IOException {
		solr.close();
	}

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
			HashMap<String, String> logginginfo) throws Exception {
		DocumentSet relatedDocuments = new DocumentSet(constants);
		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		DisplayDocument relDocument = new DisplayDocument(constants);
		query.setRequestHandler("/" + MoreLikeThisParams.MLT);
		String fallback_url = "";

		// get only documents which are in the same collection
		String filterquery = constants.getSolrCollectionShortName() + ":" + document.getCollectionShortName();
		query.addFilterQuery(filterquery);

		// get related documents for the given document
		query.setQuery(constants.getDocumentId() + ":" + document.getDocumentId());

		// return only "delimitedRows" much
		query.setRows(delimitedRows);
		// if rec_approach is Keyphrases: override default mlt.fl
		if (logginginfo.get("name").equals("RelatedDocumentsFromSolrWithKeyphrases")) {
			String similarityParams = getMltFL(logginginfo.get("cbf_text_fields"), logginginfo.get("cbf_feature_type"),
					logginginfo.get("cbf_feature_count"));
			query.setParam("mlt.fl", similarityParams);
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
							docs.get(i).getFieldValue(constants.getDocumentId()).toString());

					// add the rank
					relDocument.setSuggestedRank(i + 1);

					// add the solrScore
					relDocument
							.setTextRelevancyScore(Double.parseDouble(docs.get(i).getFieldValue("score").toString()));

					// set gesis specific link
					if (relDocument.getCollectionShortName().equals(constants.getGesis()))
						fallback_url = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());
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
			return String.format(template, type+"s");

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

		DocumentSet relatedDocuments = new DocumentSet(constants);
		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		DisplayDocument relDocument = new DisplayDocument(constants);

		// Setting solr query to select mode
		query.setRequestHandler("/select");
		String fallback_url = "";
		query.setQuery("*:*");

		// get only documents which are in the same collection
		String filterquery = constants.getSolrCollectionShortName() + ":" + document.getCollectionShortName();
		query.addFilterQuery(filterquery);

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
							docs.get(i).getFieldValue(constants.getDocumentId()).toString());

					// add the rank
					relDocument.setSuggestedRank(i + 1);

					// add the solrScore
					relDocument
							.setTextRelevancyScore(Double.parseDouble(docs.get(i).getFieldValue("score").toString()));

					// set gesis specific link
					if (relDocument.getCollectionShortName().equals(constants.getGesis()))
						fallback_url = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());
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
}
