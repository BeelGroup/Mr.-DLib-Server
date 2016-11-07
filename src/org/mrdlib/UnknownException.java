package org.mrdlib;

import javax.ws.rs.WebApplicationException;

import org.mrdlib.display.StatusMessage;
import org.mrdlib.display.StatusReport;

/**
 * throws an custom exception if an unknown error occured
 * 
 * @param the
 *            error message
 */
public class UnknownException extends WebApplicationException {
	private static final long serialVersionUID = 1L;
	private String message = "";

	public UnknownException(String message) {
		this.message = message;
	}

	public UnknownException(Exception e, Boolean debugMode) {
		String stackTrace = "";
		//catch the information from the exception and adding them as message to the custom exception if the debug mode is on
		if (debugMode) {
			stackTrace = "Error: " + e.toString() + "  |  ";
			for (StackTraceElement current : e.getStackTrace())
				stackTrace = stackTrace + "Line Number " + current.getLineNumber() + ": " + current.getMethodName()
						+ " (" + current.getClassName() + ")";
		}
		this.message = this.message + " ---------------" + stackTrace;
	}
	
	public UnknownException() {
	}

	public String getMessage() {
		return message;
	}

	public StatusReport getStatusReport() {
		if (message.isEmpty())
			return new StatusReport(500, new StatusMessage("Unknown Error", "en"));
		else
			return new StatusReport(500, new StatusMessage("Unknown Error", "en"), message);
	}
}