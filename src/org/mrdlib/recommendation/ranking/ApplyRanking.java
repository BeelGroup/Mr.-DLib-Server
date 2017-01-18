package org.mrdlib.recommendation.ranking;

import java.util.Random;

import org.mrdlib.api.manager.Constants;
import org.mrdlib.api.manager.UnknownException;
import org.mrdlib.api.response.DisplayDocument;
import org.mrdlib.api.response.DocumentSet;
import org.mrdlib.api.response.StatusReportSet;
import org.mrdlib.database.DBConnection;
import org.mrdlib.recommendation.framework.solrConnection;

/**
 * @author Millah
 * 
 *         This class executes the different ranking values based on Alt-, and
 *         Bibliometrics.
 *
 */
public class ApplyRanking {

	private DBConnection con = null;
	private solrConnection scon = null;
	private Constants constants = null;
	private StatusReportSet statusReportSet = null;

	private int rndNumberOfCandidatesToReRank;
	private int rndWeight;
	private int rndRank;
	private int numberOfCandidatesToReRank;
	// private int rndDisplayNumber;
	private int rndOrder;
	private int rndShuffling;

	/**
	 * 
	 * initialize random numbers and initialize solr connection
	 * 
	 * @param database
	 *            connection
	 */
	public ApplyRanking(DBConnection con) {
		constants = new Constants();
		this.con = con;

		Random random = new Random();
		// random number for the number of displayed recommendations
		// rndDisplayNumber = random.nextInt(15) + 1;

		// random number for the number of considered results from the algorithm
		rndNumberOfCandidatesToReRank = random.nextInt(7) + 1;

		rndWeight = random.nextInt(5) + 1;
		// random number for the chosen metric
		rndRank = random.nextInt(7) + 1;
		// random number asc or desc sorting
		rndOrder = random.nextInt(10) + 1;

		// choose a number of considered results from the algorithm
		switch (rndNumberOfCandidatesToReRank) {
		case 1:
			numberOfCandidatesToReRank = 10;
			break;
		case 2:
			numberOfCandidatesToReRank = 20;
			break;
		case 3:
			numberOfCandidatesToReRank = 30;
			break;
		case 4:
			numberOfCandidatesToReRank = 40;
			break;
		case 5:
			numberOfCandidatesToReRank = 50;
			break;
		case 6:
			numberOfCandidatesToReRank = 75;
			break;
		case 7:
			numberOfCandidatesToReRank = 100;
			break;
		default:
			numberOfCandidatesToReRank = 200;
		}
		try {
			scon = new solrConnection(con);
		} catch (Exception e) {
			statusReportSet.addStatusReport(new UnknownException(e, constants.getDebugModeOn()).getStatusReport());
		}
	}

	/**
	 * 
	 * reranks the documentset from the algorithm with random parameters
	 * 
	 * @param documentSet
	 *            by algorithm
	 * @return reranked documentSet
	 * @throws Exception
	 */
	public DocumentSet selectRandomRanking(DocumentSet documentSet) throws Exception {
		Random random = new Random();
		int displayNumber = 6;

		rndShuffling = random.nextInt(2) + 1;

		// if (rndDisplayNumber > numberOfCandidatesToReRank) {
		// rndDisplayNumber = random.nextInt(10) + 1;
		// }

		documentSet.setDesiredNumberFromAlgorithm(displayNumber);

		// if the algorithm does not provide enough results, fall back on a pre
		// picked size
		if (documentSet.getSize() < numberOfCandidatesToReRank)
			numberOfCandidatesToReRank = getNextTinierAlgorithmRows(documentSet.getSize());

		// CHANGED THIS HERE BECAUSE FOR STEREOTYPE RECOMMENDATIONS, WE CAN ONLY
		// GET AROUND 60 recommendations maximum

		documentSet.setNumberOfCandidatesToReRank(numberOfCandidatesToReRank);

		// if there are more results than wanted, cut the list
		if (documentSet.getSize() > numberOfCandidatesToReRank)
			documentSet.setDocumentList(documentSet.getDocumentList().subList(0, numberOfCandidatesToReRank));

		documentSet.setRankAfterAlgorithm();

		if (rndWeight <= 4) {
			// choose a ranking metric
			switch (rndRank) {
			case 1:
				documentSet = getAltmetric(documentSet, "simple_count", "readers", "mendeley");
				break;
			case 2:
				documentSet = getAltmetric(documentSet, "simple_count_normalized_by_age_in_years", "readers",
						"mendeley");
				break;
			case 3:
				documentSet = getAltmetric(documentSet, "simple_count_normalized_by_number_of_authors", "readers",
						"mendeley");
				break;
			case 4:
				documentSet = getAltmetric(documentSet, "sum_of_authors", "readers", "mendeley");
				break;
			case 5:
				documentSet = getAltmetric(documentSet, "sum_of_authors_normalized_by_number_of_authors", "readers",
						"mendeley");
				break;
			case 6:
				documentSet = getAltmetric(documentSet, "sum_of_h-index", "readers", "mendeley");
				break;
			case 7:
				documentSet = getAltmetric(documentSet, "h-index_average", "readers", "mendeley");
				break;
			default:
				documentSet = getAltmetric(documentSet, "simple_count", "readers", "mendeley");
				break;
			}
		}

		// choose a proportion of text relevance score and alt/bibliometric
		switch (rndWeight) {
		case 1:
			documentSet.calculateFinalScoreForRelevanceScoreTimesLogBibScore();
			break;
		case 2:
			documentSet.calculateFinalScoreForRelevanceScoreTimesRootBibScore();
			break;
		case 3:
			documentSet.calculateFinalScoreForRelevanceScoreTimesBibScore();
			break;
		case 4:
			documentSet.calculateFinalScoreOnlyBibScore();
			break;
		case 5:
			documentSet.calculateFinalScoreOnlyRelevanceScore();
			break;
		default:
			documentSet.calculateFinalScoreOnlyBibScore();
			break;
		}

		// choose an ordering with 80% Desc, 20% Asc
		if (rndOrder > 8)
			documentSet.sortAscForFinalValue();
		else
			documentSet.sortDescForFinalValue();
		
		documentSet.calculateRankingStatistics(displayNumber);

		// cut the list to the number we want to display
		if (documentSet.getSize() > displayNumber)
			documentSet.setDocumentList(documentSet.getDocumentList().subList(0, displayNumber));

		documentSet.setRankAfterReRanking();
		
		if (rndShuffling == 1) 
			documentSet.shuffle();

		return documentSet;
	}
	

	/**
	 * 
	 * if the algorithm provides not enough results, find biggest enum from
	 * database which fits
	 * 
	 * @param expected
	 *            algorithmRow
	 * @return new tinier algorithmRow
	 */
	private int getNextTinierAlgorithmRows(int algorithmRows) {

		if (algorithmRows < 10)
			return algorithmRows;
		else if (algorithmRows < 20)
			algorithmRows = 10;
		else if (algorithmRows < 30)
			algorithmRows = 20;
		else if (algorithmRows < 40)
			algorithmRows = 30;
		else if (algorithmRows < 50)
			algorithmRows = 40;
		else if (algorithmRows < 75)
			algorithmRows = 50;
		else if (algorithmRows < 100)
			algorithmRows = 75;
		else if (algorithmRows < 200)
			algorithmRows = 200;

		return algorithmRows;
	}

	/**
	 * 
	 * get the specified altmetrics for all documents in a document set
	 * 
	 * @param documentSet
	 * @param metric,
	 *            eg simple_count
	 * @param type,
	 *            eg readership
	 * @param source,
	 *            eg mendeley
	 * @return DocumentSet with attached rankingValues, -1 if no rankingValue
	 * @throws Exception
	 */
	public DocumentSet getAltmetric(DocumentSet documentset, String metric, String type, String source)
			throws Exception {
		DisplayDocument current = null;
		DisplayDocument temp = new DisplayDocument(constants);
		documentset.setBibliometricId(con.getBibId(metric, type, source));
		documentset.setBibliometric(metric);
		documentset.setBibType(type);
		documentset.setBibSource(source);

		for (int i = 0; i < documentset.getSize(); i++) {
			current = documentset.getDisplayDocument(i);
			temp = con.getRankingValue(current.getDocumentId(), documentset.getBibliometricId());
			current.setBibScore(temp.getBibScore());
			current.setBibDocId(temp.getBibDocId());
		}
		return documentset;
	}

	public int getNumberOfCandidatesToReRank() {
		return numberOfCandidatesToReRank;
	}

}
