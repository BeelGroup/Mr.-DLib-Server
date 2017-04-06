package org.mrdlib.partnerContentManager.mediatum.MDLContent;

import java.util.Date;

public class MdlPerson {

	long person_id;
	String name_first;
	String name_middle;
	String name_last;
	String name_unstructured;
	Date added;
	MdlPersonDataQuality data_quality;
	
	public MdlPerson() {
		super();
	}
	
	public MdlPerson(long person_id, String name_first, String name_middle, String name_last, String name_unstructured,
			Date added, MdlPersonDataQuality data_quality) {
		super();
		this.person_id = person_id;
		this.name_first = name_first;
		this.name_middle = name_middle;
		this.name_last = name_last;
		this.name_unstructured = name_unstructured;
		this.added = added;
		this.data_quality = data_quality;
	}
	
	public long getPerson_id() {
		return person_id;
	}
	
	public void setPerson_id(long person_id) {
		this.person_id = person_id;
	}
	
	public String getName_first() {
		return name_first;
	}
	
	public void setName_first(String name_first) {
		this.name_first = name_first;
	}
	
	public String getName_middle() {
		return name_middle;
	}
	
	public void setName_middle(String name_middle) {
		this.name_middle = name_middle;
	}
	
	public String getName_last() {
		return name_last;
	}
	
	public void setName_last(String name_last) {
		this.name_last = name_last;
	}
	
	public String getName_unstructured() {
		return name_unstructured;
	}
	
	public void setName_unstructured(String name_unstructured) {
		this.name_unstructured = name_unstructured;
	}
	
	public Date getAdded() {
		return added;
	}
	
	public void setAdded(Date added) {
		this.added = added;
	}
	
	public MdlPersonDataQuality getData_quality() {
		return data_quality;
	}
	
	public void setData_quality(MdlPersonDataQuality data_quality) {
		this.data_quality = data_quality;
	}
	
}
