package org.mrdlib.recommendation.algorithm;

import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.recommendation.framework.NoRelatedDocumentsException;
import org.mrdlib.recommendation.framework.solrConnection;

public class RelatedDocumentsQuery extends RelatedDocuments {
	DBConnection con = null;
	solrConnection scon = null;

	/**
	 * Creates a new instance of RelatedDocumentsMLT which exposes methods to
	 * use Lucene's MoreLikeThis feature to get related documents
	 * 
	 * @param con
	 *            DBConnection instance, not null, to access database methods
	 * @throws Exception
	 *             if solrConnection cannot be instantiated.
	 */
	public RelatedDocumentsQuery(DBConnection con) throws Exception {

		System.out.println("yeah I'm using the RelatedDocumentsQuery");
		try {
			this.con = con;
			scon = new solrConnection(con);

			// Store the details of the recommender approach for future use in
			// the algorithmLoggingInfo hashmap

			algorithmLoggingInfo = new AlgorithmDetails("RelatedDocumentsFromSolrByQuery", "cbf", false,
					"title_abstract_keywords_published_in", "terms", "0","standardQP");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	/**
	 * returns related documents using Lucene's Query function
	 * 
	 */
	public DocumentSet getRelatedDocumentSet(DocumentSet requestDoc) throws Exception {
		try {
			System.out.println("No im trying to getRelatedDocumentSet with a normal query");
			// Query solr using the defaults set in solrConfig.xml
			requestDoc.setAlgorithmDetails(algorithmLoggingInfo);
			return scon.getDocumentsFromSolrByQuery(requestDoc);
		} catch (NoRelatedDocumentsException f) {
			System.out.println("No related documents for document with title: " + requestDoc.getRequestedDocument().getTitle());
			throw f;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} /*
			 * finally { if (scon != null) scon.close(); }
			 */
	}

}