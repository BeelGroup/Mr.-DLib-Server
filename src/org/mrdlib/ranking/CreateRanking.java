package org.mrdlib.ranking;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.mrdlib.Constants;
import org.mrdlib.DocumentData;
import org.mrdlib.database.DBConnection;
import org.mrdlib.tools.Person;

public class CreateRanking {

	DBConnection con = null;
	Constants constants = null;

	public CreateRanking() {
		try {
			con = new DBConnection("jar");
			constants = new Constants();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createReadershipNormalizedByAge() {
		List<DocumentData> documentDataList = new ArrayList<DocumentData>();
		DocumentData current = null;
		double value;

		documentDataList = con.getRankingValueDocuments("simple_count", "readers", "mendeley");
		System.out.println(documentDataList.size());
		for (int i = 0; i < documentDataList.size(); i++) {
			current = documentDataList.get(i);
			if (i % 10000 == 0)
				System.out.println(i + "/" + documentDataList.size());

			try {
				current.setYear(con.getDocumentDataBy(constants.getDocumentId(), current.getId() + "").getYear());

				if (current.getYear() != -1) {
					if (current.getYear() == Year.now().getValue())
						value = (double) current.getRankingValue();
					else
						value = (double) current.getRankingValue() / (Year.now().getValue() - current.getYear() + 1);

					con.writeBibliometricsInDatabase(current.getId(), "normalizedByAge", "readers", "", "", value,
							"mendeley");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void createReadershipNormalizedByNumberOfAuthors() {
		List<DocumentData> documentDataList = new ArrayList<DocumentData>();
		DocumentData current = null;
		double value;
		int numberOfAuthors;

		documentDataList = con.getRankingValueDocuments("simple_count", "readers", "mendeley");
		System.out.println(documentDataList.size());

		for (int i = 0; i < documentDataList.size(); i++) {
			current = documentDataList.get(i);
			if (i % 10000 == 0)
				System.out.println(i + "/" + documentDataList.size());

			try {
				numberOfAuthors = con.getPersonsByDocumentID(current.getId()).size();

				if (current.getYear() != -1 && numberOfAuthors != 0) {
					value = (double) current.getRankingValue() / numberOfAuthors;

					con.writeBibliometricsInDatabase(current.getId(), "normalizedByNumberOfAuthors", "readers", "", "",
							value, "mendeley");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void createReadershipByAuthor() {
		List<Person> personList = new ArrayList<Person>();
		List<DocumentData> documentList = new ArrayList<DocumentData>();
		Person currentAuthor = null;
		int authorReadership;
		DocumentData currentDocument = null;
		int documentReadership;

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
								.getRankingValue(currentDocument.getId() + "", "simple_count", "readers", "mendeley")
								.getRankingValue();
						if (documentReadership != -1) {
							authorReadership = authorReadership + documentReadership;
						}
					}
					con.writeAuthorBibliometricsInDatabase(currentAuthor.getId(), "simple_count", "readers", "", "",
							authorReadership, "mendeley");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void createReadershipSumFromAuthors() {
		List<DocumentData> documentDataList = new ArrayList<DocumentData>();
		List<Person> personList = new ArrayList<Person>();
		DocumentData currentDocument = null;
		Person currentPerson = null;
		int sumReadership = 0;

		int numberOfDocuments = con.getBiggestIdFromDocuments();

		for (int k = 0; k < numberOfDocuments; k = k + 500) {
			documentDataList = con.getDocumentDataInBatches(k, k + 500);
			System.out.println(k + "/" + numberOfDocuments);

			try {
				for (int i = 0; i < documentDataList.size(); i++) {
					currentDocument = documentDataList.get(i);
					personList = con.getPersonsByDocumentID(currentDocument.getId());
					sumReadership = 0;

					for (int j = 0; j < personList.size(); j++) {
						currentPerson = personList.get(j);
						currentPerson.setRankingValue(con.getRankingValueAuthor(currentPerson.getId() + "",
								"simple_count", "readers", "mendeley"));
						sumReadership = sumReadership + (int) currentPerson.getRankingValue();
					}
					if (sumReadership != 0)
						con.writeBibliometricsInDatabase(currentDocument.getId(), "sumFromAuthors", "readers", "", "",
								sumReadership, "mendeley");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

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

				con.writeAuthorBibliometricsInDatabase(currentAuthor.getId(), "h-index", "readers", "", "", hIndex,
						"mendeley");
			}
		}
	}

	public static void main(String[] args) {
		CreateRanking cr = new CreateRanking();
		cr.createReadershipSumFromAuthors();
	}

}
