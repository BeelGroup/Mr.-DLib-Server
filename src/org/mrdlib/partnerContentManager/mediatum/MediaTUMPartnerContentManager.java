package org.mrdlib.partnerContentManager.mediatum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
                    "1) path of folder to download the content of the partner to.");

            // end program
            System.exit(1);
        }
        
        String contentFolderPath = args[0];
        
		// download content to given folder
        MediaTUMContentDownloader mediaTUMContentDownloader = new MediaTUMContentDownloader();
		mediaTUMContentDownloader.downloadAllContent(contentFolderPath);
		
		// convert and store content of each downloaded file
		MediaTUMContentConverter mediaTUMContentConverter = new MediaTUMContentConverter();
		MediaTUMContentStorer mediaTUMContentStorer = new MediaTUMContentStorer();
		try {
			Files.walk(Paths.get(contentFolderPath))
					.filter((p) -> !p.toFile().isDirectory() && p.toFile().getAbsolutePath().endsWith(".xml"))
					.forEach(p -> mediaTUMContentStorer.store(mediaTUMContentConverter.convertPartnerContentToStorablePartnerContent(p.toFile().getAbsolutePath())));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
