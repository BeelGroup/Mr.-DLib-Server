package org.mrdlib;

import java.util.List;

public class Readership {
	private List<ReadershipTuple> country;
	private List<ReadershipTuple> status;
	private List<ReadershipTuple> field;

	private int readership;

	public Readership(int readership) {
		this.readership = readership;
	}

	public List<ReadershipTuple> getCountry() {
		return country;
	}

	public void setCountry(List<ReadershipTuple> country) {
		this.country = country;
	}

	public List<ReadershipTuple> getStatus() {
		return status;
	}

	public void setStatus(List<ReadershipTuple> status) {
		this.status = status;
	}

	public List<ReadershipTuple> getField() {
		return field;
	}

	public void setField(List<ReadershipTuple> field) {
		this.field = field;
	}

	public int getReadership() {
		return readership;
	}

	public void setReadership(int readership) {
		this.readership = readership;
	}
}
