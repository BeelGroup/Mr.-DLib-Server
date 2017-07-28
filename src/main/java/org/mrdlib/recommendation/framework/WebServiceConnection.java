package org.mrdlib.recommendation.framework;

import java.io.InputStream;
import java.util.List;
import java.util.Arrays;
import java.util.function.Function;

import org.mrdlib.api.manager.Constants;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.recommendation.algorithm.AlgorithmDetails;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.HttpException;
import org.apache.http.HttpEntity;

import com.owlike.genson.Genson;

public class WebServiceConnection {

    private String searchPattern, documentPattern;
    private HttpClient http;
    private Genson json;
    private DBConnection con;
    private final String language = "en";
    private Constants constants;
    private Function<InputStream, List<WebServiceRecommendation>> parser;

    public WebServiceConnection (String searchPattern, String documentPattern, DBConnection con) {
	this.searchPattern = searchPattern;
	this.documentPattern = documentPattern;
	this.http = HttpClients.createDefault();
	this.json = new Genson();
	this.con = con;
	this.constants = new Constants();
    }

    public WebServiceConnection (String searchPattern, String documentPattern, Function<InputStream, List<WebServiceRecommendation>> parser, DBConnection con) {
	this(searchPattern, documentPattern, con);
	this.parser = parser;
    }


    private List<WebServiceRecommendation> sendDocumentQuery(String docId, String language, int limit) throws Exception {
	System.out.println("formatting " + documentPattern);
	System.out.printf("args: %s %s %d %n", docId, language, limit);
	String url = String.format(documentPattern, docId, language, limit);
	return sendQuery(url);

    }

    private List<WebServiceRecommendation> sendSearchQuery(String query, String language, int limit) throws Exception {
	String url = String.format(searchPattern, query, language, limit);
	return sendQuery(url);
    }

    private List<WebServiceRecommendation> sendQuery(String url) throws Exception {
	HttpGet req = new HttpGet(url);
	HttpResponse res = http.execute(req);
	int code = res.getStatusLine().getStatusCode();

	if (code != 200)
	    throw new HttpException("Error while making request: HTTP Status " + code + ", caused by request " + req.toString());

	HttpEntity entity = res.getEntity();
	List<WebServiceRecommendation> results;
	if (parser == null)
	    results = Arrays.asList(json.deserialize(entity.getContent(), WebServiceRecommendation[].class));
	else
	    results = parser.apply(entity.getContent());
	return results;
    }

    /**
     * query recommendations from a webservice
     */
    public DocumentSet getRelatedDocumentSetByDocument(DocumentSet relatedDocuments) throws Exception {

	DisplayDocument document = relatedDocuments.getRequestedDocument();
	AlgorithmDetails logginginfo = relatedDocuments.getAlgorithmDetails();
	int limit = relatedDocuments.getDesiredNumberFromAlgorithm();
	List<String> allowedCollections = con.getAccessableCollections(relatedDocuments.getRequestingPartnerId());

	DisplayDocument relDocument = new DisplayDocument();
	String fallback_url = "";

	try {
	    // docs = document list;
	    List<WebServiceRecommendation> docs = sendDocumentQuery(document.getDocumentId(), language, limit);

	    // no related documents found
	    if (docs.isEmpty()) {
		throw new NoRelatedDocumentsException(document.getOriginalDocumentId(), document.getDocumentId());
	    } else {
		relatedDocuments.setSuggested_label("Related Articles");
		for (int i = 0; i < docs.size() && relatedDocuments.getSize() < limit; i++) {
		    System.out.println("Got recommendations:");
		    System.out.println(docs.get(i).getId() + " - " + docs.get(i).getSimilarity());
		    relDocument = con.getDocumentBy(constants.getDocumentId(), docs.get(i).getId());

		    // TODO handle this
		    // if (!allowedCollections.contains(relDocument.getCollectionId().toString())) {
		    // 	System.out.println("Recommendation not in accessable connections.");
		    // 	continue;
		    // }

		    relDocument.setSuggestedRank(i + 1);

		    relDocument.setRelevanceScoreFromAlgorithm(docs.get(i).getSimilarity());

		    // set gesis specific link
		    if (relDocument.getCollectionShortName().equals(constants.getGesis())) {
			if (constants.getEnvironment().equals("api"))
			    fallback_url = constants.getGesisCollectionLink()
				.concat(relDocument.getOriginalDocumentId());
			else
			    fallback_url = constants.getGesisBetaCollectionLink()
				.concat(relDocument.getOriginalDocumentId());
		    } else if (relDocument.getCollectionShortName().contains(constants.getCore()))
			fallback_url = constants.getCoreCollectionLink()
			    .concat(relDocument.getOriginalDocumentId().split("-")[1]);
		    // url = "http://api.mr-dlib.org/trial/recommendations/" +
		    // relDocument.getRecommendationId() +
		    // "/original_url?access_key=" +"hash"
		    // +"&format=direct_url_forward";

		    // relDocument.setClickUrl(url);
		    relDocument.setFallbackUrl(fallback_url);
		    // add it to the collection
		    relatedDocuments.addDocument(relDocument);
		}
		relatedDocuments.setNumberOfReturnedResults(docs.size());
	    }
	} catch (Exception e) {
	    throw e;
	}

	return relatedDocuments;
    }

}
