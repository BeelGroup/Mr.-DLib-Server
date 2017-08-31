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
 * @author Millah, Wuestehube
 *
 * Holds information of an (academic) document.
 * This class acts as an intermediate between partner content module and database.
 */
public class MediaTUMXMLDocument extends XMLDocument {
	
	protected String license;
	protected String fullText;
	
	/**
	 * Constructor, which initialize the needed Maps for mapping and resolving types and language.
	 * Set some default values.
	 * 
	 * @param typeMap
	 * @param languageMap
	 * @param typeResolveMap
	 */
	public MediaTUMXMLDocument(Map<String, String> typeMap, Map<String, String> languageMap,
			Map<Tuple, String> typeResolveMap) {
		super(typeMap, languageMap, typeResolveMap);
		
		publishedInRank = 10;
	}

	/**
	 * Call the normalizing functions in the necessary order.
	 */
	public void normalize() {
		selectTitle();
		super.normalize();
	}

	/**
	 * Select a title from title and fulltitle (with priority on fulltitle).
	 */
	protected void selectTitle() {
		// if fulltitle is null, stay with title
		if (fulltitle == null) {}
		// if fulltitle is not null but and contains more information thatn title, set fulltitle
		else if (title.equals(fulltitle) || title == null || fulltitle.length() > title.length())
			title = fulltitle;
		// if fulltitle is not equals title and not as big, say it and stay with title
		else if (!title.equals(fulltitle)) {
			System.out.println("Different titles!");
			System.out.println("Title: " + title);
			System.out.println("Fulltitle: " + fulltitle);
			System.out.println("------------------------");
		}
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getFullText() {
		return fullText;
	}

	public void setFullText(String fullText) {
		this.fullText = fullText;
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
		
		return "MediaTUMXMLDocument [title=" + title + ", fulltitle=" + fulltitle + ", language="
				+ language + ", abstr=" + abstracts + ", year=" + year + ", keywords="
				+ keywords + ", type=" + type + ", publishedIn=" + publishedIn + ", collection=" + collection
				+ ", authors=" + authorsAsString + ", license=" + license + ", fulltext=" + fullText + "]";
	}
	
}
