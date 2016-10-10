package org.mrdlib.ranking;

import java.util.Random;

import org.mrdlib.Constants;
import org.mrdlib.DocumentData;
import org.mrdlib.UnknownException;
import org.mrdlib.database.DBConnection;
import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.DocumentSet;
import org.mrdlib.display.StatusReportSet;
import org.mrdlib.solrHandler.solrConnection;

public class ApplyRanking {

	private DBConnection con = null;
	private solrConnection scon = null;
	private Constants constants = null;
	private StatusReportSet statusReportSet = null;

	public ApplyRanking(DBConnection con) {
		constants = new Constants();
		this.con = con;

		try {
			scon = new solrConnection(con);
		} catch (Exception e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		}
	}

	public DocumentSet selectRandomRanking(DisplayDocument requestDocument) throws Exception {
		Random random = new Random();
		int rndSolrRows = random.nextInt(7)+1;
		int solrRows;
		int rndWeight = random.nextInt(8)+1;
		int rndRank = random.nextInt(4)+1;
		boolean onlySolr = false;
		DocumentSet documentset = null;

		switch (rndSolrRows) {
		case 1:
			solrRows = 10; break;
		case 2:
			solrRows = 20; break;
		case 3:
			solrRows = 30; break;
		case 4:
			solrRows = 40; break;
		case 5:
			solrRows = 50; break;
		case 6:
			solrRows = 75; break;
		case 7:
			solrRows = 100; break;
		default:
			solrRows = 200;
		}
		documentset = scon.getRelatedDocumentSetByDocument(requestDocument, solrRows);

		scon.close();
		
		if (documentset.getSize() < solrRows)
			solrRows = getNextTinierSolrRows(solrRows);
		documentset.setNumberOfSolrRows(solrRows);

		documentset.setDocumentList(documentset.getDocumentList().subList(0, solrRows - 1));

		switch (rndRank) {
		case 1:
			documentset = getReadershipCountMendeley(documentset); break;
		case 2:
			documentset = getReadershipNormalizedByAgeMendeley(documentset); break;
		case 3:
			documentset = getReadershipNormalizedByNumberOfAuthors(documentset); break;
		case 4:
			documentset = getSolr(documentset); onlySolr=true; rndWeight = random.nextInt(2)+1; break;
		default:
			documentset = getSolr(documentset); onlySolr=true; rndWeight = random.nextInt(2)+1; break;
		}
		
		documentset.calculatePercentageRankingValue();
		switch (rndWeight) {
		case 1:
			documentset.sortAscForRankingValue(onlySolr); break;
		case 2:
			documentset.sortDescForRankingValue(onlySolr); break;
		case 3:
			documentset.sortDescForLogRankingValueTimesSolrScore(); break;
		case 4:
			documentset.sortDescForRootRankingValueTimesSolrScore(); break;
		case 5:
			documentset.sortDescForRankingValueTimesSolrScore(); break;
		case 6:
			documentset.sortAscForRankingValueTimesSolrScore(); break;
		case 7:
			documentset.sortAscForLogRankingValueTimesSolrScore(); break;
		case 8:
			documentset.sortAscForRootRankingValueTimesSolrScore(); break;
		default:
			documentset.sortDescForRankingValue(onlySolr); break;
		}
		if(documentset.getSize() > 10)
			documentset.setDocumentList(documentset.getDocumentList().subList(0, 10));
		
		return documentset.refreshRankBoth();
	}

	private int getNextTinierSolrRows(int solrRows) {

		if (solrRows < 20)
			solrRows = 10;
		else if (solrRows < 30)
			solrRows = 20;
		else if (solrRows < 40)
			solrRows = 30;
		else if (solrRows < 50)
			solrRows = 40;
		else if (solrRows < 75)
			solrRows = 50;
		else if (solrRows < 100)
			solrRows = 75;
		else if (solrRows < 200)
			solrRows = 100;

		return solrRows;
	}

	public DocumentSet getReadershipCountMendeley(DocumentSet documentset) {
		DisplayDocument current = null;
		DocumentData temp = new DocumentData();

		for (int i = 0; i < documentset.getSize(); i++) {
			current = documentset.getDocumentList().get(i);
			temp = con.getRankingValue(current.getDocumentId(), "simple_count", "readers", "mendeley");
			current.setRankingValue(temp.getRankingValue());
			current.setBibId(temp.getBibId());
		}
		return documentset;
	}

	public DocumentSet getReadershipNormalizedByAgeMendeley(DocumentSet documentset) {
		DisplayDocument current = null;
		DocumentData temp = new DocumentData();

		for (int i = 0; i < documentset.getSize(); i++) {
			current = documentset.getDocumentList().get(i);
			temp = con.getRankingValue(current.getDocumentId(), "normalizedByAge", "readers", "mendeley");
			current.setRankingValue(temp.getRankingValue());
			current.setBibId(temp.getBibId());
		}
		return documentset;
	}

	public DocumentSet getReadershipNormalizedByNumberOfAuthors(DocumentSet documentset) {
		DisplayDocument current = null;
		DocumentData temp = new DocumentData();

		for (int i = 0; i < documentset.getSize(); i++) {
			current = documentset.getDocumentList().get(i);
			temp = con.getRankingValue(current.getDocumentId(), "normalizedByNumberOfAuthors", "readers", "mendeley");
			current.setRankingValue(temp.getRankingValue());
			current.setBibId(temp.getBibId());
		}
		return documentset;
	}

	public DocumentSet getSolr(DocumentSet documentset) {
		DisplayDocument current = null;

		for (int i = 0; i < documentset.getSize(); i++) {
			current = documentset.getDocumentList().get(i);
			current.setRankingValue(current.getSolrScore());
		}
		return documentset;
	}
}
