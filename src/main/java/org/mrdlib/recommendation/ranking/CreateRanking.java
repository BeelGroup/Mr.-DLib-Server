package org.mrdlib.recommendation.ranking;

import java.io.FileReader;
import java.nio.file.Path;
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
import org.mrdlib.database.DBConnection;
import org.mrdlib.partnerContentManager.gesis.Person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private Logger logger = LoggerFactory.getLogger(CreateRanking.class);
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
		List<DisplayDocument> documentList = new ArrayList<DisplayDocument>();
		DisplayDocument current = null;
		double value;
		int bibIdSimple = -1;
		int bibIdNormalized = -1;

		try {
			bibIdSimple = con.getBibId("simple_count", "citations", "gesis");
			bibIdNormalized = con.getBibId("simple_count_normalized_by_age_in_years", "citations", "gesis");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// get every document which has readership data
		documentList = con.getRankingValueDocuments(bibIdSimple);

		logger.debug("{}", documentList.size());
		for (int i = 0; i < documentList.size(); i++) {
			current = documentList.get(i);

			// print the current progress in 10.000 steps
			if (i % 10000 == 0)
				logger.debug(i + "/" + documentList.size());

			try {
				// get the published year of the paper
				current.setYear(
						con.getPureDocumentBy(constants.getDocumentId(), current.getDocumentId() + "").getYear());

				// if there is a year stored
				if (current.getYear() != -1) {
					// if the difference is 0, handle extra due to math error
					if (current.getYear() == Year.now().getValue())
						value = (double) current.getBibScore();
					else
						// divide through difference +1 to have a difference
						// between years identical and 1 year difference
						value = (double) current.getBibScore() / (Year.now().getValue() - current.getYear() + 1);

					// write to database
					con.writeBibliometricsInDatabase(current.getDocumentId(), bibIdNormalized, value);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.debug("finished");
	}

	/**
	 * creates the Altmetric of Readership per paper normalized by the number of
	 * authors, who contributed to it and stores it in the database
	 */
	public void createReadershipNormalizedByNumberOfAuthors() {
		List<DisplayDocument> documentList = new ArrayList<DisplayDocument>();
		DisplayDocument current = null;
		double value;
		int numberOfAuthors;
		int bibliometricIdSimple = -1;
		int bibIdNormalized = -1;

		try {
			bibliometricIdSimple = con.getBibId("simple_count", "citations", "gesis");
			bibIdNormalized = con.getBibId("simple_count_normalized_by_number_of_authors", "citations", "gesis");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// get every document which has readership data
		documentList = con.getRankingValueDocuments(bibliometricIdSimple);
		logger.debug("{}", documentList.size());

		for (int i = 0; i < documentList.size(); i++) {
			current = documentList.get(i);

			// print the current progress in 10.000 steps
			if (i % 10000 == 0)
				logger.debug(i + "/" + documentList.size());

			try {
				// get the number of authors per document
				numberOfAuthors = con.getPersonsByDocumentID(current.getDocumentId()).size();

				// only divide through non-zero values
				if (numberOfAuthors != 0) {
					value = (double) current.getBibScore() / numberOfAuthors;

					// write to database
					con.writeBibliometricsInDatabase(current.getDocumentId(), bibIdNormalized, value);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * creates the Altmetric per author over all papers he wrote and stores it
	 * in database
	 * 
	 * @throws Exception
	 */
	public void createMetricSumByAuthor(String metric, String type, String source) {
		List<Integer> personIds = new ArrayList<Integer>();
		int bibliometricId = 0;
		List<DisplayDocument> documentList = new ArrayList<DisplayDocument>();
		HashMap<String, Double> titleBibList = new HashMap<String, Double>();
		int currentId;
		double authorReadership;
		DisplayDocument currentDocument = null;
		int numberOfBib = 0;

		try {
			bibliometricId = con.getBibId(metric, type, source);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int numberOfAuthors = 3510000;
		int batchsize = 1000000;
		// con.getBiggestIdFromAuthors();

		for (int k = 0; k < numberOfAuthors; k = k + batchsize) {

			logger.debug(k + "/" + numberOfAuthors);
			logger.debug("{}", numberOfBib);
			personIds = con.getAllPersonsWithAssociatedDocumentsWithBibliometricInBatches(k, batchsize, bibliometricId);

			for (int i = 0; i < personIds.size(); i++) {
				currentId = personIds.get(i);
				authorReadership = 0;
				titleBibList.clear();

				try {
					documentList = con.getRankingValuesOfDocumentsOfSpecifiedAuthor(currentId, bibliometricId);

					for (int j = 0; j < documentList.size(); j++) {
						currentDocument = documentList.get(j);
						if (titleBibList.containsKey(currentDocument.getCleanTitle())) {
							if (currentDocument.getBibScore() > titleBibList.get(currentDocument.getCleanTitle())) {
								titleBibList.put(currentDocument.getCleanTitle(), currentDocument.getBibScore());
							}
						} else {
							titleBibList.put(currentDocument.getCleanTitle(), currentDocument.getBibScore());
						}
					}

					for (Map.Entry<String, Double> entry : titleBibList.entrySet()) {
						authorReadership = authorReadership + entry.getValue();
					}

					con.writeAuthorBibliometricsInDatabase(currentId, bibliometricId, authorReadership);
					numberOfBib++;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		logger.debug("created: " + numberOfBib);
	}

	/**
	 * creates the Altmetric of an h-index of every author
	 * 
	 */
	public void createHIndexByAuthor() {
		HashMap<String, Double> titleBibList = new HashMap<String, Double>();
		List<DisplayDocument> documentList = new ArrayList<DisplayDocument>();
		List<Person> personList = new ArrayList<Person>();
		Person currentAuthor = null;
		double hIndex = 0;
		int bibliometricIdSimple = -1;
		int bibliometricIdHIndex = -1;
		int hadHIndex = 0;
		DisplayDocument currentDocument = new DisplayDocument();

		try {
			bibliometricIdSimple = con.getBibId("simple_count", "citations", "gesis");
			bibliometricIdHIndex = con.getBibId("h-index", "citations", "gesis");
		} catch (Exception e) {
			e.printStackTrace();
		}

		int[] range = con.getRangeFromBibAuthors(bibliometricIdSimple);
		logger.debug(range[0] + ", " + range[1]);

		for (int k = range[0]; k <= range[1]; k = k + 2000) {
			personList = con.getAllPersonsInBatchesIfBibliometricId(bibliometricIdSimple, k, 2000);

			logger.debug((k - range[0]) + "/" + (range[1] - range[0]));

			for (int i = 0; i < personList.size(); i++) {
				documentList.clear();
				titleBibList.clear();
				currentAuthor = personList.get(i);

				documentList = con.getRankingValuesOfAuthorPerDocument(bibliometricIdSimple, currentAuthor.getId());

				for (int j = 0; j < documentList.size(); j++) {
					currentDocument = documentList.get(j);
					if (titleBibList.containsKey(currentDocument.getCleanTitle())) {
						if (currentDocument.getBibScore() > titleBibList.get(currentDocument.getCleanTitle())) {
							titleBibList.put(currentDocument.getCleanTitle(), currentDocument.getBibScore());
						}
					} else {
						titleBibList.put(currentDocument.getCleanTitle(), currentDocument.getBibScore());
					}
				}

				hIndex = hIndex(titleBibList.values().toArray(new Double[0]));

				if (hIndex != 0) {
					con.writeAuthorBibliometricsInDatabase(currentAuthor.getId(), bibliometricIdHIndex, hIndex);
					hadHIndex++;
				}

			}
		}
		logger.debug("inserted:" + hadHIndex);
	}

	public void fixMetric(int bibliometricIdToFix, int bibliometricIdToCalc) {
		List<Integer> docIds = new ArrayList<Integer>();
		int bibDocId;
		String query = "";
		double value;
		int count = 0;

		docIds = con.getAllDocumentsWithBadAuthorsAndSpecificBibliometric(bibliometricIdToFix);

		for (int i = 0; i < docIds.size(); i++) {
			bibDocId = con.getBibDocId(docIds.get(i), bibliometricIdToFix);

			value = con.getBibDocAvg(docIds.get(i), bibliometricIdToCalc);

			if (value <= 0) {
				query = "DELETE FROM " + constants.getBibDocuments() + " WHERE "
						+ constants.getBibliometricDocumentsId() + " = " + bibDocId;
				count++;
			} else {
				query = "UPDATE " + constants.getBibDocuments() + " SET " + constants.getMetricValue() + " = " + value
						+ " WHERE " + constants.getBibliometricDocumentsId() + " = " + bibDocId;

			}
			//logger.debug(query);
			con.executeUpdate(query);
		}
		logger.debug("Deleted: " + count + "/" + docIds.size());
	}

	public double hIndex(Double[] citations) {
		Arrays.sort(citations);

		double result = 0;
		for (int i = 0; i < citations.length; i++) {
			double smaller = Math.min(citations[i], citations.length - i);
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
			logger.debug(path.toString());
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		CreateRanking cr = new CreateRanking();
		cr.fixMetric(16, 14);
	}

}
