ALTER TABLE `mrdlib`.`document` 
ADD COLUMN `checked` TIMESTAMP NULL DEFAULT NULL COMMENT 'The last time the document was checked for deletion/correctness; if not ever checked, NULL.' AFTER `deleted`;
