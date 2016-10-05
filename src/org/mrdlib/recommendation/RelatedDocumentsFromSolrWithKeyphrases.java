package org.mrdlib.recommendation;

import java.util.Random;

import org.mrdlib.database.DBConnection;
import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.DocumentSet;
import org.mrdlib.solrHandler.NoRelatedDocumentsException;

public class RelatedDocumentsFromSolrWithKeyphrases extends RelatedDocumentsFromSolr {
	public RelatedDocumentsFromSolrWithKeyphrases(DBConnection con) throws Exception {
		super(con);
		String name = "";
		Random random = new Random();
		Boolean unigrams = random.nextBoolean();
		Boolean bigrams = random.nextBoolean();
		Boolean trigrams = random.nextBoolean();
		Boolean abstracts = random.nextBoolean();
		int sum = ((unigrams ? 1 : 0) + (bigrams ? 1 : 0) + (trigrams ? 1 : 0));
		while (sum == 0) {
			unigrams = random.nextBoolean();
			bigrams = random.nextBoolean();
			trigrams = random.nextBoolean();
			sum = ((unigrams ? 1 : 0) + (bigrams ? 1 : 0) + (trigrams ? 1 : 0));
		}
		if (sum == 3) {
			name = "allgrams";
		} else if (sum == 2) {
			if (!unigrams) {
				name = "bitri";
			} else
				name = bigrams ? "unibi" : "unitri";
		} else {
			if (unigrams) {
				name = "unigrams";
			} else
				name = bigrams ? "bigrams" : "trigrams";
		}

		loggingInfo.replace("cbf_text_fields", "title" + (abstracts ? "_abstract" : ""));
		loggingInfo.replace("name", "RelatedDocumentsFromSolrWithKeyphrases");
		loggingInfo.replace("cbf_feature_type", "keyphrase_(" + name + ")");
		loggingInfo.put("typeOfGram", name);
	}

	@Override
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs) throws Exception {
		try {
			int maxNumber = con.getMinimumNumberOfKeyphrases(requestDoc.getDocumentId(), loggingInfo.get("typeOfGram"),
					loggingInfo.get("cbf_text_fields"));
			if (maxNumber < 1)
				throw new NoRelatedDocumentsException(requestDoc.getOriginalDocumentId(), requestDoc.getDocumentId());
			Random random = new Random();
			System.out.println(loggingInfo.get("typeOfGram"));
			System.out.println(maxNumber);
			int cbf_feature_count = random.nextInt(maxNumber - 1) + 1;
			loggingInfo.replace("cbf_feature_count", Integer.toString(cbf_feature_count));
			return scon.getRelatedDocumentSetByDocument(requestDoc, numberOfRelatedDocs, loggingInfo);
		} catch (NoRelatedDocumentsException f) {
			System.out.println("No related documents for doc_id " + requestDoc.getDocumentId());
			throw f;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			loggingInfo.remove("typeOfGram");
			if (scon != null)
				scon.close();
		}
	}
}
