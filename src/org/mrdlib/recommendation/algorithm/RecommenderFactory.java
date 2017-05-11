package org.mrdlib.recommendation.algorithm;

import java.util.Random;

import org.mrdlib.api.manager.UnknownException;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.database.DBConnection;

public class RecommenderFactory {
	static RelatedDocuments rdg;

	/**
	 * Initializes a fallback recommender
	 * 
	 * Fallback recommender is currently hardcoded to Lucene-MLT
	 * 
	 * @param con
	 *            DBConnection object to pass onto the fallback recommender
	 * @return A fallback recommender object
	 * @throws Exception
	 */
	public static RelatedDocuments getFallback(DBConnection con) throws Exception {
		RelatedDocuments rdg = new RelatedDocumentsMLT(con);
		// rdg.algorithmLoggingInfo.replace("recommendation_class", "fallback");
		rdg.algorithmLoggingInfo.setFallback(true);
		return rdg;
	}

	/***
	 * Initializes a new recommender object according to the probabilities
	 * described in the probabilities.properties file. Uses the details of the
	 * document for which we are recommending to choose randomly from a valid
	 * subset of recommender objects
	 * 
	 * @param con
	 *            DBConnection object to pass onto the fallback recommender
	 * @param requestDocument
	 *            Document for which recommendations need to be generated
	 * @return A <code>RelatedDocuments</code> recommender object
	 * @throws Exception
	 */
	public static RelatedDocuments getRandomRDG(DBConnection con, DisplayDocument requestDocument,
			Boolean requestByTitle) throws Exception {

		Random random = new Random();

		if (requestByTitle) {
			if (random.nextBoolean())
				return new CoreSearch(con);
			if (random.nextBoolean())
				return new RelatedDocumentsQuery(con);
			else
				return new RelatedDocumentsQueryEdismax(con);

		}
		// Load probabilities from the config file

		// Start cumulative with prob(RandomDocumentRecommender), then keep
		// incrementing by the probability value for the next recommender in the
		// file
		if(Integer.parseInt(requestDocument.getDocumentId())>9505925){
			Integer coreRecommenderAPI = random.nextInt(10000);
			if(coreRecommenderAPI<2000){
				return random.nextBoolean()? new CoreSearch(con) : new CoreRecommender(con);
			}else return RecommenderFactory.returnStandardDistributionRDG(con, requestDocument);
		}
		// draw a random number

		return RecommenderFactory.returnStandardDistributionRDG(con, requestDocument);
		// return rdg;
	}

	public static RelatedDocuments returnStandardDistributionRDG(DBConnection con, DisplayDocument requestDocument) {
		Random random = new Random();

		int randomRecommendationApproach = random.nextInt(10000);

		Probabilities probs = new Probabilities();
		int cumulative = probs.getRandomDocumentRecommender();

		try {
			// CASE: Completely random
			if (randomRecommendationApproach < cumulative)
				rdg = new RandomDocumentRecommender(con);
			else {
				// CASE: Random with lang restriction
				cumulative += probs.getRandomDocumentRecommenderLanguageRestricted();
				if (randomRecommendationApproach < cumulative)
					rdg = new RandomDocumentRecommenderLanguageRestricted(con);
				else {
					cumulative += probs.getStereotypeRecommender(); // CASE:
																	// Stereotype
					if (randomRecommendationApproach < cumulative)
						rdg = new StereotypeRecommender(con);
					else {
						cumulative += probs.getMostPopular();
						if (randomRecommendationApproach < cumulative)
							rdg = new MostPopularRecommender(con);
						else {
							cumulative += probs.getRelatedDocumentsFromSolr(); // CASE:
																				// metadata
																				// based
																				// from
																				// SOLR
							if (randomRecommendationApproach < cumulative)
								rdg = new RelatedDocumentsMLT(con);
							else {
								String language = requestDocument.getLanguage(); // Validity
																					// of
																					// keyphrase
																					// algo
																					// depends
																					// on
																					// language.
																					// So
																					// check
																					// language
								if (language == null || !language.equals("en"))
									rdg = getFallback(con); // If not english,
															// use
															// fallback.
								else {
									// Check presence and language of abstract
									rdg = new RelatedDocumentsKeyphrases(con);
									String abstLang = con.getAbstractDetails(requestDocument);
									if (!abstLang.equals("en"))
										// if not set algorithmLoggingInfo.type
										// to title only
										rdg.algorithmLoggingInfo.setCbfTextFields("title_keywords_published_in");
									// otherwise leave it unset.
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			if (rdg != null) {
				e.printStackTrace();
				System.out.println(rdg.getClass().getName() + " has failed to initialize");
			}
			throw new UnknownException(e, true);
		}
		return rdg;
	}

}