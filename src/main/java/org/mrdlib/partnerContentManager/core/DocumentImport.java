package org.mrdlib.partnerContentManager.core;

import java.util.Calendar;
import java.util.List;
import java.util.Iterator;
import java.util.stream.Stream;

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
		doc.setLanguage(article.getLanguage().getCode()); 
		doc.setYear(article.getYear().toString());	
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

    public static void main(String args[]) throws Exception {
		int startYear = Calendar.getInstance().get(Calendar.YEAR);
		if (args.length > 0) {
			startYear = Integer.parseInt(args[0]);
		}
		DocumentImport importer = new DocumentImport();
		Stream<Article> articles = importer.api.listArticles(startYear);
		Iterator<Article> iter = articles.iterator();
		Article article;
		for (int i = 0; i < 1; i++) {
			if ((article = iter.next()) != null) {
				Document doc = importer.convert(article);
				if (!importer.hasDocumentInDB(doc.getId())) {
					importer.db.insertDocument(doc);
					importer.logger.info("Inserted document: {}", doc.getTitle());
				} else {
					importer.logger.info("Document already in database: {} - {}; updating", doc.getId(), doc.getTitle());
					importer.db.updateDocument(doc);
				}
			}
		}
    }
}
