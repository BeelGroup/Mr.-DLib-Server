package org.mrdlib.database;

import javax.ws.rs.WebApplicationException;

import org.mrdlib.api.response.StatusMessage;
import org.mrdlib.api.response.StatusReport;

public class NoEntryException extends WebApplicationException {
	private static final long serialVersionUID = 1L;
	/**
	 * throws an custom exception if the id of the entity we are looking for is not available in database
	 * @param the id which was searched for
	 */
	
	private String id;
	private String entity;
	public NoEntryException(String id) {
		this.id = id;
		this.entity="Partner document";
	}
	
	public NoEntryException(String id, String entity){
		this.id = id;
		this.entity=entity;
	}
	
	public String getId() {
		return id;
	}
	
	public StatusReport getStatusReport() {
		return new StatusReport(404, new StatusMessage(entity + " id: "+ id +" not found", "en"));
	}
}
