package org.mrdlib.solrHandler;

import javax.ws.rs.WebApplicationException;

import org.mrdlib.display.StatusMessage;
import org.mrdlib.display.StatusReport;


public class NoRelatedDocumentsException extends WebApplicationException {
	private static final long serialVersionUID = 1L;
	/**
	 * throws an custom Exception if solr found no related documents for the given id
	 * @param id, the document for which related documents were asked
	 */
	private String id;
	private String originalId;

	public NoRelatedDocumentsException(String originalId, String id) {
		this.id = id;
		this.originalId = originalId;
	}
	
	public String getId() {
		return id;
	}
	
	public StatusReport getStatusReport() {
		return new StatusReport(204, new StatusMessage("No related documents found for document id: "+id+"  (partner id: "+originalId+")", "en"));
	}
}