package org.mrdlib.partnerContentManager.mediatum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mrdlib.database.DBConnection;
import org.mrdlib.partnerContentManager.IContentStorer;
import org.mrdlib.partnerContentManager.gesis.Tuple;
import org.mrdlib.partnerContentManager.gesis.XMLDocument;

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
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
		
	}

}
