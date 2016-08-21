package org.mrdlib.MendeleyCrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mrdlib.DocumentData;
import org.mrdlib.Readership;
import org.mrdlib.database.DBConnection;
import org.mrdlib.oauth.OAuth2Client;

public class MendeleyConnection {

	private String accessToken;
	private DBConnection con;
	OAuth2Client oclient = new OAuth2Client();
	private int countAll = 0;
	private int countSuc = 0;

	public MendeleyConnection() throws Exception {
		con = new DBConnection();
		accessToken = oclient.getAccessToken();
	}

	public void getReadership() throws SQLException {
		List<DocumentData> documentDataList = new ArrayList<DocumentData>();
		DocumentData documentData = new DocumentData();
		String mendeleyId = null;
		HttpGet httpget = new HttpGet();
		URL url = null;
		String nullFragment = null;
		JSONObject jsonObject = null;
		JSONArray documents = null;
		JSONObject document = null;
		JSONObject highlights = null;
		JSONArray titleJson = null;
		String title = null;
		HttpClient httpclient = HttpClientBuilder.create().build();
		String data = null;
		URI uri = null;
		/*
		 * PoolingHttpClientConnectionManager cm = new
		 * PoolingHttpClientConnectionManager(); cm.setMaxTotal(200); //
		 * Increase default max connection per route to 20
		 * cm.setDefaultMaxPerRoute(20); // Increase max connections for
		 * localhost:80 to 50 HttpHost localhost = new HttpHost("locahost", 80);
		 * cm.setMaxPerRoute(new HttpRoute(localhost), 50);
		 */
			
		int numberOfDocuments = con.getBiggestIdFromDocuments();
			
			
		for (int k = 0; k < numberOfDocuments; k = k + 1000000) {
			documentDataList = con.getMillionDocumentData(k);

			for (int i = 0; i < documentDataList.size(); i++) {
				documentData = documentDataList.get(i);
				String current = documentData.getTitle();
				// String current = "Introducing Mr. DLib, a Machine-readable Digital Library";

				current = current.replaceAll("[^_a-zA-Z0-9 .]", "");
				String urlString = "https://api.mendeley.com/catalog?title=" + current;

				try {
					url = new URL(urlString);
					uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
					httpget = new HttpGet(uri);
					httpget.addHeader("Authorization", "Bearer " + accessToken);
					HttpResponse response = httpclient.execute(httpget);
					data = EntityUtils.toString(response.getEntity());

					if (data.isEmpty())
						break;

					jsonObject = (JSONObject) JSONValue.parse(data);
					documents = (JSONArray) jsonObject.get("documents");

					Iterator j = documents.iterator();
					while (j.hasNext()) {
						document = (JSONObject) j.next();
						if (document.toString().contains("highlights")) {
							highlights = (JSONObject) document.get("highlights");
							titleJson = (JSONArray) highlights.get("title");
							title = (String) titleJson.get(0);
							if (title.contains("<strong>") || title.contains("<\\/strong>"))
								title = title.replaceAll("<strong>|<\\/strong>", "");
							if (calculateTitleClean(current).equals(calculateTitleClean(title))) {
								mendeleyId = (String) document.get("id");
								countSuc++;
								writeToFile(documentData, getReadershipData(mendeleyId));
								break;
							}
						}
					}
					httpget.releaseConnection();
					countAll++;
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(uri);
					System.out.println(data);
				}
			}
		}
	}

	private String getReadershipData(String mendeleyId) {
		String urlString = "https://api.mendeley.com/catalog/" + mendeleyId + "?view=stats";
		HttpClient httpclient = HttpClientBuilder.create().build();
		String data = null;
		String nullFragment = null;

		try {
			URL url = new URL(urlString);
			URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);

			HttpGet httpget = new HttpGet(uri);
			httpget.addHeader("Authorization", "Bearer " + accessToken);
			HttpResponse response = httpclient.execute(httpget);
			data = EntityUtils.toString(response.getEntity());

			// System.out.println("Title: " + title);
			// System.out.println("MendeleyId: " + mendeleyId);
			// System.out.println("Data: " + data);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	private static void writeToFile(DocumentData documentData, String input) {
		JSONObject jsonObject = (JSONObject) JSONValue.parse(input);
		jsonObject.put("timestamp", new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new java.util.Date()));
		input = jsonObject.toString();

		String dirName = "MendeleyData" + File.separator + (int) (Math.floor(documentData.getId() / 10000)) + "";
		String path = dirName + File.separator + documentData.getId() + " " + documentData.getOriginalId() + ".txt";

		try {
			new File(dirName).mkdirs();
			FileWriter writer = new FileWriter(path, false);
			writer.write(input);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String calculateTitleClean(String s) {
		s = s.replaceAll("[^a-zA-Z0-9]", "");
		s = s.toLowerCase();
		return s;
	}

	public static void main(String[] args) throws Exception {
		MendeleyConnection mcon = new MendeleyConnection();
		mcon.getReadership();
		System.out.println(mcon.countSuc + "/" + mcon.countAll);
		// writeToFile(new DocumentData("keks", 0, "keks"), "\nBlub");
	}

}
