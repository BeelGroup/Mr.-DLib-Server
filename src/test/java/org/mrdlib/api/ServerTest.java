package org.mrdlib.api;

import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import java.net.URLEncoder;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.CloseableHttpResponse;
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
	private CloseableHttpClient http;
	private List<String> documentIds, searchQueries, originalIds;
	private Logger logger;

	private static final int TEST_QUERIES = 10;

	@Parameter(0)
	public DisplayDocument doc;
	@Parameter(1)
	public String query;
	@Parameter(2)
	public String algorithm;
	@Parameter(3)
	public boolean byTitle;

	@Parameters(name="{index}: /documents/{1}/related_documents?algorithm_id={2}")
	public static Collection<Object[]> data() throws Exception {
		// test by querying a set of random documents in three different ways
		// let test access queried document to account for restrictions of algorithms
		List<Object[]> options = new ArrayList<Object[]>();
		DBConnection con = new DBConnection("jar");
		List<DisplayDocument> docs = con.getRandomDocuments(TEST_QUERIES);
		for (DisplayDocument d : docs) {
			for (Algorithm a : Algorithm.values()) {
				options.add(new Object[] { d, d.getOriginalDocumentId(), a.name(), false });
				options.add(new Object[] { d, d.getDocumentId(), a.name(), false });
				options.add(new Object[] { d, d.getTitle(), a.name(), true });
			}
			// null = don't specify algorithm_id
			options.add(new Object[] { d, d.getOriginalDocumentId(), null, false });
			options.add(new Object[] { d, d.getDocumentId(), null, false });
			options.add(new Object[] { d, d.getTitle(), null, true });
			// foo - invalid algorithm_name
			options.add(new Object[] { d, d.getOriginalDocumentId(), "foo", false });
			options.add(new Object[] { d, d.getDocumentId(), "foo", false });
			options.add(new Object[] { d, d.getTitle(), "foo", true});
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

	private void testXml(RootElement result) throws Exception {
		assertNotNull(result);
		StatusReportSet status = result.getStatusReportSet();
		assertNotNull(status);
		assertEquals(1, status.getSize());
		List<StatusReport> reports = status.getStatusReportList();
		assertNotNull(reports);
		StatusReport report = reports.get(0);
		assertNotNull(report);
	}

	private void testStatus(RootElement result) throws Exception {
		StatusReport report = result.getStatusReportSet().getStatusReportList().get(0);
		assertThat(report.getStatusCode(), anyOf(equalTo(200), equalTo(204)));
		if (report.getStatusCode() == 200) {
			DocumentSet documents = result.getDocumentSet();
			assertNotNull(documents);
			assertThat(documents.getSize(), greaterThan(0));
		}
	}

	private RootElement fetchRootElement(String query, String algo) throws Exception {
		String q = URLEncoder.encode(query);
		URIBuilder url = new URIBuilder()
			.setScheme("http")
			.setHost("localhost")
			.setPort(9000)
			.setPath("/mdl-server/documents/" + q + "/related_documents");
		if (algo != null)
			url = url.addParameter("algorithm_name", algo);
		HttpGet get = new HttpGet(url.build());
		CloseableHttpResponse res = http.execute(get);
		JAXBContext jaxbContext = JAXBContext.newInstance(RootElement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		InputStream is = res.getEntity().getContent();
		RootElement result = (RootElement) jaxbUnmarshaller.unmarshal(is);
	    StringWriter responseContent = new StringWriter();
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.marshal(result, responseContent);
		is.close();
		res.close();
		logger.info("Got response to query {}: {}", url, responseContent);
		return result;
	}

	private String getAlgorithmClassName(String algo, boolean byTitle) throws Exception {
		Algorithm algorithm = Algorithm.parse(algo);
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
			if (byTitle)
				return "RelatedDocumentsFromSolrByQuery";
			else
				return "RelatedDocumentsFromSolr";
		case FROM_SOLR_WITH_KEYPHRASES:
			return "RelatedDocumentsFromSolrWithKeyphrases";
		case DOC2VEC:
			return "Doc2VecRecommender";
		default:
			throw new Exception("Unknown algorithm: " + algorithm);
		}
	}

	@Test
	public void requestRecommendation() throws Exception {
		logger.info("Querying for {} with algorithm_name = {}", query, algorithm);
		
		RootElement result = fetchRootElement(query,algorithm);
		if ("foo".equals(algorithm)) {
			testXml(result);
			List<StatusReport> status = result.getStatusReportSet().getStatusReportList();
			assertThat(status, hasItem(hasProperty("statusCode", equalTo(400))));
		} else {
			if (algorithm != null) {
				Algorithm algo = Algorithm.parse(algorithm);
				StatusReport status = result.getStatusReportSet().getStatusReportList().get(0);
				if (byTitle == true && !algo.hasTitleSearch()) {
					assertThat(status, hasProperty("statusCode", equalTo(400)));
				} else if (!algo.hasLanguageSupport(doc.getLanguageDetected())) {
					// TODO properly check for fulfilled conditions
					assertThat(status, hasProperty("statusCode", anyOf(equalTo(204), equalTo(200))));
				} else {
					testStatus(result);
					if (result.getDocumentSet() != null) {
						DebugDetailsPerSet details = result.getDocumentSet().getDebugDetailsPerSet();
						if (details != null)
							assertThat(details, hasProperty("algoDetails", hasProperty("name", equalTo(getAlgorithmClassName(algorithm, byTitle)))));
					}
				}
			} else {
				testXml(result);
				testStatus(result);
			}
		}
	}

}
