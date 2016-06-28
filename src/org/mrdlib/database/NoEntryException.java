package org.mrdlib.database;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NoEntryException extends WebApplicationException {
	private static final long serialVersionUID = 1L;

	public NoEntryException(String id) {
        super(Response.status(Response.Status.UNAUTHORIZED)
            .entity("No DB Entry: "+ id).type(MediaType.TEXT_PLAIN).build());
    }
}
