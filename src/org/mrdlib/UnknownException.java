package org.mrdlib;

import javax.ws.rs.WebApplicationException;

import org.mrdlib.display.StatusMessage;
import org.mrdlib.display.StatusReport;

public class UnknownException extends WebApplicationException {
	private static final long serialVersionUID = 1L;
	private String message = "";

    public UnknownException(String message) {
    	this.message = message;
    }
    
    public UnknownException() {}
    
	public String getMessage() {
		return message;
	}
    
	public StatusReport getStatusReport() {
		if(message.isEmpty())
			return new StatusReport(500, new StatusMessage("Unknown Error", "en"));
		else
			return new StatusReport(500, new StatusMessage("Unknown Error", "en"), message);
	}
}