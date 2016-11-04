package org.mrdlib.recommendation;

import org.mrdlib.Constants;
import org.mrdlib.database.DBConnection;
import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.DocumentSet;

public class StereotypeRecommender extends RelatedDocuments {

	private DBConnection con = null;
	private Constants constants = null;

	/**
	 * Creates a new instance of StereotypeRecommender which exposes methods to
	 * get stereotype documents from the database
	 * 
	 * @param con
	 *            DBConnection instance, not null, to access database methods
	 */
	public StereotypeRecommender(DBConnection con) {

		this.con = con;
		constants = new Constants();
		loggingInfo.clear();
		loggingInfo.put("name", "StereotypeRecommender");
		loggingInfo.put("recommendation_framework", "proprietary");
		loggingInfo.put("recommendation_class", "stereotype");
		loggingInfo.put("language_filter", "N");

	}

	@Override
	/**
	 * Calls the <code>getRelatedDocumentSet(DisplayDocument, int)</code> which
	 * returns default number of stereotype documents from the database
	 * 
	 */
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc) throws Exception {
		return getRelatedDocumentSet(requestDoc, 10);
	}

	@Override
	/**
	 * returns stereotype documents from database
	 */
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs) throws Exception {
		DocumentSet results = new DocumentSet(constants);
		try {
			results = con.getStereotypeRecommendations(requestDoc, numberOfRelatedDocs);
		} catch (Exception e) {
			throw e;
		}
		return results;

	}

}
