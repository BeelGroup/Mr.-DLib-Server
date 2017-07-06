package org.mrdlib.recommendation.algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.mrdlib.api.manager.Constants;

public class Probabilities {
	private Constants constants = new Constants();
	private String path = constants.getProbabilitiesPath();

	// probabilities for recommenders
	private int randomDocumentRecommender;
	private int randomDocumentRecommenderLanguageRestricted;
	private int relatedDocumentsFromSolr;
	private int relatedDocumentsFromSolrWithKeyphrases;
	private int stereotypeRecommender;
	private int mostPopular;

	// the constructor loads the probablities from the probabilities.properties
	// file
	public Probabilities() {

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = getClass().getClassLoader().getResourceAsStream(path);
			prop.load(input);

			// get the probability values
			this.randomDocumentRecommender = Integer.parseInt(prop.getProperty("RandomDocumentRecommender"));
			this.randomDocumentRecommenderLanguageRestricted = Integer
					.parseInt(prop.getProperty("RandomDocumentRecommenderLanguageRestricted"));
			this.relatedDocumentsFromSolr = Integer.parseInt(prop.getProperty("RelatedDocumentsFromSolr"));
			this.relatedDocumentsFromSolrWithKeyphrases = Integer
					.parseInt(prop.getProperty("RelatedDocumentsFromSolrWithKeyphrases"));
			this.stereotypeRecommender = Integer.parseInt(prop.getProperty("StereotypeRecommender"));
			this.mostPopular = Integer.parseInt(prop.getProperty("MostPopularRecommender"));

			if ((this.randomDocumentRecommender + this.randomDocumentRecommenderLanguageRestricted
					+ this.stereotypeRecommender + this.relatedDocumentsFromSolr
					+ this.relatedDocumentsFromSolrWithKeyphrases + this.mostPopular) != 10000) {
				this.randomDocumentRecommender = 0;
				this.randomDocumentRecommenderLanguageRestricted = 0;
				this.relatedDocumentsFromSolr = 10000;
				this.relatedDocumentsFromSolrWithKeyphrases = 0;
				this.stereotypeRecommender = 0;
				this.mostPopular = 0;
				System.out.println("Probabilities do not sum up to 100%: Defaulting to Backup algorithm");
			}
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

	/*
	 * Getter for probability of Random Document Recommender
	 */
	public int getRandomDocumentRecommender() {
		return randomDocumentRecommender;
	}

	/*
	 * Getter for probability of Random Document Recommender Language Restricted
	 */
	public int getRandomDocumentRecommenderLanguageRestricted() {
		return randomDocumentRecommenderLanguageRestricted;
	}

	/*
	 * Getter for probability of LuceneMLT
	 */
	public int getRelatedDocumentsFromSolr() {
		return relatedDocumentsFromSolr;
	}

	/*
	 * Getter for probability of Keyphrase Approach
	 */
	public int getRelatedDocumentsFromSolrWithKeyphrases() {
		return relatedDocumentsFromSolrWithKeyphrases;
	}

	/*
	 * Getter for probability of Stereotype Recommender
	 */
	public int getStereotypeRecommender() {
		return stereotypeRecommender;
	}

	public int getMostPopular() {
		// TODO Auto-generated method stub
		return mostPopular;
	}
}
