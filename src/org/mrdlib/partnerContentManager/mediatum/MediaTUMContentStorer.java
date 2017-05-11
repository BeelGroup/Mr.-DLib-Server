package org.mrdlib.partnerContentManager.mediatum;

import java.util.List;

import org.mrdlib.database.DBConnection;
import org.mrdlib.partnerContentManager.IContentStorer;
import org.mrdlib.partnerContentManager.gesis.XMLDocument;

/**
 * Implementation of ContentStorer for partner mediaTUM.
 * 
 * @author wuestehube
 *
 */
public class MediaTUMContentStorer implements IContentStorer<OAIDCRecordConverted> {

	@Override
	public void store(DBConnection dbConnection, OAIDCRecordConverted storableContent) {
		
		System.out.println("LET'S STORE!");
		
//		XMLDocument xmlDocument = new XMLDocument(typeMap, languageMap, typeResolveMap);
//		
//		xmlDocument.
//		
//		dbConnection.insertDocument(xmlDocument);
		
	}

}
