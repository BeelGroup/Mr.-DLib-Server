package org.mrdlib.database;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NoEntryException extends WebApplicationException {
	private static final long serialVersionUID = 1L;

	/**
	 * throws an custom exception if the id of the document dont exist in the database
	 * 
	 * @param the id which was searched for
	 */
	public NoEntryException(String id) {
        super(Response.status(Response.Status.UNAUTHORIZED)
            .entity("No DB Entry: "+ id).type(MediaType.TEXT_PLAIN).build());
    }
}
