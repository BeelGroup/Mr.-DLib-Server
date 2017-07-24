public class Doc2VecRecommender extends RelatedDocuments {

    private DBConnection con = null;
    

    /**
     * Creates a new instance of StereotypeRecommender which exposes methods to
     * get stereotype documents from the database
     * 
     * @param con
     *            DBConnection instance, not null, to access database methods
     */
    public Doc2VecRecommender(DBConnection con) {

        this.con = con;
        algorithmLoggingInfo = new AlgorithmDetails("Doc2VecRecommender", "most_popular", true);

    }


    @Override
    /**
     * returns mostPopular documents from database
     */
    public DocumentSet getRelatedDocumentSet(DocumentSet requestDocSet) throws Exception {
        DocumentSet results = new DocumentSet();
        requestDocSet.setAlgorithmDetails(algorithmLoggingInfo);
        try {
            HttpPost post = new HttpPost(url.toString());
            post.setConfig(config);
            post.setEntity(new StringEntity(body, "UTF-8"));
            HttpResponse res = http.execute(post);
	    // now i have ids
	    

        } catch (Exception e) {
            throw e;
        }
        return results;

    }

}
