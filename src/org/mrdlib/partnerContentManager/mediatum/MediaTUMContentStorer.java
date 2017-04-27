package org.mrdlib.partnerContentManager.mediatum;

import java.util.List;

import org.mrdlib.database.DBConnection;
import org.mrdlib.partnerContentManager.IContentStorer;

/**
 * Implementation of ContentStorer for partner mediaTUM.
 * 
 * @author wuestehube
 *
 */
public class MediaTUMContentStorer implements IContentStorer<OAIDCRecordConverted> {

	@Override
	public void store(List<OAIDCRecordConverted> storableContent) {
		// establish database connection
		DBConnection connection = null;
		
		try {
			// TODO: establish database connection to MDL_mediatum database
			// connection = new DBConnection("jar");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (connection == null) {
			return;
		}
		
		// iterate over data to store
		for (OAIDCRecordConverted oaidcRecordConverted : storableContent) {
			// TODO: store data
			
		}
	}

}
