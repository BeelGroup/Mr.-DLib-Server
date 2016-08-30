package org.mrdlib;
/**
 * @author Millah
 * 
 * This is a helper class which stores every information about a document, without the need to displaying it
 */
public class DocumentData {
	
	private String title = null;
	private int id;
	private String originalId = null;
	
	public DocumentData(String title, int id, String originalId) {
		this.title = title;
		this.id = id;
		this.originalId = originalId;
	}
	
	public DocumentData() {}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOriginalId() {
		return originalId;
	}
	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}

}

