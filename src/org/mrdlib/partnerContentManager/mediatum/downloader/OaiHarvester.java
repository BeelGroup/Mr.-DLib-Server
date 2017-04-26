package org.mrdlib.partnerContentManager.mediatum.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

/**
 * Offers the functionality to harvest an OAI-PMH interface.
 * For details regarding OAI-PMH see: http://www.openarchives.org/OAI/openarchivesprotocol.html.
 */
public class OaiHarvester {

    /**
     * Returns a given year and month in a formatted way.
     * @param year Year to format.
     * @param month Month to format.
     * @return Formatted date.
     */
    private static String formatDate(int year, int month) {
        return year + "-" + String.format("%02d", month);
    }

    /**
     * Returns a given day in a two-digit-format.
     * @param day Day to format.
     * @return Formatted day.
     */
    private static String formatDay(int day) {
        return String.format("%02d", day);
    }

    /**
     * Returns the number of days of a given month (format yyyy-mm).
     * @param month Month (format yyyy-mm) which number of days to return.
     * @return Number of days.
     */
    private static int getDaysOfMonth(String month) {
        String[] monthParts = month.split("-");

        int year = Integer.parseInt(monthParts[0]);
        int monthNo = Integer.parseInt(monthParts[1]);

        return YearMonth.of(year, monthNo).lengthOfMonth();
    }

    /**
     * Retrieves the earliestDatestamp from the identification of the OAI interface.
     * @param baseUrl Base URL of the OAI interface.
     * @return Earliest datestamp.
     */
    private static String getEarliestDatestamp(String baseUrl) {
        InputStream inputStream = WebsiteRetrievalService.getInputStreamFromUrl(baseUrl + "?verb=Identify");
        Document document = WebsiteRetrievalService.getDocumentFromInputStream(inputStream);

        return document.getElementsByTagName("earliestDatestamp").item(0).getTextContent();
    }

    /**
     * Harvests all data provided in a given meta data format of a given month (format yyyy-mm).
     * @param baseUrl Base URL of the OAI interface.
     * @param metadataFormat Metadata format to harvest.
     * @param month Month (format yyyy-mm) which to harvest.
     * @param outputDirectoryPath Path of directory to write harvested data to.
     */
    private static void harvestMonth(String baseUrl, String metadataFormat, String month, String outputDirectoryPath) {
        // construct OAI query string
        String url = baseUrl + "?verb=ListRecords&metadataPrefix=" + metadataFormat + "&from=" + month + "-01&until=" +
                month + "-" + formatDay(getDaysOfMonth(month));

        // for keeping track
        ConsoleOutputService.printOutStatus("[" + new Date() + "] query: " + url);

        // create file
        String fileName = metadataFormat + "_" + month + ".xml";
        File file = new File(outputDirectoryPath + "/" + fileName);

        try {
            // actual collection of data
            FileUtils.copyURLToFile(new URL(url), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Harvests all data provided in a given meta data format from a given date (format yyyy-mm-dd) until today.
     * @param baseUrl Base URL of the OAI interface.
     * @param metadataFormat Metadata format to harvest.
     * @param from Date (format yyyy-mm) from which until today to harvest.
     * @param outputDirectoryPath Path of directory to write harvested data to.
     */
    private static void harvestFrom(String baseUrl, String metadataFormat, String from, String outputDirectoryPath) {
        // get current year and month
        Calendar currentDate = Calendar.getInstance();
        int currentYear = currentDate.get(Calendar.YEAR);
        // enumeration of month starts at 0
        int currentMonth = currentDate.get(Calendar.MONTH) + 1;

        // get year and month from which until today to harvest
        String[] fromParts = from.split("-");

        int fromYear = Integer.parseInt(fromParts[0]);
        int fromMonth = Integer.parseInt(fromParts[1]);

        // harvest start year
        if (fromYear == currentYear) {
            for (int month = fromMonth; month <= currentMonth; month++) {
                harvestMonth(baseUrl, metadataFormat, formatDate(fromYear, month), outputDirectoryPath);
            }
        } else {
            for (int month = fromMonth; month <= 12; month++) {
                harvestMonth(baseUrl, metadataFormat, formatDate(fromYear, month), outputDirectoryPath);
            }
        }

        // harvest full years
        for (int year = fromYear + 1; year < currentYear; year++) {
            for (int month = 1; month <= 12; month++) {
                harvestMonth(baseUrl, metadataFormat, formatDate(year, month), outputDirectoryPath);
            }
        }

        // harvest current year
        for (int month = 1; month <= currentMonth; month++) {
            harvestMonth(baseUrl, metadataFormat, formatDate(currentYear, month), outputDirectoryPath);
        }
    }

    /**
     * Harvests all data provided by the OAI interface in the given metadata format.
     * @param baseUrl Base URL of the OAI interface.
     * @param metadataFormat Metadata Format of data to harvest.
     * @param outputDirectoryPath Path of directory to write harvested data to.
     */
    public static void harvest(String baseUrl, String metadataFormat, String outputDirectoryPath) {
        String earliestDatestamp = getEarliestDatestamp(baseUrl);

        harvestFrom(baseUrl, metadataFormat, earliestDatestamp, outputDirectoryPath);
    }

}