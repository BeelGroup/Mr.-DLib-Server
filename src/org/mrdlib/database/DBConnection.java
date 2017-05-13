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
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;
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

import org.mrdlib.api.manager.Constants;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.api.response.RootElement;
import org.mrdlib.api.response.Statistics;
import org.mrdlib.api.response.StatusReport;
import org.mrdlib.partnerContentManager.core.JSONDocument;
import org.mrdlib.partnerContentManager.gesis.Abstract;
import org.mrdlib.partnerContentManager.gesis.Person;
import org.mrdlib.partnerContentManager.gesis.XMLDocument;
import org.mrdlib.recommendation.algorithm.AlgorithmDetails;

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

	public DBConnection(String type) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		try {
			// choose the type of the database connection. Used by tomcat or by
			// a jar file?
			if (type.equals("jar"))
				createConnectionJar();
			else if (type.equals("tomcat"))
				createConnectionTomcat();
			else
				createConnectionJar();
			stmt = con.createStatement();
			stmt.executeQuery("SET NAMES 'utf8'");

			// get all the lengths of the database fields and store it in a map
			System.out.println(constants.getDocuments());
			rs = stmt.executeQuery("SHOW COLUMNS FROM " + constants.getDocuments());
			fillMap(rs);
			rs2 = stmt.executeQuery("SHOW COLUMNS FROM " + constants.getAbstracts());
			fillMap(rs2);
			// rs3 = stmt.executeQuery("SHOW COLUMNS FROM " +
			// constants.getPersons());
			// fillMap(rs3);
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
		}
	}

	public Connection getConnection() {
		return con;
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
	@Override
	protected void finalize() throws Throwable {
		con.close();
		super.finalize();
	}

	public void close() throws SQLException {
		con.close();
	}

	/**
	 * creates connection from Connection Pool (configured in the tomcat config
	 * files)
	 * 
	 * @throws Exception
	 */
	public Connection createConnectionTomcat() throws Exception {
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:comp/env");
			DataSource ds = (DataSource) envContext.lookup("jdbc/mrdlib");
			con = ds.getConnection();
		} catch (Exception e) {
			System.out.println("Exception in Database connection via tomcat");
			if (con == null)
				System.out.println("No connection");
			e.printStackTrace();
			throw e;
		}
		return con;
	}

	/**
	 * creates connection to database if no pool is needed
	 * 
	 * @throws Exception
	 */
	public Connection createConnectionJar() throws Exception {
		try {
			System.out.println("DB: " + constants.getDb());
			
			Class.forName(constants.getDbClass());
			con = DriverManager.getConnection(constants.getUrl() + constants.getDb(), constants.getUser(),
					constants.getPassword());
		} catch (Exception e) {
			System.out.println("Exception in Database connection via jar");
			e.printStackTrace();
		}
		return con;
	}

	/**
	 * Please fill me!
	 * 
	 * @param metric
	 * @param dataType
	 * @param dataSource
	 * @return
	 * @throws Exception
	 */

	public int getBibId(String metric, String dataType, String dataSource) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		int bibId = -1;

		try {
			// Select query for lookup in the database
			stmt = con.createStatement();
			String query = "SELECT " + constants.getBibliometricId() + " FROM " + constants.getBibliometrics()
					+ " WHERE " + constants.getMetric() + "='" + metric + "' AND " + constants.getDataType() + "='"
					+ dataType + "' AND " + constants.getDataSource() + "='" + dataSource + "'";

			rs = stmt.executeQuery(query);

			if (rs.next()) {
				bibId = rs.getInt(constants.getBibliometricId());
			}

		} catch (Exception e) {
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
		return bibId;
	}

	/**
	 * 
	 * write the author Bibliometric in the database
	 * 
	 * @param int,
	 *            id of the author
	 * @param String,
	 *            metric (eg "simple_count")
	 * @param String,
	 *            data_type (eg "readership")
	 * @param String,
	 *            datasource (eg "mendeley")
	 * @param double,
	 *            value of the bibliometric
	 * @throws Exception
	 */
	public void writeAuthorBibliometricsInDatabase(int id, String metric, String dataType, String dataSource,
			double value) {
		PreparedStatement stmt = null;
		String query = "";
		int bibId = -1;

		try {
			bibId = getBibId(metric, dataType, dataSource);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (bibId == -1)
			System.out.println("new Combination: " + metric + ", " + dataType + ", " + dataSource);

		try {
			// insertion query
			query = "INSERT INTO " + constants.getBibPersons() + " (" + constants.getPersonIdInBibliometricPers() + ", "
					+ constants.getBibliometricIdInBibliometricPers() + ", " + constants.getMetricValuePers()
					+ ") VALUES (" + id + ", " + bibId + ", " + value + ");";

			stmt = con.prepareStatement(query);
			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * write the author Bibliometric in the database
	 * 
	 * @param int,
	 *            id of the author
	 * @param bibId,
	 *            bibliometric id
	 * @param double,
	 *            value of the bibliometric
	 * @throws Exception
	 */
	public void writeAuthorBibliometricsInDatabase(int id, int bibId, double value) {
		PreparedStatement stmt = null;
		String query = "";

		try {
			// insertion query
			query = "INSERT INTO " + constants.getBibPersons() + " (" + constants.getPersonIdInBibliometricPers() + ", "
					+ constants.getBibliometricIdInBibliometricPers() + ", " + constants.getMetricValuePers()
					+ ") VALUES (" + id + ", " + bibId + ", " + value + ");";

			stmt = con.prepareStatement(query);
			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
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

			if (rs.next()) {
				// create a new object person with the retrieved data
				person = new Person(Integer.parseInt(authorID + ""), rs.getString(constants.getFirstname()),
						rs.getString(constants.getMiddlename()), rs.getString(constants.getSurname()),
						rs.getString(constants.getUnstructured()));
			}

		} catch (Exception e) {
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
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		return authorKey;
	}

	/**
	 * stores a author in a database if he is not already present and gives back
	 * the auto generated id of him. If he exists (which means there is another
	 * person with exactly the same name), the id of this already present person
	 * is given back
	 * 
	 * @param JSON
	 *            document, the related document which is currently processed to
	 *            trace error back
	 * @param author,
	 *            the author who has to be inserted
	 * @return the id the (inserted or retrieved) author
	 * @throws SQLException
	 */
	public Long addPersonToDbIfNotExists(JSONDocument document, Person author) throws SQLException {
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
					System.out.println(
							document.getDocumentPath() + ": " + document.getIdentifier() + "SetIfNulladdPersonToDB");
					e.printStackTrace();
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
			System.out.println(document.getDocumentPath() + ": " + document.getIdentifier() + "addPersonToDB1");
			throw sqle;
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getIdentifier() + "addPersonToDB2");
			e.printStackTrace();
			throw e;
		} finally {
			try {
				stateAuthorExists.close();
				if (rs != null)
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
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	/**
	 * 
	 * This method inserts the person - document relation to the database
	 * 
	 * @param JSON
	 *            document for trace back the error
	 * @param documentId
	 * @param authorId
	 * @param rank,
	 *            location of the naming of the author of the respective
	 *            document
	 * @throws Exception
	 */
	public void addPersonDocumentRelation(JSONDocument document, Long documentId, Long authorId, int rank)
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
			System.out.println(document.getDocumentPath() + ": " + document.getIdentifier() + "addPersonDocRel");
			throw e;
		} finally {
			try {
				if (stmt != null)
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
				if (stmt != null)
					stmt.close();
				if (rs != null)
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
	 * this method prepares parts of a given prepared statement to handle null
	 * values and high comma
	 * 
	 * @param JSON
	 *            document, for error backtracing
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
	public <T> PreparedStatement SetIfNull(JSONDocument document, PreparedStatement stmt, T value, int index,
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
						System.out.println(document.getDocumentPath() + ": " + document.getIdentifier() + ": Truncate"
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
				if ((int) value == 0)
					stmt.setNull(index, java.sql.Types.INTEGER);
				// otherwise use real value
				else
					stmt.setObject(index, value);
				// otherwise use real value
			} else
				stmt.setObject(index, value);

		} catch (SQLException e) {
			System.out.println(document.getDocumentPath() + ": " + document.getIdentifier() + " SetIfNullMethod");
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
				if (stmt != null)
					stmt.close();
				if (rs != null)
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
			
			System.out.println("TITLE = " + document.getTitle());
			
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
	 * insert a JSONDocument to the database with all the related information
	 * (like authors and so on) if it not already exists (based on the original
	 * id of the cooperation partner)
	 * 
	 * @param JSON
	 *            document, the parsed XMLDocument
	 * @throws Exception
	 */
	public void insertDocument(JSONDocument document) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement stateQueryDoc = null;
		Long docKey = null;
		LinkedHashSet<Person> authors = document.getAuthors();
		Long[] authorKey = new Long[authors.size()];
		if (document.getAuthors().size() == 0)
			System.out.println(document.getDocumentPath() + ": " + document.getIdentifier() + ": No Authors!");

		// query to check if document already exists
		String docExists = "SELECT " + constants.getDocumentId() + " FROM " + constants.getDocuments() + " WHERE "
				+ constants.getIdOriginal() + " = '" + document.getIdentifier() + "'";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(docExists);
			// if there is a document with the same original id
			if (rs.next()) {
				System.out.println(document.getDocumentPath() + ": " + document.getIdentifier() + ": Double Entry");
				return;
			}
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getIdentifier() + "insertDoc");
			throw e;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				System.out
						.println(document.getDocumentPath() + ": " + document.getIdentifier() + "insertDocOutsideCon");
				throw e;
			}
		}

		// if the document doesn't exists, insert it including authors,
		// collection,
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
			// Long collectionId = getCollectionIDByName(document,
			// document.getCollection());

			stateQueryDoc = con.prepareStatement(queryDoc, Statement.RETURN_GENERATED_KEYS);

			// set the values of the documents with the wrapper method which
			// checks for null values etc
			SetIfNull(document, stateQueryDoc, document.getIdentifier(), 1, "string", constants.getIdOriginal());
			// SetIfNull(document, stateQueryDoc, collectionId, 2, "long",
			// constants.getDocumentCollectionID());
			SetIfNull(document, stateQueryDoc, document.getRepository(), 2, "long",
					constants.getDocumentCollectionID());
			SetIfNull(document, stateQueryDoc, document.getTitle(), 3, "string", constants.getTitle());
			SetIfNull(document, stateQueryDoc, document.getCleanTitle(), 4, "string", constants.getTitleClean());
			SetIfNull(document, stateQueryDoc, null, 5, "string", constants.getPublishedId());
			SetIfNull(document, stateQueryDoc, null, 6, "string", constants.getLanguage());
			// SetIfNull(document, stateQueryDoc, document.getYear(), 7, "int",
			// constants.getYear());
			SetIfNull(document, stateQueryDoc, document.getYear(), 7, "int", constants.getYear());
			SetIfNull(document, stateQueryDoc, document.getType(), 8, "string", constants.getType());
			// SetIfNull(document, stateQueryDoc,
			// document.getKeywordsAsString(), 9, "string",
			// constants.getKeywords());
			SetIfNull(document, stateQueryDoc, null, 9, "string", constants.getKeywords());

			stateQueryDoc.executeUpdate();

			// get the key of the inserted document
			try (ResultSet generatedKeys = stateQueryDoc.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					docKey = generatedKeys.getLong(1);
				} else {
					System.out.println(document.getDocumentPath() + ": " + document.getIdentifier());
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
			System.out.println(document.getDocumentPath() + ": " + document.getIdentifier() + "insertDocAddAbsAddPer1");
			throw sqle;
		} catch (Exception e) {
			System.out.println(document.getDocumentPath() + ": " + document.getIdentifier() + "insertDocAddAbsAddPer2");
			e.printStackTrace();
			throw e;
		} finally {
			try {
				stateQueryDoc.close();
			} catch (SQLException e) {
				System.out.println(
						document.getDocumentPath() + ": " + document.getIdentifier() + "insertDocAddAbsAddPer3");
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
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	/**
	 * insert an abstract to the abstract table
	 * 
	 * @param JSON
	 *            document, for error backtracing
	 * @param abstr,
	 *            the corresponding Abstract object
	 * @param docKey,
	 *            the document key from the database
	 * @throws Exception
	 */
	private void addAbstractToDocument(JSONDocument document, Abstract abstr, Long docKey) throws Exception {
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
			System.out.println(document.getDocumentPath() + ": " + document.getIdentifier() + "addAbsToDoc");
			throw e;
		} finally {
			try {
				if (stmt != null)
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
	public List<Person> getPersonsByDocumentID(String i) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		List<Person> persons = new ArrayList<Person>();
		Person person = null;

		// query to select all author of a given document
		String query = "SELECT " + constants.getPersonIDInDocPers() + ", " + constants.getRank() + " FROM "
				+ constants.getDocPers() + " WHERE " + constants.getDocumentIDInDocPers() + " = '" + i + "'";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			// for each person obtained from the database, add it to the list of
			// authors which will be returned
			while (rs.next()) {
				// for each id obtained, get the complete person and store it in
				// the list
				person = getPersonById(rs.getLong(constants.getPersonIDInDocPers()));
				person.setPosition(rs.getInt(constants.getRank()));
				if (person != null)
					persons.add(person);
			}

			persons = persons.stream().sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
					.collect(Collectors.toList());

		} catch (Exception e) {
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

			if (rs.next())
				name = rs.getString(constants.getCollectionShortName());
		} catch (Exception e) {
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
		return name;
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
				List<Person> authors = getPersonsByDocumentID((rs.getString(constants.getDocumentId())));
				for (int i = 0; i < authors.size(); i++)
					joiner.add(authors.get(i).getName());

				authorNames = joiner.toString();

				title = rs.getString(constants.getTitle());
				publishedIn = rs.getString(constants.getPublishedId());

				// create a new document with values from the database
				document = new DisplayDocument("", String.valueOf(rs.getLong(constants.getDocumentId())),
						rs.getString(constants.getIdOriginal()), 0, title, authorNames, publishedIn,
						rs.getInt(constants.getYear()), "", "", "", constants);
				if (rs.wasNull())
					document.setYear(-1);

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
	 * 
	 * Gets a document with all information from documents, without authors and
	 * collection name
	 * 
	 * @param coloumnName
	 *            for which should be searched (please use original id or id)
	 * @param id,
	 *            either original id or id
	 * @return the (first) retrieved Document
	 * @throws Exception
	 */
	public DisplayDocument getPureDocumentBy(String coloumnName, String id) throws Exception {
		DisplayDocument document = null;
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

				title = rs.getString(constants.getTitle());
				publishedIn = rs.getString(constants.getPublishedId());

				// create a new document with values from the database
				document = new DisplayDocument(String.valueOf(rs.getLong(constants.getDocumentId())),
						rs.getString(constants.getIdOriginal()), title, publishedIn, rs.getInt(constants.getYear()), "",
						constants);
				if (rs.wasNull())
					document.setYear(-1);

				// get the collection id and then the shortName of the
				// collection
				document.setLanguage(rs.getString(constants.getLanguage()));
				document.setCollectionId(rs.getLong(constants.getDocumentCollectionID()));
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
				if (stmt != null)
					stmt.close();
				if (rs != null)
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
	public List<DisplayDocument> getDocumentDataInBatches(int start, int batchsize) {
		List<DisplayDocument> documentDataList = new ArrayList<DisplayDocument>();
		DisplayDocument newDocument = new DisplayDocument();

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
				newDocument = new DisplayDocument(rs.getString(constants.getTitle()),
						rs.getString(constants.getDocumentId()), rs.getString(constants.getIdOriginal()));
				newDocument.setYear(rs.getInt(constants.getYear()));
				if (rs.wasNull())
					newDocument.setYear(-1);
				documentDataList.add(newDocument);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
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
				if (stmt != null)
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
	public void writeBibliometricsInDatabase(String id, String metric, String dataType, String category, String subtype,
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
			// System.out.println(query);
			// stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * logs the single recommendations
	 * 
	 * @param DisplayDocument
	 *            document, the recommendation to log
	 * @param documentSet,
	 *            needed for metadata, ids, and further processing
	 * @return int, id of the created recommendation log
	 * @throws Exception
	 */
	/*
	 * public int logRecommendations(DisplayDocument document, DocumentSet
	 * documentset) throws Exception { PreparedStatement stmt = null; ResultSet
	 * rs = null; int recommendationId = -1; int bibliometricReRankingId = -1;
	 * int recommendationAlgorithmId = -1;
	 * 
	 * // logs the reranking data and get back id bibliometricReRankingId =
	 * logReRankingBibliometrics(documentset, documentset.getBibliometricId());
	 * 
	 * // logs the algorithm data and get back id recommendationAlgorithmId =
	 * logRecommendationAlgorithm(documentset);
	 * 
	 * try { // insertion query String query = "INSERT INTO " +
	 * constants.getRecommendations() + " (" +
	 * constants.getDocumentIdInRecommendations() + ", " +
	 * constants.getRecommendationSetIdInRecommendations() // + // ", // " // +
	 * // constants.getBibliometricReRankId() + ", " + constants.getRankReal() +
	 * ", " + constants.getRankCurrent() + ", " + constants.getAlgorithmId() +
	 * ", " + constants.getTextRelevanceScoreInRecommendations() + ") VALUES ("
	 * + document.getDocumentId() + ", " + documentset.getRecommendationSetId()
	 * + ", ? , '" + document.getSuggestedRank() + "', '" +
	 * document.getSuggestedRank() + "', '" + recommendationAlgorithmId + "', '"
	 * + document.getRelevanceScoreFromAlgorithm() + "');";
	 * 
	 * stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
	 * 
	 * // insert 0 if no bibliometric is present if (bibliometricReRankingId !=
	 * -1) { stmt.setInt(1, bibliometricReRankingId); } else stmt.setNull(1,
	 * java.sql.Types.INTEGER);
	 * 
	 * stmt.executeUpdate();
	 * 
	 * // get the autogenerated key back rs = stmt.getGeneratedKeys(); if
	 * (rs.next()) recommendationId = rs.getInt(1);
	 * 
	 * } catch (Exception e) { throw e; } finally { try { if (stmt != null)
	 * stmt.close(); if (rs != null) rs.close(); } catch (SQLException e) {
	 * throw e; } } return recommendationId; }
	 * 
	 * /** Helper function to log the recommendationAlgorithmId in the
	 * recommendations table Searches using the fields in the
	 * algorithmLoggingInfo hashmap for an exact match for an algorithm in the
	 * recommendationAlgorithms table in the database, and returns the id if
	 * present.
	 * 
	 * If not, adds the entry into the table and returns the newly created row's
	 * id
	 * 
	 * This method is for the case where all the documents in a document set all
	 * have been chosen using the same recommendation algorithm
	 * 
	 * @param documentset DocumentSet which contains the recommendations that
	 * have to be logged
	 * 
	 * @return the recommendationAlgorithm id
	 * 
	 * @throws Exception
	 */
	/*
	 * private int logRecommendationAlgorithm(DocumentSet documentset) throws
	 * Exception { Statement stmt = null; ResultSet rs = null;
	 * 
	 * // get the hashmap which has the details of the recommendation algorithm
	 * HashMap<String, String> recommenderDetails =
	 * documentset.getRDG().algorithmLoggingInfo;
	 * 
	 * int recommendationAlgorithmId = -1; try {
	 * 
	 * // search for an exact match of the algorithm in the table String query =
	 * "SELECT " + constants.getRecommendationAlgorithmId() + " FROM " +
	 * constants.getRecommendationAlgorithm() + " WHERE "; for (String key :
	 * recommenderDetails.keySet()) { if (key != "name" && key != "typeOfGram")
	 * { query += (key + "='" + recommenderDetails.get(key) + "' AND "); } }
	 * query = query.replaceAll(" AND $", "");
	 * 
	 * stmt = con.createStatement(); rs = stmt.executeQuery(query);
	 * 
	 * // if found, get the id of the exact match if (rs.next()) {
	 * recommendationAlgorithmId =
	 * rs.getInt(constants.getRecommendationAlgorithmId()); } else { if (stmt !=
	 * null) stmt.close(); if (rs != null) rs.close();
	 * 
	 * // Insert the row into the table query = "INSERT INTO " +
	 * constants.getRecommendationAlgorithm() + "("; String columns = ""; String
	 * values = ""; for (String key : recommenderDetails.keySet()) { if (key !=
	 * "name") { columns += (key + ", "); values += ("'" +
	 * recommenderDetails.get(key) + "', "); } } columns = columns.replaceAll(
	 * ", $", " "); values = values.replaceAll(", $", " "); query += (columns +
	 * ") VALUES(" + values + ")");
	 * 
	 * stmt = con.createStatement(); stmt.executeUpdate(query,
	 * Statement.RETURN_GENERATED_KEYS); rs = stmt.getGeneratedKeys();
	 * 
	 * // Get back the generated keys if (rs.next()) recommendationAlgorithmId =
	 * rs.getInt(1); }
	 * 
	 * } catch (Exception e) { e.printStackTrace(); throw e; } finally { try {
	 * if (stmt != null) stmt.close(); if (rs != null) rs.close(); } catch
	 * (SQLException e) { throw e; } }
	 * 
	 * // return the algorithm Id return recommendationAlgorithmId; }
	 */

	/**
	 * 
	 * logs the bibliometric data
	 * 
	 * @param DisplayDocument
	 *            document, the recommendation where the bibliometric to log
	 *            belongs to
	 * 
	 * @param int,
	 *            the BibId
	 * 
	 * @return int, the created logging rerank bibliometric id
	 * 
	 * @throws Exception
	 */
	/*
	 * public int logReRankingBibliometrics(DocumentSet documentset, int bibId)
	 * throws Exception { PreparedStatement stmt = null; ResultSet rs = null;
	 * int reRankingBibId = -1;
	 * 
	 * try { // insertion query String query = "INSERT INTO " +
	 * constants.getReRankingBibliometrics() + " (" +
	 * constants.getNumberFromSolr() + ", " + constants.getReRankingMethod() +
	 * ", " + constants.getPercentageWithBibliometrics() + ", " +
	 * constants.getBibIdInReRank() + ") VALUES ('" +
	 * documentset.getNumberOfCandidatesToReRank() + "', '" +
	 * documentset.getReRankingCombination() + "', '" +
	 * documentset.getPercentageRankingValue() + "', ?);";
	 * 
	 * stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS); //
	 * if no bibId is present, set null if (bibId > 0) stmt.setInt(1, bibId);
	 * else stmt.setNull(1, java.sql.Types.INTEGER);
	 * 
	 * stmt.executeUpdate();
	 * 
	 * // get the autogenerated key back rs = stmt.getGeneratedKeys(); if
	 * (rs.next()) reRankingBibId = rs.getInt(1);
	 * 
	 * } catch (Exception e) { e.printStackTrace(); throw e; } finally { try {
	 * if (stmt != null) stmt.close(); } catch (SQLException e) { throw e; } }
	 * return reRankingBibId; }
	 */

	/**
	 * 
	 * logs the event in the logging table
	 * 
	 * @param referenceId,
	 *            the reference for the request: documentId for recommendation
	 *            request, recommendationId for click_url request, titleSearchId
	 *            for recommendation by title
	 * @param Long,
	 *            time, where the request was registered
	 * @param RootElement,
	 *            the rootElement, where everything is stored
	 * @param String,
	 *            the type of request - request_for_recommendations,
	 *            url_redirect, search_by_title
	 * @return int, id of the created event
	 * @throws Exception
	 */
	private int logEvent(String referenceId, RootElement rootElement, String requestType) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int loggingId = -1;
		String statusCode = "";
		String debugMessage = "";
		Boolean noEntryExceptionRecorded = false;
		Long requestTime = rootElement.getDocumentSet().getStartTime();

		String referenceColumnName = "";
		switch (requestType) {
		case "related_documents": {
			referenceColumnName = constants.getDocumentIdInLogging();
			break;
		}
		case "url_for_recommended_document": {
			referenceColumnName = constants.getRecommendationIdInLogging();
			if (rootElement.getStatusReportSet().getStatusReportList().get(0).getStatusCode() == 404)
				noEntryExceptionRecorded = true;

			break;
		}
		case "search_by_title": {
			referenceColumnName = constants.getTitleSearchIdInLogging();
			break;
		}
		}

		// if there occured multiple errors error
		if (rootElement.getStatusReportSet().getSize() > 1) {
			for (int i = 0; i < rootElement.getStatusReportSet().getSize(); i++) {
				// gather every message
				debugMessage = debugMessage
						+ rootElement.getStatusReportSet().getStatusReportList().get(i).getDebugMessage();
			}
			// set status code to mutiple errors
			statusCode = "207";
		} else {
			// if theres only one report (either error or 200, gather it
			StatusReport statusReport = rootElement.getStatusReportSet().getStatusReportList().get(0);
			statusCode = statusReport.getStatusCode() + "";

			debugMessage = statusReport.getDebugMessage();
		}
		System.out.println(debugMessage);

		try {
			// insertion query
			String query = "INSERT INTO " + constants.getLoggings() + " (" + constants.getRequest() + ", "
					+ referenceColumnName + ", " + constants.getRequestReceived() + ", "
					+ constants.getResponseDelivered() + ", " + constants.getProcessingTimeTotal() + ", "
					+ constants.getStatusCode() + ", " + constants.getDebugDetails() + ", " + constants.getIpHash()
					+ ", " + constants.getIp() + ") VALUES ('";
			query += requestType + "', ?, '" + new Timestamp(requestTime) + "', '"
					+ new Timestamp(System.currentTimeMillis()) + "', '" + (System.currentTimeMillis() - requestTime)
					+ "', '" + statusCode + "',?, ?,?);";
			System.out.println(query);

			stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

			// if there was noEntryException, then set referenceId to NULL, else
			// carry on
			if (noEntryExceptionRecorded) {
				stmt.setNull(1, java.sql.Types.BIGINT);
			} else {
				stmt.setString(1, referenceId);
			}

			String ipAddress = rootElement.getDocumentSet().getIpAddress();
			stmt.setString(4, ipAddress);
			try {
				String saltedIp = "mld" + ipAddress;
				MessageDigest m = MessageDigest.getInstance("MD5");
				m.update(saltedIp.getBytes(), 0, saltedIp.length());
				String ipHash = new BigInteger(1, m.digest()).toString(16);
				stmt.setString(3, ipHash);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// if the debugMessage was empty, set database type to null

			if (debugMessage == null || debugMessage.isEmpty()) {
				stmt.setNull(2, java.sql.Types.VARCHAR);
			} else
				stmt.setString(2, debugMessage);

			stmt.executeUpdate();

			// get the autogenerated key back
			rs = stmt.getGeneratedKeys();
			if (rs.next())
				loggingId = rs.getInt(1);

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
		System.out.println("logging id is : " + loggingId);
		return loggingId;
	}

	/**
	 * 
	 * logs the recommendation set starting point for the logging process
	 * 
	 * @param documentId,
	 *            from the requested document
	 * @param Long,
	 *            time, where the request was registered
	 * @param RootElement,
	 *            the rootElement, where everything is stored
	 * @return documentset, with new logging metadata
	 * @throws Exception
	 */
	/*
	 * public DocumentSet logRecommendationDelivery(String documentId, Long
	 * requestTime, RootElement rootElement) throws Exception {
	 * PreparedStatement stmt = null; ResultSet rs = null; int
	 * recommendationSetId = -1; int loggingId = -1; DocumentSet documentset =
	 * rootElement.getDocumentSet(); String accessKeyString = "mdl" +
	 * requestTime; String accessKeyHash = "";
	 * 
	 * try { MessageDigest m = MessageDigest.getInstance("MD5");
	 * m.update(accessKeyString.getBytes(), 0, accessKeyString.length());
	 * accessKeyHash = new BigInteger(1, m.digest()).toString(16); } catch
	 * (Exception e) { e.printStackTrace(); }
	 * 
	 * loggingId = logEvent(documentId, requestTime, rootElement, false);
	 * 
	 * try { // insertion query String query = "INSERT INTO " +
	 * constants.getRecommendationSets() + " (" +
	 * constants.getLoggingIdInRecommendationSets() + ", " +
	 * constants.getNumberOfReturnedResults() + ", " +
	 * constants.getDeliveredRecommendations() + ", " + constants.getTrigger() +
	 * ", " + constants.getAccessKey() + ") VALUES (" + loggingId + ", " +
	 * documentset.getNumberOfReturnedResults() + ", " + documentset.getSize() +
	 * ", 'system', '" + accessKeyHash + "');";
	 * 
	 * System.out.println(query); stmt = con.prepareStatement(query,
	 * Statement.RETURN_GENERATED_KEYS); stmt.executeUpdate();
	 * 
	 * // get the autogenerated key back rs = stmt.getGeneratedKeys(); if
	 * (rs.next()) recommendationSetId = rs.getInt(1);
	 * 
	 * // set the generated key as recommendation set id
	 * documentset.setRecommendationSetId(recommendationSetId + "");
	 * 
	 * for (int i = 0; i < documentset.getSize(); i++) { DisplayDocument current
	 * = documentset.getDisplayDocument(i); // log each single recommendation
	 * current.setRecommendationId(logRecommendations(current, documentset) +
	 * ""); current.setAccessKeyHash(accessKeyHash); }
	 * 
	 * } catch (Exception e) { throw e; } finally { try { if (stmt != null)
	 * stmt.close(); } catch (SQLException e) { e.printStackTrace(); throw e; }
	 * } return documentset; }
	 */

	/**
	 * Utility method to easily access the document_id given the
	 * recommendation_id from the recommendations table
	 * 
	 * @param recommendationId
	 *            the recommendation_id
	 * 
	 * @return
	 * 
	 * @throws Exception
	 *             if SQL errors occur
	 */
	public String getDocIdFromRecommendation(String recommendationId) throws Exception {
		String docId = "dummy";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// Select query for lookup in the database
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
	 * one present in our database for that recommendation_id or
	 * recommendationSetId
	 * 
	 * @param Id
	 *            the id for which we need to check the accessKey if
	 *            recommendationSet=true, then Id = recommendationSetId else it
	 *            is recommendationId
	 * @param accessKey
	 *            the access key hash provided by the user
	 * @param recommendationSet
	 *            if recommendationSet=true, then Id = recommendationSetId else
	 *            it is recommendationId
	 * @return True if access key matches, false if not
	 * @throws SQLException
	 */
	public Boolean checkAccessKey(String Id, String accessKey, boolean recommendationSet)
			throws SQLException, NoEntryException {
		Statement stmt = null;
		ResultSet rs = null;
		String accessKeyInDb = "";
		try {
			stmt = con.createStatement();

			// Select query to lookup accesskey for the recommendationId in the
			// database
			String query = "SELECT " + constants.getAccessKey() + " FROM " + constants.getRecommendationSets()
					+ " WHERE " + constants.getRecommendationSetsId() + " IN (";
			if (recommendationSet) {
				query += Id + ")";
			} else {
				query += "SELECT " + constants.getRecommendationSetIdInRecommendations() + " FROM "
						+ constants.getRecommendations() + " WHERE " + constants.getRecommendationId() + " = " + Id
						+ ")";
			}
			System.out.println(query);
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				accessKeyInDb = rs.getString(constants.getAccessKey());
			} else {
				throw new NoEntryException(Id, "recommendation");
			}

			// Compare accessKey in our database against the one which was
			// submitted in the clickURL
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
	 * @param rootElement
	 *            In order to check the status of the current request and verify
	 *            that there were no errors upstream
	 * @param accessKeyCheck
	 * @return true if logged successfully, exception in every other case
	 * @throws SQLException
	 */
	public Boolean logRecommendationClick(String recommendationId, RootElement rootElement, Boolean accessKeyCheck)
			throws Exception {
		Statement stmt = null;
		int loggingId = -1;
		try {

			loggingId = logEvent(recommendationId, rootElement, "url_for_recommended_document");

			if (accessKeyCheck) {
				stmt = con.createStatement();

				// Update query to set the time at which a recommendation was
				// clicked
				String query = "UPDATE " + constants.getRecommendations() + " SET " + constants.getClicked()
						+ " =  IF( " + constants.getClicked() + " IS NULL, '"
						+ new Timestamp(rootElement.getDocumentSet().getStartTime()) + "', " + constants.getClicked()
						+ ") WHERE " + constants.getRecommendationId() + " = " + recommendationId;

				stmt.executeUpdate(query);
				int clickCount = updateClicksInRecommendationSet(recommendationId);
				if (clickCount == 0) {
					System.out.println("Something went wrong in the updateClicksInRecommendationSet function");
				}
			}

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

	/**
	 * please fill me!
	 * 
	 * @param recommendationId
	 * @return
	 */
	private int updateClicksInRecommendationSet(String recommendationId) {
		Statement stmt = null;
		ResultSet rs = null;
		int recommendationSetId = getRecommendationSetIdFromRecommendationId(recommendationId);
		if (recommendationSetId == -1)
			return 0;
		String updateQuery = "UPDATE " + constants.getRecommendationSets() + " SET click_count = (SELECT COUNT(*) FROM "
				+ constants.getRecommendations() + " WHERE " + constants.getRecommendationSetIdInRecommendations()
				+ "= " + recommendationSetId + " AND " + constants.getClicked() + " IS NOT NULL)  WHERE "
				+ constants.getRecommendationSetsId() + "=" + recommendationSetId;
		String query = "";
		String ctrUpdate = "";
		try {
			stmt = con.createStatement();
			stmt.executeUpdate(updateQuery);
			if (stmt != null)
				stmt.close();
			query = "SELECT click_count, " + constants.getDeliveredRecommendations() + " FROM "
					+ constants.getRecommendationSets() + " WHERE " + constants.getRecommendationSetsId() + " = "
					+ recommendationSetId;
			// System.out.println(query);
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				ctrUpdate = "UPDATE " + constants.getRecommendationSets() + " SET ctr ='"
						+ ((float) rs.getInt("click_count")
								/ (float) rs.getInt(constants.getDeliveredRecommendations()))
						+ "' WHERE " + constants.getRecommendationSetsId() + "=" + recommendationSetId;
			}
			if (stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
			stmt = con.createStatement();
			stmt.executeUpdate(ctrUpdate);
			return 1;
		} catch (SQLException e) {
			System.out.println(updateQuery);
			System.out.println(query);
			System.out.println(ctrUpdate);
			e.printStackTrace();
			return 0;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * PLease fill me!
	 * 
	 * @param recommendationId
	 * @return
	 */
	private int getRecommendationSetIdFromRecommendationId(String recommendationId) {
		Statement stmt = null;
		ResultSet rs = null;
		String query = "SELECT " + constants.getRecommendationSetIdInRecommendations() + " FROM "
				+ constants.getRecommendations() + " WHERE " + constants.getRecommendationId() + " ='"
				+ recommendationId + "'";
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				return rs.getInt(constants.getRecommendationSetIdInRecommendations());
			}
		} catch (SQLException e) {
			System.out.println(query);
			System.out.println("Didn't get recommendationSetId from Recommendations for id" + recommendationId);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	/*
	 * /**
	 * 
	 * get the ranking value (altmetric) from the database for a document
	 * 
	 * @param documentId, the belonging document, the altmetric is requested
	 * from
	 * 
	 * @param String, metric (eg "simple_count")
	 * 
	 * @param String, data_type (eg "readership")
	 * 
	 * @param String, datasource (eg "mendeley")
	 * 
	 * @return DisplayDocument, the document belonging to the id, with the
	 * attached ranking Value
	 * 
	 * @throws Exception
	 *
	 * public DisplayDocument getRankingValue(String documentId, String metric,
	 * String dataType, String dataSource) { Statement stmt = null; ResultSet rs
	 * = null; int metricValue = -1; int bibId = -1; DisplayDocument document =
	 * new DisplayDocument(constants);
	 * 
	 * // selection query String query = "SELECT " +
	 * constants.getBibliometricDocumentsId() + ", " +
	 * constants.getMetricValue() + " FROM " + constants.getBibDocuments() +
	 * " WHERE " + constants.getDocumentIdInBibliometricDoc() + " = '" +
	 * documentId + "' AND " + constants.getMetric() + " = '" + metric +
	 * "' AND " + constants.getDataType() + " = '" + dataType + "' AND " +
	 * constants.getDataSource() + " = '" + dataSource + "';";
	 * 
	 * try {
	 * 
	 * stmt = con.createStatement(); rs = stmt.executeQuery(query);
	 * 
	 * // get the data from the result set if (rs.next()) { metricValue =
	 * rs.getInt(constants.getMetricValue()); bibId =
	 * rs.getInt(constants.getBibliometricDocumentsId()); }
	 * 
	 * // add the data to the document document.setBibId(bibId);
	 * document.setRankingValue(metricValue);
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } finally { try { if (stmt
	 * != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
	 * } return document; }
	 */

	/**
	 * 
	 * get the ranking value (altmetric) from the database for a author
	 * 
	 * @param author,
	 *            the belonging author, the altmetric is requested from
	 * @param String,
	 *            metric (eg "simple_count")
	 * @param String,
	 *            data_type (eg "readership")
	 * @param String,
	 *            datasource (eg "mendeley")
	 * @return int, the requested metricValue
	 * @throws Exception
	 */
	public int getRankingValueAuthor(String authorId, String metric, String dataType, String dataSource)
			throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		int metricValue = -1;

		// selection query
		/*
		 * String query = "SELECT " + constants.getPersonIdInBibliometricPers()
		 * + ", " + constants.getBibliometricPersonsId() + ", " +
		 * constants.getMetricValuePers() + " FROM " + constants.getBibPersons()
		 * + " WHERE " + constants.getMetricPers() + " = '" + metric + "' AND "
		 * + constants.getDataTypePers() + " = '" + dataType + "' AND " +
		 * constants.getDataSourcePers() + " = '" + dataSource + "' AND " +
		 * constants.getPersonID() + " = " + authorId + ";";
		 */
		String query = "";

		try {

			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			// get the data from the result set
			if (rs.next())
				metricValue = rs.getInt(constants.getMetricValuePers());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return metricValue;
	}

	/**
	 * 
	 * get the ranking value (altmetric) from the database for every paper, that
	 * has a associated rankingValue
	 * 
	 * @param int,
	 *            bibliometricId
	 * @return DisplayDocument List, a list of all documents, that has a
	 *         rankingValue, with its associated data
	 * @throws Exception
	 */
	public List<DisplayDocument> getRankingValueDocuments(int bibliometricId) {
		List<DisplayDocument> documentList = new ArrayList<DisplayDocument>();
		DisplayDocument newDocument = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement();

			String query = "SELECT " + constants.getDocumentIdInBibliometricDoc() + ", "
					+ constants.getBibliometricDocumentsId() + ", " + constants.getMetricValue() + " FROM "
					+ constants.getBibDocuments() + " WHERE " + constants.getBibliometricIdInBibliometricDocument()
					+ " = '" + bibliometricId + "';";

			rs = stmt.executeQuery(query);

			// add the retrieved documentData to the list and add all the data
			while (rs.next()) {
				newDocument = new DisplayDocument();
				newDocument.setBibScore(rs.getInt(constants.getMetricValue()));
				newDocument.setDocumentId(rs.getString(constants.getDocumentIdInBibliometricDoc()));
				documentList.add(newDocument);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return documentList;
	}

	/**
	 * 
	 * get the ranking value (altmetric) from the database for authors in
	 * batch-sized chunks
	 * 
	 * @param String,
	 *            metric (eg "simple_count")
	 * @param String,
	 *            data_type (eg "readership")
	 * @param String,
	 *            datasource (eg "mendeley")
	 * @param int,
	 *            start id
	 * @param int,
	 *            bachtsize
	 * @return person List, list of batchsize of authors with associated
	 *         rankingValues
	 * @throws Exception
	 */
	public List<Person> getRankingValueAuthorsInBatches(int bibliometricId, int start, int batchsize) {
		List<Person> authorDataList = new ArrayList<Person>();
		Person newPerson = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement();

			// selection query

			String query = "SELECT " + constants.getPersonIdInBibliometricPers() + ", "
					+ constants.getBibliometricPersonsId() + ", " + constants.getMetricValuePers() + " FROM "
					+ constants.getBibPersons() + " WHERE " + constants.getBibliometricId() + " = '" + bibliometricId
					+ "' AND " + constants.getPersonID() + " >= " + start + " AND " + constants.getPersonID() + " < "
					+ (start + batchsize) + ";";

			rs = stmt.executeQuery(query);

			// add the retrieved person to the list
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
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return authorDataList;
	}

	/**
	 * 
	 * get the authors in batch-sized chunks
	 * 
	 * @param int,
	 *            start id
	 * @param int,
	 *            bachtsize
	 * @return person List, list of batchsize of authors
	 * @throws Exception
	 */
	public List<Person> getAllPersonsInBatches(int start, int batchsize) {
		List<Person> personList = new ArrayList<Person>();
		Person person = null;
		Statement stmt = null;
		ResultSet rs = null;

		// the query to get the persons
		String query = "SELECT " + constants.getPersonID() + "," + constants.getFirstname() + ","
				+ constants.getMiddlename() + "," + constants.getSurname() + "," + constants.getUnstructured()
				+ " FROM " + constants.getPersons() + " WHERE " + constants.getPersonID() + " >= " + start + " AND "
				+ constants.getPersonID() + " < " + (start + batchsize) + ";";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			// add the person to the list
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
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return personList;
	}

	/**
	 * 
	 * get the author ids in batch-sized chunks, who have a document which has a
	 * specified bibliometric
	 * 
	 * @param int,
	 *            start id
	 * @param int,
	 *            bachtsize
	 * @return Integer List, list of batchsize of author ids
	 * @throws Exception
	 */
	public List<Integer> getAllPersonsWithAssociatedDocumentsWithBibliometricInBatches(int start, int batchsize,
			int bibliometricId) {
		List<Integer> personList = new ArrayList<Integer>();
		Integer personId = null;
		Statement stmt = null;
		ResultSet rs = null;

		// the query to get the persons
		String query = "SELECT " + constants.getPersonIDInDocPers() + " FROM " + constants.getDocPers() + " DP JOIN "
				+ constants.getBibDocuments() + " BD ON DP." + constants.getDocumentIDInDocPers() + "= BD."
				+ constants.getDocumentIdInBibliometricDoc() + " WHERE "
				+ constants.getBibliometricIdInBibliometricDocument() + " = " + bibliometricId + " AND "
				+ constants.getPersonIDInDocPers() + " >= " + start + " AND " + constants.getPersonIDInDocPers() + " < "
				+ (start + batchsize) + " GROUP BY DP." + constants.getPersonIDInDocPers() + ";";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			// add the person to the list
			while (rs.next()) {
				personId = rs.getInt(constants.getPersonIDInDocPers());
				personList.add(personId);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return personList;
	}

	/**
	 * 
	 * get the author_ids in batch-sized chunks, which have a specified
	 * bibliometric id
	 * 
	 * @param bibliometric
	 *            id
	 * @param int,
	 *            start id
	 * @param int,
	 *            bachtsize
	 * @return person List, list of batchsize of authors
	 * @throws Exception
	 */
	public List<Person> getAllPersonsInBatchesIfBibliometricId(int bibliometricId, int start, int batchsize) {
		List<Person> personList = new ArrayList<Person>();
		Statement stmt = null;
		ResultSet rs = null;

		// the query to get the persons
		String query = "SELECT " + constants.getPersonIdInBibliometricPers() + " FROM " + constants.getBibPersons()
				+ " WHERE " + constants.getBibliometricIdInBibliometricPers() + " = " + bibliometricId + " AND "
				+ constants.getBibliometricPersonsId() + " >= " + start + " AND " + constants.getBibliometricPersonsId()
				+ " < " + (start + batchsize) + ";";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			// add the person to the list
			while (rs.next()) {
				personList.add(new Person(rs.getInt(constants.getPersonIdInBibliometricPers())));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return personList;
	}

	/**
	 * 
	 * get all Documents a specific author wrote
	 * 
	 * @param id,
	 *            the id of the person
	 * @return DisplayDocument List, list of all associated documents from this
	 *         author
	 * @throws Exception
	 */
	public DocumentSet getDocumentsByPersonId(int id) {
		Statement stmt = null;
		ResultSet rs = null;
		DocumentSet documents = new DocumentSet();

		// query to select all documents of a given author
		String query = "SELECT " + constants.getDocumentIDInDocPers() + " FROM " + constants.getDocPers() + " WHERE "
				+ constants.getPersonIDInDocPers() + " = '" + id + "'";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			// for each document obtained from the database, add it to the list
			// of documents which will be returned
			while (rs.next()) {
				// for each id obtained, get the complete document and store it
				// in the list
				documents.addDocument(
						getDocumentBy(constants.getDocumentId(), rs.getString(constants.getDocumentIDInDocPers())));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return documents;
	}

	/**
	 * 
	 * get the biggest author id (to know how big the table is)
	 * 
	 * @return int, biggest author id
	 */
	public int getBiggestIdFromAuthors() {
		Statement stmt = null;
		ResultSet rs = null;
		int size = 0;

		try {
			stmt = con.createStatement();
			// query for returnning biggest id
			String query = "SELECT MAX(" + constants.getPersonID() + ") FROM " + constants.getPersons();
			rs = stmt.executeQuery(query);

			// get the data from the result set
			while (rs.next()) {
				size = rs.getInt("MAX(" + constants.getPersonID() + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return size;
	}

	/**
	 * 
	 * get the biggest BibPers id, who has a bibliometric value for specified
	 * bibliometric id
	 * 
	 * @param bibliometric
	 *            id
	 * @return int, biggest BibPers id
	 */
	public int[] getRangeFromBibAuthors(int bibliometricId) {
		int[] range = new int[2];
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement();
			// query for returnning biggest id
			String query = "SELECT MAX(" + constants.getBibliometricPersonsId() + "), MIN("
					+ constants.getBibliometricPersonsId() + ") FROM " + constants.getBibPersons() + " WHERE "
					+ constants.getBibliometricIdInBibliometricPers() + " = " + bibliometricId;
			rs = stmt.executeQuery(query);

			// get the data from the result set
			while (rs.next()) {
				range[0] = rs.getInt("MIN(" + constants.getBibliometricPersonsId() + ")");
				range[1] = rs.getInt("MAX(" + constants.getBibliometricPersonsId() + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return range;
	}

	/**
	 * This method returns a subset of the stereotype set of documents which are
	 * stored in our database
	 * 
	 * @param requestDoc
	 *            document for which stereotype recommendations have been
	 *            requested
	 * @param numberOfRelatedDocs
	 *            how many to return in the document set
	 * @param algorithmLoggingInfo
	 * @return DocumentSet containing <code>numberOfRelatedDocs</code> number of
	 *         randomly chosen stereotype documents
	 * @throws Exception
	 */
	public DocumentSet getStereotypeRecommendations(DisplayDocument requestDoc, int numberOfRelatedDocs,
			AlgorithmDetails algorithmLoggingInfo) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		String query = "";
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			query = "SELECT " + constants.getDocumentIdinStereotypeRecommendations() + " FROM "
					+ constants.getStereotypeRecommendations() + " WHERE " + constants.getStereotypeCategory();
			if (algorithmLoggingInfo.getCategory().equals("mix")) {
				query += " NOT IN ('most_viewed', 'most_exported') ";
			} else {
				query += "='" + algorithmLoggingInfo.getCategory() + "'";
			}
			query += " ORDER BY RAND()";
			// System.out.println(query);
			rs = stmt.executeQuery(query);
			DocumentSet documentSet = new DocumentSet();
			documentSet.setSuggested_label("Related Articles");
			documentSet.setRequestedDocument(requestDoc);

			if (rs.last()) {
				int rows = rs.getRow();
				documentSet.setNumberOfReturnedResults(rows);
				rs.beforeFirst();
			}
			int i = 0;
			while (rs.next() && i < numberOfRelatedDocs) {
				DisplayDocument relDocument = getDocumentBy(constants.getDocumentId(),
						rs.getString(constants.getDocumentIdinStereotypeRecommendations()));
				relDocument.setSuggestedRank(rs.getRow());
				String fallback_url = "";

				// HARDCODED FOR COMPATABILITY
				relDocument.setRelevanceScoreFromAlgorithm(1.00);
				if (relDocument.getCollectionShortName().equals(constants.getGesis())) {
					if (constants.getEnvironment().equals("api"))
						fallback_url = constants.getGesisCollectionLink().concat(relDocument.getOriginalDocumentId());
					else
						fallback_url = constants.getGesisBetaCollectionLink()
								.concat(relDocument.getOriginalDocumentId());
				} else if (relDocument.getCollectionShortName().contains(constants.getCore()))
					fallback_url = constants.getCoreCollectionLink()
							.concat(relDocument.getOriginalDocumentId().split("-")[1]);
				relDocument.setFallbackUrl(fallback_url);
				documentSet.addDocument(relDocument);
				i++;
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

	/**
	 * Get the minimum basis for comparison using keyphrases. Example: doc A has
	 * 10 unigrams, 3 bigrams, and 2 trigrams We want to compare over unigrams
	 * and bigrams Then minimum basis is 3, because we have a maximum of 3
	 * bigrams that we can use
	 * 
	 * Returns -1 if one of the fields to be used for comparison has no entries
	 * for the particular document ex. Doc B has 3 unigrams, 1 bigram, and no
	 * trigram. If we want to compare using bigrams and trigrams, this method
	 * would return -1 as there are no trigram keyphrases associated with this
	 * document
	 * 
	 * @param documentId
	 *            documentId for which we need to calcualate minimum basis
	 * @param gramity
	 *            String which can take any of the following values: unigrams,
	 *            bigrams, trigrams, unibi, unitri, bitri, unibitri
	 * @param source
	 *            keyphrase source: titles only, or titles and abstracts
	 * @return int representing the minimum basis for comparison
	 * @throws Exception
	 *             if database connection fails
	 */
	public int getMinimumNumberOfKeyphrases(String documentId, String gramity, String source) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;

		String query = "SELECT " + constants.getGramity() + ", count FROM " + constants.getKeyphrases() + " WHERE "
				+ constants.getDocumentIdInKeyphrases() + "=" + documentId + " AND " + constants.getSourceInKeyphrases()
				+ "=" + (source.equals("title") ? "'title'" : "'title_and_abstract'");

		try {
			stmt = con.createStatement();
			// System.out.println(query);
			rs = stmt.executeQuery(query);
			switch (gramity) {

			// if allgrams, return minimum of unigrams, bigrams and trigrams
			case "unibitri": {
				Integer[] values = { 0, 0, 0 };

				while (rs.next())
					values[rs.getInt("gramity") - 1] = rs.getInt("count");
				return Collections.min(Arrays.asList(values));
			}

			// return minimum between unigrams and bigrams
			case "unibi": {
				Integer[] values = { 0, 0 };
				while (rs.next()) {
					int gramityNumber = rs.getInt("gramity");
					if (gramityNumber < 3)
						values[gramityNumber - 1] = rs.getInt("count");
				}
				return Collections.min(Arrays.asList(values));
			}

			// return minimum between bigrams and trigrams
			case "bitri": {
				Integer[] values = { 0, 0 };
				while (rs.next()) {
					int gramityNumber = rs.getInt("gramity");
					if (gramityNumber > 1)
						values[gramityNumber - 2] = rs.getInt("count");
				}
				return Collections.min(Arrays.asList(values));
			}

			// return minimum between unigrams and trigrams
			case "unitri": {
				Integer[] values = { 0, 0 };
				while (rs.next()) {
					int gramityNumber = rs.getInt("gramity");
					switch (gramityNumber) {
					case 1:
						values[0] = rs.getInt("count");
					case 3:
						values[1] = rs.getInt("count");
					}
				}
				return Collections.min(Arrays.asList(values));
			}

			// return unigram count
			case "unigram": {
				while (rs.next()) {
					if (rs.getInt("gramity") == 1)
						return rs.getInt("count");
				}
				return 0;
			}

			// return bigram count
			case "bigram": {
				while (rs.next()) {
					if (rs.getInt("gramity") == 2)
						return rs.getInt("count");

				}
				return 0;
			}

			// return trigram count
			case "trigram": {
				while (rs.next()) {
					if (rs.getInt("gramity") == 3)
						return rs.getInt("count");
				}
				return 0;
			}
			}
		} catch (Exception e) {
			System.out.println(e);
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
		return -1;
	}

	/**
	 * Get the language of the abstract, if recorded in the database
	 * 
	 * @param requestDocument
	 *            document for which we need the details about the abstract
	 * @return two character language code if abstract exists in our database.
	 *         ex.: 'en', 'de', else 'NONE'
	 * @throws Exception
	 */
	public String getAbstractDetails(DisplayDocument requestDocument) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;

		// Select query to lookup abstract language using the documentId from
		// the document_abstracts table
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
	 * Please fill me!
	 * 
	 * @param documentId
	 * @param rootElement
	 * @return
	 * @throws Exception
	 */
	public DocumentSet logRecommendationDeliveryNew(String referenceId, RootElement rootElement, Boolean requestByTitle)
			throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int recommendationSetId = -1;
		int loggingId = -1;
		DocumentSet documentset = rootElement.getDocumentSet();
		String accessKeyString = "mdl" + documentset.getStartTime();
		String accessKeyHash = "";

		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(accessKeyString.getBytes(), 0, accessKeyString.length());
			accessKeyHash = new BigInteger(1, m.digest()).toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		}
		documentset.setAccessKeyHash(accessKeyHash);
		loggingId = logEvent(referenceId, rootElement, requestByTitle ? "search_by_title" : "related_documents");

		if (documentset.getSize() > 0) {
			documentset = logRecommendationAlgorithmNew(documentset);

			try {
				// insertion query
				String query = "INSERT INTO " + constants.getRecommendationSets() + " ("
						+ constants.getLoggingIdInRecommendationSets() + ", " + "recommendation_algorithm_id" + ", "
						+ "fallback" + ", " + constants.getNumberOfReturnedResults() + ", "
						+ constants.getDeliveredRecommendations() + ", " + constants.getTrigger() + ", "
						+ constants.getPreparationTime() + ", " + constants.getRecFrameworkTime() + ", "
						+ constants.getPostProcessingTime() + ", " + constants.getAccessKey() + ", "
						+ constants.getMinimumRelevanceScoreDisplay() + ", "
						+ constants.getMaximumRelevanceScoreDisplay() + ", " + constants.getMeanRelevanceScoreDisplay()
						+ ", " + constants.getMedianRelevanceScoreDisplay() + ", "
						+ constants.getModeRelevanceScoreDisplay() + ", " + constants.getMinimumFinalScoreDisplay()
						+ ", " + constants.getMaximumFinalScoreDisplay() + ", " + constants.getMeanFinalScoreDisplay()
						+ ", " + constants.getMedianFinalScoreDisplay() + ", " + constants.getModeFinalScoreDisplay()
						+ ") VALUES (" + loggingId + ", " + documentset.getRecommendationAlgorithmId() + ", "
						+ (documentset.isFallback() ? "'Y'" : "'N'") + ", " + documentset.getNumberOfReturnedResults()
						+ ", " + documentset.getSize() + ", 'system', '" + documentset.getAfterAlgorithmChoosingTime()
						+ "', '" + documentset.getAfterAlgorithmExecutionTime() + "', '"
						+ documentset.getAfterRerankTime() + "', '" + accessKeyHash
						+ "', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

				stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

				for (Statistics currentStats : documentset.getDebugDetailsPerSet().getRankStats()) {

					if (currentStats.getType().equals("relevance")) {
						stmt.setDouble(1, currentStats.getRankVMin());
						stmt.setDouble(2, currentStats.getRankVMax());
						stmt.setDouble(3, currentStats.getRankVMean());
						stmt.setDouble(4, currentStats.getRankVMedian());

						if (currentStats.getRankVMode() == -1) {
							stmt.setNull(5, java.sql.Types.FLOAT);
						} else
							stmt.setDouble(5, currentStats.getRankVMode());

					} else if (currentStats.getType().equals("final")) {
						stmt.setDouble(6, currentStats.getRankVMin());
						stmt.setDouble(7, currentStats.getRankVMax());
						stmt.setDouble(8, currentStats.getRankVMean());
						stmt.setDouble(9, currentStats.getRankVMedian());

						if (currentStats.getRankVMode() == -1) {
							stmt.setNull(10, java.sql.Types.FLOAT);
						} else
							stmt.setDouble(10, currentStats.getRankVMode());
					}
				}

				stmt.executeUpdate();

				// get the autogenerated key back
				rs = stmt.getGeneratedKeys();
				if (rs.next())
					recommendationSetId = rs.getInt(1);

				// set the generated key as recommendation set id
				documentset.setRecommendationSetId(recommendationSetId + "");

				for (int i = 0; i < documentset.getSize(); i++) {
					DisplayDocument current = documentset.getDisplayDocument(i);
					// log each single recommendation
					current.setRecommendationId(logRecommendationsNew(current, documentset) + "");
				}

				if (!documentset.getReRankingCombination().equals("standard_only"))
					logRankingStatistics(documentset);

			} catch (Exception e) {
				throw e;
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw e;
				}
			}
		}
		return documentset;
	}

	/**
	 * Helper function to log the recommendationAlgorithmId in the
	 * recommendations table Searches using the fields in the
	 * algorithmLoggingInfo hashmap for an exact match for an algorithm in the
	 * recommendationAlgorithms table in the database, and returns the id if
	 * present.
	 * 
	 * If not, adds the entry into the table and returns the newly created row's
	 * id
	 * 
	 * This method is for the case where all the documents in a document set all
	 * have been chosen using the same recommendation algorithm
	 * 
	 * @param documentset
	 *            DocumentSet which contains the recommendations that have to be
	 *            logged
	 * @param requestByTitle
	 * @return the recommendationAlgorithm id
	 * @throws Exception
	 */
	private DocumentSet logRecommendationAlgorithmNew(DocumentSet documentset) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		int recommendationAlgorithmId = -1;

		// get the hashmap which has the details of the recommendation algorithm
		AlgorithmDetails recommenderDetails = documentset.getAlgorithmDetails();
		System.out.println(recommenderDetails.getQueryParser());

		String recommendationClass = recommenderDetails.getRecommendationClass();
		Boolean fallback = recommenderDetails.isFallback();
		int recommendationClassId = -1;

		try {
			switch (recommendationClass) {
			case "cbf": {
				recommendationClassId = getCbfId(recommenderDetails);
				break;
			}
			case "stereotype": {
				recommendationClassId = getStereotypesId(recommenderDetails);
				break;
			}
			case "most_popular": {
				recommendationClassId = getMostPopularId(recommenderDetails);
				break;
			}
			}
			int rerankingBibId = logBibReranking(documentset);

			// search for an exact match of the algorithm in the table
			String query = "SELECT " + constants.getRecommendationAlgorithmId() + " FROM "
					+ constants.getRecommendationAlgorithm() + " WHERE ";
			query += constants.getRecommendationClass() + "='" + recommenderDetails.getRecommendationClass() + "' AND "
					+ constants.getLanguageRestrictionInRecommenderAlgorithm() + "='"
					+ (recommenderDetails.isLanguageRestriction() ? "Y" : "N") + "' AND "
					+ constants.getBibReRankingApplied() + "=" + ((rerankingBibId > 0) ? "'Y'" : "'N'") + " AND "
					+ constants.getDesiredRecommendationsInRecommendationAlgorithms() + " = '"
					+ documentset.getDesiredNumberFromAlgorithm() + "'";
			if (rerankingBibId > 0) {
				query += " AND " /* constants.getBibReRankingId */ + "reranking_bibliometric_reranking_details" + "="
						+ Integer.toString(rerankingBibId);
			}
			switch (recommendationClass) {
			case "cbf": {
				query += " AND " + constants.getCbfId() + "=" + Integer.toString(recommendationClassId);
				break;
			}
			case "stereotype": {
				query += " AND " + constants.getStereotypeRecommendationDetailsId() + "="
						+ Integer.toString(recommendationClassId);
				break;
			}

			case "most_popular": {
				query += " AND " + constants.getMostPopularRecommendationDetailsId() + "="
						+ Integer.toString(recommendationClassId);
				break;
			}

			}
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			// if found, get the id of the exact match
			if (rs.next()) {
				recommendationAlgorithmId = rs.getInt(constants.getRecommendationAlgorithmId());
			} else {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();

				// Insert the row into the table
				query = "INSERT INTO " + constants.getRecommendationAlgorithm() + "(";
				String columns = "";
				String values = "";

				columns += constants.getRecommendationClass() + ", "
						+ constants.getLanguageRestrictionInRecommenderAlgorithm() + ", "
						+ constants.getBibReRankingApplied()
						+ ((rerankingBibId > 0) ? (", " + "reranking_bibliometric_reranking_details") : "")
						+ (recommendationClass.contains("random") ? ""
								: (", " + "recommendation_algorithm__details_" + recommendationClass + "_id"))
						+ ", " + constants.getShuffled() + ", "
						+ constants.getDesiredRecommendationsInRecommendationAlgorithms();
				values += "'" + recommenderDetails.getRecommendationClass() + "', "
						+ (recommenderDetails.isLanguageRestriction() ? "'same_language_only'" : "'N'") + ", "
						+ ((rerankingBibId > 0) ? "'Y'" : "'N'")
						+ ((rerankingBibId > 0) ? (", " + Integer.toString(rerankingBibId)) : "")
						+ (recommendationClass.contains("random") ? ""
								: (", " + Integer.toString(recommendationClassId)))
						+ ", " + (documentset.isShuffled() ? "'Y' " : "'N' ") + ", '"
						+ documentset.getDesiredNumberFromAlgorithm() + "'";
				query += (columns + ") VALUES(" + values + ")");

				stmt = con.createStatement();
				stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
				rs = stmt.getGeneratedKeys();

				// Get back the generated keys
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

		// return the algorithm Id
		documentset.setRecommendationAlgorithmId(recommendationAlgorithmId);
		documentset.setFallback(fallback);
		return documentset;
	}

	private void logRankingStatistics(DocumentSet documentset) throws Exception {
		PreparedStatement stmt = null;

		String query = "INSERT INTO " + constants.getRecommendationStatisticsReRankingBibliometric() + " ("
				+ constants.getRecommendationStatisticsRecommendationSetId() + ", "
				+ constants.getPercentageOfRecommendationsWithBibliometricDisplay() + ", "
				+ constants.getMinimumBibDisplay() + ", " + constants.getMaximumBibDisplay() + ", "
				+ constants.getMeanBibDisplay() + ", " + constants.getMedianBibDisplay() + ", "
				+ constants.getModeBibDisplay() + ", "
				+ constants.getPercentageOfRecommendationsWithBibliometricRerank() + ", "
				+ constants.getMinimumBibRerank() + ", " + constants.getMaximumBibRerank() + ", "
				+ constants.getMeanBibRerank() + ", " + constants.getMedianBibRerank() + ", "
				+ constants.getModeBibRerank() + ") VALUES ('" + documentset.getRecommendationSetId()
				+ "',?,?,?,?,?,?,?,?,?,?,?,?);";

		// System.out.println(query);

		try {
			stmt = con.prepareStatement(query);

			for (Statistics currentStats : documentset.getDebugDetailsPerSet().getRankStats()) {

				if (currentStats.getType().equals("bibliometricDisplay")) {
					stmt.setDouble(1, currentStats.getPercentageRankingValue());
					stmt.setDouble(2, currentStats.getRankVMin());
					stmt.setDouble(3, currentStats.getRankVMax());
					stmt.setDouble(4, currentStats.getRankVMean());
					stmt.setDouble(5, currentStats.getRankVMedian());

					if (currentStats.getRankVMode() == -1) {
						stmt.setNull(6, java.sql.Types.FLOAT);
					} else
						stmt.setDouble(6, currentStats.getRankVMode());

				} else if (currentStats.getType().equals("bibliometricRerank")) {
					stmt.setDouble(7, currentStats.getPercentageRankingValue());
					stmt.setDouble(8, currentStats.getRankVMin());
					stmt.setDouble(9, currentStats.getRankVMax());
					stmt.setDouble(10, currentStats.getRankVMean());
					stmt.setDouble(11, currentStats.getRankVMedian());

					if (currentStats.getRankVMode() == -1) {
						stmt.setNull(12, java.sql.Types.FLOAT);
					} else
						stmt.setDouble(12, currentStats.getRankVMode());
				}
			}

			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(query);
			throw e;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	/**
	 * 
	 * logs the single recommendations
	 * 
	 * @param DisplayDocument
	 *            document, the recommendation to log
	 * @param documentSet,
	 *            needed for metadata, ids, and further processing
	 * @return int, id of the created recommendation log
	 * @throws Exception
	 */
	public int logRecommendationsNew(DisplayDocument document, DocumentSet documentset) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int recommendationId = -1;
		Double maxRelevanceScorePerSet = 0.0;
		for (Statistics currentStats : documentset.getDebugDetailsPerSet().getRankStats()) {
			if (currentStats.getType().equals("relevance"))
				maxRelevanceScorePerSet = currentStats.getRankVMax();
		}
		try {
			// insertion query
			String query = "INSERT INTO " + constants.getRecommendations() + " ("
					+ constants.getDocumentIdInRecommendations() + ", "
					+ constants.getRecommendationSetIdInRecommendations() + ", " + constants.getRankAfterAlgorithm()
					+ ", " + constants.getRankAfterReRanking() + ", " + constants.getRankAfterShuffling() + ", "
					+ constants.getRankDelivered() + ", " + constants.getTextRelevanceScoreInRecommendations() + ", "
					+ constants.getFinalRankingScore() + ", " + constants.getRelativeRelevanceScore() + ") VALUES ("
					+ document.getDocumentId() + ", " + documentset.getRecommendationSetId() + ", '"
					+ document.getRankAfterAlgorithm() + "', ?, ?, '" + document.getRankDelivered() + "', '"
					+ document.getRelevanceScoreFromAlgorithm() + "', '" + document.getFinalScore() + "'" + ", '"
					+ ((double) document.getRelevanceScoreFromAlgorithm() / maxRelevanceScorePerSet) + "')";

			stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

			if (document.getRankAfterReRanking() == -1) {
				stmt.setNull(1, java.sql.Types.SMALLINT);
			} else
				stmt.setInt(1, document.getRankAfterReRanking());

			if (document.getRankAfterShuffling() == -1) {
				stmt.setNull(2, java.sql.Types.SMALLINT);
			} else
				stmt.setInt(2, document.getRankAfterShuffling());

			// System.out.println(query);
			stmt.executeUpdate();

			// get the autogenerated key back
			rs = stmt.getGeneratedKeys();
			if (rs.next())
				recommendationId = rs.getInt(1);

		} catch (Exception e) {
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
		return recommendationId;
	}

	/**
	 * Please fill me!
	 * 
	 * @param recommenderDetails
	 * @return
	 * @throws SQLException
	 */
	private int getMostPopularId(AlgorithmDetails recommenderDetails) throws SQLException {
		return getStereotypesId(recommenderDetails, false);
	}

	/**
	 * Please fill me!
	 * 
	 * @param recommenderDetails
	 * @return
	 * @throws SQLException
	 */
	private int getStereotypesId(AlgorithmDetails recommenderDetails) throws SQLException {
		return getStereotypesId(recommenderDetails, true);
	}

	/**
	 * Please fill me!
	 * 
	 * @param recommenderDetails
	 * @param stereotype
	 * @return
	 * @throws SQLException
	 */
	private int getStereotypesId(AlgorithmDetails recommenderDetails, boolean stereotype) throws SQLException {
		int stereotypeId = -1;
		Statement stmt = null;
		ResultSet rs = null;
		String tableName, tableRowId, tableCategoryName = "";
		if (stereotype) {
			tableName = constants.getStereotypeRecommendationDetails();
			tableRowId = constants.getStereotypeRecommendationDetailsId();
			tableCategoryName = constants.getStereotypeCategoryInStereotypeDetails();
		} else {
			tableName = constants.getMostPopularRecommendationDetails();
			tableRowId = constants.getMostPopularRecommendationDetailsId();
			tableCategoryName = constants.getMostPopularCategoryInMostPopularDetails();
		}
		String query = "SELECT " + tableRowId + " FROM " + tableName + " WHERE " + tableCategoryName + "='"
				+ recommenderDetails.getCategory() + "'";
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				stereotypeId = rs.getInt(tableRowId);
				// System.out.println(tableName + ":" + stereotypeId);
			} else {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
				query = "INSERT INTO " + tableName + "(" + tableCategoryName + ") VALUES('"
						+ recommenderDetails.getCategory() + "')";
				stmt = con.createStatement();
				// System.out.println(query);
				stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

				// get the autogenerated key back
				rs = stmt.getGeneratedKeys();
				if (rs.next())
					stereotypeId = rs.getInt(1);
				// System.out.println(stereotypeId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
		}
		return stereotypeId;
	}

	/**
	 * please fill me!
	 * 
	 * @param recommenderDetails
	 * @return
	 * @throws SQLException
	 */
	private int getCbfId(AlgorithmDetails recommenderDetails) throws SQLException {
		int cbfId = -1;
		Statement stmt = null;
		ResultSet rs = null;
		boolean inputIsDocument = !recommenderDetails.getName().contains("Query");
		String queryType = "";
		if (!inputIsDocument)
			queryType = recommenderDetails.getQueryParser();
		boolean keyphrases = !recommenderDetails.getCbfFeatureType().equals("terms");
		String query = "SELECT " + constants.getCbfId() + " FROM " + constants.getCbfDetails() + " WHERE "
				+ constants.getInputType() + " = '" + (inputIsDocument ? "document" : "query") + "' AND "
				+ constants.getCbfFeatureType() + " = '" + (keyphrases ? "keyphrases" : "terms") + "'";
		if (keyphrases) {
			query += " AND " + constants.getCbfNgramType() + " = '" + recommenderDetails.getCbfFeatureType() + "'";
		}
		if (!inputIsDocument) {
			query += " AND " + constants.getSearchMode() + " = '" + queryType + "'";
		}
		query += " AND " + constants.getCbfFeatureCount() + "='" + recommenderDetails.getCbfFeatureCount() + "' AND "
				+ constants.getCbfFields() + "= '" + recommenderDetails.getCbfTextFields() + "'";

		try {
			stmt = con.createStatement();
			System.out.println(query);
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				cbfId = rs.getInt(constants.getCbfId());
				// System.out.printf("cbfId:%d\n", cbfId);
			} else {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
				String columns = constants.getCbfFeatureType() + "," + constants.getInputType();
				String values = "'" + (keyphrases ? "keyphrases" : "terms") + "' , '"
						+ (inputIsDocument ? "document" : "query") + "' ";
				if (keyphrases) {
					columns += ", " + constants.getCbfNgramType();
					values += ", '" + recommenderDetails.getCbfFeatureType();
				}
				if (!inputIsDocument) {
					columns += ", " + constants.getSearchMode();
					values += ", '" + queryType + "'";
				}
				columns += ", " + constants.getCbfFeatureCount() + ", " + constants.getCbfFields();
				values += ", '" + recommenderDetails.getCbfFeatureCount() + "', '"
						+ recommenderDetails.getCbfTextFields() + "'";

				query = "INSERT INTO " + constants.getCbfDetails() + " (" + columns + ") VALUES(" + values + ")";

				stmt = con.createStatement();

				stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

				// get the autogenerated key back
				rs = stmt.getGeneratedKeys();
				if (rs.next())
					cbfId = rs.getInt(1);
				// System.out.println(cbfId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
		}
		return cbfId;
	}

	/**
	 * Please fill me!
	 * 
	 * @param documentset
	 * @return
	 * @throws Exception
	 */
	public int searchLogBibRerankingId(DocumentSet documentset) throws Exception {
		int rerankingBibliometricId = -1;
		String bibliometricIdQueryString = "";
		Statement stmt = null;
		ResultSet rs = null;

		if (documentset.getBibliometricId() == -1)
			bibliometricIdQueryString = " IS NULL";
		else
			bibliometricIdQueryString = "=" + documentset.getBibliometricId();

		// the query to get the person
		String query = "SELECT " + constants.getAlgorithmRerankingBibliometricsId() + " FROM "
				+ constants.getAlgorithmRerankingBibliometrics() + " WHERE " + constants.getNumberOfCandidatesToRerank()
				+ "=" + documentset.getNumberOfCandidatesToReRank() + " AND " + constants.getRerankingOrder() + "='"
				+ documentset.getRankingOrder() + "' AND "
				+ constants.getBibliometricIdInAlgorithmRerankingBibliometrics() + bibliometricIdQueryString + " AND "
				+ constants.getRerankingCombindation() + "='" + documentset.getReRankingCombination() + "' AND "
				+ constants.getFallbackReranking() + "='" + (documentset.isFallbackRanking() ? 'Y' : 'N') + "'";

		try {
			stmt = con.createStatement();
			stmt.executeQuery(query);
			rs = stmt.getResultSet();

			if (rs.next())
				rerankingBibliometricId = rs.getInt(constants.getAlgorithmRerankingBibliometricsId());

		} catch (Exception e) {
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
		return rerankingBibliometricId;
	}

	/**
	 * Please fill me!
	 * 
	 * @param documentset
	 * @return
	 * @throws Exception
	 */
	public int logBibReranking(DocumentSet documentset) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int rerankingBibliometricId = -1;

		rerankingBibliometricId = searchLogBibRerankingId(documentset);

		if (rerankingBibliometricId == -1) {

			try {
				// insertion query
				String query = "INSERT INTO " + constants.getAlgorithmRerankingBibliometrics() + " ("
						+ constants.getNumberOfCandidatesToRerank() + ", " + constants.getRerankingOrder() + ", "
						+ constants.getBibliometricIdInAlgorithmRerankingBibliometrics() + ", "
						+ constants.getRerankingCombindation() + ", " + constants.getFallbackReranking() + ") VALUES ('"
						+ documentset.getNumberOfCandidatesToReRank() + "', '" + documentset.getRankingOrder()
						+ "', ?, '" + documentset.getReRankingCombination() + "', ?);";

				stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

				if (documentset.getBibliometricId() == -1) {
					stmt.setNull(1, java.sql.Types.BIGINT);
				} else
					stmt.setInt(1, documentset.getBibliometricId());

				if (documentset.isFallbackRanking()) {
					stmt.setString(2, "Y");
				} else
					stmt.setString(2, "N");

				stmt.executeUpdate();

				// get the autogenerated key back
				rs = stmt.getGeneratedKeys();
				if (rs.next())
					rerankingBibliometricId = rs.getInt(1);

			} catch (Exception e) {
				e.printStackTrace();
				throw e;

			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e) {
					throw e;
				}
			}
		}
		return rerankingBibliometricId;
	}

	/**
	 * please fill me
	 */
	public void calculateSumOfAuthors() {
		Statement stmt = null;

		// TODO: make variables
		String query = "insert INTO bibliometrics_documents (document_id, bibliometrics_id, value) select D.id, 4 , "
				+ "sum(BP.value) as sumOfPvalue from document D JOIN xj_persons_documents PD ON PD.document_id = D.id "
				+ "JOIN bibliometrics_persons BP ON BP.person_id = PD.person_id WHERE D.id > 1 AND D.id < 9505296 "
				+ "GROUP BY D.id";

		try {

			stmt = con.createStatement();
			stmt.executeQuery(query);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				System.out.println(e);
			}
		}

	}

	/**
	 * please fill me
	 * 
	 * @param documentId
	 * @param bibliometricId
	 * @return
	 * @throws Exception
	 */

	public void writeBibliometricsInDatabase(String id, String metric, String dataType, int value, String dataSource) {
		Statement stmt = null;
		int bibId = -1;

		try {
			bibId = getBibId(metric, dataType, dataSource);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO: make variables
		String query = "insert INTO " + constants.getBibDocuments() + " (" + constants.getDocumentIdInBibliometricDoc()
				+ ", bibliometrics_id, " + constants.getMetricValue() + ") VALUES (" + id + ", " + bibId + ", " + value
				+ ")";

		try {
			stmt = con.createStatement();
			stmt.executeUpdate(query);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				System.out.println(e);
			}
		}

	}

	/**
	 * please fill me
	 * 
	 * @param documentId
	 * @param bibliometricId
	 * @return
	 * @throws Exception
	 */

	public void writeBibliometricsInDatabase(String id, int bibId, double value) {
		Statement stmt = null;

		// TODO: make variables
		String query = "insert INTO " + constants.getBibDocuments() + " (" + constants.getDocumentIdInBibliometricDoc()
				+ ", bibliometrics_id, " + constants.getMetricValue() + ") VALUES (" + id + ", " + bibId + ", " + value
				+ ")";

		try {
			stmt = con.createStatement();
			stmt.executeUpdate(query);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				System.out.println(e);
			}
		}

	}

	public DisplayDocument getRankingValue(String documentId, int bibliometricId) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		int metricValue = -1;
		int bibDocId = -1;
		DisplayDocument document = new DisplayDocument();

		// selection query
		String query = "SELECT " + constants.getBibliometricDocumentsId() + ", " + constants.getMetricValue() + " FROM "
				+ constants.getBibDocuments() + " WHERE " + constants.getDocumentIdInBibliometricDoc() + " = '"
				+ documentId + "' AND " + constants.getBibliometricIdInBibliometricDocument() + " = '" + bibliometricId
				+ "';";

		try {

			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			// get the data from the result set
			if (rs.next()) {
				metricValue = rs.getInt("value");
				bibDocId = rs.getInt("bibliometric_document_id");
			}

			// add the data to the document
			document.setBibDocId(bibDocId);
			document.setBibScore(metricValue);

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
		return document;
	}

	/**
	 * please fill me
	 * 
	 * @param bibliometricId
	 * @param authorId
	 * @return
	 */
	public List<DisplayDocument> getRankingValuesOfAuthorPerDocument(int bibliometricId, int authorId) {
		List<DisplayDocument> documentList = new ArrayList<DisplayDocument>();
		DisplayDocument document = new DisplayDocument();
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = con.createStatement();

			// selection query
			String query = "select BD.value, D.title_clean from bibliometric_person BP "
					+ "JOIN document_person PD ON PD.person_id = BP.person_id "
					+ "JOIN bibliometric_document BD ON BD.document_id = PD.document_id "
					+ "JOIN document D ON D.id = PD.document_id WHERE BD.bibliometrics_id=" + bibliometricId
					+ " AND PD.document_id < 9505296 AND BP.person_id=" + authorId;

			rs = stmt.executeQuery(query);

			// add the retrieved person to the list
			while (rs.next()) {
				document = new DisplayDocument();
				document.setCleanTitle(rs.getString(constants.getTitleClean()));
				document.setBibScore(rs.getInt(constants.getMetricValue()));
				documentList.add(document);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return documentList;
	}

	/**
	 * please fill me
	 * 
	 * @param language
	 * @return
	 */
	public long getNumberOfAbstractsInLanguage(String language) {
		Statement stmt = null;
		ResultSet rs = null;
		String query = "SELECT COUNT(*) FROM " + constants.getAbstracts() + " WHERE `" + constants.getAbstractLanguage()
				+ "`='" + language + "'";
		// System.out.println(query);

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				return rs.getInt(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	/**
	 * please fill me
	 * 
	 * @param language
	 * @param offset
	 * @return
	 */
	public List<SimpleEntry<Long, Abstract>> fillAbstractsList(String language, long offset) {
		Statement stmt = null;
		ResultSet rs = null;
		String query = "SELECT * FROM " + constants.getAbstracts() + " WHERE `" + constants.getAbstractLanguage()
				+ "`='" + language + "' LIMIT " + offset + ",500";
		// System.out.println(query);
		List<SimpleEntry<Long, Abstract>> abstractList = new ArrayList<AbstractMap.SimpleEntry<Long, Abstract>>();
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				abstractList
						.add(new AbstractMap.SimpleEntry<Long, Abstract>(rs.getLong(constants.getAbstractDocumentId()),
								new Abstract(rs.getString(constants.getAbstr()), language)));
			}
			return abstractList;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Didn't return normally");
		return null;
	}

	/**
	 * please fill me
	 * 
	 * @param documentId
	 * @param type
	 * @param text
	 * @param translationTool
	 * @param sourceLanguage
	 * @param targetLanguage
	 * @return
	 */
	public int addTranslatedEntry(long documentId, String type, String text, String translationTool,
			String sourceLanguage, String targetLanguage) {
		PreparedStatement stmt = null;
		String query = "INSERT INTO " + "translated_document_fields"
				+ "(document_id,field_type,translation_tool,source_language,target_language,text)" + " VALUES("
				+ "?, ?, ?, ?, ?, ?)";
		try {
			stmt = con.prepareStatement(query);
			stmt.setLong(1, documentId);
			stmt.setString(2, type);
			stmt.setString(3, translationTool);
			stmt.setString(4, sourceLanguage);
			stmt.setString(5, targetLanguage);
			stmt.setString(6, replaceHighComma(text).replaceAll("[^A-Za-z0-9 ]", ""));
			stmt.executeUpdate();
			return 1;
		} catch (SQLException e) {
			System.out.println(stmt.toString());
			e.printStackTrace();
			return -1;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * please fill me!
	 * 
	 * @param translatedAbstract
	 * @return
	 */
	public int addTranslatedAbstract(AbstractMap.SimpleEntry<Long, Abstract> translatedAbstract) {
		return addTranslatedEntry(translatedAbstract.getKey(), "abstract", translatedAbstract.getValue().getContent(),
				"joshua", "de", "en");
	}

	public List<DisplayDocument> getRankingValuesOfDocumentsOfSpecifiedAuthor(int personId, int bibliometricId) {
		List<DisplayDocument> documentList = new ArrayList<DisplayDocument>();
		DisplayDocument document;
		Statement stmt = null;
		ResultSet rs = null;

		// the query
		String query = "SELECT " + constants.getMetricValue() + ", " + constants.getTitleClean() + " FROM "
				+ constants.getDocPers() + " DP JOIN " + constants.getBibDocuments() + " BD ON DP."
				+ constants.getDocumentIDInDocPers() + "= BD." + constants.getDocumentIdInBibliometricDoc() + " JOIN "
				+ constants.getDocuments() + " D ON D." + constants.getDocumentId() + "= BD."
				+ constants.getDocumentIdInBibliometricDoc() + " WHERE "
				+ constants.getBibliometricIdInBibliometricDocument() + " = " + bibliometricId + " AND "
				+ constants.getPersonIDInDocPers() + " = " + personId + ";";

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				document = new DisplayDocument();
				document.setCleanTitle(rs.getString(constants.getTitleClean()));
				document.setBibScore(rs.getInt(constants.getMetricValue()));
				documentList.add(document);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return documentList;
	}

	public boolean updateStereotypes(ArrayList<SimpleEntry<String, String>> updates) {

		PreparedStatement stmt = null;
		String query = "INSERT INTO " + constants.getStereotypeRecommendations() + " ( "
				+ constants.getDocumentIdinStereotypeRecommendations() + ", " + constants.getStereotypeCategory()
				+ ") VALUES (?,?)";
		try {
			stmt = con.prepareStatement(query);
			for (SimpleEntry<String, String> entry : updates) {
				try {
					stmt.setInt(1, Integer.parseInt(entry.getKey()));
				} catch (NumberFormatException f) {
					String documentId = getDocumentIdFromURL(entry.getKey());
					if (documentId.equals("No such document in database")) {
						System.out.println("This URL has no assosciated document in our database:");
						System.out.println(entry.getKey());
						continue;
					}
					stmt.setInt(1, Integer.parseInt(documentId));
				}
				stmt.setString(2, entry.getValue());
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			System.out.println(query);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private String getDocumentIdFromURL(String key) {
		if (key.contains("sowiport")) {
			String[] parts = key.split("/");
			String originalId = parts[parts.length - 1];
			DisplayDocument document;
			try {
				document = getDocumentBy("id_original", originalId);
				return document.getDocumentId();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (key.contains("core")) {
			String[] parts = key.split("/");
			String originalId = parts[parts.length - 1];
			DisplayDocument document;
			try {
				document = getDocumentBy("id_original", "core-" + originalId);
				return document.getDocumentId();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "No document in database";
	}

	public void logRecommendationSetReceivedAcknowledgement(String recommendationSetId, Long requestRecieved)
			throws SQLException {
		String query = "UPDATE " + constants.getRecommendationSets() + " SET "
				+ constants.getRecommendationSetReceivedTime() + "=  IF( "
				+ constants.getRecommendationSetReceivedTime() + " IS NULL, ?, "
				+ constants.getRecommendationSetReceivedTime() + ") WHERE " + constants.getRecommendationSetsId()
				+ "=?";

		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setTimestamp(1, new Timestamp(requestRecieved));
			stmt.setString(2, recommendationSetId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public String getTitleStringId(DisplayDocument requestDocument) throws SQLException {
		String titleStringId = "";
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String query = "SELECT " + constants.getTitleSearchId() + " FROM " + constants.getDocumentTitleSearchTable()
				+ " WHERE " + constants.getTitleSearchString() + "=? AND " + constants.getOriginalSearchString() + "=?";
		try {
			stmt = con.prepareStatement(query);
			stmt.setString(1, requestDocument.getCleanTitle());
			stmt.setString(2, requestDocument.getTitle());
			System.out.println(requestDocument.getCleanTitle());
			rs = stmt.executeQuery();
			if (rs.next()) {
				titleStringId = rs.getString(constants.getTitleSearchId());
			} else {
				if (stmt != null)
					stmt.close();
				if (rs != null)
					rs.close();
				query = "INSERT INTO " + constants.getDocumentTitleSearchTable() + "("
						+ constants.getTitleSearchString() + "," + constants.getOriginalSearchString()
						+ ") VALUES(?,?)";
				stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1, requestDocument.getCleanTitle());
				stmt.setString(2, requestDocument.getTitle());
				stmt.executeUpdate();

				// get the autogenerated key back
				rs = stmt.getGeneratedKeys();
				if (rs.next())
					titleStringId = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
		}
		return titleStringId;
	}
}