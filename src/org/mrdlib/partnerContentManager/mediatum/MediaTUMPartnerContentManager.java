package org.mrdlib.partnerContentManager.mediatum;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.mrdlib.database.DBConnection;
import org.mrdlib.partnerContentManager.gesis.XMLDocument;

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
	 * @param args 1) path of folder to download the content of the partner to
	 * @throws IOException thrown if saving intermediate data on the hard drive fails
	 */
	public static void main(String[] args) throws IOException {
		// retrieve arguments
		int numArguments = args.length;
		
		// check if the correct number of arguments has been passed to the program
        if (numArguments != 1) {
            System.out.print("Error: Incorrect arguments passed to program. You need to pass: " +
                    "1) path of folder to download the content of the partner to,");

            // end program
            System.exit(1);
        }
        
        String contentFolderPath = args[0];
        File contentFolder = new File(contentFolderPath);
        if (!contentFolder.exists()) {
        	System.out.println("Error: Value of argument 1 is invalid. It must be: path of folder to "
        			+ "download the content of the partner to");
        	System.exit(1);
        }
        
        // TODO: add logic for downloading content from mediaTUM
		
		// convert and store content of each downloaded file
		MediaTUMContentConverter mediaTUMContentConverter = new MediaTUMContentConverter();
		MediaTUMContentStorer mediaTUMContentStorer = new MediaTUMContentStorer();
		
		DBConnection dbConnection;
		
		try {
			dbConnection = new DBConnection("jar");
					
			for (File file : contentFolder.listFiles()) {
				String filePath = file.getAbsolutePath();
				
				System.out.print(filePath);
				
				if (filePath.endsWith(".xml")) {
					MediaTUMXMLDocument xmlDocument = mediaTUMContentConverter.convertPartnerContentToStorablePartnerContent(filePath);
					
					if (xmlDocument != null) {
						
						if (!mediaTUMContentStorer.store(dbConnection, xmlDocument)) {
							System.out.println(" - storage failed");
						}
					} else {
						System.out.print(" - conversion failed");
					}
				}
				
				System.out.print("\n");
			}
		
			dbConnection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
