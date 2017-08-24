ALTER TABLE `mrdlib`.`document` 
ADD COLUMN `checked` TIMESTAMP NULL DEFAULT NULL COMMENT 'The last time the document was checked for deletion/correctness; if not ever checked, NULL.' AFTER `added`,
ADD COLUMN `deleted` TIMESTAMP NULL DEFAULT NULL COMMENT 'if the document has been removed from MDL, the timestamp of deletion, otherwise NULL' AFTER `added`;

