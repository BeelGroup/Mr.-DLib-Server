package org.mrdlib.recommendation.algorithm;

import java.util.Random;

import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;

public class RandomDocumentRecommenderLanguageRestricted extends RandomDocumentRecommender {
	/**
	 * Creates a new Recommender that returns random documents which share same
	 * language as <code>requestDoc</code>
	 * 
	 * @param con
	 *            DatabaseConnection instance through which the database methods
	 *            can be accessed
	 * @throws Exception
	 *             if solrConnection cannot be created in the super-class
	 */
	public RandomDocumentRecommenderLanguageRestricted(DBConnection con) throws Exception {
		super(con);
		algorithmLoggingInfo.setName("RandomDocumentRecommenderLanguageRestricted");
		algorithmLoggingInfo.setLanguageRestriction(true);
	}


	@Override
	public DocumentSet getRelatedDocumentSet(DocumentSet requestDocSet) throws Exception {
		Random random = new Random();
		long randomSeed = random.nextLong();
		random.setSeed(randomSeed);
		String seed = Integer.toString(random.nextInt());

		requestDocSet.setAlgorithmDetails(algorithmLoggingInfo);

		try {
			return scon.getRandomDocumentSet(requestDocSet, true, seed);
		} catch (Exception e) {
			throw e;
		} /*finally {
		if (scon != null)
		scon.close();
}*/
	}

}
