package org.mrdlib.recommendation.algorithm;

import java.util.Random;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.recommendation.framework.NoRelatedDocumentsException;

public class RelatedDocumentsKeyphrases extends RelatedDocumentsMLT {
	/**
	 * Creates a new instance of RelatedDocumentsKeyphrases which exposes
	 * methods to use find related articles using the similarity in keyphrases
	 * Also chooses the type of keyphrase approach. Unigrams, bigrams, or
	 * trigrams? Should it use the abstract?
	 * 
	 * @param con
	 *            DBConnection instance, not null, to access database methods
	 * @throws Exception
	 *             if solrConnection cannot be instantiated.
	 */
	public RelatedDocumentsKeyphrases(DBConnection con) throws Exception {
		super(con);
		String name = "";

		// Randomly initialize properties of the keyphrase approach
		Random random = new Random();

		// Unigrams, bigrams, trigrams have an equal chance of being used
		Boolean unigrams = random.nextBoolean();
		Boolean bigrams = random.nextBoolean();
		Boolean trigrams = random.nextBoolean();

		// Flip a coin to decide to use abstracts or not
		Boolean abstracts = random.nextBoolean();
		int sum = ((unigrams ? 1 : 0) + (bigrams ? 1 : 0) + (trigrams ? 1 : 0));
		while (sum == 0) {
			unigrams = random.nextBoolean();
			bigrams = random.nextBoolean();
			trigrams = random.nextBoolean();
			sum = ((unigrams ? 1 : 0) + (bigrams ? 1 : 0) + (trigrams ? 1 : 0));
		}

		// generate a string which represents the combination of keyphrases to
		// use
		if (sum == 3) {
			name = "unibitri";
		} else if (sum == 2) {
			if (!unigrams) {
				name = "bitri";
			} else
				name = bigrams ? "unibi" : "unitri";
		} else {
			if (unigrams) {
				name = "unigram";
			} else
				name = bigrams ? "bigram" : "trigram";
		}

		// Set the randomly generated properties in the algorithmLoggingInfo hashmap for
		// future use
		algorithmLoggingInfo.setCbfTextFields("title" + (abstracts ? "_abstract" : ""));
		algorithmLoggingInfo.setName("RelatedDocumentsFromSolrWithKeyphrases");
		algorithmLoggingInfo.setCbfFeatureType(name);
	}

	@Override
	/**
	 * Picks the number of keyphrases to use depending on how many there are for
	 * the document, then queries Solr for the related documents
	 * 
	 */
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs) throws Exception {
		try {

			// Get the minimum basis for the keyphrase comparison based on the
			// fields that we compare on
			int maxNumber = con.getMinimumNumberOfKeyphrases(requestDoc.getDocumentId(), algorithmLoggingInfo.getCbfFeatureType(),
					algorithmLoggingInfo.getCbfTextFields());

			// If no comparison is possible because, say, there are no trigrams,
			// which are needed for a bitri comparison, throw Exception
			if (maxNumber < 1)
				throw new NoRelatedDocumentsException(requestDoc.getOriginalDocumentId(), requestDoc.getDocumentId());

			// Else pick random number of features <= minimum basis
			Random random = new Random();
			int cbf_feature_count = maxNumber == 1 ? 1 : random.nextInt(maxNumber - 1) + 1;

			// Set algorithmLoggingInfo with the feature count that is used
			algorithmLoggingInfo.setCbfFeatureCount(Integer.toString(cbf_feature_count));

			// Query solr for the related documents
			return scon.getRelatedDocumentSetByDocument(requestDoc, numberOfRelatedDocs, algorithmLoggingInfo);
		} catch (NoRelatedDocumentsException f) {
			System.out.println("No related documents for doc_id " + requestDoc.getDocumentId());
			throw f;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} /*finally {
		if (scon != null)
		scon.close();
}*/
	}
}
