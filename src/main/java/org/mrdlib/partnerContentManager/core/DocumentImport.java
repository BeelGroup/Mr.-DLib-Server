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

public class DocumentImport
{
    private DBConnection db;
    private Constants constants;
    private CoreApi api;
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
		}
    }
}
