package org.mrdlib.api.response;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author Millah
 * 
 *         This class handles the representation of the statusReport, which
 *         contains a http status code and the report set The XML format is
 *         automatically generated through the class structure.
 *
 */
public class StatusReport implements Serializable {

	private static final long serialVersionUID = 1L;

	// elements
	private StatusMessage statusMessage;
	private String debugMessage;

	// attribute
	private int statusCode;

	public StatusReport() {
	}

	public StatusReport(int statusCode, StatusMessage statusMessage, String debugMessage) {
		this.statusMessage = statusMessage;
		this.debugMessage = "![CDATA[" + debugMessage + "]]";
		this.statusCode = statusCode;
	}

	public StatusReport(int statusCode, StatusMessage statusMessage) {
		this.statusMessage = statusMessage;
		this.statusCode = statusCode;
		if(statusCode!=200)	this.debugMessage = statusMessage.getContent();
	}

	public StatusReport(int statusCode, String statusMessage, String debugMessage) {
		this.statusMessage = new StatusMessage(statusMessage);
		this.debugMessage = "![CDATA[" + debugMessage + "]";
		this.statusCode = statusCode;
	}

	public StatusReport(int statusCode, String statusMessage) {
		this.statusMessage = new StatusMessage(statusMessage);
		this.statusCode = statusCode;
		if(statusCode!=200) this.debugMessage = "![CDATA[" + statusMessage + "]";
	}

	public StatusMessage getStatusMessage() {
		return statusMessage;
	}

	@XmlElement(name = "status_message")
	public void setStatusMessage(StatusMessage statusMessage) {
		this.statusMessage = statusMessage;
	}

	public String getDebugMessage() {
		return debugMessage;
	}

	@XmlElement(name = "debug_message")
	public void setDebugMessage(String debugMessage) {
		if (debugMessage == null)
			this.debugMessage = null;
		else
			this.debugMessage = "![CDATA[" + debugMessage + "]]";
	}

	public int getStatusCode() {
		return statusCode;
	}

	@XmlAttribute(name = "code")
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}