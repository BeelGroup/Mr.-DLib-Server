package org.mrdlib.partnerContentManager.mediatum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

/**
 * Implementation of ContentDownloader for partner mediaTUM.
 * 
 * @author wuestehube
 *
 */
public class MediaTUMContentDownloader implements IContentDownloader {
	
	// choices for importing content of mediaTUM have been fixed
	private final String baseUrl = "https://mediatum.ub.tum.de/oai/oai";
	private final String metadataFormat = "oai_dc";

	@Override
	public void downloadAllContent(String folderToStoreContentIn) {
		// not implemented for mediaTUM
	}

	@Override
	public void downloadContentSince(String folderToStoreContentIn, Date since) {
		// not implemented for mediaTUM
	}
	
	/**
	 * Returns true if the given file is a publication, otherwise false.
	 * The file is no publication, if its XML indicates that it is no record (id too high).
	 * Specific to mediaTUM.
	 * 
	 * @param file file to check
	 * @return true if the id for retrieving the file has been valid, otherwise false
	 */
	private static boolean isIdThatRetrievedFileNotTooHigh(File file) {
        boolean fileIsAPublication = true;

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.contains("<error code=\"idDoesNotExist\">")) {
                fileIsAPublication = false;
            }
        }

        return fileIsAPublication;
    }

	/**
	 * Returns true if the file contains usable XML, otherwise false.
	 * This is the case for protected records or records without a XML representation.
	 * Specific to mediaTUM.
	 * 
	 * @param file file to check usefulness of
	 * @return true if useful, otherwise false
	 */
    public static boolean isFileUseful(File file) {
        boolean fileIsUseful = true;

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.contains("<error code=\"noPermission\"")) {
                fileIsUseful = false;
            }
            if (line.contains("<recordHasNoXMLRepresentation/>")) {
                fileIsUseful = false;
            }
            if (line.contains("<error code=\"idDoesNotExist\">")) {
                fileIsUseful = false;
            }
        }

        return fileIsUseful;
    }
	
	/**
	 * Downloads all publications with a higher id than the given one. Removes records that are not useful.
	 * Stops at latest used id. Specific to mediaTUM.
	 * 
	 * @param pathOfFolderToDownloadContentTo path of folder to download content to
	 * @param idToDownloadContentWithHigherId id to download content with higher id
	 */
	public void downloadContentWithIdHigherThan(String pathOfFolderToDownloadContentTo, int idToDownloadContentWithHigherId) {
        int nodeId = idToDownloadContentWithHigherId + 1;
        boolean idIsValid = true;

        System.out.println("--- Download of new records starts ---");
        
        while (idIsValid) {
            System.out.println(nodeId);

            String url = baseUrl + "?verb=GetRecord&identifier=oai:mediatum.ub.tum.de:node/" + nodeId +
                    "&metadataPrefix=" + metadataFormat;

            String filePath = pathOfFolderToDownloadContentTo + "/" + nodeId + ".xml";
            
            File file = new File(filePath);
            try {
                FileUtils.copyURLToFile(new URL(url), file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            nodeId++;
            idIsValid = isIdThatRetrievedFileNotTooHigh(file);

            if (!isFileUseful(file)) {
                file.delete();
            }
        }
        
        System.out.println("--- Download of new records finished ---");
	}
	
}
