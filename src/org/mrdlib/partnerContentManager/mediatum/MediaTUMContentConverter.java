package org.mrdlib.partnerContentManager.mediatum;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.mrdlib.partnerContentManager.IContentConverter;
import org.mrdlib.partnerContentManager.MdlDocument;
import org.mrdlib.partnerContentManager.MdlDocumentAbstract;
import org.mrdlib.partnerContentManager.MdlDocumentExternalId;
import org.mrdlib.partnerContentManager.MdlDocumentExternalIdExternalName;
import org.mrdlib.partnerContentManager.MdlDocumentKeyphrase;
import org.mrdlib.partnerContentManager.MdlDocumentKeyphraseCount;
import org.mrdlib.partnerContentManager.MdlDocumentKeyphraseSource;
import org.mrdlib.partnerContentManager.MdlDocumentPerson;
import org.mrdlib.partnerContentManager.MdlDocumentTitleSearches;
import org.mrdlib.partnerContentManager.MdlDocumentTranslatedField;
import org.mrdlib.partnerContentManager.MdlDocumentTranslatedFieldFieldType;
import org.mrdlib.partnerContentManager.MdlDocumentTranslatedFieldTranslationTool;
import org.mrdlib.partnerContentManager.MdlDocumentType;
import org.mrdlib.partnerContentManager.MdlPerson;
import org.mrdlib.partnerContentManager.MdlPersonDataQuality;

/**
 * Implementation of ContentConverter for partner mediaTUM.
 * mediaTUM offers a standardized OAI interface exhibiting data in the OAI Dublin Core format (http://www.openarchives.org/OAI/openarchivesprotocol.html).
 * 
 * @author wuestehube
 *
 */
public class MediaTUMContentConverter implements IContentConverter<OAIDCRecordConverted> {

	@Override
	public OAIDCRecordConverted convertPartnerContentToStorablePartnerContent(String pathOfFileToConvert) {
		OAIDCRecord oaidcRecord = readOAIDCRecordFromFile(pathOfFileToConvert);
		
		MdlDocument mdlDocument = mapMediaTumContentToMdlDocumentTable(oaidcRecord);
		MdlDocumentAbstract mdlDocumentAbstract = mapMediaTumContentToMdlDocumentAbstractTable(oaidcRecord);
		MdlDocumentExternalId mdlDocumentExternalId = mapMediaTumContentToMdlDocumentExternalIdTable(oaidcRecord);
		MdlDocumentKeyphrase mdlDocumentKeyphrase = mapMediaTumContentToMdlDocumentKeyphraseTable(oaidcRecord);
		MdlDocumentKeyphraseCount mdlDocumentKeyphraseCount = mapMediaTumContentToMdlDocumentKeyphraseCountTable(oaidcRecord);
		MdlDocumentPerson mdlDocumentPerson = mapMediaTumContentToMdlDocumentPersonTable(oaidcRecord);
		MdlDocumentTitleSearches mdlDocumentTitleSearches = mapMediaTumContentToMdlDocumentTitleSearchesTable(oaidcRecord);
		MdlDocumentTranslatedField mdlDocumentTranslatedField = mapMediaTumContentToDocumentTranslatedFieldTable(oaidcRecord);
		List<MdlPerson> mdlPerson = mapMediaTumContentToMdlPersonTable(oaidcRecord);
		
		OAIDCRecordConverted oaidcRecordConverted = new OAIDCRecordConverted(mdlDocument, mdlDocumentAbstract, mdlDocumentExternalId,
				mdlDocumentKeyphrase, mdlDocumentKeyphraseCount, mdlDocumentPerson, mdlDocumentTitleSearches, mdlDocumentTranslatedField,
				mdlPerson);

		return oaidcRecordConverted;
	}
	
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
                	String attributeValue = line.split(">")[1].split("</")[0];
                	if (attributeValue.contains("<![CDATA[]]")) {
                		attributeValue = "";
                	}
                	if (attributeValue.contains("<![CDATA[")) {
                		attributeValue = attributeValue.split(Pattern.quote("![CDATA["))[1].split(Pattern.quote("]]"))[0];
                	}
                	
                	if (!attributeValue.equals("")) {
                		System.out.println(attributeName + " = " + attributeValue);
                		
                		switch (attributeName) {
						case "title":
							oaidcRecord.addTitle(attributeValue);
							break;
						case "creator":
							oaidcRecord.addCreator(attributeValue);
							break;
						case "subject":
							for (String subject : attributeValue.split(", ")) {
								oaidcRecord.addSubject(subject);
							}
							break;
						case "description":
							oaidcRecord.addDescription(attributeValue);
							break;
						case "publisher":
							oaidcRecord.addPublisher(attributeValue);
							break;
						case "contributor":
							oaidcRecord.addContributor(attributeValue);
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
						case "right":
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
	 * Converts one OAI DC record to MDL's document table. Checks provided data for plausibility.
	 * 
	 * @param oaidcRecord OAI DC record to convert to MDL's document table
	 * @return null if data is implausible, otherwise converted OAI DC record
	 */
	private MdlDocument mapMediaTumContentToMdlDocumentTable(OAIDCRecord oaidcRecord) {
		// check data for plausibility
		
		// title
		if (oaidcRecord.getTitles().size() != 1) {
			System.out.println("title != 1");
			return null;
		}
		// language
		if (oaidcRecord.getLanguages().size() != 1) {
			System.out.println("language != 1");
			return null;
		}
		// year
		if (oaidcRecord.getDates().size() != 1) {
			System.out.println("date != 1");
			return null;
		}
		
		// map data
		
		// no mapping
		long document_id = 0;
		// get all elements of oaidcRecord.getIdentifiers() as concatenated string, prefix with "mt"
		String id_original = "mt" + stringArrayListToString(oaidcRecord.getIdentifiers(), null);
		long collection_id = 0;
		String title = oaidcRecord.getTitles().get(0);
		String title_clean = getCleanTitleFromTitle(title);
		String published_in = oaidcRecord.getPublishers().get(0);
		String language = oaidcRecord.getLanguages().get(0);
		int publication_year = getPublicationYearFromOAIDCDateFormat(oaidcRecord.getDates().get(0));
		// choose type to convert
		int type_index = 0;
		if (oaidcRecord.getTypes().size() > 1) {
			int i = 0;
			for (String type : oaidcRecord.getTypes()) {
				if (type.contains("doc-type:")) {
					type_index = i;
				}
				i++;
			}
		}
		MdlDocumentType type = getMdlDocumentTypeFromOAIDCType(oaidcRecord.getTypes().get(type_index));
		String keywords = stringArrayListToString(oaidcRecord.getSubjects(), ", ");
		// use current system date
		Date added = new Date();
		
		// return data
		
		MdlDocument mdlDocument = new MdlDocument(document_id, id_original, collection_id, title, title_clean, published_in, language, publication_year, type, keywords, added);
		
		return mdlDocument;
	}
	
	/**
	 * Concatenates the elements of a string array list.
	 * 
	 * @param arrayList array list whose elements to concatenate
	 * @param separator null if strings should not be separated, otherwise printed between strings of array
	 * @return
	 */
	private String stringArrayListToString(ArrayList<String> arrayList, String separator) {
		StringBuilder builder = new StringBuilder();
		
		// for keeping track of current index
		int size = arrayList.size();
		int i = 0;
		
		for (String currentString : arrayList) {
		    builder.append(currentString);
		    if (separator != null && (i+1) < size) {
		    	builder.append(separator);
		    }
		    
		    i++;
		}
		
		return builder.toString();
	}
	
	/**
	 * Cleans a title according to the description of a clean title in the MDL database.
	 * This reads: "Clean title, i.e. only ASCII characters (no spaces, all lower case); if length of clean title is smaller than half the original title, use original title"
	 * 
	 * @param title Title to clean
	 * @return cleaned title
	 */
	private String getCleanTitleFromTitle(String title) {
		// convert to ASCII
		String result = Charset.forName("US-ASCII").encode(title).toString();
		
		// convert to lower case
		result = result.toLowerCase();
		
		// remove spaces
		result = result.replace(" ", "");
		
		// check whether clean title is shorter than half of the original title or not, return result accordingly
		if (result.length() < (title.length() / 2)) {
			return title;
		} else {
			return result;
		}
	}
	
	/**
	 * Extracts the year from a OAI DC date format (yyyy-mm-dd).
	 * 
	 * @param oaiDate date in OAI DC format (yyyy-mm-dd) to extract year from
	 * @return extracted year
	 */
	private int getPublicationYearFromOAIDCDateFormat(String oaiDate) {
		return Integer.parseInt(oaiDate.substring(0, 3));
	}
	
	/**
	 * Maps a mediaTUM OAI DC publication type to the publication types of MDL.
	 * 
	 * @param oaiType mediaTUM publication type to map
	 * @return mapped MDL publication type
	 */
	private MdlDocumentType getMdlDocumentTypeFromOAIDCType(String oaiType) {
		// TODO: improve type mapping
		
		switch (oaiType) {
		case "doc-type:report":
			return MdlDocumentType.UNKNOWN;
		case "thesis":
			return MdlDocumentType.THESIS_UNSPECIFIED;
		case "report":
			return MdlDocumentType.UNKNOWN;
		case "dissertation":
			return MdlDocumentType.UNKNOWN;
		case "doc-type:masterThesis":
			return MdlDocumentType.THESIS_UNSPECIFIED;
		case "doc-type:doctoralThesis":
			return MdlDocumentType.THESIS_UNSPECIFIED;
		case "article":
			return MdlDocumentType.UNKNOWN;
		case "doc-type:bachelorThesis":
			return MdlDocumentType.THESIS_UNSPECIFIED;
		default:
			return MdlDocumentType.UNKNOWN;
		}
	}
	
	/**
	 * Converts one OAI DC record to MDL's document abstract table. Checks provided data for plausibility.
	 * 
	 * @param oaidcRecord OAI DC record to convert to MDL's document table
	 * @return null if data is implausible, otherwise converted OAI DC record
	 */
	private MdlDocumentAbstract mapMediaTumContentToMdlDocumentAbstractTable(OAIDCRecord oaidcRecord) {
		// check data for plausibility
		
		// language
		if (oaidcRecord.getLanguages().size() != 1) {
			return null;
		}
		// description
		if (oaidcRecord.getDescriptions().size() != 1) {
			return null;
		}

		// map data
		
		// no mapping
		long document_abstract_id = 0;
		// no mapping
		long document_id = 0;
		String language = getMdlLanguageCodeFromOAIDCCode(oaidcRecord.getLanguages().get(0));
		String abstract_ = oaidcRecord.getDescriptions().get(0);
		Date added = new Date();
		
		// return data
		
		MdlDocumentAbstract mdlDocumentAbstract = new MdlDocumentAbstract(document_abstract_id, document_id, language, abstract_, added);
		
		return mdlDocumentAbstract;
	}

	/**
	 * Maps a mediaTUM OAI DC language code to the language code of MDL.
	 * 
	 * @param oaiLanguageCode language code to map
	 * @return mapped language code
	 */
	private String getMdlLanguageCodeFromOAIDCCode(String oaiLanguageCode) {
		// MDL language codes: cs, da, de, en, es, et, fr, it, lv, nl, pl, pt, ru, sv
		
		switch (oaiLanguageCode) {
		case "eng":
			return "en";
		case "deu":
			return "de";
		case "ger":
			return "de";
		case "spa":
			return "es";
		case "fra":
			return "fr";
		case "zho":
			return "zh";
		case "jpn":
			return "ja";
		case "rus":
			return "ru";
		default:
			return null;
		}
	}
	
	/**
	 * Converts one OAI DC record to MDL's document external id table. Checks provided data for plausibility.
	 * 
	 * @param oaidcRecord OAI DC record to convert to MDL's document external id table
	 * @return null if data is implausible, otherwise converted OAI DC record
	 */
	private MdlDocumentExternalId mapMediaTumContentToMdlDocumentExternalIdTable(OAIDCRecord oaidcRecord) {
		// TODO: find out if and how to use this table
		
		long document_id = 0;
		MdlDocumentExternalIdExternalName external_name = MdlDocumentExternalIdExternalName.ARXIV;
		String external_id = "";
		
		MdlDocumentExternalId mdlDocumentExternalId = new MdlDocumentExternalId(document_id, external_name, external_id);
		
		return mdlDocumentExternalId;
	}
	
	/**
	 * Converts one OAI DC record to MDL's document keyphrase table. Checks provided data for plausibility.
	 * 
	 * @param oaidcRecord OAI DC record to convert to MDL's document keyphrase table
	 * @return null if data is implausible, otherwise converted OAI DC record
	 */
	private MdlDocumentKeyphrase mapMediaTumContentToMdlDocumentKeyphraseTable(OAIDCRecord oaidcRecord) {
		// no mapping
		
		long doc_id = 0;
		String term = "";
		float score = 0;
		int gramity = 0;
		MdlDocumentKeyphraseSource source = MdlDocumentKeyphraseSource.ABSTRACT;
		
		MdlDocumentKeyphrase mdlDocumentKeyphrase = new MdlDocumentKeyphrase(doc_id, term, score, gramity, source);
		
		return mdlDocumentKeyphrase;
	}
	
	/**
	 * Converts one OAI DC record to MDL's document keyphrase count table. Checks provided data for plausibility.
	 * 
	 * @param oaidcRecord OAI DC record to convert to MDL's document keyphrase count table
	 * @return null if data is implausible, otherwise converted OAI DC record
	 */
	private MdlDocumentKeyphraseCount mapMediaTumContentToMdlDocumentKeyphraseCountTable(OAIDCRecord oaidcRecord) {
		// no mapping
		
		long doc_id = 0;
		int gramity = 0;
		MdlDocumentKeyphraseSource source = MdlDocumentKeyphraseSource.ABSTRACT;
		long count = 0;
		
		MdlDocumentKeyphraseCount mdlDocumentKeyphraseCount = new MdlDocumentKeyphraseCount(doc_id, gramity, source, count);
		
		return mdlDocumentKeyphraseCount;
	}
	
	/**
	 * Converts one OAI DC record to MDL's document person table. Checks provided data for plausibility.
	 * 
	 * @param oaidcRecord OAI DC record to convert to MDL's document person table
	 * @return null if data is implausible, otherwise converted OAI DC record
	 */
	private MdlDocumentPerson mapMediaTumContentToMdlDocumentPersonTable(OAIDCRecord oaidcRecord) {
		// TODO: find out if this table contains entries for each person mapped to a record, then insert them
		
		long document_person_id = 0;
		long document_id = 0;
		long person_id = 0;
		int rank = 0;
		Date added = new Date();
		
		MdlDocumentPerson mdlDocumentPerson = new MdlDocumentPerson(document_person_id, document_id, person_id, rank, added);
		
		return mdlDocumentPerson;
	}
	
	/**
	 * Converts one OAI DC record to MDL's document title searches table. Checks provided data for plausibility.
	 * 
	 * @param oaidcRecord OAI DC record to convert to MDL's document title searches table
	 * @return null if data is implausible, otherwise converted OAI DC record
	 */
	private MdlDocumentTitleSearches mapMediaTumContentToMdlDocumentTitleSearchesTable(OAIDCRecord oaidcRecord) {
		// no mapping
		
		long document_title_search_id = 0;
		String clean_search_string = "";
		String original_search_string = "";

		MdlDocumentTitleSearches mdlDocumentTitleSearches = new MdlDocumentTitleSearches(document_title_search_id, clean_search_string, original_search_string);
		
		return mdlDocumentTitleSearches;
	}
	
	/**
	 * Converts one OAI DC record to MDL's document translated field table. Checks provided data for plausibility.
	 * 
	 * @param oaidcRecord OAI DC record to convert to MDL's document translated field table
	 * @return null if data is implausible, otherwise converted OAI DC record
	 */
	private MdlDocumentTranslatedField mapMediaTumContentToDocumentTranslatedFieldTable(OAIDCRecord oaidcRecord) {
		// check data for plausibility
		
		// language
		if (oaidcRecord.getLanguages().size() != 1) {
			return null;
		}

		// map data
		
		long document_id = 0;
		MdlDocumentTranslatedFieldFieldType field_type = MdlDocumentTranslatedFieldFieldType.ABSTRACT;
		MdlDocumentTranslatedFieldTranslationTool translation_tool = MdlDocumentTranslatedFieldTranslationTool.JOSHUA;
		String source_language = getMdlLanguageCodeFromOAIDCCode(oaidcRecord.getLanguages().get(0));
		String target_language = "";
		String text = "";
		
		MdlDocumentTranslatedField mdlDocumentTranslatedField = new MdlDocumentTranslatedField(document_id, field_type, translation_tool, source_language, target_language, text);
		
		// return data
		
		return mdlDocumentTranslatedField;
	}
	
	/**
	 * Converts one OAI DC record to MDL's person table. Checks provided data for plausibility.
	 * 
	 * @param oaidcRecord OAI DC record to convert to MDL's person table
	 * @return null if data is implausible, otherwise converted OAI DC record
	 */
	private List<MdlPerson> mapMediaTumContentToMdlPersonTable(OAIDCRecord oaidcRecord) {
		List<MdlPerson> persons = new ArrayList<MdlPerson>();
		
		// check data for plausibility
		
		// creator
		if (oaidcRecord.getCreators().size() != 1) {
			return null;
		}
		// contributors
		if (oaidcRecord.getContributors() == null) {
			return null;
		}

		// map data of creator
		persons.add(createMdlPersonFromOaiName(oaidcRecord.getCreators().get(0)));
		
		// map data of contributors
		for (String contributor : oaidcRecord.getContributors()) {
			persons.add(createMdlPersonFromOaiName(contributor));
		}
		
		// return data
		
		return persons;
	}
	
	/**
	 * Creates a MDL person object.
	 * 
	 * @param oaiName name of the person to create an object of
	 * @return MDL person object
	 */
	private MdlPerson createMdlPersonFromOaiName(String oaiName) {
		long person_id = 0;
		String name_first = getFirstNameFromOaiName(oaiName);
		String name_middle = getMiddleNameFromOaiName(oaiName);
		String name_last = getLastNameFromOaiName(oaiName);
		String name_unstructured = oaiName;
		Date added = new Date();
		MdlPersonDataQuality data_quality = MdlPersonDataQuality.INVALID;
		
		MdlPerson mdlPerson = new MdlPerson(person_id, name_first, name_middle, name_last, name_unstructured, added, data_quality);

		return mdlPerson;
	}

	/**
	 * Returns the first name of a given name. Assumes first, middle and last names are split with spaces.
	 * 
	 * @param oaiName name to get first name of
	 * @return first name of given name
	 */
	private String getFirstNameFromOaiName(String oaiName) {
		return oaiName.split(" ")[0];
	}
	
	/**
	 * Returns the middle name of a given name. Assumes first, middle and last names are split with spaces.
	 * 
	 * @param oaiName name to get middle name of
	 * @return middle name of given name
	 */
	private String getMiddleNameFromOaiName(String oaiName) {
		String[] stringParts = oaiName.split(" ");
		
		if (stringParts.length > 2) {
			String middleName = "";
			
			for (int i=1; i < stringParts.length-1; i++) {
				middleName += stringParts[i];
				
				if (i < stringParts.length-2) {
					middleName += " ";
				}
			}
			
			return middleName;
		} else {
			return "";
		}
	}
	
	/**
	 * Returns the last name of a given name. Assumes first, middle and last names are split with spaces.
	 * 
	 * @param oaiName name to get last name of
	 * @return last name of given name
	 */
	private String getLastNameFromOaiName(String oaiName) {
		String[] stringParts = oaiName.split(" ");
		return oaiName.split(" ")[stringParts.length - 1];
	}
	
}
