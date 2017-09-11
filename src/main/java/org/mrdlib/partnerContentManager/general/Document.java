package org.mrdlib.partnerContentManager.general;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;
import org.mrdlib.partnerContentManager.gesis.Abstract;
import org.mrdlib.partnerContentManager.gesis.Person;
import org.mrdlib.partnerContentManager.gesis.Tuple;
import org.mrdlib.database.DBConnection;


/**
 * Common base for dataformats of documents we import
 * e.g. XMLDocument, JSONDocument
 */
public class Document {
	protected HashMap<String, String> externalIds = new HashMap<String, String>();
    protected LinkedHashSet<Person> authors = new LinkedHashSet<Person>();
    protected ArrayList<Abstract> abstr = new ArrayList<Abstract>();
    protected String cleanTitle;
    protected String documentPath;
    protected String id;
    protected String title;
    protected String type;
    protected String collection;
    protected int year;
    protected String language;
    protected String languageDetected;
    protected Set<String> keywords = new HashSet<String>();
    protected String publishedIn;


	public void addExternalId(String name, String id) {
		externalIds.put(name, id);
	}

	public Set<Map.Entry<String, String>> getExternalIds() {
		return externalIds.entrySet();
	}

    /**
     * add an abstract to a document, detecting its language automatically
     * @param abstract
     */
    public void addAbstract(String text, String language) {
		text = text.trim().replaceAll("\\s+", " ");
		Abstract abstractObj = new Abstract(text, language, LanguageDetection.detectLanguage(text));
        this.abstr.add(abstractObj);
    }

	/**
	 * preprocess keywords, by deleting title from it, putting everything in lower case, and splitting keywords at ":" and ","
	 */
	protected void tidyUpKeywords() {
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
     * normalize title, create clean title
     */
    public void normalize() {
        // set a clean title, dismissing every character which is not a character from a-z or a number. And put it in lower case
        cleanTitle = title.replaceAll("[^a-zA-Z]", "").toLowerCase();
        // if accidently the clean up of the title ended up with dismissing more than half of the characters (eg kanjis),
        // stay with the normal title as cleanTitle
        if (cleanTitle.length() <= title.length() / 2)
            cleanTitle = title;

        if (title.matches("^[a-z\\s]*$"))
            title = WordUtils.capitalizeFully(title);
		else if (title.matches("^[A-Z\\s]*$"))
            title = WordUtils.capitalizeFully(title.toLowerCase());
        if (title.contains(" :"))
            title = title.replace(" :", ":");
		title = title.trim().replaceAll("\\s+", " ");

		tidyUpKeywords();
    }

    /**
     * preprocess the information from the document to a int
     *
     * @param year as String
     * @return year as Int
     */
    protected int makeYearInt(String year) {
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
     * add an author to the document, but pre-process it before
     *
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

            //if the author contains a "." write the character followed by it in upper case
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


    public void setId(String id) {
        this.id = id;
    }

    public void setCollectionId(String collection) {
        this.collection = collection;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
		this.languageDetected = LanguageDetection.detectLanguage(title);
    }

    public void setYear(String year) {
        this.year = makeYearInt(year);
    }


    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public int getYear() {
        return year;
    }

    public String getId() {
        return id;
    }

    public String getCollectionId() {
        return collection;
    }

	// use this to define whether the collectionId property is a long name / short name / id / ...
	// return the ID as in database
	public Long convertCollectionIdForDb(DBConnection db) throws Exception {
		return Long.parseLong(getCollectionId());
	}

    public String getType() {
        return type;
    }

    public String getTitle()
    {
        return title;
    }

    public String getCleanTitle()
    {
        return cleanTitle;
    }

    public ArrayList<Abstract> getAbstracts() {
        return abstr;
    }

    public LinkedHashSet<Person> getAuthors() {
        return authors;
    }

	public String getAuthorsAsString() {
		StringJoiner joiner = new StringJoiner(", ");

		for (Person p : authors) {
			joiner.add(p.getName());
		}

		return joiner.toString();
	}

    public String getDocumentPath() {
        return documentPath;
    }

	public void addKeyword(String keyword) {
		keywords.add(keyword);
	}

	/**
	 * get the Keywords as a String, dismissing <,>
	 * @return keywords as String
	 */
	public String getKeywordsAsString() {
		StringJoiner joiner = new StringJoiner(", ");

		for (String keyword : keywords) {
			joiner.add(keyword);
		}

		return joiner.toString()
			.replace("<", "")
			.replace(">", "")
			.trim();
	}



	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getPublishedIn() {
		return publishedIn;
	}

	public void setPublishedIn(String publishedIn) {
		this.publishedIn = publishedIn;
	}

	public String getLanguageDetected() {
		return languageDetected;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Document{");
		sb.append("authors = ").append(getAuthors());
		sb.append(", abstr = ").append(abstr);
		sb.append(", cleanTitle = ").append(getCleanTitle());
		sb.append(", documentPath = ").append(getDocumentPath());
		sb.append(", id = ").append(getId());
		sb.append(", title = ").append(getTitle());
		sb.append(", type = ").append(getType());
		sb.append(", collection = ").append(collection);
		sb.append(", year = ").append(getYear());
		sb.append(", language = ").append(getLanguage());
		sb.append(", languageDetected = ").append(getLanguageDetected());
		sb.append(", keywords = ").append(keywords);
		sb.append(", publishedIn = ").append(getPublishedIn());
		return sb.append("}").toString();
	}

}
