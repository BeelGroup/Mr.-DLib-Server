package org.mrdlib.partnerContentManager.core;
import org.apache.commons.lang3.text.WordUtils;
import org.mrdlib.partnerContentManager.gesis.Abstract;
import org.mrdlib.partnerContentManager.gesis.Person;
import org.mrdlib.partnerContentManager.general.Document;

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

public class JSONDocument extends Document {
	private Long repository;
	private String date;
	
	public void setDate(String date)
	{
		this.date = date;
	}
	
	public int getYear() 
	{
		return year;
	}
	
	public long getRepository()
	{
		return repository;
	}
	public void setRepository(Long repo)
	{
		 repository = repo;
	}
	
	public String getDate()
	{
		return date;
	}
	
}
