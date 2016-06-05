package org.mrdlib.database;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NoEntryException extends WebApplicationException {
    public NoEntryException() {
        super(Response.status(Response.Status.UNAUTHORIZED)
            .entity("No DB Entry").type(MediaType.TEXT_PLAIN).build());
    }
}
