package org.mrdlib.partnerContentManager.mediatum;

import java.util.List;

import org.mrdlib.partnerContentManager.ConvertedPartnerContent;
import org.mrdlib.partnerContentManager.MdlDocument;
import org.mrdlib.partnerContentManager.MdlDocumentAbstract;
import org.mrdlib.partnerContentManager.MdlDocumentExternalId;
import org.mrdlib.partnerContentManager.MdlDocumentKeyphrase;
import org.mrdlib.partnerContentManager.MdlDocumentKeyphraseCount;
import org.mrdlib.partnerContentManager.MdlDocumentPerson;
import org.mrdlib.partnerContentManager.MdlDocumentTitleSearches;
import org.mrdlib.partnerContentManager.MdlDocumentTranslatedField;
import org.mrdlib.partnerContentManager.MdlPerson;

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
