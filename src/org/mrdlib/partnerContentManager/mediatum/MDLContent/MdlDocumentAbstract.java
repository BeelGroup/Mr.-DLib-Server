package org.mrdlib.partnerContentManager.mediatum.MDLContent;

import java.util.Date;

public class MdlDocumentAbstract {

	long document_abstract_id;
	long document_id;
	String language;
	String abstract_;	// _ introduced to prevent collision with Java keyword
	Date added;
	
	public MdlDocumentAbstract() {
		super();
	}
	
	public MdlDocumentAbstract(long document_abstract_id, long document_id, String language, String abstract_,
			Date added) {
		super();
		this.document_abstract_id = document_abstract_id;
		this.document_id = document_id;
		this.language = language;
		this.abstract_ = abstract_;
		this.added = added;
	}
	
	public long getDocument_abstract_id() {
		return document_abstract_id;
	}
	
	public void setDocument_abstract_id(long document_abstract_id) {
		this.document_abstract_id = document_abstract_id;
	}
	
	public long getDocument_id() {
		return document_id;
	}
	
	public void setDocument_id(long document_id) {
		this.document_id = document_id;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getAbstract_() {
		return abstract_;
	}
	
	public void setAbstract_(String abstract_) {
		this.abstract_ = abstract_;
	}
	
	public Date getAdded() {
		return added;
	}
	
	public void setAdded(Date added) {
		this.added = added;
	}
	
}
