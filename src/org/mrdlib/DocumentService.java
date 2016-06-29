package org.mrdlib;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mrdlib.database.Constants;
import org.mrdlib.database.DBConnection;
import org.mrdlib.solrHandler.solrConnection;

//set Path and allow numbers, letters and -_.,  Save Path as document_id
@Path("{documentId : [a-zA-Z0-9-_.,]+}")
public class DocumentService {

	//set up the necessary connections and load the config
	//DocumentExamples documentExample = new DocumentExamples();
	private DBConnection con = new DBConnection();
	private solrConnection scon = new solrConnection();
	private Constants constants = new Constants();

	@GET
	//set end of Path
	@Path("/related_documents")
	@Produces(MediaType.APPLICATION_XML)
	/**
	 * Get the related documentSet of a given document
	 * 
	 * @param documentIdOriginal - id from the cooperation partner
	 * @return a document set of related documents
	 */
	public DocumentSet getDocumentSet(@PathParam("documentId") String documentIdOriginal) {
		DocumentSet documentset = null;
		try {
			documentset = scon.getRelatedDocumentSetByDocument(con.getDocumentBy(constants.getIdOriginal(),documentIdOriginal));
		} catch (Exception e){
			e.printStackTrace();
		}
		//DocumentSet documentset = con.getDocumentSetByOriginalId(documentId);
		//documentset.addDocument(con.getDocumentByOriginalId(documentId));
		//return documentExample.getDocumentSet();
		return documentset;
	}
}
