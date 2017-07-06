package org.mrdlib.partnerContentManager.gesis;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.mrdlib.database.DBConnection;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * 
 * @author Millah
 * 
 * class which reads the information of a xml document containing documents (haha) and write it in a database.
 * The information is passed to XMLDocument where it is preprocessed and after that it is written to a database
 *
 */

public class readXML {
	//C:\Users\{name}\workspace\MrDlib\resources
	//"/home/mrdlib"
	
	//set config pathes
	protected String configFolder = "/home/mrdlib";
	protected String type_config = "/document_types.xml";
	protected String type_resolved_config = "/document_types_resolved.xml";
	protected String language_config = "/language.xml";
	
	public Map<String, String> typeMap = new HashMap<String, String>();
	public Map<String, String> languageMap = new HashMap<String, String>();
	public Map<Tuple, String> typeResolveMap = new HashMap<Tuple, String>();
	
	private DBConnection con;
	
	/**
	 * create new DBConnection
	 */
	public readXML() {
		try {
			con = new DBConnection("jar");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * get a ready to process XML file from a given path
	 * 
	 * @param path where the document is
	 * @return ready to process XML document
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
	
	/**
	 * 
	 * creates a map which is used to resolve conflicts when to different types of publishedIn information is given
	 * 
	 * @param path of the xml-config file
	 * @param map of the conflicts and their solution
	 * @return the filled map (key is conflicting tuple, value is solution)
	 */
	public Map<Tuple, String> createResolveMap(String path, Map<Tuple, String> map) {
		//this is the node which is looked for (containing the solution for the conflict)
		String solutionNodeText = "mdl_type";
		try {
			//get the ready to process XML
			Document doc = getDocFromPath(configFolder.concat(path));
			//get every node which contains a solution, get the nodes which contains the conflict 
			NodeList originalTypeNodes = doc.getElementsByTagName(solutionNodeText);
			
			for (int i = 0; i < originalTypeNodes.getLength(); i++) {
				//get the solution (contained in the solution node)
				String solution = originalTypeNodes.item(i).getAttributes().getNamedItem("name").getNodeValue().toLowerCase();
				//get the nodes which contain the conflicting parts
				NodeList childNodes = originalTypeNodes.item(i).getChildNodes();
				
				//create a tuple so that order dont matter in comparing later
				Tuple tuple = new Tuple(childNodes.item(1).getAttributes().getNamedItem("name").getNodeValue().toLowerCase(),
						childNodes.item(2).getNextSibling().getAttributes().getNamedItem("name").getNodeValue().toLowerCase());
				
				//put the conflicting types and the solution to a map (which is searchable by the tuple)
				map.put(tuple, solution);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * 
	 * create a map which maps original strings to custom enums
	 * 
	 * @param path of the xml-config file to use
	 * @param map where the mapping is stored
	 * @param nodeText contains the name of the node the custom enum is stored (child nodes are the original strings)
	 * @return the filled map (key is original string, value is custum enum)
	 */
	public Map<String, String> createMap(String path, Map<String, String> map, String nodeText) {
		try {
			Document doc = getDocFromPath(configFolder.concat(path));

			//get the original string nodes
			NodeList originalTypeNodes = doc.getElementsByTagName(nodeText);
			
			for (int i = 0; i < originalTypeNodes.getLength(); i++) {
				//put both the original string and the corresponding enum in a map, first the original one, than the custom enum one
				map.put(originalTypeNodes.item(i).getAttributes().getNamedItem("name").getNodeValue().toLowerCase(), 
						originalTypeNodes.item(i).getParentNode().getAttributes().getNamedItem("name").getNodeValue().toLowerCase());
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * process the XML file which has to be read in
	 * creates a document and write it to database
	 * 
	 * @param path of the document to process
	 */
	public void processXML(Path path) {
		try {
			Document doc = getDocFromPath(path.toString());
			
			//get each (academic) document
			NodeList docList = doc.getElementsByTagName("doc");

			//create a arrays where each (academic) document of an xml document is stored in
			XMLDocument[] inf = new XMLDocument[docList.getLength()];

			//for each (academix) document do
			for (int i = 0; i < docList.getLength(); i++) {
				//get a mapping of the config files
				inf[i] = new XMLDocument(typeMap, languageMap, typeResolveMap);
				//save the path for error backtracking
				inf[i].setDocumentPath(path.toString());

				Node nNode = docList.item(i).getFirstChild();
				
				//pass through each node
				while (nNode.getNextSibling() != null) {
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						//get the attribute value of attribute name
						String attribute = eElement.getAttribute("name");
						
						//add the abstracts to the XML documents
						if(attribute.matches("description_[A-Za-z]+_txt_mv")) {
							//get the language which is hidden in the attribute value
							int firstPos = attribute.indexOf("_")+1;
							int secondPos = attribute.substring(attribute.indexOf("_")+1).indexOf("_") + firstPos;
							String lan = attribute.substring(firstPos, secondPos);
							//add abstract and corresponding language
							inf[i].addAbstract(eElement.getTextContent(), lan);
						}
						
						//switch over the intresting nodes and write the information to the corresponding field.
						switch (attribute) {
						case "id":
							inf[i].setId(eElement.getTextContent());
							break;
						case "title":
							inf[i].setTitle(eElement.getTextContent());
							break;
						case "title_full":
							inf[i].setFulltitle(eElement.getTextContent());
							break;
						case "language":
							inf[i].setLanguage(eElement.getTextContent());
							break;
						case "publishDate":
							inf[i].setYear(eElement.getTextContent());
							break;
						case "facet_publishDate_str":
							inf[i].setFacetYear(eElement.getTextContent());
							break;
						case "person_author_txtP_mv":
						/*case "person_other_txtP_mv":
						case "person_other_normalized_str_mv":
						case "search_person_txtP_mv":*/
							inf[i].addAuthor(eElement.getTextContent());
							break;
						case "search_schlagwoerter_txtP_mv":
						case "spellingShingle":
						case "topic":
						case "classification_txtP_mv":
						case "facet_topic_str_mv":
							inf[i].addKeyWord(eElement.getTextContent());
							break;
						case "doctype_lit_str":
						case "doctype_lit_add_str":
						case "temp_doctypes_orginal_str":
							inf[i].addType(eElement.getTextContent());
							break;
						case "publisher":
						case "Sseries_str_mv":
						case "Satit_str":
						case "journal_full_txt_mv":
						case "journal_short_txt_mv":
						case "journal_title_txt_mv":
							inf[i].setPublishedIn(eElement.getTextContent(), attribute);
							break;
						default:
							break;
						}
					}
					nNode = nNode.getNextSibling();
				}
				inf[i].normalize();
				con.insertDocument(inf[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		readXML rxml = new readXML();
		
		//create the maps for language mapping, type mapping and type resolving
		rxml.typeMap = rxml.createMap(rxml.type_config, rxml.typeMap, "original_type");
		rxml.languageMap = rxml.createMap(rxml.language_config, rxml.languageMap, "original_lan");
		rxml.typeResolveMap = rxml.createResolveMap(rxml.type_resolved_config, rxml.typeResolveMap);
		
		//walk through every file in a folder and call processXML on it
		try {
			Files.walk(Paths.get(args[0]))
					.filter((p) -> !p.toFile().isDirectory() && p.toFile().getAbsolutePath().endsWith(".xml"))
					.forEach(p -> rxml.processXML(p));
			//rxml.processXML(Paths.get("/home/mrdlib/data/fes/solr-export-1.xml"));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}