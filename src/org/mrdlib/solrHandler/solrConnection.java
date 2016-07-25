package org.mrdlib.solrHandler;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.mrdlib.Document;
import org.mrdlib.DocumentSet;
import org.mrdlib.database.Constants;
import org.mrdlib.database.DBConnection;
/**
 * 
 * @author Millah
 *
 * This class handles all the communication with solr
 *
 */
public class solrConnection {
	private Constants constants = new Constants();
	private SolrClient solr = null;
	private DBConnection con;

	/**
	 * create a solr connection
	 */
	public solrConnection() {
		String urlString = constants.getSolrWebService().concat(constants.getSolrMrdlib());
		solr = new HttpSolrClient.Builder(urlString).build();
		con = new DBConnection();
	}

	/**
	 * close a solr connection
	 */
	protected void finalize() throws Throwable {
		solr.close();
		super.finalize();
	}

	/**
	 * 
	 * get the first 10 related documents of a query document from solr
	 * 
	 * @param document, where similar documents are searched for
	 * @return the 10 most related documents in a document set
	 */
	public DocumentSet getRelatedDocumentSetByDocument(Document document) {
		DocumentSet relatedDocuments = new DocumentSet();
		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		int delimitedRows = 10;
		Document relDocument = new Document();
		query.setRequestHandler("/" + MoreLikeThisParams.MLT);
		String url = "";
		
		//get only documents which are in the same collection
		String filterquery = constants.getSolrCollectionShortName() + ":" + document.getCollectionShortName();

		query.addFilterQuery(filterquery);
		//get related documents for the given document
		query.setQuery(constants.getDocumentId() + ":" + document.getDocumentId());
		//return only "delimitedRows" much
		query.setRows(delimitedRows);

		try {
			response = solr.query(query);
			SolrDocumentList docs = response.getResults();

			//no related documents found
			if (docs.isEmpty())
				throw new NoRelatedDocumentsException(document.getOriginalDocumentId());
			else {
				relatedDocuments.setSuggested_label("Related Articles");
				//for each document add it to documentSet
				for (int i = 0; i < docs.size(); i++) {
					//get the document
					relDocument = con.getDocumentBy(constants.getDocumentId(),
							docs.get(i).getFieldValue(constants.getDocumentId()).toString());
					//add the rank
					relDocument.setSuggestedRank(i + 1);
					//set gesis specific link
					if (relDocument.getCollectionShortName().equals(constants.getGesis()))
						url = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());

					relDocument.setClickUrl(url);
					relDocument.setFallbackUrl(url);
					//add it to the collection
					relatedDocuments.addDocument(relDocument);
				}
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoRelatedDocumentsException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return relatedDocuments;
	}
}
