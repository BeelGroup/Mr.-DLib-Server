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
 * 
 * @author wuestehube
 *
 */
public class OAIDCRecordConverted extends ConvertedPartnerContent {

	public OAIDCRecordConverted(MdlDocument mdlDocument, MdlDocumentAbstract mdlDocumentAbstract,
			MdlDocumentExternalId mdlDocumentExternalId, MdlDocumentKeyphrase mdlDocumentKeyphrase,
			MdlDocumentKeyphraseCount mdlDocumentKeyphraseCount, MdlDocumentPerson mdlDocumentPerson,
			MdlDocumentTitleSearches mdlDocumentTitleSearches, MdlDocumentTranslatedField mdlDocumentTranslatedField,
			List<MdlPerson> mdlPerson) {
		super(mdlDocument, mdlDocumentAbstract, mdlDocumentExternalId, mdlDocumentKeyphrase,
				mdlDocumentKeyphraseCount, mdlDocumentPerson, mdlDocumentTitleSearches, mdlDocumentTranslatedField,
				mdlPerson);
	}

	// TODO: overwrite getters and setters if necessary 
	
}
