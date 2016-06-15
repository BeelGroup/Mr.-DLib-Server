package org.mrdlib.tools;

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

public class XMLDocument {
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
	private LinkedHashSet<Person> authors = new LinkedHashSet<Person>();
	private Map<String, String> typeMap = new HashMap<String, String>();
	private Map<String, String> languageMap = new HashMap<String, String>();
	private Map<Tuple, String> typeResolveMap = new HashMap<Tuple, String>();

	public String chooseType(String type) {
		String tempType;
		if (type.equals(""))
			return "unknown";
		else {
			tempType = typeMap.get(type.toLowerCase());
			if (tempType != null && !tempType.equals(""))
				return tempType;
			else {
				System.out.println("In Export " + documentPath + " In Document: " + id + ": Undefined Type: " + type);
				return "unknown";
			}
		}
	}

	public void selectType() {
		Iterator<String> it;
		String allTypes = "";
		String current;

		if (typeSet.isEmpty())
			type = "unknown";
		else {
			it = typeSet.iterator();
			while (it.hasNext()) {
				current = it.next();
				allTypes = allTypes + ", " + current.toString();

				if (type == null || type.equals("unknown"))
					type = current.toString();
				else if (!type.equals(current.toString())) {
					Tuple tupleKey = new Tuple(type, current.toString());
					if (typeResolveMap.containsKey(tupleKey))
						type = typeResolveMap.get(tupleKey);
					else {
						System.out.println(
								"In Export " + documentPath + " In Document: " + id + ": Multiple Types: " + allTypes);
						type = "unknown";
					}
				}
			}
		}
	}

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
		language = setLanguageToStandard(language);
		selectYear();
		tidyUpKeywords();
		normalizeTitle();
		selectType();
	}

	private void normalizeTitle() {
		if (title.matches("[^a-z]*"))
			title = WordUtils.capitalizeFully(title);
		if (title.contains(" :"))
			title.replace(" :", ":");
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

	private String setLanguageToStandard(String language) {
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

		return language;
	}

	public String getCollection() {
		// File file = new File(documentPath.toString());
		// String dir =
		// file.getParent().substring(file.getParent().lastIndexOf(File.separator)
		// + 1);
		return "gesis";
	}

	public LinkedHashSet<Person> getAuthors() {
		return authors;
	}

	public void addKeyWord(String keyword) {
		keywords.add(keyword);
	}

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

	public XMLDocument(Map<String, String> typeMap, Map<String, String> languageMap,
			Map<Tuple, String> typeResolveMap) {
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

	public void addAbstract(String abstr, String lan) {
		lan = setLanguageToStandard("(" + lan + ")");
		this.abstr.add(new Abstract(abstr, lan));
	}
}
