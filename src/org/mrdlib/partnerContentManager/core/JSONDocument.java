package org.mrdlib.partnerContentManager.core;
import org.apache.commons.lang3.text.WordUtils;
import org.mrdlib.partnerContentManager.gesis.Abstract;
import org.mrdlib.partnerContentManager.gesis.Person;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * 
 * @author Millah
 * @author Dixon
 * 
 * stores and pre-processes all necessary information of a JSON document
 *	
 */

public class JSONDocument {
	private String identifier;
	private Long repository;
	private String type;
	private String title;
	private String cleantitle;
	private String date;
	private String documentPath;
	private int year;
	private ArrayList<Abstract> abstr = new ArrayList<Abstract>();
	private LinkedHashSet<Person> authors = new LinkedHashSet<Person>();
	
	
	/**
	 * set an abstract to a document
	 * @param abstract
	 */
	public void addAbstract(String abstr) {
		if(abstr == null){
			
			abstr = null;
		}
		else
		{
		if(abstr.contains("\\ud"))
			abstr = abstr.replace("\\ud", " ");
		if(abstr.contains("\n"))
			abstr = abstr.replace("\n", "");
		abstr = abstr.replaceAll("[^a-zA-Z\\s]", "").replaceAll("\\s+", " ");
		}
		this.abstr.add(new Abstract(abstr));
		
	}
	
	/**
	 * normalize Title by capitalizing it, if completely uppercase and erase " " if " :"
	 */
	private void normalizeTitle() {
		if (title.matches("[^a-z]*"))
			title = WordUtils.capitalizeFully(title);
		if (title.contains(" :"))
			title = title.replace(" :", ":");
		title = title.replaceAll("[^a-zA-Z\\s]", "").replaceAll("\\s+", " ");
	}

	/**
	 * if accidently the clean up of the title ended up with dismissing more than half of the characters (eg kanjis) stay with the normal title as cleantitle
	 */
	private void setCleanTitle() {
		if (cleantitle.length() <= title.length() / 2)
			cleantitle = title;
	}
	
	/**
	 * call the normalizing functions
	 */
	public void normalize() {
		this.cleantitle = calculateTitleClean(this.title);
		setCleanTitle();
		normalizeTitle();
	}
	
	/**
	 * set a clean title, dismissing every character which is not a character from a-z or a number. And put it in lower case
	 * @param title as String
	 * @return cleaned title as String
	 */
	public String calculateTitleClean(String title) {
		String temp;
		temp = title.replaceAll("[^a-zA-Z0-9]", "");
		temp = temp.toLowerCase();
		return temp;
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
	

	public void setIdentifier(String id)
	{
		this.identifier = id;
	}
	
	public void setRepository(Long id)
	{
		this.repository = id;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public void setDate(String date)
	{
		this.date = date;
	}
	
	public void setYear(String year) 
	{
		this.year = makeYearInt(year);
	}

	
	public void setDocumentPath(String documentPath)
	{
		this.documentPath = documentPath;
	}
	
	public int getYear() 
	{
		return year;
	}
	
	public String getIdentifier()
	{
		return identifier;
	}
	
	public long getRepository()
	{
		return repository;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public String getCleanTitle() 
	{
		return cleantitle;
	}
	
	public ArrayList<Abstract> getAbstracts() {
		return abstr;
	}
	
	public LinkedHashSet<Person> getAuthors()
	{
		return authors;
	}
	
	public String getDate()
	{
		return date;
	}
	
	public String getDocumentPath()
	{
		return documentPath;
	}
	
}
