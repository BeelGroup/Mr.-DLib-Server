package org.mrdlib.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants {

	private String configPath = "config.properties";

	private String dbClass;
	private String db;
	private String url;
	private String user;
	private String password;

	private String id;
	private String idOriginal;
	private String title;
	private String authors;
	private String publication;
	private String year;

	// load the config file
	public Constants() {

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = getClass().getClassLoader().getResourceAsStream(configPath);
			System.out.println(getClass().getClassLoader());
			prop.load(input);

			// get the property value and print it out
			this.dbClass = prop.getProperty("dbClass");
			System.out.println(dbClass);
			this.db = prop.getProperty("db");
			this.url = prop.getProperty("url");
			this.user = prop.getProperty("user");
			this.password = prop.getProperty("password");
			this.id = prop.getProperty("id");
			this.idOriginal = prop.getProperty("idOriginal");
			this.title = prop.getProperty("title");
			this.authors = prop.getProperty("authors");
			this.publication = prop.getProperty("publication");
			this.year = prop.getProperty("year");

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getDbClass() {
		return dbClass;
	}

	public String getDb() {
		return db;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getId() {
		return id;
	}

	public String getIdOriginal() {
		return idOriginal;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthors() {
		return authors;
	}

	public String getPublication() {
		return publication;
	}

	public String getYear() {
		return year;
	}
}