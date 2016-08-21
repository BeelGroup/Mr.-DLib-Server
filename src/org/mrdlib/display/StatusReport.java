package org.mrdlib.display;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class StatusReport implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
    // elements
    private StatusMessage statusMessage;
    private String debugDetails;
    
    //attribute
    private int statusCode;

	public StatusReport() {}
	
    public StatusReport(int statusCode, StatusMessage statusMessage, String debugDetails) {
        this.statusMessage = statusMessage;
        this.debugDetails = "![CDATA["+debugDetails+"]]";
        this.statusCode = statusCode;
    }
    
    public StatusReport(int statusCode, StatusMessage statusMessage) {
        this.statusMessage = statusMessage;
        this.statusCode = statusCode;
    }
    
    public StatusReport(int statusCode, String statusMessage, String debugDetails) {
        this.statusMessage = new StatusMessage(statusMessage);
        this.debugDetails = "![CDATA["+debugDetails+"]";
        this.statusCode = statusCode;
    }
    
    public StatusReport(int statusCode, String statusMessage) {
        this.statusMessage = new StatusMessage(statusMessage);
        this.statusCode = statusCode;
    }
    
    
    public StatusMessage getStatusMessage() {
        return statusMessage;
    }

    @XmlElement(name = "status_message")
    public void setStatusMessage(StatusMessage statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getDebugDetails() {
        return debugDetails;
    }

    @XmlElement(name = "debug_details")
    public void setDebugDetails(String debugDetails) {
        this.debugDetails = "![CDATA["+debugDetails+"]]";
    }

    public int getStatusCode() {
        return statusCode;
    }

    @XmlAttribute(name = "code")
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}