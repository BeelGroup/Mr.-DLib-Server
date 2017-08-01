package org.mrdlib.api;

import java.util.List;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mrdlib.api.manager.DocumentService;
import org.mrdlib.api.response.*;

public class ServerTest {
	private HttpClient http;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.http = HttpClients.createDefault();
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
	}

	private RootElement fetchRootElement(String query) throws Exception {
		URIBuilder url = new URIBuilder()
			.setScheme("http")
			.setHost("localhost")
			.setPort(9000)
			.setPath("/mdl-server/documents/" + query + "/related_documents");
		HttpGet get = new HttpGet(url.build());
		HttpResponse res = http.execute(get);
		JAXBContext jaxbContext = JAXBContext.newInstance(RootElement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (RootElement) jaxbUnmarshaller.unmarshal(res.getEntity().getContent());
	}

	@Test
	public void requestRecommendationByDocumentID() throws Exception {
		RootElement result = fetchRootElement("1");
		testRootElement(result);
	}

	@Test
	public void requestRecommendationByQuery() throws Exception {
		RootElement result = fetchRootElement("digital");
		testRootElement(result);
	}

	@Test
	public void requestRecommendationByPartnerID() throws Exception {
		RootElement result = fetchRootElement("csa-sa-196000531");
		testRootElement(result);
	}
}
