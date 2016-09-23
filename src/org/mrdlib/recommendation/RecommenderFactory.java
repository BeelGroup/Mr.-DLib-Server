package org.mrdlib.recommendation;

public class RecommenderFactory {
	RelatedDocumentGenerator rdg;
	
	public static RelatedDocumentGenerator  getRandomRDG() throws Exception{
		return new RelatedDocumentsFromSolr();
	}
}
