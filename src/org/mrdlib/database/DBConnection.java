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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringEscapeUtils;
import org.mrdlib.Constants;
import org.mrdlib.DocumentData;
import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.DocumentSet;
import org.mrdlib.display.Snippet;
import org.mrdlib.tools.Abstract;
import org.mrdlib.tools.Person;
import org.mrdlib.tools.XMLDocument;

/**
 * 
 * @author Millah
 * 
 *         This class handles all the operations on the database
 *
 */
public class DBConnection {

	private Connection con = null;
	private Constants constants = new Constants();
	Context ctx = null;
	// stores the length of the database fields to check for truncation error
	private Map<String, Integer> lengthMap = new HashMap<String, Integer>();

	public DBConnection() throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		// get all the lengths of the database fields and store it in a map
		try {
			createConnectionJar();
			// createConnectionTomcat();
			stmt = con.createStatement();
			stmt.executeQuery("SET NAMES 'utf8'");
			rs = stmt.executeQuery("SHOW COLUMNS FROM " + constants.getDocuments());
			fillMap(rs);
			rs2 = stmt.executeQuery("SHOW COLUMNS FROM " + constants.getAbstracts());
			fillMap(rs2);
			rs3 = stmt.executeQuery("SHOW COLUMNS FROM " + constants.getPersons());
			fillMap(rs3);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				stmt.close();
				rs.close();
				rs2.close();
				rs3.close();
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	/**
	 * stores the lengths of the database field in the length map
	 * 
	 * @param ResultSet
	 *            of a query which asked for coloumn information of the database
	 * @throws SQLException
	 */
	private void fillMap(ResultSet rs) throws SQLException {
		String result = "";
		try {
			while (rs.next()) {
				// exclude special fields which do not have a length
				result = rs.getString("Type");
				if (!result.startsWith("enum") && result.contains("(")) {
					lengthMap.put(rs.getString("Field"),
							Integer.parseInt(result.substring(result.indexOf("(") + 1, result.indexOf(")"))));
				}
			}
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * close connection
	 */
	protected void finalize() throws Throwable {
		con.close();
		super.finalize();
	}

	public void close() throws SQLException {
		con.close();
	}

	/**
	 * create connection from Connection Pool (configured in the tomcat config
	 * files)
	 * 
	 * @throws Exception
	 */

	public void createConnectionTomcat() throws Exception {
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:comp/env");
			DataSource ds = (DataSource) envContext.lookup("jdbc/mrdlib");
			con = ds.getConnection();
		} catch (Exception e) {
			throw e;
		}
	}

	public void createConnectionJar() throws Exception {
		try {
			Class.forName(constants.getDbClass());
			con = DriverManager.getConnection(constants.getUrl() + constants.getDb(), constants.getUser(),
					constants.getPassword());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * get the related information to a personId out of the database.
	 * 
	 * @param authorID
	 * @return complete Author
	 * @throws Exception
	 */
	public Person getPersonById(Long authorID) throws Exception {
		Person person = null;
		Statement stmt = null;
		ResultSet rs = null;

		// the query to get the person
		String query = "SELECT " + constants.getFirstname() + "," + constants.getMiddlename() + ","
				+ constants.getSurname() + "," + constants.getUnstructured() + " FROM " + constants.getPersons()
				+ " WHERE " + constants.getPersonID() + " = '" + authorID + "'";
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			rs.next();

			// create a new object person with the retrieved data
			person = new Person(rs.getString(constants.getFirstname()), rs.getString(constants.getMiddlename()),
					rs.getString(constants.getSurname()), rs.getString(constants.getUnstructured()));

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return person;
	}

	/**
	 * escapes high comma in a text to make it processable in a MySQL query
	 * 
	 * @param text
	 * @return escaped text
	 */
	public String replaceHighComma(String text) {
		if (text != null)
			if (text.contains("'"))
				return text.replace("'", "''");
			else
				return text;
		else
			return null;
	}

	/**
	 * stores a author in a database if he is not already present and gives back
	 * the auto generated id of him. If he exists (which means there is another
	 * person with exactly the same name), the id of this already present person
	 * is given back
	 * 
	 * @param document,
	 *            the related document which is currently processed to trace
	 *            error back
	 * @param author,
	 *            the author who has to be inserted
	 * @return the id the (inserted or retrieved) author
	 * @throws SQLException
	 */
	public Long addPersonToDbIfNotExists(XMLDocument document, Person author) throws SQLException {
		PreparedStatement stateAuthorExists = null;
		PreparedStatement stateInsertAuthor = null;
		ResultSet rs = null;
		Long authorKey = null;
		int count = 1;

		// replace highComma (for eg O'Donnel) to make it queryable
		String firstname = replaceHighComma(author.getFirstname());
		String middlename = replaceHighComma(author.getMiddlename());
		String surname = replaceHighComma(author.getSurname());
		String unstructured = replaceHighComma(author.getUnstructured());

		// the query to retrieve already present authors. In Database firstname,
		// middlename, surname and unstructured form a unique key to massivly
		// increase performance
		// the ? marks a variable which is yet to set and must be checked for
		// null values
		String queryAuthorExists = "SELECT " + constants.getPersonID() + " FROM " + constants.getPersons() + " WHERE "
				+ constants.getFirstname() + (firstname == null ? " is null" : " = ? ") + " AND "
				+ constants.getMiddlename() + (middlename == null ? " is null" : " = ? ") + " AND "
				+ constants.getSurname() + (surname == null ? " is null" : " = ? ") + " AND "
				+ constants.getUnstructured() + (unstructured == null ? " is null" : " = ? ");

		try {
			stateAuthorExists = con.prepareStatement(queryAuthorExists, Statement.RETURN_GENERATED_KEYS);

			// if not null insert true value for the corresponding ? (which is
			// represented by count)
			if (firstname != null)
				stateAuthorExists.setString(count++, firstname);

			if (middlename != null)
				stateAuthorExists.setString(count++, middlename);

			if (author.getFirstname() != null)
				stateAuthorExists.setString(count++, surname);

			if (unstructured != null)
				stateAuthorExists.setString(count++, unstructured);

			rs = stateAuthorExists.executeQuery();

			// if it is a new author not already present in the database
			if (!rs.next()) {
				ResultSet rs2 = null;

				// query to insert the author
				// the ? marks a variable which is yet to set and must be
				// checked for null values
				String queryAuthor = "INSERT INTO " + constants.getPersons() + " (" + constants.getFirstname() + ", "
						+ constants.getMiddlename() + ", " + constants.getSurname() + ", " + constants.getUnstructured()
						+ ")" + "VALUES (?, ?, ?, ?)";

				try {
					stateInsertAuthor = con.prepareStatement(queryAuthor, Statement.RETURN_GENERATED_KEYS);

					// handle null values and other preprocessing stuff
					SetIfNull(document, stateInsertAuthor, author.getFirstname(), 1, "string",
							constants.getFirstname());
					SetIfNull(document, stateInsertAuthor, author.getMiddlename(), 2, "string",
							constants.getMiddlename());
					SetIfNull(document, stateInsertAuthor, author.getSurname(), 3, "string", constants.getSurname());
					SetIfNull(document, stateInsertAuthor, author.getUnstructured(), 4, "string",
							constants.getUnstructured());

					stateInsertAuthor.executeUpdate();

					// get the autogenerated key back
					rs2 = stateInsertAuthor.getGeneratedKeys();
					if (rs2.next())
						authorKey = rs2.getLong(1);

				} catch (SQLException e) {
					System.out.println(document.getDocumentPath() + ": " + document.getId());
					throw e;
				} finally {
					rs.close();
					rs2.close();
					stateInsertAuthor.close();
				}
			} else
				// get the key of the already present author
				authorKey = (long) rs.getInt(constants.getPersonID());
		} catch (SQLException sqle) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			throw sqle;
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			throw e;
		} finally {
			try {
				stateAuthorExists.close();
				rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return authorKey;
	}

	/**
	 * 
	 * This method inserts the person - document relation to the database
	 * 
	 * @param document
	 *            for trace back the error
	 * @param documentId
	 * @param authorId
	 * @param rank,
	 *            location of the naming of the author of the respective
	 *            document
	 * @throws Exception
	 */
	public void addPersonDocumentRelation(XMLDocument document, Long documentId, Long authorId, int rank)
			throws Exception {
		Statement stmt = null;
		try {
			stmt = con.createStatement();

			// query for insert person - document relation
			String query = "INSERT INTO " + constants.getDocPers() + " (" + constants.getDocumentIDInDocPers() + ", "
					+ constants.getPersonIDInDocPers() + ", " + constants.getRank() + ") VALUES (" + documentId + ", "
					+ authorId + ", " + rank + ");";

			stmt.executeUpdate(query);
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			throw e;
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	/**
	 * get the Id of a collection by searching for its SHORT name
	 * 
	 * @param document,
	 *            for error backtracing
	 * @param collectionName
	 *            the short name of the collection
	 * @return the id of the collection
	 * @throws SQLException
	 */
	public Long getCollectionIDByName(XMLDocument document, String collectionName) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		Long id = null;

		String query = "SELECT " + constants.getCollectionID() + " FROM " + constants.getCollections() + " WHERE "
				+ constants.getCollectionShortName() + " = '" + collectionName + "'";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			while (rs.next())
				id = rs.getLong(constants.getCollectionID());
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			throw e;
		} finally {
			try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return id;
	}

	/**
	 * this method prepares parts of a given prepared statement to handle null
	 * values and high commata
	 * 
	 * @param document,
	 *            for error backtracing
	 * @param stmt,
	 *            the statement which is editet
	 * @param value,
	 *            the value which has to be inserted
	 * @param index,
	 *            the index of the related ? marker (where to insert)
	 * @param type,
	 *            the corresponding type of the inserted value
	 * @param coloumnName,
	 *            the coloumn name of the value
	 * @return
	 * @throws SQLException
	 */
	public <T> PreparedStatement SetIfNull(XMLDocument document, PreparedStatement stmt, T value, int index,
			String type, String coloumnName) throws SQLException {

		try {
			if (value instanceof String) {
				// replace high commata
				String valueString = replaceHighComma((String) value);
				// ignore values which have no specific length
				if (!(coloumnName.equals(constants.getType()) || coloumnName.equals(constants.getUnstructured())
						|| coloumnName.equals(constants.getAbstr()))) {
					// check for truncation error
					if (valueString.length() > lengthMap.get(coloumnName))
						System.out.println(document.getDocumentPath() + ": " + document.getId() + ": Truncate"
								+ coloumnName + " because too long!");
				}
				value = (T) valueString;
			}
			if (value == null) {
				// set special null value for String types
				if (type.equals("string"))
					stmt.setNull(index, java.sql.Types.VARCHAR);
				// set special null values for integer types
				else if (type.equals("int") || type.equals("long"))
					stmt.setNull(index, java.sql.Types.INTEGER);
				// the year has to be handles seperatly. Since int cannot be
				// null, a 0 is used. if a zero is set, the year in the database
				// has to be null
			} else if (coloumnName.equals(constants.getYear())) {
				if (((int) value) == 0)
					stmt.setNull(index, java.sql.Types.INTEGER);
				// otherwise use real value
				else
					stmt.setObject(index, value);
				// otherwise use real value
			} else
				stmt.setObject(index, value);

		} catch (SQLException e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			throw e;
		}
		return stmt;
	}

	/**
	 * insert a XMLDocument to the database with all the related information
	 * (like authors and so on) if it not already exists (based on the original
	 * id of the cooperation partner)
	 * 
	 * @param document,
	 *            the parsed XMLDocument
	 * @throws Exception
	 */
	public void insertDocument(XMLDocument document) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement stateQueryDoc = null;
		Long docKey = null;
		LinkedHashSet<Person> authors = document.getAuthors();
		Long[] authorKey = new Long[authors.size()];
		// if (document.getAuthors().size() == 0)
		// System.out.println(document.getDocumentPath() + ": " +
		// document.getId() + ": No Authors!");

		// query to check if document already exists
		String docExists = "SELECT " + constants.getDocumentId() + " FROM " + constants.getDocuments() + " WHERE "
				+ constants.getIdOriginal() + " = '" + document.getId() + "'";
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(docExists);
			// if there is a document with the same original id
			if (rs.next()) {
				System.out.println(document.getDocumentPath() + ": " + document.getId() + ": Double Entry");
				return;
			}
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			throw e;
		} finally {
			try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
				System.out.println(document.getDocumentPath() + ": " + document.getId());
				throw e;
			}
		}

		// if the document dont exists, insert it including authors, collection,
		// abstracts and so on
		try {
			Iterator<Person> it = authors.iterator();

			// for each person, insert in database and store related key
			for (int i = 0; i < authors.size(); i++) {
				Person author = it.next();
				authorKey[i] = addPersonToDbIfNotExists(document, author);
			}

			// query to insert all information to the documents table
			String queryDoc = "INSERT INTO " + constants.getDocuments() + " (" + constants.getIdOriginal() + ", "
					+ constants.getDocumentCollectionID() + ", " + constants.getTitle() + ", "
					+ constants.getTitleClean() + ", " + constants.getPublishedId() + ", " + ""
					+ constants.getLanguage() + ", " + constants.getYear() + ", " + constants.getType() + ", "
					+ constants.getKeywords() + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

			// get the collection id by its name, to store relation in documents
			// table
			Long collectionId = getCollectionIDByName(document, document.getCollection());

			stateQueryDoc = con.prepareStatement(queryDoc, Statement.RETURN_GENERATED_KEYS);

			// set the values of the documents with the wrapper method which
			// checks for null values etc
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

			// get the key of the inserted document
			try (ResultSet generatedKeys = stateQueryDoc.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					docKey = generatedKeys.getLong(1);
				} else {
					System.out.println(document.getDocumentPath() + ": " + document.getId());
					throw new SQLException("Creating document failed, no ID obtained.");
				}
			}

			// insert every related abstract to the abstract table with the
			// corresponding document id
			for (int i = 0; i < document.getAbstracts().size(); i++)
				addAbstractToDocument(document, document.getAbstracts().get(i), docKey);

			// insert all author document relations with the related keys from
			// author and document
			for (int i = 0; i < authors.size(); i++) {
				addPersonDocumentRelation(document, docKey, authorKey[i], i + 1);
			}

		} catch (SQLException sqle) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			throw sqle;
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			throw e;
		} finally {
			try {
				stateQueryDoc.close();
			} catch (SQLException e) {
				System.out.println(document.getDocumentPath() + ": " + document.getId());
				throw e;
			}
		}
	}

	/**
	 * insert an abstract to the abstract table
	 * 
	 * @param document,
	 *            for error backtracing
	 * @param abstr,
	 *            the corresponding Abstract object
	 * @param docKey,
	 *            the document key from the database
	 * @throws Exception
	 */
	private void addAbstractToDocument(XMLDocument document, Abstract abstr, Long docKey) throws Exception {
		PreparedStatement stmt = null;
		try {
			// query which inserts the abstract information
			String query = "INSERT INTO " + constants.getAbstracts() + " (" + constants.getAbstractDocumentId() + ", "
					+ constants.getAbstractLanguage() + ", " + constants.getAbstr() + ") VALUES (?, ?, ?)";

			stmt = con.prepareStatement(query);

			// set values of abstract with wrapper method to check for null
			// values
			SetIfNull(document, stmt, docKey, 1, "long", constants.getAbstractDocumentId());
			SetIfNull(document, stmt, abstr.getLanguage(), 2, "string", constants.getAbstractLanguage());
			SetIfNull(document, stmt, abstr.getContent(), 3, "string", constants.getAbstr());

			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getId());
			throw e;
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	/**
	 * get all authors of a given document
	 * 
	 * @param documentID
	 * @return a list of authors
	 * @throws Exception
	 */
	public List<Person> getPersonsByDocumentID(Long documentID) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		List<Person> persons = new ArrayList<Person>();

		// query to select all author of a given document
		String query = "SELECT " + constants.getPersonIDInDocPers() + " FROM " + constants.getDocPers() + " WHERE "
				+ constants.getDocumentIDInDocPers() + " = '" + documentID + "'";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			// for each person obtained from the database, add it to the list of
			// authors which will be returned
			while (rs.next()) {
				// for each id obtained, get the complete person and store it in
				// the list
				persons.add(getPersonById(rs.getLong(constants.getPersonIDInDocPers())));
			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return persons;
	}

	/**
	 * get the short name of a collection by its id
	 * 
	 * @param id
	 *            of the collection
	 * @return the short name of the collection
	 * @throws SQLException
	 */
	public String getCollectionShortNameById(Long id) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		String name = "";

		// query to obtain the short name of the collection by its id
		String query = "SELECT " + constants.getCollectionShortName() + " FROM " + constants.getCollections()
				+ " WHERE " + constants.getCollectionID() + " = '" + id + "'";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			while (rs.next())
				name = rs.getString(constants.getCollectionShortName());
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return name;
	}

	/**
	 * makes a DocumentSet out of a single Document and set some dummy
	 * attributes. Mostly for testing purposes
	 * 
	 * @param originalid
	 * @return
	 * @throws Exception
	 */
	public DocumentSet getDocumentSetByOriginalId(String originalid) throws Exception {
		DocumentSet documentSet = new DocumentSet();
		documentSet.setRecommendationSetId("DummyId");
		documentSet.setSuggested_label("Dummy Articles");
		try {
			documentSet.addDocument(getDocumentBy(constants.getIdOriginal(), originalid));
		} catch (Exception e) {
			throw e;
		}
		return documentSet;
	}

	/**
	 * 
	 * Get a complete displayable Document by any customized field (returns only
	 * first retrieved document! Please use unique coloumns to obtain like
	 * original id or id!
	 * 
	 * @param coloumnName
	 *            for which should be searched (please use original id or id)
	 * @param id,
	 *            either original id or id
	 * @return the (first) retrieved Document
	 * @throws Exception
	 */
	public DisplayDocument getDocumentBy(String coloumnName, String id) throws Exception {
		DisplayDocument document = null;
		String authorNames = "";
		StringJoiner joiner = new StringJoiner(", ");
		Statement stmt = null;
		ResultSet rs = null;
		String title = null;
		String publishedIn = null;

		try {
			stmt = con.createStatement();

			// get all information of a document stored in a database by the
			// value of a custom coloumn
			String query = "SELECT * FROM " + constants.getDocuments() + " WHERE " + coloumnName + " = '" + id + "'";

			rs = stmt.executeQuery(query);
			// if there is a document
			if (rs.next()) {

				// concatenate each author to a single string with ',' as
				// seperator.
				List<Person> authors = getPersonsByDocumentID((rs.getLong(constants.getDocumentId())));
				for (int i = 0; i < authors.size(); i++)
					joiner.add(authors.get(i).getName());

				authorNames = joiner.toString();
				// encode special characters like ae, oe, ue or others to html
				// entitys
				authorNames = StringEscapeUtils.escapeHtml4(authorNames);
				title = StringEscapeUtils.escapeHtml4(rs.getString(constants.getTitle()));
				publishedIn = StringEscapeUtils.escapeHtml4(rs.getString(constants.getPublishedId()));

				// create a new document with values from the database
				document = new DisplayDocument("", String.valueOf(rs.getLong(constants.getDocumentId())),
						rs.getString(constants.getIdOriginal()), 666,
						new Snippet(title, authorNames, publishedIn, rs.getInt(constants.getYear()), "html_and_css"),
						"", "", "");

				// get the collection id and then the shortName of the
				// collection
				document.setCollectionId(rs.getLong(constants.getDocumentCollectionID()));
				document.setCollectionShortName(getCollectionShortNameById(document.getCollectionId()));
				return document;
			} else
				throw new NoEntryException(id);
		} catch (SQLException e) {
			throw e;
		} catch (NoEntryException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	public int getBiggestIdFromDocuments() {
		Statement stmt = null;
		ResultSet rs = null;
		int size = 0;

		try {
			stmt = con.createStatement();
			String query = "SELECT MAX(" + constants.getDocumentId() + ") FROM " + constants.getDocuments();
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				size = rs.getInt("MAX(" + constants.getDocumentId() + ")");
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
		return size;

	}

	public List<DocumentData> getMillionDocumentData(int start) {
		List<DocumentData> documentDataList = new ArrayList<DocumentData>();

		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement();
			String query = "SELECT " + constants.getDocumentId() + ", " + constants.getIdOriginal() + ", "
					+ constants.getTitle() + " FROM " + constants.getDocuments() + " WHERE " + constants.getDocumentId()
					+ " >= " + start + " AND " + constants.getDocumentId() + " < " + (start + 1000000);
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				documentDataList.add(new DocumentData(rs.getString(constants.getTitle()),
						rs.getInt(constants.getDocumentId()), rs.getString(constants.getIdOriginal())));
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
		return documentDataList;
	}
}