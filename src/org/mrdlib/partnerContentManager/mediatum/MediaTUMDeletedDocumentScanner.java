package org.mrdlib.partnerContentManager.mediatum;

import java.util.ArrayList;

import org.mrdlib.database.DBConnection;

public class MediaTUMDeletedDocumentScanner {

	public static void main(String[] args) {
		
		DBConnection dbConnection;
		
		try {
			dbConnection = new DBConnection("jar");
					
			System.out.println("--- start of retrieving ids from MDL's database ---");
			
			ArrayList<Long> mdlIds = dbConnection.getMediaTUMIdsInDatabase();
		
			System.out.println(mdlIds.size() + " data sets of mediaTUM found");
			
			System.out.println("--- retrieving ids from MDL's database done ---");
			
			dbConnection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
