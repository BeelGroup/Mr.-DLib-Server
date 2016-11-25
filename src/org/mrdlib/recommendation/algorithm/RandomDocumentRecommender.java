package org.mrdlib.recommendation.algorithm;

import java.util.Random;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;

public class RandomDocumentRecommender extends RelatedDocumentsMLT {
	/**
	 * Creates a new Recommender that returns random documents
	 * 
	 * @param con
	 *            DatabaseConnection instance through which the database methods
	 *            can be accessed
	 * @throws Exception
	 *             if solrConnection cannot be created in the super-class
	 */
	public RandomDocumentRecommender(DBConnection con) throws Exception {
		super(con);
		algorithmLoggingInfo = new AlgorithmDetails("RandomDocumentRecommender", "random", false);
		
	}

	@Override
	/**
	 * calls the <code>getRelatedDocumentSet(DisplayDocument, int)</code>
	 * requesting the default number of documents.
	 * 
	 */
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc) throws Exception {
		return getRelatedDocumentSet(requestDoc, 10);
	}

	/**
	 * Returns <code>numberOfRelatedDocs</code> number of documents randomly
	 * selected from the collection. Addresses the Simplest case that all
	 * documents of same collection are consecutive in the database
	 * 
	 */
	@Override
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs) throws Exception {
		Random random = new Random();

		// get a random seed to use to generate a random number for sorting
		long randomSeed = random.nextLong();
		random.setSeed(randomSeed);
		String seed = Integer.toString(random.nextInt());

		try {
			// get the random recommendations from solr
			return scon.getRandomDocumentSet(requestDoc, numberOfRelatedDocs, false, seed);
		} catch (Exception e) {
			// e.printStackTrace();
			throw e;
		} /*finally {
		if (scon != null)
		scon.close();
}*/
	}

}
