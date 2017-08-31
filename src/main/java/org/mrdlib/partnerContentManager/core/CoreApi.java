package org.mrdlib.partnerContentManager.core;

import org.mrdlib.api.manager.Constants;
import org.mrdlib.partnerContentManager.core.model.*;
import org.mrdlib.partnerContentManager.general.QuotaReachedException;

import java.util.List;
import java.util.Calendar;
import java.util.stream.Stream;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.SocketTimeoutException;
import java.io.InputStream;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HttpException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.client.utils.URIBuilder;

import com.owlike.genson.Genson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CoreApi {
	public static class RequestParams {
		public RequestParams() {
			this(true, false, false, false, false, false, false);
		}
		public RequestParams(boolean metadata, boolean fulltext, boolean citations, boolean similar, boolean duplicate, boolean urls, boolean faithfulMetadata) {
			this.metadata = metadata;
			this.fulltext = fulltext;
			this.citations = citations;
			this.similar = similar;
			this.duplicate = duplicate;
			this.urls = urls;
			this.faithfulMetadata = faithfulMetadata;
		}

		public boolean metadata;
		public boolean fulltext;
		public boolean citations;
		public boolean similar;
		public boolean duplicate;
		public boolean urls;
		public boolean faithfulMetadata;
	}

	private static final String endpoint = "https://core.ac.uk/api-v2/";
	private static final String articleBatchPath = "articles/get";
	private static final String articleSearchPath = "articles/search";
    public static final int MAX_SEARCH_PAGE_SIZE = 100;
    public static final int MAX_SEARCH_BATCH_SIZE = 10;
    public static final int MAX_GET_BATCH_SIZE = 100; 
    public static final int QUOTA_TIME_SEARCH = 10 * 1000;
    public static final int QUOTA_TIME_GET = 10 * 1000;
	
	public static final int TIMEOUT_SEARCH = 90*1000;
	public static final int TIMEOUT_GET = 30*1000;
	public static final int RETRIES = 5;
	public static final int RETRY_WAIT = 15 * 1000;


	private Logger logger = LoggerFactory.getLogger(CoreApi.class);
	private String apiKey;
	private CloseableHttpClient http;
	private Genson json;
	private RequestConfig config_get, config_search;

	public CoreApi() {
		apiKey = new Constants().getCoreAPIKey();
		http = HttpClients.createDefault();
		json = new Genson();
		config_search = RequestConfig.custom()
			.setConnectionRequestTimeout(TIMEOUT_SEARCH)
			.setConnectTimeout(TIMEOUT_SEARCH)
			.setSocketTimeout(TIMEOUT_SEARCH)
			.build();
		config_get = RequestConfig.custom()
			.setConnectionRequestTimeout(TIMEOUT_GET)
			.setConnectTimeout(TIMEOUT_GET)
			.setSocketTimeout(TIMEOUT_GET)
			.build();
	}

	private CloseableHttpResponse doRequest(String path, String body, RequestParams params, RequestConfig config) throws Exception {
		return doRequest(path, body, params, config, 0);
	}

	private CloseableHttpResponse doRequest(String path, String body, RequestParams params, RequestConfig config, int retriesLeft) throws Exception {
		URIBuilder url = new URIBuilder(endpoint + path);
		// don't write when equal to default value
		if (!params.metadata)
			url.addParameter("metadata", String.valueOf(params.metadata));
		if (params.fulltext)
			url.addParameter("fulltext", String.valueOf(params.fulltext));
		if (params.citations)
			url.addParameter("citations", String.valueOf(params.citations));
		if (params.similar)
			url.addParameter("similar", String.valueOf(params.similar));
		if (params.duplicate)
			url.addParameter("duplicate", String.valueOf(params.duplicate));
		if (params.urls)
			url.addParameter("urls", String.valueOf(params.urls));
		if (params.faithfulMetadata)
			url.addParameter("faithfulMetadata", String.valueOf(params.faithfulMetadata));
		url.addParameter("apiKey", apiKey);

		HttpPost post = new HttpPost(url.toString());
		post.setConfig(config);
		post.setEntity(new StringEntity(body, "UTF-8"));
		logger.trace("Requesting {} : {}", url.toString(), body);
		CloseableHttpResponse res = null;
		try {
			res = http.execute(post);
			int code = res.getStatusLine().getStatusCode();
			if (code != 200)
				throw new HttpException("Error while making request: HTTP Status " + code + ", caused by request " + post.toString());

			return res;
		} catch (HttpException | SocketTimeoutException | ConnectionPoolTimeoutException e) {
			try {
				if (res != null) res.close();
			} finally {
				System.err.println("Something went wrong:");
				e.printStackTrace();
				System.out.println("Retrying...");
				if (retriesLeft > 0) {
					Thread.sleep(RETRY_WAIT);
					return doRequest(path, body, params, config, retriesLeft - 1);
				} else {
					throw new HttpException("Error while making request: " + e.toString());
				}
			}
		} 
	}


	/**
	 * Query for article information from core API.
	 * @param ids - Article IDs as used by CORE
	 * @param all others: see core api documentation (https://core.ac.uk/api-v2/docs)
	 * @return list of queried articles, or null if not found
	 * @throws on http codes != 200, status codes != OK/NOT_FOUND, json error, ...
	 */
	public List<Article> getArticles(List<Integer> ids, RequestParams params) throws Exception {

		int batches = ids.size() /  MAX_GET_BATCH_SIZE;
		if (ids.size() % MAX_GET_BATCH_SIZE != 0) batches++;
		List<Article> articles = new ArrayList<Article>(ids.size());

		for (int batch = 0; batch < batches; batch++) {

			int from = batch * MAX_GET_BATCH_SIZE;
			int to = (batch+1) * MAX_GET_BATCH_SIZE;
			List<Integer> batchIds = ids.subList(from, Math.min(ids.size(), to));

			CloseableHttpResponse res = doRequest(articleBatchPath, json.serialize(batchIds), params, config_get);
			InputStream content = res.getEntity().getContent();

			ArticleResponse[] responses = json.deserialize(content, ArticleResponse[].class);
			logger.trace("Got {} responses to request.", responses.length);
			content.close();
			res.close();

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
			Thread.sleep(QUOTA_TIME_GET);
		}
		logger.trace("Finished batch of size {}", articles.size());
		return articles;
	}
    
	public List<Article> getArticles(List<Integer> ids) throws Exception {
		return getArticles(ids, new RequestParams());
	}

	public Stream<Article> listArticles(int year) {
		return listArticles(year, new RequestParams());
	}


	class ArticleStream implements Supplier<Article> {
		private long offset;
		private long limit;
		private int year;
		private RequestParams params;
		private int lastYear = Calendar.getInstance().get(Calendar.YEAR);
		private boolean end = false;

		private List<Article> batch;
		private int index = 0;

		public Article get() {
			logger.info("Getting Article");

			if (batch == null) { // load next batch, or end of stream -> return null
				logger.info("No articles loaded; requesting now");
				try {
					batch = listArticles(year, offset, limit, params);
				} catch(Exception e) {
					logger.warn("Could not load articles from CoreAPI", e);
					batch = null;	
				}
				offset += limit; // next page of this year
				index = 0;
				if (batch == null) { // start from 0 at next year
					offset = 0;
					year++;
					if (year <= lastYear) {
						return get();
					} else {
						logger.info("Reached end of articles");
						end = true;
						return null;
					}
				}
			}

			Article next = batch.get(index);
			index++;
			if (index == batch.size()) {
				batch = null; // load next page on next call
			}
			return next;
		}
	}

	/**
	 * helper function to fetch all articles starting from some year
	 * @param startYear list articles starting from this year
	 * @param params params to pass to CORE API
	 * @returns all articles, ordered chronologically
	 */
	public Stream<Article> listArticles(int startYear, RequestParams params) {
		ArticleStream stream = new ArticleStream();
		stream. offset = 0;	
		stream.limit = MAX_SEARCH_BATCH_SIZE * MAX_SEARCH_PAGE_SIZE;
		stream.year = startYear;
		stream.params = params;
		return Stream.generate(stream);
	}

	public List<Article> listArticles(int year, long offset, long limit) throws Exception {
		return listArticles(year, offset, limit, new RequestParams());
	}


	/**
	 * fetch articles from CORE API; no complete listing available, so query chronologically by year, page through all articles
	 * @param year: year needed for query
	 * @param offset: for paging/recursion: how many full pages (= MAX_SEARCH_PAGE_SIZE * offset articles) to skip; should be almost always zero when calling externally
   
	 * @param limit: how many articles to fetch at most; 
	 * @param rest: as in getArticles
	 * @return: fetched articles
	 */
	public List<Article> listArticles(int year, long offset, long limit, RequestParams params) throws Exception {

		// building queries with paging, going chronologically through all years
		// no listAll in API; alternative: try all IDs
		List<SearchRequest> queries = new ArrayList<SearchRequest>();

		String query = "year:" + year; // TODO: try from, to syntax
		// go through as many pages as possible
		for (long i = 1; (i-1) * MAX_SEARCH_PAGE_SIZE < limit * MAX_SEARCH_PAGE_SIZE && queries.size() < MAX_SEARCH_BATCH_SIZE; i++) {
			SearchRequest search = new SearchRequest();
			int pageSize = (int)(i * MAX_SEARCH_PAGE_SIZE < limit ?
					     MAX_SEARCH_PAGE_SIZE : limit - (i-1) * MAX_SEARCH_PAGE_SIZE);
			if (pageSize > 0)
				queries.add(search
					    .query(query)
					    // last page should 'just fit' our limit
					    .pageSize(pageSize)
					    .page((int) (i + offset) ));
			else
				break;
		} 

		logger.info("Requesting {} with {}", articleSearchPath, queries);
		CloseableHttpResponse res = doRequest(articleSearchPath, json.serialize(queries), params, config_search);
		InputStream content = res.getEntity().getContent();

		ArticleSearchResponse[] responses = json.deserialize(content, ArticleSearchResponse[].class);
		content.close();
		res.close();

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
			Thread.sleep(QUOTA_TIME_SEARCH);
			List<Article> rest = listArticles(year, newOffset, newLimit, params);
			for (Article a : rest) {
				articles.put(a.getId(), a);
			}
		}

		return new ArrayList<Article>(articles.values());
	}

    
    
}
