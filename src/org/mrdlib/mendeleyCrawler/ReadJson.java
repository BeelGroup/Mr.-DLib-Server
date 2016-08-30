package org.mrdlib.mendeleyCrawler;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mrdlib.database.DBConnection;

/**
 * @author Millah
 * 
 *         This class processes the retrieved data from Mendeley
 */
public class ReadJson {

	private Config mconfig;
	private DBConnection con;

	/**
	 * open a database connection
	 */
	public ReadJson() {
		mconfig = new Config();
		try {
			con = new DBConnection("jar");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * get the statistic data from mendeley of an already found document
	 * 
	 * @param id, mrdlib id
	 * @param category, category of the jsonObject (and readership)
	 * @param jsonObj, the json object to process
	 */
	private void iterateJsonObject(int id, String category, JSONObject jsonObj) {
		
		//transform the categories to the enums in database
		if (category.equals("reader_count_by_country"))
			category = "country";
		if (category.equals("reader_count_by_subdiscipline"))
			category = "subdiscipline";
		if (category.equals("reader_count_by_academic_status"))
			category = "academic_status";
		if (category.equals("reader_count_by_subject_area"))
			category = "subject_area";
		if (category.equals("reader_count_by_user_role"))
			category = "user_role";

		//for each key inside this json object
		for (Object key : jsonObj.keySet()) {

			//get both name and value
			String keyStr = (String) key;
			Object keyvalue = jsonObj.get(keyStr);

			//if they have further subgroups, do recursion
			if (keyvalue instanceof JSONObject)
				iterateJsonObject(id, category, (JSONObject) keyvalue);
			//at the end of recursion get the final values
			else {
				System.out.println(category);
				System.out.println("key: " + keyStr + " value: " + keyvalue);
				
				//write the identifiers in the database
				if (category.equals("identifiers")) {
					con.writeIdentifiersInDatabase(id, keyStr.toString(), keyvalue.toString());
				//write the metric in the database
				} else
					con.writeBibliometricsInDatabase(id, "simple_count", "readers", category, keyStr.toString(),
							Integer.parseInt(keyvalue.toString()), "mendeley");
			}
		}
	}

	/**
	 * wrapper to get the readership by category and enable recursion
	 * 
	 * @param id,
	 *            mrdlib id
	 * @param jsonObject,
	 *            the part of json object relevant for the category
	 * @param category,
	 *            the category of the jsonObject
	 */
	private void getReaderShipByCategory(int id, JSONObject jsonObject, String category) {
		JSONObject jObject = (JSONObject) jsonObject.get(category);
		iterateJsonObject(id, category, jObject);
	}

	/**
	 * processes the Json file, get the external ids and the readership
	 * categories and stores them in the database
	 * 
	 * @param path,
	 *            the path of the Json file
	 */
	public void processJson(Path path) {
		JSONParser parser = new JSONParser();
		String filename;
		int id;
		int readerCount;

		try {
			//get the file to parse
			Object obj = parser.parse(new FileReader(path.toString()));

			//get the json object from the file
			JSONObject jsonObject = (JSONObject) obj;
			
			//get the mrdlib id from the filename
			filename = path.getFileName().toString();
			id = Integer.parseInt(filename.substring(0, filename.indexOf(' ')));
			//get the absolute readercount and write it to database
			readerCount = Integer.parseInt(jsonObject.get("reader_count").toString());
			con.writeBibliometricsInDatabase(id, "simple_count", "readers", "all", null, readerCount, "mendeley");
			
			//get the readercount of the subcategories and write them to database
			getReaderShipByCategory(id, jsonObject, "reader_count_by_country");
			getReaderShipByCategory(id, jsonObject, "reader_count_by_subdiscipline");
			getReaderShipByCategory(id, jsonObject, "reader_count_by_academic_status");
			getReaderShipByCategory(id, jsonObject, "reader_count_by_subject_area");
			getReaderShipByCategory(id, jsonObject, "reader_count_by_user_role");
			getReaderShipByCategory(id, jsonObject, "identifiers");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * get every file of the stored mendeley responses and perform processJson
	 * on each of them
	 */
	public void readStoredMendeleyJsonFiles() {
		try {
			Files.walk(Paths.get(mconfig.getPathOfDownload()))
					.filter((p) -> !p.toFile().isDirectory() && p.toFile().getAbsolutePath().endsWith(".txt"))
					.forEach(p -> this.processJson(p));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * processes the mendeley response files
	 */
	public static void main(String[] args) throws Exception {
		ReadJson rJson = new ReadJson();
		rJson.readStoredMendeleyJsonFiles();
	}
}
