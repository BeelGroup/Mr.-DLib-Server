package org.mrdlib.recommendation.algorithm;

import java.util.Random;

import org.mrdlib.api.manager.UnknownException;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.database.DBConnection;

public class RecommenderFactory {
	static RelatedDocuments rdg;

	/**
	 * Initializes a new recommender object according to the probabilities
	 * described in the probabilities.properties file Unoptimized -- Better not
	 * use this
	 * 
	 * @param con
	 *            DBConnection object to pass onto the recommender object
	 * @return A <code>RelatedDocuments</code> recommender object
	 * @throws Exception
	 */
	public static RelatedDocuments getRandomRDG(DBConnection con) throws Exception {

		// Load probabilities from the config file
		Random random = new Random();
		Probabilities probs = new Probabilities();

		// Start cumulative with prob(RandomDocumentRecommender), then keep
		// incrementing by the probability value for the next recommender in the
		// file
		int cumulative = probs.getRandomDocumentRecommender();

		// draw a random number
		int randomRecommendationApproach = random.nextInt(10000);
		if (randomRecommendationApproach < cumulative)
			rdg = new RandomDocumentRecommender(con);
		else {
			cumulative += probs.getRandomDocumentRecommenderLanguageRestricted();
			if (randomRecommendationApproach < cumulative)
				rdg = new RandomDocumentRecommenderLanguageRestricted(con);
			else {
				cumulative += probs.getRelatedDocumentsFromSolr();
				if (randomRecommendationApproach < cumulative)
					rdg = new RelatedDocumentsMLT(con);
				else {
					cumulative += probs.getRelatedDocumentsFromSolrWithKeyphrases();
					if (randomRecommendationApproach < cumulative)
						rdg = new RelatedDocumentsKeyphrases(con);
					else
						rdg = new StereotypeRecommender(con);
				}
			}
		}

		// for testing individually:
		// return new RandomDocumentRecommenderLanguageRestricted(con);
		return rdg;
	}

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
		rdg.loggingInfo.replace("recommendation_class", "fallback");
		rdg.loggingInfo.replace("name", "fallback");
		return rdg;
	}

	/***
	 * Initializes a new recommender object according to the probabilities
	 * described in the probabilities.properties file Uses the details of the
	 * document for which we are recommending to choose randomly from a valid
	 * subset of recommender objects
	 * 
	 * @param con
	 *            DBConnection object to pass onto the fallback recommender
	 * @param requestDocument
	 *            Document for which recommendations need to be generated
	 * @return A <code>RelatedDocuments</code> recommender object
	 */
	public static RelatedDocuments getRandomRDG(DBConnection con, DisplayDocument requestDocument) {

		// Load probabilities from the config file
		Random random = new Random();
		Probabilities probs = new Probabilities();

		// Start cumulative with prob(RandomDocumentRecommender), then keep
		// incrementing by the probability value for the next recommender in the
		// file
		int cumulative = probs.getRandomDocumentRecommender();

		// draw a random number
		int randomRecommendationApproach = random.nextInt(10000);
		
		try {
			if (randomRecommendationApproach < cumulative) // CASE: Completely
															// random
				rdg = new RandomDocumentRecommender(con);
			else {
				cumulative += probs.getRandomDocumentRecommenderLanguageRestricted(); // CASE:
																						// Random
																						// with
																						// lang
																						// restriction
				if (randomRecommendationApproach < cumulative)
					rdg = new RandomDocumentRecommenderLanguageRestricted(con);
				else {
					cumulative += probs.getStereotypeRecommender(); // CASE:
																	// Stereotype
					if (randomRecommendationApproach < cumulative)
						rdg = new StereotypeRecommender(con);
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
								rdg = getFallback(con); // If not english, use
														// fallback.
							else {
								// Check presence and language of abstract
								rdg = new RelatedDocumentsKeyphrases(con);
								String abstLang = con.getAbstractDetails(requestDocument);
								if (!abstLang.equals("en"))
									// if not set loggingInfo.type to title only
									rdg.loggingInfo.replace("cbf_text_fields", "title");
								// otherwise leave it unset.
							}
						}
					}
				}
			}
		} catch (Exception e) {
			if (rdg != null)
				System.out.println(rdg.getClass().getName() + " has failed to initialize");
			throw new UnknownException(e, true);
		}
		return rdg;
	}
}
