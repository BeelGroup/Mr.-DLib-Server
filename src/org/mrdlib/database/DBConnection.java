package org.mrdlib.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.mrdlib.Document;
import org.mrdlib.DocumentSet;

public class DBConnection {
	/**
	 * Method to create DB Connection
	 * 
	 * @return
	 * @throws Exception
	 */

	static Constants constants = new Constants();
	
	@SuppressWarnings("finally")
	public static Connection createConnection() throws Exception {
		Connection con = null;
		Class.forName(constants.getDbClass());
		con = DriverManager.getConnection(constants.getUrl() + constants.getDb(), constants.getUser(), constants.getPassword());
		return con;
		/*try {
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return con;
		}*/
	}

	public static DocumentSet getDocumentSetByOriginalId(String originalid) throws Exception {
		DocumentSet documentSet = new DocumentSet();

		documentSet.setRecommendationSetId("DummyId");
		documentSet.setSuggested_label("Dummy Articles");

		Connection dbConn = null;
		try {
			dbConn = DBConnection.createConnection();
			Statement stmt = dbConn.createStatement();
			String query = "SELECT * FROM documents WHERE id_original = '" + originalid + "'";

			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				Document document = new Document("dummyRec", rs.getString(constants.getId()), rs.getString(constants.getIdOriginal()), 666,
						rs.getString(constants.getTitle()) + ". " + rs.getString(constants.getAuthors()) + ". "
								+ rs.getString(constants.getPublication()) + ". " + rs.getInt(constants.getYear()),
						"&lt;span class='title'&gt;" + rs.getString(constants.getTitle())
								+ "&lt;/span&gt;. &lt;span class='authors'&gt;" + rs.getString(constants.getAuthors())
								+ ";/span&gt;. &lt;span class='journal'&gt;" + rs.getString(constants.getPublication())
								+ "&lt;/span&gt;. &lt;span class='volume_and_number'&gt;6:66&lt;/span&gt;. &lt;span class='year'&gt;"
								+ rs.getInt(constants.getYear()) + "&lt;/span&gt;",
						"DummyURL", "DummyFallBackURL");
				documentSet.addDocument(document);
			}
		} catch (SQLException sqle) {
			throw sqle;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if (dbConn != null) {
				dbConn.close();
			}
			throw e;
		} finally {
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return documentSet;
	}
}