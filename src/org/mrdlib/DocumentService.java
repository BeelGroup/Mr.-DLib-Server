package org.mrdlib;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mrdlib.database.DBConnection;
import org.mrdlib.database.NoEntryException;
import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.DocumentSet;
import org.mrdlib.display.RootElement;
import org.mrdlib.display.StatusMessage;
import org.mrdlib.display.StatusReport;
import org.mrdlib.display.StatusReportSet;
import org.mrdlib.solrHandler.NoRelatedDocumentsException;
import org.mrdlib.solrHandler.solrConnection;

/**
 * @author Millah
 * 
 * This class is called by Tomcat and the start of the webapp
 */

//set Path and allow numbers, letters and -_.,  Save Path as document_id
@Path("{documentId : [a-zA-Z0-9-_.,]+}")
public class DocumentService {

	//set up the necessary connections and load the config
	//DocumentExamples documentExample = new DocumentExamples();
	
	private DBConnection con = null;
	private solrConnection scon = null;
	private Constants constants = null;
	private RootElement rootElement = null;
	private StatusReportSet statusReportSet = null;
	
	public DocumentService() {
		constants = new Constants();
		rootElement = new RootElement();
		statusReportSet = new StatusReportSet();
		try {
			con = new DBConnection("tomcat");
			scon = new solrConnection(con);
		} catch (Exception e) {
			if(constants.getDebugModeOn()) {
				e.printStackTrace();
				statusReportSet.addStatusReport(new UnknownException("Message:" +e.getMessage() +"\n StackTrace: " +e.getStackTrace()).getStatusReport());
			} else {
				e.printStackTrace();
				statusReportSet.addStatusReport(new UnknownException().getStatusReport());
			}
		}
	}

	@GET
	//set end of Path
	@Path("/related_documents")
	@Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
	/**
	 * Get the related documentSet of a given document
	 * 
	 * @param documentIdOriginal - id from the cooperation partner
	 * @return a document set of related documents
	 */
	public RootElement getRelatedDocumentSet(@PathParam("documentId") String documentIdOriginal) {
		DocumentSet documentset = null;
		DisplayDocument document = null;
		
		try {
			//get the requested document from the databas
			document = con.getDocumentBy(constants.getIdOriginal(),documentIdOriginal);
			//get all related documents from solr
			documentset = scon.getRelatedDocumentSetByDocument(document);
			
			//if there is no such document in the database
		} catch (NoEntryException e) {
			statusReportSet.addStatusReport(e.getStatusReport());
			
			//if solr didn't found related articles
		} catch (NoRelatedDocumentsException e) {
			statusReportSet.addStatusReport(e.getStatusReport());
			
			//if there happened something else
		} catch (Exception e){
			if(constants.getDebugModeOn()) {
				e.printStackTrace();
				statusReportSet.addStatusReport(new UnknownException("Message:" +e.getMessage() +"\n StackTrace: " +e.getStackTrace()).getStatusReport());
			} else {
				e.printStackTrace();
				statusReportSet.addStatusReport(new UnknownException().getStatusReport());
			}
		}
		//if everything went ok
		if(statusReportSet.getSize() == 0)
			statusReportSet.addStatusReport(new StatusReport(200, new StatusMessage("ok", "en")));
	    
		//add both the status message and the related document to the xml
		rootElement.setStatusReportSet(statusReportSet);
		rootElement.setDocumentSet(documentset);
		try {
			con.close();
			scon.close();
		} catch (Exception e) {
			if(constants.getDebugModeOn()) {
				e.printStackTrace();
				statusReportSet.addStatusReport(new UnknownException("Message:" +e.getMessage() +"\n StackTrace: " +e.getStackTrace()).getStatusReport());
			} else {
				e.printStackTrace();
				statusReportSet.addStatusReport(new UnknownException().getStatusReport());
			}
		}
		return rootElement;
	}
}
