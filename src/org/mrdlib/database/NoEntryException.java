package org.mrdlib.database;

import javax.ws.rs.WebApplicationException;

import org.mrdlib.api.response.StatusMessage;
import org.mrdlib.api.response.StatusReport;

public class NoEntryException extends WebApplicationException {
	private static final long serialVersionUID = 1L;
	/**
	 * throws an custom exception if the id of the document don't exist in the database
	 * @param the id which was searched for
	 */
	
	private String id;
	
	public NoEntryException(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public StatusReport getStatusReport() {
		return new StatusReport(404, new StatusMessage("Partner document id: "+id+" not found", "en"));
	}
}
