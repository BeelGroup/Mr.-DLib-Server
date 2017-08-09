package org.mrdlib.api.manager;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.api.response.RootElement;
import org.mrdlib.api.response.StatusMessage;
import org.mrdlib.api.response.StatusReport;
import org.mrdlib.api.response.StatusReportSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.database.NoEntryException;
import org.mrdlib.recommendation.algorithm.RecommenderFactory;
import org.mrdlib.recommendation.algorithm.Algorithm;
import org.mrdlib.recommendation.algorithm.RelatedDocuments;
import org.mrdlib.recommendation.framework.NoRelatedDocumentsException;
import org.mrdlib.recommendation.ranking.ApplyRanking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Millah
 * 
 *         This class is called by Tomcat and the start of the webapp
 */
// yxc get the name here
@Path("documents/{documentId}") 
// set Path and allow numbers, letters and -_., Save Path as document_id
public class DocumentService {

	// set up the necessary connections and load the config
	// DocumentExamples documentExample = new DocumentExamples();

	private Long requestRecieved;
	private DBConnection con = null;
	private Constants constants = null;
	private RootElement rootElement = null;
	private StatusReportSet statusReportSet = null;
	private ApplyRanking ar = null;
	private Logger logger = LoggerFactory.getLogger(DocumentService.class);

	public DocumentService() {
		// get the time the request came in for statistic
		// initialize the xml structure
		requestRecieved = System.currentTimeMillis();
		constants = new Constants();
		rootElement = new RootElement();
		statusReportSet = new StatusReportSet();
		try {
			con = new DBConnection("tomcat");
			ar = new ApplyRanking(con);
		} catch (Exception e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		}
	}

	


	/*
	 * Try to find the document in our database described by this query, dwim way.
	 * @param inputQuery document_id, id_original, title, ...
	 * @return the document, if it could be interpreted; else null -> treat inputQuery as search query 
	 * @throws NoEntryException if format could be detected, but no document was found
	 */
	private DisplayDocument getRequestedDocument(String inputQuery, String partnerId) throws Exception {
		DisplayDocument requestDocument = null;
		/*
		 * First we have a look if it is an integer. if the conversion
		 * fails, e.g. there are some letters in it, we go on and try to get
		 * an document by its original id. If that fails, we search for the
		 * title in our database. Later, we can add here the function to do
		 * a lucene mlt.
		 */
		try {
			// get the requested document from the database by mdl ID
			logger.trace("try int");
			Integer.parseInt(inputQuery);
			requestDocument = con.getDocumentBy(constants.getDocumentId(), inputQuery);
			logger.trace("{} got recognized as documentId", inputQuery);
			return requestDocument;
		} catch (NoEntryException e) { // inputQuery is integer id, but no such document found
			requestDocument = new DisplayDocument();
			requestDocument.setDocumentId(inputQuery);
			throw e;
		} catch (NumberFormatException e) {
			logger.trace("int failed");
			try {
				logger.trace("try origonal id");
				// get the requested document from the database by its
				// Original ID
				requestDocument = con.getDocumentBy(constants.getIdOriginal(), inputQuery);
				logger.trace("{} got recognized as originalId", inputQuery);
				return requestDocument;
			} catch (NoEntryException e1) {
				logger.trace("original id failed");
				// The encoding does not work for / so we convert them
				// by our own on JabRef side
				inputQuery = inputQuery.replaceAll("convbckslsh", "/");
				logger.trace("searching the database for a document with title");
				try {
					// get the requested document from the database by its
					// title
					requestDocument = con.getDocumentBy(constants.getTitle(), inputQuery);
					logger.trace("The Document is in our Database!");
					logger.trace("{} got recognized as title", inputQuery);
					return requestDocument;
				} catch (Exception e2) {
					logger.trace("it seems there is no document in our database with this title");
					logger.trace("trying if this matches a pattern in our database. In that case, we have a 404 error");
					Boolean prefixMatch = con.matchCollectionPattern(inputQuery, partnerId);
					requestDocument = new DisplayDocument();
					requestDocument.setTitle(inputQuery);
					String originalInputQuery = inputQuery;
					inputQuery = inputQuery
						.replaceAll(":|\\+|\\-|\\&|\\!|\\(|\\)|\\{|\\}|\\[|\\]|\\^|\"|\\~|\\?|\\*|\\\\|\\'|\\;", " ");
					requestDocument.setCleanTitle(inputQuery);
					if (!prefixMatch) {
						inputQuery = inputQuery.toLowerCase();
						// lucene does not like these chars
						logger.trace("requestDocument: {}", requestDocument.getTitle());
						return null;
					} else {
						requestDocument.setDocumentId(originalInputQuery);
						throw new NoEntryException(originalInputQuery);
					}
				}
			}
		}
	}

	private DocumentSet executeAlgorithmRandomly(DisplayDocument requestDocument, Boolean requestByTitle, DocumentSet documentset) throws Exception {
		// related document generator
		RelatedDocuments relatedDocumentGenerator = null;
		// get all related documents from solr
		Boolean validAlgorithmFlag = false;
		int numberOfAttempts = 0;
		documentset.setRequestedDocument(requestDocument);
		// Retry while algorithm is not valid, and we still have retries
		// left
		while (!validAlgorithmFlag && numberOfAttempts < constants.getNumberOfRetries()) {
			try {
				logger.trace("trying to get the algorithm from the factory");
				relatedDocumentGenerator = RecommenderFactory.getRandomRDG(con, documentset, requestByTitle);
				logger.trace("chosen algorithm: {}", relatedDocumentGenerator.algorithmLoggingInfo.getName());

				documentset.setRequestedDocument(requestDocument);
				documentset.setDesiredNumberFromAlgorithm(ar.getNumberOfCandidatesToReRank());

				documentset = relatedDocumentGenerator.getRelatedDocumentSet(documentset);
				validAlgorithmFlag = true;
				// If no related documents are present, redo the algorithm
			} catch (NoRelatedDocumentsException e) {
				logger.trace("algorithmLoggingInfo: {}", relatedDocumentGenerator.algorithmLoggingInfo);
				validAlgorithmFlag = false;
				numberOfAttempts++;
				if (requestByTitle) {
					validAlgorithmFlag = true;
				}
			}
		}

		if (validAlgorithmFlag) {
			if (numberOfAttempts > 0) {
				logger.trace("We retried {} times for document {}", numberOfAttempts, requestDocument.getDocumentId());
				documentset.setRequestedDocument(requestDocument);
			}
		} else {
			logger.trace("Using fallback recommender");
			relatedDocumentGenerator = RecommenderFactory.getFallback(con);
			try {
				documentset = relatedDocumentGenerator.getRelatedDocumentSet(documentset);
			} catch (NoRelatedDocumentsException e) {
				logger.trace("No related documents in fallback either");
				documentset.setRequestedDocument(requestDocument);
			}
		}
		return documentset;
	}

	private DocumentSet executeAlgorithmById(Algorithm algo, DocumentSet documentset, DisplayDocument requestDocument) throws Exception {
		RelatedDocuments algorithm = RecommenderFactory.getAlgorithmById(algo, con);
		documentset.setRequestedDocument(requestDocument);
		documentset.setDesiredNumberFromAlgorithm(ar.getNumberOfCandidatesToReRank());
		documentset = algorithm.getRelatedDocumentSet(documentset);
		return documentset;
	}

	@GET
	// set end of Path
	@Path("related_documents")
	@Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
	/**
	 * Get the related documentSet of a given document
	 * 
	 * @param documentIdOriginal
	 *            - id from the cooperation partner
	 * @return a document set of related documents
	 */
	public RootElement getRelatedDocumentSet(@Context HttpServletRequest request,
											 @PathParam("documentId") String inputQuery,
											 @QueryParam("org_id") String partnerName,
											 @QueryParam("app_id") String appName,
											 @QueryParam("app_version") String appVersion,
											 @QueryParam("app_lang") String appLang,
											 @QueryParam("algorithm_name") String algorithmName) {
		logger.info("started getRelatedDocumentSet with input: {}?{}", request.getRequestURL(), request.getQueryString());

		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}

		DisplayDocument requestDocument = null;
		DocumentSet documentset = new DocumentSet();
		Long timeToPickAlgorithm = null;
		Long timeToUserModel = null;
		Long timeAfterExecution = null;
		Boolean requestByTitle = false;

		// could lead to inconsistency. Identifier would be better
		String applicationId = null;
		String partnerId = null;
		try {
			/*
			 * Do some checks based on the Query parameters. Add 401 status code
			 * if anything is amiss Example: 401 if application_id is wrong or
			 * if organization_id (partner_id) is wrong or if link b/w app id
			 * and organization_id is incorrect
			 * 
			 */
			if (appName != null && !appName.equals("")) {
				try {

					applicationId = con.getApplicationId(appName);
					if (partnerName != null && !appName.equals("")) {
						Boolean appVerified = true;
						try {
							partnerId = con.getOrganizationId(partnerName);
						} catch (NoEntryException e) {
							appVerified = false;
						}
						if (appVerified)
							appVerified = con.verifyLinkAppOrg(applicationId, partnerId);
						if (!appVerified)
							statusReportSet.addStatusReport(new StatusReport(401, "Application_id " + appName
																			 + " is not linked with organization_id: " + partnerName));
					}
					partnerId = con.getIdInApplications(appName, constants.getOrganizationInApplication());

				} catch (NoEntryException e) {
					statusReportSet.addStatusReport(new StatusReport(401,
																	 "The application with name: " + appName + " has not been registered with Mr. DLib"));
				}
			}

			documentset.setIpAddress(ipAddress);
			documentset.setStartTime(requestRecieved);

			if (applicationId != null)
				documentset.setRequestingAppId(applicationId);
			if (partnerId != null)
				documentset.setRequestingPartnerId(partnerId);
			if (appVersion != null && appVersion.matches("[a-z0-9A-Z-#.]+"))
				documentset.setAppVersion(appVersion);
			if (appLang != null && appLang.length() > 1 && appLang.substring(0, 2).matches("[a-zA-Z][a-zA-Z]")) {
				documentset.setAppLang(appLang.substring(0, 2));
			}

			requestDocument = getRequestedDocument(inputQuery, partnerId);
			if (requestDocument == null) {
				requestByTitle = true;
				inputQuery = inputQuery.toLowerCase();
				requestDocument = new DisplayDocument(inputQuery, inputQuery, inputQuery);
			}

			timeToPickAlgorithm = System.currentTimeMillis();
			timeToUserModel = timeToPickAlgorithm;

			try {
				if (algorithmName == null || algorithmName.equals("")) {
					documentset = executeAlgorithmRandomly(requestDocument,requestByTitle, documentset);
				} else {
					Algorithm algo = Algorithm.parse(algorithmName);
					documentset = executeAlgorithmById(algo,documentset,requestDocument);
				}


				logger.trace("Do the documentset stuff");
				timeAfterExecution = System.currentTimeMillis();

				if (documentset.getSize() > 0) {
					documentset = ar.selectRandomRanking(documentset);
					documentset.setAfterAlgorithmExecutionTime(timeAfterExecution - timeToUserModel);
					documentset.setAfterAlgorithmChoosingTime(timeToPickAlgorithm - requestRecieved);
					documentset.setAfterUserModelTime(timeToUserModel - timeToPickAlgorithm);

					documentset.setAfterRerankTime(System.currentTimeMillis() - timeAfterExecution);
					documentset.setRankDelivered();
					documentset.setNumberOfDisplayedRecommendations(documentset.getSize());
				} else {
					throw new NoRelatedDocumentsException(requestDocument.getDocumentId(),
														requestDocument.getOriginalDocumentId());
				}

			} catch(IllegalArgumentException e) {
				StatusReport status = new StatusReport(400, String.format("Invalid algorithm name specified: %s", algorithmName));
				statusReportSet.addStatusReport(status);
				rootElement.setDocumentSet(null);
				rootElement.setStatusReportSet(statusReportSet);
				return rootElement;
			} catch(NoRelatedDocumentsException e) {
				logger.info("{} returned no related documents for {}", algorithmName, inputQuery);
				if (!requestByTitle) {

					statusReportSet.addStatusReport(new StatusReport(204,
																	"No related documents found for document id: " + requestDocument.getDocumentId()
																	+ " (original document id : " + requestDocument.getOriginalDocumentId() + ")"));
				} else {
					statusReportSet.addStatusReport(new StatusReport(204, "Documents related to query by title ("
																	+ requestDocument.getTitle() + " ) were not found"));
				}
			}

		} catch (NoEntryException e1) {
			// if there is no such document in the database
			statusReportSet.addStatusReport(new StatusReport(404, "No such document with document id "
															 + requestDocument.getDocumentId() + " exists in our database"));
			rootElement.setDocumentSet(null);
			rootElement.setStatusReportSet(statusReportSet);
			return rootElement;

		} catch (Exception e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
			logger.warn("Caught exception while handling {}", inputQuery, e);
			rootElement.setDocumentSet(null);
			rootElement.setStatusReportSet(statusReportSet);
			return rootElement;
		}
		// if everything went ok
		if (statusReportSet.getSize() == 0)
			statusReportSet.addStatusReport(new StatusReport(200, new StatusMessage("ok", "en")));

		logger.trace("Did the documentset stuff");

		// add both the status message and the related document to the xml
		rootElement.setDocumentSet(documentset);
		rootElement.setStatusReportSet(statusReportSet);
		logger.trace("added stuff to root element");
		logger.trace("requestByTitle is: {}", requestByTitle);
		logger.trace("Try to do the logging stuff");

		try {
			// log all the statistic about this execution
			String referenceId = "";
			if (requestByTitle) {
				String titleStringId = con.getTitleStringId(requestDocument);
				referenceId = titleStringId;
			} else {
				referenceId = requestDocument.getDocumentId();
			}
			documentset = con.logRecommendationDeliveryNew(referenceId, rootElement, requestByTitle);

			for (DisplayDocument doc : documentset.getDocumentList()) {
				String url = "https://" + constants.getEnvironment() + ".mr-dlib.org/v1/recommendations/"
					+ doc.getRecommendationId() + "/original_url?access_key=" + documentset.getAccessKeyHash()
					+ "&format=direct_url_forward";
				if(appName != null){
					url += "&app_id=" + appName;
				}
				doc.setClickUrl(url);
			}
		} catch (Exception e) {
			logger.warn("Caught exception while logging query {}", inputQuery, e);
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		}

		try {
			logger.trace("try to close the db con");
			if (con != null)
				con.close();
		} catch (Exception e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		}

		try {
			if (statusReportSet.getSize() > 1)
				statusReportSet.setDebugDetailsPerSetInStatusReport(documentset.getDebugDetailsPerSet());
		} catch (NullPointerException e) {
			// throw new NullPointerException("It seems we don't have access to
			// the database or solr index."
			// + " This is happening most likely because you didn't changed the
			// config file properly, mysql or solr could also be down.");
			logger.error("Null pointer while setting debug details for query {}", inputQuery, e);
		}

		if (!constants.getDebugModeOn()) {
			DisplayDocument current = null;
			if (rootElement.getDocumentSet().getSize() > 0) {
				rootElement.getDocumentSet().setDebugDetailsPerSet(null);

				for (int i = 0; i < documentset.getSize(); i++) {
					current = rootElement.getDocumentSet().getDisplayDocument(i);
					current.setDebugDetails(null);
				}
			}
			for (StatusReport report : rootElement.getStatusReportSet().getStatusReportList()) {
				report.setDebugMessage(null);
			}
		}
		if (documentset.getSize() == 0) {
			rootElement.setDocumentSet(null);
		}
		return rootElement;

	}

}
