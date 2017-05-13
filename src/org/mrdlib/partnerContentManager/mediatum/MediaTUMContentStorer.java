package org.mrdlib.partnerContentManager.mediatum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mrdlib.database.DBConnection;
import org.mrdlib.partnerContentManager.IContentStorer;
import org.mrdlib.partnerContentManager.gesis.Tuple;
import org.mrdlib.partnerContentManager.gesis.XMLDocument;

/**
 * Implementation of ContentStorer for partner mediaTUM.
 * 
 * @author wuestehube
 *
 */
public class MediaTUMContentStorer implements IContentStorer<OAIDCRecordConverted> {

	/**
	 * Creates the mapping of type codes used in mediaTUM and Mr. DLib. Types are the publication types.
	 * 
	 * @return a type map that can be used in processing XML documents
	 */
	private Map<String, String> createTypeMap() {
		Map<String, String> typeMap = new HashMap<String, String>();
		
		typeMap.put("thesis_unspecified", "thesis_unspecified");
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
		
		languageMap.put("eng", "(en)");
		languageMap.put("de", "(de)");
		languageMap.put("ger", "(de)");
		languageMap.put("(ger)", "(de)");
		languageMap.put("spa", "(es)");
		languageMap.put("fra", "(fr)");
		languageMap.put("zho", "(zh)");
		languageMap.put("jpn", "(ja)");
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
	
	@Override
	public void store(DBConnection dbConnection, OAIDCRecordConverted storableContent) {
		
		System.out.println("LET'S STORE!");
		
		Map<String, String> typeMap = createTypeMap();
		Map<String, String> languageMap = createLanguageMap();
		Map<Tuple, String> typeResolveMap = createTypeResolveMap();
		
		XMLDocument xmlDocument = new XMLDocument(typeMap, languageMap, typeResolveMap);
		
		// TODO: set up XML document
		xmlDocument.addAbstract(storableContent.getMdlDocumentAbstract().getAbstract_(), storableContent.getMdlDocument().getLanguage());
		xmlDocument.setId(storableContent.getMdlDocument().getId_original());
		
		xmlDocument.setTitle(storableContent.getMdlDocument().getTitle());
		xmlDocument.setFulltitle(storableContent.getMdlDocument().getTitle());
		
		xmlDocument.setLanguage(storableContent.getMdlDocument().getLanguage());
		xmlDocument.setYear(Integer.toString(storableContent.getMdlDocument().getAdded().getYear()));
		xmlDocument.setFacetYear(Integer.toString(storableContent.getMdlDocument().getAdded().getYear()));
		
		// TODO: get author
		xmlDocument.addAuthor("temp");
		
		// TODO: get keywords
		xmlDocument.addKeyWord("temp");
		
		// TODO: cast from Enum to String or cut out ContentConverter completely
		xmlDocument.addType("unknown");
		
		// TODO: set publisher correctly
		xmlDocument.setPublishedIn("temp", "unknown");
		
		xmlDocument.normalize();
		
		try {
			dbConnection.insertDocument(xmlDocument);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
