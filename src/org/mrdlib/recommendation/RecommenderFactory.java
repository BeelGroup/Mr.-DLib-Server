package org.mrdlib.recommendation;

import java.util.Random;

import org.mrdlib.UnknownException;
import org.mrdlib.database.DBConnection;
import org.mrdlib.display.DisplayDocument;

public class RecommenderFactory {
	static RelatedDocuments rdg;
	
	public static RelatedDocuments  getRandomRDG(DBConnection con) throws Exception{
		Random random = new Random();
		Probabilities probs = new Probabilities();
		int cumulative = probs.getRandomDocumentRecommender();
		int randomRecommendationApproach = random.nextInt(10000);
		if(randomRecommendationApproach < cumulative)
			rdg = new RandomDocumentRecommender(con);
		else{
			cumulative += probs.getRandomDocumentRecommenderLanguageRestricted();
			if (randomRecommendationApproach < cumulative)
				rdg = new RandomDocumentRecommenderLanguageRestricted(con);
			else{
				cumulative += probs.getRelatedDocumentsFromSolr();
				if (randomRecommendationApproach < cumulative)
					rdg = new RelatedDocumentsMLT(con);
				else{
					cumulative += probs.getRelatedDocumentsFromSolrWithKeyphrases();
					if(randomRecommendationApproach < cumulative)
						rdg = new RelatedDocumentsKeyphrases(con);
					else rdg = new StereotypeRecommender(con);
				}
			}
		}
		//ADD lines of code to log the random recommender we have picked in this instance
		//return new RandomDocumentRecommenderLanguageRestricted(con);
		return rdg;
	}
	
	public static RelatedDocuments getFallback(DBConnection con) throws Exception{
		RelatedDocuments rdg = new RelatedDocumentsMLT(con);
		rdg.loggingInfo.replace("recommendation_class", "fallback");
		rdg.loggingInfo.replace("name", "fallback");
		return rdg;
	}

	public static RelatedDocuments getRandomRDG(DBConnection con, DisplayDocument requestDocument) {
		Random random = new Random();
		Probabilities probs = new Probabilities();
		int cumulative = probs.getRandomDocumentRecommender();
		int randomRecommendationApproach = random.nextInt(10000);
		try{
			if(randomRecommendationApproach < cumulative)	//CASE: Completely random
				rdg = new RandomDocumentRecommender(con);
			else{
				cumulative += probs.getRandomDocumentRecommenderLanguageRestricted(); //CASE: Random with lang restriction
				if (randomRecommendationApproach < cumulative)
					rdg = new RandomDocumentRecommenderLanguageRestricted(con);
				else{
					cumulative += probs.getStereotypeRecommender();	//CASE: Stereotype
					if(randomRecommendationApproach < cumulative)
						rdg = new StereotypeRecommender(con);
					else{
						cumulative += probs.getRelatedDocumentsFromSolr();	//CASE: metadata based from SOLR
						if(randomRecommendationApproach < cumulative)
							rdg = new RelatedDocumentsMLT(con);
						else{
							String language = requestDocument.getLanguage();		//Validity of keyphrase algo depends on language. So check language
							if(language == null || !language.equals("en"))
								rdg = getFallback(con);			//If not english, use fallback.
							else{
								//Check presence and language of abstract
								rdg = new RelatedDocumentsKeyphrases(con);
								String abstLang = con.getAbstractDetails(requestDocument);
								if(!abstLang.equals("en"))
									//if not set loggingInfo.type to title only
									rdg.loggingInfo.replace("cbf_text_fields", "title");
								//otherwise leave it unset. 
							}
						}
					}
				}
			}
		}catch (Exception e){
			if(rdg!=null) System.out.println(rdg.getClass().getName() + " has failed to initialize");
			throw new UnknownException(e, true);
		}
		return rdg;
	}
}				
		
		
