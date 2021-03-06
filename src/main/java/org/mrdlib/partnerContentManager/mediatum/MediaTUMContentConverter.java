package org.mrdlib.partnerContentManager.mediatum;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mrdlib.partnerContentManager.gesis.Tuple;

/**
 * Implementation of ContentConverter for partner mediaTUM.
 * mediaTUM offers a standardized OAI interface exhibiting data in the OAI Dublin Core format (http://www.openarchives.org/OAI/openarchivesprotocol.html).
 * 
 * @author wuestehube
 *
 */
public class MediaTUMContentConverter {

	/**
	 * Creates the mapping of type codes used in mediaTUM and Mr. DLib. Types are the publication types.
	 * 
	 * @return a type map that can be used in processing XML documents
	 */
	private Map<String, String> createTypeMap() {
		Map<String, String> typeMap = new HashMap<String, String>();
		
		typeMap.put("article_unknown", "article_unknown");
		typeMap.put("report", "report");
		typeMap.put("thesis_unspecified", "thesis_unspecified");
		typeMap.put("thesis_bachelor", "thesis_bachelor");
		typeMap.put("thesis_master", "thesis_master");
		typeMap.put("thesis_doctoral", "thesis_doctoral");
		typeMap.put("unknown", "unknown");
		
		return typeMap;
	}
	
	/**
	 * Creates the mapping of language codes used in mediaTUM and Mr. DLib.
	 * 
	 * @return a language map that can be used in processing XML documents
	 */
	private Map<String, String> createLanguageMap() {
		Map<String, String> languageMap = new HashMap<String, String>();
		
		languageMap.put("(eng)", "(en)");
		languageMap.put("eng", "(en)");
		// unknown is the default language
		languageMap.put("unknown", "(NULL)");
		languageMap.put("(unknown)", "(NULL)");
		languageMap.put("(deu)", "(de)");
		languageMap.put("deu", "(de)");
		languageMap.put("(ger)", "(de)");
		languageMap.put("ger", "(de)");
		languageMap.put("(spa)", "(es)");
		languageMap.put("spa", "(es)");
		languageMap.put("(fra)", "(fr)");
		languageMap.put("fra", "(fr)");
		languageMap.put("(fre)", "(fr)");
		languageMap.put("fre", "(fr)");
		languageMap.put("(zho)", "(zh)");
		languageMap.put("zho", "(zh)");
		languageMap.put("(chi)", "(zh)");
		languageMap.put("chi", "(zh)");
		languageMap.put("(jpn)", "(ja)");
		languageMap.put("jpn", "(ja)");
		languageMap.put("(rus)", "(ru)");
		languageMap.put("rus", "(ru)");
		
		return languageMap;
	}
	
	/**
	 * Creates the mapping of type resolve used in mediaTUM and Mr. DLib.
	 * 
	 * @return a type resolve map that can be used in processing XML documents
	 */
	private Map<Tuple, String> createTypeResolveMap() {
		HashMap<Tuple, String> typeResolveMap = new HashMap<Tuple, String>();
		
		// not needed in case of mediaTUM
		
		return typeResolveMap;
	}
	
	public MediaTUMXMLDocument convertPartnerContentToStorablePartnerContent(String pathOfFileToConvert) {
		// extract information
		OAIDCRecord oaidcRecord = readOAIDCRecordFromFile(pathOfFileToConvert);
		
		ArrayList<String> abstracts = getAbstractsFromOAIDCRecord(oaidcRecord);
		String language = getLanguageFromOAIDCRecord(oaidcRecord);
		// prefix original id analogous to existing original ids
		String idOriginal = "mediatum-" + getIdOriginalFromOAIDCRecord(oaidcRecord);
		String title = getTitleFromOAICDRecord(oaidcRecord);
		String fulltitle = getTitleFromOAICDRecord(oaidcRecord);
		// since the years that are stored in the XML file represent the last modification,
		// but not the publication year, ignore them by sending to default value that is later replaced with NULL
		String year = "0"; // getYearFromOAIDCRecord(oaidcRecord);
		String facetYear = "0";
		ArrayList<String> authors = getAuthorsFromOAIDCRecord(oaidcRecord);		
		ArrayList<String> keyWords = getKeyWordsFromOAIDCRecord(oaidcRecord);
		String type = getTypeFromOAIDCRecord(oaidcRecord);
		String publishedIn = getPublishedInFromOAIDCRecord(oaidcRecord);
		String collection = getCollectionFromOAIDCRecord(oaidcRecord);
		String license = getLicenseFromOAIDCRecord(oaidcRecord);
		String fullText = getFullTextFromOAIDCRecord(oaidcRecord);
		
		// check for errors
		if ((abstracts == null) || (language == null) || (idOriginal == null) || (title == null) ||
				(fulltitle == null) || (year == null) || (facetYear == null) || (authors == null) ||
				(keyWords == null) || (type == null) || (publishedIn == null) || (collection == null)) {
			return null;
		}
		
		// check if language of publication and abstract match, set publication to unknown if they don't
		boolean languageOfPublicationAndAbstractMatch = false;
		for (String abstract_ : abstracts) {
			String languageAbstract = abstract_.split(Pattern.quote("|"))[0];
			if (language.equals(languageAbstract)) {
				languageOfPublicationAndAbstractMatch = true;
				break;
			}
		}
		if (!languageOfPublicationAndAbstractMatch && (abstracts.size() != 0)) {
			language = "unknown";
		}
		
		// set up XML document
		Map<String, String> typeMap = createTypeMap();
		Map<String, String> languageMap = createLanguageMap();
		Map<Tuple, String> typeResolveMap = createTypeResolveMap();
		
		MediaTUMXMLDocument xmlDocument = new MediaTUMXMLDocument(typeMap, languageMap, typeResolveMap);
		
		for (String abstract_ : abstracts) {
			xmlDocument.addAbstract(abstract_.split(Pattern.quote("|"))[1], abstract_.split(Pattern.quote("|"))[0]);
		}
		xmlDocument.setId(idOriginal);
		xmlDocument.setTitle(title);
		xmlDocument.setFulltitle(fulltitle);
		xmlDocument.setLanguage(language);
		xmlDocument.setYear(year);
		for (String author : authors) {
			author = author.split(Pattern.quote("("))[0].trim();
			xmlDocument.addAuthor(author);
		}
		for (String keyWord : keyWords) {
			xmlDocument.addKeyword(keyWord);
		}
		xmlDocument.addType(type);
		xmlDocument.setPublishedIn(publishedIn, "publisher");
		xmlDocument.setCollectionId(collection);
		xmlDocument.setLicense(license);
		xmlDocument.setFullText(fullText);
		
		xmlDocument.normalize();
		
		return xmlDocument;
	}
	
	/**
	 * Extracts the authors from a given attribute value retrieved from mediaTUM's XML.
	 * Authors may either be comma- or semicolon-separated.
	 * 
	 * @param attributeValue value of attribute to extract authors from
	 * @return extracted authors
	 */
	private String[] getAuthorsFromAttributeValue(String attributeValue) {
		String attributeValueWithOutBrackets = attributeValue.replaceAll("\\(.*\\)", "");
		
		// comment in for debugging
//		System.out.println("ATTRIBUTE VALUE: " + attributeValue);
		
		// distinguish cases
		if (attributeValueWithOutBrackets.contains(";")) {
			// semicolon-separated, multiple
			if (!attributeValueWithOutBrackets.contains(", ")) {
				attributeValue = attributeValue.replace(",", ", ");
			}
		} else if (StringUtils.countMatches(attributeValueWithOutBrackets, ",") > 1) {
			// comma-separated, multiple
			if (attributeValueWithOutBrackets.contains("., ")) {
				// comma-separated first and last names, as well as authors
				attributeValue = attributeValue.replace("., ", ".;");
				if (!attributeValueWithOutBrackets.contains(",")) {
					// first and last name separated with space
					attributeValue = attributeValue.replace(" ", ", ");
				}
			} else {
				attributeValue = attributeValue.replace(", ", ";");
				attributeValue = attributeValue.replace(" ", ", ");
			}
		} else if (!attributeValueWithOutBrackets.contains(",")) {
			// comma-separated, single
			attributeValue = attributeValue.replace(" ", ", ");
		} else {
			// semicolon-separated, single
		}
		
		String[] authors = attributeValue.split(";");
		
		// comment in for debugging
//		for (String author : authors) {
//			System.out.println("AUTHOR: " + author);
//		}
		
		return authors;
	}
	
	/**
	 * Reads in a given file and converts it to an OAIDC record.
	 * 
	 * @param pathOfFile file to read in
	 * @return OAIDC record
	 */
	private OAIDCRecord readOAIDCRecordFromFile(String pathOfFile) {
		OAIDCRecord oaidcRecord = new OAIDCRecord();
		
		File file = new File(pathOfFile);
		
		try {
            Scanner scanner = new Scanner(file);
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // attribute found
                if (line.contains("<dc:")) {
                	String attributeName = line.split("<dc:")[1].split(">")[0].split(" ")[0];
                	
                	// take multi lines into account
                	int i = 0;
                	while (!line.contains("</dc")) {
                		if (scanner.hasNextLine()) {
                			line += " " + scanner.nextLine();
                		}
                		
                		// for safety
                		i++;
                		if (i > 100) {
                			break;
                		}
                	}
                	
                	String attributeValue = line.split(">")[1].split("</dc")[0];
                	
                	if (i > 0) {
                		attributeValue = line.substring(line.indexOf(">")).split("</dc")[0];
                	}
                	
                	// remove tags
                	attributeValue = attributeValue.replaceAll("<br>", " ");
                	attributeValue = attributeValue.replaceAll(Pattern.quote("<i>"), "");
                	attributeValue = attributeValue.replaceAll(Pattern.quote("</i>"), "");
                	attributeValue = attributeValue.replaceAll(Pattern.quote("<sup>"), "");
                	attributeValue = attributeValue.replaceAll(Pattern.quote("</sup>"), "");
                	if (attributeValue.contains("<![CDATA[]]")) {
                		attributeValue = "";
                	}
                	if (attributeValue.contains("<![CDATA[")) {
                		attributeValue = attributeValue.split(Pattern.quote("![CDATA["))[1].split(Pattern.quote("]]"))[0];
                	}
                	
                	if (!attributeValue.equals("")) {                		
                		switch (attributeName) {
						case "title":
							oaidcRecord.addTitle(attributeValue);
							break;
						case "creator":
							String[] creators = getAuthorsFromAttributeValue(attributeValue);
							
							for (String creator : creators) {
								oaidcRecord.addCreator(creator);
							}
							break;
						case "subject":
							String subjectLanguage = "";
							
							// this is the case for abstracts
							if (line.contains("xml:lang")) {
								subjectLanguage = line.split(Pattern.quote("<dc:subject xml:lang="))[1].split(">")[0].replaceAll("\"", "");
							
								if (subjectLanguage.equals(getPublicationLanguage(pathOfFile))) {
									for (String subject : attributeValue.split(", ")) {
										oaidcRecord.addSubject(subject);
									}
								}
							// this is the case for collections
							} else {
								oaidcRecord.addSubject(attributeValue);
							}
							
							break;
						case "description":
							// default language
							String descriptionLanguage = "unknown";
							
							if (line.contains("xml:lang")) {
								 descriptionLanguage = line.split(Pattern.quote("<dc:description xml:lang="))[1].split(">")[0].replaceAll("\"", "");
							}
							
							oaidcRecord.addDescription(descriptionLanguage + "|" + attributeValue);
							
							break;
						case "publisher":
							oaidcRecord.addPublisher(attributeValue);
							break;
						case "contributor":
							String[] contributors = getAuthorsFromAttributeValue(attributeValue);
							
							for (String contributor : contributors) {
								oaidcRecord.addCreator(contributor);
							}
							break;
						case "date":
							oaidcRecord.addDate(attributeValue);
							break;
						case "type":
							oaidcRecord.addType(attributeValue);
							break;
						case "format":
							oaidcRecord.addFormat(attributeValue);
							break;
						case "identifier":
							if (oaidcRecord.getIdentifiers().size() == 0) {
								oaidcRecord.addIdentifier(attributeValue.split("id=")[1]);
							}
							break;
						case "source":
							oaidcRecord.addSource(attributeValue);
							break;
						case "language":
							oaidcRecord.addLanguage(attributeValue);
							break;
						case "relation":
							oaidcRecord.addRelation(attributeValue);
							break;
						case "coverage":
							oaidcRecord.addCoverage(attributeValue);
							break;
						case "rights":
							oaidcRecord.addRight(attributeValue);
							break;

						default:
							break;
						}
                	}
                }
            }
            
            scanner.close();
            
            return oaidcRecord;
        } catch (FileNotFoundException e) {
            return null;
        }
	}
	
	/**
	 * Returns the value of the language tag of a given XML file holding an OAI DC record.
	 * 
	 * @param pathOfFile path of file to get language of
	 * @return language of given OAI DC record
	 */
	private String getPublicationLanguage(String pathOfFile) {
		// unknown is the default language
		String language = "unknown";
		
		File file = new File(pathOfFile);
		
		try {
            Scanner scanner = new Scanner(file);
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // attribute found
                if (line.contains("<dc:language>")) {
                	language = line.split(">")[1].split("</dc")[0];
                }
            }
            
            scanner.close();
		} catch (FileNotFoundException e) {
            return null;
        }
		
		return language;
	}
	
	/**
	 * Extracts given abstracts (multiple abstracts because of abstracts given in multiple languages)
	 * from a given OAIDC record.
	 * 
	 * @param oaidcRecord OAIDC record to extract abstracts from
	 * @return list of extracted abstracts
	 */
	private ArrayList<String> getAbstractsFromOAIDCRecord(OAIDCRecord oaidcRecord) {
		ArrayList<String> abstracts = new ArrayList<String>();
		
		if (oaidcRecord.getDescriptions().size() > 0) {
			abstracts = oaidcRecord.getDescriptions();
		} else {
			// a common case
		}
		
		for (int i = 0; i < abstracts.size(); i++) {
			abstracts.set(i, abstracts.get(i));
		}
		
		return abstracts;
	}
	
	/**
	 * Extracts the language of a publication from a given OAIDC record.
	 * 
	 * @param oaidcRecord OAIDC record to extract language from.
	 * @return extracted language, that may need conversion to the internal language coding format of Mr. DLib<br>
	 * for that purpose the language map exists, that may be set up using createTypeMap()
	 */
	private String getLanguageFromOAIDCRecord(OAIDCRecord oaidcRecord) {
		String language = "";
		
		if (oaidcRecord.getLanguages().size() > 0) {
			language = oaidcRecord.getLanguages().get(0);
		} else {
			// if no language is indicated, the language is unknown
			language = "unknown";
		}
		
		return language;
	}
	
	/**
	 * Extracts the "original" id used at mediaTUM and prefixes it with "mt". mediaTUM uses numbers,
	 * primarily in the 7-figures range.
	 * 
	 * @param oaidcRecord OAIDC record to get the "original" id from
	 * @return prefixed "original" id of OAIDC record from mediaTUM
	 */
	private String getIdOriginalFromOAIDCRecord(OAIDCRecord oaidcRecord) {
		String idOrignal = "";
		
		if (oaidcRecord.getIdentifiers().size() > 0) {
			idOrignal = oaidcRecord.getIdentifiers().get(0);
		} else {
			System.out.println("Error: no identifier found.");
			return null;
		}
		
		return idOrignal;
	}
	
	/**
	 * Extracts a publication's title from a given OAIDC record.
	 * 
	 * @param oaidcRecord OAIDC record to extract title from
	 * @return publication's title
	 */
	private String getTitleFromOAICDRecord(OAIDCRecord oaidcRecord) {
		String title = "";
		
		if (oaidcRecord.getTitles().size() > 0) {
			title = oaidcRecord.getTitles().get(0);
		} else {
			System.out.println("Error: no title found.");
			return null;
		}
		
		return title;
	}
	
	/**
	 * Extracts keywords from an OAIDC record. Values provided through the OAI tags "creator" and "contributor" are used.
	 * 
	 * @param oaidcRecord OAIDC record to extract authors from
	 * @return list of extracted authors
	 */
	private ArrayList<String> getAuthorsFromOAIDCRecord(OAIDCRecord oaidcRecord) {
		ArrayList<String> authors = new ArrayList<String>();
		
		authors = oaidcRecord.getCreators();
		authors.addAll(oaidcRecord.getContributors());
		
		for (int i = 0; i < authors.size(); i++) {
			authors.set(i, authors.get(i));
		}
		
		return authors;
	}
	
	/**
	 * Extracts keywords from an OAIDC record. Values provided through the OAI tag "subject" are used.
	 * This approach may be specific to mediaTUM.
	 * 
	 * @param oaidcRecord OAIDC record to extract keywords from
	 * @return list of extracted keywords
	 */
	private ArrayList<String> getKeyWordsFromOAIDCRecord(OAIDCRecord oaidcRecord) {
		ArrayList<String> keyWords = new ArrayList<>();
		
		for (String subject : oaidcRecord.getSubjects()) {
			if (subject.length() >= 4) {
				if (!subject.substring(0, 4).equals("ddc:")) {
					for (String keyWord : subject.split(";")) {
						// prevent encoded collection information to be stored as a keyword
						if (!keyWord.contains("ddc:")) {
							keyWords.add(keyWord);
						}
					}
				}
			}
		}
		
		return keyWords;
	}
	
	/**
	 * Extracts a "type" of a publication from an OAIDC record.
	 * A "publication type" may for example be a doctoral thesis or an article in a journal.
	 * The available publication types are very specific to the content provider mediaTUM.
	 * 
	 * @param oaidcRecord OAIDC record to extract a "publication type" from
	 * @return extracted "publication type"
	 */
	private String getTypeFromOAIDCRecord(OAIDCRecord oaidcRecord) {
		String type = "";
		
		if (oaidcRecord.getTypes().size() > 0) {
			switch (oaidcRecord.getTypes().get(oaidcRecord.getTypes().size() - 1)) {
			case "doc-type:report":
				type = "report";
				break;
			case "thesis":
				type = "thesis_unspecified";
				break;
			case "report":
				type = "report";
				break;
			case "dissertation":
				type = "thesis_doctoral";
				break;
			case "doc-type:masterThesis":
				type = "thesis_master";
				break;
			case "doc-type:doctoralThesis":
				type = "thesis_doctoral";
				break;
			case "article":
				type = "article_unknown";
				break;
			case "doc-type:bachelorThesis":
				type = "thesis_bachelor";
				break;
			default:
				type = "unknown";
				break;
			}
		} else {
			System.out.println("Error: no type found.");
			return null;
		}
		
		return type;
	}
	
	/**
	 * Extracts a publisher from a given OAIDC record.
	 * 
	 * @param oaidcRecord OAIDC record to extract publisher from
	 * @return extracted publisher
	 */
	private String getPublishedInFromOAIDCRecord(OAIDCRecord oaidcRecord) {
		String publishedIn = "";
		
		if (oaidcRecord.getPublishers().size() > 0) {
			publishedIn = oaidcRecord.getPublishers().get(0);
		} else {
			// might be the case
		}
		
		return publishedIn;
	}
	
	/**
	 * Extracts a "collection" from a given OAIDC record.
	 * "collections" are used by content provider mediaTUM to group publications. The groups are effectively
	 * fields of research (e.g. Mathematics) that are encoded using a three-digit code prefixed with "ddc:". 
	 * 
	 * @param oaidcRecord OAIDC record to extract "collection" from
	 * @return extracted "collection" used by mediaTUM to group publications
	 */
	private String getCollectionFromOAIDCRecord(OAIDCRecord oaidcRecord) {
		// default value
		String collection = "000";
		
		for (String subject : oaidcRecord.getSubjects()) {
			if (subject.contains("ddc:")) {
				collection = subject.split("ddc:")[1];
			}
		}
		
		return "mediatum-ddc" + collection;
	}
	
	/**
	 * Extracts an indication of the rights related to the given OAIDC record.
	 * It may either be "open_access", "licence" or an empty string
	 * 
	 * @param oaidcRecord OAIDC record to extract rights indication from
	 * @return extracted rights indication from corresponding enum
	 */
	private String getLicenseFromOAIDCRecord(OAIDCRecord oaidcRecord) {
		// default for unknown license
		String license = "NULL";
		
		if (oaidcRecord.getRights().size() > 0) {
			String rights = oaidcRecord.getRights().get(0);
			
			switch (rights) {
			case ("info:eu-repo/semantics/openAccess"):
				license = "open_access";
				break;
			default:
				license = "restricted";
				break;
			}
		} else {
			// possible case
		}
		
		return license;
	}
	
	/**
	 * Extracts an indication whether full text of a given OAIDC record is provided.
	 * And, if so, in which format.
	 * 
	 * @param oaidcRecord OAIDC record to get full text indication of
	 * @return full text indication providing information about full text type - value of the corresponding enum
	 */
	private String getFullTextFromOAIDCRecord(OAIDCRecord oaidcRecord) {
		String fullText = "no";
		
		if (oaidcRecord.getFormats().size() > 0) {
			String format = oaidcRecord.getFormats().get(0);
			
			switch (format) {
			case "application/pdf":
				fullText = "pdf";
				break;
			default:
				fullText = "no";
				break;
			}
		} else {
			// possible case
		}
		
		return fullText;
	}
	
}
