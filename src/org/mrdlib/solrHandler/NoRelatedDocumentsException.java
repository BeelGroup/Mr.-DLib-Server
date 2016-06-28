package org.mrdlib.solrHandler;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NoRelatedDocumentsException extends WebApplicationException {
	private static final long serialVersionUID = 1L;
    public NoRelatedDocumentsException(String id) {
        super(Response.status(Response.Status.UNAUTHORIZED)
            .entity("No Related Documents found for "+ id).type(MediaType.TEXT_PLAIN).build());
    }
}
