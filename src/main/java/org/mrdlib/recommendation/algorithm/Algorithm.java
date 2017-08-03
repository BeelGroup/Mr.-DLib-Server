package org.mrdlib.recommendation.algorithm;

// options in config file probabilites.properties must be written the same
public enum Algorithm {
	FROM_SOLR,
	RANDOM_DOCUMENT,
	RANDOM_LANGUAGE_RESTRICTED,
	FROM_SOLR_WITH_KEYPHRASES,
	STEREOTYPE,
	MOST_POPULAR,
	DOC2VEC
}
