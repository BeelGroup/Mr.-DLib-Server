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
import org.mrdlib.partnerContentManager.general.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentImport
{
    private DBConnection db;
    private Constants constants;
    private CoreApi api;
	private Logger logger = LoggerFactory.getLogger(DocumentImport.class);
    public static final long BATCH_SIZE = 1000;

    public DocumentImport () {
		try {
			this.constants = new Constants();
			this.db = new DBConnection("jar");
			this.api = new CoreApi();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public boolean hasDocumentInDB(Integer id) throws Exception {
		try {
			String idOriginal = String.format("%s-%d", constants.getCore(), id);
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
		doc.setLanguage(article.getLanguage().getCode()); // TODO: check mapping
		doc.setYear(article.getYear().toString());	
		doc.normalize();
		for (String keyword : article.getTopics()) { // or getSubjects()? TODO: check
			doc.addKeyword(keyword);
		}
		if (article.getDescription() != null)
			doc.addAbstract(article.getDescription());
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
			try {
				Long collectionId = db.getCollectionIDByName(article.getRepositories().get(0).getName(), false);
				doc.setCollectionId(collectionId.toString());
			} catch(Exception e) {
				logger.trace("Could not get collection id for name {} while importing article {}", 
						article.getRepositories().get(0).getName(), article);
			}
		} else {
			logger.trace("no collection id for article {}", article);
		}

		if(article.getOai() != null)
			doc.addExternalId("oai", article.getOai());

		if(article.getDoi() != null)
			doc.addExternalId("doi", article.getDoi());

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
		for (int i = 0; i < 10; i++) {
			if ((article = iter.next()) != null) {
				System.out.println(article);
			}
			System.out.println();
			System.out.println("------------------------------------------------------------");
			System.out.println();
			System.out.println(importer.convert(article));
		}
    }
}
