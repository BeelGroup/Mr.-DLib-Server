package org.mrdlib.tools;

public class Tuple {
	private String first;
	private String second;
	
	public Tuple() {
	}

	public Tuple(String first, String second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = result + ((first == null) ? 0 : first.hashCode()*prime);
		result = result + ((second == null) ? 0 : second.hashCode()*prime);
		return result;
	}

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
		if(this.first.equals(other.second) && this.second.equals(other.first))
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
	
	public String getTuple() {
		return "(" + this.first + ", " + this.second + ")";
	}
}