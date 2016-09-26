package org.mrdlib.recommendation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.mrdlib.Constants;

public class Probabilities {
	private Constants constants = new Constants();
	private String path = constants.getProbabilitiesPath();

	// db connection properties
	private int RandomDocumentRecommender;
	private int RandomDocumentRecommenderLanguageRestricted;
	private int RelatedDocumentsFromSolr;
	private int RelatedDocumentsFromSolrWithKeyphrases;
	private int StereotypeRecommender;

	public Probabilities() {

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = getClass().getClassLoader().getResourceAsStream(path);
			prop.load(input);

			// get the probability values
			this.RandomDocumentRecommender = Integer.parseInt(prop.getProperty("RandomDocumentRecommender"));
			this.RandomDocumentRecommenderLanguageRestricted = Integer
					.parseInt(prop.getProperty("RandomDocumentRecommenderLanguageRestricted"));
			this.RelatedDocumentsFromSolr = Integer.parseInt(prop.getProperty("RelatedDocumentsFromSolr"));
			this.RelatedDocumentsFromSolrWithKeyphrases = Integer
					.parseInt(prop.getProperty("RelatedDocumentsFromSolrWithKeyphrases"));
			this.StereotypeRecommender = Integer.parseInt(prop.getProperty("StereotypeRecommender"));

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

	public int getRandomDocumentRecommender() {
		return RandomDocumentRecommender;
	}

	public int getRandomDocumentRecommenderLanguageRestricted() {
		return RandomDocumentRecommenderLanguageRestricted;
	}

	public int getRelatedDocumentsFromSolr() {
		return RelatedDocumentsFromSolr;
	}

	public int getRelatedDocumentsFromSolrWithKeyphrases() {
		return RelatedDocumentsFromSolrWithKeyphrases;
	}

	public int getStereotypeRecommender() {
		return StereotypeRecommender;
	}
}
