package org.mrdlib.solrHandler;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NoRelatedDocumentsException extends WebApplicationException {
	private static final long serialVersionUID = 1L;
	/**
	 * throws an custom Exception if solr found no related documents for the given id
	 * @param id, the document for which related documents were asked
	 */
    public NoRelatedDocumentsException(String id) {
        super(Response.status(Response.Status.UNAUTHORIZED)
            .entity("No Related Documents found for "+ id).type(MediaType.TEXT_PLAIN).build());
    }
}
