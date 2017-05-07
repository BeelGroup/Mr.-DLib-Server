package org.mrdlib.partnerContentManager;

public class MdlDocumentKeyphrase {

	// "a reference to the document to which this count of keyphrase statistic belongs to"
	long doc_id;
	
	// "The extracted keyphrase"
	String term;
	
	// "The score of the extracted keyphrase as per the DISTILLER tool that we used to extract the keyphrases"
	float score;
	
	// "How many continuous words are in this keyphrase? 1= unigram, 2 = trigram, 3 = trigram"
	int gramity;
	
	// "The fields from the document using which these keyphrases were generated"
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

	@Override
	public String toString() {
		return "MdlDocumentKeyphrase [doc_id=" + doc_id + ", term=" + term + ", score=" + score + ", gramity=" + gramity
				+ ", source=" + source + "]";
	}
	
}
