package org.mrdlib.partnerContentManager.mediatum;

import java.util.List;

import org.mrdlib.partnerContentManager.mediatum.MDLContent.Document;

/**
 * Implementation of ContentConverter for partner mediaTUM.
 * mediaTUM offers a standardized OAI interface exhibiting data in the OAI Dublin Core format (http://www.openarchives.org/OAI/openarchivesprotocol.html).
 * 
 * @author wuestehube
 *
 */
public class MediaTUMContentConverter implements IContentConverter<OAIDCRecordConverted> {

	@Override
	public List<OAIDCRecordConverted> convertPartnerContentToStorablePartnerContent(String pathOfFileToConvert) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Document MapMediaTumToDocumentTable() {
		// TODO
		return null;
	}
	
	private Document MapMediaTumToDocumentAbstractTable() {
		// TODO
		return null;
	}

	private Document MapMediaTumToDocumentExternalIdTable() {
		// TODO
		return null;
	}
	
	private Document MapMediaTumToDocumentKeyphraseTable() {
		// TODO
		return null;
	}
	
	private Document MapMediaTumToDocumentKeyphraseCountTable() {
		// TODO
		return null;
	}
	
	private Document MapMediaTumToDocumentPersonTable() {
		// TODO
		return null;
	}
	
	private Document MapMediaTumToDocumentTitleSearchesTable() {
		// TODO
		return null;
	}
	
	private Document MapMediaTumToDocumentTranslatedFieldTable() {
		// TODO
		return null;
	}
	
	private Document MapMediaTumToPersonTable() {
		// TODO
		return null;
	}
	
}
