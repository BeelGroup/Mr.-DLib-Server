package org.mrdlib;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mrdlib.database.DBConnection;
import org.mrdlib.database.NoEntryException;

//set Path and allow numbers, letters and -_.,  Save Path as document_id
@Path("{documentId : [a-zA-Z0-9-_.,]+}")
public class DocumentService {

	DocumentExamples documentExample = new DocumentExamples();
	private DBConnection con = new DBConnection();

	@GET
	//set end of Path
	@Path("/related_documents")
	@Produces(MediaType.APPLICATION_XML)
	public DocumentSet getDocumentSet(@PathParam("documentId") String documentId) throws Exception {
		DocumentSet documentset = con.getDocumentSetByOriginalId(documentId);
		if(documentset.getSize() == 0)
			throw new NoEntryException();
		else
			return documentset;
		//return documentExample.getDocumentSet();
	}
}
