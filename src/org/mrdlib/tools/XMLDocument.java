package org.mrdlib.tools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;

public class XMLDocument {
	private String documentPath;
	private String id;
	private String title;
	private String fulltitle;
	private String cleantitle;
	private String language;
	private int year;
	private int facetYear;
	private Set<String> keywords = new HashSet<String>();
	private String type;
	private String[] typeList = new String[3];
	private String publishedIn;
	private String[] publishedInList = new String[3];
	private ArrayList<Person> authors = new ArrayList<Person>();
	private Map<String, String> typeMap = new HashMap<String, String>();
	private Map<String, String> languageMap = new HashMap<String, String>();
	

	public void selectPublishedIn() {
		// set to [0] IF: only [0] is set || every entry is the same || ([0] ==
		// [1] && [2] is empty) || ([0] == [2] && [1] is empty)
		if ((!publishedInList[0].equals("") && publishedInList[1].equals("") && publishedInList[2].equals(""))
				|| (publishedInList[0].equals(publishedInList[1]) && publishedInList[2].equals(""))
				|| (publishedInList[0].equals(publishedInList[2]) && publishedInList[1].equals("")))
			publishedIn = publishedInList[0];
		// set to [1] IF: only [1] is set || ([1] == [2] && [0] is empty)
		else if ((publishedInList[0].equals("") && !publishedInList[1].equals("") && publishedInList[2].equals(""))
				|| (publishedInList[1].equals(publishedInList[2]) && publishedInList[0].equals("")))
			publishedIn = publishedInList[1];
		// set to [2] IF: only [2] is set
		else if (publishedInList[0].equals("") && publishedInList[1].equals("") && !publishedInList[2].equals(""))
			publishedIn = publishedInList[2];
		else {
			System.out.println("In Export " + documentPath + " In Document: " + id + ": Multiple Publisher: "
					+ publishedInList[0] + ", " + publishedInList[1] + ", " + publishedInList[2]);
			publishedIn = null;
		}
	}

	public String chooseType(String type) {
		String tempType;

		tempType = typeMap.get(type.toLowerCase());
		if (!(tempType == null))
			return tempType;
		else {
			System.out.println("In Export " + documentPath + " In Document: " + id + ": Undefined Type: " + type);
			return "unkown";
		}
	}

	public void selectType() {
		// set to [0] IF: only [0] is set || every entry is the same || ([0] ==
		// [1] && [2] is empty) || ([0] == [2] && [1] is empty)
		if ((!typeList[0].equals("") && typeList[1].equals("") && typeList[2].equals(""))
				|| (typeList[0].equals(typeList[1]) && typeList[2].equals(typeList[2]))
				|| (typeList[0].equals(typeList[1]) && typeList[2].equals(""))
				|| (typeList[0].equals(typeList[2]) && typeList[1].equals("")))
			type = typeList[0];
		// set to [1] IF: only [1] is set || ([1] == [2] && [0] is empty)
		else if ((typeList[0].equals("") && !typeList[1].equals("") && typeList[2].equals(""))
				|| (typeList[1].equals(typeList[2]) && typeList[0].equals("")))
			type = typeList[1];
		// set to [2] IF: only [2] is set
		else if (typeList[0].equals("") && typeList[1].equals("") && !typeList[2].equals("")
				|| (!typeList[0].equals("") && !typeList[2].equals("")))
			type = typeList[2];
		else {
			System.out.println("In Export " + documentPath + " In Document: " + id + ": Multiple Types: " + typeList[0]
					+ ", " + typeList[1] + ", " + typeList[2]);
			type = "unknown";
		}
	}

	public String getAuthorsAsString() {
		String authorsString = "";
		for (int i = 0; i < authors.size(); i++) {
			authorsString = authorsString + authors.get(i).getName() + ", ";
		}
		return authorsString;
	}

	public void addAuthor(String author) {
		Person person;
		if (author.matches("[^a-z]*")) {
			author = WordUtils.capitalizeFully(author);
			if (author.startsWith("."))
				author = author.substring(1);
			char[] authorChar = author.toCharArray();
			for (int i = 0; i < authorChar.length; i++) {
				if (authorChar[i] == '.') {
					authorChar[i - 1] = Character.toUpperCase(authorChar[i - 1]);
				}
			}
			author = String.valueOf(authorChar);
		}
		if (!author.contains(",") || author.equals("[Unknown]") || author.endsWith(","))
			person = new Person(author);
		else {
			String surname = author.substring(0, author.indexOf(","));
			String firstname = author.substring(author.indexOf(",") + 2);

			if (firstname.contains(" ")) {
				String middlename = firstname.substring(firstname.indexOf(" ") + 1);
				firstname = firstname.substring(0, firstname.indexOf(" "));
				person = new Person(firstname, middlename, surname);
			} else if (firstname.matches("[A-Z].[A-Z].")) {
				String middlename = firstname.substring(firstname.indexOf(".") + 1);
				firstname = firstname.substring(0, firstname.indexOf(".") + 1);
				person = new Person(firstname, middlename, surname);
			} else {
				person = new Person(firstname, surname);
			}
		}
		authors.add(person);
	}

	public void normalize() {
		selectTitle();
		calculateTitleClean();
		setCleanTitle();
		setLanguageToStandard();
		selectYear();
		tidyUpKeywords();
		normalizeTitle();
		selectType();
	}

	private void normalizeTitle() {
		if (title.matches("[^a-z]*"))
			title = WordUtils.capitalizeFully(title);
	}

	private void tidyUpKeywords() {
		keywords.remove(title);

		String[] copy = new String[keywords.size()];
		keywords.toArray(copy);

		for (String keyword : copy) {
			String lowerKeyword = keyword.toLowerCase();
			keywords.remove(keyword);
			keywords.add(lowerKeyword);

			if (lowerKeyword.contains(":")) {
				keywords.remove(lowerKeyword);
				keywords.add(lowerKeyword.substring(0, lowerKeyword.indexOf(":")));
				keywords.add(lowerKeyword.substring(lowerKeyword.indexOf(":") + 1));
			}
			if (lowerKeyword.contains(",")) {
				keywords.remove(lowerKeyword);
				keywords.add(lowerKeyword.substring(0, lowerKeyword.indexOf(",")));
				keywords.add(lowerKeyword.substring(lowerKeyword.indexOf(",") + 1));
			}
		}
	}

	private void selectYear() {
		int tempYear = 0;
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		if (year < 1500 || year > currentYear + 2) {
			if (facetYear < 1500 || facetYear > currentYear + 2) {
				if (facetYear == 0 && year == 0) {
				} else {
					System.out.println("In Export " + documentPath + " In Document: " + id + "Weird Years: " + year
							+ ", " + facetYear);
					tempYear = 0;
				}
			} else
				tempYear = facetYear;
		} else
			tempYear = year;
		year = tempYear;
	}

	private void calculateTitleClean() {
		String temp;
		temp = title.replaceAll("[^a-zA-Z0-9]", "");
		temp = temp.toLowerCase();
		this.cleantitle = temp;
	}

	private void selectTitle() {
		if (fulltitle == null) {
		} else if (title.equals(fulltitle) || title == null || fulltitle.length() > title.length())
			title = fulltitle;
		else if (!title.equals(fulltitle)) {
			System.out.println("In Export " + documentPath + " In Document: " + id);
			System.out.println("Different titles!");
			System.out.println("Title: " + title);
			System.out.println("Fulltitle: " + fulltitle);
			System.out.println("------------------------");
		}
	}

	private void setCleanTitle() {
		if (cleantitle.length() <= title.length() / 2)
			cleantitle = title;
	}

	private int makeYearInt(String year) {
		Matcher m = Pattern.compile("\\d").matcher(year);
		if (year == null)
			return 0;
		else if (m.find()) {
			if (year.substring(m.start()).length() < 4)
				return 0;
			else if (year.substring(m.start(), m.start() + 4).matches("[0-9][0-9][0-9][0-9]"))
				year = year.substring(m.start(), m.start() + 4);
			else
				return 0;
		} else
			return 0;
		return Integer.parseInt(year);
	}

	private void setLanguageToStandard() {
		boolean validLanguage = true;
		String tempLan;
		if (!(language == null || language.equals("Keine Angabe") || language.equals("Multilingual"))) {
			String[] languages = Locale.getISOLanguages();
			validLanguage = false;

			tempLan = languageMap.get(language.toLowerCase());
			if (!(tempLan == null))
				language = tempLan;

			if (language.contains("(") && language.contains(")")) {
				String substring = language.substring(language.indexOf("(") + 1, language.indexOf(")")).toLowerCase();
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
			System.out.println("In Export " + documentPath + " In Document: " + id + ": Language needs to be defined: "
					+ language);
			language = null;
		}
	}
	
	public String getCollection() {
		//File file = new File(documentPath.toString());
		//String dir = file.getParent().substring(file.getParent().lastIndexOf(File.separator) + 1);
		return "gesis";
	}
	
	public ArrayList<Person>getAuthors() {
		return authors;
	}

	public void addKeyWord(String keyword) {
		keywords.add(keyword);
	}

	public String getKeywordsAsString() {
		return keywords.toString();
	}

	public int getFacetYyear() {
		return facetYear;
	}

	public void setFacetYear(String facetYear) {
		this.facetYear = makeYearInt(facetYear);
	}

	public void setPublishedInList(String published, int i) {
		this.publishedInList[i] = published;
	}

	public void setTypeList(String type, int i) {
		this.typeList[i] = chooseType(type);
	}

	public int getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = makeYearInt(year);
	}

	public String getLanguage() {
		if (language == null)
			return "";
		else
			return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public XMLDocument(Map<String, String> typeMap, Map<String, String> languageMap) {
		Arrays.fill(typeList, "");
		this.typeMap = typeMap;
		this.languageMap = languageMap;
		type = "unknown";
	}

	public String getType() {
		return type;
	}

	public void setTypeList(String[] typeList) {
		this.typeList = typeList;
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
}
