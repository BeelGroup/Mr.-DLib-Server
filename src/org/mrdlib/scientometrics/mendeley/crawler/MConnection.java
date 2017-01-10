package org.mrdlib.scientometrics.mendeley.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.database.DBConnection;
import org.mrdlib.scientometrics.mendeley.oauth.OAuth2Client;

/**
 * @author Millah
 * 
 *         This class handles the communication with Mendeley
 */
public class MConnection {

	private String accessToken;
	private Long expiresIn;
	private DBConnection con;
	OAuth2Client oclient = new OAuth2Client();
	private int countAll = 0;
	private int countSuc = 0;
	private Config mconfig;

	/**
	 * opens a database connection (without pool), initialize the mendeley
	 * config and retrieve the first accessToken
	 */
	public MConnection() {
		try {
			// get database connection
			con = new DBConnection("jar");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// get acessToken and expire time
		accessToken = oclient.getAccessToken();
		expiresIn = (System.currentTimeMillis() / 1000) + 3600;
		//initialize the config
		mconfig = new Config();
	}

	public void getReadership() throws SQLException {
		List<DisplayDocument> documentDataList = new ArrayList<DisplayDocument>();
		DisplayDocument documentData = new DisplayDocument();
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
		int lastSuccessId = mconfig.getLastSuccessfullId();

		//get the number of documents for batch processing
		int numberOfDocuments = con.getBiggestIdFromDocuments();

		//if the process was finished, stop
		if (lastSuccessId >= con.getBiggestIdFromDocuments()) {
			//mconfig.writeMendeleyCrawlingProcessToConfigFile(0);
			//lastSuccessId = 0;
			return;
		}

		//iterate over every document in database in batch size steps
		for (int k = lastSuccessId; k < numberOfDocuments; k = k + mconfig.getBatchSize()) {
			//get the DocumentDataList in batch size
			documentDataList = con.getDocumentDataInBatches(k, mconfig.getBatchSize());

			//iterate over each DocumentData Entry to reqeust it in mendeley
			for (int i = 0; i < documentDataList.size(); i++) {
				//if the accesToken expires, renew it
				if (expiresIn - 60 <= (System.currentTimeMillis() / 1000)) {
					accessToken = oclient.getAccessToken();
					expiresIn = (System.currentTimeMillis() / 1000) + 3600;
				}
				//get current DocumentData
				documentData = documentDataList.get(i);
				String current = documentData.getTitle();

				//replace special characters to match mendeley needs
				current = current.replaceAll("[^_a-zA-Z0-9 .]", "");
				
				//formulate the mendeley query 
				String urlString = "https://api.mendeley.com/catalog?title=" + current;

				try {
					//request mendeley
					url = new URL(urlString);
					uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
					httpget = new HttpGet(uri);
					httpget.addHeader("Authorization", "Bearer " + accessToken);
					HttpResponse response = httpclient.execute(httpget);
					data = EntityUtils.toString(response.getEntity());
					
					//if mendeley has no hit, skip this document
					if (data.isEmpty())
						break;

					//parse the json response and search for the title
					jsonObject = (JSONObject) JSONValue.parse(data);
					documents = (JSONArray) jsonObject.get("documents");

					Iterator j = documents.iterator();
					while (j.hasNext()) {
						document = (JSONObject) j.next();
						if (document.toString().contains("highlights")) {
							highlights = (JSONObject) document.get("highlights");
							titleJson = (JSONArray) highlights.get("title");
							title = (String) titleJson.get(0);
							
							//eliminate the <strong> text for comparison
							if (title.contains("<strong>") || title.contains("<\\/strong>"))
								title = title.replaceAll("<strong>|<\\/strong>", "");
							
							//eliminate special characters for better comparison and search for matching title
							if (documentData.getCleanTitle().equals(documentData.getCleanTitle())) {
								
								//if it matches, get the mendeley id for second request
								mendeleyId = (String) document.get("id");
								countSuc++;
								
								//request the statistics of the matching document
								writeMendeleyStatsToFile(documentData, getStatisticData(mendeleyId));
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
			//write progress to config file
			mconfig.writeMendeleyCrawlingProcessToConfigFile(k + mconfig.getBatchSize());
		}
	}

	/**
	 * get the statistic data from mendeley of an already found document
	 * @param mendeleyId
	 * @return JsonResponse from mendeley as String
	 */
	private String getStatisticData(String mendeleyId) {
		
		//formulate statistic query for mendeley request
		String urlString = "https://api.mendeley.com/catalog/" + mendeleyId + "?view=stats";
		
		//send request
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

		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * writes the Mendeley Response with corresponding DocumentData and a timestamp to a file for later processing
	 * @param documentData, the documentData associated with the mendeley answer, used for filename
	 * @param input, the String which will be written to the file
	 */
	private void writeMendeleyStatsToFile(DisplayDocument displayDocument, String input) {
		
		//get the data as json object
		JSONObject jsonObject = (JSONObject) JSONValue.parse(input);
		
		//get the current timestamp and add to the json Object
		jsonObject.put("timestamp", new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new java.util.Date()));
		
		//convert json object back to string
		input = jsonObject.toString();

		//generate the path of the file to write to (path from the config file + folders for each 10.000 document)
		String dirName = mconfig.getPathOfDownload() + File.separator + (int) (Math.floor(Integer.parseInt(displayDocument.getDocumentId()) / 10000))
				+ "";
		//generate the file name in the format "mrDlibId gesisId"
		String path = dirName + File.separator + displayDocument.getDocumentId() + " " + displayDocument.getOriginalDocumentId() + ".txt";

		//produce and write in the file
		try {
			new File(dirName).mkdirs();
			FileWriter writer = new FileWriter(path, false);
			writer.write(input);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		MConnection mcon = new MConnection();
		mcon.getReadership();
		System.out.println(mcon.countSuc + "/" + mcon.countAll);
		// writeToFile(new DocumentData("keks", 0, "keks"), "\nBlub");
	}

}
