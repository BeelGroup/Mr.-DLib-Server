package org.mrdlib.recommendation;

import java.util.Random;

public class RecommenderFactory {
	static RelatedDocumentGenerator rdg;
	
	public static RelatedDocumentGenerator  getRandomRDG() throws Exception{
		Random random = new Random();
		Probabilities probs = new Probabilities();
		int cumulative = probs.getRandomDocumentRecommender();
		int randomRecommendationApproach = random.nextInt(10000);
		if(randomRecommendationApproach < cumulative)
			rdg = new RandomDocumentRecommender();
		else{
			cumulative += probs.getRandomDocumentRecommenderLanguageRestricted();
			if (randomRecommendationApproach < cumulative)
				rdg = new RandomDocumentRecommenderLanguageRestricted();
			else{
				cumulative += probs.getRelatedDocumentsFromSolr();
				if (randomRecommendationApproach < cumulative)
					rdg = new RelatedDocumentsFromSolr();
				else{
					cumulative += probs.getRelatedDocumentsFromSolrWithKeyphrases();
					if(randomRecommendationApproach < cumulative)
						rdg = new RelatedDocumentsFromSolrWithKeyphrases();
					else rdg = new StereotypeRecommender();
				}
			}
		}
		//ADD lines of code to log the random recommender we have picked in this instance
		return rdg;
	}
}
