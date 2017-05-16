package org.mrdlib.api.manager;

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
import org.mrdlib.recommendation.algorithm.RelatedDocuments;
import org.mrdlib.recommendation.framework.NoRelatedDocumentsException;
import org.mrdlib.recommendation.ranking.ApplyRanking;

/**
 * @author Millah
 * 
 *         This class is called by Tomcat and the start of the webapp
 */
// yxc get the name here
@Path("documents/{documentId : [a-zA-Z0-9-_.,%:;!'&@?+*#()$]+}")
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
	// related document generator
	private RelatedDocuments relatedDocumentGenerator = null;

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
			@PathParam("documentId") String inputQuery, @QueryParam("org_id") String partnerName,
			@QueryParam("app_id") String appName, @QueryParam("app_version") String appVersion,
			@QueryParam("app_lang") String appLang) {
		if (constants.getDebugModeOn())
			System.out.println("started getRelatedDocumentSet with input: " + inputQuery);

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
		boolean fourOFourError = false;

		// could lead to inconsistency. Identifier would be better
		String applicationId = "";
		String partnerId = "";
		try {
			/*
			 * Do some checks based on the Query parameters. Add 401 status code
			 * if anything is amiss Example: 401 if application_id is wrong or
			 * if organization_id (partner_id) is wrong or if link b/w app id
			 * and organization_id is incorrect
			 * 
			 */
			if (partnerName != null && appName != null) {
				try {

					applicationId = con.getApplicationId(appName);
					partnerId = con.getOrganizationId(partnerName);

					Boolean appVerified = con.verifyLinkAppOrg(applicationId, partnerId);
					if (!appVerified)
						statusReportSet.addStatusReport(new StatusReport(401,
								"Application_id " + appName + " is not linked with organization_id: " + partnerName));
				} catch (NoEntryException e) {
					statusReportSet.addStatusReport(new StatusReport(401,
							"Authenticity check is invalid. There is no link between app_id and org_id that has been provided in the query URL",
							"Application_id: " + appName + " organization_id: " + partnerName));
				}
			} else {
				applicationId = null;
				partnerId = null;
			}

			{
				documentset.setIpAddress(ipAddress);
				documentset.setStartTime(requestRecieved);

				if (applicationId != null)
					documentset.setRequestingAppId(applicationId);
				if (partnerId != null)
					documentset.setRequestingPartnerId(partnerId);
				if (appVersion != null)
					documentset.setAppVersion(appVersion);
				if (appLang != null && appLang.length()>1)
					documentset.setAppLang(appLang.substring(0, 2));
			}
			/*
			 * First we have a look if it is an integer. if the conversion
			 * fails, e.g. there are some letters in it, we go on and try to get
			 * an document by its original id. If that fails, we search for the
			 * title in our database. Later, we can add here the function to do
			 * a lucene mlt.
			 */
			try {
				// get the requested document from the database by mdl ID
				if (constants.getDebugModeOn())
					System.out.println("try int");
				Integer.parseInt(inputQuery);
				requestDocument = con.getDocumentBy(constants.getDocumentId(), inputQuery);
			}catch(NoEntryException e){
				requestDocument = new DisplayDocument();
				requestDocument.setDocumentId(inputQuery);
				requestByTitle = false;
				throw e;
			} catch (NumberFormatException e) {
				if (constants.getDebugModeOn())
					System.out.println("int failed");
				try {
					if (constants.getDebugModeOn())
						System.out.println("try origonal id");
					// get the requested document from the database by its
					// Original ID
					requestDocument = con.getDocumentBy(constants.getIdOriginal(), inputQuery);
				} catch (NoEntryException e1) {
					if (constants.getDebugModeOn())
						System.out.println("original id failed");
					// The encoding does not work for / so we convert them
					// by our own on JabRef side
					inputQuery = inputQuery.replaceAll("convbckslsh", "/");
					if (constants.getDebugModeOn())
						System.out.println("searching the database for a document with title");
					try {
						// get the requested document from the database by its
						// title
						requestDocument = con.getDocumentBy(constants.getTitle(), inputQuery);
						if (constants.getDebugModeOn())
							System.out.println("The Document is in our Database!");
					} catch (Exception e2) {
						if (constants.getDebugModeOn())
							System.out.println("it seems there is no document in our database with this title");
						if (constants.getDebugModeOn())
							System.out.println(
									"lets now try if this matches a pattern in our database. In that case, we have a 404 error");

						Boolean prefixMatch = con.matchCollectionPattern(inputQuery, partnerId);
						requestDocument = new DisplayDocument();
						requestDocument.setTitle(inputQuery);
						inputQuery = inputQuery
								.replaceAll(":|\\+|\\-|\\&|\\!|\\(|\\)|\\{|\\}|\\[|\\]|\\^|\"|\\~|\\?|\\*|\\\\", " ");
						requestDocument.setCleanTitle(inputQuery);
						if (!prefixMatch) {
							requestByTitle = true;
							inputQuery = inputQuery.toLowerCase();
							// lucene does not like these chars
							System.out.println("requestDocument: " + requestDocument.getTitle());
						} else {
							throw new NoEntryException(inputQuery);
						}
					}
				}
			}

			// get all related documents from solr
			Boolean validAlgorithmFlag = false;
			int numberOfAttempts = 0;

			// Retry while algorithm is not valid, and we still have retries
			// left
			while (!validAlgorithmFlag && numberOfAttempts < constants.getNumberOfRetries()) {
				try {
					if (constants.getDebugModeOn())
						System.out.println("trying to get the algorithm from the factory");
					relatedDocumentGenerator = RecommenderFactory.getRandomRDG(con, requestDocument, requestByTitle);
					timeToPickAlgorithm = System.currentTimeMillis();
					timeToUserModel = timeToPickAlgorithm;
					if (constants.getDebugModeOn())
						System.out.println(
								"chosen algorithm: " + relatedDocumentGenerator.algorithmLoggingInfo.getName());

					documentset.setRequestedDocument(requestDocument);
					documentset.setDesiredNumberFromAlgorithm(ar.getNumberOfCandidatesToReRank());

					documentset = relatedDocumentGenerator.getRelatedDocumentSet(documentset);
					validAlgorithmFlag = true;
					// If no related documents are present, redo the algorithm
				} catch (NoRelatedDocumentsException e) {
					if (constants.getDebugModeOn())
						System.out.println(
								"algorithmLoggingInfo: " + relatedDocumentGenerator.algorithmLoggingInfo.toString());
					validAlgorithmFlag = false;
					numberOfAttempts++;
					if (requestByTitle) {
						statusReportSet.addStatusReport(
								new StatusReport(404, "No related documents corresponding to input query:"
										+ requestDocument.getCleanTitle()));
						validAlgorithmFlag = true;
					}
				}
			}

			if (validAlgorithmFlag) {
				if (numberOfAttempts > 0) {
					if (constants.getDebugModeOn())
						System.out.printf("We retried %d times for document " + requestDocument.getDocumentId() + "\n",
								numberOfAttempts);
					documentset.setRequestedDocument(requestDocument);
				}
			} else {
				if (constants.getDebugModeOn())
					System.out.println("Using fallback recommender");
				relatedDocumentGenerator = RecommenderFactory.getFallback(con);
				try {
					documentset = relatedDocumentGenerator.getRelatedDocumentSet(documentset);
				} catch (NoRelatedDocumentsException e) {
					if (constants.getDebugModeOn())
						System.out.println("No related documents in fallback either");
					documentset.setRequestedDocument(requestDocument);
				}
			}
			if (constants.getDebugModeOn())
				System.out.println("Do the documentset stuff");

			timeAfterExecution = System.currentTimeMillis();


			if (documentset.getSize() > 0) {
				documentset = ar.selectRandomRanking(documentset);
				documentset.setAfterAlgorithmExecutionTime(timeAfterExecution - timeToUserModel);
				documentset.setAfterAlgorithmChoosingTime(timeToPickAlgorithm - requestRecieved);
				documentset.setAfterUserModelTime(timeToUserModel - timeToPickAlgorithm);

				documentset.setAfterRerankTime(System.currentTimeMillis() - timeAfterExecution);
				documentset.setRankDelivered();
				documentset.setNumberOfDisplayedRecommendations(documentset.getSize());

			} else
				throw new NoRelatedDocumentsException(documentset.getRequestedDocument().getOriginalDocumentId(),
						documentset.getRequestedDocument().getDocumentId());

		} catch (NoEntryException e1) {
			// if there is no such document in the database
			fourOFourError = true;
			statusReportSet.addStatusReport(e1.getStatusReport());
			// if retry limit has been reached and no related documents still
			// have been extracted
		} catch (NoRelatedDocumentsException e) {
			statusReportSet.addStatusReport(e.getStatusReport());

			// if something else happened there
		} catch (Exception e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
			e.printStackTrace();
		}
		// if everything went ok
		if (statusReportSet.getSize() == 0)
			statusReportSet.addStatusReport(new StatusReport(200, new StatusMessage("ok", "en")));
		else {
			for (StatusReport statusReport : statusReportSet.getStatusReportList()) {
				if (statusReport.getStatusCode() == 404) {
					fourOFourError = true;
					break;
				}
			}
			if (fourOFourError) {
				System.out.println("Got here");
				statusReportSet = new StatusReportSet();
				if (requestByTitle) {
					statusReportSet.addStatusReport(new StatusReport(404,
							"Documents related to query by title(" + requestDocument.getTitle() + " )were not found"));
				} else {
					statusReportSet.addStatusReport(new StatusReport(404, "No such document with document id "
							+ requestDocument.getDocumentId() + " exists in our database"));
				}
			}
		}
		System.out.println("Did the documentset stuff");

		// add both the status message and the related document to the xml
		rootElement.setDocumentSet(documentset);
		rootElement.setStatusReportSet(statusReportSet);
		if (constants.getDebugModeOn()) {
			System.out.println("added stuff to root element");
			System.out.println("requestByTitle is: " + requestByTitle);
			System.out.println("Try to do the logging stuff");
		}

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
				doc.setClickUrl(url);
			}
		} catch (Exception e) {
			System.out.println("nullpointer catched");
			e.printStackTrace();
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		}

		try {
			System.out.println("try to close the db con");
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
			e.printStackTrace();
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

	/*
	 * @GET
	 * 
	 * @Produces("text/plain") public String getOriginalDoc() { return
	 * "Hello World "; }
	 */

}
