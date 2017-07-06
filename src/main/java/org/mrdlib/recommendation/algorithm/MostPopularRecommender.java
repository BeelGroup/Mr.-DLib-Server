package org.mrdlib.recommendation.algorithm;

import java.util.Random;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;

public class MostPopularRecommender extends RelatedDocuments {

	private DBConnection con = null;

	/**
	 * Creates a new instance of StereotypeRecommender which exposes methods to
	 * get stereotype documents from the database
	 * 
	 * @param con
	 *            DBConnection instance, not null, to access database methods
	 */
	public MostPopularRecommender(DBConnection con) {

		this.con = con;
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
	 * returns mostPopular documents from database
	 */
	public DocumentSet getRelatedDocumentSet(DocumentSet requestDocSet) throws Exception {
		DocumentSet results = new DocumentSet();
		requestDocSet.setAlgorithmDetails(algorithmLoggingInfo);
		try {
			results = con.getStereotypeRecommendations(requestDocSet);
		} catch (Exception e) {
			throw e;
		}
		return results;

	}

}
