package org.mrdlib.partnerContentManager.mediatum;

import java.util.Calendar;
import java.util.Date;

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
		OaiHarvester oaiHarvester = new OaiHarvester();
		
		oaiHarvester.harvest(baseUrl, metadataFormat, folderToStoreContentIn);
	}

	@Override
	public void downloadContentSince(String folderToStoreContentIn, Date since) {
		OaiHarvester oaiHarvester = new OaiHarvester();
		
		oaiHarvester.harvestFrom(baseUrl, metadataFormat, convertDateToOAIDate(since), folderToStoreContentIn);
		
		// TODO: in case of mediaTUM data that has been deleted from the database of mediaTUM needs
		// to be deleted from Mr. DLib's database as well.
	}
	
	/**
	 * Converts a given date (format Jave.util.Date) to a string (format "yyyy").
	 * 
	 * @param date date to convert (format Jave.util.Date)
	 * @return converted date as string (format "yyyy")
	 */
	private String convertDateToOAIDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		String oaiDate = year + "-";
		if (month < 10) {
			oaiDate += "0";
		}
		oaiDate += month;
		if (day < 10) {
			oaiDate += "0";
		}
		oaiDate += day;
		
		return oaiDate;
	}

}
