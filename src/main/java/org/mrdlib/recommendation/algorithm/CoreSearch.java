package org.mrdlib.recommendation.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.database.NoEntryException;
import org.mrdlib.partnerContentManager.core.PostTitleData;
import org.mrdlib.partnerContentManager.core.SimilarDocument;
import org.mrdlib.recommendation.framework.NoRelatedDocumentsException;
import org.mrdlib.recommendation.framework.core.SimilarArticleConnection;
public class CoreSearch extends RelatedDocuments {
	DBConnection con = null;
	SimilarArticleConnection sacon = null;
	
	public CoreSearch(DBConnection con){
		this.con = con;
		sacon = new SimilarArticleConnection();
		
		algorithmLoggingInfo = new AlgorithmDetails("RelatedDocumentsFromCoreByQuery", "cbf", false);
	}
	
	@Override
	public DocumentSet getRelatedDocumentSet(DocumentSet requestDocSet) throws Exception {
		requestDocSet.setAlgorithmDetails(algorithmLoggingInfo);

		DisplayDocument requestDocument = requestDocSet.getRequestedDocument();
		String title = requestDocument.getTitle();
		int multiplicativeFactor = (50/(title.split(" ").length)) +1;
		String duplicatedTitle  = String.join(" ",  Collections.nCopies(multiplicativeFactor, title));
		String json = sacon.searchByTitle(duplicatedTitle);
		List<PostTitleData> docs = sacon.parseJSONFromPostTitle(json);
		DocumentSet returnable = sacon.convertToMDLSet(docs, requestDocSet);
		if(returnable.getSize()==0) throw new NoRelatedDocumentsException(title, title);
		return mergeWithMDLDatabase(returnable);
	}
	
	private DocumentSet mergeWithMDLDatabase(DocumentSet coreDocumentSet){
		List<DisplayDocument> docList = new ArrayList<DisplayDocument>();
		for(int i = 0; i<coreDocumentSet.getSize(); i++){
			try{
				DisplayDocument docFromDatabase  = con.getDocumentBy("id_original", coreDocumentSet.getDisplayDocument(i).getOriginalDocumentId());
				docList.add(docFromDatabase);
			}catch(NoEntryException e){
				DisplayDocument docToAdd = coreDocumentSet.getDisplayDocument(i);
				docToAdd.reCalculateSnippets();
				docList.add(docToAdd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		coreDocumentSet.setDocumentList(docList);
		return coreDocumentSet;
	}
	public static void main(String[] args){
		try {
			DBConnection newcon = new DBConnection("jar");
			CoreSearch cr = new CoreSearch(newcon);
			DisplayDocument request = new DisplayDocument("On the occurence of bats Chiroptera in South Tyrol Vespertilionidae","21000000","core-14512070");
			DocumentSet requestSet = new DocumentSet();
			requestSet.setRequestedDocument(request);
			DocumentSet returnedSet = cr.getRelatedDocumentSet(requestSet);
			for(DisplayDocument doc : returnedSet.getDocumentList()){
				try{
					System.out.println(newcon.getDocumentBy("id_original", doc.getOriginalDocumentId()).getDocumentId() + ", " + doc.getOriginalDocumentId());
					System.out.println(doc.getYear() + ", " + doc.getOriginalDocumentId() + ", " + doc.getTitle());
				}catch(NoEntryException e){
					System.out.println(doc.getOriginalDocumentId() + " : "  + doc.getTitle());
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

}
