package org.mrdlib.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.mrdlib.Document;
import org.mrdlib.DocumentSet;
import org.mrdlib.tools.Person;
import org.mrdlib.tools.XMLDocument;

public class DBConnection {

	private Connection con = null;
	private Constants constants = new Constants();
	private Map<String, Integer> lengthMap = new HashMap<String, Integer>();

	public DBConnection() {
		try {
			createConnection();
			Statement stmt = con.createStatement();
			stmt.executeQuery("SET NAMES 'utf8'");
			ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM " + constants.getDocuments());
			fillMap(rs);
			ResultSet rs2 = stmt.executeQuery("SHOW COLUMNS FROM " + constants.getPersons());
			fillMap(rs2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fillMap(ResultSet rs) {
		String result = "";
		try {
			while (rs.next()) {
				result = rs.getString("Type");
				if (!result.startsWith("enum") && result.contains("(")) {
					lengthMap.put(rs.getString("Field"),
							Integer.parseInt(result.substring(result.indexOf("(") + 1, result.indexOf(")"))));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	protected void finalize() throws Throwable {

		super.finalize();
	}

	public void createConnection() throws Exception {
		Class.forName(constants.getDbClass());

		try {
			con = DriverManager.getConnection(constants.getUrl() + constants.getDb(), constants.getUser(),
					constants.getPassword());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Person getPersonById(Long authorID) {
		Person person = null;

		String query = "SELECT * FROM " + constants.getPersons() + " WHERE " + constants.getPersonID() + " = '"
				+ authorID + "'";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			rs.next();
			person = new Person(rs.getString(constants.getFirstname()), rs.getString(constants.getMiddlename()),
					rs.getString(constants.getSurname()), rs.getString(constants.getUnstructured()));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return person;
	}

	public String replaceHighComma(String text) {
		if (text != null)
			if (text.contains("'"))
				return text.replace("'", "''");
			else
				return text;
		else
			return null;
	}

	public Long addPersonToDbIfNotExists(XMLDocument document, Person author) {
		PreparedStatement stateAuthorExists = null;
		PreparedStatement stateInsertAuthor = null;
		Long authorKey = null;
		int count = 1;

		String firstname = replaceHighComma(author.getFirstname());
		String middlename = replaceHighComma(author.getMiddlename());
		String surname = replaceHighComma(author.getSurname());
		String unstructured = replaceHighComma(author.getUnstructured());

		String queryAuthorExists = "SELECT * FROM " + constants.getPersons() + " WHERE " + constants.getFirstname()
				+ (firstname == null ? " is null" : " = ? ") + " AND " + constants.getMiddlename()
				+ (middlename == null ? " is null" : " = ? ") + " AND " + constants.getSurname()
				+ (surname == null ? " is null" : " = ? ") + " AND " + constants.getUnstructured()
				+ (unstructured == null ? " is null" : " = ? ");

		try {
			stateAuthorExists = con.prepareStatement(queryAuthorExists, Statement.RETURN_GENERATED_KEYS);

			if (firstname != null) {
				stateAuthorExists.setString(count++, firstname);
			}

			if (middlename != null)
				stateAuthorExists.setString(count++, middlename);

			if (author.getFirstname() != null)
				stateAuthorExists.setString(count++, surname);

			if (unstructured != null)
				stateAuthorExists.setString(count++, unstructured);

			ResultSet rs = stateAuthorExists.executeQuery();

			if (!rs.next()) {
				String queryAuthor = "INSERT INTO " + constants.getPersons() + " (" + constants.getFirstname() + ", "
						+ constants.getMiddlename() + ", " + constants.getSurname() + ", " + constants.getUnstructured()
						+ ")" + "VALUES (?, ?, ?, ?)";

				stateInsertAuthor = con.prepareStatement(queryAuthor, Statement.RETURN_GENERATED_KEYS);

				SetIfNull(document, stateInsertAuthor, author.getFirstname(), 1, "string", constants.getFirstname());
				SetIfNull(document, stateInsertAuthor, author.getMiddlename(), 2, "string", constants.getMiddlename());
				SetIfNull(document, stateInsertAuthor, author.getSurname(), 3, "string", constants.getSurname());
				SetIfNull(document, stateInsertAuthor, author.getUnstructured(), 4, "string",
						constants.getUnstructured());

				authorKey = (long) stateInsertAuthor.executeUpdate();

			} else
				authorKey = (long) rs.getInt(constants.getPersonID());
		} catch (SQLException sqle) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			sqle.printStackTrace();
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		}

		return authorKey;
	}

	public void addPersonDocumentRelation(XMLDocument document, Long documentId, Long authorID) {
		try {
			Statement stmt = con.createStatement();
			String query = "INSERT INTO " + constants.getDocPers() + " (" + constants.getDocumentIDInDocPers() + ", "
					+ constants.getPersonIDInDocPers() + ") VALUES (" + documentId + ", " + authorID + ");";

			stmt.executeUpdate(query);
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		}
	}

	public Long getCollectionIDByName(XMLDocument document, String collectionName) {
		ResultSet rs = null;
		Long id = null;

		String query = "SELECT * FROM " + constants.getCollections() + " WHERE " + constants.getCollectionShortName()
				+ " = '" + collectionName + "'";

		try {
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			while (rs.next())
				id = rs.getLong(constants.getCollectionID());
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		}

		return id;
	}

	public <T> PreparedStatement SetIfNull(XMLDocument document, PreparedStatement stmt, T value, int index,
			String type, String coloumnName) {
		try {
			if (value instanceof String) {
				String valueString = replaceHighComma((String) value);
				if (!coloumnName.equals(constants.getType()) && !coloumnName.equals(constants.getUnstructured())) {
					if (valueString.length() > lengthMap.get(coloumnName))
						System.out.println("Truncate because too long!");
				}
				value = (T) valueString;
			}
			if (value == null) {
				if (type.equals("string"))
					stmt.setNull(index, java.sql.Types.VARCHAR);
				else if (type.equals("int") || type.equals("long"))
					stmt.setNull(index, java.sql.Types.INTEGER);
			} else
				stmt.setObject(index, value);

		} catch (SQLException e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			System.out.println(coloumnName);
			e.printStackTrace();
		}
		return stmt;
	}

	public void makeQueryOfDocument(XMLDocument document) throws SQLException {
		if (document.getAuthors().size() == 0)
			System.out.println(document.getDocumentPath() + ": " + document.getId() + ": No Authors!");

		String docExists = "SELECT * FROM " + constants.getDocuments() + " WHERE " + constants.getIdOriginal() + " = '"
				+ document.getId() + "'";

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(docExists);
			if (rs.next()) {
				System.out.println(document.getDocumentPath() + ": " + document.getId() + ": Double Entry");
				return;
			}
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		}

		PreparedStatement stateQueryDoc = null;
		Long docKey = null;
		Set<Person> authorSet = new HashSet<Person>(document.getAuthors());
		Long[] authorKey = new Long[authorSet.size()];
		try {
			for (int i = 0; i < authorKey.length; i++) {
				Person author = authorSet.iterator().next();
				authorKey[i] = addPersonToDbIfNotExists(document, author);
			}

			String queryDoc = "INSERT INTO " + constants.getDocuments() + " (" + constants.getIdOriginal() + ", "
					+ constants.getDocumentCollectionID() + ", " + constants.getTitle() + ", "
					+ constants.getTitleClean() + ", " + constants.getPublishedId() + ", " + ""
					+ constants.getLanguage() + ", " + constants.getYear() + ", " + constants.getType() + ", "
					+ constants.getKeywords() + ")" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

			Long collectionId = getCollectionIDByName(document, document.getCollection());

			stateQueryDoc = con.prepareStatement(queryDoc, Statement.RETURN_GENERATED_KEYS);

			SetIfNull(document, stateQueryDoc, document.getId(), 1, "string", constants.getIdOriginal());
			SetIfNull(document, stateQueryDoc, collectionId, 2, "long", constants.getDocumentCollectionID());
			SetIfNull(document, stateQueryDoc, document.getTitle(), 3, "string", constants.getTitle());
			SetIfNull(document, stateQueryDoc, document.getCleanTitle(), 4, "string", constants.getTitleClean());
			SetIfNull(document, stateQueryDoc, document.getPublishedIn(), 5, "string", constants.getPublishedId());
			SetIfNull(document, stateQueryDoc, document.getLanguage(), 6, "string", constants.getLanguage());
			SetIfNull(document, stateQueryDoc, document.getYear(), 7, "int", constants.getYear());
			SetIfNull(document, stateQueryDoc, document.getType(), 8, "string", constants.getType());
			SetIfNull(document, stateQueryDoc, document.getKeywordsAsString(), 9, "string", constants.getKeywords());

			stateQueryDoc.executeUpdate();

			try (ResultSet generatedKeys = stateQueryDoc.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					docKey = generatedKeys.getLong(1);
				} else {
					System.out.println(document.getDocumentPath() + ": " + document.getId());
					throw new SQLException("Creating document failed, no ID obtained.");
				}
			}

			for (int i = 0; i < authorKey.length; i++) {
				addPersonDocumentRelation(document, docKey, authorKey[i]);
			}

		} catch (SQLException sqle) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			sqle.printStackTrace();
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		}
	}

	public List<Person> getPersonsByDocumentID(Long documentID) {
		List<Person> persons = new ArrayList<Person>();

		String query = "SELECT * FROM " + constants.getDocPers() + " WHERE " + constants.getDocumentIDInDocPers()
				+ " = '" + documentID + "'";

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				persons.add(getPersonById(rs.getLong(constants.getPersonIDInDocPers())));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return persons;
	}

	public DocumentSet getDocumentSetByOriginalId(String originalid) throws Exception {
		DocumentSet documentSet = new DocumentSet();
		String authorNames = "";
		documentSet.setRecommendationSetId("DummyId");
		documentSet.setSuggested_label("Dummy Articles");
		StringJoiner joiner = new StringJoiner(", ");

		try {
			Statement stmt = con.createStatement();

			String query = "SELECT * FROM " + constants.getDocuments() + " WHERE " + constants.getIdOriginal() + " = '"
					+ originalid + "'";

			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {

				List<Person> authors = getPersonsByDocumentID((rs.getLong(constants.getDocumentId())));
				for (int i = 0; i < authors.size(); i++)
					joiner.add(authors.get(i).getName());

				authorNames = joiner.toString();

				Document document = new Document("dummyRec", String.valueOf(rs.getLong(constants.getDocumentId())),
						rs.getString(constants.getIdOriginal()), 666,
						rs.getString(constants.getTitle()) + ". " + authorNames + ". "
								+ rs.getString(constants.getPublishedId()) + ". " + rs.getInt(constants.getYear()),
						"&lt;span class='title'&gt;" + rs.getString(constants.getTitle())
								+ "&lt;/span&gt;. &lt;span class='authors'&gt;" + authorNames
								+ ";/span&gt;. &lt;span class='journal'&gt;" + rs.getString(constants.getPublishedId())
								+ "&lt;/span&gt;. &lt;span class='volume_and_number'&gt;6:66&lt;/span&gt;. &lt;span class='year'&gt;"
								+ rs.getInt(constants.getYear()) + "&lt;/span&gt;",
						"DummyURL", "DummyFallBackURL");
				documentSet.addDocument(document);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return documentSet;
	}
}