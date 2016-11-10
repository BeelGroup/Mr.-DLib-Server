package org.mrdlib.api.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
/**
 * 
 * @author Millah
 * 
 * This class handles the representation of all status reports which occur
 * The XML format is automatically generated through the class structure.
 *
 */
public class StatusReportSet {
	
	private List<StatusReport> statusReportList = new ArrayList<StatusReport>();
	
	public StatusReportSet() {}
	
	public int getSize() {
		return statusReportList.size();
	}

	public void addStatusReport(StatusReport statusReport) {
		statusReportList.add(statusReport);
	}	
	
	public List<StatusReport> getStatusReportList() {
		return statusReportList;
	}
	
	@XmlElement(name = "status_report")
	public void setStatusReportList(List<StatusReport> statusReportList) {
		this.statusReportList = statusReportList;
	}
}