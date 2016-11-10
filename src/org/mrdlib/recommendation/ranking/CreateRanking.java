package org.mrdlib.recommendation.ranking;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.mrdlib.api.manager.Constants;
import org.mrdlib.api.response.DisplayDocument;
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
		List<DisplayDocument> documentDataList = new ArrayList<DisplayDocument>();
		DisplayDocument current = null;
		double value;

		//get every document which has readership data
		documentDataList = con.getRankingValueDocuments("simple_count", "readers", "mendeley");
		System.out.println(documentDataList.size());
		for (int i = 0; i < documentDataList.size(); i++) {
			current = documentDataList.get(i);
			
			//print the current progress in 10.000 steps
			if (i % 10000 == 0)
				System.out.println(i + "/" + documentDataList.size());

			try {
				//get the published year of the paper
				current.setYear(
						con.getDocumentDataBy(constants.getDocumentId(), current.getDocumentId() + "").getYear());

				//if there is a year stored
				if (current.getYear() != -1) {
					//if the difference is 0, handle extra due to math error
					if (current.getYear() == Year.now().getValue())
						value = (double) current.getRankingValue();
					else
						//divide through difference +1 to avoid math error
						value = (double) current.getRankingValue() / (Year.now().getValue() - current.getYear() + 1);

					//write to database
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
		List<DisplayDocument> documentDataList = new ArrayList<DisplayDocument>();
		DisplayDocument current = null;
		double value;
		int numberOfAuthors;

		//get every document which has readership data
		documentDataList = con.getRankingValueDocuments("simple_count", "readers", "mendeley");
		System.out.println(documentDataList.size());

		for (int i = 0; i < documentDataList.size(); i++) {
			current = documentDataList.get(i);
			
			//print the current progress in 10.000 steps
			if (i % 10000 == 0)
				System.out.println(i + "/" + documentDataList.size());

			try {
				//get the number of authors per document
				numberOfAuthors = con.getPersonsByDocumentID(current.getDocumentId()).size();

				//only divide through non-zero values
				if (numberOfAuthors != 0) {
					value = (double) current.getRankingValue() / numberOfAuthors;
					
					//write to database
					con.writeBibliometricsInDatabase(current.getDocumentId(), "normalizedByNumberOfAuthors", "readers",
							"", "", value, "mendeley");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * creates the Altmetric of Readership per author over all papers he wrote and stores it in database
	 * 
	 * under progress -> leads to errors
	 */
	public void createReadershipByAuthor() {
		List<Person> personList = new ArrayList<Person>();
		List<DisplayDocument> documentList = new ArrayList<DisplayDocument>();
		Person currentAuthor = null;
		double authorReadership;
		DisplayDocument currentDocument = null;
		double documentReadership;

		int numberOfAuthors = con.getBiggestIdFromAuthors();

		for (int k = 0; k < numberOfAuthors; k = k + 500) {

			System.out.println(k + "/" + numberOfAuthors);
			personList = con.getAllPersonsInBatches(k, 500);

			for (int i = 0; i < personList.size(); i++) {
				currentAuthor = personList.get(i);
				authorReadership = 0;

				try {
					documentList = con.getDocumentsByPersonId(currentAuthor.getId());

					for (int j = 0; j < documentList.size(); j++) {
						currentDocument = documentList.get(j);
						documentReadership = con
								.getRankingValue(currentDocument.getDocumentId(), "simple_count", "readers", "mendeley")
								.getRankingValue();
						if (documentReadership != -1) {
							authorReadership = authorReadership + documentReadership;
						}
					}
					con.writeAuthorBibliometricsInDatabase(currentAuthor.getId(), "simple_count", "readers",
							"mendeley", authorReadership);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * creates the Altmetric of Readership per paper from the sum of the readership of all authors
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
		List<Person> personList = new ArrayList<Person>();
		int documentNumber = 0;
		Person currentAuthor = null;
		int hIndex = 0;

		int numberOfAuthors = con.getBiggestIdFromAuthors();

		for (int k = 0; k < numberOfAuthors; k = k + 500) {

			System.out.println(k + "/" + numberOfAuthors);
			personList = con.getRankingValueAuthorsInBatches("simple_count", "readers", "mendeley", k, 500);

			for (int i = 0; i < personList.size(); i++) {
				currentAuthor = personList.get(i);

				documentNumber = con.getDocumentsByPersonId(currentAuthor.getId()).size();
				hIndex = Math.min((int) currentAuthor.getRankingValue(), documentNumber);

				con.writeAuthorBibliometricsInDatabase(currentAuthor.getId(), "h-index", "readers",
						"mendeley", hIndex);
			}
		}
	}

	public static void main(String[] args) {
		CreateRanking cr = new CreateRanking();
		cr.createReadershipSumFromAuthors();
	}

}
