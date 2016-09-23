package org.mrdlib.recommendation;

import org.mrdlib.database.DBConnection;
import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.DocumentSet;
import org.mrdlib.solrHandler.NoRelatedDocumentsException;
import org.mrdlib.solrHandler.solrConnection;

public class RelatedDocumentsFromSolr implements RelatedDocumentGenerator {
	DBConnection con = null;
	solrConnection scon = null;
	
	public RelatedDocumentsFromSolr( ) throws Exception{
		
		
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
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc) throws Exception{
		return getRelatedDocumentSet(requestDoc,10);
	}

	@Override
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs) throws Exception {
		try {
			return scon.getRelatedDocumentSetByDocument(requestDoc,numberOfRelatedDocs);
		} catch(NoRelatedDocumentsException f){
			System.out.println("No related documents for doc_id " + requestDoc.getDocumentId());
			throw f;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
