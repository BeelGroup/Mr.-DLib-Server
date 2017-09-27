package org.mrdlib.partnerContentManager.core;

import java.sql.Timestamp;
import java.sql.Types;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.mrdlib.database.DBConnection;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.manager.Constants;
import org.mrdlib.partnerContentManager.core.model.Article;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentCheck
{
    private DBConnection db;
    private Constants constants;
    private CoreApi api;
	private Logger logger = LoggerFactory.getLogger(DocumentCheck.class);
    public static final long BATCH_SIZE = 1000;
	public static final long RETRY_SLEEP_TIME = 5000;

    public DocumentCheck () throws Exception {
		this.constants = new Constants();
		this.db = new DBConnection("jar");
		this.api = new CoreApi();
    }

    public List<DisplayDocument> getCoreDocumentsBatch(long batch) throws Exception {
		return db.getDeleteCandidates(constants.getCore(), batch * BATCH_SIZE, BATCH_SIZE);
    }

    public long getBatchesForAllDocuments() throws Exception {
		long max = db.getBiggestIdFromDocuments();
		long batches = max / BATCH_SIZE;
		if (max % BATCH_SIZE != 0)
			batches++;
		return batches;
    }

    public static List<Integer> getCoreIdsFromDocuments(List<DisplayDocument> docs) throws Exception {
		return getCoreIdsFromStrings(
				docs.stream()
				.map(doc -> doc.getOriginalDocumentId())
				.collect(Collectors.toList()));
	}
    public static List<Integer> getCoreIdsFromStrings(List<String> docs) throws Exception {
		Pattern idRegex = Pattern.compile("^core-(\\d+)$");
		List<Integer> ids = docs.stream()
			.map(id -> {
					try {
						Matcher match = idRegex.matcher(id);
						if (match.matches()) {
							String group = match.group(1);
							return Integer.parseInt(group);
						} else {
							return -1;
						}
					} catch(NumberFormatException e) {
						return -1;
					}
				}).collect(Collectors.toList());
		if(ids.indexOf(-1) != -1)
			throw new Exception("Invalid document ids in results: " + docs.get(ids.indexOf(-1)));
		return ids;
    }

    public boolean checkDocument(Article a, DisplayDocument doc) {
		return false;
    }

    public static void main(String args[]) {
		boolean success = false;
		long sleepTime = RETRY_SLEEP_TIME;
		DocumentCheck check = null;
		long batches = 0;
		try {
			check = new DocumentCheck();
			batches = check.getBatchesForAllDocuments();
		} catch (Exception e) {
			System.err.println("Failed initializing document checker"); 
			e.printStackTrace();
			System.exit(1);
		}
		try {
			for (long batch = 0; batch < batches; batch++) {
				List<DisplayDocument> docs = null;
				List<Integer> ids = null;
				success = false;
				while (!success) {
					try {
						docs = check.getCoreDocumentsBatch(batch);
						ids = check.getCoreIdsFromDocuments(docs);
						success = true;
					} catch (Exception e) {
						check.logger.warn("Getting documents from DB failed; retrying in {}s", sleepTime / 1000, e);
						Thread.sleep(sleepTime);
						sleepTime += RETRY_SLEEP_TIME;
					}
				}

				List<Object> missing = new ArrayList<Object>();
				List<Object> deletedTimestamps = new ArrayList<Object>();
				List<Object> checked = new ArrayList<Object>();
				List<Object> checkedTimestamps = new ArrayList<Object>();
				List<Article> articles = null;
				if (ids.size() != 0) {
					success = false;
					sleepTime = RETRY_SLEEP_TIME;
					while(!success) {
						try {
							articles = check.api.getArticles(ids);
							if (ids.size() != articles.size()) {
								throw new Exception("Missing document in query results: " + articles.toString() + " vs " + ids.toString());
							}
							success = true;
						} catch(Exception e) {
							check.logger.warn("Getting documents from Core API failed; retrying in {}s", sleepTime / 1000, e);
							Thread.sleep(sleepTime);
							sleepTime += RETRY_SLEEP_TIME;
						}
					}
					for (int i = 0; i < articles.size(); i++) {
						String id = String.format("core-%d", ids.get(i));
						Timestamp time = new Timestamp(System.currentTimeMillis());
						checked.add(id);
						checkedTimestamps.add(time);
						if (articles.get(i) == null) {
							missing.add(id);
							deletedTimestamps.add(time);
						}
					}
				}

				success = false;
				sleepTime = RETRY_SLEEP_TIME;
				while (!success) {
					try {
						check.db.setRowValues(check.constants.getDocuments(),
								check.constants.getIdOriginal(), missing, Types.VARCHAR,
								check.constants.getDeleted(), deletedTimestamps, Types.TIMESTAMP);

						check.db.setRowValues(check.constants.getDocuments(),
								check.constants.getIdOriginal(), checked, Types.VARCHAR,
								check.constants.getChecked(), checkedTimestamps, Types.TIMESTAMP);
						success = true;
					} catch(Exception e) {
						check.logger.warn("Saving results to database failed; retyring in {}s", sleepTime / 1000, e);
						Thread.sleep(sleepTime);
						sleepTime += RETRY_SLEEP_TIME;
					}
				}
			}
			check.db.close();
		} catch(Exception e) {
			check.logger.warn("Something went rather wrong.", e);
		}
    }

}
