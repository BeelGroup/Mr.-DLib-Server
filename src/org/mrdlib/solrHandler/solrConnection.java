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

public class solrConnection {
	private Constants constants = new Constants();
	private SolrClient solr = null;
	private DBConnection con;

	public solrConnection() {
		String urlString = constants.getSolrWebService().concat(constants.getSolrMrdlib());
		solr = new HttpSolrClient.Builder(urlString).build();
		con = new DBConnection();
	}

	protected void finalize() throws Throwable {
		solr.close();
		super.finalize();
	}

	public DocumentSet getRelatedDocumentSetByDocument(Document document) {
		DocumentSet relatedDocuments = new DocumentSet();
		SolrQuery query = new SolrQuery();
		QueryResponse response = null;
		int delimitedRows = 10;
		Document relDocument = new Document();
		query.setRequestHandler("/" + MoreLikeThisParams.MLT);
		String url = "";
		String filterquery = constants.getSolrCollectionShortName() + ":" + document.getCollectionShortName();

		query.addFilterQuery(filterquery);
		query.setQuery(constants.getDocumentId() + ":" + document.getDocumentId());
		query.setRows(delimitedRows);

		try {
			response = solr.query(query);
			SolrDocumentList docs = response.getResults();

			if (docs.isEmpty())
				throw new NoRelatedDocumentsException(document.getOriginalDocumentId());
			else {
				for (int i = 0; i < docs.size(); i++) {
					relDocument = con.getDocumentBy(constants.getDocumentId(),
							docs.get(i).getFieldValue(constants.getDocumentId()).toString());
					relDocument.setSuggestedRank(i + 1);
					if (relDocument.getCollectionShortName().equals(constants.getGesis()))
						url = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());

					relDocument.setClickUrl(url);
					relDocument.setFallbackUrl(url);
					relatedDocuments.addDocument(relDocument);
					relatedDocuments.setSuggested_label("Related Articles");
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
