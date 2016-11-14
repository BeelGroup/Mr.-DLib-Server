package org.mrdlib.recommendation.algorithm;

import java.util.Random;

import org.mrdlib.api.manager.Constants;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;

public class MostPopularRecommender extends RelatedDocuments {

	private DBConnection con = null;
	private Constants constants = null;

	/**
	 * Creates a new instance of StereotypeRecommender which exposes methods to
	 * get stereotype documents from the database
	 * 
	 * @param con
	 *            DBConnection instance, not null, to access database methods
	 */
	public MostPopularRecommender(DBConnection con) {

		this.con = con;
		constants = new Constants();
		Random random = new Random();
		String category = "";
		switch(random.nextInt(2)){
		case 0: category = "top_views";
		break;
		case 1: category = "top_exported";
		}
		algorithmLoggingInfo = new AlgorithmDetails("MostPopularRecommender", "most_popular", false, category);

	}

	@Override
	/**
	 * Calls the <code>getRelatedDocumentSet(DisplayDocument, int)</code> which
	 * returns default number of mostPopular documents from the database
	 * 
	 */
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc) throws Exception {
		return getRelatedDocumentSet(requestDoc, 10);
	}

	@Override
	/**
	 * returns mostPopular documents from database
	 */
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs) throws Exception {
		DocumentSet results = new DocumentSet(constants);
		try {
			results = con.getStereotypeRecommendations(requestDoc, numberOfRelatedDocs, algorithmLoggingInfo);
		} catch (Exception e) {
			throw e;
		}
		return results;

	}

}
