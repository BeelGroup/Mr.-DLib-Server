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
	@Path("{recommendationId: [a-zA-Z0-9-_.,]+}/original_url/")
    public String getOriginalDoc(@PathParam("recommendationId") String reco_id, @PathParam("access_key") String hash,
    		@PathParam("request_format") String format) throws SQLException {
		String rec_id = "1";
		String doc_id = "dummy2";
		try {
			doc_id = con.getDocIdFromRecommendation(rec_id);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return "Hello World " + reco_id + "\n" + "Access key is "+ hash +"\nrequest format is:" + format
        		+"doc id is:" + doc_id;
    }

	@GET
	@Path("{recommendationId: [a-zA-Z0-9-_.,]+}/original_url/&access_key={access_key: [0-9]+}&format={request_format}")
    public Response getRedirectedPath(@PathParam("recommendationId") String reco_id, 
    		@PathParam("request_format") String format) throws Exception {
        URI url;
        DisplayDocument relDocument;
        if (con==null){
        	url = new URI("http://google.com");
        	return Response.seeOther(url).build();
        }
		try {
			String doc_id = con.getDocIdFromRecommendation(reco_id);
			relDocument = con.getDocumentBy(constants.getDocumentId(),
					doc_id);
	        String url_string = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());
	        //System.out.println("\n" + url_string);
	        //	url = new URI("http://sowiport.gesis.org/search/id/gesis-smarth-0000003281");
	        url = new URI(url_string);	
		} catch (Exception e){
			throw e;
		}
        return Response.seeOther(url).build();
    }
}	
