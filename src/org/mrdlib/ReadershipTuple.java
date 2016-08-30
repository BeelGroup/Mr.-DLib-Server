package org.mrdlib;
/**
 * @author Millah
 * 
 * This is a helper class for processing the readership information. Might be need in future
 */
public class ReadershipTuple {
	private String name;
	private String count;
	
	
	public ReadershipTuple(String name, String count) {
		this.name = name;
		this.count = count;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
}
