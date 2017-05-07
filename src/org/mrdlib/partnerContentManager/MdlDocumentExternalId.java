package org.mrdlib.partnerContentManager;

public class MdlDocumentExternalId {

	// "MDL document ID"
	long document_id;
	
	// "Name of the external organization the id is from"
	MdlDocumentExternalIdExternalName external_name;
	
	// "external id"
	String external_id;
	
	public MdlDocumentExternalId() {
		super();
	}
	
	public MdlDocumentExternalId(long document_id, MdlDocumentExternalIdExternalName external_name, String external_id) {
		super();
		this.document_id = document_id;
		this.external_name = external_name;
		this.external_id = external_id;
	}
	
	public long getDocument_id() {
		return document_id;
	}
	
	public void setDocument_id(long document_id) {
		this.document_id = document_id;
	}
	
	public MdlDocumentExternalIdExternalName getExternal_name() {
		return external_name;
	}
	
	public void setExternal_name(MdlDocumentExternalIdExternalName external_name) {
		this.external_name = external_name;
	}
	
	public String getExternal_id() {
		return external_id;
	}
	
	public void setExternal_id(String external_id) {
		this.external_id = external_id;
	}

	@Override
	public String toString() {
		return "MdlDocumentExternalId [document_id=" + document_id + ", external_name=" + external_name
				+ ", external_id=" + external_id + "]";
	}
	
}
