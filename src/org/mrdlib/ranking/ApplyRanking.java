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
	
	private int rndSolrRows; 
	private int rndWeight;
	private int rndRank;
	private int solrRows;
	private int rndDisplayNumber;

	
	public ApplyRanking(DBConnection con) {
		constants = new Constants();
		this.con = con;
	
		Random random = new Random();
		rndSolrRows = random.nextInt(7)+1;
		rndWeight = random.nextInt(8)+1;
		rndRank = random.nextInt(4)+1;
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
		try {
			scon = new solrConnection(con);
		} catch (Exception e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		}
	}

	public DocumentSet selectRandomRanking(DocumentSet documentSet) throws Exception {
		boolean onlySolr = false;
		Random random = new Random();
		rndDisplayNumber = random.nextInt(15)+1;

		
		//documentset = scon.getRelatedDocumentSetByDocument(requestDocument, solrRows);

		if (documentSet.getSize() < solrRows)
			solrRows = getNextTinierSolrRows(documentSet.getSize());  //CHANGED THIS HERE BECAUSE FOR STEREOTYPE RECOMMENDATIONS, WE 
		//CAN ONLY GET AROUND 60 recommendations maximum
		
		documentSet.setNumberOfSolrRows(solrRows);
		
		if (documentSet.getSize() > solrRows-1)
			documentSet.setDocumentList(documentSet.getDocumentList().subList(0, solrRows - 1));

		switch (rndRank) {
		case 1:
			documentSet = getReadershipCountMendeley(documentSet); break;
		case 2:
			documentSet = getReadershipNormalizedByAgeMendeley(documentSet); break;
		case 3:
			documentSet = getReadershipNormalizedByNumberOfAuthors(documentSet); break;
		case 4:
			documentSet = getSolr(documentSet); onlySolr=true; rndWeight = random.nextInt(2)+1; break;
		default:
			documentSet = getSolr(documentSet); onlySolr=true; rndWeight = random.nextInt(2)+1; break;
		}
		
		documentSet.calculatePercentageRankingValue();
		switch (rndWeight) {
		case 1:
			documentSet.sortAscForRankingValue(onlySolr); break;
		case 2:
			documentSet.sortDescForRankingValue(onlySolr); break;
		case 3:
			documentSet.sortDescForLogRankingValueTimesSolrScore(); break;
		case 4:
			documentSet.sortDescForRootRankingValueTimesSolrScore(); break;
		case 5:
			documentSet.sortDescForRankingValueTimesSolrScore(); break;
		case 6:
			documentSet.sortAscForRankingValueTimesSolrScore(); break;
		case 7:
			documentSet.sortAscForLogRankingValueTimesSolrScore(); break;
		case 8:
			documentSet.sortAscForRootRankingValueTimesSolrScore(); break;
		default:
			documentSet.sortDescForRankingValue(onlySolr); break;
		}
		if(documentSet.getSize() > 6)
			documentSet.setDocumentList(documentSet.getDocumentList().subList(0, rndDisplayNumber));
		
		return documentSet.refreshRankBoth();
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
			solrRows = 200;

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
	
	public int getRndSolrRows() {
		return rndSolrRows;
	}

	public int getRndWeight() {
		return rndWeight;
	}

	public int getRndRank() {
		return rndRank;
	}
	
	public int getSolrRows() {
		return solrRows;
	}

}
