package org.mrdlib.display;

/**
 * 
 * @author Millah
 *
 * This class creates hardcoded Examples, that are used to test functionality without needing a database or solr connection
 */

public class DocumentExamples {
	public DocumentSet getDocumentSet() {
		DocumentSet documentSet = new DocumentSet();
		// Hardcode Example document
		DisplayDocument document1 = new DisplayDocument("5524543354", "exmp-thepowerofscience-4423232", "thepowerofscience", 5, 
				"The Power of Science","A. Einstein and J Doe","Journal of Science 4:42", 1960,
				"https://api.mr-dlib.org/related_articles/5524543354/", "http://partner.com/library/documents/thepowerofscience/", "");
		documentSet.addDocument(document1);
		documentSet.setRecommendationSetId("665445334");
		documentSet.setSuggested_label("Related Articles");
		return documentSet;
	}

}