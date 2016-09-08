package org.mrdlib;

import javax.ws.rs.WebApplicationException;

import org.mrdlib.display.StatusMessage;
import org.mrdlib.display.StatusReport;
/**
 * throws an custom exception if an access key mismatch occurs
 * @param the error message
 */
public class InvalidAccessKeyException extends WebApplicationException {
	private static final long serialVersionUID = 1L;
	private String message = "";

    public InvalidAccessKeyException(String message) {
    	this.message = message;
    }
    
    public InvalidAccessKeyException() {}
    
	public String getMessage() {
		return message;
	}
    
	public StatusReport getStatusReport() {
		if(message.isEmpty())
			return new StatusReport(403, new StatusMessage("Access Denied because of invalid access key", "en"));
		else
			return new StatusReport(403, new StatusMessage("Access Denied because of invalid access key", "en"), message);
	}
}