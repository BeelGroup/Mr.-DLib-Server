package org.mrdlib.recommendation.algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;

import org.mrdlib.api.manager.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Probabilities {
	private Logger logger;
	private Constants constants = new Constants();
	private String path = constants.getProbabilitiesPath();
	private Random random;
	private static final int TOTAL = 10000;

	// probabilities for recommenders
	private List<Integer> probs = new ArrayList<Integer>();

	/**
	 * fallback - first one, always
	 */
	private void reset() {
		probs.clear();
		probs.add(TOTAL);
		for (int i = 1; i < Algorithm.values().length; i++)
			probs.add(0);
	}

	/** 
	 * the constructor loads the probablities from the probabilities.properties file
	 */
	public Probabilities() {
		random = new Random();
		Properties prop = new Properties();
		InputStream input = null;
		logger = LoggerFactory.getLogger(Probabilities.class);

		try {
			input = getClass().getClassLoader().getResourceAsStream(path);
			prop.load(input);

			int sum = 0;
			for (Algorithm choice : Algorithm.values()) {
				int probability = Integer.parseInt(prop.getProperty(choice.name()));
				this.probs.add(probability);
				sum += probability;
			}
			if (sum != TOTAL) {
				logger.error("Probabilities do not sum up ({}, should be {}), falling back to default.", sum, TOTAL);
				reset();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Could not read probabilites; falling back to default.", ex);
			reset();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Could not close file.", e);
				}
			}
		}
	}

	/**
	 * generate random next choice
	 */
	public Algorithm next() {
		int num = random.nextInt(TOTAL);
		int cum = 0;
		for (int i = 0; i < Algorithm.values().length; i++) {
			cum += probs.get(i);
			if (num < cum) {
				return Algorithm.values()[i];
			}
		}
		throw new Error("Math failed us. This doesn't add up.");
	}


}
