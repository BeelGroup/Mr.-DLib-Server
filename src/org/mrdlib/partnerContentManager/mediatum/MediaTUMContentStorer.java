package org.mrdlib.partnerContentManager.mediatum;

import org.mrdlib.database.DBConnection;

/**
 * Implementation of ContentStorer for partner mediaTUM.
 * 
 * @author wuestehube
 *
 */
public class MediaTUMContentStorer implements IContentStorer<MediaTUMXMLDocument> {

	@Override
	public Boolean store(DBConnection dbConnection, MediaTUMXMLDocument storableContent) {

		// store the given document
		try {
			dbConnection.insertMediaTUMDocument(storableContent);
			
			// content stored successfully
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// storing content failed
		return false;
	}

}
