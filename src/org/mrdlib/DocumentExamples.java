package org.mrdlib;
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
		Document document1 = new Document("5524543354", "exmp-thepowerofscience-4423232", "thepowerofscience", 5, new Snippet(
				"The Power of Science","A. Einstein and J Doe","Journal of Science 4:42", 1960, "html_and_css"),
				"https://api.mr-dlib.org/related_articles/5524543354/", "http://partner.com/library/documents/thepowerofscience/", "");
		documentSet.addDocument(document1);
		documentSet.setRecommendationSetId("665445334");
		documentSet.setSuggested_label("Related Articles");
		return documentSet;
	}

}