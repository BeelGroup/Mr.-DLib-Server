package org.mrdlib.tools;
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

public class readXML {
	//C:\Users\Sophie\workspace\MrDlib\resources
	//"/home/mrdlib"
	protected String configFolder = "/home/mrdlib";
	protected String type_config = "/document_types.xml";
	protected String language_config = "/language.xml";
	public Map<String, String> typeMap = new HashMap<String, String>();
	public Map<String, String> languageMap = new HashMap<String, String>();
	private DBConnection con = new DBConnection();
	//private static Set<String> types = new HashSet<String>();
	
	public readXML() {}
	
	public Map<String, String> createMap(String path, Map<String, String> map, String nodeText) {
		try {
			File inputFile = new File(configFolder.concat(path).toString());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			NodeList originalTypeNodes = doc.getElementsByTagName(nodeText);
			for (int i = 0; i < originalTypeNodes.getLength(); i++) {
				map.put(originalTypeNodes.item(i).getAttributes().getNamedItem("name").getNodeValue().toLowerCase(), 
						originalTypeNodes.item(i).getParentNode().getAttributes().getNamedItem("name").getNodeValue().toLowerCase());
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public void processXML(Path path) {
		try {
			File inputFile = new File(path.toString());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			NodeList docList = doc.getElementsByTagName("doc");

			XMLDocument[] inf = new XMLDocument[docList.getLength()];

			for (int i = 0; i < docList.getLength(); i++) {
				inf[i] = new XMLDocument(typeMap, languageMap);
				inf[i].setDocumentPath(path.toString());

				Node nNode = docList.item(i).getFirstChild();
				while (nNode.getNextSibling() != null) {
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						String attribute = eElement.getAttribute("name");
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
						case "person_other_txtP_mv":
						case "person_other_normalized_str_mv":
						case "search_person_txtP_mv":
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
							inf[i].setTypeList(eElement.getTextContent(), 0);
							//types.add(eElement.getTextContent());
							break;
						case "doctype_lit_add_str":
							inf[i].setTypeList(eElement.getTextContent(), 1);
							//types.add(eElement.getTextContent());
							break;
						case "temp_doctypes_orginal_str":
							inf[i].setTypeList(eElement.getTextContent(), 2);
							//types.add(eElement.getTextContent());
							break;
						case "publisher":
							inf[i].setPublishedInList(eElement.getTextContent(), 0);
							break;
						case "Sseries_str_mv ":
							inf[i].setPublishedInList(eElement.getTextContent(), 1);
							break;
						case "Satit_str":
							inf[i].setPublishedInList(eElement.getTextContent(), 2);
							break;
						default:
							break;
						}
					}
					nNode = nNode.getNextSibling();
				}
				inf[i].normalize();
				 /*System.out.println("id: " + inf[i].getId()+"\r\ntitle: " +
				 inf[i].getTitle() + "\r\ncleantitle: "
				 + inf[i].getCleanTitle() + "\r\nlanguage: " +
				 inf[i].getLanguage() + "\r\nyear: "
				 + inf[i].getYear() + "\r\nauthors: " +
				 inf[i].getAuthorsAsString() + "\r\nkeywords: "
				 + inf[i].getKeywordsAsString() + "\r\ntype: " +
				 inf[i].getType());
				 System.out.println("---------------------------------------------");*/
				con.makeQueryOfDocument(inf[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		
		readXML rxml = new readXML();
		
		rxml.typeMap = rxml.createMap(rxml.type_config, rxml.typeMap, "original_type");
		rxml.languageMap = rxml.createMap(rxml.language_config, rxml.languageMap, "original_lan");
		//System.out.println(typeMap.toString());
		try {
			//Files.walk(Paths.get(args[0]))
					//.filter((p) -> !p.toFile().isDirectory() && p.toFile().getAbsolutePath().endsWith(".xml"))
					//.forEach(p -> processXML(p));
			rxml.processXML(Paths.get("/home/mrdlib/data/fes/solr-export-1.xml"));
			/*
			Iterator<String> iterator = types.iterator();
			while (iterator.hasNext()) {
				System.out.println("Type" + iterator.next() + " ");
			}*/
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
}