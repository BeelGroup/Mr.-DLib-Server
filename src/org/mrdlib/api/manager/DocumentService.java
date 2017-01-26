package org.mrdlib.api.manager;

import java.net.URLDecoder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
@Path("documents/{documentId : [a-zA-Z0-9-_.,%:!?+]+}")
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
	public RootElement getRelatedDocumentSet(@PathParam("documentId") String inputQuery) {
		System.out.println("started getRelatedDocumentSet with documentI: " + inputQuery);
		DisplayDocument requestDocument = null;
		DocumentSet documentset = null;
		Long timeToPickAlgorithm = null;
		Long timeToUserModel = null;
		Boolean requestByTitle = false;
		// could lead to inconsistency. Identifyer would be better
		try {

			/*
			 * First we have a look if it is an integer. if the conversion
			 * fails, e.g. there are some letters in it, we go on and try to get
			 * an document by its original id. If that fails, we search for the
			 * title in our database. Later, we can add here the function to do
			 * a lucene mlt.
			 */
			try {
				// get the requested document from the database by mdl ID
				System.out.println("try int");
				Integer.parseInt(inputQuery);
				requestDocument = con.getDocumentBy(constants.getDocumentId(), inputQuery);
			} catch (NumberFormatException e) {
				System.out.println("int failed");
				try {
					System.out.println("try origonal id");
					// get the requested document from the database by its
					// Original ID
					requestDocument = con.getDocumentBy(constants.getIdOriginal(), inputQuery);
				} catch (NoEntryException e1) {
					System.out.println("original id failed");
					inputQuery = URLDecoder.decode(inputQuery, "UTF-8");
					System.out.println("the stuff coming in: " + inputQuery);
					System.out.println("searching the database for a document with the title");
					try {
						// get the requested document from the database by its
						// title
						requestDocument = con.getDocumentBy(constants.getTitle(), inputQuery);
						System.out.println("The Document is in our Database!");
					} catch (Exception e2) {
						System.out.println("it seems there is no document in our database with this title");
						System.out.println("lets now try if lucene find some documents for us.");
						requestByTitle = true;
						requestDocument = new DisplayDocument();
						requestDocument.setTitle(inputQuery);
						System.out.println("requestDocument: " + requestDocument.getTitle());
					}
				}
			}

			System.out.println("after catch. the document exists!");

			// get all related documents from solr
			Boolean validAlgorithmFlag = false;
			int numberOfAttempts = 0;

			// Retry while algorithm is not valid, and we still have retries
			// left
			while (!validAlgorithmFlag && numberOfAttempts < constants.getNumberOfRetries()) {
				try {
					System.out.println("trying to get the algorithm from the factory");
					relatedDocumentGenerator = RecommenderFactory.getRandomRDG(con, requestDocument, requestByTitle);
					timeToPickAlgorithm = System.currentTimeMillis();
					timeToUserModel = timeToPickAlgorithm;
					System.out.println("chosen algorithm: " + relatedDocumentGenerator.algorithmLoggingInfo.getName());

					documentset = relatedDocumentGenerator.getRelatedDocumentSet(requestDocument,
							ar.getNumberOfCandidatesToReRank());

					validAlgorithmFlag = true;
					// If no related documents are present, redo the algorithm
				} catch (NoRelatedDocumentsException e) {
					System.out.println(
							"algorithmLoggingInfo: " + relatedDocumentGenerator.algorithmLoggingInfo.toString());
					validAlgorithmFlag = false;
					numberOfAttempts++;
				}
			}

			if (validAlgorithmFlag) {
				if (numberOfAttempts > 0)
					System.out.printf("We retried %d times for document " + requestDocument.getDocumentId() + "\n",
							numberOfAttempts);
			} else {
				System.out.println("Using fallback recommender");
				relatedDocumentGenerator = RecommenderFactory.getFallback(con);
				documentset = relatedDocumentGenerator.getRelatedDocumentSet(requestDocument,
						ar.getNumberOfCandidatesToReRank());
			}
			System.out.println("Do the documentset stuff");
			Long timeAfterExecution = System.currentTimeMillis();
			documentset.setAfterAlgorithmExecutionTime(timeAfterExecution - timeToUserModel);
			documentset.setAfterAlgorithmChoosingTime(timeToPickAlgorithm - requestRecieved);
			documentset.setAfterUserModelTime(timeToUserModel - timeToPickAlgorithm);
			documentset.setAlgorithmDetails(relatedDocumentGenerator.getAlgorithmLoggingInfo());

			documentset = ar.selectRandomRanking(documentset);
			documentset.setAfterRerankTime(System.currentTimeMillis() - timeAfterExecution);
			documentset.setRankDelivered();
			documentset.setNumberOfDisplayedRecommendations(documentset.getSize());
			documentset.setStartTime(requestRecieved);
			System.out.println("Did the documentset stuff");
		} catch (NoEntryException e1) {
			// if there is no such document in the database
			statusReportSet.addStatusReport(e1.getStatusReport());

			// if retry limit has been reached and no related documents still
			// have been extracted
		} catch (NoRelatedDocumentsException e) {
			statusReportSet.addStatusReport(e.getStatusReport());
			// if something else happened there
		} catch (Exception e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		}
		// if everything went ok
		if (statusReportSet.getSize() == 0)
			statusReportSet.addStatusReport(new StatusReport(200, new StatusMessage("ok", "en")));

		// add both the status message and the related document to the xml
		rootElement.setDocumentSet(documentset);
		rootElement.setStatusReportSet(statusReportSet);
		System.out.println("added stuff to root element");
		System.out.println("requestByTitle is: " + requestByTitle);
		if (!requestByTitle) {
			System.out.println("Try to do the logging stuff");
			try {
				// log all the statistic about this execution
				documentset = con.logRecommendationDeliveryNew(requestDocument.getDocumentId(), rootElement);

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
			//throw new NullPointerException("It seems we don't have access to the database or solr index."
			//		+ " This is happening most likely because you didn't changed the config file properly, mysql or solr could also be down.");
			e.printStackTrace();
		}

		if (!constants.getDebugModeOn()) {
			DisplayDocument current = null;
			rootElement.getDocumentSet().setDebugDetailsPerSet(null);
			for (int i = 0; i < documentset.getSize(); i++) {
				current = rootElement.getDocumentSet().getDisplayDocument(i);
				current.setDebugDetails(null);
			}
		}

		return rootElement;
	}

	@GET
	@Produces("text/plain")
	public String getOriginalDoc() {
		return "Hello World ";
	}

}
