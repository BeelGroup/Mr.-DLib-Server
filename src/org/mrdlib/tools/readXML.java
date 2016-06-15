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
	protected String type_resolved_config = "/document_types_resolved.xml";
	protected String language_config = "/language.xml";
	public Map<String, String> typeMap = new HashMap<String, String>();
	public Map<String, String> languageMap = new HashMap<String, String>();
	public Map<Tuple, String> typeResolveMap = new HashMap<Tuple, String>();
	
	private DBConnection con;
	
	public readXML() {
		con = new DBConnection();
	}
	
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
	
	public Map<Tuple, String> createResolveMap(String path, Map<Tuple, String> map, String nodeText) {
		try {
			Document doc = getDocFromPath(configFolder.concat(path));
			NodeList originalTypeNodes = doc.getElementsByTagName(nodeText);
			for (int i = 0; i < originalTypeNodes.getLength(); i++) {
				String rootNode = originalTypeNodes.item(i).getAttributes().getNamedItem("name").getNodeValue().toLowerCase();
				NodeList childNodes = originalTypeNodes.item(i).getChildNodes();
				Tuple tuple = new Tuple(childNodes.item(1).getAttributes().getNamedItem("name").getNodeValue().toLowerCase(),
						childNodes.item(2).getNextSibling().getAttributes().getNamedItem("name").getNodeValue().toLowerCase());
				map.put(tuple, rootNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public Map<String, String> createMap(String path, Map<String, String> map, String nodeText) {
		try {
			Document doc = getDocFromPath(configFolder.concat(path));

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
			Document doc = getDocFromPath(path.toString());

			NodeList docList = doc.getElementsByTagName("doc");

			XMLDocument[] inf = new XMLDocument[docList.getLength()];

			for (int i = 0; i < docList.getLength(); i++) {
				inf[i] = new XMLDocument(typeMap, languageMap, typeResolveMap);
				inf[i].setDocumentPath(path.toString());

				Node nNode = docList.item(i).getFirstChild();
				while (nNode.getNextSibling() != null) {
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						String attribute = eElement.getAttribute("name");
						if(attribute.matches("description_[A-Za-z]+_txt_mv")) {
							int firstPos = attribute.indexOf("_")+1;
							int secondPos = attribute.substring(attribute.indexOf("_")+1).indexOf("_") + firstPos;
							String lan = attribute.substring(firstPos, secondPos);
							inf[i].addAbstract(eElement.getTextContent(), lan);
						}
						
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
		rxml.typeResolveMap = rxml.createResolveMap(rxml.type_resolved_config, rxml.typeResolveMap, "mdl_type");
		
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