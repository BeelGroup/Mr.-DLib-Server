package org.mrdlib.recommendation.framework.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.mrdlib.api.manager.Constants;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.partnerContentManager.core.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.parser.ParseException;

import com.owlike.genson.Genson;

public class SimilarArticleConnection {

	String apiKey;
	HttpClient httpclient;
	Constants constants;
	Genson genson;

	public SimilarArticleConnection() {
		constants = new Constants();
		apiKey = constants.getCoreAPIKey();
		httpclient = HttpClients.createDefault();
		genson = new Genson();

	}

	public static void main(String[] args) throws ParseException {

		SimilarArticleConnection con = new SimilarArticleConnection();
		try {

			String json = con.getSimilarArticles("9192112");
			List<SimilarDocument> docs = con.parseJSONFromGetArticle(json);
			for (SimilarDocument doc : docs)
				System.out.println(doc.getId() + " : " + doc.getTitle());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException f) {
			f.printStackTrace();
		}

	}

	public List<SimilarDocument> parseJSONFromGetArticle(String json) {
		SimilarDocumentSearch similardocuments = genson.deserialize(json, SimilarDocumentSearch.class);
		GetArticleData x = similardocuments.getData();
		if (x == null) {
			// This happens when CORE API gives back the following JSON
			// {"status":"Not found","data":null}
			// we return an empty similarities list
			return new ArrayList<SimilarDocument>();
		} else
			return x.getSimilarities();
	}

	public List<PostTitleData> parseJSONFromPostTitle(String json) {
		TitleSearch similardocuments = genson.deserialize(json, TitleSearch.class);
		return similardocuments.getData();
	}

	public String getSimilarArticles(String documentId) throws ClientProtocolException, IOException {
		String uri = "https://core.ac.uk/api-v2/articles/get/" + documentId
				+ "?metadata=false&fulltext=false&citations=false&similar=true&duplicate=false&urls=false&faithfulMetadata=false&apiKey="
				+ apiKey;
		HttpGet http = new HttpGet(uri);
		HttpResponse response = httpclient.execute(http);
		HttpEntity entity = response.getEntity();
		return getJSONFromEntity(entity);
	}

	public String getJSONFromEntity(HttpEntity entity) throws UnsupportedOperationException, IOException {
		String str = "", output = "";
		if (entity != null) {
			InputStream instream = entity.getContent();
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(instream));
				while ((output = br.readLine()) != null) {
					str += output;
				}
				// do something useful
			} finally {
				instream.close();
			}
		}
		return str;
	}

	public String searchByTitle(String title) throws ClientProtocolException, IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(
				"https://core.ac.uk:443/api-v2/articles/similar?limit=10&metadata=true&fulltext=false&citations=false&similar=false&duplicate=false&urls=false&faithfulMetadata=false&apiKey=VS69J25GmeWsZiAjMHTvlX3oh1ntONQw");

		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
		params.add(new BasicNameValuePair("text", title));
		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		return getJSONFromEntity(entity);
	}

	public DocumentSet convertToMDLSet(List<? extends ExternalDocumentRepresentation> externalRepresentationSet,
			DocumentSet containerSet) {
		if(externalRepresentationSet==null){
			return containerSet;
		}
		for (ExternalDocumentRepresentation item : externalRepresentationSet) {
			containerSet.addDocument(item.convertToMDLDocument());
		}
		if (containerSet.getSize() > 0) {
			String url = containerSet.getDisplayDocument(0).getClickUrl();
			if (url != null) {
				String recommendationSetId;
				String algorithmId;
				try {
					algorithmId = url.split("algorithmId=")[1].split("&")[0];
				} catch (NullPointerException e) {
					algorithmId = null;
				}
				try {
					recommendationSetId = url.split("recSetID=")[1].split("&")[0];
				} catch (NullPointerException e) {
					recommendationSetId = null;
				}
				containerSet.setExternalAlgorithmId(algorithmId);
				containerSet.setExternalRecommendationSetId(recommendationSetId);
			}
		}

		return containerSet;
	}

}