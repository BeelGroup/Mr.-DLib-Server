package org.mrdlib.recommendation.algorithm;

import java.util.Random;

import org.mrdlib.api.manager.UnknownException;
import org.mrdlib.api.manager.Constants;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.database.NoEntryException;

public class RecommenderFactory {

	public static RelatedDocuments getAlgorithmById(Algorithm id, DBConnection con) throws Exception {
		return getAlgorithmById(id,con,false);
	}

	public static RelatedDocuments getAlgorithmById(Algorithm id, DBConnection con, boolean byTitle) throws Exception {
		switch(id) {
		case RANDOM_DOCUMENT:
			return new RandomDocumentRecommender(con);
		case RANDOM_LANGUAGE_RESTRICTED:
			return new RandomDocumentRecommenderLanguageRestricted(con);
		case FROM_SOLR:
			if (byTitle)
				return new RelatedDocumentsQuery(con);
			else
				return new RelatedDocumentsMLT(con);
		case STEREOTYPE:
			return new StereotypeRecommender(con);
		case MOST_POPULAR:
			return new MostPopularRecommender(con);
		case FROM_SOLR_WITH_KEYPHRASES:
			return new RelatedDocumentsKeyphrasesRevised(con);
		case DOC2VEC:
			return new Doc2VecRecommender(con);
		default:
			throw new Exception("Unknown algorithm: " + id);
		}
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
	public static RelatedDocuments getRandomRDG(DBConnection con, DocumentSet docSet, Boolean requestByTitle)
			throws Exception {
		DisplayDocument requestDocument = docSet.getRequestedDocument();
		String appId = docSet.getDebugDetailsPerSet().getRequestingAppId();
		Random random = new Random();

		if (requestByTitle) {
			return requestByTitleRDG(appId, random, con);
		}
		// Load probabilities from the config file

		// Start cumulative with prob(RandomDocumentRecommender), then keep
		// incrementing by the probability value for the next recommender in the
		// file
		if (Integer.parseInt(requestDocument.getDocumentId()) > 9505925) {
			Integer coreRecommenderAPI = random.nextInt(10000);
			if (coreRecommenderAPI < 2000 && checkAccessToCoreAPI(appId, con)) {
				return random.nextBoolean() ? new CoreRecommender(con) : new CoreRecommender(con);
			} else
				return RecommenderFactory.returnStandardDistributionRDG(con, requestDocument);
		}
		// draw a random number

		return RecommenderFactory.returnStandardDistributionRDG(con, requestDocument);
		// return new CoreRecommender(con);
	}

	private static RelatedDocuments requestByTitleRDG(String appId, Random random, DBConnection con)
			throws Exception {

		/*
		 * If the documentSet's application_id indicates that the request is
		 * from Jabref, we can include the CORE Recommender in the randomization
		 * process. If not we use the standardDistributionRDG for requestByTitle
		 */
		
		Boolean requestIsFromJabref = checkAccessToCoreAPI(appId, con);

		if (requestIsFromJabref) {
			if (random.nextInt(3) == 0 )
				return new CoreSearch(con);
			else
				return returnStandardQueryByTitleRDG(con, random);
		} else
			return returnStandardQueryByTitleRDG(con, random);

	}

	private static Boolean checkAccessToCoreAPI(String appId, DBConnection con) {
		try {
			return con.getApplicationId("jabref_desktop").equalsIgnoreCase(appId)
					|| con.getApplicationId("mdl_client_test").equalsIgnoreCase(appId);
		} catch (NoEntryException e) {
			if (appId != null)
				System.out.println("AppId " + appId + " not present in our db");
			return false;
		}
	}

	private static RelatedDocuments returnStandardQueryByTitleRDG(DBConnection con, Random random) throws Exception {
		if (random.nextBoolean()) {
			return new RelatedDocumentsQuery(con);
		} else {
			return new RelatedDocumentsQueryEdismax(con);
		}
	}

	public static RelatedDocuments returnStandardDistributionRDG(DBConnection con, DisplayDocument requestDocument) {
		RelatedDocuments rdg = null;
		Probabilities probs = new Probabilities();
		Algorithm choice = probs.next();
		Constants constants = new Constants();

		try {
			rdg = getAlgorithmById(choice,con);
			if (choice == Algorithm.FROM_SOLR_WITH_KEYPHRASES) {
				String language = requestDocument.getLanguage(); 
				if (language == null || !language.equals("en")) {
					rdg = getFallback(con); 
				} else if (choice == Algorithm.FROM_SOLR_WITH_KEYPHRASES &&
						   !con.getAbstractDetails(requestDocument, constants.getAbstractLanguage()).equals("en")) {
					// if abstract not english set algorithmLoggingInfo.type to title only
					rdg.algorithmLoggingInfo.setCbfTextFields("title_keywords_published_in");
				}
			} else if (choice == Algorithm.DOC2VEC) {
				// get detected language of abstract
				String language = con.getAbstractDetails(requestDocument, constants.getLanguageDetected());
				if (language == null || !language.equals("en")) {
					rdg = getFallback(con);
				}
			}
			return rdg;
		} catch (Exception e) {
			if (rdg != null) {
				e.printStackTrace();
				System.out.println(rdg.getClass().getName() + " has failed to initialize");
			}
			throw new UnknownException(e, true);
		}
	}

}
