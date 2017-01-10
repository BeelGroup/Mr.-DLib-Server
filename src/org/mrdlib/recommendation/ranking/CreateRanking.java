package org.mrdlib.recommendation.ranking;

import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mrdlib.api.manager.Constants;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.partnerContentManager.gesis.Person;

/**
 * 
 * @author Millah
 * 
 *         This class creates the different ranking values based on Alt-, and
 *         Bibliometrics and stores them in the database. The calculcations are
 *         done offline.
 *
 */

public class CreateRanking {

	DBConnection con = null;
	Constants constants = null;

	public CreateRanking() {
		try {
			// setup a database connection from a jar file
			con = new DBConnection("jar");
			constants = new Constants();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * creates the Altmetric of Readership per paper normalized by the age of
	 * the paper in years and stores it in the database
	 */
	public void createReadershipNormalizedByAge() {
		DocumentSet documentSet = new DocumentSet(constants);
		DisplayDocument current = null;
		double value;

		// get every document which has readership data
		documentSet = con.getRankingValueDocuments("simple_count", "readers", "mendeley");
		System.out.println(documentSet.getSize());
		for (int i = 0; i < documentSet.getSize(); i++) {
			current = documentSet.getDisplayDocument(i);

			// print the current progress in 10.000 steps
			if (i % 10000 == 0)
				System.out.println(i + "/" + documentSet.getSize());

			try {
				// get the published year of the paper
				current.setYear(con.getDocumentBy(constants.getDocumentId(), current.getDocumentId() + "").getYear());

				// if there is a year stored
				if (current.getYear() != -1) {
					// if the difference is 0, handle extra due to math error
					if (current.getYear() == Year.now().getValue())
						value = (double) current.getBibScore();
					else
						// divide through difference +1 to avoid math error
						value = (double) current.getBibScore() / (Year.now().getValue() - current.getYear() + 1);

					// write to database
					con.writeBibliometricsInDatabase(current.getDocumentId(), "normalizedByAge", "readers", "", "",
							value, "mendeley");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * creates the Altmetric of Readership per paper normalized by the number of
	 * authors, who contributed to it and stores it in the database
	 */
	public void createReadershipNormalizedByNumberOfAuthors() {
		DocumentSet documentSet = new DocumentSet(constants);
		DisplayDocument current = null;
		double value;
		int numberOfAuthors;

		// get every document which has readership data
		documentSet = con.getRankingValueDocuments("simple_count", "readers", "mendeley");
		System.out.println(documentSet.getSize());

		for (int i = 0; i < documentSet.getSize(); i++) {
			current = documentSet.getDisplayDocument(i);

			// print the current progress in 10.000 steps
			if (i % 10000 == 0)
				System.out.println(i + "/" + documentSet.getSize());

			try {
				// get the number of authors per document
				numberOfAuthors = con.getPersonsByDocumentID(current.getDocumentId()).size();

				// only divide through non-zero values
				if (numberOfAuthors != 0) {
					value = (double) current.getBibScore() / numberOfAuthors;

					// write to database
					con.writeBibliometricsInDatabase(current.getDocumentId(), "normalizedByNumberOfAuthors", "readers",
							"", "", value, "mendeley");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * creates the Altmetric per author over all papers he wrote
	 * and stores it in database
	 * 
	 * under progress -> leads to errors
	 * 
	 * @throws Exception
	 */
	public void createMetricByAuthor(String metric, String type, String source) {
		List<Person> personList = new ArrayList<Person>();
		DocumentSet documentSet = new DocumentSet(constants);
		Person currentAuthor = null;
		double authorReadership;
		DisplayDocument currentDocument = null;
		double documentReadership;
		int numberOfBib = 0;

		try {
			documentSet.setBibliometricId(con.getBibId(metric, type, source));
		} catch (Exception e) {
			e.printStackTrace();
		}

		int numberOfAuthors = 3510000;
		// con.getBiggestIdFromAuthors();

		for (int k = 2815780; k < numberOfAuthors; k = k + 500) {

			System.out.println(k + "/" + numberOfAuthors);
			System.out.println(numberOfBib);
			personList = con.getAllPersonsInBatches(k, 500);

			for (int i = 0; i < personList.size(); i++) {
				currentAuthor = personList.get(i);
				authorReadership = 0;

				try {
					documentSet.setDocumentList(con.getDocumentsByPersonId(currentAuthor.getId()).getDocumentList());

					for (int j = 0; j < documentSet.getSize(); j++) {
						currentDocument = documentSet.getDisplayDocument(j);
						documentReadership = con
								.getRankingValue(currentDocument.getDocumentId(), documentSet.getBibliometricId())
								.getBibScore();

						if (documentReadership != -1) {
							authorReadership = authorReadership + documentReadership;
						}
					}

					if (authorReadership != 0) {
						con.writeAuthorBibliometricsInDatabase(currentAuthor.getId(), documentSet.getBibliometricId(), authorReadership);
						numberOfBib++;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("created: " + numberOfBib);
	}

	/**
	 * creates the Altmetric of Readership per paper from the sum of the
	 * readership of all authors
	 * 
	 * under progress -> leads to errors
	 */
	public void createReadershipSumFromAuthors() {
		List<DisplayDocument> documentDataList = new ArrayList<DisplayDocument>();
		List<Person> personList = new ArrayList<Person>();
		DisplayDocument currentDocument = null;
		Person currentPerson = null;
		int sumReadership = 0;

		int numberOfDocuments = con.getBiggestIdFromDocuments();

		for (int k = 0; k < numberOfDocuments; k = k + 500) {
			documentDataList = con.getDocumentDataInBatches(k, k + 500);
			System.out.println(k + "/" + numberOfDocuments);

			try {
				for (int i = 0; i < documentDataList.size(); i++) {
					currentDocument = documentDataList.get(i);
					personList = con.getPersonsByDocumentID(currentDocument.getDocumentId());
					sumReadership = 0;

					for (int j = 0; j < personList.size(); j++) {
						currentPerson = personList.get(j);
						currentPerson.setRankingValue(con.getRankingValueAuthor(currentPerson.getId() + "",
								"simple_count", "readers", "mendeley"));
						sumReadership = sumReadership + (int) currentPerson.getRankingValue();
					}
					if (sumReadership != 0)
						con.writeBibliometricsInDatabase(currentDocument.getDocumentId(), "sumFromAuthors", "readers",
								"", "", sumReadership, "mendeley");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * creates the Altmetric of an h-index of every author
	 * 
	 * under progress -> leads to errors
	 */
	public void createHIndexByAuthor() {
		Map<String, Integer> documentCitations = new HashMap<String, Integer>();
		Map<String, Integer> documentCitations2 = new HashMap<String, Integer>();
		List<Person> personList = new ArrayList<Person>();
		Person currentAuthor = null;
		int hIndex = 0;
		int bibliometricId = 0;
		int hadHIndex = 0;

		try {
			bibliometricId = con.getBibId("simple_count", "readers", "mendeley");
		} catch (Exception e) {
			e.printStackTrace();
		}

		int numberOfAuthors = con.getBiggestIdFromBibAuthors(bibliometricId);
		System.out.println(numberOfAuthors);

		for (int k = 1; k < numberOfAuthors; k = k + 500) {
			personList = con.getAllPersonsInBatchesIfBibliometricId(k, 500);

			System.out.println(k + "/" + numberOfAuthors);

			for (int i = 0; i < personList.size(); i++) {
				documentCitations.clear();
				documentCitations2.clear();
				currentAuthor = personList.get(i);

				documentCitations = con.getRankingValuesOfAuthorPerDocument(bibliometricId, currentAuthor.getId());

				for (Map.Entry<String, Integer> entry : documentCitations.entrySet()) {
					String key = entry.getKey();
					Integer value = entry.getValue();
					documentCitations2.put(getCleanTitle(key), value);
				}

				hIndex = hIndex(documentCitations2.values().toArray(new Integer[0]));

				con.writeAuthorBibliometricsInDatabase(currentAuthor.getId(), "h-index", "readers", "mendeley", hIndex);
				hadHIndex++;

			}
			System.out.println("inserted:" + hadHIndex);
		}
	}

	public int hIndex(Integer[] citations) {
		Arrays.sort(citations);

		int result = 0;
		for (int i = 0; i < citations.length; i++) {
			int smaller = Math.min(citations[i], citations.length - i);
			result = Math.max(result, smaller);
		}

		return result;
	}

	public String getCleanTitle(String s) {
		String cleanTitle = "";
		cleanTitle = s.replaceAll("[^a-zA-Z]", "");
		cleanTitle = cleanTitle.toLowerCase();
		return cleanTitle;
	}

	public void createCitationsForGesis(Path path) {
		JSONParser parser = new JSONParser();

		try {
			// get the file to parse
			Object obj = parser.parse(new FileReader(path.toString()));

			// get the json object from the file
			JSONObject jsonObject = (JSONObject) obj;
			jsonObject = (JSONObject) jsonObject.get("response");
			JSONArray arr = (JSONArray) jsonObject.get("docs");

			Iterator i = arr.iterator();

			while (i.hasNext()) {
				jsonObject = (JSONObject) i.next();
				int citation = (int) (long) jsonObject.get("citation_count_int");
				String gesisId = (String) jsonObject.get("id");

				String id = con.getDocumentBy(constants.getIdOriginal(), gesisId).getDocumentId();

				con.writeBibliometricsInDatabase(id, "simple_count", "citations", citation, "gesis");
			}

		} catch (Exception e) {
			System.out.println(path.toString());
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		CreateRanking cr = new CreateRanking();
		cr.createMetricByAuthor("simple_count", "citations", "gesis");
	}

}
