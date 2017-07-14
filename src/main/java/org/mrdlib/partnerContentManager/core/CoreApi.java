package org.mrdlib.partnerContentManager.core;

import org.mrdlib.api.manager.Constants;
import org.mrdlib.partnerContentManager.core.model.*;
import org.mrdlib.partnerContentManager.general.QuotaReachedException;

import java.util.List;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpException;
import org.apache.http.client.utils.URIBuilder;

import com.owlike.genson.Genson;

public class CoreApi {


    private static final String endpoint = "https://core.ac.uk/api-v2/";
    private static final String articleBatchPath = "articles/get";
    private static final String articleSearchPath = "articles/search";
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MAX_BATCH_SIZE = 90; // test fails for 100 for some reason
    public static final int QUOTA_TIME_SEARCH = 10 * 1000;
    public static final int QUOTA_TIME_GET = 10 * 1000;

    private String apiKey;
    private HttpClient http;
    private Genson json;

    public CoreApi() {
        apiKey = new Constants().getCoreAPIKey();
	http = HttpClients.createDefault();
	json = new Genson();
    }

    private HttpEntity doRequest(String path, String body, boolean metadata, boolean fulltext, boolean citations, boolean similar, boolean duplicate, boolean urls, boolean faithfulMetadata) throws Exception {

	URIBuilder url = new URIBuilder(endpoint + path);
	// don't write when equal to default value
	if (!metadata)
	    url.addParameter("metadata", String.valueOf(metadata));
	if (fulltext)
	    url.addParameter("fulltext", String.valueOf(fulltext));
	if (citations)
	    url.addParameter("citations", String.valueOf(citations));
	if (similar)
	    url.addParameter("similar", String.valueOf(similar));
	if (duplicate)
	    url.addParameter("duplicate", String.valueOf(duplicate));
	if (urls)
	    url.addParameter("urls", String.valueOf(urls));
	if (faithfulMetadata)
	    url.addParameter("faithfulMetadata", String.valueOf(faithfulMetadata));
	url.addParameter("apiKey", apiKey);

	HttpPost post = new HttpPost(url.toString());
	post.setEntity(new StringEntity(body, "UTF-8"));
	HttpResponse res = http.execute(post);

	int code = res.getStatusLine().getStatusCode();
	if (code != 200)
	    throw new HttpException("Error while making request: HTTP Status " + code);
	HttpEntity entity = res.getEntity();
	
	return entity;
    }


    /**
     * Query for article information from core API.
     * @param ids - Article IDs as used by CORE
     * @param all others: see core api documentation (https://core.ac.uk/api-v2/docs)
     * @return list of queried articles, or null if not found
     * @throws on http codes != 200, status codes != OK/NOT_FOUND, json error, ...
     */
    public List<Article> getArticles(List<Integer> ids, boolean metadata, boolean fulltext, boolean citations, boolean similar, boolean duplicate, boolean urls, boolean faithfulMetadata) throws Exception {

	HttpEntity entity = doRequest(articleBatchPath, json.serialize(ids),
	    metadata, fulltext, citations, similar, duplicate, urls, faithfulMetadata);

	ArticleResponse[] responses = json.deserialize(entity.getContent(), ArticleResponse[].class);

	List<Article> articles = new ArrayList<Article>(responses.length);
	for (ArticleResponse response : responses) {
	    String status = response.getStatus();
	    if (status.equals(ArticleResponse.OK))
		articles.add(response.getData());
	    else if (status.equals(ArticleResponse.NOT_FOUND))
		articles.add(null);
	    else
		// TODO deal with other responses
		throw new Exception("Error response from request: " + response.getStatus());
	}
	return articles;
    }
    
    public List<Article> getArticles(List<Integer> ids) throws Exception {
	return getArticles(ids, true, false, false, false, false, false, false);
    }

    public Collection<Article> listArticles(int year, long offset, long limit) throws Exception {
	return listArticles(year, offset, limit, true, false, false, false, false, false, false);
    }


    /**
     * fetch articles from CORE API; no complete listing available, so query chronologically by year, page through all articles
     * @param year: year needed for query
     * @param offset: for paging/recursion: how many full pages (= MAX_PAGE_SIZE * offset articles) to skip; should be almost always zero when calling externally
   
     * @param limit: how many articles to fetch at most; 
     * @param rest: as in getArticles
     * @return: fetched articles
     */
    public Collection<Article> listArticles(int year, long offset, long limit, boolean metadata, boolean fulltext, boolean citations, boolean similar, boolean duplicate, boolean urls, boolean faithfulMetadata) throws Exception {

	// building queries with paging, going chronologically through all years
	// no listAll in API; alternative: try all IDs
	List<SearchRequest> queries = new ArrayList<SearchRequest>();

	String query = "year:" + year; // TODO: try from, to syntax
	// go through as many pages as possible
	for (long i = 1; (i-1) * MAX_PAGE_SIZE < limit * MAX_PAGE_SIZE && queries.size() < MAX_BATCH_SIZE; i++) {
	    SearchRequest search = new SearchRequest();
	    int pageSize = (int)(i * MAX_PAGE_SIZE < limit ?
				 MAX_PAGE_SIZE : limit - (i-1) * MAX_PAGE_SIZE);
	    if (pageSize > 0)
		queries.add(search
			    .query(query)
			    // last page should 'just fit' our limit
			    .pageSize(pageSize)
			    .page((int) (i + offset) ));
	    else
		break;
	} 

	long startTime = System.currentTimeMillis();
	HttpEntity entity = doRequest(articleSearchPath, json.serialize(queries),
	    metadata, fulltext, citations, similar, duplicate, urls, faithfulMetadata);

	ArticleSearchResponse[] responses = json.deserialize(entity.getContent(), ArticleSearchResponse[].class);

	long totalHits = -1; // did we get all?

	// extract articles
	HashMap<Integer, Article> articles = new HashMap<Integer, Article>(responses.length);
	for (ArticleSearchResponse response : responses) {
	    String status = response.getStatus();
	    if (status.equals(ArticleSearchResponse.OK)) {
		totalHits = response.getTotalHits();
		List<Article> matches = response.getData();
		// avoid duplicates
		for (Article a : matches) {
		    articles.put(a.getId(), a);
		}
	    // TODO deal with other responses
	    // especially quota
	    } else if (status.equals(ArticleSearchResponse.TOO_MANY_QUERIES)) {
		throw new QuotaReachedException(QUOTA_TIME_SEARCH);
	    } else {
		throw new Exception("Error response from request: " + response.getStatus());
	    }
	}

	if (totalHits == -1) // nothing found
	    return null;

	// cover the rest, via recursion
	if (articles.size() < totalHits && (limit < 0 || articles.size() < limit)) { // articles left?
	    long newLimit = (limit < 0 ? limit : limit - articles.size());
	    long newOffset = offset + queries.size();
	    Collection<Article> rest = listArticles(year, newOffset, newLimit,
		metadata, fulltext, citations, similar, duplicate, urls, faithfulMetadata);
	    for (Article a : rest) {
		articles.put(a.getId(), a);
	    }
	}

	return articles.values();
    }

    
    
}
