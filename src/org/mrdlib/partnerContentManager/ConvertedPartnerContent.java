package org.mrdlib.partnerContentManager;

import java.util.List;

/**
 * Abstract class resembling Mr. DLibs database. Classes for holding converted partner content must extend it.
 * Getters and setters may be overwritten to handle specialities of partner's content.
 * 
 * @author wuestehube
 *
 */
public abstract class ConvertedPartnerContent {

	private MdlDocument mdlDocument;
	private MdlDocumentAbstract mdlDocumentAbstract;
	private MdlDocumentExternalId mdlDocumentExternalId;
	private MdlDocumentKeyphrase mdlDocumentKeyphrase;
	private MdlDocumentKeyphraseCount mdlDocumentKeyphraseCount;
	private MdlDocumentPerson mdlDocumentPerson;
	private MdlDocumentTitleSearches mdlDocumentTitleSearches;
	private MdlDocumentTranslatedField mdlDocumentTranslatedField;
	private List<MdlPerson> mdlPerson;
	
	public ConvertedPartnerContent() {
		super();
	}

	public ConvertedPartnerContent(MdlDocument mdlDocument, MdlDocumentAbstract mdlDocumentAbstract,
			MdlDocumentExternalId mdlDocumentExternalId, MdlDocumentKeyphrase mdlDocumentKeyphrase,
			MdlDocumentKeyphraseCount mdlDocumentKeyphraseCount, MdlDocumentPerson mdlDocumentPerson,
			MdlDocumentTitleSearches mdlDocumentTitleSearches, MdlDocumentTranslatedField mdlDocumentTranslatedField,
			List<MdlPerson> mdlPerson) {
		super();
		this.mdlDocument = mdlDocument;
		this.mdlDocumentAbstract = mdlDocumentAbstract;
		this.mdlDocumentExternalId = mdlDocumentExternalId;
		this.mdlDocumentKeyphrase = mdlDocumentKeyphrase;
		this.mdlDocumentKeyphraseCount = mdlDocumentKeyphraseCount;
		this.mdlDocumentPerson = mdlDocumentPerson;
		this.mdlDocumentTitleSearches = mdlDocumentTitleSearches;
		this.mdlDocumentTranslatedField = mdlDocumentTranslatedField;
		this.mdlPerson = mdlPerson;
	}

	public MdlDocument getMdlDocument() {
		return mdlDocument;
	}

	public void setMdlDocument(MdlDocument mdlDocument) {
		this.mdlDocument = mdlDocument;
	}

	public MdlDocumentAbstract getMdlDocumentAbstract() {
		return mdlDocumentAbstract;
	}

	public void setMdlDocumentAbstract(MdlDocumentAbstract mdlDocumentAbstract) {
		this.mdlDocumentAbstract = mdlDocumentAbstract;
	}

	public MdlDocumentExternalId getMdlDocumentExternalId() {
		return mdlDocumentExternalId;
	}

	public void setMdlDocumentExternalId(MdlDocumentExternalId mdlDocumentExternalId) {
		this.mdlDocumentExternalId = mdlDocumentExternalId;
	}

	public MdlDocumentKeyphrase getMdlDocumentKeyphrase() {
		return mdlDocumentKeyphrase;
	}

	public void setMdlDocumentKeyphrase(MdlDocumentKeyphrase mdlDocumentKeyphrase) {
		this.mdlDocumentKeyphrase = mdlDocumentKeyphrase;
	}

	public MdlDocumentKeyphraseCount getMdlDocumentKeyphraseCount() {
		return mdlDocumentKeyphraseCount;
	}

	public void setMdlDocumentKeyphraseCount(MdlDocumentKeyphraseCount mdlDocumentKeyphraseCount) {
		this.mdlDocumentKeyphraseCount = mdlDocumentKeyphraseCount;
	}

	public MdlDocumentPerson getMdlDocumentPerson() {
		return mdlDocumentPerson;
	}

	public void setMdlDocumentPerson(MdlDocumentPerson mdlDocumentPerson) {
		this.mdlDocumentPerson = mdlDocumentPerson;
	}

	public MdlDocumentTitleSearches getMdlDocumentTitleSearches() {
		return mdlDocumentTitleSearches;
	}

	public void setMdlDocumentTitleSearches(MdlDocumentTitleSearches mdlDocumentTitleSearches) {
		this.mdlDocumentTitleSearches = mdlDocumentTitleSearches;
	}

	public MdlDocumentTranslatedField getMdlDocumentTranslatedField() {
		return mdlDocumentTranslatedField;
	}

	public void setMdlDocumentTranslatedField(MdlDocumentTranslatedField mdlDocumentTranslatedField) {
		this.mdlDocumentTranslatedField = mdlDocumentTranslatedField;
	}

	public List<MdlPerson> getMdlPerson() {
		return mdlPerson;
	}

	public void setMdlPerson(List<MdlPerson> mdlPerson) {
		this.mdlPerson = mdlPerson;
	}

	@Override
	public String toString() {
		return "ConvertedPartnerContent [mdlDocument=" + mdlDocument + ", mdlDocumentAbstract=" + mdlDocumentAbstract
				+ ", mdlDocumentExternalId=" + mdlDocumentExternalId + ", mdlDocumentKeyphrase=" + mdlDocumentKeyphrase
				+ ", mdlDocumentKeyphraseCount=" + mdlDocumentKeyphraseCount + ", mdlDocumentPerson="
				+ mdlDocumentPerson + ", mdlDocumentTitleSearches=" + mdlDocumentTitleSearches
				+ ", mdlDocumentTranslatedField=" + mdlDocumentTranslatedField + ", mdlPerson=" + mdlPerson + "]";
	}
	
	/**
	 * Checks whether a field of the converted partner content is null. Returns false if so.
	 * 
	 * @return true if the partner content may be stored into the database, false otherwise
	 */
	public boolean isContentValid() {
		return (mdlDocument != null) && (mdlDocumentAbstract != null) && (mdlDocumentExternalId != null) &&
				(mdlDocumentKeyphrase != null) && (mdlDocumentPerson != null) && (mdlDocumentTitleSearches != null) &&
				(mdlDocumentTranslatedField != null) && (mdlPerson != null);
	}
	
}
