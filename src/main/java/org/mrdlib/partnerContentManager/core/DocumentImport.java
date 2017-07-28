package org.mrdlib.partnerContentManager.core;

import org.mrdlib.database.DBConnection;
import org.mrdlib.database.NoEntryException;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.manager.Constants;

public class DocumentImport
{
    private DBConnection db;
    private Constants constants;
    private CoreApi api;
    public static final long BATCH_SIZE = 1000;

    public DocumentImport () {
	try {
	    this.constants = new Constants();
	    this.db = new DBConnection("jar");
	    this.api = new CoreApi();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public boolean hasDocumentInDB(Integer id) throws Exception {
	try {
	    String idOriginal = String.format("%s-%d", constants.getCore(), id);
	    DisplayDocument doc = db.getDocumentBy(constants.getIdOriginal(), idOriginal);
	    return true;
	} catch(NoEntryException e) {
	    return false;
	}
    }

    public static void main(String args[])
    {
	
    }
}
