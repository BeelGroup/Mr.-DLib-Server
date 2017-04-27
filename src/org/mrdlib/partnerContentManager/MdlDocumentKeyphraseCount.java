package org.mrdlib.partnerContentManager;

public class MdlDocumentKeyphraseCount {

	// "a reference to the document to which this count of keyphrase statistic belongs to"
	long doc_id;
	
	// "How many continuous words are in this keyphrase? 1= unigram, 2 = trigram, 3 = trigram"
	int gramity;
	
	// "The fields from the document using which these keyphrases were generated"
	MdlDocumentKeyphraseSource source;
	
	// "the number of keyphrases of a particular type. ex: it could represent the number of unigram keyphrases generated from title only for the document 200"
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
