package org.mrdlib.partnerContentManager.mediatum.MDLContent;

public class MdlDocumentKeyphraseCount {

	long doc_id;
	int gramity;
	MdlDocumentKeyphraseSource source;
	long count;
	
	public MdlDocumentKeyphraseCount() {
		super();
	}
	
	public MdlDocumentKeyphraseCount(long doc_id, int gramity, MdlDocumentKeyphraseSource source, long count) {
		super();
		this.doc_id = doc_id;
		this.gramity = gramity;
		this.source = source;
		this.count = count;
	}
	
	public long getDoc_id() {
		return doc_id;
	}
	
	public void setDoc_id(long doc_id) {
		this.doc_id = doc_id;
	}
	
	public int getGramity() {
		return gramity;
	}
	
	public void setGramity(int gramity) {
		this.gramity = gramity;
	}
	
	public MdlDocumentKeyphraseSource getSource() {
		return source;
	}
	
	public void setSource(MdlDocumentKeyphraseSource source) {
		this.source = source;
	}
	
	public long getCount() {
		return count;
	}
	
	public void setCount(long count) {
		this.count = count;
	}
	
}
