package org.mrdlib.recommendation.algorithm;

import java.util.Random;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;

public class StereotypeRecommender extends RelatedDocuments {

	private DBConnection con = null;

	/**
	 * Creates a new instance of StereotypeRecommender which exposes methods to
	 * get stereotype documents from the database
	 * 
	 * @param con
	 *            DBConnection instance, not null, to access database methods
	 */
	public StereotypeRecommender(DBConnection con) {

		this.con = con;
		Random random = new Random();
		String category = "";
		switch(random.nextInt(4)){
		case 0: category = "mix";
		break;
		case 1: category = "academic_writing";
		break;
		case 2: category = "research_methods";		break;

		case 3: category = "research_evaluation_and_peer_review";		break;

		default: category = "mix";
		}
		algorithmLoggingInfo = new AlgorithmDetails("StereotypeRecommender", "stereotypes", false, category);

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
		DocumentSet results = new DocumentSet();
		try {
			results = con.getStereotypeRecommendations(requestDoc, numberOfRelatedDocs, algorithmLoggingInfo);
		} catch (Exception e) {
			throw e;
		}
		return results;

	}

}
