package org.mrdlib.partnerContentManager.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.LineNumberReader;


import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mrdlib.database.DBConnection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;

import static java.nio.charset.StandardCharsets.*;
/**
 * 
 * @author Millah
 * @author Dixon
 * 
 * class which reads the information of a json document and writes it in a database.
 * The information is passed to JSONDocument where it is preprocessed and after that it is written to a database
 *
 */


public class readJSON {
	
	private DBConnection con;
	
	/**
	 * create new DBConnection
	 */
	
	public readJSON()
	{
		
		try {
			con = new DBConnection("jar");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * 
	 * get a ready to process JSON file from a given path
	 * 
	 * @param path where the document is
	 * @return ready to process JSON document
	 */
	
	public Document getDocFromPath(String path) {
		Document doc = null;
		try {
			File inputFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	/*
	 * counts the number of lines in the file
	 * 
	 * @param name of the file
	 * @return count
	 */
	public static int countLines(String filename) throws IOException {
		/*
		File file =new File(filename);
		FileReader fr = new FileReader(file);
		LineNumberReader lnr = new LineNumberReader(fr);
		int linenumber = 0;
		try{
			
    		if(file.exists()){
    		    

    	            while (lnr.readLine() != null){
    	        	linenumber++;
    	            }
    	                	         
    	    
    		}else{
    			 System.out.println("File does not exists!");
    		}
    		
    	}catch(IOException e){
    		e.printStackTrace();
    	}finally{
    		lnr.close();
    	}
		return(linenumber);
	
	}*/
		
		
		
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }		
	}
	
	/**
	 * process the JSON file which has to be read in
	 * creates a document and write it to database
	 * 
	 * @param path of the document to process
	 */
	
	public void processJSON(String path) { 
		
		
		BufferedReader br = null;
        JSONParser parser = new JSONParser();
		Object obj;
		int docLength;	
		
		try {
			
			String sCurrentLine;
			
			//get the path in which the json files are stored
			br = new BufferedReader(new FileReader(path));			           	        			
			
			//count the number of lines in the json document
			docLength = countLines(path);	
			
			//create arrays where each record present in a json document is stored
			JSONDocument[] inf = new JSONDocument[docLength];
			
			//for each record do
			while ((sCurrentLine = br.readLine()) != null) {
                
				//System.out.println("Record:\t" + sCurrentLine);
				
				obj = parser.parse(sCurrentLine);
				JSONObject jsonObject = (JSONObject) obj;
				
				int i = 0;             
                try {
                	
                	//get the identifier value from record in json file
                	String identifier = String.valueOf((Long) jsonObject.get("identifier"));
                	
                	//get the type from the record in json file
                	String type = (String) jsonObject.get("dc:type").toString();
                	
                	System.out.println(identifier);
                	
                	
                	inf[i] = new JSONDocument();
                	
                	//set the document path
                	inf[i].setDocumentPath(path);
                	
                	//add a prefix "core-" to the identifers
                	inf[i].setId("core-"+ identifier);
                	
                	//get the repository value from the record in json file
                	inf[i].setRepository((Long) jsonObject.get("ep:Repository")); 
                	
                	//get the title from the record in json file
                	inf[i].setTitle((String) jsonObject.get("bibo:shortTitle"));
                	
                	//if the record type is null or has "[]" as its value set type as unknown
                	if (type == null || type.equals("[]"))
                	{
                		inf[i].setType("unknown");
                	}
                	else
                	{
                		inf[i].setType((String) jsonObject.get("dc:type").toString());
                	}                	 
                	 
                	//get the abstracts for the documents
					String abstractText = (String) jsonObject.get("bibo:abstract");
                	inf[i].addAbstract(abstractText, null);
                	
                	//split the array of authors for each document
                	Pattern p = Pattern.compile("\"([^\"]*)\"");
            		Matcher m = p.matcher(jsonObject.get("bibo:AuthorList").toString());
            		 
            		 
            		while (m.find()) {
            			inf[i].addAuthor(m.group(1));
            			/*byte ptext[] = m.group(1).getBytes(ISO_8859_1);
            			String value1 = new String(ptext, UTF_8);
            			inf[i].addAuthor(value1);*/
            		}                	
                	
            		//get the year for each record
                	inf[i].setYear((String) jsonObject.get("dc:date"));
                	
                
                	inf[i].normalize();
                	con.insertDocument(inf[i]);
                	i++;
                	
                	
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                	// TODO Auto-generated catch block
                	e.printStackTrace();
                }
            }
	}catch (IOException e) {
        e.printStackTrace();
    }catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }finally {
        try {
            if (br != null)br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
	}
	
	public static void main(String[] args) throws JSONException{
		   readJSON rjson = new readJSON();
		   
		   // walk through every file in the folder and call processJSON
		   try {
			   Files.walk(Paths.get(args[0]))
			     .filter(p -> p.toString().endsWith(".json"))
		         .distinct()
		         //.forEach(System.out::println);
		         .forEach(p -> rjson.processJSON(p.toString()));

			} catch (Exception e) {
				e.printStackTrace();
			}
	   }
	
}
