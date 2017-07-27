
package org.mrdlib.recommendation.algorithm;

import org.mrdlib.api.response.DocumentSet;

/**
 * @author sid
 *
 */
public abstract class RelatedDocuments {
	/**
	 * Class holding necessary information for correct algorithm logging Fields
	 * roughly correspond to columns in recommendation_algorithms table of
	 * database
	 */
	public AlgorithmDetails algorithmLoggingInfo;

	/**
	 * Method to get related documents similar to a given input document.
	 * Agnostic of the approach used
	 * 
	 * @param requestDoc
	 *            The DocumentSet object that holds the details of the requested
	 *            document and if appicable the number of desired
	 *            recommendations for reranking
	 * @return A set of related Documents
	 * @throws Exception
	 *             if noRelatedDocuments are found or SQLException occurs
	 */
	public abstract DocumentSet getRelatedDocumentSet(DocumentSet requestDoc) throws Exception;

	/**
	 * @return the algorithmLoggingInfo
	 */
	public AlgorithmDetails getAlgorithmLoggingInfo() {
		return algorithmLoggingInfo;
	}

}
