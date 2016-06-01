package org.mrdlib;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

//set Path and allow numbers, letters and -_,. . Save Path as document_id
@Path("{document_id : [a-zA-Z0-9-_.,]+}")
public class DocumentService {

	DocumentExamples documentExample = new DocumentExamples();

	@GET
	//set end of Path
	@Path("/related_documents")
	@Produces(MediaType.APPLICATION_XML)
	public List<Related_article> getDocuments() {
		return documentExample.getAllDocuments();
	}
}
