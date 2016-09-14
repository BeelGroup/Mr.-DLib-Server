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
	private int rankingValue;
	private int year;
	private int bibId;
	
	public DocumentData(String title, int id, String originalId) {
		this.title = title;
		this.id = id;
		this.originalId = originalId;
	}
	
	public DocumentData() {}
	
	public int getBibId() {
		return bibId;
	}

	public void setBibId(int bibId) {
		this.bibId = bibId;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getRankingValue() {
		return rankingValue;
	}

	public void setRankingValue(int rankingValue) {
		this.rankingValue = rankingValue;
	}

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

