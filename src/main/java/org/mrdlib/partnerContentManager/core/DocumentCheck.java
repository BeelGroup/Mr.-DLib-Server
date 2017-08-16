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

    public DocumentCheck () {
		try {
			this.constants = new Constants();
			this.db = new DBConnection("jar");
			this.api = new CoreApi();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public List<DisplayDocument> getCoreDocumentsBatch(long batch) throws Exception {
		return db.getDocumentsByIdSchemaMissingField(constants.getCore(), batch * BATCH_SIZE, BATCH_SIZE, constants.getDeleted());
    }
    public List<DisplayDocument> getCoreDocumentsById(long start) throws Exception {
		return db.getDocumentsByIdSchemaMissingField(constants.getCore(), start, BATCH_SIZE, constants.getDeleted());
    }

    public long getBatchesForAllDocuments() throws Exception {
		long max = db.getBiggestIdFromDocuments();
		long batches = max / BATCH_SIZE;
		if (max % BATCH_SIZE != 0)
			batches++;
		return batches;
    }

    public List<Integer> getCoreIdsFromDocuments(List<DisplayDocument> docs) throws Exception {
		Pattern idRegex = Pattern.compile("^core-(\\d+)$");
		List<Integer> ids = docs.stream()
			.map( d -> d.getOriginalDocumentId())
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
		try {
			if (args.length == 0) {
				System.err.println("Usage: ./gradlew coreDocumentCheck:run -Dexec.args=filename [startBatch]");
			}

			String filename = args[0];
			long start = 0;
			if (args.length >= 2) 
				start = Long.parseLong(args[1]);
			FileWriter progress = new FileWriter(filename, true);
			DocumentCheck check = new DocumentCheck();
			long batches = check.getBatchesForAllDocuments();
			SimpleDateFormat elapsed = new SimpleDateFormat("HH:mm:ss");
			elapsed.setTimeZone(TimeZone.getTimeZone("UTC"));

			for (long batch = start; batch < batches; batch++) {
				List<DisplayDocument> docs = check.getCoreDocumentsBatch(batch);
				List<Integer> ids = check.getCoreIdsFromDocuments(docs);
				List<Object> missing = new ArrayList<Object>();
				List<Object> deletedTimestamps = new ArrayList<Object>();

				if (ids.size() != 0) {
					try {
						List<Article> articles = check.api.getArticles(ids);
						if (ids.size() != articles.size()) {
							throw new Exception("Missing document in query results: " + articles.toString() + " vs " + ids.toString());
						}
						for (int i = 0; i < articles.size(); i++) {
							if (articles.get(i) == null) {
								progress.append("-" + ids.get(i) + System.lineSeparator());
								missing.add(ids.get(i));
								Timestamp time = new Timestamp(System.currentTimeMillis());
								deletedTimestamps.add(time);
							}
						}
					} catch(Exception e) {
						progress.append("!" + batch + " " + e + System.lineSeparator());
						progress.flush();
						continue;
					}
				}
				progress.append("+" + batch + System.lineSeparator());
				progress.flush();

				check.db.setRowValues(check.constants.getDocuments(),
					check.constants.getIdOriginal(), missing, Types.VARCHAR,
					check.constants.getDeleted(), deletedTimestamps, Types.TIMESTAMP);
			}
			progress.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	// public static void main(String[] args) throws Exception {
	// 	DocumentCheck check = new DocumentCheck();
	// 	String filename = args[0];
	// 	BufferedReader file = new BufferedReader(new FileReader(filename));
	// 	String line;
	// 	Timestamp time = new Timestamp(System.currentTimeMillis());
	// 	List<Object> missing = new ArrayList<Object>();
	// 	List<Object> deletedTimestamps = new ArrayList<Object>();
	// 	check.logger.info("Collecting documents to mark as deleted.");
	// 	long i = 0;
	// 	while ((line = file.readLine()) != null) {
	// 		char type = line.charAt(0);
	// 		if (type == '-') { 
	// 			String id = String.format("core-%s", line.substring(1));
	// 			missing.add(id);
	// 			deletedTimestamps.add(time);
	// 			if (++i % 1000 == 0) {
	// 				check.logger.info("Progress: {} documents collected.", i);
	// 			}
	// 		}
	// 	}
	// 	check.logger.info("Marking documents as deleted.");
	// 	check.db.setRowValues(check.constants.getDocuments(),
	// 		check.constants.getIdOriginal(), missing, Types.VARCHAR,
	// 		check.constants.getDeleted(), deletedTimestamps, Types.TIMESTAMP);
	// }
}
