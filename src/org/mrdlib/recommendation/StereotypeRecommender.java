package org.mrdlib.recommendation;

import org.mrdlib.database.DBConnection;
import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.DocumentSet;

public class StereotypeRecommender implements RelatedDocumentGenerator {

	private DBConnection con = null;

	public StereotypeRecommender() throws Exception {
		try {
			con = new DBConnection("tomcat");
			loggingInfo.clear();
			loggingInfo.put("name", "StereotypeRecommender");
			loggingInfo.put("recommendation_framework", "proprietary");
			loggingInfo.put("recommendation_class", "stereotype");
			loggingInfo.put("language_filter", "N");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc) throws Exception {
		// TODO Auto-generated method stub
		return getRelatedDocumentSet(requestDoc, 10);
	}

	@Override
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs) throws Exception {
		// TODO Auto-generated method stub
		DocumentSet results = new DocumentSet();
		try{
			 results = con.getStereotypeRecommendations(requestDoc, numberOfRelatedDocs);
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			if(con!=null) con.close();
		}
		return results;
		

	}

}
