package org.mrdlib.partnerContentManager.mediatum;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.mrdlib.database.DBConnection;

/**
 * The mediaTUM deleted document scanner retrieves all saved node ids from Mr. DLib's database.
 * Subsequently, it tries to retrieve the publications with these ids from mediaTUM.
 * Thus it identifies nodes that have been removed from mediaTUM's repository.
 *
 */
public class MediaTUMDeletedDocumentScanner {
	
	private final static String baseUrl = "https://mediatum.ub.tum.de/oai/oai";
	private final static String metadataFormat = "oai_dc";

	public static void main(String[] args) {
		
		ArrayList<Long> deletedMediaTumIds = new ArrayList<>();
		
		// get program arguments
		if (args.length != 1) {
			System.out.println("Error: 1 argument needs to be passed to the program: 1) path of folder to store downloaded content temporarily in.");
		}
		
		String pathOfFolderToDownloadContentTo = args[0];
		
		DBConnection dbConnection;
		MediaTUMContentDownloader mediaTUMContentDownloader = new MediaTUMContentDownloader();
		
		try {
			dbConnection = new DBConnection("jar");
					
			System.out.println("--- start of retrieving ids from MDL's database ---");
			ArrayList<Long> mdlIds = dbConnection.getMediaTUMIdsInDatabase();
			System.out.println(mdlIds.size() + " data sets of mediaTUM found");
			System.out.println("--- retrieving ids from MDL's database done ---");
			
			System.out.println("--- start of checking validity of ids ---");
            String filePath = pathOfFolderToDownloadContentTo + "/temp.xml";
            File file = new File(filePath);
            
            int i = 0;
            
			for (Long id : mdlIds) {
				String url = baseUrl + "?verb=GetRecord&identifier=oai:mediatum.ub.tum.de:node/" + id +
	                    "&metadataPrefix=" + metadataFormat;
				FileUtils.copyURLToFile(new URL(url), file);
				if (!mediaTUMContentDownloader.isFileUseful(file)) {
					System.out.println("NODE DELETED: " + id);
					deletedMediaTumIds.add(id);
				}
				
				// print out progress
				i++;
				if ((i%100) == 0) {
					System.out.println(i + " out of " + mdlIds.size() + "nodes have been checked");
				}
				
				// sleep to prevent overload of mediaTUM's system
				TimeUnit.MILLISECONDS.sleep(500);
			}
			System.out.println("--- checking validity of ids done ---");
			
			// print out removed nodes
			System.out.println("--- removed nodes: ---");
			
			for (Long id : mdlIds) {
				System.out.println(id);
			}
			
			dbConnection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
