package org.mrdlib.recommendation.algorithm;

import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.recommendation.framework.NoRelatedDocumentsException;
import org.mrdlib.recommendation.framework.solrConnection;

public class RelatedDocumentsMLT extends RelatedDocuments {
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
	public RelatedDocumentsMLT(DBConnection con) throws Exception {

		try {
			this.con = con;
			scon = new solrConnection(con);

			// Store the details of the recommender approach for future use in
			// the algorithmLoggingInfo hashmap

			algorithmLoggingInfo = new AlgorithmDetails("RelatedDocumentsFromSolr", "cbf", false,
					"title_abstract_keywords_published_in", "terms", "0");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	@Override
	/**
	 * returns related documents using Lucene's MoreLikeThis function
	 * 
	 */
	public DocumentSet getRelatedDocumentSet(DocumentSet requestDoc) throws Exception {
		try {

			// Query solr using the defaults set in solrConfig.xml
			requestDoc.setAlgorithmDetails(algorithmLoggingInfo);
			return scon.getRelatedDocumentSetByDocument(requestDoc);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} /*
			 * finally { if (scon != null) scon.close(); }
			 */
	}

}
