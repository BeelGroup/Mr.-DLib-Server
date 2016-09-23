/**
 * 
 */
package org.mrdlib.recommendation;

import org.mrdlib.display.DisplayDocument;
import org.mrdlib.display.DocumentSet;

/**
 * @author sid
 *
 */
public interface RelatedDocumentGenerator {
	/**Method to get related documents similar to a given input document. Agnostic of the approach used
	 * 
	 * @param requestDoc The DisplayDocument object that holds the details of the documents
	 * @return A set of related Documents
	 * @throws Exception 
	 */
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc) throws Exception;
	
	/**Method to get a chosen number of related documents similar to a given input document. Agnostic of the approach used
	 * 
	 * @param requestDoc The DisplayDocument object that holds the details of the documents
	 * @param numberOfRelatedDocs number of documents to be contained in the DocumentSet object
	 * @return A set of numberOfRelatedDocs DisplayDocuments
	 */
	public DocumentSet getRelatedDocumentSet(DisplayDocument requestDoc, int numberOfRelatedDocs) throws Exception;
}
