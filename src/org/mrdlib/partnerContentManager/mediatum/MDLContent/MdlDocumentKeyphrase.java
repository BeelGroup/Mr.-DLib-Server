package org.mrdlib.partnerContentManager.mediatum.MDLContent;

public class MdlDocumentKeyphrase {

	long doc_id;
	String term;
	float score;
	int gramity;
	MdlDocumentKeyphraseSource source;
	
	public MdlDocumentKeyphrase() {
		super();
	}
	
	public MdlDocumentKeyphrase(long doc_id, String term, float score, int gramity, MdlDocumentKeyphraseSource source) {
		super();
		this.doc_id = doc_id;
		this.term = term;
		this.score = score;
		this.gramity = gramity;
		this.source = source;
	}
	
	public long getDoc_id() {
		return doc_id;
	}
	
	public void setDoc_id(long doc_id) {
		this.doc_id = doc_id;
	}
	
	public String getTerm() {
		return term;
	}
	
	public void setTerm(String term) {
		this.term = term;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
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
	
}
