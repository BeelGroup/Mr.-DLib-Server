package org.mrdlib.partnerContentManager.mediatum;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.mrdlib.database.DBConnection;

/**
 * Partner content manager for mediaTUM.
 * Offers a runnable main function that retrieves content from mediaTUM and stores it in MDL's database.
 * 
 * @author wuestehube
 *
 */
public class MediaTUMPartnerContentManager {
	
	private static void testDatabaseConnection() {
		// TODO: alter DB mediaTUM, perform read of that alteration => see if everything works
		DBConnection con;
		
		try {
			con = new DBConnection("jar");
			
			System.out.println("database connection successfully established");
			
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void executeActualLogic(String[] args) {
		// retrieve arguments
		int numArguments = args.length;
		
		// check if the correct number of arguments has been passed to the program
        if (numArguments != 2) {
            System.out.print("Error: Incorrect arguments passed to program. You need to pass: " +
                    "1) path of folder to download the content of the partner to, 2) 'update' if "
                    + "new data should be downloaded from mediaTUM or 'populate' if all data should "
                    + "be downloaded.");

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
        
        String partnerContentManagerTask = args[1];
        if (!(partnerContentManagerTask.equals("update") || partnerContentManagerTask.equals("populate"))) {
        	System.out.println("Error: Value of argument 2 is invalid. It must be: 'update' if "
                    + "new data should be downloaded from mediaTUM or 'populate' if all data should "
                    + "be downloaded.");
        	System.exit(1);
        }
        
        // TODO: remove output for testing
//        System.out.println(contentFolderPath);
        
        // "/home/admin/scripts/scripts/mediaTUM"
        
//        testFileSystem(contentFolderPath);
        
		// download content to given folder
        MediaTUMContentDownloader mediaTUMContentDownloader = new MediaTUMContentDownloader();
        
        // TODO: change second argument to a date stamp if data should be downloaded
        // TODO: call methods of downloader accordingly
        
//		mediaTUMContentDownloader.downloadAllContent(contentFolderPath);
		
		// convert and store content of each downloaded file
		MediaTUMContentConverter mediaTUMContentConverter = new MediaTUMContentConverter();
//		MediaTUMContentStorer mediaTUMContentStorer = new MediaTUMContentStorer();
		
		// TODO: store content of files
		for (File file : contentFolder.listFiles()) {
			String filePath = file.getAbsolutePath();
			
			if (filePath.endsWith(".xml")) {
				System.out.println(filePath);
				
				OAIDCRecordConverted oaidcRecordConverted = mediaTUMContentConverter.convertPartnerContentToStorablePartnerContent(filePath);
			
				System.out.println(oaidcRecordConverted.toString());
				if (oaidcRecordConverted.isContentValid()) {
					System.out.println("conversion completed successfully");
				} else {
					// TODO: handle failed conversions
				}
			}
		}
	}
	
	/**
	 * Runnable main function that retrieves content from mediaTUM and stores it in MDL's database.
	 * 
	 * @param args 1) path of folder to download the content of the partner to
	 * @throws IOException thrown if saving intermediate data on the hard drive fails
	 */
	public static void main(String[] args) throws IOException {
//		testDatabaseConnection();
		
		executeActualLogic(args);
	}
}
