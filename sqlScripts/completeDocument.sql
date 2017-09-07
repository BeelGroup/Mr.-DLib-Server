SELECT d.`title`, d.`language`, d.`published_in`, d.`type`, d.`keywords`, d.`publication_year`,
d.`title_clean`, d.`collection_id`, p.`name_first`, p.`name_middle`, p.`name_last`,
a.`abstract`, a.`language` as abstr_lang
from `mrdlib`.`document` as d
inner join `mrdlib`.`document_person` dp on dp.`document_id` = d.`document_id`
inner join `mrdlib`.`person` p on p.`person_id` = dp.`person_id`
inner join `mrdlib`.`document_abstract` a on a.`document_id` = d.`document_id`
where d.`id_original` = 'core-25047423';
