package org.mrdlib.database;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.mrdlib.Constants;
import org.mrdlib.DocumentData;
import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.DocumentSet;
import org.mrdlib.display.RootElement;
import org.mrdlib.display.Snippet;
import org.mrdlib.display.StatusReport;
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

	public static int numberOfOpenConnections = 0;
	private Connection con = null;
	private Constants constants = new Constants();
	Context ctx = null;
	// stores the length of the database fields to check for truncation error
	private Map<String, Integer> lengthMap = new HashMap<String, Integer>();

	public DBConnection(String type) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		// get all the lengths of the database fields and store it in a map
		try {
			if (type.equals("jar"))
				createConnectionJar();
			else if (type.equals("tomcat"))
				createConnectionTomcat();
			else
				createConnectionJar();
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
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
				if (rs2 != null)
					rs2.close();
				if (rs3 != null)
					rs3.close();
			} catch (SQLException e) {
				throw e;
			}
			numberOfOpenConnections++;
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
		numberOfOpenConnections--;
	}

	/**
	 * creates connection from Connection Pool (configured in the tomcat config
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
			if (con == null)
				System.out.println("No connection");
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * creates connection to database if no pool is needed
	 * 
	 * @throws Exception
	 */
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
			person = new Person(Integer.parseInt(authorID + ""), rs.getString(constants.getFirstname()),
					rs.getString(constants.getMiddlename()), rs.getString(constants.getSurname()),
					rs.getString(constants.getUnstructured()));

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

		// query to select the collectionName
		String query = "SELECT " + constants.getCollectionID() + " FROM " + constants.getCollections() + " WHERE "
				+ constants.getCollectionShortName() + " = '" + collectionName + "'";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			// get first collection id
			if (rs.next())
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
	 * @param i
	 * @return a list of authors
	 * @throws Exception
	 */
	public List<Person> getPersonsByDocumentID(int i) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		List<Person> persons = new ArrayList<Person>();

		// query to select all author of a given document
		String query = "SELECT " + constants.getPersonIDInDocPers() + " FROM " + constants.getDocPers() + " WHERE "
				+ constants.getDocumentIDInDocPers() + " = '" + i + "'";

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
				List<Person> authors = getPersonsByDocumentID((rs.getInt(constants.getDocumentId())));
				for (int i = 0; i < authors.size(); i++)
					joiner.add(authors.get(i).getName());

				authorNames = joiner.toString();
				// encode special characters like ae, oe, ue or others to html
				// entitys
				// authorNames = StringEscapeUtils.escapeHtml4(authorNames);
				// title =
				// StringEscapeUtils.escapeHtml4(rs.getString(constants.getTitle()));
				// publishedIn =
				// StringEscapeUtils.escapeHtml4(rs.getString(constants.getPublishedId()));

				title = rs.getString(constants.getTitle());
				publishedIn = rs.getString(constants.getPublishedId());

				// create a new document with values from the database
				document = new DisplayDocument("", String.valueOf(rs.getLong(constants.getDocumentId())),
						rs.getString(constants.getIdOriginal()), 666, title, authorNames, publishedIn,
						rs.getInt(constants.getYear()), "", "", "");

				// get the collection id and then the shortName of the
				// collection
				document.setLanguage(rs.getString(constants.getLanguage()));
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

	/**
	 * Get the number of documents present in database (by searching for highest
	 * ID
	 * 
	 * @return biggest document id present in database
	 */
	public int getBiggestIdFromDocuments() {
		Statement stmt = null;
		ResultSet rs = null;
		int size = 0;

		try {
			stmt = con.createStatement();
			// query for returnning biggest id
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

	/**
	 * 
	 * Get all documents in a specified range for batching
	 * 
	 * @param start,
	 *            first id to start with
	 * @param batchsize,
	 *            number of documents to retrieve
	 * @return a list of documentData with id's between start and
	 *         start+batchsize
	 */
	public List<DocumentData> getDocumentDataInBatches(int start, int batchsize) {
		List<DocumentData> documentDataList = new ArrayList<DocumentData>();
		DocumentData newDocument = new DocumentData();

		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement();
			// query for getting Id, originalId and Title between ids start and
			// start+batchsize
			String query = "SELECT " + constants.getDocumentId() + ", " + constants.getIdOriginal() + ", "
					+ constants.getTitle() + ", " + constants.getYear() + " FROM " + constants.getDocuments()
					+ " WHERE " + constants.getDocumentId() + " >= " + start + " AND " + constants.getDocumentId()
					+ " < " + (start + batchsize);
			rs = stmt.executeQuery(query);

			// add the retrieved documentData to the list
			while (rs.next()) {
				newDocument = new DocumentData(rs.getString(constants.getTitle()), rs.getInt(constants.getDocumentId()),
						rs.getString(constants.getIdOriginal()));
				newDocument.setYear(rs.getInt(constants.getYear()));
				if (rs.wasNull())
					newDocument.setYear(-1);
				documentDataList.add(newDocument);
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

	/**
	 * 
	 * Insert a row to the external_identifiers database
	 * 
	 * @param id,
	 *            id from the corresponding document
	 * @param externalName,
	 *            type of the externalId (eg ISBN)
	 * @param externalId,
	 *            value of the external id
	 */
	public void writeIdentifiersInDatabase(int id, String externalName, String externalId) {
		PreparedStatement stmt = null;
		String query = "";
		try {

			// query to insert the external id's
			query = "INSERT IGNORE INTO " + constants.getExternalIds() + " (" + constants.getDocumentIdInExternalIds()
					+ ", " + constants.getExternalName() + ", " + constants.getExternalId() + ") VALUES (" + id + ", '"
					+ externalName + "', '" + externalId + "');";

			stmt = con.prepareStatement(query);
			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * Insert a row to the bibliometrices Table
	 * 
	 * @param id,
	 *            id from the corresponding document
	 * @param metric,
	 *            the metric to insert (eg total_count)
	 * @param dataType,
	 *            the dataType of the metric (eg readers, citations)
	 * @param category,
	 *            the category of the metric (eg for mendeley: user_role,
	 *            country, subdiscipline)
	 * @param subtype,
	 *            the subtype of the metric (eg for mendeley: physics, germany,
	 *            professor)
	 * @param value,
	 *            the value of the metric (eg number of readers/citations,
	 *            h-index of readers/citations etc)
	 * @param value,
	 *            the datasourcce of the metric (eg mendeley, google scholar,
	 *            microsoft acamdemics)
	 */
	public void writeBibliometricsInDatabase(int id, String metric, String dataType, String category, String subtype,
			double value, String dataSource) {
		PreparedStatement stmt = null;
		String query = "";

		try {
			query = "INSERT IGNORE INTO " + constants.getBibDocuments() + " ("
					+ constants.getDocumentIdInBibliometricDoc() + ", " + constants.getMetric() + ", "
					+ constants.getDataType() + ", " + constants.getMetricValue() + ", " + constants.getDataSource()
					+ ") VALUES (" + id + ", '" + metric + "', '" + dataType + "', " + value + ", '" + dataSource
					+ "');";

			stmt = con.prepareStatement(query);
			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void writeSubBibliometricsInDatabase(int id, String category, String subtype, int value) {
		PreparedStatement stmt = null;
		try {

			String query = "INSERT INTO " + constants.getBibDocumentsSubCounts() + " ("
					+ constants.getBibliometricDocIdInBibliometricDocSubCount() + ", " + constants.getCountry() + ", "
					+ constants.getCountryCount() + ", " + constants.getSubdiscipline() + ", "
					+ constants.getSubdisciplineCount() + ", " + constants.getAcademicStatus() + ", "
					+ constants.getAcademicStatusCount() + ", " + constants.getSubjectArea() + ", "
					+ constants.getSubjectAreaCount() + ", " + constants.getUserRole() + ", "
					+ constants.getUserRoleCount() + ") VALUES (" + id + "? ? ? ? ? ? ? ? ? ?);";

			stmt = con.prepareStatement(query);

			if (category.equals("reader_count_by_country")) {
				stmt.setString(1, subtype);
				stmt.setInt(2, value);
				stmt.setNull(3, java.sql.Types.VARCHAR);
				stmt.setNull(4, java.sql.Types.INTEGER);
				stmt.setNull(5, java.sql.Types.VARCHAR);
				stmt.setNull(6, java.sql.Types.INTEGER);
				stmt.setNull(7, java.sql.Types.VARCHAR);
				stmt.setNull(8, java.sql.Types.INTEGER);
				stmt.setNull(9, java.sql.Types.VARCHAR);
				stmt.setNull(10, java.sql.Types.INTEGER);
			} else if (category.equals("reader_count_by_subdiscipline")) {
				stmt.setString(3, subtype);
				stmt.setInt(4, value);
				stmt.setNull(1, java.sql.Types.VARCHAR);
				stmt.setNull(2, java.sql.Types.INTEGER);
				stmt.setNull(5, java.sql.Types.VARCHAR);
				stmt.setNull(6, java.sql.Types.INTEGER);
				stmt.setNull(7, java.sql.Types.VARCHAR);
				stmt.setNull(8, java.sql.Types.INTEGER);
				stmt.setNull(9, java.sql.Types.VARCHAR);
				stmt.setNull(10, java.sql.Types.INTEGER);
			} else if (category.equals("reader_count_by_academic_status")) {
				stmt.setString(5, subtype);
				stmt.setInt(6, value);
				stmt.setNull(1, java.sql.Types.VARCHAR);
				stmt.setNull(2, java.sql.Types.INTEGER);
				stmt.setNull(3, java.sql.Types.VARCHAR);
				stmt.setNull(4, java.sql.Types.INTEGER);
				stmt.setNull(7, java.sql.Types.VARCHAR);
				stmt.setNull(8, java.sql.Types.INTEGER);
				stmt.setNull(9, java.sql.Types.VARCHAR);
				stmt.setNull(10, java.sql.Types.INTEGER);
			} else if (category.equals("reader_count_by_subject_area")) {
				stmt.setString(7, subtype);
				stmt.setInt(8, value);
				stmt.setNull(1, java.sql.Types.VARCHAR);
				stmt.setNull(2, java.sql.Types.INTEGER);
				stmt.setNull(3, java.sql.Types.VARCHAR);
				stmt.setNull(4, java.sql.Types.INTEGER);
				stmt.setNull(5, java.sql.Types.VARCHAR);
				stmt.setNull(6, java.sql.Types.INTEGER);
				stmt.setNull(9, java.sql.Types.VARCHAR);
				stmt.setNull(10, java.sql.Types.INTEGER);
			} else if (category.equals("reader_count_by_user_role")) {
				stmt.setString(9, subtype);
				stmt.setInt(10, value);
				stmt.setNull(1, java.sql.Types.VARCHAR);
				stmt.setNull(2, java.sql.Types.INTEGER);
				stmt.setNull(3, java.sql.Types.VARCHAR);
				stmt.setNull(4, java.sql.Types.INTEGER);
				stmt.setNull(5, java.sql.Types.VARCHAR);
				stmt.setNull(6, java.sql.Types.INTEGER);
				stmt.setNull(7, java.sql.Types.VARCHAR);
				stmt.setNull(8, java.sql.Types.INTEGER);
			}

			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int logRecommendations(DisplayDocument document, DocumentSet documentset) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int recommendationId = -1;
		int bibliometricReRankingId = -1;
		int recommendationAlgorithmId = -1;

		bibliometricReRankingId = logReRankingBibliometrics(documentset, document.getBibId());

		recommendationAlgorithmId = logRecommendationAlgorithm(documentset, document);

		try {
			String query = "INSERT INTO " + constants.getRecommendations() + " ("
					+ constants.getDocumentIdInRecommendations() + ", "
					+ constants.getRecommendationSetIdInRecommendations() + ", " + constants.getBibliometricReRankId()
					+ ", " + constants.getRankReal() + ", " + constants.getRankCurrent() + ", "
					+ constants.getAlgorithmId() + ") VALUES (" + document.getDocumentId() + ", "
					+ documentset.getRecommendationSetId() + ", ? , '" + document.getSuggestedRank() + "', '"
					+ document.getSuggestedRank() + "', '" + recommendationAlgorithmId + "');";

			stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

			if (bibliometricReRankingId != -1) {
				stmt.setInt(1, bibliometricReRankingId);
			} else
				stmt.setNull(1, java.sql.Types.INTEGER);

			stmt.executeUpdate();

			// get the autogenerated key back
			rs = stmt.getGeneratedKeys();
			if (rs.next())
				recommendationId = rs.getInt(1);

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return recommendationId;
	}

	@SuppressWarnings("resource")
	private int logRecommendationAlgorithm(DocumentSet documentset, DisplayDocument document) throws Exception {
		// TODO Auto-generated method stub
		Statement stmt = null;
		ResultSet rs = null;
		HashMap<String, String> recommenderDetails = documentset.getRDG().loggingInfo;
		int recommendationAlgorithmId = -1;
		try {
			String query = "SELECT " + constants.getRecommendationAlgorithmId() + " FROM "
					+ constants.getRecommendationAlgorithm() + " WHERE ";
			for (String key : recommenderDetails.keySet()) {
				if (key != "name") {
					query += (key + "='" + recommenderDetails.get(key) + "' AND ");
				}
			}
			query = query.replaceAll(" AND $", "");
			// System.out.println(query);
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			if (rs.next()) {
				recommendationAlgorithmId = rs.getInt(constants.getRecommendationAlgorithmId());
			} else {
				query = "INSERT INTO " + constants.getRecommendationAlgorithm() + "(";
				String columns = "";
				String values = "";
				for (String key : recommenderDetails.keySet()) {
					if (key != "name") {
						columns += (key + ", ");
						values += ("'" + recommenderDetails.get(key) + "', ");
					}
				}
				columns = columns.replaceAll(", $", " ");
				values = values.replaceAll(", $", " ");
				query += (columns + ") VALUES(" + values + ")");
				// System.out.println(query);
				stmt = con.createStatement();
				stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
				rs = stmt.getGeneratedKeys();
				if (rs.next())
					recommendationAlgorithmId = rs.getInt(1);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return recommendationAlgorithmId;
	}

	private int logReRankingBibliometrics(DocumentSet documentset, int bibId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int reRankingBibId = -1;

		try {
			String query = "INSERT INTO " + constants.getReRankingBibliometrics() + " (" + constants.getNumberFromSolr()
					+ ", " + constants.getReRankingMethod() + ", " + constants.getPercentageWithBibliometrics() + ", "
					+ constants.getBibIdInReRank() + ") VALUES ('" + documentset.getNumberOfSolrRows() + "', '"
					+ documentset.getRankingMethod() + "', '" + documentset.getPercentageRankingValue() + "', ?);";

			stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			if (bibId > 0)
				stmt.setInt(1, bibId);
			else
				stmt.setNull(1, java.sql.Types.INTEGER);

			stmt.executeUpdate();

			// get the autogenerated key back
			rs = stmt.getGeneratedKeys();
			if (rs.next())
				reRankingBibId = rs.getInt(1);

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return reRankingBibId;
	}

	public int logEvent(String documentId, Long requestTime, RootElement rootElement, Boolean clicked)
			throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int loggingId = -1;
		String statusCode = "";
		String debugDetails = "";

		if (rootElement.getStatusReportSet().getSize() > 1) {
			for (int i = 0; i < rootElement.getStatusReportSet().getSize(); i++)
				debugDetails = debugDetails
						+ rootElement.getStatusReportSet().getStatusReportList().get(i).getDebugDetails();
			statusCode = "207";
		} else {
			StatusReport statusReport = rootElement.getStatusReportSet().getStatusReportList().get(0);
			statusCode = statusReport.getStatusCode() + "";
			debugDetails = statusReport.getDebugDetails();
		}

		try {
			String query = "INSERT INTO " + constants.getLoggings() + " (" + constants.getRequest() + ", "
					+ constants.getDocumentIdInLogging() + ", " + constants.getRequestReceived() + ", "
					+ constants.getResponseDelivered() + ", " + constants.getStatusCode() + ", "
					+ constants.getDebugDetails() + ") VALUES (";
			if (clicked)
				query += "'url_for_recommended_document'";
			else
				query += "'related_documents'";
			query += ", " + documentId + ", '" + new Timestamp(requestTime) + "', '"
					+ new Timestamp(System.currentTimeMillis()) + "', '" + statusCode + "', ?);";

			stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

			if (debugDetails == null || debugDetails.isEmpty()) {
				stmt.setNull(1, java.sql.Types.VARCHAR);
			} else
				stmt.setString(1, debugDetails);

			stmt.executeUpdate();

			// get the autogenerated key back
			rs = stmt.getGeneratedKeys();
			if (rs.next())
				loggingId = rs.getInt(1);

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return loggingId;
	}

	public DocumentSet logRecommendationDelivery(String documentId, Long requestTime, RootElement rootElement)
			throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int recommendationSetId = -1;
		int loggingId = -1;
		DocumentSet documentset = rootElement.getDocumentSet();
		String accessKeyString = "mdl" + requestTime;
		String accessKeyHash = "";

		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(accessKeyString.getBytes(), 0, accessKeyString.length());
			accessKeyHash = new BigInteger(1, m.digest()).toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		}

		loggingId = logEvent(documentId, requestTime, rootElement, false);

		try {
			String query = "INSERT INTO " + constants.getRecommendationSets() + " ("
					+ constants.getLoggingIdInRecommendationSets() + ", " + constants.getDeliveredRecommendations()
					+ ", " + constants.getTrigger() + ", " + constants.getAccessKey() + ") VALUES (" + loggingId + ", "
					+ documentset.getSize() + ", 'system', '" + accessKeyHash + "');";

			stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			stmt.executeUpdate();

			// get the autogenerated key back
			rs = stmt.getGeneratedKeys();
			if (rs.next())
				recommendationSetId = rs.getInt(1);

			documentset.setRecommendationSetId(recommendationSetId + "");

			for (int i = 0; i < documentset.getSize(); i++) {
				DisplayDocument current = documentset.getDocumentList().get(i);
				current.setRecommendationId(logRecommendations(current, documentset) + "");
				current.setAccessKeyHash(accessKeyHash);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return documentset;
	}

	/**
	 * Utility method to easily access the document_id given the
	 * recommendation_id from the recommendations table
	 * 
	 * @param recommendationId
	 *            the recommendation_id
	 * @return
	 * @throws Exception
	 *             if SQL errors occur
	 */
	public String getDocIdFromRecommendation(String recommendationId) throws Exception {
		String docId = "dummy";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			String query = "SELECT * FROM " + constants.getRecommendations() + " WHERE "
					+ constants.getRecommendationId() + " = '" + recommendationId + "'";
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				docId = rs.getString(constants.getDocumentIdInRecommendations());
			} else {
				throw new NoEntryException(recommendationId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NoEntryException f) {
			throw f;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return docId;
	}

	/**
	 * Utility method to verify the accesskey provided by the user against the
	 * one present in our database for that recommendation_id
	 * 
	 * @param recoId
	 *            the recommendation_id for which we need to check the accessKey
	 * @param accessKey
	 *            the access key hash provided by the user
	 * @return True if access key matches, false if not
	 * @throws SQLException
	 */
	public Boolean checkAccessKey(String recoId, String accessKey) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		String accessKeyInDb = "";
		try {
			stmt = con.createStatement();
			String query = "SELECT " + constants.getAccessKey() + " FROM " + constants.getRecommendationSets()
					+ " WHERE " + constants.getRecommendationSetsId() + " IN (SELECT "
					+ constants.getRecommendationSetIdInRecommendations() + " FROM " + constants.getRecommendations()
					+ " WHERE " + constants.getRecommendationId() + " = " + recoId + ")";

			rs = stmt.executeQuery(query);
			if (rs.next()) {
				accessKeyInDb = rs.getString(constants.getAccessKey());
			} else {
				throw new NoEntryException(recoId);
			}
			return accessKeyInDb.contentEquals(accessKey);
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (NoEntryException f) {
			throw f;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	/**
	 * This method updates the clicked column in the recommendations table of
	 * our database with the timestamp at which the click was recorded and also
	 * creates a log entry in the logging table
	 * 
	 * @param recommendationId
	 *            the recommendation_id for which the click needs to be recorded
	 * @param documentId
	 *            the document_id corresponding to the recommendation
	 * @param requestTime
	 *            the time at which the click was recorded
	 * @param rootElement
	 *            In order to check the status of the current request and verify
	 *            that there were no errors upstream
	 * @return true if logged successfully, exception in every other case
	 * @throws SQLException
	 */
	public Boolean logRecommendationClick(String recommendationId, String documentId, Long requestTime,
			RootElement rootElement) throws Exception {
		Statement stmt = null;
		int loggingId = -1;
		try {
			stmt = con.createStatement();
			String query = "UPDATE " + constants.getRecommendations() + " SET " + constants.getClicked() + " = '"
					+ new Timestamp(requestTime) + "' WHERE " + constants.getRecommendationId() + " = "
					+ recommendationId;

			stmt.executeUpdate(query);
			loggingId = logEvent(documentId, requestTime, rootElement, true);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				throw e;
			}
		}

		return loggingId > 0;
	}

	public DocumentData getRankingValue(String documentId, String metric, String dataType, String dataSource) {
		Statement stmt = null;
		ResultSet rs = null;
		int metricValue = -1;
		int bibId = -1;
		DocumentData document = new DocumentData();

		String query = "SELECT " + constants.getBibliometricDocumentsId() + ", " + constants.getMetricValue() + " FROM "
				+ constants.getBibDocuments() + " WHERE " + constants.getDocumentIdInBibliometricDoc() + " = '"
				+ documentId + "' AND " + constants.getMetric() + " = '" + metric + "' AND " + constants.getDataType()
				+ " = '" + dataType + "' AND " + constants.getDataSource() + " = '" + dataSource + "';";

		try {

			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			if (rs.next()) {
				metricValue = rs.getInt(constants.getMetricValue());
				bibId = rs.getInt(constants.getBibliometricDocumentsId());
			}

			document.setBibId(bibId);
			document.setRankingValue(metricValue);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return document;
	}

	public int getRankingValueAuthor(String authorId, String metric, String dataType, String dataSource) {
		Statement stmt = null;
		ResultSet rs = null;
		int metricValue = -1;

		String query = "SELECT " + constants.getPersonIdInBibliometricPers() + ", "
				+ constants.getBibliometricPersonsId() + ", " + constants.getMetricValuePers() + " FROM "
				+ constants.getBibPersons() + " WHERE " + constants.getMetricPers() + " = '" + metric + "' AND "
				+ constants.getDataTypePers() + " = '" + dataType + "' AND " + constants.getDataSourcePers() + " = '"
				+ dataSource + "' AND " + constants.getPersonID() + " = " + authorId + ";";

		try {

			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			if (rs.next())
				metricValue = rs.getInt(constants.getMetricValuePers());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return metricValue;
	}

	public List<DocumentData> getRankingValueDocuments(String metric, String dataType, String dataSource) {
		List<DocumentData> documentDataList = new ArrayList<DocumentData>();
		DocumentData newDocument = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement();

			String query = "SELECT " + constants.getDocumentIdInBibliometricDoc() + ", "
					+ constants.getBibliometricDocumentsId() + ", " + constants.getMetricValue() + " FROM "
					+ constants.getBibDocuments() + " WHERE " + constants.getMetric() + " = '" + metric + "' AND "
					+ constants.getDataType() + " = '" + dataType + "' AND " + constants.getDataSource() + " = '"
					+ dataSource + "';";

			rs = stmt.executeQuery(query);

			// add the retrieved documentData to the list
			while (rs.next()) {
				newDocument = new DocumentData();
				newDocument.setBibId(rs.getInt(constants.getBibliometricDocumentsId()));
				newDocument.setRankingValue(rs.getInt(constants.getMetricValue()));
				newDocument.setId(rs.getInt(constants.getDocumentIdInBibliometricDoc()));
				documentDataList.add(newDocument);
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

	public List<Person> getRankingValueAuthorsInBatches(String metric, String dataType, String dataSource, int start,
			int batchsize) {
		List<Person> authorDataList = new ArrayList<Person>();
		Person newPerson = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement();

			String query = "SELECT " + constants.getPersonIdInBibliometricPers() + ", "
					+ constants.getBibliometricPersonsId() + ", " + constants.getMetricValuePers() + " FROM "
					+ constants.getBibPersons() + " WHERE " + constants.getMetricPers() + " = '" + metric + "' AND "
					+ constants.getDataTypePers() + " = '" + dataType + "' AND " + constants.getDataSourcePers()
					+ " = '" + dataSource + "' AND " + constants.getPersonID() + " >= " + start + " AND "
					+ constants.getPersonID() + " < " + (start + batchsize) + ";";

			rs = stmt.executeQuery(query);

			// add the retrieved documentData to the list
			while (rs.next()) {
				newPerson = new Person();
				newPerson.setRankingValue(rs.getInt(constants.getMetricValuePers()));
				newPerson.setId(rs.getInt(constants.getPersonIdInBibliometricPers()));
				authorDataList.add(newPerson);
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
		return authorDataList;
	}

	public DocumentData getDocumentDataBy(String coloumnName, String id) throws Exception {
		DocumentData document = new DocumentData();
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement();

			// get all information of a document stored in a database by the
			// value of a custom coloumn
			String query = "SELECT " + constants.getTitle() + ", " + constants.getIdOriginal() + ", "
					+ constants.getYear() + " FROM " + constants.getDocuments() + " WHERE " + coloumnName + " = '" + id
					+ "'";

			rs = stmt.executeQuery(query);

			// if there is a document
			if (rs.next()) {
				document.setId(Integer.parseInt(id));
				document.setTitle(rs.getString(constants.getTitle()));
				document.setOriginalId(rs.getString(constants.getIdOriginal()));
				document.setYear(rs.getInt(constants.getYear()));
				if (rs.wasNull())
					document.setYear(-1);

			} else
				throw new NoEntryException(id);
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
		return document;
	}

	public List<Person> getAllPersonsInBatches(int start, int batchsize) {
		List<Person> personList = new ArrayList<Person>();
		Person person = null;
		Statement stmt = null;
		ResultSet rs = null;

		// the query to get the person
		String query = "SELECT " + constants.getPersonID() + "," + constants.getFirstname() + ","
				+ constants.getMiddlename() + "," + constants.getSurname() + "," + constants.getUnstructured()
				+ " FROM " + constants.getPersons() + " WHERE " + constants.getPersonID() + " >= " + start + " AND "
				+ constants.getPersonID() + " < " + (start + batchsize) + ";";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				person = new Person(rs.getInt(constants.getPersonID()), rs.getString(constants.getFirstname()),
						rs.getString(constants.getMiddlename()), rs.getString(constants.getSurname()),
						rs.getString(constants.getUnstructured()));
				personList.add(person);
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
		return personList;
	}

	public List<DocumentData> getDocumentsByPersonId(int id) {
		Statement stmt = null;
		ResultSet rs = null;
		List<DocumentData> documents = new ArrayList<DocumentData>();

		// query to select all documents of a given author
		String query = "SELECT " + constants.getDocumentIDInDocPers() + " FROM " + constants.getDocPers() + " WHERE "
				+ constants.getPersonIDInDocPers() + " = '" + id + "'";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			// for each document obtained from the database, add it to the list
			// of
			// documents which will be returned
			while (rs.next()) {
				// for each id obtained, get the complete document and store it
				// in
				// the list
				documents.add(
						getDocumentDataBy(constants.getDocumentId(), rs.getString(constants.getDocumentIDInDocPers())));
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
		return documents;
	}

	public void writeAuthorBibliometricsInDatabase(int id, String metric, String dataType, String category,
			String subtype, double value, String dataSource) {
		PreparedStatement stmt = null;
		String query = "";

		try {
			query = "INSERT IGNORE INTO " + constants.getBibPersons() + " (" + constants.getPersonIdInBibliometricPers()
					+ ", " + constants.getMetricPers() + ", " + constants.getDataTypePers() + ", "
					+ constants.getMetricValuePers() + ", " + constants.getDataSourcePers() + ") VALUES (" + id + ", '"
					+ metric + "', '" + dataType + "', " + value + ", '" + dataSource + "');";

			stmt = con.prepareStatement(query);
			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int getBiggestIdFromAuthors() {
		Statement stmt = null;
		ResultSet rs = null;
		int size = 0;

		try {
			stmt = con.createStatement();
			// query for returnning biggest id
			String query = "SELECT MAX(" + constants.getPersonID() + ") FROM " + constants.getPersons();
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

	public DocumentSet getStereotypeRecommendations(DisplayDocument requestDoc, int numberOfRelatedDocs)
			throws Exception {
		// TODO Auto-generated method stub
		Statement stmt = null;
		ResultSet rs = null;
		String query = "";

		try {
			stmt = con.createStatement();

			query = "SELECT " + constants.getDocumentIdinStereotypeRecommendations() + " FROM "
					+ constants.getStereotypeRecommendations() + " ORDER BY RAND() LIMIT "
					+ Integer.toString(numberOfRelatedDocs);

			rs = stmt.executeQuery(query);
			DocumentSet documentSet = new DocumentSet();
			documentSet.setSuggested_label("Related Articles");
			documentSet.setRequestedDocument(requestDoc);

			while (rs.next()) {
				DisplayDocument relDocument = getDocumentBy(constants.getDocumentId(),
						rs.getString(constants.getDocumentIdinStereotypeRecommendations()));
				relDocument.setSuggestedRank(rs.getRow());
				String fallback_url = "";
				// HARDCODED FOR COMPATABILITY
				relDocument.setSolrScore(1.00);
				if (relDocument.getCollectionShortName().equals(constants.getGesis()))
					fallback_url = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());

				// url = "http://api.mr-dlib.org/trial/recommendations/" +
				// relDocument.getRecommendationId() +
				// "/original_url/&access_key=" +"hash"
				// +"&format=direct_url_forward";

				// relDocument.setClickUrl(url);
				relDocument.setFallbackUrl(fallback_url);
				// add it to the collection
				documentSet.addDocument(relDocument);

			}
			return documentSet;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}

	}

	public int getMinimumNumberOfKeyphrases(String documentId, String gramity, String source) throws Exception {
		// TODO Auto-generated method stub
		Statement stmt = null;
		ResultSet rs = null;
		String template = "SELECT COUNT(*) AS count FROM " + constants.getKeyphrases() + " WHERE "
				+ constants.getDocumentIdInKeyphrases() + "=" + documentId + " AND " + constants.getGramity() + "=?"
				+ " AND " + constants.getSourceInKeyphrases() + "="
				+ (source.equals("title") ? "'title'" : "'title_and_abstract'");
		// System.out.println(template);
		try {
			stmt = con.createStatement();
			switch (gramity) {
			case "allgrams": {
				Integer[] values = { 0, 0, 0 };
				for (int i = 1; i < 4; i++) {
					String query = template.replace("?", Integer.toString(i));
					rs = stmt.executeQuery(query);
					if (rs.next())
						values[i - 1] = rs.getInt("count");
				}
				// System.out.println(values);
				return Collections.min(Arrays.asList(values));
			}
			case "unibi": {
				Integer[] values = { 0, 0 };
				for (int i = 1; i < 3; i++) {
					String query = template.replace("?", Integer.toString(i));
					rs = stmt.executeQuery(query);
					if (rs.next())
						values[i - 1] = rs.getInt("count");
				}
				return Collections.min(Arrays.asList(values));
			}
			case "bitri": {
				Integer[] values = { 0, 0 };
				for (int i = 2; i < 4; i++) {
					String query = template.replace("?", Integer.toString(i));
					rs = stmt.executeQuery(query);
					if (rs.next())
						values[i - 2] = rs.getInt("count");
				}
				return Collections.min(Arrays.asList(values));
			}
			case "unitri": {
				Integer[] values = { 0, 0 };
				String query = template.replace("?", Integer.toString(1));
				rs = stmt.executeQuery(query);
				if (rs.next()) {
					values[0] = rs.getInt("count");
				}
				query = template.replace("?", Integer.toString(3));
				rs = stmt.executeQuery(query);
				if (rs.next())
					values[1] = rs.getInt("count");
				return Collections.min(Arrays.asList(values));
			}
			case "unigrams": {
				String query = template.replace("?", Integer.toString(1));
				rs = stmt.executeQuery(query);
				if (rs.next())
					return rs.getInt("count");
			}
			case "bigrams": {
				String query = template.replace("?", Integer.toString(2));
				rs = stmt.executeQuery(query);
				if (rs.next())
					return rs.getInt("count");
			}
			case "trigrams": {
				String query = template.replace("?", Integer.toString(3));
				rs = stmt.executeQuery(query);
				if (rs.next())
					return rs.getInt("count");
			}
			}
		} catch (Exception e) {
			System.out.println(e);
			throw e;
		}
		return -1;
	}

	public String getAbstractDetails(DisplayDocument requestDocument) throws Exception {
		// TODO Auto-generated method stub
		Statement stmt = null;
		ResultSet rs = null;
		String query = "SELECT `" + constants.getAbstractLanguage() + "` AS lang FROM " + constants.getAbstracts()
				+ " WHERE " + constants.getAbstractDocumentId() + " = " + requestDocument.getDocumentId();

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				return rs.getString("lang");
			} else {
				return "NONE";
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}
}