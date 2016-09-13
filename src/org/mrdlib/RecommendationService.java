package org.mrdlib;

import java.net.URI;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mrdlib.database.DBConnection;
import org.mrdlib.database.NoEntryException;
import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.RootElement;
import org.mrdlib.display.StatusMessage;
import org.mrdlib.display.StatusReport;
import org.mrdlib.display.StatusReportSet;

@Path("recommendations")
public class RecommendationService {
	private Long requestRecieved;
	private DBConnection con = null;
	private Constants constants = null;
	private RootElement rootElement = null;
	private StatusReportSet statusReportSet = null;

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

	@GET
	@Produces("text/plain")
	@Path("{recommendationId}/original_url/")
	public String getOriginalDoc(@PathParam("recommendationId") String recoId, @PathParam("access_key") String hash,
			@PathParam("request_format") String format) throws SQLException {
		String docId = "dummy2";

		try {
			docId = con.getDocIdFromRecommendation(recoId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (con != null) {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Hello World " + recoId + "\n" + "Access key is " + hash + "\nrequest format is:" + format + "doc id is:"
				+ docId;
	}

	@GET
	@Path("{recommendationId:[0-9]+}/original_url/&access_key={access_key: [0-9a-z]+}&format={request_format}")
	public Response getRedirectedPath(@PathParam("recommendationId") String recoId,
			@PathParam("access_key") String accessKey, @PathParam("request_format") String format) throws Exception {
		URI url;
		Boolean accessKeyCheck = false;
		DisplayDocument relDocument;
		String docId = "";
		String urlString = "";
		try {
			accessKeyCheck = con.checkAccessKey(recoId, accessKey);
			if (accessKeyCheck) {
				try {
					docId = con.getDocIdFromRecommendation(recoId);
					relDocument = con.getDocumentBy(constants.getDocumentId(), docId);
					urlString = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());

				} catch (NoEntryException e) {
					statusReportSet.addStatusReport(e.getStatusReport());
				} catch (Exception e) {
					statusReportSet
							.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
				}
			} else {
				statusReportSet.addStatusReport(new InvalidAccessKeyException().getStatusReport());
			}

		} catch (NoEntryException e) {
			statusReportSet.addStatusReport(
					new UnknownException("Recommendation id" + recoId + " is invalid").getStatusReport());
		}
		if (statusReportSet.getSize() == 0)
			statusReportSet.addStatusReport(new StatusReport(200, new StatusMessage("ok", "en")));

		rootElement.setStatusReportSet(statusReportSet);
		try {
			url = new URI(urlString);
			Boolean loggingDone = con.logRecommendationClick(recoId, docId, requestRecieved, rootElement);
			if (loggingDone)
				return Response.seeOther(url).build();
			else
				throw new UnknownException("Logging could not be completed for this click");

		} catch (Exception e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (Exception e) {
				statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
			}
		}

		return Response.ok(rootElement, MediaType.APPLICATION_XML).build();
	}
}
