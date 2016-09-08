package org.mrdlib;


import java.net.URI;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.mrdlib.database.DBConnection;
import org.mrdlib.display.DisplayDocument;


@Path("recommendations")
public class RecommendationService {
	
	private DBConnection con = null;
	private Constants constants = null;
	public RecommendationService() {
		
		constants = new Constants();
		try {
			con = new DBConnection("tomcat");
		} catch (Exception e) {
			if(constants.getDebugModeOn()) {
				e.printStackTrace();
			} else {
				e.printStackTrace();
			}
		}
	
	}
	
	@GET
	@Produces("text/plain")
	@Path("{recommendationId}/original_url/")
    public String getOriginalDoc(@PathParam("recommendationId") String recoId, @PathParam("access_key") String hash,
    		@PathParam("request_format") String format) throws SQLException {
		String docId = "dummy2";
		/*try {
			docId = con.getDocIdFromRecommendation(recoId);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
        return "Hello World " + recoId + "\n" + "Access key is "+ hash +"\nrequest format is:" + format
        		+"doc id is:" + docId;
    }

	@GET
	@Path("{recommendationId:[a-zA-Z0-9-_.,]+}/original_url/&access_key={access_key: [0-9a-z]+}&format={request_format}")
    public Response getRedirectedPath(@PathParam("recommendationId") String recoId, 
    		@PathParam("request_format") String format) throws Exception {
        URI url;
        DisplayDocument relDocument;
        if (con == null){
        	url = new URI("http://google.com");
        	return Response.seeOther(url).build();
        }
		try {
			String docId = con.getDocIdFromRecommendation(recoId);
			relDocument = con.getDocumentBy(constants.getDocumentId(),
					docId);
	        String urlString = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());
	        //System.out.println("\n" + url_string);
	        //	url = new URI("http://sowiport.gesis.org/search/id/gesis-smarth-0000003281");
	        url = new URI(urlString);	
		} catch (Exception e){
			throw e;
		}
        return Response.seeOther(url).build();
    }
}	
