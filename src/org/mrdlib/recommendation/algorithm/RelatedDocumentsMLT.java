package org.mrdlib.recommendation.algorithm;

import org.mrdlib.api.response.DisplayDocument;
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
			// the loggingInfo hashmap
			loggingInfo.put("name", "RelatedDocumentsFromSolr");
			//loggingInfo.put("recommendation_framework", "lucene");
			loggingInfo.put("recommendation_class", "cbf");
			loggingInfo.put("restriction_only_documents_from_this_language", "N");
			loggingInfo.put("cbf_text_fields", "all");
			loggingInfo.put("cbf_feature_type", "terms");
			loggingInfo.put("cbf_feature_count", "0");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	/**
	 * Calls the <code>getRelatedDocumentSet(DisplayDocument, int)</code> which
	 * returns default number of related documents using Lucene's MoreLikeThis
	 * function
	 * 
	 */
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc) throws Exception {
		return getRelatedDocumentSet(requestDoc, 10);
	}

	@Override
	/**
	 * returns related documents using Lucene's MoreLikeThis function
	 * 
	 */
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs) throws Exception {
		try {

			// Query solr using the defaults set in solrConfig.xml
			return scon.getRelatedDocumentSetByDocument(requestDoc, numberOfRelatedDocs, loggingInfo);
		} catch (NoRelatedDocumentsException f) {
			System.out.println("No related documents for doc_id " + requestDoc.getDocumentId());
			throw f;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (scon != null)
				scon.close();
		}
	}

}
