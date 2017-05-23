package org.mrdlib.partnerContentManager.mediatum;

import java.util.Date;

/**
 * Interface specifying methods for downloading content of MDL partners.
 * Method definitions reflect the current design decision of downloading provided content to possibly XML files.
 * 
 * In implementing classes, introduce fields for necessary parameters of content downloading and populate them in a constructor.
 * 
 * The ContentDownloader is the first of three parts of the Download-Convert-Store mechanism used for persisting partner's content.
 * 
 * @author wuestehube
 *
 */
public interface IContentDownloader {
	
	/**
	 * Downloads all available content from the content provider to the specified folder.
	 * Possibly one record may be stored per file.
	 * 
	 * @param pathOfFolderToDownloadContentTo path of folder to download content to
	 */
	public void downloadAllContent(String pathOfFolderToDownloadContentTo);
	
	/**
	 * Downloads all content that has been added by the content provider since a given date to the specified folder.
	 * This may be useful for regularly fetching content. 
	 * 
	 * @param pathOfFolderToDownloadContentTo path of folder to download content to
	 * @param since date since which to download content
	 */
	public void downloadContentSince(String pathOfFolderToDownloadContentTo, Date since);
	
}
