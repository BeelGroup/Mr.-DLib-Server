package org.mrdlib.partnerContentManager.mediatum;

import java.util.List;

/**
 * Interface specifying methods for converting content of MDL partners to a data format that may be easier persisted in the MDL database.
 * 
 * In implementing classes, introduce fields for necessary parameters of content downloading and populate them in a constructor.
 * 
 * The ContentConverter is the second of three parts of the Download-Convert-Store mechanism used for persisting partner's content.
 * 
 * @author wuestehube
 *
 * @param <T> intermediate data format to convert partner content to
 */
public interface IContentConverter<T> {

	/**
	 * Converts the data in a given file containing from the partner's content format to the format <T> for the purpose of easier persistence in the MDL database.
	 * Possibly the given file contains data of one record.
	 * 
	 * @param pathOfFileToConvert path of file to convert
	 * @return a list of converted records, possibly containing solely one entry
	 */
	public List<T> convertPartnerContentToStorablePartnerContent(String pathOfFileToConvert);
	
}
