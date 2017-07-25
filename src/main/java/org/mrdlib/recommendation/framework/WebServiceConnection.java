package org.mrdlib.recommendation.framework;

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
    private Function<String, List<WebServiceRecommendation>> parser;

    public WebServiceConnection (String searchPattern, String documentPattern) {
	this.searchPattern = searchPattern;
	this.documentPattern = documentPattern;
	this.http = HttpClients.createDefault();
	this.genson = new Genson();
    }

    public WebServiceConnection (String searchPattern, String documentPattern, Function<String, List<WebServiceRecommendation>> parser) {
	this.searchPattern = searchPattern;
	this.documentPattern = documentPattern;
	this.parser = parser;
	this.genson = new Genson();
	this.http = HttpClients.createDefault();
    }


    private List<WebServiceRecommendation> sendDocumentQuery(String docId) {
	String url = String.format(documentPattern, docId);
	return sendQuery(url);

    }

    private List<WebServiceRecommendation> sendSearchQuery(String query) {
	String url = String.format(searchPattern, docId);
	return sendQuery(url);
    }

    private List<WebServiceRecommendation> sendQuery(String url) {
	HttpGet req = new HttpGet(url);
	HttpResponse res = http.execute(req);
	int code = res.getStatusLine().getStatusCode();

	if (code != 200)
	    throw new HttpException("Error while making request: HTTP Status " + code + ", caused by request " + post.toString());

	HttpEntity entity = res.getEntity();
	String body = entity.getContent();
	List<WebServiceRecommendation> results;
	if (parser == null)
	    results = Arrays.toList(genson.deserialize(body, WebServiceRecommendation[].class));
	else
	    results = parser.apply(body);
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
	    List<WebServiceRecommendation> docs = sendDocumentQuery(document.getDocumentId());

	    // no related documents found
	    if (docs.isEmpty()) {
		throw new NoRelatedDocumentsException(document.getOriginalDocumentId(), document.getDocumentId());
	    } else {
		relatedDocuments.setSuggested_label("Related Articles");
		relatedDocuments.setNumberOfReturnedResults(docs.getNumFound());
		for (int i = 0; i < docs.size() && relatedDocuments.getSize() < limit; i++) {
		    relDocument = con.getDocumentBy(constants.getDocumentId(), docs.get(i).getId());

		    if (!allowedCollections.contains(relDocument.getCollectionId().toString())) {
			System.out.println("Recommendation not in accessable connections.");
			continue;
		    }

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
	    }
	} catch (Exception e) {
	    throw e;
	}

	return relatedDocuments;
    }

}
