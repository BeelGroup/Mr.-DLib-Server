package org.mrdlib.tools; 						// Why is a class called "Persons" in the package "tools"?

/**
 * 
 * @author Millah
 *
 * a class which wraps the structure of a person (containing the names)
 * 
 */
public class Person {
	String firstname;
	String middlename;
	String surname;
	String unstructured;
	
	
	public Person(String unstructured) {
		this.unstructured = unstructured;
	}
	public Person(String firstname, String surname) {
		this.firstname = firstname;
		this.surname = surname;
	}
	public Person(String firstname, String middlename, String surname) {
		this.firstname = firstname;
		this.middlename = middlename;
		this.surname = surname;
	}
	
	public Person(String firstname, String middlename, String surname, String unstructured) {
		this.firstname = firstname;
		this.middlename = middlename;
		this.surname = surname;
		this.unstructured = unstructured;
	}
	
	public String getUnstructured() {
		return unstructured;
	}
	public void setUnstructured(String unstructured) {
		this.unstructured = unstructured;
	}
	
	//get the whole name as String with "firstname (middlename?) surname" or "unstructured"
	public String getName() {
		if((firstname == null || surname == null) && unstructured != null)
			return unstructured;
		else if (middlename == null)
			return firstname + " " + surname;
		else
			return firstname + " " + middlename + " " + surname;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getMiddlename() {
		return middlename;
	}
	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}

}
