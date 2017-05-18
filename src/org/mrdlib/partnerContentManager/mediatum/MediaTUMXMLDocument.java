package org.mrdlib.partnerContentManager.mediatum;

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
import org.mrdlib.partnerContentManager.gesis.Abstract;
import org.mrdlib.partnerContentManager.gesis.Person;
import org.mrdlib.partnerContentManager.gesis.Tuple;
import org.mrdlib.partnerContentManager.gesis.XMLDocument;

/**
 * 
 * 
 * @author Millah
 *
 * stores and preprocesses all necessary information of an (academic) document
 *
 */
public class MediaTUMXMLDocument extends XMLDocument {
	private String documentPath;
	private String id;
	private String title;
	private String fulltitle;
	private String cleantitle;
	private String language;
	private ArrayList<Abstract> abstr = new ArrayList<Abstract>();
	private int year;
	private int facetYear;
	private Set<String> keywords = new HashSet<String>();
	private String type;
	private Set<String> typeSet = new HashSet<String>();
	private String publishedIn;
	private int publishedInRank;
	private String collection;
	
	private LinkedHashSet<Person> authors = new LinkedHashSet<Person>();
	private Map<String, String> typeMap = new HashMap<String, String>();
	private Map<String, String> languageMap = new HashMap<String, String>();
	private Map<Tuple, String> typeResolveMap = new HashMap<Tuple, String>();

	/**
	 * maps a string to a specific (in a config file determined) enum
	 * 
	 * @param original string of the type
	 * @return enum type
	 */
	private String chooseType(String type) {
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
	private void selectType() {
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
	 * @return the authors as a String, single names seperated by ","
	 */
	public String getAuthorsAsString() {
		String authorsString = "";
		Person current;
		Iterator<Person> it = authors.iterator();

		while (it.hasNext()) {
			current = it.next();
			authorsString = authorsString + current.getName() + ", ";
		}
		return authorsString;
	}

	/**
	 * add an author to the document, but preprocess it before
	 * @param author, which is to be added
	 */
	
	public void addAuthor(String author) {
		Person person;
		
		//is the person is completely written in upper case letters, capitalize it
		if (author.matches("[^a-z]*")) {
			author = WordUtils.capitalizeFully(author);
			//if the authors starts with "." dismiss it
			if (author.startsWith("."))
				author = author.substring(1);
			char[] authorChar = author.toCharArray();
			
			//if the author contains a "." write the character followd by it in upper case
			for (int i = 0; i < authorChar.length; i++) {
				if (authorChar[i] == '.') {
					authorChar[i - 1] = Character.toUpperCase(authorChar[i - 1]);
				}
			}
			author = String.valueOf(authorChar);
		}
		
		//if the author is not in a acceptable format, the whole string is written to unstructured
		if (!author.contains(",") || author.equals("[Unknown]") || author.endsWith(","))
			person = new Person(author);
		//otherwise split up in firstname, middlename, surname
		else {
			//surname is the first name until first ","
			String surname = author.substring(0, author.indexOf(","));
			
			//firstname is everything behind
			String firstname = author.substring(author.indexOf(",") + 2);
			
			//if the firstname contains a " " it has a middlename, extract it
			if (firstname.contains(" ")) {
				String middlename = firstname.substring(firstname.indexOf(" ") + 1);
				firstname = firstname.substring(0, firstname.indexOf(" "));
				person = new Person(firstname, middlename, surname);
			//a middlename to extract is also present if there are points in the first name
			} else if (firstname.matches("[A-Z].[A-Z].")) {
				String middlename = firstname.substring(firstname.indexOf(".") + 1);
				firstname = firstname.substring(0, firstname.indexOf(".") + 1);
				person = new Person(firstname, middlename, surname);
			} else {
				person = new Person(firstname, surname);
			}
		}
		//add the person to the authors
		authors.add(person);
	}

	/**
	 * call the normalizing functions in the necessary order
	 */
	public void normalize() {
		selectTitle();
		this.cleantitle = calculateTitleClean(this.title);
		setCleanTitle();
		language = setLanguageToStandard(language);
		selectYear();
		tidyUpKeywords();
		normalizeTitle();
		selectType();
		normalizePublishedIn();
	}
	
	/**
	 * normalize publishedIn by cutting of "; ..."
	 */
	private void normalizePublishedIn() {
		if(publishedIn != null)
			if (publishedIn.endsWith(" ; ..."))
				publishedIn = publishedIn.replace(" ; ...", "");
	}

	/**
	 * normalize Title by capitalizing it, if completly uppercase and erase " " if " :"
	 */
	private void normalizeTitle() {
		if (title.matches("[^a-z]*"))
			title = WordUtils.capitalizeFully(title);
		if (title.contains(" :"))
			title = title.replace(" :", ":");
	}

	/**
	 * preprocess keywords, by deleting title from it, putting everything in lower case, and splitting keywords at ":" and ","
	 */
	private void tidyUpKeywords() {
		//remove title
		keywords.remove(title);

		String[] copy = new String[keywords.size()];
		keywords.toArray(copy);

		for (String keyword : copy) {
			//put in lowercase
			String lowerKeyword = keyword.toLowerCase();
			keywords.remove(keyword);
			keywords.add(lowerKeyword);

			//split at ":"
			if (lowerKeyword.contains(":")) {
				keywords.remove(lowerKeyword);
				keywords.add(lowerKeyword.substring(0, lowerKeyword.indexOf(":")));
				keywords.add(lowerKeyword.substring(lowerKeyword.indexOf(":") + 1));
			}
			//split at ","
			if (lowerKeyword.contains(",")) {
				keywords.remove(lowerKeyword);
				keywords.add(lowerKeyword.substring(0, lowerKeyword.indexOf(",")));
				keywords.add(lowerKeyword.substring(lowerKeyword.indexOf(",") + 1));
			}
		}
	}

	/**
	 * select between two years a reasonable year, which is between 1500 and current+2
	 * year has priority over facetYear if both are reasonable
	 * if both are weird, say it and save it as 0, which will later be interpreted as NULL
	 */
	private void selectYear() {
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
	 * set a clean title, dismissing every character which is not a character from a-z or a number. And put it in lower case
	 */
	public String calculateTitleClean(String title) {
		String temp;
		temp = title.replaceAll("[^a-zA-Z0-9]", "");
		temp = temp.toLowerCase();
		return temp;
	}

	/**
	 * select a title from title and fulltitle (with priority on fulltitle)
	 */
	private void selectTitle() {
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
	 * if accidently the clean up of the title ended up with dismissing more than half of the characters (eg kanjis) stay with the normal title as cleantitle
	 */
	private void setCleanTitle() {
		if (cleantitle.length() <= title.length() / 2)
			cleantitle = title;
	}

	/**
	 * preprocess the information from the document to a int
	 * 
	 * @param year as String
	 * @return year as Int
	 */
	private int makeYearInt(String year) {
		Matcher m = Pattern.compile("\\d").matcher(year);
		
		//if year is empty, set 0
		if (year == null)
			return 0;
		//if year contains only numbers
		else if (m.find()) {
			//but less than 4, set 0
			if (year.substring(m.start()).length() < 4)
				return 0;
			//otherwise take the first 4 consecutive digits as year
			else if (year.substring(m.start(), m.start() + 4).matches("[0-9][0-9][0-9][0-9]"))
				year = year.substring(m.start(), m.start() + 4);
			else
				return 0;
		} else
			return 0;
		return Integer.parseInt(year);
	}

	/**
	 * maps a language string to a 2 char enum (from ISO standard)
	 * 
	 * @param language as String
	 * @return language as 2 char enum
	 */
	private String setLanguageToStandard(String language) {
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

	public void setCollection(String collection) {
		this.collection = collection;
	}
	
	/**
	 * Default getter for property collection.
	 * 
	 * @return value of property collection
	 */
	public String getCollection() {
		return collection;
	}

	public LinkedHashSet<Person> getAuthors() {
		return authors;
	}

	public void addKeyWord(String keyword) {
		keywords.add(keyword);
	}

	/**
	 * get the Keywords as a String, dismissing <,>
	 * @return keywords as String
	 */
	public String getKeywordsAsString() {
		String keywordString = "";
		StringJoiner joiner = new StringJoiner(", ");
		Iterator<String> it = keywords.iterator();

		while (it.hasNext()) {
			joiner.add(it.next());
			it.remove();
		}

		keywordString = joiner.toString();
		keywordString.replace("<", "");
		keywordString.replace(">", "");
		keywordString.trim();
		return keywordString;
	}

	public int getFacetYyear() {
		return facetYear;
	}

	public void setFacetYear(String facetYear) {
		this.facetYear = makeYearInt(facetYear);
	}

	public void addType(String type) {
		if (!chooseType(type).equals("unknown"))
			this.typeSet.add(chooseType(type));
	}

	public int getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = makeYearInt(year);
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
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
	public MediaTUMXMLDocument(Map<String, String> typeMap, Map<String, String> languageMap,
			Map<Tuple, String> typeResolveMap) {
		super(typeMap, languageMap, typeResolveMap);
		
		this.typeMap = typeMap;
		this.languageMap = languageMap;
		this.typeResolveMap = typeResolveMap;
		type = "unknown";
		publishedInRank = 10;
	}

	public String getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFulltitle() {
		return fulltitle;
	}

	public void setFulltitle(String fulltitle) {
		this.fulltitle = fulltitle;
	}

	public String getCleanTitle() {
		return cleantitle;
	}

	public String getDocumentPath() {
		return documentPath;
	}

	public void setDocumentPath(String documentPath) {
		this.documentPath = documentPath;
	}

	public String getPublishedIn() {
		return publishedIn;
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

	public ArrayList<Abstract> getAbstracts() {
		return abstr;
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

	@Override
	public String toString() {
		String abstracts = "";
		for (Abstract abstract_ : abstr) {
			abstracts += "(" + abstract_.getLanguage() + "|" + abstract_.getContent() + ")";
		}
		
		String authorsAsString = "";
		for (Person person : authors) {
			authorsAsString += "(" + person.getName() + ")";
		}
		
		return "MediaTUMXMLDocument [id=" + id + ", title=" + title + ", fulltitle=" + fulltitle + ", language="
				+ language + ", abstr=" + abstracts + ", year=" + year + ", facetYear=" + facetYear + ", keywords="
				+ keywords + ", type=" + type + ", publishedIn=" + publishedIn + ", collection=" + collection
				+ ", authors=" + authorsAsString + "]";
	}
	
}
