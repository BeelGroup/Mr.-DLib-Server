package org.mrdlib;

public class DocumentExamples {
	public DocumentSet getDocumentSet() {
		DocumentSet documentSet = new DocumentSet();
		// Hardcode Example document
		Document document1 = new Document("5524543354", "exmp-thepowerofscience-4423232", "thepowerofscience", 5, new Snippet(
				"&lt;span class='title'&gt;The Power of Science&lt;/span&gt;. &lt;span class='authors'&gt;A. Einstein and J Doe&lt;/span&gt;. &lt;span class='journal'&gt;Journal of Science&lt;/span&gt;. &lt;span class='volume_and_number'&gt;4:42&lt;/span&gt;. &lt;span class='year'&gt;1960&lt;/span&gt;",
				"html_and_css"),
				"https://api.mr-dlib.org/related_articles/5524543354/", "http://partner.com/library/documents/thepowerofscience/");
		documentSet.addDocument(document1);
		documentSet.setRecommendationSetId("665445334");
		documentSet.setSuggested_label("Related Articles");
		return documentSet;
	}

}