package org.mrdlib.recommendation.algorithm;

import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// options in config file probabilites.properties must be written the same
public enum Algorithm {
	FROM_SOLR,
	RANDOM_DOCUMENT,
	RANDOM_LANGUAGE_RESTRICTED,
	FROM_SOLR_WITH_KEYPHRASES,
	STEREOTYPE,
	MOST_POPULAR,
	DOC2VEC;

	public static Algorithm parse(String name) throws IllegalArgumentException, NullPointerException {
		return Algorithm.valueOf(name.toUpperCase());
	}

	private static final Map<Algorithm, List<String>> languageRestrictedAlgorithms;

	static {
		languageRestrictedAlgorithms = new HashMap<Algorithm, List<String>>();
		languageRestrictedAlgorithms.put(Algorithm.DOC2VEC, Arrays.asList(new String[] { "en" }));
		languageRestrictedAlgorithms.put(Algorithm.FROM_SOLR_WITH_KEYPHRASES, Arrays.asList(new String[] { "en" }));
		languageRestrictedAlgorithms.put(Algorithm.RANDOM_LANGUAGE_RESTRICTED, null);
	}

	private static final List<Algorithm> titleSearchAlgorithms = 
		Arrays.asList(new Algorithm[] { FROM_SOLR, RANDOM_DOCUMENT, RANDOM_LANGUAGE_RESTRICTED, STEREOTYPE, MOST_POPULAR });

	public boolean hasTitleSearch() {
		return titleSearchAlgorithms.contains(this);
	}

	public boolean hasLanguageSupport(String lang) {
		if (languageRestrictedAlgorithms.containsKey(this)) {
			List<String> langs = languageRestrictedAlgorithms.get(this);
			if (langs == null) // needs to know language, but does not matter which it is
				return lang != null;
			else
				return langs.contains(lang);
		} else
			return true;
	}
}
