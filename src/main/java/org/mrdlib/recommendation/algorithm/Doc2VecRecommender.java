package org.mrdlib.recommendation.algorithm;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.database.DBConnection;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.recommendation.framework.WebServiceConnection;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

public class Doc2VecRecommender extends RelatedDocuments {

    private DBConnection con;
    private WebServiceConnection service;
    private Constants constants;
    private static final String language = "en";
    private static final int limit = 10;
    
    public Doc2VecRecommender(DBConnection con) {

        con = con;
	constants = new Constants();
        algorithmLoggingInfo = new AlgorithmDetails("Doc2VecRecommender", "most_popular", true);
	String searchPattern, documentPattern;
	searchPattern = new URIBuilder()
	    .setHost(constants.getDoc2VecServiceHost())
	    .setPort(constants.getDoc2VecServicePort())
	    .setPath(constants.getDoc2VecSearchRoute())
	    .addParameter(new BasicNameValuePair("language", language))
	    .addParameter(new BasicNameValuePair("limit", limit))
	    .toString();
	documentPattern = new URIBuilder()
	    .setHost(constants.getDoc2VecServiceHost())
	    .setPort(constants.getDoc2VecServicePort())
	    .setPath(constants.getDoc2VecDocumentRoute())
	    .addParameter(new BasicNameValuePair("language", language))
	    .addParameter(new BasicNameValuePair("limit", limit))
	    .toString();
	service = new WebServiceConnection();
    }


    @Override
    /**
     * returns mostPopular documents from database
     */
    public DocumentSet getRelatedDocumentSet(DocumentSet requestDocSet) throws Exception {
	
    }

}
