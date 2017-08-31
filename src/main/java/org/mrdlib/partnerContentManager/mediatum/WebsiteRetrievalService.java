package org.mrdlib.partnerContentManager.mediatum;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Provides helper functions for retrieving data from a website.
 */
public class WebsiteRetrievalService {

    /**
     * Returns the data retrieved from a given URL as an InputStream.
     * 
     * @param url URL to retrieve data from.
     * @return Retrieved data as InputStream.
     */
    public static InputStream getInputStreamFromUrl(String url) {
        URLConnection connection = null;
        try {
            connection = new URL(url).openConnection();
        } catch (IOException e) {
            ConsoleOutputService.printOutError("Error while connecting to URL " + url + ".", e);
        }

        InputStream inputStream = null;
        try {
            assert connection != null;
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            ConsoleOutputService.printOutError("Error while getting the InputStream of URL " + url + ".", e);
        }

        return inputStream;
    }

    /**
     * Transforms an InputStream to a Document.
     * 
     * @param inputStream InputStream to transform
     * @return document that the InputStream has been transformed into
     */
    public static Document getDocumentFromInputStream(InputStream inputStream) {
        DocumentBuilderFactory factory;
        DocumentBuilder builder = null;
        Document document = null;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            assert builder != null;
            document = builder.parse(new InputSource(inputStream));
        // catch both, SAXException and IOException
        } catch (Exception e) {
            ConsoleOutputService.printOutError("Error while getting the Document from InputStream.", e);
        }

        return document;
    }

}
