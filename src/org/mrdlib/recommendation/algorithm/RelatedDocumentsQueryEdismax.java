package org.mrdlib.recommendation.algorithm;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.recommendation.framework.NoRelatedDocumentsException;
import org.mrdlib.recommendation.framework.solrConnection;

public class RelatedDocumentsQueryEdismax extends RelatedDocuments {
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
	public RelatedDocumentsQueryEdismax(DBConnection con) throws Exception {

		System.out.println("yeah I'm using the RelatedDocumentsQuery");
		try {
			this.con = con;
			scon = new solrConnection(con);

			// Store the details of the recommender approach for future use in
			// the algorithmLoggingInfo hashmap

			algorithmLoggingInfo = new AlgorithmDetails("RelatedDocumentsFromSolrByQueryEdismax", "cbf", false,
					"title_abstract_keywords_published_in", "terms", "0", "edismaxQP");

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
	 * returns related documents using Lucene's Query function
	 * 
	 */
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs) throws Exception {
		try {
			System.out.println("No im trying to getRelatedDocumentSet with a normal query");
			// Query solr using the defaults set in solrConfig.xml
			return scon.getDocumentsFromSolrByQuery(requestDoc, numberOfRelatedDocs, algorithmLoggingInfo);
		} catch (NoRelatedDocumentsException f) {
			System.out.println("No related documents for document with title: " + requestDoc.getTitle());
			throw f;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} /*
			 * finally { if (scon != null) scon.close(); }
			 */
	}

}