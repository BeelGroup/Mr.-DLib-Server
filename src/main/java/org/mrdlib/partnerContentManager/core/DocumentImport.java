package org.mrdlib.partnerContentManager.core;

import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.mrdlib.database.DBConnection;
import org.mrdlib.database.NoEntryException;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.manager.Constants;
import org.mrdlib.partnerContentManager.core.model.Article;
import org.mrdlib.partnerContentManager.core.model.Repository;
import org.mrdlib.partnerContentManager.general.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentImport
{
	private DBConnection db;
	private Constants constants;
	private CoreApi api;
	private Long coreOrgId;
	private Logger logger = LoggerFactory.getLogger(DocumentImport.class);
	public static final long BATCH_SIZE = 1000;
	public static final long RETRY_SLEEP_TIME = 1000;

	public DocumentImport () {
		try {
			constants = new Constants();
			db = new DBConnection("jar");
			api = new CoreApi();
			coreOrgId = db.getOrganizationId(constants.getCore());
		} catch (Exception e) {
			logger.error("Could not setup document import", e);
		}
	}

	public boolean hasDocumentInDB(String idOriginal) throws Exception {
		try {
			DisplayDocument doc = db.getDocumentBy(constants.getIdOriginal(), idOriginal);
			return true;
		} catch(NoEntryException e) {
			return false;
		}
	}

	public Document convert(Article article) {
		Document doc = new Document(); // TODO: maybe instantiate subclass for peculiarities of core documents
		for (String author : article.getAuthors()) {
			doc.addAuthor(author);
		}
		doc.setTitle(article.getTitle());
		doc.setLanguage((article.getLanguage() != null ? article.getLanguage().getCode() : null)); 
		doc.setYear((article.getYear() != null ? article.getYear().toString() : null));	
		doc.normalize();
		for (String keyword : article.getTopics()) { 
			doc.addKeyword(keyword);
		}
		if (article.getDescription() != null)
			doc.addAbstract(article.getDescription(), doc.getLanguage());
		else
			logger.trace("no abstract for article {}", article);
		doc.setId("core-" + article.getId());

		if (article.getJournals() != null && article.getJournals().size() > 0)
			// TODO: only take first or join list?
			doc.setPublishedIn(article.getJournals().get(0).getTitle());
		else {
			doc.setPublishedIn(null);
			logger.trace("no journal for article {}", article);
		}

		if (article.getTypes() != null && article.getTypes().size() > 0)
			// TODO: select relevant as in XMLDocument?
			doc.setType(article.getTypes().get(0));
		else {
			logger.trace("no type for article {}", article);
			doc.setType("unknown");
		}

		if (article.getRepositories() != null && article.getRepositories().size() > 0) {
			// uses long name
			Repository repo = article.getRepositories().get(0);
			try {
				logger.trace("Searching for collection {}", repo.getName());
				Long collectionId = db.getCollectionIDByName(repo.getName(), false);
				if (collectionId == null) {
					logger.trace("Creating collection: {}", repo);
					try {
						collectionId = db.createCollection(
								repo.getName(), String.format("core-collection-%d", repo.getId()),
								coreOrgId, new Long(repo.getId()));
						logger.trace("Created collection; id {}", collectionId);
					} catch(Exception e) {
						logger.warn("Could not create collection id for name {} while importing article {}", 
								article.getRepositories().get(0).getName(), article, e);
					}
				}
				logger.trace("Got id: {}", collectionId);
				doc.setCollectionId(collectionId.toString());
			} catch(Exception e) {
				logger.warn("Could not associate collection for name {} while importing article {}", 
						article.getRepositories().get(0).getName(), article, e);
			}
		} else {
			logger.error("no collection id for article {}", article);
		}

		if(article.getOai() != null)
			doc.addExternalId("oai", article.getOai());

		if(article.getDoi() != null)
			doc.addExternalId("doi", article.getDoi());

		logger.info("Converted document: {}", doc);

		return doc;	
	}

	public void upsertDocument(Document doc, List<String> failedImports) {
		try {
			if (!hasDocumentInDB(doc.getId())) {
				db.insertDocument(doc);
				logger.info("Inserted document: {}", doc.getTitle());
			} else {
				logger.info("Document already in database: {} - {}; updating", doc.getId(), doc.getTitle());
				db.updateDocument(doc);
			}
		} catch(Exception e) {
			logger.warn("Could not import document {} - {}", doc.getId(), doc.getTitle(), e);
			failedImports.add(doc.getId());
		}
	}

	public File getSettings(String filename, Properties settings) {
		File file = null;
		try {
			file = new File(filename);
			file.createNewFile();
		} catch (Exception e) {
			logger.warn("could not load progress/settings file; will be created", e);
		}
		// default values
		try (FileInputStream in = new FileInputStream(file)){
			settings.load(in);
		}  catch(Exception e) {
			logger.warn("Error while loading settings", e);
		}
		return file;
	}

	public static void main(String args[]) {
		String filename = "coreImportStatus.properties";
		if (args.length > 0) {
			filename = args[0];
		}
		DocumentImport importer = new DocumentImport();

		Properties settings = new Properties();
		File file = importer.getSettings(filename, settings);

		int year = Integer.parseInt(settings.getProperty("year", 
					String.valueOf(Calendar.getInstance().get(Calendar.YEAR))));
		long progress = Long.parseLong(settings.getProperty("offset", "0"));


		// try failed imports again,
		List<String> failedImports = new ArrayList<String>();
		String errorList = settings.getProperty("errors", "").trim();
		if (errorList.length() != 0) {
			for (String failed : errorList.split(",")) {
				failedImports.add(failed);
			}
		}
		List<Article> failedArticles = null;
		List<Integer> ids = null;
		try {
			ids = DocumentCheck.getCoreIdsFromStrings(failedImports);
			importer.logger.info("Rechecking {} failed imports", ids.size());
			failedArticles = importer.api.getArticles(ids);
		} catch(Exception e) {
			importer.logger.warn("Tried requesting failed documents again but encountered error", e);
			for (Integer id : ids) {
				failedImports.add("core-"+id.toString());
			}
		}

		// reset failures
		failedImports = new ArrayList<String>();
		for (Article a : failedArticles) {
			importer.upsertDocument(importer.convert(a), failedImports);
		}
		importer.logger.info("Finished rechecking failed imports");

		// look for new articles

		Stream<CoreApi.StreamedArticle> articles = importer.api.streamArticles(year, progress);
		Iterator<CoreApi.StreamedArticle> iter = articles.iterator();
		CoreApi.StreamedArticle article = null;
		long sleepTime = RETRY_SLEEP_TIME;
		boolean success = false;
		while (true) {
			success = false;
			while (!success) {
				try {
					article = iter.next();
					if (article == null) {
						String message = "Finished importing articles.";
						importer.logger.info(message);
						System.out.println(message);
						settings.setProperty("finished", String.valueOf(true));
						try (FileOutputStream out = new FileOutputStream(file, false)) {
							settings.store(out, "Progress & Settings of core document import");
						}
						return;
					} else {
						success = true;
					}
				} catch(Exception e) {
					importer.logger.warn("Getting next article from core api failed; retrying in {}s", sleepTime / 1000, e);
					try { Thread.sleep(sleepTime); } catch(Exception ee) {}
					sleepTime += RETRY_SLEEP_TIME;
				}
			}
			Document doc = importer.convert(article.article);
			importer.upsertDocument(doc, failedImports);

			progress = article.offset;
			year = article.year;
			settings.setProperty("offset", String.valueOf(progress));
			settings.setProperty("year", String.valueOf(year));
			String[] failedArray = new String[failedImports.size()];
			settings.setProperty("errors", String.join(",", failedImports.toArray(failedArray)));
			try (FileOutputStream out = new FileOutputStream(file, false)) {
				settings.store(out, "Progress & Settings of core document import");
			} catch(IOException e) {
				String last = null;
				if (failedImports.size() != 0)
					last = failedImports.get(failedImports.size() - 1);
				importer.logger.warn("Could not save progress: year = {}, progress = {}, last error: {}",
						year, progress, last, e);
			}
		}
	}
}
