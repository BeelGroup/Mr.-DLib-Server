package org.mrdlib.partnerContentManager.mediatum.MDLContent;

import java.util.Date;

public class MdlDocumentPerson {

	long document_person_id;
	long document_id;
	long person_id;
	int rank;
	Date added;
	
	public MdlDocumentPerson() {
		super();
	}
	
	public MdlDocumentPerson(long document_person_id, long document_id, long person_id, int rank, Date added) {
		super();
		this.document_person_id = document_person_id;
		this.document_id = document_id;
		this.person_id = person_id;
		this.rank = rank;
		this.added = added;
	}
	
	public long getDocument_person_id() {
		return document_person_id;
	}
	
	public void setDocument_person_id(long document_person_id) {
		this.document_person_id = document_person_id;
	}
	
	public long getDocument_id() {
		return document_id;
	}
	
	public void setDocument_id(long document_id) {
		this.document_id = document_id;
	}
	
	public long getPerson_id() {
		return person_id;
	}
	
	public void setPerson_id(long person_id) {
		this.person_id = person_id;
	}
	
	public int getRank() {
		return rank;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	public Date getAdded() {
		return added;
	}
	
	public void setAdded(Date added) {
		this.added = added;
	}
	
}
