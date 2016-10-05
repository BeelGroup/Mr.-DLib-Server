package org.mrdlib.solrHandler;

import java.io.IOException;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.mrdlib.Constants;
import org.mrdlib.database.DBConnection;
import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.DocumentSet;

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
	 *            where similar documents are searched for
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
		DocumentSet relatedDocuments = new DocumentSet();
		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		DisplayDocument relDocument = new DisplayDocument();
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
			String similarityParams = getMltFL(logginginfo.get("cbf_text_fields"), logginginfo.get("typeOfGram"),
					logginginfo.get("cbf_feature_count"));
			query.setParam("mlt.fl", similarityParams);
		}
		// set display params
		query.setParam("fl", "score,id");
		//System.out.println(query);

		try {
			response = solr.query(query);
			SolrDocumentList docs = response.getResults();

			// no related documents found
			if (docs.isEmpty())
				throw new NoRelatedDocumentsException(document.getOriginalDocumentId(), document.getDocumentId());
			else {
				relatedDocuments.setSuggested_label("Related Articles");
				// for each document add it to documentSet
				for (int i = 0; i < docs.size(); i++) {
					// get the document
					relDocument = con.getDocumentBy(constants.getDocumentId(),
							docs.get(i).getFieldValue(constants.getDocumentId()).toString());

					// add the rank
					relDocument.setSuggestedRank(i + 1);

					// add the solrScore
					relDocument.setSolrScore(Double.parseDouble(docs.get(i).getFieldValue("score").toString()));

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
			}
		} catch (Exception e) {
			System.out.println("test: " + e.getStackTrace());
			throw e;
		}

		return relatedDocuments;
	}

	private String getMltFL(String source, String type, String number) {
		// TODO Auto-generated method stub
		String template = source + "_%s_" + number;
		String uni = String.format(template, "unigrams");
		String bi = String.format(template, "bigrams");
		String tri = String.format(template, "trigrams");
		switch (type) {
		case "allgrams":
			return uni + "," + bi + "," + "," + tri;
		case "unibi":
			return uni + "," + bi;
		case "unitri":
			return uni + "," + tri;
		case "bitri":
			return bi + "," + tri;
		default:
			return String.format(template, type);

		}

	}

	public DocumentSet getRandomDocumentSet(DisplayDocument document, int delimitedRows, Boolean restrictLanguage,
			String seed) throws Exception {
		DocumentSet relatedDocuments = new DocumentSet();
		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		DisplayDocument relDocument = new DisplayDocument();
		query.setRequestHandler("/select");
		String fallback_url = "";
		query.setQuery( "*:*");
		// get only documents which are in the same collection
		String filterquery = constants.getSolrCollectionShortName() + ":" + document.getCollectionShortName();
		query.addFilterQuery(filterquery);
		if (restrictLanguage) {
			query.addFilterQuery(constants.getLanguage() + ":" + document.getLanguage());
		}
		query.setParam("fl", "id,score");
		// return only "delimitedRows" much
		query.setRows(delimitedRows);
		query.setSort(SortClause.asc("random_" + seed));
		//System.out.println(query);
		try {
			response = solr.query(query);
			SolrDocumentList docs = response.getResults();

			// no related documents found
			if (docs.isEmpty())
				throw new NoRelatedDocumentsException(document.getOriginalDocumentId(), document.getDocumentId());
			else {
				relatedDocuments.setSuggested_label("Related Articles");
				// for each document add it to documentSet
				for (int i = 0; i < docs.size(); i++) {
					// get the document
					relDocument = con.getDocumentBy(constants.getDocumentId(),
							docs.get(i).getFieldValue(constants.getDocumentId()).toString());

					// add the rank
					relDocument.setSuggestedRank(i + 1);

					// add the solrScore
					relDocument.setSolrScore(Double.parseDouble(docs.get(i).getFieldValue("score").toString()));

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
