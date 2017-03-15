package org.mrdlib.api.manager;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.api.response.RootElement;
import org.mrdlib.api.response.StatusMessage;
import org.mrdlib.api.response.StatusReport;
import org.mrdlib.api.response.StatusReportSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.database.NoEntryException;

@Path("recommendations")
// Path for the root of this class
public class RecommendationService {
	private Long requestRecieved;
	private DBConnection con = null;
	private Constants constants = null;
	private RootElement rootElement = null;
	private StatusReportSet statusReportSet = null;

	// set up the necessary connections
	public RecommendationService() {
		requestRecieved = System.currentTimeMillis();
		rootElement = new RootElement();
		statusReportSet = new StatusReportSet();
		constants = new Constants();
		try {
			con = new DBConnection("tomcat");
		} catch (Exception e) {
			if (constants.getDebugModeOn()) {
				e.printStackTrace();
				statusReportSet.addStatusReport(
						new UnknownException("Message:" + e.getMessage() + "\n StackTrace: " + e.getStackTrace())
								.getStatusReport());
			} else {
				e.printStackTrace();
				statusReportSet.addStatusReport(new UnknownException().getStatusReport());
			}
		}
	}

	/**
	 * This method accepts click(ed) url's and logs the click in our database.
	 * It returns the actual link which was to be viewed
	 * 
	 * @param recoId
	 *            The recommendation ID created during the initial
	 *            recommendation process
	 * @param accessKey
	 *            The access key hash that was created as part of the creation
	 *            of the recommendation set.
	 * @param format
	 *            Describes the type of response to be returned. Currently only
	 *            supports direct_url_forward
	 * @return a Response object that contains the url of the actual link to be
	 *         clicked
	 * @throws Exception
	 */
	@GET
	@Path("{recommendationId:[0-9]+}/original_url/")
	public Response getRedirectedPathReversedParams(@Context HttpServletRequest request, @PathParam("recommendationId") String recoId,
			@QueryParam("access_key") String accessKey, @QueryParam("request_format") String format) throws Exception {
		return getRedirectedPath(request, recoId, accessKey, format);
	}

	/**
	 * This method accepts click(ed) url's and logs the click in our database.
	 * It returns the actual link which was to be viewed
	 * 
	 * @param recoId
	 *            The recommendation ID created during the initial
	 *            recommendation process
	 * @param accessKey
	 *            The access key hash that was created as part of the creation
	 *            of the recommendation set.
	 * @param format
	 *            Describes the type of response to be returned. Currently only
	 *            supports direct_url_forward
	 * @return a Response object that contains the url of the actual link to be
	 *         clicked
	 * @throws Exception
	 */
	public Response getRedirectedPath(HttpServletRequest request, String recoId, String accessKey, String format) throws Exception {
		URI url;
		Boolean accessKeyCheck = false;
		DisplayDocument relDocument;
		String docId = "";
		String urlString = "";
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}

		try {

			// Check accessKey from clickURL against the one stored in our
			// database
			accessKeyCheck = con.checkAccessKey(recoId, accessKey, false);
			if (accessKeyCheck) {
				try {
					// Get document related to recommendation
					docId = con.getDocIdFromRecommendation(recoId);
					relDocument = con.getDocumentBy(constants.getDocumentId(), docId);

					// Generate the redirection Path
					if (relDocument.getCollectionShortName().equals(constants.getGesis())) {
						if (constants.getEnvironment().equals("api"))
							urlString = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());
						else
							urlString = constants.getGesisBetaCollectionLink()
									.concat(relDocument.getOriginalDocumentId());
					} else if (relDocument.getCollectionShortName().contains(constants.getCore()))
						urlString = constants.getCoreCollectionLink()
								.concat(relDocument.getOriginalDocumentId().split("-")[1]);

				} catch (Exception e) {
					statusReportSet
							.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
				}
			} else {
				statusReportSet.addStatusReport(new InvalidAccessKeyException().getStatusReport());
			}

		} catch (NoEntryException e) {
			statusReportSet.addStatusReport(new NoEntryException(recoId, "Recommendation").getStatusReport());
		}
		if (statusReportSet.getSize() == 0)
			statusReportSet.addStatusReport(new StatusReport(200, new StatusMessage("ok", "en")));

		rootElement.setStatusReportSet(statusReportSet);
		try {
			url = new URI(urlString);
			DocumentSet results = new DocumentSet();
			results.setIpAddress(ipAddress);
			results.setStartTime(requestRecieved);
			rootElement.setDocumentSet(results);
			// Log recommendation Click
			Boolean clickLoggingDone = con.logRecommendationClick(recoId, rootElement, accessKeyCheck);
			if (accessKeyCheck) {
				if (clickLoggingDone)
					// Return redirected response
					return Response.seeOther(url).build();
				else
					throw new UnknownException("Logging could not be completed for this click");
			}
			
			rootElement.setDocumentSet(null);
		} catch (Exception e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (Exception e) {
				statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
				e.printStackTrace();
			}
		}

		return Response.ok(rootElement, MediaType.APPLICATION_XML).build();
	}

}
