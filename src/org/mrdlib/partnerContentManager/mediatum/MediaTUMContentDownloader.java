package org.mrdlib.partnerContentManager.mediatum;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.mrdlib.partnerContentManager.IContentDownloader;

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
		
		int year = since.getYear();
		int month = since.getMonth();
		int day = since.getDate();
		
		String oaiDate = year + "-";
		if (month < 10) {
			oaiDate += "0";
		}
		oaiDate += month;
		if (day < 10) {
			oaiDate += "0";
		}
		oaiDate += day;
		
		oaiHarvester.harvestFrom(baseUrl, metadataFormat, oaiDate, folderToStoreContentIn);
	}

}
