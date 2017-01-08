
package org.mrdlib.recommendation.algorithm;

import org.mrdlib.api.response.DisplayDocument;
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
	 *            The DisplayDocument object that holds the details of the
	 *            documents
	 * @return A set of related Documents
	 * @throws Exception
	 *             if noRelatedDocuments are found or SQLException occurs
	 */
	public abstract DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc) throws Exception;

	/**
	 * Method to get a chosen number of related documents similar to a given
	 * input document. Agnostic of the approach used
	 * 
	 * @param requestDoc
	 *            The DisplayDocument object that holds the details of the
	 *            documents
	 * @param numberOfRelatedDocs
	 *            number of documents to be contained in the DocumentSet object
	 * @return A set of numberOfRelatedDocs DisplayDocuments
	 * @throws Exception
	 *             if noRelatedDocuments are found or SQLException occurs
	 */
	public abstract DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs)
			throws Exception;

	/**
	 * @return the algorithmLoggingInfo
	 */
	public AlgorithmDetails getAlgorithmLoggingInfo() {
		return algorithmLoggingInfo;
	}

}
