package org.mrdlib.api;

import java.util.List;
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

@RunWith(Parameterized.class)
public class ServerTest {
	private HttpClient http;
	private List<String> documentIds, searchQueries, originalIds;
	private Logger logger;

	private static final String[] queries = {"1", "digital", "csa-sa-196000531"};
	private static final String[] algorithms = {
		null, "most_popular", "mlt", "random", "stereotype",
		"keyphrases", "random_language"
	};

	@Parameter(0)
	public String query;
	@Parameter(1)
	public String algorithm;

	@Parameters(name="{index}: /documents/{0}/related_documents?algorithm_id={1}")
	public static Collection<Object[]> data() {
		List<Object[]> options = new ArrayList<Object[]>();
		for (String q : queries) {
			for (String a : algorithms) {
				options.add(new Object[] { q, a });
			}
		}
		return options;
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		http = HttpClients.createDefault();
		logger = LoggerFactory.getLogger(ServerTest.class);
	}

	private void testRootElement(RootElement result) throws Exception {
		assertNotNull(result);
		StatusReportSet status = result.getStatusReportSet();
		assertNotNull(status);
		assertEquals(1, status.getSize());
		List<StatusReport> reports = status.getStatusReportList();
		assertNotNull(reports);
		StatusReport report = reports.get(0);
		assertNotNull(report);
		assertEquals(200, report.getStatusCode());
		DocumentSet documents = result.getDocumentSet();
		assertNotNull(documents);
		assertThat(documents.getSize(), greaterThan(0));
	}

	private RootElement fetchRootElement(String query, String algorithmId) throws Exception {
		URIBuilder url = new URIBuilder()
			.setScheme("http")
			.setHost("localhost")
			.setPort(9000)
			.setPath("/mdl-server/documents/" + query + "/related_documents");
		if (algorithmId != null)
			url = url.addParameter("algorithm_id", algorithmId);
		HttpGet get = new HttpGet(url.build());
		HttpResponse res = http.execute(get);
		JAXBContext jaxbContext = JAXBContext.newInstance(RootElement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (RootElement) jaxbUnmarshaller.unmarshal(res.getEntity().getContent());
	}

	private String getAlgorithmClassName(String algorithm) throws Exception {
		switch (algorithm) {
		case "random":
			return "RandomDocumentRecommender";
		case "random_language":
			return "RandomDocumentRecommenderLanguageRestricted";
		case "most_popular":
			return "MostPopularRecommender";
		case "stereotype":
			return "StereotypeRecommender";
		case "mlt":
			return "RelatedDocumentsFromSolr";
		case "keyphrases":
			return "RelatedDocumentsFromSolrWithKeyphrases";
		case "doc2vec":
			return "Doc2VecRecommender";
		default:
			throw new Exception("Unknown algorithm: " + algorithm);
		}
	}

	@Test
	public void requestRecommendation() throws Exception {
		logger.debug("Querying for {} with algorithm_id = {}", query, algorithm);
		RootElement result = fetchRootElement(query,algorithm);
		testRootElement(result);
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
