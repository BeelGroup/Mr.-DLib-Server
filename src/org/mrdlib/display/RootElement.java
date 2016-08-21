package org.mrdlib.display;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="mr-dlib")
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
