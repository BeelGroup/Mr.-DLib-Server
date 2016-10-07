package org.mrdlib.recommendation;

import java.util.Random;

import org.mrdlib.database.DBConnection;

public class RecommenderFactory {
	static RelatedDocumentGenerator rdg;
	
	public static RelatedDocumentGenerator  getRandomRDG(DBConnection con) throws Exception{
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
					rdg = new RelatedDocumentsFromSolr(con);
				else{
					cumulative += probs.getRelatedDocumentsFromSolrWithKeyphrases();
					if(randomRecommendationApproach < cumulative)
						rdg = new RelatedDocumentsFromSolrWithKeyphrases(con);
					else rdg = new StereotypeRecommender(con);
				}
			}
		}
		//ADD lines of code to log the random recommender we have picked in this instance
		//return new RandomDocumentRecommenderLanguageRestricted(con);
		return rdg;
	}
	
	public static RelatedDocumentGenerator getFallback(DBConnection con) throws Exception{
		return new RelatedDocumentsFromSolr(con);
	}
}
