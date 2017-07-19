package main.java.org.mrdlib.partnerContentManager.mediatum;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.math.NumberUtils;
import org.mrdlib.database.DBConnection;

/**
 * Partner content manager for mediaTUM.
 * Offers a runnable main function that retrieves content from mediaTUM and stores it in MDL's database.
 * 
 * @author wuestehube
 *
 */
public class MediaTUMPartnerContentManager {
	
	/**
	 * Runnable main function that retrieves content from mediaTUM and stores it in MDL's database.
	 * 
	 * @param args 1) path of folder to store the partner's content, 2) id_original (without prefix) of the latest
	 * mediaTUM record inserted into the database, if new records should be downloaded
	 * before importing, -1 otherwise
	 * @throws IOException thrown if saving intermediate data on the hard drive fails
	 */
	public static void main(String[] args) throws IOException {
		// retrieve arguments
		int numArguments = args.length;
		
		// check if the correct number of arguments has been passed to the program
        if (numArguments != 2) {
            System.out.print("Error: Incorrect arguments passed to program. You need to pass: " +
                    "1) path of folder to store the partner's content, 2) id_original (without prefix) of the latest "
                    + "mediaTUM record inserted into the database, if new records should be downloaded, otherwise '-1'");

            // end program
            System.exit(1);
        }
        
        // check path to folder
        String contentFolderPath = args[0];
        File contentFolder = new File(contentFolderPath);
        if (!contentFolder.exists()) {
        	System.out.println("Error: Value of argument 1 is invalid. It must be: path of folder to "
        			+ "store the partner's content");
        	System.exit(1);
        }
        
        // check id to eventually download from
        int idToDownloadFrom = -1;
        if (args[1].equals("-1")) {
        	// do nothing
        } else if (NumberUtils.isNumber(args[1])) {
        	idToDownloadFrom = Integer.parseInt(args[1]);
        } else {
        	System.out.println("Error: Value of argument 2 is invalid. It must be: either id_original (without prefix) "
        			+ "of latest mediaTUM record or '-1'");
        	System.exit(1);
        }
        
        // eventually download data
        if (idToDownloadFrom != -1) {
        	MediaTUMContentDownloader mediaTUMContentDownloader = new MediaTUMContentDownloader();
        	
        	mediaTUMContentDownloader.downloadContentWithIdHigherThan(contentFolderPath, idToDownloadFrom);
        }
		
		// convert and store content of each downloaded file
		MediaTUMContentConverter mediaTUMContentConverter = new MediaTUMContentConverter();
		MediaTUMContentStorer mediaTUMContentStorer = new MediaTUMContentStorer();
		
		DBConnection dbConnection;
		
		try {
			dbConnection = new DBConnection("jar");
					
			for (File file : contentFolder.listFiles()) {
				String filePath = file.getAbsolutePath();
				
				// comment in for debugging
//				System.out.println(filePath);
				
				if (filePath.endsWith(".xml")) {
					MediaTUMXMLDocument xmlDocument = mediaTUMContentConverter.convertPartnerContentToStorablePartnerContent(filePath);
					
					if (xmlDocument != null) {
						
						if (!mediaTUMContentStorer.store(dbConnection, xmlDocument)) {
							System.out.println(filePath + ": storage failed");
						}
					} else {
						System.out.println(filePath + ": conversion failed");
					}
				}
			}
		
			dbConnection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
