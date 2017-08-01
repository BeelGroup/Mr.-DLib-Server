package org.mrdlib.partnerContentManager.core;

import java.io.FileWriter;
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



public class DocumentCheck
{
    private DBConnection db;
    private Constants constants;
    private CoreApi api;
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
		return db.getDocumentsByIdSchema(constants.getCore(), batch * BATCH_SIZE, BATCH_SIZE);
    }
    public List<DisplayDocument> getCoreDocumentsById(long start) throws Exception {
		return db.getDocumentsByIdSchema(constants.getCore(), start, BATCH_SIZE);
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
				if (ids.size() != 0) {
					try {
						List<Article> articles = check.api.getArticles(ids);
						if (ids.size() != articles.size()) {
							throw new Exception("Missing document in query results: " + articles.toString() + " vs " + ids.toString());
						}
						for (int i = 0; i < articles.size(); i++) {
							if (articles.get(i) == null) {
								progress.append("-" + ids.get(i) + System.lineSeparator());
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
			}
			progress.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
