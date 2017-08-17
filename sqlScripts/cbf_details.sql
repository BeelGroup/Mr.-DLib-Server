ALTER TABLE `mrdlib`.`recommendation_algorithm__details_cbf` 
CHANGE COLUMN `corpus` `training_corpus` ENUM('GloVe') CHARACTER SET 'utf8' COLLATE 'utf8_unicode_ci' NULL DEFAULT NULL COMMENT 'Source of word embeddings used for training paragraph vectors',
CHANGE COLUMN `dimensions` `feature_dimensions` smallint(5) unsigned DEFAULT NULL COMMENT 'The number of dimensions used for document embeddings',
CHANGE COLUMN `matching` `matching` enum('bm25') COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'The matching algorithm used to compare similarities of documents',
CHANGE COLUMN `feature_type` `feature_type` enum('terms','keyphrases','embeddings') CHARACTER SET latin1 DEFAULT NULL COMMENT 'The type of feature that was used to calculate similarities between documents, for instance terms or keyphrases',
DROP INDEX `cbf_description`,
ADD UNIQUE INDEX `cbf_description` (`feature_type` ASC, `ngram_type` ASC, `feature_count` ASC, `content_fields_input_document` ASC, `search_mode` ASC, `matching` ASC, `input` ASC, `feature_dimensions` ASC, `training_corpus` ASC)
;
