package org.mrdlib.api.manager;

import java.sql.SQLException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mrdlib.api.response.RootElement;
import org.mrdlib.api.response.StatusMessage;
import org.mrdlib.api.response.StatusReport;
import org.mrdlib.api.response.StatusReportSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.database.NoEntryException;

@Path("recommendation_sets")
// Path for the root of this class
public class RecommendationSetService {
	private Long requestRecieved;
	private DBConnection con = null;
	private Constants constants = null;
	private RootElement rootElement = null;
	private StatusReportSet statusReportSet = null;

	// set up the necessary connections
	public RecommendationSetService() {
		requestRecieved = System.currentTimeMillis();
		rootElement = new RootElement();
		statusReportSet = new StatusReportSet();
		constants = new Constants();
		System.out.println("In here");
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
	 * This method accepts recommendation_set received POST acknowledgments and
	 * logs in our database. It returns the actual link which was to be viewed
	 * 
	 * @param recommendationSetId
	 *            The recommendation Set ID created during the initial
	 *            recommendation process
	 * @param accessKey
	 *            The access key hash that was created as part of the creation
	 *            of the recommendation set.
	 * @return a Response object that contains the status code for this request
	 * @throws Exception
	 */
	@POST
	@Path("{recommendationSetId:[0-9]+}/confirmation_of_receipt")
	public Response postAcknowledgeRecommendationReceipt(@PathParam("recommendationSetId") String recommendationSetId,
			@QueryParam("access_key") String accessKey) throws Exception {
		Boolean accessKeyCheck = false;
		if (!accessKey.matches("[0-9a-z]+"))
			System.out.println(accessKey);
		try {
			if (!accessKey.matches("[0-9a-z]+"))
				throw new InvalidAccessKeyException();
			accessKeyCheck = con.checkAccessKey(recommendationSetId, accessKey, true);
			if (accessKeyCheck)
				con.logRecommendationSetReceivedAcknowledgement(recommendationSetId, requestRecieved);
			else
				throw new InvalidAccessKeyException();
		} catch (NoEntryException e) {
			statusReportSet.addStatusReport(e.getStatusReport());
		} catch (SQLException e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (Exception e) {
				statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
			}

			if (statusReportSet.getSize() == 0)
				statusReportSet.addStatusReport(new StatusReport(201, new StatusMessage("ok", "en")));

			rootElement.setStatusReportSet(statusReportSet);
		}

		return Response.ok(rootElement, MediaType.APPLICATION_XML).build();
	}
	
	@POST
	@Path("{recommendationSetId:[0-9]+}/spoof")
	@Produces("text/plain")
	public String foo(){
		return "Gotcha fam";
	}
}
