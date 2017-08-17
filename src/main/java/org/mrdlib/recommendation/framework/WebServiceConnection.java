package org.mrdlib.recommendation.framework;

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.owlike.genson.Genson;

public class WebServiceConnection {


	private static final double BUFFER_FACTOR = 2.0;
	private static final int MIN_RELATED_DOCUMENTS = 3;

    private String searchPattern, documentPattern;
    private HttpClient http;
    private Genson json;
    private DBConnection con;
    private final String language = "en";
    private Constants constants;
    private Function<InputStream, List<WebServiceRecommendation>> parser;
	private Logger logger = LoggerFactory.getLogger(WebServiceConnection.class);

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


    private List<WebServiceRecommendation> sendDocumentQuery(String docId, String language, int limit, String... arguments) throws Exception {
		String url = String.format(documentPattern, docId, language, String.valueOf(limit), arguments);
		return sendQuery(url);

    }

    private List<WebServiceRecommendation> sendSearchQuery(String query, String language, int limit, String... arguments) throws Exception {
		String url = String.format(searchPattern, query, language, String.valueOf(limit), arguments);
		return sendQuery(url);
    }

    private List<WebServiceRecommendation> sendQuery(String url) throws Exception {
		HttpGet req = new HttpGet(url);
		HttpResponse res = http.execute(req);
		int code = res.getStatusLine().getStatusCode();

		if (code == 200) {
			HttpEntity entity = res.getEntity();
			List<WebServiceRecommendation> results;
			if (parser == null)
				results = Arrays.asList(json.deserialize(entity.getContent(), WebServiceRecommendation[].class));
			else
				results = parser.apply(entity.getContent());
			return results;
		} else if (code == 404) { // no data for this document
			// empty result list - throw NoRelatedDocumentsException later
			return new ArrayList<WebServiceRecommendation>(); 
		} else {
			throw new HttpException("Error while making request: HTTP Status " + code + ", caused by request " + req.toString());
		}

    }

    /**
     * query recommendations from a webservice
     */
    public DocumentSet getRelatedDocumentSetByDocument(DocumentSet relatedDocuments, String... arguments) throws Exception {

		DisplayDocument document = relatedDocuments.getRequestedDocument();
		AlgorithmDetails logginginfo = relatedDocuments.getAlgorithmDetails();
		int limit = relatedDocuments.getDesiredNumberFromAlgorithm();
		int limitBuffer = (int) (limit * BUFFER_FACTOR); // get more documents because they may not be in the right collection
		List<String> allowedCollections = con.getAccessableCollections(relatedDocuments.getRequestingPartnerId());

		DisplayDocument relDocument = new DisplayDocument();
		String fallback_url = "";

	    List<WebServiceRecommendation> docs = sendDocumentQuery(document.getDocumentId(), language, limitBuffer, arguments);
		logger.info("Requesting {} documents from web service; returned {}; need {}", limitBuffer, docs.size(), limit);

	    // no related documents found
	    if (docs.isEmpty()) {
			throw new NoRelatedDocumentsException(document.getOriginalDocumentId(), document.getDocumentId());
	    } else {
			relatedDocuments.setSuggested_label("Related Articles");
			for (int i = 0; i < docs.size() && relatedDocuments.getSize() < limit; i++) {
				relDocument = con.getDocumentBy(constants.getDocumentId(), docs.get(i).getId());

				if (!allowedCollections.contains(relDocument.getCollectionId().toString())) {
					continue; // skip this; we have a buffer of other documents 
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
			logger.info("Filtered returned documents; {} remaining", relatedDocuments.getSize());

			if (relatedDocuments.getSize() < MIN_RELATED_DOCUMENTS) {
				throw new NoRelatedDocumentsException(document.getDocumentId(), document.getOriginalDocumentId());
			}
			relatedDocuments.setNumberOfReturnedResults(relatedDocuments.getSize());
	    }
		return relatedDocuments;
    }

}
