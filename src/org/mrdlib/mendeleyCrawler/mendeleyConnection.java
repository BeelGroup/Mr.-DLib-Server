package org.mrdlib.mendeleyCrawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mrdlib.Readership;
import org.mrdlib.database.DBConnection;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

public class mendeleyConnection {

	private OAuthAccessor client;
	private String access_token;
	private String request_token;
	private DBConnection con;

	public mendeleyConnection() {
		con = new DBConnection();
	}

	public String convertToAccessToken(String request_token) {
		ArrayList<Map.Entry<String, String>> params = new ArrayList<Map.Entry<String, String>>();
		OAuthClient oclient = new OAuthClient(new HttpClient4());
		OAuthAccessor accessor = client;
		params.add(new OAuth.Parameter("oauth_token", request_token));
		try {
			OAuthMessage omessage = oclient.invoke(accessor, "POST", accessor.consumer.serviceProvider.accessTokenURL,
					params);
			return omessage.getParameter("oauth_token");
		} catch (OAuthProblemException e) {
			e.printStackTrace();
			System.out.println("Ac: " + accessor.accessToken + "req: " + accessor.requestToken + "sec: "
					+ accessor.tokenSecret + "con: " + accessor.consumer + "str: " + accessor.toString());
			return "";
		} catch (Exception ioe) {
			ioe.printStackTrace();
			return "";
		}
	}

	public OAuthAccessor defaultClient() {
		String callbackUrl = "www.mr-dlib.org";
		String consumerKey = "id";
		String consumerSecret = "secret";
		String reqUrl = "https://www.mendeley.com/oauth/request_token/";
		String authzUrl = "https://api-oauth2.mendeley.com/oauth/authorize/";
		String accessUrl = "https://www.mendeley.com/oauth/access_token/";
		OAuthServiceProvider provider = new OAuthServiceProvider(reqUrl, authzUrl, accessUrl);
		OAuthConsumer consumer = new OAuthConsumer(callbackUrl, consumerKey, consumerSecret, provider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);

		OAuthClient oaclient = new OAuthClient(new HttpClient4());

		try {
			oaclient.getRequestToken(accessor);
			request_token = accessor.requestToken;
		} catch (OAuthProblemException e) {
			System.out.println("hello default client");
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.out.println(e.getProblem());
			System.out.println(e.getCause());
			System.out.println(e.toString());
			System.out.println(e.getHttpStatusCode());
			System.out.println(e.getSuppressed());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OAuthException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return accessor;
	}

	public HashMap<String, Readership> getReadership() {
		HashMap<String, Readership> map = new HashMap<String, Readership>();
		List<String> documentTitles = new ArrayList<>();
		Readership readership = null;
		String mendeleyId = null;
		int score = 0;
		HttpPost httppost = new HttpPost();
		URL url = null;
		String nullFragment = null;
		JSONObject jsonObject = null;

		documentTitles = con.getAllDocumentTitles();

		for (int i = 0; i < documentTitles.size(); i++) {
			String current = documentTitles.get(i);

			HttpClient httpclient = HttpClientBuilder.create().build();
			String urlString = "https://api.mendeley.com/catalog?title=" + current;

			client = defaultClient();
			access_token = convertToAccessToken(client.requestToken);
			System.out.println(client.consumer.serviceProvider.userAuthorizationURL + "?oauth_token="
					+ client.requestToken + "&oauth_callback=" + client.consumer.callbackURL);
			System.out.println("-----");
			System.out.println("Ac: " + client.accessToken + " req: " + client.requestToken + " sec: "
					+ client.tokenSecret + " con: " + client.consumer + " str: " + client.toString());
			System.out.println("-----");
			System.out.println("Access Token: " + access_token);
			System.out.println("Request Token: " + request_token);

			try {
				url = new URL(urlString);
				URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
				httppost = new HttpPost(uri);
				httppost.addHeader("Authorization", "Bearer " + client.requestToken);
				// HttpGet request = new
				// HttpGet("https://api.mendeley.com/oauth/token?grant_type=client_credentials&scope=all&client_id=3367&client_secret=AOlZEEKVRHyutUo5");

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("action", "getjson"));

				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				String data = EntityUtils.toString(response.getEntity());

				System.out.println(current + ":  ");
				System.out.println(data);
				jsonObject = (JSONObject) JSONValue.parse(data);
				mendeleyId = (String) jsonObject.get("id");
				score = (Integer) jsonObject.get("score");

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				urlString = "https://api.mendeley.com/catalog/" + mendeleyId + "?view=stats";
				url = new URL(urlString);
				URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
				httppost = new HttpPost(uri);
				httppost.addHeader("Authorization", "Bearer " + client.requestToken);
				// httppost.addHeader("Accept", );

				List<NameValuePair> nameValuePairsStats = new ArrayList<NameValuePair>(2);
				nameValuePairsStats.add(new BasicNameValuePair("action", "getjson"));

				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairsStats));
				HttpResponse response = httpclient.execute(httppost);
				String data = EntityUtils.toString(response.getEntity());

				System.out.println("Title: " + current);
				System.out.println("Score: " + score);
				System.out.println("Data: " + data);
				System.out.println("---------------------------------------");

			} catch (Exception e) {
				e.printStackTrace();
			}
			map.put(current, readership);
		}
		return map;
	}

	public static void main(String[] args) {
		mendeleyConnection mcon = new mendeleyConnection();
		mcon.getReadership();

	}

}
