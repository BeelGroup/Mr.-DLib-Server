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

	DocumentExamples documentExample = new DocumentExamples();
	private DBConnection con = new DBConnection();
	private solrConnection scon = new solrConnection();
	private Constants constants = new Constants();

	@GET
	//set end of Path
	@Path("/related_documents")
	@Produces(MediaType.APPLICATION_XML)
	public DocumentSet getDocumentSet(@PathParam("documentId") String documentIdOriginal) throws Exception {
		DocumentSet documentset = scon.getRelatedDocumentSetByDocument(con.getDocumentBy(constants.getIdOriginal(),documentIdOriginal));
		//DocumentSet documentset = con.getDocumentSetByOriginalId(documentId);
		//documentset.addDocument(con.getDocumentByOriginalId(documentId));
		//return documentExample.getDocumentSet();
		return documentset;
	}
}
