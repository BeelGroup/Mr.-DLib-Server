package org.mrdlib.partnerContentManager.mediatum;

import java.util.List;

/**
 * Interface specifying methods for storing converted partner's content in the MDL database.
 * 
 * In implementing classes, introduce fields for necessary parameters of content downloading and populate them in a constructor.
 * 
 * The ContentStorer is the third of three parts of the Download-Convert-Store mechanism used for persisting partner's content.
 * 
 * @author wuestehube
 *
 * @param <T> intermediate data format partner content has been converted to
 */
public interface IContentStorer<T> {

	/**
	 * Stores the list of records in the intermediate data format <T>, possibly converted from the partner's content format by the ContentConverter, in the MDL database.
	 * 
	 * @param storableContent content in intermediate data format <T> to store in the MDL database
	 */
	public void store(List<T> storableContent);
	
}
