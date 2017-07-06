package org.mrdlib.api.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
/**
 * 
 * @author Millah
 * 
 * This class handles the representation of the whole webapp
 * The XML format is automatically generated through the class structure.
 *
 */
@XmlRootElement(name="mr-dlib")
@XmlType(propOrder={""})
public class RootElement {

	private StatusReportSet statusReportSet;
	private DocumentSet documentSet;
	
	public RootElement(StatusReportSet statusReportSet, DocumentSet documentSet) {
		this.statusReportSet = statusReportSet;
		this.documentSet = documentSet;
	}
	public RootElement() {}
	
	public StatusReportSet getStatusReportSet() {
		return statusReportSet;
	}
	
	@XmlElement(name = "status_reports")
	public void setStatusReportSet(StatusReportSet statusReportSet) {
		this.statusReportSet = statusReportSet;
	}

	public DocumentSet getDocumentSet() {
		return documentSet;
	}
	
	@XmlElement(name = "related_articles")
	public void setDocumentSet(DocumentSet documentSet) {
		this.documentSet = documentSet;
	}
}
