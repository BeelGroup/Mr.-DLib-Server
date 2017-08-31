package org.mrdlib.partnerContentManager.gesis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;
import org.mrdlib.partnerContentManager.general.Document;

/**
 * 
 * 
 * @author Millah
 *
 * stores and preprocesses all necessary information of an (academic) document
 *
 */
public class XMLDocument extends Document {
	protected String fulltitle;
	protected String cleantitle;
	protected int facetYear;
	protected Set<String> typeSet = new HashSet<String>();
	protected int publishedInRank;
	protected Map<String, String> typeMap = new HashMap<String, String>();
	protected Map<String, String> languageMap = new HashMap<String, String>();
	protected Map<Tuple, String> typeResolveMap = new HashMap<Tuple, String>();

	/**
	 * maps a string to a specific (in a config file determined) enum
	 * 
	 * @param original string of the type
	 * @return enum type
	 */
	protected String chooseType(String type) {
		String tempType;
		//if empty its unknown
		if (type.equals(""))
			return "unknown";
		else {
			//get the corresponding enum type
			tempType = typeMap.get(type.toLowerCase());
			//if this is not null (-> defined), return the new type
			if (tempType != null && !tempType.equals(""))
				return tempType;
			else {
				//otherwise say its not handled yet but return unknown
				System.out.println("In Export " + documentPath + " In Document: " + id + ": Undefined Type: " + type);
				return "unknown";
			}
		}
	}

	/**
	 * since multiple types can be set, select on of these which seems to be most reasonable
	 */
	protected void selectType() {
		Iterator<String> it;
		String allTypes = "";
		String current;

		//if no type is set, its unknown
		if (typeSet.isEmpty())
			type = "unknown";
		else {
			it = typeSet.iterator();
			//for each occuring type
			while (it.hasNext()) {
				current = it.next();
				//save every occuring type for troubleshooting
				allTypes = allTypes + ", " + current.toString();
				//if until now there is no "good" type, set current type
				if (type == null || type.equals("unknown"))
					type = current.toString();
				//if there is another good or reasonable type, ask the resolveMap how to handle the conflict
				else if (!type.equals(current.toString())) {
					Tuple tupleKey = new Tuple(type, current.toString());
					//if the resolve map has an answer, set it as solution
					if (typeResolveMap.containsKey(tupleKey))
						type = typeResolveMap.get(tupleKey);
					//if not, say its not handled, but set unknown
					else {
						System.out.println(
								"In Export " + documentPath + " In Document: " + id + ": Multiple Types: " + allTypes);
						type = "unknown";
					}
				}
			}
		}
	}


	/**
	 * call the normalizing functions in the necessary order
	 */
	public void normalize() {
		super.normalize();
		selectTitle();
		language = setLanguageToStandard(language);
		selectYear();
		selectType();
		normalizePublishedIn();
	}
	
	/**
	 * normalize publishedIn by cutting of "; ..."
	 */
	protected void normalizePublishedIn() {
		if(publishedIn != null)
			if (publishedIn.endsWith(" ; ..."))
				publishedIn = publishedIn.replace(" ; ...", "");
	}

	
	/**
	 * select between two years a reasonable year, which is between 1500 and current+2
	 * year has priority over facetYear if both are reasonable
	 * if both are weird, say it and save it as 0, which will later be interpreted as NULL
	 */
	protected void selectYear() {
		int tempYear = 0;
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		//if year is not reasonable (outside of 1500 until current+2
		if (year < 1500 || year > currentYear + 2) {
			//if facetyear also is weird
			if (facetYear < 1500 || facetYear > currentYear + 2) {
				if (facetYear == 0 && year == 0) {
				} else {
					//say it and save as 0
					System.out.println("In Export " + documentPath + " In Document: " + id + "Weird Years: " + year
							+ ", " + facetYear);
					tempYear = 0;
				}
			//take facetYear if year is not reasonable but facetYear is
			} else
				tempYear = facetYear;
		//take year if year is reasonable
		} else
			tempYear = year;
		year = tempYear;
	}
	/**
	 * select a title from title and fulltitle (with priority on fulltitle)
	 */
	protected void selectTitle() {
		//if fulltitle is null, stay with title
		if (fulltitle == null) {}
		//if fulltitle is not null but and contains more information thatn title, set fulltitle
		else if (title.equals(fulltitle) || title == null || fulltitle.length() > title.length())
			title = fulltitle;
		//if fulltitle is not equals title and not as big, say it and stay with title
		else if (!title.equals(fulltitle)) {
			System.out.println("In Export " + documentPath + " In Document: " + id);
			System.out.println("Different titles!");
			System.out.println("Title: " + title);
			System.out.println("Fulltitle: " + fulltitle);
			System.out.println("------------------------");
		}
	}


	/**
	 * maps a language string to a 2 char enum (from ISO standard)
	 * 
	 * @param language as String
	 * @return language as 2 char enum
	 */
	protected String setLanguageToStandard(String language) {
		boolean validLanguage = true;
		String tempLan;
		//if the language is not undefined
		if (!(language == null || language.equals("Keine Angabe") || language.equals("Multilingual"))) {
			//get the ISO languages
			String[] languages = Locale.getISOLanguages();
			validLanguage = false;

			//look up the language in the language Map (which has its information from a xml config file)
			tempLan = languageMap.get(language.toLowerCase());
			
			if (!(tempLan == null))
				language = tempLan;

			//if it contains bracktes, cut them of
			if (language.contains("(") && language.contains(")")) {
				String substring = language.substring(language.indexOf("(") + 1, language.indexOf(")")).toLowerCase();
				//if it has the right length, compare it to the ISO languages
				if (substring.length() == 2)
					for (int i = 0; i < languages.length; i++) {
						if (languages[i].equals(substring)) {
							language = substring;
							validLanguage = true;
						}
					}
			}
		} else
			language = null;
		if (!validLanguage) {
			//if its not a valid Language print out the wrong language but write null
			System.out.println("In Export " + documentPath + " In Document: " + id + ": Language needs to be defined: "
					+ language);
			language = null;
		}

		return language;
	}

	/**
	 * TO BE IMPLEMENTED FOR NEW COLLECTIONS
	 * @return gesis
	 */
	@Override
	public String getCollectionId() {
		// File file = new File(documentPath.toString());
		// String dir =
		// file.getParent().substring(file.getParent().lastIndexOf(File.separator)
		// + 1);
		return "gesis";
	}

	public int getFacetYear() {
		return facetYear;
	}

	public void setFacetYear(String facetYear) {
		this.facetYear = makeYearInt(facetYear);
	}

	public void addType(String type) {
		if (!chooseType(type).equals("unknown"))
			this.typeSet.add(chooseType(type));
	}

	/**
	 * 
	 * Constructor, which initialize the needed Maps for mapping and resolving types and language
	 * Set some default values
	 * 
	 * @param typeMap
	 * @param languageMap
	 * @param typeResolveMap
	 */
	public XMLDocument(Map<String, String> typeMap, Map<String, String> languageMap,
			Map<Tuple, String> typeResolveMap) {
		this.typeMap = typeMap;
		this.languageMap = languageMap;
		this.typeResolveMap = typeResolveMap;
		type = "unknown";
		publishedInRank = 10;
	}

	public String getFulltitle() {
		return fulltitle;
	}

	public void setFulltitle(String fulltitle) {
		this.fulltitle = fulltitle;
	}


	/**
	 * Set publishedIn in a priority manner
	 * journal_full_txt_mv > journal_title_txt_mv > journal_short_txt_mv > Satit_str > Sseries_str_mv > publisher
	 * 
	 * @param publishedIn the "journal" where the (academic) document is published in
	 * @param type the XML type where the "journal" was found
	 */
	public void setPublishedIn(String publishedIn, String type) {
		if (type.equals("journal_full_txt_mv")) {
			this.publishedIn = publishedIn;
			this.publishedInRank = 1;
		} else if (type.equals("journal_title_txt_mv") && publishedInRank > 1) {
			this.publishedIn = publishedIn;
			this.publishedInRank = 2;
		} else if (type.equals("journal_short_txt_mv") && publishedInRank > 2) {
			this.publishedIn = publishedIn;
			this.publishedInRank = 3;
		} else if (type.equals("Satit_str") && publishedInRank > 3) {
			this.publishedIn = publishedIn;
			this.publishedInRank = 4;
		} else if (type.equals("Sseries_str_mv") && publishedInRank > 4) {
			this.publishedIn = publishedIn;
			this.publishedInRank = 5;
		} else if (type.equals("publisher") && publishedInRank > 5) {
			this.publishedIn = publishedIn;
			this.publishedInRank = 6;
		}
	}

	/**
	 * set an abstract to a document with a valided ISO language
	 * @param abstract
	 * @param language
	 */
	public void addAbstract(String abstr, String lan) {
		lan = setLanguageToStandard("(" + lan + ")");
		this.abstr.add(new Abstract(abstr, lan));
	}
}
