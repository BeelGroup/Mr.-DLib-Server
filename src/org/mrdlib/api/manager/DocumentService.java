package org.mrdlib.api.manager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mrdlib.api.response.DebugDetails;
import org.mrdlib.api.response.DebugDetailsPerSet;
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
@Path("documents/{documentId : [a-zA-Z0-9-_.,]+}")
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
	private RelatedDocuments rdg = null;

	public DocumentService() {
		//get the time the request came in for statistic
		//initialize the xml structure
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
	public RootElement getRelatedDocumentSet(@PathParam("documentId") String documentIdOriginal) {
		DisplayDocument requestDocument = null;
		DocumentSet documentset = null;
		Long timeToPickAlgorithm = null;
		Long timeToUserModel = null;
		try {
			// get the requested document from the database
			requestDocument = con.getDocumentBy(constants.getIdOriginal(), documentIdOriginal);
			// get all related documents from solr
			Boolean validAlgorithmFlag = false;
			int numberOfAttempts = 0;
			
			// Retry while algorithm is not valid, and we still have retries
			// left
			while (!validAlgorithmFlag && numberOfAttempts < constants.getNumberOfRetries()) {
				try {
					rdg = RecommenderFactory.getRandomRDG(con, requestDocument);
					timeToPickAlgorithm = System.currentTimeMillis();
					timeToUserModel = timeToPickAlgorithm;
					System.out.println(rdg.algorithmLoggingInfo.getName());
					documentset = rdg.getRelatedDocumentSet(requestDocument, ar.getNumberOfCandidatesToReRank());
					
					validAlgorithmFlag = true;
					// If no related documents are present, redo the algorithm
				} catch (NoRelatedDocumentsException e) {
					System.out.println(rdg.algorithmLoggingInfo.toString());
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
				rdg = RecommenderFactory.getFallback(con);
				documentset = rdg.getRelatedDocumentSet(requestDocument, ar.getNumberOfCandidatesToReRank());
			}
			Long timeAfterExecution = System.currentTimeMillis();
			documentset.setAfterAlgorithmExecutionTime(timeAfterExecution-timeToUserModel);
			documentset.setAfterAlgorithmChoosingTime(timeToPickAlgorithm-requestRecieved);
			documentset.setAfterUserModelTime(timeToUserModel-timeToPickAlgorithm);
			documentset.setAlgorithmDetails(rdg.getAlgorithmLoggingInfo());
			documentset = ar.selectRandomRanking(documentset);
			documentset.setAfterRerankTime(System.currentTimeMillis()-timeAfterExecution);
			documentset.setRankDelivered();
			documentset.setNumberOfDisplayedRecommendations(documentset.getSize());
			documentset.setStartTime(requestRecieved);
			// if there is no such document in the database
		} catch (NoEntryException e) {
			statusReportSet.addStatusReport(e.getStatusReport());
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

		try {
			//log all the statistic about this execution
			documentset = con.logRecommendationDeliveryNew(requestDocument.getDocumentId(), rootElement);

			for (DisplayDocument doc : documentset.getDocumentList()) {
				String url = "https://" + constants.getEnvironment() + ".mr-dlib.org/v1/recommendations/"
						+ doc.getRecommendationId() + "/original_url?access_key=" + documentset.getAccessKeyHash()
						+ "&format=direct_url_forward";
				doc.setClickUrl(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		}

		try {
			if (con != null)
				con.close();
		} catch (Exception e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		}
		
		if(!constants.getDebugModeOn()) {
			DisplayDocument current = null;
			rootElement.getDocumentSet().setDebugDetailsPerSet(null);
			for(int i = 0; i < documentset.getSize(); i++) {
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
