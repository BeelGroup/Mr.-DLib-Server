package org.mrdlib.partnerContentManager.core;

import java.util.HashMap;
import java.util.Map;

public class Repository {

private String id;
private long openDoarId;
private String name;
private Object uri;
private Object uriJournals;
private String physicalName;
private Object source;
private Object software;
private Object metadataFormat;
private Object description;
private Object journal;
private Object pdfStatus;
private long nrUpdates;
private boolean disabled;
private Object lastUpdateTime;
private long metadataRecordCount;
private long metadataDeletedRecordCount;
private long metadataLinkCount;
private long metadataSize;
private long journalMetadataSize;
private Object metadataAge;
private Object journalMetadataAge;
private long metadataInIndexCount;
private long metadataDeletedInIndexCount;
private long metadataAlloweInIndexCount;
private long metadataDisabledInIndexCount;
private Object metadataExtractionDate;
private Object journalMetadataExtractionDate;
private long databaseRecordCount;
private long databaseDeletedRecordCount;
private long databasePdfLinkCount;
private long databasePdfCount;
private long databaseDeletedPdfCount;
private long hardDrivePdfSize;
private long hardDrivePdfCount;
private long hardDriveDeletedPdfCount;
private long databaseTextCount;
private long databaseTextNotDeletedCount;
private long hardDriveTextCount;
private long hardDriveDeletedTextCount;
private long databaseIndexCount;
private long indexRecordCount;
private long indexJournalCount;
private long indexTextCount;
private long metadataOnlyIndex;
private long indexTextCountDB;
private long indexedPdfDB;
private long indexedDisabledDB;
private long indexTextNotDeletedCount;
private long hardDriveCitationFiles;
private long citationFilesDb;
private long crawlingLimit;
private long citationCount;
private long citationWithDocCount;
private long citationDoiCount;
private long documentDoiCount;
private long documentDoiWithFulltextCount;
private Object repositoryLocation;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

/**
* No args constructor for use in serialization
* 
*/
public Repository() {
}

/**
* 
* @param crawlingLimit
* @param databaseIndexCount
* @param metadataAlloweInIndexCount
* @param metadataSize
* @param hardDrivePdfCount
* @param metadataDisabledInIndexCount
* @param pdfStatus
* @param repositoryLocation
* @param documentDoiWithFulltextCount
* @param journalMetadataExtractionDate
* @param databaseDeletedRecordCount
* @param software
* @param indexedPdfDB
* @param indexedDisabledDB
* @param description
* @param hardDriveDeletedTextCount
* @param physicalName
* @param journalMetadataSize
* @param hardDrivePdfSize
* @param citationWithDocCount
* @param uri
* @param indexTextCountDB
* @param uriJournals
* @param citationDoiCount
* @param databasePdfLinkCount
* @param hardDriveTextCount
* @param openDoarId
* @param metadataExtractionDate
* @param citationCount
* @param lastUpdateTime
* @param journalMetadataAge
* @param databaseTextNotDeletedCount
* @param hardDriveCitationFiles
* @param id
* @param journal
* @param metadataRecordCount
* @param indexRecordCount
* @param name
* @param metadataFormat
* @param metadataDeletedRecordCount
* @param metadataLinkCount
* @param metadataAge
* @param databaseDeletedPdfCount
* @param metadataOnlyIndex
* @param metadataInIndexCount
* @param databaseRecordCount
* @param nrUpdates
* @param citationFilesDb
* @param indexTextNotDeletedCount
* @param indexJournalCount
* @param source
* @param databasePdfCount
* @param databaseTextCount
* @param documentDoiCount
* @param hardDriveDeletedPdfCount
* @param indexTextCount
* @param metadataDeletedInIndexCount
* @param disabled
*/
public Repository(String id, long openDoarId, String name, Object uri, Object uriJournals, String physicalName, Object source, Object software, Object metadataFormat, Object description, Object journal, Object pdfStatus, long nrUpdates, boolean disabled, Object lastUpdateTime, long metadataRecordCount, long metadataDeletedRecordCount, long metadataLinkCount, long metadataSize, long journalMetadataSize, Object metadataAge, Object journalMetadataAge, long metadataInIndexCount, long metadataDeletedInIndexCount, long metadataAlloweInIndexCount, long metadataDisabledInIndexCount, Object metadataExtractionDate, Object journalMetadataExtractionDate, long databaseRecordCount, long databaseDeletedRecordCount, long databasePdfLinkCount, long databasePdfCount, long databaseDeletedPdfCount, long hardDrivePdfSize, long hardDrivePdfCount, long hardDriveDeletedPdfCount, long databaseTextCount, long databaseTextNotDeletedCount, long hardDriveTextCount, long hardDriveDeletedTextCount, long databaseIndexCount, long indexRecordCount, long indexJournalCount, long indexTextCount, long metadataOnlyIndex, long indexTextCountDB, long indexedPdfDB, long indexedDisabledDB, long indexTextNotDeletedCount, long hardDriveCitationFiles, long citationFilesDb, long crawlingLimit, long citationCount, long citationWithDocCount, long citationDoiCount, long documentDoiCount, long documentDoiWithFulltextCount, Object repositoryLocation) {
super();
this.id = id;
this.openDoarId = openDoarId;
this.name = name;
this.uri = uri;
this.uriJournals = uriJournals;
this.physicalName = physicalName;
this.source = source;
this.software = software;
this.metadataFormat = metadataFormat;
this.description = description;
this.journal = journal;
this.pdfStatus = pdfStatus;
this.nrUpdates = nrUpdates;
this.disabled = disabled;
this.lastUpdateTime = lastUpdateTime;
this.metadataRecordCount = metadataRecordCount;
this.metadataDeletedRecordCount = metadataDeletedRecordCount;
this.metadataLinkCount = metadataLinkCount;
this.metadataSize = metadataSize;
this.journalMetadataSize = journalMetadataSize;
this.metadataAge = metadataAge;
this.journalMetadataAge = journalMetadataAge;
this.metadataInIndexCount = metadataInIndexCount;
this.metadataDeletedInIndexCount = metadataDeletedInIndexCount;
this.metadataAlloweInIndexCount = metadataAlloweInIndexCount;
this.metadataDisabledInIndexCount = metadataDisabledInIndexCount;
this.metadataExtractionDate = metadataExtractionDate;
this.journalMetadataExtractionDate = journalMetadataExtractionDate;
this.databaseRecordCount = databaseRecordCount;
this.databaseDeletedRecordCount = databaseDeletedRecordCount;
this.databasePdfLinkCount = databasePdfLinkCount;
this.databasePdfCount = databasePdfCount;
this.databaseDeletedPdfCount = databaseDeletedPdfCount;
this.hardDrivePdfSize = hardDrivePdfSize;
this.hardDrivePdfCount = hardDrivePdfCount;
this.hardDriveDeletedPdfCount = hardDriveDeletedPdfCount;
this.databaseTextCount = databaseTextCount;
this.databaseTextNotDeletedCount = databaseTextNotDeletedCount;
this.hardDriveTextCount = hardDriveTextCount;
this.hardDriveDeletedTextCount = hardDriveDeletedTextCount;
this.databaseIndexCount = databaseIndexCount;
this.indexRecordCount = indexRecordCount;
this.indexJournalCount = indexJournalCount;
this.indexTextCount = indexTextCount;
this.metadataOnlyIndex = metadataOnlyIndex;
this.indexTextCountDB = indexTextCountDB;
this.indexedPdfDB = indexedPdfDB;
this.indexedDisabledDB = indexedDisabledDB;
this.indexTextNotDeletedCount = indexTextNotDeletedCount;
this.hardDriveCitationFiles = hardDriveCitationFiles;
this.citationFilesDb = citationFilesDb;
this.crawlingLimit = crawlingLimit;
this.citationCount = citationCount;
this.citationWithDocCount = citationWithDocCount;
this.citationDoiCount = citationDoiCount;
this.documentDoiCount = documentDoiCount;
this.documentDoiWithFulltextCount = documentDoiWithFulltextCount;
this.repositoryLocation = repositoryLocation;
}

public String getId() {
return id;
}

public void setId(String id) {
this.id = id;
}

public long getOpenDoarId() {
return openDoarId;
}

public void setOpenDoarId(long openDoarId) {
this.openDoarId = openDoarId;
}

public String getName() {
return name;
}

public void setName(String name) {
this.name = name;
}

public Object getUri() {
return uri;
}

public void setUri(Object uri) {
this.uri = uri;
}

public Object getUriJournals() {
return uriJournals;
}

public void setUriJournals(Object uriJournals) {
this.uriJournals = uriJournals;
}

public String getPhysicalName() {
return physicalName;
}

public void setPhysicalName(String physicalName) {
this.physicalName = physicalName;
}

public Object getSource() {
return source;
}

public void setSource(Object source) {
this.source = source;
}

public Object getSoftware() {
return software;
}

public void setSoftware(Object software) {
this.software = software;
}

public Object getMetadataFormat() {
return metadataFormat;
}

public void setMetadataFormat(Object metadataFormat) {
this.metadataFormat = metadataFormat;
}

public Object getDescription() {
return description;
}

public void setDescription(Object description) {
this.description = description;
}

public Object getJournal() {
return journal;
}

public void setJournal(Object journal) {
this.journal = journal;
}

public Object getPdfStatus() {
return pdfStatus;
}

public void setPdfStatus(Object pdfStatus) {
this.pdfStatus = pdfStatus;
}

public long getNrUpdates() {
return nrUpdates;
}

public void setNrUpdates(long nrUpdates) {
this.nrUpdates = nrUpdates;
}

public boolean isDisabled() {
return disabled;
}

public void setDisabled(boolean disabled) {
this.disabled = disabled;
}

public Object getLastUpdateTime() {
return lastUpdateTime;
}

public void setLastUpdateTime(Object lastUpdateTime) {
this.lastUpdateTime = lastUpdateTime;
}

public long getMetadataRecordCount() {
return metadataRecordCount;
}

public void setMetadataRecordCount(long metadataRecordCount) {
this.metadataRecordCount = metadataRecordCount;
}

public long getMetadataDeletedRecordCount() {
return metadataDeletedRecordCount;
}

public void setMetadataDeletedRecordCount(long metadataDeletedRecordCount) {
this.metadataDeletedRecordCount = metadataDeletedRecordCount;
}

public long getMetadataLinkCount() {
return metadataLinkCount;
}

public void setMetadataLinkCount(long metadataLinkCount) {
this.metadataLinkCount = metadataLinkCount;
}

public long getMetadataSize() {
return metadataSize;
}

public void setMetadataSize(long metadataSize) {
this.metadataSize = metadataSize;
}

public long getJournalMetadataSize() {
return journalMetadataSize;
}

public void setJournalMetadataSize(long journalMetadataSize) {
this.journalMetadataSize = journalMetadataSize;
}

public Object getMetadataAge() {
return metadataAge;
}

public void setMetadataAge(Object metadataAge) {
this.metadataAge = metadataAge;
}

public Object getJournalMetadataAge() {
return journalMetadataAge;
}

public void setJournalMetadataAge(Object journalMetadataAge) {
this.journalMetadataAge = journalMetadataAge;
}

public long getMetadataInIndexCount() {
return metadataInIndexCount;
}

public void setMetadataInIndexCount(long metadataInIndexCount) {
this.metadataInIndexCount = metadataInIndexCount;
}

public long getMetadataDeletedInIndexCount() {
return metadataDeletedInIndexCount;
}

public void setMetadataDeletedInIndexCount(long metadataDeletedInIndexCount) {
this.metadataDeletedInIndexCount = metadataDeletedInIndexCount;
}

public long getMetadataAlloweInIndexCount() {
return metadataAlloweInIndexCount;
}

public void setMetadataAlloweInIndexCount(long metadataAlloweInIndexCount) {
this.metadataAlloweInIndexCount = metadataAlloweInIndexCount;
}

public long getMetadataDisabledInIndexCount() {
return metadataDisabledInIndexCount;
}

public void setMetadataDisabledInIndexCount(long metadataDisabledInIndexCount) {
this.metadataDisabledInIndexCount = metadataDisabledInIndexCount;
}

public Object getMetadataExtractionDate() {
return metadataExtractionDate;
}

public void setMetadataExtractionDate(Object metadataExtractionDate) {
this.metadataExtractionDate = metadataExtractionDate;
}

public Object getJournalMetadataExtractionDate() {
return journalMetadataExtractionDate;
}

public void setJournalMetadataExtractionDate(Object journalMetadataExtractionDate) {
this.journalMetadataExtractionDate = journalMetadataExtractionDate;
}

public long getDatabaseRecordCount() {
return databaseRecordCount;
}

public void setDatabaseRecordCount(long databaseRecordCount) {
this.databaseRecordCount = databaseRecordCount;
}

public long getDatabaseDeletedRecordCount() {
return databaseDeletedRecordCount;
}

public void setDatabaseDeletedRecordCount(long databaseDeletedRecordCount) {
this.databaseDeletedRecordCount = databaseDeletedRecordCount;
}

public long getDatabasePdfLinkCount() {
return databasePdfLinkCount;
}

public void setDatabasePdfLinkCount(long databasePdfLinkCount) {
this.databasePdfLinkCount = databasePdfLinkCount;
}

public long getDatabasePdfCount() {
return databasePdfCount;
}

public void setDatabasePdfCount(long databasePdfCount) {
this.databasePdfCount = databasePdfCount;
}

public long getDatabaseDeletedPdfCount() {
return databaseDeletedPdfCount;
}

public void setDatabaseDeletedPdfCount(long databaseDeletedPdfCount) {
this.databaseDeletedPdfCount = databaseDeletedPdfCount;
}

public long getHardDrivePdfSize() {
return hardDrivePdfSize;
}

public void setHardDrivePdfSize(long hardDrivePdfSize) {
this.hardDrivePdfSize = hardDrivePdfSize;
}

public long getHardDrivePdfCount() {
return hardDrivePdfCount;
}

public void setHardDrivePdfCount(long hardDrivePdfCount) {
this.hardDrivePdfCount = hardDrivePdfCount;
}

public long getHardDriveDeletedPdfCount() {
return hardDriveDeletedPdfCount;
}

public void setHardDriveDeletedPdfCount(long hardDriveDeletedPdfCount) {
this.hardDriveDeletedPdfCount = hardDriveDeletedPdfCount;
}

public long getDatabaseTextCount() {
return databaseTextCount;
}

public void setDatabaseTextCount(long databaseTextCount) {
this.databaseTextCount = databaseTextCount;
}

public long getDatabaseTextNotDeletedCount() {
return databaseTextNotDeletedCount;
}

public void setDatabaseTextNotDeletedCount(long databaseTextNotDeletedCount) {
this.databaseTextNotDeletedCount = databaseTextNotDeletedCount;
}

public long getHardDriveTextCount() {
return hardDriveTextCount;
}

public void setHardDriveTextCount(long hardDriveTextCount) {
this.hardDriveTextCount = hardDriveTextCount;
}

public long getHardDriveDeletedTextCount() {
return hardDriveDeletedTextCount;
}

public void setHardDriveDeletedTextCount(long hardDriveDeletedTextCount) {
this.hardDriveDeletedTextCount = hardDriveDeletedTextCount;
}

public long getDatabaseIndexCount() {
return databaseIndexCount;
}

public void setDatabaseIndexCount(long databaseIndexCount) {
this.databaseIndexCount = databaseIndexCount;
}

public long getIndexRecordCount() {
return indexRecordCount;
}

public void setIndexRecordCount(long indexRecordCount) {
this.indexRecordCount = indexRecordCount;
}

public long getIndexJournalCount() {
return indexJournalCount;
}

public void setIndexJournalCount(long indexJournalCount) {
this.indexJournalCount = indexJournalCount;
}

public long getIndexTextCount() {
return indexTextCount;
}

public void setIndexTextCount(long indexTextCount) {
this.indexTextCount = indexTextCount;
}

public long getMetadataOnlyIndex() {
return metadataOnlyIndex;
}

public void setMetadataOnlyIndex(long metadataOnlyIndex) {
this.metadataOnlyIndex = metadataOnlyIndex;
}

public long getIndexTextCountDB() {
return indexTextCountDB;
}

public void setIndexTextCountDB(long indexTextCountDB) {
this.indexTextCountDB = indexTextCountDB;
}

public long getIndexedPdfDB() {
return indexedPdfDB;
}

public void setIndexedPdfDB(long indexedPdfDB) {
this.indexedPdfDB = indexedPdfDB;
}

public long getIndexedDisabledDB() {
return indexedDisabledDB;
}

public void setIndexedDisabledDB(long indexedDisabledDB) {
this.indexedDisabledDB = indexedDisabledDB;
}

public long getIndexTextNotDeletedCount() {
return indexTextNotDeletedCount;
}

public void setIndexTextNotDeletedCount(long indexTextNotDeletedCount) {
this.indexTextNotDeletedCount = indexTextNotDeletedCount;
}

public long getHardDriveCitationFiles() {
return hardDriveCitationFiles;
}

public void setHardDriveCitationFiles(long hardDriveCitationFiles) {
this.hardDriveCitationFiles = hardDriveCitationFiles;
}

public long getCitationFilesDb() {
return citationFilesDb;
}

public void setCitationFilesDb(long citationFilesDb) {
this.citationFilesDb = citationFilesDb;
}

public long getCrawlingLimit() {
return crawlingLimit;
}

public void setCrawlingLimit(long crawlingLimit) {
this.crawlingLimit = crawlingLimit;
}

public long getCitationCount() {
return citationCount;
}

public void setCitationCount(long citationCount) {
this.citationCount = citationCount;
}

public long getCitationWithDocCount() {
return citationWithDocCount;
}

public void setCitationWithDocCount(long citationWithDocCount) {
this.citationWithDocCount = citationWithDocCount;
}

public long getCitationDoiCount() {
return citationDoiCount;
}

public void setCitationDoiCount(long citationDoiCount) {
this.citationDoiCount = citationDoiCount;
}

public long getDocumentDoiCount() {
return documentDoiCount;
}

public void setDocumentDoiCount(long documentDoiCount) {
this.documentDoiCount = documentDoiCount;
}

public long getDocumentDoiWithFulltextCount() {
return documentDoiWithFulltextCount;
}

public void setDocumentDoiWithFulltextCount(long documentDoiWithFulltextCount) {
this.documentDoiWithFulltextCount = documentDoiWithFulltextCount;
}

public Object getRepositoryLocation() {
return repositoryLocation;
}

public void setRepositoryLocation(Object repositoryLocation) {
this.repositoryLocation = repositoryLocation;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}