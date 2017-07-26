package org.mrdlib.recommendation.algorithm;

import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.database.DBConnection;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.api.manager.Constants;
import org.mrdlib.recommendation.framework.WebServiceConnection;

public class Doc2VecRecommender extends RelatedDocuments {

    private DBConnection con;
    private WebServiceConnection service;
    private Constants constants;
    private static final String language = "en";
    private static final int limit = 10;
    
    public Doc2VecRecommender(DBConnection con) {
        con = con;
	constants = new Constants();
        algorithmLoggingInfo = new AlgorithmDetails("Doc2VecRecommender", "cbf", true, "abstract", "doc2vec pretrained, word vectors locked", "d=50,i=20");
	service = new WebServiceConnection(constants.getDoc2VecSearchRoute(), constants.getDoc2VecDocumentRoute(), con);
    }


    @Override
    /**
     * returns mostPopular documents from database
     */
    public DocumentSet getRelatedDocumentSet(DocumentSet requestDocSet) throws Exception {
	return service.getRelatedDocumentSetByDocument(requestDocSet);
    }

}
