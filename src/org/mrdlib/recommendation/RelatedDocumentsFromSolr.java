package org.mrdlib.recommendation;

import org.mrdlib.database.DBConnection;
import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.DocumentSet;
import org.mrdlib.solrHandler.solrConnection;

public abstract class RelatedDocumentsFromSolr implements RelatedDocumentGenerator {
	
	
	public RelatedDocumentsFromSolr( ) throws Exception{
		DBConnection con = null;
		solrConnection scon = null;
		
		try {
			con = new DBConnection("tomcat");
			scon = new solrConnection(con);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}
	
	@Override
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs) {
		// TODO Auto-generated method stub
		return null;
	}

}
