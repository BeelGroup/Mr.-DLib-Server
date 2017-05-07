package org.mrdlib.partnerContentManager;

import java.util.Date;

public class MdlDocument {

	// "Our Mr. DLib ID of the document"
	long document_id;
	
	// "Original ID of the document given by our partner"
	String id_original;
	
	// "Reference to the collection to which this document belongs"
	long collection_id;
	
	// "Title of the document"
	String title;
	
	// "Clean title, i.e. only ASCII characters (no spaces, all lower case); if length of clean title is smaller than half the original title, use original title"
	String title_clean;
	
	// "Name of the journal, conference, etc. where the article was published"
	String published_in;
	
	// "ISO 639-1 language (2 letters)"
	String language;
	
	// "Year in which the document was published"
	int publication_year;
	
	// "The type of the document"
	MdlDocumentType type;
	
	// "keywords and categories"
	String keywords;
	
	// "date when the document was added to our database"
	Date added;
	
	public MdlDocument() {
		super();
	}
	
	public MdlDocument(long document_id, String id_original, long collection_id, String title, String title_clean,
			String published_in, String language, int publication_year, MdlDocumentType type, String keywords,
			Date added) {
		super();
		this.document_id = document_id;
		this.id_original = id_original;
		this.collection_id = collection_id;
		this.title = title;
		this.title_clean = title_clean;
		this.published_in = published_in;
		this.language = language;
		this.publication_year = publication_year;
		this.type = type;
		this.keywords = keywords;
		this.added = added;
	}
	
	public long getDocument_id() {
		return document_id;
	}
	
	public void setDocument_id(long document_id) {
		this.document_id = document_id;
	}
	
	public String getId_original() {
		return id_original;
	}
	
	public void setId_original(String id_original) {
		this.id_original = id_original;
	}
	
	public long getCollection_id() {
		return collection_id;
	}
	
	public void setCollection_id(long collection_id) {
		this.collection_id = collection_id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle_clean() {
		return title_clean;
	}
	
	public void setTitle_clean(String title_clean) {
		this.title_clean = title_clean;
	}
	
	public String getPublished_in() {
		return published_in;
	}
	
	public void setPublished_in(String published_in) {
		this.published_in = published_in;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public int getPublication_year() {
		return publication_year;
	}
	
	public void setPublication_year(int publication_year) {
		this.publication_year = publication_year;
	}
	
	public MdlDocumentType getType() {
		return type;
	}
	
	public void setType(MdlDocumentType type) {
		this.type = type;
	}
	
	public String getKeywords() {
		return keywords;
	}
	
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	
	public Date getAdded() {
		return added;
	}
	
	public void setAdded(Date added) {
		this.added = added;
	}

	@Override
	public String toString() {
		return "MdlDocument [document_id=" + document_id + ", id_original=" + id_original + ", collection_id="
				+ collection_id + ", title=" + title + ", title_clean=" + title_clean + ", published_in=" + published_in
				+ ", language=" + language + ", publication_year=" + publication_year + ", type=" + type + ", keywords="
				+ keywords + ", added=" + added + "]";
	}
	
}
