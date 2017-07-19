package org.mrdlib.recommendation.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.database.NoEntryException;
import org.mrdlib.partnerContentManager.core.SimilarDocument;
import org.mrdlib.recommendation.framework.NoRelatedDocumentsException;
import org.mrdlib.recommendation.framework.core.SimilarArticleConnection;
public class CoreRecommender extends RelatedDocuments {
	DBConnection con = null;
	SimilarArticleConnection sacon = null;
	
	public CoreRecommender(DBConnection con){
		this.con = con;
		sacon = new SimilarArticleConnection();
		
		algorithmLoggingInfo = new AlgorithmDetails("RelatedDocumentsFromCore", "cbf", false);
	}
	
	@Override
	public DocumentSet getRelatedDocumentSet(DocumentSet requestDocSet) throws Exception {
		requestDocSet.setAlgorithmDetails(algorithmLoggingInfo);

		DisplayDocument requestDocument = requestDocSet.getRequestedDocument();
		String originalDocumentId = requestDocument.getOriginalDocumentId();
		String documentIdInCore = originalDocumentId.split("-")[1];
		String json = sacon.getSimilarArticles(documentIdInCore);
		List<SimilarDocument> docs = sacon.parseJSONFromGetArticle(json);
		DocumentSet returnable = sacon.convertToMDLSet(docs, requestDocSet);
		if(returnable.getSize()==0) throw new NoRelatedDocumentsException(documentIdInCore, requestDocument.getDocumentId());
		return mergeWithMDLDatabase(returnable);
	}
	
	private DocumentSet mergeWithMDLDatabase(DocumentSet coreDocumentSet){
		List<DisplayDocument> docList = new ArrayList<DisplayDocument>();
		for(int i = 0; i<coreDocumentSet.getSize(); i++){
			try{
				DisplayDocument docFromDatabase  = con.getDocumentBy("id_original", coreDocumentSet.getDisplayDocument(i).getOriginalDocumentId());
				docFromDatabase.setFallbackUrl("https://core.ac.uk/display/".concat(docFromDatabase.getOriginalDocumentId().split("-")[1]));
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
			CoreRecommender cr = new CoreRecommender(newcon);
			DisplayDocument request = new DisplayDocument("On the occurence of bats Chiroptera in South Tyrol Vespertilionidae","21000000","core-14512070");
			DocumentSet requestSet = new DocumentSet();
			requestSet.setRequestedDocument(request);
			DocumentSet returnedSet = cr.getRelatedDocumentSet(requestSet);
			for(DisplayDocument doc : returnedSet.getDocumentList()){
				try{
					System.out.println(newcon.getDocumentBy("id_original", doc.getOriginalDocumentId()).getDocumentId() + ", " + doc.getOriginalDocumentId());
					System.out.println(doc.getYear() + ", " + doc.getOriginalDocumentId() + ", " + doc.getTitle());
				}catch(NoEntryException e){
					System.out.println(doc.getOriginalDocumentId());
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

}
