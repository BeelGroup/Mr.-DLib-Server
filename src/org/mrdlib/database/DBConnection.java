package org.mrdlib.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.mrdlib.Document;
import org.mrdlib.DocumentSet;
import org.mrdlib.Snippet;
import org.mrdlib.tools.Abstract;
import org.mrdlib.tools.Person;
import org.mrdlib.tools.XMLDocument;

public class DBConnection {

	private Connection con = null;
	private Constants constants = new Constants();
	private Map<String, Integer> lengthMap = new HashMap<String, Integer>();

	public DBConnection() {
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		try {
			createConnection();
			stmt = con.createStatement();
			stmt.executeQuery("SET NAMES 'utf8'");
			rs = stmt.executeQuery("SHOW COLUMNS FROM " + constants.getDocuments());
			fillMap(rs);
			rs2 = stmt.executeQuery("SHOW COLUMNS FROM " + constants.getAbstracts());
			fillMap(rs2);
			rs3 = stmt.executeQuery("SHOW COLUMNS FROM " + constants.getPersons());
			fillMap(rs3);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				rs.close();
				rs2.close();
				rs3.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
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
		con.close();
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
		Statement stmt = null;
		ResultSet rs = null;

		String query = "SELECT * FROM " + constants.getPersons() + " WHERE " + constants.getPersonID() + " = '"
				+ authorID + "'";
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			rs.next();
			person = new Person(rs.getString(constants.getFirstname()), rs.getString(constants.getMiddlename()),
					rs.getString(constants.getSurname()), rs.getString(constants.getUnstructured()));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
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
		ResultSet rs = null;
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

			rs = stateAuthorExists.executeQuery();

			if (!rs.next()) {
				ResultSet rs2 = null;

				String queryAuthor = "INSERT INTO " + constants.getPersons() + " (" + constants.getFirstname() + ", "
						+ constants.getMiddlename() + ", " + constants.getSurname() + ", " + constants.getUnstructured()
						+ ")" + "VALUES (?, ?, ?, ?)";

				try {
					stateInsertAuthor = con.prepareStatement(queryAuthor, Statement.RETURN_GENERATED_KEYS);

					SetIfNull(document, stateInsertAuthor, author.getFirstname(), 1, "string",
							constants.getFirstname());
					SetIfNull(document, stateInsertAuthor, author.getMiddlename(), 2, "string",
							constants.getMiddlename());
					SetIfNull(document, stateInsertAuthor, author.getSurname(), 3, "string", constants.getSurname());
					SetIfNull(document, stateInsertAuthor, author.getUnstructured(), 4, "string",
							constants.getUnstructured());

					stateInsertAuthor.executeUpdate();

					rs2 = stateInsertAuthor.getGeneratedKeys();
					if (rs2.next())
						authorKey = rs2.getLong(1);

				} catch (Exception e) {
					System.out.println(document.getDocumentPath() + ": " + document.getId());
					e.printStackTrace();
				} finally {
					rs.close();
					rs2.close();
					stateInsertAuthor.close();
				}
			} else
				authorKey = (long) rs.getInt(constants.getPersonID());
		} catch (

		SQLException sqle) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			sqle.printStackTrace();
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		} finally {
			try {
				stateAuthorExists.close();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return authorKey;
	}

	public void addPersonDocumentRelation(XMLDocument document, Long documentId, Long authorID, int rank) {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			String query = "INSERT INTO " + constants.getDocPers() + " (" + constants.getDocumentIDInDocPers() + ", "
					+ constants.getPersonIDInDocPers() + ", " + constants.getRank() + ") VALUES (" + documentId + ", "
					+ authorID + ", " + rank + ");";

			stmt.executeUpdate(query);
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Long getCollectionIDByName(XMLDocument document, String collectionName) {
		Statement stmt = null;
		ResultSet rs = null;
		Long id = null;

		String query = "SELECT * FROM " + constants.getCollections() + " WHERE " + constants.getCollectionShortName()
				+ " = '" + collectionName + "'";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			while (rs.next())
				id = rs.getLong(constants.getCollectionID());
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return id;
	}

	public <T> PreparedStatement SetIfNull(XMLDocument document, PreparedStatement stmt, T value, int index,
			String type, String coloumnName) {

		try {
			if (value instanceof String) {
				String valueString = replaceHighComma((String) value);
				if (!(coloumnName.equals(constants.getType()) || coloumnName.equals(constants.getUnstructured())
						|| coloumnName.equals(constants.getAbstr()))) {
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
			} else if (coloumnName.equals(constants.getYear())) {
				if (((int) value) == 0)
					stmt.setNull(index, java.sql.Types.INTEGER);
				else
					stmt.setObject(index, value);
			} else
				stmt.setObject(index, value);

		} catch (SQLException e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		}
		return stmt;
	}

	public void makeQueryOfDocument(XMLDocument document) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement stateQueryDoc = null;
		Long docKey = null;
		LinkedHashSet<Person> authors = document.getAuthors();
		Long[] authorKey = new Long[authors.size()];
		// if (document.getAuthors().size() == 0)
		// System.out.println(document.getDocumentPath() + ": " +
		// document.getId() + ": No Authors!");

		// Doc Exists already?
		String docExists = "SELECT * FROM " + constants.getDocuments() + " WHERE " + constants.getIdOriginal() + " = '"
				+ document.getId() + "'";
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(docExists);
			if (rs.next()) {
				System.out.println(document.getDocumentPath() + ": " + document.getId() + ": Double Entry");
				return;
			}
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		} finally {
			stmt.close();
			rs.close();
		}

		// Doc don't exists, insert!
		try {
			Iterator<Person> it = authors.iterator();

			for (int i = 0; i < authors.size(); i++) {
				Person author = it.next();
				authorKey[i] = addPersonToDbIfNotExists(document, author);
			}

			String queryDoc = "INSERT INTO " + constants.getDocuments() + " (" + constants.getIdOriginal() + ", "
					+ constants.getDocumentCollectionID() + ", " + constants.getTitle() + ", "
					+ constants.getTitleClean() + ", " + constants.getPublishedId() + ", " + ""
					+ constants.getLanguage() + ", " + constants.getYear() + ", " + constants.getType() + ", "
					+ constants.getKeywords() + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

			for (int i = 0; i < document.getAbstracts().size(); i++)
				addAbstractToDocument(document, document.getAbstracts().get(i), docKey);

			for (int i = 0; i < authors.size(); i++) {
				addPersonDocumentRelation(document, docKey, authorKey[i], i + 1);
			}

		} catch (SQLException sqle) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			sqle.printStackTrace();
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		} finally {
			stateQueryDoc.close();
		}
	}

	private void addAbstractToDocument(XMLDocument document, Abstract abstr, Long docKey) {
		PreparedStatement stmt = null;
		try {
			String query = "INSERT INTO " + constants.getAbstracts() + " (" + constants.getAbstractDocumentId() + ", "
					+ constants.getAbstractLanguage() + ", " + constants.getAbstr() + ") VALUES (?, ?, ?)";

			stmt = con.prepareStatement(query);

			SetIfNull(document, stmt, docKey, 1, "long", constants.getAbstractDocumentId());
			SetIfNull(document, stmt, abstr.getLanguage(), 2, "string", constants.getAbstractLanguage());
			SetIfNull(document, stmt, abstr.getContent(), 3, "string", constants.getAbstr());

			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public List<Person> getPersonsByDocumentID(Long documentID) {
		Statement stmt = null;
		ResultSet rs = null;
		List<Person> persons = new ArrayList<Person>();

		String query = "SELECT * FROM " + constants.getDocPers() + " WHERE " + constants.getDocumentIDInDocPers()
				+ " = '" + documentID + "'";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				persons.add(getPersonById(rs.getLong(constants.getPersonIDInDocPers())));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return persons;
	}

	public DocumentSet getDocumentSetByOriginalId(String originalid) throws Exception {
		DocumentSet documentSet = new DocumentSet();
		String authorNames = "";
		documentSet.setRecommendationSetId("DummyId");
		documentSet.setSuggested_label("Dummy Articles");
		StringJoiner joiner = new StringJoiner(", ");
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement();

			String query = "SELECT * FROM " + constants.getDocuments() + " WHERE " + constants.getIdOriginal() + " = '"
					+ originalid + "'";

			rs = stmt.executeQuery(query);
			while (rs.next()) {

				List<Person> authors = getPersonsByDocumentID((rs.getLong(constants.getDocumentId())));
				for (int i = 0; i < authors.size(); i++)
					joiner.add(authors.get(i).getName());

				authorNames = joiner.toString();

				Document document = new Document("dummyRec", String.valueOf(rs.getLong(constants.getDocumentId())),
						rs.getString(constants.getIdOriginal()), 666,
						new Snippet("&lt;span class='title'&gt;" + rs.getString(constants.getTitle())
								+ "&lt;/span&gt;. &lt;span class='authors'&gt;" + authorNames
								+ ";/span&gt;. &lt;span class='journal'&gt;" + rs.getString(constants.getPublishedId())
								+ "&lt;/span&gt;. &lt;span class='volume_and_number'&gt;6:66&lt;/span&gt;. &lt;span class='year'&gt;"
								+ rs.getInt(constants.getYear()) + "&lt;/span&gt;", "html_and_css"),
						"DummyURL", "DummyFallBackURL");
				documentSet.addDocument(document);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			stmt.close();
			rs.close();
		}
		return documentSet;
	}
}