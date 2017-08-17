select doc.document_id, doc.title, abs.abstract, doc.language_detected from mrdlib.document as doc
left outer join mrdlib.document_abstract as abs on doc.document_id = abs.document_id
where (abs.abstract is null or abs.language_detected = doc.language_detected) and doc.deleted is null
into outfile '/var/lib/mysql-files/title_abstract_dump.csv'
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
ESCAPED BY '\\'
LINES TERMINATED BY '\n';

select doc.document_id, doc.title, doc.language_detected from mrdlib.document as doc
where doc.deleted is null
into outfile '/var/lib/mysql-files/title_dump.csv'
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
ESCAPED BY '\\'
LINES TERMINATED BY '\n';

select doc.document_id, abs.abstract, doc.language_detected from mrdlib.document as doc
left outer join mrdlib.document_abstract as abs on doc.document_id = abs.document_id
where abs.abstract is not null and abs.language_detected = doc.language_detected and doc.deleted is null
into outfile '/var/lib/mysql-files/abstract_dump.csv'
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
ESCAPED BY '\\'
LINES TERMINATED BY '\n';
