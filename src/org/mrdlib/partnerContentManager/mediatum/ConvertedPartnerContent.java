package org.mrdlib.partnerContentManager.mediatum;

import java.util.List;

import org.mrdlib.partnerContentManager.mediatum.MDLContent.MdlDocument;
import org.mrdlib.partnerContentManager.mediatum.MDLContent.MdlDocumentAbstract;
import org.mrdlib.partnerContentManager.mediatum.MDLContent.MdlDocumentExternalId;
import org.mrdlib.partnerContentManager.mediatum.MDLContent.MdlDocumentKeyphrase;
import org.mrdlib.partnerContentManager.mediatum.MDLContent.MdlDocumentKeyphraseCount;
import org.mrdlib.partnerContentManager.mediatum.MDLContent.MdlDocumentPerson;
import org.mrdlib.partnerContentManager.mediatum.MDLContent.MdlDocumentTitleSearches;
import org.mrdlib.partnerContentManager.mediatum.MDLContent.MdlDocumentTranslatedField;
import org.mrdlib.partnerContentManager.mediatum.MDLContent.MdlPerson;

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
	
}
