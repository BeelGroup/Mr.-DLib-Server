package org.mrdlib.api;

import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.HttpException;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.mrdlib.api.response.*;
import org.mrdlib.database.DBConnection;
import org.mrdlib.recommendation.algorithm.Algorithm;

@RunWith(Parameterized.class)
public class ServerTest {
	private HttpClient http;
	private List<String> documentIds, searchQueries, originalIds;
	private Logger logger;

	private static final int TEST_QUERIES = 10;

	@Parameter(0)
	public DisplayDocument doc;
	@Parameter(1)
	public String query;
	@Parameter(2)
	public Algorithm algorithm;

	@Parameters(name="{index}: /documents/{1}/related_documents?algorithm_id={2}")
	public static Collection<Object[]> data() throws Exception {
		// test by querying a set of random documents in three different ways
		// let test access queried document to account for restrictions of algorithms
		List<Object[]> options = new ArrayList<Object[]>();
		DBConnection con = new DBConnection("jar");
		List<DisplayDocument> docs = con.getRandomDocuments(TEST_QUERIES);
		for (DisplayDocument d : docs) {
			for (Algorithm a : Algorithm.values()) {
				options.add(new Object[] { d, d.getOriginalDocumentId(), a });
				options.add(new Object[] { d, d.getDocumentId(), a });
				options.add(new Object[] { d, d.getTitle(), a });
			}
			// null = don't specify algorithm_id
			options.add(new Object[] { d, d.getOriginalDocumentId(), null });
			options.add(new Object[] { d, d.getDocumentId(), null });
			options.add(new Object[] { d, d.getTitle(), null });
			// foo - invalid algorithm_name
			options.add(new Object[] { d, d.getOriginalDocumentId(), "foo" });
			options.add(new Object[] { d, d.getDocumentId(), "foo" });
			options.add(new Object[] { d, d.getTitle(), "foo" });
		}
		con.close();
		return options;
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		http = HttpClients.createDefault();
		logger = LoggerFactory.getLogger(ServerTest.class);
	}

	private void testRootElement(RootElement result, boolean ensureStatusOk) throws Exception {
		assertNotNull(result);
		StatusReportSet status = result.getStatusReportSet();
		assertNotNull(status);
		assertEquals(1, status.getSize());
		List<StatusReport> reports = status.getStatusReportList();
		assertNotNull(reports);
		StatusReport report = reports.get(0);
		assertNotNull(report);
		if (ensureStatusOk) {
			assertEquals(200, report.getStatusCode());
			DocumentSet documents = result.getDocumentSet();
			assertNotNull(documents);
			assertThat(documents.getSize(), greaterThan(0));
		}
	}

	private RootElement fetchRootElement(String query, Algorithm algo) throws Exception {
		URIBuilder url = new URIBuilder()
			.setScheme("http")
			.setHost("localhost")
			.setPort(9000)
			.setPath("/mdl-server/documents/" + query + "/related_documents");
		if (algo != null)
			url = url.addParameter("algorithm_name", algo.name());
		HttpGet get = new HttpGet(url.build());
		HttpResponse res = http.execute(get);
		JAXBContext jaxbContext = JAXBContext.newInstance(RootElement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (RootElement) jaxbUnmarshaller.unmarshal(res.getEntity().getContent());
	}

	private String getAlgorithmClassName(Algorithm algorithm) throws Exception {
		switch (algorithm) {
		case RANDOM_DOCUMENT:
			return "RandomDocumentRecommender";
		case RANDOM_LANGUAGE_RESTRICTED:
			return "RandomDocumentRecommenderLanguageRestricted";
		case MOST_POPULAR:
			return "MostPopularRecommender";
		case STEREOTYPE:
			return "StereotypeRecommender";
		case FROM_SOLR:
			return "RelatedDocumentsFromSolr";
		case FROM_SOLR_WITH_KEYPHRASES:
			return "RelatedDocumentsFromSolrWithKeyphrases";
		case DOC2VEC:
			return "Doc2VecRecommender";
		default:
			throw new Exception("Unknown algorithm: " + algorithm);
		}
	}
	private static final List<Algorithm> languageRestrictedAlgorithms =
		Arrays.asList(new Algorithm[] { Algorithm.DOC2VEC, Algorithm.FROM_SOLR_WITH_KEYPHRASES, Algorithm.RANDOM_LANGUAGE_RESTRICTED });
	private static final List<Algorithm> englishRestrictedAlgorithms = 
		Arrays.asList(new Algorithm[] { Algorithm.DOC2VEC, Algorithm.FROM_SOLR_WITH_KEYPHRASES });

	@Test
	public void requestRecommendation() throws Exception {
		logger.debug("Querying for {} with algorithm_name = {}", query, algorithm);
		
		RootElement result = fetchRootElement(query,algorithm);
		if (languageRestrictedAlgorithms.contains(algorithm) && doc.getLanguage() == null ||
			(englishRestrictedAlgorithms.contains(algorithm) && !doc.getLanguage().equals("en"))) {
			testRootElement(result, false);
			List<StatusReport> status = result.getStatusReportSet().getStatusReportList();
			assertThat(status, hasItem(hasProperty("statusCode", anyOf(equalTo(204), equalTo(200)))));
		} else {
			if (algorithm != Algorithm.DOC2VEC && algorithm != Algorithm.FROM_SOLR_WITH_KEYPHRASES)
				testRootElement(result, true);
			else // TODO: check requirements of algorithms further
				testRootElement(result, false);

			if (algorithm != null) {
				String algoName = result
					.getDocumentSet()
					.getDebugDetailsPerSet()
					.getAlgoDetails()
					.getName();
				String expected = getAlgorithmClassName(algorithm);
				assertEquals(expected, algoName);
			}
		}
	}

}
