package org.mrdlib.tools;

/**
 * 
 * @author Millah
 *
 *         This class handles a custom key in a map. Since a map is needed which
 *         uses a tuple as a key entry where the order dont matter a custom hash
 *         and equals method is written
 * 
 *
 */
public class Tuple {
	private String first;
	private String second;

	public Tuple() {
	}

	public Tuple(String first, String second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * method which hashes undependent of order of the elements
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = result + ((first == null) ? 0 : first.hashCode() * prime);
		result = result + ((second == null) ? 0 : second.hashCode() * prime);
		return result;
	}
	
	/**
	 *  method which returns true it two tuples have the same two entries undependent of order
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tuple other = (Tuple) obj;
		if (this.first.equals(other.first) && this.second.equals(other.second))
			return true;
		if (this.first.equals(other.second) && this.second.equals(other.first))
			return true;

		return false;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	/**
	 * get a String representation of the Tuple (not use it for comparison, since the order matters!)
	 * @return a String representation of the Tuple
	 */
	public String getTuple() {
		return "(" + this.first + ", " + this.second + ")";
	}
}